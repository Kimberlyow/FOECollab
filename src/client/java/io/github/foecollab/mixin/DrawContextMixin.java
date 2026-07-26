package io.github.foecollab.mixin;

import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.handler.ItemStackDisplayHandler;
import io.github.foecollab.handler.LoadingHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {

    @Inject(
            method = "drawStackCount(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void foecollab$smallStackCount(TextRenderer textRenderer, ItemStack stack, int x, int y, String countOverride, CallbackInfo ci) {
        if (LoadingHandler.instance().isOnServer
                && FOEConfig.getConfig().itemStackDisplay.showStackCountDisplay) {
            ItemStackDisplayHandler.instance().drawStackCount((DrawContext) (Object) this, textRenderer, stack, x, y, countOverride);
            ci.cancel();
        }
    }
}
