package io.github.foecollab.handler;

/// Tracks fish-bite state for the custom bite title (see
/// {@link io.github.foecollab.screens.hud.BiteTitleHud}). When the server's "BITE!" title fires it
/// stamps the bite time so the HUD can show the custom text for a configurable window and then hide
/// it on its own — reeling in no longer clears it. The boolean flag is only a one-shot guard so
/// the alert sound plays once per bite even though the server re-sends "BITE!" mid-bite; it is
/// re-armed when the bobber leaves and when the previous "BITE!" title expires (a later fish biting
/// the same bobber, see InGameHudMixin), so every bite of a cast pops the notification again.
public class BiteTitleHandler {
    private static BiteTitleHandler INSTANCE = new BiteTitleHandler();

    private boolean bite = false;
    private long biteTime = 0L;

    public static BiteTitleHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new BiteTitleHandler();
        }
        return INSTANCE;
    }

    /// A fish bit: stamp the bite time so the timed display (re)starts. Returns {@code true} only on
    /// the transition into the bite state, so callers can fire a one-shot bite sound without it
    /// repeating while the server re-sends the "BITE!" title.
    public boolean trigger() {
        boolean wasNewBite = !this.bite;
        this.bite = true;
        if (wasNewBite) {
            this.biteTime = System.currentTimeMillis();
        }
        return wasNewBite;
    }

    /// Epoch millis of the most recent bite (0 if none yet), used to time the bite-text display.
    public long biteTime() {
        return this.biteTime;
    }

    /// Re-arms the one-shot bite guard (and sound) for the next cast. Leaves the bite time alone so
    /// a title already on screen stays for the rest of its timed window.
    public void reset() {
        this.bite = false;
    }
}
