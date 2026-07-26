package io.github.foecollab.FOMC.Types;

import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.util.ItemStackHelper;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.Objects;

public class Shard extends FOMCItem {
    public final String climateId;
    public final CustomModelDataComponent customModelData;
    public final Constant rarity;

    private Shard(NbtCompound nbtCompound, String type, CustomModelDataComponent customModelData) {
        super(type, Constant.DEFAULT);
        this.climateId = nbtCompound.getString("name").orElse("");
        this.customModelData = customModelData;
        this.rarity = Constant.valueOfId(nbtCompound.getString("rarity").orElse(""));
    }

    public static Shard getShard(ItemStack itemStack, String type) {
        return new Shard(Objects.requireNonNull(ItemStackHelper.getNbt(itemStack)), type, itemStack.get(DataComponentTypes.CUSTOM_MODEL_DATA));
    }

    public static Shard getShard(ItemStack itemStack) {
        if(itemStack.get(DataComponentTypes.LORE) != null
                && itemStack.get(DataComponentTypes.CUSTOM_DATA) != null
                && !Objects.requireNonNull(ItemStackHelper.getNbt(itemStack)).getBoolean("shopitem").orElse(false)) {
            NbtCompound nbtCompound = ItemStackHelper.getNbt(itemStack);
            if (nbtCompound != null && nbtCompound.contains("type")
                    && Objects.equals(nbtCompound.getString("type").orElse(""), Defaults.ItemTypes.SHARD)) {
                return Shard.getShard(itemStack, Defaults.ItemTypes.SHARD);
            }
        }
        return null;
    }
}
