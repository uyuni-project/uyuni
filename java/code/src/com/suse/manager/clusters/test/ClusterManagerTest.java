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

package com.suse.manager.clusters.test;

import com.google.gson.JsonObject;
import com.redhat.rhn.domain.action.cluster.test.ClusterActionTest;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.manager.formula.FormulaManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.suse.manager.clusters.ClusterManager;
import com.suse.manager.model.clusters.Cluster;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.salt.custom.ClusterUpgradePlanSlsResult;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.event.JobReturnEvent;
import com.suse.utils.Json;
import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ClusterManagerTest extends JMockBaseTestCaseWithUser {

    private SaltService saltServiceMock;
    private FormulaManager formulaManagerMock;

    {
        context().setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }


    @Override
    public void setUp() throws Exception {
        super.setUp();
        saltServiceMock = context().mock(SaltService.class);
        formulaManagerMock = context().mock(FormulaManager.class);
    }

    public void testGetUpgradePlan() throws Exception {

        MinionServer managementNode = MinionServerFactoryTest.createTestMinionServer(user);
        Cluster cluster = ClusterActionTest.createTestCluster(user, managementNode);
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("minion-id", managementNode.getMinionId());

        JobReturnEvent result = Json.GSON.fromJson(new InputStreamReader(getClass()
                        .getResourceAsStream("/com/suse/manager/clusters/test/cluster.upgrade.success.json")),
                JobReturnEvent.class);

        ClusterUpgradePlanSlsResult upgradeResult = Json.GSON.fromJson(result.getData()
                .getResult(JsonObject.class), ClusterUpgradePlanSlsResult.class);

        context().checking(new Expectations() {{
            oneOf(saltServiceMock).callSync(
                with(any(LocalCall.class)),
                with(managementNode.getMinionId()));
            will(returnValue(Optional.of(upgradeResult)));

            oneOf(formulaManagerMock).getClusterFormulaData(with(any(Cluster.class)), with("settings"));
            will(returnValue(Optional.of(new HashMap())));
        }});

        ClusterManager clusterManager = new ClusterManager(saltServiceMock,
                saltServiceMock,
                ServerGroupManager.getInstance(),
                formulaManagerMock);
        Optional<String> plan = clusterManager.getUpgradePlan(cluster);
        assertTrue(plan.isPresent());
        assertEquals("Current Kubernetes cluster version: 1.17.4\n" +
                "Latest Kubernetes version: 1.17.4\n" +
                "\n" +
                "All nodes match the current cluster version: 1.17.4.\n" +
                "\n" +
                "Addon upgrades for 1.17.4:\n" +
                "  - cilium: 1.5.3 (manifest version from 1 to 2)\n" +
                "  - kured: 1.3.0 (manifest version from 2 to 4)\n" +
                "  - psp (manifest version from 1 to 3)\n", plan.get());

    }

}
