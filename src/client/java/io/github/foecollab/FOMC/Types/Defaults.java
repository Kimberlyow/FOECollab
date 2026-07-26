package io.github.foecollab.FOMC.Types;

import io.github.foecollab.FOMC.Constant;

import java.util.Map;

public class Defaults {
    public static final String EMPTY_STRING = "";
    public static final int DEFAULT_COLOR = 0xFFFFFF;

    public static class ItemTypes {
        public static final String PET = "pet";
        public static final String FISH = "fish";
        public static final String SHARD = "armorShard";
        public static final String ARMOR = "armor";
        public static final String BAIT = "bait";
        public static final String LURE = "lure";
        public static final String LINE = "line";
        public static final String POLE = "pole";
        public static final String REEL = "reel";
        public static final String FISHINGROD = "fishing_rod";
        public static final String CRAFTINGCOMPONENT = "craftingComponent";
        public static final String BAITPACKAGE = "package";
        public static final String CHUMMER = "chummer";
        public static final String PETITEM = "petItem";
    }

    public static final Map<String, FoEDevType> foeDevs = Map.of(
            "b5a9bbb7-42b4-4a6a-9ebe-bdf6697c8ee0",  new FoEDevType(Constant.ADMIRAL.TAG.getString() + " DannyPX", "DannyPX", false),
            "4c42b880-84df-4b0b-abd6-817997c16732",  new FoEDevType(Constant.CAPTAIN.TAG.getString() + " Krokodaxl", "Krokodaxl", false),
            "afbe233b-a6b3-4a3d-88bc-2dbf157a6cf3",  new FoEDevType(Constant.ADMIRAL.TAG.getString() + "  Kimberlyqt", "Kimberlyqt", true),
            "4bb09b56-b73a-4dfe-b0ad-0bb4fe9621ed",  new FoEDevType(Constant.ADMIRAL.TAG.getString() + " landofif", "landofif", true)
    );

    public static class FoEDevType {
        public String text;
        public String name;
        public boolean usePurpleTag;

        public FoEDevType(String text, String name, boolean usePurpleTag) {
            this.text = text;
            this.name = name;
            this.usePurpleTag = usePurpleTag;
        }
    }
}
