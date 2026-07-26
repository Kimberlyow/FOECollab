package io.github.foecollab.handler;

import com.google.gson.*;
import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.util.TextHelper;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.startsWith;

public class ChatTagHandler {

    private static ChatTagHandler INSTANCE = new ChatTagHandler();

    public static ChatTagHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new ChatTagHandler();
        }
        return INSTANCE;
    }

    // see Constant.java
    private static final Constant[] ALLOWED_CONSTANTS = new Constant[] {
            // Fish Size
            Constant.BABY,
            Constant.JUVENILE,
            Constant.ADULT,
            Constant.LARGE,
            Constant.GIGANTIC,

            // Rarity
            Constant.COMMON,
            Constant.RARE,
            Constant.EPIC,
            Constant.LEGENDARY,
            Constant.MYTHICAL,

            // Event Rarities
            Constant.SPECIAL,

            // Location
            Constant.CYPRESS_LAKE,
            Constant.KENAI_RIVER,
            Constant.LAKE_BIWA,
            Constant.MURRAY_RIVER,
            Constant.EVERGLADES,
            Constant.KEY_WEST,
            Constant.TOLEDO_BEND,
            Constant.GREAT_LAKES,
            Constant.DANUBE_RIVER,
            Constant.OIL_RIG,
            Constant.AMAZON_RIVER,
            Constant.MEDITERRANEAN_SEA,
            Constant.CAPE_COD,
            Constant.HAWAII,
            Constant.LOFOTEN_ISLANDS,
            Constant.CAIRNS,

            // Variants
            Constant.NORMAL,
            Constant.ALBINO,
            Constant.MELANISTIC,
            Constant.TROPHY,
            Constant.FABLED,

            // Rare Catches
            Constant.LIGHTNING_BOTTLE,
            Constant.INFUSION_CAPSULE,
            Constant.SHARD,
            Constant.PROSPECTING_AMULET,
            // Bigfoot Drops
            Constant.BIGFOOT_FUR,
            Constant.BIGFOOT_TOOTH,
            // Pet
            Constant.PET,

            // Pet Rating
            Constant.SICKLY,
            Constant.BAD,
            Constant.BELOW_AVERAGE,
            Constant.AVERAGE,
            Constant.GOOD,
            Constant.GREAT,
            Constant.EXCELLENT,
            Constant.AMAZING,
            Constant.PERFECT,

            // Pets
            Constant.BULLFROG,
            Constant.BEAR,
            Constant.FOX,
            Constant.KANGAROO,
            Constant.MARSH_RABBIT,
            Constant.SEA_TURTLE,
            Constant.DUCK,
            Constant.EAGLE,
            Constant.WOLF,
            Constant.PELICAN,
            Constant.CAPYBARA,
            Constant.LYNX,
            Constant.SHARK,
            Constant.DOLPHIN,
            Constant.SHEEP,
            Constant.KOALA,

            // Water Types
            Constant.FRESHWATER,
            Constant.SALTWATER,

            // Ranks
            Constant.ANGLER,
            Constant.SAILOR,
            Constant.MARINER,
            Constant.CAPTAIN,
            Constant.ADMIRAL,
            Constant.FOE,

            // Stats
            Constant.LUCK,
            Constant.SCALE,

            // Climate
            Constant.SUBTROPICAL,
            Constant.SUBARCTIC,
            Constant.SEMI_ARID,
            Constant.SAVANNA,
            Constant.CONTINENTAL,
            Constant.RAINFOREST,
            Constant.MEDITERRANEAN,
            Constant.OCEANIC,
            Constant.MONSOON
    };

    private static final Gson GSON = new Gson();
    private static final Pattern TAG_PATTERN = Pattern.compile("\\[([A-Z0-9_]+)\\]");

    private static final Constant[] PET_RARITY_CONSTANTS = new Constant[] {
            Constant.COMMON,
            Constant.RARE,
            Constant.EPIC,
            Constant.LEGENDARY,
            Constant.MYTHICAL
    };

    private final FOEConfig config = FOEConfig.getConfig();

    public Text displayTags(Text message) {
        String messageString = message.getString();
        Matcher matcher = TAG_PATTERN.matcher(messageString);
        if (startsWith(messageString, "FOE » ")) {
            return message;
        }
        if (!matcher.find()) {
            return message;
        }

        String json = TextHelper.textToJson(message.copy());
        JsonElement root;
        try {
            root = JsonParser.parseString(json);
        } catch (Exception ignored) {
            return message;
        }

        boolean changed = replaceTagsInJson(root);
        if (!changed) {
            return message;
        }

        return TextHelper.jsonToText(GSON.toJson(root));
    }

    private static boolean replaceTagsInJson(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return false;
        }

        boolean changed = false;

        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                changed |= replaceTagsInJson(child);
            }
            return changed;
        }

        if (!element.isJsonObject()) {
            return false;
        }

        JsonObject obj = element.getAsJsonObject();

        if (obj.has("extra") && obj.get("extra").isJsonArray()) {
            for (JsonElement child : obj.getAsJsonArray("extra")) {
                changed |= replaceTagsInJson(child);
            }
        }
        if (obj.has("with") && obj.get("with").isJsonArray()) {
            for (JsonElement child : obj.getAsJsonArray("with")) {
                changed |= replaceTagsInJson(child);
            }
        }

        if (!obj.has("text") || !obj.get("text").isJsonPrimitive()) {
            return changed;
        }

        String text = obj.get("text").getAsString();
        Matcher matcher = TAG_PATTERN.matcher(text);
        if (!matcher.find()) {
            return changed;
        }

        JsonObject styleTemplate = new JsonObject();
        for (var entry : obj.entrySet()) {
            String key = entry.getKey();
            if (key.equals("text") || key.equals("extra")) {
                continue;
            }
            styleTemplate.add(key, entry.getValue().deepCopy());
        }

        int firstStart = matcher.start();
        int lastEnd = matcher.end();

        JsonArray newExtra = new JsonArray();
        boolean replacedAnyAllowed = false;

        Constant constant = getConstantOrNull(matcher.group(1));
        if (isAllowedConstant(constant)) {
            newExtra.add(JsonParser.parseString(TextHelper.textToJson(constant.TAG.copy())));
            replacedAnyAllowed = true;
        } else {
            JsonObject literal = new JsonObject();
            copyJsonObject(styleTemplate, literal);
            literal.addProperty("text", text.substring(matcher.start(), matcher.end()));
            newExtra.add(literal);
        }

        while (matcher.find()) {
            String between = text.substring(lastEnd, matcher.start());
            if (!between.isEmpty()) {
                JsonObject betweenObj = new JsonObject();
                copyJsonObject(styleTemplate, betweenObj);
                betweenObj.addProperty("text", between);
                newExtra.add(betweenObj);
            }

            Constant c = getConstantOrNull(matcher.group(1));
            if (isAllowedConstant(c)) {
                newExtra.add(JsonParser.parseString(TextHelper.textToJson(c.TAG.copy())));
                replacedAnyAllowed = true;
            } else {
                JsonObject literal = new JsonObject();
                copyJsonObject(styleTemplate, literal);
                literal.addProperty("text", text.substring(matcher.start(), matcher.end()));
                newExtra.add(literal);
            }

            lastEnd = matcher.end();
        }

        String suffix = text.substring(lastEnd);
        if (!suffix.isEmpty()) {
            JsonObject suffixObj = new JsonObject();
            copyJsonObject(styleTemplate, suffixObj);
            suffixObj.addProperty("text", suffix);
            newExtra.add(suffixObj);
        }

        if (!replacedAnyAllowed) {
            return changed;
        }

        String prefix = text.substring(0, firstStart);
        obj.addProperty("text", prefix);

        if (obj.has("extra") && obj.get("extra").isJsonArray()) {
            JsonArray existingExtra = obj.getAsJsonArray("extra");
            for (JsonElement existing : existingExtra) {
                newExtra.add(existing);
            }
        }
        obj.add("extra", newExtra);

        return true;
    }

    private static boolean isAllowedConstant(Constant constant) {
        if (constant == null) {
            return false;
        }
        for (Constant allowed : ALLOWED_CONSTANTS) {
            if (allowed == constant) {
                return true;
            }
        }
        return false;
    }

    private static Constant getConstantOrNull(String constantName) {
        try {
            return Constant.valueOf(constantName);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static void copyJsonObject(JsonObject from, JsonObject to) {
        for (var entry : from.entrySet()) {
            to.add(entry.getKey(), entry.getValue().deepCopy());
        }
    }

    public Text changePetTags(Text message) {
        String messageString = message.getString();
        if (!startsWith(messageString, "PET DROP! You pulled a ")) {
            return message;
        }

        if (!config.chatconfig.makeSomeTagsCopyPastable) {
            return message;
        }

        String json = TextHelper.textToJson(message.copy());
        JsonElement root;
        try {
            root = JsonParser.parseString(json);
        } catch (Exception ignored) {
            return message;
        }

        boolean changed = replaceTagsInJson(root);

        boolean changedPetRarity = replacePetRarityIconsInJson(root);
        if (!changed && !changedPetRarity) {
            return message;
        }

        return TextHelper.jsonToText(GSON.toJson(root));
    }

    private static boolean replacePetRarityIconsInJson(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return false;
        }

        boolean changed = false;

        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                changed |= replacePetRarityIconsInJson(child);
            }
            return changed;
        }

        if (!element.isJsonObject()) {
            return false;
        }

        JsonObject obj = element.getAsJsonObject();

        if (obj.has("extra") && obj.get("extra").isJsonArray()) {
            for (JsonElement child : obj.getAsJsonArray("extra")) {
                changed |= replacePetRarityIconsInJson(child);
            }
        }
        if (obj.has("with") && obj.get("with").isJsonArray()) {
            for (JsonElement child : obj.getAsJsonArray("with")) {
                changed |= replacePetRarityIconsInJson(child);
            }
        }

        if (!obj.has("text") || !obj.get("text").isJsonPrimitive()) {
            return changed;
        }

        String text = obj.get("text").getAsString();

        int firstIndex = indexOfRarityUnicode(text);
        if (firstIndex < 0) {
            return changed;
        }

        JsonObject styleTemplate = new JsonObject();
        for (var entry : obj.entrySet()) {
            String key = entry.getKey();
            if (key.equals("text") || key.equals("extra")) {
                continue;
            }
            styleTemplate.add(key, entry.getValue().deepCopy());
        }

        String prefix = text.substring(0, firstIndex);
        obj.addProperty("text", prefix);

        com.google.gson.JsonArray newExtra = new com.google.gson.JsonArray();

        int cursor = firstIndex;
        while (cursor < text.length()) {
            int nextIndex = indexOfRarityUnicode(text, cursor);
            if (nextIndex < 0) {
                String tail = text.substring(cursor);
                if (!tail.isEmpty()) {
                    JsonObject tailObj = new JsonObject();
                    copyJsonObject(styleTemplate, tailObj);
                    tailObj.addProperty("text", tail);
                    newExtra.add(tailObj);
                }
                break;
            }

            if (nextIndex > cursor) {
                String between = text.substring(cursor, nextIndex);
                if (!between.isEmpty()) {
                    JsonObject betweenObj = new JsonObject();
                    copyJsonObject(styleTemplate, betweenObj);
                    betweenObj.addProperty("text", between);
                    newExtra.add(betweenObj);
                }
            }

            Constant rarity = constantFromUnicode(text.charAt(nextIndex));
            if (rarity != null) {
                Text bracketTag = Text.literal("[" + rarity.name() + "]").withColor(rarity.COLOR);
                newExtra.add(JsonParser.parseString(TextHelper.textToJson(bracketTag)));
                changed = true;
                cursor = nextIndex + 1;
            } else {
                JsonObject literal = new JsonObject();
                copyJsonObject(styleTemplate, literal);
                literal.addProperty("text", String.valueOf(text.charAt(nextIndex)));
                newExtra.add(literal);
                cursor = nextIndex + 1;
            }
        }

        if (obj.has("extra") && obj.get("extra").isJsonArray()) {
            for (JsonElement existing : obj.getAsJsonArray("extra")) {
                newExtra.add(existing);
            }
        }
        obj.add("extra", newExtra);

        return changed;
    }

    private static int indexOfRarityUnicode(String text) {
        return indexOfRarityUnicode(text, 0);
    }

    private static int indexOfRarityUnicode(String text, int fromIndex) {
        if (text == null || text.isEmpty()) {
            return -1;
        }
        for (int i = Math.max(0, fromIndex); i < text.length(); i++) {
            if (constantFromUnicode(text.charAt(i)) != null) {
                return i;
            }
        }
        return -1;
    }

    private static Constant constantFromUnicode(char unicode) {
        String unicodeString = String.valueOf(unicode);
        for (Constant rarity : PET_RARITY_CONSTANTS) {
            if (rarity.TAG.getString().equals(unicodeString)) {
                return rarity;
            }
        }
        return null;
    }

    public static CompletionList findCompletions(String rawText, int cursor) {
        if (rawText == null) {
            return null;
        }
        cursor = Math.max(0, Math.min(cursor, rawText.length()));

        String beforeCursor = rawText.substring(0, cursor);

        int open = beforeCursor.lastIndexOf('[');
        if (open < 0) {
            return null;
        }

        if (beforeCursor.indexOf(']', open) != -1) {
            return null;
        }

        if (open > 0 && !Character.isWhitespace(beforeCursor.charAt(open - 1))) {
            return null;
        }

        String typed = beforeCursor.substring(open + 1);
        String typedUpper = typed.toUpperCase();

        java.util.ArrayList<Suggestion> matches = new java.util.ArrayList<>();
        for (Constant c : ALLOWED_CONSTANTS) {
            if (typedUpper.isEmpty() || c.name().startsWith(typedUpper)) {
                matches.add(new Suggestion(c.name(), c.COLOR));
            }
        }

        if (typedUpper.isEmpty() || "ITEM".startsWith(typedUpper)) {
            matches.add(new Suggestion("item", Constant.DEFAULT.COLOR));
        }

        if (matches.isEmpty()) {
            return null;
        }

        return new CompletionList(open, cursor, typed, matches);
    }

    public record CompletionList(int replaceFrom, int replaceTo, String typed, java.util.List<Suggestion> matches) {
    }

    public record Suggestion(String tag, int color) {
    }
}
