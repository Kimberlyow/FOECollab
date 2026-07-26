package io.github.foecollab.FOMC.Types;

import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.util.ItemStackHelper;
import io.github.foecollab.util.UUIDHelper;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.Objects;
import java.util.UUID;

public class BaitPackage extends FOMCItem{
    public final CustomModelDataComponent customModelData;
    public final Constant location;
    public final UUID id;
    public final String intricacy;

    public BaitPackage(NbtCompound nbtCompound, String type, CustomModelDataComponent customModelData) {
        super(type, Constant.valueOfId(nbtCompound.getString("rarity").orElse("")));
        this.customModelData = customModelData;
        this.location = Constant.valueOfId(nbtCompound.getString("location").orElse(""));
        this.id = UUIDHelper.getUUID(nbtCompound.getIntArray("id").orElse(new int[0]));
        this.intricacy = nbtCompound.getString("intricacy").orElse("");
    }

    public static BaitPackage getBaitPackage(ItemStack itemStack, String type) {
        return new BaitPackage(Objects.requireNonNull(ItemStackHelper.getNbt(itemStack)), type, itemStack.get(DataComponentTypes.CUSTOM_MODEL_DATA));
    }

    public static BaitPackage getBaitPackage(ItemStack itemStack) {
        if(itemStack.get(DataComponentTypes.LORE) != null
                && itemStack.get(DataComponentTypes.CUSTOM_DATA) != null
                && Objects.requireNonNull(ItemStackHelper.getNbt(itemStack)).getBoolean("shopitem").orElse(false)) {
            NbtCompound nbtCompound = ItemStackHelper.getNbt(itemStack);
            if (nbtCompound != null && nbtCompound.contains("type")
                    && Objects.equals(nbtCompound.getString("type").orElse(""), Defaults.ItemTypes.BAITPACKAGE)) {
                return BaitPackage.getBaitPackage(itemStack, Defaults.ItemTypes.BAITPACKAGE);
            }
        }
        return null;
    }
}
