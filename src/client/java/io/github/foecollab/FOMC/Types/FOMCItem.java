package io.github.foecollab.FOMC.Types;

import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.util.ItemStackHelper;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.util.Objects;

public class FOMCItem {
    public final String type;
    public final Constant rarity;

    public FOMCItem(String type, Constant rarity) {
        this.type = type;
        this.rarity = rarity;
    }

    public static FOMCItem getFOMCItem(ItemStack itemStack) {
        // Read-only view: this only inspects fields. The per-type parsers below still
        // take their own (copied) NBT, so a view here can't be mutated downstream.
        NbtCompound nbtCompound = ItemStackHelper.getNbtView(itemStack);
        if(nbtCompound != null && !nbtCompound.getBoolean("shopitem").orElse(false)) {
            if (nbtCompound.contains("type")) {
                return switch (nbtCompound.getString("type").orElse("")) {
                    case Defaults.ItemTypes.PET -> Pet.getPet(itemStack, Defaults.ItemTypes.PET);
                    case Defaults.ItemTypes.SHARD -> Shard.getShard(itemStack, Defaults.ItemTypes.SHARD);
                    case Defaults.ItemTypes.ARMOR -> Armor.getArmor(itemStack, Defaults.ItemTypes.ARMOR);
                    case Defaults.ItemTypes.BAIT -> Bait.getBait(itemStack, Defaults.ItemTypes.BAIT);
                    case Defaults.ItemTypes.LURE -> Lure.getLure(itemStack, Defaults.ItemTypes.LURE);
                    case Defaults.ItemTypes.LINE -> Line.getLine(itemStack, Defaults.ItemTypes.LINE);
                    case Defaults.ItemTypes.POLE -> Pole.getPole(itemStack, Defaults.ItemTypes.POLE);
                    case Defaults.ItemTypes.REEL -> Reel.getReel(itemStack, Defaults.ItemTypes.REEL);
                    case Defaults.ItemTypes.CRAFTINGCOMPONENT -> CraftingComponent.getCraftingComponent(itemStack, Defaults.ItemTypes.CRAFTINGCOMPONENT);
                    case Defaults.ItemTypes.BAITPACKAGE -> BaitPackage.getBaitPackage(itemStack, Defaults.ItemTypes.BAITPACKAGE);
                    case Defaults.ItemTypes.CHUMMER -> Chummer.getChummer(itemStack, Defaults.ItemTypes.CHUMMER);
                    default -> null;
                };
            } else if (itemStack.getItem() == Items.COD
                    || itemStack.getItem() == Items.WHITE_DYE
                    || itemStack.getItem() == Items.BLACK_DYE
                    || itemStack.getItem() == Items.ROTTEN_FLESH
                    || itemStack.getItem() == Items.GOLD_INGOT
                    || itemStack.getItem() == Items.PRISMARINE_SHARD
                    || itemStack.getItem() == Items.DRIED_KELP
                    || itemStack.getItem() == Items.BONE
                    || itemStack.getItem() == Items.LIGHT_BLUE_DYE
            ) {
                String line = Objects.requireNonNull(itemStack.getComponents().get(DataComponentTypes.LORE)).lines().get(15).getString();
                return Fish.getFish(itemStack, Defaults.ItemTypes.FISH, line.substring(line.lastIndexOf(" ") + 1));
            } else if (itemStack.getItem() == Items.FISHING_ROD) {
                return FishingRod.getFishingRod(itemStack, Defaults.ItemTypes.FISHINGROD, itemStack.getName().getString());
            }
        }
        return null;
    }

    public static boolean isFOMCItem(ItemStack itemStack) {
        if(itemStack.get(DataComponentTypes.LORE) != null
                && itemStack.get(DataComponentTypes.CUSTOM_DATA) != null) {
            NbtCompound nbtCompound = ItemStackHelper.getNbtView(itemStack);
            if (nbtCompound == null || nbtCompound.getBoolean("shopitem").orElse(false)) {
                return false;
            }
            if (nbtCompound.contains("type")) {
                return switch (nbtCompound.getString("type").orElse("")) {
                    case Defaults.ItemTypes.PET, Defaults.ItemTypes.REEL, Defaults.ItemTypes.POLE,
                         Defaults.ItemTypes.LINE, Defaults.ItemTypes.LURE, Defaults.ItemTypes.BAIT,
                         Defaults.ItemTypes.ARMOR, Defaults.ItemTypes.SHARD, Defaults.ItemTypes.CRAFTINGCOMPONENT,
                         Defaults.ItemTypes.BAITPACKAGE, Defaults.ItemTypes.CHUMMER -> true;
                    default -> false;
                };
            } else {
                return itemStack.getItem() == Items.COD
                        || itemStack.getItem() == Items.WHITE_DYE
                        || itemStack.getItem() == Items.BLACK_DYE
                        || itemStack.getItem() == Items.ROTTEN_FLESH
                        || itemStack.getItem() == Items.GOLD_INGOT
                        || itemStack.getItem() == Items.PRISMARINE_SHARD
                        || itemStack.getItem() == Items.DRIED_KELP
                        || itemStack.getItem() == Items.BONE
                        || itemStack.getItem() == Items.LIGHT_BLUE_DYE
                        || itemStack.getItem() == Items.FISHING_ROD;
            }
        }
        return false;
    }

    public static Constant getRarity(ItemStack itemStack) {
        if(itemStack.get(DataComponentTypes.CUSTOM_DATA) != null && itemStack.getItem() != Items.FISHING_ROD) {
            NbtCompound nbtCompound = ItemStackHelper.getNbtView(itemStack);
            if(nbtCompound != null) {
                return Constant.valueOfId(nbtCompound.getString("rarity").orElse(""));
            }
        }
        return Constant.DEFAULT;
    }

    public static boolean isFish(ItemStack itemStack) {
        if(itemStack.get(DataComponentTypes.CUSTOM_DATA) != null) {
            return itemStack.getItem() == Items.COD
                    || itemStack.getItem() == Items.WHITE_DYE
                    || itemStack.getItem() == Items.BLACK_DYE
                    || itemStack.getItem() == Items.GOLD_INGOT
                    || itemStack.getItem() == Items.ROTTEN_FLESH
                    || itemStack.getItem() == Items.PRISMARINE_SHARD
                    || itemStack.getItem() == Items.DRIED_KELP
                    || itemStack.getItem() == Items.BONE
                    || itemStack.getItem() == Items.LIGHT_BLUE_DYE;
        }
        return false;
    }

    /// {isPet, hasSkin, hasItem, hasTrail}. hasItem means the "item" list actually holds a pet
    /// item — a key that exists but is empty doesn't count.
    public static boolean[] isPet(ItemStack itemStack) {
        if(itemStack.get(DataComponentTypes.CUSTOM_DATA) != null) {
            NbtCompound nbtCompound = ItemStackHelper.getNbtView(itemStack);
            if (nbtCompound != null && nbtCompound.contains("type")) {
                return Objects.equals(nbtCompound.getString("type").orElse(""), Defaults.ItemTypes.PET) ? new boolean[]{true, nbtCompound.contains("skin"), !nbtCompound.getList("item").orElse(new NbtList()).isEmpty(), nbtCompound.contains("trail")} : new boolean[]{false};
            }
        }
        return new boolean[]{false};
    }

    public static boolean isArmor(ItemStack itemStack) {
        if(itemStack.get(DataComponentTypes.CUSTOM_DATA) != null) {
            NbtCompound nbtCompound = ItemStackHelper.getNbtView(itemStack);
            if (nbtCompound != null && nbtCompound.contains("type")) {
                return Objects.equals(nbtCompound.getString("type").orElse(""), Defaults.ItemTypes.ARMOR);
            }
        }
        return false;
    }

    public static boolean isLure(ItemStack itemStack) {
        if(itemStack.get(DataComponentTypes.CUSTOM_DATA) != null) {
            NbtCompound nbtCompound = ItemStackHelper.getNbtView(itemStack);
            if (nbtCompound != null && nbtCompound.contains("type")) {
                return Objects.equals(nbtCompound.getString("type").orElse(""), Defaults.ItemTypes.LURE);
            }
        }
        return false;
    }

    public static boolean isBait(ItemStack itemStack) {
        if(itemStack.get(DataComponentTypes.CUSTOM_DATA) != null) {
            NbtCompound nbtCompound = ItemStackHelper.getNbtView(itemStack);
            if (nbtCompound != null && nbtCompound.contains("type")) {
                return Objects.equals(nbtCompound.getString("type").orElse(""), Defaults.ItemTypes.BAIT);
            }
        }
        return false;
    }
}