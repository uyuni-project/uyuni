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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.common.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.RhnError;
import com.redhat.rhn.common.RhnErrorReport;
import com.redhat.rhn.common.RhnGeneralException;
import com.redhat.rhn.common.RhnReportStrategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class RhnErrorReportTest {

    public static final String DUMMY_ERROR_MESSAGE = "dummy error";
    private RhnErrorReport errorReport;

    @BeforeEach
    public void setUp() {
        errorReport = new RhnErrorReport();
    }


    @Test
    public void testHasErrors() {
        assertFalse(errorReport.hasErrors());
        errorReport.register(DUMMY_ERROR_MESSAGE);
        assertTrue(errorReport.hasErrors());
    }

    @Test
    public void testGetErrors() {
        errorReport.register("Test error 1");
        errorReport.register("Test error 2");
        List<RhnError> errors = errorReport.getErrors();
        assertEquals(2, errors.size());
        assertEquals("Test error 1", errors.get(0).getMessage());
        assertEquals("Test error 2", errors.get(1).getMessage());
    }

    @Test
    public void testReportWithStrategy() {
        RhnReportStrategy<RhnError> strategy = errors -> assertEquals(1, errors.size());
        errorReport.register(DUMMY_ERROR_MESSAGE);
        errorReport.report(strategy);
    }

    @Test
    public void testReportWithDefaultStrategyThrowsException() {
        errorReport.register(DUMMY_ERROR_MESSAGE);
        assertThrows(RhnGeneralException.class, () -> errorReport.report());
    }

    @Test
    public void testReportWithDefaultStrategyNoException() {
        assertDoesNotThrow(() -> errorReport.report());
    }
}
