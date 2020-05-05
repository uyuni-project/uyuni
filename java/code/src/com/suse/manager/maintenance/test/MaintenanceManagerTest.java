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
import com.redhat.rhn.domain.action.server.test.ServerActionTest;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.maintenance.MaintenanceManager;
import com.suse.manager.model.maintenance.MaintenanceCalendar;
import com.suse.manager.model.maintenance.MaintenanceSchedule;
import com.suse.manager.model.maintenance.MaintenanceSchedule.ScheduleType;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


public class MaintenanceManagerTest extends BaseTestCaseWithUser {

    private static final String TESTDATAPATH = "/com/suse/manager/maintenance/test/testdata";
    private static final String KDE_ICS = "maintenance-windows-kde.ics";
    private static final String EXCHANGE_ICS = "maintenance-windows-exchange.ics";

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

        mm.updateCalendar(user, "testcalendar", details);

        dbCal = mm.lookupCalendarByUserAndLabel(user, "testcalendar").orElseThrow(() -> new RuntimeException("Cannot find testcalendar"));
        assertEquals(user.getOrg(), dbCal.getOrg());
        assertEquals("testcalendar", dbCal.getLabel());
        assertEquals(FileUtils.readStringFromFile(icalEx.getAbsolutePath()), dbCal.getIcal());
        assertNotNull(dbCal.getUrlOpt().orElse(null));
        assertEquals("http://dummy.domain.top/exchange", dbCal.getUrlOpt().get());

        Map<String, String> sDetails = new HashMap<>();
        sDetails.put("type", "multi");

        mm.updateMaintenanceSchedule(user, "test server", sDetails);

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
