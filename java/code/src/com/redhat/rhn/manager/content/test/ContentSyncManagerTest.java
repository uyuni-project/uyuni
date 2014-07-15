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
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.channel.test.ChannelFamilyFactoryTest;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductChannel;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
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
        // Create a test channel and set a specific label
        String channelLabel = "sles11-sp3-pool-x86_64";
        Channel c = createTestVendorChannel();
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
        File channelsXML = getTestFile("channels.xml");
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

        // Delete test file from /tmp
        deleteIfTempFile(channelsXML);
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
     * Test for {@link ContentSyncManager#updateSubscriptions()
     * @throws Exception
     */
    public void testUpdateSubscriptions() throws Exception {
        SCCSubscription subscription = new SCCSubscription();
        subscription.setName("SUSE Manager Management Unlimited Virtual Machines License");
        subscription.setSystemsCount(0);
        subscription.setSystemLimit(1);
        subscription.setProductClasses(new ArrayList<String>(){
            {add("SM_ENT_MGM_V");add("SMS");}});
        subscription.setType("full");
        subscription.setStartsAt("2013-12-12T00:00:00.000Z");
        subscription.setExpiresAt("2016-12-12T00:00:00.000Z");
        subscription.setId(652);
        List<SCCSubscription> subscriptions = new ArrayList<SCCSubscription>();
        subscriptions.add(subscription);

        File channelsXML = getTestFile("channels.xml");
        File channelFamiliesXML = getTestFile("channel_families.xml");

        ContentSyncManager csm = new ContentSyncManager();
        csm.setChannelsXML(channelsXML);
        csm.setChannelFamiliesXML(channelFamiliesXML);
        csm.updateSubscriptions(subscriptions);

        deleteIfTempFile(channelsXML);
        deleteIfTempFile(channelFamiliesXML);
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
     * Test for {@link ContentSyncManager#syncSUSEProductChannels()}.
     * @throws Exception
     */
    public void testSyncSUSEProductChannels() throws Exception {
        // Setup a product in the database
        Channel channel = createTestVendorChannel();
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
        new ContentSyncManager().syncSUSEProductChannels(availableChannels);

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
     * Test for {@link ContentSyncManager#updateChannelFamilies() method.
     * @throws Exception
     */
    public void testUpdateChannelFamilies() throws Exception {
        File channelFamiliesXML = getTestFile("channel_families.xml");
        ContentSyncManager csm = new ContentSyncManager();
        csm.setChannelFamiliesXML(channelFamiliesXML);

        try {
            // Prepare private families data (i.e. no private families)
            for (MgrSyncChannelFamily scf : csm.readChannelFamilies()) {
                ChannelFamily f = ChannelFamilyFactory.lookupByLabel(scf.getLabel(), null);
                assertNotNull(f);
                if (!f.getPrivateChannelFamilies().isEmpty()) {
                    f.getPrivateChannelFamilies().clear();
                }
                assertTrue(f.getPrivateChannelFamilies().isEmpty());
                ChannelFamilyFactory.save(f);
            }

            csm.updateChannelFamilies();

            for (MgrSyncChannelFamily scf : csm.readChannelFamilies()) {
                ChannelFamily f = ChannelFamilyFactory.lookupByLabel(scf.getLabel(), null);
                assertNotNull(f);
                assertFalse(f.getPrivateChannelFamilies().isEmpty());
                assertEquals(scf.getDefaultNodeCount() < 0 ? 200000L : 0L,
                     (long) f.getPrivateChannelFamilies().iterator().next().getMaxMembers());
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
        ContentSyncManager csm = new ContentSyncManager();
        csm.setUpgradePathsXML(upgradePathsXML);
        csm.updateUpgradePaths();

        deleteIfTempFile(upgradePathsXML);
    }

    /**
     * Create a vendor channel (org is null) for testing.
     * @return vendor channel for testing
     * @throws Exception (FIXME)
     */
    public static Channel createTestVendorChannel() throws Exception {
        Channel c =  ChannelFactoryTest.createTestChannel(null,
                ChannelFamilyFactoryTest.createTestChannelFamily());
        ChannelFactory.save(c);
        return c;
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
