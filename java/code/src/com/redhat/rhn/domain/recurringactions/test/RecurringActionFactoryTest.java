package com.redhat.rhn.domain.recurringactions.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
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

import javax.persistence.PersistenceException;

/**
 * Tests for {@link RecurringActionFactory}
 */
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

    public void testLookupRecurringActionByScheduleNameNoMatch() throws Exception {
        assertTrue(RecurringActionFactory.lookupByJobName("recurring-action-987654321").isEmpty());
    }

    public void testLookupRecurringActionByScheduleName() throws Exception {
        var action = new MinionRecurringAction();
        var minion = MinionServerFactoryTest.createTestMinionServer(user);
        action.setMinion(minion);
        RecurringActionFactory.save(action);

        assertEquals(
                action,
                RecurringActionFactory.lookupByJobName("recurring-action-" + action.getId()).orElseThrow());
    }

    public void testMultipleActionsWithSameName() throws Exception {
        try {
            var orgAction = new OrgRecurringAction();
            orgAction.setOrg(user.getOrg());
            orgAction.setName("already-existing-action");
            RecurringActionFactory.save(orgAction);

            var minAction1 = new MinionRecurringAction();
            var minion1 = MinionServerFactoryTest.createTestMinionServer(user);
            minAction1.setMinion(minion1);
            minAction1.setName("already-existing-action");
            RecurringActionFactory.save(minAction1);

            var minAction2 = new MinionRecurringAction();
            var minion2 = MinionServerFactoryTest.createTestMinionServer(user);
            minAction2.setMinion(minion2);
            minAction2.setName("already-existing-action");
            RecurringActionFactory.save(minAction2);

            // we want to make sure multiple actions with the name can co-exist and can be persisted
            // as long they reference different target entity (e.g. 2 different minions)
            HibernateFactory.getSession().flush();
        }
        catch (PersistenceException e) {
            fail("No persistence exception should have occured");
        }
    }
}
