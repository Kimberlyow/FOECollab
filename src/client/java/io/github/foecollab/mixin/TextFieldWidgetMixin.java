package io.github.foecollab.mixin;

import io.github.foecollab.handler.CrewHandler;
import io.github.foecollab.handler.LoadingHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(TextFieldWidget.class)
public abstract class TextFieldWidgetMixin {
    @Shadow @Final private TextRenderer textRenderer;

    @Unique
    private static int xCoord = 0;
    @Unique
    private static int yCoord = 0;
    @Unique
    private static boolean ticker = false;

    // 1.21.11: the blinking cursor "_" is now drawn via DrawContext.drawText(TextRenderer, String, x, y, color, shadow)
    // (the old drawTextWithShadow(..String..)->int overload is gone). ordinal=1 selects the cursor draw; ordinal=0 is
    // the suggestion text. Arg indices are unchanged: 1=String, 2=x, 3=y.
    @ModifyArgs(method = "renderWidget", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)V"))
    private void getLocals(Args args) {
        if(LoadingHandler.instance().isOnServer) {
            xCoord = args.get(2);
            yCoord = args.get(3);
            args.set(1, ticker ? "_" : "");
        }
    }

    @ModifyVariable(method = "renderWidget", at = @At("STORE"), ordinal = 1)
    private boolean getbl2(boolean bl2) {
        ticker = bl2;
        if(LoadingHandler.instance().isOnServer) {
            return true;
        }
        return bl2;
    }

    @Inject(method = "renderWidget", at = @At("HEAD"))
    private void injectMarker(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if(LoadingHandler.instance().isOnServer) {
            CrewHandler.instance().renderCrewChatMarker(context, this.textRenderer, xCoord, yCoord);
        }
    }
}
