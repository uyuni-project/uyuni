/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.utils.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.suse.utils.CustomCollectors;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class CustomCollectorsTest {

    @Test
    public void testNullSafeGroupingBy() {
        Map<Long, List<String>> collectorResult = Stream.of(
            new Data(1L, "test-1"),
            new Data(1L, "test-2"),
            new Data(1L, "test-3"),
            new Data(5L, "test-4"),
            new Data(null, "test-5"),
            new Data(null, "test-6")
        ).collect(CustomCollectors.nullSafeGroupingBy(Data::orgId, Data::label));

        // Can't use Map.of(), it doesn't allow null
        Map<Long, List<String>> expectedMap = new HashMap<>();
        expectedMap.put(1L, List.of("test-1", "test-2", "test-3"));
        expectedMap.put(5L, List.of("test-4"));
        expectedMap.put(null, List.of("test-5", "test-6"));

        assertEquals(expectedMap, collectorResult);
    }

    @Test
    public void testNullSafeGroupingBySingleParameter() {
        Map<Character, List<String>> collectorResult = Stream.of(
            "Bright",
            "snakes",
            "slid",
            "silly",
            "birds",
            "flew",
            "high",
            "fast"
        ).collect(CustomCollectors.nullSafeGroupingBy(word -> Character.toLowerCase(word.charAt(0))));

        assertEquals(Map.of(
            's', List.of("snakes", "slid", "silly"),
            'b', List.of("Bright", "birds"),
            'f', List.of("flew", "fast"),
            'h', List.of("high")
        ), collectorResult);
    }

    private record Data(Long orgId, String label) { }
}
