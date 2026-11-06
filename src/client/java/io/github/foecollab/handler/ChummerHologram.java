package io.github.foecollab.handler;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads the hologram a placed chummer carries: a text display whose lines are
 * {@code Chummer™}, the rarity tag glyph, {@code Activated by <name>}, a blank line, {@code Bite Speed: +75} and
 * {@code 14m 55s Remaining}. Pure string code, so the tests run it without the game.
 *
 * <p>A chummer is found by its first line plus the {@code Activated by} line, never by the player head under it
 * (the same skin and name show up on a lot of decoration). Server glyphs (private use and hangul) are dropped
 * from every line before matching; the rarity glyph is read from the raw line first.
 */
public final class ChummerHologram {
    private static final String HEADER = "chummer™";
    private static final String ACTIVATED = "activated by ";
    private static final Pattern BITE = Pattern.compile("^bite speed:\\s*\\+?(\\d{1,6})");
    private static final Pattern LEFT = Pattern.compile("^((?:\\d{1,6}\\s*[hms]\\s*)+)remaining$");
    private static final Pattern PART = Pattern.compile("(\\d+)\\s*([hms])");
    private static final String[] RARITIES = {"common", "rare", "epic", "legendary", "mythical"};

    private ChummerHologram() {}

    /**
     * One chummer as its hologram reads. {@code bite} and {@code seconds} are -1 when their line is missing, {@code rarityGlyph}
     * is 0 when no glyph sits between the first line and the owner line.
     */
    public record Chummer(String owner, int bite, int rarityGlyph, int seconds) {
        /** common, rare, epic, legendary or mythical (glyphs U+F033 to U+F037), or "" for any other glyph. */
        public String rarity() {
            int i = rarityGlyph - 0xF033;
            return i >= 0 && i < RARITIES.length ? RARITIES[i] : "";
        }
    }

    /** The chummer this hologram text describes, or empty when it is anything else (a nameplate, a pet name, a hologram). */
    public static Optional<Chummer> parse(String text) {
        if (text == null || text.indexOf('™') < 0) { // cheap exit: every other hologram on the server
            return Optional.empty();
        }
        String[] raw = text.split("\n", -1);
        int header = -1;
        int activated = -1;
        String owner = "";
        int glyph = 0;
        int bite = -1;
        int seconds = -1;
        for (int i = 0; i < raw.length; i++) {
            String line = clean(raw[i]);
            if (header < 0) {
                if (line.isEmpty()) {
                    continue;
                }
                if (!line.toLowerCase(Locale.ROOT).equals(HEADER)) {
                    return Optional.empty(); // the first line says what a hologram is
                }
                header = i;
            } else if (activated < 0) {
                if (line.toLowerCase(Locale.ROOT).startsWith(ACTIVATED)) {
                    owner = line.substring(ACTIVATED.length()).strip();
                    activated = i;
                } else if (glyph == 0) {
                    glyph = firstGlyph(raw[i]);
                }
            } else {
                Matcher m = BITE.matcher(line.toLowerCase(Locale.ROOT));
                if (bite < 0 && m.find()) {
                    bite = Integer.parseInt(m.group(1));
                    continue;
                }
                m = LEFT.matcher(line.toLowerCase(Locale.ROOT));
                if (seconds < 0 && m.matches()) {
                    seconds = seconds(m.group(1));
                }
            }
        }
        if (activated < 0 || owner.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Chummer(owner, bite, glyph, seconds));
    }

    /** A line without server glyphs, formatting codes and the spaces around it. */
    private static String clean(String line) {
        StringBuilder out = new StringBuilder(line.length());
        for (int i = 0; i < line.length(); ) {
            int cp = line.codePointAt(i);
            i += Character.charCount(cp);
            if (cp == '§') { // a formatting code and its letter
                i = Math.min(line.length(), i + 1);
            } else if (!isArt(cp) && !Character.isISOControl(cp)) {
                out.appendCodePoint(cp);
            }
        }
        return out.toString().strip();
    }

    /** The first server glyph on a raw line, or 0. */
    private static int firstGlyph(String line) {
        for (int i = 0; i < line.length(); ) {
            int cp = line.codePointAt(i);
            if (isArt(cp)) {
                return cp;
            }
            i += Character.charCount(cp);
        }
        return 0;
    }

    /** "14m 55s" as 895, "1h 2m 3s" as 3723. */
    private static int seconds(String parts) {
        int total = 0;
        Matcher m = PART.matcher(parts);
        while (m.find()) {
            int n = Integer.parseInt(m.group(1));
            total += switch (m.group(2).charAt(0)) {
                case 'h' -> n * 3600;
                case 'm' -> n * 60;
                default -> n;
            };
        }
        return total;
    }

    /** Server font glyphs: private use areas and the hangul block the resource pack draws over. */
    private static boolean isArt(int cp) {
        return (cp >= 0xE000 && cp <= 0xF8FF) || cp >= 0xF0000 || (cp >= 0xAC00 && cp <= 0xD7A3);
    }
}
