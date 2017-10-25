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

package com.suse.manager.webui.services.test;

import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigFile;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.manager.configuration.ConfigurationManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.suse.manager.webui.services.ConfigChannelSaltManager;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * Test for {@link ConfigChannelSaltManagerTest}
 * - tests the lifecycle of the salt files structure on the disk and how it's reflected when
 * a ConfigChannel changes.
 */
public class ConfigChannelSaltManagerLifecycleTest extends BaseTestCaseWithUser {

    /** the instance used for testing **/
    private ConfigChannelSaltManager manager;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.manager = ConfigChannelSaltManager.getInstance();
        manager.setBaseDirPath(tmpSaltRoot.toAbsolutePath().toString());
        user.getOrg().addRole(RoleFactory.CONFIG_ADMIN);
        user.addPermanentRole(RoleFactory.CONFIG_ADMIN);
        TestUtils.saveAndFlush(user);
    }

    public void testCreateAndRemoveChannel() throws Exception {
        ConfigChannel channel = ConfigChannelSaltManagerTestUtils.createTestChannel(user);
        ConfigChannelSaltManagerTestUtils.addFileToChannel(channel);
        ConfigurationManager.getInstance().save(channel, empty());

        File initSls = getGeneratedFile(channel, "init.sls");
        initSlsAssertions(initSls);

        ConfigurationManager.getInstance().deleteConfigChannel(user, channel);
        assertFalse(initSls.getParentFile().exists());
    }

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

    // todo
    // CRUD channel
    // CRUD file
    // CRUD revision
    // file crud + copy from other channel
    // change file (revision) type
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
                channel.getConfigChannelType().getLabel(),
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
        Assert.assertTrue(initSlsFile.exists());
        Assert.assertTrue(initSlsFile.isFile());
        String initSlsContents = FileUtils.readFileToString(initSlsFile);
        for (String contentChunk : contentChunks) {
            Assert.assertTrue(initSlsContents.contains(contentChunk));
        }
    }
}