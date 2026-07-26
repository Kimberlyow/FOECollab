package io.github.foecollab.util;

import java.util.function.Supplier;

/// Caches the result of an expensive {@link Supplier} and only recomputes it once
/// {@code ttlMs} has elapsed since the last computation.
///
/// The per-frame HUD renderers rebuild their whole {@code List<Text>} every single frame,
/// even though the data behind them only changes a few times per second (on fish catches,
/// ticks, config edits). At high framerates that is a lot of wasted allocation and map
/// lookups. Wrapping the build in a {@code ThrottledCache} caps the rebuild rate regardless
/// of framerate; the worst-case staleness is {@code ttlMs}, which is imperceptible for a
/// stats HUD.
///
/// Not thread-safe — intended to be called only from the client render/tick thread.
public class ThrottledCache<T> {
    private final long ttlMs;
    private final Supplier<T> supplier;
    private T value;
    private long lastUpdateMs = Long.MIN_VALUE;

    public ThrottledCache(long ttlMs, Supplier<T> supplier) {
        this.ttlMs = ttlMs;
        this.supplier = supplier;
    }

    public T get() {
        long now = System.currentTimeMillis();
        if (value == null || now - lastUpdateMs >= ttlMs) {
            value = supplier.get();
            lastUpdateMs = now;
        }
        return value;
    }
}
