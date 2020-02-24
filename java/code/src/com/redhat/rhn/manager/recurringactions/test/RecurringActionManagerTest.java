package com.redhat.rhn.manager.recurringactions.test;

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

    public void setUp() throws Exception {
        super.setUp();
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        anotherOrg = UserTestUtils.createNewOrgFull("anotherOrg");
        anotherUser = UserTestUtils.createUser("anotherUser", anotherOrg.getId());
    }

    public void testCreateMinionRecurringActions() throws Exception {
        CONTEXT.setImposteriser(ClassImposteriser.INSTANCE);
        TaskomaticApi taskomaticMock = CONTEXT.mock(TaskomaticApi.class);
        RecurringActionManager.setTaskomaticApi(taskomaticMock);

        var minion = MinionServerFactoryTest.createTestMinionServer(user);

        CONTEXT.checking(new Expectations() { {
            allowing(taskomaticMock).scheduleSatBunch(with(any(User.class)), with(any(String.class)), with(any(String.class)), with(any(String.class)));
        } });

        try {
            RecurringActionManager.createMinionRecurringAction(minion.getId(), "", false, false, anotherUser);
            fail("User shouldn't have access");
        }
        catch (PermissionException e) {
            // no-op
        }

        RecurringActionManager.createMinionRecurringAction(minion.getId(), "", false, false, user);
        assertNotEmpty(RecurringActionFactory.listMinionRecurringActions(minion.getId()));
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
}
