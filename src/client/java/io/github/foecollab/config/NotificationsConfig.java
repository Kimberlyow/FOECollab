package io.github.foecollab.config;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class NotificationsConfig {
    public static class Notifications {
        @ConfigEntry.Gui.Tooltip
        public boolean showWarningHud = true;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public HudAlignment alignment = HudAlignment.CENTER;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 20)
        public int fontSize = 8;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int backgroundOpacity = 40;
        @ConfigEntry.Gui.Excluded
        public int hudX = 50;
        @ConfigEntry.Gui.Excluded
        public int hudY = 10;
    }
}
