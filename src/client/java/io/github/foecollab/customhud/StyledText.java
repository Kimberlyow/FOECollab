package io.github.foecollab.customhud;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Text helpers for the custom-HUD engine: legacy ({@code §}) colour-code parsing that threads the
 * active style across placeholder boundaries, and a style-preserving component substring. Ported
 * from FishOnMC-Extras-R's ComponentHelper and Yarn-mapped.
 */
public class StyledText {
    private static final Pattern LEGACY = Pattern.compile("(§#[0-9A-Fa-f]{6}|§[0-9A-FK-ORa-fk-or])");

    public record StyledResult(MutableText text, Style style) {
    }

    public static StyledResult parseLegacy(String input, Style startingStyle) {
        MutableText component = Text.empty();
        Matcher matcher = LEGACY.matcher(input);

        int lastEnd = 0;
        Style currentStyle = startingStyle;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                component.append(Text.literal(input.substring(lastEnd, matcher.start())).setStyle(currentStyle));
            }

            String code = matcher.group();
            if (code.equalsIgnoreCase("§r")) {
                currentStyle = Style.EMPTY;
            } else if (code.startsWith("§#")) {
                int rgb = Integer.parseInt(code.substring(2), 16);
                currentStyle = currentStyle.withColor(rgb);
            } else {
                Formatting fmt = Formatting.byCode(code.charAt(1));
                if (fmt != null) {
                    currentStyle = currentStyle.withFormatting(fmt);
                }
            }

            lastEnd = matcher.end();
        }

        if (lastEnd < input.length()) {
            component.append(Text.literal(input.substring(lastEnd)).setStyle(currentStyle));
        }

        return new StyledResult(component, currentStyle);
    }

    public static StyledResult parseLegacy(String input) {
        return parseLegacy(input, Style.EMPTY);
    }

    /** Substring of a component by visible-character range, preserving each run's style. */
    public static Text substring(Text component, int start, int end) {
        int length = component.getString().length();

        if (start < 0 || end < 0 || start > end || end > length) {
            return Text.empty();
        }

        MutableText result = Text.empty();
        AtomicInteger index = new AtomicInteger(0);

        component.visit((style, string) -> {
            int strStart = index.get();

            if (strStart + string.length() > start && strStart < end) {
                int from = Math.max(0, start - strStart);
                int to = Math.min(string.length(), end - strStart);
                result.append(Text.literal(string.substring(from, to)).setStyle(style));
            }

            index.addAndGet(string.length());
            return Optional.empty();
        }, Style.EMPTY);

        return result;
    }

    public static String floatToString(float f, int decimals) {
        return String.format(Locale.US, "%." + decimals + "f", f);
    }
}
