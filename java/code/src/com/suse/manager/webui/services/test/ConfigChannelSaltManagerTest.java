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
        String expected = file.getConfigFileName().getPath() + ":\n" +
                "    file.managed:\n" +
                "    -   source:\n" +
                "        - salt://" + manager.getOrgNamespace(channel.getOrgId()) + "/"
                + file.getConfigFileName().getPath() + "\n";

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
        String expected = file.getConfigFileName().getPath() + ":\n"
                + "    file.directory:\n"
                + "    -   makedirs: true\n";

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
        String expected = file.getConfigFileName().getPath() + ":\n"
                + "    file.symlink:\n"
                + "    -   target: /tmp/symlink_tgt\n";

        assertEquals(expected, manager.configChannelInitSLSContent(fromDb));
    }
}