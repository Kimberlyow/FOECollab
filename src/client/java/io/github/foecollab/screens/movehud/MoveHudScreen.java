package io.github.foecollab.screens.movehud;

import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.config.HudAlignment;
import io.github.foecollab.handler.CustomHudHandler;
import io.github.foecollab.handler.CustomHudHandler.CustomHud;
import io.github.foecollab.handler.TitleHandler;
import io.github.foecollab.handler.screens.hud.*;
import io.github.foecollab.screens.hud.CustomHudRenderer;
import io.github.foecollab.screens.widget.movablebox.MovableBoxWidget;
import io.github.foecollab.util.TextHelper;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class MoveHudScreen extends Screen {
    private final Screen parent;
    FOEConfig config = FOEConfig.getConfig();

    public MoveHudScreen(MinecraftClient minecraftClient, Screen parent) {
        super(Text.literal("Move Hud Elements"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.renderWidgets();
    }

    /// Maps a HUD's configured alignment onto the drag box's anchor mode.
    private static MovableBoxWidget.Alignment boxAlignment(HudAlignment alignment) {
        return switch (alignment) {
            case LEFT -> MovableBoxWidget.Alignment.LEFT;
            case RIGHT -> MovableBoxWidget.Alignment.RIGHT;
            case CENTER -> MovableBoxWidget.Alignment.CENTER;
        };
    }

    private void renderWidgets() {
        List<MovableBoxWidget> movableBoxWidgetList = new ArrayList<>();

        // Pixels reserved at the top for the bar-label strip. When the top bar is hidden it is not
        // drawn and not reserved, so elements can be dragged all the way to the top of the screen.
        int topReserved = config.barHUD.showBar ? barStripHeight() : 0;

        // Each element only appears here while it is actually enabled, so a HUD that's switched off
        // no longer clutters the editor (and frees its space for the others).

        // Fish Tracker HUD
        if (config.fishTracker.showFishTrackerHUD) {
            List<Text> fishTrackerTextList = FishTrackerHudHandler.instance().assembleFishText();
            int fishTrackerX = config.fishTracker.hudX;
            int fishTrackerY = config.fishTracker.hudY;
            MovableBoxWidget.Alignment fishTrackerAlignment = boxAlignment(config.fishTracker.alignment);
            int fishTrackerFontSize = config.fishTracker.fontSize;
            int fishTrackerMaxLength = fishTrackerTextList.stream().map(textRenderer::getWidth).max(Integer::compareTo).orElse(0);
            movableBoxWidgetList.add(new MovableBoxWidget(textRenderer, fishTrackerX, fishTrackerY, fishTrackerAlignment, Text.literal("Fish Tracker"), fishTrackerFontSize, 2, 20, fishTrackerTextList.size(), fishTrackerMaxLength, topReserved, 1f, (xPercent, yPercent) -> {
                config.fishTracker.hudX = xPercent;
                config.fishTracker.hudY = yPercent;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
            }, fontSize -> {
                config.fishTracker.fontSize = fontSize;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
            }));
        }

        // Contest Tracker HUD
        if (config.contestTracker.showContest) {
            List<Text> contestTrackerTextList = ContestHudHandler.instance().assembleContestText();
            int contestTrackerX = config.contestTracker.hudX;
            int contestTrackerY = config.contestTracker.hudY;
            MovableBoxWidget.Alignment contestTrackerAlignment = boxAlignment(config.contestTracker.alignment);
            int contestTrackerFontSize = config.contestTracker.fontSize;
            int contestTrackerMaxLength = contestTrackerTextList.stream().map(textRenderer::getWidth).max(Integer::compareTo).orElse(0);
            movableBoxWidgetList.add(new MovableBoxWidget(textRenderer, contestTrackerX, contestTrackerY, contestTrackerAlignment, Text.literal("Contest Tracker"), contestTrackerFontSize, 2, 20, contestTrackerTextList.size(), contestTrackerMaxLength, topReserved, 1f, (xPercent, yPercent) -> {
                config.contestTracker.hudX = xPercent;
                config.contestTracker.hudY = yPercent;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
            }, fontSize -> {
                config.contestTracker.fontSize = fontSize;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
            }));
        }

        // Pet Tracker HUD
        if (config.petEquipTracker.showPetEquipTrackerHUD) {
            List<Text> petTrackerTextList = PetEquipHudHandler.instance().assemblePetText();
            int petTrackerX = config.petEquipTracker.activePetHUDOptions.hudX;
            int petTrackerY = config.petEquipTracker.activePetHUDOptions.hudY;
            MovableBoxWidget.Alignment petTrackerAlignment = boxAlignment(config.petEquipTracker.activePetHUDOptions.alignment);
            int petTrackerFontSize = config.petEquipTracker.activePetHUDOptions.fontSize;
            int petTrackerMaxLength = petTrackerTextList.stream().map(textRenderer::getWidth).max(Integer::compareTo).orElse(0);
            movableBoxWidgetList.add(new MovableBoxWidget(textRenderer, petTrackerX, petTrackerY, petTrackerAlignment, Text.literal("Pet Tracker"), petTrackerFontSize, 1, 20, petTrackerTextList.size(), petTrackerMaxLength + 16 + 8, topReserved, 1f, (xPercent, yPercent) -> {
                config.petEquipTracker.activePetHUDOptions.hudX = xPercent;
                config.petEquipTracker.activePetHUDOptions.hudY = yPercent;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
            }, fontSize -> {
                config.petEquipTracker.activePetHUDOptions.fontSize = fontSize;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
            }));
        }

        // Quest Tracker HUD
        if (config.questTracker.showQuestHud) {
            List<Text> questTrackerTextList = QuestTrackerHudHandler.instance().assembleQuestText();
            int questTrackerX = config.questTracker.hudX;
            int questTrackerY = config.questTracker.hudY;
            MovableBoxWidget.Alignment questTrackerAlignment = boxAlignment(config.questTracker.alignment);
            int questTrackerFontSize = config.questTracker.fontSize;
            int questTrackerMaxLength = questTrackerTextList.stream().map(textRenderer::getWidth).max(Integer::compareTo).orElse(0);
            movableBoxWidgetList.add(new MovableBoxWidget(textRenderer, questTrackerX, questTrackerY, questTrackerAlignment, Text.literal("Quest Tracker"), questTrackerFontSize, 2, 20, questTrackerTextList.size(), questTrackerMaxLength, topReserved, 1f, (xPercent, yPercent) -> {
                config.questTracker.hudX = xPercent;
                config.questTracker.hudY = yPercent;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
            }, fontSize -> {
                config.questTracker.fontSize = fontSize;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
            }));
        }

        // Daily Quest Tracker HUD
        if (config.dailyQuestTracker.showDailyQuestHud) {
            List<Text> dailyQuestTrackerTextList = DailyQuestTrackerHudHandler.instance().assembleQuestText();
            int dailyQuestTrackerX = config.dailyQuestTracker.hudX;
            int dailyQuestTrackerY = config.dailyQuestTracker.hudY;
            MovableBoxWidget.Alignment dailyQuestTrackerAlignment = boxAlignment(config.dailyQuestTracker.alignment);
            int dailyQuestTrackerFontSize = config.dailyQuestTracker.fontSize;
            int dailyQuestTrackerMaxLength = dailyQuestTrackerTextList.stream().map(textRenderer::getWidth).max(Integer::compareTo).orElse(0);
            movableBoxWidgetList.add(new MovableBoxWidget(textRenderer, dailyQuestTrackerX, dailyQuestTrackerY, dailyQuestTrackerAlignment, Text.literal("Daily Quest Tracker"), dailyQuestTrackerFontSize, 2, 20, dailyQuestTrackerTextList.size(), dailyQuestTrackerMaxLength, topReserved, 1f, (xPercent, yPercent) -> {
                config.dailyQuestTracker.hudX = xPercent;
                config.dailyQuestTracker.hudY = yPercent;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
            }, fontSize -> {
                config.dailyQuestTracker.fontSize = fontSize;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
            }));
        }

        // Bait HUD
        if (config.baitTracker.showBaitHud) {
            Text baitText = BaitHudHandler.instance().assembleBaitText();
            int baitX = config.baitTracker.hudX;
            int baitY = config.baitTracker.hudY;
            int baitFontSize = config.baitTracker.fontSize;
            int baitMaxLength = textRenderer.getWidth(baitText) + 16 + 8;
            movableBoxWidgetList.add(new MovableBoxWidget(textRenderer, baitX, baitY, boxAlignment(config.baitTracker.alignment), Text.literal("Bait"), baitFontSize, 1, 20, 1, baitMaxLength, topReserved, 1f, (xPercent, yPercent) -> {
                config.baitTracker.hudX = xPercent;
                config.baitTracker.hudY = yPercent;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
            }, fontSize -> {
                config.baitTracker.fontSize = fontSize;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
            }));
        }

        // Chummer HUD (icon + two text lines + time bar; only shows in-game while a chummer is
        // active, so the editor uses a representative sample for sizing)
        if (config.chummerTracker.showChummerHud) {
            int chummerX = config.chummerTracker.hudX;
            int chummerY = config.chummerTracker.hudY;
            int chummerFontSize = config.chummerTracker.fontSize;
            Text chummerLabel = Text.literal("Chummer");
            int chummerMaxLength = Math.max(textRenderer.getWidth(chummerLabel), textRenderer.getWidth(Text.literal("ᴄʜᴜᴍᴍᴇʀ"))) + 16 + 4;
            movableBoxWidgetList.add(new MovableBoxWidget(textRenderer, chummerX, chummerY, boxAlignment(config.chummerTracker.alignment), chummerLabel, chummerFontSize, 2, 20, 2, chummerMaxLength, topReserved, 1f, (xPercent, yPercent) -> {
                config.chummerTracker.hudX = xPercent;
                config.chummerTracker.hudY = yPercent;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
            }, fontSize -> {
                config.chummerTracker.fontSize = fontSize;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
            }));
        }

        // Title Popup HUD (the "new title system" that shows the fish you caught). It renders its
        // title lines at 2x the slider scale and grows up (name/size) and down (weight/length) from
        // the anchor, so the box uses a 2x scale and a representative multi-line height + width.
        if (config.titlePopup.useNewTitleSystem) {
            int titleX = config.titlePopup.hudX;
            int titleY = config.titlePopup.hudY;
            int titleFontSize = config.titlePopup.scale;
            Text titleLabel = Text.literal("Title Popup");
            // icon + blank + name + size, plus the weight/length stat lines (drawn at half scale).
            int titleLines = 4;
            if (config.fishTracker.fishTrackerToggles.otherToggles.showStatsOnCatch) {
                int statLines = (config.titlePopup.showWeight ? 2 : 0) + (config.titlePopup.showLength ? 2 : 0);
                titleLines += (statLines + 1) / 2;
            }
            // No live catch to measure, so size the width off a representative rarity + fish name.
            int titleMaxLength = Math.max(textRenderer.getWidth(titleLabel), textRenderer.getWidth(Text.literal("ʟᴇɢᴇɴᴅᴀʀʏ ʟᴀʀɢᴇᴍᴏᴜᴛʜ ʙᴀss")));
            movableBoxWidgetList.add(new MovableBoxWidget(textRenderer, titleX, titleY, MovableBoxWidget.Alignment.CENTER, titleLabel, titleFontSize, 2, 20, titleLines, titleMaxLength, topReserved, 2f, (xPercent, yPercent) -> {
                config.titlePopup.hudX = xPercent;
                config.titlePopup.hudY = yPercent;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
            }, fontSize -> {
                config.titlePopup.scale = fontSize;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
            }));
        }

        // Notification HUD
        MovableBoxWidget.Alignment notificationAlignment = switch (config.notifications.alignment) {
            case LEFT -> MovableBoxWidget.Alignment.LEFT;
            case RIGHT -> MovableBoxWidget.Alignment.RIGHT;
            case CENTER -> MovableBoxWidget.Alignment.CENTER;
        };
        Text notificationLabel = Text.literal("Notifications");
        int notificationX = config.notifications.hudX;
        int notificationY = config.notifications.hudY;
        int notificationFontSize = config.notifications.fontSize;
        int notificationMaxLength = textRenderer.getWidth(notificationLabel);
        movableBoxWidgetList.add(new MovableBoxWidget(textRenderer, notificationX, notificationY, notificationAlignment, notificationLabel, notificationFontSize, 1, 20, 1, notificationMaxLength, topReserved, 1f, (xPercent, yPercent) -> {
            config.notifications.hudX = xPercent;
            config.notifications.hudY = yPercent;
            AutoConfig.getConfigHolder(FOEConfig.class).save();
        }, fontSize -> {
            config.notifications.fontSize = fontSize;
            AutoConfig.getConfigHolder(FOEConfig.class).save();
        }));

        // Bite notification HUD (also hosts the bobber timer when mergeWithTimer is on). Like the
        // title popup it renders at 2x the slider scale, so the box matches with a 2x multiplier.
        if (config.biteTitle.enabled) {
            int biteTitleX = config.biteTitle.hudX;
            int biteTitleY = config.biteTitle.hudY;
            int biteTitleFontSize = config.biteTitle.scale;
            Text biteTitleLabel = Text.literal(config.biteTitle.text == null || config.biteTitle.text.isEmpty() ? "Bite Title" : config.biteTitle.text);
            int biteTitleMaxLength = textRenderer.getWidth(biteTitleLabel);
            movableBoxWidgetList.add(new MovableBoxWidget(textRenderer, biteTitleX, biteTitleY, MovableBoxWidget.Alignment.CENTER, biteTitleLabel, biteTitleFontSize, 2, 40, 1, biteTitleMaxLength, topReserved, 2f, (xPercent, yPercent) -> {
                config.biteTitle.hudX = xPercent;
                config.biteTitle.hudY = yPercent;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
            }, fontSize -> {
                config.biteTitle.scale = fontSize;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
            }));
        }

        // Bobber Timer HUD (the waiting time, only while it's shown as its own fixed HUD element —
        // merged into the bite notification's box above when mergeWithTimer is on)
        if (config.bobberTracker.timerAsHud && !(config.biteTitle.enabled && config.biteTitle.mergeWithTimer)) {
            int bobberTimerX = config.bobberTracker.timerHudX;
            int bobberTimerY = config.bobberTracker.timerHudY;
            int bobberTimerFontSize = config.bobberTracker.timerHudFontSize;
            Text bobberTimerLabel = Text.literal("Bobber Timer");
            int bobberTimerMaxLength = textRenderer.getWidth(bobberTimerLabel);
            movableBoxWidgetList.add(new MovableBoxWidget(textRenderer, bobberTimerX, bobberTimerY, boxAlignment(config.bobberTracker.timerHudAlignment), bobberTimerLabel, bobberTimerFontSize, 1, 40, 1, bobberTimerMaxLength, topReserved, 1f, (xPercent, yPercent) -> {
                config.bobberTracker.timerHudX = xPercent;
                config.bobberTracker.timerHudY = yPercent;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
            }, fontSize -> {
                config.bobberTracker.timerHudFontSize = fontSize;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
            }));
        }

        // Level + XP HUD — only shown (and movable) while the top bar is hidden.
        if (!config.barHUD.showBar && config.barHUD.levelHud.showWhenBarHidden) {
            List<Text> levelTextList = LevelHudHandler.instance().assembleText();
            int levelX = config.barHUD.levelHud.hudX;
            int levelY = config.barHUD.levelHud.hudY;
            int levelFontSize = config.barHUD.levelHud.fontSize;
            int levelMaxLength = levelTextList.stream().map(textRenderer::getWidth).max(Integer::compareTo).orElse(0);
            movableBoxWidgetList.add(new MovableBoxWidget(textRenderer, levelX, levelY, boxAlignment(config.barHUD.levelHud.alignment), Text.literal("Level"), levelFontSize, 2, 20, levelTextList.size(), levelMaxLength, topReserved, 1f, (xPercent, yPercent) -> {
                config.barHUD.levelHud.hudX = xPercent;
                config.barHUD.levelHud.hudY = yPercent;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
            }, fontSize -> {
                config.barHUD.levelHud.fontSize = fontSize;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
            }));
        }

        // Custom HUDs (user-made / imported). Each enabled one gets its own drag box, sized from its
        // resolved lines exactly like the live renderer; a HUD with no current data falls back to its
        // name so it stays grabbable. Position/scale persist to the custom-HUD file, not the config.
        if (config.customHuds) {
            for (CustomHud hud : CustomHudHandler.instance().getHuds()) {
                if (!hud.enabled) {
                    continue;
                }
                List<Text> customLines = CustomHudRenderer.resolveHudLines(hud);
                Text customLabel = Text.literal(hud.name == null || hud.name.isBlank() ? "Custom HUD" : hud.name);
                int customLineCount = customLines.isEmpty() ? 1 : customLines.size();
                int customMaxLength = customLines.isEmpty()
                        ? textRenderer.getWidth(customLabel)
                        : customLines.stream().map(textRenderer::getWidth).max(Integer::compareTo).orElse(0);
                MovableBoxWidget.Alignment customAlignment = boxAlignment(hud.alignment == null ? HudAlignment.LEFT : hud.alignment);
                movableBoxWidgetList.add(new MovableBoxWidget(textRenderer, hud.hudX, hud.hudY, customAlignment, customLabel, hud.fontSize, 2, 20, customLineCount, customMaxLength, topReserved, 1f, (xPercent, yPercent) -> {
                    hud.hudX = xPercent;
                    hud.hudY = yPercent;
                    CustomHudHandler.instance().save();
                }, fontSize -> {
                    hud.fontSize = fontSize;
                    CustomHudHandler.instance().save();
                }).topAnchored());
            }
        }

        movableBoxWidgetList.forEach(this::addDrawableChild);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // The top bar is fixed/not movable, so it's only shown here (and reserves the top strip)
        // while it's enabled. With it hidden, the strip is freed and elements can be dragged up.
        if (config.barHUD.showBar) {
            int stripHeight = barStripHeight();
            int alphaInt = (int) ((40 / 100f) * 255f) << 24;
            int barScreenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
            context.fill(0, 0, barScreenWidth, stripHeight, alphaInt);
            Text barHudText = TextHelper.concat(
                    Text.literal("Bar HUD "), Text.literal("Not movable").formatted(Formatting.GRAY, Formatting.ITALIC),
                    Text.literal("   •   ").formatted(Formatting.DARK_GRAY),
                    Text.literal("Scroll to resize").formatted(Formatting.GRAY, Formatting.ITALIC));
            context.drawText(textRenderer, barHudText, barScreenWidth / 2 - textRenderer.getWidth(barHudText) / 2, stripHeight / 2 - textRenderer.fontHeight / 2, 0xFFFFFFFF, true);
        }
    }

    private int barStripHeight() {
        return 16 + config.barHUD.fontSize;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // The top bar isn't a movable widget, so scrolling over its strip resizes it here.
        if (config.barHUD.showBar && verticalAmount != 0 && mouseY < barStripHeight()) {
            int newSize = (int) Math.clamp((long) (config.barHUD.fontSize + (verticalAmount > 0 ? 1 : -1)), 2, 20);
            if (newSize != config.barHUD.fontSize) {
                config.barHUD.fontSize = newSize;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
                this.clearAndInit();
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void close() {
        assert this.client != null;
        this.client.setScreen(this.parent);
    }
}
