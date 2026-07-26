package io.github.foecollab.handler;

import io.github.foecollab.FOMC.Types.Defaults;
import io.github.foecollab.util.TextHelper;
import net.minecraft.text.Text;

public class ChatScreenHandler {
    private static ChatScreenHandler INSTANCE = new ChatScreenHandler();

    public boolean screenInit = false;

    public static ChatScreenHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new ChatScreenHandler();
        }
        return INSTANCE;
    }

    public Text appendTooltip(Text text) {
        String textString = text.getString();
        if (textString.startsWith("!")
                && textString.contains("»")) {
            Defaults.FoEDevType senderDev = Defaults.foeDevs.values().stream()
                    .filter(foEDevType -> textString.contains(foEDevType.text))
                    .findFirst()
                    .orElse(null);
            
            if (senderDev != null) {
                String jsonText = TextHelper.textToJson(text);
                jsonText = TextHelper.replaceToFoE(jsonText, senderDev.usePurpleTag);
                if (!senderDev.usePurpleTag) {
                    jsonText = jsonText.replace("B05BF9", "00AF0E");
                }
                return TextHelper.jsonToText(jsonText);
            }
        }
        return text;
    }
}
