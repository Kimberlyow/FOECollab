package io.github.foecollab.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.github.foecollab.FOMC.Constant;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextHelper {
    private static final Gson gson = new Gson();

    public static MutableText concat(Text... texts) {
        MutableText text = Text.empty();
        for (Text t : texts) {
            text.append(t);
        }
        return text;
    }

    /**
     * Cleans up the spacer lines in an assembled HUD text list: drops blank lines at the
     * very top and bottom and collapses any run of consecutive blanks down to a single
     * spacer. Returns a new list; the input is left untouched. A line counts as blank when
     * its rendered string is empty or whitespace-only. This keeps intentional single-line
     * gaps between sections while removing the dangling gaps left when a section, title or
     * location line is toggled off.
     */
    public static List<Text> trimBlankLines(List<Text> lines) {
        List<Text> out = new ArrayList<>();
        boolean pendingBlank = false;
        for (Text line : lines) {
            if (line == null || line.getString().isBlank()) {
                // Remember the gap but only emit it once a later non-blank line needs it;
                // this drops leading/trailing blanks and collapses consecutive ones.
                pendingBlank = !out.isEmpty();
                continue;
            }
            if (pendingBlank) {
                out.add(Text.empty());
                pendingBlank = false;
            }
            out.add(line);
        }
        return out;
    }

    // Format to string
    public static String fmt(float d) {
        return String.format(Locale.US, "%.0f", d);
    }

    public static String fmt(float d, int decimalPlaces) {
        switch (decimalPlaces) {
            case 1 -> {
                return String.format(Locale.US,"%.1f", d).replaceAll("[\\.,]$", "");
            }
            case 2 -> {
                return String.format(Locale.US,"%.2f", d).replaceAll("[\\.,]$", "");
            }
            default -> {
                return String.format(Locale.US,"%.0f", d).replaceAll("[\\.,]$", "");
            }
        }
    }

    // Parse float that handles both comma and period decimal separators
    public static float parseFloat(String s) {
        if (s == null || s.isEmpty()) {
            throw new NumberFormatException("Cannot parse empty string");
        }
        // Replace comma with period for decimal separator
        String normalized = s.trim().replace(',', '.');
        return Float.parseFloat(normalized);
    }

    // Format to number string
    public static String fmnt(float d) {
        if (d >= 1000 && d < 1000000) {
            String s = String.format(Locale.US, "%.2f", d / 1000);
            return s.replaceAll("0*$", "").replaceAll("[\\.,]$", "") + "K";
        } else if (d >= 1000000 && d < 1000000000) {
            String s = String.format(Locale.US, "%.2f", d / 1000000);
            return s.replaceAll("0*$", "").replaceAll("[\\.,]$", "") + "M";
        } else if (d >= 1000000000) {
            String s = String.format(Locale.US, "%.2f", d / 1000000000);
            return s.replaceAll("0*$", "").replaceAll("[\\.,]$", "") + "B";
        } else if (d == 0) {
            return "0";
        } else {
            return String.format(Locale.US, "%.0f", d);
        }
    }

    // Like fmnt but with no decimals: for values >= 1000 it rounds to the nearest
    // thousand and renders "<n>K" (e.g. 1340 -> "1K", 1750 -> "2K"); smaller values are
    // shown as-is. Used for the bait/lure stack-count overlay, where the exact count (and
    // its "1.34K" decimals) just clutters the slot — matches FOE-R's rounded display.
    public static String fmntRoundThousands(int value) {
        if (value >= 1000) {
            return Math.round(value / 1000f) + "K";
        }
        return String.valueOf(value);
    }

    // Convert a string to small-caps / subscript glyphs (visually smaller text,
    // e.g. for compact stack counts). Digits become unicode subscripts, ASCII
    // letters become small-caps; everything else is left untouched.
    public static String smallCaps(String string) {
        char[] characters = string.toCharArray();
        for (int i = 0; i < characters.length; i++) {
            characters[i] = smallChar(characters[i]);
        }
        return String.valueOf(characters);
    }

    public static char smallChar(char c) {
        if (c >= '0' && c <= '9') {
            return (char) (c + 8272); // '0'..'9' -> '₀'..'₉'
        }
        return switch (c) {
            case 'A', 'a' -> 'ᴀ';
            case 'B', 'b' -> 'ʙ';
            case 'C', 'c' -> 'ᴄ';
            case 'D', 'd' -> 'ᴅ';
            case 'E', 'e' -> 'ᴇ';
            case 'F', 'f' -> 'ꜰ';
            case 'G', 'g' -> 'ɢ';
            case 'H', 'h' -> 'ʜ';
            case 'I', 'i' -> 'ɪ';
            case 'J', 'j' -> 'ᴊ';
            case 'K', 'k' -> 'ᴋ';
            case 'L', 'l' -> 'ʟ';
            case 'M', 'm' -> 'ᴍ';
            case 'N', 'n' -> 'ɴ';
            case 'O', 'o' -> 'ᴏ';
            case 'P', 'p' -> 'ᴘ';
            case 'Q', 'q' -> 'ꞯ';
            case 'R', 'r' -> 'ʀ';
            case 'S', 's' -> 's';
            case 'T', 't' -> 'ᴛ';
            case 'U', 'u' -> 'ᴜ';
            case 'V', 'v' -> 'ᴠ';
            case 'W', 'w' -> 'ᴡ';
            case 'X', 'x' -> 'x';
            case 'Y', 'y' -> 'ʏ';
            case 'Z', 'z' -> 'ᴢ';
            default -> c;
        };
    }

    /// Inverse of {@link #smallChar}: maps a small-caps / subscript glyph back to its ASCII
    /// letter or digit (lower-case), leaving anything else untouched.
    public static char deSmallChar(char c) {
        if (c >= '₀' && c <= '₉') {
            return (char) (c - 8272); // '₀'..'₉' -> '0'..'9'
        }
        return switch (c) {
            case 'ᴀ' -> 'a';
            case 'ʙ' -> 'b';
            case 'ᴄ' -> 'c';
            case 'ᴅ' -> 'd';
            case 'ᴇ' -> 'e';
            case 'ꜰ' -> 'f';
            case 'ɢ' -> 'g';
            case 'ʜ' -> 'h';
            case 'ɪ' -> 'i';
            case 'ᴊ' -> 'j';
            case 'ᴋ' -> 'k';
            case 'ʟ' -> 'l';
            case 'ᴍ' -> 'm';
            case 'ɴ' -> 'n';
            case 'ᴏ' -> 'o';
            case 'ᴘ' -> 'p';
            case 'ꞯ' -> 'q';
            case 'ʀ' -> 'r';
            case 'ᴛ' -> 't';
            case 'ᴜ' -> 'u';
            case 'ᴠ' -> 'v';
            case 'ᴡ' -> 'w';
            case 'ʏ' -> 'y';
            case 'ᴢ' -> 'z';
            case 'ѕ' -> 's'; // FishOnMC uses Cyrillic 'ѕ' (U+0455) as the small-caps 's'
            default -> c; // small-caps 'x' already uses the ASCII glyph
        };
    }

    /// Normalises text for loose matching against an NBT value: maps small-caps/subscript
    /// glyphs back to ASCII, keeps only letters and digits, and lower-cases. Lets a tooltip
    /// line be matched regardless of font (small-caps vs ASCII), casing, or separators
    /// (spaces vs underscores).
    public static String normalizeLoose(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = deSmallChar(s.charAt(i));
            if (Character.isLetterOrDigit(c)) {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    public static boolean isSmallNumber(char c) {
        return c >= '₀' && c <= '₉'; // '₀'..'₉'
    }

    public static boolean isSmallLetter(char c) {
        return switch (c) {
            case 'ᴀ', 'ʙ', 'ᴄ', 'ᴅ', 'ᴇ', 'ꜰ', 'ɢ', 'ʜ', 'ɪ', 'ᴊ', 'ᴋ', 'ʟ', 'ᴍ', 'ɴ', 'ᴏ', 'ᴘ', 'ꞯ', 'ʀ', 's', 'ᴛ',
                 'ᴜ', 'ᴠ', 'ᴡ', 'x', 'ʏ', 'ᴢ', '.', ',', ':', ';', '_' -> true;
            default -> false;
        };
    }

    public static int ordinalIndexOf(String str, String substr, int n) {
        int pos = str.indexOf(substr);
        while (--n > 0 && pos != -1)
            pos = str.indexOf(substr, pos + 1);
        return pos;
    }

    public static String textToJson(Text text) {
        return gson.toJson(TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, text).getOrThrow());
    }

    public static Text jsonToText(String text) {
        return TextCodecs.CODEC
                .decode(JsonOps.INSTANCE, gson.fromJson(text, JsonElement.class))
                .getOrThrow()
                .getFirst();
    }

    public static String upperCaseAllFirstCharacter(String text) {
        String regex = "\\b(.)(.*?)\\b";
        return Pattern.compile(regex).matcher(text).replaceAll(
                matched -> matched.group(1).toUpperCase() + matched.group(2));
    }

    public static float roundFirstSignificantDigit(float input) {
        if (!Float.isNaN(input) && !Float.isInfinite(input)) {
            if (input >= 0.1f || input == 0) {
                return input;
            }

            int precision = 0;
            float val = input - Math.round(input);
            while (Math.abs(val) < 1) {
                val *= 10;
                precision++;
            }
            return BigDecimal.valueOf(input).setScale(precision, RoundingMode.HALF_UP).floatValue();
        }
        return input;
    }

    public static String replaceToFoE(String text) {
        return replaceToFoE(text, false);
    }

    public static String replaceToFoE(String text, boolean usePurple) {
        Constant foeTag = usePurple ? Constant.FOE_PURPLE : Constant.FOE;
        if (text.contains(Constant.ANGLER.TAG.getString()))
            text = text.replace(Constant.ANGLER.TAG.getString(), foeTag.TAG.getString());
        if (text.contains(Constant.SAILOR.TAG.getString()))
            text = text.replace(Constant.SAILOR.TAG.getString(), foeTag.TAG.getString());
        if (text.contains(Constant.MARINER.TAG.getString()))
            text = text.replace(Constant.MARINER.TAG.getString(), foeTag.TAG.getString());
        if (text.contains(Constant.CAPTAIN.TAG.getString()))
            text = text.replace(Constant.CAPTAIN.TAG.getString(), foeTag.TAG.getString());
        if (text.contains(Constant.ADMIRAL.TAG.getString()))
            text = text.replace(Constant.ADMIRAL.TAG.getString(), foeTag.TAG.getString());
        return text;
    }

    /**
     * Splits a Text object by newlines and returns a list of Text objects, one per line.
     * This preserves the original Text structure and formatting by traversing the Text tree.
     */
    public static List<Text> splitByNewlines(Text text) {
        List<Text> lines = new ArrayList<>();
        if (text == null) {
            return lines;
        }

        // Check if there are any newlines first
        if (!text.getString().contains("\n")) {
            lines.add(text);
            return lines;
        }

        // Build lines by processing the root and siblings sequentially
        MutableText currentLine = Text.empty();
        
        // Process root content if it's not empty (Text.empty() creates an empty root)
        String rootContent = getRootContent(text);
        if (!rootContent.isEmpty() && !rootContent.equals("\n")) {
            if (rootContent.contains("\n")) {
                // Split root content by newlines
                String[] parts = rootContent.split("\n", -1);
                for (int i = 0; i < parts.length; i++) {
                    if (!parts[i].isEmpty()) {
                        MutableText partText = Text.literal(parts[i]);
                        if (text.getStyle() != null) {
                            partText.setStyle(text.getStyle());
                        }
                        currentLine.append(partText);
                    }
                    if (i < parts.length - 1) {
                        if (!currentLine.getString().isEmpty()) {
                            lines.add(currentLine);
                        }
                        currentLine = Text.empty();
                    }
                }
            } else {
                // Add root content to current line
                MutableText rootText = Text.literal(rootContent);
                if (text.getStyle() != null) {
                    rootText.setStyle(text.getStyle());
                }
                currentLine.append(rootText);
            }
        }
        
        // Process siblings sequentially
        for (Text sibling : text.getSiblings()) {
            String siblingContent = sibling.getString();
            
            // Check if this sibling is a newline
            if (siblingContent.equals("\n")) {
                // Save current line and start a new one
                if (!currentLine.getString().isEmpty()) {
                    lines.add(currentLine);
                }
                currentLine = Text.empty();
                continue;
            }
            
            // Check if sibling content contains newlines
            if (siblingContent.contains("\n")) {
                // Split the sibling content by newlines
                String[] parts = siblingContent.split("\n", -1);
                for (int i = 0; i < parts.length; i++) {
                    if (!parts[i].isEmpty()) {
                        // Create a new Text with the same style as the sibling
                        MutableText partText = Text.literal(parts[i]);
                        if (sibling.getStyle() != null) {
                            partText.setStyle(sibling.getStyle());
                        }
                        currentLine.append(partText);
                    }
                    
                    // After each part except the last, start a new line
                    if (i < parts.length - 1) {
                        if (!currentLine.getString().isEmpty()) {
                            lines.add(currentLine);
                        }
                        currentLine = Text.empty();
                    }
                }
            } else if (!siblingContent.isEmpty()) {
                // No newlines, just append the sibling preserving its style
                currentLine.append(sibling.copy());
            }
        }
        
        // Add the last line if it's not empty
        if (!currentLine.getString().isEmpty()) {
            lines.add(currentLine);
        }
        
        return lines;
    }
    
    /**
     * Gets the root content string of a Text node without including its siblings.
     */
    private static String getRootContent(Text text) {
        // If the text has no siblings, getString() gives us just the content
        if (text.getSiblings().isEmpty()) {
            return text.getString();
        }
        
        // If it has siblings, we need to get just the root content
        // We can do this by getting the string and subtracting sibling strings
        String fullString = text.getString();
        StringBuilder siblingStrings = new StringBuilder();
        for (Text sibling : text.getSiblings()) {
            siblingStrings.append(sibling.getString());
        }
        
        // Remove sibling strings from the full string to get root content
        String rootContent = fullString;
        if (siblingStrings.length() > 0) {
            int siblingStart = fullString.indexOf(siblingStrings.toString());
            if (siblingStart >= 0) {
                rootContent = fullString.substring(0, siblingStart);
            }
        }
        
        return rootContent;
    }

    // ---- Styled-text editing helpers --------------------------------------
    // These flatten a Text tree into a parallel char + per-char Style stream,
    // edit it, then rebuild a MutableText grouping consecutive equal styles.
    // The tree shape is lost but the visual styling is preserved exactly, which
    // is all that matters for tooltip / chat display.

    private static StringBuilder flattenChars(Text text, List<Style> outStyles) {
        StringBuilder sb = new StringBuilder();
        text.visit((style, str) -> {
            for (int i = 0; i < str.length(); i++) {
                sb.append(str.charAt(i));
                outStyles.add(style);
            }
            return Optional.empty();
        }, Style.EMPTY);
        return sb;
    }

    private static MutableText rebuildChars(CharSequence text, List<Style> styles) {
        MutableText out = Text.empty();
        int n = text.length();
        int i = 0;
        while (i < n) {
            Style s = styles.get(i);
            int j = i + 1;
            while (j < n && Objects.equals(styles.get(j), s)) {
                j++;
            }
            out.append(Text.literal(text.subSequence(i, j).toString()).setStyle(s));
            i = j;
        }
        return out;
    }

    /** Returns a copy of {@code text} with the given font applied to every char matching {@code isTarget}. */
    public static Text setFontForChars(Text text, IntPredicate isTarget, Identifier font) {
        StyleSpriteSource fontSource = new StyleSpriteSource.Font(font);
        List<Style> styles = new ArrayList<>();
        StringBuilder chars = flattenChars(text, styles);
        boolean changed = false;
        for (int i = 0; i < chars.length(); i++) {
            if (isTarget.test(chars.charAt(i))) {
                styles.set(i, styles.get(i).withFont(fontSource));
                changed = true;
            }
        }
        return changed ? rebuildChars(chars, styles) : text;
    }

    /** Returns a copy of {@code text} with the first regex match deleted, preserving styling of the rest. */
    public static Text deleteFirstMatch(Text text, Pattern pattern) {
        List<Style> styles = new ArrayList<>();
        StringBuilder chars = flattenChars(text, styles);
        Matcher m = pattern.matcher(chars);
        if (!m.find()) {
            return text;
        }
        chars.delete(m.start(), m.end());
        styles.subList(m.start(), m.end()).clear();
        return rebuildChars(chars, styles);
    }

    /** Returns true if any whole tooltip line's flattened string matches the pattern. */
    public static boolean matches(Text text, Pattern pattern) {
        return pattern.matcher(text.getString()).find();
    }

    /** Replaces every key with its value inside each styled run, preserving that run's style. */
    public static Text replaceInRuns(Text text, Map<String, String> replacements) {
        boolean[] changed = {false};
        MutableText out = Text.empty();
        text.visit((style, str) -> {
            String replaced = str;
            for (Map.Entry<String, String> e : replacements.entrySet()) {
                if (replaced.contains(e.getKey())) {
                    replaced = replaced.replace(e.getKey(), e.getValue());
                    changed[0] = true;
                }
            }
            out.append(Text.literal(replaced).setStyle(style));
            return Optional.empty();
        }, Style.EMPTY);
        return changed[0] ? out : text;
    }
}
