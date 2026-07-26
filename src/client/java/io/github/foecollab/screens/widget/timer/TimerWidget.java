package io.github.foecollab.screens.widget.timer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;

public class TimerWidget extends ClickableWidget {
    protected final TextRenderer textRenderer;
    private final ClickCallback clickCallback;

    public TimerWidget(int x, int y) {
        super(x, y, 0, 0, null);
        this.textRenderer = MinecraftClient.getInstance().textRenderer;
        this.setHeight(textRenderer.fontHeight);
        this.clickCallback = null;
    }

    public TimerWidget(int x, int y, ClickCallback clickCallback) {
        super(x, y, 0, 0, null);
        this.textRenderer = MinecraftClient.getInstance().textRenderer;
        this.setHeight(textRenderer.fontHeight);
        this.clickCallback = clickCallback;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
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
    public boolean mouseClicked(Click click, boolean doubled) {
        if(clickCallback != null) {
            this.clickCallback.onClick(this);
            return super.mouseClicked(click, doubled);
        }
        return false;
    }

    public interface ClickCallback {
        void onClick(TimerWidget iconButtonWidget);
    }
}
