package io.github.foecollab.mixin;

import io.github.foecollab.handler.ItemMarkerHandler;
import io.github.foecollab.handler.LoadingHandler;
import io.github.foecollab.handler.QuestMenuSquareHandler;
import io.github.foecollab.handler.SearchBarContainerHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    @Shadow public abstract void close();

    // Behind-item highlights (equipped-pet, search, bait sorting) draw at HEAD, before
    // the item, so they sit behind it (they used z=100 before 1.21.11 dropped matrix z).
    @Inject(method = "drawSlot", at = @At("HEAD"))
    private void injectDrawSlotBackground(DrawContext context, Slot slot, int x, int y, CallbackInfo ci) {
        if(LoadingHandler.instance().isOnServer) {
            ItemMarkerHandler.instance().renderItemMarkerBackground(context, slot);
        }
    }

    @Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
    private void injectDrawSlotQuestSquare(DrawContext context, Slot slot, int x, int y, CallbackInfo ci) {
        if (QuestMenuSquareHandler.instance().isSquaredSlot(slot)) {
            QuestMenuSquareHandler.instance().render(context, slot);
            ci.cancel();
        }
    }

    @Inject(method = "drawSlot", at = @At("TAIL"))
    private void injectDrawSlot(DrawContext context, Slot slot, int x, int y, CallbackInfo ci) {
        if(LoadingHandler.instance().isOnServer) {
            ItemMarkerHandler.instance().renderItemMarker(context, slot);
        }
    }

    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;close()V"), cancellable = true)
    private void injectKeypressed(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        if (SearchBarContainerHandler.instance().searchBar != null
                && SearchBarContainerHandler.instance().searchBar.isFocused()) {
            cir.setReturnValue(true);
        }
    }
}
