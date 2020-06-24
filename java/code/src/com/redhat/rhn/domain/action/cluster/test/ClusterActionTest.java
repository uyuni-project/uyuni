/**
 * Copyright (c) 2020 SUSE LLC
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

package com.redhat.rhn.domain.action.cluster.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.cluster.ClusterJoinNodeAction;
import com.redhat.rhn.domain.action.cluster.ClusterGroupRefreshNodesAction;
import com.redhat.rhn.domain.action.cluster.ClusterRemoveNodeAction;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerGroupTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.suse.manager.model.clusters.Cluster;

public class ClusterActionTest extends JMockBaseTestCaseWithUser {

    public void testSaveClusterGroupRefreshNodesAction() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);

        Cluster cluster = createTestCluster(user, minion);

        ClusterGroupRefreshNodesAction testAction = (ClusterGroupRefreshNodesAction) ActionFactoryTest.createNewAction(user,
                ActionFactory.TYPE_CLUSTER_GROUP_REFRESH_NODES);
        testAction.setCluster(cluster);

        saveAction(testAction);

        Action action = ActionFactory.lookupById(testAction.getId());
        assertNotNull(action);
        assertTrue(action instanceof ClusterGroupRefreshNodesAction);
        assertEquals(cluster.getId(), ((ClusterGroupRefreshNodesAction)action).getCluster().getId());
        removeAction(action);

        action = ActionFactory.lookupById(testAction.getId());
        assertNull(action);
    }

    public void testSaveClusterJoinNodeAction() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer toJoin1 = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer toJoin2 = MinionServerFactoryTest.createTestMinionServer(user);

        Cluster cluster = createTestCluster(user, minion);
        ClusterJoinNodeAction testAction = (ClusterJoinNodeAction) ActionFactoryTest.createNewAction(user,
                ActionFactory.TYPE_CLUSTER_JOIN_NODE);

        testAction.setCluster(cluster);
        testAction.setJsonParams("{'foo': 'bar'}");
        testAction.getServerIds().add(toJoin1.getId());
        testAction.getServerIds().add(toJoin2.getId());
        saveAction(testAction);

        Action action = ActionFactory.lookupById(testAction.getId());
        assertNotNull(action);
        assertTrue(action instanceof ClusterJoinNodeAction);
        assertEquals(cluster.getId(), ((ClusterJoinNodeAction)action).getCluster().getId());
        assertEquals("{'foo': 'bar'}", ((ClusterJoinNodeAction)action).getJsonParams());
        assertEquals(2, ((ClusterJoinNodeAction) action).getServerIds().size());
        assertTrue(((ClusterJoinNodeAction) action).getServerIds().contains(toJoin1.getId()));
        assertTrue(((ClusterJoinNodeAction) action).getServerIds().contains(toJoin2.getId()));
        removeAction(action);

        action = ActionFactory.lookupById(testAction.getId());
        assertNull(action);
    }

    public void testSaveClusterRemoveNodeAction() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer toRemove = MinionServerFactoryTest.createTestMinionServer(user);

        Cluster cluster = createTestCluster(user, minion);

        ClusterRemoveNodeAction testAction = (ClusterRemoveNodeAction) ActionFactoryTest.createNewAction(user,
                ActionFactory.TYPE_CLUSTER_REMOVE_NODE);
        testAction.setCluster(cluster);
        testAction.getServerIds().add(toRemove.getId());
        testAction.setJsonParams("{}");
        saveAction(testAction);

        Action action = ActionFactory.lookupById(testAction.getId());
        assertNotNull(action);
        assertTrue(action instanceof ClusterRemoveNodeAction);
        assertEquals(cluster.getId(), ((ClusterRemoveNodeAction)action).getCluster().getId());
        assertEquals(1, ((ClusterRemoveNodeAction) action).getServerIds().size());
        assertTrue(((ClusterRemoveNodeAction) action).getServerIds().contains(toRemove.getId()));
        removeAction(action);

        action = ActionFactory.lookupById(testAction.getId());
        assertNull(action);
    }

    private void saveAction(Action action) {
        HibernateFactory.getSession().save(action);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();
    }

    private void removeAction(Action action) {
        HibernateFactory.getSession().remove(action);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();
    }

    public static Cluster createTestCluster(User user, MinionServer managementNode) {
        ManagedServerGroup grp = ServerGroupTestUtils.createManaged(user);

        Cluster cluster = new Cluster();
        cluster.setOrg(user.getOrg());
        cluster.setGroup(grp);
        cluster.setLabel("test-" +  TestUtils.randomString());
        cluster.setName("test-" + TestUtils.randomString());
        cluster.setDescription("desc");
        cluster.setProvider("caasp");
        cluster.setManagementNode(managementNode);

        TestUtils.saveAndReload(cluster);

        return cluster;
    }

}
