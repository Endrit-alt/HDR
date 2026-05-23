package com.endrit.hdr;

import net.runelite.api.SceneTileModel;

public final class ColorAdjuster {
    private ColorAdjuster() {}

    public static void adjustSceneTileModel(SceneTileModel model, ColorMap colorMap, boolean flatShading, boolean overrideColorEnabled, int overrideColor) {
        int[] colorA = model.getTriangleColorA();
        int[] colorB = model.getTriangleColorB();
        int[] colorC = model.getTriangleColorC();
        int[] textures = model.getTriangleTextureId();

        for (int i = 0; i < colorA.length; i++) {
            if (textures != null && textures.length > i && textures[i] != -1) {
                continue;
            }

            int newA = colorMap.getModifiedHsl(colorA[i]);
            int newB = colorMap.getModifiedHsl(colorB[i]);
            int newC = colorMap.getModifiedHsl(colorC[i]);

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
}
