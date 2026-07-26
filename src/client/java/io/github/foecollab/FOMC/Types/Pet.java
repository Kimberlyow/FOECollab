package io.github.foecollab.FOMC.Types;

import io.github.foecollab.FOMC.ClimateConstant;
import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.util.ItemStackHelper;
import io.github.foecollab.util.UUIDHelper;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Pet extends FOMCItem {
    public final UUID id;
    public final Constant pet;
    public final ClimateConstant climate;
    public final Constant location;
    public final int lvl;
    public final float currentXp;
    public final float neededXp;
    public final Stat climateStat;
    public final Stat locationStat;
    public final float percentPetRating;
    public final String discovererName;
    public final UUID discoverer;
    public final String date;
    public final String petItem;

    private Pet(NbtCompound nbtCompound, String type) {
        super(type, Constant.valueOfId(nbtCompound.getString("rarity").orElse("")));
        this.id = UUIDHelper.getUUID(nbtCompound.getIntArray("id").orElse(new int[0]));
        this.pet = Constant.valueOfId(nbtCompound.getString("pet").orElse(""));
        this.climate = ClimateConstant.valueOfId(nbtCompound.getString("climate").orElse(""));
        this.location = Constant.valueOfId(nbtCompound.getString("location").orElse(""));
        this.lvl = nbtCompound.getInt("level").orElse(0);
        this.currentXp = nbtCompound.getFloat("xp_cur").orElse(0f);
        this.neededXp = nbtCompound.getFloat("xp_need").orElse(0f);
        this.climateStat = new Stat(nbtCompound, Constant.CLIMATE_BASE);
        this.locationStat = new Stat(nbtCompound, Constant.LOCATION_BASE);
        this.percentPetRating = getPercentPetRating(this.climateStat.percentLuck, this.climateStat.percentScale,
                this.locationStat.percentLuck, this.locationStat.percentScale);
        this.discovererName = nbtCompound.getString("username").orElse("");
        this.discoverer = UUIDHelper.getUUID(nbtCompound.getIntArray("uuid").orElse(new int[0]));
        this.date = nbtCompound.getString("date").orElse("");
        this.petItem = readPetItem(nbtCompound);
    }

    public Pet(
            Constant pet,
            Constant rarity,
            float cMaxLuck,
            float cMaxScale,
            float cPercentLuck,
            float cPercentScale,
            float lMaxLuck,
            float lMaxScale,
            float lPercentLuck,
            float lPercentScale) {
        super("pet", rarity);
        this.id = UUID.randomUUID();
        this.pet = pet;
        this.climate = ClimateConstant.DEFAULT;
        this.location = Constant.DEFAULT;
        this.lvl = 100;
        this.currentXp = 0;
        this.neededXp = 0;
        this.climateStat = new Stat(
                Constant.CLIMATE_BASE.ID,
                cMaxLuck,
                cMaxScale,
                cPercentLuck,
                cPercentScale);
        this.locationStat = new Stat(
                Constant.LOCATION_BASE.ID,
                lMaxLuck,
                lMaxScale,
                lPercentLuck,
                lPercentScale);
        this.percentPetRating = getPercentPetRating(climateStat.percentLuck, climateStat.percentScale,
                locationStat.percentLuck, locationStat.percentScale);
        this.discovererName = "";
        this.discoverer = null;
        this.date = LocalDate.now().toString();
        this.petItem = null;
    }

    public static class Stat {
        public final String id;
        public final float currentLuck;
        public final float currentScale;
        public final float maxLuck;
        public final float maxScale;
        public final float percentLuck;
        public final float percentScale;

        private Stat(NbtCompound nbtCompound, Constant base) {
            switch (base) {
                case Constant.CLIMATE_BASE -> {
                    this.id = nbtCompound.getString("climate").orElse("");
                    this.currentLuck = nbtCompound.getList("cbase").orElse(new NbtList()).getCompound(0)
                            .orElseThrow().getInt("cur").orElse(0);
                    this.currentScale = nbtCompound.getList("cbase").orElse(new NbtList()).getCompound(1)
                            .orElseThrow().getInt("cur").orElse(0);
                    this.maxLuck = nbtCompound.getList("cbase").orElse(new NbtList()).getCompound(0)
                            .orElseThrow().getInt("cur_max").orElse(0);
                    this.maxScale = nbtCompound.getList("cbase").orElse(new NbtList()).getCompound(1)
                            .orElseThrow().getInt("cur_max").orElse(0);
                    this.percentLuck = nbtCompound.getList("cbase").orElse(new NbtList()).getCompound(0)
                            .orElseThrow().getFloat("percent_max").orElse(0f);
                    this.percentScale = nbtCompound.getList("cbase").orElse(new NbtList()).getCompound(1)
                            .orElseThrow().getFloat("percent_max").orElse(0f);
                }
                case Constant.LOCATION_BASE -> {
                    this.id = nbtCompound.getString("location").orElse("");
                    this.currentLuck = nbtCompound.getList("lbase").orElse(new NbtList()).getCompound(0)
                            .orElseThrow().getInt("cur").orElse(0);
                    this.currentScale = nbtCompound.getList("lbase").orElse(new NbtList()).getCompound(1)
                            .orElseThrow().getInt("cur").orElse(0);
                    this.maxLuck = nbtCompound.getList("lbase").orElse(new NbtList()).getCompound(0)
                            .orElseThrow().getInt("cur_max").orElse(0);
                    this.maxScale = nbtCompound.getList("lbase").orElse(new NbtList()).getCompound(1)
                            .orElseThrow().getInt("cur_max").orElse(0);
                    this.percentLuck = nbtCompound.getList("lbase").orElse(new NbtList()).getCompound(0)
                            .orElseThrow().getFloat("percent_max").orElse(0f);
                    this.percentScale = nbtCompound.getList("lbase").orElse(new NbtList()).getCompound(1)
                            .orElseThrow().getFloat("percent_max").orElse(0f);
                }
                default -> {
                    this.id = Defaults.EMPTY_STRING;
                    this.currentScale = 0f;
                    this.maxLuck = 0f;
                    this.maxScale = 0f;
                    this.percentLuck = 0f;
                    this.percentScale = 0f;
                    this.currentLuck = 0f;
                }
            }
        }

        private Stat(
                String id,
                float maxLuck,
                float maxScale,
                float percentLuck,
                float percentScale) {
            this.id = id;
            this.currentLuck = 0f;
            this.currentScale = 0f;
            this.maxLuck = maxLuck;
            this.maxScale = maxScale;
            this.percentLuck = percentLuck;
            this.percentScale = percentScale;
        }
    }

    private static float getPercentPetRating(float climateLuck, float climateScale, float locationLuck,
                                             float locationScale) {
        return (climateLuck + climateScale + locationLuck + locationScale) / 4;
    }

    public boolean isMaxLuck() {
        return climateStat.percentLuck >= 1.0f && locationStat.percentLuck >= 1.0f;
    }

    public boolean isMaxScale() {
        return climateStat.percentScale >= 1.0f && locationStat.percentScale >= 1.0f;
    }

    public static Constant getConstantFromPercent(float value) {
        BigDecimal percent = new BigDecimal(Float.toString(value))
                .multiply(BigDecimal.valueOf(100));
        if (percent.compareTo(BigDecimal.valueOf(20)) <= 0)
            return Constant.SICKLY;
        else if (percent.compareTo(BigDecimal.valueOf(30)) < 0)
            return Constant.BAD;
        else if (percent.compareTo(BigDecimal.valueOf(40)) < 0)
            return Constant.BELOW_AVERAGE;
        else if (percent.compareTo(BigDecimal.valueOf(50)) < 0)
            return Constant.AVERAGE;
        else if (percent.compareTo(BigDecimal.valueOf(60)) < 0)
            return Constant.GOOD;
        else if (percent.compareTo(BigDecimal.valueOf(80)) < 0)
            return Constant.GREAT;
        else if (percent.compareTo(BigDecimal.valueOf(90)) < 0)
            return Constant.EXCELLENT;
        else if (percent.compareTo(BigDecimal.valueOf(100)) < 0)
            return Constant.AMAZING;
        else if (percent.compareTo(BigDecimal.valueOf(101)) <= 0)
            return Constant.PERFECT;
        return Constant.DEFAULT;
    }

    public static Constant getConstantFromLine(Text line) {
        if (line.getString().contains(Constant.SICKLY.TAG.getString()))
            return Constant.SICKLY;
        else if (line.getString().contains(Constant.BAD.TAG.getString()))
            return Constant.BAD;
        else if (line.getString().contains(Constant.BELOW_AVERAGE.TAG.getString()))
            return Constant.BELOW_AVERAGE;
        else if (line.getString().contains(Constant.AVERAGE.TAG.getString()))
            return Constant.AVERAGE;
        else if (line.getString().contains(Constant.GOOD.TAG.getString()))
            return Constant.GOOD;
        else if (line.getString().contains(Constant.GREAT.TAG.getString()))
            return Constant.GREAT;
        else if (line.getString().contains(Constant.EXCELLENT.TAG.getString()))
            return Constant.EXCELLENT;
        else if (line.getString().contains(Constant.AMAZING.TAG.getString()))
            return Constant.AMAZING;
        else if (line.getString().contains(Constant.PERFECT.TAG.getString()))
            return Constant.PERFECT;
        return Constant.DEFAULT;
    }

    public static Pet getPet(ItemStack itemStack, String type) {
        return new Pet(Objects.requireNonNull(ItemStackHelper.getNbt(itemStack)), type);
    }

    public static Pet getPet(ItemStack itemStack) {
        if (itemStack.get(DataComponentTypes.LORE) != null
                && itemStack.get(DataComponentTypes.CUSTOM_DATA) != null) {
            // Single read-only view for validation (was 3 deep NBT copies: shopitem
            // check, type check, then the build). getPet(stack, type) still takes its
            // own copied NBT for the actual Pet it returns.
            NbtCompound nbtCompound = ItemStackHelper.getNbtView(itemStack);
            if (nbtCompound != null
                    && !nbtCompound.getBoolean("shopitem").orElse(false)
                    && nbtCompound.contains("type")
                    && Objects.equals(nbtCompound.getString("type").orElse(""), Defaults.ItemTypes.PET)) {
                return Pet.getPet(itemStack, Defaults.ItemTypes.PET);
            }
        }
        return null;
    }

    private static String readPetItem(NbtCompound nbtCompound) {
        if (nbtCompound == null) {
            return null;
        }
        NbtList items = nbtCompound.getList("item").orElse(new NbtList());
        if (items.isEmpty()) {
            return null;
        }
        NbtCompound item = items.getCompound(0).orElse(null);
        if (item == null) return null;
        return item
                .getCompound("components").orElse(null) == null ? null :
                item.getCompound("components").orElse(new NbtCompound())
                        .getCompound("minecraft:custom_data").orElse(new NbtCompound())
                        .getString("petItem").orElse(null);
    }
}