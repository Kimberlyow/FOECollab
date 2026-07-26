package io.github.foecollab.screens.hud;

import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.config.HudAlignment;
import io.github.foecollab.handler.BiteTitleHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/// The user-configurable "bite" title (custom text + color + position). It pops in the instant a
/// fish bites and then disappears on its own after the configured number of seconds (no fade) —
/// reeling in no longer clears it. The waiting-time timer is a separate HUD element
/// ({@link BobberTimerHud}) unless mergeWithTimer is on, in which case this element shows the
/// timer while waiting and swaps to the bite text while a fish bites.
public class BiteTitleHud {
    public void render(DrawContext drawContext, MinecraftClient client) {
        if (client.player == null) {
            return;
        }

        BiteTitleHandler handler = BiteTitleHandler.instance();

        // While the bobber is gone, re-arm the one-shot bite guard so the next cast's first "BITE!"
        // plays the alert sound again. The title itself is timed (below), so this no longer hides it.
        if (client.player.fishHook == null) {
            handler.reset();
        }

        FOEConfig config = FOEConfig.getConfig();

        if (biteTextVisible(config)) {
            String textString = config.biteTitle.text;
            if (textString == null || textString.isEmpty()) {
                return;
            }
            // Stay at full opacity for the whole window, then disappear outright (no fade).
            int color = 0xFF000000 | (config.biteTitle.textColor & 0xFFFFFF);
            this.draw(drawContext, client, Text.literal(textString), color, config);
        } else if (config.biteTitle.mergeWithTimer && BobberTimerHud.timerActive(client)) {
            // Merged mode: between bites the waiting timer lives in this slot (same position and
            // scale), with its own configurable color.
            Text text = BobberTimerHud.timerText(config);
            int color = 0xFF000000 | (config.biteTitle.timerColor & 0xFFFFFF);
            this.draw(drawContext, client, text, color, config);
        }
    }

    /// Whether the bite text's display window is currently running.
    static boolean biteTextVisible(FOEConfig config) {
        long biteTime = BiteTitleHandler.instance().biteTime();
        if (biteTime == 0L) {
            return false;
        }
        return System.currentTimeMillis() - biteTime < config.biteTitle.displaySeconds * 1000L;
    }

    private void draw(DrawContext drawContext, MinecraftClient client, Text text, int color, FOEConfig config) {
        TextRenderer textRenderer = client.textRenderer;

        drawContext.getMatrices().pushMatrix();
        try {
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();

            // Calculate base positions relative to screen size (movable via the HUD editor)
            int baseX = (int) (screenWidth * (config.biteTitle.hudX / 100f));
            int baseY = (int) (screenHeight * (config.biteTitle.hudY / 100f));

            // Scaling setup (matches the caught-fish title popup)
            float fontSize = config.biteTitle.scale * 2f;
            float scale = fontSize / 10.0f;
            drawContext.getMatrices().scale(scale, scale);

            int scaledX = (int) (baseX / scale);
            int scaledY = (int) (baseY / scale);

            int x = alignX(config.biteTitle.alignment, scaledX, textRenderer.getWidth(text));
            drawContext.drawText(textRenderer, text, x, scaledY, color, config.biteTitle.textShadow);
        } finally {
            drawContext.getMatrices().popMatrix();
        }
    }

    /// X coordinate for a line of the given width drawn from anchor {@code anchorX}, per alignment.
    private static int alignX(HudAlignment alignment, int anchorX, int width) {
        return switch (alignment) {
            case LEFT -> anchorX;
            case RIGHT -> anchorX - width;
            case CENTER -> anchorX - width / 2;
        };
    }
}
