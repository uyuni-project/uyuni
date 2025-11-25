/*
 * Copyright (c) 2024--2025 SUSE LLC
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

package com.redhat.rhn.common;

import static com.redhat.rhn.common.ExceptionMessage.NOT_INSTANTIABLE;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

/**
 * This class provides various strategies for error reporting and logging.
 * Base validation reporting strategy throws a {@link UyuniGeneralException} if there are errors.
 */
public class ErrorReportingStrategies {

    private ErrorReportingStrategies() {
        throw new UnsupportedOperationException(NOT_INSTANTIABLE);
    }

    private static final Map<Object, Logger> OBJ_LOGGER = Collections.synchronizedMap(new WeakHashMap<>());
    private static final UyuniReportStrategy<UyuniError> VALIDATION_REPORT_STRATEGY;

    static {
        VALIDATION_REPORT_STRATEGY = errors -> {
            if (!errors.isEmpty()) {
                throw new UyuniGeneralException(errors);
            }
        };
    }


    /**
     * Returns a default validation reporting strategy
     * @return UyuniReportStrategy
     */
    public static UyuniReportStrategy<UyuniError> validationReportingStrategy() {
        return VALIDATION_REPORT_STRATEGY;
    }

    /**
     * Raise and log an exception
     * @param obj Object to log
     * @param message Message to log
     * @return Supplier of RhnRuntimeException that logs the message and throw the exception
     */
    public static Supplier<RhnRuntimeException> raiseAndLog(Object obj, String message) {
        Logger logger = OBJ_LOGGER.computeIfAbsent(obj, key -> LogManager.getLogger(obj.getClass().getName()));
        return () -> {
            logger.error(message);
            return new RhnRuntimeException(message);
        };
    }

    /**
     * All errors are logged using the logger of the provided object
     * @param obj Object to log
     * @return UyuniReportStrategy
     */
    public static UyuniReportStrategy<UyuniError> logReportingStrategy(Object obj) {
        Logger logger = OBJ_LOGGER.computeIfAbsent(obj, key -> LogManager.getLogger(obj.getClass().getName()));

        return errors -> {
            for (UyuniError error : errors) {
                logger.error(error.getMessage());
            }
        };
    }
}
