package io.github.foecollab.screens.main;

import io.github.foecollab.config.ConfigConstants;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.handler.TabHandler;
import io.github.foecollab.screens.CustomHudMakerScreen;
import io.github.foecollab.screens.debug.DebugScreen;
import io.github.foecollab.screens.movehud.MoveHudScreen;
import io.github.foecollab.screens.widget.IconButtonWidget;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class MainScreen extends Screen {
    private final MinecraftClient minecraftClient;
    private final Screen parent;

    public MainScreen(MinecraftClient minecraftClient, Screen parent) {
        super(Text.literal("FoE Main Screen"));
        this.minecraftClient = minecraftClient;
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.renderWidgets();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int centerX = width / 2;
        int centerY = height / 2;
        int lineHeight = textRenderer.fontHeight + 1;
        int totalLines = 4;
        int startY = centerY - (totalLines * lineHeight) / 2;

        context.drawVerticalLine(centerX, centerY - 28, centerY + 28, 0xFFFFFFFF);

        context.drawText(textRenderer, Text.literal("Welcome to FishOnMC-Extras"), centerX - 4 - textRenderer.getWidth("Welcome to FishOnMC-Extras"), startY, 0xFFFFFFFF, true);
        context.drawText(textRenderer, TabHandler.instance().player, centerX - 4 - textRenderer.getWidth(TabHandler.instance().player), startY + lineHeight, 0xFFFFFFFF, true);
        context.drawText(textRenderer, Text.literal(ConfigConstants.MOD_VERSION), centerX - 4 - textRenderer.getWidth(ConfigConstants.MOD_VERSION), startY + lineHeight * 3, 0xFFAAAAAA, true);
    }

    private void renderWidgets() {
        List<ClickableWidget> widgets = new ArrayList<>();

        widgets.add(IconButtonWidget.builder(Text.literal("FoE Config"), button -> minecraftClient.setScreen(AutoConfig.getConfigScreen(FOEConfig.class, minecraftClient.currentScreen).get()))
                .position(width / 2 + 4, height / 2 - 24 - 4)
                .itemIcon(Items.COMMAND_BLOCK.getDefaultStack())
                .width(130)
                .build());

        widgets.add(IconButtonWidget.builder(Text.literal("Move HUD Elements"), button -> minecraftClient.setScreen(new MoveHudScreen(minecraftClient, minecraftClient.currentScreen)))
                .position(width / 2 + 4, height / 2 + 4)
                .itemIcon(Items.STRUCTURE_VOID.getDefaultStack())
                .width(130)
                .build());

        widgets.add(IconButtonWidget.builder(Text.literal("Custom HUDs"), button -> minecraftClient.setScreen(new CustomHudMakerScreen(minecraftClient.currentScreen)))
                .position(width / 2 + 4, height / 2 + 36)
                .itemIcon(Items.OAK_SIGN.getDefaultStack())
                .width(130)
                .build());

        if(ConfigConstants.DEV) {
            widgets.add(net.minecraft.client.gui.widget.ButtonWidget.builder(Text.literal("FoE Debug"), button -> minecraftClient.setScreen(new DebugScreen(minecraftClient, minecraftClient.currentScreen)))
                    .dimensions(width / 2 - 100, height - 20 - 8, 200, 20)
                    .tooltip(Tooltip.of(Text.literal("Open Debug Screen")))
                    .build());
        }

        widgets.forEach(this::addDrawableChild);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}
