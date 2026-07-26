package io.github.foecollab.util;

import net.minecraft.text.Text;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shortens FishOnMC location names to just the recognisable part
 * (e.g. "Cypress Lake" -> "Cypress", "Mediterranean Sea" -> "Med").
 * Locations the user wants left intact (Everglades, Key West, Great Lakes,
 * Hawaii, Cairns, Oil Rig, Lofoten Islands, Crew Island) are simply absent
 * from the map.
 */
public class LocationNameHelper {
    // LinkedHashMap so the longest / most-specific names are replaced first; this
    // avoids a shorter key matching inside a longer name before the full name is handled.
    private static final Map<String, String> SHORT = new LinkedHashMap<>();

    static {
        SHORT.put("Toledo Bend Reservoir", "Toledo");
        SHORT.put("Mediterranean Sea", "Med");
        SHORT.put("Cypress Lake", "Cypress");
        SHORT.put("Kenai River", "Kenai");
        SHORT.put("Murray River", "Murray");
        SHORT.put("Danube River", "Danube");
        SHORT.put("Amazon River", "Amazon");
        SHORT.put("Lake Biwa", "Biwa");
        SHORT.put("Cape Cod", "Cape");
    }

    public static Text shorten(Text line) {
        return TextHelper.replaceInRuns(line, SHORT);
    }

    public static String shorten(String text) {
        for (Map.Entry<String, String> e : SHORT.entrySet()) {
            text = text.replace(e.getKey(), e.getValue());
        }
        return text;
    }
}
