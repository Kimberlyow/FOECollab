package io.github.foecollab.handler.screens.hud;

import io.github.foecollab.FOMC.Types.Bait;
import io.github.foecollab.FOMC.Types.FOMCItem;
import io.github.foecollab.FOMC.Types.Lure;
import io.github.foecollab.common.HudFont;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.handler.FishingRodHandler;
import io.github.foecollab.util.TextHelper;
import io.github.foecollab.util.ThrottledCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BaitHudHandler {
    private static BaitHudHandler INSTANCE = new BaitHudHandler();

    private final ThrottledCache<Text> baitTextCache =
            new ThrottledCache<>(200L, () -> HudFont.recolor(this.buildBaitText()));

    public static BaitHudHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new BaitHudHandler();
        }
        return INSTANCE;
    }

    public Text assembleBaitText() {
        return baitTextCache.get();
    }

    private Text buildBaitText() {
        if (FishingRodHandler.instance().isTackleboxDisabled(MinecraftClient.getInstance())) {
            return Text.literal("");
        }

        FOMCItem active = getActiveBait();
        if (active == null) {
            return Text.literal("");
        }

        var fishingRod = FishingRodHandler.instance().fishingRod;
        if (active instanceof Lure firstLure && FOEConfig.getConfig().baitTracker.calculateLures) {
            int lureQty = firstLure.calculateLures(fishingRod.tacklebox);

            if (lureQty > 0) {
                return TextHelper.concat(
                        Text.literal(TextHelper.upperCaseAllFirstCharacter(firstLure.name)).formatted(Formatting.WHITE),
                        Text.literal(": ").formatted(Formatting.GRAY),
                        Text.literal(String.valueOf(lureQty)).formatted(Formatting.WHITE),
                        Text.literal("x").formatted(Formatting.GRAY));
            }
        }

        return TextHelper.concat(
                active instanceof Bait bait
                        ? Text.literal(TextHelper.upperCaseAllFirstCharacter(bait.name)).formatted(Formatting.WHITE)
                        : active instanceof Lure lure
                                ? Text.literal(TextHelper.upperCaseAllFirstCharacter(lure.name)).formatted(Formatting.WHITE)
                                : Text.empty(),
                Text.literal(": ").formatted(Formatting.GRAY),
                active instanceof Bait bait
                        ? Text.literal(String.valueOf(bait.counter)).formatted(Formatting.WHITE)
                        : active instanceof Lure lure
                                ? Text.literal(String.valueOf(lure.counter)).formatted(Formatting.WHITE)
                                : Text.empty(),
                Text.literal("x").formatted(Formatting.GRAY));
    }

    public CustomModelDataComponent getModelData() {
        if (FishingRodHandler.instance().isTackleboxDisabled(MinecraftClient.getInstance())) {
            return CustomModelDataComponent.DEFAULT;
        }

        FOMCItem active = getActiveBait();
        return active instanceof Bait bait ? bait.customModelData
                : active instanceof Lure lure ? lure.customModelData
                : CustomModelDataComponent.DEFAULT;
    }

    /// The bait/lure to show in the HUD: the rod's equipped {@code activeBait}, or null when the
    /// active bait slot is empty (tacklebox contents that aren't equipped are not shown); see
    /// {@link io.github.foecollab.FOMC.Types.FishingRod#getActiveBaitItem()}.
    private FOMCItem getActiveBait() {
        var fishingRod = FishingRodHandler.instance().fishingRod;
        return fishingRod == null ? null : fishingRod.getActiveBaitItem();
    }
}
