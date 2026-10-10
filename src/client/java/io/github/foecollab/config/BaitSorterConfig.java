package io.github.foecollab.config;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class BaitSorterConfig {
    public enum SortOrder {
        COMMON_TO_LEGENDARY("Common > Legendary"),
        LEGENDARY_TO_COMMON("Legendary > Common");

        private final String displayName;

        SortOrder(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public static class BaitSorter {
        @ConfigEntry.Gui.Tooltip
        public SortOrder sortOrder = SortOrder.COMMON_TO_LEGENDARY;

        /** Click groups per tick, in halves: 1 = one every 2 ticks (default), 8 = 4 per tick. */
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 8)
        public int clickSpeed = 1;
    }
}
