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
package com.suse.manager.model.maintenance.test;

import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;

import com.suse.manager.model.maintenance.CalendarAssignment;
import com.suse.manager.model.maintenance.CalendarFactory;
import com.suse.manager.model.maintenance.MaintenanceCalendar;
import com.suse.manager.model.maintenance.MaintenanceSchedule;
import com.suse.manager.model.maintenance.ScheduleFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Tuple;

/**
 * Test For {@link CalendarFactory}
 */
public class CalendarFactoryTest extends JMockBaseTestCaseWithUser {

    private CalendarFactory calendarFactory;
    private ScheduleFactory scheduleFactory;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.calendarFactory = new CalendarFactory();
        this.scheduleFactory = new ScheduleFactory();
    }

    /**
     * Test listing all calendars with their schedules
     */
    public void testListCalendarsWithSchedules() {
        MaintenanceCalendar calendarNoSchedule = new MaintenanceCalendar();
        calendarNoSchedule.setLabel("my-calendar-no-schedule");
        calendarNoSchedule.setOrg(user.getOrg());
        calendarNoSchedule.setIcal("some content");
        calendarFactory.save(calendarNoSchedule);

        MaintenanceCalendar calendar = new MaintenanceCalendar();
        calendar.setLabel("my-calendar-with-schedule");
        calendar.setOrg(user.getOrg());
        calendar.setIcal("some content");
        calendarFactory.save(calendar);

        MaintenanceSchedule schedule = new MaintenanceSchedule();
        schedule.setCalendar(calendar);
        schedule.setName("my-schedule-with-calendar");
        schedule.setOrg(user.getOrg());
        schedule.setScheduleType(MaintenanceSchedule.ScheduleType.SINGLE);
        scheduleFactory.save(schedule);

        MaintenanceSchedule schedule2 = new MaintenanceSchedule();
        schedule2.setCalendar(calendar);
        schedule2.setName("my-schedule-with-calendar-2");
        schedule2.setOrg(user.getOrg());
        schedule2.setScheduleType(MaintenanceSchedule.ScheduleType.SINGLE);
        scheduleFactory.save(schedule2);

        List<CalendarAssignment> result = calendarFactory.listCalendarToSchedulesAssignments(user);

        assertEquals(3, result.size());

        Set<List<Object>> tuplesAsLists = result.stream()
                .map(tuple -> createList(
                        tuple.getCalendarId(),
                        tuple.getCalendarName(),
                        tuple.getScheduleId(),
                        tuple.getScheduleName()))
                .collect(Collectors.toSet());

        // we need to create this list explicitly, as List.of does not support adding null elems
        Set<List<?>> expectedTuples = Set.of(
                createList(calendarNoSchedule.getId(), calendarNoSchedule.getLabel(), null, null),
                createList(calendar.getId(), calendar.getLabel(), schedule.getId(), schedule.getName()),
                createList(calendar.getId(), calendar.getLabel(), schedule2.getId(), schedule2.getName())
        );
        assertEquals(expectedTuples, tuplesAsLists);
    }

    // we need to create our own list creator, as List.of does not support adding null elements
    private static List<Object> createList(Object ... elems) {
        ArrayList<Object> result = new ArrayList<>();
        for (Object elem : elems) {
            result.add(elem);
        }
        return result;
    }
}
