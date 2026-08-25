/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.autoinstallation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class KernelOptionsListTest {

    /**
     * Tests that an empty list returns an empty string.
     */
    @Test
    public void testEmptyInitialization() {
        KernelOptionsList list = new KernelOptionsList();
        assertEquals("", list.toString());
    }

    /**
     * Tests initialization with a string containing multiple options.
     */
    @Test
    public void testStringInitialization() {
        KernelOptionsList list = new KernelOptionsList("foo bar=baz");
        assertEquals("foo bar=baz", list.toString());
    }

    /**
     * Tests initialization with a Map containing string and list values.
     */
    @Test
    public void testMapInitialization() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("foo", "1");
        map.put("bar", Arrays.asList("2", "3"));
        map.put("flag", Collections.emptyList());
        map.put("nullflag", null);

        KernelOptionsList list = new KernelOptionsList(map);
        assertEquals("foo=1 bar=2 bar=3 flag nullflag", list.toString());
    }

    /**
     * Tests that the order of added options is preserved in the output.
     */
    @Test
    public void testOrderPreservation() {
        KernelOptionsList list = new KernelOptionsList();
        list.addOption("z", "1");
        list.addOption("a", "2");
        list.addOption("m", "3");
        assertEquals("z=1 a=2 m=3", list.toString());
    }

    /**
     * Tests that duplicate keys are preserved and sequential values are kept.
     */
    @Test
    public void testDuplicateKeysPreserved() {
        KernelOptionsList list = new KernelOptionsList();
        list.addOption("foo", "1");
        list.addOption("foo", "2");
        assertEquals("foo=1 foo=2", list.toString());
    }

    /**
     * Tests that setOption (with default behavior) ignores the new value if the key already exists.
     */
    @Test
    public void testSetOptionIgnoresExisting() {
        KernelOptionsList list = new KernelOptionsList();
        list.addOption("foo", "1");
        list.setOptionIfNotPresent("foo", "2");
        assertEquals("foo=1", list.toString());
    }

    /**
     * Tests that setOption with replace=true updates the first value and removes subsequent duplicates.
     */
    @Test
    public void testSetOptionReplacesExisting() {
        KernelOptionsList list = new KernelOptionsList();
        list.addOption("foo", "1");
        list.addOption("bar", "baz");
        list.addOption("foo", "2");
        list.setOptionOrReplace("foo", "3");
        assertEquals("foo=3 bar=baz", list.toString());
    }

    /**
     * Tests that remove deletes all occurrences of a specified key.
     */
    @Test
    public void testRemoveOption() {
        KernelOptionsList list = new KernelOptionsList();
        list.addOption("foo", "1");
        list.addOption("bar", "2");
        list.addOption("foo", "3");
        list.removeOption("foo");
        assertEquals("bar=2", list.toString());
    }

    /**
     * Tests that applyOverrides replaces matched keys and appends all options from the overrides list.
     */
    @Test
    public void testApplyOverrides() {
        KernelOptionsList base = new KernelOptionsList();
        base.addOption("foo", "1");
        base.addOption("bar", "2");
        base.addOption("console", "tty0");
        base.addOption("console", "tty1");

        KernelOptionsList overrides = new KernelOptionsList();
        overrides.addOption("console", "ttyS0");
        overrides.addOption("console", "tty0");
        overrides.addOption("foo", "3");

        base.applyOverrides(overrides);
        assertEquals("bar=2 console=ttyS0 console=tty0 foo=3", base.toString());
    }

    /**
     * Tests that addMissingOptions skip matched keys and appends all new options from the list.
     */
    @Test
    public void testAddMissingOptions() {
        KernelOptionsList base = new KernelOptionsList();
        base.addOption("foo", "1");
        base.addOption("bar", "2");
        base.addOption("console", "tty0");
        base.addOption("console", "tty1");

        KernelOptionsList newOptions = new KernelOptionsList();
        newOptions.addOption("console", "ttyS0");
        newOptions.addOption("console", "tty0");
        newOptions.addOption("foo", "3");
        newOptions.addOption("newfoo");

        base.addMissingOptions(newOptions);
        assertEquals("foo=1 bar=2 console=tty0 console=tty1 newfoo", base.toString());
    }

    /**
     * Tests adding both flag options (without values) and key-value options.
     */
    @Test
    public void testFlagsVsKeyValue() {
        KernelOptionsList list = new KernelOptionsList();
        list.addOption("flag1");
        list.addOption("key1", "val1");
        list.addOption("flag2");
        assertEquals("flag1 key1=val1 flag2", list.toString());
    }

    /**
     * Tests parsing of options where the value contains spaces enclosed in quotes.
     */
    @Test
    public void testQuotedSpaces() {
        KernelOptionsList list = new KernelOptionsList("foo=\"bar baz\" qux=1");
        assertEquals("foo=\"bar baz\" qux=1", list.toString());
    }

    /**
     * Tests parsing of complex options with varying spaces, flags, and quoted values.
     */
    @SuppressWarnings("checkstyle:LineLength")
    @Test
    public void testComplexQuotedSpaces() {
        KernelOptionsList list = new KernelOptionsList("  console=ttyS0,115200n8   net.ifnames=0 myflag  foo=\"bar baz xyz\" qux=1  ");
        assertEquals("console=ttyS0,115200n8 net.ifnames=0 myflag foo=\"bar baz xyz\" qux=1", list.toString());
    }

    /**
     * Test lookup of existing items
     */
    @Test
    public void testHasOption() {
        KernelOptionsList list = new KernelOptionsList();
        list.addOption("flag1");
        list.addOption("key1", "val1");
        list.addOption("flag2");
        assertTrue(list.hasOption("flag1"));
        assertTrue(list.hasOption("key1"));
        assertFalse(list.hasOption("unknown"));
    }
}
