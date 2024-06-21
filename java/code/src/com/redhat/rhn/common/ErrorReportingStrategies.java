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

package com.redhat.rhn.common;

import static com.redhat.rhn.common.ExceptionMessage.NOT_INSTANTIABLE;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

public class ErrorReportingStrategies {

    private ErrorReportingStrategies() {
        throw new UnsupportedOperationException(NOT_INSTANTIABLE);
    }

    private static final Map<Object, Logger> OBJ_LOGGER = Collections.synchronizedMap(new WeakHashMap<>());

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
}
