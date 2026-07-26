package io.github.foecollab.mixin;

import io.github.foecollab.handler.ChatTagHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {

    @Shadow protected TextFieldWidget chatField;

    @Unique private static final int MAX_SUGGESTIONS = 8;
    @Unique private ChatTagHandler.CompletionList pendingCompletions;
    @Unique private int selectedSuggestionIndex = 0;
    @Unique private boolean suggestionsVisible = false;
    @Unique private int suggestX;
    @Unique private int suggestY;
    @Unique private int suggestW;
    @Unique private int suggestH;
    @Unique private int suggestRowH;
    @Inject(method = "onChatFieldUpdate", at = @At("TAIL"))
    private void onChatFieldUpdate(String chatText, CallbackInfo ci) {
        pendingCompletions = null;
        suggestionsVisible = false;
        selectedSuggestionIndex = 0;
        if (chatField != null) {
            chatField.setSuggestion(null);
        }

        if (chatField == null) {
            return;
        }

        if (chatText != null && chatText.startsWith("/")) {
            return;
        }

        int cursor = chatField.getCursor();
        ChatTagHandler.CompletionList completions = ChatTagHandler.findCompletions(chatText, cursor);
        if (completions == null) {
            return;
        }

        List<ChatTagHandler.Suggestion> matches = completions.matches();
        if (matches == null || matches.isEmpty()) {
            return;
        }

        pendingCompletions = completions;
        suggestionsVisible = true;
        selectedSuggestionIndex = Math.max(0, Math.min(selectedSuggestionIndex, matches.size() - 1));
        updateGhostSuggestion(chatText, cursor);
    }

    @Unique
    private void updateGhostSuggestion(String chatText, int cursor) {
        if (chatField == null) {
            return;
        }

        ChatTagHandler.CompletionList completions = pendingCompletions;
        if (!suggestionsVisible || completions == null) {
            chatField.setSuggestion(null);
            return;
        }

        List<ChatTagHandler.Suggestion> matches = completions.matches();
        int totalCount = matches.size();
        if (totalCount <= 0) {
            chatField.setSuggestion(null);
            return;
        }

        int idx = Math.max(0, Math.min(selectedSuggestionIndex, totalCount - 1));
        ChatTagHandler.Suggestion selected = matches.get(idx);

        int open = completions.replaceFrom();
        int to = completions.replaceTo();
        if (chatText == null || open < 0 || to < open || to > chatText.length()) {
            chatField.setSuggestion(null);
            return;
        }

        String typedAfterBracket = chatText.substring(open + 1, cursor);
        String full = "[" + selected.tag() + "]";

        String remaining = "";
        if (typedAfterBracket.length() <= full.length() - 1) {
            remaining = full.substring(1 + typedAfterBracket.length());
        }
        chatField.setSuggestion(remaining);
    }
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void keyPressed(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        int keyCode = input.key();
        if (chatField == null) {
            return;
        }

        if (!suggestionsVisible || pendingCompletions == null) {
            return;
        }

        if (keyCode == GLFW.GLFW_KEY_UP) {
            selectedSuggestionIndex = Math.max(0, selectedSuggestionIndex - 1);
            updateGhostSuggestion(chatField.getText(), chatField.getCursor());
            cir.setReturnValue(true);
            cir.cancel();
            return;
        }
        if (keyCode == GLFW.GLFW_KEY_DOWN) {
            int max = Math.max(0, pendingCompletions.matches().size() - 1);
            if (max <= 0) {
                selectedSuggestionIndex = 0;
            } else {
                selectedSuggestionIndex = (selectedSuggestionIndex + 1) > max ? 0 : (selectedSuggestionIndex + 1);
            }
            updateGhostSuggestion(chatField.getText(), chatField.getCursor());
            cir.setReturnValue(true);
            cir.cancel();
            return;
        }

        if (keyCode == GLFW.GLFW_KEY_TAB || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            applySelectedSuggestion();
            cir.setReturnValue(true);
            cir.cancel();
        }
}

    @Inject(method = "render", at = @At("TAIL"))
    private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!suggestionsVisible || pendingCompletions == null || chatField == null) {
            return;
        }

        List<ChatTagHandler.Suggestion> matches = pendingCompletions.matches();
        if (matches == null || matches.isEmpty()) {
            return;
        }

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int totalCount = matches.size();
        int visibleCount = Math.min(totalCount, MAX_SUGGESTIONS);

        int paddingX = 4;
        int paddingY = 3;
        int lineH = textRenderer.fontHeight + 2;

        int maxWidth = 0;
        int selected = Math.max(0, Math.min(selectedSuggestionIndex, totalCount - 1));
        int maxStart = Math.max(0, totalCount - visibleCount);
        int windowStart = Math.min(Math.max(selected - (visibleCount - 1), 0), maxStart);
        int longestLen = 0;
        ChatTagHandler.Suggestion longest = null;
        for (ChatTagHandler.Suggestion c : matches) {
            int len = c.tag().length();
            if (len > longestLen) {
                longestLen = len;
                longest = c;
            }
        }
        if (longest != null) {
            Text display = Text.literal("[" + longest.tag() + "]").withColor(longest.color());
            maxWidth = textRenderer.getWidth(display);
        }

        int boxW = maxWidth + paddingX * 2;
        int boxH = visibleCount * lineH + paddingY * 2;

        int x = chatField.getX();
        int yAbove = chatField.getY() - boxH - 2;
        int y = yAbove >= 0 ? yAbove : chatField.getY() + chatField.getHeight() + 2;

        context.getMatrices().pushMatrix();
        try {
            context.fill(x, y, x + boxW, y + boxH, 0xCC000000);
            // context.drawBorder(x, y, boxW, boxH, 0xFFFFAA00);

            int selectedRow = selected - windowStart;
            for (int i = 0; i < visibleCount; i++) {
                int rowY = y + paddingY + i * lineH;
                if (i == selectedRow) {
                    context.fill(x + 1, rowY - 1, x + boxW - 1, rowY - 1 + lineH, 0x66333333);
                }
                var c = matches.get(windowStart + i);
                Text display = Text.literal("[" + c.tag() + "]").withColor(c.color());
                context.drawText(textRenderer, display, x + paddingX, rowY, 0xFFFFFFFF, true);
            }
        } finally {
            context.getMatrices().popMatrix();
        }

        suggestX = x;
        suggestY = y;
        suggestW = boxW;
        suggestH = boxH;
        suggestRowH = lineH;
    }

    @Unique
    private void applySelectedSuggestion() {
        if (chatField == null) {
            return;
        }
        ChatTagHandler.CompletionList completions = pendingCompletions;
        if (!suggestionsVisible || completions == null) {
            return;
        }
        List<ChatTagHandler.Suggestion> matches = completions.matches();
        int totalCount = matches.size();
        if (totalCount <= 0) {
            return;
        }

        int idx = Math.max(0, Math.min(selectedSuggestionIndex, totalCount - 1));
        ChatTagHandler.Suggestion selected = matches.get(idx);
        String fullText = "[" + selected.tag() + "]";

        String text = chatField.getText();
        int from = completions.replaceFrom();
        int to = completions.replaceTo();
        if (text == null || from < 0 || to < from || to > text.length()) {
            return;
        }

        String replaced = text.substring(0, from) + fullText + text.substring(to);
        chatField.setText(replaced);
        chatField.setCursor(from + fullText.length(), false);

        pendingCompletions = null;
        suggestionsVisible = false;
        selectedSuggestionIndex = 0;
        chatField.setSuggestion(null);

        return;
    }

}
