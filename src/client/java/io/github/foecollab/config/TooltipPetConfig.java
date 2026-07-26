package io.github.foecollab.config;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class TooltipPetConfig {
    public static class PetTooltip {
        // Decimal precision for the percentages shown in the Pet Merge Calculator screen.
        // (The server now shows pet stat percentages on item tooltips natively, so the old
        // tooltip-percentage injection was removed; this only affects the calculator.)
        @ConfigEntry.BoundedDiscrete(min = 0, max = 2)
        public int decimalPlaces = 1;
    }
}
