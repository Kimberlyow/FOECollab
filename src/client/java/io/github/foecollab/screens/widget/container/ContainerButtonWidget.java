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
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public class ContainerButtonWidget extends ClickableWidget {
    private final TextRenderer textRenderer;
    private final ClickCallback clickCallback;
    @Nullable
    private final ItemStack itemIcon;

    public ContainerButtonWidget(int x, int y, Text message, @Nullable Tooltip tooltip, ClickCallback clickCallback) {
        this(x, y, message, tooltip, null, clickCallback);
    }

    public ContainerButtonWidget(int x, int y, Text message, @Nullable Tooltip tooltip) {
        this(x, y, message, tooltip, null, null);
    }

    public ContainerButtonWidget(int x, int y, Text message, @Nullable Tooltip tooltip, @Nullable ItemStack itemIcon, @Nullable ClickCallback clickCallback) {
        super(x, y, 22, 22, message);
        this.textRenderer = MinecraftClient.getInstance().textRenderer;
        this.setTooltip(tooltip);
        this.itemIcon = itemIcon;
        this.clickCallback = clickCallback;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        BoxRenderer.draw(context, (this.isHovered() || this.isFocused()) ? BoxRenderer.BOX_SOLID_ALT : BoxRenderer.BOX_SOLID, this.getX(), this.getY(), this.width, this.height, ButtonColor.tint());

        if(itemIcon != null && !itemIcon.isEmpty()) {
            context.drawItem(itemIcon, this.getX() + this.width / 2 - 8, this.getY() + this.height / 2 - 8);
        } else {
            context.drawText(textRenderer, this.getMessage(), this.getX() + this.width / 2 - this.textRenderer.getWidth(this.getMessage()) / 2, this.getY() + this.height / 2 - this.textRenderer.fontHeight / 2, 0xFFFFFFFF, true);
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        super.onClick(click, doubled);
        if(clickCallback != null) {
            this.clickCallback.onClick(this);
            this.setMessage(Text.literal("...").formatted(Formatting.GRAY));
        }
    }

    public interface ClickCallback {
        void onClick(ContainerButtonWidget iconButtonWidget);
    }
}
