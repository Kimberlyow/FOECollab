package io.github.foecollab.handler;

import io.github.foecollab.FOMC.Types.Defaults;
import io.github.foecollab.util.TextHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OtherPlayerHandler {
    private static OtherPlayerHandler INSTANCE = new OtherPlayerHandler();

    // Private-use glyph the server stamps onto FoE-relevant text-display nameplates.
    private static final String NAMEPLATE_MARKER = "";

    public PlayerListEntry highlightedPlayer = null;
    public long highlightStartTime = 0L;
    public boolean isHighlighted = false;

    private final List<Integer> hiddenNamePlates = new ArrayList<>();
    // A text display's Text reference only changes when the server actually updates the line,
    // so the last processed reference per entity id lets a tick skip the getString tree walk
    // (a crowd has hundreds of nameplates) and ensures a dev nameplate is re-tagged only once
    // per text, not json-roundtripped every tick.
    private final Map<Integer, Text> processedNameplates = new HashMap<>();
    private final Set<Integer> markedNameplates = new HashSet<>();

    public static OtherPlayerHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new OtherPlayerHandler();
        }
        return INSTANCE;
    }

    public void tick(MinecraftClient minecraftClient) {
        if(System.currentTimeMillis() - highlightStartTime <= 300000L && highlightedPlayer != null) {
            this.isHighlighted = true;
        } else if (this.isHighlighted && highlightedPlayer != null) {
            highlightedPlayer = null;
            this.isHighlighted = false;
        }
    }

    public void tickEntities(Entity entity, MinecraftClient minecraftClient) {
        // Restore previously dimmed nameplates once the HUD is shown again.
        if (!minecraftClient.options.hudHidden && !hiddenNamePlates.isEmpty() && minecraftClient.world != null) {
            hiddenNamePlates.forEach(id -> {
                Entity namePlate = minecraftClient.world.getEntityById(id);
                if (namePlate != null) {
                    ((DisplayEntity.TextDisplayEntity) namePlate).setTextOpacity((byte) -1);
                }
            });
            hiddenNamePlates.clear();
        }

        if (!(entity instanceof DisplayEntity.TextDisplayEntity textDisplayEntity)) {
            return;
        }

        int id = entity.getId();
        Text current = textDisplayEntity.getText();
        boolean unchanged = processedNameplates.get(id) == current;
        String nameplateText = unchanged ? null : current.getString();
        boolean marked = unchanged ? markedNameplates.contains(id) : nameplateText.contains(NAMEPLATE_MARKER);

        // Dim FoE nameplates while the HUD is hidden (e.g. for screenshots).
        if (minecraftClient.options.hudHidden && marked && !hiddenNamePlates.contains(id)) {
            textDisplayEntity.setTextOpacity((byte) 24);
            hiddenNamePlates.add(id);
        }

        if (unchanged) {
            return;
        }
        if (processedNameplates.size() > 4096) {
            // Entity ids of long-gone nameplates pile up over a session; a rare full clear
            // just makes the next tick re-resolve everything once.
            processedNameplates.clear();
            markedNameplates.clear();
        }
        processedNameplates.put(id, current);
        if (!marked) {
            markedNameplates.remove(id);
            return;
        }
        markedNameplates.add(id);

        // Re-tag FoE dev nameplates (once per text — setText stores the new reference below).
        Defaults.FoEDevType senderDev = Defaults.foeDevs.values().stream()
                .filter(foEDevType -> nameplateText.contains(foEDevType.name))
                .findFirst()
                .orElse(null);

        if (senderDev != null) {
            String jsonText = TextHelper.textToJson(current);
            jsonText = TextHelper.replaceToFoE(jsonText, senderDev.usePurpleTag);
            if (!senderDev.usePurpleTag) {
                jsonText = jsonText.replace("B05BF9", "00AF0E");
            }
            textDisplayEntity.setText(TextHelper.jsonToText(jsonText));
            processedNameplates.put(id, textDisplayEntity.getText());
        }
    }

    /// Forget all cached nameplate state (server join / world change).
    public void clear() {
        this.hiddenNamePlates.clear();
        this.processedNameplates.clear();
        this.markedNameplates.clear();
    }
}
