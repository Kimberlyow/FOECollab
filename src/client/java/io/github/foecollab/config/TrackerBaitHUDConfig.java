package io.github.foecollab.config;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class TrackerBaitHUDConfig {
    public static class BaitTracker {
        public boolean showBaitHud = true;
        public boolean showBaitWarningHUD = true;
        @ConfigEntry.Gui.Tooltip
        public boolean showLowBaitWarningHUD = true;
        @ConfigEntry.Gui.Tooltip
        public int lowBaitThreshold = 10;
        // CENTER keeps the block centred on its anchor; LEFT/RIGHT pin that edge and put the
        // bait icon on the matching side.
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public HudAlignment alignment = HudAlignment.CENTER;
        @ConfigEntry.Gui.Tooltip
        public boolean calculateLures = true;
        @ConfigEntry.Gui.Excluded
        public int hudX = 50;
        @ConfigEntry.Gui.Excluded
        public int hudY = 90;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int backgroundOpacity = 40;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 20)
        public int fontSize = 8;
    }
}
