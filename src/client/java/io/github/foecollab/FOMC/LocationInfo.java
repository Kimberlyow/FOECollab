package io.github.foecollab.FOMC;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

public enum LocationInfo {
    SPAWNHUB("spawnhub", ClimateConstant.SUBTROPICAL, Constant.FRESHWATER),
    CYPRESS_LAKE("spawn", ClimateConstant.SUBTROPICAL, Constant.FRESHWATER),
    KENAI_RIVER("kenai", ClimateConstant.SUBARCTIC, Constant.FRESHWATER),
    LAKE_BIWA("biwa", ClimateConstant.SUBTROPICAL, Constant.FRESHWATER),
    MURRAY_RIVER("murray", ClimateConstant.SEMI_ARID, Constant.FRESHWATER),
    EVERGLADES("everglades", ClimateConstant.SAVANNA, Constant.FRESHWATER, Constant.SALTWATER),
    KEY_WEST("keywest", ClimateConstant.SAVANNA, Constant.SALTWATER),
    TOLEDO_BEND("toledobend", ClimateConstant.SUBTROPICAL, Constant.FRESHWATER),
    GREAT_LAKES("greatlakes", ClimateConstant.CONTINENTAL, Constant.FRESHWATER),
    DANUBE_RIVER("danube", ClimateConstant.CONTINENTAL, Constant.FRESHWATER),
    OIL_RIG("oilrig", ClimateConstant.SAVANNA, Constant.SALTWATER),
    AMAZON_RIVER("amazon", ClimateConstant.RAINFOREST, Constant.FRESHWATER),
    MEDITERRANEAN_SEA("mediterranean", ClimateConstant.MEDITERRANEAN, Constant.SALTWATER),
    CAPE_COD("capecod", ClimateConstant.OCEANIC, Constant.SALTWATER),
    HAWAII("hawaii", ClimateConstant.SAVANNA, Constant.SALTWATER),
    CAIRNS("cairns", ClimateConstant.MONSOON, Constant.SALTWATER),
    LOFOTEN_ISLANDS("lofotenislands", ClimateConstant.SUBARCTIC, Constant.SALTWATER),
    NILE_RIVER("nile", ClimateConstant.ARID, Constant.FRESHWATER),
    DEFAULT("", ClimateConstant.DEFAULT, Constant.DEFAULT)
    ;

    public final String ID;
    public final ClimateConstant CLIMATE;
    public final Set<Constant> WATER;

    LocationInfo(String id, ClimateConstant climate, Constant... water) {
        this.ID = id;
        this.CLIMATE = climate;
        this.WATER = EnumSet.copyOf(Arrays.asList(water));
    }

    public boolean hasWater(Constant type) {
        return WATER.contains(type);
    }

    public Text waterTag() {
        MutableText tag = Text.empty();
        boolean first = true;
        for (Constant water : WATER) {
            if (!first) tag.append(Text.literal("/"));
            tag.append(water.TAG);
            first = false;
        }
        return tag;
    }

    public static LocationInfo valueOfId(String id) {
        for (LocationInfo c : values()) {
            if (c.ID.equals(id)) {
                return c;
            }
        }
        return DEFAULT;
    }
}
