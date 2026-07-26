package io.github.foecollab.customhud;

import net.minecraft.text.Text;

/**
 * A resolved value produced by a placeholder or function in the custom-HUD code language
 * (ported from DannyPX's FishOnMC-Extras-R, with permission). Either a raw legacy-coded
 * {@link StringValue} (re-parsed for {@code &}/{@code §} colours by the engine) or an
 * already-built {@link ComponentValue}.
 */
public sealed interface PlaceholderValue permits PlaceholderValue.StringValue, PlaceholderValue.ComponentValue {
    String asString();

    record StringValue(String value) implements PlaceholderValue {
        @Override
        public String asString() {
            return value;
        }
    }

    record ComponentValue(Text value) implements PlaceholderValue {
        @Override
        public String asString() {
            return value.getString();
        }
    }
}
