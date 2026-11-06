package io.github.foecollab.handler;

import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.screens.hud.ChummerRingGeometry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks placed chummers so the HUD can show the remaining time and the world renderer can
 * replace the particle circle with a solid ring. A placed chummer carries a hologram (a text
 * display: "Chummer™", the rarity tag glyph, "Activated by [name]", "Bite Speed: +75",
 * "14m 55s Remaining") above a floating chummer head; every few ticks the loaded text displays
 * are read with {@link ChummerHologram}. That gives the exact spot, owner, rarity and the time
 * left the server counts down, for every chummer the client tracks, including ones placed
 * before joining or by players out of render distance. The HUD shows the one whose range the
 * player is standing in.
 */
public class ChummerHandler {
    private static ChummerHandler INSTANCE = new ChummerHandler();

    /// Ticks between two scans of the loaded entities.
    private static final int SCAN_TICKS = 10;
    /// How far below the hologram the ground is looked for.
    private static final int GROUND_SEARCH = 6;
    /// Every chummer runs 15 minutes, whatever its rarity; the HUD bar drains over that.
    private static final float DURATION_SECONDS = 15 * 60;
    /// Slack added to the range radius so the HUD doesn't flicker right on the ring.
    private static final double IN_RANGE_MARGIN = 0.5;
    /// How far above/below a chummer the player still counts as inside its range.
    private static final double IN_RANGE_VERTICAL = 12;

    private List<Active> actives = List.of();
    private int countdown;

    public static ChummerHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new ChummerHandler();
        }
        return INSTANCE;
    }

    /// Reads every loaded hologram every {@link #SCAN_TICKS} ticks; a chummer that runs out
    /// loses its hologram and drops out on the next scan.
    public void tick(MinecraftClient client) {
        if (client.world == null) {
            this.clear();
            return;
        }
        if (--this.countdown > 0) {
            return;
        }
        this.countdown = SCAN_TICKS;
        long now = System.currentTimeMillis();
        List<Active> found = new ArrayList<>();
        for (Entity entity : client.world.getEntities()) {
            if (entity instanceof DisplayEntity.TextDisplayEntity text && !text.isRemoved()) {
                ChummerHologram.parse(text.getText().getString()).ifPresent(chummer -> found.add(fromHologram(chummer,
                        new Vec3d(text.getX(), groundY(client.world, text), text.getZ()), head(client.world, text), now)));
            }
        }
        this.actives = found;
    }

    /// One chummer as its hologram reads, standing at {@code pos} (the hologram's x and z, the
    /// ground under it).
    static Active fromHologram(ChummerHologram.Chummer chummer, Vec3d pos, ItemStack stack, long nowMs) {
        boolean timerUnknown = chummer.seconds() < 0;
        return new Active(pos, (float) ChummerRingGeometry.RADIUS,
                timerUnknown ? Long.MAX_VALUE : nowMs + chummer.seconds() * 1000L, DURATION_SECONDS, stack,
                chummer.rarityGlyph() == 0 ? Constant.DEFAULT : Constant.valueOfTag(Character.toString(chummer.rarityGlyph())),
                timerUnknown);
    }

    /// The top of the first solid block under the hologram, looking down a few blocks; the
    /// hologram itself bobs up and down.
    private static double groundY(World world, Entity text) {
        BlockPos.Mutable pos = new BlockPos.Mutable(text.getBlockX(), 0, text.getBlockZ());
        for (int dy = 0; dy <= GROUND_SEARCH; dy++) {
            pos.setY(text.getBlockY() - dy);
            VoxelShape shape = world.getBlockState(pos).getCollisionShape(world, pos);
            if (!shape.isEmpty()) {
                return pos.getY() + shape.getMax(Direction.Axis.Y) + ChummerRingGeometry.LIFT;
            }
        }
        return ChummerRingGeometry.fallbackY(text.getY());
    }

    /// The chummer head floating just under the hologram, for the HUD icon.
    private static ItemStack head(World world, Entity text) {
        for (Entity entity : world.getOtherEntities(text, text.getBoundingBox().expand(0.25, 1.5, 0.25))) {
            if (entity instanceof DisplayEntity.ItemDisplayEntity item
                    && Math.abs(item.getX() - text.getX()) < 0.01 && Math.abs(item.getZ() - text.getZ()) < 0.01) {
                return item.getItemStack().copy();
            }
        }
        return ItemStack.EMPTY;
    }

    /// Returns true when a spawned particle belongs to a tracked chummer's circle and the solid
    /// ring is replacing it (cancel it).
    public boolean observeParticle(double x, double y, double z) {
        if (this.actives.isEmpty()) {
            return false;
        }
        for (Active active : this.actives) {
            if (Math.abs(y - active.pos.y) > 4) {
                continue;
            }
            double dx = x - active.pos.x;
            double dz = z - active.pos.z;
            double horizontalSq = dx * dx + dz * dz;
            if (horizontalSq >= active.annulusMinSq && horizontalSq <= active.annulusMaxSq) {
                return FOEConfig.getConfig().chummerTracker.solidRangeBar;
            }
        }
        return false;
    }

    public void clear() {
        this.actives = List.of();
        this.countdown = 0; // scan at once when a world loads
    }

    /// Replaces the tracked chummers; the scan's own way in, open for tests.
    void setActives(List<Active> actives) {
        this.actives = List.copyOf(actives);
    }

    public boolean isActive() {
        return !this.actives.isEmpty();
    }

    public List<Active> actives() {
        return this.actives;
    }

    /// The chummer whose range circle contains {@code position} (nearest one when ranges
    /// overlap), or null when standing outside all of them — the HUD hides then.
    public Active activeInRange(Vec3d position) {
        Active best = null;
        double bestSq = Double.MAX_VALUE;
        for (Active active : this.actives) {
            double radius = active.radius + IN_RANGE_MARGIN;
            double dx = position.x - active.pos.x;
            double dz = position.z - active.pos.z;
            double horizontalSq = dx * dx + dz * dz;
            if (horizontalSq <= radius * radius && Math.abs(position.y - active.pos.y) <= IN_RANGE_VERTICAL
                    && horizontalSq < bestSq) {
                best = active;
                bestSq = horizontalSq;
            }
        }
        return best;
    }

    /// One tracked chummer. The timer is unknown when its hologram has no "Remaining" line.
    public static class Active {
        public final Vec3d pos;
        public final float radius;
        public final long expiresAtMs;
        public final float durationSeconds;
        public final ItemStack stack;
        public final Constant rarity;
        public final boolean timerUnknown;
        /// Squared bounds of the ring annulus (radius ± 2.5), so the per-particle check in
        /// {@link #observeParticle} needs no square root.
        final double annulusMinSq;
        final double annulusMaxSq;

        Active(Vec3d pos, float radius, long expiresAtMs, float durationSeconds, ItemStack stack,
               Constant rarity, boolean timerUnknown) {
            this.pos = pos;
            this.radius = radius;
            double inner = Math.max(0, radius - 2.5);
            double outer = radius + 2.5;
            this.annulusMinSq = inner * inner;
            this.annulusMaxSq = outer * outer;
            this.expiresAtMs = expiresAtMs;
            this.durationSeconds = durationSeconds;
            this.stack = stack;
            this.rarity = rarity;
            this.timerUnknown = timerUnknown;
        }

        public float remainingSeconds() {
            return Math.max(0, (this.expiresAtMs - System.currentTimeMillis()) / 1000f);
        }

        /// Remaining lifetime as 0..1, for the HUD progress bar.
        public float remainingFraction() {
            if (this.durationSeconds <= 0) {
                return 0;
            }
            return Math.clamp(remainingSeconds() / this.durationSeconds, 0f, 1f);
        }
    }
}
