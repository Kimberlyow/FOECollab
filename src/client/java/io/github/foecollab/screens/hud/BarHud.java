package io.github.foecollab.screens.hud;

import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.handler.screens.hud.BarHudHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class BarHud {
    public void render(DrawContext drawContext, MinecraftClient client) {

        FOEConfig config = FOEConfig.getConfig();
        TextRenderer textRenderer = client.textRenderer;

        // Assemble all text lines
        Text leftText = BarHudHandler.instance().assembleLeftText();
        Text middleText = BarHudHandler.instance().assembleMiddleText();
        Text rightText = BarHudHandler.instance().assembleRightText();

        drawContext.getMatrices().pushMatrix();
        try {
            // Get non scaled screen size
            int width = client.getWindow().getWidth();

            int screenWidth = client.getWindow().getScaledWidth();

            // Scaling setup
            int fontSize = config.barHUD.fontSize;
            float scale = fontSize / 10.0f;
            drawContext.getMatrices().scale(scale, scale);

            // Alpha
            int alphaInt = (int) ((config.barHUD.backgroundOpacity / 100f) * 255f) << 24;

            int scaledX = (int) (screenWidth / scale);
            int padding = 8;

            // Draw Box
            drawContext.fill(0, 0, width, padding * 2 + fontSize, alphaInt);

            // Draw Text
            drawContext.drawText(textRenderer, leftText, padding, padding, 0xFFFFFFFF, true);
            if(config.bossBarTracker.hideBossBar) {
                drawContext.drawText(textRenderer, middleText, scaledX / 2 - textRenderer.getWidth(middleText) / 2, padding, 0xFFFFFFFF, true);
            }
            drawContext.drawText(textRenderer, rightText, scaledX - textRenderer.getWidth(rightText) - padding, padding, 0xFFFFFFFF, true);
        } finally {
            drawContext.getMatrices().popMatrix();
        }
    }
}
