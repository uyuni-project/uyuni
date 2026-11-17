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

package com.suse.manager.webui.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.redhat.rhn.frontend.context.Context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ViewHelperDateTest {

    private static final Date DATE = Date.from(Instant.parse("2024-03-05T07:08:09Z"));

    private TimeZone originalTimeZone;
    private Locale originalLocale;

    @BeforeEach
    public void setUp() {
        originalTimeZone = TimeZone.getDefault();
        originalLocale = Locale.getDefault();
    }

    @AfterEach
    public void tearDown() {
        TimeZone.setDefault(originalTimeZone);
        Locale.setDefault(originalLocale);
        Context.freeCurrentContext();
    }

    @Test
    public void testRenderDateUsesContextTimeZone() {
        Context.getCurrentContext().setLocale(Locale.US);
        Context.getCurrentContext().setTimezone(TimeZone.getTimeZone("Europe/Berlin"));

        assertEquals("2024-03-05T08:08+01:00", ViewHelper.getInstance().renderDate(DATE));
    }

    @Test
    public void testRenderDateWritesLocaleDigits() {
        Context.getCurrentContext().setLocale(Locale.forLanguageTag("bn-IN"));
        Context.getCurrentContext().setTimezone(TimeZone.getTimeZone("UTC"));

        // 2024-03-05T07:08Z in Bengali digits, as SimpleDateFormat wrote it
        assertEquals("২০২৪-০৩-০৫T০৭:০৮Z",
                ViewHelper.getInstance().renderDate(DATE));
    }

    @Test
    public void testFormatDateTimeToISOUsesServerTimeZone() {
        Locale.setDefault(Locale.US);

        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        assertEquals("2024-03-05T12:38:09+05:30", ViewHelper.formatDateTimeToISO(DATE));

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        assertEquals("2024-03-05T07:08:09Z", ViewHelper.formatDateTimeToISO(DATE));
    }

    @Test
    public void testGetDateFromISOString() throws ParseException {
        assertEquals(DATE, ViewHelper.getDateFromISOString("2024-03-05T12:38:09+05:30"));
        assertEquals(DATE, ViewHelper.getDateFromISOString("2024-03-05T07:08:09Z"));
    }

    @Test
    public void testGetDateFromISOStringRejectsOtherFormats() {
        assertThrows(ParseException.class, () -> ViewHelper.getDateFromISOString("2024-03-05T07:08:09.123Z"));
        assertThrows(ParseException.class, () -> ViewHelper.getDateFromISOString("2024-03-05 07:08:09Z"));
    }
}
