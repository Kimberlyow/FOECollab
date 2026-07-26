package io.github.foecollab.handler;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Util;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves player UUIDs to usernames for display. Checks the tab list first, then
 * falls back to a Mojang profile lookup on a worker thread — the vanilla resolver
 * blocks on a network call on cache miss, so it must never run on the render thread.
 */
public class ProfileNameHandler {
    private static ProfileNameHandler INSTANCE = new ProfileNameHandler();

    /// Resolved names; "" marks a failed/unknown lookup.
    private final Map<UUID, String> cache = new ConcurrentHashMap<>();
    private final Set<UUID> fetching = ConcurrentHashMap.newKeySet();

    public static ProfileNameHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new ProfileNameHandler();
        }
        return INSTANCE;
    }

    /// Returns the username, "" when the lookup failed, or null while still loading.
    public String getUsername(UUID uuid) {
        String cached = cache.get(uuid);
        if (cached != null) {
            return cached;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() != null) {
            PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(uuid);
            if (entry != null) {
                String name = entry.getProfile().name();
                cache.put(uuid, name);
                return name;
            }
        }

        if (fetching.add(uuid)) {
            CompletableFuture.runAsync(() -> {
                try {
                    cache.put(uuid, client.getApiServices().profileResolver()
                            .getProfileById(uuid).map(GameProfile::name).orElse(""));
                } catch (Exception e) {
                    cache.put(uuid, "");
                } finally {
                    fetching.remove(uuid);
                }
            }, Util.getIoWorkerExecutor());
        }
        return null;
    }
}
