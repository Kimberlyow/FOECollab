package io.github.foecollab.screens.hud;

import io.github.foecollab.common.Theming;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.config.HudAlignment;
import io.github.foecollab.handler.ThemingHandler;
import io.github.foecollab.handler.screens.hud.LevelHudHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/// Standalone, movable level + XP-progress HUD shown only while the top bar is hidden. Mirrors the
/// built-in trackers' box/theme layout (and their vertical anchoring, so it lines up in the Move-HUD
/// editor); no title strip or flair since it's a single status line.
public class LevelHud {
    public void render(DrawContext drawContext, MinecraftClient client) {
        FOEConfig config = FOEConfig.getConfig();
        var opts = config.barHUD.levelHud;
        TextRenderer textRenderer = client.textRenderer;

        List<Text> textList = LevelHudHandler.instance().assembleText();

        drawContext.getMatrices().pushMatrix();
        try {
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();

            HudAlignment alignment = opts.alignment;
            boolean center = alignment == HudAlignment.CENTER;
            boolean left = alignment == HudAlignment.LEFT;

            // hudX is the anchor's position from the screen's left edge for every alignment;
            // alignment only picks which point of the box sits there, so changing alignment keeps
            // the HUD in place instead of jumping.
            float xPercent = opts.hudX / 100f;
            float yPercent = opts.hudY / 100f;

            int baseX = (int) (screenWidth * xPercent);
            int baseY = (int) (screenHeight * yPercent);

            float scale = opts.fontSize / 10.0f;
            drawContext.getMatrices().scale(scale, scale);

            int alphaInt = (int) ((opts.backgroundOpacity / 100f) * 255f) << 24;

            int lineSpacing = 2;
            int lineHeight = (int) (textRenderer.fontHeight + (lineSpacing / scale));
            int scaledX = (int) (baseX / scale);
            int scaledY = (int) (baseY / scale);
            int padding = 8;
            AtomicInteger count = new AtomicInteger(0);

            int maxLength = textList.stream().map(textRenderer::getWidth).max(Integer::compareTo).orElse(0);
            int heightClampTranslation = HudLayout.heightClampTranslation(padding, padding * 2 + textList.size() * lineHeight, yPercent);

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

            drawContext.fill(boxLeft, scaledY - heightClampTranslation, boxRight,
                    scaledY + ((textList.size() - 1) * lineHeight) + padding * 3 - heightClampTranslation, alphaInt);

            if (config.theme.themeType != Theming.ThemeType.OFF) {
                Theming theme = ThemingHandler.instance().currentTheme;
                int colorOverlay = config.theme.colorOverlay;
                int alphaOverlay = (int) ((config.theme.opacity / 100f) * 255f) << 24;
                int bottomY = scaledY + padding * 2 + ((textList.size() - 1) * lineHeight) - heightClampTranslation;
                int topY = scaledY - padding - heightClampTranslation;
                int sideHeight = ((textList.size() - 1) * lineHeight) + padding;

                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, theme.GUI_TOP_LEFT, boxLeft - padding, topY, 16, 16, alphaOverlay | colorOverlay);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, theme.GUI_TOP_RIGHT, boxRight - padding, topY, 16, 16, alphaOverlay | colorOverlay);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, theme.GUI_BOTTOM_LEFT, boxLeft - padding, bottomY, 16, 16, alphaOverlay | colorOverlay);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, theme.GUI_BOTTOM_RIGHT, boxRight - padding, bottomY, 16, 16, alphaOverlay | colorOverlay);

                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, theme.GUI_LEFT, boxLeft - padding, scaledY + padding - heightClampTranslation, 16, sideHeight, alphaOverlay | colorOverlay);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, theme.GUI_RIGHT, boxRight - padding, scaledY + padding - heightClampTranslation, 16, sideHeight, alphaOverlay | colorOverlay);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, theme.GUI_TOP, contentLeft, topY, maxLength, 16, alphaOverlay | colorOverlay);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, theme.GUI_BOTTOM, contentLeft, bottomY, maxLength, 16, alphaOverlay | colorOverlay);
            }

            int finalHeightClampTranslation = heightClampTranslation;
            textList.forEach(text -> {
                int textWidth = textRenderer.getWidth(text);
                int x = center ? contentLeft + (maxLength - textWidth) / 2 : left ? contentLeft : contentRight - textWidth;
                drawContext.drawText(textRenderer, text,
                        x, scaledY + (count.getAndIncrement() * lineHeight) + padding - finalHeightClampTranslation,
                        0xFFFFFFFF, true);
            });
        } finally {
            drawContext.getMatrices().popMatrix();
        }
    }
}
