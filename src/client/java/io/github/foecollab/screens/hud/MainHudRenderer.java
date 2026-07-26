package io.github.foecollab.screens.hud;

import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.config.ConfigConstants;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.handler.BossBarHandler;
import io.github.foecollab.handler.LoadingHandler;
import io.github.foecollab.handler.LookTickHandler;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/// Registered via HudElementRegistry just before the vanilla player list, so all mod HUDs keep
/// rendering while tab is held and the player list simply layers on top of them.
public class MainHudRenderer implements HudElement {
    final FishTrackerHud fishTrackerHud = new FishTrackerHud();
    final PetEquipHud petEquipHud = new PetEquipHud();
    final NotificationHud notificationHud = new NotificationHud();
    final TitleHud titleHud = new TitleHud();
    final BiteTitleHud biteTitleHud = new BiteTitleHud();
    final BobberTimerHud bobberTimerHud = new BobberTimerHud();
    final ItemFrameTooltipHud itemFrameTooltipHud = new ItemFrameTooltipHud();
    final CustomHudRenderer customHudRenderer = new CustomHudRenderer();
    final BarHud barHud = new BarHud();
    final LevelHud levelHud = new LevelHud();
    final ContestHud contestHud = new ContestHud();
    final BaitHud baitHud = new BaitHud();
    final ChummerHud chummerHud = new ChummerHud();
    final EquipmentHud equipmentHud = new EquipmentHud();
    final CrewHud crewHud = new CrewHud();
    final QuestHud questHud = new QuestHud();
    final DailyQuestHud dailyQuestHud = new DailyQuestHud();

    @Override
    public void render(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        FOEConfig config = FOEConfig.getConfig();
        if(!MinecraftClient.getInstance().options.hudHidden && LoadingHandler.instance().isOnServer && LoadingHandler.instance().isLoadingDone) {
            this.notificationHud.render(drawContext, MinecraftClient.getInstance());

            if(!config.fun.immersionMode) {
                if(config.titlePopup.useNewTitleSystem) {
                    this.titleHud.render(drawContext, MinecraftClient.getInstance());
                }

                if(config.biteTitle.enabled) {
                    this.biteTitleHud.render(drawContext, MinecraftClient.getInstance());
                }

                // When merged, BiteTitleHud draws the timer in its slot instead.
                if(!(config.biteTitle.enabled && config.biteTitle.mergeWithTimer)) {
                    this.bobberTimerHud.render(drawContext, MinecraftClient.getInstance());
                }

                if(config.itemFrameTooltip.showTooltip) {
                    this.itemFrameTooltipHud.render(drawContext, MinecraftClient.getInstance());
                }

                if(config.customHuds) {
                    this.customHudRenderer.render(drawContext, MinecraftClient.getInstance());
                }

                if(config.barHUD.showBar) {
                    this.barHud.render(drawContext, MinecraftClient.getInstance());
                } else if(config.barHUD.levelHud.showWhenBarHidden) {
                    this.levelHud.render(drawContext, MinecraftClient.getInstance());
                }

                if(BossBarHandler.instance().currentLocation != Constant.CREW_ISLAND) {

                    if(config.fishTracker.showFishTrackerHUD) {
                        this.fishTrackerHud.render(drawContext, MinecraftClient.getInstance());
                    }

                    if(config.petEquipTracker.showPetEquipTrackerHUD) {
                        this.petEquipHud.render(drawContext, MinecraftClient.getInstance());
                    }

                    if(config.contestTracker.showContest) {
                        this.contestHud.render(drawContext, MinecraftClient.getInstance());
                    }

                    if(config.baitTracker.showBaitHud) {
                        this.baitHud.render(drawContext, MinecraftClient.getInstance());
                    }

                    if(config.chummerTracker.showChummerHud) {
                        this.chummerHud.render(drawContext, MinecraftClient.getInstance());
                    }

                    if(config.equipmentTracker.showEquipmentHud) {
                        this.equipmentHud.render(drawContext, MinecraftClient.getInstance());
                    }

                    if(config.crewTracker.showCrewNearby) {
                        this.crewHud.render(drawContext, MinecraftClient.getInstance());
                    }

                    if(config.questTracker.showQuestHud) {
                        this.questHud.render(drawContext, MinecraftClient.getInstance());
                    }

                    if(config.dailyQuestTracker.showDailyQuestHud) {
                        this.dailyQuestHud.render(drawContext, MinecraftClient.getInstance());
                    }
                }

                // The item-frame tooltip is queued onto the DrawContext's deferred tooltipDrawer,
                // which is normally only flushed by Screen.renderWithTooltip (drawDeferredElements).
                // During normal play no screen flushes it, so it only appeared with chat/inventory
                // open. Flush here — after every HUD so the tooltip layers on top — so it shows
                // whenever you hover an item frame.
                if(config.itemFrameTooltip.showTooltip
                        && LookTickHandler.instance().targetedItemInItemFrame != null) {
                    drawContext.drawDeferredElements();
                }
            }

            // Dev
            if(ConfigConstants.DEV) {
                Text dev = Text.literal("Development version, do not distribute").formatted(Formatting.RED);
                drawContext.drawText(
                        MinecraftClient.getInstance().textRenderer, dev,
                        0,
                        MinecraftClient.getInstance().getWindow().getScaledHeight() - MinecraftClient.getInstance().textRenderer.fontHeight, 0xFFFFFFFF, true);
            }
        }
    }
}
