package com.endrit.hdr;

import java.awt.Color;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ColorMapTest {

    @Test
    public void targetedLightnessReductionIgnoresColorsBelowThreshold() {
        ColorMap colorMap = new ColorMap();
        int color = Colors.packJagexHsl(10, 3, 50);

        colorMap.updateColors(new ColorMap.Settings().withLightness(50, 80, 0, -1, 0));

        assertEquals(color, colorMap.getModifiedHsl(color));
    }

    @Test
    public void targetedLightnessReductionDarkensColorsAboveThreshold() {
        ColorMap colorMap = new ColorMap();
        int color = Colors.packJagexHsl(10, 3, Colors.MAX_LIGHTNESS);

        colorMap.updateColors(new ColorMap.Settings().withLightness(50, 80, 0, -1, 0));

        int modified = colorMap.getModifiedHsl(color);
        assertTrue(Colors.unpackJagexLightness(modified) < Colors.MAX_LIGHTNESS);
    }

    @Test
    public void finalLightnessAdjustmentAppliesAfterProfileChanges() {
        ColorMap colorMap = new ColorMap();
        int color = Colors.packJagexHsl(10, 3, Colors.MAX_LIGHTNESS);

        colorMap.updateColors(new ColorMap.Settings().withLightness(50, 80, 0, -1, 10));

        int modified = colorMap.getModifiedHsl(color);
        assertEquals(74, Colors.unpackJagexLightness(modified));
    }

    @Test
    public void targetedSaturationAdjustmentDesaturatesMatchingHue() {
        ColorMap colorMap = new ColorMap();
        int yellowHue = Colors.unpackJagexHue(Colors.colorToJagexHsl(Color.YELLOW));
        int yellow = Colors.packJagexHsl(yellowHue, Colors.MAX_SATURATION, 60);

        colorMap.updateColors(new ColorMap.Settings().withTargetSaturation(-100, yellowHue, 2));

        int modified = colorMap.getModifiedHsl(yellow);
        assertEquals(Colors.MIN_SATURATION, Colors.unpackJagexSaturation(modified));
    }

    @Test
    public void targetedSaturationAdjustmentIgnoresHueOutsideRange() {
        ColorMap colorMap = new ColorMap();
        int yellowHue = Colors.unpackJagexHue(Colors.colorToJagexHsl(Color.YELLOW));
        int blueHue = Colors.unpackJagexHue(Colors.colorToJagexHsl(Color.BLUE));
        int blue = Colors.packJagexHsl(blueHue, Colors.MAX_SATURATION, 60);

        colorMap.updateColors(new ColorMap.Settings().withTargetSaturation(-100, yellowHue, 2));

        assertEquals(blue, colorMap.getModifiedHsl(blue));
    }
}
