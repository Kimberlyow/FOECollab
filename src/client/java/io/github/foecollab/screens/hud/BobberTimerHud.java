package io.github.foecollab.screens.hud;

import io.github.foecollab.common.HudFont;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.config.HudAlignment;
import io.github.foecollab.handler.FishingRodHandler;
import io.github.foecollab.mixin.InGameHudAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/// Draws the bobber waiting-time as a fixed HUD element instead of floating over the bobber.
/// Active only while the player's own bobber is out (see {@link FishingRodHandler}); its color
/// follows the cosmetic HUD font color chosen in the config. It steps aside while the bite
/// notification is on screen and stays hidden once the fish is hooked.
public class BobberTimerHud {
    /// Whether the waiting timer is currently allowed to show: the player's own bobber must be
    /// out and the fish must not be hooked. The reel-in minigame runs in the action bar, so an
    /// active overlay message means the fish is on the hook and the wait is over.
    static boolean timerActive(MinecraftClient client) {
        if (!FishingRodHandler.instance().showTimerHud) {
            return false;
        }
        // Disappear the instant the bobber is reeled in, without waiting for the next tick to
        // clear showTimerHud (render runs many times between ticks).
        if (client.player == null || client.player.fishHook == null) {
            return false;
        }
        // An active overlay means the reel-in minigame is running — unless it's just the previous
        // catch's message finishing its ~3s display, which predates this cast (see staleOverlayTicks).
        return ((InGameHudAccessor) client.inGameHud).getOverlayRemaining() <= FishingRodHandler.instance().staleOverlayTicks;
    }

    /// The waiting-time text, with the "s" unit suffix unless toggled off in the config. Shared
    /// with {@link BiteTitleHud}, which shows the same timer in merged mode.
    static Text timerText(FOEConfig config) {
        String format = config.bobberTracker.timerSecondsSuffix ? "%.1fs" : "%.1f";
        return Text.literal(String.format(format, FishingRodHandler.instance().timerSeconds));
    }

    public void render(DrawContext drawContext, MinecraftClient client) {
        if (!timerActive(client)) {
            return;
        }

        FOEConfig config = FOEConfig.getConfig();

        // Hand the moment to the bite notification; the timer comes back once it's gone (and the
        // fish wasn't hooked — timerActive above keeps it hidden after a hook).
        if (config.biteTitle.enabled && BiteTitleHud.biteTextVisible(config)) {
            return;
        }

        TextRenderer textRenderer = client.textRenderer;
        Text text = timerText(config);
        int color = 0xFF000000 | (HudFont.baseColor() & 0xFFFFFF);

        drawContext.getMatrices().pushMatrix();
        try {
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();

            HudAlignment alignment = config.bobberTracker.timerHudAlignment;

            // Calculate base positions relative to screen size (movable via the HUD editor). hudX is
            // the anchor's position from the screen's left edge for every alignment; alignment only
            // picks which point of the box sits there, so it stays put when alignment changes.
            float xPercent = config.bobberTracker.timerHudX / 100f;
            int baseX = (int) (screenWidth * xPercent);
            int baseY = (int) (screenHeight * (config.bobberTracker.timerHudY / 100f));

            float scale = config.bobberTracker.timerHudFontSize / 10.0f;
            drawContext.getMatrices().scale(scale, scale);

            int scaledX = (int) (baseX / scale);
            int scaledY = (int) (baseY / scale);

            int textX = switch (alignment) {
                case CENTER -> scaledX - textRenderer.getWidth(text) / 2;
                case LEFT -> scaledX;
                case RIGHT -> scaledX - textRenderer.getWidth(text);
            };
            drawContext.drawText(textRenderer, text, textX, scaledY, color, true);
        } finally {
            drawContext.getMatrices().popMatrix();
        }
    }
}
