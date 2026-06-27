package com.endrit.hdr;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UtilsTest {

    @Test
    public void testParseRegionIdsMixedFormat() {
        String value = "1,   2, 3,4,5\n6\n\n7";
        List<Integer> parsed = Utils.parseRegionIds(value);
        assertEquals(7, parsed.size());
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7), parsed);
    }

    @Test
    public void testParseHiddenTileKeysMixedFormat() {
        Set<Integer> parsed = HDRPlugin.parseHiddenTileKeys("9776:12:14:0, tileKey(13136, 61, 50, 0)\n"
                + "215220274, 215220275");

        assertEquals(4, parsed.size());
        assertTrue(parsed.contains(160_170_766));
        assertTrue(parsed.contains(215_224_178));
        assertTrue(parsed.contains(215_220_274));
        assertTrue(parsed.contains(215_220_275));
    }

}
