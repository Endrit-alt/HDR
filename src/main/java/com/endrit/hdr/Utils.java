package com.endrit.hdr;

import java.util.*;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.*;

public final class Utils {
    private static final Pattern REGION_IDS_PATTERN = Pattern.compile("[,\\s\\n]");

    private Utils() {}

    public static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    public static List<Integer> parseRegionIds(String value) {
        return REGION_IDS_PATTERN
                .splitAsStream(value)
                .filter(Predicate.not(String::isEmpty))
                .map(Integer::valueOf)
                .collect(Collectors.toList());
    }

}
