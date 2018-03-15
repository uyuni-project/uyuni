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

import com.suse.mgrsync.XMLChannel;
import com.suse.mgrsync.XMLChannelFamilies;
import com.suse.mgrsync.XMLChannelFamily;
import com.suse.mgrsync.XMLChannels;
import com.suse.mgrsync.XMLProduct;
import com.suse.mgrsync.XMLUpgradePath;
import com.suse.mgrsync.XMLUpgradePaths;

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
    private List<XMLChannel> readChannels() throws Exception {
        File source = new File(TestUtils.findTestData(CHANNELS_XML).getPath());
        List<XMLChannel> data = persister.read(
                XMLChannels.class, source).getChannels();
        SUSEProductTestUtils.deleteIfTempFile(source);
        return data;
    }

    /**
     * Get families.
     *
     * @return
     * @throws Exception
     */
    private List<XMLChannelFamily> readFamilies() throws Exception {
        File source = new File(TestUtils.findTestData(CHANNEL_FAMILIES_XML).getPath());
        List<XMLChannelFamily> data = persister.read(
                XMLChannelFamilies.class, source).getFamilies();
        SUSEProductTestUtils.deleteIfTempFile(source);
        return data;
    }

    /**
     * Get upgrade paths.
     *
     * @return
     * @throws Exception
     */
    private List<XMLUpgradePath> readUpgradePaths() throws Exception {
        File source = new File(TestUtils.findTestData(UPGRADE_PATHS_XML).getPath());
        List<XMLUpgradePath> data = persister.read(
                XMLUpgradePaths.class, source).getPaths();
        SUSEProductTestUtils.deleteIfTempFile(source);
        return data;
    }

    /**
     * Test read and parse channels.xml data file.
     * @throws Exception
     */
    public void testReadChannels() throws Exception {
        List<XMLChannel> channels = readChannels();

        // Verify the first channel
        XMLChannel c = channels.get(0);
        assertEquals("ati-driver-sle11-sp2-i586", c.getLabel());
        assertEquals("i586", c.getArch());
        assertEquals("7260", c.getFamily());
        assertEquals("http://www2.ati.com/suse/sle11sp2", c.getSourceUrl());
        assertFalse(c.isOptional());

        // Verify the products inside
        List<XMLProduct> products = c.getProducts();
        assertEquals(4, products.size());
        XMLProduct p0 = products.get(0);
        assertEquals(new Long(879), p0.getId());
        assertEquals("SUSE_SLED", p0.getName());
        assertEquals("11.2", p0.getVersion());
        assert(p0.getEol());
        XMLProduct p1 = products.get(1);
        assertEquals(new Long(844), p1.getId());
        assertEquals("SUSE_SLED", p1.getName());
        assertEquals("11.2", p1.getVersion());
        assert(p0.getEol());
        XMLProduct p2 = products.get(2);
        assertEquals(new Long(843), p2.getId());
        assertEquals("SUSE_SLED", p2.getName());
        assertEquals("11.2", p2.getVersion());
        assert(p0.getEol());
        XMLProduct p3 = products.get(3);
        assertEquals(new Long(868), p3.getId());
        assertEquals("SUSE_SLED", p3.getName());
        assertEquals("11.2", p3.getVersion());
        assert(p0.getEol());
    }

    /**
     * Test read and parse channel_families.xml data file.
     * @throws Exception
     */
    public void testReadFamilies() throws Exception {
        List<XMLChannelFamily> families = readFamilies();
        assertTrue("Not enough families read from file", families.size() > 63);
    }

    /**
     * Test read and parse upgrade_paths.xml data file.
     * @throws Exception
     */
    public void testReadUpgradePaths() throws Exception {
        List<XMLUpgradePath> paths = readUpgradePaths();
        assertTrue("Not enough Upgrade Paths read from file", paths.size() > 100);
    }
}
