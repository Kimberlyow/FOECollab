package io.github.foecollab.FOMC.Types;

import io.github.foecollab.FOECollab;
import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.util.ItemStackHelper;
import io.github.foecollab.util.UUIDHelper;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

public class Fish extends FOMCItem {
    public final UUID id;
    public final CustomModelDataComponent customModelData;
    public final String fishId;
    public final String scientific;
    public final Constant variant;
    public final float value;
    public final float xp;
    public final String natureId;
    public final Constant location;
    public final Constant size;
    public final String sex;
    public final float weight;
    public final float length;
    public final String groupId;
    public final String lifestyleId;
    public final String ecosystem;
    public final String migrationId;
    public final String catcherName;
    public final UUID catcher;
    public final LocalDate date;
    public final String rodName;

    private Fish(NbtCompound nbtCompound, String type, CustomModelDataComponent customModelData, String name) {
        super(type, Constant.valueOfId(nbtCompound.getString("rarity").orElse("")));
        this.id = UUIDHelper.getUUID(nbtCompound.getIntArray("id").orElse(new int[0]));
        this.customModelData = customModelData;
        this.fishId = nbtCompound.getString("fish").orElse("");
        this.scientific = nbtCompound.getString("scientific").orElse("");
        String variantString = nbtCompound.getString("variant").orElse("");
        this.variant = Constant.valueOfId(variantString);
        if (!variantString.isEmpty() && this.variant == Constant.DEFAULT && !variantString.equals("normal")) {
            FOECollab.LOGGER.warn("[FoE] Unknown variant string: '{}' for fish: {}", variantString, this.fishId);
        }
        this.value = nbtCompound.getFloat("value").orElse(0f);
        this.xp = nbtCompound.getFloat("xp").orElse(0f);
        this.natureId = nbtCompound.getString("nature").orElse("");
        this.location = Constant.valueOfId(nbtCompound.getString("location").orElse(""));
        this.size = Constant.valueOfId(nbtCompound.getString("size").orElse(""));
        this.sex = nbtCompound.getString("sex").orElse("");
        this.weight = nbtCompound.getFloat("weight").orElse(0f);
        this.length = nbtCompound.getFloat("length").orElse(0f);
        this.groupId = nbtCompound.getString("group").orElse("");
        this.lifestyleId = nbtCompound.getString("lifestyle").orElse("");
        this.ecosystem = nbtCompound.getString("native").orElse("");
        this.migrationId = nbtCompound.getString("migration").orElse("");
        this.catcherName = name;
        this.catcher = UUIDHelper.getUUID(nbtCompound.getIntArray("catcher").orElse(new int[0]));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        this.date = LocalDate.parse(nbtCompound.getString("date").orElse("01/01/2000"), formatter);
        this.rodName = nbtCompound.getString("rod").orElse("");
    }

    public static Fish getFish(ItemStack itemStack, String type, String name) {
        return new Fish(Objects.requireNonNull(ItemStackHelper.getNbt(itemStack)), type, itemStack.get(DataComponentTypes.CUSTOM_MODEL_DATA), name);
    }

    public static Fish getFish(ItemStack itemStack) {
        if(itemStack.get(DataComponentTypes.LORE) != null
                && itemStack.get(DataComponentTypes.CUSTOM_DATA) != null
                && !Objects.requireNonNull(ItemStackHelper.getNbt(itemStack)).getBoolean("shopitem").orElse(false)) {
            if (itemStack.getItem() == Items.COD
                    || itemStack.getItem() == Items.WHITE_DYE
                    || itemStack.getItem() == Items.BLACK_DYE
                    || itemStack.getItem() == Items.GOLD_INGOT
                    || itemStack.getItem() == Items.PRISMARINE_SHARD
                    || itemStack.getItem() == Items.DRIED_KELP
                    || itemStack.getItem() == Items.BONE
                    || itemStack.getItem() == Items.LIGHT_BLUE_DYE
            ) {
                String line = Objects.requireNonNull(itemStack.getComponents().get(DataComponentTypes.LORE)).lines().get(15).getString();
                return Fish.getFish(itemStack, Defaults.ItemTypes.FISH, line.substring(line.lastIndexOf(" ") + 1));
            }
        }
        return null;
    }

    public static Constant getSize(ItemStack itemStack) {
        if(itemStack.get(DataComponentTypes.CUSTOM_DATA) != null) {
            NbtCompound nbtCompound = ItemStackHelper.getNbtView(itemStack);
            if(nbtCompound != null) {
                return Constant.valueOfId(nbtCompound.getString("size").orElse(""));
            }
        }
        return Constant.DEFAULT;
    }
}