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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.frontend.xmlrpc.recurringaction.test;

import static com.redhat.rhn.domain.recurringactions.type.RecurringActionType.ActionType.HIGHSTATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.org.Org;
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
import com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringHighstateHandler;
import com.redhat.rhn.manager.recurringactions.RecurringActionManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

/**
 * Test for {@link RecurringActionHandler}
 */
public class RecurringActionHandlerTest extends JMockBaseTestCaseWithUser {

    private RecurringHighstateHandler highstateHandler;
    private RecurringActionHandler actionHandler;
    private static final String TEST_CRON_EXPR = "0 * * * * ?";

    private TaskomaticApi taskomaticMock;

    {
        context().setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        highstateHandler = new RecurringHighstateHandler();
        actionHandler = new RecurringActionHandler();

        // mocking
        taskomaticMock = context().mock(TaskomaticApi.class);
        RecurringActionManager.setTaskomaticApi(taskomaticMock);
        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
            allowing(taskomaticMock).unscheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
        } });
    }

    @Test
    public void testCreateOrgAction() {
        int actionId = highstateHandler.create(user, Map.of(
                "entity_id", user.getOrg().getId().intValue(),
                "entity_type", "org",
                "name", "test-action",
                "cron_expr", TEST_CRON_EXPR
        ));
        RecurringAction action = RecurringActionFactory.lookupById(actionId).get();

        assertEquals(HIGHSTATE, action.getActionType());
        assertEquals(user, action.getCreator());
        assertEquals("test-action", action.getName());
        assertEquals(user.getOrg().getId(), action.getEntityId());
        assertEquals("0 * * * * ?", action.getCronExpr());
        assertEquals(RecurringAction.TargetType.ORG, action.getTargetType());
    }

    @Test
    public void testCreateMinionAction() throws Exception {
        int minionId = MinionServerFactoryTest.createTestMinionServer(user).getId().intValue();
        int actionId = highstateHandler.create(user, Map.of(
                "entity_id", minionId,
                "entity_type", "minion",
                "name", "test_action",
                "cron_expr", TEST_CRON_EXPR
        ));

        RecurringAction action = RecurringActionFactory.lookupById(actionId).get();

        assertEquals(HIGHSTATE, action.getActionType());
        assertEquals(user, action.getCreator());
        assertEquals("test_action", action.getName());
        assertEquals(minionId, action.getEntityId());
        assertEquals("0 * * * * ?", action.getCronExpr());
        assertEquals(RecurringAction.TargetType.MINION, action.getTargetType());
    }

    @Test
    public void testCreateActionInvalidData() {
        assertThrows(InvalidArgsException.class, () -> highstateHandler.create(user, Collections.emptyMap()));
    }

    @Test
    public void testCreateExistingAction() {
        Map<String, Object> actionProps = Map.of(
                "entity_id", user.getOrg().getId().intValue(),
                "entity_type", "org",
                "name", "test-action",
                "cron_expr", TEST_CRON_EXPR
        );
        highstateHandler.create(user, actionProps);
        ValidationException e = assertThrows(ValidationException.class,
                () -> highstateHandler.create(user, actionProps));
        assertEquals("Recurring Action with given name already exists", e.getMessage());
    }

    @Test
    public void testCreateActionWithNonExistingEntity() {
        assertThrows(EntityNotExistsFaultException.class, () -> highstateHandler.create(user, Map.of(
                "entity_id", -12345,
                "entity_type", "org",
                "name", "test-action",
                "cron_expr", TEST_CRON_EXPR
        )));
    }

    @Test
    public void testCreateNonAccessibleEntity() {
        Org org = OrgFactory.createOrg();
        org.setName("test org: " + TestUtils.randomString());
        org = OrgFactory.save(org);

        Org finalOrg = org;
        ValidationException e = assertThrows(ValidationException.class, () -> highstateHandler.create(user, Map.of(
                "entity_id", finalOrg.getId().intValue(),
                "entity_type", "ORG",
                "name", "test-action-123",
                "cron_expr", TEST_CRON_EXPR
        )));
        assertEquals("User has no permissions to do the action", e.getMessage());
    }

    @Test
    public void testCreateInvalidCronExpr() {
        ValidationException e = assertThrows(ValidationException.class, () -> highstateHandler.create(user, Map.of(
                "entity_id", user.getOrg().getId().intValue(),
                "entity_type", "org",
                "name", "test-action",
                "cron_expr", "This is invalid"
        )));

        assertEquals("Invalid Quartz expression provided.", e.getMessage());
    }

    @Test
    public void testUpdateAction() {
        int actionId = highstateHandler.create(user, Map.of(
                "entity_id", user.getOrg().getId().intValue(),
                "entity_type", "org",
                "name", "test-action",
                "cron_expr", TEST_CRON_EXPR
        ));

        Map<String, Object> updateProps = Map.of(
                "id", actionId,
                "name", "new-test-action",
                "active", false
        );
        highstateHandler.update(user, updateProps);

        RecurringAction action = RecurringActionFactory.lookupById(actionId).get();
        assertEquals("new-test-action", action.getName());
        assertFalse(action.isActive());
    }

    @Test
    public void testUpdateActionSetExistingName() {
        highstateHandler.create(user, Map.of(
                "entity_id", user.getOrg().getId().intValue(),
                "entity_type", "org",
                "name", "test-action",
                "cron_expr", TEST_CRON_EXPR
        ));

        int otherActionId = highstateHandler.create(user, Map.of(
                "entity_id", user.getOrg().getId().intValue(),
                "entity_type", "org",
                "name", "other-test-action",
                "cron_expr", TEST_CRON_EXPR
        ));

        ValidationException e = assertThrows(ValidationException.class, () -> highstateHandler.update(user, Map.of(
                "id", otherActionId,
                "name", "test-action"
        )));

        assertEquals("Recurring Action with given name already exists", e.getMessage());
    }

    @Test
    public void testUpdateActionSetInvalidCronExpr() {
        var actionId = highstateHandler.create(user, Map.of(
                "entity_id", user.getOrg().getId().intValue(),
                "entity_type", "org",
                "name", "test-action",
                "cron_expr", TEST_CRON_EXPR
        ));

        ValidationException e = assertThrows(ValidationException.class, () -> highstateHandler.update(user, Map.of(
                "id", actionId,
                "cron_expr", "THIS IS NOT CRON, THIS IS A STRING!"
        )));

        assertEquals("Invalid Quartz expression provided.", e.getMessage());
    }

    @Test
    public void testUpdateNonexistingAction() {
        assertThrows(EntityNotExistsFaultException.class, () -> highstateHandler.update(user, Map.of(
                "id", -123,
                "name", "new-name"
        )));
    }

    @Test
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

        assertThrows(com.redhat.rhn.frontend.xmlrpc.TaskomaticApiException.class,
                () -> highstateHandler.create(user, Map.of(
                        "entity_id", minionId.intValue(),
                        "entity_type", "minion",
                        "name", "test-action",
                        "cron_expr", TEST_CRON_EXPR
                )));
    }

    @Test
    public void testDeleteAction() {
        int actionId = highstateHandler.create(user, Map.of(
                "entity_id", user.getOrg().getId().intValue(),
                "entity_type", "org",
                "name", "test-action",
                "cron_expr", TEST_CRON_EXPR
        ));

        actionHandler.delete(user, actionId);
        assertTrue(RecurringActionFactory.lookupById(actionId).isEmpty());
    }

    @Test
    public void testDeleteNonexistingAction() {
        assertThrows(EntityNotExistsFaultException.class, () -> actionHandler.delete(user, -123));
    }
}
