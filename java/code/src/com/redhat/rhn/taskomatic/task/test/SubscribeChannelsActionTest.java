/**
 * Copyright (c) 2018 SUSE LLC
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

package com.redhat.rhn.taskomatic.task.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionStatus;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsAction;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsActionDetails;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.taskomatic.task.MinionActionExecutor;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.suse.manager.webui.services.SaltServerActionService;
import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.redhat.rhn.domain.action.ActionFactory.STATUS_QUEUED;

/**
 * Test for SubscribeChannelsAction.
 */
public class SubscribeChannelsActionTest extends JMockBaseTestCaseWithUser {


    @Override
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    public void testSubscribeChannels_minions() throws Exception {
        Channel base = ChannelFactoryTest.createBaseChannel(user);
        Channel ch1 = ChannelFactoryTest.createTestChannel(user.getOrg());
        ch1.setParentChannel(base);
        TestUtils.saveAndFlush(ch1);
        Channel ch2 = ChannelFactoryTest.createTestChannel(user.getOrg());
        ch2.setParentChannel(base);
        TestUtils.saveAndFlush(ch2);
        ServerAction serverAction;
        long serverId = 0;
        {
            final MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
            serverId = server.getId();
            SubscribeChannelsAction action = (SubscribeChannelsAction) ActionFactoryTest
                    .createAction(user, ActionFactory.TYPE_SUBSCRIBE_CHANNELS);
            serverAction = createChildServerAction(action, server, STATUS_QUEUED);

            SubscribeChannelsActionDetails details = new SubscribeChannelsActionDetails();
            details.setBaseChannel(base);
            details.setChannels(Arrays.asList(ch1, ch2).stream().collect(Collectors.toSet()));
            action.setDetails(details);
            details.setParentAction(action);
            HibernateFactory.getSession().save(details);

            SaltServerActionService saltServerActionService = mock(SaltServerActionService.class);
            JobExecutionContext ctx = mock(JobExecutionContext.class);
            JobDetail jobDetail = mock(JobDetail.class);
            context().checking(new Expectations() {{
                oneOf(saltServerActionService).execute(with(any(Action.class)), with(false), with(false), with(Optional.empty()));

                allowing(ctx).getJobDetail();
                will(returnValue(jobDetail));

                JobDataMap dataMap = new JobDataMap();
                dataMap.putAsString("action_id", action.getId());
                dataMap.putAsString("user_id", user.getId());

                allowing(jobDetail).getJobDataMap();
                will(returnValue(dataMap));
            }});

            MinionActionExecutor executor = new MinionActionExecutor();
            executor.setSaltServerActionService(saltServerActionService);
            executor.execute(ctx);

            HibernateFactory.getSession().flush();
            HibernateFactory.getSession().clear();
        }
        {
            MinionServer server = MinionServerFactory.lookupById(serverId).get();
            assertEquals(ActionFactory.STATUS_QUEUED, serverAction.getStatus());
            assertNull(serverAction.getResultCode());
            assertEquals(server, serverAction.getServer());

            assertNull(server.getBaseChannel());
            assertTrue(server.getChannels().isEmpty());
        }
    }

    private ServerAction createChildServerAction(Action action, Server server, ActionStatus status)
            throws Exception {
        ServerAction serverAction = ActionFactoryTest.createServerAction(server, action);
        serverAction.setStatus(status);
        serverAction.setRemainingTries(1L);
        action.setServerActions(Collections.singleton(serverAction));
        return serverAction;
    }

}
