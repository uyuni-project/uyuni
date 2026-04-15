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
package com.suse.utils.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

public class EmptyStringAdapterTest {

    private final EmptyStringAdapter adapter = new EmptyStringAdapter();

    @ParameterizedTest
    @ValueSource(strings = {""})
    @NullSource
    public void testUnmarshalEmptyOrNullToNull(String value) {
        assertNull(adapter.unmarshal(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello", "   "})
    public void testUnmarshalNonEmptyStringReturnsValue(String value) {
        assertEquals(value, adapter.unmarshal(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello", "   ", ""})
    @NullSource
    public void testMarshalReturnsSameValue(String value) {
        assertEquals(value, adapter.marshal(value));
    }
}

