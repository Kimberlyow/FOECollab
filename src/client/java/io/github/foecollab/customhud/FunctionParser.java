package io.github.foecollab.customhud;

/**
 * Splits a custom-HUD function placeholder such as {@code %condition.(<a>=<b>)%} into its
 * function name and left/right operands around the first top-level operator. Operands wrapped in
 * {@code <...>} are "bracketed" (the engine evaluates them recursively as nested placeholders);
 * unbracketed operands are literals. Ported from FishOnMC-Extras-R.
 */
public class FunctionParser {
    public static class FunctionPlaceholder {
        public String function;

        public Operator operator;

        public String left;
        public boolean leftBracketed;

        public String right;
        public boolean rightBracketed;

        public boolean isCorrect = true;

        public static FunctionPlaceholder getFalse() {
            FunctionPlaceholder result = new FunctionPlaceholder();
            result.isCorrect = false;
            return result;
        }
    }

    public static FunctionPlaceholder parse(String input) {
        input = input.trim();

        if (!input.startsWith("%") || !input.endsWith("%")) {
            return FunctionPlaceholder.getFalse();
        }

        String inner = input.substring(1, input.length() - 1);

        int dotIndex = inner.indexOf(".(");
        if (dotIndex == -1) {
            return FunctionPlaceholder.getFalse();
        }

        String function = inner.substring(0, dotIndex);
        String expr = inner.substring(dotIndex + 2, inner.length() - 1).trim();

        FunctionPlaceholder result = new FunctionPlaceholder();
        result.function = function;

        parseOperator(expr, result);

        return result;
    }

    private static void parseOperator(String expression, FunctionPlaceholder result) {
        int angleDepth = 0;
        int parenDepth = 0;

        for (int i = 0; i < expression.length(); i++) {
            if (i > 0 && angleDepth == 0 && parenDepth == 0) {
                Operator op = matchOperator(expression, i);

                if (op != null) {
                    String left = expression.substring(0, i).trim();
                    String right = expression.substring(i + op.symbol.length()).trim();

                    result.operator = op;

                    result.leftBracketed = isBracketed(left);
                    result.left = result.leftBracketed
                            ? stripBrackets(left)
                            : left;

                    result.rightBracketed = isBracketed(right);
                    result.right = result.rightBracketed
                            ? stripBrackets(right)
                            : right;

                    return;
                }
            }

            char c = expression.charAt(i);

            if (c == '<') {
                angleDepth++;
            } else if (c == '>') {
                angleDepth--;
            } else if (c == '(') {
                parenDepth++;
            } else if (c == ')') {
                parenDepth--;
            }
        }

        if (result.operator == null) {
            handleSingleValue(expression, result);
        }
    }

    private static void handleSingleValue(String expr, FunctionPlaceholder result) {
        expr = expr.trim();

        if (expr.isEmpty()) {
            result.left = "";
            result.leftBracketed = false;
            result.operator = null;
            result.right = null;
            result.rightBracketed = false;
            return;
        }

        result.leftBracketed = isBracketed(expr);
        result.left = result.leftBracketed
                ? stripBrackets(expr)
                : expr;

        result.operator = null;
        result.right = null;
        result.rightBracketed = false;
    }

    private static boolean isBracketed(String s) {
        if (!s.startsWith("<") || !s.endsWith(">")) {
            return false;
        }

        int depth = 0;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == '<') {
                depth++;
            } else if (c == '>') {
                depth--;
            }

            if (depth < 0) {
                return false;
            }

            // Only one top-level bracket block allowed
            if (depth == 0 && i != s.length() - 1) {
                return false;
            }
        }

        return depth == 0;
    }

    private static String stripBrackets(String s) {
        return s.substring(1, s.length() - 1);
    }

    private static Operator matchOperator(String s, int i) {
        Operator[] ops = Operator.values();

        for (Operator op : ops) {
            if (s.startsWith(op.symbol, i)) {
                return op;
            }
        }
        return null;
    }
}
