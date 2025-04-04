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
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.redhat.rhn.common.UyuniReportStrategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class UyuniReportStrategyTest {

    private UyuniReportStrategy<String> strategy;

    @BeforeEach
    public void setUp() {
        strategy = errors -> {
            if (errors == null || errors.isEmpty()) {
                throw new IllegalArgumentException("Errors list cannot be null or empty");
            }
        };
    }

    @Test
    public void testReportWithErrors() {
        List<String> errors = List.of("Error 1", "Error 2");
        assertDoesNotThrow(() -> strategy.report(errors));
    }

    @Test
    public void testReportWithEmptyErrors() {
        List<String> errors = List.of();
        assertThrows(IllegalArgumentException.class, () -> strategy.report(errors));
    }

    @Test
    public void testReportWithNullErrors() {
        assertThrows(IllegalArgumentException.class, () -> strategy.report(null));
    }
}
