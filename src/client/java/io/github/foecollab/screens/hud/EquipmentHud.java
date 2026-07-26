package io.github.foecollab.screens.hud;

import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.FOMC.Types.FOMCItem;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.handler.FishingRodHandler;
import io.github.foecollab.handler.screens.hud.EquipmentHudHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class EquipmentHud {
    public void render(DrawContext drawContext, MinecraftClient client) {
        FOEConfig config = FOEConfig.getConfig();
        TextRenderer textRenderer = client.textRenderer;

        // Assemble all text lines
        ItemStack chestplate = EquipmentHudHandler.instance().getChestPlate();
        ItemStack leggings = EquipmentHudHandler.instance().getLeggings();
        ItemStack boots = EquipmentHudHandler.instance().getBoots();
        ItemStack reel = EquipmentHudHandler.instance().getReel();
        ItemStack pole = EquipmentHudHandler.instance().getPole();
        ItemStack line = EquipmentHudHandler.instance().getLine();

        drawContext.getMatrices().pushMatrix();
        try {
            // Get screen size
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();

            // Convert percentage config values to screen coordinates
            float xPercent = 50 / 100f;
            float yPercent = 100 / 100f;

            // Calculate base positions relative to screen size
            int baseX = (int) (screenWidth * xPercent);
            int baseY = (int) (screenHeight * yPercent);

            // Scaling setup
            int fontSize = config.equipmentTracker.fontSize;
            float scale = fontSize / 10.0f;
            drawContext.getMatrices().scale(scale, scale);

            // Alpha
            int alphaInt = (int) ((config.equipmentTracker.backgroundOpacity / 100f) * 255f) << 24;

            int scaledX = (int) (baseX / scale);
            int scaledY = (int) (baseY / scale);


            int offsetMiddleArmor = config.equipmentTracker.armorHUDOptions.offsetFromMiddle;
            int offsetMiddleRodParts = config.equipmentTracker.rodPartsHUDOptions.offsetFromMiddle;
            int offsetBottomArmor = config.equipmentTracker.armorHUDOptions.offsetFromBottom;
            int offsetBottomRodParts = config.equipmentTracker.rodPartsHUDOptions.offsetFromBottom;

            // Chestplate
            renderBox(drawContext, textRenderer, scaledX - 64 + offsetMiddleArmor, scaledY - offsetBottomArmor - 20, alphaInt, chestplate, "ᴄ", null);
            // Leggings
            renderBox(drawContext, textRenderer, scaledX - 42 + offsetMiddleArmor, scaledY - offsetBottomArmor - 20, alphaInt, leggings, "ʟ", null);
            // Boots
            renderBox(drawContext, textRenderer, scaledX - 20 + offsetMiddleArmor, scaledY - offsetBottomArmor - 20, alphaInt, boots, "ʙ", null);

            // Reel
            renderBox(drawContext, textRenderer, scaledX + offsetMiddleRodParts, scaledY - offsetBottomRodParts - 20, alphaInt, reel, "ʀ", "reel");
            // Pole
            renderBox(drawContext, textRenderer, scaledX + 22 + offsetMiddleRodParts, scaledY - offsetBottomRodParts - 20, alphaInt, pole, "ᴘ", "pole");
            // Line
            renderBox(drawContext, textRenderer, scaledX + 44 + offsetMiddleRodParts, scaledY - offsetBottomRodParts - 20, alphaInt, line, "ʟ", "line");

        } finally {
            drawContext.getMatrices().popMatrix();
        }
    }

    private void renderBox(DrawContext drawContext, TextRenderer textRenderer, int x, int y, int alpha, ItemStack itemStack, String character, String equipmentType) {
        drawContext.fill(x, y, x + 20, y + 20, alpha);

        int borderColor = alpha | 0xFFFFFF;
        Constant rarity = Constant.DEFAULT;

        if (equipmentType != null) {
            switch (equipmentType) {
                case "reel" -> {
                    if (FishingRodHandler.instance().fishingRod.reel != null) {
                        rarity = FishingRodHandler.instance().fishingRod.reel.rarity;
                    }
                }
                case "pole" -> {
                    if (FishingRodHandler.instance().fishingRod.pole != null) {
                        rarity = FishingRodHandler.instance().fishingRod.pole.rarity;
                    }
                }
                case "line" -> {
                    if (FishingRodHandler.instance().fishingRod.line != null) {
                        rarity = FishingRodHandler.instance().fishingRod.line.rarity;
                    }
                }
            }
        } else {
            rarity = FOMCItem.getRarity(itemStack);
        }

        if (rarity != Constant.DEFAULT) {
            borderColor = (alpha & 0xFF000000) | (rarity.COLOR & 0x00FFFFFF);
        }

        drawContext.drawStrokedRectangle( x, y + 20, 20, 1, borderColor);

        if(itemStack.getItem() != Items.AIR) {
            drawContext.drawItem(itemStack, x + 2, y + 2);
        } else {
            drawContext.drawText(textRenderer, Text.literal(character).formatted(Formatting.GRAY), x + 10 - textRenderer.getWidth(character) / 2, y + 10 - textRenderer.fontHeight / 2, alpha | 0xFFFFFF, true);
        }
    }
}