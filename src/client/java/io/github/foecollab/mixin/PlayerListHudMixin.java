package io.github.foecollab.mixin;

import io.github.foecollab.FOMC.Types.Defaults;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.handler.LoadingHandler;
import io.github.foecollab.handler.ProfileDataHandler;
import io.github.foecollab.util.SimpleTagFont;
import io.github.foecollab.util.TextHelper;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {
    @Unique
    private final FOEConfig config = FOEConfig.getConfig();

    // Inject into getPlayerName's RETURN rather than @Redirect-ing its call site inside render(): other
    // mods (e.g. FishyAddons' MixinPlayerListHud) also @Redirect that exact invoke, and two @Redirects on
    // one instruction are mutually exclusive — the second to apply finds nothing ("Scanned 0 target(s)")
    // and hard-crashes. Modifying the method's return value composes with those redirects instead.
    // NOTE:  /  are the friend / crew tag glyphs (mod bitmap font).
    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
    private void foeApplyTabListTags(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        String playerUuid = entry.getProfile().id().toString();
        Defaults.FoEDevType devType = Defaults.foeDevs.get(playerUuid);

        MutableText text;
        if (LoadingHandler.instance().isOnServer && devType != null) {
            String jsonText = TextHelper.textToJson(cir.getReturnValue());
            jsonText = TextHelper.replaceToFoE(jsonText, devType.usePurpleTag);
            if (!devType.usePurpleTag) {
                jsonText = jsonText.replace("B05BF9", "00AF0E");
            }
            // .copy() (not a raw cast): jsonToText's deserialized Text has an IMMUTABLE siblings
            // list, so the friend/crew .append(...) below would throw UnsupportedOperationException
            // (crashes the tab-list render for any dev who is also a friend/crew member, e.g. you in
            // your own crew). copy() yields a MutableText with a mutable siblings list, like the
            // non-dev branch.
            text = TextHelper.jsonToText(jsonText).copy();
        } else {
            text = cir.getReturnValue().copy();
        }

        if (config.friendTracker.showFriendTag && LoadingHandler.instance().isOnServer && ProfileDataHandler.instance().profileData.friends.contains(entry.getProfile().id())) {
            text = config.friendTracker.isPrefix ? Text.literal("").append(text) : text.append(Text.literal("").formatted(Formatting.WHITE));
        }

        if (config.crewTracker.showCrewTag && LoadingHandler.instance().isOnServer && ProfileDataHandler.instance().profileData.crewMembers.contains(entry.getProfile().id())) {
            text = config.crewTracker.isPrefix ? Text.literal("").append(text) : text.append(Text.literal(" ").formatted(Formatting.WHITE));
        }

        Text result = text;
        if ((config.cleanerDisplay.simpleRankTags || config.cleanerDisplay.simpleRarityTags) && LoadingHandler.instance().isOnServer) {
            result = SimpleTagFont.apply(result, config.cleanerDisplay.simpleRankTags, config.cleanerDisplay.simpleRarityTags);
        }
        cir.setReturnValue(result);
    }
}
