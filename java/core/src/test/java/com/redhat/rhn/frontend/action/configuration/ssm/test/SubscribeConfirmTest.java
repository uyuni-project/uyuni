/**
 * Copyright (c) 2017 SUSE LLC
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

package com.redhat.rhn.frontend.action.configuration.ssm.test;

import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.rhnset.RhnSetFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.frontend.action.configuration.ssm.SubscribeConfirm;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.testing.ConfigTestUtils;
import com.redhat.rhn.testing.RhnMockStrutsTestCase;
import com.redhat.rhn.testing.ServerTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SubscribeConfirmTest extends RhnMockStrutsTestCase {

    public void testRequests() {
        // Assert invalid request
        setRequestPathInfo("/systems/ssm/config/SubscribeConfirm");
        actionPerform();
        assertBadParamException();

        // Assert initial page request
        addRequestParameter(SubscribeConfirm.POSITION, SubscribeConfirm.LOWEST);
        actionPerform();
        verifyForward(RhnHelper.DEFAULT_FORWARD);
    }

    public void testExecuteLowestPosition() throws Exception {
        /*
         * Add servers and channels
         *
         * Initial setup:
         * Server1 subscriptions in order: 1, 2
         * Server2 subscriptions in order: 3, 2
         * Server3 subscriptions in order: -
         */

        Server server1 = ServerTestUtils.createTestSystem(user);
        Server server2 = ServerTestUtils.createTestSystem(user);
        Server server3 = ServerTestUtils.createTestSystem(user);

        ConfigChannel channel1 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 1", "cfg-channel-1");
        ConfigChannel channel2 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 2", "cfg-channel-2");
        ConfigChannel channel3 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 3", "cfg-channel-3");
        ConfigChannel channel4 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 4", "cfg-channel-4");

        List<ConfigChannel> srv1Channels = new ArrayList<>();
        srv1Channels.add(channel1);
        srv1Channels.add(channel2);

        List<ConfigChannel> srv2Channels = new ArrayList<>();
        srv2Channels.add(channel3);
        srv2Channels.add(channel2);

        server1.subscribeConfigChannels(srv1Channels, user);
        server2.subscribeConfigChannels(srv2Channels, user);
        ServerFactory.save(server1);
        ServerFactory.save(server2);
        ServerFactory.save(server3);

        // Add systems to SSM
        RhnSet ssm = RhnSetDecl.SYSTEMS.get(user);
        ssm.addElement(server1.getId());
        ssm.addElement(server2.getId());
        ssm.addElement(server3.getId());
        RhnSetFactory.save(ssm);

        // Select channels 2, 3, 4
        RhnSet channels = RhnSetDecl.CONFIG_CHANNELS.get(user);
        channels.addElement(channel2.getId());
        channels.addElement(channel3.getId());
        channels.addElement(channel4.getId());
        RhnSetFactory.save(channels);

        // Set ranking: 2, 4, 3, 1
        RhnSet channelRanking = RhnSetDecl.CONFIG_CHANNELS_RANKING.get(user);
        channelRanking.addElement(channel2.getId(), 0L);
        channelRanking.addElement(channel4.getId(), 1L);
        channelRanking.addElement(channel3.getId(), 2L);
        channelRanking.addElement(channel1.getId(), 3L);
        RhnSetFactory.save(channelRanking);

        // Send confirm request
        setRequestPathInfo("/systems/ssm/config/SubscribeConfirm");
        addDispatchCall("ssm.config.subscribeconfirm.jsp.confirm");
        addRequestParameter(SubscribeConfirm.POSITION, SubscribeConfirm.LOWEST);
        actionPerform();
        verifyForward("success");

        /*
         * Expected channels by server for LOWEST, in order:
         * Server 1: 1, 2, 4, 3
         * Server 2: 3, 2, 4
         * Server 3: 2. 4. 3
         */
        List<ConfigChannel> srv1Expected = Arrays.asList(channel1, channel2, channel4, channel3);
        List<ConfigChannel> srv2Expected = Arrays.asList(channel3, channel2, channel4);
        List<ConfigChannel> srv3Expected = Arrays.asList(channel2, channel4, channel3);

        // Assert channel counts
        assertEquals(srv1Expected.size(), server1.getConfigChannelCount());
        assertEquals(srv2Expected.size(), server2.getConfigChannelCount());
        assertEquals(srv3Expected.size(), server3.getConfigChannelCount());

        // Assert channel order
        assertEquals(srv1Expected, server1.getConfigChannelList());
        assertEquals(srv2Expected, server2.getConfigChannelList());
        assertEquals(srv3Expected, server3.getConfigChannelList());
    }

    public void testExecuteHighestPosition() throws Exception {
        /*
         * Add servers and channels
         *
         * Initial setup:
         * Server1 subscriptions in order: 1, 2
         * Server2 subscriptions in order: 3, 2
         * Server3 subscriptions in order: -
         */

        Server server1 = ServerTestUtils.createTestSystem(user);
        Server server2 = ServerTestUtils.createTestSystem(user);
        Server server3 = ServerTestUtils.createTestSystem(user);

        ConfigChannel channel1 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 1", "cfg-channel-1");
        ConfigChannel channel2 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 2", "cfg-channel-2");
        ConfigChannel channel3 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 3", "cfg-channel-3");
        ConfigChannel channel4 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 4", "cfg-channel-4");

        List<ConfigChannel> srv1Channels = new ArrayList<>();
        srv1Channels.add(channel1);
        srv1Channels.add(channel2);

        List<ConfigChannel> srv2Channels = new ArrayList<>();
        srv2Channels.add(channel3);
        srv2Channels.add(channel2);

        server1.subscribeConfigChannels(srv1Channels, user);
        server2.subscribeConfigChannels(srv2Channels, user);
        ServerFactory.save(server1);
        ServerFactory.save(server2);
        ServerFactory.save(server3);

        // Add systems to SSM
        RhnSet ssm = RhnSetDecl.SYSTEMS.get(user);
        ssm.addElement(server1.getId());
        ssm.addElement(server2.getId());
        ssm.addElement(server3.getId());
        RhnSetFactory.save(ssm);

        // Select channels 2, 3, 4
        RhnSet channels = RhnSetDecl.CONFIG_CHANNELS.get(user);
        channels.addElement(channel2.getId());
        channels.addElement(channel3.getId());
        channels.addElement(channel4.getId());
        RhnSetFactory.save(channels);

        // Set ranking: 2, 4, 3, 1
        RhnSet channelRanking = RhnSetDecl.CONFIG_CHANNELS_RANKING.get(user);
        channelRanking.addElement(channel2.getId(), 0L);
        channelRanking.addElement(channel4.getId(), 1L);
        channelRanking.addElement(channel3.getId(), 2L);
        channelRanking.addElement(channel1.getId(), 3L);
        RhnSetFactory.save(channelRanking);

        // Send confirm request
        setRequestPathInfo("/systems/ssm/config/SubscribeConfirm");
        addDispatchCall("ssm.config.subscribeconfirm.jsp.confirm");
        addRequestParameter(SubscribeConfirm.POSITION, SubscribeConfirm.HIGHEST);
        actionPerform();
        verifyForward("success");

        /*
         * Expected channels by server for HIGHEST, in order:
         * Server 1: 4, 3, 1, 2
         * Server 2: 4, 3, 2
         * Server 3: 2, 4, 3
         */
        List<ConfigChannel> srv1Expected = Arrays.asList(channel4, channel3, channel1, channel2);
        List<ConfigChannel> srv2Expected = Arrays.asList(channel4, channel3, channel2);
        List<ConfigChannel> srv3Expected = Arrays.asList(channel2, channel4, channel3);

        // Assert channel counts
        assertEquals(srv1Expected.size(), server1.getConfigChannelCount());
        assertEquals(srv2Expected.size(), server2.getConfigChannelCount());
        assertEquals(srv3Expected.size(), server3.getConfigChannelCount());

        // Assert channel order
        assertEquals(srv1Expected, server1.getConfigChannelList());
        assertEquals(srv2Expected, server2.getConfigChannelList());
        assertEquals(srv3Expected, server3.getConfigChannelList());
    }

    public void testExecuteReplacePosition() throws Exception {
        /*
         * Add servers and channels
         *
         * Initial setup:
         * Server1 subscriptions in order: 1, 2
         * Server2 subscriptions in order: 3, 2
         * Server3 subscriptions in order: -
         */

        Server server1 = ServerTestUtils.createTestSystem(user);
        Server server2 = ServerTestUtils.createTestSystem(user);
        Server server3 = ServerTestUtils.createTestSystem(user);

        ConfigChannel channel1 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 1", "cfg-channel-1");
        ConfigChannel channel2 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 2", "cfg-channel-2");
        ConfigChannel channel3 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 3", "cfg-channel-3");
        ConfigChannel channel4 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 4", "cfg-channel-4");

        List<ConfigChannel> srv1Channels = new ArrayList<>();
        srv1Channels.add(channel1);
        srv1Channels.add(channel2);

        List<ConfigChannel> srv2Channels = new ArrayList<>();
        srv2Channels.add(channel3);
        srv2Channels.add(channel2);

        server1.subscribeConfigChannels(srv1Channels, user);
        server2.subscribeConfigChannels(srv2Channels, user);
        ServerFactory.save(server1);
        ServerFactory.save(server2);
        ServerFactory.save(server3);

        // Add systems to SSM
        RhnSet ssm = RhnSetDecl.SYSTEMS.get(user);
        ssm.addElement(server1.getId());
        ssm.addElement(server2.getId());
        ssm.addElement(server3.getId());
        RhnSetFactory.save(ssm);

        // Select channels 2, 3, 4
        RhnSet channels = RhnSetDecl.CONFIG_CHANNELS.get(user);
        channels.addElement(channel2.getId());
        channels.addElement(channel3.getId());
        channels.addElement(channel4.getId());
        RhnSetFactory.save(channels);

        // Set ranking: 2, 4, 3
        RhnSet channelRanking = RhnSetDecl.CONFIG_CHANNELS_RANKING.get(user);
        channelRanking.addElement(channel2.getId(), 0L);
        channelRanking.addElement(channel4.getId(), 1L);
        channelRanking.addElement(channel3.getId(), 2L);
        RhnSetFactory.save(channelRanking);

        // Send confirm request
        setRequestPathInfo("/systems/ssm/config/SubscribeConfirm");
        addDispatchCall("ssm.config.subscribeconfirm.jsp.confirm");
        addRequestParameter(SubscribeConfirm.POSITION, SubscribeConfirm.REPLACE);
        actionPerform();
        verifyForward("success");

        /*
         * Expected channels by server for REPLACE, in order:
         * Server 1: 2, 4, 3
         * Server 2: 2, 4, 3
         * Server 3: 2, 4, 3
         */
        List<ConfigChannel> srv1Expected = Arrays.asList(channel2, channel4, channel3);
        List<ConfigChannel> srv2Expected = Arrays.asList(channel2, channel4, channel3);
        List<ConfigChannel> srv3Expected = Arrays.asList(channel2, channel4, channel3);

        // Assert channel counts
        assertEquals(srv1Expected.size(), server1.getConfigChannelCount());
        assertEquals(srv2Expected.size(), server2.getConfigChannelCount());
        assertEquals(srv3Expected.size(), server3.getConfigChannelCount());

        // Assert channel order
        assertEquals(srv1Expected, server1.getConfigChannelList());
        assertEquals(srv2Expected, server2.getConfigChannelList());
        assertEquals(srv3Expected, server3.getConfigChannelList());
    }
}
