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
package com.redhat.rhn.manager.content.test;

import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.channel.test.ChannelFamilyFactoryTest;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.content.ListedProduct;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.suse.mgrsync.MgrSyncChannel;
import com.suse.mgrsync.MgrSyncChannels;
import com.suse.scc.model.SCCProduct;

import org.apache.commons.io.FileUtils;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Tests for {@link ContentSyncManager} against expected output from previous
 * tool, mgr-ncc-sync.
 */
public class ContentSyncManagerNonRegressionTest extends BaseTestCaseWithUser {

    // Files we read
    private static final String JARPATH = "/com/redhat/rhn/manager/content/test/";
    private static final String CHANNELS_XML = JARPATH + "channels.xml";
    private static final String PRODUCTS_JSON = JARPATH + "products.json";

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
        add("SLE-HAE-X86"); add("SLES-IA");
    } };

    /**
     * Tests listProducts() against known correct output (originally from mgr-ncc-sync).
     * @throws Exception if anything goes wrong
     */
    public void testListProducts() throws Exception {
        File channelsXML = new File(TestUtils.findTestData(CHANNELS_XML).getPath());
        File productsJSON = new File(TestUtils.findTestData(PRODUCTS_JSON).getPath());
        try {
            List<MgrSyncChannel> allChannels =
                    new Persister().read(MgrSyncChannels.class, channelsXML).getChannels();

            for (String label : ENTITLED_LABELS) {
                ChannelFamily cf = ChannelFamilyFactory.lookupByLabel(label, null);
                if (cf == null) {
                    cf = ChannelFamilyFactoryTest.createTestChannelFamily(user,
                        20000L, 0L, true, TestUtils.randomString());
                    cf.setName(label);
                    ChannelFamilyFactory.save(cf);
                }
            }

            SUSEProductFactory.clearAllProducts();

            List<SCCProduct> sccProducts =
                    new Gson().fromJson(FileUtils.readFileToString(productsJSON),
                            new TypeToken<List<SCCProduct>>() { } .getType());

            ContentSyncManager csm = new ContentSyncManager();

            // HACK: some SCC products do not have correct data
            // to be removed when SCC team fixes this
            csm.addDirtyFixes(sccProducts);

            csm.updateSUSEProducts(sccProducts);
            Collection<ListedProduct> products =
                    csm.listProducts(csm.getAvailableChannels(allChannels));

            Iterator<ListedProduct> i = products.iterator();
            ListedProduct product = null;

            // RES
            for (int version = 4; version <= 6; version++) {
                for (String arch : new String[] {"i386", "x86_64"}) {
                    product = i.next();
                    assertMatches("RES " + version, version + "", arch, product);
                }
            }

            // SLED 11
            for (String sp : new String[] {"2", "3"}) {
                for (String arch : new String[] {"i586", "x86_64"}) {
                    product = i.next();
                    assertMatches("SUSE Linux Enterprise Desktop 11 SP" + sp, "11." + sp,
                            arch, product);

                    Iterator<ListedProduct> j = product.getExtensions().iterator();
                    product = j.next();
                    assertMatches("SUSE Linux Enterprise Software Development Kit 11 SP"
                            + sp, "11." + sp, arch, product);
                    assertFalse(j.hasNext());
                }
            }

            // SLES 11 for SAP
            for (String sp : new String[] {"1", "2", "3"}) {
                product = i.next();
                assertMatches("SUSE Linux Enterprise Server 10 SP1 SAP AiO 11 SP" + sp,
                        "11." + sp, "x86_64", product);

                Iterator<ListedProduct> j = product.getExtensions().iterator();
                product = j.next();
                assertMatches("SUSE Linux Enterprise Software Development Kit 11 SP" + sp,
                        "11." + sp, "x86_64", product);

                if (sp.equals("3")) {
                    product = j.next();
                    assertMatches(
                            "SUSE Linux Enterprise Subscription Management Tool 11 SP" + sp,
                            "11." + sp, "x86_64", product);
                }

                assertFalse(j.hasNext());
            }

            // SLES 10
            for (String sp : new String[] {"3", "4"}) {
                for (String arch : new String[] {"i586", "ia64", "ppc",
                        "s390x", "x86_64"}) {
                    product = i.next();
                    assertMatches("SUSE Linux Enterprise Server 10 SP" + sp, "10." + sp,
                            arch, product);

                    Iterator<ListedProduct> j = product.getExtensions().iterator();
                    product = j.next();
                    assertMatches("SUSE Linux Enterprise Software Development Kit 10 SP"
                            + sp, "10." + sp, arch, product);
                    assertFalse(j.hasNext());
                }
            }

            // SLES 11
            for (String sp : new String[] {"1", "2", "3"}) {
                for (String arch : new String[] {"i586", "ia64", "ppc64", "s390x",
                        "x86_64"}) {
                    product = i.next();
                    assertEquals("SUSE Linux Enterprise Server 11 SP" + sp,
                            product.getFriendlyName());
                    assertEquals("11." + sp, product.getVersion());
                    assertEquals(arch, product.getArch());

                    Iterator<ListedProduct> j = product.getExtensions().iterator();

                    if (sp.equals("1") && arch.equals("x86_64")) {
                        product = j.next();
                        assertMatches("Novell Open Enterprise Server 2 11", "11", arch,
                                product);
                    }

                    if (sp.equals("2") && arch.equals("x86_64")) {
                        product = j.next();
                        assertMatches("Novell Open Enterprise Server 2 11.1", "11.1",
                                arch, product);

                        product = j.next();
                        assertMatches("SUSE Cloud 1.0", "1.0", arch, product);

                        product = j.next();
                        assertMatches("SUSE Lifecycle Management Server 1.3", "1.3", arch,
                                product);
                    }

                    if (sp.equals("3") && arch.equals("x86_64")) {
                        product = j.next();
                        assertMatches("Novell Open Enterprise Server 2 11.2", "11.2",
                                arch, product);

                        product = j.next();
                        assertMatches("SUSE Cloud 2.0", "2.0", arch, product);

                        product = j.next();
                        assertMatches("SUSE Cloud 3", "3", arch, product);
                    }

                    product = j.next();
                    assertMatches("SUSE Linux Enterprise High Availability Extension 11 SP"
                            + sp, "11." + sp, arch, product);

                    if ((sp.equals("1") || sp.equals("3")) &&
                        (arch.equals("i586") || arch.equals("x86_64"))) {
                        product = j.next();
                        assertMatches("SUSE Linux Enterprise Point of Service 11 SP" + sp,
                                "11." + sp, arch, product);
                    }

                    if (arch.equals("x86_64")) {
                        product = j.next();
                        assertMatches("SUSE Linux Enterprise Real Time 11", "11." + sp,
                                "x86_64", product);
                    }

                    product = j.next();
                    assertMatches("SUSE Linux Enterprise Software Development Kit 11 SP"
                            + sp, "11." + sp, arch, product);

                    if ((arch.equals("i586") || arch.equals("x86_64") ||
                            arch.equals("s390x"))) {

                        if (sp.equals("1")) {
                            product = j.next();
                            assertMatches(
                                "SUSE Linux Enterprise Subscription Management Tool 11",
                                "11", arch, product);
                        }
                        if (sp.equals("2") || sp.equals("3")) {
                            product = j.next();
                            assertMatches(
                                "SUSE Linux Enterprise Subscription Management Tool 11" +
                                " SP" + sp, "11." + sp, arch, product);
                        }
                    }

                    if (sp.equals("2")) {
                        product = j.next();
                        assertMatches("SUSE WebYaST 1.3", "1.3", arch, product);
                    }

                    // note: mono extensions were dropped
                    assertFalse(j.hasNext());
                }

                // SLES 11 for VMWare
                for (String arch : new String[] {"i586", "x86_64"}) {
                    product = i.next();
                    assertMatches("SUSE Linux Enterprise Server 11 SP" + sp + " VMWare",
                            "11." + sp, arch, product);

                    Iterator<ListedProduct> j = product.getExtensions().iterator();
                    product = j.next();
                    assertMatches("SUSE Linux Enterprise High Availability Extension 11 SP"
                            + sp, "11." + sp, arch, product);

                    product = j.next();
                    assertMatches("SUSE Linux Enterprise Software Development Kit 11 SP"
                            + sp, "11." + sp, arch, product);

                    if (sp.equals("1")) {
                        product = j.next();
                        assertMatches(
                                "SUSE Linux Enterprise Subscription Management Tool 11",
                                "11", arch, product);
                    }
                    if (sp.equals("2") || sp.equals("3")) {
                        product = j.next();
                        assertMatches(
                                "SUSE Linux Enterprise Subscription Management Tool 11" +
                                " SP" + sp, "11." + sp, arch, product);
                    }

                    if (sp.equals("3")) {
                        product = j.next();
                        assertMatches("SUSE WebYaST 1.3", "1.3", arch, product);
                    }

                    assertFalse(j.hasNext());
                }
            }

            // SUSE Manager
            for (String version : new String[]{"1.2", "1.7", "2.1"}) {
                product = i.next();
                assertMatches("SUSE Manager Proxy " + version, version, "x86_64", product);
            }
            for (String arch : new String[]{"s390x", "x86_64"}) {
                product = i.next();
                assertMatches("SUSE Manager Server 2.1", "2.1", arch, product);
            }

            // SUSE Studio
            product = i.next();
            assertMatches("SUSE Studio OnSite 1.3", "1.3", "x86_64", product);

            assertFalse(i.hasNext());
        }
        finally {
            SUSEProductTestUtils.deleteIfTempFile(productsJSON);
            SUSEProductTestUtils.deleteIfTempFile(channelsXML);
        }
    }

    /**
     * Asserts that a product matches some fields.
     *
     * @param friendlyName the friendly name
     * @param version the version
     * @param arch the arch
     * @param product the product
     */
    private void assertMatches(String friendlyName, String version, String arch,
            ListedProduct product) {
        assertEquals(friendlyName, product.getFriendlyName());
        assertEquals(version, product.getVersion());
        assertEquals(arch, product.getArch());
    }
}
