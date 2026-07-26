package io.github.foecollab.mixin;

import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.handler.LoadingHandler;
import net.minecraft.client.Mouse;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mouse.class)
public class MouseMixin {
    // This setSelectedSlot call is the in-game mouse-wheel hotbar cycling and nothing else —
    // chat and menu scrolling go through Screen#mouseScrolled earlier in onMouseScroll, so
    // swallowing it here only stops the hotbar slot from changing.
    @Redirect(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;setSelectedSlot(I)V"))
    private void foeToggleHotbarScroll(PlayerInventory inventory, int slot) {
        if (LoadingHandler.instance().isOnServer && !FOEConfig.getConfig().hotbarScrolling) {
            return;
        }
        inventory.setSelectedSlot(slot);
    }
}
