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

import com.mchange.v2.lang.StringUtils;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigContent;
import com.redhat.rhn.domain.config.ConfigFile;
import com.redhat.rhn.domain.config.ConfigFileType;
import com.redhat.rhn.domain.config.ConfigInfo;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.config.ConfigurationFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.ConfigTestUtils;

/**
 * Static class with helper utils for
 * {@link com.suse.manager.webui.services.ConfigChannelSaltManager} tests.
 */
public class ConfigChannelSaltManagerTestUtils {
    private ConfigChannelSaltManagerTestUtils() { }

    /**
     * Create a test channel and reloads the object.
     * @param user the user
     * @return the channel
     * @throws Exception if anything goes wrong
     */
    public static ConfigChannel createTestChannel(User user) throws Exception {
        ConfigChannel cc = ConfigTestUtils.createConfigChannel(user.getOrg());
        return (ConfigChannel) HibernateFactory.reload(cc);
    }

    /**
     * Add a config revision of a file (with content) to the test channel.
     * @param channel the channel
     * @return the config revision of the file
     */
    public static ConfigRevision addFileToChannel(ConfigChannel channel) {
        ConfigFile fl = ConfigTestUtils.createConfigFile(channel);
        ConfigRevision configRevision = ConfigTestUtils.createConfigRevision(fl,
                ConfigFileType.file());
        ConfigContent configContent = ConfigTestUtils.createConfigContent(10L, false);
        configContent.setContents(StringUtils.getUTF8Bytes("aoeuäö€üáóéúř"));
        configRevision.setConfigContent(configContent);
        changeConfigInfo(configRevision.getConfigInfo());
        return configRevision;
    }

    /**
     * Add a config revision of a directory to the test channel.
     * @param channel the channel
     */
    public static void addDirToChannel(ConfigChannel channel) {
        ConfigFile fl = ConfigTestUtils.createConfigFile(channel);
        ConfigRevision configRevision =
                ConfigTestUtils.createConfigRevision(fl, ConfigFileType.dir());
        changeConfigInfo(configRevision.getConfigInfo());
    }

    /**
     * Add a config revision of a symlink to the test channel.
     * @param channel the channel
     */
    public static void addSymlinkToChannel(ConfigChannel channel) {
        ConfigFile fl = ConfigTestUtils.createConfigFile(channel);
        ConfigRevision configRevision = ConfigTestUtils.createConfigRevision(fl,
                ConfigFileType.symlink());
        ConfigInfo configInfo = ConfigTestUtils.createConfigInfo();
        configInfo.setTargetFileName(
                ConfigurationFactory.lookupOrInsertConfigFileName("/tmp/symlink_tgt"));
        changeConfigInfo(configInfo);
        configRevision.setConfigInfo(configInfo);
    }

    private static void changeConfigInfo(ConfigInfo configInfo) {
        configInfo.setUsername("chuck");
        configInfo.setGroupname("nobody");
        configInfo.setFilemode(700L); // only Chuck has access
    }
}
