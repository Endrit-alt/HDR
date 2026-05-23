package com.endrit.hdr;

import java.util.Objects;

public class ColorMap {
    private final Integer[] modifiedColors;

    private Settings lastSettings = new Settings();

    public ColorMap() {
        this.modifiedColors = new Integer[Colors.MAX_HSL];
    }

    public void updateColors(Settings settings) {
        if (settings.equals(lastSettings)) {
            return;
        }

        for (int hsl = 0; hsl < modifiedColors.length; hsl++) {
            int modified = getNewHsl(hsl, settings);
            modifiedColors[hsl] = modified;
        }

        this.lastSettings = new Settings(settings);
    }

    public int getModifiedHsl(int hsl) {
        if (hsl == 12_345_678 || hsl < 0 || hsl > modifiedColors.length - 1) {
            return hsl;
        }
        Integer modified = modifiedColors[hsl];
        return modified == null ? hsl : modified;
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    private int getNewHsl(int hsl, Settings settings) {
        if (hsl == 12_345_678 || hsl < Colors.MIN_HSL || hsl > Colors.MAX_HSL) {
            return hsl;
        }

        if (settings.hasNoAdjustments()) {
            return hsl;
        }

        int[] unpackedHsl = Colors.getUnpackedJagexHsl(hsl);
        int originalHue = unpackedHsl[0];
        int originalSaturation = unpackedHsl[1];
        int originalLightness = unpackedHsl[2];

        int newHue = getReducedAmount(originalHue, settings.hueReduction, Colors.MIN_HUE, Colors.MAX_HUE);
        int newSaturation = getReducedAmount(
            originalSaturation,
            settings.saturationReduction,
            Colors.MIN_SATURATION,
            Colors.MAX_SATURATION);

        if (isHueWithinRange(originalHue, settings.targetSaturationHue, settings.targetSaturationHueRange)) {
            newSaturation = getAdjustedSaturation(newSaturation, settings.targetSaturationAdjustment);
        }

        int newLightness = originalLightness;

        if (settings.lightnessReduction > 0 && originalLightness >= settings.lightnessReductionThreshold) {
            newLightness = getTargetedReducedLightness(
                newLightness,
                originalLightness,
                settings.lightnessReduction,
                settings.lightnessReductionThreshold);
        }

        if (settings.lightnessIncrease > 0 && originalLightness <= settings.lightnessIncreaseThreshold) {
            double factor = (settings.lightnessIncrease / 100.0) * 0.3;
            // Smoothly fade out the effect as lightness approaches the threshold
            double fade = 1.0 - ((double) originalLightness / Math.max(1, settings.lightnessIncreaseThreshold));
            int boost = (int) Math.ceil((Colors.MAX_LIGHTNESS - newLightness) * factor * fade);
            newLightness = Utils.clamp(newLightness + boost, Colors.MIN_LIGHTNESS, Colors.MAX_LIGHTNESS);
        }

        if (settings.finalLightnessAdjustment != 0) {
            newLightness = Utils.clamp(
                newLightness + settings.finalLightnessAdjustment,
                Colors.MIN_LIGHTNESS,
                Colors.MAX_LIGHTNESS);
        }

        return Colors.packJagexHsl(newHue, newSaturation, newLightness);
    }

    private int getReducedAmount(int value, int reductionPercent, int min, int max) {
        return Utils.clamp(
            (int) Math.ceil(((100 - reductionPercent) / 100.0) * value),
            min,
            max);
    }

    private int getTargetedReducedLightness(int lightness, int originalLightness, int reductionPercent, int threshold) {
        int reducedLightness = getReducedAmount(lightness, reductionPercent, Colors.MIN_LIGHTNESS, Colors.MAX_LIGHTNESS);
        double range = Math.max(1, Colors.MAX_LIGHTNESS - threshold);
        double fade = (originalLightness - threshold) / range;
        int reduction = (int) Math.ceil((lightness - reducedLightness) * fade);
        return Utils.clamp(lightness - reduction, Colors.MIN_LIGHTNESS, Colors.MAX_LIGHTNESS);
    }

    private int getAdjustedSaturation(int saturation, int adjustmentPercent) {
        if (adjustmentPercent < 0) {
            return getReducedAmount(saturation, -adjustmentPercent, Colors.MIN_SATURATION, Colors.MAX_SATURATION);
        }

        int increase = (int) Math.ceil((Colors.MAX_SATURATION - saturation) * (adjustmentPercent / 100.0));
        return Utils.clamp(saturation + increase, Colors.MIN_SATURATION, Colors.MAX_SATURATION);
    }

    private static boolean isHueWithinRange(int hue, int targetHue, int hueRange) {
        if (hueRange >= (Colors.MAX_HUE + 1) / 2) {
            return true;
        }

        int distance = Math.abs(hue - targetHue);
        int circularDistance = Math.min(distance, Colors.MAX_HUE + 1 - distance);
        return circularDistance <= hueRange;
    }

    public static final class Settings {
        private int hueReduction;
        private int saturationReduction;
        private int lightnessReduction;
        private int lightnessReductionThreshold;
        private int lightnessIncrease;
        private int lightnessIncreaseThreshold;
        private int finalLightnessAdjustment;
        private int targetSaturationAdjustment;
        private int targetSaturationHue;
        private int targetSaturationHueRange;

        public Settings() {
        }

        private Settings(Settings settings) {
            this.hueReduction = settings.hueReduction;
            this.saturationReduction = settings.saturationReduction;
            this.lightnessReduction = settings.lightnessReduction;
            this.lightnessReductionThreshold = settings.lightnessReductionThreshold;
            this.lightnessIncrease = settings.lightnessIncrease;
            this.lightnessIncreaseThreshold = settings.lightnessIncreaseThreshold;
            this.finalLightnessAdjustment = settings.finalLightnessAdjustment;
            this.targetSaturationAdjustment = settings.targetSaturationAdjustment;
            this.targetSaturationHue = settings.targetSaturationHue;
            this.targetSaturationHueRange = settings.targetSaturationHueRange;
        }

        public Settings withBaseAdjustments(int hueReduction, int saturationReduction) {
            this.hueReduction = hueReduction;
            this.saturationReduction = saturationReduction;
            return this;
        }

        public Settings withLightness(
            int reduction,
            int reductionThreshold,
            int increase,
            int increaseThreshold,
            int finalAdjustment) {
            this.lightnessReduction = reduction;
            this.lightnessReductionThreshold = reductionThreshold;
            this.lightnessIncrease = increase;
            this.lightnessIncreaseThreshold = increaseThreshold;
            this.finalLightnessAdjustment = finalAdjustment;
            return this;
        }

        public Settings withTargetSaturation(int adjustment, int hue, int hueRange) {
            this.targetSaturationAdjustment = adjustment;
            this.targetSaturationHue = hue;
            this.targetSaturationHueRange = hueRange;
            return this;
        }

        private boolean hasNoAdjustments() {
            return hueReduction == 0
                && saturationReduction == 0
                && lightnessReduction == 0
                && lightnessIncrease == 0
                && finalLightnessAdjustment == 0
                && targetSaturationAdjustment == 0;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Settings)) {
                return false;
            }
            Settings settings = (Settings) other;
            return hueReduction == settings.hueReduction
                && saturationReduction == settings.saturationReduction
                && lightnessReduction == settings.lightnessReduction
                && lightnessReductionThreshold == settings.lightnessReductionThreshold
                && lightnessIncrease == settings.lightnessIncrease
                && lightnessIncreaseThreshold == settings.lightnessIncreaseThreshold
                && finalLightnessAdjustment == settings.finalLightnessAdjustment
                && targetSaturationAdjustment == settings.targetSaturationAdjustment
                && targetSaturationHue == settings.targetSaturationHue
                && targetSaturationHueRange == settings.targetSaturationHueRange;
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                hueReduction,
                saturationReduction,
                lightnessReduction,
                lightnessReductionThreshold,
                lightnessIncrease,
                lightnessIncreaseThreshold,
                finalLightnessAdjustment,
                targetSaturationAdjustment,
                targetSaturationHue,
                targetSaturationHueRange);
        }
    }
}
