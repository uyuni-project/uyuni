/**
 * Copyright (c) 2020 SUSE LLC
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
package com.suse.manager.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilities for threads.
 */
public class ThreadUtils {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private ThreadUtils() { }

    /**
     * @param namePrefix name prefix
     * @return a thread name with the given prefix and the current timestamp as sufix
     */
    public static String threadName(String namePrefix) {
        return namePrefix + TIMESTAMP.format(LocalDateTime.now());
    }

}
