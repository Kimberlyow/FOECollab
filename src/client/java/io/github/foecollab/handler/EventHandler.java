package io.github.foecollab.handler;

import io.github.foecollab.FOECollab;
import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.config.TrackerEventConfig;
import io.github.foecollab.util.TextHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class EventHandler {
        private static EventHandler INSTANCE = new EventHandler();
        private final FOEConfig config = FOEConfig.getConfig();

        public Map<WeatherEvent, Long> weatherEvents = new HashMap<>();
        private final Map<GenericEventConfig, Long> genericEvents = new LinkedHashMap<>();
        public long genericEventAlertTime = 0L;
        public long weatherEventAlertTime = 0L;

        public WeatherEvent currentMoon = null;

        private boolean isWitchingHour = false;

        // Fabled tracking
        public String fabledLocation = "";
        public long fabledEventAlertTime = 0L;
        public boolean isFabledActive = false;

        public static EventHandler instance() {
                if (INSTANCE == null) {
                        INSTANCE = new EventHandler();
                }
                return INSTANCE;
        }

        public boolean onReceiveMessage(Text text) {
                processOtherEvents(text);
                processWeatherEvents(text);

                return false; // Don't suppress any messages
        }

        private void processOtherEvents(Text text) {
                String textString = text.getString();
                
                // Fabled event ends
                if (isFabledActive && textString.startsWith("Caught by ")) {
                        isFabledActive = false;
                        FOECollab.LOGGER.info("[FoE] Fabled event ended, caught by {}", textString.substring(10));
                        return;
                }
                
                // Fabled event starts or is active on join
                if ((textString.startsWith("Visit the ") && textString.contains(" to")) || 
                    textString.startsWith("FABLED » A Fabled Event is active at ")) {
                        
                        String locationName;
                        if (textString.startsWith("Visit the ")) {
                                locationName = textString.substring(textString.indexOf("Visit the ") + 10, textString.lastIndexOf(" "));
                        } else {
                                locationName = textString.substring(textString.indexOf("at ") + 3).trim();
                        }
                        
                        // convert chat name to bossbar value, otherwise leave empty (matches any location as failsafe)
                        Constant location = Constant.valueOfTag(locationName);
                        fabledLocation = (location != Constant.DEFAULT) ? location.ID : "";
                        
                        isFabledActive = true;
                        fabledEventAlertTime = System.currentTimeMillis();
                        if (config.eventTracker.otherEventOptions.fabledOptions.useAlertWarningSound) {
                                NotificationSoundHandler.instance().playSoundWarning(
                                                config.eventTracker.otherEventOptions.fabledOptions.alertSoundType,
                                                MinecraftClient.getInstance());
                        }
                        FOECollab.LOGGER.info("[FoE] Fabled event started at {}", fabledLocation.isEmpty() ? "any location" : fabledLocation);
                        return;
                }
        }

        public void onEventTick() {
                TrackerEventConfig.EventTracker.OtherEventOptions.WitchingHourOptions witchingHourOptions = config.eventTracker.otherEventOptions.witchingHourOptions;

                String location = BossBarHandler.instance().currentLocation.ID;
                boolean isAtCypressLake = location.equals(Constant.CYPRESS_LAKE.ID);
                boolean requireCypressLake = !witchingHourOptions.showOutsideCypressLake;
                int offsetHours = witchingHourOptions.alertOffsetHours;

                if (requireCypressLake && !isAtCypressLake && !isWitchingHour) {
                        isWitchingHour = false;
                        return;
                }

                if (!witchingHourOptions.showAlertHUD) {
                        isWitchingHour = false;
                        return;
                }

                if (isAtCypressLake || isWitchingHour || !requireCypressLake) {
                        String time = BossBarHandler.instance().time;
                        Integer parsedHour = extractHour(time) != null ? extractHour(time) : 0;
                        String timeSuffix = BossBarHandler.instance().timeSuffix.toUpperCase();
                        // FOECollab.LOGGER.info("Time suffix: " + timeSuffix);
                        // FOECollab.LOGGER.info("Parsed hour: " + parsedHour);
                        // FOECollab.LOGGER.info("Is witching hour: " + isWitchingHour);

                        // Convert to 24 hour for offset implementation
                        Integer currentHour = normHour(parsedHour, timeSuffix);
                        // Witching Hour runs 12 AM–3 AM (hours 0–2); anchor the window on midnight so
                        // offset 0 starts it at 12 AM. (Was 1 - offset, which skipped midnight and
                        // started at 1 AM.) normHour maps 12 AM→0 and 12 PM→12, so noon is never in range.
                        int startHour = Math.floorMod(-offsetHours, 24);
                        boolean isInRange = currentHour != null
                                        && (startHour <= 3 ? currentHour >= startHour && currentHour < 3
                                                        : currentHour >= startHour || currentHour < 3);
                        if (!isWitchingHour && isInRange == true) {
                                // witching hour
                                isWitchingHour = true;

                                MutableText eventText = TextHelper.concat(
                                                Text.literal("It is now ").formatted(Formatting.WHITE),
                                                Text.literal("Witching Hour").formatted(Formatting.BOLD)
                                                                .withColor(0x990000),
                                                Text.literal("\n").formatted(Formatting.WHITE),
                                                Text.literal("in ").formatted(Formatting.GRAY),
                                                Constant.CYPRESS_LAKE.TAG.copy(),
                                                Text.literal("\n").formatted(Formatting.WHITE),
                                                Text.literal("1 in 300 chance for bigfoot to steal your fish (and leave a gift)")
                                                                .formatted(Formatting.GRAY));
                                boolean playSound = witchingHourOptions.useAlertWarningSound;
                                NotificationSoundHandler.SoundType soundType = playSound
                                                ? witchingHourOptions.alertSoundType
                                                : null;

                                if (witchingHourOptions.showAlertHUD) {
                                        GenericEventConfig eventConfig = new GenericEventConfig(eventText, playSound,
                                                        soundType, witchingHourOptions.alertDismissSeconds);
                                        triggerGenericEvent(eventConfig);
                                }

                                FOECollab.LOGGER.info("Witching hour detected");
                        } else if (!isInRange && isWitchingHour) {
                                isWitchingHour = false;
                                boolean playSound = witchingHourOptions.useAlertWarningSound;
                                NotificationSoundHandler.SoundType soundType = playSound
                                                ? witchingHourOptions.alertSoundType
                                                : null;
                                FOECollab.LOGGER.info("Witching hour ended");
                                // generic event
                                GenericEventConfig eventConfig = new GenericEventConfig(
                                                Text.literal("Witching hour ended").formatted(Formatting.DARK_PURPLE,
                                                                Formatting.ITALIC),
                                                playSound,
                                                soundType, 2);
                                triggerGenericEvent(eventConfig);
                        } else if (!isInRange) {
                                isWitchingHour = false;
                        }
                } else {
                        isWitchingHour = false;
                }
        }

        private Integer extractHour(String time) {
                if (time == null || time.isEmpty()) {
                        return null;
                }

                int colonIndex = time.indexOf(':');
                String hourPart = colonIndex >= 0 ? time.substring(0, colonIndex) : time;
                hourPart = hourPart.trim();

                if (hourPart.isEmpty()) {
                        return null;
                }

                try {
                        return Integer.parseInt(hourPart);
                } catch (NumberFormatException exception) {
                        FOECollab.LOGGER.debug("Unable to parse hour from time string '{}'", time, exception);
                        return null;
                }
        }

        private Integer normHour(Integer parsedHour, String suffix) {
                if (parsedHour == null || suffix == null) {
                        return null;
                }

                int normalized = Math.floorMod(parsedHour, 12);
                String normalizedSuffix = suffix.trim().toUpperCase(Locale.ROOT);

                if (normalizedSuffix.equals("AM")) {
                        return normalized;
                }
                if (normalizedSuffix.equals("PM") && normalized != 12) {
                        return normalized + 12;
                }
                if (normalizedSuffix.equals("PM") && normalized == 12) {
                        return normalized;
                }

                return null;
        }

        public void triggerGenericEvent(GenericEventConfig genericEventConfig) {
                pruneGenericEvents();
                genericEvents.put(genericEventConfig, System.currentTimeMillis());
                genericEventAlertTime = System.currentTimeMillis();

                if (genericEventConfig.shouldPlaySound()) {
                        NotificationSoundHandler.SoundType soundType = genericEventConfig.getSoundType()
                                        .orElse(NotificationSoundHandler.SoundType.BELL);
                        NotificationSoundHandler.instance()
                                        .playSoundWarning(soundType, MinecraftClient.getInstance());
                }
        }

        public List<GenericEventState> getActiveGenericEvents() {
                pruneGenericEvents();
                return genericEvents.entrySet().stream()
                                .map(entry -> new GenericEventState(entry.getKey(), entry.getValue()))
                                .toList();
        }

        public void pruneGenericEvents() {
                long now = System.currentTimeMillis();
                genericEvents.entrySet()
                                .removeIf(entry -> now - entry.getValue() > entry.getKey().getAlertDismissMillis());
        }

        private void processWeatherEvents(Text text) {
                String textString = text.getString();

                WeatherEvent foundEvent = Arrays.stream(WeatherEvent.values())
                                .filter(weatherEvent -> text.getString().contains(weatherEvent.TEXT)).findFirst()
                                .orElse(null);

                if (!config.eventTracker.weatherEventOptions.toggleOptions.rainbow
                                && foundEvent == WeatherEvent.RAINBOW)
                        foundEvent = null;
                if (!config.eventTracker.weatherEventOptions.toggleOptions.thunderstorm
                                && foundEvent == WeatherEvent.THUNDERSTORM)
                        foundEvent = null;
                if (!config.eventTracker.weatherEventOptions.toggleOptions.supercell
                                && foundEvent == WeatherEvent.SUPERCELL)
                        foundEvent = null;
                if (!config.eventTracker.weatherEventOptions.toggleOptions.goldRush
                                && foundEvent == WeatherEvent.GOLD_RUSH)
                        foundEvent = null;
                if (!config.eventTracker.weatherEventOptions.toggleOptions.rain && foundEvent == WeatherEvent.RAIN)
                        foundEvent = null;
                if (!config.eventTracker.weatherEventOptions.toggleOptions.bloomingOasis
                                && foundEvent == WeatherEvent.BLOOMING_OASIS)
                        foundEvent = null;
                if (!config.eventTracker.weatherEventOptions.toggleOptions.fullMoon
                                && foundEvent == WeatherEvent.FULL_MOON)
                        foundEvent = null;
                if (!config.eventTracker.weatherEventOptions.toggleOptions.blueMoon
                                && foundEvent == WeatherEvent.BLUE_MOON)
                        foundEvent = null;
                if (!config.eventTracker.weatherEventOptions.toggleOptions.superMoon
                                && foundEvent == WeatherEvent.SUPER_MOON)
                        foundEvent = null;
                if (!config.eventTracker.weatherEventOptions.toggleOptions.bloodMoon
                                && foundEvent == WeatherEvent.BLOOD_MOON)
                        foundEvent = null;

                // Check moon alerts mute override
                boolean isMoon = foundEvent == WeatherEvent.FULL_MOON || foundEvent == WeatherEvent.BLUE_MOON ||
                                foundEvent == WeatherEvent.SUPER_MOON || foundEvent == WeatherEvent.BLOOD_MOON;

                if (isMoon && !textString.contains("» The ") && !textString.contains(" has set.")) {
                        this.currentMoon = foundEvent;
                        FOECollab.LOGGER.info("Current moon: " + foundEvent.TEXT);
                } else if (isMoon) {
                        this.currentMoon = null;
                        FOECollab.LOGGER.info("Moon set");
                }

                if (config.eventTracker.weatherEventOptions.muteMoonAlerts && isMoon) {
                        foundEvent = null;
                }

                if (foundEvent != null) {
                        weatherEvents.put(foundEvent, System.currentTimeMillis());

                        // Set current moon if it's a moon event
                        if (isMoon) {
                                this.currentMoon = foundEvent;
                        }

                        weatherEventAlertTime = System.currentTimeMillis();
                        if (config.eventTracker.weatherEventOptions.useAlertWarningSound) {
                                NotificationSoundHandler.instance().playSoundWarning(
                                                config.eventTracker.weatherEventOptions.alertSoundType,
                                                MinecraftClient.getInstance());
                        }
                }
        }

        public static class GenericEventConfig {
                private MutableText text;
                private boolean playSound;
                private NotificationSoundHandler.SoundType soundType;
                private int alertDismissSeconds = 15;

                public GenericEventConfig(MutableText text) {
                        this(text, false, null, 15);
                }

                public GenericEventConfig(MutableText text, boolean playSound) {
                        this(text, playSound, null, 15);
                }

                public GenericEventConfig(MutableText text, boolean playSound,
                                @Nullable NotificationSoundHandler.SoundType soundType) {
                        this(text, playSound, soundType, 15);
                }

                public GenericEventConfig(MutableText text, boolean playSound,
                                @Nullable NotificationSoundHandler.SoundType soundType, int alertDismissSeconds) {
                        this.text = text;
                        this.playSound = playSound;
                        this.soundType = soundType;
                        setAlertDismissSeconds(alertDismissSeconds);
                }

                public MutableText getText() {
                        return text;
                }

                public void setText(MutableText text) {
                        this.text = text;
                }

                public boolean shouldPlaySound() {
                        return playSound;
                }

                public void setPlaySound(boolean playSound) {
                        this.playSound = playSound;
                }

                public Optional<NotificationSoundHandler.SoundType> getSoundType() {
                        return Optional.ofNullable(soundType);
                }

                public void setSoundType(@Nullable NotificationSoundHandler.SoundType soundType) {
                        this.soundType = soundType;
                }

                public int getAlertDismissSeconds() {
                        return alertDismissSeconds;
                }

                public void setAlertDismissSeconds(int alertDismissSeconds) {
                        this.alertDismissSeconds = Math.max(1, alertDismissSeconds);
                }

                public long getAlertDismissMillis() {
                        return alertDismissSeconds * 1000L;
                }
        }

        public record GenericEventState(GenericEventConfig config, long timestamp) {
                public long ageMillis(long now) {
                        return now - timestamp;
                }

                public int remainingSeconds(long now) {
                        long elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(ageMillis(now));
                        return config.getAlertDismissSeconds() - (int) elapsedSeconds;
                }
        }

        public enum WeatherEvent {
                RAINBOW("rainbow", "RAINBOW » A Rainbow has appeared!", TextHelper.concat(
                                Text.literal("A "),
                                Text.literal("R").withColor(0xFC5454).formatted(Formatting.BOLD),
                                Text.literal("a").withColor(0xFCA800).formatted(Formatting.BOLD),
                                Text.literal("i").withColor(0xFCFC54).formatted(Formatting.BOLD),
                                Text.literal("n").withColor(0x54FC54).formatted(Formatting.BOLD),
                                Text.literal("b").withColor(0x54FCFC).formatted(Formatting.BOLD),
                                Text.literal("o").withColor(0xFC54FC).formatted(Formatting.BOLD),
                                Text.literal("w ").withColor(0xA800A8).formatted(Formatting.BOLD),
                                Text.literal("has appeared").formatted(Formatting.WHITE)),
                                TextHelper.concat(
                                                Text.literal("+").formatted(Formatting.GRAY),
                                                Text.literal("500 ").formatted(Formatting.WHITE),
                                                Text.literal("Luck").withColor(0x5454fc))),
                THUNDERSTORM("thunderstorm", "STORM » A thunderstorm has formed!", TextHelper.concat(
                                Text.literal("A ").formatted(Formatting.WHITE),
                                Text.literal("Thunderstorm ").formatted(Formatting.BOLD, Formatting.YELLOW),
                                Text.literal("has formed!").formatted(Formatting.WHITE)),
                                TextHelper.concat(
                                                Text.literal("+").formatted(Formatting.GRAY),
                                                Text.literal("100 ").formatted(Formatting.WHITE),
                                                Text.literal("Bite Speed").withColor(0x40f25a))),
                SUPERCELL("supercell", "STORM » A supercell has formed!", TextHelper.concat(
                                Text.literal("A ").formatted(Formatting.WHITE),
                                Text.literal("Supercell ").formatted(Formatting.BOLD).withColor(0xFCE211),
                                Text.literal("has formed!").formatted(Formatting.WHITE)),
                                TextHelper.concat(
                                                Text.literal("+").formatted(Formatting.GRAY),
                                                Text.literal("150 ").formatted(Formatting.WHITE),
                                                Text.literal("Bite Speed").withColor(0x40f25a))),
                GOLD_RUSH("gold_rush", "GOLD RUSH » A Gold Rush has begun!", TextHelper.concat(
                                Text.literal("A ").formatted(Formatting.WHITE),
                                Text.literal("Gold Rush ").formatted(Formatting.BOLD).withColor(0xECD65A),
                                Text.literal("has begun!").formatted(Formatting.WHITE)),
                                TextHelper.concat(
                                                Text.literal("Base Shard Chance → ").formatted(Formatting.WHITE),
                                                Text.literal("1/25").withColor(0xfca800))),
                RAIN("rain", "RAIN » A rain shower has begun!", TextHelper.concat(
                                Text.literal("A ").formatted(Formatting.WHITE),
                                Text.literal("Rain Shower ").formatted(Formatting.BOLD).withColor(0x63B6EC),
                                Text.literal("has begun!").formatted(Formatting.WHITE)),
                                TextHelper.concat(
                                                Text.literal("+").formatted(Formatting.GRAY),
                                                Text.literal("50 ").formatted(Formatting.WHITE),
                                                Text.literal("Bite Speed").withColor(0x40f25a))),
                BLOOMING_OASIS("blooming_oasis", "BLOOMING OASIS » A Blooming Oasis has appeared!", TextHelper.concat(
                                Text.literal("A ").formatted(Formatting.WHITE),
                                Text.literal("Blooming Oasis ").formatted(Formatting.BOLD).withColor(0xFC54FC),
                                Text.literal("has appeared!").formatted(Formatting.WHITE)),
                                TextHelper.concat(
                                                Text.literal("+").formatted(Formatting.GRAY),
                                                Text.literal("100% ").formatted(Formatting.WHITE),
                                                Text.literal("Pet Luck").withColor(0xfa93f1))),
                FULL_MOON("full_moon", "FULL MOON » A Full Moon has risen!", TextHelper.concat(
                                Text.literal("A ").formatted(Formatting.WHITE),
                                Text.literal("Full Moon ").formatted(Formatting.BOLD).withColor(0xFFFFFF),
                                Text.literal("has risen!").formatted(Formatting.WHITE)),
                                TextHelper.concat(
                                                Text.literal("+").formatted(Formatting.GRAY),
                                                Text.literal("120 ").formatted(Formatting.WHITE),
                                                Text.literal("Bite Speed").withColor(0x40f25a))),
                BLUE_MOON("blue_moon", "BLUE MOON » A Blue Moon has risen.", TextHelper.concat(
                                Text.literal("A ").formatted(Formatting.WHITE),
                                Text.literal("Blue Moon ").formatted(Formatting.BOLD).withColor(0x60C2E9),
                                Text.literal("has risen!").formatted(Formatting.WHITE)),
                                TextHelper.concat(
                                                Text.literal("+").formatted(Formatting.GRAY),
                                                Text.literal("120 ").formatted(Formatting.WHITE),
                                                Text.literal("Bite Speed").withColor(0x40f25a),
                                                Text.literal(", ").formatted(Formatting.GRAY),
                                                Text.literal("5x ").formatted(Formatting.WHITE),
                                                Text.literal("XP").withColor(0x73b3e6))),
                SUPER_MOON("super_moon", "SUPER MOON » A Super Moon has risen.", TextHelper.concat(
                                Text.literal("A ").formatted(Formatting.WHITE),
                                Text.literal("Super Moon ").formatted(Formatting.BOLD).withColor(0xB0F9D5),
                                Text.literal("has risen!").formatted(Formatting.WHITE)),
                                TextHelper.concat(
                                                Text.literal("+").formatted(Formatting.GRAY),
                                                Text.literal("120 ").formatted(Formatting.WHITE),
                                                Text.literal("Bite Speed").withColor(0x40f25a),
                                                Text.literal(", ").formatted(Formatting.GRAY),
                                                Text.literal("+").formatted(Formatting.GRAY),
                                                Text.literal("150 ").formatted(Formatting.WHITE),
                                                Text.literal("Reel Speed").withColor(0x72ebfc))),
                BLOOD_MOON("blood_moon", "BLOOD MOON » A Blood Moon has risen!", TextHelper.concat(
                                Text.literal("A ").formatted(Formatting.WHITE),
                                Text.literal("Blood Moon ").formatted(Formatting.BOLD).withColor(0xFF0000),
                                Text.literal("has risen!").formatted(Formatting.WHITE)),
                                TextHelper.concat(
                                                Text.literal("+").formatted(Formatting.GRAY),
                                                Text.literal("120 ").formatted(Formatting.WHITE),
                                                Text.literal("Bite Speed").withColor(0x40f25a)));

                public final String ID;
                public final String TEXT;
                public final Text TAG;
                public final Text DESC;

                WeatherEvent(String id, String text, Text tag, Text desc) {
                        this.ID = id;
                        this.TEXT = text;
                        this.TAG = tag;
                        this.DESC = desc;
                }
        }
}
