package io.github.foecollab.screens;

import io.github.foecollab.screens.widget.VariableListWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Reference list of every custom-HUD {@code %variable%}, opened from {@link CustomHudMakerScreen}.
 * The search box autocompletes the list by token prefix (e.g. {@code %sco}); left-clicking a token
 * copies it to the clipboard so it can be pasted into a HUD line.
 */
public class CustomHudVariablesScreen extends Screen {
    private final Screen parent;

    private TextFieldWidget search;
    private VariableListWidget list;

    private String status = "";
    private long statusUntil = 0L;

    public CustomHudVariablesScreen(Screen parent) {
        super(Text.literal("Custom HUD Variables"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int searchW = Math.min(300, this.width - 40);
        search = new TextFieldWidget(this.textRenderer, this.width / 2 - searchW / 2, 28, searchW, 18,
                Text.literal("Search variables"));
        search.setMaxLength(64);
        search.setPlaceholder(Text.literal("Search… e.g. %sco").formatted(Formatting.DARK_GRAY));
        search.setChangedListener(q -> {
            if (list != null) {
                list.applyFilter(q);
            }
        });
        this.addDrawableChild(search);

        int top = 52;
        int bottom = this.height - 32;
        list = new VariableListWidget(this.client, this.width, bottom - top, top, this::copy);
        this.addDrawableChild(list);
        list.applyFilter(search.getText());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(this.width / 2 - 100, this.height - 26, 200, 20).build());

        this.setInitialFocus(search);
    }

    private void copy(String token) {
        if (this.client != null) {
            this.client.keyboard.setClipboard(token);
            this.status = "§aCopied " + token;
            this.statusUntil = System.currentTimeMillis() + 2500L;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.getTitle(), this.width / 2, 8, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Type to filter • click a variable to copy it").formatted(Formatting.GRAY),
                this.width / 2, 18, 0xFFFFFFFF);

        if (!status.isEmpty() && System.currentTimeMillis() < statusUntil) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(status), this.width / 2, this.height - 40, 0xFFFFFFFF);
        }
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
}
