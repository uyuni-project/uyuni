package com.redhat.rhn.manager.recurringactions.test;

import static com.redhat.rhn.domain.recurringactions.RecurringAction.Type.GROUP;
import static com.redhat.rhn.domain.recurringactions.RecurringAction.Type.MINION;
import static com.redhat.rhn.domain.recurringactions.RecurringAction.Type.ORG;

import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.recurringactions.GroupRecurringAction;
import com.redhat.rhn.domain.recurringactions.MinionRecurringAction;
import com.redhat.rhn.domain.recurringactions.OrgRecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringActionFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.manager.recurringactions.RecurringActionManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerGroupTestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit3.JUnit3Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;

import java.util.List;

/**
 * Tests for {@link RecurringActionManager}
 */
public class RecurringActionManagerTest extends BaseTestCaseWithUser {

    private static final Mockery CONTEXT = new JUnit3Mockery() {{
        setThreadingPolicy(new Synchroniser());
    }};

    private Org anotherOrg;
    private User anotherUser;
    private static TaskomaticApi taskomaticMock;
    private static final String CRON_EXPR = "0 * * * * ?";

    static {
        CONTEXT.setImposteriser(ClassImposteriser.INSTANCE);
        taskomaticMock = CONTEXT.mock(TaskomaticApi.class);
        RecurringActionManager.setTaskomaticApi(taskomaticMock);
    }

    public void setUp() throws Exception {
        super.setUp();
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        anotherOrg = UserTestUtils.createNewOrgFull("anotherOrg");
        anotherUser = UserTestUtils.createUser("anotherUser", anotherOrg.getId());
    }

    public void testCreateMinionRecurringActions() throws Exception {
        var minion = MinionServerFactoryTest.createTestMinionServer(user);

        CONTEXT.checking(new Expectations() { {
            allowing(taskomaticMock).scheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
        } });

        try {
            var recurringAction = RecurringActionManager.createRecurringAction(MINION, minion.getId(), anotherUser);
            recurringAction.setCronExpr(CRON_EXPR); // todo maybe put to create?
            recurringAction.setName("test-recurring-action-1");
            RecurringActionManager.saveAndSchedule(recurringAction, anotherUser);
            fail("User shouldn't have access");
        }
        catch (PermissionException e) {
            // no-op
        }

        var recurringAction = RecurringActionManager.createRecurringAction(MINION, minion.getId(), user);
        recurringAction.setCronExpr(CRON_EXPR);
        recurringAction.setName("test-recurring-action-2");
        RecurringActionManager.saveAndSchedule(recurringAction, user);
        assertNotEmpty(RecurringActionFactory.listMinionRecurringActions(minion.getId()));
    }

    public void testCreateGroupRecurringActions() throws Exception {
        CONTEXT.checking(new Expectations() { {
            allowing(taskomaticMock).scheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
        } });

        try {
            var group = ServerGroupTestUtils.createManaged(anotherUser);
            /* Restrict anotherUser from accessing the minion */
            anotherUser.removePermanentRole(RoleFactory.SYSTEM_GROUP_ADMIN);
            var recurringAction = RecurringActionManager.createRecurringAction(GROUP, group.getId(), anotherUser);
            recurringAction.setCronExpr(CRON_EXPR);
            recurringAction.setName("recurringaction1");
            RecurringActionManager.saveAndSchedule(recurringAction, anotherUser);
            fail("User shouldn't have access");
        }
        catch (PermissionException e) {
            // no-op
        }

        var group = ServerGroupTestUtils.createManaged(user);
        var recurringAction = RecurringActionManager.createRecurringAction(GROUP, group.getId(), user);
        recurringAction.setCronExpr(CRON_EXPR);
        recurringAction.setName("test-recurring-action-2");
        RecurringActionManager.saveAndSchedule(recurringAction, user);
        assertNotEmpty(RecurringActionFactory.listGroupRecurringActions(group.getId()));
    }

    public void testCreateOrgRecurringActions() throws Exception {
        var org = user.getOrg();

        CONTEXT.checking(new Expectations() { {
            allowing(taskomaticMock).scheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
        } });

        try {
            var recurringAction = RecurringActionManager.createRecurringAction(ORG, org.getId(), anotherUser);
            recurringAction.setCronExpr(CRON_EXPR);
            recurringAction.setName("test-recurring-action-1");
            RecurringActionManager.saveAndSchedule(recurringAction, anotherUser);
            fail("User shouldn't have access");
        }
        catch (PermissionException e) {
            // no-op
        }

        var recurringAction = RecurringActionManager.createRecurringAction(ORG, org.getId(), user);
        recurringAction.setCronExpr(CRON_EXPR);
        recurringAction.setName("test-recurring-action-2");
        RecurringActionManager.saveAndSchedule(recurringAction, user);
        assertNotEmpty(RecurringActionFactory.listOrgRecurringActions(org.getId()));
    }

    public void testCreateOrgActionCrossOrg() throws Exception {
        CONTEXT.checking(new Expectations() { {
            allowing(taskomaticMock).scheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
        } });

        // let's make 'anotherUser' admin of 'anotherOrg'
        anotherUser.addPermanentRole(RoleFactory.ORG_ADMIN);

        // user 'user' creates an action
        var action = RecurringActionManager.createRecurringAction(ORG, user.getOrg().getId(), user);
        action.setCronExpr(CRON_EXPR);
        action.setName("test-recurring-action-2");
        RecurringActionManager.saveAndSchedule(action, user);

        // 'anotherUser' tries to update that action, which should fail
        action.setName("hack the planet!");
        try {
            RecurringActionManager.saveAndSchedule(action, anotherUser);
            fail("PermissionException should have been thrown");
        }
        catch (PermissionException e) {
            // no-op
        }
    }

    public void testListMinionRecurringActions() throws Exception {
        var minion = MinionServerFactoryTest.createTestMinionServer(user);
        var action = new MinionRecurringAction();
        action.setMinion(minion);
        action.setName("test-recurring-action-1");
        action.setCronExpr(CRON_EXPR);
        action.setName("test-recurring-action");
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

    public void testListGroupRecurringActions() {
        ServerGroupManager manager = ServerGroupManager.getInstance();
        ManagedServerGroup group = ServerGroupTestUtils.createManaged(user);

        var action = new GroupRecurringAction();
        action.setGroup(group);
        action.setCronExpr(CRON_EXPR);
        action.setName("test-recurring-action-1");
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

    public void testListOrgRecurringAction() {
        var action = new OrgRecurringAction();
        action.setOrg(user.getOrg());
        action.setCronExpr(CRON_EXPR);
        action.setName("test-recurring-action-1");
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

    public void testUpdateAction() throws Exception {
        var minion = MinionServerFactoryTest.createTestMinionServer(user);

        CONTEXT.checking(new Expectations() { {
            allowing(taskomaticMock).scheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
        } });

        var recurringAction = RecurringActionManager.createRecurringAction(MINION, minion.getId(), user);
        recurringAction.setCronExpr(CRON_EXPR);
        recurringAction.setName("test-recurring-action-1");
        RecurringActionManager.saveAndSchedule(recurringAction, user);

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

    public void testDeleteAction() throws Exception {
        var minion = MinionServerFactoryTest.createTestMinionServer(user);

        CONTEXT.checking(new Expectations() { {
            allowing(taskomaticMock).scheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
            allowing(taskomaticMock).unscheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
        } });

        var recurringAction = RecurringActionManager.createRecurringAction(MINION, minion.getId(), user);
        recurringAction.setCronExpr(CRON_EXPR);
        recurringAction.setName("test-recurring-action-1");
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
        assertTrue(RecurringActionFactory.listMinionRecurringActions(recurringAction.getId()).isEmpty());
    }

    public void testCreateActionsWithSameName() throws Exception {
        var minion = MinionServerFactoryTest.createTestMinionServer(user);

        CONTEXT.checking(new Expectations() { {
            allowing(taskomaticMock).scheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
        } });

        var action = RecurringActionManager.createRecurringAction(MINION, minion.getId(), user);
        action.setCronExpr(CRON_EXPR);
        action.setName("test-recurring-action");
        RecurringActionManager.saveAndSchedule(action, user);

        var sameAction = RecurringActionManager.createRecurringAction(MINION, minion.getId(), user);
        sameAction.setCronExpr(CRON_EXPR);
        sameAction.setName("test-recurring-action");
        try {
            RecurringActionManager.saveAndSchedule(sameAction, user);
            fail("An exception should have been thrown");
        }
        catch (EntityExistsException e) {
            // no-op
        }
    }

    public void testCreateActionsWithSameNameDifferentEntity() throws Exception {
        var minion = MinionServerFactoryTest.createTestMinionServer(user);
        var minion2 = MinionServerFactoryTest.createTestMinionServer(user);

        CONTEXT.checking(new Expectations() { {
            allowing(taskomaticMock).scheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
        } });

        var action = RecurringActionManager.createRecurringAction(MINION, minion.getId(), user);
        action.setCronExpr(CRON_EXPR);
        action.setName("test-recurring-action");
        RecurringActionManager.saveAndSchedule(action, user);

        var otherAction = RecurringActionManager.createRecurringAction(MINION, minion2.getId(), user);
        otherAction.setCronExpr(CRON_EXPR);
        otherAction.setName("test-recurring-action");
        try {
            RecurringActionManager.saveAndSchedule(otherAction, user);
        }
        catch (EntityExistsException e) {
            fail("An exception shouldn't have been thrown");
        }
    }
}
