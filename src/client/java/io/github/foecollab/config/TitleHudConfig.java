package io.github.foecollab.config;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class TitleHudConfig {
    public static class TitlePopup {
        @ConfigEntry.Gui.Tooltip
        public boolean useNewTitleSystem = true;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public HudAlignment alignment = HudAlignment.CENTER;
        public boolean showWeight = true;
        public boolean showLength = true;
        @ConfigEntry.BoundedDiscrete(max = 20, min = 2)
        public int scale = 10;
        @ConfigEntry.Gui.Excluded
        public int hudX = 50;
        @ConfigEntry.Gui.Excluded
        public int hudY = 50;
    }
}
