package io.github.foecollab.config;

import io.github.foecollab.common.ButtonColor;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class InventoryButtonConfig {
    public static class InventoryButtonSettings {
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        @ConfigEntry.Gui.Tooltip
        public ButtonColor buttonColor = ButtonColor.OFF;

        @ConfigEntry.ColorPicker()
        @ConfigEntry.Gui.Tooltip
        public int customColor = ButtonColor.PURPLE.base;
    }
}
