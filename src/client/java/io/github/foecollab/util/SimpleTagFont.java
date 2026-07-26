package io.github.foecollab.util;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Set;

/**
 * Re-styles the FishOnMC rank / rarity / variant tag glyphs (the {@code \\uF0xx}
 * private-use codepoints) to use the mod's own {@code foecollab:tags} font, which
 * draws them as compact coloured squares with a letter.
 *
 * <p>Why a font swap instead of a resource pack: those glyphs are supplied by the
 * FishOnMC <i>server</i> resource pack, which loads at the highest priority and so
 * overrides both the mod's assets and any pack in the {@code resourcepacks} folder.
 * A separate font id that only the mod defines is the one thing the server pack
 * can't shadow, so we leave the codepoint in place and just change which font
 * renders it.
 *
 * <p>The glyphs are split into two independently-toggleable groups: player <b>ranks</b>
 * and item <b>rarity/variant</b> tags.
 */
public class SimpleTagFont {
    public static final Identifier FONT = Identifier.of("foecollab", "tags");

    // Player ranks (staff + the angler progression: sailor/angler/mariner/captain/admiral, …).
    private static final Set<Integer> RANK_CHARS = Set.of(
            0xF021, // owner
            0xF022, // admin
            0xF023, // manager
            0xF024, // staff
            0xF026, // designer
            0xF027, // builder
            0xF028, // admiral
            0xF029, // captain
            0xF030, // mariner
            0xF031, // sailor
            0xF032, // angler
            0xF066, // media
            0xF088  // community manager
    );

    // Item rarity + event-variant tags. (Albino / melanistic / trophy / fabled are intentionally
    // left out so they keep the server's original glyph — their simplified squares were removed.)
    private static final Set<Integer> RARITY_CHARS = Set.of(
            0xF033, // common
            0xF034, // rare
            0xF035, // epic
            0xF036, // legendary
            0xF037, // mythical
            0xF092, // special
            0xF098, // alternate
            0xF102, // spooky
            0xF179  // frozen
    );

    public static boolean isTagChar(int codepoint) {
        return RANK_CHARS.contains(codepoint) || RARITY_CHARS.contains(codepoint);
    }

    /**
     * Re-fonts the tag glyphs in {@code text}, restricted to the enabled groups. Returns the text
     * unchanged when both groups are disabled (or when {@code text} is null).
     */
    public static Text apply(Text text, boolean ranks, boolean rarities) {
        if (text == null || (!ranks && !rarities)) {
            return text;
        }
        return TextHelper.setFontForChars(text,
                c -> (ranks && RANK_CHARS.contains(c)) || (rarities && RARITY_CHARS.contains(c)),
                FONT);
    }
}
