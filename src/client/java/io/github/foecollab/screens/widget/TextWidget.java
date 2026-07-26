package io.github.foecollab.screens.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class TextWidget extends ClickableWidget {
    private final TextRenderer textRenderer;
    private final int color;
    private final boolean shadow;
    private final ClickCallback clickCallback;

    public TextWidget(int x, int y, Text message, int color, boolean shadow) {
        super(x, y, 0, 0, message);
        this.textRenderer = MinecraftClient.getInstance().textRenderer;
        this.color = color;
        this.shadow = shadow;
        this.setWidth(textRenderer.getWidth(message));
        this.setHeight(textRenderer.fontHeight);
        this.clickCallback = null;
    }

    public TextWidget(int x, int y, Text message, int color, boolean shadow, ClickCallback clickCallback) {
        super(x, y, 0, 0, message);
        this.textRenderer = MinecraftClient.getInstance().textRenderer;
        this.color = color;
        this.shadow = shadow;
        this.setWidth(textRenderer.getWidth(message));
        this.setHeight(textRenderer.fontHeight);
        this.clickCallback = clickCallback;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawText(this.textRenderer, this.getMessage(), this.getX(), this.getY(), this.color, this.shadow);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        super.onClick(click, doubled);
        if(clickCallback != null) {
            this.clickCallback.onClick(this);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        // Decorative labels are click-through so they can't steal a click from a widget beneath them;
        // only labels with a callback (e.g. crew names) take part in ParentElement#hoveredElement.
        return clickCallback != null && super.isMouseOver(mouseX, mouseY);
    }

    public interface ClickCallback {
        void onClick(TextWidget iconButtonWidget);
    }
}
