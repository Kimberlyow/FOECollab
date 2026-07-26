package io.github.foecollab.util;

import net.minecraft.client.option.KeyBinding;

public class AdvancedKeyBinding extends KeyBinding {
    public AdvancedKeyBinding(String translationKey, int code, KeyBinding.Category category) {
        super(translationKey, code, category);
    }

    public void onPressed(Runnable runTrue) {
        while (this.wasPressed()) {
            runTrue.run();
        }
    }
}
