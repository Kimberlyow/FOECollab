package io.github.foecollab.FOMC.Types;

import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.util.ItemStackHelper;
import io.github.foecollab.util.UUIDHelper;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class FishingRod extends FOMCItem {
    public final String name;
    public final CustomModelDataComponent customModelData;
    public final boolean soulboundRod;
    public final String skin;
    public final UUID owner;
    public final List<FOMCItem> tacklebox;
    /// The bait/lure currently equipped on the rod. The server moved this out of the
    /// tacklebox into its own NBT list, so it (not {@code tacklebox.getFirst()}) is the
    /// source of truth for what's actively in use.
    public final List<FOMCItem> activeBait;
    public final Line line;
    public final Pole pole;
    public final Reel reel;

    private FishingRod(NbtCompound nbtCompound, String type, CustomModelDataComponent customModelData, String name) {
        super(type, Constant.DEFAULT);
        this.name = name;
        this.customModelData = customModelData;
        this.soulboundRod = nbtCompound.getBoolean("soulbound_rod").orElse(false);
        this.skin = nbtCompound.getString("skin").orElse("");
        this.owner = UUIDHelper.getUUID(nbtCompound.getIntArray("uuid").orElse(new int[0]));

        this.tacklebox = parseBaitList(nbtCompound, "tacklebox");
        this.activeBait = parseBaitList(nbtCompound, "activeBait");

        if(nbtCompound.get("line") instanceof NbtList nbtList) {
            this.line = nbtList.stream().map(nbtElement -> {
                if(nbtElement instanceof NbtCompound compound) {
                    ItemStack lineStack = ItemStackHelper.nbtToItemStack(compound);
                    return Line.getLine(lineStack);
                } return null;
            }).filter(Objects::nonNull).findFirst().orElse(null);
        } else this.line = null;

        if(nbtCompound.get("pole") instanceof NbtList nbtList) {
            this.pole = nbtList.stream().map(nbtElement -> {
                if(nbtElement instanceof NbtCompound compound) {
                    ItemStack poleStack = ItemStackHelper.nbtToItemStack(compound);
                    return Pole.getPole(poleStack);
                } return null;
            }).filter(Objects::nonNull).findFirst().orElse(null);
        } else this.pole = null;

        if(nbtCompound.get("reel") instanceof NbtList nbtList) {
            this.reel = nbtList.stream().map(nbtElement -> {
                if(nbtElement instanceof NbtCompound compound) {
                    ItemStack reelStack = ItemStackHelper.nbtToItemStack(compound);
                    return Reel.getReel(reelStack);
                } return null;
            }).filter(Objects::nonNull).findFirst().orElse(null);
        } else this.reel = null;
    }

    /// The bait/lure currently equipped: the first {@code activeBait} entry, or null when the
    /// active bait slot is empty. Bait sitting in the {@code tacklebox} but not equipped is NOT in
    /// use by the server, so it is deliberately ignored here (no tacklebox fallback) — an empty
    /// active slot means "no bait", even if the tacklebox still holds some.
    public FOMCItem getActiveBaitItem() {
        return activeBait.isEmpty() ? null : activeBait.getFirst();
    }

    /// Parses an NBT list of serialized bait/lure item stacks (e.g. {@code tacklebox} or
    /// {@code activeBait}) into the corresponding {@link Lure}/{@link Bait} models.
    private static List<FOMCItem> parseBaitList(NbtCompound nbtCompound, String key) {
        if (nbtCompound.get(key) instanceof NbtList nbtList) {
            return nbtList.stream().map(nbtElement -> {
                if (nbtElement instanceof NbtCompound compound) {
                    ItemStack baitStack = ItemStackHelper.nbtToItemStack(compound);
                    Lure lure = Lure.getLure(baitStack);
                    Bait bait = Bait.getBait(baitStack);
                    if (lure != null) return lure;
                    return bait;
                }
                return null;
            }).filter(Objects::nonNull).toList();
        }
        return new ArrayList<>();
    }

    public static FishingRod getFishingRod(ItemStack itemStack, String type, String name) {
        return new FishingRod(Objects.requireNonNull(ItemStackHelper.getNbt(itemStack)), type, itemStack.get(DataComponentTypes.CUSTOM_MODEL_DATA), name);
    }

    public static FishingRod getFishingRod(ItemStack itemStack) {
        if(itemStack.get(DataComponentTypes.LORE) != null
                && itemStack.get(DataComponentTypes.CUSTOM_DATA) != null
                && !Objects.requireNonNull(ItemStackHelper.getNbt(itemStack)).getBoolean("shopitem").orElse(false)) {
            if (itemStack.getItem() == Items.FISHING_ROD) {
                return FishingRod.getFishingRod(itemStack, Defaults.ItemTypes.FISHINGROD, itemStack.getName().getString());
            }
        }
        return null;
    }

    public static boolean isTackleboxDisabled(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) return false;
        NbtCompound nbt = ItemStackHelper.getNbt(itemStack);
        if (nbt == null) return false;
        return nbt.getInt("disableBait").orElse(0) == 1;
    }
}
