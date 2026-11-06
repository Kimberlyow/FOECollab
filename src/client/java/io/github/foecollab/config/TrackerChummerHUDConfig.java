package io.github.foecollab.config;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class TrackerChummerHUDConfig {
    public static class ChummerTracker {
        public boolean showChummerHud = true;
        // The time bar under the hud's text, draining as the chummer runs out.
        public boolean showProgressBar = true;
        // Replaces the server's particle circle with a solid ring at the chummer's range (20 blocks).
        public boolean solidRangeBar = true;
        @ConfigEntry.ColorPicker
        public int barColor = 0x00E5FF;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public HudAlignment alignment = HudAlignment.CENTER;
        @ConfigEntry.BoundedDiscrete(min = 2, max = 20)
        public int fontSize = 10;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int backgroundOpacity = 40;
        @ConfigEntry.Gui.Excluded
        public int hudX = 50;
        @ConfigEntry.Gui.Excluded
        public int hudY = 26;
    }
}
