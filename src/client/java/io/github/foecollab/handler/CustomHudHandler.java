package io.github.foecollab.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.github.foecollab.FOECollab;
import io.github.foecollab.config.HudAlignment;
import net.fabricmc.loader.api.FabricLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Stores the user's custom HUDs (ported from FishOnMC-Extras-R's custom HUD system, with the
 * creator's permission). Each HUD is a list of "custom code" lines, a position/alignment, a font
 * scale, a background opacity and an enabled flag. Like the inventory buttons this is a client
 * preference kept in a single global Gson file under {@code config/foe/}. Lines are resolved by
 * {@link io.github.foecollab.customhud.PlaceholderEngine} and rendered in FoE's HUD style.
 */
public class CustomHudHandler {
    private static CustomHudHandler INSTANCE = new CustomHudHandler();

    public static final int HUD_VERSION = 1;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private HudData data;

    public static CustomHudHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new CustomHudHandler();
        }
        return INSTANCE;
    }

    private Path filePath() {
        return FabricLoader.getInstance().getConfigDir().resolve("foe").resolve("custom_huds.json");
    }

    private void ensureLoaded() {
        if (data == null) {
            load();
        }
    }

    public List<CustomHud> getHuds() {
        ensureLoaded();
        return data.huds;
    }

    public void addHud(CustomHud hud) {
        getHuds().add(hud);
        save();
    }

    public void deleteHud(int index) {
        List<CustomHud> huds = getHuds();
        if (index >= 0 && index < huds.size()) {
            huds.remove(index);
            save();
        }
    }

    public void moveUp(int index) {
        List<CustomHud> huds = getHuds();
        if (index > 0 && index < huds.size()) {
            Collections.swap(huds, index, index - 1);
            save();
        }
    }

    public void moveDown(int index) {
        List<CustomHud> huds = getHuds();
        if (index >= 0 && index < huds.size() - 1) {
            Collections.swap(huds, index, index + 1);
            save();
        }
    }

    public void resetToDefaults() {
        ensureLoaded();
        data.huds = defaultHuds();
        save();
    }

    public void load() {
        try {
            Path filePath = filePath();
            Files.createDirectories(filePath.getParent());
            if (!Files.exists(filePath)) {
                data = new HudData();
                save();
                return;
            }
            String json = Files.readString(filePath, UTF_8);
            if (json == null || json.isBlank()) {
                data = new HudData();
                save();
                return;
            }
            try {
                data = gson.fromJson(json, HudData.class);
            } catch (JsonSyntaxException ex) {
                FOECollab.LOGGER.error("[FoE] Failed to parse custom HUDs ({}): {}", filePath, ex.getMessage());
                data = new HudData();
                save();
            }
            if (data == null) {
                data = new HudData();
            }
            if (data.huds == null) {
                data.huds = defaultHuds();
            }
        } catch (IOException e) {
            FOECollab.LOGGER.error("[FoE] Failed to load custom HUDs: {}", e.getMessage());
            data = new HudData();
        }
    }

    public void save() {
        ensureLoaded();
        try {
            Path filePath = filePath();
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, gson.toJson(data));
        } catch (IOException e) {
            FOECollab.LOGGER.error("[FoE] Failed to save custom HUDs: {}", e.getMessage());
        }
    }

    // region Import / Export
    public String exportHud(CustomHud hud) {
        ExportedHud exported = new ExportedHud(hud, HUD_VERSION);
        return Base64.getEncoder().encodeToString(compress(gson.toJson(exported)));
    }

    public CustomHud importHud(String raw) {
        if (raw == null) {
            return null;
        }
        String base64 = extractBase64(raw);
        if (base64.isEmpty()) {
            return null;
        }
        try {
            String json = decompress(Base64.getDecoder().decode(base64));
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            // FoE-R exports a Triplet<name, hud, version> which serialises to
            // {"value1":name,"value2":{hud},"value3":version}. Translate that shape into ours so
            // HUDs shared from FoE-R import cleanly (its codes already match our PlaceholderEngine).
            if (root.has("value2") && !root.has("hud")) {
                return fromFoeR(root);
            }

            ExportedHud exported = gson.fromJson(root, ExportedHud.class);
            if (exported == null || exported.hud == null || exported.version > HUD_VERSION) {
                return null;
            }
            return exported.hud;
        } catch (Exception e) {
            FOECollab.LOGGER.error("[FoE] Failed to import custom HUD: {}", e.getMessage());
            return null;
        }
    }

    /// Converts a FoE-R custom-HUD export into our {@link CustomHud}. FoE-R's HUD has a different
    /// shape: {@code stringLines} of (text, isCentre, isSmall) triplets, a float {@code scale},
    /// separate {@code showBackground}/{@code showElement} flags and a 9-way alignment. We keep the
    /// line text, position and scale and map the rest onto our model; the per-line centre/small
    /// flags have no equivalent here and are dropped.
    private CustomHud fromFoeR(JsonObject root) {
        JsonObject src = root.getAsJsonObject("value2");
        if (src == null) {
            return null;
        }
        CustomHud hud = new CustomHud();
        if (root.has("value1") && !root.get("value1").isJsonNull()) {
            hud.name = root.get("value1").getAsString();
        }

        List<String> lines = new ArrayList<>();
        if (src.has("stringLines") && src.get("stringLines").isJsonArray()) {
            for (JsonElement element : src.getAsJsonArray("stringLines")) {
                if (element.isJsonObject()) {
                    JsonObject line = element.getAsJsonObject();
                    if (line.has("value1") && !line.get("value1").isJsonNull()) {
                        lines.add(line.get("value1").getAsString());
                    }
                }
            }
        }
        if (!lines.isEmpty()) {
            hud.lines = lines;
        }

        if (src.has("alignment") && !src.get("alignment").isJsonNull()) {
            hud.alignment = mapFoeRAlignment(src.get("alignment").getAsString());
        }
        if (src.has("xPos")) {
            hud.hudX = src.get("xPos").getAsInt();
        }
        if (src.has("yPos")) {
            hud.hudY = src.get("yPos").getAsInt();
        }
        if (src.has("scale")) {
            // Our renderer derives scale from fontSize/10, so invert FoE-R's float scale.
            hud.fontSize = Math.max(1, Math.round(src.get("scale").getAsFloat() * 10f));
        }
        if (src.has("showBackground")) {
            hud.backgroundOpacity = src.get("showBackground").getAsBoolean() ? 50 : 0;
        }
        if (src.has("showElement")) {
            hud.enabled = src.get("showElement").getAsBoolean();
        }
        return hud;
    }

    /// Maps FoE-R's 9-way {@code Alignment} onto our horizontal-only {@link HudAlignment}.
    private static HudAlignment mapFoeRAlignment(String name) {
        if (name == null) {
            return HudAlignment.LEFT;
        }
        return switch (name) {
            case "RIGHT", "TOP_RIGHT", "BOTTOM_RIGHT" -> HudAlignment.RIGHT;
            case "TOP", "BOTTOM", "CENTER" -> HudAlignment.CENTER;
            default -> HudAlignment.LEFT;
        };
    }

    private String extractBase64(String raw) {
        String text = raw.trim();
        int first = text.indexOf("```");
        if (first != -1) {
            int second = text.indexOf("```", first + 3);
            if (second != -1) {
                text = text.substring(first + 3, second);
            }
        }
        String best = "";
        for (String line : text.split("\\r?\\n")) {
            String trimmed = line.trim();
            if (trimmed.matches("[A-Za-z0-9+/=]+") && trimmed.length() > best.length()) {
                best = trimmed;
            }
        }
        return best;
    }

    private static byte[] compress(String value) {
        Deflater deflater = new Deflater();
        deflater.setInput(value.getBytes(UTF_8));
        deflater.finish();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            out.write(buffer, 0, count);
        }
        deflater.end();
        return out.toByteArray();
    }

    private static String decompress(byte[] data) throws Exception {
        // FoE-R compresses with GZIP (magic 0x1F 0x8B); our own exports use raw zlib/Deflate.
        if (data.length >= 2 && (data[0] & 0xFF) == 0x1F && (data[1] & 0xFF) == 0x8B) {
            try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(data))) {
                return new String(gis.readAllBytes(), UTF_8);
            }
        }
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            if (count == 0 && inflater.needsInput()) {
                break;
            }
            out.write(buffer, 0, count);
        }
        inflater.end();
        return out.toString(UTF_8);
    }
    // endregion

    private static List<CustomHud> defaultHuds() {
        // One example HUD so the feature is discoverable; uses placeholders that resolve in FoE.
        // The user can edit or delete it. '&' are legacy colour codes; '%...%' are custom codes.
        CustomHud example = new CustomHud();
        example.name = "Example";
        example.alignment = HudAlignment.LEFT;
        example.hudX = 2;
        example.hudY = 38;
        example.lines = new ArrayList<>(List.of(
                "&6&lExample HUD",
                "&7Location: &f%boss_bar.location%",
                "&7Coords: &f%player.x%&7, &f%player.y%&7, &f%player.z%",
                "&7Fish caught: &f%stats.fish_caught%"));
        return new ArrayList<>(List.of(example));
    }

    public static class HudData {
        public int version = HUD_VERSION;
        public List<CustomHud> huds = defaultHuds();
    }

    public static class CustomHud {
        public String name = "New HUD";
        public List<String> lines = new ArrayList<>(List.of("Example text"));
        public HudAlignment alignment = HudAlignment.LEFT;
        public int hudX = 30;
        public int hudY = 30;
        public int fontSize = 10;
        public int backgroundOpacity = 50;
        public boolean enabled = true;

        public CustomHud copy() {
            CustomHud copy = new CustomHud();
            copy.name = name;
            copy.lines = new ArrayList<>(lines);
            copy.alignment = alignment;
            copy.hudX = hudX;
            copy.hudY = hudY;
            copy.fontSize = fontSize;
            copy.backgroundOpacity = backgroundOpacity;
            copy.enabled = enabled;
            return copy;
        }
    }

    private static class ExportedHud {
        CustomHud hud;
        int version;

        ExportedHud(CustomHud hud, int version) {
            this.hud = hud;
            this.version = version;
        }
    }
}
