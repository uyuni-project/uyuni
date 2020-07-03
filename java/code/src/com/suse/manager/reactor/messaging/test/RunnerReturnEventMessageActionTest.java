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

package com.suse.manager.reactor.messaging.test;

import com.google.gson.reflect.TypeToken;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionSaltRunnerJob;
import com.redhat.rhn.domain.action.cluster.ClusterJoinNodeAction;
import com.redhat.rhn.domain.action.cluster.ClusterRemoveNodeAction;
import com.redhat.rhn.domain.action.cluster.test.ClusterActionTest;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;
import com.suse.manager.model.clusters.Cluster;
import com.suse.manager.reactor.messaging.JobReturnEventMessage;
import com.suse.manager.reactor.messaging.JobReturnEventMessageAction;
import com.suse.manager.reactor.messaging.RunnerReturnEventMessage;
import com.suse.manager.reactor.messaging.RunnerReturnEventMessageAction;
import com.suse.salt.netapi.datatypes.Event;
import com.suse.salt.netapi.event.JobReturnEvent;
import com.suse.salt.netapi.event.RunnerReturnEvent;
import com.suse.salt.netapi.parser.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.jmock.lib.legacy.ClassImposteriser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RunnerReturnEventMessageActionTest extends JMockBaseTestCaseWithUser {

    public static final JsonParser<Event> EVENTS =
            new JsonParser<>(new TypeToken<Event>(){});

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ClassImposteriser.INSTANCE);


    }

    public void testClusterRemoveNodes() throws Exception {
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        Cluster cluster = ClusterActionTest.createTestCluster(user, server);

        ClusterRemoveNodeAction clusterAction = new ClusterRemoveNodeAction();
        clusterAction.setActionType(ActionFactory.TYPE_CLUSTER_REMOVE_NODE);
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
        job1.setJid("20200702152745552502");
        job1.setStatus(ActionFactory.STATUS_QUEUED);

        clusterAction.getRunnerJobs().add(job1);

        ActionFactory.save(clusterAction);

        // Setup an event message from file contents
        Optional<RunnerReturnEvent> event = RunnerReturnEvent.parse(
                getRunnerReturnEvent("runner.state.orchestrate.cluster.removenodes.json",
                        Collections.singletonMap("$management-node", cluster.getManagementNode().getMinionId())
                        ));
        RunnerReturnEventMessage message = new RunnerReturnEventMessage(event.get());

        // Process the event message
        RunnerReturnEventMessageAction messageAction = new RunnerReturnEventMessageAction();
        messageAction.execute(message);

        assertEquals(ActionFactory.STATUS_COMPLETED, job1.getStatus());
        assertEquals(Long.valueOf(0L), job1.getResultCode());
        assertEquals("{\"dev-min-caasp-worker-3.lan\":{\"retcode\":0,\"stdout\":\"[remove-node] removing worker node dev-min-caasp-worker-3.lan (drain timeout: 0s)\\n[remove-node] failed disarming kubelet: failed waiting for job caasp-kubelet-disarm-a45f8de6a2550bba476bccbc958f59e32fd37eca; node could be down, continuing with node removal...\\n[remove-node] node dev-min-caasp-worker-3.lan successfully removed from the cluster\\n\",\"stderr\":\"\",\"success\":true}}", job1.getResultMsg());

    }

    private Event getRunnerReturnEvent(String file) throws Exception  {
        return getRunnerReturnEvent(file, Collections.emptyMap());
    }

    private Event getRunnerReturnEvent(String filename, Map<String, String> placeholders) throws Exception {
        Path path = new File(TestUtils.findTestData(
                "/com/suse/manager/reactor/messaging/test/" + filename).getPath()).toPath();
        String eventString = Files.lines(path)
                .collect(Collectors.joining("\n"));
//                .replaceAll("$management-node", "\"suma-action-id\": " + actionId);
        if (placeholders != null) {
            for (Map.Entry<String, String> entries : placeholders.entrySet()) {
                String placeholder = entries.getKey();
                String value = entries.getValue();
                eventString = StringUtils.replace(eventString, placeholder, value);
            }
        }
        return EVENTS.parse(eventString);
    }

    // TODO move to ActionFactoryTest
    public void testLookupSaltRunnerJobByJid() throws Exception{
        User user = UserTestUtils.createUser("testUser",
                UserTestUtils .createOrg("testOrg" + this.getClass().getSimpleName()));

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

        Optional<ActionSaltRunnerJob> runnerJob = ActionFactory.lookupSaltRunnerJobByJid("111111111");

        assertTrue(runnerJob.isPresent());
        assertEquals("111111111", runnerJob.get().getJid());
    }

}
