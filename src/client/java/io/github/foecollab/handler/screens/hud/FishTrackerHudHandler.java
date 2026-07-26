package io.github.foecollab.handler.screens.hud;

import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.common.HudFont;
import io.github.foecollab.common.Theming;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.handler.ProfileDataHandler;
import io.github.foecollab.handler.ThemingHandler;
import io.github.foecollab.util.TextHelper;
import io.github.foecollab.util.ThrottledCache;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FishTrackerHudHandler {
    private static FishTrackerHudHandler INSTANCE = new FishTrackerHudHandler();

    // The Fish HUD is the heaviest per-frame text build (dozens of Text allocations + map
    // lookups). Throttle the rebuild so framerate doesn't drive the allocation rate. The
    // HudFont recolor deep-copies every line, so it lives inside the cache too.
    private final ThrottledCache<List<Text>> fishTextCache =
            new ThrottledCache<>(200L, () -> HudFont.recolorAll(this.buildFishText()));

    public static FishTrackerHudHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new FishTrackerHudHandler();
        }
        return INSTANCE;
    }

    public List<Text> assembleFishText() {
        return fishTextCache.get();
    }

    private List<Text> buildFishText() {
        FOEConfig config = FOEConfig.getConfig();
        ProfileDataHandler.ProfileData profileData = ProfileDataHandler.instance().profileData;
        List<Text> textList = new ArrayList<>();

        long timeSinceReset = ProfileDataHandler.instance().profileData.activeTime;

        // All-time or not display stat strings
        int displayFishCaughtCount = config.fishTracker.isFishTrackerOnTimer
                ? profileData.fishCaughtCount
                : profileData.allFishCaughtCount;
        float displayTotalXp = config.fishTracker.isFishTrackerOnTimer
                ? profileData.totalXP
                : profileData.allTotalXP;
        float displayTotalValue = config.fishTracker.isFishTrackerOnTimer
                ? profileData.totalValue
                : profileData.allTotalValue;
        int displayPetCaughtCount = config.fishTracker.isFishTrackerOnTimer
                ? profileData.petCaughtCount
                : profileData.allPetCaughtCount;
        int displayShardCaughtCount = config.fishTracker.isFishTrackerOnTimer
                ? profileData.shardCaughtCount
                : profileData.allShardCaughtCount;
        int displayLightningBottleCaughtCount = config.fishTracker.isFishTrackerOnTimer
                ? profileData.lightningBottleCount
                : profileData.allLightningBottleCount;
        int displayInfusionCapsuleCaughtCount = config.fishTracker.isFishTrackerOnTimer
                ? profileData.infusionCapsuleCount
                : profileData.allInfusionCapsuleCount;
        int displayQuestsCompleted = config.fishTracker.isFishTrackerOnTimer
                ? profileData.questsCompleted
                : profileData.allQuestsCompleted;
        int displayPetsFromQuests = config.fishTracker.isFishTrackerOnTimer
                ? profileData.petsFromQuests
                : profileData.allPetsFromQuests;
        int displayShardsFromQuests = config.fishTracker.isFishTrackerOnTimer
                ? profileData.shardsFromQuests
                : profileData.allShardsFromQuests;
        Map<Constant, Integer> displayRarityCounts = config.fishTracker.isFishTrackerOnTimer
                ? profileData.rarityCounts
                : profileData.allRarityCounts;
        Map<Constant, Integer> displayFishSizeCounts = config.fishTracker.isFishTrackerOnTimer
                ? profileData.fishSizeCounts
                : profileData.allFishSizeCounts;
        Map<Constant, Integer> displayVariantCounts = config.fishTracker.isFishTrackerOnTimer
                ? profileData.variantCounts
                : profileData.allVariantCounts;
        int displayCommonCount = displayRarityCounts.getOrDefault(Constant.COMMON, 0);
        int displayRareCount = displayRarityCounts.getOrDefault(Constant.RARE, 0);
        int displayEpicCount = displayRarityCounts.getOrDefault(Constant.EPIC, 0);
        int displayLegendaryCount = displayRarityCounts.getOrDefault(Constant.LEGENDARY, 0);
        int displayMythicalCount = displayRarityCounts.getOrDefault(Constant.MYTHICAL, 0);
        int displayBabyCount = displayFishSizeCounts.getOrDefault(Constant.BABY, 0);
        int displayJuvenileCount = displayFishSizeCounts.getOrDefault(Constant.JUVENILE, 0);
        int displayAdultCount = displayFishSizeCounts.getOrDefault(Constant.ADULT, 0);
        int displayLargeCount = displayFishSizeCounts.getOrDefault(Constant.LARGE, 0);
        int displayGiganticCount = displayFishSizeCounts.getOrDefault(Constant.GIGANTIC, 0);
        int displayAlbinoCount = displayVariantCounts.getOrDefault(Constant.ALBINO, 0);
        int displayMelanisticCount = displayVariantCounts.getOrDefault(Constant.MELANISTIC, 0);
        int displayTrophyCount = displayVariantCounts.getOrDefault(Constant.TROPHY, 0);
        int displayFabledCount = displayVariantCounts.getOrDefault(Constant.FABLED, 0);
        int displaySpookyCount = displayVariantCounts.getOrDefault(Constant.SPOOKY, 0);

        Map<Constant, Integer> displayRarityDryStreak = profileData.rarityDryStreak;
        Map<Constant, Integer> displayFishSizeDryStreak = profileData.fishSizeDryStreak;
        Map<Constant, Integer> displayVariantDryStreak = profileData.variantDryStreak;

        int displayDryStreakCommonCount = displayRarityDryStreak.getOrDefault(Constant.COMMON, 0);
        int displayDryStreakRareCount = displayRarityDryStreak.getOrDefault(Constant.RARE, 0);
        int displayDryStreakEpicCount = displayRarityDryStreak.getOrDefault(Constant.EPIC, 0);
        int displayDryStreakLegendaryCount = displayRarityDryStreak.getOrDefault(Constant.LEGENDARY, 0);
        int displayDryStreakMythicalCount = displayRarityDryStreak.getOrDefault(Constant.MYTHICAL, 0);
        int displayDryStreakBabyCount = displayFishSizeDryStreak.getOrDefault(Constant.BABY, 0);
        int displayDryStreakJuvenileCount = displayFishSizeDryStreak.getOrDefault(Constant.JUVENILE, 0);
        int displayDryStreakAdultCount = displayFishSizeDryStreak.getOrDefault(Constant.ADULT, 0);
        int displayDryStreakLargeCount = displayFishSizeDryStreak.getOrDefault(Constant.LARGE, 0);
        int displayDryStreakGiganticCount = displayFishSizeDryStreak.getOrDefault(Constant.GIGANTIC, 0);
        int displayDryStreakAlbinoCount = displayVariantDryStreak.getOrDefault(Constant.ALBINO, 0);
        int displayDryStreakMelanisticCount = displayVariantDryStreak.getOrDefault(Constant.MELANISTIC, 0);
        int displayDryStreakTrophyCount = displayVariantDryStreak.getOrDefault(Constant.TROPHY, 0);
        int displayDryStreakFabledCount = displayVariantDryStreak.getOrDefault(Constant.FABLED, 0);
        int displayDryStreakSpookyCount = displayVariantDryStreak.getOrDefault(Constant.SPOOKY, 0);

        int displaySpecialCount = displayRarityCounts.getOrDefault(Constant.SPECIAL, 0);
        int displayDryStreakSpecialCount = displayRarityDryStreak.getOrDefault(Constant.SPECIAL, 0);

        int displayAlternateCount = displayVariantCounts.getOrDefault(Constant.ALTERNATE, 0);
        int displayDryStreakAlternateCount = displayVariantDryStreak.getOrDefault(Constant.ALTERNATE, 0);

        int displayFrozenCount = displayVariantCounts.getOrDefault(Constant.FROZEN, 0);
        int displayDryStreakFrozenCount = displayVariantDryStreak.getOrDefault(Constant.FROZEN, 0);

        int displayTimerFishCaughtCount = profileData.timerFishCaughtCount;

        if (!config.fishTracker.hideTitle && ThemingHandler.instance().currentThemeType == Theming.ThemeType.OFF) {
            // The arrow points at the anchored edge; a centered HUD has none, so no arrow.
            switch (config.fishTracker.alignment) {
                case RIGHT -> textList.add(TextHelper.concat(
                        this.getTitle().copy().formatted(Formatting.GRAY),
                        Text.literal(" ◀").formatted(Formatting.GRAY)
                ));
                case LEFT -> textList.add(TextHelper.concat(
                        Text.literal("▶ ").formatted(Formatting.GRAY),
                        this.getTitle().copy().formatted(Formatting.GRAY)
                ));
                case CENTER -> textList.add(this.getTitle().copy().formatted(Formatting.GRAY));
            }
        }

        // Put into Texts if enabled in config
        if (config.fishTracker.fishTrackerToggles.generalToggles.showFishCaught) textList.add(TextHelper.concat(
                    Text.literal("ꜰɪѕʜ ᴄᴀᴜɢʜᴛ: ").formatted(Formatting.GRAY),
                    Text.literal(getNumber(displayFishCaughtCount)).formatted(Formatting.WHITE)
        ));

        if (config.fishTracker.isFishTrackerOnTimer || config.fishTracker.showTimerOnAllTime) {

            if (config.fishTracker.fishTrackerToggles.generalToggles.showFishPerHour) {
                double fishPerHour = (displayTimerFishCaughtCount / (timeSinceReset / 3600000.0));
                textList.add(TextHelper.concat(
                        Text.literal("ꜰɪѕʜ/ʜᴏᴜʀ: ").formatted(Formatting.GRAY),
                        Text.literal(String.format("%.1f", fishPerHour))
                ));
            }

            if (config.fishTracker.fishTrackerToggles.generalToggles.showTimer) {
                long hours = TimeUnit.MILLISECONDS.toHours(timeSinceReset);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(timeSinceReset) % 60;
                long seconds = TimeUnit.MILLISECONDS.toSeconds(timeSinceReset) % 60;
                textList.add(TextHelper.concat(
                        Text.literal("ꜰɪѕʜ ᴛɪᴍᴇ: ").formatted(Formatting.GRAY),
                        Text.literal(String.format("%02d:%02d:%02d", hours, minutes, seconds))
                ));
            }
        }

        if (config.fishTracker.fishTrackerToggles.generalToggles.showTotalXp) textList.add(TextHelper.concat(
                Text.literal("ᴛᴏᴛᴀʟ xᴘ: ").formatted(Formatting.GRAY),
                Text.literal(TextHelper.fmnt(displayTotalXp)).formatted(Formatting.WHITE)
        ));

        if (config.fishTracker.fishTrackerToggles.generalToggles.showTotalValue) textList.add(TextHelper.concat(
                Text.literal("ᴛᴏᴛᴀʟ ᴠᴀʟᴜᴇ: ").formatted(Formatting.GRAY),
                Text.literal("$").formatted(Formatting.WHITE),
                Text.literal(TextHelper.fmnt(displayTotalValue)).formatted(Formatting.WHITE)

        ));

        if(config.fishTracker.fishTrackerToggles.generalToggles.showPetCaught
                || config.fishTracker.fishTrackerToggles.generalToggles.showShardCaught
                || config.fishTracker.fishTrackerToggles.generalToggles.showLightningBottleCaught
                || config.fishTracker.fishTrackerToggles.generalToggles.showInfusionCapsuleCaught
                || config.fishTracker.fishTrackerToggles.generalToggles.showQuestsCompleted
        ) {
            textList.add(Text.empty());

            // Shows Quests completed (Qᴜᴇѕᴛѕ) following the same pattern as existing parts
            if (config.fishTracker.fishTrackerToggles.generalToggles.showQuestsCompleted) {
                textList.add(TextHelper.concat(
                        Text.literal("Qᴜᴇѕᴛѕ: ").formatted(Formatting.GRAY),
                        Text.literal(String.valueOf(displayQuestsCompleted)).formatted(Formatting.WHITE)
                ));
            }

            // Shows Quest Pets/Shards separately if "showQuestPetsAndShardsSeparately" is on
            if(config.fishTracker.fishTrackerToggles.generalToggles.trackPetsAndShardsFromQuests
                    && config.fishTracker.fishTrackerToggles.generalToggles.showQuestPetsAndShardsSeparately) {
                if(displayPetsFromQuests > 0) {
                    textList.add(TextHelper.concat(
                            Text.literal("Qᴜᴇѕᴛ ᴘᴇᴛѕ: ").formatted(Formatting.GRAY),
                            Text.literal(String.valueOf(displayPetsFromQuests)).formatted(Formatting.WHITE)
                    ));
                }
                if(displayShardsFromQuests > 0) {
                    textList.add(TextHelper.concat(
                            Text.literal("Qᴜᴇѕᴛ ѕʜᴀʀᴅѕ: ").formatted(Formatting.GRAY),
                            Text.literal(String.valueOf(displayShardsFromQuests)).formatted(Formatting.WHITE)
                    ));
                }
            }

            // Shows pets, either only caught pets or caught + quest pets (based on "showQuestPetsAndShardsSeparately")
            if (config.fishTracker.fishTrackerToggles.generalToggles.showPetCaught) {
                int displayPets = displayPetCaughtCount;
                boolean separateMode = config.fishTracker.fishTrackerToggles.generalToggles.trackPetsAndShardsFromQuests 
                        && config.fishTracker.fishTrackerToggles.generalToggles.showQuestPetsAndShardsSeparately;
                
                // Combine when not separate
                if (!separateMode && config.fishTracker.fishTrackerToggles.generalToggles.trackPetsAndShardsFromQuests) {
                    displayPets += displayPetsFromQuests;
                }
                
                String petLabel = separateMode ? "Cᴀᴜɢʜᴛ ᴘᴇᴛѕ: " : "ᴘᴇᴛѕ: ";
                textList.add(TextHelper.concat(
                        Text.literal(petLabel).formatted(Formatting.GRAY),
                        Text.literal(String.valueOf(displayPets)).formatted(Formatting.WHITE),
                        config.fishTracker.fishTrackerToggles.dryStreakToggles.showPet
                                ? getDryStreakSuffix(profileData.petDryStreak)
                                : Text.empty()
                ));

                if (config.fishTracker.fishTrackerToggles.generalToggles.showPetPerHour && config.fishTracker.isFishTrackerOnTimer) {
                    double petPerHour = (displayPets / (timeSinceReset / 3600000.0));
                    textList.add(TextHelper.concat(
                            Text.literal("ᴘᴇᴛѕ/ʜᴏᴜʀ: ").formatted(Formatting.GRAY),
                            Text.literal(String.format("%.1f", petPerHour))
                    ));
                }
            }

            // Shows caught/quest shards, also based on "showQuestPetsAndShardsSeparately" toggle
            if (config.fishTracker.fishTrackerToggles.generalToggles.showShardCaught) {
                int displayShards = displayShardCaughtCount;
                boolean separateMode = config.fishTracker.fishTrackerToggles.generalToggles.trackPetsAndShardsFromQuests 
                        && config.fishTracker.fishTrackerToggles.generalToggles.showQuestPetsAndShardsSeparately;
                
                if (!separateMode && config.fishTracker.fishTrackerToggles.generalToggles.trackPetsAndShardsFromQuests) {
                    displayShards += displayShardsFromQuests;
                }
                
                String shardLabel = separateMode ? "Cᴀᴜɢʜᴛ ѕʜᴀʀᴅѕ: " : "ѕʜᴀʀᴅѕ: ";
                textList.add(TextHelper.concat(
                        Text.literal(shardLabel).formatted(Formatting.GRAY),
                        Text.literal(getNumber(displayShards)).formatted(Formatting.WHITE),
                        config.fishTracker.fishTrackerToggles.dryStreakToggles.showShard
                                ? getDryStreakSuffix(profileData.shardDryStreak)
                                : Text.empty()
                ));

                if (config.fishTracker.fishTrackerToggles.generalToggles.showShardPerHour && config.fishTracker.isFishTrackerOnTimer) {
                    double shardPerHour = (displayShards / (timeSinceReset / 3600000.0));
                    textList.add(TextHelper.concat(
                            Text.literal("ѕʜᴀʀᴅѕ/ʜᴏᴜʀ: ").formatted(Formatting.GRAY),
                            Text.literal(String.format("%.1f", shardPerHour))
                    ));
                }
            }

            if(config.fishTracker.fishTrackerToggles.generalToggles.showLightningBottleCaught) {
                textList.add(TextHelper.concat(
                        Text.literal("ʟɪɢʜᴛ. ʙᴏᴛᴛʟᴇѕ: ").formatted(Formatting.GRAY),
                        Text.literal(String.valueOf(displayLightningBottleCaughtCount)).formatted(Formatting.WHITE),
                        config.fishTracker.fishTrackerToggles.dryStreakToggles.showLightningBottle
                                ? getDryStreakSuffix(profileData.lightningBottleDryStreak)
                                : Text.empty()
                ));
            }

            if(config.fishTracker.fishTrackerToggles.generalToggles.showInfusionCapsuleCaught) {
                textList.add(TextHelper.concat(
                        Text.literal("ɪɴꜰᴜѕɪᴏɴ ᴄᴀᴘѕᴜʟᴇѕ: ").formatted(Formatting.GRAY),
                        Text.literal(String.valueOf(displayInfusionCapsuleCaughtCount)).formatted(Formatting.WHITE),
                        config.fishTracker.fishTrackerToggles.dryStreakToggles.showInfusionCapsule
                                ? getDryStreakSuffix(profileData.infusionCapsuleDryStreak)
                                : Text.empty()
                ));
            }
        }



        if (config.fishTracker.fishTrackerToggles.rarityToggles.showRarities) {
            textList.add(Text.empty());

            if (config.fishTracker.fishTrackerToggles.rarityToggles.showCommon) {
                addStatLine(textList, Constant.COMMON.TAG, displayCommonCount, displayFishCaughtCount,
                        config.fishTracker.fishTrackerToggles.dryStreakToggles.showCommon, displayDryStreakCommonCount);
            }
            if (config.fishTracker.fishTrackerToggles.rarityToggles.showRare) {
                addStatLine(textList, Constant.RARE.TAG, displayRareCount, displayFishCaughtCount,
                        config.fishTracker.fishTrackerToggles.dryStreakToggles.showRare, displayDryStreakRareCount);
            }
            if (config.fishTracker.fishTrackerToggles.rarityToggles.showEpic) {
                addStatLine(textList, Constant.EPIC.TAG, displayEpicCount, displayFishCaughtCount,
                        config.fishTracker.fishTrackerToggles.dryStreakToggles.showEpic, displayDryStreakEpicCount);
            }
            if (config.fishTracker.fishTrackerToggles.rarityToggles.showLegendary) {
                addStatLine(textList, Constant.LEGENDARY.TAG, displayLegendaryCount, displayFishCaughtCount,
                        config.fishTracker.fishTrackerToggles.dryStreakToggles.showLegendary, displayDryStreakLegendaryCount);
            }
            if (config.fishTracker.fishTrackerToggles.rarityToggles.showMythical) {
                addStatLine(textList, Constant.MYTHICAL.TAG, displayMythicalCount, displayFishCaughtCount,
                        config.fishTracker.fishTrackerToggles.dryStreakToggles.showMythical, displayDryStreakMythicalCount);
            }

            //EVENT FISHES
            if(config.fishTracker.fishTrackerToggles.fishEventToggles.rarityToggles.showSpecial) {
                addStatLine(textList, Constant.SPECIAL.TAG, displaySpecialCount, displayFishCaughtCount,
                        config.fishTracker.fishTrackerToggles.fishEventToggles.dryStreakToggles.showSpecial, displayDryStreakSpecialCount);
            }
        }

        if (config.fishTracker.fishTrackerToggles.fishSizeToggles.showFishSizes) {
            textList.add(Text.empty());

            if (config.fishTracker.fishTrackerToggles.fishSizeToggles.showBaby) {
                addStatLine(textList, Constant.BABY.TAG, displayBabyCount, displayFishCaughtCount,
                        config.fishTracker.fishTrackerToggles.dryStreakToggles.showBaby, displayDryStreakBabyCount);
            }
            if (config.fishTracker.fishTrackerToggles.fishSizeToggles.showJuvenile) {
                addStatLine(textList, Constant.JUVENILE.TAG, displayJuvenileCount, displayFishCaughtCount,
                        config.fishTracker.fishTrackerToggles.dryStreakToggles.showJuvenile, displayDryStreakJuvenileCount);
            }
            if (config.fishTracker.fishTrackerToggles.fishSizeToggles.showAdult) {
                addStatLine(textList, Constant.ADULT.TAG, displayAdultCount, displayFishCaughtCount,
                        config.fishTracker.fishTrackerToggles.dryStreakToggles.showAdult, displayDryStreakAdultCount);
            }
            if (config.fishTracker.fishTrackerToggles.fishSizeToggles.showLarge) {
                addStatLine(textList, Constant.LARGE.TAG, displayLargeCount, displayFishCaughtCount,
                        config.fishTracker.fishTrackerToggles.dryStreakToggles.showLarge, displayDryStreakLargeCount);
            }
            if (config.fishTracker.fishTrackerToggles.fishSizeToggles.showGigantic) {
                addStatLine(textList, Constant.GIGANTIC.TAG, displayGiganticCount, displayFishCaughtCount,
                        config.fishTracker.fishTrackerToggles.dryStreakToggles.showGigantic, displayDryStreakGiganticCount);
            }
        }

        if (config.fishTracker.fishTrackerToggles.variantToggles.showVariants) {
            textList.add(Text.empty());

            if(config.fishTracker.fishTrackerToggles.variantToggles.showAlbino) {
                addStatLine(textList, Constant.ALBINO.TAG, displayAlbinoCount, displayFishCaughtCount,
                        config.fishTracker.fishTrackerToggles.dryStreakToggles.showAlbino, displayDryStreakAlbinoCount);
            }
            if(config.fishTracker.fishTrackerToggles.variantToggles.showMelanistic) {
                addStatLine(textList, Constant.MELANISTIC.TAG, displayMelanisticCount, displayFishCaughtCount,
                        config.fishTracker.fishTrackerToggles.dryStreakToggles.showMelanistic, displayDryStreakMelanisticCount);
            }
            if(config.fishTracker.fishTrackerToggles.variantToggles.showTrophy) {
                addStatLine(textList, Constant.TROPHY.TAG, displayTrophyCount, displayFishCaughtCount,
                        config.fishTracker.fishTrackerToggles.dryStreakToggles.showTrophy, displayDryStreakTrophyCount);
            }
            if(config.fishTracker.fishTrackerToggles.variantToggles.showFabled) {
                addStatLine(textList, Constant.FABLED.TAG, displayFabledCount, displayFishCaughtCount,
                        config.fishTracker.fishTrackerToggles.dryStreakToggles.showFabled, profileData.allFishCaughtCount - displayDryStreakFabledCount);
            }

            //EVENT FISHES
            if(config.fishTracker.fishTrackerToggles.fishEventToggles.variantToggles.showAlternate) {
                addStatLine(textList, Constant.ALTERNATE.TAG, displayAlternateCount, displayFishCaughtCount,
                        config.fishTracker.fishTrackerToggles.fishEventToggles.dryStreakToggles.showAlternate, displayDryStreakAlternateCount);
            }
            if(config.fishTracker.fishTrackerToggles.fishEventToggles.variantToggles.showSpooky) {
                addStatLine(textList, Constant.SPOOKY.TAG, displaySpookyCount, displayFishCaughtCount,
                        config.fishTracker.fishTrackerToggles.fishEventToggles.dryStreakToggles.showSpooky, displayDryStreakSpookyCount);
            }
            if(config.fishTracker.fishTrackerToggles.fishEventToggles.variantToggles.showFrozen) {
                addStatLine(textList, Constant.FROZEN.TAG, displayFrozenCount, displayFishCaughtCount,
                        config.fishTracker.fishTrackerToggles.fishEventToggles.dryStreakToggles.showFrozen, displayDryStreakFrozenCount);
            }
        }

        // Cleaner Display (simple-square rarity tags + shortened location names), applied here
        // in the cached build — not per frame, which is what made the old global mixin laggy.
        return HudFont.applyCleanerDisplay(TextHelper.trimBlankLines(textList));
    }

    public Text getTitle() {
        FOEConfig config = FOEConfig.getConfig();
        return config.fishTracker.isFishTrackerOnTimer ? Text.literal("ѕᴇѕѕɪᴏɴ ѕᴛᴀᴛѕ").formatted(Formatting.BOLD) : Text.literal("ᴀʟʟ-ᴛɪᴍᴇ ѕᴛᴀᴛѕ").formatted(Formatting.BOLD);
    }

    private Text getPercentage(int count, int totalCount) {
        FOEConfig config = FOEConfig.getConfig();
        if (config.fishTracker.fishTrackerToggles.otherToggles.showPercentages) {
            float percentage = (count * 100f) / totalCount;
            float roundedPercentage = TextHelper.roundFirstSignificantDigit(percentage);
            return Text.literal(roundedPercentage >= 0.1f ? String.format(" (%.1f%%)", percentage) : " (" + roundedPercentage + "%)").formatted(Formatting.GRAY);
        } else {
            return Text.empty();
        }
    }

    /// Builds a single rarity/size/variant stat line — "TAG count (pct)" — and, when
    /// {@code showDryStreak} is on, appends the dry streak as a grayed parenthetical
    /// (e.g. trophies render "🏆 7 (1593)": 7 caught, 1593 dry) instead of its own line.
    private void addStatLine(List<Text> textList, Text tag, int count, int total, boolean showDryStreak, int dryStreakValue) {
        textList.add(TextHelper.concat(
                tag,
                Text.literal(" "),
                Text.literal(getNumber(count)),
                getPercentage(count, total),
                showDryStreak ? getDryStreakSuffix(dryStreakValue) : Text.empty()
        ));
    }

    /// The dry streak (fish caught since the last catch of this type) as a grayed
    /// parenthetical to tack onto a stat line, e.g. " (1593)" — no label.
    private Text getDryStreakSuffix(int value) {
        int dryStreak = ProfileDataHandler.instance().profileData.allFishCaughtCount - value;
        return Text.literal(" (" + TextHelper.fmt(dryStreak) + ")").formatted(Formatting.GRAY);
    }

    private String getNumber(int value) {
        FOEConfig config = FOEConfig.getConfig();
        return config.fishTracker.fishTrackerToggles.otherToggles.abbreviateNumbers ? TextHelper.fmnt(value) : String.valueOf(value);
    }
}