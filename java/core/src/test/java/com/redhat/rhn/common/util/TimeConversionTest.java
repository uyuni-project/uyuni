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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

class TimeConversionTest {

    private static final long INSTANT_MILLIS = Instant.parse("2019-05-29T13:49:09.123Z").toEpochMilli();

    private TimeZone originalDefault;

    @BeforeEach
    void setUp() {
        originalDefault = TimeZone.getDefault();
    }

    @AfterEach
    void tearDown() {
        TimeZone.setDefault(originalDefault);
    }

    @Test
    void testLocalDateTimeStringFromDateUsesDefaultZoneAtCallTime() {
        Date date = new Date(INSTANT_MILLIS);

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        assertEquals("2019-05-29 13:49:09", TimeConversion.localDateTimeStringFromDate(date));

        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        assertEquals("2019-05-29 19:19:09", TimeConversion.localDateTimeStringFromDate(date));
    }

    @Test
    void testLocalDateTimeStringFromDateAcceptsSqlTypes() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        assertEquals("2019-05-29 13:49:09",
                TimeConversion.localDateTimeStringFromDate(new Timestamp(INSTANT_MILLIS)));
        assertEquals("2019-05-29 13:49:09",
                TimeConversion.localDateTimeStringFromDate(new java.sql.Date(INSTANT_MILLIS)));
    }

    @Test
    void testLocalDateTimeStringFromDateRejectsNull() {
        assertThrows(NullPointerException.class, () -> TimeConversion.localDateTimeStringFromDate(null));
    }

    @Test
    void testDateFromDbDateTimeStringUsesDefaultZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));

        Date parsed = TimeConversion.dateFromDbDateTimeString("2021-06-25 00:56:01");

        assertEquals(Instant.parse("2021-06-24T19:26:01Z"), parsed.toInstant());
    }

    @Test
    void testDateFromDbDateTimeStringIgnoresFractionalSeconds() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        // java.sql.Timestamp.toString() format, reaches the DTO setters when a query selects a raw timestamp
        Date parsed = TimeConversion.dateFromDbDateTimeString("2021-06-25 00:56:01.123456");

        assertEquals(Instant.parse("2021-06-25T00:56:01Z"), parsed.toInstant());
    }

    @Test
    void testDateFromDbDateTimeStringResolvesRepeatedLocalTimeToLaterInstant() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
        // 02:56 happens twice on 2021-10-31: at 00:56Z (CEST) and at 01:56Z (CET)
        Date parsed = TimeConversion.dateFromDbDateTimeString("2021-10-31 02:56:12");

        assertEquals(Instant.parse("2021-10-31T01:56:12Z"), parsed.toInstant());
    }

    @Test
    void testDateFromDbDateTimeStringRejectsOtherFormats() {
        assertThrows(DateTimeParseException.class, () -> TimeConversion.dateFromDbDateTimeString("2024-03-05"));
        assertThrows(DateTimeParseException.class,
                () -> TimeConversion.dateFromDbDateTimeString("2024-03-05T07:08:09"));
        assertThrows(NullPointerException.class, () -> TimeConversion.dateFromDbDateTimeString(null));
    }

    @Test
    void testLocalDateStringFromDateUsesDefaultZone() {
        Date date = Date.from(Instant.parse("2024-03-05T20:00:00Z"));

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        assertEquals("2024-03-05", TimeConversion.localDateStringFromDate(date));

        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        assertEquals("2024-03-06", TimeConversion.localDateStringFromDate(date));
    }

    @Test
    void testDateFromLocalDateStringReturnsStartOfDayInDefaultZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));

        Date parsed = TimeConversion.dateFromLocalDateString("2021-11-22");

        assertEquals(Instant.parse("2021-11-21T18:30:00Z"), parsed.toInstant());
    }

    @Test
    void testDateFromLocalDateStringIgnoresTrailingText() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        Date parsed = TimeConversion.dateFromLocalDateString("2021-11-22T10:00:00");

        assertEquals(Instant.parse("2021-11-22T00:00:00Z"), parsed.toInstant());
    }

    @Test
    void testDateFromLocalDateStringSkipsMissingMidnight() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
        // daylight saving time started at midnight on 2018-11-04, the day began at 01:00 (-02:00)
        Date parsed = TimeConversion.dateFromLocalDateString("2018-11-04");

        assertEquals(Instant.parse("2018-11-04T03:00:00Z"), parsed.toInstant());
    }

    @Test
    void testDateFromLocalDateStringResolvesRepeatedMidnightToLaterInstant() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Havana"));
        // daylight saving time ended at 01:00 on 1991-10-13: midnight happened at 04:00Z and at 05:00Z
        Date parsed = TimeConversion.dateFromLocalDateString("1991-10-13");

        assertEquals(Instant.parse("1991-10-13T05:00:00Z"), parsed.toInstant());
    }

    @Test
    void testDateFromLocalDateStringRejectsOtherFormats() {
        assertThrows(DateTimeParseException.class, () -> TimeConversion.dateFromLocalDateString("22/11/2021"));
        assertThrows(NullPointerException.class, () -> TimeConversion.dateFromLocalDateString(null));
    }

    @Test
    void testDateFromLocalDateTimeStringUsesGivenPattern() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        DateTimeFormatter clockHour = DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm:ss");

        assertEquals(Instant.parse("2024-03-05T00:30:00Z"),
                TimeConversion.dateFromLocalDateTimeString(clockHour, "2024-03-05 00:30:00").toInstant());
        // with kk, 24 is midnight at the start of the same day
        assertEquals(Instant.parse("2024-03-05T00:30:00Z"),
                TimeConversion.dateFromLocalDateTimeString(clockHour, "2024-03-05 24:30:00").toInstant());
    }

    @Test
    void testDateFromLocalDateTimeStringIgnoresFractionalSeconds() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        DateTimeFormatter clockHour = DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm:ss");

        Date parsed = TimeConversion.dateFromLocalDateTimeString(clockHour, "2024-03-05 12:38:09.123456");

        assertEquals(Instant.parse("2024-03-05T07:08:09Z"), parsed.toInstant());
    }

    @Test
    void testDateFromLocalDateTimeStringRejectsOtherFormats() {
        DateTimeFormatter clockHour = DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm:ss");

        assertThrows(DateTimeParseException.class,
                () -> TimeConversion.dateFromLocalDateTimeString(clockHour, "2024-03-05"));
        assertThrows(NullPointerException.class, () -> TimeConversion.dateFromLocalDateTimeString(clockHour, null));
    }

    @Test
    void testTimeZoneShortNameDependsOnDaylightSavingTime() {
        TimeZone berlin = TimeZone.getTimeZone("Europe/Berlin");
        Date winter = Date.from(Instant.parse("2024-01-15T12:00:00Z"));
        Date summer = Date.from(Instant.parse("2024-07-15T12:00:00Z"));

        assertEquals("CET", TimeConversion.timeZoneShortName(berlin, winter, Locale.US));
        assertEquals("CEST", TimeConversion.timeZoneShortName(berlin, summer, Locale.US));
        assertEquals("MEZ", TimeConversion.timeZoneShortName(berlin, winter, Locale.GERMAN));
        assertEquals("MESZ", TimeConversion.timeZoneShortName(berlin, summer, Locale.GERMAN));
    }

    @Test
    void testTimeZoneShortNameKeepsLegacyIds() {
        // java.time would write -05:00 for this id
        Date summer = Date.from(Instant.parse("2024-07-15T12:00:00Z"));

        assertEquals("EST", TimeConversion.timeZoneShortName(TimeZone.getTimeZone("EST"), summer, Locale.US));
    }

    @Test
    void testLocalDateTimeWithZoneNameString() {
        TimeZone berlin = TimeZone.getTimeZone("Europe/Berlin");

        assertEquals("2010-04-01 15:04:24 CEST", TimeConversion.localDateTimeWithZoneNameString(
                Date.from(Instant.parse("2010-04-01T13:04:24Z")), berlin, Locale.US));
        assertEquals("2010-01-01 13:00:00 CET", TimeConversion.localDateTimeWithZoneNameString(
                Date.from(Instant.parse("2010-01-01T12:00:00Z")), berlin, Locale.US));
    }
}
