package io.github.foecollab.handler;

import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.screens.hud.ChummerRingGeometry;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// From the hologram text of a placed chummer to what the HUD and the ring use: rarity, timer,
/// the range the HUD shows in and the ring's corners on that range.
class ChummerHandlerTest {
    /// The real hologram of a common chummer (the glyph on line two is the common rarity tag),
    /// standing on the ground at y 26 under the text display's x and z.
    private static final String DUMP = "Chummer™\n\uF033\nActivated by meowfih\n\nBite Speed: +75\n14m 55s Remaining";
    private static final Vec3d SPOT = new Vec3d(-810.5, 26.02, 1065.5);
    private static final long NOW = 1_000_000L;

    private static ChummerHandler.Active active(String hologram, Vec3d pos) {
        return ChummerHandler.fromHologram(ChummerHologram.parse(hologram).orElseThrow(), pos, null, NOW);
    }

    @Test
    void theHologramGivesRarityAndTheTimeLeft() {
        ChummerHandler.Active a = active(DUMP, SPOT);
        assertEquals(Constant.COMMON, a.rarity);
        assertFalse(a.timerUnknown);
        assertEquals(NOW + 895_000L, a.expiresAtMs);
        assertEquals(15 * 60, a.durationSeconds);
        assertEquals(20f, a.radius);
        assertEquals(SPOT, a.pos);
    }

    @Test
    void everyRarityGlyphMapsToItsRarity() {
        Constant[] rarities = {Constant.COMMON, Constant.RARE, Constant.EPIC, Constant.LEGENDARY, Constant.MYTHICAL};
        for (int i = 0; i < rarities.length; i++) {
            String hologram = "Chummer™\n" + (char) (0xF033 + i) + "\nActivated by a\n\nBite Speed: +75\n15m Remaining";
            assertEquals(rarities[i], active(hologram, SPOT).rarity);
        }
        assertEquals(Constant.DEFAULT, active("Chummer™\n\nActivated by a\n15m Remaining", SPOT).rarity);
    }

    @Test
    void noTimeLineMeansTheTimerIsUnknown() {
        ChummerHandler.Active a = active("Chummer™\n\nActivated by a\n\nBite Speed: +75", SPOT);
        assertTrue(a.timerUnknown);
        assertEquals(Long.MAX_VALUE, a.expiresAtMs);
    }

    @Test
    void theHudShowsInsideTheRangeAndHidesOutside() {
        ChummerHandler handler = new ChummerHandler();
        ChummerHandler.Active a = active(DUMP, SPOT);
        handler.setActives(List.of(a));
        assertTrue(handler.isActive());
        assertSame(a, handler.activeInRange(new Vec3d(SPOT.x, 27, SPOT.z)));
        assertSame(a, handler.activeInRange(new Vec3d(SPOT.x + 20, 27, SPOT.z)));
        assertNull(handler.activeInRange(new Vec3d(SPOT.x + 21, 27, SPOT.z)));
        assertNull(handler.activeInRange(new Vec3d(SPOT.x, 60, SPOT.z))); // far above
        handler.clear();
        assertFalse(handler.isActive());
        assertNull(handler.activeInRange(new Vec3d(SPOT.x, 27, SPOT.z)));
    }

    @Test
    void theNearestChummerWinsWhereRangesOverlap() {
        ChummerHandler handler = new ChummerHandler();
        ChummerHandler.Active west = active(DUMP, SPOT);
        ChummerHandler.Active east = active(DUMP, SPOT.add(30, 0, 0));
        handler.setActives(List.of(west, east));
        assertSame(west, handler.activeInRange(SPOT.add(14, 1, 0)));
        assertSame(east, handler.activeInRange(SPOT.add(16, 1, 0)));
    }

    @Test
    void theRingStandsOnTheGroundRightOnTheRangeTheHudUses() {
        ChummerHandler handler = new ChummerHandler();
        ChummerHandler.Active a = active(DUMP, SPOT);
        handler.setActives(List.of(a));
        List<float[]> corners = new ArrayList<>();
        ChummerRingGeometry.emit((float) a.pos.x, (float) a.pos.y, (float) a.pos.z,
                (x, y, z) -> corners.add(new float[]{x, y, z}));
        for (float[] c : corners) {
            assertTrue(c[1] >= (float) SPOT.y - 1e-3 && c[1] <= (float) SPOT.y + 1.001f);
            assertSame(a, handler.activeInRange(new Vec3d(c[0], SPOT.y + 1, c[2])));
            Vec3d outward = new Vec3d(c[0] - SPOT.x, 0, c[2] - SPOT.z).normalize().multiply(1.0);
            assertNull(handler.activeInRange(new Vec3d(c[0] + outward.x, SPOT.y + 1, c[2] + outward.z)));
        }
    }
}
