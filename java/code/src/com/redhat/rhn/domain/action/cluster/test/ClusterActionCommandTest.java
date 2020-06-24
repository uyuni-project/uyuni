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
import com.redhat.rhn.domain.action.cluster.ClusterActionCommand;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.suse.manager.model.clusters.Cluster;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;

import java.util.Date;
import java.util.Optional;

public class ClusterActionCommandTest extends JMockBaseTestCaseWithUser {

    private TaskomaticApi taskomaticMock;

    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ClassImposteriser.INSTANCE);
        taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);
        ClusterActionCommand.setTaskomaticApi(taskomaticMock);
        context().checking(new Expectations() {{
            ignoring(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
        }});

    }

    public void testScheduleCommandSimple() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);

        Cluster cluster = ClusterActionTest.createTestCluster(user, minion);

        this.user.addPermanentRole(RoleFactory.ORG_ADMIN);

        ClusterActionCommand testCommand =
                new ClusterActionCommand(Optional.of(user), user.getOrg(),
                        new Date(),
                        null,
                        ActionFactory.TYPE_CLUSTER_GROUP_REFRESH_NODES,
                        minion,
                        cluster,
                        cluster.getGroup().getName(),
                        null);
        testCommand.store();

        assertNotNull(testCommand.getAction());
        assertNotNull(testCommand.getAction().getId());

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        Action action = ActionFactory.lookupById(testCommand.getAction().getId());
        assertEquals(ActionFactory.TYPE_CLUSTER_GROUP_REFRESH_NODES, action.getActionType());
        assertEquals(1, action.getServerActions().size());
        assertEquals(minion.getId(), action.getServerActions().iterator().next().getServer().getId());
    }


}
