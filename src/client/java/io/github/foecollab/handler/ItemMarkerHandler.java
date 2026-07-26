package io.github.foecollab.handler;

import io.github.foecollab.FOECollab;
import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.FOMC.Types.Armor;
import io.github.foecollab.FOMC.Types.Defaults;
import io.github.foecollab.FOMC.Types.FOMCItem;
import io.github.foecollab.FOMC.Types.Fish;
import io.github.foecollab.FOMC.Types.Pet;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.util.ItemStackHelper;
import io.github.foecollab.util.TextHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.Optional;

public class ItemMarkerHandler {
    private static ItemMarkerHandler INSTANCE = new ItemMarkerHandler();
    private final FOEConfig config = FOEConfig.getConfig();

    private final Identifier rarityMarker = Identifier.of(FOECollab.MOD_ID, "icons/rarity");
    private final Identifier petItemMarker = Identifier.of(FOECollab.MOD_ID, "icons/pet_item");
    private final Identifier fishSizeMarker = Identifier.of(FOECollab.MOD_ID, "icons/fish_size");
    private final Identifier selectedSlotMarker = Identifier.of(FOECollab.MOD_ID, "icons/selected_slot");
    // for showing icons on pets with max luck/scale/perfect stats
    private final Identifier maxLuckMarker = Identifier.of(FOECollab.MOD_ID, "icons/max_luck");
    private final Identifier maxScaleMarker = Identifier.of(FOECollab.MOD_ID, "icons/max_scale");
    private final Identifier maxPerfectMarker = Identifier.of(FOECollab.MOD_ID, "icons/max_perfect");

    public static ItemMarkerHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new ItemMarkerHandler();
        }
        return INSTANCE;
    }

    public void renderItemMarker(DrawContext drawContext, Slot slot) {
        if (config.itemMarker.itemSlotMarker.showItemMarker && !slot.getStack().isEmpty()) {
            renderItemMarker(drawContext, slot.getStack(), slot.x, slot.y);
        }
    }

    public void renderItemMarker(DrawContext drawContext, ItemStack itemStack, int x, int y) {
        // Show Rarity Marker
        Constant rarity = FOMCItem.getRarity(itemStack);
        if (FOMCItem.isFish(itemStack)
                && rarity != Constant.DEFAULT) {
            if (config.itemMarker.itemSlotMarker.showFishRarityMarker
                    || config.itemMarker.itemSlotMarker.showFishSizeMarker != FishSizeMarkerToggle.OFF) {
                Constant size = Fish.getSize(itemStack);

                int alpha = ((int) 255f << 24);
                drawContext.getMatrices().pushMatrix();
                try {
                    drawContext.getMatrices().translate(0, 0);
                    if (config.itemMarker.itemSlotMarker.showFishRarityMarker) {
                        drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   rarityMarker, x, y, 16, 16,
                                alpha | rarity.COLOR);
                    }

                    if (config.itemMarker.itemSlotMarker.showFishSizeMarker == FishSizeMarkerToggle.CHARACTER) {
                        Text sizeChar = Text.literal(size.TAG.getString().substring(0, 1)).withColor(size.COLOR);
                        drawContext.drawText(MinecraftClient.getInstance().textRenderer, sizeChar,
                                x + 16 - MinecraftClient.getInstance().textRenderer.getWidth(sizeChar),
                                y + 16 - MinecraftClient.getInstance().textRenderer.fontHeight + 1, 0xFFFFFFFF, true);
                    } else if (config.itemMarker.itemSlotMarker.showFishSizeMarker == FishSizeMarkerToggle.MARKER) {
                        drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   fishSizeMarker, x, y, 16, 16,
                                alpha | size.COLOR);
                    }
                } finally {
                    drawContext.getMatrices().popMatrix();
                }
            }
        } else if (config.itemMarker.itemSlotMarker.showOtherRarityMarker
                && rarity != Constant.DEFAULT) {
            int alpha = ((int) 255f << 24);
            drawContext.getMatrices().pushMatrix();
            try {
                drawContext.getMatrices().translate(0, 0);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   rarityMarker, x, y, 16, 16,
                        alpha | rarity.COLOR);
            } finally {
                drawContext.getMatrices().popMatrix();
            }
        }

        if (config.itemMarker.itemSlotMarker.showPetItemEquippedMarker) {
            // Show Pet Item Marker — only for an actual pet item ("item" NBT), not for a
            // cosmetic skin or trail.
            boolean[] pet = FOMCItem.isPet(itemStack);
            if (pet[0] && pet[2]) {
                int alpha = ((int) 255f << 24);
                drawContext.getMatrices().pushMatrix();
                try {
                    drawContext.getMatrices().translate(0, 0);
                    drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   petItemMarker, x, y, 16, 16,
                            alpha | 0xFFFFFF);
                } finally {
                    drawContext.getMatrices().popMatrix();
                }
            }
        }

        // Pet markers: resolve the pet once and reuse it for both the percent marker
        // and the max-stat icons below (each block used to re-parse it every frame).
        Pet pet = (config.itemMarker.itemSlotMarker.showPetPercentMarker != PetPercentMarkerToggle.OFF
                || config.itemMarker.itemSlotMarker.showMaxPetStatsMarker)
                ? Pet.getPet(itemStack) : null;

        if (config.itemMarker.itemSlotMarker.showPetPercentMarker != PetPercentMarkerToggle.OFF) {
            if (pet != null) {
                Constant constant = Pet.getConstantFromPercent(pet.percentPetRating);

                int alpha = ((int) 255f << 24);
                drawContext.getMatrices().pushMatrix();
                try {
                    drawContext.getMatrices().translate(0, 0);

                    if (config.itemMarker.itemSlotMarker.showPetPercentMarker == PetPercentMarkerToggle.CHARACTER) {
                        Text constChar = Text.literal(constant.TAG.getString().substring(0, 1))
                                .withColor(constant.COLOR);
                        drawContext.drawText(MinecraftClient.getInstance().textRenderer, constChar,
                                x + 16 - MinecraftClient.getInstance().textRenderer.getWidth(constChar),
                                y + 16 - MinecraftClient.getInstance().textRenderer.fontHeight + 1, 0xFFFFFFFF, true);
                    } else if (config.itemMarker.itemSlotMarker.showPetPercentMarker == PetPercentMarkerToggle.MARKER) {
                        drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   fishSizeMarker, x, y, 16, 16,
                                alpha | constant.COLOR);
                    }
                } finally {
                    drawContext.getMatrices().popMatrix();
                }
            }
        }

        // show icons on pets when they have max luck/scale/perfect stats
        if (config.itemMarker.itemSlotMarker.showMaxPetStatsMarker) {
            if (pet != null) {
                drawContext.getMatrices().pushMatrix();
                try {
                    drawContext.getMatrices().translate(0, 0);
                    
                    int iconSize = 7;
                    int iconY = y + 16 - iconSize; // bottom left corner
                    int iconX = x;
                    
                    // perfect shows when both luck AND scale are maxed
                    if (pet.isMaxLuck() && pet.isMaxScale()) {
                        drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   maxPerfectMarker, iconX, iconY, iconSize, iconSize);
                    }
                    else if (pet.isMaxLuck()) {
                        drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   maxLuckMarker, iconX, iconY, iconSize, iconSize);
                    }
                    else if (pet.isMaxScale()) {
                        drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   maxScaleMarker, iconX, iconY, iconSize, iconSize);
                    }
                } finally {
                    drawContext.getMatrices().popMatrix();
                }
            }
        }

        // Show Armor Quality percentage, drawn bottom-right in the same compact
        // small-caps style as the stack count (armor never stacks, so that corner
        // is free). Quality lives in the item's NBT; reading it directly avoids
        // building a full Armor object every frame for every visible armor slot.
        if (config.itemMarker.itemSlotMarker.showArmorQualityMarker
                && itemStack.contains(DataComponentTypes.CUSTOM_DATA)) {
            NbtCompound nbt = ItemStackHelper.getNbtView(itemStack); // read-only; no deep copy
            if (nbt != null
                    && Objects.equals(nbt.getString("type").orElse(""), Defaults.ItemTypes.ARMOR)
                    && nbt.getBoolean("identified").orElse(false)) {
                int quality = nbt.getInt("quality").orElse(0);
                if (quality > 0) {
                    TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
                    String display = TextHelper.smallCaps(String.valueOf(quality));
                    int width = textRenderer.getWidth(display);
                    // Match FOER: color the % by quality tier (ʙʀᴏᴋᴇɴ … sᴜᴘᴇʀɪᴏʀ). The server
                    // already colors the quality text in the lore, so reuse that exact color
                    // instead of re-deriving the tier; fall back to white if not found.
                    int qualityColor = findArmorQualityColor(itemStack, quality);
                    int textColor = qualityColor != -1 ? (0xFF000000 | qualityColor) : 0xFFFFFFFF;
                    drawContext.getMatrices().pushMatrix();
                    try {
                        drawContext.getMatrices().translate(0, 0);
                        drawContext.drawText(textRenderer, display,
                                x + 17 - width, y + 8, textColor, true);
                    } finally {
                        drawContext.getMatrices().popMatrix();
                    }
                }
            }
        }

    }

    /**
     * Markers drawn BEFORE the slot's item (at drawSlot HEAD) so they sit behind it:
     * the equipped-pet highlight, the search-match highlight, and the bait sorting
     * helper. These used z=100 (below the item) before the 1.21.11 matrix lost its z
     * axis; drawing them ahead of the item restores that layering so they no longer
     * cover the item.
     */
    public void renderItemMarkerBackground(DrawContext drawContext, Slot slot) {
        if (config.itemMarker.itemSlotMarker.showItemMarker && !slot.getStack().isEmpty()) {
            renderItemMarkerBackground(drawContext, slot.getStack(), slot.x, slot.y);
        }
    }

    public void renderItemMarkerBackground(DrawContext drawContext, ItemStack itemStack, int x, int y) {
        // this config option was defined but never actually used
        if (config.itemMarker.showSelectedPetHighlight
                && MinecraftClient.getInstance().player != null
                && ProfileDataHandler.instance().profileData.equippedPet != null
                && ProfileDataHandler.instance().profileData.equippedPetSlot >= 0
                && itemStack.equals(MinecraftClient.getInstance().player.getInventory()
                        .getStack(ProfileDataHandler.instance().profileData.equippedPetSlot))) {
            int alpha = ((int) 175f << 24);
            drawContext.getMatrices().pushMatrix();
            try {
                drawContext.getMatrices().translate(0, 0);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   selectedSlotMarker, x, y, 16, 16,
                        alpha | config.itemMarker.selectedPetHighlightColor);
            } finally {
                drawContext.getMatrices().popMatrix();
            }
        }

        // Show search item
        if (FOMCItem.isFOMCItem(itemStack)
                && !SearchBarContainerHandler.instance().searchString.isBlank()) {
            boolean isMatch = false;

            if (SearchBarContainerHandler.instance().searchFilter != null
                    && SearchBarContainerHandler.instance().operator != null) {
                if (SearchBarContainerHandler.instance().searchValue != null) {
                    if (FOMCItem.isPet(itemStack)[0]) {
                        isMatch = SearchBarContainerHandler.checkItem(Pet.getPet(itemStack),
                                SearchBarContainerHandler.instance().searchFilter,
                                SearchBarContainerHandler.instance().operator,
                                SearchBarContainerHandler.instance().searchValue);
                    } else if (FOMCItem.isArmor(itemStack)) {
                        isMatch = SearchBarContainerHandler.checkItem(Armor.getArmor(itemStack),
                                SearchBarContainerHandler.instance().searchFilter,
                                SearchBarContainerHandler.instance().operator,
                                SearchBarContainerHandler.instance().searchValue);
                    }
                }
            } else {
                NbtCompound data = ItemStackHelper.getNbt(itemStack);
                if (data != null
                        && (data.toString().toLowerCase()
                                .contains(SearchBarContainerHandler.instance().searchString.toLowerCase())
                                || itemStack.getName().getString().toLowerCase()
                                        .contains(SearchBarContainerHandler.instance().searchString.toLowerCase()))) {
                    isMatch = true;
                }
            }

            if (isMatch) {
                int alphaInt = (int) (0.6f * 255f) << 24;

                drawContext.getMatrices().pushMatrix();
                try {
                    drawContext.getMatrices().translate(0, 0);
                    drawContext.fill(x, y, x + 16, y + 16,
                            alphaInt | config.itemMarker.itemSearchMarker.searchHighlightColor);
                } finally {
                    drawContext.getMatrices().popMatrix();
                }
            }
        }

        // Show Bait Sorting Helper
        BaitSortingHelperHandler.instance().renderItemMarker(drawContext, itemStack, x, y);
    }

    /**
     * FoE colors the armor quality % in the item's lore by quality tier (ʙʀᴏᴋᴇɴ … sᴜᴘᴇʀɪᴏʀ).
     * Find the colored "&lt;quality&gt;%" lore segment and return its RGB so the slot marker
     * matches the server exactly, independent of the (server-side) tier thresholds. Returns
     * -1 when no colored match is found.
     */
    private int findArmorQualityColor(ItemStack itemStack, int quality) {
        LoreComponent lore = itemStack.get(DataComponentTypes.LORE);
        if (lore == null) {
            return -1;
        }
        String target = quality + "%";
        for (Text line : lore.lines()) {
            Optional<Integer> color = line.<Integer>visit((style, string) -> {
                TextColor textColor = style.getColor();
                if (textColor != null && string.contains(target)) {
                    return Optional.of(textColor.getRgb());
                }
                return Optional.empty();
            }, Style.EMPTY);
            if (color.isPresent()) {
                return color.get();
            }
        }
        return -1;
    }

    public void renderHotBarSelectedPet(DrawContext drawContext, int x, int y, ItemStack itemStack) {
        if (config.itemMarker.showSelectedPetHighlight
                && MinecraftClient.getInstance().player != null
                && !itemStack.isEmpty()
                && ProfileDataHandler.instance().profileData.equippedPet != null
                && ProfileDataHandler.instance().profileData.equippedPetSlot >= 0
                && itemStack.equals(MinecraftClient.getInstance().player.getInventory()
                        .getStack(ProfileDataHandler.instance().profileData.equippedPetSlot))) {
            int alpha = ((int) (0.6f * 255f) << 24);
            drawContext.getMatrices().pushMatrix();
            try {
                drawContext.getMatrices().translate(0, 0);
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,   selectedSlotMarker, x, y, 16, 16,
                        alpha | config.itemMarker.selectedPetHighlightColor);
            } finally {
                drawContext.getMatrices().popMatrix();
            }
        }
    }

    public enum FishSizeMarkerToggle {
        OFF,
        CHARACTER,
        MARKER
    }

    public enum PetPercentMarkerToggle {
        OFF,
        CHARACTER,
        MARKER
    }
}
