package com.endrit.hdr;

import java.util.function.IntUnaryOperator;
import net.runelite.api.SceneTileModel;

public final class ColorAdjuster {
    private ColorAdjuster() {}

    public static void adjustSceneTileModel(SceneTileModel model, IntUnaryOperator colorAdjuster, boolean flatShading, boolean overrideColorEnabled, int overrideColor) {
        int[] colorA = model.getTriangleColorA();
        int[] colorB = model.getTriangleColorB();
        int[] colorC = model.getTriangleColorC();
        int[] textures = model.getTriangleTextureId();

        for (int i = 0; i < colorA.length; i++) {
            if (textures != null && textures.length > i && textures[i] != -1) {
                continue;
            }

            int newA = colorAdjuster.applyAsInt(colorA[i]);
            int newB = colorAdjuster.applyAsInt(colorB[i]);
            int newC = colorAdjuster.applyAsInt(colorC[i]);

            if (overrideColorEnabled) {
                newA = overrideColor;
                newB = overrideColor;
                newC = overrideColor;
            } else if (flatShading) {
                newB = newA;
                newC = newA;
            }

            colorA[i] = newA;
            colorB[i] = newB;
            colorC[i] = newC;
        }
    }

    /** Maps a color through the color map, except neutral colors (hue 0, saturation 0) when asked to keep them. */
    public static int adjustColor(ColorMap colorMap, int hsl, boolean keepNeutralColors) {
        return keepNeutralColors && isNeutralColor(hsl) ? hsl : colorMap.getModifiedHsl(hsl);
    }

    /* package */ static boolean isNeutralColor(int hsl) {
        return hsl >= 0 && hsl <= Colors.MAX_HSL
                && Colors.unpackJagexHue(hsl) == 0
                && Colors.unpackJagexSaturation(hsl) == 0;
    }
}
