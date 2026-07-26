package io.github.foecollab.screens.hud;

import io.github.foecollab.config.FOEConfig;

/// Shared vertical-placement maths for the box HUDs. The returned value is subtracted from a HUD
/// box's draw Y so its configured anchor percentage maps onto the screen: near the top the box sits
/// flush with the top edge, near the bottom it grows upward from the bottom edge.
///
/// While the top bar is shown the built-in HUDs keep a {@code 3*padding} reserve at the very top so
/// they don't collide with it. When the top bar is hidden that space is free, so they switch to the
/// flush model (the same one custom HUDs always use) and can be placed right at the top of the
/// screen. Routing every HUD's translation through here makes that the default for any new HUD —
/// and keeps it in sync with {@code MovableBoxWidget}'s drag bounds in the Move-HUD editor.
public final class HudLayout {
    private HudLayout() {
    }

    /// @param padding       the HUD's box padding (the reserve is {@code 3*padding})
    /// @param contentHeight the box's content height in unscaled px (typically {@code padding*2 + lines*lineHeight})
    /// @param yPercent      the configured anchor fraction (0 = top, 1 = bottom)
    public static int heightClampTranslation(int padding, int contentHeight, float yPercent) {
        int translation = (int) (contentHeight * yPercent);
        if (FOEConfig.getConfig().barHUD.showBar) {
            translation -= (int) ((padding * 3) * (1 - yPercent));
        }
        return translation;
    }
}
