package io.github.foecollab;

import io.github.foecollab.commands.CommandRegistry;
import io.github.foecollab.config.ConfigConstants;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.handler.*;
import io.github.foecollab.handler.packet.PacketHandler;
import io.github.foecollab.util.LocationNameHelper;
import io.github.foecollab.util.SimpleTagFont;
import io.github.foecollab.screens.hud.ChummerRangeRenderer;
import io.github.foecollab.screens.hud.MainHudRenderer;
import io.github.foecollab.screens.main.FoETitleScreen;
import io.github.foecollab.screens.petCalculator.PetCalculatorScreen;
import io.github.foecollab.screens.widget.IconButtonWidget;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Objects;

public class FishOnMCExtrasClient implements ClientModInitializer {
    public static FOEConfig CONFIG;
    private Text buffer = null;
    private boolean shouldFilter = false;

    public static final MainHudRenderer MAIN_HUD_RENDERER = new MainHudRenderer();

    @Override
    public void onInitializeClient() {
        // Setup config screen, reads correct data to load IMPORTANT MUST BE FIRST
        AutoConfig.register(FOEConfig.class, GsonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(FOEConfig.class).getConfig();
        CommandRegistry.initialize();
        // Before MinecraftClient scans resource packs: mirror the cached server packs so they
        // load at startup (the join-time pack push is then skipped, see ServerPackHandler).
        ServerPackHandler.instance().extract();
        KeybindHandler.instance().init();
        PacketHandler.instance().addHandlers();
        ChummerRangeRenderer.register();

        ClientPlayConnectionEvents.JOIN.register(this::onJoin);
        ClientPlayConnectionEvents.DISCONNECT.register(this::onLeave);
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
        ClientReceiveMessageEvents.ALLOW_GAME.register(this::allowGameMessage);
        ClientReceiveMessageEvents.MODIFY_GAME.register(this::modifyGameMessage);
        ItemTooltipCallback.EVENT.register(this::onItemTooltipCallback);
        ScreenEvents.BEFORE_INIT.register(this::beforeScreenInit);
        ScreenEvents.AFTER_INIT.register(this::afterScreenInit);
        ClientEntityEvents.ENTITY_LOAD.register(this::onEntityLoad);

        // Attach just below the vanilla player list so holding tab layers it over our HUDs
        // instead of hiding them.
        HudElementRegistry.attachElementBefore(VanillaHudElements.PLAYER_LIST,
                Identifier.of("foecollab", "main_hud"), MAIN_HUD_RENDERER);
    }

    private void onEntityLoad(Entity entity, ClientWorld clientWorld) {
    }

    private void onEndClientTick(MinecraftClient minecraftClient) {
        if(minecraftClient.getCurrentServerEntry() != null ) {
            if(LoadingHandler.instance().checkAddress(minecraftClient) && LoadingHandler.instance().isOnServer) {
                LoadingHandler.instance().tick(minecraftClient);
                if(LoadingHandler.instance().isLoadingDone) {
                    WorldHandler.instance().tick(minecraftClient);
                    FishCatchHandler.instance().tick(minecraftClient);
                    PetEquipHandler.instance().tick(minecraftClient);
                    ChummerHandler.instance().tick(minecraftClient);
                    FullInventoryHandler.instance().tick(minecraftClient);
                    NotificationSoundHandler.instance().tick(minecraftClient);
                    RayTracingHandler.instance().tick(minecraftClient);
                    LookTickHandler.instance().tick();
                    ScoreboardHandler.instance().tick(minecraftClient);
                    ContestHandler.instance().tick();
                    TabHandler.instance().tick(minecraftClient);
                    BossBarHandler.instance().tick(minecraftClient);
                    QuestHandler.instance().tick(minecraftClient);
                    DailyQuestHandler.instance().tick(minecraftClient);
                    ArmorHandler.instance().tick(minecraftClient);
                    FishingRodHandler.instance().tick(minecraftClient);
                    CrewHandler.instance().tick(minecraftClient);
                    StatsImportHandler.instance().tick(minecraftClient);
                    DiscordHandler.instance().tick();
                    KeybindHandler.instance().tick(minecraftClient);
                    InventoryScreenHandler.instance().tick(minecraftClient);
                    PersonalVaultScreenHandler.instance().tick(minecraftClient);
                    SearchBarContainerHandler.instance().tick(minecraftClient);
                    ThemingHandler.instance().tick();
                    OtherPlayerHandler.instance().tick(minecraftClient);
                    HiderHandler.instance().tick(minecraftClient);
                    OwnPlayerHandler.instance().tick(minecraftClient);
                    TimerHandler.instance().tick();
                    EventHandler.instance().onEventTick();
                }
            }
        }
        ProfileDataHandler.instance().tick();
    }

    private void onJoin(ClientPlayNetworkHandler clientPlayNetworkHandler, PacketSender packetSender, MinecraftClient minecraftClient) {
        LoadingHandler.instance().init();
        PetEquipHandler.instance().init();
        ChummerHandler.instance().clear();
        OtherPlayerHandler.instance().clear();
        NotificationSoundHandler.instance().init();
        DiscordHandler.instance().init();

        if(minecraftClient.getCurrentServerEntry() != null ) {
            if(LoadingHandler.instance().checkAddress(minecraftClient)) {
                FOECollab.LOGGER.info("[FoE] On server. (play.fishonmc.net)");
                FOECollab.LOGGER.info("[FoE] Loading Start");
                minecraftClient.execute(() -> {
                    if (minecraftClient.player != null) {
                        ProfileDataHandler.instance().onJoinServer(minecraftClient.player);
                        // Auto backup when player joins
                        if (CONFIG.statsBackup.backupOnServerJoin) {
                            ProfileDataHandler.instance().createBackup();
                        }
                        BaitSortingHelperHandler.instance().loadFromProfile();
                        FishCatchHandler.instance().onJoinServer();
                        CrewHandler.instance().onJoinServer();
                        DiscordHandler.instance().connect();
                        LoadingHandler.instance().isOnServer = true;
                        LoadingHandler.instance().wasOnServer = true;
                    }
                });
            } else {
                FOECollab.LOGGER.info("[FoE] Not on server. (play.fishonmc.net)");
                LoadingHandler.instance().isOnServer = false;
            }
        }
    }

    private boolean allowGameMessage(Text text, boolean overlay) {
        if(LoadingHandler.instance().isOnServer) {
            // Observes "... activated a ... Chummer" broadcasts; never suppresses the message.
            ChummerHandler.instance().onReceiveMessage(text);

            if (PetEquipHandler.instance().onReceiveMessage(text) ||
                ContestHandler.instance().onReceiveMessage(text) ||
                CrewHandler.instance().onReceiveMessage(text) ||
                FishCatchHandler.instance().onReceiveMessage(text) ||
                StaffHandler.instance().onReceiveMessage(text) ||
                TimerHandler.instance().onReceiveMessage(text) ||
                EventHandler.instance().onReceiveMessage(text) ||
                AutoTippingHandler.instance().onReceiveMessage(text) ||
                DailyQuestHandler.instance().onReceiveMessage(text) ||
                QuestHandler.instance().onReceiveMessage(text)) {
                FOECollab.LOGGER.info("[FoE] Suppressing message: {}", text.getString());
                return false; // Return false to completely suppress the message
            }

            String raw = text.getString();

            // Revamped version of Gladionus' chat filter
            if (CONFIG.chatconfig.chatFilter.enabled) {
                if (shouldFilter && raw.isBlank()) {
                    shouldFilter = false;
                    return false;
                }
                shouldFilter = false;

                if (isChatFilterTarget(text)) {
                    FOECollab.LOGGER.info("[FoE] Suppressing chat filter message: {}", text.getString());
                    if (buffer != null) {
                        buffer = null;
                    }
                    shouldFilter = true;
                    return false;
                }

                if (raw.isBlank()) {
                    buffer = text;
                    return false;
                }

                if (buffer != null) {
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(buffer);
                    buffer = null;
                }
            } else {
                if (buffer != null) {
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(buffer);
                    buffer = null;
                }
                shouldFilter = false;
            }
        }
        return true;
    }

    private boolean isChatFilterTarget(Text text) {
        if (text == null) {
            return false;
        }
        String raw = text.getString();
        if (raw == null) {
            return false;
        }
        return raw.contains("DESIGN TEAM »")
            || raw.contains("HELP »")
            || raw.contains("WIKI »")
            || raw.contains("RULES »")
            || raw.contains("BUILD TEAM »")
            || raw.contains("COSMETICS »")
            || raw.contains("STORE »")
            || raw.contains("DISCORD »");
    }

    private Text modifyGameMessage(Text text, boolean overlay) {
        if(LoadingHandler.instance().isOnServer) {
            // Apply modifications to messages that are allowed
            text = ContestHandler.instance().modifyMessage(text);
            text = ChatScreenHandler.instance().appendTooltip(text);
            text = ChatTagHandler.instance().displayTags(text);
            text = ChatTagHandler.instance().changePetTags(text);
            // Only rewrite location names on the server's own LOCATION / LOCATIONS messages.
            // Other messages (e.g. REACTIONS) can legitimately contain a full location name
            // that must stay as-is, so we leave anything that doesn't start with LOCATION alone.
            if (CONFIG.cleanerDisplay.shortenLocationNames
                    && text.getString().stripLeading().startsWith("LOCATION")) {
                text = LocationNameHelper.shorten(text);
            }
            if (CONFIG.cleanerDisplay.simpleRankTags || CONFIG.cleanerDisplay.simpleRarityTags) {
                text = SimpleTagFont.apply(text, CONFIG.cleanerDisplay.simpleRankTags, CONFIG.cleanerDisplay.simpleRarityTags);
            }
        }
        return text;
    }

    private void onLeave(ClientPlayNetworkHandler clientPlayNetworkHandler, MinecraftClient minecraftClient) {
        LoadingHandler.instance().init();
        ChummerHandler.instance().clear();
        FishCatchHandler.instance().onLeaveServer();
        ContestHandler.instance().onLeaveServer();
        LoadingHandler.instance().isOnServer = false;
        DiscordHandler.instance().disconnect();
//        WeatherHandler.instance().onLeaveServer();
    }

    private void onItemTooltipCallback(ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipType tooltipType, List<Text> textList) {
        if(LoadingHandler.instance().isOnServer) {
            ArmorHandler.instance().appendTooltip(textList, itemStack);
            FishingStatsHandler.instance().appendTooltip(textList, itemStack);
            AuctionHandler.instance().appendTooltip(textList, itemStack);
            // Must run last: it edits lines, which would break the fixed
            // line indices the handlers above rely on.
            TooltipShortenerHandler.instance().cleanTooltip(textList);
        }
    }

    private void afterScreenInit(MinecraftClient minecraftClient, Screen screen, int scaledWidth, int scaledHeight) {
        if(LoadingHandler.instance().isOnServer) {
            if(Objects.equals(screen.getTitle().getString(), "Pet Menu\uEEE6\uEEE5\uEEE3핑")) {
//                 Pet Menu핑
                Screens.getButtons(screen).add(IconButtonWidget.builder(Text.literal("Pet Merge Calculator"), button ->
                                minecraftClient.setScreen(new PetCalculatorScreen(minecraftClient.player, minecraftClient.currentScreen)))
                        .position(scaledWidth / 2 + 100, scaledHeight / 2 - 100)
                        .tooltip(Tooltip.of(Text.literal("Open up the screen to calculate pet merging.")))
                        .itemIcon(Items.TURTLE_EGG.getDefaultStack())
                        .build());
            } else if (Objects.equals(screen.getTitle().getString(), "\uEEE4픹")) {
                // Quest Menu : 픹
                QuestHandler.instance().questMenuState = true;
            } else if(Objects.equals(screen.getTitle().getString() , "\uEEE4핒")) {
                // Crew Menu: 핒 (shares title with Leveling Bonuses)
                CrewHandler.instance().crewMenuState = true;
            } else if (Objects.equals(screen.getTitle().getString() , "\uEEE4픲")) {
                // Stats Menu: 픲
                StatsImportHandler.instance().screenInit = true;
                StatsImportHandler.instance().isOnScreen = true;
            } else if (screen instanceof InventoryScreen) {
                InventoryScreenHandler.instance().screenInit = true;
            } else if (screen instanceof ChatScreen || Objects.equals(screen.getTitle().getString(), "Chat screen")) {
                ChatScreenHandler.instance().screenInit = true;
            } else if (screen.getTitle().getString().contains("Personal Vault ")) {
                PersonalVaultScreenHandler.instance().page = Integer.parseInt(screen.getTitle().getString().substring(screen.getTitle().getString().length() - 1));
                PersonalVaultScreenHandler.instance().personalVaultMenuState = true;
            } else if (Objects.equals(screen.getTitle().getString(), "Tackle Shop\uEEE7\uEEE3합")) {
                // Tackle Shop
                AuctionHandler.instance().tackleShopMenuState = true;
            } else if (Objects.equals(screen.getTitle().getString(), "\uEEE4할")) {
                // Main Menu : 할
                DailyQuestHandler.instance().questMenuState = true;
            } else if (Objects.equals(screen.getTitle().getString(), "Armor Menu\uEEE7\uEEE2핈")) {
                // Armor Menu : Armor Menu핈
            }

            if((screen.getTitle().getString().isBlank() || screen.getTitle().getString().contains("Personal Vault ")) && screen instanceof GenericContainerScreen) {
                SearchBarContainerHandler.instance().containerMenuState = true;
            } else if (SearchBarContainerHandler.instance().searchBar != null
                     && (!screen.getTitle().getString().isBlank()
                    || screen instanceof RecipeBookScreen
                    || screen instanceof StonecutterScreen
                    || screen instanceof AbstractFurnaceScreen)
            ) {
                SearchBarContainerHandler.instance().searchBar.setText("");
            }

            if (ConfigConstants.DEV) {
                DebugHelperHandler.instance().logCurrentScreen();
            }
        }
        ScreenEvents.remove(screen).register(this::onRemoveScreen);
    }

    private void beforeScreenInit(MinecraftClient minecraftClient, Screen screen, int scaledWidth, int scaledHeight) {
        if(CONFIG.fun.useCustomTitleScreen
                && screen instanceof TitleScreen) {
            minecraftClient.setScreen(new FoETitleScreen());
        }
    }

    private void onRemoveScreen(Screen screen) {
        if(LoadingHandler.instance().isOnServer) {
            if (Objects.equals(screen.getTitle().getString(), "\uEEE4픹")) {
                // Quest Menu : 픹
                QuestHandler.instance().questMenuState = false;
                QuestHandler.instance().onScreenClose();
            } else if(Objects.equals(screen.getTitle().getString() , "\uEEE4핒")) {
                // Crew Menu:
                CrewHandler.instance().crewMenuState = false;
                CrewHandler.instance().onScreenClose();
            } else if(screen instanceof ChatScreen || Objects.equals(screen.getTitle().getString(), "")) {
                ChatScreenHandler.instance().screenInit = false;
            } else if (Objects.equals(screen.getTitle().getString(), "Tackle Shop\uEEE7\uEEE3합")) {
                // Tackle Shop
                AuctionHandler.instance().tackleShopMenuState = false;
            } else if (Objects.equals(screen.getTitle().getString(), "\uEEE4할")) {
                // Main Menu : 할
                DailyQuestHandler.instance().questMenuState = false;
            } else if (Objects.equals(screen.getTitle().getString(), "Armor Menu\uEEE7\uEEE2핈")) {
                // Armor Menu : Armor Menu핈
            }

            if ((screen.getTitle().getString().isBlank() || screen.getTitle().getString().contains("Personal Vault ")) && screen instanceof GenericContainerScreen) {
                SearchBarContainerHandler.instance().searchBar.setFocused(false);
            }
        }
    }
}