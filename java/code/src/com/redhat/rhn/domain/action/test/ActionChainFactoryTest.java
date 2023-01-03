/*
 * Copyright (c) 2014 SUSE LLC
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
/**
 * Copyright (c) 2014 Red Hat, Inc.
 */
package com.redhat.rhn.domain.action.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainEntry;
import com.redhat.rhn.domain.action.ActionChainEntryGroup;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.hibernate.ObjectNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tests for {@link ActionChainFactory}.
 * @author Silvio Moioli {@literal <smoioli@suse.de>}
 */
public class ActionChainFactoryTest extends BaseTestCaseWithUser {

    /**
     * Tests createActionChain() and getActionChain().
     */
    @Test
    public void testCreateActionChain() {
        String label = TestUtils.randomString();
        ActionChain actionChain = ActionChainFactory.createActionChain(label, user);
        assertNotNull(actionChain);

        ActionChain retrievedActionChain = ActionChainFactory.getActionChain(user, label);
        assertNotNull(retrievedActionChain);
        assertEquals(label, retrievedActionChain.getLabel());
        assertEquals(user, retrievedActionChain.getUser());

        retrievedActionChain = ActionChainFactory.getActionChain(user, actionChain.getId());
        assertNotNull(retrievedActionChain);
        assertEquals(label, retrievedActionChain.getLabel());
        assertEquals(user, retrievedActionChain.getUser());
    }

    /**
     * Tests delete().
     */
    @Test
    public void testDelete() {
        String label = TestUtils.randomString();
        ActionChain actionChain = ActionChainFactory.createActionChain(label, user);
        assertNotNull(actionChain);

        ActionChainFactory.delete(actionChain);

        assertDeleted(actionChain);
    }

    /**
     * Tests getActionChains().
     */
    @Test
    public void testGetActionChains() {
        int previousSize = ActionChainFactory.getActionChains(user).size();

        ActionChainFactory.createActionChain(TestUtils.randomString(), user);
        ActionChainFactory.createActionChain(TestUtils.randomString(), user);
        ActionChainFactory.createActionChain(TestUtils.randomString(), user);

        assertEquals(previousSize + 3, ActionChainFactory.getActionChains(user).size());
    }


    /**
     * Tests getActionChainsByServer().
     * @throws Exception if something bad happens
     */
    @Test
    public void testGetActionChainsByServer() throws Exception {
        ActionChain actionChain1 = ActionChainFactory.createActionChain(TestUtils.randomString(), user);
        ActionChain actionChain2 = ActionChainFactory.createActionChain(TestUtils.randomString(), user);
        ActionChain actionChain3 = ActionChainFactory.createActionChain(TestUtils.randomString(), user);

        Action action1 = ActionFactory.createAction(ActionFactory.TYPE_ERRATA);
        action1.setOrg(user.getOrg());
        Server server1 = ServerFactoryTest.createTestServer(user);

        ActionChainEntry entry1 = ActionChainFactory.queueActionChainEntry(action1,
            actionChain1, server1);

        Action action2 = ActionFactory.createAction(ActionFactory.TYPE_ERRATA);
        action2.setOrg(user.getOrg());

        ActionChainEntry entry2 = ActionChainFactory.queueActionChainEntry(action2,
            actionChain2, server1);

        Action action3 = ActionFactory.createAction(ActionFactory.TYPE_ERRATA);
        action3.setOrg(user.getOrg());
        Server server2 = ServerFactoryTest.createTestServer(user);

        ActionChainEntry entry3 = ActionChainFactory.queueActionChainEntry(action3,
            actionChain3, server2);

        ActionChainFactory.schedule(actionChain1, new Date());
        ActionChainFactory.schedule(actionChain2, new Date());
        ActionChainFactory.schedule(actionChain3, new Date());

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        List<ActionChain> list1 = ActionChainFactory.getActionChainsByServer(server1);
        assertEquals(2, list1.size());
        assertContains(list1, actionChain1);
        assertContains(list1, actionChain2);


        List<ActionChain> list2 = ActionChainFactory.getActionChainsByServer(server2);
        assertEquals(1, list2.size());
        assertContains(list2, actionChain3);
    }

    /**
     * Tests getOrCreateActionChain().
     */
    @Test
    public void testGetOrCreateActionChain() {
        String label = TestUtils.randomString();
        ActionChain actionChain = ActionChainFactory.getActionChain(user, label);
        assertNull(actionChain);

        ActionChain newActionChain = ActionChainFactory.getOrCreateActionChain(label, user);
        assertNotNull(newActionChain);

        ActionChain retrievedActionChain = ActionChainFactory.getActionChain(user, label);
        assertNotNull(retrievedActionChain);
    }

    /**
     * Tests queueActionChainEntry().
     * @throws Exception if something bad happens
     */
    @Test
    public void testQueueActionChainEntry() throws Exception {
        String label = TestUtils.randomString();
        ActionChain actionChain = ActionChainFactory.createActionChain(label, user);
        Action action = ActionFactory.createAction(ActionFactory.TYPE_ERRATA);
        action.setOrg(user.getOrg());
        Server server = ServerFactoryTest.createTestServer(user);

        assertEquals(0, actionChain.getEntries().size());

        ActionChainEntry entry = ActionChainFactory.queueActionChainEntry(action,
            actionChain, server);
        assertNotNull(entry);
        assertEquals(0, entry.getSortOrder().intValue());

        // test that entries are correct after reload()
        HibernateFactory.reload(actionChain);
        assertEquals(1, actionChain.getEntries().size());

        ActionChainEntry secondEntry = ActionChainFactory.queueActionChainEntry(action,
            actionChain, server);
        assertNotNull(secondEntry);
        assertEquals(1, secondEntry.getSortOrder().intValue());

        // test that entries are correct after flush()
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();
        assertEquals(2, actionChain.getEntries().size());

        ActionChain secondActionChain = ActionChainFactory.createActionChain(
            TestUtils.randomString(), user);
        ActionChainEntry thirdEntry = ActionChainFactory.queueActionChainEntry(action,
            secondActionChain, server);
        assertNotNull(thirdEntry);
        assertEquals(0, thirdEntry.getSortOrder().intValue());
    }

    /**
     * Tests testGetActionChainEntry().
     * @throws Exception if something bad happens
     */
    @Test
    public void testGetActionChainEntry() throws Exception {
        String label = TestUtils.randomString();
        ActionChain actionChain = ActionChainFactory.createActionChain(label, user);
        Action action = ActionFactory.createAction(ActionFactory.TYPE_ERRATA);
        action.setOrg(user.getOrg());
        ActionChainEntry entry = ActionChainFactory.queueActionChainEntry(action,
            actionChain, ServerFactoryTest.createTestServer(user), 0);

        HibernateFactory.getSession().flush();

        ActionChainEntry retrievedEntry = ActionChainFactory.getActionChainEntry(user,
            entry.getId());
        assertEquals(entry.getServerId(), retrievedEntry.getServerId());
        assertEquals(entry.getSortOrder(), retrievedEntry.getSortOrder());
    }

    /**
     * Tests getActionChainEntryGroups().
     * @throws Exception if something bad happens
     */
    @Test
    public void testGetActionChainEntryGroups() throws Exception {
        String label = TestUtils.randomString();
        ActionChain actionChain = ActionChainFactory.createActionChain(label, user);
        for (int i = 0; i < 5; i++) {
            Action action = ActionFactory.createAction(ActionFactory.TYPE_ERRATA);
            action.setOrg(user.getOrg());
            ActionChainFactory.queueActionChainEntry(action, actionChain,
                ServerFactoryTest.createTestServer(user), 0);
        }
        for (int i = 5; i < 10; i++) {
            Action action = ActionFactory.createAction(ActionFactory.TYPE_PACKAGES_UPDATE);
            action.setOrg(user.getOrg());
            ActionChainFactory.queueActionChainEntry(action, actionChain,
                ServerFactoryTest.createTestServer(user), 1);
        }

        List<ActionChainEntryGroup> result = ActionChainFactory
            .getActionChainEntryGroups(actionChain);
        ActionChainEntryGroup secondGroup = result.get(0);
        assertEquals(ActionFactory.TYPE_ERRATA.getLabel(),
            secondGroup.getActionTypeLabel());
        assertEquals((Integer) 0, secondGroup.getSortOrder());
        assertEquals((Long) 5L, secondGroup.getSystemCount());

        ActionChainEntryGroup firstGroup = result.get(1);
        assertEquals(ActionFactory.TYPE_PACKAGES_UPDATE.getLabel(),
            firstGroup.getActionTypeLabel());
        assertEquals((Integer) 1, firstGroup.getSortOrder());
        assertEquals((Long) 5L, firstGroup.getSystemCount());
    }

    private List<Integer> getOrders(Set<ActionChainEntry> entries) {
        List<Integer> orders = new ArrayList<>();
        for (ActionChainEntry entry : entries) {
            orders.add(entry.getSortOrder());
        }
        Collections.sort(orders);
        return orders;
    }

    @Test
    public void testRemoveActionChainEntrySortGaps() throws Exception {

        ActionChain actionChain =
                ActionChainFactory.createActionChain(TestUtils.randomString(), user);
        Action action;
        for (int i = 0; i < 2; i++) {
            action = ActionFactory.createAction(ActionFactory.TYPE_ERRATA);
            action.setOrg(user.getOrg());
            ActionChainFactory.queueActionChainEntry(action, actionChain,
                ServerFactoryTest.createTestServer(user), 0);
            TestUtils.saveAndFlush(action);
        }

        for (int i = 0; i < 2; i++) {
            action = ActionFactory.createAction(ActionFactory.TYPE_PACKAGES_UPDATE);
            action.setOrg(user.getOrg());
            ActionChainFactory.queueActionChainEntry(action, actionChain,
                ServerFactoryTest.createTestServer(user), 2);
            TestUtils.saveAndFlush(action);
        }

        TestUtils.saveAndFlush(actionChain);
        ActionChainFactory.removeActionChainEntrySortGaps(actionChain, 1);
        TestUtils.saveAndReload(actionChain);

        List<Integer> result = new ArrayList<>();
        result.add(0);
        result.add(0);
        result.add(1);
        result.add(1);
        assertEquals(result, getOrders(actionChain.getEntries()));
    }

    @Test
    public void testRemoveActionChainEntry() throws Exception {

        ActionChain actionChain =
                ActionChainFactory.createActionChain(TestUtils.randomString(), user);
        Action action;
        for (int i = 0; i < 2; i++) {
            action = ActionFactory.createAction(ActionFactory.TYPE_ERRATA);
             action.setOrg(user.getOrg());
            TestUtils.saveAndFlush(action);
            ActionChainFactory.queueActionChainEntry(action, actionChain,
                ServerFactoryTest.createTestServer(user), 0);
        }

        action = ActionFactory.createAction(ActionFactory.TYPE_ERRATA);
        action.setOrg(user.getOrg());
        TestUtils.saveAndFlush(action);
        ActionChainEntry toRemove =
                ActionChainFactory.queueActionChainEntry(action, actionChain,
                        ServerFactoryTest.createTestServer(user), 1);

        for (int i = 0; i < 2; i++) {
            action = ActionFactory.createAction(ActionFactory.TYPE_PACKAGES_UPDATE);
            action.setOrg(user.getOrg());
            TestUtils.saveAndFlush(action);
            ActionChainFactory.queueActionChainEntry(action, actionChain,
                ServerFactoryTest.createTestServer(user), 2);
        }

        for (ActionChainEntry entry : actionChain.getEntries()) {
            System.out.println(entry + " " + entry.hashCode());
        }

        ActionChainFactory.removeActionChainEntry(actionChain, toRemove);

        System.out.println(toRemove + " ** " + toRemove.hashCode());
        for (ActionChainEntry entry : actionChain.getEntries()) {
            System.out.println(entry + " " + entry.hashCode());
        }

        List<Integer> result = new ArrayList<>();
        result.add(0);
        result.add(0);
        result.add(1);
        result.add(1);
        assertEquals(4, actionChain.getEntries().size());
        assertEquals(result, getOrders(actionChain.getEntries()));
    }

    /**
     * Test getActionChainEntries().
     * @throws Exception if something bad happens
     */
    @Test
    public void testGetActionChainEntries() throws Exception {
        ActionChain actionChain = ActionChainFactory.createActionChain(
            TestUtils.randomString(), user);
        for (int i = 0; i < 10; i++) {
            Action action = ActionFactory.createAction(ActionFactory.TYPE_ERRATA);
            action.setOrg(user.getOrg());
            ActionChainFactory.queueActionChainEntry(action, actionChain,
                ServerFactoryTest.createTestServer(user), i % 2);
        }

        List<ActionChainEntry> entries = ActionChainFactory.getActionChainEntries(
            actionChain, 0);
        assertEquals(5, entries.size());
        for (ActionChainEntry entry : entries) {
            assertEquals(actionChain.getId(), entry.getActionChainId());
            assertEquals((Integer) 0, entry.getSortOrder());
        }
    }

    /**
     * Tests schedule().
     * @throws Exception if something bad happens
     */
    @Test
    public void testSchedule() throws Exception {
        String label = TestUtils.randomString();
        ActionChain actionChain = ActionChainFactory.createActionChain(label, user);
        Server server1 = ServerFactoryTest.createTestServer(user);
        Server server2 = ServerFactoryTest.createTestServer(user);
        Map<Long, Integer> sortOrders = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            Action action = ActionFactory.createAction(ActionFactory.TYPE_ERRATA);
            action.setOrg(user.getOrg());
            ActionChainFactory.queueActionChainEntry(action, actionChain, server1, i);
            TestUtils.saveAndFlush(action);
            sortOrders.put(action.getId(), i);
            if (i % 2 == 0) {
                action = ActionFactory.createAction(ActionFactory.TYPE_ERRATA);
                action.setOrg(user.getOrg());
                ActionChainFactory.queueActionChainEntry(action, actionChain, server2, i);
                TestUtils.saveAndFlush(action);
                sortOrders.put(action.getId(), i);
            }
        }

        ActionChainFactory.schedule(actionChain, new Date());

        // check actions are scheduled in correct order
        for (ActionChainEntry entry : actionChain.getEntries()) {
            Action action = entry.getAction();
            Action prerequisite = action.getPrerequisite();
            if (prerequisite != null) {
                assertTrue(sortOrders.get(action.getId()) > sortOrders.get(prerequisite
                    .getId()));
            }
        }

        // check ServerAction objects have been created
        for (ActionChainEntry entry : actionChain.getEntries()) {
            assertNotEmpty(entry.getAction().getServerActions());
        }
    }

    /**
     * Tests that actionchains are only accessible to the user that created them
     * @throws Exception if something bad happens
     */
    @Test
    public void testPermissions() throws Exception {
        Org otherOrg = UserTestUtils.createNewOrgFull("OtherOrg");
        User other = UserTestUtils.createUser("otherAdmin", otherOrg.getId());

        // Create the thing
        ActionChain ac = ActionChainFactory.getOrCreateActionChain("chain1", user);
        assertNotNull(ac);
        Long acId = ac.getId();

        // Can we find our own thing?
        ac = ActionChainFactory.getActionChain(user, "chain1");
        assertNotNull(ac);

        // Can someone else find our thing by-label?
        ac = ActionChainFactory.getActionChain(other, "chain1");
        assertNull(ac);

        // Can someone else find our thing by-id?
        try {
            ac = ActionChainFactory.getActionChain(other, acId);
        }
        catch (ObjectNotFoundException onfe) {
            return;
        }
        catch (Throwable t) {
            fail();
        }

        Action action = ActionFactory.createAction(ActionFactory.TYPE_ERRATA);
        action.setOrg(user.getOrg());
        Server server = ServerFactoryTest.createTestServer(user);
        ac = ActionChainFactory.getActionChain(user, "chain1");
        ActionChainEntry entry = ActionChainFactory.queueActionChainEntry(action,
                        ac, server);

        ActionChainEntry ace = ActionChainFactory.getActionChainEntry(user, entry.getId());
        assertNotNull(ace);

        ace = ActionChainFactory.getActionChainEntry(other, entry.getId());
        assertNull(ace);
    }

    /**
     * Checks that an Action Chain does not exist anymore.
     * @param actionChain the Action Chain to check
     */
    public static void assertDeleted(ActionChain actionChain) {
        try {
            ActionChainFactory.getActionChain(actionChain.getUser(),
                            actionChain.getId());
            fail();
        }
        catch (ObjectNotFoundException e) {
            // correct
        }
    }
}
