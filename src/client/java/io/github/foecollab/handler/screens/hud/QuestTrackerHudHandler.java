package io.github.foecollab.handler.screens.hud;

import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.common.HudFont;
import io.github.foecollab.common.Theming;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.handler.BossBarHandler;
import io.github.foecollab.handler.QuestHandler;
import io.github.foecollab.handler.ThemingHandler;
import io.github.foecollab.util.TextHelper;
import io.github.foecollab.util.ThrottledCache;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class QuestTrackerHudHandler {
    private static QuestTrackerHudHandler INSTANCE = new QuestTrackerHudHandler();

    private final ThrottledCache<List<Text>> questTextCache =
            new ThrottledCache<>(200L, () -> HudFont.recolorAll(this.buildQuestText()));

    public static QuestTrackerHudHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new QuestTrackerHudHandler();
        }
        return INSTANCE;
    }

    public List<Text> assembleQuestText() {
        return questTextCache.get();
    }

    private List<Text> buildQuestText() {
        FOEConfig config = FOEConfig.getConfig();
        List<Text> textList = new ArrayList<>();

        if (!config.questTracker.hideTitle && ThemingHandler.instance().currentThemeType == Theming.ThemeType.OFF) {
            // The arrow points at the anchored edge; a centered HUD has none, so no arrow.
            switch (config.questTracker.alignment) {
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

        if(QuestHandler.instance().isQuestInitialized()) {
            List<QuestHandler.Quest> activeQuests = BossBarHandler.instance().currentLocation == Constant.SPAWNHUB ? QuestHandler.instance().activeQuests.get(Constant.CYPRESS_LAKE) : QuestHandler.instance().activeQuests.get(BossBarHandler.instance().currentLocation);

            if (!config.questTracker.hideLocation) {
                textList.add(TextHelper.concat(
                        Text.literal("ʟᴏᴄ: ").formatted(Formatting.GRAY),
                        BossBarHandler.instance().currentLocation == Constant.SPAWNHUB ? Constant.CYPRESS_LAKE.TAG : BossBarHandler.instance().currentLocation.TAG
                ));
            }

            if(activeQuests != null) {
                boolean hideNumbers = config.questTracker.hideQuestNumbers;
                activeQuests.forEach(quest -> {
                    // "#<slot>: " prefix, dropped entirely when quest numbers are hidden.
                    Text prefix = hideNumbers ? Text.empty() : TextHelper.concat(
                            Text.literal("#").formatted(Formatting.GRAY),
                            Text.literal(String.valueOf(quest.slot)).formatted(Formatting.GRAY),
                            Text.literal(": ").formatted(Formatting.GRAY)
                    );
                    if(quest.questDone()) {
                        textList.add(TextHelper.concat(
                                prefix,
                                Text.literal("ꞯᴜᴇѕᴛ ᴅᴏɴᴇ").formatted(Formatting.GREEN, Formatting.BOLD)
                        ));
                    } else if (!quest.isStarted) {
                        textList.add(TextHelper.concat(
                                prefix,
                                Text.literal("ɴᴏᴛ ѕᴛᴀʀᴛᴇᴅ").formatted(Formatting.WHITE)
                        ));
                    } else {
                        textList.add(TextHelper.concat(
                                prefix,
                                quest.goal.TAG,
                                Text.literal(" ").formatted(Formatting.GRAY),
                                Text.literal(String.valueOf(quest.progress)).formatted(Formatting.YELLOW),
                                Text.literal("/").formatted(Formatting.GRAY),
                                Text.literal(String.valueOf(quest.needed)).formatted(Formatting.WHITE)
                        ));
                    }
                });
            }
        }

        // Cleaner Display: shorten the location line + simple-square tags (cached build).
        return HudFont.applyCleanerDisplay(TextHelper.trimBlankLines(textList));
    }

    public Text getTitle() {
        return Text.literal("ꞯᴜᴇѕᴛѕ").formatted(Formatting.BOLD);
    }
}
