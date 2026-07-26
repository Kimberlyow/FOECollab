package io.github.foecollab.handler;

import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.FOMC.LevelColors;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.mixin.RecipeBookScreenAccessor;
import io.github.foecollab.screens.CustomButtonMakerScreen;
import io.github.foecollab.screens.widget.TextWidget;
import io.github.foecollab.screens.widget.container.ContainerButtonWidget;
import io.github.foecollab.screens.widget.container.ContainerButtonsWidget;
import io.github.foecollab.screens.widget.container.ContainerHeaderWidget;
import io.github.foecollab.screens.widget.container.ContainerSideWidget;
import io.github.foecollab.screens.widget.container.BoxRenderer;
import io.github.foecollab.screens.widget.container.ModernBoxWidget;
import io.github.foecollab.screens.widget.container.ModernButtonWidget;
import io.github.foecollab.screens.widget.timer.BaitShopTimerWidget;
import io.github.foecollab.screens.widget.timer.MoonTimerWidget;
import io.github.foecollab.screens.widget.timer.TimerWidget;
import io.github.foecollab.util.TextHelper;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.RecipeBookScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class InventoryScreenHandler {
    private static InventoryScreenHandler INSTANCE = new InventoryScreenHandler();
    private final FOEConfig config = FOEConfig.getConfig();

    public boolean screenInit = false;
    public boolean isRecipeBookOpen = false;

    private final int recipeTranslation = 77;
    private boolean isReset = true;
    private List<String> playerList = new ArrayList<>();

    public static InventoryScreenHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new InventoryScreenHandler();
        }
        return INSTANCE;
    }

    public void tick(MinecraftClient minecraftClient) {
        if (screenInit) {
            if(CrewHandler.instance().crewState == CrewHandler.CrewState.HASCREW) {
                playerList.clear();
                ProfileDataHandler.instance().profileData.crewMembers.forEach(uuid -> {
                    String displayName = TabHandler.instance().getPlayer(uuid);
                    if(displayName != null) {
                        playerList.add(displayName);
                    }
                });
            }

            this.createButtonMenu(minecraftClient);
            this.createCrewMenu(minecraftClient);
            this.createHeader(minecraftClient);
            this.createTimerMenu(minecraftClient);

            this.screenInit = false;
        }

        if(minecraftClient.currentScreen != null) {
            if(minecraftClient.currentScreen instanceof RecipeBookScreen<?> recipeBookScreen) {
                if(isRecipeBookOpen != ((RecipeBookScreenAccessor) recipeBookScreen).getRecipeBook().isOpen()) {
                    isRecipeBookOpen = ((RecipeBookScreenAccessor) recipeBookScreen).getRecipeBook().isOpen();

                    // Update Buttons
                    this.resetButtons(minecraftClient);
                }
            }
        }

        if(!isReset) {
            isReset = true;
            this.resetButtons(minecraftClient);
        }
    }

    private void createHeader(MinecraftClient minecraftClient) {
        if(minecraftClient.currentScreen != null) {
            int height = 83;
            List<ClickableWidget> clickableWidgets = new ArrayList<>();

            Text header = TabHandler.instance().player;
            // Panel first so it renders behind the header text (z layering is gone in 1.21.11).
            clickableWidgets.add(new ContainerHeaderWidget(minecraftClient.getWindow().getScaledWidth() / 2 - 174 / 2,  minecraftClient.getWindow().getScaledHeight() / 2 - height - 28, Text.empty()));

            clickableWidgets.add(new TextWidget(minecraftClient.getWindow().getScaledWidth() / 2 - MinecraftClient.getInstance().textRenderer.getWidth(header) / 2, minecraftClient.getWindow().getScaledHeight() / 2 - height - 28 / 2 - MinecraftClient.getInstance().textRenderer.fontHeight / 2, header, 0xFFFFFFFF, true));

            Screens.getButtons(minecraftClient.currentScreen).addAll(clickableWidgets);
        }
    }

    private void resetButtons(MinecraftClient minecraftClient) {
        if (minecraftClient.currentScreen != null) {
            Screens.getButtons(minecraftClient.currentScreen).removeIf(clickableWidget -> clickableWidget instanceof ContainerButtonWidget || clickableWidget instanceof ContainerButtonsWidget || clickableWidget instanceof ContainerSideWidget || clickableWidget instanceof TextWidget || clickableWidget instanceof ContainerHeaderWidget || clickableWidget instanceof TimerWidget || clickableWidget instanceof ModernButtonWidget || clickableWidget instanceof ModernBoxWidget);
        }
        this.createButtonMenu(minecraftClient);
        this.createCrewMenu(minecraftClient);
        this.createHeader(minecraftClient);
        this.createTimerMenu(minecraftClient);
    }

    private void createCrewMenu(MinecraftClient minecraftClient) {
        if (CrewHandler.instance().crewState == CrewHandler.CrewState.HASCREW) {
            if(minecraftClient.currentScreen != null && config.isCrewButtonMenuOpen) {
                int height = - 82;
                int buttonSize = 22 + 1;
                int offsetRecipe = isRecipeBookOpen ? recipeTranslation : 0;

                List<ClickableWidget> clickableWidgets = new ArrayList<>();

                // Panel first so it renders behind the crew buttons/list (z layering is gone in 1.21.11).
                clickableWidgets.add(new ContainerSideWidget(minecraftClient.getWindow().getScaledWidth() / 2 + 177 / 2 + offsetRecipe,  minecraftClient.getWindow().getScaledHeight() / 2 + height, Text.empty()));

                Text crew = TextHelper.concat(Text.literal(ScoreboardHandler.instance().crewName).formatted(Formatting.DARK_GREEN), Text.literal(" [").formatted(Formatting.DARK_GRAY), Text.literal(ScoreboardHandler.instance().crewLevel).withColor(LevelColors.valueOfLvl(Integer.parseInt(ScoreboardHandler.instance().crewLevel)).color), Text.literal("]").formatted(Formatting.DARK_GRAY));
                clickableWidgets.add(new TextWidget(minecraftClient.getWindow().getScaledWidth() / 2 + 177 / 2 + 105 / 2 - MinecraftClient.getInstance().textRenderer.getWidth(crew) / 2 + offsetRecipe, minecraftClient.getWindow().getScaledHeight() / 2 + height + 7 + buttonSize / 2 - MinecraftClient.getInstance().textRenderer.fontHeight / 2, crew, 0xFFFFFFFF, true));

                clickableWidgets.add(assembleCrewButton(-buttonSize * 1, buttonSize, Text.literal("\uF038"), "crew", Tooltip.of(
                        TextHelper.concat(
                                Text.literal("Crew Info\n").formatted(Formatting.BOLD, Formatting.WHITE),
                                Text.literal("Open crew menu of " + ScoreboardHandler.instance().crewName + ".").formatted(Formatting.GRAY, Formatting.ITALIC)
                        )), minecraftClient));
                clickableWidgets.add(assembleCrewButton(buttonSize * 0, buttonSize, Text.literal("\uF039"), "crew home", Tooltip.of(
                        TextHelper.concat(
                                Text.literal("Crew Home\n").formatted(Formatting.BOLD, Formatting.WHITE),
                                Text.literal("Go to your crew island.").formatted(Formatting.GRAY, Formatting.ITALIC)
                        )), minecraftClient));
                clickableWidgets.add(assembleCrewButton(buttonSize * 1, buttonSize, Text.literal("ab"), "crew chat", Tooltip.of(
                        TextHelper.concat(
                                Text.literal("Crew Chat\n").formatted(Formatting.BOLD, Formatting.WHITE),
                                Text.literal("Toggles crew chat on and off.").formatted(Formatting.GRAY, Formatting.ITALIC)
                        )), minecraftClient));

                if(BossBarHandler.instance().currentLocation == Constant.CREW_ISLAND) {
                    clickableWidgets.add(assembleCrewButton(buttonSize * 2, buttonSize, Text.literal("↑"), "crew fly", Tooltip.of(
                            TextHelper.concat(
                                    Text.literal("Crew Fly\n").formatted(Formatting.BOLD, Formatting.WHITE),
                                    Text.literal("Toggle crew fly on and off.").formatted(Formatting.GRAY, Formatting.ITALIC)
                            )), minecraftClient));
                }

                clickableWidgets.addAll(assembleCrewList(minecraftClient.getWindow().getScaledWidth() / 2 + 177 / 2 + 105 /2 + offsetRecipe, minecraftClient.getWindow().getScaledHeight() / 2 - 82 + buttonSize * 3, minecraftClient));

                clickableWidgets.add(new ContainerButtonWidget(minecraftClient.getWindow().getScaledWidth() / 2 + 177 / 2 + offsetRecipe + 105, minecraftClient.getWindow().getScaledHeight() / 2 + height + 1, Text.literal("←"), Tooltip.of(
                        Text.literal("Close Crew Menu").formatted(Formatting.BOLD, Formatting.WHITE)
                ), button -> {
                    config.isCrewButtonMenuOpen = false;
                    AutoConfig.getConfigHolder(FOEConfig.class).save();
                    this.resetButtons(minecraftClient);
                }));

                Screens.getButtons(minecraftClient.currentScreen).addAll(clickableWidgets);
            } else if(minecraftClient.currentScreen != null) {
                int offsetRecipe = isRecipeBookOpen ? recipeTranslation : 0;

                List<ClickableWidget> clickableWidgets = new ArrayList<>();

                clickableWidgets.add(new ContainerButtonWidget(minecraftClient.getWindow().getScaledWidth() / 2 + 177 / 2 + offsetRecipe + 1, minecraftClient.getWindow().getScaledHeight() / 2 - 22 / 2, Text.literal("→"), Tooltip.of(
                        Text.literal("Open Crew Menu").formatted(Formatting.BOLD, Formatting.WHITE)
                ), button -> {
                    config.isCrewButtonMenuOpen = true;
                    AutoConfig.getConfigHolder(FOEConfig.class).save();
                    this.resetButtons(minecraftClient);
                }));

                Screens.getButtons(minecraftClient.currentScreen).addAll(clickableWidgets);
            }
        }
    }

    private void createButtonMenu(MinecraftClient minecraftClient) {
        if (minecraftClient.currentScreen == null) {
            return;
        }
        createModernButtonMenu(minecraftClient);
    }

    private void createTimerMenu(MinecraftClient minecraftClient) {
        if(minecraftClient.currentScreen != null && config.isTimerButtonMenuOpen) {
            int offsetRecipe = isRecipeBookOpen ? recipeTranslation : 0;
            int height = - 82;
            int buttonSize = 22 + 1;

            List<ClickableWidget> clickableWidgets = new ArrayList<>();

            // Panel first so it renders behind the timer widgets/text (z layering is gone in 1.21.11).
            clickableWidgets.add(new ContainerSideWidget(minecraftClient.getWindow().getScaledWidth() / 2 - 177 / 2 - offsetRecipe - 105,  minecraftClient.getWindow().getScaledHeight() / 2 + height, Text.empty()));

            Text title = Text.literal("Timers").formatted(Formatting.DARK_GREEN);
            clickableWidgets.add(new TextWidget(minecraftClient.getWindow().getScaledWidth() / 2 - 177 / 2 - 105 / 2 - MinecraftClient.getInstance().textRenderer.getWidth(title) / 2 - offsetRecipe, minecraftClient.getWindow().getScaledHeight() / 2 + height + 7 + buttonSize / 2 - MinecraftClient.getInstance().textRenderer.fontHeight / 2, title, 0xFFFFFFFF, true));

            Text lineText = Text.literal("─────────").formatted(Formatting.DARK_GRAY);
            // Line
            clickableWidgets.add(new TextWidget(minecraftClient.getWindow().getScaledWidth() / 2 - 177 / 2 - 105 / 2 - MinecraftClient.getInstance().textRenderer.getWidth(lineText) / 2 - offsetRecipe, minecraftClient.getWindow().getScaledHeight() / 2 + height - 4 + buttonSize * 2 - MinecraftClient.getInstance().textRenderer.fontHeight * 2, lineText, 0xFFFFFFFF, true));

            Text baitShop = Text.literal("Tackle Shop").formatted(Formatting.WHITE);
            clickableWidgets.add(new TextWidget(minecraftClient.getWindow().getScaledWidth() / 2 - 177 / 2 - 105 / 2 - MinecraftClient.getInstance().textRenderer.getWidth(baitShop) / 2 - offsetRecipe, minecraftClient.getWindow().getScaledHeight() / 2 + height - 4 + buttonSize * 2 - MinecraftClient.getInstance().textRenderer.fontHeight, baitShop, 0xFFFFFFFF, true));

            if(config.timerTracker.baitShopNotification) {
                clickableWidgets.add(new ContainerButtonWidget(minecraftClient.getWindow().getScaledWidth() / 2 - 177 / 2 - 82/2 - 22/2 - (23 * 2) - offsetRecipe, minecraftClient.getWindow().getScaledHeight() / 2 + height - 4 + buttonSize * 2, Text.literal("✔").formatted(Formatting.GREEN), Tooltip.of(
                        TextHelper.concat(
                                Text.literal("Currently ").formatted(Formatting.WHITE),
                                Text.literal("Enabled\n").formatted(Formatting.GREEN),
                                Text.literal("When enabled, it will show a notification when tackle shop is restocked").formatted(Formatting.GRAY, Formatting.ITALIC)
                        )
                ), button -> {
                    config.timerTracker.baitShopNotification = false;
                    AutoConfig.getConfigHolder(FOEConfig.class).save();
                    this.resetButtons(minecraftClient);
                }));
            } else {
                clickableWidgets.add(new ContainerButtonWidget(minecraftClient.getWindow().getScaledWidth() / 2 - 177 / 2 - 82/2 - 22/2 - (23 * 2) - offsetRecipe, minecraftClient.getWindow().getScaledHeight() / 2 + height - 4 + buttonSize * 2, Text.literal("❌").formatted(Formatting.RED), Tooltip.of(
                        TextHelper.concat(
                                Text.literal("Currently ").formatted(Formatting.WHITE),
                                Text.literal("Disabled\n").formatted(Formatting.RED),
                                Text.literal("When enabled, it will show a notification when tackle shop is restocked").formatted(Formatting.GRAY, Formatting.ITALIC)
                        )
                ), button -> {
                    config.timerTracker.baitShopNotification = true;
                    AutoConfig.getConfigHolder(FOEConfig.class).save();
                    this.resetButtons(minecraftClient);
                }));
            }

            clickableWidgets.add(new BaitShopTimerWidget(minecraftClient.getWindow().getScaledWidth() / 2 - 177 / 2 - 22 / 2 - offsetRecipe, minecraftClient.getWindow().getScaledHeight() / 2 + height - 2 + buttonSize * 2));

            // Moon Timer section
            if(config.timerTracker.showMoonTimerWidget) {
                Text moonTimer = Text.literal("Moon Cycle").formatted(Formatting.WHITE);
                clickableWidgets.add(new TextWidget(minecraftClient.getWindow().getScaledWidth() / 2 - 177 / 2 - 105 / 2 - MinecraftClient.getInstance().textRenderer.getWidth(moonTimer) / 2 - offsetRecipe, minecraftClient.getWindow().getScaledHeight() / 2 + height - 4 + buttonSize * 3 + buttonSize / 2 - MinecraftClient.getInstance().textRenderer.fontHeight, moonTimer, 0xFFFFFFFF, true));

                // Moon alerts mute toggle
                if(config.eventTracker.weatherEventOptions.muteMoonAlerts) {
                    clickableWidgets.add(new ContainerButtonWidget(minecraftClient.getWindow().getScaledWidth() / 2 - 177 / 2 - 82/2 - 22/2 - (23 * 2) - offsetRecipe, minecraftClient.getWindow().getScaledHeight() / 2 + height - 4 + buttonSize * 3 + buttonSize / 2, Text.literal("🔕").formatted(Formatting.RED), Tooltip.of(
                            TextHelper.concat(
                                    Text.literal("Moon alerts ").formatted(Formatting.WHITE),
                                    Text.literal("Muted\n").formatted(Formatting.RED),
                                    Text.literal("Unmute to use config toggles").formatted(Formatting.GRAY, Formatting.ITALIC)
                            )
                    ), button -> {
                        config.eventTracker.weatherEventOptions.muteMoonAlerts = false;
                        AutoConfig.getConfigHolder(FOEConfig.class).save();
                        this.resetButtons(minecraftClient);
                    }));
                } else {
                    clickableWidgets.add(new ContainerButtonWidget(minecraftClient.getWindow().getScaledWidth() / 2 - 177 / 2 - 82/2 - 22/2 - (23 * 2) - offsetRecipe, minecraftClient.getWindow().getScaledHeight() / 2 + height - 4 + buttonSize * 3 + buttonSize / 2, Text.literal("🔔").formatted(Formatting.GREEN), Tooltip.of(
                            TextHelper.concat(
                                    Text.literal("Moon alerts ").formatted(Formatting.WHITE),
                                    Text.literal("Using Config\n").formatted(Formatting.GREEN),
                                    Text.literal("Click to mute all moon alerts").formatted(Formatting.GRAY, Formatting.ITALIC)
                            )
                    ), button -> {
                        config.eventTracker.weatherEventOptions.muteMoonAlerts = true;
                        AutoConfig.getConfigHolder(FOEConfig.class).save();
                        this.resetButtons(minecraftClient);
                    }));
                }

                clickableWidgets.add(new MoonTimerWidget(minecraftClient.getWindow().getScaledWidth() / 2 - 177 / 2 - 22 / 2 - offsetRecipe, minecraftClient.getWindow().getScaledHeight() / 2 + height - 2 + buttonSize * 3 + buttonSize / 2));
            }

            clickableWidgets.add(new ContainerButtonWidget(minecraftClient.getWindow().getScaledWidth() / 2 - 177 / 2 - offsetRecipe - 105 - 22, minecraftClient.getWindow().getScaledHeight() / 2 + height + 1, Text.literal("→"), Tooltip.of(
                    Text.literal("Close Timer Menu").formatted(Formatting.BOLD, Formatting.WHITE)
            ), button -> {
                config.isTimerButtonMenuOpen = false;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
                this.resetButtons(minecraftClient);
            }));

            Screens.getButtons(minecraftClient.currentScreen).addAll(clickableWidgets);
        } else if (minecraftClient.currentScreen != null) {
            int offsetRecipe = isRecipeBookOpen ? recipeTranslation : 0;
            List<ClickableWidget> clickableWidgets = new ArrayList<>();

            clickableWidgets.add(new ContainerButtonWidget(minecraftClient.getWindow().getScaledWidth() / 2 - 177 / 2 - offsetRecipe - 1 - 22, minecraftClient.getWindow().getScaledHeight() / 2 - 22 / 2, Text.literal("←"), Tooltip.of(
                    Text.literal("Open Timer Menu").formatted(Formatting.BOLD, Formatting.WHITE)
            ), button -> {
                config.isTimerButtonMenuOpen = true;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
                this.resetButtons(minecraftClient);
            }));

            Screens.getButtons(minecraftClient.currentScreen).addAll(clickableWidgets);
        }
    }

    private ClickableWidget assembleCrewButton(int x, int y, Text icon, String command, @Nullable Tooltip tooltip, MinecraftClient minecraftClient) {
        int offsetRecipe = isRecipeBookOpen ? recipeTranslation : 0;

        return new ContainerButtonWidget(minecraftClient.getWindow().getScaledWidth() / 2 + 177 / 2 + 82 / 2 - 22 / 2 + x + offsetRecipe, minecraftClient.getWindow().getScaledHeight() / 2 - 82 + 7 + y, icon, tooltip, button -> {
            if (minecraftClient.player != null) {
                minecraftClient.player.networkHandler.sendChatCommand(command);
            }
        });
    }

    private List<ClickableWidget> assembleCrewList(int x, int y, MinecraftClient minecraftClient) {
        int lineHeight = MinecraftClient.getInstance().textRenderer.fontHeight + 1;
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        List<ClickableWidget> clickableWidgets = new ArrayList<>();

        Text onlineText = Text.literal("ᴏɴʟɪɴᴇ").formatted(Formatting.GRAY, Formatting.ITALIC);
        clickableWidgets.add(new TextWidget(x - textRenderer.getWidth(onlineText) / 2, y - lineHeight * 2 + 5, onlineText, 0xFFFFFFFF, true));
        Text lineText = Text.literal("─────────").formatted(Formatting.DARK_GRAY);
        clickableWidgets.add(new TextWidget(x - textRenderer.getWidth(lineText) / 2, y - lineHeight + 2, lineText, 0xFFFFFFFF, true));

        for (int i = 0; i < playerList.size(); i++) {
            String player = StringUtils.abbreviate(playerList.get(i), 16);
            int finalI = i;
            TextWidget playerWidget = new TextWidget(x - textRenderer.getWidth(player) / 2, y + lineHeight * i, Text.literal(player).formatted(Formatting.GREEN), 0xFFFFFFFF, true, iconButtonWidget -> {
                if (minecraftClient.player != null) {
                    minecraftClient.player.networkHandler.sendChatCommand("find " + playerList.get(finalI));
                }
            });
            playerWidget.setTooltip(Tooltip.of(Text.literal("/find " + playerList.get(i))));
            clickableWidgets.add(playerWidget);
        }

        return clickableWidgets;
    }

    private void createModernButtonMenu(MinecraftClient minecraftClient) {
        int centerX = minecraftClient.getWindow().getScaledWidth() / 2;
        int centerY = minecraftClient.getWindow().getScaledHeight() / 2;

        List<ClickableWidget> clickableWidgets = new ArrayList<>();

        if (config.isButtonMenuOpen) {
            int perRow = 8;
            int spacing = 20;
            int buttonSize = 18;
            java.util.List<InventoryButtonHandler.CustomButton> all = InventoryButtonHandler.instance().getButtons();
            int rows = Math.max(1, (all.size() + perRow - 1) / perRow);

            int panelW = 170;
            int panelX = centerX - panelW / 2;
            int panelTop = centerY + 78;
            int panelH = 11 + rows * spacing;

            // Panel first so it renders behind the buttons (z layering is gone in 1.21.11).
            clickableWidgets.add(new ModernBoxWidget(panelX, panelTop, panelW, panelH, BoxRenderer.BOX));

            int gridLeft = centerX - (perRow * spacing) / 2 + (spacing - buttonSize) / 2;
            int gridTop = panelTop + 6;
            // Position by full-list index and skip hidden buttons, so hidden "spacer" entries leave
            // gaps in the grid — matching FOE-R's arrangement.
            for (int i = 0; i < all.size(); i++) {
                InventoryButtonHandler.CustomButton b = all.get(i);
                if (!b.showButton) {
                    continue;
                }
                int bx = gridLeft + (i % perRow) * spacing;
                int by = gridTop + (i / perRow) * spacing;
                clickableWidgets.add(assembleModernButton(b, bx, by, buttonSize, minecraftClient));
            }

            int toggleY = panelTop + panelH + 1;
            clickableWidgets.add(new ModernButtonWidget(centerX - buttonSize - 1, toggleY, buttonSize, buttonSize, Text.literal("✎").formatted(Formatting.GREEN), Tooltip.of(
                    Text.literal("Edit Buttons").formatted(Formatting.BOLD, Formatting.WHITE)
            ), null, button -> minecraftClient.setScreen(new CustomButtonMakerScreen(minecraftClient.currentScreen))));
            clickableWidgets.add(new ModernButtonWidget(centerX + 1, toggleY, buttonSize, buttonSize, Text.literal("↑"), Tooltip.of(
                    Text.literal("Close Button Menu").formatted(Formatting.BOLD, Formatting.WHITE)
            ), null, button -> {
                config.isButtonMenuOpen = false;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
                this.resetButtons(minecraftClient);
            }));
        } else {
            clickableWidgets.add(new ModernButtonWidget(centerX - 9, centerY + 84, 18, 18, Text.literal("↓"), Tooltip.of(
                    Text.literal("Open Button Menu").formatted(Formatting.BOLD, Formatting.WHITE)
            ), null, button -> {
                config.isButtonMenuOpen = true;
                AutoConfig.getConfigHolder(FOEConfig.class).save();
                this.resetButtons(minecraftClient);
            }));
        }

        Screens.getButtons(minecraftClient.currentScreen).addAll(clickableWidgets);
    }

    private ClickableWidget assembleModernButton(InventoryButtonHandler.CustomButton button, int x, int y, int size, MinecraftClient minecraftClient) {
        String displayName = button.name == null ? "" : button.name.replace('&', '§');
        Tooltip tooltip;
        if (button.description != null && !button.description.isBlank()) {
            tooltip = Tooltip.of(TextHelper.concat(
                    Text.literal(displayName).formatted(Formatting.BOLD, Formatting.WHITE),
                    Text.literal("\n"),
                    Text.literal(button.description.replace('&', '§')).formatted(Formatting.GRAY, Formatting.ITALIC)
            ));
        } else {
            tooltip = Tooltip.of(Text.literal(displayName).formatted(Formatting.BOLD, Formatting.WHITE));
        }

        ItemStack itemIcon = InventoryButtonHandler.iconItem(button.icon);
        Text iconText = Text.literal(button.icon == null ? "" : button.icon.replace('&', '§'));
        String command = button.action == null ? "" : button.action;
        String finalCommand = command.startsWith("/") ? command.substring(1) : command;

        return new ModernButtonWidget(x, y, size, size, iconText, tooltip, itemIcon, b -> {
            if (minecraftClient.player != null && !finalCommand.isBlank()) {
                minecraftClient.player.networkHandler.sendChatCommand(finalCommand);
            }
        });
    }

}
