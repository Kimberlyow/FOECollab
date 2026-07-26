package io.github.foecollab.handler;

import io.github.foecollab.FOMC.Types.Defaults;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.util.ItemStackHelper;
import io.github.foecollab.util.TextHelper;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.Objects;

/// Replacement for vanilla {@code DrawContext#drawStackCount}. Draws the stack count
/// in a compact small-caps style and, for FoE bait/lure items (whose visible stack is
/// always 1), pulls the real amount out of the item's {@code counter} NBT instead.
public class ItemStackDisplayHandler {
    private static ItemStackDisplayHandler INSTANCE = new ItemStackDisplayHandler();

    public static ItemStackDisplayHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new ItemStackDisplayHandler();
        }
        return INSTANCE;
    }

    public void drawStackCount(DrawContext context, TextRenderer textRenderer, ItemStack stack, int x, int y, String countOverride) {
        FOEConfig config = FOEConfig.getConfig();
        boolean small = config.itemStackDisplay.useSmallStackCountNumber;

        String text;
        if (countOverride != null) {
            // Respect counts the game forced (recipe book, creative, ...); just restyle them.
            text = countOverride;
        } else {
            DisplayCount displayCount = getDisplayCount(stack, config);
            if (displayCount.count() <= 1) {
                return; // matches vanilla: nothing is drawn for a single item
            }
            // Bait/lure counts run into the thousands, so round them to the nearest "K"
            // instead of showing "1.34K"; ordinary stacks keep their exact (small) count.
            text = displayCount.isBait()
                    ? TextHelper.fmntRoundThousands(displayCount.count())
                    : TextHelper.fmnt(displayCount.count());
        }

        String display = small ? TextHelper.smallCaps(text) : text;
        int width = textRenderer.getWidth(display);
        int drawX = x + 19 - 2 - width;
        int drawY = y + 6 + (small ? 4 : 3);

        if (small) {
            drawSmallString(context, textRenderer, display, drawX, drawY);
        } else {
            context.drawText(textRenderer, display, drawX, drawY, 0xFFFFFFFF, true);
        }
    }

    /// The count to draw, and whether it came from a FoE bait/lure {@code counter} (which
    /// reaches the thousands and so gets the rounded "K" display) rather than a plain stack.
    private record DisplayCount(int count, boolean isBait) {}

    private DisplayCount getDisplayCount(ItemStack stack, FOEConfig config) {
        // contains(...) is a cheap component-map lookup; only FoE items carry custom
        // data, so this skips the (deep-copying) getNbt() call for vanilla items — this
        // runs for every rendered slot every frame, so the early-out matters.
        if (config.itemStackDisplay.showStackCountOnBait
                && stack.contains(DataComponentTypes.CUSTOM_DATA)) {
            NbtCompound nbt = ItemStackHelper.getNbtView(stack); // read-only; no deep copy
            if (nbt != null) {
                String type = nbt.getString("type").orElse("");
                if (Objects.equals(type, Defaults.ItemTypes.BAIT) || Objects.equals(type, Defaults.ItemTypes.LURE)) {
                    return new DisplayCount(nbt.getInt("counter").orElse(stack.getCount()), true);
                }
            }
        }
        return new DisplayCount(stack.getCount(), false);
    }

    /// Draws an already-small-capped string glyph-by-glyph, nudging each glyph so the
    /// (low-sitting) subscript digits line up with the small-caps letters at the bottom
    /// of the slot.
    private void drawSmallString(DrawContext context, TextRenderer textRenderer, String text, int x, int y) {
        int cursorX = x;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String glyph = String.valueOf(c);

            int offsetY = TextHelper.isSmallNumber(c) ? 1 : 0;
            int translateY = (TextHelper.isSmallNumber(c) || TextHelper.isSmallLetter(c)) ? -1 : 0;

            context.drawText(textRenderer, glyph, cursorX, y - offsetY + translateY, 0xFFFFFFFF, true);
            cursorX += textRenderer.getWidth(glyph);
        }
    }
}
