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
package com.suse.manager.maintenance.test;

import static com.redhat.rhn.domain.role.RoleFactory.ORG_ADMIN;

import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.server.test.ServerActionTest;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.maintenance.CancelRescheduleStrategy;
import com.suse.manager.maintenance.MaintenanceManager;
import com.suse.manager.maintenance.RescheduleStrategy;
import com.suse.manager.model.maintenance.MaintenanceCalendar;
import com.suse.manager.model.maintenance.MaintenanceSchedule;
import com.suse.manager.model.maintenance.MaintenanceSchedule.ScheduleType;

import java.io.File;
import java.io.StringReader;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;


public class MaintenanceManagerTest extends BaseTestCaseWithUser {

    private static final String TESTDATAPATH = "/com/suse/manager/maintenance/test/testdata";
    private static final String KDE_ICS = "maintenance-windows-kde.ics";
    private static final String KDE2_ICS = "maintenance-windows-kde-2.ics";
    private static final String EXCHANGE_ICS = "maintenance-windows-exchange.ics";
    private static final String EXCHANGE_MULTI1_ICS = "maintenance-windows-multi-exchange-1.ics";
    private static final String EXCHANGE_MULTI2_ICS = "maintenance-windows-multi-exchange-2.ics";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        TestUtils.saveAndFlush(user);
    }

    public void testCreateSchedule() throws Exception {
        MaintenanceManager mm = MaintenanceManager.instance();
        mm.createMaintenanceSchedule(user, "test server", ScheduleType.SINGLE, Optional.empty());

        List<String> names = mm.listScheduleNamesByUser(user);
        assertEquals(1, names.size());
        assertContains(names, "test server");

        Optional<MaintenanceSchedule> dbScheduleOpt = mm.lookupMaintenanceScheduleByUserAndName(user, "test server");
        assertNotNull(dbScheduleOpt.orElse(null));
        MaintenanceSchedule dbSchedule = dbScheduleOpt.get();

        assertEquals(user.getOrg(), dbSchedule.getOrg());
        assertEquals("test server", dbSchedule.getName());
        assertEquals(ScheduleType.SINGLE, dbSchedule.getScheduleType());
        assertTrue(dbSchedule.getCalendarOpt().isEmpty());

    }

    public void testCreateCalendar() throws Exception {
        File ical = new File(TestUtils.findTestData(
                new File(TESTDATAPATH,  KDE_ICS).getAbsolutePath()).getPath());

        MaintenanceManager mm = MaintenanceManager.instance();
        mm.createMaintenanceCalendar(user, "testcalendar", FileUtils.readStringFromFile(ical.getAbsolutePath()));

        List<String> labels = mm.listCalendarLabelsByUser(user);
        assertEquals(1, labels.size());
        assertContains(labels, "testcalendar");

        Optional<MaintenanceCalendar> dbCalOpt = mm.lookupCalendarByUserAndLabel(user, "testcalendar");
        assertNotNull(dbCalOpt.orElse(null));
        MaintenanceCalendar dbCal = dbCalOpt.get();

        assertEquals(user.getOrg(), dbCal.getOrg());
        assertEquals("testcalendar", dbCal.getLabel());
        assertEquals(FileUtils.readStringFromFile(ical.getAbsolutePath()), dbCal.getIcal());
        assertNull(dbCal.getUrlOpt().orElse(null));
    }

    public void testCreateScheduleWithCalendar() throws Exception {
        File ical = new File(TestUtils.findTestData(
                new File(TESTDATAPATH,  KDE_ICS).getAbsolutePath()).getPath());
        MaintenanceManager mm = MaintenanceManager.instance();

        MaintenanceCalendar mc = mm.createMaintenanceCalendar(user, "testcalendar", FileUtils.readStringFromFile(ical.getAbsolutePath()));
        mm.createMaintenanceSchedule(user, "test server", ScheduleType.SINGLE, Optional.of(mc));

        List<String> names = mm.listScheduleNamesByUser(user);
        assertEquals(1, names.size());
        assertContains(names, "test server");

        Optional<MaintenanceSchedule> dbScheduleOpt = mm.lookupMaintenanceScheduleByUserAndName(user, "test server");
        assertNotNull(dbScheduleOpt.orElse(null));
        MaintenanceSchedule dbSchedule = dbScheduleOpt.get();

        assertEquals(user.getOrg(), dbSchedule.getOrg());
        assertEquals("test server", dbSchedule.getName());
        assertEquals(ScheduleType.SINGLE, dbSchedule.getScheduleType());
        assertNotNull(dbSchedule.getCalendarOpt().orElse(null));

        MaintenanceCalendar dbCal = dbSchedule.getCalendarOpt().get();
        assertEquals(user.getOrg(), dbCal.getOrg());
        assertEquals("testcalendar", dbCal.getLabel());
        assertEquals(FileUtils.readStringFromFile(ical.getAbsolutePath()), dbCal.getIcal());
        assertNull(dbCal.getUrlOpt().orElse(null));
    }

    public void testUpdateScheduleWithCalendarURL() throws Exception {
        File icalKde = new File(TestUtils.findTestData(
                new File(TESTDATAPATH,  KDE_ICS).getAbsolutePath()).getPath());
        File icalEx = new File(TestUtils.findTestData(
                new File(TESTDATAPATH,  EXCHANGE_ICS).getAbsolutePath()).getPath());
        MaintenanceManager mm = new MaintenanceManager() {
            @Override
            protected String fetchCalendarData(String url) {
                return FileUtils.readStringFromFile(icalEx.getAbsolutePath());
            }
        };

        MaintenanceCalendar mc = mm.createMaintenanceCalendar(user, "testcalendar", FileUtils.readStringFromFile(icalKde.getAbsolutePath()));
        mm.createMaintenanceSchedule(user, "test server", ScheduleType.SINGLE, Optional.of(mc));

        Optional<MaintenanceSchedule> dbScheduleOpt = mm.lookupMaintenanceScheduleByUserAndName(user, "test server");
        assertNotNull(dbScheduleOpt.orElse(null));
        MaintenanceSchedule dbSchedule = dbScheduleOpt.get();
        assertNotNull(dbSchedule.getCalendarOpt().orElse(null));

        MaintenanceCalendar dbCal = dbSchedule.getCalendarOpt().get();
        assertEquals(user.getOrg(), dbCal.getOrg());
        assertEquals("testcalendar", dbCal.getLabel());
        assertEquals(FileUtils.readStringFromFile(icalKde.getAbsolutePath()), dbCal.getIcal());
        assertNull(dbCal.getUrlOpt().orElse(null));

        Map<String, String> details = new HashMap<>();
        details.put("url", "http://dummy.domain.top/exchange");

        mm.updateCalendar(user, "testcalendar", details, Collections.emptyList());

        dbCal = mm.lookupCalendarByUserAndLabel(user, "testcalendar").orElseThrow(() -> new RuntimeException("Cannot find testcalendar"));
        assertEquals(user.getOrg(), dbCal.getOrg());
        assertEquals("testcalendar", dbCal.getLabel());
        assertEquals(FileUtils.readStringFromFile(icalEx.getAbsolutePath()), dbCal.getIcal());
        assertNotNull(dbCal.getUrlOpt().orElse(null));
        assertEquals("http://dummy.domain.top/exchange", dbCal.getUrlOpt().get());

        Map<String, String> sDetails = new HashMap<>();
        sDetails.put("type", "multi");

        mm.updateMaintenanceSchedule(user, "test server", sDetails, Collections.emptyList());

        dbScheduleOpt = mm.lookupMaintenanceScheduleByUserAndName(user, "test server");
        assertNotNull(dbScheduleOpt.orElse(null));
        dbSchedule = dbScheduleOpt.get();

        assertEquals(user.getOrg(), dbSchedule.getOrg());
        assertEquals("test server", dbSchedule.getName());
        assertEquals(ScheduleType.MULTI, dbSchedule.getScheduleType());
        assertEquals(dbCal, dbSchedule.getCalendarOpt().orElse(null));
    }

    /**
     * Test assigning a {@link MaintenanceSchedule} to {@link Server}s and listing it.
     *
     * @throws Exception
     */
    public void testListSystemsWithSchedule() throws Exception {
        user.addPermanentRole(ORG_ADMIN);
        MaintenanceManager mm = MaintenanceManager.instance();
        MaintenanceSchedule schedule = mm.createMaintenanceSchedule(
                user, "test-schedule-1", MaintenanceSchedule.ScheduleType.SINGLE, Optional.empty());

        Server withSchedule = MinionServerFactoryTest.createTestMinionServer(user);
        Server withoutSchedule = MinionServerFactoryTest.createTestMinionServer(user);

        mm.assignScheduleToSystems(user, schedule, Set.of(withSchedule.getId()));

        assertEquals(
                List.of(withSchedule.getId()),
                mm.listSystemIdsWithSchedule(user, schedule)
        );
    }

    /**
     * Test retracting a {@link MaintenanceSchedule} from {@link Server}
     *
     * @throws Exception
     */
    public void testRetractScheduleFromSystems() throws Exception {
        user.addPermanentRole(ORG_ADMIN);
        MaintenanceManager mm = MaintenanceManager.instance();
        MaintenanceSchedule schedule = MaintenanceManager.instance().createMaintenanceSchedule(
                user, "test-schedule-1", MaintenanceSchedule.ScheduleType.SINGLE, Optional.empty());

        // assign the schedule to both systems
        Server system1 = MinionServerFactoryTest.createTestMinionServer(user);
        Server system2 = MinionServerFactoryTest.createTestMinionServer(user);
        mm.assignScheduleToSystems(user, schedule, Set.of(system1.getId(), system2.getId()));

        // retract it from one system
        mm.retractScheduleFromSystems(user, Set.of(system1.getId()));

        // check, that the other system still has it
        assertEquals(
                List.of(system2.getId()),
                mm.listSystemIdsWithSchedule(user, schedule)
        );
    }

    /**
     * Test the behavior when user tries to assign a schedule to a system, that has already some offending actions
     * pending.
     *
     * @throws Exception
     */
    public void testAssignScheduleToSystemWithPendingActions() throws Exception {
        user.addPermanentRole(ORG_ADMIN);
        MaintenanceManager mm = MaintenanceManager.instance();
        MaintenanceSchedule schedule = MaintenanceManager.instance().createMaintenanceSchedule(
                user, "test-schedule-1", MaintenanceSchedule.ScheduleType.SINGLE, Optional.empty());

        Server sys1 = MinionServerFactoryTest.createTestMinionServer(user);
        Server sys2 = MinionServerFactoryTest.createTestMinionServer(user);

        // assign an offending action to one system
        Action disallowedAction = ActionFactoryTest.createAction(user, ActionFactory.TYPE_APPLY_STATES);
        ServerActionTest.createServerAction(sys1, disallowedAction);

        assertExceptionThrown(
                () -> mm.assignScheduleToSystems(user, schedule, Set.of(sys1.getId(), sys2.getId())),
                IllegalArgumentException.class);

        // assign an action not tied to maintenance mode
        Action allowedAction = createActionForServerAt(ActionFactory.TYPE_VIRTUALIZATION_START, sys2, "2020-04-13T08:15:00+02:00");
        assertEquals(1, mm.assignScheduleToSystems(user, schedule, Set.of(sys2.getId())));
    }

    /**
     * Test schedules assigning in a cross-organization context.
     *
     * @throws Exception
     */
    public void testAssignScheduleCrossOrg() throws Exception {
        MaintenanceManager mm = MaintenanceManager.instance();
        user.addPermanentRole(ORG_ADMIN);
        Org acmeOrg = UserTestUtils.createNewOrgFull("acme-123");
        User user2 = UserTestUtils.createUser("user-321", acmeOrg.getId());
        user2.addPermanentRole(ORG_ADMIN);

        MaintenanceSchedule schedule1 = MaintenanceManager.instance().createMaintenanceSchedule(
                user, "test-schedule-1", MaintenanceSchedule.ScheduleType.SINGLE, Optional.empty());
        MaintenanceSchedule schedule2 = MaintenanceManager.instance().createMaintenanceSchedule(
                user2, "test-schedule-2", MaintenanceSchedule.ScheduleType.SINGLE, Optional.empty());

        Server system1 = MinionServerFactoryTest.createTestMinionServer(user);
        Server system2 = MinionServerFactoryTest.createTestMinionServer(user2);

        // user 2 assigns schedule 1
        assertExceptionThrown(
                () -> mm.assignScheduleToSystems(user2, schedule1, Set.of(system2.getId())),
                PermissionException.class);

        // user 2 retracts from system1
        assertExceptionThrown(
                () -> mm.retractScheduleFromSystems(user2, Set.of(system1.getId())),
                PermissionException.class);

        // user 2 assigns to system 1
        assertExceptionThrown(
                () -> mm.assignScheduleToSystems(user2, schedule2, Set.of(system1.getId())),
                PermissionException.class);

        // user 2 lists systems with schedule 1
        assertExceptionThrown(
                () -> mm.listSystemIdsWithSchedule(user2, schedule1),
                PermissionException.class);
    }

    public void testActionInMaintenanceWindow() throws Exception {
        File icalKde = new File(TestUtils.findTestData(
                new File(TESTDATAPATH,  KDE_ICS).getAbsolutePath()).getPath());
        File icalKde2 = new File(TestUtils.findTestData(
                new File(TESTDATAPATH,  KDE2_ICS).getAbsolutePath()).getPath());
        MaintenanceManager mm = new MaintenanceManager() {
            @Override
            protected String fetchCalendarData(String url) {
                return FileUtils.readStringFromFile(icalKde2.getAbsolutePath());
            }
        };

        MaintenanceCalendar mc = mm.createMaintenanceCalendar(user, "testcalendar", FileUtils.readStringFromFile(icalKde.getAbsolutePath()));
        MaintenanceSchedule ms = mm.createMaintenanceSchedule(user, "test server", ScheduleType.SINGLE, Optional.of(mc));

        Server server = ServerTestUtils.createTestSystem(user);

        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_ERRATA);
        ZonedDateTime start = ZonedDateTime.parse("2020-04-21T09:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        action.setEarliestAction(Date.from(start.toInstant()));

        ServerAction serverAction = ServerActionTest.createServerAction(server, action);
        serverAction.setStatus(ActionFactory.STATUS_QUEUED);

        action.addServerAction(serverAction);
        ActionManager.storeAction(action);

        StringReader sin = new StringReader(FileUtils.readStringFromFile(icalKde.getAbsolutePath()));
        CalendarBuilder builder = new CalendarBuilder();
        Calendar calendar = null;
        calendar = builder.build(sin);

        assertFalse(mm.isActionInMaintenanceWindow(action, ms, Optional.ofNullable(calendar)));

        start = ZonedDateTime.parse("2020-04-20T09:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        action.setEarliestAction(Date.from(start.toInstant()));
        ActionManager.storeAction(action);
        assertTrue(mm.isActionInMaintenanceWindow(action, ms, Optional.ofNullable(calendar)));

        // icalKde2 has an EXDATE 20200420 set
        sin = new StringReader(FileUtils.readStringFromFile(icalKde2.getAbsolutePath()));
        builder = new CalendarBuilder();
        calendar = null;
        calendar = builder.build(sin);
        assertFalse(mm.isActionInMaintenanceWindow(action, ms, Optional.ofNullable(calendar)));
    }

    public void testScheduleChangeMultiWithCancel() throws Exception {
        File icalExM1 = new File(TestUtils.findTestData(
                new File(TESTDATAPATH,  EXCHANGE_MULTI1_ICS).getAbsolutePath()).getPath());
        File icalExM2 = new File(TestUtils.findTestData(
                new File(TESTDATAPATH,  EXCHANGE_MULTI2_ICS).getAbsolutePath()).getPath());

        /* setup test environment */
        Server sapServer = ServerTestUtils.createTestSystem(user);
        Server coreServer = ServerTestUtils.createTestSystem(user);

        MaintenanceManager mm = new MaintenanceManager();
        MaintenanceCalendar mcal = mm.createMaintenanceCalendar(user, "multicalendar", FileUtils.readStringFromFile(icalExM1.getAbsolutePath()));
        MaintenanceSchedule sapSchedule = mm.createMaintenanceSchedule(user, "SAP Maintenance Window", ScheduleType.MULTI, Optional.of(mcal));
        MaintenanceSchedule coreSchedule = mm.createMaintenanceSchedule(user, "Core Server Window", ScheduleType.MULTI, Optional.of(mcal));

        mm.assignScheduleToSystems(user, sapSchedule, Collections.singleton(sapServer.getId()));
        mm.assignScheduleToSystems(user, coreSchedule, Collections.singleton(coreServer.getId()));

        Action sapAction1 = createActionForServerAt(ActionFactory.TYPE_ERRATA, sapServer, "2020-04-13T08:15:00+02:00"); //moved
        Action sapActionEx = createActionForServerAt(ActionFactory.TYPE_VIRTUALIZATION_START, sapServer, "2020-04-13T08:15:00+02:00"); //moved
        Action sapAction2 = createActionForServerAt(ActionFactory.TYPE_ERRATA, sapServer, "2020-04-27T08:15:00+02:00"); //stay
        Action sapAction3 = createActionForServerAt(ActionFactory.TYPE_ERRATA, sapServer, "2020-04-30T09:15:00+02:00"); //wrong window (Core)
        Action coreAction1 = createActionForServerAt(ActionFactory.TYPE_ERRATA, coreServer, "2020-04-30T09:15:00+02:00"); //stay
        Action coreActionEx = createActionForServerAt(ActionFactory.TYPE_VIRTUALIZATION_START, coreServer, "2020-05-21T09:15:00+02:00"); //moved
        Action coreAction2 = createActionForServerAt(ActionFactory.TYPE_ERRATA, coreServer, "2020-05-21T09:15:00+02:00"); //moved
        Action coreAction3 = createActionForServerAt(ActionFactory.TYPE_ERRATA, coreServer, "2020-04-27T08:15:00+02:00"); //wrong window (SAP)

        List sapActionsBefore = ActionFactory.listActionsForServer(user, sapServer);
        List coreActionsBefore = ActionFactory.listActionsForServer(user, coreServer);

        assertEquals(4, sapActionsBefore.size());
        assertEquals(4, coreActionsBefore.size());

        /* update the calendar */
        Map<String, String> details = new HashMap<>();
        details.put("ical", FileUtils.readStringFromFile(icalExM2.getAbsolutePath()));

        List<RescheduleStrategy> rescheduleStrategy = new LinkedList<>();
        rescheduleStrategy.add(new CancelRescheduleStrategy());

        mm.updateCalendar(user, "multicalendar", details, rescheduleStrategy);

        /* check results */
        List<Action> sapActionsAfter = ActionFactory.listActionsForServer(user, sapServer);
        List<Action> coreActionsAfter = ActionFactory.listActionsForServer(user, coreServer);

        assertEquals(2, sapActionsAfter.size());
        assertEquals(2, coreActionsAfter.size());

        assertEquals(1, sapActionsAfter.stream().filter(a -> a.equals(sapAction2)).count());
        assertEquals(1, sapActionsAfter.stream().filter(a -> a.equals(sapActionEx)).count()); //Action not tied to maintenance mode

        assertEquals(1, coreActionsAfter.stream().filter(a -> a.equals(coreAction1)).count());
        assertEquals(1, coreActionsAfter.stream().filter(a -> a.equals(coreActionEx)).count()); //Action not tied to maintenance mode

    }

    public void testScheduleChangeMultiWithActionChain() throws Exception {
        File icalExM1 = new File(TestUtils.findTestData(
                new File(TESTDATAPATH,  EXCHANGE_MULTI1_ICS).getAbsolutePath()).getPath());
        File icalExM2 = new File(TestUtils.findTestData(
                new File(TESTDATAPATH,  EXCHANGE_MULTI2_ICS).getAbsolutePath()).getPath());

        /* setup test environment */
        Server sapServer = ServerTestUtils.createTestSystem(user);
        Server coreServer = ServerTestUtils.createTestSystem(user);

        MaintenanceManager mm = new MaintenanceManager();
        MaintenanceCalendar mcal = mm.createMaintenanceCalendar(user, "multicalendar", FileUtils.readStringFromFile(icalExM1.getAbsolutePath()));
        MaintenanceSchedule sapSchedule = mm.createMaintenanceSchedule(user, "SAP Maintenance Window", ScheduleType.MULTI, Optional.of(mcal));
        MaintenanceSchedule coreSchedule = mm.createMaintenanceSchedule(user, "Core Server Window", ScheduleType.MULTI, Optional.of(mcal));

        mm.assignScheduleToSystems(user, sapSchedule, Collections.singleton(sapServer.getId()));
        mm.assignScheduleToSystems(user, coreSchedule, Collections.singleton(coreServer.getId()));

        // Action Chain which start inside of the window, but has parts outside of the window
        // Expected Result: No change
        Action sapAction1 = createActionForServerAt(ActionFactory.TYPE_ERRATA, sapServer, "2020-04-27T09:59:00+02:00"); //stay (MW end at 10am)
        Action sapAction2 = createActionForServerAt(ActionFactory.TYPE_REBOOT, sapServer, "2020-04-27T10:01:00+02:00", sapAction1); //stay (MW end at 10am)

        // Action Chain which start with an action not tied to a maintenance window
        // Expected Result: Cancel all Actions
        Action sapAction3 = createActionForServerAt(ActionFactory.TYPE_VIRTUALIZATION_START, sapServer, "2020-04-13T09:59:00+02:00"); //moved
        Action sapAction4 = createActionForServerAt(ActionFactory.TYPE_ERRATA, sapServer, "2020-04-13T08:10:02+02:00", sapAction3); //moved

        List<Action> sapActionsBefore = ActionFactory.listActionsForServer(user, sapServer);
        assertEquals(4, sapActionsBefore.size());

        // Action Chain which is inside of a Window but the window gets moved.
        // Expected Result: Cancel all Actions
        Action coreAction1 = createActionForServerAt(ActionFactory.TYPE_ERRATA, coreServer, "2020-05-21T09:15:00+02:00"); //moved
        Action coreAction2 = createActionForServerAt(ActionFactory.TYPE_REBOOT, coreServer, "2020-05-21T09:16:00+02:00", coreAction1); //moved

        // Action Chain which start with an action not tied to a maintenance window
        // Expected Result: No change
        Action coreAction3 = createActionForServerAt(ActionFactory.TYPE_VIRTUALIZATION_START, coreServer, "2020-04-30T11:59:30+02:00"); //stay
        Action coreAction4 = createActionForServerAt(ActionFactory.TYPE_ERRATA, coreServer, "2020-04-30T13:01:00+02:00", coreAction3); //stay

        List<Action> coreActionsBefore = ActionFactory.listActionsForServer(user, coreServer);
        assertEquals(4, coreActionsBefore.size());


        /* update the calendar */
        Map<String, String> details = new HashMap<>();
        details.put("ical", FileUtils.readStringFromFile(icalExM2.getAbsolutePath()));

        List<RescheduleStrategy> rescheduleStrategy = new LinkedList<>();
        rescheduleStrategy.add(new CancelRescheduleStrategy());

        mm.updateCalendar(user, "multicalendar", details, rescheduleStrategy);

        /* check results */
        List<Action> sapActionsAfter = ActionFactory.listActionsForServer(user, sapServer);
        assertEquals(2, sapActionsAfter.size()); // First chain should be unchanged, second should be removed.
        assertContains(sapActionsAfter, sapAction1);
        assertContains(sapActionsAfter, sapAction2);

        List<Action> coreActionsAfter = ActionFactory.listActionsForServer(user, coreServer);
        assertContains(coreActionsAfter, coreAction3);
        assertContains(coreActionsAfter, coreAction4);
        assertEquals(2, coreActionsAfter.size()); // First chain should be canceled, second stay

    }

    /**
     * Create an Errata Action for the given server at a specific point in time
     *
     * @param type action type
     * @param server the server
     * @param datetime time template for earliest action. Example: "2020-04-21T09:00:00+01:00"
     * @param prerequisite dependend action
     * @return the Action
     * @throws Exception
     */
    private Action createActionForServerAt(ActionType type, Server server, String datetime,
            Action prerequisite) throws Exception {
        Action action = ActionFactoryTest.createAction(user, type);
        action.setPrerequisite(prerequisite);
        ZonedDateTime start = ZonedDateTime.parse(datetime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        action.setEarliestAction(Date.from(start.toInstant()));

        ServerAction serverAction = ServerActionTest.createServerAction(server, action);
        serverAction.setStatus(ActionFactory.STATUS_QUEUED);

        action.addServerAction(serverAction);
        ActionManager.storeAction(action);
        return ActionFactory.lookupById(action.getId());
    }

    /**
     * Create an Errata Action for the given server at a specific point in time
     *
     * @param server the server
     * @param datetime time template for earliest action. Example: "2020-04-21T09:00:00+01:00"
     * @return the Action
     * @throws Exception
     */
    private Action createActionForServerAt(ActionType type, Server server, String datetime) throws Exception {
        return createActionForServerAt(type, server, datetime, null);
    }

    private void assertExceptionThrown(Runnable body, Class exceptionClass) {
        try {
            body.run();
            fail("An exceptions should have been thrown.");
        }
        catch (Exception e) {
            assertEquals("The exception should be of class: " + exceptionClass, exceptionClass, e.getClass());
        }
    }
}
