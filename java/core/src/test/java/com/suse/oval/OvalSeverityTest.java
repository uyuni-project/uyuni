/*
 * Copyright (c) 2026 SUSE LLC
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

package com.suse.oval;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;

public class OvalSeverityTest {

    @ParameterizedTest
    @CsvSource({
            "Critical, CRITICAL",
            "Important, IMPORTANT",
            "Moderate, MODERATE",
            "Low, LOW",
            "High, IMPORTANT",
            "Medium, MODERATE",
            "Negligible, LOW",
            "'  important ', IMPORTANT",
            "LOW, LOW",
            "Not set, UNKNOWN",
            "Untriaged, UNKNOWN",
            "'', UNKNOWN"
    })
    public void testFromOval(String ovalValue, OvalSeverity expected) {
        assertEquals(expected, OvalSeverity.fromOval(ovalValue));
    }

    @ParameterizedTest
    @NullSource
    public void testFromOvalNull(String ovalValue) {
        assertEquals(OvalSeverity.UNKNOWN, OvalSeverity.fromOval(ovalValue));
    }

    @ParameterizedTest
    @CsvSource({"CRITICAL", "LOW"})
    public void testToDbValue(OvalSeverity severity) {
        assertEquals(severity.name(), severity.toDbValue());
    }

    @Test
    public void testUnknownIsStoredAsNull() {
        assertNull(OvalSeverity.UNKNOWN.toDbValue());
    }
}
