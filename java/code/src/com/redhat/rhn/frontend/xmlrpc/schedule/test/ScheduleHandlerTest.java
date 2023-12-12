/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.schedule.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.server.test.ServerActionTest;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.frontend.dto.ActionedSystem;
import com.redhat.rhn.frontend.dto.ScheduledAction;
import com.redhat.rhn.frontend.xmlrpc.UnsupportedOperationException;
import com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.manager.action.ActionManager;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class ScheduleHandlerTest extends BaseHandlerTestCase {

    private ScheduleHandler handler = new ScheduleHandler();

    @Test
    public void testCancelActions() throws Exception {

        // setup

        //obtain number of actions from action manager
        DataResult<ScheduledAction> actions = ActionManager.allActions(admin, null);
        int numActions = actions.size();

        //compare against number retrieved from api... should be the same
        Object[] apiActions = handler.listAllActions(admin);
        assertEquals(numActions, apiActions.length);

        //add new actions and verify that the value returned by the api
        //has increased correctly
        Server server = ServerFactoryTest.createTestServer(admin, true);

        Action a1 = ActionFactoryTest.createAction(admin,
                ActionFactory.TYPE_PACKAGES_UPDATE);
        ServerAction saction1 = ServerActionTest.createServerAction(server, a1);
        saction1.setStatus(ActionFactory.STATUS_COMPLETED);

        Action a2 = ActionFactoryTest.createAction(admin,
                ActionFactory.TYPE_PACKAGES_UPDATE);
        ServerAction saction2 = ServerActionTest.createServerAction(server, a2);
        saction2.setStatus(ActionFactory.STATUS_QUEUED);

        Action a3 = ActionFactoryTest.createAction(admin,
                ActionFactory.TYPE_PACKAGES_UPDATE);
        ServerAction saction3 = ServerActionTest.createServerAction(server, a3);
        saction3.setStatus(ActionFactory.STATUS_FAILED);

        apiActions = handler.listAllActions(admin);
        assertEquals(numActions + 3, apiActions.length);

        // execute
        List<Integer> actionIds = new ArrayList<>();
        actionIds.add(a1.getId().intValue());
        actionIds.add(a2.getId().intValue());
        actionIds.add(a3.getId().intValue());
        int result = handler.cancelActions(admin, actionIds);

        // verify
        assertEquals(1, result);
        apiActions = handler.listAllActions(admin);
        assertEquals(numActions + 2, apiActions.length);
    }

    @Test
    public void testListAllActions() throws Exception {

        // setup
        //obtain number of actions from action manager
        DataResult<ScheduledAction> actions = ActionManager.allActions(admin, null);
        int numActions = actions.size();

        //compare against number retrieved from api... should be the same
        Object[] apiActions = handler.listAllActions(admin);
        assertEquals(numActions, apiActions.length);

        //add new actions and verify that the value returned by the api
        //has increased correctly
        Server server = ServerFactoryTest.createTestServer(admin, true);

        Action a1 = ActionFactoryTest.createAction(admin,
                ActionFactory.TYPE_PACKAGES_UPDATE);
        ServerAction saction1 = ServerActionTest.createServerAction(server, a1);
        saction1.setStatus(ActionFactory.STATUS_COMPLETED);

        Action a2 = ActionFactoryTest.createAction(admin,
                ActionFactory.TYPE_PACKAGES_UPDATE);
        ServerAction saction2 = ServerActionTest.createServerAction(server, a2);
        saction2.setStatus(ActionFactory.STATUS_QUEUED);

        Action a3 = ActionFactoryTest.createAction(admin,
                ActionFactory.TYPE_PACKAGES_UPDATE);
        ServerAction saction3 = ServerActionTest.createServerAction(server, a3);
        saction3.setStatus(ActionFactory.STATUS_FAILED);

        // execute
        apiActions = handler.listAllActions(admin);

        // verify
        assertEquals(numActions + 3, apiActions.length);
    }

    @Test
    public void testListCompletedActions() throws Exception {

        //obtain number of actions from action manager
        DataResult<ScheduledAction> actions = ActionManager.completedActions(admin, null);
        int numActions = actions.size();

        //compare against number retrieved from api... should be the same
        Object[] apiActions = handler.listCompletedActions(admin);

        assertEquals(numActions, apiActions.length);

        //add a new action and verify that the value returned by the api
        //has increased
        Server server = ServerFactoryTest.createTestServer(admin, true);
        Action a = ActionFactoryTest.createAction(admin,
                ActionFactory.TYPE_PACKAGES_UPDATE);
        ServerAction saction = ServerActionTest.createServerAction(server, a);
        saction.setStatus(ActionFactory.STATUS_COMPLETED);

        apiActions = handler.listCompletedActions(admin);

        assertTrue(apiActions.length > numActions);
    }

    @Test
    public void testListInProgressActions() throws Exception {
        //obtain number of actions from action manager
        DataResult<ScheduledAction> actions = ActionManager.pendingActions(admin, null);
        int numActions = actions.size();

        //compare against number retrieved from api... should be the same
        Object[] apiActions = handler.listInProgressActions(admin);

        assertEquals(numActions, apiActions.length);

        //add a new action and verify that the value returned by the api
        //has increased
        Server server = ServerFactoryTest.createTestServer(admin, true);
        Action a = ActionFactoryTest.createAction(admin,
                ActionFactory.TYPE_PACKAGES_UPDATE);
        ServerAction saction = ServerActionTest.createServerAction(server, a);
        saction.setStatus(ActionFactory.STATUS_QUEUED);

        apiActions = handler.listInProgressActions(admin);

        assertTrue(apiActions.length > numActions);
    }

    @Test
    public void testListFailedActions() throws Exception {
        //obtain number of actions from action manager
        DataResult<ScheduledAction> actions = ActionManager.failedActions(admin, null);
        int numActions = actions.size();

        //compare against number retrieved from api... should be the same
        Object[] apiActions = handler.listFailedActions(admin);

        assertEquals(numActions, apiActions.length);

        //add a new action and verify that the value returned by the api
        //has increased
        Server server = ServerFactoryTest.createTestServer(admin, true);
        Action a = ActionFactoryTest.createAction(admin,
                ActionFactory.TYPE_PACKAGES_UPDATE);
        ServerAction saction = ServerActionTest.createServerAction(server, a);
        saction.setStatus(ActionFactory.STATUS_FAILED);

        apiActions = handler.listFailedActions(admin);

        assertTrue(apiActions.length > numActions);
    }

    @Test
    public void testListArchivedActions() throws Exception {
        //obtain number of actions from action manager
        DataResult<ScheduledAction> actions = ActionManager.archivedActions(admin, null);
        int numActions = actions.size();

        //compare against number retrieved from api... should be the same
        Object[] apiActions = handler.listArchivedActions(admin);

        assertEquals(numActions, apiActions.length);

        //add a new action and verify that the value returned by the api
        //has increased
        Server server = ServerFactoryTest.createTestServer(admin, true);
        Action a = ActionFactoryTest.createAction(admin,
                ActionFactory.TYPE_PACKAGES_UPDATE);
        a.setArchived(1L);
        ServerAction saction = ServerActionTest.createServerAction(server, a);
        saction.setStatus(ActionFactory.STATUS_QUEUED);

        apiActions = handler.listArchivedActions(admin);

        assertTrue(apiActions.length > numActions);
    }

    @Test
    public void testListAllArchivedActions() throws Exception {
        //obtain number of actions from action manager
        DataResult<ScheduledAction> actions = ActionManager.allArchivedActions(admin, null);
        int numActions = actions.size();

        //compare against number retrieved from api... should be the same
        Object[] apiActions = handler.listAllArchivedActions(admin);
        assertEquals(numActions, apiActions.length);

        //add a new action and verify that the value returned by the api
        //has increased
        Server server = ServerFactoryTest.createTestServer(admin, true);
        Action a = ActionFactoryTest.createAction(admin,
                ActionFactory.TYPE_PACKAGES_UPDATE);
        a.setArchived(1L);
        ServerAction saction = ServerActionTest.createServerAction(server, a);
        saction.setStatus(ActionFactory.STATUS_QUEUED);

        Action a2 = ActionFactoryTest.createAction(admin,
                ActionFactory.TYPE_PACKAGES_UPDATE);
        a2.setArchived(1L);
        ServerAction saction2 = ServerActionTest.createServerAction(server, a2);
        saction2.setStatus(ActionFactory.STATUS_QUEUED);

        apiActions = handler.listAllArchivedActions(admin);
        assertTrue(apiActions.length > numActions);

        int oldLimit = ConfigDefaults.get().getActionsDisplayLimit();
        Config.get().setString(ConfigDefaults.ACTIONS_DISPLAY_LIMIT, "1");
        Object[] apiActionsLimitted = handler.listArchivedActions(admin);
        Config.get().setString(ConfigDefaults.ACTIONS_DISPLAY_LIMIT, String.valueOf(oldLimit));

        assertEquals(apiActionsLimitted.length, 1);
        assertTrue(apiActions.length > apiActionsLimitted.length);
    }

    @Test
    public void testListAllCompletedActions() throws Exception {
        //obtain number of actions from action manager
        DataResult<ScheduledAction> actions = ActionManager.allCompletedActions(admin, null);
        int numActions = actions.size();

        //compare against number retrieved from api... should be the same
        Object[] apiActions = handler.listAllCompletedActions(admin);
        assertEquals(numActions, apiActions.length);

        //add a new action and verify that the value returned by the api
        //has increased
        Server server = ServerFactoryTest.createTestServer(admin, true);
        Action a = ActionFactoryTest.createAction(admin,
                ActionFactory.TYPE_PACKAGES_UPDATE);
        ServerAction saction = ServerActionTest.createServerAction(server, a);
        saction.setStatus(ActionFactory.STATUS_COMPLETED);

        Action a2 = ActionFactoryTest.createAction(admin,
                ActionFactory.TYPE_PACKAGES_UPDATE);
        ServerAction saction2 = ServerActionTest.createServerAction(server, a2);
        saction2.setStatus(ActionFactory.STATUS_COMPLETED);

        apiActions = handler.listAllCompletedActions(admin);
        assertTrue(apiActions.length > numActions);

        int oldLimit = ConfigDefaults.get().getActionsDisplayLimit();
        Config.get().setString(ConfigDefaults.ACTIONS_DISPLAY_LIMIT, "1");
        Object[] apiActionsLimitted = handler.listCompletedActions(admin);
        Config.get().setString(ConfigDefaults.ACTIONS_DISPLAY_LIMIT, String.valueOf(oldLimit));

        assertEquals(apiActionsLimitted.length, 1);
        assertTrue(apiActions.length > apiActionsLimitted.length);
    }

    @Test
    public void testListCompletedSystems() throws Exception {
        //create a new action
        Server server = ServerFactoryTest.createTestServer(admin, true);
        Action action = ActionFactoryTest.createAction(admin,
                ActionFactory.TYPE_PACKAGES_UPDATE);
        ServerAction saction = ServerActionTest.createServerAction(server, action);
        saction.setStatus(ActionFactory.STATUS_COMPLETED);

        //obtain number of systems from action manager
        DataResult<ActionedSystem> systems = ActionManager.completedSystems(admin, action, null);
        int numSystems = systems.size();

        //compare against number retrieved from api... should be the same
        Object[] apiSystems = handler.listCompletedSystems(admin,
            action.getId().intValue());

        assertTrue(apiSystems.length > 0);
        assertEquals(numSystems, apiSystems.length);
    }

    @Test
    public void testListInProgressSystems() throws Exception {
        //create a new action
        Server server = ServerFactoryTest.createTestServer(admin, true);
        Action action = ActionFactoryTest.createAction(admin,
                ActionFactory.TYPE_PACKAGES_UPDATE);
        ServerAction saction = ServerActionTest.createServerAction(server, action);
        saction.setStatus(ActionFactory.STATUS_QUEUED);

        //obtain number of systems from action manager
        DataResult<ActionedSystem> systems = ActionManager.inProgressSystems(admin, action, null);
        int numSystems = systems.size();

        //compare against number retrieved from api... should be the same
        Object[] apiSystems = handler.listInProgressSystems(admin,
            action.getId().intValue());

        assertTrue(apiSystems.length > 0);
        assertEquals(numSystems, apiSystems.length);
    }

    @Test
    public void testListFailedSystems() throws Exception {
        //create a new action
        Server server = ServerFactoryTest.createTestServer(admin, true);
        Action action = ActionFactoryTest.createAction(admin,
                ActionFactory.TYPE_PACKAGES_UPDATE);
        ServerAction saction = ServerActionTest.createServerAction(server, action);
        saction.setStatus(ActionFactory.STATUS_FAILED);

        //obtain number of systems from action manager
        DataResult<ActionedSystem> systems = ActionManager.failedSystems(admin, action, null);
        int numSystems = systems.size();

        //compare against number retrieved from api... should be the same
        Object[] apiSystems = handler.listFailedSystems(admin,
            action.getId().intValue());

        assertTrue(apiSystems.length > 0);
        assertEquals(numSystems, apiSystems.length);
    }

    @Test
    public void testCannotCancelPendingActionsWithPrerequisite() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);

        Action parent = ActionFactoryTest.createEmptyAction(admin, ActionFactory.TYPE_PACKAGES_UPDATE);
        ServerActionTest.createServerAction(server, parent);

        Action child = ActionFactoryTest.createEmptyAction(admin, ActionFactory.TYPE_SCRIPT_RUN);
        child.setPrerequisite(parent);
        ActionFactory.save(child);
        ServerActionTest.createServerAction(server, child);

        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
            () -> handler.cancelActions(admin, List.of(child.getId().intValue())));
        String expected = String.format("Cannot cancel an action with a pending prerequisite.%n" +
                "To cancel the whole chain, please cancel Action %d%n", parent.getId());
        assertEquals(expected, exception.getMessage());
    }

    @Test
    public void testCannotCancelPickedUpAction() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);

        Action action = ActionFactoryTest.createEmptyAction(admin, ActionFactory.TYPE_PACKAGES_UPDATE);
        ServerAction serverAction = ServerActionTest.createServerAction(server, action);
        serverAction.setStatus(ActionFactory.STATUS_PICKED_UP);

        ActionFactory.save(action);
        ActionFactory.save(serverAction);

        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
            () -> handler.cancelActions(admin, List.of(action.getId().intValue())));
        assertEquals("Cannot cancel an action in PICKED UP state.", exception.getMessage());
    }

}
