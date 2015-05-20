/**
 * Copyright (c) 2014 SUSE LLC
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
package com.redhat.rhn.manager.content.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.test.ChannelFamilyTest;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.content.MgrSyncProductDto;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.suse.mgrsync.XMLChannel;
import com.suse.mgrsync.XMLChannels;
import com.suse.scc.model.SCCProduct;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.io.FileUtils;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Tests for {@link ContentSyncManager} against expected output from previous
 * tool, mgr-ncc-sync.
 */
public class ContentSyncManagerNonRegressionTest extends BaseTestCaseWithUser {
    // Files we read
    private static final String JARPATH = "/com/redhat/rhn/manager/content/test/";
    private static final String CHANNELS_XML = JARPATH + "channels.xml";
    private static final String PRODUCTS_JSON = JARPATH + "products.json";
    private static final String EXPECTED_PRODUCTS_CSV = JARPATH + "expected_products.csv";

    /** Channel family labels that are entitled in the scope of this non regression test. */
    private static final List<String> ENTITLED_LABELS = new LinkedList<String>() { {
        add("SLE-HAE-PPC"); add("RES"); add("SMS");
        add("SMP"); add("SUSE_CLOUD"); add("DSMP");
        add("MONO"); add("7261"); add("7260");
        add("VMDP"); add("SLE-SDK"); add("OES2");
        add("Moblin-2.1-MSI"); add("SLES-EC2"); add("JBEAP");
        add("SUSE"); add("20082"); add("MEEGO-1"); add("ZLM7");
        add("SLE-HAS"); add("SLES-X86-VMWARE"); add("13319");
        add("18962"); add("AiO"); add("SLES-PPC");
        add("10040"); add("SLESMT"); add("NAM-AGA");
        add("STUDIOONSITE"); add("SLMS"); add("SLES-Z");
        add("RES-HA"); add("SLE-HAE-IA"); add("WEBYAST");
        add("jeos"); add("SLM"); add("Moblin-2-Samsung");
        add("nVidia"); add("STUDIOONSITERUNNER"); add("SLE-HAE-Z");
        add("SLE-HAE-X86"); add("SLES-IA"); add("SLE-HAE-GEO");
        add("SLE-WE");
    } };

    /**
     * Tests listProducts() against known correct output (originally from
     * mgr-ncc-sync).
     * @throws Exception if anything goes wrong
     */
    @SuppressWarnings("unchecked")
    public void testListProducts() throws Exception {
        File channelsXML = new File(TestUtils.findTestData(CHANNELS_XML).getPath());
        File productsJSON = new File(TestUtils.findTestData(PRODUCTS_JSON).getPath());
        File expectedProductsCSV =
                new File(TestUtils.findTestData(EXPECTED_PRODUCTS_CSV).getPath());
        try {
            // clear existing products
            SUSEProductFactory.clearAllProducts();

            // ensure all needed channel families have enough entitlements, so
            // that channels are available later
            User admin = UserTestUtils.createUserInOrgOne();
            for (String label : ENTITLED_LABELS) {
                ChannelFamily cf = ChannelFamilyTest.ensureChannelFamilyExists(
                        admin, label);
                ChannelFamilyTest.ensureChannelFamilyHasMembers(admin, cf,
                        ContentSyncManagerTest.MANY_MEMBERS);
                HibernateFactory.getSession().flush();
            }

            List<XMLChannel> allChannels =
                    new Persister().read(XMLChannels.class, channelsXML).getChannels();

            List<SCCProduct> sccProducts =
                    new Gson().fromJson(FileUtils.readFileToString(productsJSON),
                    new TypeToken<List<SCCProduct>>() { } .getType());

            ContentSyncManager csm = new ContentSyncManager();

            // HACK: some SCC products do not have correct data
            // to be removed when SCC team fixes this
            csm.addDirtyFixes(sccProducts);

            csm.updateSUSEProducts(sccProducts);
            Collection<MgrSyncProductDto> products =
                    csm.listProducts(csm.getAvailableChannels(allChannels));

            Iterator<MgrSyncProductDto> i = products.iterator();
            MgrSyncProductDto base = null;
            Iterator<MgrSyncProductDto> j = IteratorUtils.EMPTY_ITERATOR;
            for (String line : (List<String>) FileUtils.readLines(expectedProductsCSV)) {
                Iterator<String> expected = Arrays.asList(line.split(",")).iterator();
                String friendlyName = expected.next();
                String version = expected.next();
                String arch = expected.next();
                boolean isBase = expected.next().equals("base");
                SortedSet<String> channelLabels = new TreeSet<String>();
                while (expected.hasNext()) {
                    channelLabels.add(expected.next());
                }

                if (isBase) {
                    if (j.hasNext()) {
                        fail("Base product " + base.toString()
                                + " should not have extension " + j.next().toString());
                    }

                    base = i.next();

                    assertProductMatches(friendlyName, version, arch, channelLabels, base);

                    j = base.getExtensions().iterator();
                }
                else {
                    if (!j.hasNext()) {
                        fail("Base product " + base.toString()
                                + " should have an extension named " + friendlyName);
                    }
                    MgrSyncProductDto extension = j.next();

                    assertProductMatches(friendlyName, version, arch, channelLabels,
                            extension);
                }
            }
            if (i.hasNext()) {
                fail("Unexpected base product " + i.next().toString());
            }
            if (j.hasNext()) {
                fail("Unexpected extension product " + j.next().toString());
            }
        }
        finally {
            SUSEProductTestUtils.deleteIfTempFile(expectedProductsCSV);
            SUSEProductTestUtils.deleteIfTempFile(productsJSON);
            SUSEProductTestUtils.deleteIfTempFile(channelsXML);
        }
    }

    /**
     * Checks that updateSUSEProducts() can be called multiple times in a row
     * without failing.
     * @throws Exception if anything goes wrong
     */
    public void testUpdateProductsMultipleTimes() throws Exception {
        File productsJSON = new File(TestUtils.findTestData(PRODUCTS_JSON).getPath());
        try {
            // clear existing products
            SUSEProductFactory.clearAllProducts();

            List<SCCProduct> sccProducts =
                    new Gson().fromJson(FileUtils.readFileToString(productsJSON),
                    new TypeToken<List<SCCProduct>>() { } .getType());

            ContentSyncManager csm = new ContentSyncManager();

            // HACK: some SCC products do not have correct data
            // to be removed when SCC team fixes this
            csm.addDirtyFixes(sccProducts);

            csm.updateSUSEProducts(sccProducts);
            csm.updateSUSEProducts(sccProducts);
        }
        finally {
            SUSEProductTestUtils.deleteIfTempFile(productsJSON);
        }
    }

    /**
     * Check that a product matches expected attributes.
     * @param friendlyName the expected friendly name
     * @param version the expected version
     * @param arch the expected arch
     * @param channelLabels the expected channel labels
     * @param product the actual product
     */
    public void assertProductMatches(String friendlyName, String version, String arch,
            SortedSet<String> channelLabels, MgrSyncProductDto product) {
        System.out.println("Checking product " + product.getId() + " (" + friendlyName
                + ", " + arch + ")");
        assertEquals(friendlyName, product.getFriendlyName());
        assertEquals(version, product.getVersion());
        assertEquals(arch, product.getArch());
        SortedSet<String> actualChannelLabels = new TreeSet<String>();
        for (XMLChannel channel : product.getChannels()) {
            String actualLabel = channel.getLabel();
            // mandatory channels have a trailing * in the CSV file
            if (!channel.isOptional()) {
                actualLabel += "*";
            }
            actualChannelLabels.add(actualLabel);
        }
        assertEquals(channelLabels.toString(), actualChannelLabels.toString());
    }
}
