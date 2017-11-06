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

package com.redhat.rhn.domain.server.test;

import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ConfigTestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MinionServer tests
 */
public class MinionServerTest extends BaseTestCaseWithUser {

    public void testConfigChannelSubscriptions() throws Exception {
        Server server = MinionServerFactoryTest.createTestMinionServer(user);
        ConfigChannel channel1 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 1", "cfg-channel-1");

        server.subscribeConfigChannel(channel1, user);
        ServerStateRevision revision1 = StateFactory.latestStateRevision(server).get();

        assertEquals(user, revision1.getCreator());
        assertEquals(1, revision1.getConfigChannels().size());
        assertTrue(revision1.getConfigChannels().stream().anyMatch(channel1::equals));

        ConfigChannel channel2 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 2", "cfg-channel-2");
        ConfigChannel channel3 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 3", "cfg-channel-3");

        User other = UserTestUtils.createUser("other", user.getOrg().getId());
        List<ConfigChannel> channelList = new ArrayList<>();
        channelList.add(channel2);
        channelList.add(channel3);

        server.subscribeConfigChannels(channelList, other);
        ServerStateRevision revision2 = StateFactory.latestStateRevision(server).get();

        // Assert channel order
        channelList.add(0, channel1);
        assertEquals(channelList, server.getConfigChannels());

        assertEquals(other, revision2.getCreator());
        assertEquals(3, revision2.getConfigChannels().size());
        assertTrue(revision2.getConfigChannels().stream().anyMatch(channel1::equals));
        assertTrue(revision2.getConfigChannels().stream().anyMatch(channel2::equals));
        assertTrue(revision2.getConfigChannels().stream().anyMatch(channel3::equals));

        channelList.clear();
        channelList.add(channel1);
        channelList.add(channel2);
        server.setConfigChannels(channelList, user);
        ServerStateRevision revision3 = StateFactory.latestStateRevision(server).get();

        assertEquals(user, revision3.getCreator());
        assertEquals(2, revision3.getConfigChannels().size());
        assertTrue(revision2.getConfigChannels().stream().anyMatch(channel1::equals));
        assertTrue(revision2.getConfigChannels().stream().anyMatch(channel2::equals));

        server.unsubscribeConfigChannels(Collections.singletonList(channel2), user);
        ServerStateRevision revision4 = StateFactory.latestStateRevision(server).get();

        assertEquals(1, revision4.getConfigChannels().size());
        assertTrue(revision2.getConfigChannels().stream().anyMatch(channel1::equals));
    }
}
