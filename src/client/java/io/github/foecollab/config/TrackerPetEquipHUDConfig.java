package io.github.foecollab.config;

import io.github.foecollab.handler.NotificationSoundHandler;
import io.github.foecollab.handler.screens.hud.PetEquipHudHandler;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class TrackerPetEquipHUDConfig {
    public static class PetEquipTracker {
        @ConfigEntry.Gui.Tooltip
        public boolean showPetEquipTrackerHUD = true;

        @ConfigEntry.Gui.CollapsibleObject
        public ActivePetHUDOptions activePetHUDOptions = new ActivePetHUDOptions();
        public static class ActivePetHUDOptions {
            // Title (rendered through TextHelper.smallCaps, so "PET" shows as ᴘᴇᴛ)
            public boolean showTitle = true;
            public String titleText = "PET";

            // Per-element line toggles
            public boolean showLevel = true;
            public boolean showXp = true;
            public boolean showRating = true;
            public boolean showItemLine = true;
            public boolean showItemIcon = true;

            // XP display: number ("text") or progress bar
            @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
            public PetEquipHudHandler.XpDisplayMode xpDisplayMode = PetEquipHudHandler.XpDisplayMode.TEXT;
            // Small XP number/percent drawn over the bar (bar mode only)
            public boolean showXpBarText = false;

            public boolean colorPetBorderToRating = true;
            @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
            public HudAlignment alignment = HudAlignment.RIGHT;
            @ConfigEntry.BoundedDiscrete(min = 1, max = 20)
            public int fontSize = 8;
            @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
            public int backgroundOpacity = 40;
            // hudX is measured from the screen's left edge; RIGHT pins the box's right edge there,
            // so 100 = flush against the right side (the default for this right-aligned HUD).
            @ConfigEntry.Gui.Excluded
            public int hudX = 100;
            @ConfigEntry.Gui.Excluded
            public int hudY = 0;
        }

        @ConfigEntry.Gui.CollapsibleObject
        public WarningOptions warningOptions = new WarningOptions();
        public static class WarningOptions {
            @ConfigEntry.Gui.Tooltip
            public boolean showPetEquipWarningHUD = false;
            public boolean showWrongPetWarningHUD = true;
            @ConfigEntry.Gui.Tooltip
            public boolean usePetEquipWarningSound = false;
            @ConfigEntry.BoundedDiscrete(min = 1, max = 30)
            public int timePetEquipWarningSound = 10;
            @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
            public NotificationSoundHandler.SoundType petEquipSoundType = NotificationSoundHandler.SoundType.DIDGERIDOO;
        }
    }
}
