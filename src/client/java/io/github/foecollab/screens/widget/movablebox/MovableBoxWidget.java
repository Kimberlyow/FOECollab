package io.github.foecollab.screens.widget.movablebox;

import io.github.foecollab.util.TextHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class MovableBoxWidget extends ClickableWidget {
    public enum Alignment {
        LEFT,
        RIGHT,
        CENTER
    }

    private final int padding = 8;
    private final TextRenderer textRenderer;
    private final CallbackCoordinates callbackCoordinates;
    private final CallbackFontSize callbackFontSize;
    private final int lines;
    private final Text text;
    private final Alignment alignment;
    private final int maxLength;
    private final int minFontSize;
    private final int maxFontSize;
    /// Pixels reserved at the very top of the screen (the editor's bar-label strip). Drag bounds
    /// never let an element above this; 0 when the top bar is hidden so elements can use that space.
    private final int topReserved;
    /// Extra on-screen scale factor for elements drawn larger than their stored {@code fontSize}
    /// would imply (the title/bite popups render at 2x their slider), so the box matches them.
    private final float scaleMultiplier;
    /// Minimum number of box pixels kept on-screen so a box dragged toward an edge stays grabbable.
    private static final int GRAB_MARGIN = 16;

    private float scale;
    private int fontSize;
    /// When true the element anchors flush to the screen top/bottom (no top reserve) — used by custom
    /// HUDs, whose live renderer also uses the flush model so they can be placed at the very top.
    private boolean topAnchored = false;
    private int anchorOffsetX; // distance from the box's left edge to the anchored point
    private int xMin;
    private int xMax;
    private int yMin;
    private int yMax;

    private double deltaX = 0;
    private double deltaY = 0;
    private int originalX;
    private int originalY;
    private int xPercent;
    private int yPercent;

    public MovableBoxWidget(TextRenderer textRenderer, int xPercent, int yPercent, Alignment alignment, Text text, int fontSize, int minFontSize, int maxFontSize, int lines, int maxLength, int topReserved, float scaleMultiplier, CallbackCoordinates callbackCoordinates, CallbackFontSize callbackFontSize) {
        super(1, 1, 1, 1, null);

        this.text = text;
        this.alignment = alignment;
        this.textRenderer = textRenderer;
        this.callbackCoordinates = callbackCoordinates;
        this.callbackFontSize = callbackFontSize;
        this.fontSize = fontSize;
        this.minFontSize = minFontSize;
        this.maxFontSize = maxFontSize;
        this.lines = lines;
        this.maxLength = maxLength;
        this.topReserved = topReserved;
        this.scaleMultiplier = scaleMultiplier;
        this.xPercent = xPercent;
        this.yPercent = yPercent;

        recomputeGeometry();
    }

    /// Anchor this element flush to the screen top/bottom (no top reserve), matching a HUD whose live
    /// renderer uses the flush model (custom HUDs). Recomputes bounds so the box repositions at once.
    public MovableBoxWidget topAnchored() {
        this.topAnchored = true;
        recomputeGeometry();
        return this;
    }

    /// Recompute the widget's scale, size, drag bounds and (from the stored percentages) its
    /// on-screen position. Called once on construction and again whenever the font size is
    /// scrolled. The anchored point — the box centre for {@link Alignment#CENTER}, otherwise the
    /// left/right edge — is what {@code (xPercent, yPercent)} marks and what the live HUD pins to,
    /// so the bounds let that point reach either screen edge (the box may hang off, but always
    /// keeps {@link #GRAB_MARGIN} px on-screen). Vertically, centred popups behave the same while
    /// left/right blocks grow downward and stay fully on-screen, matching the live HUD.
    private void recomputeGeometry() {
        this.scale = (fontSize / 10f) * scaleMultiplier;

        int screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
        int screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();

        this.setWidth((int) ((padding * 2 + this.maxLength) * this.scale));
        this.setHeight((int) ((padding * 2 + (textRenderer.fontHeight + 2f) * this.lines) * this.scale));

        this.anchorOffsetX = switch (alignment) {
            case CENTER -> this.width / 2;
            case RIGHT -> this.width;
            case LEFT -> 0;
        };

        int grab = Math.min(this.width, GRAB_MARGIN);
        this.xMin = Math.max(-this.anchorOffsetX, grab - this.width);
        this.xMax = Math.min(screenWidth - this.anchorOffsetX, screenWidth - grab);

        if (alignment == Alignment.CENTER) {
            this.yMin = topReserved - this.height / 2;
            this.yMax = screenHeight - this.height / 2;
        } else {
            // The live HUD pins its box 3*padding below the top (renderHud's heightClampTranslation)
            // while the top bar is shown, so mirror that — using topReserved as the offset here made
            // the drag box drift from the real HUD when the font scale isn't 1. When the top bar is
            // hidden (topReserved == 0) the live HUD switches to the flush model (HudLayout) and can
            // reach the very top, so drop the reserve here too. topAnchored HUDs (custom HUDs) always
            // use the flush model, so yMin = 0.
            this.yMin = (topAnchored || topReserved == 0) ? 0 : Math.round(3 * padding * scale);
            this.yMax = Math.max(this.yMin, screenHeight - this.height);
        }

        this.setX(boxXFromPercent(xPercent, screenWidth));
        this.setY(boxYFromPercent(yPercent, screenHeight));
        this.originalX = getX();
        this.originalY = getY();
    }

    /// Box left edge for the given anchor percentage, clamped to the drag bounds. The percentage is
    /// always measured from the screen's left edge for every alignment; the alignment only decides
    /// which point of the box ({@link #anchorOffsetX}) sits at that x, so switching alignment keeps
    /// the same x and the box just grows in a different direction rather than jumping across.
    private int boxXFromPercent(int percent, int screenWidth) {
        float anchorScreenX = percent / 100f * screenWidth;
        return Math.clamp((long) (anchorScreenX - anchorOffsetX), xMin, xMax);
    }

    /// Box top edge for the given anchor percentage, clamped to the drag bounds.
    private int boxYFromPercent(int percent, int screenHeight) {
        float top = alignment == Alignment.CENTER
                ? percent / 100f * screenHeight - this.height / 2f
                : yMin + percent / 100f * (yMax - yMin);
        return Math.clamp((long) top, yMin, yMax);
    }

    /// Anchor percentage (0-100) for the box's current anchored point, measured from the screen's
    /// left edge for every alignment (see {@link #boxXFromPercent}).
    private int percentXFromBox(int screenWidth) {
        float pct = (getX() + anchorOffsetX) / (float) screenWidth * 100f;
        return Math.clamp((long) pct, 0, 100);
    }

    /// Anchor percentage (0-100) for the box's current top edge.
    private int percentYFromBox(int screenHeight) {
        float pct;
        if (alignment == Alignment.CENTER) {
            pct = (getY() + this.height / 2f) / screenHeight * 100f;
        } else {
            int range = yMax - yMin;
            pct = range <= 0 ? 0 : (getY() - yMin) / (float) range * 100f;
        }
        return Math.clamp((long) pct, 0, 100);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int alphaInt = (int) ((40 / 100f) * 255f) << 24;
        boolean rightAlignment = alignment == Alignment.RIGHT;

        context.fill(getX(), getY(), getX() + width, getY() + height, this.isHovered() ? alphaInt | 0xFFFFFF : alphaInt);

        if (alignment == Alignment.CENTER) {
            // Anchor line through the centre so it's clear where the element is pinned.
            context.fill(getX() + width / 2, getY(), getX() + width / 2 + 1, getY() + height, 0xFFFFFFFF);
        } else {
            context.fill(rightAlignment ? getX() + width - 1 : getX(), getY(), rightAlignment ? getX() + width : getX() + 1, getY() + height, 0xFFFFFFFF);
        }

        context.drawText(textRenderer, text, getX() + width / 2 - textRenderer.getWidth(text) / 2, getY() + height / 2 - textRenderer.fontHeight, 0xFFFFFFFF, true);
        context.drawText(textRenderer,
                TextHelper.concat(
                        Text.literal("X: ").formatted(Formatting.GRAY),
                        Text.literal(xPercent + "% ").formatted(Formatting.YELLOW),
                        Text.literal("Y: ").formatted(Formatting.GRAY),
                        Text.literal(yPercent + "%").formatted(Formatting.YELLOW)

                ),
                getX() + width / 2 - textRenderer.getWidth(text) / 2, getY() + height / 2 + 1, 0xFFFFFFFF, true);

        if (alignment == Alignment.CENTER) {
            return;
        }

        context.getMatrices().pushMatrix();
        try {
            context.getMatrices().translate(rightAlignment ? getX() + width : getX(), getY());
            context.getMatrices().rotate((float) Math.toRadians(rightAlignment ? 90.0 : -90.0));
            Text alignmentText = Text.literal(rightAlignment ? "ʀɪɢʜᴛ" : "ʟᴇꜰᴛ").formatted(Formatting.GRAY, Formatting.ITALIC);
            context.drawText(textRenderer, alignmentText, rightAlignment ? height / 2 - textRenderer.getWidth(alignmentText) / 2 : - height / 2 - textRenderer.getWidth(alignmentText) / 2, 0, 0xFFFFFFFF, true);

        } finally {
            context.getMatrices().popMatrix();
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount == 0) {
            return false;
        }

        // Scroll up -> bigger font, scroll down -> smaller font.
        int newFontSize = (int) Math.clamp((long) (fontSize + (verticalAmount > 0 ? 1 : -1)), this.minFontSize, this.maxFontSize);
        if (newFontSize == fontSize) {
            return true;
        }

        this.fontSize = newFontSize;
        recomputeGeometry();
        callbackFontSize.onScroll(this.fontSize);
        return true;
    }

    @Override
    protected void onDrag(Click click, double deltaX, double deltaY) {
        super.onDrag(click, deltaX, deltaY);

        this.deltaX += deltaX;
        this.deltaY += deltaY;

        this.setX(Math.clamp(originalX + (long) this.deltaX, this.xMin, this.xMax));
        this.setY(Math.clamp(originalY + (long) this.deltaY, this.yMin, this.yMax));
    }

    @Override
    public void onRelease(Click click) {
        super.onRelease(click);
        this.originalX = getX();
        this.originalY = getY();
        this.deltaX = 0;
        this.deltaY = 0;

        int screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
        int screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();
        this.xPercent = percentXFromBox(screenWidth);
        this.yPercent = percentYFromBox(screenHeight);

        // Send percentages back
        callbackCoordinates.onRelease(this.xPercent, this.yPercent);
    }
}
