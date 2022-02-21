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

package com.redhat.rhn.frontend.xmlrpc.recurringaction.test;

import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.recurringactions.RecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringActionFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.EntityNotExistsFaultException;
import com.redhat.rhn.frontend.xmlrpc.InvalidArgsException;
import com.redhat.rhn.frontend.xmlrpc.ValidationException;
import com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringActionHandler;
import com.redhat.rhn.manager.recurringactions.RecurringActionManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;

import java.util.HashMap;
import java.util.Map;


/**
 * Test for {@link RecurringActionHandler}
 */
public class RecurringActionHandlerTest extends JMockBaseTestCaseWithUser {

    private RecurringActionHandler handler;

    // common props for creating action
    private Map<String, Object> testActionProps;

    private TaskomaticApi taskomaticMock;

    {
        context().setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        handler = new RecurringActionHandler();
        testActionProps = Map.of(
                "entity_id", user.getOrg().getId().intValue(),
                "entity_type", "ORG",
                "name", "test-action-123",
                "cron_expr", "0 * * * * ?"
        );

        // mocking
        taskomaticMock = context().mock(TaskomaticApi.class);
        RecurringActionManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
            allowing(taskomaticMock).unscheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
        } });
    }

    public void testCreateAndLookupOrgAction() {
        var actionId = handler.create(user, testActionProps);

        var createdAction = handler.lookupById(user, actionId);
        var dbAction = RecurringActionFactory.lookupById(actionId).get();

        assertEquals(dbAction, createdAction);
        assertEquals(RecurringAction.Type.ORG, createdAction.getType());
    }

    public void testCreateAndLookupMinionAction() throws Exception {
        var minionId = MinionServerFactoryTest.createTestMinionServer(user).getId().intValue();
        var props = new HashMap<>(testActionProps);
        props.put("entity_id", minionId);
        props.put("entity_type", "MINION");
        var actionId = handler.create(user, props);

        var createdAction = handler.lookupById(user, actionId);
        var dbAction = RecurringActionFactory.lookupById(actionId).get();

        assertEquals(dbAction, createdAction);
        assertEquals(RecurringAction.Type.MINION, createdAction.getType());
    }

    public void testCreateActionInvalidData() {
        try {
            handler.create(user, Map.of());
            fail("An exception should have been thrown");
        }
        catch (InvalidArgsException e) {
            // no-op
        }
    }

    public void testCreateExistingAction() {
        handler.create(user, testActionProps);
        try {
            handler.create(user, testActionProps);
            fail("An exception should have been thrown");
        }
        catch (ValidationException e) {
            // no-op
        }
    }

    public void testCreateActionWithNonExistingEntity() {
        var props = new HashMap<>(testActionProps);
        props.put("entity_id", -12345);
        try {
            handler.create(user, props);
            fail("An exception should have been thrown");
        }
        catch (EntityNotExistsFaultException e) {
            // no-op
        }
    }

    public void testCreateNonAccessibleEntity() {
        var org = OrgFactory.createOrg();
        org.setName("test org: " + TestUtils.randomString());
        org = OrgFactory.save(org);

        var props = Map.<String, Object>of(
                "entity_id", org.getId().intValue(),
                "entity_type", "ORG",
                "name", "test-action-123",
                "cron_expr", "0 * * * * ?"
        );

        try {
            handler.create(user, props);
            fail("An exception should have been thrown");
        }
        catch (ValidationException e) {
            // no-op
        }
    }

    public void testUpdateAction() {
        int actionId = handler.create(user, testActionProps);

        var updateProps = Map.<String, Object>of(
                "id", actionId,
                "name", "new-test-action-123",
                "active", false
        );
        handler.update(user, updateProps);

        var updatedAction = handler.lookupById(user, actionId);
        assertEquals("new-test-action-123", updatedAction.getName());
        assertFalse(updatedAction.isActive());
    }

    public void testUpdateActionSetExistingName() {
        handler.create(user, testActionProps);
        var otherActionProps = new HashMap<>(testActionProps);
        otherActionProps.put("name", "new-test-action-123");
        int otherAction = handler.create(user, otherActionProps);

        var updateProps = Map.<String, Object>of(
                "id", otherAction,
                "name", "test-action-123" // try to set the name to the name of the first action
        );
        try {
            handler.update(user, updateProps);
            fail("An exception should have been thrown");
        }
        catch (ValidationException e) {
            // no-op
        }
    }

    public void testUpdateActionSetInvalidCronExpr() {
        var actionId = handler.create(user, testActionProps);

        var updateProps = Map.<String, Object>of(
                "id", actionId,
                "cron_expr", "THIS IS NOT CRON, THIS IS A STRING!"
        );

        try {
            handler.update(user, updateProps);
            fail("An exception should have been thrown");
        }
        catch (ValidationException e) {
            // no-op
        }
    }

    public void testUpdateNonexistingAction() {
        var updateProps = Map.<String, Object>of(
                "id", -123456,
                "name", "test-name"
        );
        try {
            handler.update(user, updateProps);
            fail("An exception should have been thrown");
        }
        catch (EntityNotExistsFaultException e) {
            // no-op
        }
    }

    public void testCreateActionTaskomaticDown() throws Exception {
        // mock taskomatic down
        TaskomaticApi taskomaticMock2 = context().mock(TaskomaticApi.class, "taskomaticApi2");
        context().checking(new Expectations() { {
            allowing(taskomaticMock2).scheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
            will(throwException(new TaskomaticApiException(new RuntimeException())));
            allowing(taskomaticMock2).unscheduleRecurringAction(
                    with(any(RecurringAction.class)), with(any(User.class)));
            will(throwException(new TaskomaticApiException(new RuntimeException())));
        } });
        RecurringActionManager.setTaskomaticApi(taskomaticMock2);

        // create a minion with no recurring actions
        var minionId = MinionServerFactoryTest.createTestMinionServer(user).getId();
        var props = new HashMap<>(testActionProps);
        props.put("entity_id", minionId.intValue());
        props.put("entity_type", "MINION");

        try {
            handler.create(user, props);
            fail("An exception should have been thrown");
        }
        catch (com.redhat.rhn.frontend.xmlrpc.TaskomaticApiException e) {
            // no-op
        }
    }

    public void testDeleteAction() {
        int actionId = handler.create(user, testActionProps);

        handler.delete(user, actionId);

        assertTrue(RecurringActionFactory.lookupById(actionId).isEmpty());
    }

    public void testDeleteNonexistingAction() {
        try {
            handler.delete(user, -12345);
            fail("An exception should have been thrown");
        }
        catch (EntityNotExistsFaultException e) {
            // no-op
        }
    }
}
