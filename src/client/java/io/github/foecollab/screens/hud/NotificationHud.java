package io.github.foecollab.screens.hud;

import io.github.foecollab.common.FlairDecor;
import io.github.foecollab.common.HudFont;
import io.github.foecollab.common.Theming;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.config.HudAlignment;
import io.github.foecollab.handler.ThemingHandler;
import io.github.foecollab.handler.screens.hud.NotificationHudHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class NotificationHud {
    public void render(DrawContext drawContext, MinecraftClient client) {
        FOEConfig config = FOEConfig.getConfig();
        TextRenderer textRenderer = client.textRenderer;

        // Assemble all text lines
        List<Text> textList = NotificationHudHandler.instance().assembleNotificationText();

        // Follow the cosmetic HUD font color: when a color is chosen there, force every notification
        // line to it (warning reds included). When the cosmetic color is OFF, leave colors as-is.
        int drawColor = 0xFFFFFFFF;
        if (HudFont.isEnabled()) {
            drawColor = 0xFF000000 | (HudFont.baseColor() & 0xFFFFFF);
            textList = forceColor(textList, drawColor);
        }

        drawContext.getMatrices().pushMatrix();
        try {
            if(!textList.isEmpty()) {
                // Get screen size
                int screenWidth = client.getWindow().getScaledWidth();
                int screenHeight = client.getWindow().getScaledHeight();

                HudAlignment alignment = config.notifications.alignment;
                boolean center = alignment == HudAlignment.CENTER;
                boolean left = alignment == HudAlignment.LEFT;

                // hudX is the anchor's position from the screen's left edge for every alignment;
                // alignment only picks which point of the box (left edge / centre / right edge) sits
                // there, so changing alignment keeps the HUD in place instead of jumping.
                float yFraction = config.notifications.hudY / 100f;
                float xPercent = config.notifications.hudX / 100f;

                // Calculate base positions relative to screen size
                int baseX = (int) (screenWidth * xPercent);
                int baseY = (int) (screenHeight * yFraction);

                // Scaling setup
                int fontSize = config.notifications.fontSize;
                float scale = fontSize / 10.0f;
                drawContext.getMatrices().scale(scale, scale);

                // Alpha
                int alphaInt = (int) ((config.notifications.backgroundOpacity / 100f) * 255f) << 24;

                int lineSpacing = 2;
                int lineHeight = (int) (textRenderer.fontHeight + (lineSpacing / scale));
                int scaledX = (int) (baseX / scale);
                int scaledY = (int) (baseY / scale);
                int padding = 8;
                AtomicInteger count = new AtomicInteger(0);

                int maxLength = textList.stream().map(textRenderer::getWidth).max(Integer::compareTo).orElse(0);
                int heightClampTranslation = HudLayout.heightClampTranslation(padding, padding * 2 + textList.size() * lineHeight, yFraction);

                // Layout anchors. scaledX is the box center (CENTER), left edge (LEFT) or right edge (RIGHT).
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
                FlairDecor flairDecor = ThemingHandler.instance().flairDecorNotification;

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

                    // Title (kept centered over the box regardless of text alignment)
                    Text title = NotificationHudHandler.instance().getTitle().copy().withColor(ThemingHandler.instance().currentThemeType.TEXT_COLOR);
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
                int finalContentLeft = contentLeft;
                int finalContentRight = contentRight;
                int finalMaxLength = maxLength;
                int finalColor = drawColor;
                textList.forEach(text -> {
                    int textWidth = textRenderer.getWidth(text);
                    int x;
                    if (center) {
                        x = finalContentLeft + (finalMaxLength - textWidth) / 2;
                    } else if (left) {
                        x = finalContentLeft;
                    } else {
                        x = finalContentRight - textWidth;
                    }
                    drawContext.drawText(textRenderer, text, x, scaledY + padding + (count.getAndIncrement() * lineHeight) - finalHeightClampTranslation, finalColor, true);
                });
            }
        } finally {
            drawContext.getMatrices().popMatrix();
        }
    }

    /// Returns copies of {@code texts} with every styled run forced to {@code rgb}, used by the
    /// optional notification text-color override. The cached source list is left untouched.
    private static List<Text> forceColor(List<Text> texts, int rgb) {
        int color = rgb & 0xFFFFFF;
        List<Text> out = new ArrayList<>(texts.size());
        for (Text text : texts) {
            MutableText result = Text.empty();
            text.visit((style, string) -> {
                result.append(Text.literal(string).setStyle(style.withColor(color)));
                return Optional.empty();
            }, Style.EMPTY);
            out.add(result);
        }
        return out;
    }
}
