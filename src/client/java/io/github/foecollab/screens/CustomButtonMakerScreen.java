package io.github.foecollab.screens;

import io.github.foecollab.handler.InventoryButtonHandler;
import io.github.foecollab.handler.InventoryButtonHandler.CustomButton;
import io.github.foecollab.screens.widget.ButtonListWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Editor for the customizable inventory buttons (ported from FishOnMC-Extras-R). Lets the player
 * create, delete, reorder, show/hide, import and export buttons, and edit each one's name, hover
 * description, command and icon. Structural changes (create/delete/reorder/reset) persist
 * immediately; field edits apply on "Save &amp; Return".
 */
public class CustomButtonMakerScreen extends Screen {
    private final Screen parent;

    private ButtonListWidget buttonList;
    private TextFieldWidget nameField;
    private TextFieldWidget descriptionField;
    private TextFieldWidget commandField;
    private TextFieldWidget iconField;
    private ButtonWidget showToggle;

    private int selectedIndex = -1;
    private boolean editShow = true;

    private String statusMessage = "";
    private long statusUntil = 0L;

    // Layout
    private int fieldX;
    private int fieldWidth;
    private static final int LIST_WIDTH = 150;
    private static final int LIST_LEFT = 10;

    public CustomButtonMakerScreen(Screen parent) {
        super(Text.literal("Inventory Button Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        fieldX = LIST_LEFT + LIST_WIDTH + 20;
        fieldWidth = Math.max(80, this.width - fieldX - 10);

        int listHeight = this.height - 30 - 60;
        buttonList = new ButtonListWidget(this.client, LIST_WIDTH, listHeight, LIST_LEFT, 30, 20);
        this.addDrawableChild(buttonList);

        nameField = addField(50);
        descriptionField = addField(90);
        commandField = addField(130);
        iconField = addField(170);

        showToggle = ButtonWidget.builder(showLabel(), button -> {
            editShow = !editShow;
            button.setMessage(showLabel());
        }).dimensions(fieldX, 200, Math.min(fieldWidth, 120), 20).build();
        this.addDrawableChild(showToggle);

        // Left column actions (under the list)
        int actionY = this.height - 52;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Create"), button -> onCreate())
                .dimensions(LIST_LEFT, actionY, LIST_WIDTH / 2 - 2, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Delete"), button -> onDelete())
                .dimensions(LIST_LEFT + LIST_WIDTH / 2 + 2, actionY, LIST_WIDTH / 2 - 2, 20).build());

        // Right column actions (under the fields)
        int shareY = 232;
        int third = (fieldWidth - 8) / 3;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Import"), button -> onImport())
                .dimensions(fieldX, shareY, third, 20)
                .tooltip(Tooltip.of(Text.literal("Import a button from your clipboard"))).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Export"), button -> onExport())
                .dimensions(fieldX + third + 4, shareY, third, 20)
                .tooltip(Tooltip.of(Text.literal("Copy the selected button to your clipboard"))).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset all"), button -> onReset())
                .dimensions(fieldX + (third + 4) * 2, shareY, third, 20)
                .tooltip(Tooltip.of(Text.literal("Restore the default buttons"))).build());

        // Bottom-right: save / return
        int bottomY = this.height - 26;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save & Return"), button -> onSaveAndReturn())
                .dimensions(this.width - 10 - 150, bottomY, 150, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Return"), button -> close())
                .dimensions(this.width - 10 - 150 - 80, bottomY, 76, 20).build());

        rebuildList();
        applySelectionToFields();
    }

    private TextFieldWidget addField(int y) {
        TextFieldWidget field = new TextFieldWidget(this.textRenderer, fieldX, y, fieldWidth, 20, Text.empty());
        field.setMaxLength(256);
        this.addDrawableChild(field);
        return field;
    }

    private Text showLabel() {
        return Text.literal("Show button: " + (editShow ? "ON" : "OFF"))
                .formatted(editShow ? Formatting.GREEN : Formatting.RED);
    }

    private void rebuildList() {
        buttonList.clearAll();
        List<CustomButton> buttons = InventoryButtonHandler.instance().getButtons();
        int rowWidth = buttonList.rowWidth();
        for (int i = 0; i < buttons.size(); i++) {
            final int index = i;
            CustomButton button = buttons.get(i);
            String label = button.name == null ? "" : button.name.replace('&', '§');
            label = this.textRenderer.trimToWidth(label, rowWidth - 40);
            if (index == selectedIndex) {
                label = "§f▶ " + label;
            }

            ButtonWidget select = ButtonWidget.builder(Text.literal(label), b -> selectButton(index))
                    .dimensions(0, 0, rowWidth - 16, 20).build();
            ButtonWidget up = ButtonWidget.builder(Text.literal("▲"), b -> {
                InventoryButtonHandler.instance().moveUp(index);
                if (selectedIndex == index) {
                    selectedIndex = index - 1;
                } else if (selectedIndex == index - 1) {
                    selectedIndex = index;
                }
                rebuildList();
            }).dimensions(0, 0, 12, 9).build();
            ButtonWidget down = ButtonWidget.builder(Text.literal("▼"), b -> {
                InventoryButtonHandler.instance().moveDown(index);
                if (selectedIndex == index) {
                    selectedIndex = index + 1;
                } else if (selectedIndex == index + 1) {
                    selectedIndex = index;
                }
                rebuildList();
            }).dimensions(0, 0, 12, 9).build();

            buttonList.addButtonEntry(new ButtonListWidget.ButtonEntry(select, up, down, rowWidth));
        }
    }

    private void selectButton(int index) {
        selectedIndex = index;
        applySelectionToFields();
        rebuildList();
    }

    private void applySelectionToFields() {
        List<CustomButton> buttons = InventoryButtonHandler.instance().getButtons();
        if (selectedIndex < 0 || selectedIndex >= buttons.size()) {
            selectedIndex = -1;
            nameField.setText("");
            descriptionField.setText("");
            commandField.setText("");
            iconField.setText("");
            editShow = true;
            showToggle.setMessage(showLabel());
            return;
        }
        CustomButton button = buttons.get(selectedIndex);
        nameField.setText(button.name == null ? "" : button.name);
        descriptionField.setText(button.description == null ? "" : button.description);
        commandField.setText(button.action == null ? "" : button.action);
        iconField.setText(button.icon == null ? "" : button.icon);
        editShow = button.showButton;
        showToggle.setMessage(showLabel());
    }

    private void onCreate() {
        CustomButton button = new CustomButton();
        InventoryButtonHandler.instance().addButton(button);
        selectedIndex = InventoryButtonHandler.instance().getButtons().size() - 1;
        applySelectionToFields();
        rebuildList();
    }

    private void onDelete() {
        if (selectedIndex >= 0) {
            InventoryButtonHandler.instance().deleteButton(selectedIndex);
            selectedIndex = -1;
            applySelectionToFields();
            rebuildList();
        }
    }

    private void onReset() {
        InventoryButtonHandler.instance().resetToDefaults();
        selectedIndex = -1;
        applySelectionToFields();
        rebuildList();
        setStatus("Restored default buttons");
    }

    private void onImport() {
        if (this.client == null) {
            return;
        }
        String clipboard = this.client.keyboard.getClipboard();
        CustomButton imported = InventoryButtonHandler.instance().importButton(clipboard);
        if (imported == null) {
            setStatus("§cCould not import — clipboard data invalid");
            return;
        }
        InventoryButtonHandler.instance().addButton(imported);
        selectedIndex = InventoryButtonHandler.instance().getButtons().size() - 1;
        applySelectionToFields();
        rebuildList();
        setStatus("§aImported button");
    }

    private void onExport() {
        if (this.client == null || selectedIndex < 0) {
            setStatus("§cSelect a button to export");
            return;
        }
        CustomButton button = InventoryButtonHandler.instance().getButtons().get(selectedIndex);
        String code = InventoryButtonHandler.instance().exportButton(button);
        String shared = "**Custom Button: ** " + button.name + "\n```\n" + code + "\n```";
        this.client.keyboard.setClipboard(shared);
        setStatus("§aCopied button to clipboard");
    }

    private void onSaveAndReturn() {
        if (selectedIndex >= 0) {
            if (nameField.getText().isBlank()) {
                setStatus("§cName cannot be empty");
                return;
            }
            if (!commandField.getText().startsWith("/")) {
                setStatus("§cCommand must start with /");
                return;
            }
            CustomButton button = InventoryButtonHandler.instance().getButtons().get(selectedIndex);
            button.name = nameField.getText();
            button.description = descriptionField.getText();
            button.action = commandField.getText();
            button.icon = iconField.getText();
            button.showButton = editShow;
            InventoryButtonHandler.instance().save();
        }
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

        drawLabel(context, "Name", 50);
        drawLabel(context, "Description", 90);
        drawLabel(context, "Command (e.g. /spawn)", 130);
        drawLabel(context, "Icon (a glyph or namespace:id)", 170);

        if (!statusMessage.isEmpty() && System.currentTimeMillis() < statusUntil) {
            context.drawText(this.textRenderer, Text.literal(statusMessage), fieldX, this.height - 44, 0xFFFFFFFF, true);
        }
    }

    private void drawLabel(DrawContext context, String text, int fieldY) {
        context.drawText(this.textRenderer, Text.literal(text).formatted(Formatting.GRAY), fieldX, fieldY - 10, 0xFFFFFFFF, true);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}
