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

package com.suse.mgrsync.test;

import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;

import com.suse.mgrsync.MgrSyncChannel;
import com.suse.mgrsync.MgrSyncChannelFamilies;
import com.suse.mgrsync.MgrSyncChannelFamily;
import com.suse.mgrsync.MgrSyncChannels;
import com.suse.mgrsync.SUSEUpgradePath;
import com.suse.mgrsync.SUSEUpgradePaths;

import java.io.File;
import java.util.List;

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
    private List<MgrSyncChannel> readChannels() throws Exception {
        File source = new File(TestUtils.findTestData(CHANNELS_XML).getPath());
        List<MgrSyncChannel> data = this.persister.read(MgrSyncChannels.class,
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
    private List<MgrSyncChannelFamily> readFamilies() throws Exception {
        File source = new File(TestUtils.findTestData(CHANNEL_FAMILIES_XML).getPath());
        List<MgrSyncChannelFamily> data = this.persister.read(MgrSyncChannelFamilies.class,
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
        List<MgrSyncChannel> channels = this.readChannels();
        assertNotNull(channels);
        assertNotEmpty(channels);
        assertEquals(60, channels.size());
    }

    /**
     * Test read and parse channel_families.xml data file.
     * @throws Exception
     */
    public void testReadFamilies() throws Exception {
        List<MgrSyncChannelFamily> families = this.readFamilies();
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
