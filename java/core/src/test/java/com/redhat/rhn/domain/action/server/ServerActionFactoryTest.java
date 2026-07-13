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
package com.redhat.rhn.domain.action.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.util.TimeUtilsTest;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionFactoryTest;
import com.redhat.rhn.domain.action.ActionTypeEnum;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.BaseTestCase;
import com.redhat.rhn.testing.SaltTestCaseExtension;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserForTest;
import com.redhat.rhn.testing.UserForTestCaseExtension;

import com.suse.manager.maintenance.MaintenanceTestUtils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

@ExtendWith(UserForTestCaseExtension.class)
@ExtendWith(SaltTestCaseExtension.class)
public class ServerActionFactoryTest extends BaseTestCase {

    @UserForTest(useClassNameForOrg = true)
    protected User user;

    private Server testServer;
    private Server anotherTestServer;
    private final String testActionTypeName = "Patch Update";
    private Date createdBeforeTime;
    private Date createdLaterTime;

    private void createListServerActionsForServerTest() throws Exception {
        testServer = ServerFactoryTest.createTestServer(user);
        MaintenanceTestUtils.createActionForServerAt(user, ActionTypeEnum.TYPE_ERRATA, testServer,
                "2026-03-10T08:00:00.000Z");
        createdBeforeTime = new Date(System.currentTimeMillis() - 10_000);
        createdLaterTime = new Date(System.currentTimeMillis() + 10_000);

        anotherTestServer = ServerFactoryTest.createTestServer(user);
        MaintenanceTestUtils.createActionForServerAt(user, ActionTypeEnum.TYPE_ERRATA, anotherTestServer,
                "2026-03-10T08:00:00.000Z");
    }


    @Test
    @DisplayName("listServerActionsForServer is behaving correctly")
    void testListServerActionsForServer() throws Exception {
        createListServerActionsForServerTest();

        List<ServerAction> actionsForServer = ServerActionFactory.listServerActionsForServer(testServer);
        assertEquals(1, actionsForServer.size());
    }

    @Test
    @DisplayName("listServerActionsForServer filtered by action name and created date is behaving correctly")
    void testListServerActionsForServerWithActionNameAndCreatedDate() throws Exception {
        createListServerActionsForServerTest();

        List<ServerAction> actionsForServer =
                ServerActionFactory.listServerActionsForServer(testServer, testActionTypeName, createdBeforeTime);
        assertEquals(1, actionsForServer.size());
        actionsForServer =
                ServerActionFactory.listServerActionsForServer(testServer, "another type name", createdBeforeTime);
        assertEquals(0, actionsForServer.size());
        actionsForServer =
                ServerActionFactory.listServerActionsForServer(testServer, testActionTypeName, createdLaterTime);
        assertEquals(0, actionsForServer.size());
    }

    @Test
    @DisplayName("listServerActionsForServer filtered by action type is behaving correctly")
    void testListServerActionsForServerWithActionType() throws Exception {
        createListServerActionsForServerTest();

        List<ServerAction> actionsForServer =
                ServerActionFactory.listServerActionsForServer(testServer, ActionTypeEnum.TYPE_ERRATA);
        assertEquals(1, actionsForServer.size());
        actionsForServer = ServerActionFactory.listServerActionsForServer(testServer, ActionTypeEnum.TYPE_REBOOT);
        assertEquals(0, actionsForServer.size());
    }

    @Test
    @DisplayName("listServerActionsForServer filtered by action status list is behaving correctly")
    void testListServerActionsForServerWithActionStatus() throws Exception {
        createListServerActionsForServerTest();

        List<ServerAction> actionsForServer =
                ServerActionFactory.listServerActionsForServer(testServer, List.of(ActionFactory.STATUS_QUEUED));
        assertEquals(1, actionsForServer.size());
        actionsForServer =
                ServerActionFactory.listServerActionsForServer(testServer, ActionFactory.ALL_PENDING_STATUSES);
        assertEquals(1, actionsForServer.size());
        actionsForServer =
                ServerActionFactory.listServerActionsForServer(testServer, List.of(ActionFactory.STATUS_PICKED_UP));
        assertEquals(0, actionsForServer.size());
    }

    @Test
    @DisplayName("listServerActionsForServer filtered by action status and created date is behaving correctly")
    void testListServerActionsForServerWithActionStatusAndCreatedDate() throws Exception {
        createListServerActionsForServerTest();

        List<ServerAction> actionsForServer =
                ServerActionFactory.listServerActionsForServer(testServer, ActionFactory.ALL_STATUSES,
                        createdBeforeTime);
        assertEquals(1, actionsForServer.size());
        actionsForServer =
                ServerActionFactory.listServerActionsForServer(testServer, List.of(ActionFactory.STATUS_PICKED_UP),
                        createdBeforeTime);
        assertEquals(0, actionsForServer.size());
        actionsForServer =
                ServerActionFactory.listServerActionsForServer(testServer, ActionFactory.ALL_STATUSES,
                        createdLaterTime);
        assertEquals(0, actionsForServer.size());
    }

    private static ServerAction addServerAction(User user, Action newA, Consumer<ServerAction> statusSetter) {
        Server newS = ServerFactoryTest.createTestServer(user, true);
        return ServerActionTest.createServerAction(newS, newA, statusSetter);
    }

    @Test
    @DisplayName("rescheduleFailedServerActions is behaving correctly")
    public void testRescheduleFailedServerActions() {
        Instant testStartInstant = ZonedDateTime.now().toInstant();
        Instant originalInstant = testStartInstant.minus(1, ChronoUnit.DAYS);

        Action a1 = ActionFactoryTest.createEmptyAction(user, ActionTypeEnum.TYPE_REBOOT);
        a1.setEarliestAction(Date.from(originalInstant));

        ServerAction sa1 = addServerAction(user, a1, ServerAction::setStatusFailed);
        ServerAction sa2 = addServerAction(user, a1, ServerAction::setStatusCompleted);

        ActionFactory.save(a1);

        ServerActionFactory.rescheduleFailedServerActions(a1, 5L);
        sa1 = TestUtils.reload(sa1);

        assertTrue(sa1.isStatusQueued());
        assertEquals(5L, sa1.getRemainingTries());

        assertTrue(sa2.isStatusCompleted());

        Instant newEarliestInstant = a1.getEarliestAction().toInstant();
        assertTrue(originalInstant.isBefore(newEarliestInstant));
        assertFalse(testStartInstant.isAfter(newEarliestInstant));
    }


    @Test
    @DisplayName("rescheduleAllServerActions is behaving correctly")
    public void testRescheduleAllServerActions() {
        Instant testStartInstant = ZonedDateTime.now().toInstant();
        Instant originalInstant = testStartInstant.minus(1, ChronoUnit.DAYS);

        Action a1 = ActionFactoryTest.createEmptyAction(user, ActionTypeEnum.TYPE_REBOOT);
        a1.setEarliestAction(Date.from(originalInstant));

        ServerAction sa1 = addServerAction(user, a1, ServerAction::setStatusFailed);
        ServerAction sa2 = addServerAction(user, a1, ServerAction::setStatusCompleted);

        ActionFactory.save(a1);

        ServerActionFactory.rescheduleAllServerActions(a1, 5L);

        sa1 = TestUtils.reload(sa1);
        sa2 = TestUtils.reload(sa2);

        assertTrue(sa1.isStatusQueued());
        assertTrue(sa1.getRemainingTries() > 0);

        assertTrue(sa2.isStatusQueued());
        assertTrue(sa2.getRemainingTries() > 0);
    }

    @Test
    @DisplayName("rescheduleSingleServerAction is behaving correctly")
    public void rescheduleSingleActionUpdatesEarliestDate() throws Exception {
        Instant testStartInstant = ZonedDateTime.now().toInstant();
        Instant originalInstant = testStartInstant.minus(1, ChronoUnit.DAYS);

        Action a1 = ActionFactoryTest.createAction(user, ActionTypeEnum.TYPE_REBOOT);
        a1.setEarliestAction(Date.from(originalInstant));
        ServerAction sa = (ServerAction) a1.getServerActions().toArray()[0];

        sa.setStatusFailed();
        sa.setRemainingTries(0L);
        ActionFactory.save(a1);

        ServerActionFactory.rescheduleSingleServerAction(a1, 5L, sa.getServerId());

        a1 = TestUtils.reload(a1);
        sa = TestUtils.reload(sa);

        assertTrue(sa.isStatusQueued());
        assertTrue(sa.getRemainingTries() > 0);

        Instant newEarliestInstant = a1.getEarliestAction().toInstant();
        assertTrue(originalInstant.isBefore(newEarliestInstant));
        assertFalse(testStartInstant.isAfter(newEarliestInstant));
    }


    @Test
    @DisplayName("updateServerActions correctly not picking up if already in failed state")
    public void testUpdateServerActionsPickedUp() {
        Action action = ActionFactoryTest.createEmptyAction(user, ActionTypeEnum.TYPE_REBOOT);
        ServerAction serverAction = addServerAction(user, action, ServerAction::setStatusFailed);

        ActionFactory.save(action);
        TestUtils.flushSession();
        TestUtils.evict(serverAction);
        TestUtils.evict(action);

        // Should NOT update if already in final state.
        ServerActionFactory.updateServerActions(action, List.of(serverAction.getServerId()),
                ActionFactory.STATUS_PICKED_UP);
        serverAction = TestUtils.reload(serverAction);
        assertTrue(serverAction.isStatusFailed());
    }


    @Test
    @DisplayName("updateServerActions correctly updating to completed")
    public void testUpdateServerActions2() {
        Action action = ActionFactoryTest.createEmptyAction(user, ActionTypeEnum.TYPE_REBOOT);
        ServerAction serverAction = addServerAction(user, action, ServerAction::setStatusQueued);

        ActionFactory.save(action);
        TestUtils.flushSession();
        TestUtils.evict(serverAction);
        TestUtils.evict(action);

        //Should update to STATUS_COMPLETED
        ServerActionFactory.updateServerActions(action, List.of(serverAction.getServerId()),
                ActionFactory.STATUS_COMPLETED);
        serverAction = TestUtils.reload(serverAction);
        assertTrue(serverAction.isStatusCompleted());
    }


    @Test
    @DisplayName("updateServerActions correctly picking up and setting pickup date")
    public void testUpdateServerActions3() {
        Action action = ActionFactoryTest.createEmptyAction(user, ActionTypeEnum.TYPE_REBOOT);
        ServerAction serverAction = addServerAction(user, action, ServerAction::setStatusQueued);

        ActionFactory.save(action);
        assertTrue(serverAction.isStatusQueued());
        assertNull(serverAction.getPickupTime());
        TestUtils.flushSession();
        TestUtils.evict(serverAction);
        TestUtils.evict(action);

        Date almostNow = new Date(System.currentTimeMillis() - 20_000);
        ServerActionFactory.updateServerActions(action, List.of(serverAction.getServerId()),
                ActionFactory.STATUS_PICKED_UP);
        serverAction = TestUtils.reload(serverAction);
        assertTrue(serverAction.isStatusPickedUp());
        assertTrue(serverAction.getPickupTime().after(almostNow));
    }


    @Test
    @DisplayName("addServerToAction correctly creating and adding an ServerAction ")
    public void testAddServerToAction()  {
        Server s = ServerFactoryTest.createTestServer(user);
        Action a = ActionFactoryTest.createEmptyAction(user, ActionTypeEnum.TYPE_REBOOT);

        ServerActionFactory.addServerToAction(s.getId(), a);

        assertNotNull(a.getServerActions());
        assertEquals(1, a.getServerActions().size());
        Object[] array = a.getServerActions().toArray();
        ServerAction sa = (ServerAction)array[0];
        assertTrue(TimeUtilsTest.timeEquals(sa.getCreated().getTime(), sa.getModified().getTime()));
        assertTrue(sa.isStatusQueued());
        assertEquals(sa.getServer(), s);
    }
}
