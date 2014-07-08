/**
 * Copyright (c) 2014 SUSE
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

package com.suse.contentsync.test;

import com.redhat.rhn.manager.content.test.ContentSyncManagerTest;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;
import com.suse.contentsync.SUSEChannel;
import com.suse.contentsync.SUSEChannelFamilies;
import com.suse.contentsync.SUSEChannelFamily;
import com.suse.contentsync.SUSEChannels;
import com.suse.contentsync.SUSEUpgradePath;
import com.suse.contentsync.SUSEUpgradePaths;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.simpleframework.xml.core.Persister;

/**
 *
 * @author bo
 */
public class SUSEContentSyncXMLParseTest extends RhnBaseTestCase {
    private static final String JARPATH = "/com/redhat/rhn/manager/content/test/";
    private static final String CHANNELS_XML = JARPATH + "channels.xml";
    private static final String CHANNEL_FAMILIES_XML = JARPATH + "channel_families.xml";
    private static final String UPGRADE_PATHS_XML = JARPATH + "upgrade_paths.xml";

    private Persister persister;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.persister = new Persister();
    }

        /**
     * Get channels.
     *
     * @return
     * @throws Exception
     */
    private List<SUSEChannel> readChannels() throws Exception {
        File source = new File(TestUtils.findTestData(CHANNELS_XML).getPath());
        List<SUSEChannel> data = this.persister.read(SUSEChannels.class,
                                                     source).getChannels();
        source.delete();
        return data;
    }

    /**
     * Get families.
     *
     * @return
     * @throws Exception
     */
    private List<SUSEChannelFamily> readFamilies() throws Exception {
        File source = new File(TestUtils.findTestData(CHANNEL_FAMILIES_XML).getPath());
        List<SUSEChannelFamily> data = this.persister.read(SUSEChannelFamilies.class,
                                                           source).getFamilies();
        source.delete();
        return data;
    }

    /**
     * Get upgrade paths.
     *
     * @return
     * @throws Exception
     */
    private List<SUSEUpgradePath> readUpgradePaths() throws Exception {
        File source = new File(TestUtils.findTestData(UPGRADE_PATHS_XML).getPath());
        List<SUSEUpgradePath> data = this.persister.read(SUSEUpgradePaths.class,
                                                         source).getPaths();
        source.delete();
        return data;
    }

    /**
     * Test read and parse channels.xml data file.
     * @throws Exception
     */
    public void testReadChannels() throws Exception {
        List<SUSEChannel> channels = this.readChannels();
        assertNotNull(channels);
        assertNotEmpty(channels);
        assertEquals(60, channels.size());
    }

    /**
     * Test read and parse channel_families.xml data file.
     * @throws Exception
     */
    public void testReadFamilies() throws Exception {
        List<SUSEChannelFamily> families = this.readFamilies();
        assertNotNull(families);
        assertNotEmpty(families);
        assertEquals(3, families.size());
    }

    /**
     * Test read and parse upgrade_paths.xml data file.
     * @throws Exception
     */
    public void testReadUpgradePaths() throws Exception {
        List<SUSEUpgradePath> paths = this.readUpgradePaths();
        assertNotNull(paths);
        assertNotEmpty(paths);
        assertEquals(3, paths.size());
    }
}
