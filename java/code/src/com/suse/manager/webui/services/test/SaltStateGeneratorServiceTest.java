/*
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

import static com.suse.manager.webui.services.SaltConstants.PILLAR_IMAGE_DATA_FILE_EXT;
import static com.suse.manager.webui.services.SaltConstants.SALT_CONFIG_STATES_DIR;
import static com.suse.manager.webui.services.SaltConstants.SALT_SERVER_STATE_FILE_PREFIX;
import static com.suse.manager.webui.services.SaltConstants.SUMA_PILLAR_IMAGES_DATA_PATH;
import static com.suse.manager.webui.utils.SaltFileUtils.defaultExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigurationFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerGroupTest;
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ConfigTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.webui.services.ConfigChannelSaltManager;
import com.suse.manager.webui.services.SaltStateGeneratorService;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Tests for {@link SaltStateGeneratorService}
 */
public class SaltStateGeneratorServiceTest extends BaseTestCaseWithUser {

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        Config.get().setString("server.secret_key",
                DigestUtils.sha256Hex(TestUtils.randomString()));
    }

    @Test
    public void testGenerateServerConfigState() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);

        ServerStateRevision serverRev = new ServerStateRevision();
        serverRev.setServer(minion);

        ConfigChannel channel1 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 1", "cfg-channel-1");
        ConfigChannel channel2 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 2", "cfg-channel-2");

        serverRev.getConfigChannels().add(channel1);
        serverRev.getConfigChannels().add(channel2);

        SaltStateGeneratorService.INSTANCE.generateConfigState(serverRev);

        Path filePath = tmpSaltRoot.resolve(SALT_CONFIG_STATES_DIR)
                .resolve(defaultExtension(SALT_SERVER_STATE_FILE_PREFIX +
                        minion.getMachineId()));

        assertTrue(Files.exists(filePath));

        Map<String, Object> map;
        try (FileInputStream fi = new FileInputStream(filePath.toFile())) {
            map = new Yaml().loadAs(fi, Map.class);
        }
        assertTrue(map.containsKey("include"));
        List<String> includes = (List<String>)map.get("include");
        assertTrue(includes.contains(
                ConfigChannelSaltManager.getInstance().getChannelStateName(channel1)));
        assertTrue(includes.contains(
                ConfigChannelSaltManager.getInstance().getChannelStateName(channel2)));
    }

    @Test
    public void testRegenerateConfigStates() throws Exception {
        Server minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        Server minion2 = MinionServerFactoryTest.createTestMinionServer(user);

        // Prepare input
        ConfigChannel channel1 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 1", "cfg-channel-1");
        ConfigChannel channel2 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 2", "cfg-channel-2");

        ServerStateRevision minion1Revision = new ServerStateRevision(minion1);
        minion1Revision.getConfigChannels().add(channel1);
        minion1Revision.getConfigChannels().add(channel2);

        ServerStateRevision minion2Revision = new ServerStateRevision(minion2);
        minion2Revision.getConfigChannels().add(channel1);

        StateFactory.save(minion1Revision);
        StateFactory.save(minion2Revision);
        StateFactory.getSession().flush();

        SaltStateGeneratorService.INSTANCE.generateConfigState(minion1Revision);
        SaltStateGeneratorService.INSTANCE.generateConfigState(minion2Revision);

        Path minion1StateFile = tmpSaltRoot.resolve(SALT_CONFIG_STATES_DIR).resolve(
                defaultExtension(SALT_SERVER_STATE_FILE_PREFIX + minion1.getMachineId()));
        Path minion2StateFile = tmpSaltRoot.resolve(SALT_CONFIG_STATES_DIR).resolve(
                defaultExtension(SALT_SERVER_STATE_FILE_PREFIX + minion2.getMachineId()));

        byte[] min1InitialContent = Files.readAllBytes(minion1StateFile);
        byte[] min2InitialContent = Files.readAllBytes(minion2StateFile);

        channel1.setLabel("cfg-channel-1-upd");
        channel2.setLabel("cfg-channel-2-upd");
        ConfigurationFactory.commit(channel1);
        ConfigurationFactory.commit(channel2);

        // Execute
        SaltStateGeneratorService.INSTANCE.regenerateConfigStates(channel2);
        byte[] min1FinalContent = Files.readAllBytes(minion1StateFile);
        byte[] min2FinalContent = Files.readAllBytes(minion2StateFile);

        // Assert file modification times
        // State file for minion 1 should be regenerated
        assertFalse(Arrays.equals(min1InitialContent, min1FinalContent));
        // State file for minion 1 should NOT be regenerated
        assertTrue(Arrays.equals(min2InitialContent, min2FinalContent));

        // Assert file contents
        Map<String, Object> map;
        try (FileInputStream fi = new FileInputStream(minion1StateFile.toFile())) {
            map = new Yaml().loadAs(fi, Map.class);
        }
        assertTrue(map.containsKey("include"));
        List<String> includes = (List<String>)map.get("include");
        assertEquals(2, includes.size());
        assertTrue(includes.contains(
                ConfigChannelSaltManager.getInstance().getChannelStateName(channel1)));
        assertTrue(includes.contains(
                ConfigChannelSaltManager.getInstance().getChannelStateName(channel2)));
    }

    @Test
    public void testImageSyncedPillar() throws Exception {
        ServerGroup group = ServerGroupTest.createTestServerGroup(user.getOrg(), null);

        Path filePath = tmpSaltRoot.resolve(SUMA_PILLAR_IMAGES_DATA_PATH)
                .resolve("group" + group.getId().toString())
                .resolve("ImageName-1.0.0." + PILLAR_IMAGE_DATA_FILE_EXT);

        SaltStateGeneratorService.INSTANCE.createImageSyncedPillar(group, "ImageName", "1.0.0");

        assertTrue(Files.exists(filePath));

        Map<String, Object> map;
        try (FileInputStream fi = new FileInputStream(filePath.toFile())) {
            map = new Yaml().loadAs(fi, Map.class);
        }
        assertTrue(map.containsKey("images"));
        Map<String, Object> images = (Map<String, Object>)map.get("images");
        assertTrue(images.containsKey("ImageName"));
        Map<String, Object> image = (Map<String, Object>)images.get("ImageName");
        assertTrue(image.containsKey("1.0.0"));
        Map<String, Object> version = (Map<String, Object>)image.get("1.0.0");
        assertTrue(version.containsKey("synced"));

        SaltStateGeneratorService.INSTANCE.removeImageSyncedPillar(group, "ImageName", "1.0.0");
        assertFalse(Files.exists(filePath));
    }

}
