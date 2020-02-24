package com.redhat.rhn.domain.recurringactions.test;

import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.recurringactions.GroupRecurringAction;
import com.redhat.rhn.domain.recurringactions.MinionRecurringAction;
import com.redhat.rhn.domain.recurringactions.OrgRecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringActionFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerGroupTestUtils;
import com.redhat.rhn.testing.TestUtils;

import java.util.List;

public class RecurringActionFactoryTest extends BaseTestCaseWithUser {

    public void testListMinionRecurringActions() throws Exception {
        var action = new MinionRecurringAction();
        var minion = MinionServerFactoryTest.createTestMinionServer(user);
        action.setMinion(minion);
        RecurringActionFactory.save(action);

        assertEquals(List.of(action), RecurringActionFactory.listMinionRecurringActions(minion.getId()));
    }

    public void testListMultipleMinionRecurringActions() throws Exception {
        var action = new MinionRecurringAction();
        var minion = MinionServerFactoryTest.createTestMinionServer(user);
        action.setMinion(minion);
        RecurringActionFactory.save(action);

        var action2 = new MinionRecurringAction();
        action2.setMinion(minion);
        RecurringActionFactory.save(action2);

        assertEquals(List.of(action, action2), RecurringActionFactory.listMinionRecurringActions(minion.getId()));
    }

    public void testListGroupRecurringActions() {
        var action = new GroupRecurringAction();
        var group = ServerGroupTestUtils.createManaged(user);

        action.setGroup(group);
        RecurringActionFactory.save(action);

        assertEquals(List.of(action), RecurringActionFactory.listGroupRecurringActions(group.getId()));
    }

    public void testListOrgRecurringActions() {
        var action = new OrgRecurringAction();
        var org = OrgFactory.createOrg();
        org.setName("org created by OrgFactory test: " + TestUtils.randomString());
        org = OrgFactory.save(org);
        assertTrue(org.getId().longValue() > 0);

        action.setOrg(org);
        RecurringActionFactory.save(action);

        assertEquals(List.of(action), RecurringActionFactory.listOrgRecurringActions(org.getId()));
    }

    public void testMinionActionTaskomaticPrefixComputation() throws Exception {
        var action = new MinionRecurringAction();
        var minion = MinionServerFactoryTest.createTestMinionServer(user);
        action.setMinion(minion);
        RecurringActionFactory.save(action);
        assertEquals("recurring-action-" + action.getId(), action.computeTaskoScheduleName());
    }

    public void testGroupActionTaskomaticPrefixComputation() throws Exception {
        var action = new GroupRecurringAction();
        var group = ServerGroupTestUtils.createManaged(user);
        action.setGroup(group);
        RecurringActionFactory.save(action);
        assertEquals("recurring-action-" + action.getId(), action.computeTaskoScheduleName());
    }

    public void testOrgActionTaskomaticPrefixComputation() throws Exception {
        var action = new OrgRecurringAction();
        action.setOrg(user.getOrg());
        RecurringActionFactory.save(action);
        assertEquals("recurring-action-" + action.getId(), action.computeTaskoScheduleName());
    }
}
