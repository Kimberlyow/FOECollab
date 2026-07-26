package io.github.foecollab.handler.screens.hud;

import io.github.foecollab.common.HudFont;
import io.github.foecollab.handler.CrewHandler;
import io.github.foecollab.util.TextHelper;
import io.github.foecollab.util.ThrottledCache;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CrewHudHandler {
    private static CrewHudHandler INSTANCE = new CrewHudHandler();

    private final ThrottledCache<Text> crewTextCache =
            new ThrottledCache<>(200L, () -> HudFont.recolor(this.buildCrewNearbyText()));

    public static CrewHudHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new CrewHudHandler();
        }
        return INSTANCE;
    }

    public Text assembleCrewNearbyText() {
        return crewTextCache.get();
    }

    private Text buildCrewNearbyText() {
        return TextHelper.concat(
                Text.literal("ᴄʀᴇᴡ ɴᴇᴀʀʙʏ: ").formatted(Formatting.GRAY),
                CrewHandler.instance().isCrewNearby ? Text.literal("✔").formatted(Formatting.DARK_GREEN) : Text.literal("✘").formatted(Formatting.DARK_RED)
        );
    }
}
