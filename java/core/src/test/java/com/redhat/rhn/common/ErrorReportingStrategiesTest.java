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

package com.redhat.rhn.common;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.jupiter.api.Test;

import java.util.List;

class ErrorReportingStrategiesTest {

    @Test
    void testValidationReportingStrategyThrowsExceptionOnErrors() {
        UyuniReportStrategy<UyuniError> strategy = ErrorReportingStrategies.validationReportingStrategy();
        List<UyuniError> errors = List.of(new UyuniError("Test error"));

        assertThrows(UyuniGeneralException.class, () -> strategy.report(errors));
    }

    @Test
    void testRaiseAndLog() {
        String expectedLoggerName = Dummy.class.getName().replace('$', '.');
        String testMessage = "Test error message";
        LoggerContext context = (LoggerContext) LogManager.getContext(false);

        assertFalse(hasLogger(context, expectedLoggerName));
        RhnRuntimeException exception = ErrorReportingStrategies.raiseAndLog(new Dummy(), testMessage).get();
        assertTrue(exception.getMessage().contains(testMessage));
        assertTrue(hasLogger(context, expectedLoggerName));
    }

    static class Dummy {
    }

    private static boolean hasLogger(LoggerContext context, String loggerName) {
        return context.getLoggers().stream()
                .anyMatch(logger -> logger.getName().equals(loggerName));
    }
}
