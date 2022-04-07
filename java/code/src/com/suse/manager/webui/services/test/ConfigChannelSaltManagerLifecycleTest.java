/*
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

package com.suse.manager.webui.services.test;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.util.SHA256Crypt;
import com.redhat.rhn.domain.common.Checksum;
import com.redhat.rhn.domain.common.ChecksumFactory;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigContent;
import com.redhat.rhn.domain.config.ConfigFile;
import com.redhat.rhn.domain.config.ConfigFileType;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.manager.configuration.ConfigurationManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ConfigTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.webui.services.ConfigChannelSaltManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

/**
 * Test for {@link ConfigChannelSaltManagerTest}
 * - tests the lifecycle of the salt files structure on the disk and how it's reflected when
 * a ConfigChannel changes.
 */
public class ConfigChannelSaltManagerLifecycleTest extends BaseTestCaseWithUser {

    /** the instance used for testing **/
    private ConfigChannelSaltManager manager;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        this.manager = ConfigChannelSaltManager.getInstance();
        manager.setBaseDirPath(tmpSaltRoot.toAbsolutePath().toString());
        user.getOrg().addRole(RoleFactory.CONFIG_ADMIN);
        user.addPermanentRole(RoleFactory.CONFIG_ADMIN);
        TestUtils.saveAndFlush(user);
    }

    @Test
    public void testCreateAndRemoveChannel() throws Exception {
        ConfigChannel channel = ConfigChannelSaltManagerTestUtils.createTestChannel(user);
        ConfigChannelSaltManagerTestUtils.addFileToChannel(channel);
        ConfigurationManager.getInstance().save(channel, empty());

        File initSls = getGeneratedFile(channel, "init.sls");
        initSlsAssertions(initSls);

        ConfigurationManager.getInstance().deleteConfigChannel(user, channel);
        assertFalse(initSls.getParentFile().exists());
    }

    @Test
    public void testRemoveAssignedChannel() throws Exception {
        ConfigChannel channel1 = ConfigChannelSaltManagerTestUtils.createTestChannel(user);
        ConfigChannelSaltManagerTestUtils.addFileToChannel(channel1);
        ConfigurationManager.getInstance().save(channel1, empty());
        ConfigChannel channel2 = ConfigChannelSaltManagerTestUtils.createTestChannel(user);
        ConfigChannelSaltManagerTestUtils.addFileToChannel(channel2);
        ConfigurationManager.getInstance().save(channel2, empty());

        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        server.setConfigChannels(Arrays.asList(channel1, channel2), user);
        ServerFactory.save(server);

        ConfigurationManager.getInstance().deleteConfigChannel(user, channel1);

        try {
            ConfigurationManager.getInstance().lookupConfigChannel(user, channel1.getId());
            fail("Channel shouldn't be there...Error detected.");
        }
        catch (LookupException e) {
            // expected
        }
    }

    @Test
    public void testCreatedBinaryFile() throws Exception {
        ConfigChannel channel = ConfigChannelSaltManagerTestUtils.createTestChannel(user);
        ConfigFile fl = ConfigTestUtils.createConfigFile(channel);
        ConfigRevision configRevision = ConfigTestUtils.createConfigRevision(fl, ConfigFileType.file());

        byte [] randomData = new byte[1000];
        new Random().nextBytes(randomData);
        ConfigContent configContent = ConfigTestUtils.createConfigContent(1000L, true);
        configContent.setContents(randomData);
        Checksum contentsChecksum = ChecksumFactory.safeCreate(SHA256Crypt.sha256Hex(randomData), "sha256");
        configContent.setChecksum(contentsChecksum);
        configRevision.setConfigContent(configContent);

        ConfigurationManager.getInstance().save(channel, empty());
        File createdFile = getGeneratedFile(channel, fl.getConfigFileName().getPath());
        Checksum fileChecksum =  ChecksumFactory.
                safeCreate(SHA256Crypt.sha256Hex(
                        IOUtils.toByteArray(new FileInputStream(createdFile))),
                        "sha256");
        assertEquals(contentsChecksum, fileChecksum);

        ConfigurationManager.getInstance().deleteConfigChannel(user, channel);
        assertFalse(createdFile.getParentFile().exists());
    }

    @Test
    public void testRenameChannelLabel() throws Exception {
        ConfigChannel channel = ConfigChannelSaltManagerTestUtils.createTestChannel(user);
        ConfigChannelSaltManagerTestUtils.addFileToChannel(channel);
        ConfigurationManager.getInstance().save(channel, empty());

        File oldInitSls = getGeneratedFile(channel, "init.sls");
        initSlsAssertions(oldInitSls);

        String oldLabel = channel.getLabel();
        channel.setLabel(oldLabel + "-new");
        ConfigurationManager.getInstance().save(channel, of(oldLabel));

        assertFalse(oldInitSls.getParentFile().exists());
        File newInitSls = getGeneratedFile(channel, "init.sls");
        assertTrue(newInitSls.getParentFile().exists());
        initSlsAssertions(newInitSls);
    }

    @Test
    public void testRemoveFile() throws Exception {
        ConfigChannel channel = ConfigChannelSaltManagerTestUtils.createTestChannel(user);
        ConfigRevision configRevision =
                ConfigChannelSaltManagerTestUtils.addFileToChannel(channel);
        ConfigurationManager.getInstance().save(channel, empty());

        File initSls = getGeneratedFile(channel, "init.sls");
        initSlsAssertions(initSls);

        ConfigFile configFile = configRevision.getConfigFile();
        File configFileOnDisk = getGeneratedFile(channel,
                configFile.getConfigFileName().getPath());
        assertTrue(configFileOnDisk.exists());

        ConfigurationManager.getInstance().deleteConfigFile(user, configFile);
        getGeneratedFile(channel, configFile.getConfigFileName().getPath());
        assertFalse(configFileOnDisk.exists());
    }

    @Test
    public void testUpdateRevision() throws Exception {
        ConfigChannel channel = ConfigChannelSaltManagerTestUtils.createTestChannel(user);
        ConfigRevision configRevision =
                ConfigChannelSaltManagerTestUtils.addFileToChannel(channel);
        ConfigurationManager.getInstance().save(channel, empty());

        File initSls = getGeneratedFile(channel, "init.sls");
        initSlsAssertions(initSls);

        ConfigFile configFile = configRevision.getConfigFile();
        File configFileOnDisk = getGeneratedFile(channel,
                configFile.getConfigFileName().getPath());
        assertTrue(configFileOnDisk.exists());

        ConfigurationManager.getInstance().deleteConfigFile(user, configFile);
        getGeneratedFile(channel, configFile.getConfigFileName().getPath());
        assertFalse(configFileOnDisk.exists());
    }

    private File getGeneratedFile(ConfigChannel channel, String filePathInChannel) {
        return Paths.get(tmpSaltRoot.toAbsolutePath().toString(),
                manager.getOrgNamespace(channel.getOrgId()),
                channel.getLabel(),
                filePathInChannel)
                .toFile();
    }

    /**
     * Common assertions on a init.sls file.
     *
     * @param initSlsFile the init.sls File object
     * @param contentChunks optional chunks of content of the init.sls file
     * @throws IOException if anything goes wrong
     */
    private static void initSlsAssertions(File initSlsFile, String ... contentChunks)
            throws IOException {
        assertTrue(initSlsFile.exists());
        assertTrue(initSlsFile.isFile());
        String initSlsContents = FileUtils.readFileToString(initSlsFile);
        for (String contentChunk : contentChunks) {
            assertTrue(initSlsContents.contains(contentChunk));
        }
    }
}
