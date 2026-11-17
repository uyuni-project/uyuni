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

import com.redhat.rhn.common.localization.LocalizationService;

import java.text.ParsePosition;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * Standard conversions between {@link Date} and their string representations.
 * The JVM default time zone is read on every call, as {@code SimpleDateFormat} did.
 */
public final class TimeConversion {

    private static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter DB_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(LocalizationService.RHN_DB_DATEFORMAT);

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

    /**
     * Parses a date in {@link LocalizationService#RHN_DB_DATEFORMAT} in the JVM default time zone, as produced by
     * {@code TO_CHAR(..., 'YYYY-MM-DD HH24:MI:SS')} or by {@code java.sql.Timestamp.toString()}.
     * As {@code SimpleDateFormat.parse(String)} did, text after the pattern (e.g. fractional seconds) is ignored
     * and a local time repeated at the end of daylight saving time resolves to the later instant.
     *
     * @param value the text to parse
     * @return the parsed date
     * @throws DateTimeParseException if value does not start with a date in that format
     * @throws NullPointerException if value is null
     */
    public static Date dateFromDbDateTimeString(String value) {
        LocalDateTime local = LocalDateTime.from(DB_DATE_TIME_FORMATTER.parse(value, new ParsePosition(0)));
        return Date.from(local.atZone(ZoneId.systemDefault()).withLaterOffsetAtOverlap().toInstant());
    }

    // java.sql.Date.toInstant() throws UnsupportedOperationException, getTime() works for every Date
    private static Instant instantFromDate(Date date) {
        return Instant.ofEpochMilli(date.getTime());
    }

    private TimeConversion() {
        throw new UnsupportedOperationException(NOT_INSTANTIABLE);
    }
}
