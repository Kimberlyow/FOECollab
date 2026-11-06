package io.github.foecollab.screens.hud;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChummerRingGeometryTest {
    private static List<float[]> ring(float cx, float y, float cz) {
        List<float[]> vertices = new ArrayList<>();
        ChummerRingGeometry.emit(cx, y, cz, (x, vy, z) -> vertices.add(new float[]{x, vy, z}));
        return vertices;
    }

    private static double distance(float[] v, float cx, float cz) {
        return Math.hypot(v[0] - cx, v[2] - cz);
    }

    @Test
    void oneQuadPerSegment() {
        assertEquals(ChummerRingGeometry.SEGMENTS * 4, ring(0, 0, 0).size());
    }

    @Test
    void theWallStandsOneBlockHighOnTheGivenGround() {
        for (int i = 0; i < ring(3.5f, -1.25f, -7f).size(); i += 4) {
            List<float[]> v = ring(3.5f, -1.25f, -7f);
            assertEquals(-1.25f, v.get(i)[1]);
            assertEquals(-0.25f, v.get(i + 1)[1], 1e-6);
            assertEquals(-0.25f, v.get(i + 2)[1], 1e-6);
            assertEquals(-1.25f, v.get(i + 3)[1]);
        }
        assertEquals(1.0, ChummerRingGeometry.HEIGHT);
    }

    @Test
    void everyCornerIsOnTheRangeAroundTheCentre() {
        float cx = -10.5f; // any centre must work
        float cz = 17.25f;
        for (float[] v : ring(cx, 0, cz)) {
            assertEquals(ChummerRingGeometry.RADIUS, distance(v, cx, cz), 1e-3);
        }
    }

    @Test
    void theWallIsClosedAndHasNoGaps() {
        List<float[]> v = ring(0, 0, 0);
        int quads = v.size() / 4;
        for (int i = 0; i < quads; i++) {
            float[] topEnd = v.get(i * 4 + 2);
            float[] bottomEnd = v.get(i * 4 + 3);
            float[] nextTopStart = v.get(((i + 1) % quads) * 4 + 1);
            float[] nextBottomStart = v.get(((i + 1) % quads) * 4);
            assertEquals(topEnd[0], nextTopStart[0], 1e-4);
            assertEquals(topEnd[2], nextTopStart[2], 1e-4);
            assertEquals(bottomEnd[0], nextBottomStart[0], 1e-4);
            assertEquals(bottomEnd[2], nextBottomStart[2], 1e-4);
        }
    }

    @Test
    void theFallbackSitsBelowTheHologramLikeInTheDump() {
        // a real chummer: the text display at y 27.98, the ground at 26.0
        assertEquals(26.0, ChummerRingGeometry.fallbackY(27.98), 1e-9);
    }

    @Test
    void rangeIsTwentyBlocks() {
        assertEquals(20.0, ChummerRingGeometry.RADIUS);
    }

    @Test
    void colourIsTranslucentAndKeepsOnlyTheRgbOfTheSetting() {
        int argb = ChummerRingGeometry.argb(0x00E5FF);
        assertEquals(ChummerRingGeometry.ALPHA, argb >>> 24);
        assertEquals(0x00E5FF, argb & 0xFFFFFF);
        assertEquals(0x00E5FF, ChummerRingGeometry.argb(0xFF00E5FF) & 0xFFFFFF);
        assertTrue(ChummerRingGeometry.ALPHA > 0 && ChummerRingGeometry.ALPHA < 0xFF);
    }
}
