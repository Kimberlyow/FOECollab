package io.github.foecollab.mixin;

import io.github.foecollab.handler.ServerPackHandler;
import net.minecraft.client.option.GameOptions;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GameOptions.class)
public class GameOptionsMixin {
    @Shadow public List<String> resourcePacks;
    @Shadow public List<String> incompatibleResourcePacks;

    // Runs at startup right after scanPacks(): enable the mirrored server packs. Only ADD missing
    // entries — if the user reordered packs (e.g. to override the server pack) that order sticks.
    @Inject(method = "addResourcePackProfilesToManager", at = @At("HEAD"))
    private void foecollab$enableMirroredServerPacks(ResourcePackManager manager, CallbackInfo ci) {
        for (String id : ServerPackHandler.instance().getProfileIds()) {
            ResourcePackProfile profile = manager.getProfile(id);
            if (profile == null) {
                continue;
            }
            if (!this.resourcePacks.contains(id)) {
                this.resourcePacks.add(id);
            }
            // server packs often target a slightly different pack_format; marking them as
            // user-confirmed incompatible packs makes vanilla keep them enabled anyway
            if (!profile.getCompatibility().isCompatible() && !this.incompatibleResourcePacks.contains(id)) {
                this.incompatibleResourcePacks.add(id);
            }
        }
    }
}
