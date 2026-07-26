package io.github.foecollab.config;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class TrackerFriendHUDConfig {
        public static class FriendTracker {
        public boolean notifyFriendOnJoin = true;
        public boolean notifyFriendOnLeave = true;
        public boolean showFriendTag = true;
        @ConfigEntry.Gui.Tooltip
        public boolean isPrefix = true;
    }
}
