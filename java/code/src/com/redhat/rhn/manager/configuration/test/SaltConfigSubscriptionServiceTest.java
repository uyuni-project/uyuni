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

package com.redhat.rhn.manager.configuration.test;

import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.domain.state.StateRevision;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.configuration.SaltConfigSubscriptionService;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ConfigTestUtils;
import com.redhat.rhn.testing.ServerGroupTestUtils;
import com.redhat.rhn.testing.UserTestUtils;
import com.suse.manager.webui.services.ConfigChannelSaltManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SaltConfigSubscriptionServiceTest extends BaseTestCaseWithUser {

    public void testSubscribeChannels() throws Exception {
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        Path slsPath = tmpSaltRoot.resolve("custom").resolve("custom_" + server.getMachineId() + ".sls");
        assertFalse(slsPath.toFile().exists());

        ConfigChannel channel1 = ConfigTestUtils.createConfigChannel(user.getOrg(), "Channel 1", "cfg-channel-1");

        SaltConfigSubscriptionService.subscribeChannels(server, Collections.singletonList(channel1), user);
        StateRevision revision1 = StateFactory.latestStateRevision(server).get();

        assertEquals(user, revision1.getCreator());
        assertEquals(1, revision1.getConfigChannels().size());
        assertTrue(revision1.getConfigChannels().stream().anyMatch(channel1::equals));
        assertTrue(slsPath.toFile().exists());
        assertContains(new String(Files.readAllBytes(slsPath)),
                ConfigChannelSaltManager.getInstance().getChannelStateName(channel1));

        // Test for server groups
        ServerGroup serverGroup = ServerGroupTestUtils.createManaged(user);
        SaltConfigSubscriptionService.subscribeChannels(serverGroup, Collections.singletonList(channel1), user);
        revision1 = StateFactory.latestStateRevision(serverGroup).get();

        assertEquals(user, revision1.getCreator());
        assertEquals(1, revision1.getConfigChannels().size());
        assertTrue(revision1.getConfigChannels().stream().anyMatch(channel1::equals));

        // Test for orgs
        Org org = UserTestUtils.createNewOrgFull("Test Org");
        SaltConfigSubscriptionService.subscribeChannels(org, Collections.singletonList(channel1), user);
        revision1 = StateFactory.latestStateRevision(org).get();

        assertEquals(user, revision1.getCreator());
        assertEquals(1, revision1.getConfigChannels().size());
        assertTrue(revision1.getConfigChannels().stream().anyMatch(channel1::equals));

        ConfigChannel channel2 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 2", "cfg-channel-2");
        ConfigChannel channel3 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 3", "cfg-channel-3");

        // Test revision creator assignment
        User other = UserTestUtils.createUser("other", user.getOrg().getId());
        List<ConfigChannel> channelList = new ArrayList<>();
        channelList.add(channel2);
        channelList.add(channel3);

        SaltConfigSubscriptionService.subscribeChannels(server, channelList, other);
        ServerStateRevision revision2 = StateFactory.latestStateRevision(server).get();

        // Assert channel order
        channelList.add(0, channel1);
        assertEquals(channelList, revision2.getConfigChannels());

        assertEquals(other, revision2.getCreator());
        assertEquals(3, revision2.getConfigChannels().size());
        assertFalse(revision1.equals(revision2));
        assertTrue(revision2.getConfigChannels().stream().anyMatch(channel1::equals));
        assertTrue(revision2.getConfigChannels().stream().anyMatch(channel2::equals));
        assertTrue(revision2.getConfigChannels().stream().anyMatch(channel3::equals));

        // Test subscribe to existing channel
        channelList.clear();
        channelList.add(channel1);
        SaltConfigSubscriptionService.subscribeChannels(server, channelList, user);
        ServerStateRevision revision3 = StateFactory.latestStateRevision(server).get();

        // Should be a new revision with no changes
        assertEquals(user, revision3.getCreator());
        assertEquals(3, revision3.getConfigChannels().size());
        assertTrue(revision2.getConfigChannels().stream().anyMatch(channel1::equals));
        assertTrue(revision2.getConfigChannels().stream().anyMatch(channel2::equals));
        assertTrue(revision2.getConfigChannels().stream().anyMatch(channel3::equals));
    }

    public void testUnsubscribeChannels() throws Exception {
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        ServerGroup serverGroup = ServerGroupTestUtils.createManaged(user);
        Org org = UserTestUtils.createNewOrgFull("Test Org");

        ConfigChannel channel1 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 1", "cfg-channel-1");
        ConfigChannel channel2 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 2", "cfg-channel-2");
        ConfigChannel channel3 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 3", "cfg-channel-3");

        List<ConfigChannel> initialChannels = new ArrayList<>();
        initialChannels.add(channel1);
        initialChannels.add(channel2);
        initialChannels.add(channel3);

        SaltConfigSubscriptionService.subscribeChannels(server, initialChannels, user);
        SaltConfigSubscriptionService.subscribeChannels(serverGroup, initialChannels, user);
        SaltConfigSubscriptionService.subscribeChannels(org, initialChannels, user);

        List<ConfigChannel> channelsToUnsubscribe = new ArrayList<>();
        channelsToUnsubscribe.add(channel1);
        channelsToUnsubscribe.add(channel3);

        // Test for servers
        SaltConfigSubscriptionService.unsubscribeChannels(server, channelsToUnsubscribe, user);
        StateRevision revision1 = StateFactory.latestStateRevision(server).get();

        assertEquals(user, revision1.getCreator());
        assertEquals(1, revision1.getConfigChannels().size());
        assertTrue(revision1.getConfigChannels().stream().anyMatch(channel2::equals));

        // Test for server groups
        SaltConfigSubscriptionService.unsubscribeChannels(serverGroup, channelsToUnsubscribe, user);
        revision1 = StateFactory.latestStateRevision(serverGroup).get();

        assertEquals(user, revision1.getCreator());
        assertEquals(1, revision1.getConfigChannels().size());
        assertTrue(revision1.getConfigChannels().stream().anyMatch(channel2::equals));

        // Test for orgs
        SaltConfigSubscriptionService.unsubscribeChannels(org, channelsToUnsubscribe, user);
        revision1 = StateFactory.latestStateRevision(org).get();

        assertEquals(user, revision1.getCreator());
        assertEquals(1, revision1.getConfigChannels().size());
        assertTrue(revision1.getConfigChannels().stream().anyMatch(channel2::equals));

        // Test revision creator assignment
        User other = UserTestUtils.createUser("other", user.getOrg().getId());
        channelsToUnsubscribe.clear();
        channelsToUnsubscribe.add(channel2);

        SaltConfigSubscriptionService.unsubscribeChannels(server, channelsToUnsubscribe, other);
        ServerStateRevision revision2 = StateFactory.latestStateRevision(server).get();

        assertEquals(other, revision2.getCreator());
        assertEquals(0, revision2.getConfigChannels().size());
        assertFalse(revision1.equals(revision2));

        // Test non-subscribed channels
        channelsToUnsubscribe.clear();
        channelsToUnsubscribe.add(channel1);

        SaltConfigSubscriptionService.unsubscribeChannels(server, channelsToUnsubscribe, user);
        ServerStateRevision revision3 = StateFactory.latestStateRevision(server).get();

        // Should be a new revision with no changes
        assertEquals(user, revision3.getCreator());
        assertEquals(0, revision3.getConfigChannels().size());
    }

    public void testSetConfigChannels() throws Exception {
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        ServerGroup serverGroup = ServerGroupTestUtils.createManaged(user);
        Org org = UserTestUtils.createNewOrgFull("Test Org");

        ConfigChannel channel1 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 1", "cfg-channel-1");
        ConfigChannel channel2 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 2", "cfg-channel-2");
        ConfigChannel channel3 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 3", "cfg-channel-3");

        List<ConfigChannel> channelList = new ArrayList<>();
        channelList.add(channel1);
        channelList.add(channel2);

        // Test for servers
        SaltConfigSubscriptionService.setConfigChannels(server, channelList, user);
        StateRevision revision1 = StateFactory.latestStateRevision(server).get();

        assertEquals(user, revision1.getCreator());
        assertEquals(2, revision1.getConfigChannels().size());
        assertEquals(channelList, revision1.getConfigChannels());

        // Test for server groups
        SaltConfigSubscriptionService.setConfigChannels(serverGroup, channelList, user);
        revision1 = StateFactory.latestStateRevision(serverGroup).get();

        assertEquals(user, revision1.getCreator());
        assertEquals(2, revision1.getConfigChannels().size());
        assertEquals(channelList, revision1.getConfigChannels());

        // Test for orgs
        SaltConfigSubscriptionService.setConfigChannels(org, channelList, user);
        revision1 = StateFactory.latestStateRevision(org).get();

        assertEquals(user, revision1.getCreator());
        assertEquals(2, revision1.getConfigChannels().size());
        assertEquals(channelList, revision1.getConfigChannels());

        // Test revision creator assignment
        User other = UserTestUtils.createUser("other", user.getOrg().getId());
        channelList.clear();
        channelList.add(channel2);
        channelList.add(channel3);

        SaltConfigSubscriptionService.setConfigChannels(server, channelList, other);
        ServerStateRevision revision2 = StateFactory.latestStateRevision(server).get();

        assertEquals(other, revision2.getCreator());
        assertEquals(2, revision2.getConfigChannels().size());
        assertEquals(channelList, revision2.getConfigChannels());
        assertFalse(revision1.equals(revision2));
    }
}
