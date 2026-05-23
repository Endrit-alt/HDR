package com.endrit.hdr;

public final class Colors {
    public static final int MIN_HUE = 0;
    public static final int MAX_HUE = 63;
    public static final int MIN_SATURATION = 0;
    public static final int MAX_SATURATION = 7;
    public static final int MIN_LIGHTNESS = 0;
    public static final int MAX_LIGHTNESS = 127;

    public static final int MIN_HSL = packJagexHsl(MIN_HUE, MIN_SATURATION, MIN_LIGHTNESS);
    public static final int MAX_HSL = packJagexHsl(MAX_HUE, MAX_SATURATION, MAX_LIGHTNESS);

    private Colors() {}

    public static int[] getUnpackedJagexHsl(int jagexHsl) {
        int hue = unpackJagexHue(jagexHsl);
        int saturation = unpackJagexSaturation(jagexHsl);
        int lightness = unpackJagexLightness(jagexHsl);
        return new int[] { hue, saturation, lightness };
    }

    public static int packJagexHsl(int hue, int saturation, int lightness) {
        return hue << 10 | saturation << 7 | lightness;
    }

    public static int unpackJagexHue(int jagexHsl) {
        return jagexHsl >> 10 & 0x3F;
    }

    public static int unpackJagexSaturation(int jagexHsl) {
        return jagexHsl >> 7 & 7;
    }

    public static int unpackJagexLightness(int jagexHsl) {
        return jagexHsl & 0x7F;
    }

    @SuppressWarnings({"PMD.ShortVariable", "PMD.ControlStatementBraces", "PMD.CyclomaticComplexity", "PMD.NPathComplexity", "PMD.AvoidLiteralsInIfCondition"})
    public static int colorToJagexHsl(java.awt.Color color) {
        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));

        float l = (max + min) / 2.0f;
        float h = 0f;
        float s = 0f;

        if (max != min) {
            float d = max - min;
            s = l > 0.5f ? d / (2.0f - max - min) : d / (max + min);
            if (max == r) h = (g - b) / d + (g < b ? 6.0f : 0.0f);
            else if (max == g) h = (b - r) / d + 2.0f;
            else if (max == b) h = (r - g) / d + 4.0f;
            h /= 6.0f;
        }

        int jagexHue = (int) (h * 63);
        int jagexSat = (int) (s * 7);
        int jagexLig = (int) (l * 127);

        return packJagexHsl(
                Math.max(0, Math.min(63, jagexHue)),
                Math.max(0, Math.min(7, jagexSat)),
                Math.max(0, Math.min(127, jagexLig))
        );
    }
}
