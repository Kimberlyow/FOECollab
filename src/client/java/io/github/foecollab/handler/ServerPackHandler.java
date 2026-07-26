package io.github.foecollab.handler;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.foecollab.FOECollab;
import io.github.foecollab.config.FOEConfig;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Mirrors the server-pushed resource packs (vanilla caches them under downloads/&lt;uuid&gt;/&lt;sha1&gt;,
 * journaled in downloads/log.json) into the resourcepacks folder so they load once at startup as
 * ordinary local packs. GameOptionsMixin force-enables the mirrored packs, and
 * ClientCommonNetworkHandlerMixin answers the server's pack push with "successfully loaded" when it
 * matches a mirrored pack — so joining never re-downloads or reloads, and the packs can be
 * reordered/overridden in the pack screen like any other pack.
 */
public class ServerPackHandler {
    private static ServerPackHandler INSTANCE = new ServerPackHandler();

    public static final String FILE_PREFIX = "foecollab_";

    private final Set<String> localHashes = new HashSet<>();
    private final Set<String> localUrls = new HashSet<>();
    // pack-screen profile ids ("file/foecollab_FishOnMC-Pack-1.21.11.zip") in server push order
    private final List<String> profileIds = new ArrayList<>();

    public static ServerPackHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new ServerPackHandler();
        }
        return INSTANCE;
    }

    /**
     * Called from the client entrypoint, i.e. before MinecraftClient scans resource packs.
     */
    public void extract() {
        if (!FOEConfig.getConfig().loadServerPackLocally) {
            return;
        }
        try {
            Path gameDir = FabricLoader.getInstance().getGameDir();
            Path journal = gameDir.resolve("downloads").resolve("log.json");
            if (!Files.isRegularFile(journal)) {
                return; // nothing cached yet — first join uses the vanilla flow, next launch mirrors it
            }

            // keep only the newest journal entry per pack id, preserving first-seen (push) order
            Map<String, JsonObject> latest = new LinkedHashMap<>();
            for (String line : Files.readAllLines(journal)) {
                if (line.isBlank()) continue;
                try {
                    JsonObject entry = JsonParser.parseString(line).getAsJsonObject();
                    latest.put(entry.get("id").getAsString(), entry);
                } catch (Exception ignored) {
                    // partially written / corrupt journal line
                }
            }

            Path packsDir = gameDir.resolve("resourcepacks");
            Files.createDirectories(packsDir);
            Set<Path> keep = new HashSet<>();

            for (JsonObject entry : latest.values()) {
                String url = entry.get("url").getAsString();
                String relative = entry.getAsJsonObject("file").get("name").getAsString().replace('\\', '/');
                Path source = gameDir.resolve("downloads").resolve(relative);
                if (!Files.isRegularFile(source)) continue;

                String sha1 = relative.substring(relative.lastIndexOf('/') + 1).toLowerCase(Locale.ROOT);
                Path dest = packsDir.resolve(destFileName(url, sha1));
                if (!Files.isRegularFile(dest) || Files.size(dest) != Files.size(source)) {
                    Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
                    FOECollab.LOGGER.info("[FoE] Mirrored server pack {} -> {}", url, dest.getFileName());
                }
                keep.add(dest);
                localHashes.add(sha1);
                localUrls.add(url);
                profileIds.add("file/" + dest.getFileName());
            }

            // drop mirrors of packs the server no longer pushes (e.g. after a version bump)
            try (Stream<Path> files = Files.list(packsDir)) {
                files.filter(p -> p.getFileName().toString().startsWith(FILE_PREFIX) && !keep.contains(p))
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                                FOECollab.LOGGER.info("[FoE] Removed stale server pack mirror {}", p.getFileName());
                            } catch (Exception e) {
                                FOECollab.LOGGER.warn("[FoE] Could not delete stale server pack mirror {}", p, e);
                            }
                        });
            }
        } catch (Exception e) {
            FOECollab.LOGGER.warn("[FoE] Failed to mirror server resource packs", e);
            localHashes.clear();
            localUrls.clear();
            profileIds.clear();
        }
    }

    private static String destFileName(String url, String sha1) {
        String name = url.substring(url.lastIndexOf('/') + 1);
        int query = name.indexOf('?');
        if (query >= 0) {
            name = name.substring(0, query);
        }
        name = name.replaceAll("[^A-Za-z0-9._ -]", "_");
        if (name.length() <= ".zip".length()) {
            name = sha1 + ".zip";
        } else if (!name.toLowerCase(Locale.ROOT).endsWith(".zip")) {
            name += ".zip";
        }
        return FILE_PREFIX + name;
    }

    /**
     * True if the pushed pack is already loaded as one of our mirrored local packs.
     */
    public boolean isLocallyLoaded(String hash, String url) {
        if (!FOEConfig.getConfig().loadServerPackLocally || profileIds.isEmpty()) {
            return false;
        }
        if (hash != null && !hash.isBlank()) {
            return localHashes.contains(hash.toLowerCase(Locale.ROOT));
        }
        return localUrls.contains(url);
    }

    public List<String> getProfileIds() {
        return FOEConfig.getConfig().loadServerPackLocally ? profileIds : List.of();
    }
}
