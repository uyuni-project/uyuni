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

import static com.redhat.rhn.domain.role.RoleFactory.ORG_ADMIN;
import static com.suse.manager.model.maintenance.MaintenanceSchedule.ScheduleType.SINGLE;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.maintenance.MaintenanceManager;
import com.suse.manager.maintenance.NotInMaintenanceModeException;
import com.suse.manager.model.maintenance.MaintenanceCalendar;
import com.suse.manager.model.maintenance.MaintenanceSchedule;
import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Tests
 */
public class MaintenanceManagerScheduleActionsTest extends JMockBaseTestCaseWithUser {

    private String calString;

    private TaskomaticApi taskomaticMock;

    private static final String TESTDATAPATH = "/com/suse/manager/maintenance/test/testdata";
    private static final String KDE_ICS = "maintenance-windows-kde.ics";

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);

        user.addPermanentRole(ORG_ADMIN);

        taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);
        ActionChainManager.setTaskomaticApi(taskomaticMock);
        ActionChainFactory.setTaskomaticApi(taskomaticMock);

        File ical = new File(TestUtils.findTestData(new File(TESTDATAPATH,  KDE_ICS).getAbsolutePath()).getPath());
        calString = FileUtils.readStringFromFile(ical.getAbsolutePath());
    }

    /**
     * Tests scheduling a state apply when a system is assigned to a schedule with maintenance windows
     * (= it's not in the maintenance mode)
     *
     */
    @Test
    public void testScheduleHighstateNoMaintWindow() throws Exception {
        // this tests assumes that APPLY STATES is a maintenance-mode-only action
        assertTrue(ActionFactory.TYPE_APPLY_STATES.isMaintenancemodeOnly());

        context().checking(new Expectations() {{
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
        }});

        MaintenanceManager mm = new MaintenanceManager();
        MaintenanceSchedule schedule = mm.createSchedule(user, "test-schedule-2", SINGLE, empty());

        MinionServer sys1 = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer sys2 = MinionServerFactoryTest.createTestMinionServer(user);

        assertTrue(mm.isSystemInMaintenanceMode(sys1));
        assertTrue(mm.isSystemInMaintenanceMode(sys2));

        mm.assignScheduleToSystems(user, schedule, Set.of(sys1.getId()), false);

        try {
            ActionChainManager.scheduleApplyStates(
                    user, List.of(sys1.getId(), sys2.getId()), empty(), new Date(12345), null);
            fail("NoMaintenanceWindowException should have been thrown.");
        }
        catch (NotInMaintenanceModeException e) {
            // this should happen
        }

        // no exception should happen here:
        ActionChainManager.scheduleApplyStates(user, List.of(sys2.getId()), empty(), new Date(12345), null);
    }

    /**
     * Tests scheduling a state apply outside maintenance window (= not in maintenance mode)
     */
    @Test
    public void testScheduleHighstateOutsideMaintWindow() throws Exception {
        // this tests assumes that APPLY STATES is a maintenance-mode-only action
        assertTrue(ActionFactory.TYPE_APPLY_STATES.isMaintenancemodeOnly());

        context().checking(new Expectations() {{
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
        }});

        MaintenanceManager mm = new MaintenanceManager();
        MaintenanceCalendar mc = mm.createCalendar(user, "testcalendar", calString);
        MaintenanceSchedule schedule = mm.createSchedule(user, "test-schedule-2", SINGLE, of(mc));

        MinionServer sys1 = MinionServerFactoryTest.createTestMinionServer(user);
        mm.assignScheduleToSystems(user, schedule, Set.of(sys1.getId()), false);
        assertTrue(mm.isSystemInMaintenanceMode(sys1));

        try {
            ActionChainManager.scheduleApplyStates(user, List.of(sys1.getId()), empty(), new Date(12345), null);
            fail("NoMaintenanceWindowException should have been thrown.");
        }
        catch (NotInMaintenanceModeException e) {
            // this should happen
        }

        ZonedDateTime start = ZonedDateTime.parse("2020-04-30T09:15:00+02:00", ISO_OFFSET_DATE_TIME);
        Date date = Date.from(start.toInstant());
        ActionChainManager.scheduleApplyStates(user, List.of(sys1.getId()), empty(), date, null);
    }

    /**
     * Tests scheduling a hardware refresh with no maintenance window (= system not in maintenance mode)
     */
    @Test
    public void testScheduleHwRefreshNoMaintWindow() throws Exception {
        // this tests assumes that HW refresh is not a maintenance-mode-only action
        assertFalse(ActionFactory.TYPE_HARDWARE_REFRESH_LIST.isMaintenancemodeOnly());

        MaintenanceManager mm = new MaintenanceManager();
        MaintenanceSchedule schedule = mm.createSchedule(user, "test-schedule-3", SINGLE, empty());

        Server sys1 = MinionServerFactoryTest.createTestMinionServer(user);

        mm.assignScheduleToSystems(user, schedule, Set.of(sys1.getId()), false);

        try {
            ActionManager.scheduleHardwareRefreshAction(user, sys1, new Date(12345));
        }
        catch (NotInMaintenanceModeException e) {
            fail("NoMaintenanceWindowException should NOT have been thrown.");
        }
    }

    /**
     * Tests scheduling a just a channel state (part of channel change which should be allowed)
     */
    @Test
    public void testScheduleChannelChangeNoMaintWindow() throws Exception {
        MaintenanceManager mm = new MaintenanceManager();
        MaintenanceSchedule schedule = mm.createSchedule(user, "test-schedule-3", SINGLE, empty());

        Server sys1 = MinionServerFactoryTest.createTestMinionServer(user);

        mm.assignScheduleToSystems(user, schedule, Set.of(sys1.getId()), false);

        try {
            ActionManager.scheduleApplyStates(user, Collections.singletonList(sys1.getId()),
                    Collections.singletonList(ApplyStatesEventMessage.CHANNELS), new Date(12345));
        }
        catch (NotInMaintenanceModeException e) {
            fail("NoMaintenanceWindowException should NOT have been thrown.");
        }
    }
}
