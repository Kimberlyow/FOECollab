package io.github.foecollab.screens.widget.container;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Decorative FishOnMC-Extras-R-style box panel (the drawer behind the modern inventory buttons).
 * Like the other container panels it stays out of click routing so the buttons on top get clicks.
 */
public class ModernBoxWidget extends ClickableWidget {
    private final Identifier texture;

    public ModernBoxWidget(int x, int y, int width, int height, Identifier texture) {
        super(x, y, width, height, Text.empty());
        this.texture = texture;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        BoxRenderer.draw(context, texture, this.getX(), this.getY(), this.width, this.height);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return false;
    }
}
