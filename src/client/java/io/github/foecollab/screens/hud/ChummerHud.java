package io.github.foecollab.screens.hud;

import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.common.HudFont;
import io.github.foecollab.common.Theming;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.config.HudAlignment;
import io.github.foecollab.handler.ChummerHandler;
import io.github.foecollab.handler.ThemingHandler;
import io.github.foecollab.util.SimpleTagFont;
import io.github.foecollab.util.TextHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/// Totem-style display for the chummer whose range the player is standing in (like the
/// totem-of-corruption displays in skyblock mods): the chummer icon, a title with the
/// chummer's rarity tag, the remaining time and a solid progress bar that drains as the
/// chummer runs out. Hidden while outside every tracked chummer's range.
public class ChummerHud {
    private static final int PADDING = 8;
    private static final int ICON_SIZE = 16;
    private static final int ICON_GAP = 4;
    private static final int BAR_HEIGHT = 3;
    private static final int BAR_GAP = 4;

    // The title/time texts only change once per second (or when another chummer takes over),
    // so they're rebuilt at that rate instead of allocating new Text trees every frame.
    private ChummerHandler.Active cachedFor;
    private int cachedSecondsLeft = Integer.MIN_VALUE;
    private Text cachedTitle;
    private Text cachedTime;

    public void render(DrawContext drawContext, MinecraftClient client) {
        if (client.player == null) {
            return;
        }
        ChummerHandler.Active active = ChummerHandler.instance().activeInRange(client.player.getEntityPos());
        if (active == null) {
            this.cachedFor = null;
            return;
        }

        FOEConfig config = FOEConfig.getConfig();
        TextRenderer textRenderer = client.textRenderer;

        float fraction = active.remainingFraction();
        int secondsLeft = active.timerUnknown ? -1 : (int) Math.ceil(active.remainingSeconds());
        if (active != this.cachedFor || secondsLeft != this.cachedSecondsLeft) {
            this.cachedFor = active;
            this.cachedSecondsLeft = secondsLeft;
            // The rarity tag glyph keeps its own (white) style so the recolor can't tint it.
            Text title = HudFont.recolor(Text.literal("ᴄʜᴜᴍᴍᴇʀ").formatted(Formatting.GRAY));
            if (active.rarity != Constant.DEFAULT) {
                Text rarityTag = SimpleTagFont.apply(active.rarity.TAG, false, config.cleanerDisplay.simpleRarityTags);
                title = TextHelper.concat(title, Text.literal(" "), rarityTag);
            }
            this.cachedTitle = title;
            // Without the placed chummer's NBT there is no timer to count down or item to show.
            this.cachedTime = active.timerUnknown
                    ? Text.literal("?:??").formatted(Formatting.GRAY)
                    : Text.literal(formatTime(secondsLeft)).formatted(timeColor(fraction));
        }
        Text title = this.cachedTitle;
        Text time = this.cachedTime;
        ItemStack icon = active.stack;
        boolean hasIcon = !icon.isEmpty();
        boolean hasBar = !active.timerUnknown;

        drawContext.getMatrices().pushMatrix();
        try {
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();

            HudAlignment alignment = config.chummerTracker.alignment;

            // hudX is the anchor's position from the screen's left edge for every alignment; CENTER
            // puts the display's centre there, LEFT/RIGHT pin that edge. Changing alignment keeps it
            // in place instead of jumping.
            float xPercent = config.chummerTracker.hudX / 100f;
            int baseX = (int) (screenWidth * xPercent);
            int baseY = (int) (screenHeight * (config.chummerTracker.hudY / 100f));

            int fontSize = config.chummerTracker.fontSize;
            float scale = fontSize / 10.0f;
            drawContext.getMatrices().scale(scale, scale);

            int alphaInt = (int) ((config.chummerTracker.backgroundOpacity / 100f) * 255f) << 24;

            int lineHeight = textRenderer.fontHeight + 2;
            int textBlockWidth = Math.max(textRenderer.getWidth(title), textRenderer.getWidth(time));
            int contentWidth = (hasIcon ? ICON_SIZE + ICON_GAP : 0) + textBlockWidth;
            int contentHeight = Math.max(hasIcon ? ICON_SIZE : 0, lineHeight * 2)
                    + (hasBar ? BAR_GAP + BAR_HEIGHT : 0);
            int blockWidth = contentWidth + PADDING * 2;
            int blockHeight = contentHeight + PADDING * 2;

            // Anchor the block in scaled space, clamped on-screen.
            int scaledAnchorX = (int) (baseX / scale);
            int scaledAnchorY = (int) (baseY / scale);
            int scaledScreenWidth = (int) (screenWidth / scale);
            int scaledScreenHeight = (int) (screenHeight / scale);
            int unclampedLeft = switch (alignment) {
                case CENTER -> scaledAnchorX - blockWidth / 2;
                case LEFT -> scaledAnchorX;
                case RIGHT -> scaledAnchorX - blockWidth;
            };
            int unclampedTop = alignment == HudAlignment.CENTER ? scaledAnchorY - blockHeight / 2 : scaledAnchorY;
            int left = (int) Math.clamp((long) unclampedLeft, 0, Math.max(0, scaledScreenWidth - blockWidth));
            int top = (int) Math.clamp((long) unclampedTop, 0, Math.max(0, scaledScreenHeight - blockHeight));

            // Background
            drawContext.fill(left, top, left + blockWidth, top + blockHeight, alphaInt);

            // Theming — draw the same 9-slice frame the built-in HUDs use so a chosen theme applies
            // to the chummer HUD too. It carries its own header line, so no title strip or flair
            // (like the custom HUDs). The 16px corners centre on the box edges (inset 8 = half a
            // corner) and the interior PADDING matches that, so the frame sits exactly like the
            // other HUDs (whose corners straddle the fill edge by their own padding of 8).
            if (config.theme.themeType != Theming.ThemeType.OFF) {
                Theming theme = ThemingHandler.instance().currentTheme;
                int colorOverlay = config.theme.colorOverlay;
                int alphaOverlay = (int) ((config.theme.opacity / 100f) * 255f) << 24;
                int boxRightX = left + blockWidth;
                int boxBottomY = top + blockHeight;
                int topY = top - 8;
                int bottomY = boxBottomY - 8;
                int sideHeight = Math.max(0, blockHeight - 16);
                int spanWidth = Math.max(0, blockWidth - 16);

                // Corners
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, theme.GUI_TOP_LEFT, left - 8, topY, 16, 16, alphaOverlay | colorOverlay);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, theme.GUI_TOP_RIGHT, boxRightX - 8, topY, 16, 16, alphaOverlay | colorOverlay);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, theme.GUI_BOTTOM_LEFT, left - 8, bottomY, 16, 16, alphaOverlay | colorOverlay);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, theme.GUI_BOTTOM_RIGHT, boxRightX - 8, bottomY, 16, 16, alphaOverlay | colorOverlay);

                // Sides
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, theme.GUI_LEFT, left - 8, top + 8, 16, sideHeight, alphaOverlay | colorOverlay);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, theme.GUI_RIGHT, boxRightX - 8, top + 8, 16, sideHeight, alphaOverlay | colorOverlay);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, theme.GUI_TOP, left + 8, topY, spanWidth, 16, alphaOverlay | colorOverlay);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, theme.GUI_BOTTOM, left + 8, bottomY, spanWidth, 16, alphaOverlay | colorOverlay);
            }

            // Icon + title + remaining time
            int iconTop = top + PADDING;
            if (hasIcon) {
                drawContext.drawItem(icon, left + PADDING, iconTop);
            }
            int textX = left + PADDING + (hasIcon ? ICON_SIZE + ICON_GAP : 0);
            drawContext.drawText(textRenderer, title, textX, iconTop, 0xFFFFFFFF, true);
            drawContext.drawText(textRenderer, time, textX, iconTop + lineHeight, 0xFFFFFFFF, true);

            // Solid time bar, draining with the remaining fraction.
            if (hasBar) {
                int barLeft = left + PADDING;
                int barTop = top + blockHeight - PADDING - BAR_HEIGHT;
                int barRight = barLeft + contentWidth;
                int barBottom = barTop + BAR_HEIGHT;
                int barColor = 0xFF000000 | (config.chummerTracker.barColor & 0xFFFFFF);
                drawContext.fill(barLeft, barTop, barRight, barBottom, 0x66000000);
                int filled = Math.round(contentWidth * fraction);
                if (filled > 0) {
                    // Drop shadow (1px down-right) under the FILLED portion only, so it drains with
                    // the bar (like the vanilla XP bar's shadow) instead of spanning the empty track.
                    drawContext.fill(barLeft + 1, barTop + 1, barLeft + filled + 1, barBottom + 1, 0xFF000000);
                    drawContext.fill(barLeft, barTop, barLeft + filled, barBottom, barColor);
                }
            }
        } finally {
            drawContext.getMatrices().popMatrix();
        }
    }

    private static String formatTime(int total) {
        return String.format("%d:%02d", total / 60, total % 60);
    }

    private static Formatting timeColor(float fraction) {
        return fraction > 0.5f ? Formatting.GREEN : fraction > 0.25f ? Formatting.YELLOW : Formatting.RED;
    }
}
