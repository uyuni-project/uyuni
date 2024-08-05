/*
 * Copyright (c) 2020--2021 SUSE LLC
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
package com.redhat.rhn.manager.recurringactions.test;

import static com.redhat.rhn.domain.recurringactions.RecurringAction.TargetType.GROUP;
import static com.redhat.rhn.domain.recurringactions.RecurringAction.TargetType.MINION;
import static com.redhat.rhn.domain.recurringactions.RecurringAction.TargetType.ORG;
import static com.redhat.rhn.domain.recurringactions.type.RecurringActionType.ActionType.HIGHSTATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.recurringactions.GroupRecurringAction;
import com.redhat.rhn.domain.recurringactions.MinionRecurringAction;
import com.redhat.rhn.domain.recurringactions.OrgRecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringActionFactory;
import com.redhat.rhn.domain.recurringactions.type.RecurringActionType;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.manager.EntityNotExistsException;
import com.redhat.rhn.manager.recurringactions.RecurringActionManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerGroupTestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.webui.services.test.TestSaltApi;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.junit5.JUnit5Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;

/**
 * Tests for {@link RecurringActionManager}
 */
public class RecurringActionManagerTest extends BaseTestCaseWithUser {

    @RegisterExtension
    public static final Mockery CONTEXT = new JUnit5Mockery() {{
        setThreadingPolicy(new Synchroniser());
    }};

    private Org anotherOrg;
    private User anotherUser;
    private static TaskomaticApi taskomaticMock;
    private static final String CRON_EXPR = "0 * * * * ?";

    static {
        CONTEXT.setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        taskomaticMock = CONTEXT.mock(TaskomaticApi.class);
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        anotherOrg = UserTestUtils.createNewOrgFull("anotherOrg");
        anotherUser = UserTestUtils.createUser("anotherUser", anotherOrg.getId());

        RecurringActionManager.setTaskomaticApi(taskomaticMock);
    }

    @Test
    public void testCreateMinionRecurringActions() throws Exception {
        var minion = MinionServerFactoryTest.createTestMinionServer(user);

        CONTEXT.checking(new Expectations() { {
            allowing(taskomaticMock).scheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
        } });

        try {
            var recurringAction = RecurringActionManager.createRecurringAction(
                    MINION, HIGHSTATE, minion.getId(), anotherUser);
            recurringAction.setCronExpr(CRON_EXPR); // todo maybe put to create?
            recurringAction.setName("test-recurring-action-1");
            RecurringActionManager.saveAndSchedule(recurringAction, anotherUser);
            fail("User shouldn't have access");
        }
        catch (ValidatorException e) {
            // no-op
        }

        var recurringAction = RecurringActionManager.createRecurringAction(
                MINION, HIGHSTATE, minion.getId(), user);
        recurringAction.setCronExpr(CRON_EXPR);
        recurringAction.setName("test-recurring-action-2");
        RecurringActionManager.saveAndSchedule(recurringAction, user);
        assertNotEmpty(RecurringActionFactory.listMinionRecurringActions(minion));
    }

    @Test
    public void testCreateGroupRecurringActions() throws Exception {
        CONTEXT.checking(new Expectations() { {
            allowing(taskomaticMock).scheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
        } });

        try {
            var group = ServerGroupTestUtils.createManaged(anotherUser);
            /* Restrict anotherUser from accessing the minion */
            anotherUser.removePermanentRole(RoleFactory.SYSTEM_GROUP_ADMIN);
            var recurringAction = RecurringActionManager.createRecurringAction(
                    GROUP, HIGHSTATE, group.getId(), anotherUser);
            recurringAction.setCronExpr(CRON_EXPR);
            recurringAction.setName("recurringaction1");
            RecurringActionManager.saveAndSchedule(recurringAction, anotherUser);
            fail("User shouldn't have access");
        }
        catch (ValidatorException e) {
            // no-op
        }

        var group = ServerGroupTestUtils.createManaged(user);
        var recurringAction = RecurringActionManager.createRecurringAction(
                GROUP, HIGHSTATE, group.getId(), user);
        recurringAction.setCronExpr(CRON_EXPR);
        recurringAction.setName("test-recurring-action-2");
        RecurringActionManager.saveAndSchedule(recurringAction, user);
        assertNotEmpty(RecurringActionFactory.listGroupRecurringActions(group));
    }

    @Test
    public void testCreateOrgRecurringActions() throws Exception {
        var org = user.getOrg();

        CONTEXT.checking(new Expectations() { {
            allowing(taskomaticMock).scheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
        } });

        try {
            var recurringAction = RecurringActionManager.createRecurringAction(
                    ORG, HIGHSTATE, org.getId(), anotherUser);
            recurringAction.setCronExpr(CRON_EXPR);
            recurringAction.setName("test-recurring-action-1");
            RecurringActionManager.saveAndSchedule(recurringAction, anotherUser);
            fail("User shouldn't have access");
        }
        catch (ValidatorException e) {
            // no-op
        }

        var recurringAction = RecurringActionManager.createRecurringAction(
                ORG, HIGHSTATE, org.getId(), user);
        recurringAction.setCronExpr(CRON_EXPR);
        recurringAction.setName("test-recurring-action-2");
        RecurringActionManager.saveAndSchedule(recurringAction, user);
        assertNotEmpty(RecurringActionFactory.listOrgRecurringActions(org.getId()));
    }

    @Test
    public void testCreateOrgActionCrossOrg() throws Exception {
        CONTEXT.checking(new Expectations() { {
            allowing(taskomaticMock).scheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
        } });

        // let's make 'anotherUser' admin of 'anotherOrg'
        anotherUser.addPermanentRole(RoleFactory.ORG_ADMIN);

        // user 'user' creates an action
        var action = RecurringActionManager.createRecurringAction(
                ORG, HIGHSTATE, user.getOrg().getId(), user);
        action.setCronExpr(CRON_EXPR);
        action.setName("test-recurring-action-2");
        RecurringActionManager.saveAndSchedule(action, user);

        // 'anotherUser' tries to update that action, which should fail
        action.setName("hack the planet!");
        try {
            RecurringActionManager.saveAndSchedule(action, anotherUser);
            fail("ValidatorException should have been thrown");
        }
        catch (ValidatorException e) {
            // no-op
        }
    }

    @Test
    public void testCreateOrgActionNoOrg() {
        try {
            // let's try to create an action for a nonexisting org
            RecurringActionManager.createRecurringAction(ORG, HIGHSTATE, -123456L, user);
            fail("An exception should have been thrown");
        }
        catch (EntityNotExistsException e) {
            // no-op
        }
    }

    @Test
    public void testListMinionRecurringActions() throws Exception {
        var minion = MinionServerFactoryTest.createTestMinionServer(user);
        var action = new MinionRecurringAction();
        action.setMinion(minion);
        action.setCronExpr(CRON_EXPR);
        action.setName("test-recurring-action");
        action.setActionType(RecurringActionType.ActionType.HIGHSTATE);
        RecurringActionFactory.save(action);

        assertEquals(List.of(action), RecurringActionManager.listMinionRecurringActions(minion.getId(), user));

        try {
            RecurringActionManager.listMinionRecurringActions(minion.getId(), anotherUser);
            fail("User shouldn't have access");
        }
        catch (PermissionException e) {
            // no-op
        }
    }

    @Test
    public void testListGroupRecurringActions() {
        ServerGroupManager manager = new ServerGroupManager(new TestSaltApi());
        ManagedServerGroup group = ServerGroupTestUtils.createManaged(user);

        var action = new GroupRecurringAction();
        action.setGroup(group);
        action.setCronExpr(CRON_EXPR);
        action.setName("test-recurring-action-1");
        action.setActionType(RecurringActionType.ActionType.HIGHSTATE);
        RecurringActionFactory.save(action);

        assertTrue(manager.canAccess(user, group));
        assertEquals(List.of(action), RecurringActionManager.listGroupRecurringActions(group.getId(), user));

        try {
            RecurringActionManager.listGroupRecurringActions(group.getId(), anotherUser);
            fail("User shouldn't have access");
        }
        catch (PermissionException e) {
            // no-op
        }
    }

    @Test
    public void testListOrgRecurringAction() {
        var action = new OrgRecurringAction();
        action.setOrg(user.getOrg());
        action.setCronExpr(CRON_EXPR);
        action.setName("test-recurring-action-1");
        action.setActionType(RecurringActionType.ActionType.HIGHSTATE);
        RecurringActionFactory.save(action);

        assertEquals(List.of(action), RecurringActionManager.listOrgRecurringActions(user.getOrg().getId(), user));

        try {
            RecurringActionManager.listOrgRecurringActions(user.getOrg().getId(), anotherUser);
            fail("User shouldn't have access");
        }
        catch (PermissionException e) {
            // no-op
        }
    }

    @Test
    public void testUpdateAction() throws Exception {
        var minion = MinionServerFactoryTest.createTestMinionServer(user);

        CONTEXT.checking(new Expectations() { {
            allowing(taskomaticMock).scheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
        } });

        var recurringAction = RecurringActionManager.createRecurringAction(
                MINION, HIGHSTATE, minion.getId(), user);
        recurringAction.setCronExpr(CRON_EXPR);
        recurringAction.setName("test-recurring-action-1");
        recurringAction = RecurringActionManager.saveAndSchedule(recurringAction, user);

        var sameAction = RecurringActionFactory.lookupById(recurringAction.getId()).get();
        var newName = "testname";
        var newCronExpr = "1 * * * * ?";
        sameAction.setName(newName);
        sameAction.setCronExpr(newCronExpr);
        RecurringActionManager.saveAndSchedule(recurringAction, user);

        var sameAction2 = RecurringActionFactory.lookupById(recurringAction.getId()).get();
        // the action with the original id has changed name and cron expr
        assertEquals(newName, sameAction2.getName());
        assertEquals(newCronExpr, sameAction2.getCronExpr());
    }

    @Test
    public void testDeleteAction() throws Exception {
        var minion = MinionServerFactoryTest.createTestMinionServer(user);

        CONTEXT.checking(new Expectations() { {
            allowing(taskomaticMock).scheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
            allowing(taskomaticMock).unscheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
        } });

        var recurringAction = RecurringActionManager.createRecurringAction(
                MINION, HIGHSTATE, minion.getId(), user);
        recurringAction.setCronExpr(CRON_EXPR);
        recurringAction.setName("test-recurring-action-1");
        // Make sure action type is persisted when running the test
        HibernateFactory.getSession().save(recurringAction.getRecurringActionType());
        RecurringActionManager.saveAndSchedule(recurringAction, user);

        assertEquals(List.of(recurringAction), RecurringActionManager.listMinionRecurringActions(minion.getId(), user));

        try {
            RecurringActionManager.deleteAndUnschedule(recurringAction, anotherUser);
            fail("User shouldn't have permission");
        }
        catch (PermissionException e) {
            // no-op
        }

        RecurringActionManager.deleteAndUnschedule(recurringAction, user);
        assertTrue(RecurringActionFactory.listMinionRecurringActions(minion).isEmpty());
    }

    @Test
    public void testCreateActionsWithSameName() throws Exception {
        var minion = MinionServerFactoryTest.createTestMinionServer(user);

        CONTEXT.checking(new Expectations() { {
            allowing(taskomaticMock).scheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
        } });

        var action = RecurringActionManager.createRecurringAction(
                MINION, HIGHSTATE, minion.getId(), user);
        action.setCronExpr(CRON_EXPR);
        action.setName("test-recurring-action");
        RecurringActionManager.saveAndSchedule(action, user);

        var sameAction = RecurringActionManager.createRecurringAction(
                MINION, HIGHSTATE, minion.getId(), user);
        sameAction.setCronExpr(CRON_EXPR);
        sameAction.setName("test-recurring-action");
        try {
            RecurringActionManager.saveAndSchedule(sameAction, user);
            fail("An exception should have been thrown");
        }
        catch (ValidatorException e) {
            // no-op
        }
    }

    @Test
    public void testCreateActionsWithSameNameDifferentEntity() throws Exception {
        var minion = MinionServerFactoryTest.createTestMinionServer(user);
        var minion2 = MinionServerFactoryTest.createTestMinionServer(user);

        CONTEXT.checking(new Expectations() { {
            allowing(taskomaticMock).scheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
        } });

        var action = RecurringActionManager.createRecurringAction(
                MINION, HIGHSTATE, minion.getId(), user);
        action.setCronExpr(CRON_EXPR);
        action.setName("test-recurring-action");
        RecurringActionManager.saveAndSchedule(action, user);

        var otherAction = RecurringActionManager.createRecurringAction(
                MINION, HIGHSTATE, minion2.getId(), user);
        otherAction.setCronExpr(CRON_EXPR);
        otherAction.setName("test-recurring-action");
        try {
            RecurringActionManager.saveAndSchedule(otherAction, user);
        }
        catch (EntityExistsException e) {
            fail("An exception shouldn't have been thrown");
        }
    }

    @Test
    public void testCreateActionWithInvalidCron() throws Exception {
        var minion = MinionServerFactoryTest.createTestMinionServer(user);
        var invalidCron = "SOMETHING INVALID";

        CONTEXT.checking(new Expectations() { {
            allowing(taskomaticMock).scheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
        } });

        var action = RecurringActionManager.createRecurringAction(
                MINION, HIGHSTATE, minion.getId(), user);
        action.setCronExpr(invalidCron);
        action.setName("test-recurring-action");

        try {
            RecurringActionManager.saveAndSchedule(action, user);
            fail("An exception should have been thrown");
        }
        catch (ValidatorException e) {
            // no-op
        }
    }
}
