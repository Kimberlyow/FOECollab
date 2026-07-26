package io.github.foecollab.common;

import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.util.LocationNameHelper;
import io.github.foecollab.util.SimpleTagFont;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Global HUD text coloring. Vanilla HUD lines use gray for labels and white (or no
 * color) for values; this remaps those two roles to a configured base color and a
 * lighter tint of it, e.g. "ꜰɪѕʜ ᴄᴀᴜɢʜᴛ:" in purple and "12542" in light purple.
 * Any other color (rarity tags, FoE tags, ...) is left untouched so it keeps its meaning.
 */
public class HudFont {
    // Roles to remap, matched on the resolved color of each text run.
    private static final int LABEL_COLOR = 0xAAAAAA; // Formatting.GRAY
    private static final int VALUE_COLOR = 0xFFFFFF; // Formatting.WHITE
    // How far values are blended toward white relative to the base color (0 = same, 1 = white).
    private static final float LIGHTEN = 0.4f;

    public enum FontColor {
        OFF(0xFFFFFF),
        PURPLE(0xB45AF2),
        PINK(0xFF6EC7),
        BLUE(0x4AA3FF),
        RED(0xFF5555),
        ORANGE(0xFFA040),
        YELLOW(0xFFD93B),
        GREEN(0x55E06A),
        CYAN(0x40E0D0),
        CUSTOM(0xB45AF2);

        public final int base;
        FontColor(int base) {
            this.base = base;
        }
    }

    public static boolean isEnabled() {
        return FOEConfig.getConfig().hudFont.fontColor != FontColor.OFF;
    }

    /** Color used for labels / most text. */
    public static int baseColor() {
        var cfg = FOEConfig.getConfig().hudFont;
        return cfg.fontColor == FontColor.CUSTOM ? (cfg.customColor & 0xFFFFFF) : cfg.fontColor.base;
    }

    /** Lighter color used for values. */
    public static int valueColor() {
        return lighten(baseColor());
    }

    public static int lighten(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        r += Math.round((255 - r) * LIGHTEN);
        g += Math.round((255 - g) * LIGHTEN);
        b += Math.round((255 - b) * LIGHTEN);
        return (r << 16) | (g << 8) | b;
    }

    /** Recolor a single line. Returns the input unchanged when the feature is off. */
    public static Text recolor(Text text) {
        if (text == null || !isEnabled()) {
            return text;
        }
        return recolor(text, baseColor(), valueColor());
    }

    /** Recolor a list of lines without mutating the input list (it may be a cached instance). */
    public static List<Text> recolorAll(List<Text> texts) {
        if (texts == null || !isEnabled()) {
            return texts;
        }
        int base = baseColor();
        int value = valueColor();
        List<Text> out = new ArrayList<>(texts.size());
        for (Text text : texts) {
            out.add(recolor(text, base, value));
        }
        return out;
    }

    /// Applies the Cleaner Display options that make sense on a HUD line list — shortened
    /// location names and the simple-square tag font — to {@code lines} in place. Cheap enough
    /// to call from a handler's throttled (cached) build; do NOT call it per frame. The four
    /// dropped variant tags aren't in {@link SimpleTagFont}, so they keep the server's glyph.
    public static List<Text> applyCleanerDisplay(List<Text> lines) {
        FOEConfig config = FOEConfig.getConfig();
        if (config.cleanerDisplay.shortenLocationNames) {
            lines.replaceAll(LocationNameHelper::shorten);
        }
        if (config.cleanerDisplay.simpleRankTags || config.cleanerDisplay.simpleRarityTags) {
            lines.replaceAll(t -> SimpleTagFont.apply(t, config.cleanerDisplay.simpleRankTags, config.cleanerDisplay.simpleRarityTags));
        }
        return lines;
    }

    private static Text recolor(Text text, int base, int value) {
        MutableText result = Text.empty();
        text.visit((style, string) -> {
            result.append(Text.literal(string).setStyle(remap(style, base, value)));
            return Optional.empty();
        }, Style.EMPTY);
        return result;
    }

    private static Style remap(Style style, int base, int value) {
        TextColor color = style.getColor();
        if (color == null) {
            return style.withColor(value);
        }
        int rgb = color.getRgb();
        if (rgb == LABEL_COLOR) {
            return style.withColor(base);
        }
        if (rgb == VALUE_COLOR) {
            return style.withColor(value);
        }
        return style;
    }
}
