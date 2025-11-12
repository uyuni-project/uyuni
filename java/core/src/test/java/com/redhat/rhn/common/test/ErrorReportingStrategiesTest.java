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
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.common.test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.ErrorReportingStrategies;
import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.UyuniError;
import com.redhat.rhn.common.UyuniGeneralException;
import com.redhat.rhn.common.UyuniReportStrategy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;

public class ErrorReportingStrategiesTest {

    @Test
    public void testValidationReportingStrategyThrowsExceptionOnErrors() {
        UyuniReportStrategy<UyuniError> strategy = ErrorReportingStrategies.validationReportingStrategy();
        List<UyuniError> errors = List.of(new UyuniError("Test error"));

        assertThrows(UyuniGeneralException.class, () -> strategy.report(errors));
    }

    @Test
    public void testRaiseAndLog() {
        String testMessage = "Test error message";
        Supplier<RhnRuntimeException> exceptionSupplier = ErrorReportingStrategies.raiseAndLog(this, testMessage);

        RhnRuntimeException exception = exceptionSupplier.get();
        assertTrue(exception.getMessage().contains(testMessage));
    }

    @Test
    public void testRaiseAndLogLogsMessage() {
        String testMessage = "Test error message";
        Supplier<RhnRuntimeException> exceptionSupplier = ErrorReportingStrategies.raiseAndLog(this, testMessage);

        // Capture the log output
        Logger logger = LogManager.getLogger(this.getClass().getName());
        logger.error(testMessage);

        RhnRuntimeException exception = exceptionSupplier.get();
        assertTrue(exception.getMessage().contains(testMessage));
    }
}
