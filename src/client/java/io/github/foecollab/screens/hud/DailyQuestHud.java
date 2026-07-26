package io.github.foecollab.screens.hud;

import io.github.foecollab.common.FlairDecor;
import io.github.foecollab.common.Theming;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.config.HudAlignment;
import io.github.foecollab.handler.DailyQuestHandler;
import io.github.foecollab.handler.ThemingHandler;
import io.github.foecollab.handler.screens.hud.DailyQuestTrackerHudHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

// "Borrowed" from QuestHud.java
public class DailyQuestHud {
    public void render(DrawContext drawContext, MinecraftClient client) {

        FOEConfig config = FOEConfig.getConfig();
        TextRenderer textRenderer = client.textRenderer;

        // Assemble all text lines
        List<Text> textList = DailyQuestTrackerHudHandler.instance().assembleQuestText();

        drawContext.getMatrices().pushMatrix();
        try {
            if(!DailyQuestHandler.instance().quests.isEmpty()) {
                // Get screen size
                int screenWidth = client.getWindow().getScaledWidth();
                int screenHeight = client.getWindow().getScaledHeight();

                HudAlignment alignment = config.dailyQuestTracker.alignment;
                boolean center = alignment == HudAlignment.CENTER;
                boolean left = alignment == HudAlignment.LEFT;

                // hudX is the anchor's position from the screen's left edge for every alignment;
                // alignment only picks which point of the box (left edge / centre / right edge) sits
                // there, so changing alignment keeps the HUD in place instead of jumping.
                float xPercent = config.dailyQuestTracker.hudX / 100f;
                float yPercent = config.dailyQuestTracker.hudY / 100f;
                // Calculate base positions relative to screen size
                int baseX = (int) (screenWidth * xPercent);
                int baseY = (int) (screenHeight * yPercent);

                // Scaling setup
                int fontSize = config.dailyQuestTracker.fontSize;
                float scale = fontSize / 10.0f;
                drawContext.getMatrices().scale(scale, scale);

                // Alpha
                int alphaInt = (int) ((config.dailyQuestTracker.backgroundOpacity / 100f) * 255f) << 24;

                int lineSpacing = 2;
                int lineHeight = (int) (textRenderer.fontHeight + (lineSpacing / scale));
                int scaledX = (int) (baseX / scale);
                int scaledY = (int) (baseY / scale);
                int padding = 8;
                AtomicInteger count = new AtomicInteger(0);

                int maxLength = textList.stream().map(textRenderer::getWidth).max(Integer::compareTo).orElse(0);
                int heightClampTranslation = HudLayout.heightClampTranslation(padding, padding * 2 + textList.size() * lineHeight, yPercent);

                // Layout anchors. scaledX is the box center (CENTER) or the box's outer left/right edge.
                int contentLeft;
                if (center) {
                    contentLeft = scaledX - maxLength / 2;
                } else if (left) {
                    contentLeft = scaledX + padding;
                } else {
                    contentLeft = scaledX - padding - maxLength;
                }
                int contentRight = contentLeft + maxLength;
                int boxLeft = contentLeft - padding;
                int boxRight = contentRight + padding;

                // Draw Background
                drawContext.fill(boxLeft, scaledY - heightClampTranslation, boxRight, scaledY + ((textList.size() - 1) * lineHeight) + padding * 3 - heightClampTranslation, alphaInt);

                // Theming
                FlairDecor flairDecor = ThemingHandler.instance().flairDecorQuest;

                if(config.theme.themeType != Theming.ThemeType.OFF) {
                    Theming theme = ThemingHandler.instance().currentTheme;
                    int colorOverlay = config.theme.colorOverlay;
                    int themeTextColor = ThemingHandler.instance().currentThemeType.TEXT_COLOR;
                    int alphaOverlay = (int) ((config.theme.opacity / 100f) * 255f) << 24;

                    // Corners
                    drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   theme.GUI_TOP_LEFT, boxLeft - padding, scaledY - padding - heightClampTranslation, 16, 16, alphaOverlay | colorOverlay);
                    drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   theme.GUI_TOP_RIGHT, boxRight - padding, scaledY - padding - heightClampTranslation, 16, 16, alphaOverlay | colorOverlay);
                    drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   theme.GUI_BOTTOM_LEFT, boxLeft - padding, scaledY + padding * 2 + ((textList.size() - 1) * lineHeight) - heightClampTranslation, 16, 16, alphaOverlay | colorOverlay);
                    drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   theme.GUI_BOTTOM_RIGHT, boxRight - padding, scaledY + padding * 2 + ((textList.size() - 1) * lineHeight) - heightClampTranslation, 16, 16, alphaOverlay | colorOverlay);

                    // Sides
                    drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   theme.GUI_LEFT, boxLeft - padding, scaledY + padding - heightClampTranslation, 16, ((textList.size() - 1) * lineHeight) + padding, alphaOverlay | colorOverlay);
                    drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   theme.GUI_RIGHT, boxRight - padding, scaledY + padding - heightClampTranslation, 16, ((textList.size() - 1) * lineHeight) + padding, alphaOverlay | colorOverlay);
                    drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   theme.GUI_TOP, contentLeft, scaledY - padding - heightClampTranslation, maxLength, 16, alphaOverlay | colorOverlay);
                    drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   theme.GUI_BOTTOM, contentLeft, scaledY + padding * 2 + ((textList.size() - 1) * lineHeight) - heightClampTranslation, maxLength, 16, alphaOverlay | colorOverlay);

                    // Title
                    Text title = DailyQuestTrackerHudHandler.instance().getTitle().copy().withColor(ThemingHandler.instance().currentThemeType.TEXT_COLOR);
                    int titleWidth = textRenderer.getWidth(title);
                    int titleX = contentLeft + (maxLength - titleWidth) / 2;
                    drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   theme.GUI_TEXT_LEFT, titleX - 16, scaledY - padding - heightClampTranslation, 16, 16, alphaOverlay | colorOverlay);
                    drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   theme.GUI_TEXT_MIDDLE, titleX, scaledY - padding - heightClampTranslation, titleWidth, 16, alphaOverlay | colorOverlay);
                    drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   theme.GUI_TEXT_RIGHT, titleX + titleWidth, scaledY - padding - heightClampTranslation, 16, 16, alphaOverlay | colorOverlay);
                    drawContext.drawText(textRenderer, title, titleX, scaledY - textRenderer.fontHeight / 2 - heightClampTranslation - 1, themeTextColor, false);
                }

                // Flair
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   flairDecor.GUI_FLAIR_TOP_LEFT, boxLeft - padding - 24, scaledY - padding - heightClampTranslation - 24, 64, 64);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   flairDecor.GUI_FLAIR_TOP_RIGHT, boxRight - padding - 24, scaledY - padding - heightClampTranslation - 24, 64, 64);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   flairDecor.GUI_FLAIR_BOTTOM_LEFT, boxLeft - padding - 24, scaledY + padding * 2 + ((textList.size() - 1) * lineHeight) - heightClampTranslation - 24, 64, 64);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   flairDecor.GUI_FLAIR_BOTTOM_RIGHT, boxRight - padding - 24, scaledY + padding * 2 + ((textList.size() - 1) * lineHeight) - heightClampTranslation - 24, 64, 64);

                int finalHeightClampTranslation = heightClampTranslation;
                textList.forEach(text -> {
                    int textWidth = textRenderer.getWidth(text);
                    int x = center ? contentLeft + (maxLength - textWidth) / 2 : left ? contentLeft : contentRight - textWidth;
                    drawContext.drawText(textRenderer, text, x, scaledY + (count.getAndIncrement() * lineHeight) + padding - finalHeightClampTranslation, 0xFFFFFFFF, true);
                });
            }
        } finally {
            drawContext.getMatrices().popMatrix();
        }
    }
}
