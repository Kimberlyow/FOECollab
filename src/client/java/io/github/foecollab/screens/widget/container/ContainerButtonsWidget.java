package io.github.foecollab.screens.widget.container;

import io.github.foecollab.FOECollab;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ContainerButtonsWidget extends ClickableWidget {
    private final Identifier panelTexture = Identifier.of(FOECollab.MOD_ID, "containers/panel_button");

    public ContainerButtonsWidget(int x, int y, Text message) {
        super(x, y, 174, 59, message);
    }

    public ContainerButtonsWidget(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawGuiTexture( RenderPipelines.GUI_TEXTURED, panelTexture, this.getX(), this.getY(), this.width, this.height);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    // Purely decorative background panel. Since 1.21.11 a screen dispatches a click only to the
    // topmost element whose isMouseOver() is true (ParentElement#hoveredElement). Returning false
    // here keeps this panel out of that lookup so the buttons drawn on top of it still get clicks.
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return false;
    }
}
