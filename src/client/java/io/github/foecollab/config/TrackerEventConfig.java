package io.github.foecollab.config;

import io.github.foecollab.handler.NotificationSoundHandler;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class TrackerEventConfig {
    public static class EventTracker {
        @ConfigEntry.Gui.CollapsibleObject
        public WeatherEventOptions weatherEventOptions = new WeatherEventOptions();
        public static class WeatherEventOptions {
            public boolean showAlertHUD = true;
            @ConfigEntry.Gui.CollapsibleObject
            public ToggleOptions toggleOptions = new ToggleOptions();
            public static class ToggleOptions {
                public boolean rainbow = true;
                public boolean thunderstorm = true;
                public boolean supercell = true;
                public boolean goldRush = true;
                public boolean rain = true;
                public boolean bloomingOasis = true;
                public boolean fullMoon = true;
                public boolean blueMoon = true;
                public boolean superMoon = true;
                public boolean bloodMoon = true;
            }

            @ConfigEntry.BoundedDiscrete(min = 1, max = 300)
            public int alertDismissSeconds = 15;
            @ConfigEntry.Gui.Tooltip
            public boolean useAlertWarningSound = true;
            @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
            public NotificationSoundHandler.SoundType alertSoundType = NotificationSoundHandler.SoundType.BELL;
            @ConfigEntry.Gui.Tooltip
            public boolean muteMoonAlerts = false;
        }

        @ConfigEntry.Gui.CollapsibleObject
        public OtherEventOptions otherEventOptions = new OtherEventOptions();
        public static class OtherEventOptions {
            @ConfigEntry.Gui.CollapsibleObject
            public FabledOptions fabledOptions = new FabledOptions();
            @ConfigEntry.Gui.CollapsibleObject
            public WitchingHourOptions witchingHourOptions = new WitchingHourOptions();
            public static class FabledOptions {
                public boolean showAlertHUD = true;
                @ConfigEntry.BoundedDiscrete(min = 1, max = 300)
                public int alertDismissSeconds = 15;
                @ConfigEntry.Gui.Tooltip
                public boolean useAlertWarningSound = true;
                @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
                public NotificationSoundHandler.SoundType alertSoundType = NotificationSoundHandler.SoundType.BELL;
            }

            public static class WitchingHourOptions {
                public boolean showAlertHUD = true;
                @ConfigEntry.BoundedDiscrete(min = 1, max = 300)
                public int alertDismissSeconds = 5;
                @ConfigEntry.BoundedDiscrete(min = 0, max = 6)
                @ConfigEntry.Gui.Tooltip
                public int alertOffsetHours = 0;
                @ConfigEntry.Gui.Tooltip
                public boolean useAlertWarningSound = true;
                @ConfigEntry.Gui.Tooltip
                public boolean showOutsideCypressLake = false;
                @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
                public NotificationSoundHandler.SoundType alertSoundType = NotificationSoundHandler.SoundType.DIDGERIDOO;
            }
        }
    }
}
