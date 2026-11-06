package io.github.foecollab.screens.hud;

/**
 * Where and how big the chummer range ring is: a one block high wall of quads standing on the ground, 20 blocks
 * out horizontally from the chummer's hologram. No Minecraft classes, so the tests run it without the game;
 * {@link ChummerRangeRenderer} feeds the vertices to the world renderer.
 */
public final class ChummerRingGeometry {
    /** Every chummer placed today reaches 20 blocks horizontally from the middle of its hologram. */
    public static final double RADIUS = 20.0;
    public static final int SEGMENTS = 128;
    /** The wall is this tall, standing on the ground. */
    public static final double HEIGHT = 1.0;
    /** The wall starts this far above the block top, so its bottom edge does not fight the ground. */
    public static final double LIFT = 0.02;
    /** The hologram floats about this far above the ground; only the fallback when no block is found under it. */
    public static final double BELOW_TEXT = 2.0;
    /** Translucent: the colour option is rgb only. */
    public static final int ALPHA = 0xA0;

    private static final float[] COS = new float[SEGMENTS + 1];
    private static final float[] SIN = new float[SEGMENTS + 1];

    static {
        for (int i = 0; i <= SEGMENTS; i++) {
            double angle = Math.PI * 2 * i / SEGMENTS;
            COS[i] = (float) Math.cos(angle);
            SIN[i] = (float) Math.sin(angle);
        }
    }

    private ChummerRingGeometry() {}

    /** Receives the corners of the quads, four per segment, going round the wall. */
    @FunctionalInterface
    public interface Sink {
        void vertex(float x, float y, float z);
    }

    /** The bottom of the wall when no ground was found under a hologram at {@code textY}. */
    public static double fallbackY(double textY) {
        return textY - BELOW_TEXT + LIFT;
    }

    /** The ring colour: the rgb of the option with the ring's alpha. */
    public static int argb(int rgb) {
        return ALPHA << 24 | (rgb & 0xFFFFFF);
    }

    /** The whole wall around {@code (cx, y, cz)}, {@code y} being its bottom: {@link #SEGMENTS} upright quads. */
    public static void emit(float cx, float y, float cz, Sink sink) {
        float r = (float) RADIUS;
        float top = y + (float) HEIGHT;
        for (int i = 0; i < SEGMENTS; i++) {
            sink.vertex(cx + COS[i] * r, y, cz + SIN[i] * r);
            sink.vertex(cx + COS[i] * r, top, cz + SIN[i] * r);
            sink.vertex(cx + COS[i + 1] * r, top, cz + SIN[i + 1] * r);
            sink.vertex(cx + COS[i + 1] * r, y, cz + SIN[i + 1] * r);
        }
    }
}
