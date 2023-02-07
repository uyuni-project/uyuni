/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
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
package com.redhat.rhn.common.util.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.util.DatePicker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * DatePickerTest
 */
public class DatePickerTest  {

    private static final TimeZone TZ = TimeZone.getTimeZone("America/Los_Angeles");

    @Test
    public void testDateFormat() {
        DatePicker p = makePicker(Locale.ENGLISH);
        assertTrue(p.isLatin());
        assertFalse(p.isDayBeforeMonth());

        p = makePicker(Locale.GERMAN);
        assertFalse(p.isLatin());
        assertTrue(p.isDayBeforeMonth());
    }

    @Test
    public void testSetDate() throws ParseException {
        DatePicker p = makePicker(Locale.ENGLISH);
        Date d = parseDate("1996-08-03T15:33");
        p.setDate(d);
        assertEquals(1996, p.getYear());
        assertEquals(7, p.getMonth()); // Months are zero-based !
        assertEquals(3, p.getDay());
        assertEquals(Calendar.PM, p.getAmPm());
        assertEquals(3, p.getHour());
        assertEquals(33, p.getMinute());
    }

    @Test
    public void testPositiveRange() throws ParseException {
        DatePicker p = makePicker(Locale.ENGLISH, DatePicker.YEAR_RANGE_POSITIVE);
        Date d = parseDate("2005-08-03T15:33");
        p.setDate(d);
        // the range is always the current year plus 4 (according to the test)
        // this test failed when hard coded with 2005 as year range 0.
        int curYear = Calendar.getInstance().get(Calendar.YEAR);
        assertEquals(2005, p.getYear());
        assertEquals(curYear, p.getYearRange()[0]);
        assertEquals(curYear + 4, p.getYearRange()[4]);
    }


    @Test
    public void testReadWriteFromMap() throws ParseException {
        Map<String, Integer> form = new HashMap<>();
        DatePicker p = makePicker(Locale.ENGLISH);
        Date d = parseDate("1996-08-03T15:33");
        p.setDate(d);
        p.writeToMap(form);

        p = makePicker(Locale.ENGLISH);
        p.readMap(form);
        Assertions.assertEquals(d, p.getDate());
    }

    @Test
    public void testBadDate() throws ParseException {
        DatePicker p = makePicker(Locale.ENGLISH);
        Date d = parseDate("1996-08-03T15:33");
        p.setDate(d);
        p.setMonth(13);
        assertNull(p.getDate());
    }

    @Test
    public void testBadField() throws ParseException {
        DatePicker p = makePicker(Locale.ENGLISH);
        Date d = parseDate("1996-08-03T15:33");
        p.setDate(d);
        p.setMonth(13);
        assertNull(p.getYear());
    }

    private Date parseDate(String date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm", Locale.ENGLISH);
        sdf.setTimeZone(TZ);
        return sdf.parse(date);
    }

    private DatePicker makePicker(Locale locale, int yearDirection) {
        return new DatePicker("test", TZ, locale, yearDirection);
    }

    private DatePicker makePicker(Locale locale) {
        return makePicker(locale, DatePicker.YEAR_RANGE_NEGATIVE);
    }

    private void assertEquals(int exp, Integer act) {
        Assertions.assertEquals(Integer.valueOf(exp), act);
    }
}
