package io.github.foecollab.handler;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The hologram of a placed chummer from an entity dump of 2026-10-17 (a common
 * chummer, 14m 55s left) and the other text displays in that dump.
 */
class ChummerHologramTest {
    /** The text display 0.5 above the chummer's head, as the dump printed it (the glyph on line two is the common rarity tag). */
    private static final String DUMP = "Chummer™\n\nActivated by meowfih\n\nBite Speed: +75\n14m 55s Remaining";

    private static ChummerHologram.Chummer parse(String text) {
        return ChummerHologram.parse(text).orElseThrow(() -> new AssertionError("not a chummer: " + text));
    }

    private static String hologram(char rarity, String owner, String bite, String left) {
        return "Chummer™\n" + rarity + "\nActivated by " + owner + "\n\nBite Speed: " + bite + "\n" + left;
    }

    @Test
    void readsTheRealDump() {
        ChummerHologram.Chummer c = parse(DUMP);
        assertEquals("meowfih", c.owner());
        assertEquals(75, c.bite());
        assertEquals(0xF033, c.rarityGlyph());
        assertEquals("common", c.rarity());
        assertEquals(14 * 60 + 55, c.seconds());
    }

    @Test
    void everyRarityGlyphAndItsBiteBonus() {
        String[] names = {"common", "rare", "epic", "legendary", "mythical"};
        int[] bonus = {75, 130, 180, 250, 375};
        for (int i = 0; i < names.length; i++) {
            ChummerHologram.Chummer c = parse(hologram((char) (0xF033 + i), "noawah", "+" + bonus[i], "15m 0s Remaining"));
            assertEquals(names[i], c.rarity());
            assertEquals(bonus[i], c.bite());
            assertEquals(0xF033 + i, c.rarityGlyph());
        }
    }

    @Test
    void anUnknownGlyphKeepsTheCodepointButHasNoRarityName() {
        ChummerHologram.Chummer c = parse(hologram('', "noawah", "+75", "1m Remaining"));
        assertEquals(0xF092, c.rarityGlyph());
        assertEquals("", c.rarity());
    }

    @Test
    void otherHologramsFromTheSameDumpAreNotChummers() {
        assertEquals(Optional.empty(), ChummerHologram.parse("Vondopolous Ablehower II"));
        assertEquals(Optional.empty(), ChummerHologram.parse("Largent Dinkenschmoofle"));
        assertEquals(Optional.empty(), ChummerHologram.parse(" \n[278] meowfih\n368.6K")); // a player's nameplate
    }

    @Test
    void aNameplateOfAPlayerNamedChummerIsNotAChummer() {
        assertEquals(Optional.empty(), ChummerHologram.parse(" \n[12] Chummer™\n1.2K"));
        assertEquals(Optional.empty(), ChummerHologram.parse("Chummer™")); // a lone first line, nobody activated it
    }

    @Test
    void needsTheFirstLineAndTheOwnerLine() {
        assertEquals(Optional.empty(), ChummerHologram.parse("Activated by meowfih\nBite Speed: +75\n14m 55s Remaining"));
        assertEquals(Optional.empty(), ChummerHologram.parse("Chummer\n\nActivated by meowfih")); // no trademark sign
        assertEquals(Optional.empty(), ChummerHologram.parse("Hello™\n\nActivated by meowfih"));
        assertEquals(Optional.empty(), ChummerHologram.parse("Chummer™\n\nActivated by \nBite Speed: +75"));
    }

    @Test
    void nothingToParse() {
        assertEquals(Optional.empty(), ChummerHologram.parse(null));
        assertEquals(Optional.empty(), ChummerHologram.parse(""));
        assertEquals(Optional.empty(), ChummerHologram.parse("\n\n"));
    }

    @Test
    void remainingTimeFormats() {
        assertEquals(14 * 60 + 55, parse(hologram('', "a", "+75", "14m 55s Remaining")).seconds());
        assertEquals(55, parse(hologram('', "a", "+75", "55s Remaining")).seconds());
        assertEquals(15 * 60, parse(hologram('', "a", "+75", "15m Remaining")).seconds());
        assertEquals(15 * 60, parse(hologram('', "a", "+75", "15m 0s Remaining")).seconds());
        assertEquals(0, parse(hologram('', "a", "+75", "0s Remaining")).seconds());
        assertEquals(3600 + 2 * 60 + 3, parse(hologram('', "a", "+75", "1h 2m 3s Remaining")).seconds());
        assertEquals(15 * 60, parse(hologram('', "a", "+75", "15m 0s remaining")).seconds());
    }

    @Test
    void aMissingOrUnknownTimeLineIsMinusOne() {
        assertEquals(-1, parse("Chummer™\n\nActivated by meowfih\n\nBite Speed: +75").seconds());
        assertEquals(-1, parse(hologram('', "a", "+75", "Remaining")).seconds());
        assertEquals(-1, parse(hologram('', "a", "+75", "soon")).seconds());
    }

    @Test
    void aMissingBiteLineIsMinusOne() {
        ChummerHologram.Chummer c = parse("Chummer™\n\nActivated by meowfih\n\n14m 55s Remaining");
        assertEquals(-1, c.bite());
        assertEquals(895, c.seconds());
    }

    @Test
    void serverGlyphsAndFormattingCodesAreStrippedBeforeMatching() {
        ChummerHologram.Chummer c = parse(" §fChummer™\n\nActivated by   meowfih \n\nBite Speed: +130\n 1m 2s Remaining");
        assertEquals("meowfih", c.owner());
        assertEquals("rare", c.rarity());
        assertEquals(130, c.bite());
        assertEquals(62, c.seconds());
    }

    @Test
    void theOwnerLineIsMatchedWhateverTheCase() {
        assertTrue(ChummerHologram.parse("CHUMMER™\n\nACTIVATED BY meowfih").isPresent());
    }
}
