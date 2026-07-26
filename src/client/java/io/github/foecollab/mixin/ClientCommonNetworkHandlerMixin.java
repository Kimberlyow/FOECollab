package io.github.foecollab.mixin;

import io.github.foecollab.FOECollab;
import io.github.foecollab.handler.ServerPackHandler;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonNetworkHandler.class)
public abstract class ClientCommonNetworkHandlerMixin {
    @Shadow public abstract void sendPacket(Packet<?> packet);

    // If the pushed pack is already loaded as a mirrored local pack (see ServerPackHandler),
    // report it as loaded instead of downloading/applying it — joining causes no resource reload.
    // A hash mismatch (server updated its pack) falls through to the vanilla download flow.
    @Inject(method = "onResourcePackSend", at = @At("HEAD"), cancellable = true)
    private void foecollab$skipMirroredServerPack(ResourcePackSendS2CPacket packet, CallbackInfo ci) {
        if (!ServerPackHandler.instance().isLocallyLoaded(packet.hash(), packet.url())) {
            return;
        }
        FOECollab.LOGGER.info("[FoE] Server pack {} already loaded locally, skipping download/reload", packet.url());
        this.sendPacket(new ResourcePackStatusC2SPacket(packet.id(), ResourcePackStatusC2SPacket.Status.ACCEPTED));
        this.sendPacket(new ResourcePackStatusC2SPacket(packet.id(), ResourcePackStatusC2SPacket.Status.DOWNLOADED));
        this.sendPacket(new ResourcePackStatusC2SPacket(packet.id(), ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED));
        ci.cancel();
    }
}
