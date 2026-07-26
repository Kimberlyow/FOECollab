package io.github.foecollab.config;

import io.github.foecollab.handler.ItemMarkerHandler;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class ItemMarkerConfig {
    public static class ItemMarker {
        @ConfigEntry.Gui.CollapsibleObject
        public ItemSlotMarker itemSlotMarker = new ItemSlotMarker();
        public static class ItemSlotMarker {
            public boolean showItemMarker = true;
            public boolean showFishRarityMarker = true;
            @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
            public ItemMarkerHandler.FishSizeMarkerToggle showFishSizeMarker = ItemMarkerHandler.FishSizeMarkerToggle.CHARACTER;
            public boolean showOtherRarityMarker = true;
            @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
            public ItemMarkerHandler.PetPercentMarkerToggle showPetPercentMarker = ItemMarkerHandler.PetPercentMarkerToggle.CHARACTER;
            public boolean showPetItemEquippedMarker = true;
            public boolean showMaxPetStatsMarker = true;
            public boolean showArmorQualityMarker = true;
            public boolean showHotbarMarkers = true;
        }

        @ConfigEntry.Gui.CollapsibleObject
        public ItemSearchMarker itemSearchMarker = new ItemSearchMarker();
        public static class ItemSearchMarker {
            @ConfigEntry.ColorPicker()
            public int searchHighlightColor = 0x55FF55;
        }


        public boolean showSelectedPetHighlight = true;
        @ConfigEntry.ColorPicker()
        public int selectedPetHighlightColor = 0xFFAA00;
    }
}
