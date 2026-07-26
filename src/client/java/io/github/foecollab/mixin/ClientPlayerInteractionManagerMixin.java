package io.github.foecollab.mixin;

import io.github.foecollab.handler.QuestMenuSquareHandler;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {

    // Skips the client-side click prediction on the /quests menu "square" buttons so the clicked
    // item never visually jumps onto the cursor. clickSlot still diffs the (now unchanged) slots
    // and sends the click packet, and the server resyncs the menu as usual.
    @Redirect(method = "clickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandler;onSlotClick(IILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)V"))
    private void redirectOnSlotClick(ScreenHandler screenHandler, int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (QuestMenuSquareHandler.instance().isSquaredSlot(screenHandler, slotIndex)) {
            return;
        }
        screenHandler.onSlotClick(slotIndex, button, actionType, player);
    }
}
