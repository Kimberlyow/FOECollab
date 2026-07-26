package io.github.foecollab.config;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class TrackerQuestHUDConfig {
    public static class QuestTracker {
        public boolean showQuestHud = true;
        public boolean hideTitle = false;
        public boolean hideLocation = false;
        public boolean hideQuestNumbers = false;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public HudAlignment alignment = HudAlignment.RIGHT;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int backgroundOpacity = 40;
        @ConfigEntry.BoundedDiscrete(max = 20, min = 2)
        public int fontSize = 8;
        // hudX is measured from the screen's left edge; RIGHT pins the box's right edge there, so
        // 100 = flush against the right side (the default for this right-aligned HUD).
        @ConfigEntry.Gui.Excluded
        public int hudX = 100;
        @ConfigEntry.Gui.Excluded
        public int hudY = 100;
    }

    public static class DailyQuestTracker {
        public boolean showDailyQuestHud = true;
        public boolean showNotification = true;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public HudAlignment alignment = HudAlignment.RIGHT;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int backgroundOpacity = 40;
        @ConfigEntry.BoundedDiscrete(max = 20, min = 2)
        public int fontSize = 8;
        // hudX is measured from the screen's left edge; RIGHT pins the box's right edge there, so
        // 85 keeps this right-aligned HUD just inside the right side (was the old "15 from right").
        @ConfigEntry.Gui.Excluded
        public int hudX = 85;
        @ConfigEntry.Gui.Excluded
        public int hudY = 100;
    }
}
