package io.github.foecollab.screens.hud;

import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.config.HudAlignment;
import io.github.foecollab.handler.screens.hud.BaitHudHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.Objects;

public class BaitHud {
    public void render(DrawContext drawContext, MinecraftClient client) {
        FOEConfig config = FOEConfig.getConfig();
        TextRenderer textRenderer = client.textRenderer;

        // Assemble all text lines
        Text baitText = BaitHudHandler.instance().assembleBaitText();
        CustomModelDataComponent modelData = BaitHudHandler.instance().getModelData();
        ItemStack baitStack = Items.COOKED_COD.getDefaultStack().copy();
        baitStack.set(DataComponentTypes.CUSTOM_MODEL_DATA, modelData);

        drawContext.getMatrices().pushMatrix();
        try {
            if(!Objects.equals(baitText.getString(), ""))  {
                // Get screen size
                int screenWidth = client.getWindow().getScaledWidth();
                int screenHeight = client.getWindow().getScaledHeight();

                HudAlignment alignment = config.baitTracker.alignment;

                // hudX is the anchor's position from the screen's left edge for every alignment;
                // CENTER puts the bait display's centre there, LEFT/RIGHT pin that edge of the block
                // (and the icon side). Changing alignment keeps it in place instead of jumping.
                float xPercent = config.baitTracker.hudX / 100f;
                float yPercent = config.baitTracker.hudY / 100f;

                // Calculate anchor position relative to screen size
                int baseX = (int) (screenWidth * xPercent);
                int baseY = (int) (screenHeight * yPercent);

                // Scaling setup
                int fontSize = config.baitTracker.fontSize;
                float scale = fontSize / 10.0f;
                drawContext.getMatrices().scale(scale, scale);

                // Alpha
                int alphaInt = (int) ((config.baitTracker.backgroundOpacity / 100f) * 255f) << 24;

                int padding = 8;
                int maxLength = textRenderer.getWidth(baitText);
                int blockWidth = padding * 2 + 4 + 16 + maxLength;
                int blockHeight = 24;

                // Anchor the block in scaled space, clamped on-screen.
                int scaledAnchorX = (int) (baseX / scale);
                int scaledAnchorY = (int) (baseY / scale);
                int scaledScreenWidth = (int) (screenWidth / scale);
                int scaledScreenHeight = (int) (screenHeight / scale);
                int unclampedLeft = switch (alignment) {
                    case CENTER -> scaledAnchorX - blockWidth / 2;
                    case LEFT -> scaledAnchorX;
                    case RIGHT -> scaledAnchorX - blockWidth;
                };
                int unclampedTop = alignment == HudAlignment.CENTER ? scaledAnchorY - blockHeight / 2 : scaledAnchorY;
                int left = (int) Math.clamp((long) unclampedLeft, 0, Math.max(0, scaledScreenWidth - blockWidth));
                int top = (int) Math.clamp((long) unclampedTop, 0, Math.max(0, scaledScreenHeight - blockHeight));
                int right = left + blockWidth;

                // Draw Background
                drawContext.fill(left, top, right, top + blockHeight, alphaInt);

                // Draw item border, icon and text. LEFT alignment puts the icon on the left
                // (icon then text), otherwise the icon sits on the right (text then icon).
                if(alignment == HudAlignment.LEFT) {
                    drawContext.drawStrokedRectangle(left + 2, top + 2, 20, 20, alphaInt | 0xFFFFFF);
                    drawContext.drawItem(baitStack, left + 4, top + 4);
                    drawContext.drawText(textRenderer, baitText, left + padding + 4 + 16, top + 12 - fontSize / 2, 0xFFFFFFFF, true);
                } else {
                    drawContext.drawStrokedRectangle(right - 2 - 20, top + 2, 20, 20, alphaInt | 0xFFFFFF);
                    drawContext.drawItem(baitStack, right - 4 - 16, top + 4);
                    drawContext.drawText(textRenderer, baitText, right - padding - 4 - 16 - maxLength, top + 12 - fontSize / 2, 0xFFFFFFFF, true);
                }
            }
        } finally {
            drawContext.getMatrices().popMatrix();
        }
    }
}
