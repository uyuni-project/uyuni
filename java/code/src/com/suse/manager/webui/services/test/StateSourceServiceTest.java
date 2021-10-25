/*
 * Copyright (c) 2021 SUSE LLC
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
package com.suse.manager.webui.services.test;

import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigChannelType;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.manager.configuration.SaltConfigurable;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ConfigTestUtils;
import com.redhat.rhn.testing.ServerGroupTestUtils;

import com.suse.manager.webui.services.StateSourceService;
import com.suse.manager.webui.utils.gson.StateSourceDto;

import java.util.Arrays;
import java.util.List;

public class StateSourceServiceTest extends BaseTestCaseWithUser {

    private MinionServer server;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        server = MinionServerFactoryTest.createTestMinionServer(user);
    }

    public void testInternalStates() {
        List<StateSourceDto> result = StateSourceService.getSystemStateSources(server);

        // Internal states should be listed for every minion
        assertEquals(1, result.size());
        assertEquals("INTERNAL", result.get(0).getType());
    }

    public void testSystemStates() {
        ConfigChannel stateChannel = ConfigTestUtils.createConfigChannel(user.getOrg(), ConfigChannelType.state());
        server.subscribeConfigChannel(stateChannel, user);
        ConfigChannel configChannel = ConfigTestUtils.createConfigChannel(user.getOrg(), ConfigChannelType.normal());
        server.subscribeConfigChannel(configChannel, user);

        List<StateSourceDto> result = StateSourceService.getSystemStateSources(server);
        assertEquals(3, result.size());

        assertStateSourceEquals(stateChannel, server, findResultById(stateChannel.getId(), result));
        assertStateSourceEquals(configChannel, server, findResultById(configChannel.getId(), result));
        assertTrue(result.stream().anyMatch(s -> "INTERNAL".equals(s.getType())));
    }

    public void testGroupAndOrgStates() {
        ServerGroup group1 = ServerGroupTestUtils.createManaged(user);
        ServerGroup group2 = ServerGroupTestUtils.createManaged(user);
        ServerGroup group3 = ServerGroupTestUtils.createManaged(user);

        server.addGroup(group1);
        server.addGroup(group2);
        // Not subscribed to group3

        ConfigChannel ch1 = ConfigTestUtils.createConfigChannel(user.getOrg());
        ConfigChannel ch2 = ConfigTestUtils.createConfigChannel(user.getOrg());
        ConfigChannel ch3 = ConfigTestUtils.createConfigChannel(user.getOrg());
        ConfigChannel ch4 = ConfigTestUtils.createConfigChannel(user.getOrg());

        Org org = server.getOrg();

        org.subscribeConfigChannels(Arrays.asList(ch3, ch4), user);
        group3.subscribeConfigChannels(Arrays.asList(ch4), user);
        group2.subscribeConfigChannels(Arrays.asList(ch3), user);
        group1.subscribeConfigChannels(Arrays.asList(ch1, ch2), user);
        server.subscribeConfigChannel(ch1, user);

        List<StateSourceDto> result = StateSourceService.getSystemStateSources(server);
        assertEquals(5, result.size());

        // Sources should be assigned in priority: System > Group > Org
        assertStateSourceEquals(ch1, server, findResultById(ch1.getId(), result));
        assertStateSourceEquals(ch2, group1, findResultById(ch2.getId(), result));
        assertStateSourceEquals(ch3, group2, findResultById(ch3.getId(), result));
        assertStateSourceEquals(ch4, org, findResultById(ch4.getId(), result));
        assertTrue(result.stream().anyMatch(s -> "INTERNAL".equals(s.getType())));
    }

    private StateSourceDto findResultById(Long id, List<StateSourceDto> results) {
        return results.stream().filter(s -> id.equals(s.getId())).findFirst().orElseThrow();
    }

    private void assertStateSourceEquals(ConfigChannel expState, SaltConfigurable expSource, StateSourceDto actual) {
        // Assert state
        assertEquals(expState.getId(), actual.getId());
        assertEquals(expState.getDisplayName(), actual.getName());
        if (expState.getConfigChannelType() == ConfigChannelType.state()) {
            assertEquals("STATE", actual.getType());
        }
        else if (expState.getConfigChannelType() == ConfigChannelType.normal()) {
            assertEquals("CONFIG", actual.getType());
        }

        // Assert source
        assertEquals(expSource.getId(), actual.getSourceId());
        assertEquals(expSource.getName(), actual.getSourceName());

        if (expSource instanceof MinionServer) {
            assertEquals("SYSTEM", actual.getSourceType());
        }
        else if (expSource instanceof ServerGroup) {
            assertEquals("GROUP", actual.getSourceType());
        }
        else if (expSource instanceof Org) {
            assertEquals("ORG", actual.getSourceType());
        }
        else {
            throw new IllegalArgumentException("Invalid source type");
        }
    }
}
