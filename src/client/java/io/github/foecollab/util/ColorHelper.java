package io.github.foecollab.util;

import me.shedaniel.math.Color;

public class ColorHelper {
    public static int getColorFromNbt(String value) {
        try {
            int r = Integer.parseInt(value.substring(0, value.indexOf(",")).trim());
            int g = Integer.parseInt(value.substring(value.indexOf(",") + 1, TextHelper.ordinalIndexOf(value, ",", 2)).trim());
            int b = Integer.parseInt(value.substring(value.lastIndexOf(",") + 1).trim());
            return Color.ofRGB(r, g, b).getColor();
        } catch (Exception e) {
            return Color.ofRGB(255, 255, 255).getColor();
        }
    }

    public static int getClrFromString(String key) {
        if (key == null || key.isBlank()) {
            return Color.ofRGB(255, 255, 255).getColor();
        }

        int hash = key.toLowerCase().hashCode();
        float hue = (hash & 0xFFFF) / 65535.0f;
        float saturation = 0.80f;
        float value = 1.00f;

        int rgb = hsvToRgb(hue, saturation, value);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return Color.ofRGB(r, g, b).getColor();
    }

    private static int hsvToRgb(float hue, float saturation, float value) {
        hue = hue - (float) Math.floor(hue);
        saturation = clamp01(saturation);
        value = clamp01(value);

        float h = hue * 6.0f;
        int i = (int) Math.floor(h);
        float f = h - i;
        float p = value * (1.0f - saturation);
        float q = value * (1.0f - saturation * f);
        float t = value * (1.0f - saturation * (1.0f - f));

        float r;
        float g;
        float b;

        switch (i % 6) {
            case 0:
                r = value;
                g = t;
                b = p;
                break;
            case 1:
                r = q;
                g = value;
                b = p;
                break;
            case 2:
                r = p;
                g = value;
                b = t;
                break;
            case 3:
                r = p;
                g = q;
                b = value;
                break;
            case 4:
                r = t;
                g = p;
                b = value;
                break;
            default:
                r = value;
                g = p;
                b = q;
                break;
        }

        int ri = (int) (r * 255.0f);
        int gi = (int) (g * 255.0f);
        int bi = (int) (b * 255.0f);
        return (ri << 16) | (gi << 8) | bi;
    }

    private static float clamp01(float v) {
        if (v < 0.0f) {
            return 0.0f;
        }
        if (v > 1.0f) {
            return 1.0f;
        }
        return v;
    }
}
