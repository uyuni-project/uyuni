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

import com.redhat.rhn.domain.action.cluster.test.ClusterActionTest;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.suse.manager.clusters.ClusterFactory;
import com.suse.manager.model.clusters.Cluster;

import java.util.List;

public class ClusterFactoryTest extends BaseTestCaseWithUser {

    public void testFindAllClusters() throws Exception {
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        Cluster cluster1 = ClusterActionTest.createTestCluster(user, server);
        Cluster cluster2 = ClusterActionTest.createTestCluster(user, server);

        List<Cluster> clusters = ClusterFactory.findClustersByOrg(user.getOrg().getId());
        assertEquals(2, clusters.size());
    }

}
