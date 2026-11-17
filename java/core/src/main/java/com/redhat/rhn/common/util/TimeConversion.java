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

package com.redhat.rhn.common.util;

import static com.redhat.rhn.common.ExceptionMessage.NOT_INSTANTIABLE;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Standard conversions between {@link Date} and their string representations.
 * The JVM default time zone is read on every call, as {@code SimpleDateFormat} did.
 */
public final class TimeConversion {

    private static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Formats a date as {@code yyyy-MM-dd HH:mm:ss} in the JVM default time zone, e.g. for
     * {@code TO_TIMESTAMP(:date, 'YYYY-MM-DD HH24:MI:SS')} query parameters.
     *
     * @param date the date to format, also accepts {@code java.sql.Date} and {@code java.sql.Timestamp}
     * @return the formatted date, without milliseconds
     * @throws NullPointerException if date is null
     */
    public static String localDateTimeStringFromDate(Date date) {
        return LOCAL_DATE_TIME_FORMATTER.withZone(ZoneId.systemDefault()).format(instantFromDate(date));
    }

    // java.sql.Date.toInstant() throws UnsupportedOperationException, getTime() works for every Date
    private static Instant instantFromDate(Date date) {
        return Instant.ofEpochMilli(date.getTime());
    }

    private TimeConversion() {
        throw new UnsupportedOperationException(NOT_INSTANTIABLE);
    }
}
