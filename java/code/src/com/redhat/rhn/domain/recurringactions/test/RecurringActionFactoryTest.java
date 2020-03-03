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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.PersistenceException;

/**
 * Tests for {@link RecurringActionFactory}
 */
public class RecurringActionFactoryTest extends BaseTestCaseWithUser {

    private static final String CRON_EXPR = "0 * * * * ?";

    public void testListMinionRecurringActions() throws Exception {
        var action = new MinionRecurringAction();
        var minion = MinionServerFactoryTest.createTestMinionServer(user);
        action.setMinion(minion);
        action.setName("test-recurring-action-1");
        action.setCronExpr(CRON_EXPR);
        RecurringActionFactory.save(action);

        assertEquals(List.of(action), RecurringActionFactory.listMinionRecurringActions(minion.getId()));
    }

    public void testListMultipleMinionRecurringActions() throws Exception {
        var action = new MinionRecurringAction();
        var minion = MinionServerFactoryTest.createTestMinionServer(user);
        action.setName("action name 1");
        action.setCronExpr(CRON_EXPR);
        action.setMinion(minion);
        RecurringActionFactory.save(action);

        var action2 = new MinionRecurringAction();
        action2.setName("action name 2");
        action2.setCronExpr(CRON_EXPR);
        action2.setMinion(minion);
        RecurringActionFactory.save(action2);

        var actualSet = new HashSet<>(RecurringActionFactory.listMinionRecurringActions(minion.getId()));
        assertEquals(Set.of(action, action2), actualSet);
    }

    public void testListGroupRecurringActions() {
        var action = new GroupRecurringAction();
        var group = ServerGroupTestUtils.createManaged(user);

        action.setGroup(group);
        action.setName("action name 1");
        action.setCronExpr(CRON_EXPR);
        RecurringActionFactory.save(action);

        assertEquals(List.of(action), RecurringActionFactory.listGroupRecurringActions(group.getId()));
    }

    public void testListOrgRecurringActions() {
        var action = new OrgRecurringAction();
        var org = OrgFactory.createOrg();
        org.setName("org created by OrgFactory test: " + TestUtils.randomString());
        org = OrgFactory.save(org);
        assertTrue(org.getId().longValue() > 0);

        action.setName("action name 1");
        action.setCronExpr(CRON_EXPR);
        action.setOrg(org);
        RecurringActionFactory.save(action);

        assertEquals(List.of(action), RecurringActionFactory.listOrgRecurringActions(org.getId()));
    }

    public void testListAllActions() throws Exception {
        var minionAction = new MinionRecurringAction();
        var minion = MinionServerFactoryTest.createTestMinionServer(user);
        minionAction.setName("action name 1");
        minionAction.setCronExpr(CRON_EXPR);
        minionAction.setMinion(minion);
        RecurringActionFactory.save(minionAction);

        var org = OrgFactory.createOrg();
        org.setName("org created by OrgFactory test: " + TestUtils.randomString());
        org = OrgFactory.save(org);
        var otherOrgAction = new OrgRecurringAction();
        otherOrgAction.setName("action name 1");
        otherOrgAction.setCronExpr(CRON_EXPR);
        otherOrgAction.setOrg(org);
        RecurringActionFactory.save(otherOrgAction);

        var userOrgAction = new OrgRecurringAction();
        userOrgAction.setName("action name 1");
        userOrgAction.setCronExpr(CRON_EXPR);
        userOrgAction.setOrg(user.getOrg());
        RecurringActionFactory.save(userOrgAction);

        var group = ServerGroupTestUtils.createManaged(user);
        var groupAction = new GroupRecurringAction();
        groupAction.setName("action name 1");
        groupAction.setCronExpr(CRON_EXPR);
        groupAction.setGroup(group);
        RecurringActionFactory.save(groupAction);

        var expectedActions = Set.of(minionAction, userOrgAction, groupAction);
        var actualActions = new HashSet<>(RecurringActionFactory.listAllRecurringActions(user));
        assertEquals(expectedActions, actualActions);
    }

    public void testMinionActionTaskomaticPrefixComputation() throws Exception {
        var action = new MinionRecurringAction();
        var minion = MinionServerFactoryTest.createTestMinionServer(user);
        action.setName("action name 1");
        action.setCronExpr(CRON_EXPR);
        action.setMinion(minion);
        RecurringActionFactory.save(action);
        assertEquals("recurring-action-" + action.getId(), action.computeTaskoScheduleName());
    }

    public void testGroupActionTaskomaticPrefixComputation() throws Exception {
        var action = new GroupRecurringAction();
        var group = ServerGroupTestUtils.createManaged(user);
        action.setName("action name 1");
        action.setCronExpr(CRON_EXPR);
        action.setGroup(group);
        RecurringActionFactory.save(action);
        assertEquals("recurring-action-" + action.getId(), action.computeTaskoScheduleName());
    }

    public void testOrgActionTaskomaticPrefixComputation() throws Exception {
        var action = new OrgRecurringAction();
        action.setName("action name 1");
        action.setCronExpr(CRON_EXPR);
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
        action.setName("action name 1");
        action.setCronExpr(CRON_EXPR);
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
            orgAction.setCronExpr(CRON_EXPR);
            RecurringActionFactory.save(orgAction);

            var minAction1 = new MinionRecurringAction();
            var minion1 = MinionServerFactoryTest.createTestMinionServer(user);
            minAction1.setMinion(minion1);
            minAction1.setName("already-existing-action");
            minAction1.setCronExpr(CRON_EXPR);
            RecurringActionFactory.save(minAction1);

            var minAction2 = new MinionRecurringAction();
            var minion2 = MinionServerFactoryTest.createTestMinionServer(user);
            minAction2.setMinion(minion2);
            minAction2.setName("already-existing-action");
            minAction2.setCronExpr(CRON_EXPR);
            RecurringActionFactory.save(minAction2);

            // we want to make sure multiple actions with the name can co-exist and can be persisted
            // as long they reference different target entity (e.g. 2 different minions)
            HibernateFactory.getSession().flush();
        }
        catch (PersistenceException e) {
            fail("No persistence exception should have occured");
        }
    }

    public void testDeleteRecurringAction() throws Exception {
        var action = new MinionRecurringAction();
        var minion = MinionServerFactoryTest.createTestMinionServer(user);
        action.setMinion(minion);
        action.setName("already-existing-action");
        action.setCronExpr(CRON_EXPR);

        RecurringActionFactory.save(action);
        assertEquals(List.of(action), RecurringActionFactory.listMinionRecurringActions(minion.getId()));

        RecurringActionFactory.delete(action);

        assertTrue(RecurringActionFactory.listMinionRecurringActions(minion.getId()).isEmpty());
    }

    public void testLookupEqualEntity() throws Exception {
        String name = "already-existing-action";
        var action = new MinionRecurringAction();
        var minion = MinionServerFactoryTest.createTestMinionServer(user);
        action.setMinion(minion);
        action.setName(name);
        action.setCronExpr(CRON_EXPR);
        RecurringActionFactory.save(action);

        var orgAction = new OrgRecurringAction();
        orgAction.setOrg(user.getOrg());
        orgAction.setName(name);
        orgAction.setCronExpr(CRON_EXPR);
        RecurringActionFactory.save(orgAction);

        assertEquals(action.getId(), RecurringActionFactory.lookupEqualEntityId(action).get());
        assertEquals(orgAction.getId(), RecurringActionFactory.lookupEqualEntityId(orgAction).get());
    }

    public void testLookupEqualActionObject() throws Exception {
        var action = new MinionRecurringAction();
        var minion = MinionServerFactoryTest.createTestMinionServer(user);
        action.setMinion(minion);
        action.setName("already-existing-action");
        action.setCronExpr(CRON_EXPR);
        RecurringActionFactory.save(action);

        // we don't save the 2nd object -> it's just an object, no entity, but it has same properties as the 1st object
        var action2 = new MinionRecurringAction();
        action2.setMinion(minion);
        action2.setName("already-existing-action");
        action2.setCronExpr(CRON_EXPR);

        assertEquals(action.getId(), RecurringActionFactory.lookupEqualEntityId(action2).get());
    }

    public void testLookupNotEqualActionObject() throws Exception {
        var action = new MinionRecurringAction();
        var minion = MinionServerFactoryTest.createTestMinionServer(user);
        action.setMinion(minion);
        action.setName("already-existing-action");
        action.setCronExpr(CRON_EXPR);
        RecurringActionFactory.save(action);

        // we don't save the 2nd object -> it's just an object, no entity
        var action2 = new MinionRecurringAction();
        action2.setMinion(minion);
        action2.setName("already-existing-action2");
        action2.setCronExpr(CRON_EXPR);

        assertFalse(RecurringActionFactory.lookupEqualEntityId(action2).isPresent());
    }
}
