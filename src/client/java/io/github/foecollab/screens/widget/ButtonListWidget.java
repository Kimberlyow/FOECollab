package io.github.foecollab.screens.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;

import java.util.List;

/**
 * Scrollable list of the user's custom inventory buttons, used by {@code CustomButtonMakerScreen}.
 * Each row is a select button (the button's name) plus small up/down arrows to reorder it.
 */
public class ButtonListWidget extends ElementListWidget<ButtonListWidget.ButtonEntry> {
    public ButtonListWidget(MinecraftClient client, int width, int height, int x, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
        this.setX(x);
    }

    @Override
    public int getRowWidth() {
        return this.width - 12;
    }

    @Override
    protected int getScrollbarX() {
        return this.getX() + this.width - 6;
    }

    public int rowWidth() {
        return getRowWidth();
    }

    public void addButtonEntry(ButtonEntry entry) {
        this.addEntry(entry);
    }

    public void clearAll() {
        this.clearEntries();
    }

    public static class ButtonEntry extends ElementListWidget.Entry<ButtonEntry> {
        private static final int ARROW_WIDTH = 12;
        private static final int GAP = 2;

        private final ButtonWidget selectButton;
        private final ButtonWidget upButton;
        private final ButtonWidget downButton;
        private final int rowWidth;

        public ButtonEntry(ButtonWidget selectButton, ButtonWidget upButton, ButtonWidget downButton, int rowWidth) {
            this.selectButton = selectButton;
            this.upButton = upButton;
            this.downButton = downButton;
            this.rowWidth = rowWidth;
        }

        @Override
        public List<? extends Element> children() {
            return List.of(selectButton, upButton, downButton);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of(selectButton, upButton, downButton);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float delta) {
            int x = getX();
            int y = getY();

            upButton.setX(x);
            upButton.setY(y);
            downButton.setX(x);
            downButton.setY(y + 10);

            selectButton.setX(x + ARROW_WIDTH + GAP);
            selectButton.setY(y);
            selectButton.setWidth(rowWidth - ARROW_WIDTH - GAP);

            selectButton.render(context, mouseX, mouseY, delta);
            upButton.render(context, mouseX, mouseY, delta);
            downButton.render(context, mouseX, mouseY, delta);
        }
    }
}
