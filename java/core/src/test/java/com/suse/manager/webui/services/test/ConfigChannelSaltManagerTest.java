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
import com.redhat.rhn.domain.config.ConfigurationFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import com.suse.manager.webui.services.ConfigChannelSaltManager;
import com.suse.manager.webui.services.SaltConstants;

/**
 * Test for {@link ConfigChannelSaltManagerTest}.
 */
public class ConfigChannelSaltManagerTest extends BaseTestCaseWithUser {

    /**
     * Tests that a configuration file in a channel is reflected as a 'file.managed'
     * state module in the init.sls file contents.
     *
     * @throws Exception - if anything goes wrong
     */
    public void testFileInInitSls() throws Exception {
        ConfigChannel channel = ConfigChannelSaltManagerTestUtils.createTestChannel(user);
        ConfigChannelSaltManagerTestUtils.addFileToChannel(channel);
        ConfigChannel fromDb = ConfigurationFactory.lookupConfigChannelById(channel.getId());
        ConfigChannelSaltManager manager = ConfigChannelSaltManager.getInstance();

        ConfigFile file = channel.getConfigFiles().first();
        String expected =  manager.getFileStateName(file)  + ":\n" +
                "    file.managed:\n" +
                "    -   name: " + file.getConfigFileName().getPath() + "\n" +
                "    -   source: salt://" + manager.getOrgNamespace(channel.getOrgId()) + "/" +
                file.getConfigChannel().getLabel() + "/" +
                file.getConfigFileName().getPath() + "\n" +
                "    -   makedirs: true\n" +
                "    -   template: jinja\n" +
                "    -   user: chuck\n" +
                "    -   group: nobody\n" +
                "    -   mode: 700\n";

        assertEquals(expected, manager.configChannelInitSLSContent(fromDb));
    }

    /**
     * Tests that a configuration directory in a channel is reflected as a 'file.directory'
     * state module in the init.sls file contents.
     *
     * @throws Exception - if anything goes wrong
     */
    public void testDirectoryInInitSls() throws Exception {
        ConfigChannel channel = ConfigChannelSaltManagerTestUtils.createTestChannel(user);
        ConfigChannelSaltManagerTestUtils.addDirToChannel(channel);
        ConfigChannel fromDb = ConfigurationFactory.lookupConfigChannelById(channel.getId());
        ConfigChannelSaltManager manager = ConfigChannelSaltManager.getInstance();

        ConfigFile file = channel.getConfigFiles().first();
        String expected =  manager.getFileStateName(file)  + ":\n" +
                "    file.directory:\n" +
                "    -   name: " + file.getConfigFileName().getPath() + "\n" +
                "    -   makedirs: true\n" +
                "    -   user: chuck\n" +
                "    -   group: nobody\n" +
                "    -   mode: 700\n";

        assertEquals(expected, manager.configChannelInitSLSContent(fromDb));
    }

    /**
     * Tests that a configuration symlink in a channel is reflected as a 'file.symlink'
     * state module in the init.sls file contents.
     *
     * @throws Exception - if anything goes wrong
     */
    public void testSymlinkInInitSls() throws Exception {
        ConfigChannel channel = ConfigChannelSaltManagerTestUtils.createTestChannel(user);
        ConfigChannelSaltManagerTestUtils.addSymlinkToChannel(channel);
        ConfigChannel fromDb = ConfigurationFactory.lookupConfigChannelById(channel.getId());
        ConfigChannelSaltManager manager = ConfigChannelSaltManager.getInstance();

        ConfigFile file = channel.getConfigFiles().first();
        String expected =  manager.getFileStateName(file)  + ":\n" +
                "    file.symlink:\n" +
                "    -   name: " + file.getConfigFileName().getPath() + "\n" +
                "    -   target: /tmp/symlink_tgt\n" +
                "    -   makedirs: true\n";

        assertEquals(expected, manager.configChannelInitSLSContent(fromDb));
    }

    /**
     * Tests getting the salt URI for a file
     *
     * @throws  java.lang.Exception if anything goes wrong
     */
    public void testGetSaltUriForConfigFile() throws Exception {
        ConfigChannel channel = ConfigChannelSaltManagerTestUtils.createTestChannel(user);
        ConfigFile file = ConfigChannelSaltManagerTestUtils
                .addFileToChannel(channel)
                .getConfigFile();

        String saltUri = ConfigChannelSaltManager.getInstance().getSaltUriForConfigFile(file);

        String expectedUri = "salt://" +
                SaltConstants.ORG_STATES_DIRECTORY_PREFIX + channel.getOrgId() +
                "/" + channel.getLabel() +
                "/" + file.getConfigFileName().getPath();
        assertEquals(expectedUri, saltUri);
    }

    public void testGetSaltBaseUriForChannel() throws Exception {
        ConfigChannel channel = ConfigChannelSaltManagerTestUtils.createTestChannel(user);
        String saltUri = ConfigChannelSaltManager.getInstance().getSaltBaseUriForChannel(channel);

        String expectedUri = "salt://" +
                SaltConstants.ORG_STATES_DIRECTORY_PREFIX + channel.getOrgId() +
                "/" + channel.getLabel();
        assertEquals(expectedUri, saltUri);
    }
}
