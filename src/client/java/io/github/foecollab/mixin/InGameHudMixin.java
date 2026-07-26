package io.github.foecollab.mixin;

import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.handler.BiteTitleHandler;
import io.github.foecollab.handler.FishCatchHandler;
import io.github.foecollab.handler.ItemMarkerHandler;
import io.github.foecollab.handler.LoadingHandler;
import io.github.foecollab.handler.NotificationSoundHandler;
import io.github.foecollab.handler.OwnPlayerHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.hud.bar.Bar;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow private @Nullable Text title;
    @Shadow private int titleRemainTicks;
    @Unique
    private final FOEConfig config = FOEConfig.getConfig();

    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", at = @At("HEAD"), cancellable = true)
    private void injectRenderScoreboardSidebar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if(config.scoreboardTracker.hideScoreboard && LoadingHandler.instance().isOnServer) {
            ci.cancel();
        }
    }

    @Inject(method = "renderTitleAndSubtitle", at = @At("HEAD"), cancellable = true)
    private void injectRenderTitleAndSubtitle(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if((LoadingHandler.instance().isOnServer
                && System.currentTimeMillis() - FishCatchHandler.instance().lastTimeUsedRod < 1000L
                && config.titlePopup.useNewTitleSystem)
                || config.fun.immersionMode
                || (
                        config.titlePopup.useNewTitleSystem
                                && config.fishTracker.fishTrackerToggles.otherToggles.useNewTitle
                                && this.title != null
                                && !this.title.getString().isEmpty()
                                && this.titleRemainTicks > 0
                                && (int) this.title.getString().charAt(0) > 0xE000
                                && (int) this.title.getString().charAt(0) < 0xE999
                   )
                || (
                        config.fun.biteBobber
                                && this.title != null
                                && !this.title.getString().isEmpty()
                                && this.titleRemainTicks > 0
                                && Objects.equals(this.title.getString(), "BITE!")
                )
                || (
                        config.biteTitle.enabled
                                && this.title != null
                                && !this.title.getString().isEmpty()
                                && this.titleRemainTicks > 0
                                && Objects.equals(this.title.getString(), "BITE!")
                )
        ) {
            ci.cancel();
        }
    }

    // 1.21.11: InGameHud no longer has renderExperienceBar/renderExperienceLevel.
    // The bar graphic moved to ExperienceBar#renderBar (see ExperienceBarMixin) and the
    // level number is drawn by the static Bar#drawExperienceLevel call inside renderMainHud.
    @Redirect(method = "renderMainHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/bar/Bar;drawExperienceLevel(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/font/TextRenderer;I)V"))
    private void redirectExperienceLevel(DrawContext context, TextRenderer textRenderer, int level) {
        // Hidden when the FoE top bar replaces it, OR when "hide hearts & hunger" is on (that toggle
        // hides the XP bar + level number too, even with the top bar off).
        if((config.barHUD.showBar || config.hideHealthAndHunger) && LoadingHandler.instance().isOnServer) {
            return;
        }
        Bar.drawExperienceLevel(context, textRenderer, level);
    }

    @Inject(method = "setTitle", at = @At("HEAD"))
    private void injectSetTitle(Text title, CallbackInfo ci) {
        if(LoadingHandler.instance().isOnServer) {
            FishCatchHandler.instance().catchTitle(title);
            if(config.biteTitle.enabled && title != null && Objects.equals(title.getString(), "BITE!")) {
                // The server re-sends "BITE!" while one fish is on the line, but a "BITE!" arriving
                // after the previous one already expired is a *new* fish biting the same cast —
                // re-arm so every bite pops the notification (and sound) again, not just the first.
                boolean sameBiteOngoing = this.title != null && this.titleRemainTicks > 0
                        && Objects.equals(this.title.getString(), "BITE!");
                if(!sameBiteOngoing) {
                    BiteTitleHandler.instance().reset();
                }
                if(BiteTitleHandler.instance().trigger() && config.biteTitle.playSound) {
                    NotificationSoundHandler.instance().playSoundWarning(config.biteTitle.soundType, MinecraftClient.getInstance(), config.biteTitle.volume / 100f);
                }
            }
        }
    }

    @Inject(method = "setSubtitle", at = @At("HEAD"))
    private void injectSetSubtitle(Text title, CallbackInfo ci) {
        if(LoadingHandler.instance().isOnServer) {
            FishCatchHandler.instance().catchSubtitle(title);
        }
    }

    @Inject(method = "renderOverlayMessage", at = @At("HEAD"), cancellable = true)
    private void injectRenderOverlayMessage(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if(LoadingHandler.instance().isOnServer && config.fun.minigameOnBobber) {
            ci.cancel();
        }
    }

    // Server GUIs report click results in the action bar (e.g. the sell menu posts each fish's
    // sale price). Besides being noise behind the menu, a fresh overlay reads as "reel-in
    // minigame running" to BobberTimerHud, hiding the bobber timer mid-wait. No overlay the
    // player cares about can start while a container screen is open (reeling needs the rod in
    // hand), so drop these instead of letting them set overlayRemaining.
    @Inject(method = "setOverlayMessage", at = @At("HEAD"), cancellable = true)
    private void injectSetOverlayMessage(Text message, boolean tinted, CallbackInfo ci) {
        if(LoadingHandler.instance().isOnServer
                && MinecraftClient.getInstance().currentScreen instanceof HandledScreen<?>) {
            ci.cancel();
        }
    }

    @Inject(method = "renderMainHud", at = @At("HEAD"), cancellable = true)
    private void injectRenderMainHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if(LoadingHandler.instance().isOnServer && config.fun.immersionMode && System.currentTimeMillis() - OwnPlayerHandler.instance().changedSlotTime > 5000L) {
            ci.cancel();
        }
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void injectRenderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if(LoadingHandler.instance().isOnServer && config.fun.immersionMode) {
            ci.cancel();
        }
    }

    @Inject(method = "renderHealthBar", at = @At("HEAD"), cancellable = true)
    private void injectRenderHealthBar(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
        if(config.hideHealthAndHunger && LoadingHandler.instance().isOnServer) {
            ci.cancel();
        }
    }

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    private void injectRenderFood(DrawContext context, PlayerEntity player, int top, int right, CallbackInfo ci) {
        if(config.hideHealthAndHunger && LoadingHandler.instance().isOnServer) {
            ci.cancel();
        }
    }

    @Inject(method = "renderAirBubbles", at = @At("HEAD"), cancellable = true)
    private void injectRenderAirBubbles(DrawContext context, PlayerEntity player, int heartCount, int top, int left, CallbackInfo ci) {
        if(config.hideHealthAndHunger && LoadingHandler.instance().isOnServer) {
            ci.cancel();
        }
    }

    // renderArmor is static in InGameHud, so this handler must be static too (and can't read the
    // instance `config` field — fetch the config directly). Same "hide hearts & hunger" toggle.
    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private static void injectRenderArmor(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, CallbackInfo ci) {
        if(FOEConfig.getConfig().hideHealthAndHunger && LoadingHandler.instance().isOnServer) {
            ci.cancel();
        }
    }

    // HEAD, not TAIL: the equipped-pet highlight must draw behind the hotbar item
    // (it used z=100 before 1.21.11 dropped the matrix z axis).
    @Inject(method = "renderHotbarItem", at = @At("HEAD"))
    private void injectDrawHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
        if(LoadingHandler.instance().isOnServer && LoadingHandler.instance().isLoadingDone) {
            ItemMarkerHandler.instance().renderHotBarSelectedPet(context, x, y, stack);
        }
    }

    // TAIL: the rarity / fish-size / pet / max-stat / armor-quality markers draw on TOP of the
    // hotbar item, mirroring the inventory slot markers (drawSlot TAIL).
    @Inject(method = "renderHotbarItem", at = @At("TAIL"))
    private void injectDrawHotbarItemMarkers(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
        if(LoadingHandler.instance().isOnServer && LoadingHandler.instance().isLoadingDone
                && config.itemMarker.itemSlotMarker.showItemMarker
                && config.itemMarker.itemSlotMarker.showHotbarMarkers
                && !stack.isEmpty()) {
            ItemMarkerHandler.instance().renderItemMarker(context, stack, x, y);
        }
    }
}
