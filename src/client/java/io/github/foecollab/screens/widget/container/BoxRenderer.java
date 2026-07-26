package io.github.foecollab.screens.widget.container;

import io.github.foecollab.FOECollab;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

/**
 * Draws FishOnMC-Extras-R's "box" panels — a 15x15 atlas split into 5px nine-slice pieces, stretched
 * to any size. Ported from FOE-R's {@code BoxElement} (texture assets pulled from that repo with the
 * creator's permission). Used by the modern inventory-button look.
 */
public final class BoxRenderer {
    public static final Identifier BOX = tex("box_atlas");
    public static final Identifier BOX_SOLID = tex("box_solid_atlas");
    public static final Identifier BOX_ALT = tex("box_alt_atlas");
    public static final Identifier BOX_SOLID_ALT = tex("box_solid_alt_atlas");

    private static final int TEX = 15;
    private static final int SLICE = 5;

    private BoxRenderer() {
    }

    private static Identifier tex(String name) {
        return Identifier.of(FOECollab.MOD_ID, "textures/gui/sprites/elements/" + name + ".png");
    }

    public static void draw(DrawContext context, Identifier texture, int x, int y, int width, int height) {
        draw(context, texture, x, y, width, height, 0xFFFFFFFF);
    }

    public static void draw(DrawContext context, Identifier texture, int x, int y, int width, int height, int color) {
        int s = SLICE;
        int mw = width - s * 2;
        int mh = height - s * 2;

        // Corners
        blit(context, texture, x, y, 0, 0, s, s, color);
        blit(context, texture, x + width - s, y, TEX - s, 0, s, s, color);
        blit(context, texture, x, y + height - s, 0, TEX - s, s, s, color);
        blit(context, texture, x + width - s, y + height - s, TEX - s, TEX - s, s, s, color);

        // Edges
        if (mw > 0) {
            blit(context, texture, x + s, y, s, 0, mw, s, color);
            blit(context, texture, x + s, y + height - s, s, TEX - s, mw, s, color);
        }
        if (mh > 0) {
            blit(context, texture, x, y + s, 0, s, s, mh, color);
            blit(context, texture, x + width - s, y + s, TEX - s, s, s, mh, color);
        }

        // Center
        if (mw > 0 && mh > 0) {
            blit(context, texture, x + s, y + s, s, s, mw, mh, color);
        }
    }

    private static void blit(DrawContext context, Identifier texture, int x, int y, int u, int v, int width, int height, int color) {
        // Region is always one 5x5 slice, stretched onto the destination width/height.
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, x, y, (float) u, (float) v, width, height, SLICE, SLICE, TEX, TEX, color);
    }
}
