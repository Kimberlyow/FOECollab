package io.github.foecollab.screens.widget.container;

import io.github.foecollab.common.ButtonColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

/**
 * A FishOnMC-Extras-R-style inventory button: a dark "box" background (solid normal / alt on hover)
 * with a glyph or item icon centered on it. Ported look from FOE-R's {@code SmallButtonWidget}.
 */
public class ModernButtonWidget extends ClickableWidget {
    private final TextRenderer textRenderer;
    private final ClickCallback clickCallback;
    @Nullable
    private final ItemStack itemIcon;

    public ModernButtonWidget(int x, int y, int width, int height, Text message, @Nullable Tooltip tooltip, @Nullable ItemStack itemIcon, @Nullable ClickCallback clickCallback) {
        super(x, y, width, height, message);
        this.textRenderer = MinecraftClient.getInstance().textRenderer;
        this.setTooltip(tooltip);
        this.itemIcon = itemIcon;
        this.clickCallback = clickCallback;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        net.minecraft.util.Identifier box = (this.isHovered() || this.isFocused()) ? BoxRenderer.BOX_SOLID_ALT : BoxRenderer.BOX_SOLID;
        BoxRenderer.draw(context, box, this.getX(), this.getY(), this.width, this.height, ButtonColor.tint());

        if (itemIcon != null && !itemIcon.isEmpty()) {
            context.drawItem(itemIcon, this.getX() + this.width / 2 - 8, this.getY() + this.height / 2 - 8);
        } else {
            context.drawText(textRenderer, this.getMessage(),
                    this.getX() + this.width / 2 - textRenderer.getWidth(this.getMessage()) / 2,
                    this.getY() + this.height / 2 - textRenderer.fontHeight / 2, 0xFFFFFFFF, true);
        }
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        super.onClick(click, doubled);
        if (clickCallback != null) {
            this.clickCallback.onClick(this);
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    public interface ClickCallback {
        void onClick(ModernButtonWidget widget);
    }
}
