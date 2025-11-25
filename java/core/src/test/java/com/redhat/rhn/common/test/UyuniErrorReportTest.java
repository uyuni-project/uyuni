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

package com.redhat.rhn.common.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.UyuniError;
import com.redhat.rhn.common.UyuniErrorReport;
import com.redhat.rhn.common.UyuniGeneralException;
import com.redhat.rhn.common.UyuniReportStrategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class UyuniErrorReportTest {

    public static final String DUMMY_ERROR_MESSAGE = "dummy error";
    private UyuniErrorReport errorReport;

    @BeforeEach
    public void setUp() {
        errorReport = new UyuniErrorReport();
    }


    @Test
    public void testHasErrors() {
        assertFalse(errorReport.hasErrors());
        errorReport.register(DUMMY_ERROR_MESSAGE);
        assertTrue(errorReport.hasErrors());
    }

    @Test
    public void testGetErrors() {
        final String dummyError1 = "Test error 1";
        final String dummyError2 = "Test error 2";
        errorReport.register(dummyError1);
        errorReport.register(dummyError2);
        List<UyuniError> errors = errorReport.getErrors();
        String[] expectedMessages = {dummyError1, dummyError2};
        assertEquals(2, errors.size());
        assertEquals(dummyError1, errors.get(0).getMessage());
        assertEquals(dummyError2, errors.get(1).getMessage());
        assertArrayEquals(expectedMessages, errorReport.getErrorMessages());
    }

    @Test
    public void testReportWithStrategy() {
        UyuniReportStrategy<UyuniError> strategy = errors -> assertEquals(1, errors.size());
        errorReport.register(DUMMY_ERROR_MESSAGE);
        errorReport.report(strategy);
    }

    @Test
    public void testReportWithDefaultStrategyThrowsException() {
        errorReport.register(DUMMY_ERROR_MESSAGE);
        assertThrows(UyuniGeneralException.class, () -> errorReport.report());
    }

    @Test
    public void testReportWithDefaultStrategyNoException() {
        assertDoesNotThrow(() -> errorReport.report());
    }

    @Test
    public void testRegisterWithFormatString() {
        errorReport.register("Failed to subscribe server ID {0} to channels: {1}. Error: {2}",
                123, "channel1, channel2", "Network error");
        List<UyuniError> errors = errorReport.getErrors();
        assertEquals(1, errors.size());
        assertEquals("Failed to subscribe server ID 123 to channels: channel1, channel2. Error: Network error",
                errors.get(0).getMessage());
    }

    @Test
    public void testRegisterWithNullArguments() {
        errorReport.register("Error with null value: {0}", (Object) null);
        List<UyuniError> errors = errorReport.getErrors();
        assertEquals(1, errors.size());
        assertEquals("Error with null value: null", errors.get(0).getMessage());
    }

    @Test
    public void testRegisterWithNullFormatString() {
        errorReport.register(null, "arg1", "arg2");
        List<UyuniError> errors = errorReport.getErrors();
        assertEquals(1, errors.size());
        assertNull(errors.get(0).getMessage());
    }
}
