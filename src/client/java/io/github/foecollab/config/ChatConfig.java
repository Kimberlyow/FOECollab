package io.github.foecollab.config;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class ChatConfig {
    @ConfigEntry.Gui.CollapsibleObject
    public ChatSettings chatconfig = new ChatSettings();

    public static class ChatSettings {
        // Very stupid name ik, basically changes "PET DROP! You pulled a \uF034 Lynx Pet" to "PET DROP! You pulled a [RARE] Lynx Pet"
        @ConfigEntry.Gui.Tooltip
        public boolean makeSomeTagsCopyPastable = true;

        @ConfigEntry.Gui.CollapsibleObject
        public ChatFilter chatFilter = new ChatFilter();
        public static class ChatFilter {
            @ConfigEntry.Gui.Tooltip
            public boolean enabled = false;
        }
    }
}