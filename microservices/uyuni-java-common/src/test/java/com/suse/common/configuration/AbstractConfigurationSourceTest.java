/*
 * Copyright (c) 2024 SUSE LLC
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

package com.suse.common.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

abstract class AbstractConfigurationSourceTest {

    protected ConfigurationSource source;

    @BeforeEach
    public void setup() throws Exception {
        source = createConfigurationSource();
    }

    protected abstract ConfigurationSource createConfigurationSource() throws Exception ;

    @Nested
    @DisplayName("Basic retrieval")
    class BasicRetrievalTest {

        @Test
        @DisplayName("Value string should be retrieved")
        void testRetrieveValue() {
            assertOptionalEquals("value", source.getString("prefix.simple_value"));
        }
        @Test
        @DisplayName("Spaces should be trimmed")
        void testTrimSpaces() {
            assertOptionalEquals("all trailing spaces  should be  trimmed", source.getString("prefix.with_spaces"));
        }

        @Test
        @DisplayName("Empty definition should be treated as undefined property")
        void testDefaultValueQuoteQuote() {
            assertOptionalEmpty(source.getString("prefix.empty"));
        }
    }

    @Nested
    @DisplayName("List values")
    class ListTest {

        @Test
        @DisplayName("Retrieve list of one element")
        void testGetList1Elem() {
            assertOptionalEquals(List.of("some value"), source.getList("prefix.array_one_element", String.class));
        }

        @Test
        @DisplayName("Retrieve comma separated value as list")
        void testGetStringArrayMultElem() {
            assertOptionalEquals(List.of("every", "good", "boy", "does", "fine"), source.getList("prefix.comma_separated", String.class));
        }

        @Test
        @DisplayName("Item whitespace behaviour when retrieving list values")
        void testGetStringArrayWhitespace() {
            assertOptionalEquals(List.of("every", " good ", " boy ", " does", "fine"), source.getList("prefix.comma_no_trim", String.class));
        }
    }

    @Nested
    @DisplayName("Conversion from string to specific type")
    class ConversionTest {

        @Test
        @DisplayName("Boolean values")
        public void testGetBoolean() {
            assertOptionalTrue(source.getBoolean("prefix.boolean_true"));

            assertOptionalFalse(source.getBoolean("prefix.boolean_false"));

            assertOptionalTrue(source.getBoolean("prefix.boolean_1"));
            assertOptionalFalse(source.getBoolean("prefix.boolean_0"));

            assertOptionalTrue(source.getBoolean("prefix.boolean_y"));
            assertOptionalTrue(source.getBoolean("prefix.boolean_Y"));
            assertOptionalFalse(source.getBoolean("prefix.boolean_n"));

            assertOptionalTrue(source.getBoolean("prefix.boolean_on"));
            assertOptionalFalse(source.getBoolean("prefix.boolean_off"));

            assertOptionalTrue(source.getBoolean("prefix.boolean_yes"));
            assertOptionalFalse(source.getBoolean("prefix.boolean_no"));

            assertOptionalFalse(source.getBoolean("prefix.boolean_foo"));
            assertOptionalFalse(source.getBoolean("prefix.boolean_10"));
            assertOptionalEmpty(source.getBoolean("prefix.boolean_empty"));
            assertOptionalEmpty(source.getBoolean("prefix.boolean_not_there"));

            assertOptionalTrue(source.getBoolean("prefix.boolean_on"));
            assertOptionalFalse(source.getBoolean("prefix.boolean_off"));
        }

        @Test
        @DisplayName("Integer values")
        void testGetInteger() {
            // lookup a non existent value
            assertOptionalEmpty(source.getInteger("value.doesnotexist"));

            // lookup an existing value
            assertOptionalEquals(100, source.getInteger("prefix.int_100"));

            assertOptionalEquals(-10, source.getInteger("prefix.int_minus10"));
            assertOptionalEquals(0, source.getInteger("prefix.int_zero"));
            assertOptionalEquals(100, source.getInteger("prefix.int_100"));

            Assertions.assertThrows(NumberFormatException.class, () -> source.getInteger("prefix.int_y"));
        }

        @Test
        @DisplayName("Double values")
        void testGetDouble() {
            assertOptionalEquals(10.0, source.getDouble("prefix.double"));
        }

        @Test
        @DisplayName("Float values")
        void testGetFloat() {
            assertOptionalEquals(10.0f, source.getFloat("prefix.float"));
        }
    }

    @Nested
    @DisplayName("Undefined properties")
    class UndefinedTest {
        @Test
        void testGetUndefinedInt() {
            assertOptionalEmpty(source.getInteger("Undefined_integer_variable"));
        }

        @Test
        void testGetUndefinedString() {
            assertOptionalEmpty(source.getString("Undefined_string_variable"));
        }

        @Test
        void testGetUndefinedBoolean() {
            assertOptionalEmpty(source.getBoolean("Undefined_boolean_variable"));
        }

        @Test
        @DisplayName("Retrieve undefined list")
        void testGetListNull() {
            assertOptionalEmpty(source.getList("Undefined_list_variable", String.class));
        }
    }

    protected static <T> void assertOptionalEquals(T expected, Optional<T> actual) {
        assertEquals(expected, actual.orElseGet(() -> Assertions.fail("Optional has no value")));
    }

    protected static <T> void assertOptionalEmpty(Optional<T> actual) {
        assertTrue(actual.isEmpty(), "Optional has a value");
    }

    protected static void assertOptionalTrue(Optional<Boolean> actual) {
        assertTrue(actual.orElseGet(() -> Assertions.fail("Optional has no value")));
    }

    protected static void assertOptionalFalse(Optional<Boolean> actual) {
        assertFalse(actual.orElseGet(() -> Assertions.fail("Optional has no value")));
    }
}
