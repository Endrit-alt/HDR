package com.endrit.hdr;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class UtilsTest {

    @Test
    public void testParseRegionIdsMixedFormat() {
        String value = "1,   2, 3,4,5\n6\n\n7";
        List<Integer> parsed = Utils.parseRegionIds(value);
        assertEquals(7, parsed.size());
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7), parsed);
    }

}
