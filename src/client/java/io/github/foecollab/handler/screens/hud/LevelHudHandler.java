package io.github.foecollab.handler.screens.hud;

import io.github.foecollab.FOMC.LevelColors;
import io.github.foecollab.common.HudFont;
import io.github.foecollab.handler.ScoreboardHandler;
import io.github.foecollab.util.TextHelper;
import io.github.foecollab.util.ThrottledCache;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/// Builds the standalone level + XP-progress line for {@link io.github.foecollab.screens.hud.LevelHud}
/// — the same level number + strikethrough-space progress bar + percent the top bar shows on its left,
/// without the player name. Shown only while the top bar is hidden. Rebuilt from the scoreboard data
/// (slow-changing), so it's throttled/cached like the other HUD handlers.
public class LevelHudHandler {
    private static LevelHudHandler INSTANCE = new LevelHudHandler();

    private final ThrottledCache<List<Text>> cache =
            new ThrottledCache<>(200L, () -> HudFont.recolorAll(this.buildText()));

    public static LevelHudHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new LevelHudHandler();
        }
        return INSTANCE;
    }

    public List<Text> assembleText() {
        return cache.get();
    }

    private List<Text> buildText() {
        ScoreboardHandler scoreboard = ScoreboardHandler.instance();
        int level = scoreboard.level;
        float percent = scoreboard.percentLevel * 100f;

        // 20-cell strikethrough-space bar, matching the top bar's left section.
        float repetitions = 100f / 20f;
        float filledCells = (float) Math.floor(scoreboard.percentLevel * 100);
        float emptyCells = 100 - filledCells;

        Text line = TextHelper.concat(
                Text.literal("ʟᴇᴠᴇʟ ").formatted(Formatting.GRAY),
                Text.literal("[").formatted(Formatting.DARK_GRAY),
                Text.literal(String.valueOf(level)).withColor(LevelColors.valueOfLvl(level).color),
                Text.literal("] ").formatted(Formatting.DARK_GRAY),
                Text.literal(" ".repeat(Math.round(filledCells / repetitions))).formatted(Formatting.GREEN, Formatting.STRIKETHROUGH),
                Text.literal(" ".repeat(Math.round(emptyCells / repetitions))).formatted(Formatting.DARK_GRAY, Formatting.STRIKETHROUGH),
                Text.literal(" (").formatted(Formatting.DARK_GRAY),
                Text.literal(TextHelper.fmt(percent)).formatted(Formatting.GRAY),
                Text.literal("%").formatted(Formatting.GRAY),
                Text.literal(")").formatted(Formatting.DARK_GRAY));

        return new ArrayList<>(List.of(line));
    }
}
