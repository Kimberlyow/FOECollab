package io.github.foecollab.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.github.foecollab.FOECollab;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

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
 * Stores the user's customizable inventory buttons (ported from FishOnMC-Extras-R's custom button
 * system, with the creator's permission). Each button is a name + hover description, a command to
 * run, an icon (a short glyph or a {@code namespace:id} item) and a show/hide flag. The list is a
 * client preference, so it is kept in a single global Gson file under {@code config/foe/} rather
 * than per-account. Defaults mirror FOE-R's own default buttons and icons.
 */
public class InventoryButtonHandler {
    private static InventoryButtonHandler INSTANCE = new InventoryButtonHandler();

    /**
     * Bumped for the export format AND to re-seed the saved defaults: on load, a file written by an
     * older version has its buttons reset to the current defaults (so default changes actually take
     * effect for existing users instead of being masked by their on-disk copy).
     */
    public static final int BUTTON_VERSION = 2;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private ButtonData data;

    public static InventoryButtonHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new InventoryButtonHandler();
        }
        return INSTANCE;
    }

    private Path filePath() {
        return FabricLoader.getInstance().getConfigDir().resolve("foe").resolve("inventory_buttons.json");
    }

    private void ensureLoaded() {
        if (data == null) {
            load();
        }
    }

    public List<CustomButton> getButtons() {
        ensureLoaded();
        return data.buttons;
    }

    /** Buttons that should actually be drawn in the inventory, in order. */
    public List<CustomButton> getShownButtons() {
        List<CustomButton> shown = new ArrayList<>();
        for (CustomButton button : getButtons()) {
            if (button.showButton) {
                shown.add(button);
            }
        }
        return shown;
    }

    public void addButton(CustomButton button) {
        getButtons().add(button);
        save();
    }

    public void addButton(CustomButton button, int index) {
        List<CustomButton> buttons = getButtons();
        index = Math.max(0, Math.min(index, buttons.size()));
        buttons.add(index, button);
        save();
    }

    public void deleteButton(int index) {
        List<CustomButton> buttons = getButtons();
        if (index >= 0 && index < buttons.size()) {
            buttons.remove(index);
            save();
        }
    }

    public void moveUp(int index) {
        List<CustomButton> buttons = getButtons();
        if (index > 0 && index < buttons.size()) {
            Collections.swap(buttons, index, index - 1);
            save();
        }
    }

    public void moveDown(int index) {
        List<CustomButton> buttons = getButtons();
        if (index >= 0 && index < buttons.size() - 1) {
            Collections.swap(buttons, index, index + 1);
            save();
        }
    }

    public void resetToDefaults() {
        ensureLoaded();
        data.buttons = defaultButtons();
        save();
    }

    public void load() {
        try {
            Path filePath = filePath();
            Files.createDirectories(filePath.getParent());
            if (!Files.exists(filePath)) {
                data = new ButtonData();
                save();
                return;
            }
            String json = Files.readString(filePath, UTF_8);
            if (json == null || json.isBlank()) {
                data = new ButtonData();
                save();
                return;
            }
            try {
                data = gson.fromJson(json, ButtonData.class);
            } catch (JsonSyntaxException ex) {
                FOECollab.LOGGER.error("[FoE] Failed to parse inventory buttons ({}): {}", filePath, ex.getMessage());
                data = new ButtonData();
                save();
            }
            if (data == null) {
                data = new ButtonData();
            }
            if (data.buttons == null) {
                data.buttons = defaultButtons();
            }
            // Re-seed defaults when an older file is loaded so default/icon changes take effect.
            if (data.version < BUTTON_VERSION) {
                data.buttons = defaultButtons();
                data.version = BUTTON_VERSION;
                save();
            }
        } catch (IOException e) {
            FOECollab.LOGGER.error("[FoE] Failed to load inventory buttons: {}", e.getMessage());
            data = new ButtonData();
        }
    }

    public void save() {
        ensureLoaded();
        try {
            Path filePath = filePath();
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, gson.toJson(data));
        } catch (IOException e) {
            FOECollab.LOGGER.error("[FoE] Failed to save inventory buttons: {}", e.getMessage());
        }
    }

    // region Import / Export
    /** Encodes a single button to a Base64 (deflated JSON) string for sharing. */
    public String exportButton(CustomButton button) {
        ExportedButton exported = new ExportedButton(button, BUTTON_VERSION);
        return Base64.getEncoder().encodeToString(compress(gson.toJson(exported)));
    }

    /**
     * Decodes a button from a previously {@link #exportButton exported} string. Tolerates the
     * decorated form copied to clipboard (a name line and ``` fences) by pulling the Base64 blob
     * out of it. Returns {@code null} if the data is invalid or from a newer version.
     */
    public CustomButton importButton(String raw) {
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

            // FoE-R exports a Pair<CustomButton, version> which serialises to
            // {"value1":{button},"value2":version}. Its CustomButton has the same fields as ours, so
            // translate that shape directly (its GZIP compression is handled in decompress) — this is
            // what lets buttons shared from FoE-R import cleanly.
            if (root.has("value1") && !root.has("button")) {
                if (root.has("value2") && !root.get("value2").isJsonNull()
                        && root.get("value2").getAsInt() > BUTTON_VERSION) {
                    return null;
                }
                return gson.fromJson(root.get("value1"), CustomButton.class);
            }

            ExportedButton exported = gson.fromJson(root, ExportedButton.class);
            if (exported == null || exported.button == null || exported.version > BUTTON_VERSION) {
                return null;
            }
            return exported.button;
        } catch (Exception e) {
            FOECollab.LOGGER.error("[FoE] Failed to import button: {}", e.getMessage());
            return null;
        }
    }

    private String extractBase64(String raw) {
        String text = raw.trim();
        // Pull the contents of the first ```...``` fenced block if present (the shared/decorated form).
        int first = text.indexOf("```");
        if (first != -1) {
            int second = text.indexOf("```", first + 3);
            if (second != -1) {
                text = text.substring(first + 3, second);
            }
        }
        // Keep only the longest line that looks like Base64 (drops any prefix/suffix lines).
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

    /**
     * Resolves an icon string to an item stack when it looks like a {@code namespace:id} (any
     * trailing {@code [components]} is ignored). Returns {@code null} for a plain glyph/letter icon,
     * which the button renders as text instead.
     */
    public static ItemStack iconItem(String icon) {
        if (icon == null || !icon.contains(":")) {
            return null;
        }
        String id = icon.contains("[") ? icon.substring(0, icon.indexOf("[")) : icon;
        Identifier identifier = Identifier.tryParse(id.trim());
        if (identifier == null) {
            return null;
        }
        Item item = Registries.ITEM.get(identifier);
        if (item == Items.AIR) {
            return null;
        }
        return item.getDefaultStack();
    }

    private static List<CustomButton> defaultButtons() {
        // Mirrors FOE-R's default inventory buttons (names, commands and icons) so the modern look
        // matches Danny's out of the box. Names/descriptions use legacy '&' colour codes.
        return new ArrayList<>(List.of(
                new CustomButton("&6&lInstances", "&7Change Instances", "/instances", "Ⓘ", true),
                new CustomButton("&6&lTurbo Travel", "&7Change Location", "/tt", "⚡", true),
                new CustomButton("&6&lCrew Home", "&7Go to your crew home", "/crew home", "⚓", true),
                new CustomButton("&6&lSpawn", "&7Go to spawn", "/spawn", "Ⓢ", true),
                new CustomButton("&6&lQuests", "&7Open your quests", "/quests", "⚐", true),
                new CustomButton("&6&lPersonal Vault", "&7Open your personal vault", "/pv", "⛨", true),
                new CustomButton("&6&lAuction House", "&7Open the auction house", "/ah", "⭐", true),
                new CustomButton("&6&lCraft", "&7Open the crafting station", "/craft", "⛏", true),
                new CustomButton("&6&lArtisan", "&7Open the Artisan Menu", "/artisan", "A", true),
                new CustomButton("&6&lIdentifier", "&7Open the Identifier Menu", "/identifier", "I", true),
                new CustomButton("&6&lForge", "&7Open the Forge Menu", "/forge", "F", true),
                new CustomButton("&6&lScrapper", "&7Open the Scrapper Menu", "/scrapper", "S", true),
                new CustomButton("&6&lCalibrator", "&7Open the Calibrator Menu", "/calibrator", "C", true),
                new CustomButton("Spacer #1", "Used to show an empty spot", "/", "minecraft:barrier", false),
                new CustomButton("Spacer #2", "Used to show an empty spot", "/", "minecraft:barrier", false),
                new CustomButton("&6&lSell", "&7Open the sell menu", "/sell", "$", true)
        ));
    }

    public static class ButtonData {
        public int version = BUTTON_VERSION;
        public List<CustomButton> buttons = defaultButtons();
    }

    public static class CustomButton {
        public String name;
        public String description;
        public String action;
        public String icon;
        public boolean showButton;

        public CustomButton() {
            this("New Button", "", "/", "E", true);
        }

        public CustomButton(String name, String description, String action, String icon, boolean showButton) {
            this.name = name;
            this.description = description;
            this.action = action;
            this.icon = icon;
            this.showButton = showButton;
        }

        public CustomButton copy() {
            return new CustomButton(name, description, action, icon, showButton);
        }
    }

    private static class ExportedButton {
        CustomButton button;
        int version;

        ExportedButton(CustomButton button, int version) {
            this.button = button;
            this.version = version;
        }
    }
}
