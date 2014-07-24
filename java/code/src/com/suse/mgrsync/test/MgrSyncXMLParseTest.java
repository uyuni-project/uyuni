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

import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;

import com.suse.mgrsync.MgrSyncChannel;
import com.suse.mgrsync.MgrSyncChannelFamilies;
import com.suse.mgrsync.MgrSyncChannelFamily;
import com.suse.mgrsync.MgrSyncChannels;
import com.suse.mgrsync.MgrSyncUpgradePath;
import com.suse.mgrsync.MgrSyncUpgradePaths;

import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.util.List;

/**
 * Tests for parsing MgrSync* classes from xml files.
 *
 * TODO: Check also attributes inside classes, current tests check only the number
 * of parsed objects!
 */
public class MgrSyncXMLParseTest extends RhnBaseTestCase {

    // Paths to xml files
    private static final String JARPATH = "/com/redhat/rhn/manager/content/test/";
    private static final String CHANNELS_XML = JARPATH + "channels.xml";
    private static final String CHANNEL_FAMILIES_XML = JARPATH + "channel_families.xml";
    private static final String UPGRADE_PATHS_XML = JARPATH + "upgrade_paths.xml";

    private Persister persister = new Persister();

    /**
     * Get channels.
     *
     * @return
     * @throws Exception
     */
    private List<MgrSyncChannel> readChannels() throws Exception {
        File source = new File(TestUtils.findTestData(CHANNELS_XML).getPath());
        List<MgrSyncChannel> data = persister.read(
                MgrSyncChannels.class, source).getChannels();
        SUSEProductTestUtils.deleteIfTempFile(source);
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
        List<MgrSyncChannelFamily> data = persister.read(
                MgrSyncChannelFamilies.class, source).getFamilies();
        SUSEProductTestUtils.deleteIfTempFile(source);
        return data;
    }

    /**
     * Get upgrade paths.
     *
     * @return
     * @throws Exception
     */
    private List<MgrSyncUpgradePath> readUpgradePaths() throws Exception {
        File source = new File(TestUtils.findTestData(UPGRADE_PATHS_XML).getPath());
        List<MgrSyncUpgradePath> data = persister.read(
                MgrSyncUpgradePaths.class, source).getPaths();
        SUSEProductTestUtils.deleteIfTempFile(source);
        return data;
    }

    /**
     * Test read and parse channels.xml data file.
     * @throws Exception
     */
    public void testReadChannels() throws Exception {
        List<MgrSyncChannel> channels = readChannels();
        assertEquals(740, channels.size());
    }

    /**
     * Test read and parse channel_families.xml data file.
     * @throws Exception
     */
    public void testReadFamilies() throws Exception {
        List<MgrSyncChannelFamily> families = readFamilies();
        assertEquals(48, families.size());
    }

    /**
     * Test read and parse upgrade_paths.xml data file.
     * @throws Exception
     */
    public void testReadUpgradePaths() throws Exception {
        List<MgrSyncUpgradePath> paths = readUpgradePaths();
        assertEquals(87, paths.size());
    }
}
