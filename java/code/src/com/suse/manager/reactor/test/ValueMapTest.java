/**
 * Copyright (c) 2016 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.reactor.test;

import com.suse.manager.reactor.utils.ValueMap;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test for {@link com.suse.manager.reactor.utils.ValueMap}
 */
public class ValueMapTest extends TestCase {

    public void testGetValueAsMaxLengthString() {

        Map<String, Object> map = new HashMap<>();
        map.put("short", "12345");
        map.put("long", "a very very very very very very very very very very long string");

        ValueMap vmap = new ValueMap(map);
        assertEquals("12345", vmap.getValueAsString("short", 100));
        assertEquals("12345", vmap.getValueAsString("short", 5));
        assertEquals("a very", vmap.getValueAsString("long", 6));
        assertEquals("a", vmap.getValueAsString("long", 1));
        assertEquals("", vmap.getValueAsString(null, 1));
    }

    public void testGetCollectionValueAsString() {
        Map<String, Object> map = new HashMap<>();
        map.put("stringList", Arrays.asList("one", "two", "three"));
        map.put("longList", Arrays.asList(1L, 2L, 3L));
        map.put("doubleList", Arrays.asList(1.111d, 2.222d, 3.333d));
        map.put("nestedList", Arrays.asList(Arrays.asList("one"), Arrays.asList("two"),
                Arrays.asList("three four")));

        List<List> deeplyNested = new ArrayList<>();
        List<List> l = deeplyNested;
        for (int i = 0; i < 12; i++) {
            l.add(new ArrayList<>());
            l = l.get(0);
        }
        l.add(Arrays.asList("one"));

        map.put("deeplyNestedList", deeplyNested);
        map.put("empty", Collections.emptySet());
        map.put("emptyString", Arrays.asList(""));
        map.put("twoEmptyStrings", Arrays.asList("", ""));

        ValueMap vmap = new ValueMap(map);
        assertEquals("one two three", vmap.getValueAsString("stringList"));
        assertEquals("1 2 3", vmap.getValueAsString("longList"));
        assertEquals("1.11 2.22 3.33", vmap.getValueAsString("doubleList"));
        assertEquals("", vmap.getValueAsString("empty"));
        assertEquals("", vmap.getValueAsString("emptyString"));
        assertEquals(" ", vmap.getValueAsString("twoEmptyStrings"));
        assertEquals("one two three four", vmap.getValueAsString("nestedList"));
        assertEquals("", vmap.getValueAsString("deeplyNestedList"));

    }

}
