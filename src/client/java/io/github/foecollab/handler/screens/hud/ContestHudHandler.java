package io.github.foecollab.handler.screens.hud;

import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.FOMC.LevelColors;
import io.github.foecollab.common.HudFont;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.config.TrackerContestHUDConfig;
import io.github.foecollab.handler.BossBarHandler;
import io.github.foecollab.handler.ContestHandler;
import io.github.foecollab.util.LocationNameHelper;
import io.github.foecollab.util.TextHelper;
import io.github.foecollab.util.ThrottledCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ContestHudHandler {
    private static ContestHudHandler INSTANCE = new ContestHudHandler();

    private final ThrottledCache<List<Text>> contestTextCache =
            new ThrottledCache<>(200L, () -> HudFont.recolorAll(this.buildContestText()));

    public static ContestHudHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new ContestHudHandler();
        }
        return INSTANCE;
    }

    public List<Text> assembleContestText() {
        return contestTextCache.get();
    }

    private List<Text> buildContestText() {
        FOEConfig config = FOEConfig.getConfig();
        List<Text> textList = new ArrayList<>();

        long timeLeftMinutes = TimeUnit.MILLISECONDS.toMinutes(ContestHandler.instance().timeLeft) % 60;
        long timeLeftSeconds = TimeUnit.MILLISECONDS.toSeconds(ContestHandler.instance().timeLeft) % 60;

        long timeAgo = System.currentTimeMillis() - ContestHandler.instance().lastUpdated;
        long lastUpdatedMinutes = TimeUnit.MILLISECONDS.toMinutes(timeAgo) % 60;
        long lastUpdatedSeconds = TimeUnit.MILLISECONDS.toSeconds(timeAgo) % 60;

        Text location = LocationNameHelper.shorten(Constant.valueOfTag(ContestHandler.instance().location) != null
                ? Objects.requireNonNull(Constant.valueOfTag(ContestHandler.instance().location)) == Constant.SPAWNHUB
                        ? Constant.CYPRESS_LAKE.TAG
                        : Constant.valueOfTag(ContestHandler.instance().location).TAG
                : Text.literal(ContestHandler.instance().location).formatted(Formatting.WHITE));

        // todo: make code more pretty here
        if (!config.contestTracker.useOldContestHUD) {
            if (!ContestHandler.instance().isReset) {
                if (!Objects.equals(ContestHandler.instance().type, "")) {
                    // Check if location matches
                    boolean locationMatches = Objects.equals(
                            Objects.requireNonNull(
                                    Constant.valueOfTag(ContestHandler.instance().location)) == Constant.SPAWNHUB
                                            ? Constant.CYPRESS_LAKE.ID
                                            : Objects.requireNonNull(
                                                    Constant.valueOfTag(ContestHandler.instance().location).ID),
                            BossBarHandler.instance().currentLocation.ID);
                    // Combine contest header with timer and level range
                    if (ContestHandler.instance().isContest) {
                        // Determine timer color based on remaining time
                        Formatting timerColor = Formatting.GREEN;
                        long totalTimeLeft = ContestHandler.instance().timeLeft;
                        if (totalTimeLeft < 5 * 60 * 1000) { // 5 minutes or less
                            timerColor = Formatting.DARK_RED;
                        } else if (totalTimeLeft <= 10 * 60 * 1000) { // 10 minutes or less
                            timerColor = Formatting.RED;
                        } else if (totalTimeLeft <= 15 * 60 * 1000) { // 15 minutes or less
                            timerColor = Formatting.GOLD;
                        }

                        if (ContestHandler.instance().levelLow > 0 && ContestHandler.instance().levelHigh > 0) {
                            textList.add(TextHelper.concat(
                                    Text.literal("ᴄᴏɴᴛᴇѕᴛ (").formatted(Formatting.GOLD),
                                    Text.literal(String.valueOf(ContestHandler.instance().levelLow))
                                            .withColor(
                                                    LevelColors.valueOfLvl(ContestHandler.instance().levelLow).color),
                                    Text.literal("-").formatted(Formatting.GRAY),
                                    Text.literal(String.valueOf(ContestHandler.instance().levelHigh))
                                            .withColor(
                                                    LevelColors.valueOfLvl(ContestHandler.instance().levelHigh).color),
                                    Text.literal(") ").formatted(Formatting.GOLD),
                                    Text.literal("⏱ ").formatted(Formatting.GRAY),
                                    Text.literal(String.format("%02d:%02d", timeLeftMinutes, timeLeftSeconds))
                                            .formatted(timerColor)));
                        } else {
                            textList.add(TextHelper.concat(
                                    Text.literal("ᴄᴏɴᴛᴇѕᴛ ").formatted(Formatting.GOLD),
                                    Text.literal("⏱ ").formatted(Formatting.GRAY),
                                    Text.literal(String.format("%02d:%02d", timeLeftMinutes, timeLeftSeconds))
                                            .formatted(timerColor)));
                        }
                    } else {
                        // Results mode
                        if (ContestHandler.instance().levelLow > 0 && ContestHandler.instance().levelHigh > 0) {
                            textList.add(TextHelper.concat(
                                    Text.literal("ᴄᴏɴᴛᴇѕᴛ (").formatted(Formatting.GRAY),
                                    Text.literal(String.valueOf(ContestHandler.instance().levelLow))
                                            .withColor(
                                                    LevelColors.valueOfLvl(ContestHandler.instance().levelLow).color),
                                    Text.literal("-").formatted(Formatting.GRAY),
                                    Text.literal(String.valueOf(ContestHandler.instance().levelHigh))
                                            .withColor(
                                                    LevelColors.valueOfLvl(ContestHandler.instance().levelHigh).color),
                                    Text.literal(") ").formatted(Formatting.GRAY),
                                    Text.literal("ʀᴇѕᴜʟᴛѕ").formatted(Formatting.GRAY)));
                        } else {
                            textList.add(TextHelper.concat(
                                    Text.literal("ᴄᴏɴᴛᴇѕᴛ ").formatted(Formatting.GOLD),
                                    Text.literal("ʀᴇѕᴜʟᴛѕ").formatted(Formatting.GRAY)));
                        }
                    }

                    boolean shouldShowDetails = false;
                    switch (config.contestTracker.contestStatsDisplay) {
                        case ALWAYS:
                            shouldShowDetails = true;
                            break;
                        case AT_LOCATION:
                            shouldShowDetails = locationMatches;
                            break;
                        case NEVER:
                            shouldShowDetails = false;
                            break;
                    }
                    boolean removeExtraSpacing = config.contestTracker.compact.removeExtraSpacing;
                    boolean mergeTypeAndLocation = config.contestTracker.compact.mergeTypeAndLocation;
                    boolean hideLocationWarning = config.contestTracker.compact.hideLocationWarning;
                    boolean combineRankLine = config.contestTracker.compact.combineRankLine;

                    if (!removeExtraSpacing) {
                        textList.add(Text.empty());
                    }

                    if (mergeTypeAndLocation) {
                        // Put type and location on the same row in compact mode
                        if (shouldShowDetails
                                || config.contestTracker.contestStatsDisplay != TrackerContestHUDConfig.ContestStatsDisplay.AT_LOCATION) {
                            textList.add(TextHelper.concat(
                                    Text.literal("ᴛʏᴘᴇ: ").formatted(Formatting.GRAY),
                                    Text.literal(ContestHandler.instance().type).formatted(Formatting.WHITE),
                                    Text.literal(" | ").formatted(Formatting.DARK_GRAY),
                                    Text.literal("ʟᴏᴄᴀᴛɪᴏɴ: ").formatted(Formatting.GRAY),
                                    location));
                        } else {
                            // Show type only when not showing location to avoid duplication
                            textList.add(TextHelper.concat(
                                    Text.literal("ᴛʏᴘᴇ: ").formatted(Formatting.GRAY),
                                    Text.literal(ContestHandler.instance().type).formatted(Formatting.WHITE)));
                        }
                        textList.add(Text.empty()); // Add spacing after type/location in compact mode
                    } else {
                        textList.add(TextHelper.concat(
                                Text.literal("ᴛʏᴘᴇ: ").formatted(Formatting.GRAY),
                                Text.literal(ContestHandler.instance().type).formatted(Formatting.WHITE)));
                        // Only show location if we're showing detailed stats or if it's not AT_LOCATION
                        // mode
                        if (shouldShowDetails
                                || config.contestTracker.contestStatsDisplay != TrackerContestHUDConfig.ContestStatsDisplay.AT_LOCATION) {
                            textList.add(TextHelper.concat(
                                    Text.literal("ʟᴏᴄᴀᴛɪᴏɴ: ").formatted(Formatting.GRAY),
                                    location));
                        }
                    }

                    // Show warning if location doesn't match (skip when hidden)
                    if (!locationMatches && !hideLocationWarning) {
                        textList.add(TextHelper.concat(
                                Text.literal("⚠ ").formatted(Formatting.YELLOW),
                                Text.literal("ɴᴏᴛ ɪɴ ᴀᴄᴛɪᴠᴇ ᴀʀᴇᴀ").formatted(Formatting.YELLOW)));
                    }

                    // Determine if we should show detailed contest information based on config

                    if (shouldShowDetails) {
                        if (!removeExtraSpacing) {
                            textList.add(Text.empty());
                        }
                        // First place
                        if (!Objects.equals(ContestHandler.instance().firstName, "")) {
                            textList.add(TextHelper.concat(
                                    Text.literal("\uF060 ").formatted(Formatting.WHITE),
                                    Text.literal(ContestHandler.instance().firstName).formatted(Formatting.WHITE),
                                    Text.literal(" (").formatted(Formatting.DARK_GRAY),
                                    Text.literal(ContestHandler.instance().firstStat).formatted(Formatting.GRAY),
                                    Text.literal(")").formatted(Formatting.DARK_GRAY)));
                        } else {
                            textList.add(TextHelper.concat(
                                    Text.literal("\uF060 ").formatted(Formatting.WHITE),
                                    Text.literal("ᴜɴᴄʟᴀɪᴍᴇᴅ").formatted(Formatting.GRAY)));
                        }
                        // Second place
                        if (!Objects.equals(ContestHandler.instance().secondName, "")) {
                            textList.add(TextHelper.concat(
                                    Text.literal("\uF061 ").formatted(Formatting.WHITE),
                                    Text.literal(ContestHandler.instance().secondName).formatted(Formatting.WHITE),
                                    Text.literal(" (").formatted(Formatting.DARK_GRAY),
                                    Text.literal(ContestHandler.instance().secondStat).formatted(Formatting.GRAY),
                                    Text.literal(")").formatted(Formatting.DARK_GRAY)));
                        } else {
                            textList.add(TextHelper.concat(
                                    Text.literal("\uF061 ").formatted(Formatting.WHITE),
                                    Text.literal("ᴜɴᴄʟᴀɪᴍᴇᴅ").formatted(Formatting.GRAY)));
                        }
                        // Third place
                        if (!Objects.equals(ContestHandler.instance().thirdName, "")) {
                            textList.add(TextHelper.concat(
                                    Text.literal("\uF062 ").formatted(Formatting.WHITE),
                                    Text.literal(ContestHandler.instance().thirdName).formatted(Formatting.WHITE),
                                    Text.literal(" (").formatted(Formatting.DARK_GRAY),
                                    Text.literal(ContestHandler.instance().thirdStat).formatted(Formatting.GRAY),
                                    Text.literal(")").formatted(Formatting.DARK_GRAY)));
                        } else {
                            textList.add(TextHelper.concat(
                                    Text.literal("\uF062 ").formatted(Formatting.WHITE),
                                    Text.literal("ᴜɴᴄʟᴀɪᴍᴇᴅ").formatted(Formatting.GRAY)));
                        }
                        if (!Objects.equals(ContestHandler.instance().firstName, "")) {
                            if (MinecraftClient.getInstance().player != null) {
                                // Add spacing before player rank
                                textList.add(Text.empty());

                                // Check if player is in top 3 and get appropriate indicator
                                String playerRank = ContestHandler.instance().rank;
                                Text rankIndicator;
                                Text playerNameColor;

                                if (playerRank.equals("#1") || playerRank.equals("1")) {
                                    rankIndicator = Text.literal("\uF060 ").formatted(Formatting.WHITE);
                                    playerNameColor = Text
                                            .literal(MinecraftClient.getInstance().player.getName().getString())
                                            .formatted(Formatting.GOLD);
                                } else if (playerRank.equals("#2") || playerRank.equals("2")) {
                                    rankIndicator = Text.literal("\uF061 ").formatted(Formatting.WHITE);
                                    playerNameColor = Text
                                            .literal(MinecraftClient.getInstance().player.getName().getString())
                                            .formatted(Formatting.AQUA);
                                } else if (playerRank.equals("#3") || playerRank.equals("3")) {
                                    rankIndicator = Text.literal("\uF062 ").formatted(Formatting.WHITE);
                                    playerNameColor = Text
                                            .literal(MinecraftClient.getInstance().player.getName().getString())
                                            .formatted(Formatting.YELLOW);
                                } else {
                                    rankIndicator = Text.literal("🫡 ").formatted(Formatting.DARK_GRAY);
                                    playerNameColor = Text
                                            .literal(MinecraftClient.getInstance().player.getName().getString())
                                            .formatted(Formatting.YELLOW);
                                }

                                if (combineRankLine) {
                                    // Combine rank and stat on same line in compact mode
                                    Text rankText = TextHelper.concat(
                                            Text.literal("ʏᴏᴜʀ ʀᴀɴᴋ: ").formatted(Formatting.GRAY),
                                            rankIndicator,
                                            Text.literal(playerRank).formatted(Formatting.WHITE));

                                    // Add total participants if available
                                    if (ContestHandler.instance().totalParticipants > 0) {
                                        rankText = TextHelper.concat(
                                                rankText,
                                                Text.literal(" (ᴏᴜᴛ ᴏғ ").formatted(Formatting.DARK_GRAY),
                                                Text.literal(
                                                        String.valueOf(ContestHandler.instance().totalParticipants))
                                                        .formatted(Formatting.GRAY),
                                                Text.literal(")").formatted(Formatting.DARK_GRAY));
                                    }

                                    // Add stat if available
                                    if (!Objects.equals(ContestHandler.instance().rankStat, "")) {
                                        rankText = TextHelper.concat(
                                                rankText,
                                                Text.literal(" (").formatted(Formatting.DARK_GRAY),
                                                Text.literal(ContestHandler.instance().rankStat)
                                                        .formatted(Formatting.GRAY),
                                                Text.literal(")").formatted(Formatting.DARK_GRAY));
                                    }

                                    textList.add(rankText);
                                } else {
                                    Text rankText = TextHelper.concat(
                                            Text.literal("ʏᴏᴜʀ ʀᴀɴᴋ: ").formatted(Formatting.GRAY),
                                            rankIndicator,
                                            Text.literal(playerRank).formatted(Formatting.WHITE));

                                    // Add total participants if available
                                    if (ContestHandler.instance().totalParticipants > 0) {
                                        rankText = TextHelper.concat(
                                                rankText,
                                                Text.literal(" (ᴏᴜᴛ ᴏғ ").formatted(Formatting.DARK_GRAY),
                                                Text.literal(
                                                        String.valueOf(ContestHandler.instance().totalParticipants))
                                                        .formatted(Formatting.GRAY),
                                                Text.literal(")").formatted(Formatting.DARK_GRAY));
                                    }

                                    textList.add(rankText);
                                    textList.add(
                                            !Objects.equals(ContestHandler.instance().rankStat, "") ? TextHelper.concat(
                                                    playerNameColor,
                                                    Text.literal(" (").formatted(Formatting.DARK_GRAY),
                                                    Text.literal(ContestHandler.instance().rankStat)
                                                            .formatted(Formatting.GRAY),
                                                    Text.literal(")").formatted(Formatting.DARK_GRAY)) : Text.empty());
                                    if (!removeExtraSpacing) {
                                        textList.add(Text.empty());
                                    }
                                }
                            }
                        }
                    } else {
                        // Show appropriate message based on config
                        if (config.contestTracker.contestStatsDisplay == TrackerContestHUDConfig.ContestStatsDisplay.AT_LOCATION
                                && !locationMatches) {
                            // Show message when not at the correct location
                            textList.add(TextHelper.concat(
                                    Text.literal("ᴛᴏ ᴠɪᴇᴡ ᴄᴏɴᴛᴇѕᴛ ᴅᴇᴛᴀɪʟѕ, ").formatted(Formatting.GRAY),
                                    Text.literal("ɢᴏ ᴛᴏ ").formatted(Formatting.GRAY),
                                    location));
                        }
                        // For NEVER option, don't show any additional message
                    }
                    if (ContestHandler.instance().isContest) {

                        textList.add(TextHelper.concat(
                                Text.literal("ʟᴀѕᴛ ᴜᴘᴅᴀᴛᴇ ᴡᴀѕ ").formatted(Formatting.GRAY),
                                Text.literal(String.format("%02d:%02d", lastUpdatedMinutes, lastUpdatedSeconds))
                                        .formatted(Formatting.GREEN),
                                Text.literal(" ᴀɢᴏ").formatted(Formatting.GRAY)));

                    }
                } else {
                    textList.add(TextHelper.concat(
                            Text.literal("ᴡᴀɪᴛɪɴɢ ɴᴇxᴛ ᴜᴘᴅᴀᴛᴇ...").formatted(Formatting.GRAY)));
                    textList.add(TextHelper.concat(
                            Text.literal("ᴏʀ ᴅᴏ ").formatted(Formatting.GRAY),
                            Text.literal("/contest").formatted(Formatting.AQUA)));
                }
            } else if (!ContestHandler.instance().isContest && ContestHandler.instance().isReset) {
                textList.add(TextHelper.concat(
                        Text.literal("ɴᴇxᴛ ᴄᴏɴᴛᴇѕᴛ ɪɴ: ").formatted(Formatting.GRAY),
                        Text.literal(String.format("%02d:%02d", timeLeftMinutes, timeLeftSeconds))
                                .formatted(Formatting.GREEN)));
            }

            return TextHelper.trimBlankLines(textList);
        } else {
            if (!ContestHandler.instance().isReset) {
                if (ContestHandler.instance().isContest) {
                    textList.add(TextHelper.concat(
                            Text.literal("ᴄᴏɴᴛᴇѕᴛ ᴛɪᴍᴇ ʟᴇꜰᴛ: ").formatted(Formatting.GRAY),
                            Text.literal(String.format("%02d:%02d", timeLeftMinutes, timeLeftSeconds))
                                    .formatted(Formatting.GREEN)));
                } else if (config.contestTracker.showFullContest) {
                    textList.add(TextHelper.concat(
                            Text.literal("ᴄᴏɴᴛᴇѕᴛ ʀᴇѕᴜʟᴛѕ").formatted(Formatting.GRAY)));
                }
                if (!Objects.equals(ContestHandler.instance().type, "") && config.contestTracker.showFullContest) {
                    textList.add(TextHelper.concat(
                            Text.literal("ᴛʏᴘᴇ: ").formatted(Formatting.GRAY),
                            Text.literal(ContestHandler.instance().type).formatted(Formatting.WHITE)));
                    textList.add(TextHelper.concat(
                            Text.literal("ʟᴏᴄᴀᴛɪᴏɴ: ").formatted(Formatting.GRAY),
                            location));
                    if (Objects.equals(
                            Objects.requireNonNull(
                                    Constant.valueOfTag(ContestHandler.instance().location)) == Constant.SPAWNHUB
                                            ? Constant.CYPRESS_LAKE.ID
                                            : Objects.requireNonNull(
                                                    Constant.valueOfTag(ContestHandler.instance().location).ID),
                            BossBarHandler.instance().currentLocation.ID)) {
                        if (!Objects.equals(ContestHandler.instance().firstName, "")) {
                            textList.add(Text.empty());
                            textList.add(TextHelper.concat(
                                    Text.literal("\uF060 ").formatted(Formatting.WHITE),
                                    Text.literal(ContestHandler.instance().firstName).formatted(Formatting.WHITE),
                                    Text.literal(" (").formatted(Formatting.DARK_GRAY),
                                    Text.literal(ContestHandler.instance().firstStat).formatted(Formatting.GRAY),
                                    Text.literal(")").formatted(Formatting.DARK_GRAY)));
                        }
                        if (!Objects.equals(ContestHandler.instance().secondName, "")) {
                            textList.add(TextHelper.concat(
                                    Text.literal("\uF061 ").formatted(Formatting.WHITE),
                                    Text.literal(ContestHandler.instance().secondName).formatted(Formatting.WHITE),
                                    Text.literal(" (").formatted(Formatting.DARK_GRAY),
                                    Text.literal(ContestHandler.instance().secondStat).formatted(Formatting.GRAY),
                                    Text.literal(")").formatted(Formatting.DARK_GRAY)));
                        }
                        if (!Objects.equals(ContestHandler.instance().thirdName, "")) {
                            textList.add(TextHelper.concat(
                                    Text.literal("\uF062 ").formatted(Formatting.WHITE),
                                    Text.literal(ContestHandler.instance().thirdName).formatted(Formatting.WHITE),
                                    Text.literal(" (").formatted(Formatting.DARK_GRAY),
                                    Text.literal(ContestHandler.instance().thirdStat).formatted(Formatting.GRAY),
                                    Text.literal(")").formatted(Formatting.DARK_GRAY)));
                        }
                        if (!Objects.equals(ContestHandler.instance().firstName, "")) {
                            if (MinecraftClient.getInstance().player != null) {
                                textList.add(TextHelper.concat(
                                        Text.literal(ContestHandler.instance().rank).formatted(Formatting.GRAY),
                                        Text.literal(" " + MinecraftClient.getInstance().player.getName().getString())
                                                .formatted(Formatting.YELLOW),
                                        !Objects.equals(ContestHandler.instance().rankStat, "")
                                                ? Text.literal(" (").formatted(Formatting.DARK_GRAY)
                                                        .append(Text.literal(ContestHandler.instance().rankStat)
                                                                .formatted(Formatting.GRAY))
                                                        .append(Text.literal(")").formatted(Formatting.DARK_GRAY))
                                                : Text.empty()));
                            }
                            textList.add(Text.empty());
                        }
                        if (ContestHandler.instance().isContest) {
                            textList.add(TextHelper.concat(
                                    Text.literal("ʟᴀѕᴛ ᴜᴘᴅᴀᴛᴇ ᴡᴀѕ ").formatted(Formatting.GRAY),
                                    Text.literal(String.format("%02d:%02d", lastUpdatedMinutes, lastUpdatedSeconds))
                                            .formatted(Formatting.GREEN),
                                    Text.literal(" ᴀɢᴏ").formatted(Formatting.GRAY)));
                        }
                    }
                } else if (config.contestTracker.showFullContest) {
                    textList.add(TextHelper.concat(
                            Text.literal("ᴡᴀɪᴛɪɴɢ ɴᴇxᴛ ᴜᴘᴅᴀᴛᴇ...").formatted(Formatting.GRAY)));
                    textList.add(TextHelper.concat(
                            Text.literal("ᴏʀ ᴅᴏ ").formatted(Formatting.GRAY),
                            Text.literal("/contest").formatted(Formatting.AQUA)));
                }
            } else if (!ContestHandler.instance().isContest
                    && (ContestHandler.instance().isReset || !config.contestTracker.showFullContest)) {
                textList.add(TextHelper.concat(
                        Text.literal("ɴᴇxᴛ ᴄᴏɴᴛᴇѕᴛ ɪɴ: ").formatted(Formatting.GRAY),
                        Text.literal(String.format("%02d:%02d", timeLeftMinutes, timeLeftSeconds))
                                .formatted(Formatting.GREEN)));
            }

            return TextHelper.trimBlankLines(textList);
        }
    }
}
