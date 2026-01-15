/*
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
package com.suse.manager.maintenance.test;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.maintenance.IcalUtils;
import com.suse.manager.maintenance.MaintenanceWindowData;
import com.suse.manager.model.maintenance.MaintenanceCalendar;
import com.suse.manager.model.maintenance.MaintenanceSchedule;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;

/**
 * Tests focusing on maintenance windows computation
 */
public class IcalUtilsTest {

    private CalendarBuilder calendarBuilder;

    private IcalUtils icalUtils;

    private MaintenanceCalendar maintenanceCalendar;

    private static final String TESTDATAPATH = "/com/suse/manager/maintenance/test/testdata";
    private static final String GOOGLE_ICS = "maintenance-windows-google-multizones.ics";

    private static final String TEST_ICAL = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//SUSE//Test//EN
            BEGIN:VEVENT
            UID:1
            SUMMARY:Past Event
            DTSTART:20230101T100000Z
            DTEND:20230101T110000Z
            END:VEVENT
            BEGIN:VEVENT
            UID:2
            SUMMARY:Future Event
            DTSTART:20250101T100000Z
            DTEND:20250101T110000Z
            END:VEVENT
            BEGIN:VEVENT
            UID:3
            SUMMARY:Specific Event
            DTSTART:20250601T100000Z
            DTEND:20250601T110000Z
            END:VEVENT
            END:VCALENDAR
            """;

    private Calendar multiZonesCal;

    /**
     * {@inheritDoc}
     */
    @BeforeEach
    public void setUp() throws Exception {
        calendarBuilder = new CalendarBuilder();

        File ical = new File(TestUtils.findTestData(new File(TESTDATAPATH, GOOGLE_ICS).getAbsolutePath()).getPath());
        try (Reader fileReader = new FileReader(ical)) {
            multiZonesCal = calendarBuilder.build(fileReader);
        }

        maintenanceCalendar = new MaintenanceCalendar("Test Calendar", TEST_ICAL);
        icalUtils = new IcalUtils();
    }

    /**
     * Tests calculating upcoming windows from 2 different timezones.
     * Both of the test times represent same point in time, therefore the list of maint. windows should be equal.
     */
    @Test
    public void testSameMomentDifferentTimezones() {
        ZonedDateTime newYorkStart = ZonedDateTime.parse("2020-06-09T08:00:00-04:00"); // NY
        ZonedDateTime tokyoStart = ZonedDateTime.parse("2020-06-09T21:00:00+09:00"); // Japan (same moment in time!)

        List<Pair<Instant, Instant>> listNewYork = icalUtils.calculateUpcomingPeriods(multiZonesCal, empty(),
                newYorkStart.toInstant(), 5).collect(Collectors.toList());

        List<Pair<Instant, Instant>> listTokyo = icalUtils.calculateUpcomingPeriods(multiZonesCal, empty(),
                tokyoStart.toInstant(), 5).collect(Collectors.toList());

        assertEquals(listNewYork, listTokyo);
    }

    /**
     * Test calculating upcoming windows at:
     * - 2:00 in New York
     * - 11:00 in Sri Lanka
     *
     * Since 11:00 in Sri Lanka equals to 1:30 in NY, we should see the maintenance window starting at 2:00 NY time.
     */
    @Test
    public void testSameLocalTimeAhead() {
        ZonedDateTime newYorkStart = ZonedDateTime.parse("2020-06-08T02:00:00-04:00"); // NY

        List<Pair<Instant, Instant>> listNewYork = icalUtils.calculateUpcomingPeriods(multiZonesCal, empty(),
                newYorkStart.toInstant(), 5).collect(Collectors.toList());

        // Sri Lanka is ahead of NYC, so at 11:00 in Sri Lanka, we still see maint. windows starting at 2:00 in NY
        ZonedDateTime sriLankaAlike = ZonedDateTime.parse("2020-06-08T11:00:00+05:30");
        List<Pair<Instant, Instant>> listSriLanka = icalUtils.calculateUpcomingPeriods(multiZonesCal, empty(),
                sriLankaAlike.toInstant(), 5).collect(Collectors.toList());
        assertEquals(listNewYork, listSriLanka);
    }

    /**
     * Opposite scenario to testSameLocalTimeAhead. Test calculating windows at:
     * - 8:00 in New York
     * - 8:00 on Tahiti
     *
     * At 8:00 Tahitian time, the maintenance window starting at 8:00 and ending at 10:00 NY time is already over,
     * so we shouldn't see it in the list.
     */
    @Test
    public void testSameLocalTimeBehind() {
        ZonedDateTime newYorkStart = ZonedDateTime.parse("2020-06-08T08:00:00-04:00"); // NY

        List<Pair<Instant, Instant>> listNewYork = icalUtils.calculateUpcomingPeriods(multiZonesCal, empty(),
                newYorkStart.toInstant(), 5).toList();

        // Tahiti, on the other hand is behind NYC, so at 8:00 Tahitian time, the maintenance windows at 8:00 NY time
        // is already over and we shouldn't see it
        ZonedDateTime tahitiLike = ZonedDateTime.parse("2020-06-08T08:00:00-10:00");
        List<Pair<Instant, Instant>> listTahiti = icalUtils.calculateUpcomingPeriods(multiZonesCal, empty(),
                tahitiLike.toInstant(), 4).collect(Collectors.toList());

        List<Pair<Instant, Instant>> newYorkButFirst = listNewYork.stream().skip(1).collect(Collectors.toList());
        // the Tahitian windows should match the NYC ones minus the first one
        assertEquals(newYorkButFirst, listTahiti);
    }

    /**
     * Test rendering maintenance windows in MULTI calendar
     */
    @Test
    public void testMultiScheduleFiltering() {
        ZonedDateTime datetime = ZonedDateTime.parse("2020-06-12T08:00:00-04:00");

        // let's generate the upcoming events and convert them to the human readable strings, so that the
        // dates in the tests are readable my human eyes too
        List<Pair<String, String>> nycEvents = icalUtils.calculateUpcomingPeriods(
                        multiZonesCal, of("Maint. windows - NYC - weekdays"), datetime.toInstant(), 3)
                .map(pair -> Pair.of(formatNY(pair.getLeft()), formatNY((pair.getRight()))))
                .collect(Collectors.toList());

        List<Pair<String, String>> sriLankaEvts = icalUtils.calculateUpcomingPeriods(
                        multiZonesCal, of("Maint. windows-Sri Lanka"), datetime.toInstant(), 3)
                .map(pair -> Pair.of(formatSriLanka(pair.getLeft()), formatSriLanka((pair.getRight()))))
                .collect(Collectors.toList());

        List<Pair<Instant, Instant>> listNoEvts = icalUtils.calculateUpcomingPeriods(
                        multiZonesCal, of("There is no window, only zuul"), datetime.toInstant(), 5)
                .toList();

        // NY maintenance windows take place every weekday, 8:00 - 10:00 local time
        assertEquals(
                List.of(
                        Pair.of("2020-06-12T08:00:00", "2020-06-12T10:00:00"),
                        Pair.of("2020-06-15T08:00:00", "2020-06-15T10:00:00"),
                        Pair.of("2020-06-16T08:00:00", "2020-06-16T10:00:00")
                ),
                nycEvents);

        // Sri Lanka maintenance windows take place just on MO, WE & FR, 8:00 - 10:30 local time
        assertEquals(
                List.of(
                        Pair.of("2020-06-15T08:00:00", "2020-06-15T10:30:00"),
                        Pair.of("2020-06-17T08:00:00", "2020-06-17T10:30:00"),
                        Pair.of("2020-06-19T08:00:00", "2020-06-19T10:30:00")
                ),
                sriLankaEvts);

        assertTrue(listNoEvts.isEmpty());
    }

    @Test
    public void canExtractEventNames() {
        Set<String> names = icalUtils.getEventNames(maintenanceCalendar);

        assertEquals(3, names.size());
        assertTrue(names.contains("Past Event"));
        assertTrue(names.contains("Future Event"));
        assertTrue(names.contains("Specific Event"));
    }

    @Test
    public void canGetAllEventsInRange() {
        long start = Instant.parse("2025-01-01T00:00:00Z").toEpochMilli();
        long end = Instant.parse("2025-12-31T23:59:59Z").toEpochMilli();

        var events = icalUtils.getCalendarEvents(maintenanceCalendar, Optional.empty(), start, end);

        assertEquals(2, events.size());
        assertEquals("Future Event", events.get(0).getName());
        assertEquals("Specific Event", events.get(1).getName());
    }

    @Test
    public void canGetFilteredEventsInRange() {
        long start = Instant.parse("2025-01-01T00:00:00Z").toEpochMilli();
        long end = Instant.parse("2025-12-31T23:59:59Z").toEpochMilli();

        var events = icalUtils.getCalendarEvents(maintenanceCalendar, Optional.of("Specific Event"), start, end);

        assertEquals(1, events.size());
        assertEquals("Specific Event", events.get(0).getName());
    }

    @Test
    public void canGetNextEvent() {
        long baseDate = Instant.parse("2024-01-01T00:00:00Z").toEpochMilli();

        Optional<MaintenanceWindowData> next = icalUtils.getNextEvent(maintenanceCalendar, Optional.empty(), baseDate);

        assertTrue(next.isPresent());
        assertEquals("Future Event", next.get().getName());
    }

    @Test
    public void canGetLastEvent() {
        long baseDate = Instant.parse("2024-01-01T00:00:00Z").toEpochMilli();

        Optional<MaintenanceWindowData> last = icalUtils.getLastEvent(maintenanceCalendar, Optional.empty(), baseDate);

        assertTrue(last.isPresent());
        assertEquals("Past Event", last.get().getName());
    }

    @Test
    public void testGetCalendarEventsAtDate() throws Exception {
        Optional<Calendar> parsedCal = Optional.of(calendarBuilder.build(new StringReader(TEST_ICAL)));
        Date targetDate = Date.from(Instant.parse("2025-01-01T10:30:00Z"));

        Collection<CalendarComponent> events;

        events = icalUtils.getCalendarEventsAtDate(targetDate, parsedCal, Optional.empty());
        assertEquals(1, events.size());

        Component event = events.iterator().next();
        assertEquals("Future Event", event.getProperty(Property.SUMMARY).getValue());

        events = icalUtils.getCalendarEventsAtDate(targetDate, parsedCal, Optional.of("Future Event"));
        assertEquals(1, events.size());
        assertEquals("Future Event", events.iterator().next().getProperty(Property.SUMMARY).getValue());

        events = icalUtils.getCalendarEventsAtDate(targetDate, parsedCal, Optional.of("Wrong Name"));
        assertTrue(events.isEmpty());
    }

    @Test
    public void testCalculateUpcomingMaintenanceWindowsWrapper() {
        maintenanceCalendar.setIcal(multiZonesCal.toString());

        MaintenanceSchedule schedule = new MaintenanceSchedule();
        schedule.setCalendar(maintenanceCalendar);
        schedule.setScheduleType(MaintenanceSchedule.ScheduleType.SINGLE);

        Instant start = Instant.parse("2020-06-12T08:00:00-04:00");

        Optional<List<MaintenanceWindowData>> windows = icalUtils.calculateUpcomingMaintenanceWindows(schedule, start);

        assertTrue(windows.isPresent());
        // Ensure the first 3 windows are ok
        assertEquals(
                List.of(
                        // First window should then NYC one
                        Pair.of(Instant.parse("2020-06-12T08:00:00-04:00"), Instant.parse("2020-06-12T10:00:00-04:00")),
                        // Then it's time for Sri Lanka
                        Pair.of(Instant.parse("2020-06-15T08:00:00+05:30"), Instant.parse("2020-06-15T10:30:00+05:30")),
                        // Then NYC again
                        Pair.of(Instant.parse("2020-06-15T08:00:00-04:00"), Instant.parse("2020-06-15T10:00:00-04:00"))
                ),
                windows.stream().flatMap(v -> v.stream())
                        .map(window -> Pair.of(
                            Instant.ofEpochMilli(window.getFromMilliseconds()),
                            Instant.ofEpochMilli(window.getToMilliseconds())
                        ))
                        .limit(3)
                        .toList()
        );
    }

    @Test
    public void testParseInvalidCalendar() {
        maintenanceCalendar.setIcal("THIS IS NOT A VALID CALENDAR");

        Optional<Calendar> result = icalUtils.parseCalendar(maintenanceCalendar);

        assertFalse(result.isPresent(), "Should return empty Optional for invalid ICAL");

        Set<String> names = icalUtils.getEventNames(maintenanceCalendar);
        assertTrue(names.isEmpty());
    }

    private static String formatNY(Instant instant) {
        return instant.atZone(ZoneId.of("America/New_York")).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private static String formatSriLanka(Instant instant) {
        return instant.atZone(ZoneId.of("Asia/Colombo")).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
