package io.github.foecollab.config;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class BarHUDConfig {
    public static class BarHUD {
        public boolean showBar = true;
        @ConfigEntry.Gui.Tooltip
        public boolean showClimate = true;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int backgroundOpacity = 40;
        @ConfigEntry.BoundedDiscrete(max = 20, min = 2)
        public int fontSize = 8;

        // Standalone level + XP-progress HUD shown only while the top bar is hidden (movable).
        @ConfigEntry.Gui.CollapsibleObject
        public LevelHudOptions levelHud = new LevelHudOptions();
        public static class LevelHudOptions {
            @ConfigEntry.Gui.Tooltip
            public boolean showWhenBarHidden = true;
            @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
            public HudAlignment alignment = HudAlignment.LEFT;
            @ConfigEntry.BoundedDiscrete(min = 1, max = 20)
            public int fontSize = 8;
            @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
            public int backgroundOpacity = 40;
            @ConfigEntry.Gui.Excluded
            public int hudX = 1;
            @ConfigEntry.Gui.Excluded
            public int hudY = 2;
        }
    }
}
