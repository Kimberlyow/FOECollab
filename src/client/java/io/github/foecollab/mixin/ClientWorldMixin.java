package io.github.foecollab.mixin;

import io.github.foecollab.handler.ChummerHandler;
import io.github.foecollab.handler.LoadingHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Routes every spawned particle through chummer tracking: every public particle-spawn path on
 * ClientWorld funnels into this private addParticle. {@code ChummerHandler#observeParticle}
 * keeps tracked chummers alive and says whether to drop the particle because the solid range
 * ring replaces the server's circle.
 */
@Mixin(ClientWorld.class)
public class ClientWorldMixin {
    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;ZZDDDDDD)V", at = @At("HEAD"), cancellable = true)
    private void foecollab$observeChummerCircle(ParticleEffect parameters, boolean force, boolean canSpawnOnMinimal,
                                                double x, double y, double z,
                                                double velocityX, double velocityY, double velocityZ,
                                                CallbackInfo ci) {
        if (!LoadingHandler.instance().isOnServer) {
            return;
        }
        if (ChummerHandler.instance().observeParticle(x, y, z)) {
            ci.cancel();
        }
    }
}
