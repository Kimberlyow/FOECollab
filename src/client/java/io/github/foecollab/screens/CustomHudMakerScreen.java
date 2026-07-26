package io.github.foecollab.screens;

import io.github.foecollab.config.HudAlignment;
import io.github.foecollab.handler.CustomHudHandler;
import io.github.foecollab.handler.CustomHudHandler.CustomHud;
import io.github.foecollab.screens.widget.ButtonListWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Editor for the user's custom HUDs (ported from FishOnMC-Extras-R). The left column lists the
 * HUDs (create / delete / reorder); the right column edits the selected HUD's name, alignment,
 * position, scale, opacity and its "custom code" lines. Field edits are committed to the selected
 * HUD whenever the selection changes or an action runs, so nothing is lost while editing.
 */
public class CustomHudMakerScreen extends Screen {
    private static final String CODE_HINT =
            "Lines: & colour codes + %codes%. e.g. %player.x%, %stats.fish_caught%, %boss_bar.location%. "
                    + "Click \"Variables\" to search the full list and copy any code.";

    private final Screen parent;

    private ButtonListWidget hudList;
    private TextFieldWidget nameField;
    private TextFieldWidget xField;
    private TextFieldWidget yField;
    private TextFieldWidget sizeField;
    private TextFieldWidget opacityField;
    private ButtonWidget alignmentButton;
    private ButtonWidget enabledButton;
    private EditBoxWidget linesBox;

    private int selectedIndex = -1;
    private HudAlignment editAlignment = HudAlignment.LEFT;
    private boolean editEnabled = true;

    private String statusMessage = "";
    private long statusUntil = 0L;

    private static final int LIST_LEFT = 10;
    private static final int LIST_WIDTH = 130;
    private int fieldX;
    private int fieldWidth;

    public CustomHudMakerScreen(Screen parent) {
        super(Text.literal("Custom HUD Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        fieldX = LIST_LEFT + LIST_WIDTH + 20;
        fieldWidth = Math.max(120, this.width - fieldX - 10);

        int listHeight = this.height - 30 - 30;
        hudList = new ButtonListWidget(this.client, LIST_WIDTH, listHeight, LIST_LEFT, 30, 20);
        this.addDrawableChild(hudList);

        // Left column actions (under the list)
        int actionY = this.height - 26;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Create"), b -> onCreate())
                .dimensions(LIST_LEFT, actionY, LIST_WIDTH / 2 - 2, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Delete"), b -> onDelete())
                .dimensions(LIST_LEFT + LIST_WIDTH / 2 + 2, actionY, LIST_WIDTH / 2 - 2, 20).build());

        // Right column: properties
        nameField = new TextFieldWidget(this.textRenderer, fieldX, 40, fieldWidth, 20, Text.empty());
        nameField.setMaxLength(64);
        this.addDrawableChild(nameField);

        alignmentButton = ButtonWidget.builder(alignmentLabel(), b -> {
            editAlignment = nextAlignment(editAlignment);
            b.setMessage(alignmentLabel());
        }).dimensions(fieldX, 72, fieldWidth / 2 - 4, 20).build();
        this.addDrawableChild(alignmentButton);

        enabledButton = ButtonWidget.builder(enabledLabel(), b -> {
            editEnabled = !editEnabled;
            b.setMessage(enabledLabel());
        }).dimensions(fieldX + fieldWidth / 2 + 4, 72, fieldWidth / 2 - 4, 20).build();
        this.addDrawableChild(enabledButton);

        int quarter = (fieldWidth - 12) / 4;
        xField = addNumberField(fieldX, 114, quarter);
        yField = addNumberField(fieldX + (quarter + 4), 114, quarter);
        sizeField = addNumberField(fieldX + (quarter + 4) * 2, 114, quarter);
        opacityField = addNumberField(fieldX + (quarter + 4) * 3, 114, quarter);

        int linesTop = 148;
        int linesBottom = this.height - 56;
        int linesHeight = Math.max(40, linesBottom - linesTop);
        linesBox = EditBoxWidget.builder()
                .x(fieldX).y(linesTop)
                .build(this.textRenderer, fieldWidth, linesHeight, Text.literal("HUD lines (one per row)"));
        linesBox.setMaxLength(8192);
        linesBox.setMaxLines(64);
        this.addDrawableChild(linesBox);

        // Share row
        int shareY = this.height - 52;
        int shareW = (fieldWidth - 12) / 4;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Variables"), b -> onVariables())
                .dimensions(fieldX, shareY, shareW, 20)
                .tooltip(Tooltip.of(Text.literal("Browse every %variable% you can use, and click to add one"))).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Import"), b -> onImport())
                .dimensions(fieldX + (shareW + 4), shareY, shareW, 20)
                .tooltip(Tooltip.of(Text.literal("Import a HUD from your clipboard"))).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Export"), b -> onExport())
                .dimensions(fieldX + (shareW + 4) * 2, shareY, shareW, 20)
                .tooltip(Tooltip.of(Text.literal("Copy the selected HUD to your clipboard"))).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), b -> onReset())
                .dimensions(fieldX + (shareW + 4) * 3, shareY, shareW, 20)
                .tooltip(Tooltip.of(Text.literal("Restore the default example HUD"))).build());

        // Done
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> onDone())
                .dimensions(this.width - 10 - 100, this.height - 26, 100, 20).build());

        rebuildList();
        applySelectionToFields();
    }

    private TextFieldWidget addNumberField(int x, int y, int width) {
        TextFieldWidget field = new TextFieldWidget(this.textRenderer, x, y, width, 20, Text.empty());
        field.setMaxLength(4);
        field.setTextPredicate(s -> s.matches("\\d*"));
        this.addDrawableChild(field);
        return field;
    }

    private static HudAlignment nextAlignment(HudAlignment alignment) {
        return switch (alignment) {
            case LEFT -> HudAlignment.CENTER;
            case CENTER -> HudAlignment.RIGHT;
            case RIGHT -> HudAlignment.LEFT;
        };
    }

    private Text alignmentLabel() {
        return Text.literal("Align: " + editAlignment.name());
    }

    private Text enabledLabel() {
        return Text.literal("Enabled: " + (editEnabled ? "ON" : "OFF"))
                .formatted(editEnabled ? Formatting.GREEN : Formatting.RED);
    }

    private void rebuildList() {
        hudList.clearAll();
        List<CustomHud> huds = CustomHudHandler.instance().getHuds();
        int rowWidth = hudList.rowWidth();
        for (int i = 0; i < huds.size(); i++) {
            final int index = i;
            CustomHud hud = huds.get(i);
            String label = hud.name == null ? "" : hud.name.replace('&', '§');
            if (!hud.enabled) {
                label = "§8" + label + " §7(off)";
            }
            label = this.textRenderer.trimToWidth(label, rowWidth - 40);
            if (index == selectedIndex) {
                label = "§f▶ " + label;
            }

            ButtonWidget select = ButtonWidget.builder(Text.literal(label), b -> selectHud(index))
                    .dimensions(0, 0, rowWidth - 16, 20).build();
            ButtonWidget up = ButtonWidget.builder(Text.literal("▲"), b -> {
                commitToSelected();
                CustomHudHandler.instance().moveUp(index);
                if (selectedIndex == index) {
                    selectedIndex = index - 1;
                } else if (selectedIndex == index - 1) {
                    selectedIndex = index;
                }
                rebuildList();
            }).dimensions(0, 0, 12, 9).build();
            ButtonWidget down = ButtonWidget.builder(Text.literal("▼"), b -> {
                commitToSelected();
                CustomHudHandler.instance().moveDown(index);
                if (selectedIndex == index) {
                    selectedIndex = index + 1;
                } else if (selectedIndex == index + 1) {
                    selectedIndex = index;
                }
                rebuildList();
            }).dimensions(0, 0, 12, 9).build();

            hudList.addButtonEntry(new ButtonListWidget.ButtonEntry(select, up, down, rowWidth));
        }
    }

    private void selectHud(int index) {
        commitToSelected();
        selectedIndex = index;
        applySelectionToFields();
        rebuildList();
    }

    private void applySelectionToFields() {
        List<CustomHud> huds = CustomHudHandler.instance().getHuds();
        boolean valid = selectedIndex >= 0 && selectedIndex < huds.size();
        setFieldsActive(valid);
        if (!valid) {
            selectedIndex = -1;
            nameField.setText("");
            xField.setText("");
            yField.setText("");
            sizeField.setText("");
            opacityField.setText("");
            linesBox.setText("");
            editAlignment = HudAlignment.LEFT;
            editEnabled = true;
            alignmentButton.setMessage(alignmentLabel());
            enabledButton.setMessage(enabledLabel());
            return;
        }
        CustomHud hud = huds.get(selectedIndex);
        nameField.setText(hud.name == null ? "" : hud.name);
        xField.setText(String.valueOf(hud.hudX));
        yField.setText(String.valueOf(hud.hudY));
        sizeField.setText(String.valueOf(hud.fontSize));
        opacityField.setText(String.valueOf(hud.backgroundOpacity));
        editAlignment = hud.alignment == null ? HudAlignment.LEFT : hud.alignment;
        editEnabled = hud.enabled;
        alignmentButton.setMessage(alignmentLabel());
        enabledButton.setMessage(enabledLabel());
        linesBox.setText(hud.lines == null ? "" : String.join("\n", hud.lines));
    }

    private void setFieldsActive(boolean active) {
        nameField.active = active;
        nameField.visible = active;
        xField.active = active;
        xField.visible = active;
        yField.active = active;
        yField.visible = active;
        sizeField.active = active;
        sizeField.visible = active;
        opacityField.active = active;
        opacityField.visible = active;
        alignmentButton.active = active;
        alignmentButton.visible = active;
        enabledButton.active = active;
        enabledButton.visible = active;
        linesBox.active = active;
        linesBox.visible = active;
    }

    /** Writes the current field values into the selected HUD (in memory) and persists. */
    private void commitToSelected() {
        List<CustomHud> huds = CustomHudHandler.instance().getHuds();
        if (selectedIndex < 0 || selectedIndex >= huds.size()) {
            return;
        }
        CustomHud hud = huds.get(selectedIndex);
        if (!nameField.getText().isBlank()) {
            hud.name = nameField.getText();
        }
        hud.hudX = clampPercent(xField.getText(), hud.hudX, 100);
        hud.hudY = clampPercent(yField.getText(), hud.hudY, 100);
        hud.fontSize = clampPercent(sizeField.getText(), hud.fontSize, 60);
        hud.fontSize = Math.max(4, hud.fontSize);
        hud.backgroundOpacity = clampPercent(opacityField.getText(), hud.backgroundOpacity, 100);
        hud.alignment = editAlignment;
        hud.enabled = editEnabled;

        List<String> lines = new ArrayList<>(Arrays.asList(linesBox.getText().split("\n", -1)));
        // Drop a single trailing empty line the editor leaves behind, but keep intentional gaps.
        if (!lines.isEmpty() && lines.get(lines.size() - 1).isBlank()) {
            lines.remove(lines.size() - 1);
        }
        hud.lines = lines;

        CustomHudHandler.instance().save();
    }

    private int clampPercent(String text, int fallback, int max) {
        try {
            return Math.max(0, Math.min(max, Integer.parseInt(text.trim())));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private void onCreate() {
        commitToSelected();
        CustomHud hud = new CustomHud();
        CustomHudHandler.instance().addHud(hud);
        selectedIndex = CustomHudHandler.instance().getHuds().size() - 1;
        applySelectionToFields();
        rebuildList();
    }

    private void onDelete() {
        if (selectedIndex >= 0) {
            CustomHudHandler.instance().deleteHud(selectedIndex);
            selectedIndex = -1;
            applySelectionToFields();
            rebuildList();
        }
    }

    private void onReset() {
        CustomHudHandler.instance().resetToDefaults();
        selectedIndex = -1;
        applySelectionToFields();
        rebuildList();
        setStatus("§aRestored default HUD");
    }

    private void onImport() {
        if (this.client == null) {
            return;
        }
        CustomHud imported = CustomHudHandler.instance().importHud(this.client.keyboard.getClipboard());
        if (imported == null) {
            setStatus("§cCould not import — clipboard data invalid");
            return;
        }
        commitToSelected();
        CustomHudHandler.instance().addHud(imported);
        selectedIndex = CustomHudHandler.instance().getHuds().size() - 1;
        applySelectionToFields();
        rebuildList();
        setStatus("§aImported HUD");
    }

    private void onExport() {
        if (this.client == null || selectedIndex < 0) {
            setStatus("§cSelect a HUD to export");
            return;
        }
        commitToSelected();
        CustomHud hud = CustomHudHandler.instance().getHuds().get(selectedIndex);
        String code = CustomHudHandler.instance().exportHud(hud);
        this.client.keyboard.setClipboard("**Custom HUD: ** " + hud.name + "\n```\n" + code + "\n```");
        setStatus("§aCopied HUD to clipboard");
    }

    private void onVariables() {
        commitToSelected();
        if (this.client != null) {
            this.client.setScreen(new CustomHudVariablesScreen(this));
        }
    }

    private void onDone() {
        commitToSelected();
        close();
    }

    private void setStatus(String message) {
        this.statusMessage = message;
        this.statusUntil = System.currentTimeMillis() + 3000L;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.getTitle(), this.width / 2, 12, 0xFFFFFFFF);

        if (selectedIndex >= 0) {
            drawLabel(context, "Name", 30);
            drawLabel(context, "Position X% / Y% / Font / Opacity%", 104);
            drawLabel(context, "Lines", 138);
        } else {
            context.drawText(this.textRenderer, Text.literal("Select or create a HUD →").formatted(Formatting.GRAY),
                    fieldX, 40, 0xFFFFFFFF, true);
        }

        context.drawText(this.textRenderer, Text.literal(CODE_HINT).formatted(Formatting.DARK_GRAY),
                LIST_LEFT, this.height - 40, 0xFFFFFFFF, false);

        if (!statusMessage.isEmpty() && System.currentTimeMillis() < statusUntil) {
            context.drawText(this.textRenderer, Text.literal(statusMessage), fieldX, this.height - 64, 0xFFFFFFFF, true);
        }
    }

    private void drawLabel(DrawContext context, String text, int y) {
        context.drawText(this.textRenderer, Text.literal(text).formatted(Formatting.GRAY), fieldX, y, 0xFFFFFFFF, true);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}
