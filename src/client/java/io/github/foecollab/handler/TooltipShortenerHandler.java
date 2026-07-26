package io.github.foecollab.handler;

import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.util.LocationNameHelper;
import io.github.foecollab.util.SimpleTagFont;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Tidies FishOnMC item tooltips (location names, simple tag squares). Runs LAST in the
 * tooltip callback so it never disturbs the fixed line indices the other tooltip handlers
 * ({@link PetTooltipHandler}, {@link FishingStatsHandler}, {@link ArmorHandler})
 * depend on.
 */
public class TooltipShortenerHandler {
    private static TooltipShortenerHandler INSTANCE = new TooltipShortenerHandler();
    private final FOEConfig config = FOEConfig.getConfig();

    public static TooltipShortenerHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new TooltipShortenerHandler();
        }
        return INSTANCE;
    }

    public void cleanTooltip(List<Text> textList) {
        if (textList.isEmpty()) {
            return;
        }

        if (config.cleanerDisplay.shortenLocationNames) {
            for (int i = 0; i < textList.size(); i++) {
                textList.set(i, LocationNameHelper.shorten(textList.get(i)));
            }
        }

        if (config.cleanerDisplay.simpleRankTags || config.cleanerDisplay.simpleRarityTags) {
            for (int i = 0; i < textList.size(); i++) {
                textList.set(i, SimpleTagFont.apply(textList.get(i), config.cleanerDisplay.simpleRankTags, config.cleanerDisplay.simpleRarityTags));
            }
        }
    }
}
