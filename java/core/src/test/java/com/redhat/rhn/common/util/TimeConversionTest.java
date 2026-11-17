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
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.TimeZone;

public class TimeConversionTest {

    private static final long INSTANT_MILLIS = Instant.parse("2019-05-29T13:49:09.123Z").toEpochMilli();

    private TimeZone originalDefault;

    @BeforeEach
    public void setUp() {
        originalDefault = TimeZone.getDefault();
    }

    @AfterEach
    public void tearDown() {
        TimeZone.setDefault(originalDefault);
    }

    @Test
    public void testLocalDateTimeStringFromDateUsesDefaultZoneAtCallTime() {
        Date date = new Date(INSTANT_MILLIS);

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        assertEquals("2019-05-29 13:49:09", TimeConversion.localDateTimeStringFromDate(date));

        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        assertEquals("2019-05-29 19:19:09", TimeConversion.localDateTimeStringFromDate(date));
    }

    @Test
    public void testLocalDateTimeStringFromDateAcceptsSqlTypes() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        assertEquals("2019-05-29 13:49:09",
                TimeConversion.localDateTimeStringFromDate(new Timestamp(INSTANT_MILLIS)));
        assertEquals("2019-05-29 13:49:09",
                TimeConversion.localDateTimeStringFromDate(new java.sql.Date(INSTANT_MILLIS)));
    }

    @Test
    public void testLocalDateTimeStringFromDateRejectsNull() {
        assertThrows(NullPointerException.class, () -> TimeConversion.localDateTimeStringFromDate(null));
    }

    @Test
    public void testDateFromDbDateTimeStringUsesDefaultZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));

        Date parsed = TimeConversion.dateFromDbDateTimeString("2021-06-25 00:56:01");

        assertEquals(Instant.parse("2021-06-24T19:26:01Z"), parsed.toInstant());
    }

    @Test
    public void testDateFromDbDateTimeStringIgnoresFractionalSeconds() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        // java.sql.Timestamp.toString() format, reaches the DTO setters when a query selects a raw timestamp
        Date parsed = TimeConversion.dateFromDbDateTimeString("2021-06-25 00:56:01.123456");

        assertEquals(Instant.parse("2021-06-25T00:56:01Z"), parsed.toInstant());
    }

    @Test
    public void testDateFromDbDateTimeStringResolvesRepeatedLocalTimeToLaterInstant() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
        // 02:56 happens twice on 2021-10-31: at 00:56Z (CEST) and at 01:56Z (CET)
        Date parsed = TimeConversion.dateFromDbDateTimeString("2021-10-31 02:56:12");

        assertEquals(Instant.parse("2021-10-31T01:56:12Z"), parsed.toInstant());
    }

    @Test
    public void testDateFromDbDateTimeStringRejectsOtherFormats() {
        assertThrows(DateTimeParseException.class, () -> TimeConversion.dateFromDbDateTimeString("2024-03-05"));
        assertThrows(DateTimeParseException.class,
                () -> TimeConversion.dateFromDbDateTimeString("2024-03-05T07:08:09"));
        assertThrows(NullPointerException.class, () -> TimeConversion.dateFromDbDateTimeString(null));
    }
}
