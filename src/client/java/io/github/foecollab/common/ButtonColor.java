package io.github.foecollab.common;

import io.github.foecollab.config.FOEConfig;

/**
 * Tint color applied to the inventory menu buttons ({@code ContainerButtonWidget} /
 * {@code ModernButtonWidget}). Mirrors the HUD font palette ({@link HudFont.FontColor}) and adds
 * plain white / black. The tint is multiplied onto the dark box textures, so colors come out
 * muted: {@code OFF} and {@code WHITE} both leave the default look, {@code BLACK} darkens it,
 * and the rest recolor it.
 */
public enum ButtonColor {
    OFF(0xFFFFFF),
    WHITE(0xFFFFFF),
    BLACK(0x000000),
    PURPLE(0xB45AF2),
    PINK(0xFF6EC7),
    BLUE(0x4AA3FF),
    RED(0xFF5555),
    ORANGE(0xFFA040),
    YELLOW(0xFFD93B),
    GREEN(0x55E06A),
    CYAN(0x40E0D0),
    CUSTOM(0xB45AF2);

    public final int base;

    ButtonColor(int base) {
        this.base = base;
    }

    /** ARGB color to multiply the button texture with. {@code OFF} returns opaque white (no change). */
    public static int tint() {
        var cfg = FOEConfig.getConfig().inventoryButton;
        if (cfg.buttonColor == OFF) {
            return 0xFFFFFFFF;
        }
        int rgb = (cfg.buttonColor == CUSTOM ? cfg.customColor : cfg.buttonColor.base) & 0xFFFFFF;
        return 0xFF000000 | rgb;
    }
}
