package io.github.foecollab.FOMC.Types;

import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.util.ItemStackHelper;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Pole extends FOMCItem {
    public final String name;
    public final CustomModelDataComponent customModelData;
    public final Constant water;
    public final List<PoleStats> poleStats;
    public final List<Calibration> calibration;

    private Pole(NbtCompound nbtCompound, String type, CustomModelDataComponent customModelData) {
        super(type, Constant.valueOfId(nbtCompound.getString("rarity").orElse("")));
        this.name = nbtCompound.getString("name").orElse("");
        this.customModelData = customModelData;
        this.water = Constant.valueOfId(nbtCompound.getString("water").orElse("global"));
        NbtList nbtList = nbtCompound.getList("base").orElse(new NbtList());
        List<NbtCompound> nbtCompoundList = new ArrayList<>();
        for (int i = 0; i < nbtList.size(); i++) {
            nbtCompoundList.add(nbtList.getCompound(i).orElse(new NbtCompound()));
        }
        this.poleStats = nbtCompoundList.stream().map(PoleStats::new).toList();
        NbtList nbtList1 = nbtCompound.getList("calibration").orElse(new NbtList());
        List<NbtCompound> nbtCompoundList1 = new ArrayList<>();
        for (int i = 0; i < nbtList1.size(); i++) {
            nbtCompoundList1.add(nbtList1.getCompound(i).orElse(new NbtCompound()));
        }
        this.calibration = nbtCompoundList1.stream().map(Calibration::new).toList();
    }

    public static class PoleStats {
        public final int cur;
        public final String id;

        private PoleStats(NbtCompound nbtCompound) {
            this.cur = nbtCompound.getInt("cur").orElse(0);
            this.id = nbtCompound.getString("id").orElse("");
        }
    }

    public static class Calibration {
        public final int cur;
        public final String id;
        public final String calibration;

        private Calibration(NbtCompound nbtCompound) {
            this.cur = nbtCompound.getInt("cur").orElse(0);
            this.id = nbtCompound.getString("id").orElse("");
            this.calibration = nbtCompound.getString("calibration").orElse("");
        }
    }

    public static Pole getPole(ItemStack itemStack, String type) {
        return new Pole(Objects.requireNonNull(ItemStackHelper.getNbt(itemStack)), type, itemStack.get(DataComponentTypes.CUSTOM_MODEL_DATA));
    }

    public static Pole getPole(ItemStack itemStack) {
        if(itemStack.get(DataComponentTypes.LORE) != null
                && itemStack.get(DataComponentTypes.CUSTOM_DATA) != null
                && !Objects.requireNonNull(ItemStackHelper.getNbt(itemStack)).getBoolean("shopitem").orElse(false)) {
            NbtCompound nbtCompound = ItemStackHelper.getNbt(itemStack);
            if (nbtCompound != null && nbtCompound.contains("type")
                    && Objects.equals(nbtCompound.getString("type").orElse(""), Defaults.ItemTypes.POLE)) {
                return Pole.getPole(itemStack, Defaults.ItemTypes.POLE);
            }
        }
        return null;
    }
}
