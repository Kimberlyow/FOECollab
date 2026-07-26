package io.github.foecollab.handler.screens.hud;

import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.FOMC.LevelColors;
import io.github.foecollab.FOMC.LocationInfo;
import io.github.foecollab.common.HudFont;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.handler.BossBarHandler;
import io.github.foecollab.handler.ScoreboardHandler;
import io.github.foecollab.handler.TabHandler;
import io.github.foecollab.util.LocationNameHelper;
import io.github.foecollab.util.TextHelper;
import io.github.foecollab.util.ThrottledCache;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Objects;

public class BarHudHandler {
    private static BarHudHandler INSTANCE = new BarHudHandler();
    private final FOEConfig config = FOEConfig.getConfig();

    // The bar is rebuilt from scoreboard/bossbar strings; none of that changes per frame.
    private final ThrottledCache<Text> leftTextCache =
            new ThrottledCache<>(200L, () -> HudFont.recolor(this.buildLeftText()));
    private final ThrottledCache<Text> middleTextCache =
            new ThrottledCache<>(200L, () -> HudFont.recolor(this.buildMiddleText()));
    private final ThrottledCache<Text> rightTextCache =
            new ThrottledCache<>(200L, () -> HudFont.recolor(this.buildRightText()));

    public static BarHudHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new BarHudHandler();
        }
        return INSTANCE;
    }

    public Text assembleLeftText() {
        return leftTextCache.get();
    }

    public Text assembleMiddleText() {
        return middleTextCache.get();
    }

    public Text assembleRightText() {
        return rightTextCache.get();
    }

    private Text buildLeftText() {
        float repetitions = 100f / 20f;
        float stringRepetitions = (float) Math.floor(ScoreboardHandler.instance().percentLevel * 100);
        float restRepetitions = 100 - stringRepetitions;

        return TextHelper.concat(
                TabHandler.instance().player,
                Text.literal(" [").formatted(Formatting.DARK_GRAY),
                Text.literal(String.valueOf(ScoreboardHandler.instance().level)).withColor(LevelColors.valueOfLvl(ScoreboardHandler.instance().level).color),
                Text.literal("] ").formatted(Formatting.DARK_GRAY),
                Text.literal(" ".repeat(Math.round(stringRepetitions / repetitions))).formatted(Formatting.GREEN, Formatting.STRIKETHROUGH),
                Text.literal(" ".repeat(Math.round(restRepetitions / repetitions))).formatted(Formatting.DARK_GRAY, Formatting.STRIKETHROUGH),
                Text.literal(" (").formatted(Formatting.DARK_GRAY),
                Text.literal(TextHelper.fmt(ScoreboardHandler.instance().percentLevel * 100)).formatted(Formatting.GRAY),
                Text.literal("%").formatted(Formatting.GRAY),
                Text.literal(")").formatted(Formatting.DARK_GRAY)
        );
    }

    private Text buildMiddleText() {
        Text weather;
        if(BossBarHandler.instance().weather.contains(Constant.SUN.ID)) {
            weather = Text.literal(BossBarHandler.instance().weather).withColor(Constant.SUN.COLOR);
        } else if(BossBarHandler.instance().weather.contains(Constant.RAIN.ID)) {
            weather = Text.literal(BossBarHandler.instance().weather).withColor(Constant.RAIN.COLOR);
        } else if(BossBarHandler.instance().weather.contains(Constant.THUNDERSTORM.ID)) {
            weather = Text.literal(BossBarHandler.instance().weather).withColor(Constant.THUNDERSTORM.COLOR);
        } else if(BossBarHandler.instance().weather.contains(Constant.BLOOMINGOASIS.ID)) {
            weather = Text.literal(BossBarHandler.instance().weather).withColor(Constant.BLOOMINGOASIS.COLOR);
        } else if(BossBarHandler.instance().weather.contains(Constant.FABLEDWEATHER.ID)) {
            weather = Text.literal(BossBarHandler.instance().weather).withColor(Constant.FABLEDWEATHER.COLOR);
        } else if(BossBarHandler.instance().weather.contains(Constant.GOLDRUSH.ID)) {
            weather = Text.literal(BossBarHandler.instance().weather).withColor(Constant.GOLDRUSH.COLOR);
        } else{
            // Moon
            weather = Text.literal(BossBarHandler.instance().weather);
        }
        Text time = Text.empty();

        if(BossBarHandler.instance().currentLocation != Constant.CREW_ISLAND) {
            time = Text.literal(" ").append(weather).append(Text.literal(" ")).append(Text.literal(BossBarHandler.instance().time).formatted(Formatting.WHITE)).append(BossBarHandler.instance().timeSuffix.contains("AM") ? Text.literal("ᴀᴍ").formatted(Formatting.GRAY) : Text.literal("ᴘᴍ").formatted(Formatting.GRAY));
        }

        Text locationCatch = !Objects.equals(ScoreboardHandler.instance().locationMin, ScoreboardHandler.instance().locationMax) && BossBarHandler.instance().currentLocation != Constant.CREW_ISLAND ? TextHelper.concat(
                Text.literal(" (").formatted(Formatting.DARK_GRAY),
                Text.literal(ScoreboardHandler.instance().locationMin).formatted(Formatting.GOLD),
                Text.literal("/").formatted(Formatting.GRAY),
                Text.literal(ScoreboardHandler.instance().locationMax).formatted(Formatting.GOLD),
                Text.literal(")").formatted(Formatting.DARK_GRAY)
        ) : Text.literal("");

        // Add climate information if enabled and not in crew island
        Text climateText = Text.empty();
        if(config.barHUD.showClimate && BossBarHandler.instance().currentLocation != Constant.CREW_ISLAND && BossBarHandler.instance().currentLocation != Constant.UNKNOWN) {
            LocationInfo locationInfo = LocationInfo.valueOfId(BossBarHandler.instance().currentLocation.ID);
            if(locationInfo != LocationInfo.DEFAULT) {
                climateText = TextHelper.concat(
                        Text.literal(" ").formatted(Formatting.DARK_GRAY),
                        Text.literal("[").formatted(Formatting.DARK_GRAY),
                        locationInfo.CLIMATE.TAG,
                        Text.literal("]").formatted(Formatting.DARK_GRAY)
                );
            }
        }

        Text locationName = BossBarHandler.instance().currentLocation != null
                ? BossBarHandler.instance().currentLocation.TAG
                : Text.empty();
        if (config.cleanerDisplay.shortenLocationNames) {
            locationName = LocationNameHelper.shorten(locationName);
        }

        return TextHelper.concat(
                Text.literal("\uF039 ").formatted(Formatting.WHITE),
                locationName,
                locationCatch,
                climateText,
                time
        );
    }

    private Text buildRightText() {
        Text padding = Text.literal("    ");

        return TextHelper.concat(
                Text.literal("ʙᴀʟᴀɴᴄᴇ: ").formatted(Formatting.GRAY),
                Text.literal("$").formatted(Formatting.DARK_GREEN),
                Text.literal(ScoreboardHandler.instance().wallet).formatted(Formatting.GREEN),
                Text.literal(" \uF00C").formatted(Formatting.WHITE),
                Text.literal(ScoreboardHandler.instance().credits).formatted(Formatting.YELLOW),

                padding,


                Text.literal("ᴄᴀᴛᴄʜᴇѕ: ").formatted(Formatting.GRAY),
                Text.literal(ScoreboardHandler.instance().catches).formatted(Formatting.RED),
                Text.literal(" (").formatted(Formatting.DARK_GRAY),
                Text.literal(ScoreboardHandler.instance().catchRate).formatted(Formatting.GREEN),
                Text.literal(")").formatted(Formatting.DARK_GRAY)

        );
    }
}
