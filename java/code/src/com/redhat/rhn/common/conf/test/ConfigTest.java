/*
 * Copyright (c) 2009--2017 Red Hat, Inc.
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

package com.redhat.rhn.common.conf.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class ConfigTest extends RhnBaseTestCase {
    static final String TEST_KEY = "user";
    static final String TEST_VALUE = "newval";
    static final String TEST_CONF_LOCATION = "/usr/share/rhn/unit-tests/";
    private Config c;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        // create test config path
        String confPath = "/tmp/" + TestUtils.randomString();
        new File(confPath + "/conf/default").mkdirs();

        ArrayList<String> paths = new ArrayList<>();
        paths.add("conf/rhn.conf");
        paths.add("conf/default/rhn_web.conf");
        paths.add("conf/default/rhn_prefix.conf");
        paths.add("conf/default/bug154517.conf.rpmsave");

        // copy test configuration files over
        for (String relPath : paths) {
            try {
                FileUtils.copyURLToFile(TestUtils.findTestData(relPath),
                        new File(confPath, relPath));
            }
            catch (NullPointerException e) {
                FileUtils.copyFile(new File(TEST_CONF_LOCATION + relPath),
                        new File(confPath, relPath));
            }

        }

        c = new Config(List.of(confPath + "/conf", confPath + "/conf/default"));
    }

    /**
     * define a value in rhn_web.conf with a prefix, call get using the
     * fully qualified property name.
     * define value in rhn_web.conf without a prefix, call get fully qualified.
     */
    @Test
    public void testGetFullyQualified() {
        assertEquals("this is a property with a prefix",
                     c.getString("web.property_with_prefix"));
        assertEquals("this is a property without a prefix",
                     c.getString("web.without_prefix"));
    }

    /**
     * define a value in rhn_web.conf with a prefix, call get using only the
     * property name.
     * define value in rhn_web.conf without a prefix, call get with just prop name.
     */
    @Test
    public void testGetByPropertyNameOnly() {
        assertEquals("this is a property with a prefix",
                     c.getString("property_with_prefix"));
        assertEquals("this is a property without a prefix",
                     c.getString("without_prefix"));
    }

    /**
     * property defined fully qualifed in rhn_web.conf,
     * overridden without prefix in rhn.conf,
     * Accessed fully qualified.
     */
    @Test
    public void testOverride() {
        assertEquals("keep", c.getString("web.to_override"));
    }

    /**
     * property defined fully qualifed in rhn_web.conf,
     * overridden without prefix in rhn.conf,
     * Accessed by property name only.
     */
    @Test
    public void testOverride1() {
        assertEquals("keep", c.getString("to_override"));
    }

    /**
     * property defined fully qualifed in rhn_web.conf
     * overridden fully qualfied in rhn.conf.
     * Accessed fully qualified.
     */
    @Test
    public void testOverride2() {
        assertEquals("1", c.getString("web.fq_to_override"));
    }

    /**
     * property defined fully qualifed in rhn_web.conf
     * overridden fully qualfied in rhn.conf.
     * Accessed by property name only.
     */
    @Test
    public void testOverride3() {
        assertEquals("1", c.getString("fq_to_override"));
    }

    /**
     * property defined without a prefix in rhn_web.conf
     * overridden fully qualfied in rhn.conf.
     * Accessed fully qualified.
     */
    @Test
    public void testOverride4() {
        assertEquals("overridden",
                c.getString("web.to_override_without_prefix"));
        assertEquals("overridden",
                c.getString("to_override_without_prefix"));
    }

    /**
     * property defined without a prefix in rhn_web.conf
     * overridden without a prefix in rhn.conf.
     * Accessed fully qualified.
     */
    @Test
    public void testOverride5() {
        assertEquals("overridden",
                c.getString("to_override_without_prefix1"));
        assertEquals("overridden",
                c.getString("web.to_override_without_prefix1"));
    }

    /**
     * Tests a property with the same name defined in
     * more than one conf file with different prefixes.
     * Accesses the value fully qualfied.
     */
    @Test
    public void testCollision() {
        assertEquals("10", c.getString("web.collision"));
        assertEquals("12", c.getString("prefix.collision"));
    }

    /**
     * Tests a property with the same name defined in
     * more than one conf file with different prefixes.
     * Accesses the value without a prefix.  This will look through the
     * predefined prefix order to find the value.
     */
    @Test
    public void testPrefixOrder() {
        assertEquals("10", c.getString("collision"));
    }

    @Test
    public void testGetStringArray1Elem() {
        String[] elems = c.getStringArray("prefix.array_one_element");
        assertEquals(1, elems.length);
        assertEquals("some value", elems[0]);
    }

    @Test
    public void testGetStringArrayNull() {
        String[] elems = c.getStringArray("find.this.entry.b****");
        assertNull(elems);
    }

    /**
     * define a boolean value in rhn_prefix.conf, call getBoolean.
     * Test true, false, 1, 0, y, n, foo, 10
     */
    @Test
    public void testGetBoolean() {
        boolean b = c.getBoolean("prefix.boolean_true");
        assertTrue(b);

        assertFalse(c.getBoolean("prefix.boolean_false"));

        assertTrue(c.getBoolean("prefix.boolean_1"));
        assertFalse(c.getBoolean("prefix.boolean_0"));

        assertTrue(c.getBoolean("prefix.boolean_y"));
        assertTrue(c.getBoolean("prefix.boolean_Y"));
        assertFalse(c.getBoolean("prefix.boolean_n"));

        assertTrue(c.getBoolean("prefix.boolean_on"));
        assertFalse(c.getBoolean("prefix.boolean_off"));

        assertTrue(c.getBoolean("prefix.boolean_yes"));
        assertFalse(c.getBoolean("prefix.boolean_no"));

        assertFalse(c.getBoolean("prefix.boolean_foo"));
        assertFalse(c.getBoolean("prefix.boolean_10"));
        assertFalse(c.getBoolean("prefix.boolean_empty"));
        assertFalse(c.getBoolean("prefix.boolean_not_there"));

        assertTrue(c.getBoolean("prefix.boolean_on"));
        assertFalse(c.getBoolean("prefix.boolean_off"));
    }

    @Test
    public void testGetIntWithDefault() {
        // lookup a non existent value
        assertEquals(1000, c.getInt("value.doesnotexist", 1000));

        // lookup an existing value
        assertEquals(100, c.getInt("prefix.int_100", 1000));
    }

    /**
     * define an integer value in rhN_prefix.conf, call getInt.
     * Test -10, 0, 100, y
     */
    @Test
    public void testGetInt() {
        int i = c.getInt("prefix.int_minus10");
        assertEquals(-10, i);
        assertEquals(0, c.getInt("prefix.int_zero"));
        assertEquals(100, c.getInt("prefix.int_100"));

        boolean flag = false;
        try {
            c.getInt("prefix.int_y");
            flag = true;
        }
        catch (NumberFormatException nfe) {
            assertFalse(flag);
        }
    }

    @Test
    public void testGetInteger() {
        assertEquals(Integer.valueOf(-10), c.getInteger("prefix.int_minus10"));
        assertEquals(Integer.valueOf(0), c.getInteger("prefix.int_zero"));
        assertEquals(Integer.valueOf(100), c.getInteger("prefix.int_100"));
        assertNull(c.getInteger(null));
        assertEquals(c.getInt("prefix.int_100"),
                c.getInteger("prefix.int_100").intValue());

        boolean flag = false;
        try {
            c.getInteger("prefix.int_y");
            flag = true;
        }
        catch (NumberFormatException nfe) {
            assertFalse(flag);
        }
    }

    @Test
    public void testGetDouble() {
        assertEquals(Double.valueOf(10.0), c.getDouble("prefix.double"));
    }

    @Test
    public void testGetFloat() {
        assertEquals(Float.valueOf(10.0f), c.getFloat("prefix.float"));
    }

    /**
     * define comma separated value in rhn_prefix.conf,
     * call using StringArrayElem, verify all values are in array.
     */
    @Test
    public void testGetStringArrayMultElem() {
        String[] elems = c.getStringArray("prefix.comma_separated");
        assertEquals(5, elems.length);
        assertEquals("every", elems[0]);
        assertEquals("good", elems[1]);
        assertEquals("boy", elems[2]);
        assertEquals("does", elems[3]);
        assertEquals("fine", elems[4]);
    }

    @Test
    public void testGetStringArrayWhitespace() {
        String[] elems = c.getStringArray("prefix.comma_no_trim");
        assertEquals(5, elems.length);
        assertEquals("every", elems[0]);
        assertEquals(" good ", elems[1]);
        assertEquals(" boy ", elems[2]);
        assertEquals(" does", elems[3]);
        assertEquals("fine", elems[4]);
    }

    @Test
    public void testSetBoolean() {
        boolean oldValue = c.getBoolean("prefix.boolean_true");
        c.setBoolean("prefix.boolean_true", Boolean.FALSE.toString());
        assertFalse(c.getBoolean("prefix.boolean_true"));
        assertEquals("0", c.getString("prefix.boolean_true"));
        c.setBoolean("prefix.boolean_true", Boolean.valueOf(oldValue).toString());
    }

    @Test
    public void testSetString() {
        String oldValue = c.getString("to_override");
        c.setString("to_override", "newValue");
        assertEquals("newValue", c.getString("to_override"));
        c.setString("to_override", oldValue);
    }

    @Test
    public void testGetUndefinedInt() {
        int zero = c.getInt("Undefined_config_variable");
        assertEquals(0, zero);
    }

    @Test
    public void testGetUndefinedString() {
        assertNull(c.getString("Undefined_config_variable"));
    }

    @Test
    public void testNewValue() {
        String key = "newvalue" + TestUtils.randomString();
        c.setString(key, "somevalue");
        assertNotNull(c.getString(key));
    }

    @Test
    public void testGetUndefinedBoolean() {
        assertFalse(c.getBoolean("Undefined_config_variable"));
    }

    /**
     * property defined in conf file whose prefix is not a member
     * of the prefix order. Access property fully qualified, then
     * unqualified.
     */
    @Test
    public void testUnprefixedProperty() {
        assertEquals("thirty-three", c.getString("prefix.foo"));
        assertNull(c.getString("foo"));
    }

    @Test
    public void testNamespaceProperties() {
        Set<String> expectedProperties = Set.of("web.without_prefix",
            "web.to_override_without_prefix",
            "web.product_name",
            "web.to_override_without_prefix1",
            "web.property_with_prefix",
            "web.fq_to_override",
            "web.collision",
            "web.java.taskomatic_cobbler_user",
            "web.to_override"
        );

        Properties prop = c.getNamespaceProperties("web");
        assertEquals(expectedProperties.size(), prop.size());
        assertEquals(expectedProperties, prop.keySet());
    }

    @Test
    public void testNamespacePropertiesWithRewriting() {
        Set<String> expectedProperties = Set.of("test.without_prefix",
            "test.to_override_without_prefix",
            "test.product_name",
            "test.to_override_without_prefix1",
            "test.property_with_prefix",
            "test.fq_to_override",
            "test.collision",
            "test.java.taskomatic_cobbler_user",
            "test.to_override"
        );

        Properties prop = c.getNamespaceProperties("web", "test");
        assertEquals(expectedProperties.size(), prop.size());
        assertEquals(expectedProperties, prop.keySet());
    }

    @Test
    public void testBug154517IgnoreRpmsave() {
        assertNull(c.getString("bug154517.conf.betternotfindme"));
        assertNull(c.getString("betternotfindme"));
    }

    /**
     * Before implementing the code behind this test if a config entry had this:
     *
     * web.some_configvalue =
     *
     * you would get back ""
     */
    @Test
    public void testDefaultValueQuoteQuote() {
        Config.get().setString("somevalue8923984", "");
        assertNull(Config.get().getString("somevalue8923984"));
        String somevalue = Config.get().getString("somevalue8923984",
                "xmlrpc.rhn.redhat.com");
        assertNotNull(somevalue);
        assertNotEquals("", somevalue);
        assertEquals("xmlrpc.rhn.redhat.com", somevalue);
    }
    @Test
    public void testForNull() {
        assertNull(c.getString(null));
        assertNull(c.getInteger(null));
        assertEquals(0, c.getInt(null));
        assertFalse(c.getBoolean(null));
        assertNull(c.getStringArray(null));
    }

    /**
     * Commenting using '#' aren't supported currently by the config parser.
     * This test is here to document this behavior.
     */
    @Test
    public void testComment() {
        assertEquals(
                "#this will NOT be a comment!",
                c.getString("server.satellite.key_with_seeming_comment"));
    }

    /**
     * Verify that we treat the backslash as a normal character
     * (normally the Properties.load() would require them escaped).
     */
    @Test
    public void testBackSlashes() {
        assertEquals(
                "we\\have\\backslashes", // we\have\backslashes
                c.getString("server.satellite.key_with_backslash"));
    }
}
