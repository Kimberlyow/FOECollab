package io.github.foecollab.handler;

import io.github.foecollab.config.BaitSorterConfig;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.util.SortPlanner;
import net.minecraft.client.MinecraftClient;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Merges identical baits and lures (same name, color and size) and sorts baits and lures by rarity in the player inventory (and the open
 * chest or vault). Click driven: one press queues the work, the clicks are then sent a few per tick
 * (see {@link BaitSorterConfig.BaitSorter#clickSpeed}). Only player inventory slots 0-35 are touched.
 */
public class BaitSorterHandler {
    private static BaitSorterHandler INSTANCE = new BaitSorterHandler();

    /** Ticks to wait after a stage so the server's answers have arrived before the next scan. */
    private static final int TICKS_BETWEEN_STAGES = 6;

    private final ArrayDeque<List<SortPlanner.Click>> queue = new ArrayDeque<>();
    /** Stages run in order; each scans the screen fresh when it starts, because the server decides where a shift clicked bait lands. */
    private final ArrayDeque<Supplier<List<List<SortPlanner.Click>>>> stages = new ArrayDeque<>();
    private int syncId = -1;
    private int cooldown = 0;
    /** Click groups we may still send; grows by clickSpeed / 2 every tick. */
    private double credit = 0;

    public static BaitSorterHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new BaitSorterHandler();
        }
        return INSTANCE;
    }

    private boolean busy() {
        return !queue.isEmpty() || !stages.isEmpty();
    }

    private boolean commonFirst() {
        return FOEConfig.getConfig().baitSorter.sortOrder == BaitSorterConfig.SortOrder.COMMON_TO_LEGENDARY;
    }

    /** Hooks the sort keybind into a freshly opened container screen (keybinds do not fire while a screen is open). */
    public void onScreenInit(Screen screen) {
        if (!(screen instanceof HandledScreen<?>) || screen instanceof CreativeInventoryScreen) {
            return;
        }
        ScreenKeyboardEvents.afterKeyPress(screen).register((s, input) -> {
            // Typing in a search box must not start a sort.
            if (s.getFocused() instanceof TextFieldWidget) {
                return;
            }
            if (KeybindHandler.instance().sortBaits.matchesKey(input)) {
                start(MinecraftClient.getInstance());
            }
        });
        ScreenEvents.remove(screen).register(s -> {
            queue.clear();
            stages.clear();
        });
    }

    public void start(MinecraftClient mc) {
        if (busy() || !(mc.currentScreen instanceof HandledScreen<?> screen)
                || screen instanceof CreativeInventoryScreen) {
            return;
        }
        ScreenHandler handler = screen.getScreenHandler();
        syncId = handler.syncId;
        boolean container = handler instanceof GenericContainerScreenHandler;
        boolean commonFirst = commonFirst();

        // Inventory baits first, so there is one bait per name to carry into the chest.
        stages.add(() -> SortPlanner.plan(scan(true), true, false, commonFirst));
        if (container) {
            // Merge inventory baits into their stashed twins and shift them back in.
            stages.add(() -> SortPlanner.planCrossMerge(scan(false), scan(true)));
        }
        stages.add(() -> SortPlanner.plan(scan(true), true, true, commonFirst));
        if (container) {
            // Baits cannot stack in a chest or vault, so only rearrange there.
            stages.add(() -> SortPlanner.plan(scan(false), false, true, commonFirst));
        }
        cooldown = 0;
        credit = 0;
    }

    /** Baits and lures in the open screen: the player's main inventory and hotbar when {@code playerSide}, otherwise the chest or vault slots. */
    private List<SortPlanner.Entry> scan(boolean playerSide) {
        List<SortPlanner.Entry> found = new ArrayList<>();
        if (!(MinecraftClient.getInstance().currentScreen instanceof HandledScreen<?> screen)) {
            return found;
        }
        for (Slot slot : screen.getScreenHandler().slots) {
            boolean mine = slot.inventory instanceof PlayerInventory;
            // Player side is index 0-35 only: never armor or offhand. Chest side is everything else.
            if (mine != playerSide || (mine && slot.getIndex() >= 36)) {
                continue;
            }
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) {
                continue;
            }
            var data = stack.get(DataComponentTypes.CUSTOM_DATA);
            if (data == null) {
                continue;
            }
            NbtCompound nbt = data.copyNbt();
            found.add(new SortPlanner.Entry(slot.id, nbt.getString("type").orElse(""),
                    nbt.getString("name").orElse(""), nbt.getString("rarity").orElse(""),
                    nbt.getString("color").orElse(""), nbt.getString("size").orElse("")));
        }
        return found;
    }

    public void tick(MinecraftClient mc) {
        if (!busy()) {
            return;
        }
        // Stop between groups (never inside one) if the screen went away.
        if (!(mc.currentScreen instanceof HandledScreen<?> screen)
                || screen.getScreenHandler().syncId != syncId || mc.player == null) {
            queue.clear();
            stages.clear();
            return;
        }
        if (cooldown-- > 0) {
            return;
        }
        credit += FOEConfig.getConfig().baitSorter.clickSpeed / 2.0;
        while (credit >= 1 && busy()) {
            if (queue.isEmpty()) {
                queue.addAll(stages.poll().get());
                if (queue.isEmpty()) {
                    continue;
                }
            }
            for (SortPlanner.Click click : queue.poll()) {
                mc.interactionManager.clickSlot(syncId, click.slot(), 0,
                        click.shift() ? SlotActionType.QUICK_MOVE : SlotActionType.PICKUP, mc.player);
            }
            credit--;
            if (queue.isEmpty()) {
                cooldown = TICKS_BETWEEN_STAGES;
                credit = 0;
                return;
            }
        }
    }
}
