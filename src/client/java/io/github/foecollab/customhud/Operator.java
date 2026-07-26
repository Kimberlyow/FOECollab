package io.github.foecollab.customhud;

/**
 * Operators understood inside a custom-HUD function expression, e.g. {@code %condition.(<a>=<b>)%}
 * or {@code %expression.(<a>\+<b>)%}. Ported verbatim from FishOnMC-Extras-R; the math operators
 * carry a leading backslash so they don't clash with the comparison symbols when matched.
 */
public enum Operator {
    EQUAL("=="),
    SHORT_EQUAL("="),
    NOT_EQUAL("!="),
    GREATER_EQUAL(">="),
    LESS_EQUAL("<="),
    GREATER(">"),
    LESS("<"),

    SEPARATOR(","),

    ADDITION("\\+"),
    SUBTRACTION("\\-"),
    MULTIPLICATION("\\*"),
    DIVISION("\\/"),
    MODULO("\\%"),
    POWER("\\^");

    public final String symbol;

    Operator(String symbol) {
        this.symbol = symbol;
    }

    public static Operator fromSymbol(String symbol) {
        for (Operator op : values()) {
            if (op.symbol.equals(symbol)) {
                return op;
            }
        }
        return null;
    }
}
