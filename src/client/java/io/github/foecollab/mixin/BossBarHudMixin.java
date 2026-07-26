package io.github.foecollab.mixin;

import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.handler.LoadingHandler;
import io.github.foecollab.util.LocationNameHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossBarHud.class)
public class BossBarHudMixin {
    @Unique
    private final FOEConfig config = FOEConfig.getConfig();

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void injectRender(DrawContext context, CallbackInfo ci) {
        if(config.bossBarTracker.hideBossBar && LoadingHandler.instance().isOnServer) {
            ci.cancel();
        }
    }

    // Shorten the location name in the (visible) server boss bar. Redirect the getName() call
    // rather than the drawText arg: render() measures the name's width to centre it, so the
    // shortened text must be in place BEFORE that measurement or it draws off-centre.
    @Redirect(
            method = "render(Lnet/minecraft/client/gui/DrawContext;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ClientBossBar;getName()Lnet/minecraft/text/Text;"))
    private Text foeShortenBossBarLocation(ClientBossBar bar) {
        Text name = bar.getName();
        if(config.cleanerDisplay.shortenLocationNames && LoadingHandler.instance().isOnServer) {
            return LocationNameHelper.shorten(name);
        }
        return name;
    }
}
