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
import java.util.stream.Collectors;

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

    public void testGetValueAsCollection() {
        Map<String, Object> map = new HashMap<>();
        map.put("list", Arrays.asList("one"));
        map.put("string", "a string");

        ValueMap vmap = new ValueMap(map);
        assertTrue(vmap.getValueAsCollection("list").isPresent());
        assertEquals(map.get("list"), vmap.getValueAsCollection("list").get());
        assertFalse(vmap.getValueAsCollection("string").isPresent());
    }

}
