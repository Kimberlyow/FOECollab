package io.github.foecollab.handler;

import io.github.foecollab.config.FOEConfig;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.DyeColor;

/**
 * Renders the colored-block buttons of the /quests menu (concrete on the quest-select page,
 * shulker boxes on the difficulty and type pages — all pages share the menu title tracked by
 * {@link QuestHandler#questMenuState}) as flat colored squares instead of 3D items, and lets the
 * slot-click prediction be skipped for them so a clicked "square" never visually jumps onto the
 * cursor while the server processes the click.
 */
public class QuestMenuSquareHandler {
    private static QuestMenuSquareHandler INSTANCE = new QuestMenuSquareHandler();
    private final FOEConfig config = FOEConfig.getConfig();

    // The undyed shulker box has no DyeColor; matches its purple-ish texture tone.
    private static final int PLAIN_SHULKER_COLOR = 0xFF976B97;

    public static QuestMenuSquareHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new QuestMenuSquareHandler();
        }
        return INSTANCE;
    }

    /** Whether this slot holds a quest-menu colored block that should render (and click) as a square. */
    public boolean isSquaredSlot(Slot slot) {
        if (!config.cleanerDisplay.questMenuSquares
                || !LoadingHandler.instance().isOnServer
                || !QuestHandler.instance().questMenuState) {
            return false;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || slot.inventory == client.player.getInventory()) {
            return false;
        }
        Item item = slot.getStack().getItem();
        return Block.getBlockFromItem(item) instanceof ShulkerBoxBlock || concreteColor(item) != null
                || isTerracotta(item);
    }

    /** {@link #isSquaredSlot(Slot)} for a clicked slot id (guards the -999 outside-click id). */
    public boolean isSquaredSlot(ScreenHandler screenHandler, int slotId) {
        return slotId >= 0 && slotId < screenHandler.slots.size() && isSquaredSlot(screenHandler.getSlot(slotId));
    }

    public void render(DrawContext context, Slot slot) {
        context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, squareColor(slot.getStack()));
    }

    private static int squareColor(ItemStack stack) {
        DyeColor color;
        if (Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock shulkerBox) {
            color = shulkerBox.getColor();
        } else if (isTerracotta(stack.getItem())) {
            // Map color, not DyeColor: keeps terracotta's muted tone so e.g. a blue terracotta
            // square stays distinguishable from a blue concrete one.
            return 0xFF000000 | Block.getBlockFromItem(stack.getItem()).getDefaultMapColor().color;
        } else {
            color = concreteColor(stack.getItem());
        }
        return color != null ? 0xFF000000 | color.getEntityColor() : PLAIN_SHULKER_COLOR;
    }

    /** Plain, dyed and glazed terracotta — like concrete, there's no block class to check. */
    private static boolean isTerracotta(Item item) {
        String path = Registries.ITEM.getId(item).getPath();
        return path.equals("terracotta") || path.endsWith("_terracotta");
    }

    /** DyeColor of a {@code <color>_concrete} item, or null — concrete has no block class to check. */
    private static DyeColor concreteColor(Item item) {
        String path = Registries.ITEM.getId(item).getPath();
        if (!path.endsWith("_concrete")) {
            return null;
        }
        return DyeColor.byId(path.substring(0, path.length() - "_concrete".length()), null);
    }
}
