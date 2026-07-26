package io.github.foecollab.screens.hud;

import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.config.HudAlignment;
import io.github.foecollab.handler.TitleHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TitleHud {
    public void render(DrawContext drawContext, MinecraftClient client) {
        FOEConfig config = FOEConfig.getConfig();
        TextRenderer textRenderer = client.textRenderer;

        long showedAt = TitleHandler.instance().showedAt;
        long elapsed = System.currentTimeMillis() - showedAt;

        if(elapsed < 3000L) {
            client.inGameHud.setTitle(Text.empty());
            client.inGameHud.setSubtitle(Text.empty());
        }

        // No fade: full opacity for the whole duration, then gone instantly.
        int alphaInt = 0xFF000000;


        if(elapsed >= 0L && elapsed < TitleHandler.instance().time) {
            // Assemble all text lines
            List<Text> title = TitleHandler.instance().title;
            List<Text> subtitle = TitleHandler.instance().subtitle;

            // Title
            drawContext.getMatrices().pushMatrix();
            try {
                // Get screen size
                int screenWidth = client.getWindow().getScaledWidth();
                int screenHeight = client.getWindow().getScaledHeight();

                // Calculate base positions relative to screen size (movable via the HUD editor)
                int baseX = (int) (screenWidth * (config.titlePopup.hudX / 100f));
                int baseY = (int) (screenHeight * (config.titlePopup.hudY / 100f));

                // Scaling setup
                float fontSize = config.titlePopup.scale * 2f;
                float scale = fontSize / 10.0f;
                drawContext.getMatrices().scale(scale, scale);

                int lineSpacing = 4;
                int lineHeight = (int) (textRenderer.fontHeight + (lineSpacing / scale));
                int scaledX = (int) (baseX / scale);
                int scaledY = (int) (baseY / scale);
                AtomicInteger count = new AtomicInteger(0);

                HudAlignment alignment = config.titlePopup.alignment;
                title.forEach(text -> drawContext.drawText(textRenderer, text, alignX(alignment, scaledX, textRenderer.getWidth(text)), scaledY - ((count.getAndIncrement() + 1) * lineHeight), alphaInt, true));
            } finally {
                drawContext.getMatrices().popMatrix();
            }

            // subtitle
            drawContext.getMatrices().pushMatrix();
            try {
                // Get screen size
                int screenWidth = client.getWindow().getScaledWidth();
                int screenHeight = client.getWindow().getScaledHeight();

                // Calculate base positions relative to screen size (movable via the HUD editor)
                int baseX = (int) (screenWidth * (config.titlePopup.hudX / 100f));
                int baseY = (int) (screenHeight * (config.titlePopup.hudY / 100f));

                // Scaling setup
                float fontSize = config.titlePopup.scale * 1f;
                float scale = fontSize / 10.0f;
                drawContext.getMatrices().scale(scale, scale);

                int lineSpacing = 4;
                int lineHeight = (int) (textRenderer.fontHeight + (lineSpacing / scale));
                int scaledX = (int) (baseX / scale);
                int scaledY = (int) (baseY / scale);
                AtomicInteger count = new AtomicInteger(0);

                HudAlignment alignment = config.titlePopup.alignment;
                subtitle.forEach(text -> drawContext.drawText(textRenderer, text, alignX(alignment, scaledX, textRenderer.getWidth(text)), scaledY + (count.getAndIncrement() * lineHeight) + lineHeight, alphaInt, true));
            } finally {
                drawContext.getMatrices().popMatrix();
            }
        }
    }

    /// X coordinate for a line of the given width drawn from anchor {@code anchorX}, per alignment.
    /// The popup's position anchor stays fixed; alignment only changes how lines flow from it.
    private static int alignX(HudAlignment alignment, int anchorX, int width) {
        return switch (alignment) {
            case LEFT -> anchorX;
            case RIGHT -> anchorX - width;
            case CENTER -> anchorX - width / 2;
        };
    }
}
