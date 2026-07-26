package io.github.foecollab.handler;

import io.github.foecollab.FOECollab;
import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.FOMC.Types.Chummer;
import io.github.foecollab.config.FOEConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tracks placed chummers so the HUD can show the remaining time and the world renderer can
 * replace the particle circle with a solid ring. A placement is detected from the server's own
 * chat broadcast ("CHUMMER » [name] activated a [rarity] Chummer. (+130 Bite Speed)"): the
 * named player is looked up in the world for the placement position (their look target,
 * falling back to their feet), and
 * when they still hold more chummers the synced stack's NBT supplies the exact timer.
 * Several chummers can be tracked at once; the HUD shows the one whose range the player is
 * standing in.
 */
public class ChummerHandler {
    private static ChummerHandler INSTANCE = new ChummerHandler();

    /// A new placement within this distance of a tracked one replaces it (re-chumming a spot).
    private static final double DUPLICATE_RANGE = 4.0;
    /// Cap on simultaneously tracked chummers.
    private static final int MAX_ACTIVE = 8;
    /// Slack added to the range radius so the HUD doesn't flicker right on the ring.
    private static final double IN_RANGE_MARGIN = 0.5;
    /// How far above/below a chummer the player still counts as inside its range.
    private static final double IN_RANGE_VERTICAL = 12;
    /// A timer-less chummer whose circle stops spawning particles this long is gone.
    private static final long NO_TIMER_TIMEOUT_MS = 10_000;

    /// The server's activation broadcast, e.g. "CHUMMER » Niteek activated a  Chummer.
    /// (+130 Bite Speed)". Whatever sits between "a" and "Chummer" (the rarity tag glyph) is
    /// captured and searched for a known rarity separately, so glyphs or extra formatting
    /// can't break the match. "has placed" is kept as an alternate verb just in case.
    private static final Pattern PLACED_PATTERN =
            Pattern.compile("(\\w{1,16}) (?:activated|has placed) a(.*?)chummer", Pattern.CASE_INSENSITIVE);
    private static final Constant[] RARITIES = {Constant.COMMON, Constant.RARE, Constant.EPIC,
            Constant.LEGENDARY, Constant.MYTHICAL, Constant.SPECIAL};

    private final List<Active> actives = new ArrayList<>();

    public static ChummerHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new ChummerHandler();
        }
        return INSTANCE;
    }

    /// Watches chat for the server's "... activated a ... Chummer" broadcast; never suppresses.
    public void onReceiveMessage(Text text) {
        Matcher matcher = PLACED_PATTERN.matcher(text.getString());
        if (!matcher.find()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return;
        }
        PlayerEntity placer = null;
        for (PlayerEntity player : client.world.getPlayers()) {
            if (player.getGameProfile().name().equalsIgnoreCase(matcher.group(1))) {
                placer = player;
                break;
            }
        }
        if (placer == null) {
            return; // not in render distance: no position to anchor the ring or HUD to
        }
        // When the placer still holds more chummers, the synced stack's NBT gives the timer
        // (the rest of a stack shares the NBT of the one just placed).
        ItemStack held = placer.getMainHandStack();
        Chummer chummer = Chummer.getChummer(held);
        if (chummer == null) {
            held = placer.getOffHandStack();
            chummer = Chummer.getChummer(held);
        }
        Constant rarity = parseRarity(matcher.group(2));
        if (rarity == Constant.DEFAULT && chummer != null) {
            rarity = chummer.rarity;
        }
        this.addActive(placementPos(placer), rarity, chummer, held);
    }

    private static Constant parseRarity(String betweenPlacedAndChummer) {
        String lower = betweenPlacedAndChummer.toLowerCase(Locale.ROOT);
        for (Constant rarity : RARITIES) {
            if (lower.contains(rarity.ID) || betweenPlacedAndChummer.contains(rarity.TAG.getString())) {
                return rarity;
            }
        }
        return Constant.DEFAULT;
    }

    /// Best guess for where the chummer landed: the placer's look target (including water,
    /// which chummers are typically thrown into), falling back to their feet.
    private static Vec3d placementPos(PlayerEntity player) {
        HitResult hit = player.raycast(6, 0, true);
        return hit.getType() != HitResult.Type.MISS ? hit.getPos() : player.getEntityPos();
    }

    public void tick(MinecraftClient client) {
        long now = System.currentTimeMillis();
        this.actives.removeIf(active -> now >= active.expiresAtMs
                || (active.timerUnknown && now - active.lastParticleMs > NO_TIMER_TIMEOUT_MS));
    }

    /**
     * Feeds one spawned particle through chummer tracking; returns true when the particle
     * belongs to a tracked chummer's circle and the solid ring is replacing it (cancel it).
     * The particles also keep timer-less chummers alive: once their circle stops spawning,
     * they expire.
     */
    public boolean observeParticle(double x, double y, double z) {
        if (this.actives.isEmpty()) {
            return false;
        }
        long now = System.currentTimeMillis();
        for (Active active : this.actives) {
            if (Math.abs(y - active.pos.y) > 4) {
                continue;
            }
            double dx = x - active.pos.x;
            double dz = z - active.pos.z;
            double horizontalSq = dx * dx + dz * dz;
            if (horizontalSq >= active.annulusMinSq && horizontalSq <= active.annulusMaxSq) {
                active.lastParticleMs = now;
                return FOEConfig.getConfig().chummerTracker.solidRangeBar;
            }
        }
        return false;
    }

    private void addActive(Vec3d pos, Constant rarity, Chummer chummer, ItemStack stack) {
        long now = System.currentTimeMillis();
        boolean timerUnknown = chummer == null;
        float durationSeconds = timerUnknown ? 0 : durationSeconds(chummer.timer);
        if (timerUnknown) {
            FOECollab.LOGGER.info("[FoE] Chummer placed (timer unknown: placer holds no more chummers)");
        } else {
            FOECollab.LOGGER.info("[FoE] Chummer placed (timer NBT: {}, using {}s)", chummer.timer, durationSeconds);
        }
        this.actives.removeIf(active -> active.pos.squaredDistanceTo(pos) < DUPLICATE_RANGE * DUPLICATE_RANGE);
        if (this.actives.size() >= MAX_ACTIVE) {
            this.actives.remove(0);
        }
        float radius = FOEConfig.getConfig().chummerTracker.rangeRadius;
        this.actives.add(new Active(pos, radius,
                timerUnknown ? Long.MAX_VALUE : now + (long) (durationSeconds * 1000L), durationSeconds,
                timerUnknown ? ItemStack.EMPTY : stack.copy(), rarity, timerUnknown, now));
    }

    /// The chummer's "timer" NBT unit isn't documented; small values are read as minutes and
    /// larger ones as seconds. The placement log line shows the raw value for verification.
    private static float durationSeconds(float timer) {
        if (timer <= 0) {
            return 0;
        }
        return timer <= 30 ? timer * 60 : timer;
    }

    public void clear() {
        this.actives.clear();
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

    /// One tracked chummer. The timer is unknown when the placer's hands held no further
    /// chummer to read NBT from; such a chummer lives as long as its particle circle does.
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
        /// Last time a particle of this chummer's circle appeared; timer-less liveness.
        long lastParticleMs;

        Active(Vec3d pos, float radius, long expiresAtMs, float durationSeconds, ItemStack stack,
               Constant rarity, boolean timerUnknown, long lastParticleMs) {
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
            this.lastParticleMs = lastParticleMs;
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
