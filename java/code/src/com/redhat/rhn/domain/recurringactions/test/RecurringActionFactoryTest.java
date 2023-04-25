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
package com.redhat.rhn.domain.recurringactions.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.recurringactions.GroupRecurringAction;
import com.redhat.rhn.domain.recurringactions.MinionRecurringAction;
import com.redhat.rhn.domain.recurringactions.OrgRecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringActionFactory;
import com.redhat.rhn.domain.recurringactions.type.RecurringActionType;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerGroupTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.utils.PagedSqlQueryBuilder;
import com.suse.manager.webui.utils.gson.RecurringActionScheduleJson;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.PersistenceException;

/**
 * Tests for {@link RecurringActionFactory}
 */
public class RecurringActionFactoryTest extends BaseTestCaseWithUser {

    private static final String CRON_EXPR = "0 * * * * ?";

    @Test
    public void testListMinionRecurringActions() throws Exception {
        var action = new MinionRecurringAction();
        var minion = MinionServerFactoryTest.createTestMinionServer(user);
        action.setMinion(minion);
        action.setName("test-recurring-action-1");
        action.setCronExpr(CRON_EXPR);
        action.setActionType(RecurringActionType.ActionType.HIGHSTATE);
        RecurringActionFactory.save(action);

        assertEquals(List.of(action), RecurringActionFactory.listMinionRecurringActions(minion));
    }

    @Test
    public void testListMultipleMinionRecurringActions() throws Exception {
        var action = new MinionRecurringAction();
        var minion = MinionServerFactoryTest.createTestMinionServer(user);
        action.setName("action name 1");
        action.setCronExpr(CRON_EXPR);
        action.setMinion(minion);
        action.setActionType(RecurringActionType.ActionType.HIGHSTATE);
        RecurringActionFactory.save(action);

        var action2 = new MinionRecurringAction();
        action2.setName("action name 2");
        action2.setCronExpr(CRON_EXPR);
        action2.setMinion(minion);
        action2.setActionType(RecurringActionType.ActionType.HIGHSTATE);
        RecurringActionFactory.save(action2);

        var actualSet = new HashSet<>(RecurringActionFactory.listMinionRecurringActions(minion));
        assertEquals(Set.of(action, action2), actualSet);
    }

    @Test
    public void testListGroupRecurringActions() {
        var action = new GroupRecurringAction();
        var group = ServerGroupTestUtils.createManaged(user);

        action.setGroup(group);
        action.setName("action name 1");
        action.setCronExpr(CRON_EXPR);
        action.setActionType(RecurringActionType.ActionType.HIGHSTATE);
        RecurringActionFactory.save(action);

        assertEquals(List.of(action), RecurringActionFactory.listGroupRecurringActions(group));
    }

    @Test
    public void testListOrgRecurringActions() {
        var action = new OrgRecurringAction();
        var org = OrgFactory.createOrg();
        org.setName("org created by OrgFactory test: " + TestUtils.randomString());
        org = OrgFactory.save(org);
        assertTrue(org.getId() > 0);

        action.setName("action name 1");
        action.setCronExpr(CRON_EXPR);
        action.setOrg(org);
        action.setActionType(RecurringActionType.ActionType.HIGHSTATE);
        RecurringActionFactory.save(action);

        assertEquals(List.of(action), RecurringActionFactory.listOrgRecurringActions(org.getId()));
    }

    @Test
    public void testListAllActions() throws Exception {
        var minionAction = new MinionRecurringAction();
        var minion = MinionServerFactoryTest.createTestMinionServer(user);
        minionAction.setName("action name 1");
        minionAction.setCronExpr(CRON_EXPR);
        minionAction.setMinion(minion);
        minionAction.setActionType(RecurringActionType.ActionType.HIGHSTATE);
        RecurringActionFactory.save(minionAction);

        var org = OrgFactory.createOrg();
        org.setName("org created by OrgFactory test: " + TestUtils.randomString());
        org = OrgFactory.save(org);
        var otherOrgAction = new OrgRecurringAction();
        otherOrgAction.setName("action name 1");
        otherOrgAction.setCronExpr(CRON_EXPR);
        otherOrgAction.setOrg(org);
        otherOrgAction.setActionType(RecurringActionType.ActionType.HIGHSTATE);
        RecurringActionFactory.save(otherOrgAction);

        var userOrgAction = new OrgRecurringAction();
        userOrgAction.setName("action name 1");
        userOrgAction.setCronExpr(CRON_EXPR);
        userOrgAction.setOrg(user.getOrg());
        userOrgAction.setActionType(RecurringActionType.ActionType.HIGHSTATE);
        RecurringActionFactory.save(userOrgAction);

        var group = ServerGroupTestUtils.createManaged(user);
        var groupAction = new GroupRecurringAction();
        groupAction.setName("action name 1");
        groupAction.setCronExpr(CRON_EXPR);
        groupAction.setGroup(group);
        groupAction.setActionType(RecurringActionType.ActionType.HIGHSTATE);
        RecurringActionFactory.save(groupAction);

        HibernateFactory.getSession().flush();

        var expectedActions = Set.of(
            new RecurringActionScheduleJson(minionAction),
            new RecurringActionScheduleJson(groupAction),
            new RecurringActionScheduleJson(userOrgAction)
        );
        var actualActions = RecurringActionFactory.listAllRecurringActions(
            user, new PageControl(), PagedSqlQueryBuilder::parseFilterAsText
        );
        assertTrue(actualActions.containsAll(expectedActions) && expectedActions.containsAll(actualActions));
    }

    @Test
    public void testMinionActionTaskomaticPrefixComputation() throws Exception {
        var action = new MinionRecurringAction();
        var minion = MinionServerFactoryTest.createTestMinionServer(user);
        action.setName("action name 1");
        action.setCronExpr(CRON_EXPR);
        action.setMinion(minion);
        action.setActionType(RecurringActionType.ActionType.HIGHSTATE);
        RecurringActionFactory.save(action);
        assertEquals("recurring-action-" + action.getId(), action.computeTaskoScheduleName());
    }

    @Test
    public void testGroupActionTaskomaticPrefixComputation() {
        var action = new GroupRecurringAction();
        var group = ServerGroupTestUtils.createManaged(user);
        action.setName("action name 1");
        action.setCronExpr(CRON_EXPR);
        action.setGroup(group);
        action.setActionType(RecurringActionType.ActionType.HIGHSTATE);
        RecurringActionFactory.save(action);
        assertEquals("recurring-action-" + action.getId(), action.computeTaskoScheduleName());
    }

    @Test
    public void testOrgActionTaskomaticPrefixComputation() {
        var action = new OrgRecurringAction();
        action.setName("action name 1");
        action.setCronExpr(CRON_EXPR);
        action.setOrg(user.getOrg());
        action.setActionType(RecurringActionType.ActionType.HIGHSTATE);
        RecurringActionFactory.save(action);
        assertEquals("recurring-action-" + action.getId(), action.computeTaskoScheduleName());
    }

    @Test
    public void testLookupRecurringActionByScheduleNameNoMatch() {
        assertTrue(RecurringActionFactory.lookupByJobName("recurring-action-987654321").isEmpty());
    }

    @Test
    public void testLookupRecurringActionByScheduleName() throws Exception {
        var action = new MinionRecurringAction();
        var minion = MinionServerFactoryTest.createTestMinionServer(user);
        action.setName("action name 1");
        action.setCronExpr(CRON_EXPR);
        action.setMinion(minion);
        action.setActionType(RecurringActionType.ActionType.HIGHSTATE);
        RecurringActionFactory.save(action);

        assertEquals(
                action,
                RecurringActionFactory.lookupByJobName("recurring-action-" + action.getId()).orElseThrow());
    }

    @Test
    public void testMultipleActionsWithSameName() throws Exception {
        try {
            var orgAction = new OrgRecurringAction();
            orgAction.setOrg(user.getOrg());
            orgAction.setName("already-existing-action");
            orgAction.setCronExpr(CRON_EXPR);
            orgAction.setActionType(RecurringActionType.ActionType.HIGHSTATE);
            RecurringActionFactory.save(orgAction);

            var minAction1 = new MinionRecurringAction();
            var minion1 = MinionServerFactoryTest.createTestMinionServer(user);
            minAction1.setMinion(minion1);
            minAction1.setName("already-existing-action");
            minAction1.setCronExpr(CRON_EXPR);
            minAction1.setActionType(RecurringActionType.ActionType.HIGHSTATE);
            RecurringActionFactory.save(minAction1);

            var minAction2 = new MinionRecurringAction();
            var minion2 = MinionServerFactoryTest.createTestMinionServer(user);
            minAction2.setMinion(minion2);
            minAction2.setName("already-existing-action");
            minAction2.setCronExpr(CRON_EXPR);
            minAction2.setActionType(RecurringActionType.ActionType.HIGHSTATE);
            RecurringActionFactory.save(minAction2);

            // we want to make sure multiple actions with the name can co-exist and can be persisted
            // as long they reference different target entity (e.g. 2 different minions)
            HibernateFactory.getSession().flush();
        }
        catch (PersistenceException e) {
            fail("No persistence exception should have occured");
        }
    }

    @Test
    public void testDeleteRecurringAction() throws Exception {
        var action = new MinionRecurringAction();
        var minion = MinionServerFactoryTest.createTestMinionServer(user);
        action.setMinion(minion);
        action.setName("already-existing-action");
        action.setCronExpr(CRON_EXPR);
        action.setActionType(RecurringActionType.ActionType.HIGHSTATE);
        RecurringActionFactory.save(action);

        assertEquals(List.of(action), RecurringActionFactory.listMinionRecurringActions(minion));

        RecurringActionFactory.delete(action);

        assertTrue(RecurringActionFactory.listMinionRecurringActions(minion).isEmpty());
    }

    @Test
    public void testLookupEqualEntity() throws Exception {
        String name = "already-existing-action";
        var action = new MinionRecurringAction();
        var minion = MinionServerFactoryTest.createTestMinionServer(user);
        action.setMinion(minion);
        action.setName(name);
        action.setCronExpr(CRON_EXPR);
        action.setActionType(RecurringActionType.ActionType.HIGHSTATE);
        RecurringActionFactory.save(action);

        var orgAction = new OrgRecurringAction();
        orgAction.setOrg(user.getOrg());
        orgAction.setName(name);
        orgAction.setCronExpr(CRON_EXPR);
        orgAction.setActionType(RecurringActionType.ActionType.HIGHSTATE);
        RecurringActionFactory.save(orgAction);

        assertEquals(action.getId(), RecurringActionFactory.lookupEqualEntityId(action).get());
        assertEquals(orgAction.getId(), RecurringActionFactory.lookupEqualEntityId(orgAction).get());
    }

    @Test
    public void testLookupEqualActionObject() throws Exception {
        var action = new MinionRecurringAction();
        var minion = MinionServerFactoryTest.createTestMinionServer(user);
        action.setMinion(minion);
        action.setName("already-existing-action");
        action.setCronExpr(CRON_EXPR);
        action.setActionType(RecurringActionType.ActionType.HIGHSTATE);
        RecurringActionFactory.save(action);

        // we don't save the 2nd object -> it's just an object, no entity, but it has same properties as the 1st object
        var action2 = new MinionRecurringAction();
        action2.setMinion(minion);
        action2.setName("already-existing-action");
        action2.setCronExpr(CRON_EXPR);
        action2.setActionType(RecurringActionType.ActionType.HIGHSTATE);

        assertEquals(action.getId(), RecurringActionFactory.lookupEqualEntityId(action2).get());
    }

    @Test
    public void testLookupNotEqualActionObject() throws Exception {
        var action = new MinionRecurringAction();
        var minion = MinionServerFactoryTest.createTestMinionServer(user);
        action.setMinion(minion);
        action.setName("already-existing-action");
        action.setCronExpr(CRON_EXPR);
        action.setActionType(RecurringActionType.ActionType.HIGHSTATE);
        RecurringActionFactory.save(action);

        // we don't save the 2nd object -> it's just an object, no entity
        var action2 = new MinionRecurringAction();
        action2.setMinion(minion);
        action2.setName("already-existing-action2");
        action2.setCronExpr(CRON_EXPR);
        action2.setActionType(RecurringActionType.ActionType.HIGHSTATE);

        assertFalse(RecurringActionFactory.lookupEqualEntityId(action2).isPresent());
    }
}
