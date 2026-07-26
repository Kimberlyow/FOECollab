package io.github.foecollab.config;

import io.github.foecollab.handler.NotificationSoundHandler;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class BiteTitleConfig {
    public static class BiteTitle {
        public boolean enabled = false;
        public String text = "BITE!";
        @ConfigEntry.BoundedDiscrete(min = 1, max = 30)
        public int displaySeconds = 3;
        // Show the bobber timer in this element's slot, swapping to the bite text while a fish bites.
        public boolean mergeWithTimer = false;
        // Color of the merged timer text (the standalone BobberTimerHud keeps the HUD font color).
        @ConfigEntry.ColorPicker
        public int timerColor = 0xFFFFFF;
        public boolean textShadow = true;
        public boolean playSound = true;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public NotificationSoundHandler.SoundType soundType = NotificationSoundHandler.SoundType.PLING;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 200)
        public int volume = 100;
        @ConfigEntry.ColorPicker
        public int textColor = 0xFFFF55;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public HudAlignment alignment = HudAlignment.CENTER;
        @ConfigEntry.BoundedDiscrete(max = 40, min = 2)
        public int scale = 15;
        @ConfigEntry.Gui.Excluded
        public int hudX = 50;
        @ConfigEntry.Gui.Excluded
        public int hudY = 40;
    }
}
