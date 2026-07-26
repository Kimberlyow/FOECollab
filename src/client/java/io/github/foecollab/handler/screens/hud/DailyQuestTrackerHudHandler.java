package io.github.foecollab.handler.screens.hud;

import io.github.foecollab.common.HudFont;
import io.github.foecollab.common.Theming;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.handler.DailyQuestHandler;
import io.github.foecollab.handler.ThemingHandler;
import io.github.foecollab.util.TextHelper;
import io.github.foecollab.util.ThrottledCache;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class DailyQuestTrackerHudHandler {
    private static DailyQuestTrackerHudHandler INSTANCE = new DailyQuestTrackerHudHandler();

    private final ThrottledCache<List<Text>> questTextCache =
            new ThrottledCache<>(200L, () -> HudFont.recolorAll(this.buildQuestText()));

    public static DailyQuestTrackerHudHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new DailyQuestTrackerHudHandler();
        }
        return INSTANCE;
    }

    public List<Text> assembleQuestText() {
        return questTextCache.get();
    }

    private List<Text> buildQuestText() {
        FOEConfig config = FOEConfig.getConfig();
        List<Text> textList = new ArrayList<>();

        if (ThemingHandler.instance().currentThemeType == Theming.ThemeType.OFF) {
            // The arrow points at the anchored edge; a centered HUD has none, so no arrow.
            switch (config.dailyQuestTracker.alignment) {
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

        if(DailyQuestHandler.instance() != null && !DailyQuestHandler.instance().quests.isEmpty()) {
            List<DailyQuestHandler.Quest> activeQuests = DailyQuestHandler.instance().quests;

            if(activeQuests != null) {
                activeQuests.forEach(quest -> {
                    if(quest.questDone()) {
                        textList.add(TextHelper.concat(
                                Text.literal("#").formatted(Formatting.GRAY),
                                Text.literal(String.valueOf(quest.slot)).formatted(Formatting.GRAY),
                                Text.literal(": ").formatted(Formatting.GRAY),
                                Text.literal("ꞯᴜᴇѕᴛ ᴅᴏɴᴇ").formatted(Formatting.GREEN, Formatting.BOLD)
                        ));
                    } else if (!quest.isStarted) {
                        textList.add(TextHelper.concat(
                                Text.literal("#").formatted(Formatting.GRAY),
                                Text.literal(String.valueOf(quest.slot)).formatted(Formatting.GRAY),
                                Text.literal(": ").formatted(Formatting.GRAY),
                                Text.literal("ɴᴏᴛ ѕᴛᴀʀᴛᴇᴅ").formatted(Formatting.WHITE)
                        ));
                    } else {
                        textList.add(TextHelper.concat(
                                Text.literal("#").formatted(Formatting.GRAY),
                                Text.literal(String.valueOf(quest.slot)).formatted(Formatting.GRAY),
                                Text.literal(": ").formatted(Formatting.GRAY),
                                Text.literal(quest.goal).formatted(Formatting.WHITE),
                                Text.literal(" (").formatted(Formatting.GRAY),
                                Text.literal(String.valueOf(quest.progress)).formatted(Formatting.YELLOW),
                                Text.literal("/").formatted(Formatting.GRAY),
                                Text.literal(String.valueOf(quest.needed)).formatted(Formatting.WHITE),
                                Text.literal(")").formatted(Formatting.GRAY)
                        ));
                    }
                });
            }
        }

        return TextHelper.trimBlankLines(textList);
    }

    public Text getTitle() {
        return Text.literal("ᴅᴀɪʟʏ ꞯᴜᴇѕᴛѕ").formatted(Formatting.BOLD);
    }
}
