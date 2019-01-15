/**
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.common.util;


import org.apache.log4j.Logger;

import java.util.function.Supplier;

/**
 * TimeUtils is a utility class for dealing with time.
 * @version $Rev$
 */
public class TimeUtils {

    private TimeUtils() {
        // private constructor
    }

    /**
     * Returns the current system time in seconds.
     * @see java.lang.System#currentTimeMillis
     * @return the current system time in seconds.
     */
    public static long currentTimeSeconds() {
        return (System.currentTimeMillis() / 1000);
    }

    /**
     * Helper for logging the time some code took to execute.
     * @param log logger to use.
     * @param name a name/tag to describe whats being executed
     * @param fun the code to time
     */
    public static void logTime(Logger log, String name, Runnable fun) {
        long start = System.nanoTime();
        fun.run();
        long end = System.nanoTime();
        log.info(name + " took " + ((end - start) / 1e9) + " seconds.");
    }

    /**
     * Helper for logging the time some code took to execute.
     * @param log logger to use.
     * @param name a name/tag to describe whats being executed
     * @param fun the code to time
     * @param <T> type of return value
     * @return returns whatever fun returns
     */
    public static <T> T logTime(Logger log, String name, Supplier<T> fun) {
        long start = System.nanoTime();
        T result = fun.get();
        long end = System.nanoTime();
        log.info(name + " took " + ((end - start) / 1e9) + " seconds.");
        return result;
    }
}
