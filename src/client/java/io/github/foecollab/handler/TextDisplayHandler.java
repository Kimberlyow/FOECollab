package io.github.foecollab.handler;

public class TextDisplayHandler {
    // sowwy for making it pwetty instead of best-pwactice :owo:
    public enum TextDisplay {
        UPPERCASE,
        lowercase,
        Capitalized,
        TAG,
        OFF
    }

    public static String formatText(String input, TextDisplay textDisplay) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        switch (textDisplay) {
            case UPPERCASE:
                return input.toUpperCase();
            case lowercase:
                return input.toLowerCase();
            case Capitalized:
                return capitalizeWords(input);
            case TAG:
                return "[" + input.toUpperCase() + "]";
            case OFF:
                return input;
            default:
                return input;
        }
    }

    private static String capitalizeWords(String input) {
        String[] words = input.split("\\s+");
        StringBuilder capitalized = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                capitalized.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    capitalized.append(word.substring(1).toLowerCase());
                }
                capitalized.append(" ");
            }
        }
        return capitalized.toString().trim();
    }
}