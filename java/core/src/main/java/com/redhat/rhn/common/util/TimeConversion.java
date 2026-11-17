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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Standard conversions between {@link Date} and their string representations.
 * The JVM default time zone is read on every call, as {@code SimpleDateFormat} did.
 */
public final class TimeConversion {

    private static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter DB_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(LocalizationService.RHN_DB_DATEFORMAT);

    private static final DateTimeFormatter LOCAL_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
        return dateFromLocalDateTimeString(DB_DATE_TIME_FORMATTER, value);
    }

    /**
     * Formats a date as {@code yyyy-MM-dd} in the JVM default time zone.
     *
     * @param date the date to format, also accepts {@code java.sql.Date} and {@code java.sql.Timestamp}
     * @return the formatted date
     * @throws NullPointerException if date is null
     */
    public static String localDateStringFromDate(Date date) {
        return LOCAL_DATE_FORMATTER.withZone(ZoneId.systemDefault()).format(instantFromDate(date));
    }

    /**
     * Parses a date in {@code yyyy-MM-dd} format as the start of that day in the JVM default time zone.
     * As {@code SimpleDateFormat.parse(String)} did, text after the pattern is ignored and a midnight repeated
     * at the end of daylight saving time resolves to the later instant.
     *
     * @param value the text to parse
     * @return the start of the parsed day
     * @throws DateTimeParseException if value does not start with a date in that format
     * @throws NullPointerException if value is null
     */
    public static Date dateFromLocalDateString(String value) {
        LocalDate local = LocalDate.from(LOCAL_DATE_FORMATTER.parse(value, new ParsePosition(0)));
        return Date.from(local.atStartOfDay().atZone(ZoneId.systemDefault()).withLaterOffsetAtOverlap().toInstant());
    }

    /**
     * Parses a local date and time with the given formatter in the JVM default time zone.
     * As {@code SimpleDateFormat.parse(String)} did, text after the pattern (e.g. fractional seconds) is ignored
     * and a local time repeated at the end of daylight saving time resolves to the later instant.
     *
     * @param formatter the formatter, its pattern must contain a date and a time
     * @param value the text to parse
     * @return the parsed date
     * @throws DateTimeParseException if value does not start with a date and time matching the formatter
     * @throws NullPointerException if value is null
     */
    public static Date dateFromLocalDateTimeString(DateTimeFormatter formatter, String value) {
        LocalDateTime local = LocalDateTime.from(formatter.parse(value, new ParsePosition(0)));
        return Date.from(local.atZone(ZoneId.systemDefault()).withLaterOffsetAtOverlap().toInstant());
    }

    /**
     * Returns the short name of a time zone at the given date, e.g. CET or CEST for Europe/Berlin, as
     * {@code SimpleDateFormat} wrote it for the {@code z} pattern. java.time names some zones differently
     * (e.g. EST as -05:00), so the name comes from {@link TimeZone}.
     *
     * @param timeZone the time zone
     * @param date the date, which decides between the standard and the daylight saving time name
     * @param locale the locale of the name
     * @return the short name of the time zone
     */
    public static String timeZoneShortName(TimeZone timeZone, Date date, Locale locale) {
        return timeZone.getDisplayName(timeZone.inDaylightTime(date), TimeZone.SHORT, locale);
    }

    // java.sql.Date.toInstant() throws UnsupportedOperationException, getTime() works for every Date
    private static Instant instantFromDate(Date date) {
        return Instant.ofEpochMilli(date.getTime());
    }

    private TimeConversion() {
        throw new UnsupportedOperationException(NOT_INSTANTIABLE);
    }
}
