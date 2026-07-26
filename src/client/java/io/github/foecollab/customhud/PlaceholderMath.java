package io.github.foecollab.customhud;

/** Comparison and arithmetic evaluation for the custom-HUD {@code condition}/{@code expression} functions. */
public class PlaceholderMath {
    public static boolean checkOperation(Operator operator, float leftValue, float rightValue) {
        return switch (operator) {
            case GREATER -> leftValue > rightValue;
            case LESS -> leftValue < rightValue;
            case EQUAL, SHORT_EQUAL -> leftValue == rightValue;
            case NOT_EQUAL -> leftValue != rightValue;
            case GREATER_EQUAL -> leftValue >= rightValue;
            case LESS_EQUAL -> leftValue <= rightValue;
            default -> false;
        };
    }

    public static float checkExpression(Operator operator, float leftValue, float rightValue) {
        return switch (operator) {
            case ADDITION -> leftValue + rightValue;
            case SUBTRACTION -> leftValue - rightValue;
            case MULTIPLICATION -> leftValue * rightValue;
            case DIVISION -> leftValue / rightValue;
            case MODULO -> leftValue % rightValue;
            case POWER -> (float) Math.pow(leftValue, rightValue);
            default -> Float.MIN_VALUE;
        };
    }
}
