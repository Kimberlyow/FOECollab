package io.github.foecollab.screens.hud;

import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.FOMC.Types.FOMCItem;
import io.github.foecollab.FOMC.Types.Pet;
import io.github.foecollab.common.FlairDecor;
import io.github.foecollab.common.Theming;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.config.HudAlignment;
import io.github.foecollab.config.TrackerPetEquipHUDConfig.PetEquipTracker.ActivePetHUDOptions;
import io.github.foecollab.handler.PetEquipHandler;
import io.github.foecollab.handler.ProfileDataHandler;
import io.github.foecollab.handler.ThemingHandler;
import io.github.foecollab.handler.screens.hud.PetEquipHudHandler;
import io.github.foecollab.handler.screens.hud.PetEquipHudHandler.PetRow;
import io.github.foecollab.util.TextHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class PetEquipHud {
    // XP progress bar (bar mode) geometry, in the HUD's pre-scale pixels.
    private static final int BAR_GAP = 3;
    private static final int BAR_MIN_WIDTH = 40;
    private static final int BAR_HEIGHT = 1;
    // Empty-track colour and the darker shadow row drawn beneath it.
    private static final int TRACK_COLOR = 0x80000000;
    private static final int TRACK_SHADOW_COLOR = 0xB0000000;

    public void render(DrawContext drawContext, MinecraftClient client) {
        FOEConfig config = FOEConfig.getConfig();
        ActivePetHUDOptions opts = config.petEquipTracker.activePetHUDOptions;
        TextRenderer textRenderer = client.textRenderer;

        // Assemble all rows (text lines + optional XP bar; cached + recolored in the handler)
        List<PetRow> rows = PetEquipHudHandler.instance().assemblePetRows();
        ItemStack activePet = PetEquipHandler.instance().currentPetItem;
        ItemStack activePetItem = PetEquipHandler.instance().currentPetItemItem;

        drawContext.getMatrices().pushMatrix();
        try {
            // Get screen size
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();

            HudAlignment alignment = opts.alignment;
            boolean center = alignment == HudAlignment.CENTER;
            // RIGHT mirrors the whole layout: icons on the right, text right-aligned to their left.
            boolean mirrored = alignment == HudAlignment.RIGHT;

            // hudX is the anchor's position from the screen's left edge for every alignment;
            // alignment only picks which point of the box sits there (RIGHT pins the right edge and
            // mirrors the layout), so changing alignment keeps the HUD in place instead of jumping.
            float xPercent = opts.hudX / 100f;
            float yPercent = opts.hudY / 100f;

            // Calculate base positions relative to screen size
            int baseX = (int) (screenWidth * xPercent);
            int baseY = (int) (screenHeight * yPercent);

            // Scaling setup
            int fontSize = opts.fontSize;
            float scale = fontSize / 10.0f;
            drawContext.getMatrices().scale(scale, scale);

            // Alpha
            int alphaInt = (int) ((opts.backgroundOpacity / 100f) * 255f) << 24;

            int lineSpacing = 2;
            int lineHeight = (int) (textRenderer.fontHeight + (lineSpacing / scale));
            int scaledX = (int) (baseX / scale);
            int scaledY = (int) (baseY / scale);
            int padding = 8;
            int itemBoxGap = 4;
            boolean itemBoxVisible = opts.showItemIcon && activePetItem != null;
            int extraBoxWidth = itemBoxVisible ? (16 + padding + itemBoxGap) : 0;

            // Widest row. A bar row needs room for its level label plus a minimum bar.
            int maxLength = 0;
            for (PetRow row : rows) {
                int width = row.isBar()
                        ? barRowMinWidth(textRenderer, row)
                        : textRenderer.getWidth(row.text());
                maxLength = Math.max(maxLength, width);
            }

            // Full content width: pet icon + gap (+ item box) + text block.
            int contentWidth = 16 + padding + extraBoxWidth + maxLength;
            int heightClampTranslation = HudLayout.heightClampTranslation(padding, padding * 2 + rows.size() * lineHeight, yPercent);

            // Layout anchors. scaledX is the box center (CENTER) or the box's outer left/right edge.
            int contentLeft;
            if (center) {
                contentLeft = scaledX - contentWidth / 2;
            } else if (mirrored) {
                contentLeft = scaledX - padding - contentWidth;
            } else {
                contentLeft = scaledX + padding;
            }
            int boxLeft = contentLeft - padding;
            int boxRight = contentLeft + contentWidth + padding;

            // Icon column (pet icon plus the optional item box) and the text block beside it.
            int petIconLeft = mirrored ? contentLeft + contentWidth - 16 : contentLeft;
            int itemIconLeft = mirrored ? petIconLeft - (16 + padding + itemBoxGap) : petIconLeft + 16 + padding + itemBoxGap;
            int textLeft = mirrored ? contentLeft : contentLeft + 16 + padding + extraBoxWidth;
            int textRight = textLeft + maxLength;

            // Draw Background
            Constant ratingTag = Constant.DEFAULT;
            if (opts.colorPetBorderToRating
                    && ProfileDataHandler.instance().profileData != null
                    && ProfileDataHandler.instance().profileData.equippedPet != null) {
                ratingTag = Pet.getConstantFromPercent(
                        ProfileDataHandler.instance().profileData.equippedPet.percentPetRating);
            }
            int borderColor = opts.colorPetBorderToRating ? ratingTag.COLOR : 0xFFFFFF;
            drawContext.fill(boxLeft, scaledY - heightClampTranslation, boxRight,
                    scaledY + ((rows.size() - 1) * lineHeight) + padding * 3 - heightClampTranslation,
                    alphaInt);
            drawContext.drawStrokedRectangle(petIconLeft - padding / 2,
                    scaledY + padding + 1 - padding / 2 - heightClampTranslation, 16 + padding, 16 + padding,
                    0xFF000000 | borderColor);
            if (itemBoxVisible) {
                // Match the pet box: when rating-coloring is on, color this border by
                // the pet item's rarity.
                int itemBorderColor = 0xFFFFFF;
                if (opts.colorPetBorderToRating) {
                    Constant itemRarity = FOMCItem.getRarity(activePetItem);
                    if (itemRarity != Constant.DEFAULT) {
                        itemBorderColor = itemRarity.COLOR;
                    }
                }
                drawContext.drawStrokedRectangle(itemIconLeft - padding / 2,
                        scaledY + padding + 1 - padding / 2 - heightClampTranslation, 16 + padding,
                        16 + padding, 0xFF000000 | itemBorderColor);
            }

            if (PetEquipHandler.instance().petStatus == PetEquipHandler.PetStatus.HAS_PET) {
                // Draw Pet and Pet Item
                drawContext.drawItem(activePet, petIconLeft, scaledY + padding + 1 - heightClampTranslation);
                if (itemBoxVisible) {
                    drawContext.drawItem(activePetItem, itemIconLeft,
                            scaledY + padding + 1 - heightClampTranslation);
                }
            }

            // Theming
            FlairDecor flairDecor = ThemingHandler.instance().flairDecorPetEquip;

            if (config.theme.themeType != Theming.ThemeType.OFF) {
                Theming theme = ThemingHandler.instance().currentTheme;
                int colorOverlay = config.theme.colorOverlay;
                int themeTextColor = ThemingHandler.instance().currentThemeType.TEXT_COLOR;
                int alphaOverlay = (int) ((config.theme.opacity / 100f) * 255f) << 24;

                // Corners
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   theme.GUI_TOP_LEFT,
                        boxLeft - padding, scaledY - padding - heightClampTranslation, 16, 16,
                        alphaOverlay | colorOverlay);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   theme.GUI_TOP_RIGHT,
                        boxRight - padding,
                        scaledY - padding - heightClampTranslation, 16, 16, alphaOverlay | colorOverlay);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   theme.GUI_BOTTOM_LEFT,
                        boxLeft - padding,
                        scaledY + padding * 2 + ((rows.size() - 1) * lineHeight) - heightClampTranslation, 16, 16,
                        alphaOverlay | colorOverlay);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   theme.GUI_BOTTOM_RIGHT,
                        boxRight - padding,
                        scaledY + padding * 2 + ((rows.size() - 1) * lineHeight) - heightClampTranslation, 16, 16,
                        alphaOverlay | colorOverlay);

                // Sides
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   theme.GUI_LEFT,
                        boxLeft - padding, scaledY + padding - heightClampTranslation, 16,
                        ((rows.size() - 1) * lineHeight) + padding, alphaOverlay | colorOverlay);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   theme.GUI_RIGHT,
                        boxRight - padding,
                        scaledY + padding - heightClampTranslation, 16, ((rows.size() - 1) * lineHeight) + padding,
                        alphaOverlay | colorOverlay);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   theme.GUI_TOP,
                        contentLeft, scaledY - padding - heightClampTranslation,
                        contentWidth, 16, alphaOverlay | colorOverlay);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   theme.GUI_BOTTOM,
                        contentLeft,
                        scaledY + padding * 2 + ((rows.size() - 1) * lineHeight) - heightClampTranslation,
                        contentWidth, 16, alphaOverlay | colorOverlay);

                // Title (configurable text, rendered in small-caps to match the HUD style)
                if (opts.showTitle && opts.titleText != null && !opts.titleText.isBlank()) {
                    Text title = Text.literal(TextHelper.smallCaps(opts.titleText))
                            .withColor(themeTextColor).formatted(Formatting.BOLD);
                    int titleWidth = textRenderer.getWidth(title);
                    int titleX = contentLeft + (contentWidth - titleWidth) / 2;
                    drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   theme.GUI_TEXT_LEFT,
                            titleX - 16,
                            scaledY - padding - heightClampTranslation, 16, 16, alphaOverlay | colorOverlay);
                    drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   theme.GUI_TEXT_MIDDLE,
                            titleX,
                            scaledY - padding - heightClampTranslation, titleWidth, 16,
                            alphaOverlay | colorOverlay);
                    drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   theme.GUI_TEXT_RIGHT,
                            titleX + titleWidth,
                            scaledY - padding - heightClampTranslation, 16, 16, alphaOverlay | colorOverlay);
                    drawContext.drawText(textRenderer, title, titleX,
                            scaledY - textRenderer.fontHeight / 2 - heightClampTranslation - 1, themeTextColor, false);
                }
            }

            // Flair
            drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   flairDecor.GUI_FLAIR_TOP_LEFT,
                    boxLeft - padding - 24, scaledY - padding - heightClampTranslation - 24, 64,
                    64);
            drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   flairDecor.GUI_FLAIR_TOP_RIGHT,
                    boxRight - padding - 24,
                    scaledY - padding - heightClampTranslation - 24, 64, 64);
            drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   flairDecor.GUI_FLAIR_BOTTOM_LEFT,
                    boxLeft - padding - 24,
                    scaledY + padding * 2 + ((rows.size() - 1) * lineHeight) - heightClampTranslation - 24, 64, 64);
            drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   flairDecor.GUI_FLAIR_BOTTOM_RIGHT,
                    boxRight - padding - 24,
                    scaledY + padding * 2 + ((rows.size() - 1) * lineHeight) - heightClampTranslation - 24, 64, 64);

            // Draw rows (text lines + the XP bar)
            for (int i = 0; i < rows.size(); i++) {
                PetRow row = rows.get(i);
                int lineY = scaledY + (i * lineHeight) + padding - heightClampTranslation;
                if (row.isBar()) {
                    drawXpBar(drawContext, textRenderer, row, opts, textLeft, textRight, lineY);
                } else {
                    Text text = row.text();
                    int textWidth = textRenderer.getWidth(text);
                    int x = center ? textLeft + (maxLength - textWidth) / 2 : mirrored ? textRight - textWidth : textLeft;
                    drawContext.drawText(textRenderer, text, x, lineY, 0xFFFFFFFF, true);
                }
            }
        } finally {
            drawContext.getMatrices().popMatrix();
        }
    }

    /// Minimum width a bar row occupies: its level label, a gap, and a minimum bar length.
    private int barRowMinWidth(TextRenderer textRenderer, PetRow row) {
        int labelWidth = textRenderer.getWidth(row.text());
        return (labelWidth > 0 ? labelWidth + BAR_GAP : 0) + BAR_MIN_WIDTH;
    }

    /// Draws the level label (left) and the XP progress bar filling the rest of the text column.
    private void drawXpBar(DrawContext drawContext, TextRenderer textRenderer, PetRow row,
            ActivePetHUDOptions opts, int textLeft, int textRight, int lineY) {
        Text label = row.text();
        int labelWidth = textRenderer.getWidth(label);
        if (labelWidth > 0) {
            drawContext.drawText(textRenderer, label, textLeft, lineY, 0xFFFFFFFF, true);
        }
        int barX = textLeft + (labelWidth > 0 ? labelWidth + BAR_GAP : 0);
        int barEndX = textRight;
        if (barEndX <= barX) {
            return;
        }
        int barTop = lineY + (textRenderer.fontHeight - BAR_HEIGHT) / 2;
        int barBottom = barTop + BAR_HEIGHT;

        // Thin tooltip-style bar, no outline: the coloured fill for the filled portion and a dark
        // track for the empty portion, each with a 1px shadow row directly beneath it in a darker
        // shade of its OWN colour — so both segments read as raised, like the vanilla XP bar.
        int fillWidth = Math.round((barEndX - barX) * row.barProgress());
        int split = barX + fillWidth;
        if (fillWidth > 0) {
            drawContext.fill(barX, barTop, split, barBottom, row.barColor());
            drawContext.fill(barX, barBottom, split, barBottom + 1, darker(row.barColor()));
        }
        if (split < barEndX) {
            drawContext.fill(split, barTop, barEndX, barBottom, TRACK_COLOR);
            drawContext.fill(split, barBottom, barEndX, barBottom + 1, TRACK_SHADOW_COLOR);
        }

        // Optional small number/percent overlaid on the bar (default off)
        if (opts.showXpBarText) {
            Pet equippedPet = ProfileDataHandler.instance().profileData != null
                    ? ProfileDataHandler.instance().profileData.equippedPet : null;
            String overlayStr;
            if (equippedPet != null && equippedPet.lvl >= 100) {
                // Maxed: the bar stays full, so show the overflow XP that keeps accruing
                // instead of a flat "MAX".
                overlayStr = TextHelper.fmnt(equippedPet.currentXp);
            } else if (row.barProgress() >= 1f) {
                overlayStr = "MAX";
            } else {
                overlayStr = TextHelper.fmt(row.barProgress() * 100f, 1) + "%";
            }
            Text overlay = Text.literal(overlayStr).formatted(Formatting.WHITE);
            int overlayX = barX + ((barEndX - barX) - textRenderer.getWidth(overlay)) / 2;
            drawContext.drawText(textRenderer, overlay, overlayX, lineY, 0xFFFFFFFF, true);
        }
    }

    /// A darker shade of an ARGB colour (half-bright RGB, same alpha) for a bar segment's shadow row.
    private static int darker(int argb) {
        int a = argb & 0xFF000000;
        int r = ((argb >> 16) & 0xFF) / 2;
        int g = ((argb >> 8) & 0xFF) / 2;
        int b = (argb & 0xFF) / 2;
        return a | (r << 16) | (g << 8) | b;
    }
}
