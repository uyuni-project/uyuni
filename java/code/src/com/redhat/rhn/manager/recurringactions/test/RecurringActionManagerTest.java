package com.redhat.rhn.manager.recurringactions.test;

import static com.redhat.rhn.domain.recurringactions.RecurringAction.Type.GROUP;
import static com.redhat.rhn.domain.recurringactions.RecurringAction.Type.MINION;
import static com.redhat.rhn.domain.recurringactions.RecurringAction.Type.ORG;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.recurringactions.GroupRecurringAction;
import com.redhat.rhn.domain.recurringactions.MinionRecurringAction;
import com.redhat.rhn.domain.recurringactions.OrgRecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringActionFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.recurringactions.RecurringActionManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerGroupTestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit3.JUnit3Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;

import java.util.List;

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
            allowing(taskomaticMock).scheduleSatBunch(with(any(User.class)), with(any(String.class)), with(any(String.class)), with(any(String.class)));
        } });

        try {
            var recurringAction = RecurringActionManager.createRecurringAction(MINION, minion.getId(), anotherUser);
            recurringAction.setCronExpr(CRON_EXPR); // todo maybe put to create?
            RecurringActionManager.saveAndSchedule(recurringAction, anotherUser);
            fail("User shouldn't have access");
        }
        catch (PermissionException e) {
            // no-op
        }

        var recurringAction = RecurringActionManager.createRecurringAction(MINION, minion.getId(), user);
        recurringAction.setCronExpr(CRON_EXPR);
        RecurringActionManager.saveAndSchedule(recurringAction, user);
        assertNotEmpty(RecurringActionFactory.listMinionRecurringActions(minion.getId()));
    }

    public void testCreateGroupRecurringActions() throws Exception {
        CONTEXT.checking(new Expectations() { {
            allowing(taskomaticMock).scheduleSatBunch(with(any(User.class)), with(any(String.class)), with(any(String.class)), with(any(String.class)));
        } });

        try {
            var group = ServerGroupTestUtils.createManaged(anotherUser);
            /* Restrict anotherUser from accessing the minion */
            anotherUser.removePermanentRole(RoleFactory.SYSTEM_GROUP_ADMIN);
            var recurringAction = RecurringActionManager.createRecurringAction(GROUP, group.getId(), anotherUser);
            recurringAction.setCronExpr(CRON_EXPR);
            RecurringActionManager.saveAndSchedule(recurringAction, anotherUser);
            fail("User shouldn't have access");
        }
        catch (PermissionException e) {
            // no-op
        }

        var group = ServerGroupTestUtils.createManaged(user);
        var recurringAction = RecurringActionManager.createRecurringAction(GROUP, group.getId(), user);
        recurringAction.setCronExpr(CRON_EXPR);
        RecurringActionManager.saveAndSchedule(recurringAction, user);
        assertNotEmpty(RecurringActionFactory.listGroupRecurringActions(group.getId()));
    }

    public void testCreateOrgRecurringActions() throws Exception {
        var org = user.getOrg();

        CONTEXT.checking(new Expectations() { {
            allowing(taskomaticMock).scheduleSatBunch(with(any(User.class)), with(any(String.class)), with(any(String.class)), with(any(String.class)));
        } });

        try {
            var recurringAction = RecurringActionManager.createRecurringAction(ORG, org.getId(), anotherUser);
            recurringAction.setCronExpr(CRON_EXPR);
            RecurringActionManager.saveAndSchedule(recurringAction, anotherUser);
            fail("User shouldn't have access");
        }
        catch (PermissionException e) {
            // no-op
        }

        var recurringAction = RecurringActionManager.createRecurringAction(ORG, org.getId(), user);
        recurringAction.setCronExpr(CRON_EXPR);
        RecurringActionManager.saveAndSchedule(recurringAction, user);
        assertNotEmpty(RecurringActionFactory.listOrgRecurringActions(org.getId()));
    }

    public void testListMinionRecurringActions() throws Exception {
        var minion = MinionServerFactoryTest.createTestMinionServer(user);
        var action = new MinionRecurringAction();
        action.setMinion(minion);
        RecurringActionFactory.save(action);

        assertEquals(List.of(action), RecurringActionManager.listMinionRecurringActions(minion.getId(), user));

        try{
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
        RecurringActionFactory.save(action);

        assertTrue(manager.canAccess(user, group));
        assertEquals(List.of(action), RecurringActionManager.listGroupRecurringActions(group.getId(), user));

        try{
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
        RecurringActionFactory.save(action);

        assertEquals(List.of(action), RecurringActionManager.listOrgRecurringActions(user.getOrg().getId(), user));

        try{
            RecurringActionManager.listOrgRecurringActions(user.getOrg().getId(), anotherUser);
            fail("User shouldn't have access");
        }
        catch (PermissionException e) {
            // no-op
        }
    }

    // todo make this complete
    public void testUpdateAction() throws Exception {
        var minion = MinionServerFactoryTest.createTestMinionServer(user);

        CONTEXT.checking(new Expectations() { {
            allowing(taskomaticMock).scheduleSatBunch(with(any(User.class)), with(any(String.class)), with(any(String.class)), with(any(String.class)));
        } });

        var recurringAction = RecurringActionManager.createRecurringAction(MINION, minion.getId(), user);
        recurringAction.setCronExpr(CRON_EXPR);
        RecurringActionManager.saveAndSchedule(recurringAction, user);

        HibernateFactory.getSession().flush();

        var other = RecurringActionFactory.lookupById(recurringAction.getId());
        other.get().setName("testname");
        RecurringActionManager.saveAndSchedule(recurringAction, user);
        HibernateFactory.getSession().flush();
        var other2 = RecurringActionFactory.lookupById(recurringAction.getId());

        System.out.println(other2);
        System.out.println(RecurringActionManager.listMinionRecurringActions(minion.getId(), user));
    }
}
