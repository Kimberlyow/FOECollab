package io.github.foecollab.mixin;

import io.github.foecollab.config.FOEConfig;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/// Suppresses the "advancement / challenge complete" toast sound
/// ({@code ui.toast.challenge_complete}) when {@link FOEConfig#muteAdvancementSound} is on.
/// On FishOnMC an advancement is granted on every fish catch, so this sound otherwise spams
/// constantly while fishing. It's the only source of this sound, so cancelling it here mutes
/// just that sound and nothing else.
@Mixin(SoundManager.class)
public abstract class SoundManagerMixin {

    @Inject(
            method = "play(Lnet/minecraft/client/sound/SoundInstance;)Lnet/minecraft/client/sound/SoundSystem$PlayResult;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void foecollab$muteAdvancementSound(SoundInstance sound, CallbackInfoReturnable<SoundSystem.PlayResult> cir) {
        if (FOEConfig.getConfig().muteAdvancementSound
                && sound != null
                && SoundEvents.UI_TOAST_CHALLENGE_COMPLETE.id().equals(sound.getId())) {
            cir.setReturnValue(SoundSystem.PlayResult.NOT_STARTED);
        }
    }
}
