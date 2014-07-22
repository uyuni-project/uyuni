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

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.channel.ContentSource;
import com.redhat.rhn.domain.channel.PrivateChannelFamily;
import com.redhat.rhn.domain.channel.test.ChannelFamilyFactoryTest;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductChannel;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.SUSEUpgradePath;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.EntitlementServerGroup;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.ServerGroupType;
import com.redhat.rhn.manager.content.ConsolidatedSubscriptions;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;

import com.suse.mgrsync.MgrSyncChannel;
import com.suse.mgrsync.MgrSyncChannelFamily;
import com.suse.mgrsync.MgrSyncProduct;
import com.suse.scc.model.SCCProduct;
import com.suse.scc.model.SCCSubscription;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Tests for {@link ContentSyncManager}.
 */
public class ContentSyncManagerTest extends RhnBaseTestCase {

    /**
     * Test for {@link ContentSyncManager#updateChannels()}.
     * @throws Exception
     */
    public void testUpdateChannels() throws Exception {
        File channelsXML = getTestFile("channels.xml");
        try {
            // Create a test channel and set a specific label
            String channelLabel = "sles11-sp3-pool-x86_64";
            Channel c = SUSEProductTestUtils.createTestVendorChannel();
            c.setLabel(channelLabel);
            c.setDescription("UPDATE ME!");
            c.setName("UPDATE ME!");
            c.setSummary("UPDATE ME!");
            c.setUpdateTag("UPDATE ME!");

            // Setup content source
            ContentSource cs = new ContentSource();
            cs.setLabel(c.getLabel());
            cs.setSourceUrl("UPDATE ME!");
            cs.setType(ChannelFactory.CONTENT_SOURCE_TYPE_YUM);
            cs.setOrg(null);
            cs = (ContentSource) TestUtils.saveAndReload(cs);
            c.getSources().add(cs);
            TestUtils.saveAndFlush(c);

            // Update the channel information
            ContentSyncManager csm = new ContentSyncManager();
            csm.setChannelsXML(channelsXML);
            csm.updateChannels();

            // Verify channel attributes
            c = ChannelFactory.lookupByLabel(channelLabel);
            assertEquals("SUSE Linux Enterprise Server 11 SP3", c.getDescription());
            assertEquals("SLES11-SP3-Pool for x86_64", c.getName());
            assertEquals("SUSE Linux Enterprise Server 11 SP3", c.getSummary());
            assertEquals("slessp3", c.getUpdateTag());

            // Verify content sources (there is only one)
            Set<ContentSource> sources = c.getSources();
            for (ContentSource s : sources) {
                assertEquals("https://nu.novell.com/repo/$RCE/SLES11-SP3-Pool/sle-11-x86_64/",
                        s.getSourceUrl());
            }
        }
        finally {
            deleteIfTempFile(channelsXML);
        }
    }

    /**
     * Test for {@link ContentSyncManager#updateSUSEProducts()} inserting a new product.
     * @throws Exception
     */
    public void testUpdateSUSEProductsNew() throws Exception {
        // Create test product attributes
        int productId = 12345;
        assertNull(SUSEProductFactory.lookupByProductId(productId));
        String name = TestUtils.randomString();
        String version = TestUtils.randomString();
        String releaseType = TestUtils.randomString();
        String friendlyName = TestUtils.randomString();
        String productClass = TestUtils.randomString();

        // Setup a product as it comes from SCC
        SCCProduct p = new SCCProduct();
        p.setId(productId);
        p.setName(name);
        p.setVersion(version);
        p.setReleaseType(releaseType);
        p.setFriendlyName(friendlyName);
        p.setProductClass(productClass);
        p.setArch("i686");
        List<SCCProduct> products = new ArrayList<SCCProduct>();
        products.add(p);

        // Call updateSUSEProducts()
        ContentSyncManager csm = new ContentSyncManager();
        csm.updateSUSEProducts(products);

        // Verify that a new product has been created correctly
        SUSEProduct suseProduct = SUSEProductFactory.lookupByProductId(productId);
        assertEquals(name.toLowerCase(), suseProduct.getName());
        assertEquals(version.toLowerCase(), suseProduct.getVersion());
        assertEquals(releaseType.toLowerCase(), suseProduct.getRelease());
        assertEquals(friendlyName, suseProduct.getFriendlyName());
        assertEquals(PackageFactory.lookupPackageArchByLabel("i686"),
                suseProduct.getArch());

        // Verify that a new channel family has been created correctly
        ChannelFamily cf = ChannelFamilyFactory.lookupByLabel(productClass, null);
        assertNotNull(cf);
        assertEquals(cf.getId().toString(), suseProduct.getChannelFamilyId());
    }

    /**
     * Test for {@link ContentSyncManager#updateSUSEProducts()} update a product.
     * @throws Exception
     */
    public void testUpdateSUSEProductsUpdate() throws Exception {
        // Create test product attributes
        int productId = 12345;
        assertNull(SUSEProductFactory.lookupByProductId(productId));
        String name = TestUtils.randomString().toLowerCase();
        String version = TestUtils.randomString().toLowerCase();
        String releaseType = TestUtils.randomString().toLowerCase();
        String friendlyName = TestUtils.randomString();

        // Store a SUSE product with those attributes
        SUSEProduct suseProduct = new SUSEProduct();
        suseProduct.setName(name);
        suseProduct.setVersion(version);
        suseProduct.setRelease(releaseType);
        suseProduct.setFriendlyName(friendlyName);
        suseProduct.setProductId(productId);
        PackageArch arch = PackageFactory.lookupPackageArchByLabel("i686");
        suseProduct.setArch(arch);
        suseProduct.setProductList('Y');
        SUSEProductFactory.save(suseProduct);

        // Setup SCC product accordingly
        SCCProduct p = new SCCProduct();
        p.setId(productId);
        p.setName(name);
        p.setVersion(version);
        p.setReleaseType(releaseType);
        p.setArch("i686");
        String productClass = TestUtils.randomString();
        p.setProductClass(productClass);

        // Set a new friendly name that should be updated
        String friendlyNameNew = TestUtils.randomString();
        p.setFriendlyName(friendlyNameNew);
        List<SCCProduct> products = new ArrayList<SCCProduct>();
        products.add(p);

        // Call updateSUSEProducts()
        ContentSyncManager csm = new ContentSyncManager();
        csm.updateSUSEProducts(products);

        // Verify that the product has been updated correctly
        suseProduct = SUSEProductFactory.lookupByProductId(productId);
        assertEquals(friendlyNameNew, suseProduct.getFriendlyName());

        // Verify that a new channel family has been created correctly
        ChannelFamily cf = ChannelFamilyFactory.lookupByLabel(productClass, null);
        assertNotNull(cf);
        assertEquals(cf.getId().toString(), suseProduct.getChannelFamilyId());
    }

    /**
     * Test for {@link ContentSyncManager#consolidateSubscriptions(java.util.Collection)}.
     * @throws Exception
     */
    public void testConsolidateSubscriptions() throws Exception {
        SCCSubscription subscription = new SCCSubscription();
        List<String> productClasses = new ArrayList<String>();
        productClasses.add("SMS");
        productClasses.add("SM_ENT_MGM_V");
        subscription.setProductClasses(productClasses);
        subscription.setType("full");
        subscription.setStartsAt("2013-12-12T00:00:00.000Z");
        subscription.setExpiresAt("2016-12-12T00:00:00.000Z");
        List<SCCSubscription> subscriptions = new ArrayList<SCCSubscription>();
        subscriptions.add(subscription);

        // Check the consolidated product classes
        ContentSyncManager csm = new ContentSyncManager();
        File channelFamiliesXML = getTestFile("channel_families.xml");
        csm.setChannelFamiliesXML(channelFamiliesXML);
        ConsolidatedSubscriptions result = csm.consolidateSubscriptions(subscriptions);
        List<String> entitlements = result.getSystemEntitlements();
        assertEquals(1, entitlements.size());
        assertTrue(entitlements.contains("SM_ENT_MGM_V"));
        List<String> channelSubscriptions = result.getChannelSubscriptions();
        assertEquals(2, channelSubscriptions.size());
        assertTrue(channelSubscriptions.contains("SMS"));
        // SLESMT is a free product class
        assertTrue(channelSubscriptions.contains("SLESMT"));

        // Delete temp file
        deleteIfTempFile(channelFamiliesXML);
    }

    /**
     * Test for {@link ContentSyncManager#updateSystemEntitlements(List)}.
     * @throws Exception
     */
    public void testUpdateSystemEntitlements() throws Exception {
        // Start with no subscribed product classes
        List<String> productClasses = new ArrayList<String>();

        // Update system entitlements
        ContentSyncManager csm = new ContentSyncManager();
        csm.updateSystemEntitlements(productClasses);

        // Check reset to 10
        ServerGroupType sgt = ServerFactory.lookupServerGroupTypeByLabel(
                "bootstrap_entitled");
        EntitlementServerGroup serverGroup = ServerGroupFactory.lookupEntitled(
                OrgFactory.getSatelliteOrg(), sgt);
        assertEquals(new Long(10), serverGroup.getMaxMembers());

        // Add subscription for product class
        productClasses.add("SM_ENT_MGM_V");

        // Update system entitlements and check max_members = 200000
        csm.updateSystemEntitlements(productClasses);
        serverGroup = ServerGroupFactory.lookupEntitled(
                OrgFactory.getSatelliteOrg(), sgt);
        assertEquals(new Long(200000), serverGroup.getMaxMembers());
    }

    /**
     * Test for {@link ContentSyncManager#updateChannelSubscriptions(List)}.
     * @throws Exception
     */
    public void testUpdateChannelSubscriptions() throws Exception {
        // Start with no subscribed product classes
        List<String> productClasses = new ArrayList<String>();

        // Update channel subscriptions
        ContentSyncManager csm = new ContentSyncManager();
        csm.updateChannelSubscriptions(productClasses);

        // Check reset to 0
        ChannelFamily family = ChannelFamilyFactory.lookupByLabel("SMS", null);
        for (PrivateChannelFamily pcf : family.getPrivateChannelFamilies()) {
            assertEquals(new Long(0), pcf.getMaxMembers());
        }

        // Add subscription for a product class
        productClasses.add("SMS");

        // Update subscriptions and check max_members = 200000
        csm.updateChannelSubscriptions(productClasses);
        family = ChannelFamilyFactory.lookupByLabel("SMS", null);
        for (PrivateChannelFamily pcf : family.getPrivateChannelFamilies()) {
            assertEquals(new Long(200000), pcf.getMaxMembers());
        }
    }

    /**
     * Test for {@link ContentSyncManager#getAvailableChannels()}.
     * @throws Exception
     */
    public void testGetAvailableChannels() throws Exception {
        // Create channel family with availability
        ChannelFamily channelFamily1 = ChannelFamilyFactoryTest.createTestChannelFamily();
        channelFamily1.setOrg(null);
        TestUtils.saveAndFlush(channelFamily1);

        // Create channel family with no availability
        ChannelFamily channelFamily2 = ChannelFamilyFactoryTest.createTestChannelFamily();
        channelFamily2.setOrg(null);
        for (PrivateChannelFamily pcf : channelFamily2.getPrivateChannelFamilies()) {
            pcf.setMaxMembers(0L);
            pcf.setMaxFlex(0L);
            TestUtils.saveAndFlush(pcf);
        }
        TestUtils.saveAndFlush(channelFamily2);

        // Create c1 as a base channel and c2 as a child of it
        MgrSyncChannel c1 = new MgrSyncChannel();
        c1.setFamily(channelFamily1.getLabel());
        String baseChannelLabel = TestUtils.randomString();
        c1.setLabel(baseChannelLabel);
        c1.setParent("BASE");
        MgrSyncChannel c2 = new MgrSyncChannel();
        c2.setFamily(channelFamily1.getLabel());
        c2.setLabel(TestUtils.randomString());
        c2.setParent(baseChannelLabel);

        // Create c3 to test no availability
        MgrSyncChannel c3 = new MgrSyncChannel();
        c3.setFamily(channelFamily2.getLabel());
        c3.setLabel(TestUtils.randomString());

        // Create c4 with unknown channel family
        MgrSyncChannel c4 = new MgrSyncChannel();
        c4.setFamily(TestUtils.randomString());
        c4.setLabel(TestUtils.randomString());

        // Put all channels together to a list
        List<MgrSyncChannel> allChannels = new ArrayList<MgrSyncChannel>();
        allChannels.add(c1);
        allChannels.add(c2);
        allChannels.add(c3);
        allChannels.add(c4);

        // Available: c1 and c2. Not available: c3 and c4.
        ContentSyncManager csm = new ContentSyncManager();
        List<MgrSyncChannel> availableChannels = csm.getAvailableChannels(allChannels);
        assertTrue(availableChannels.contains(c1));
        assertTrue(availableChannels.contains(c2));
        assertFalse(availableChannels.contains(c3));
        assertFalse(availableChannels.contains(c4));
    }

    /**
     * Test for {@link ContentSyncManager#updateSUSEProductChannels()}.
     * @throws Exception
     */
    public void testUpdateSUSEProductChannels() throws Exception {
        // Setup a product in the database
        Channel channel = SUSEProductTestUtils.createTestVendorChannel();
        ChannelFamily family = channel.getChannelFamily();
        SUSEProduct product = SUSEProductTestUtils.createTestSUSEProduct(family);
        MgrSyncProduct mgrSyncProduct = new MgrSyncProduct();
        mgrSyncProduct.setId(product.getProductId());

        // Create a channel belonging to that product and assume it's available
        MgrSyncChannel c1 = new MgrSyncChannel();
        c1.setFamily(family.getLabel());
        c1.setLabel(channel.getLabel());
        List<MgrSyncProduct> productList = new ArrayList<MgrSyncProduct>();
        productList.add(mgrSyncProduct);
        c1.setProducts(productList);

        // Create a product channel that we can verify
        SUSEProductChannel spc1 = new SUSEProductChannel();
        spc1.setChannel(channel);
        spc1.setChannelLabel(channel.getLabel());
        spc1.setProduct(product);

        // Create a product channel that should be removed after sync
        SUSEProductChannel spc2 = new SUSEProductChannel();
        spc2.setChannelLabel(TestUtils.randomString());
        spc2.setProduct(product);
        TestUtils.saveAndFlush(spc2);

        // Setup available channels list
        List<MgrSyncChannel> availableChannels = new ArrayList<MgrSyncChannel>();
        availableChannels.add(c1);
        new ContentSyncManager().updateSUSEProductChannels(availableChannels);

        // Get all product channel relationships and verify
        List<SUSEProductChannel> productChannels = SUSEProductFactory.
                findAllSUSEProductChannels();
        assertEquals(availableChannels.size(), productChannels.size());
        assertTrue(productChannels.contains(spc1));
        assertFalse(productChannels.contains(spc2));

        // Verify the single attributes
        SUSEProductChannel actual = productChannels.get(productChannels.indexOf(spc1));
        assertEquals(channel, actual.getChannel());
        assertEquals(spc1.getChannelLabel(), actual.getChannelLabel());
        assertNull(actual.getParentChannelLabel());
        assertEquals(product, actual.getProduct());
    }

    /**
     * Test for {@link ContentSyncManager#updateChannelFamilies() method, insert case.
     * @throws Exception
     */
    public void testUpdateChannelFamiliesInsert() throws Exception {
        File channelFamiliesXML = getTestFile("channel_families.xml");
        try {
            // Remove channel families for testing the insertion
            ContentSyncManager csm = new ContentSyncManager();
            csm.setChannelFamiliesXML(channelFamiliesXML);
            for (MgrSyncChannelFamily cf : csm.readChannelFamilies()) {
                ChannelFamily family = ChannelFamilyFactory.lookupByLabel(
                        cf.getLabel(), null);
                if (family != null) {
                    TestUtils.removeObject(family);
                }
            }
            csm.updateChannelFamilies();

            // Assert everything is as expected
            for (MgrSyncChannelFamily cf : csm.readChannelFamilies()) {
                ChannelFamily family = ChannelFamilyFactory.lookupByLabel(
                        cf.getLabel(), null);
                assertNotNull(family);
                assertEquals(cf.getLabel(), family.getLabel());
                assertEquals(cf.getName(), family.getName());
                assertEquals(1, family.getPrivateChannelFamilies().size());
                for (PrivateChannelFamily pcf : family.getPrivateChannelFamilies()) {
                    assertEquals(new Long(1), pcf.getOrg().getId());
                    assertEquals(cf.getDefaultNodeCount() < 0 ? 200000L : 0L,
                            (long) pcf.getMaxMembers());
                }
            }
        }
        finally {
            deleteIfTempFile(channelFamiliesXML);
        }
    }

    /**
     * Test for {@link ContentSyncManager#updateChannelFamilies() method, update case.
     * @throws Exception
     */
    public void testUpdateChannelFamiliesUpdate() throws Exception {
        File channelFamiliesXML = getTestFile("channel_families.xml");
        try {
            // Insert channel families with different data for testing update
            ContentSyncManager csm = new ContentSyncManager();
            csm.setChannelFamiliesXML(channelFamiliesXML);
            for (MgrSyncChannelFamily cf : csm.readChannelFamilies()) {
                ChannelFamily family = ChannelFamilyFactory.lookupByLabel(
                        cf.getLabel(), null);
                if (family != null) {
                    family.setLabel(TestUtils.randomString());
                    family.setName(TestUtils.randomString());
                }
                else {
                    family = ChannelFamilyFactoryTest.createTestChannelFamily();
                }

                // Remove all private channel families
                if (!family.getPrivateChannelFamilies().isEmpty()) {
                    family.getPrivateChannelFamilies().clear();
                }
                TestUtils.saveAndFlush(family);
            }
            csm.updateChannelFamilies();

            // Assert everything is as expected
            for (MgrSyncChannelFamily cf : csm.readChannelFamilies()) {
                ChannelFamily family = ChannelFamilyFactory.lookupByLabel(
                        cf.getLabel(), null);
                assertNotNull(family);
                assertEquals(cf.getLabel(), family.getLabel());
                assertEquals(cf.getName(), family.getName());
                assertEquals(1, family.getPrivateChannelFamilies().size());
                for (PrivateChannelFamily pcf : family.getPrivateChannelFamilies()) {
                    assertEquals(new Long(1), pcf.getOrg().getId());
                    assertEquals(cf.getDefaultNodeCount() < 0 ? 200000L : 0L,
                            (long) pcf.getMaxMembers());
                }
            }
        }
        finally {
            deleteIfTempFile(channelFamiliesXML);
        }
    }

    /**
     * Update the upgrade paths test.
     * @throws Exception
     */
    public void testUpdateUpgradePaths() throws Exception {
        File upgradePathsXML = getTestFile("upgrade_paths.xml");
        try {
            // Prepare products since they will be looked up
            ChannelFamily family = ChannelFamilyFactoryTest.createTestChannelFamily();
            SUSEProduct p;
            if (SUSEProductFactory.lookupByProductId(690) == null) {
                p = SUSEProductTestUtils.createTestSUSEProduct(family);
                p.setProductId(690);
                TestUtils.saveAndFlush(p);
            }
            if (SUSEProductFactory.lookupByProductId(814) == null) {
                p = SUSEProductTestUtils.createTestSUSEProduct(family);
                p.setProductId(814);
                TestUtils.saveAndFlush(p);
            }
            if (SUSEProductFactory.lookupByProductId(1001) == null) {
                p = SUSEProductTestUtils.createTestSUSEProduct(family);
                p.setProductId(1001);
                TestUtils.saveAndFlush(p);
            }
            if (SUSEProductFactory.lookupByProductId(1119) == null) {
                p = SUSEProductTestUtils.createTestSUSEProduct(family);
                p.setProductId(1119);
                TestUtils.saveAndFlush(p);
            }
            if (SUSEProductFactory.lookupByProductId(1193) == null) {
                p = SUSEProductTestUtils.createTestSUSEProduct(family);
                p.setProductId(1193);
                TestUtils.saveAndFlush(p);
            }
            if (SUSEProductFactory.lookupByProductId(1198) == null) {
                p = SUSEProductTestUtils.createTestSUSEProduct(family);
                p.setProductId(1198);
                TestUtils.saveAndFlush(p);
            }

            // Update the upgrade paths
            ContentSyncManager csm = new ContentSyncManager();
            csm.setUpgradePathsXML(upgradePathsXML);
            csm.updateUpgradePaths();

            // Check the results
            List<SUSEUpgradePath> upgradePaths = SUSEProductFactory.findAllSUSEUpgradePaths();
            List<String> paths = new ArrayList<String>();
            for (SUSEUpgradePath path : upgradePaths) {
                String identifier = String.format("%s-%s",
                        path.getFromProduct().getProductId(),
                        path.getToProduct().getProductId());
                paths.add(identifier);
            }
            assertEquals(3, upgradePaths.size());
            assertTrue(paths.contains("690-814"));
            assertTrue(paths.contains("1001-1119"));
            assertTrue(paths.contains("1193-1198"));
        }
        finally {
            deleteIfTempFile(upgradePathsXML);
        }
    }

    /**
     * Finds a given testfile by name and returns it.
     * @param filename name of the testfile
     * @return the file
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private File getTestFile(String filename)
            throws ClassNotFoundException, IOException {
        return new File(TestUtils.findTestData(filename).getPath());
    }

    /**
     * For a given file, delete it in case it is a temp file.
     * @param file test file to delete
     */
    private void deleteIfTempFile(File file) {
        if (file.exists() && file.getAbsolutePath().startsWith(
                System.getProperty("java.io.tmpdir") + File.separatorChar)) {
            file.delete();
        }
    }
}
