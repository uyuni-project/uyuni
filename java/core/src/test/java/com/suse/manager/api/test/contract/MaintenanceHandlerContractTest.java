/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.api.test.contract;

import com.redhat.rhn.domain.user.User;

import com.suse.manager.maintenance.rescheduling.RescheduleResult;
import com.suse.manager.model.maintenance.MaintenanceCalendar;
import com.suse.manager.model.maintenance.MaintenanceSchedule;
import com.suse.manager.xmlrpc.maintenance.MaintenanceHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MaintenanceHandlerContractTest extends BaseOpenApiTest {

    private static final String SCHEDULE_NAME = "test-schedule";
    private static final String CALENDAR_LABEL = "test-calendar";
    private static final String ICAL = "BEGIN:VCALENDAR\nVERSION:2.0\nEND:VCALENDAR";
    private static final String CALENDAR_URL = "https://example.com/maintenance.ics";
    private static final List<String> STRATEGY = List.of("Cancel");
    private static final List<Integer> SIDS = List.of(1000010000, 1000010001);

    @Override
    protected String getApiNamespace() {
        return "maintenance";
    }

    @Override
    protected Class<MaintenanceHandler> getHandlerClass() {
        return MaintenanceHandler.class;
    }

    private MaintenanceHandler handler() {
        return (MaintenanceHandler) handlerMock;
    }

    /**
     * The calendar is documented as part of every schedule, so the schedule the handler answers
     * with always carries one.
     *
     * @return a maintenance schedule carrying a calendar
     */
    private MaintenanceSchedule schedule() {
        MaintenanceSchedule schedule = new MaintenanceSchedule();
        schedule.setId(10L);
        schedule.setOrg(fakeOrg);
        schedule.setName(SCHEDULE_NAME);
        schedule.setScheduleType(MaintenanceSchedule.ScheduleType.SINGLE);
        schedule.setCalendar(calendar());
        return schedule;
    }

    /**
     * The URL is documented as part of every calendar, so the calendar the handler answers with
     * always carries one.
     *
     * @return a maintenance calendar carrying a URL
     */
    private MaintenanceCalendar calendar() {
        MaintenanceCalendar maintenanceCalendar = new MaintenanceCalendar();
        maintenanceCalendar.setId(20L);
        maintenanceCalendar.setOrg(fakeOrg);
        maintenanceCalendar.setLabel(CALENDAR_LABEL);
        maintenanceCalendar.setUrl(CALENDAR_URL);
        maintenanceCalendar.setIcal(ICAL);
        return maintenanceCalendar;
    }

    /**
     * @return a reschedule result that affected no action
     */
    private RescheduleResult rescheduleResult() {
        return new RescheduleResult(SCHEDULE_NAME, true);
    }

    @Test
    public void testListScheduleNames() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listScheduleNames(with(mockUser));
            will(returnValue(List.of(SCHEDULE_NAME, "other-schedule")));
        }});

        validateApiContract("/maintenance/listScheduleNames", "POST")
                .onHandlerMethod("listScheduleNames", User.class);
    }

    @Test
    public void testGetScheduleDetails() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getScheduleDetails(with(mockUser), with(SCHEDULE_NAME));
            will(returnValue(schedule()));
        }});

        validateApiContract("/maintenance/getScheduleDetails", "POST")
                .withBody(Map.of("name", SCHEDULE_NAME))
                .onHandlerMethod("getScheduleDetails", User.class, String.class);
    }

    @Test
    public void testCreateSchedule() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", SCHEDULE_NAME);
        body.put("type", "single");
        body.put("calendar", CALENDAR_LABEL);

        context.checking(new Expectations() {{
            oneOf(handler()).createSchedule(with(mockUser), with(SCHEDULE_NAME), with("single"),
                    with(CALENDAR_LABEL));
            will(returnValue(schedule()));
        }});

        validateApiContract("/maintenance/createSchedule", "POST")
                .withBody(body)
                .onHandlerMethod("createSchedule", User.class, String.class, String.class, String.class);
    }

    /**
     * The calendar label is documented as optional, so the shorter overload has to accept a
     * request that leaves it out.
     */
    @Test
    public void testCreateScheduleWithoutCalendar() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", SCHEDULE_NAME);
        body.put("type", "multi");

        context.checking(new Expectations() {{
            oneOf(handler()).createSchedule(with(mockUser), with(SCHEDULE_NAME), with("multi"));
            will(returnValue(schedule()));
        }});

        validateApiContract("/maintenance/createSchedule", "POST")
                .withBody(body)
                .onHandlerMethod("createSchedule", User.class, String.class, String.class);
    }

    @Test
    public void testUpdateSchedule() throws Exception {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("type", "multi");
        details.put("calendar", CALENDAR_LABEL);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", SCHEDULE_NAME);
        body.put("details", details);
        body.put("rescheduleStrategy", STRATEGY);

        context.checking(new Expectations() {{
            oneOf(handler()).updateSchedule(with(mockUser), with(SCHEDULE_NAME), with(details), with(STRATEGY));
            will(returnValue(rescheduleResult()));
        }});

        validateApiContract("/maintenance/updateSchedule", "POST")
                .withBody(body)
                .onHandlerMethod("updateSchedule", User.class, String.class, Map.class, List.class);
    }

    @Test
    public void testDeleteSchedule() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).deleteSchedule(with(mockUser), with(SCHEDULE_NAME));
            will(returnValue(1));
        }});

        validateApiContract("/maintenance/deleteSchedule", "POST")
                .withBody(Map.of("name", SCHEDULE_NAME))
                .onHandlerMethod("deleteSchedule", User.class, String.class);
    }

    @Test
    public void testListCalendarLabels() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listCalendarLabels(with(mockUser));
            will(returnValue(List.of(CALENDAR_LABEL, "other-calendar")));
        }});

        validateApiContract("/maintenance/listCalendarLabels", "POST")
                .onHandlerMethod("listCalendarLabels", User.class);
    }

    @Test
    public void testGetCalendarDetails() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getCalendarDetails(with(mockUser), with(CALENDAR_LABEL));
            will(returnValue(calendar()));
        }});

        validateApiContract("/maintenance/getCalendarDetails", "POST")
                .withBody(Map.of("label", CALENDAR_LABEL))
                .onHandlerMethod("getCalendarDetails", User.class, String.class);
    }

    @Test
    public void testCreateCalendar() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("label", CALENDAR_LABEL);
        body.put("ical", ICAL);

        context.checking(new Expectations() {{
            oneOf(handler()).createCalendar(with(mockUser), with(CALENDAR_LABEL), with(ICAL));
            will(returnValue(calendar()));
        }});

        validateApiContract("/maintenance/createCalendar", "POST")
                .withBody(body)
                .onHandlerMethod("createCalendar", User.class, String.class, String.class);
    }

    @Test
    public void testCreateCalendarWithUrl() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("label", CALENDAR_LABEL);
        body.put("url", CALENDAR_URL);

        context.checking(new Expectations() {{
            oneOf(handler()).createCalendarWithUrl(with(mockUser), with(CALENDAR_LABEL), with(CALENDAR_URL));
            will(returnValue(calendar()));
        }});

        validateApiContract("/maintenance/createCalendarWithUrl", "POST")
                .withBody(body)
                .onHandlerMethod("createCalendarWithUrl", User.class, String.class, String.class);
    }

    @Test
    public void testUpdateCalendar() throws Exception {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("ical", ICAL);
        details.put("url", CALENDAR_URL);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("label", CALENDAR_LABEL);
        body.put("details", details);
        body.put("rescheduleStrategy", STRATEGY);

        context.checking(new Expectations() {{
            oneOf(handler()).updateCalendar(with(mockUser), with(CALENDAR_LABEL), with(details), with(STRATEGY));
            will(returnValue(List.of(rescheduleResult())));
        }});

        validateApiContract("/maintenance/updateCalendar", "POST")
                .withBody(body)
                .onHandlerMethod("updateCalendar", User.class, String.class, Map.class, List.class);
    }

    @Test
    public void testRefreshCalendar() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("label", CALENDAR_LABEL);
        body.put("rescheduleStrategy", STRATEGY);

        context.checking(new Expectations() {{
            oneOf(handler()).refreshCalendar(with(mockUser), with(CALENDAR_LABEL), with(STRATEGY));
            will(returnValue(List.of(rescheduleResult())));
        }});

        validateApiContract("/maintenance/refreshCalendar", "POST")
                .withBody(body)
                .onHandlerMethod("refreshCalendar", User.class, String.class, List.class);
    }

    @Test
    public void testDeleteCalendar() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("label", CALENDAR_LABEL);
        body.put("cancelScheduledActions", true);

        context.checking(new Expectations() {{
            oneOf(handler()).deleteCalendar(with(mockUser), with(CALENDAR_LABEL), with(true));
            will(returnValue(List.of(rescheduleResult())));
        }});

        validateApiContract("/maintenance/deleteCalendar", "POST")
                .withBody(body)
                .onHandlerMethod("deleteCalendar", User.class, String.class, boolean.class);
    }

    @Test
    public void testAssignScheduleToSystems() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("scheduleName", SCHEDULE_NAME);
        body.put("sids", SIDS);
        body.put("rescheduleStrategy", STRATEGY);

        context.checking(new Expectations() {{
            oneOf(handler()).assignScheduleToSystems(with(mockUser), with(SCHEDULE_NAME), with(SIDS),
                    with(STRATEGY));
            will(returnValue(2));
        }});

        validateApiContract("/maintenance/assignScheduleToSystems", "POST")
                .withBody(body)
                .onHandlerMethod("assignScheduleToSystems", User.class, String.class, List.class, List.class);
    }

    @Test
    public void testRetractScheduleFromSystems() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).retractScheduleFromSystems(with(mockUser), with(SIDS));
            will(returnValue(2));
        }});

        validateApiContract("/maintenance/retractScheduleFromSystems", "POST")
                .withBody(Map.of("sids", SIDS))
                .onHandlerMethod("retractScheduleFromSystems", User.class, List.class);
    }

    @Test
    public void testListSystemsWithSchedule() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listSystemsWithSchedule(with(mockUser), with(SCHEDULE_NAME));
            will(returnValue(List.of(1000010000L, 1000010001L)));
        }});

        validateApiContract("/maintenance/listSystemsWithSchedule", "POST")
                .withBody(Map.of("scheduleName", SCHEDULE_NAME))
                .onHandlerMethod("listSystemsWithSchedule", User.class, String.class);
    }
}
