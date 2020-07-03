/**
 * Copyright (c) 2020 SUSE LLC
 * <p>
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 * <p>
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.manager.clusters.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionSaltRunnerJob;
import com.redhat.rhn.domain.action.cluster.ClusterJoinNodeAction;
import com.redhat.rhn.domain.action.cluster.test.ClusterActionTest;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.suse.manager.clusters.ClusterFactory;
import com.suse.manager.model.clusters.Cluster;

import java.util.Date;
import java.util.List;

public class ClusterFactoryTest extends BaseTestCaseWithUser {

    public void testFindAllClusters() throws Exception {
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        Cluster cluster1 = ClusterActionTest.createTestCluster(user, server);
        Cluster cluster2 = ClusterActionTest.createTestCluster(user, server);

        List<Cluster> clusters = ClusterFactory.findClustersByOrg(user.getOrg().getId());
        assertEquals(2, clusters.size());
    }

    public void testClusterActionTest() throws Exception {
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        Cluster cluster = ClusterActionTest.createTestCluster(user, server);

        ClusterJoinNodeAction clusterAction = new ClusterJoinNodeAction();
        clusterAction.setActionType(ActionFactory.TYPE_CLUSTER_JOIN_NODE);
        clusterAction.setName("test cluster action");
        clusterAction.setOrg(user.getOrg());
        clusterAction.setSchedulerUser(user);
        clusterAction.setEarliestAction(new Date());
        clusterAction.setCluster(cluster);
        clusterAction.setJsonParams("{}");
        clusterAction.setVersion(2L);
        clusterAction.setArchived(0L);

        ActionSaltRunnerJob job1 = new ActionSaltRunnerJob();
        job1.setAction(clusterAction);
        job1.setJid("111111111");
        job1.setStatus(ActionFactory.STATUS_COMPLETED);

        clusterAction.getRunnerJobs().add(job1);

        ActionSaltRunnerJob job2 = new ActionSaltRunnerJob();
        job2.setAction(clusterAction);
        job2.setJid("222222222");
        job2.setStatus(ActionFactory.STATUS_COMPLETED);

        clusterAction.getRunnerJobs().add(job2);

        ActionFactory.save(clusterAction);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        clusterAction = (ClusterJoinNodeAction)ActionFactory.lookupById(clusterAction.getId());
        assertEquals(2, clusterAction.getRunnerJobs().size());

        ActionSaltRunnerJob actionJob1 = clusterAction.getRunnerJobs().stream().filter(job -> job.getJid().equals("111111111")).findFirst().get();
        ActionSaltRunnerJob actionJob2 = clusterAction.getRunnerJobs().stream().filter(job -> job.getJid().equals("222222222")).findFirst().get();

        assertEquals(job1.getStatus(), actionJob1.getStatus());

        assertEquals(job2.getStatus(), actionJob2.getStatus());
    }

}
