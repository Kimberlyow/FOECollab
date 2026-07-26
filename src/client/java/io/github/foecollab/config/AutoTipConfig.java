package io.github.foecollab.config;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class AutoTipConfig {
    public static class AutoTip {
        @ConfigEntry.Gui.Tooltip
        public boolean autoTipReactions = false;
        public int reactionTipAmount = 1000;
        @ConfigEntry.Gui.Tooltip
        public boolean disableManualTippingMsg = false;
    }
}
