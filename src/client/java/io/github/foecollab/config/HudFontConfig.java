package io.github.foecollab.config;

import io.github.foecollab.common.HudFont;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class HudFontConfig {
    public static class HudFontSettings {
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        @ConfigEntry.Gui.Tooltip
        public HudFont.FontColor fontColor = HudFont.FontColor.OFF;

        @ConfigEntry.ColorPicker()
        @ConfigEntry.Gui.Tooltip
        public int customColor = HudFont.FontColor.PURPLE.base;
    }
}
