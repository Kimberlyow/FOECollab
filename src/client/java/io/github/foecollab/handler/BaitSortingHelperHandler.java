package io.github.foecollab.handler;

import io.github.foecollab.FOMC.Types.FOMCItem;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.util.ColorHelper;
import io.github.foecollab.util.ItemStackHelper;
import io.github.foecollab.util.ThrottledCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.HashMap;
import java.util.Map;

public class BaitSortingHelperHandler {
    private static BaitSortingHelperHandler INSTANCE = new BaitSortingHelperHandler();

    public boolean toggle = false;

    // The bait counts only change when items move, but renderItemMarker runs once per slot per
    // frame. Recomputing the whole inventory scan (with an NBT read per slot) for every slot every
    // frame was the source of the lag — O(slots^2) per frame. Cache the counts and rebuild a few
    // times a second regardless of framerate.
    private final ThrottledCache<Map<String, Integer>> baitCountCache =
            new ThrottledCache<>(150, BaitSortingHelperHandler::computeBaitCounts);

    public static BaitSortingHelperHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new BaitSortingHelperHandler();
        }
        return INSTANCE;
    }

    public void loadFromProfile() {
        if (ProfileDataHandler.instance().isDataLoaded) {
            this.toggle = ProfileDataHandler.instance().profileData.baitSortingHelperToggle;
        }
    }

    public void setToggle(boolean value) {
        this.toggle = value;
        if (ProfileDataHandler.instance().isDataLoaded) {
            ProfileDataHandler.instance().profileData.baitSortingHelperToggle = value;
        }
    }

    public void renderItemMarker(DrawContext drawContext, ItemStack itemStack, int x, int y) {
        boolean showOnlyWhilePressingKeybind = FOEConfig
                .getConfig().baitSortingHelperVisibility.showOnlyWhilePressingKeybind;

        // When the keybind-hold mode is off, the helper is always on; otherwise it shows only
        // while the keybind is held.
        boolean isActive = !showOnlyWhilePressingKeybind
                || KeybindHandler.instance().visualizeBaitSorting;

        if (!isActive) {
            return;
        }

        if (!FOMCItem.isFOMCItem(itemStack)) {
            return;
        }

        if (!isBait(itemStack)) {
            return;
        }

        Map<String, Integer> baitCounts = baitCountCache.get();
        String baitKey = getBaitKey(itemStack);

        if (baitCounts.getOrDefault(baitKey, 0) > 1) {
            int alphaInt = (int) (0.6f * 255f) << 24;
            int rgb = ColorHelper.getClrFromString(baitKey);

            drawContext.getMatrices().pushMatrix();
            try {
                drawContext.getMatrices().translate(0, 0);
                drawContext.fill(x, y, x + 16, y + 16,
                        alphaInt | rgb);
            } finally {
                drawContext.getMatrices().popMatrix();
            }
        }
    }

    private static boolean isBait(ItemStack itemStack) {
        NbtCompound data = ItemStackHelper.getNbtView(itemStack);
        if (data == null) {
            return false;
        }
        if (!data.contains("type")) {
            return false;
        }
        return "bait".equalsIgnoreCase(data.getString("type").orElse(""));
    }

    private static String getBaitKey(ItemStack itemStack) {
        NbtCompound data = ItemStackHelper.getNbtView(itemStack);
        if (data != null && data.contains("name")) {
            String nbtName = data.getString("name").orElse("");
            if (!nbtName.isBlank()) {
                return nbtName.toLowerCase();
            }
        }
        return itemStack.getName().getString().toLowerCase();
    }

    private static Map<String, Integer> computeBaitCounts() {
        Map<String, Integer> counts = new HashMap<>();

        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (!(screen instanceof HandledScreen<?> handledScreen)) {
            return counts;
        }

        for (var slot : handledScreen.getScreenHandler().slots) {
            ItemStack stack = slot.getStack();
            if (stack == null || stack.isEmpty() || !FOMCItem.isFOMCItem(stack) || !isBait(stack)) {
                continue;
            }

            String key = getBaitKey(stack);
            counts.put(key, counts.getOrDefault(key, 0) + 1);
        }

        return counts;
    }
}
