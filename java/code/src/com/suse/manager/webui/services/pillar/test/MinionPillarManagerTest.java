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
package com.suse.manager.webui.services.pillar.test;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerPath;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerGroupTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.webui.services.pillar.MinionPillarManager;
import org.apache.commons.codec.digest.DigestUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static com.suse.manager.webui.services.SaltConstants.PILLAR_DATA_FILE_PREFIX;
import static com.suse.manager.webui.services.SaltConstants.PILLAR_DATA_FILE_EXT;

/**
 * Tests for {@link MinionPillarManager}
 */
public class MinionPillarManagerTest extends BaseTestCaseWithUser {

    protected MinionPillarManager minionPillarManager;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Config.get().setString("server.secret_key",
                DigestUtils.sha256Hex(TestUtils.randomString()));
        minionPillarManager = new MinionPillarManager(
                Arrays.asList(this.minionGeneralPillarFileManager, this.minionGroupMembershipPillarFileManager));
    }

    public void testGeneratePillarForServer() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);

        ServerGroup group = ServerGroupTest.createTestServerGroup(user.getOrg(), null);
        ServerFactory.addServerToGroup(minion, group);
        String machineId = TestUtils.randomString();
        minion.setDigitalServerId(machineId);

        Channel channel1 = ChannelTestUtils.createBaseChannel(user);
        minion.addChannel(channel1);

        Channel channel2 = ChannelTestUtils.createBaseChannel(user);
        minion.addChannel(channel2);

        ServerFactory.save(minion);
        minionPillarManager.generatePillar(minion);

        Path filePath = tmpPillarRoot.resolve(
                PILLAR_DATA_FILE_PREFIX + "_" +
                minion.getMinionId() + "." +
                PILLAR_DATA_FILE_EXT);

        assertTrue(Files.exists(filePath));

        Map<String, Object> map;
        try (FileInputStream fi = new FileInputStream(filePath.toFile())) {
            map = new Yaml().loadAs(fi, Map.class);
        }

        assertTrue(map.containsKey("org_id"));
        assertEquals(minion.getOrg().getId(), Long.valueOf((int) map.get("org_id")));
        assertTrue(map.containsKey("mgr_server"));
        assertEquals(ConfigDefaults.get().getCobblerHost(), map.get("mgr_server"));
        assertTrue(map.containsKey("mgr_origin_server"));
        assertEquals(ConfigDefaults.get().getCobblerHost(), map.get("mgr_origin_server"));

        assertTrue(map.containsKey("channels"));
        Map<String, Object> channels = (Map<String, Object>) map.get("channels");
        assertEquals(2, channels.size());
        assertTrue(channels.containsKey(channel1.getLabel()));
        assertTrue(channels.containsKey(channel2.getLabel()));

        for (String chanLabel : channels.keySet()) {
            Map<String, Object> values =  (Map<String, Object>) channels.get(chanLabel);

            assertTrue(values.containsKey("alias"));
            assertEquals("susemanager:" + chanLabel, (String) values.get("alias"));

            assertTrue(values.containsKey("name"));
            assertTrue(values.containsKey("enabled"));
            assertEquals("1", (String) values.get("enabled"));

            assertTrue(values.containsKey("autorefresh"));
            assertEquals("1", (String) values.get("autorefresh"));

            assertTrue(values.containsKey("host"));
            assertEquals(ConfigDefaults.get().getCobblerHost(),
                    (String) values.get("host"));

            assertTrue(values.containsKey("token"));
            assertFalse(((String) values.get("host")).isEmpty());

            assertTrue(values.containsKey("type"));
            assertEquals("rpm-md",
                    (String) values.get("type"));

            assertTrue(values.containsKey("gpgcheck"));
            assertEquals("0", (String) values.get("gpgcheck"));

            assertTrue(values.containsKey("repo_gpgcheck"));
            assertEquals("0", (String) values.get("repo_gpgcheck"));

            assertTrue(values.containsKey("pkg_gpgcheck"));
            assertEquals("1", (String) values.get("pkg_gpgcheck"));
        }
    }

    public void testGeneratePillarForServerGPGCheckOn() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        Channel channel1 = ChannelTestUtils.createBaseChannel(user);
        minion.addChannel(channel1);
        ServerFactory.save(minion);

        minionPillarManager.generatePillar(minion);

        Path filePath = tmpPillarRoot
                .resolve(PILLAR_DATA_FILE_PREFIX + "_" + minion.getMinionId() + "." + PILLAR_DATA_FILE_EXT);

        assertTrue(Files.exists(filePath));

        Map<String, Object> map;
        try (FileInputStream fi = new FileInputStream(filePath.toFile())) {
            map = new Yaml().loadAs(fi, Map.class);
        }

        Map<String, Object> channels = (Map<String, Object>) map.get("channels");
        assertEquals(1, channels.size());
        assertTrue(channels.containsKey(channel1.getLabel()));

        Map<String, Object> values = (Map<String, Object>) channels.get(channel1.getLabel());

        assertTrue(values.containsKey("pkg_gpgcheck"));
        assertEquals("1", (String) values.get("pkg_gpgcheck"));
    }

    public void testGeneratePillarForServerGPGCheckOff() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        Channel channel1 = ChannelTestUtils.createBaseChannel(user);
        channel1.setGPGCheck(false);
        minion.addChannel(channel1);
        ServerFactory.save(minion);

        minionPillarManager.generatePillar(minion);

        Path filePath = tmpPillarRoot.resolve(
            PILLAR_DATA_FILE_PREFIX + "_" +
            minion.getMinionId() + "." +
            PILLAR_DATA_FILE_EXT);

        assertTrue(Files.exists(filePath));

        Map<String, Object> map;
        try (FileInputStream fi = new FileInputStream(filePath.toFile())) {
            map = new Yaml().loadAs(fi, Map.class);
        }

        Map<String, Object> channels = (Map<String, Object>) map.get("channels");
        assertEquals(1, channels.size());
        assertTrue(channels.containsKey(channel1.getLabel()));

        Map<String, Object> values = (Map<String, Object>) channels.get(channel1.getLabel());

        assertTrue(values.containsKey("pkg_gpgcheck"));
        assertEquals("0", (String) values.get("pkg_gpgcheck"));
    }

    /**
     * Test that the "host" attribute of the channel of the minion connected to a proxy
     * is populated with the proxy hostname.
     *
     * @throws Exception - if anything goes wrong
     */
    public void testGeneratePillarForProxyServer() throws Exception {
        // create a minion
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);

        ServerGroup group = ServerGroupTest.createTestServerGroup(user.getOrg(), null);
        ServerFactory.addServerToGroup(minion, group);
        String machineId = TestUtils.randomString();
        minion.setDigitalServerId(machineId);

        // create a channel for the minion
        Channel channel = ChannelTestUtils.createBaseChannel(user);
        minion.addChannel(channel);
        ServerFactory.save(minion);

        // create proxy server
        Server proxy = ServerTestUtils.createTestSystem();
        String proxyHostname = "proxyHostname";

        // create a serverPath linking the minion to the proxy
        Set<ServerPath> proxyPaths = ServerFactory.createServerPaths(minion, proxy, proxyHostname);
        minion.getServerPaths().addAll(proxyPaths);

        // flush session & refresh the minion object
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().refresh(minion);

        minionPillarManager.generatePillar(minion);

        Path filePath = tmpPillarRoot.resolve(
                PILLAR_DATA_FILE_PREFIX + "_" +
                minion.getMinionId() + "." +
                PILLAR_DATA_FILE_EXT);

        Map<String, Object> map;
        try (FileInputStream fi = new FileInputStream(filePath.toFile())) {
            map = new Yaml().loadAs(fi, Map.class);
        }

        assertTrue(map.containsKey("mgr_server"));
        assertEquals(proxyHostname, map.get("mgr_server"));
        assertTrue(map.containsKey("mgr_origin_server"));
        assertEquals(ConfigDefaults.get().getCobblerHost(), map.get("mgr_origin_server"));

        Map<String, Object> channels = (Map<String, Object>) map.get("channels");
        assertEquals(1, channels.size());
        Map<String, Object> channelFromFile = (Map<String, Object>) channels.values().iterator().next();
        assertEquals(proxyHostname, channelFromFile.get("host"));
    }

}
