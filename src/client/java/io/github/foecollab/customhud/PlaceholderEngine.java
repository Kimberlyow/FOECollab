package io.github.foecollab.customhud;

import io.github.foecollab.customhud.FunctionParser.FunctionPlaceholder;
import io.github.foecollab.customhud.PlaceholderValue.ComponentValue;
import io.github.foecollab.customhud.PlaceholderValue.StringValue;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * Resolves the custom-HUD "custom code" language — the {@code %...%} placeholders and
 * {@code %function.(...)%} expressions a user types into a custom HUD line. Faithfully ported from
 * DannyPX's FishOnMC-Extras-R {@code PlaceholderHandler} (with permission) and Yarn-mapped.
 *
 * <p>A line resolves to a {@link ParseResult}: a built {@link MutableText} plus a {@code complete}
 * flag. A line whose placeholders didn't all resolve (a false {@code condition}, missing data, ...)
 * is {@code complete = false} — the renderer hides such lines, which is how conditions act as
 * per-line filters.</p>
 */
public class PlaceholderEngine {
    /** A resolved placeholder/function value plus whether it was actually present. */
    public record ValueResult(boolean present, PlaceholderValue value) {
        public static ValueResult str(String s) {
            return new ValueResult(true, new StringValue(s));
        }

        public static ValueResult comp(Text t) {
            return new ValueResult(true, new ComponentValue(t));
        }

        public static ValueResult bool(boolean b) {
            return new ValueResult(b, new StringValue(""));
        }

        public static ValueResult absent() {
            return new ValueResult(false, new StringValue(""));
        }

        /** Present only when {@code s} is non-blank — blank data hides the line that used it. */
        public static ValueResult of(String s) {
            return s == null || s.isBlank() ? absent() : str(s);
        }
    }

    /** A resolved line: its text plus whether every placeholder in it resolved. */
    public record ParseResult(boolean complete, MutableText text) {
    }

    // Guards against runaway recursion from a pathological (e.g. self-referential) template.
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);
    private static final int MAX_DEPTH = 48;

    private static final Map<String, Function<FunctionPlaceholder, ValueResult>> FUNCTIONS = Map.ofEntries(
            // Boolean
            Map.entry("condition", PlaceholderEngine::condition),
            Map.entry("is_blank", p -> isBlank(p, true)),
            Map.entry("is_not_blank", p -> isBlank(p, false)),
            Map.entry("or", PlaceholderEngine::or),
            Map.entry("and", PlaceholderEngine::and),
            Map.entry("not", PlaceholderEngine::not),
            Map.entry("xor", PlaceholderEngine::xor),
            // String
            Map.entry("substring_front", p -> substring(p, true)),
            Map.entry("substring_back", p -> substring(p, false)),
            Map.entry("index_of", PlaceholderEngine::indexOf),
            // Math
            Map.entry("expression", PlaceholderEngine::expression),
            Map.entry("max", p -> minMax(p, true)),
            Map.entry("min", p -> minMax(p, false)),
            Map.entry("abs", p -> unaryMath(p, Math::abs)),
            Map.entry("ceil", p -> unaryMath(p, v -> (float) Math.ceil(v))),
            Map.entry("floor", p -> unaryMath(p, v -> (float) Math.floor(v))),
            Map.entry("round", PlaceholderEngine::round));

    /** Resolves one custom-HUD line template (already {@code §}-coded) to a styled component. */
    public static ParseResult parse(String input) {
        int depth = DEPTH.get();
        if (depth > MAX_DEPTH) {
            return new ParseResult(false, Text.empty());
        }
        DEPTH.set(depth + 1);
        try {
            return parse0(input);
        } catch (Exception e) {
            // A malformed template should never crash the HUD — treat it as unresolved.
            return new ParseResult(false, Text.literal(input));
        } finally {
            DEPTH.set(depth);
        }
    }

    private static ParseResult parse0(String input) {
        boolean complete = true;

        MutableText result = Text.empty();
        int lastEnd = 0;
        Style activeStyle = Style.EMPTY;

        while (complete) {
            int start = -1;
            int end = -1;

            for (int i = lastEnd; i < input.length(); i++) {
                if (input.charAt(i) == '%' && (i == 0 || input.charAt(i - 1) != '\\')) {
                    if (start == -1) {
                        start = i;
                    } else {
                        end = i;
                        break;
                    }
                }
            }

            if (start == -1 || end == -1) {
                break;
            }

            if (start > lastEnd) {
                StyledText.StyledResult parsed = StyledText.parseLegacy(input.substring(lastEnd, start), activeStyle);
                result.append(parsed.text());
                activeStyle = parsed.style();
            }

            String full = input.substring(start + 1, end);
            String[] parts = full.split("\\.");
            String identifier = parts[0];
            String[] parameters = Arrays.copyOfRange(parts, 1, parts.length);

            ValueResult functionResult = null;

            if (PlaceholderSources.SOURCES.containsKey(identifier)) {
                functionResult = PlaceholderSources.SOURCES.get(identifier).apply(parameters);
            } else if (FUNCTIONS.containsKey(identifier)) {
                functionResult = evalFunction("%" + full + "%");
            }

            if (functionResult != null && functionResult.present()) {
                switch (functionResult.value()) {
                    case StringValue stringValue -> {
                        StyledText.StyledResult parsed = StyledText.parseLegacy(stringValue.value(), activeStyle);
                        result.append(parsed.text());
                        activeStyle = parsed.style();
                    }
                    case ComponentValue componentValue -> {
                        result.append(componentValue.value().copy());
                        activeStyle = componentValue.value().getStyle();
                    }
                }
            } else {
                result.append(Text.literal(input.substring(start, end + 1)).setStyle(activeStyle));
                complete = false;
            }

            lastEnd = end + 1;
        }

        if (lastEnd < input.length()) {
            StyledText.StyledResult parsed = StyledText.parseLegacy(input.substring(lastEnd), activeStyle);
            result.append(parsed.text());
        }

        return new ParseResult(complete, result);
    }

    private static ValueResult evalFunction(String placeholder) {
        FunctionPlaceholder functionPlaceholder = FunctionParser.parse(placeholder);
        if (!functionPlaceholder.isCorrect) {
            return ValueResult.absent();
        }
        Function<FunctionPlaceholder, ValueResult> function = FUNCTIONS.get(functionPlaceholder.function);
        return function != null ? function.apply(functionPlaceholder) : ValueResult.absent();
    }

    // region Functions
    private static String resolveOperand(String operand, boolean bracketed) {
        return bracketed ? parse("%" + operand + "%").text().getString() : operand;
    }

    private static ParseResult resolveOperandResult(String operand, boolean bracketed) {
        return bracketed ? parse("%" + operand + "%") : new ParseResult(true, Text.literal(operand));
    }

    private static boolean resolveOperandTruth(String operand, boolean bracketed) {
        return bracketed ? parse("%" + operand + "%").complete() : Boolean.parseBoolean(operand);
    }

    private static ValueResult condition(FunctionPlaceholder p) {
        if (p.operator != null && p.left != null && p.right != null) {
            String left = resolveOperand(p.left, p.leftBracketed);
            String right = resolveOperand(p.right, p.rightBracketed);

            try {
                return ValueResult.bool(PlaceholderMath.checkOperation(p.operator, Float.parseFloat(left), Float.parseFloat(right)));
            } catch (NumberFormatException e) {
                return switch (p.operator) {
                    case SHORT_EQUAL -> ValueResult.bool(left.contains(right));
                    case EQUAL -> ValueResult.bool(left.equals(right));
                    case NOT_EQUAL -> ValueResult.bool(!left.equals(right));
                    default -> ValueResult.absent();
                };
            }
        }
        return ValueResult.absent();
    }

    private static ValueResult isBlank(FunctionPlaceholder p, boolean wantBlank) {
        if (p.operator == null && p.left != null && p.right == null) {
            String left;
            if (p.leftBracketed) {
                ParseResult parsed = parse("%" + p.left + "%");
                left = parsed.complete() ? parsed.text().getString() : "";
            } else {
                left = p.left;
            }
            return ValueResult.bool(left.isBlank() == wantBlank);
        }
        return ValueResult.absent();
    }

    private static ValueResult substring(FunctionPlaceholder p, boolean front) {
        if (p.operator == Operator.SEPARATOR && p.left != null && p.right != null) {
            ParseResult left = resolveOperandResult(p.left, p.leftBracketed);
            try {
                if (left.complete()) {
                    int amount = (int) Float.parseFloat(resolveOperand(p.right, p.rightBracketed));
                    if (front) {
                        return ValueResult.comp(StyledText.substring(left.text(), 0, amount));
                    }
                    return ValueResult.comp(StyledText.substring(left.text(), amount, left.text().getString().length()));
                }
                return ValueResult.absent();
            } catch (NumberFormatException e) {
                return ValueResult.absent();
            }
        }
        return ValueResult.absent();
    }

    private static ValueResult indexOf(FunctionPlaceholder p) {
        if (p.operator == Operator.SEPARATOR && p.left != null && p.right != null) {
            ParseResult left = resolveOperandResult(p.left, p.leftBracketed);
            ParseResult right = resolveOperandResult(p.right, p.rightBracketed);

            if (left.complete() && right.complete()) {
                int index = left.text().getString().indexOf(right.text().getString());
                return index == -1 ? ValueResult.absent() : ValueResult.str(String.valueOf(index));
            }
        }
        return ValueResult.absent();
    }

    private static ValueResult or(FunctionPlaceholder p) {
        if (p.operator == Operator.SEPARATOR && p.left != null && p.right != null) {
            return ValueResult.bool(resolveOperandTruth(p.left, p.leftBracketed) || resolveOperandTruth(p.right, p.rightBracketed));
        }
        return ValueResult.absent();
    }

    private static ValueResult and(FunctionPlaceholder p) {
        if (p.operator == Operator.SEPARATOR && p.left != null && p.right != null) {
            return ValueResult.bool(resolveOperandTruth(p.left, p.leftBracketed) && resolveOperandTruth(p.right, p.rightBracketed));
        }
        return ValueResult.absent();
    }

    private static ValueResult xor(FunctionPlaceholder p) {
        if (p.operator == Operator.SEPARATOR && p.left != null && p.right != null) {
            return ValueResult.bool(resolveOperandTruth(p.left, p.leftBracketed) ^ resolveOperandTruth(p.right, p.rightBracketed));
        }
        return ValueResult.absent();
    }

    private static ValueResult not(FunctionPlaceholder p) {
        if (p.operator == null && p.left != null && p.right == null) {
            return ValueResult.bool(!resolveOperandTruth(p.left, p.leftBracketed));
        }
        return ValueResult.absent();
    }

    private static ValueResult expression(FunctionPlaceholder p) {
        if (p.operator != null && p.left != null && p.right != null) {
            try {
                float left = Float.parseFloat(resolveOperandResult(p.left, p.leftBracketed).text().getString());
                float right = Float.parseFloat(resolveOperandResult(p.right, p.rightBracketed).text().getString());
                float result = PlaceholderMath.checkExpression(p.operator, left, right);
                if (result != Float.MIN_VALUE) {
                    return ValueResult.str(String.format(Locale.US, "%f", result));
                }
            } catch (NumberFormatException e) {
                return ValueResult.absent();
            }
        }
        return ValueResult.absent();
    }

    private static ValueResult minMax(FunctionPlaceholder p, boolean max) {
        if (p.operator == Operator.SEPARATOR && p.left != null && p.right != null) {
            try {
                float left = Float.parseFloat(resolveOperandResult(p.left, p.leftBracketed).text().getString());
                float right = Float.parseFloat(resolveOperandResult(p.right, p.rightBracketed).text().getString());
                float result = max ? Math.max(left, right) : Math.min(left, right);
                return ValueResult.str(String.format(Locale.US, "%f", result));
            } catch (NumberFormatException e) {
                return ValueResult.absent();
            }
        }
        return ValueResult.absent();
    }

    private static ValueResult unaryMath(FunctionPlaceholder p, Function<Float, Float> op) {
        if (p.operator == null && p.left != null && p.right == null) {
            try {
                float left = Float.parseFloat(resolveOperandResult(p.left, p.leftBracketed).text().getString());
                return ValueResult.str(String.format(Locale.US, "%f", op.apply(left)));
            } catch (NumberFormatException e) {
                return ValueResult.absent();
            }
        }
        return ValueResult.absent();
    }

    private static ValueResult round(FunctionPlaceholder p) {
        if (p.operator == Operator.SEPARATOR && p.left != null && p.right != null) {
            try {
                float left = Float.parseFloat(resolveOperandResult(p.left, p.leftBracketed).text().getString());
                int decimals = Integer.parseInt(resolveOperandResult(p.right, p.rightBracketed).text().getString());
                if (decimals < 0) {
                    return ValueResult.absent();
                }
                BigDecimal bd = new BigDecimal(Double.toString(left)).setScale(decimals, RoundingMode.HALF_UP);
                return ValueResult.str(StyledText.floatToString((float) bd.doubleValue(), decimals));
            } catch (NumberFormatException e) {
                return ValueResult.absent();
            }
        }
        return ValueResult.absent();
    }
    // endregion
}
