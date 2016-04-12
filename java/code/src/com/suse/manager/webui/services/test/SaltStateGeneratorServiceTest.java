/**
 * Copyright (c) 2016 SUSE LLC
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

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerGroupTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import org.apache.commons.codec.digest.DigestUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tests for {@link SaltStateGeneratorService}
 */
public class SaltStateGeneratorServiceTest extends BaseTestCaseWithUser {

    public void setUp() throws Exception {
        super.setUp();
        Config.get().setString("server.secret_key",
                DigestUtils.sha256Hex(TestUtils.randomString()));
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
        SaltStateGeneratorService.INSTANCE.generatePillarForServer(minion);

        Path filePath = tmpPillarRoot.resolve(
                SaltStateGeneratorService.PILLAR_DATA_FILE_PREFIX + "_" +
                minion.getMinionId() + "." +
                SaltStateGeneratorService.PILLAR_DATA_FILE_EXT);

        assertTrue(Files.exists(filePath));

        Yaml yaml = new Yaml();
        FileInputStream fi = new FileInputStream(filePath.toFile());
        Map<String, Object> map = yaml.loadAs(fi, Map.class);
        fi.close();

        assertTrue(map.containsKey("org_id"));
        assertEquals(minion.getOrg().getId(), new Long((int) map.get("org_id")));

        assertTrue(map.containsKey("group_ids"));
        List<Integer> groups = (List<Integer>) map.get("group_ids");
        assertContains(groups.stream().map(id -> new Long((int) id))
                .collect(Collectors.toList()), group.getId());

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
}
