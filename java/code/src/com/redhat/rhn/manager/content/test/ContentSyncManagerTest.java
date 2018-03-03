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

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.channel.ContentSource;
import com.redhat.rhn.domain.channel.test.ChannelFamilyFactoryTest;
import com.redhat.rhn.domain.channel.test.ChannelFamilyTest;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductChannel;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.SUSEUpgradePath;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCOrderItem;
import com.redhat.rhn.domain.scc.SCCRepository;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.content.MgrSyncProductDto;
import com.redhat.rhn.manager.content.MgrSyncUtils;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.mgrsync.MgrSyncStatus;
import com.suse.mgrsync.XMLChannel;
import com.suse.mgrsync.XMLChannelFamily;
import com.suse.mgrsync.XMLProduct;
import com.suse.scc.model.SCCProduct;
import com.suse.scc.model.SCCSubscription;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Tests for {@link ContentSyncManager}.
 */
public class ContentSyncManagerTest extends BaseTestCaseWithUser {

    // Files we read
    private static final String JARPATH = "/com/redhat/rhn/manager/content/test/";
    private static final String CHANNELS_XML = JARPATH + "channels.xml";
    private static final String UPGRADE_PATHS_XML = JARPATH + "upgrade_paths.xml";
    private static final String UPGRADE_PATHS_EMPTY_XML = JARPATH + "upgrade_paths_empty.xml";

    private static final String SUBSCRIPTIONS_JSON = "organizations_subscriptions.json";
    private static final String ORDERS_JSON = "organizations_orders.json";

    public void testListSubscriptionsCaching() throws Exception {
        File subJson = new File(TestUtils.findTestData(
                new File(JARPATH,  "sccdata/" + SUBSCRIPTIONS_JSON).getAbsolutePath()).getPath());
        File orderJson = new File(TestUtils.findTestData(
                new File(JARPATH, "sccdata/" + ORDERS_JSON).getAbsolutePath()).getPath());
        Path fromdir = Files.createTempDirectory("sumatest");
        File subtempFile = new File(fromdir.toString(), SUBSCRIPTIONS_JSON);
        File ordertempFile = new File(fromdir.toString(), ORDERS_JSON);
        Files.copy(subJson.toPath(), subtempFile.toPath());
        Files.copy(orderJson.toPath(), ordertempFile.toPath());
        try {
            Config.get().setString(ContentSyncManager.RESOURCE_PATH, fromdir.toString());

            SUSEProductTestUtils.clearAllProducts();
            SUSEProductTestUtils.createVendorSUSEProducts();
            ContentSyncManager cm = new ContentSyncManager();
            Collection<SCCSubscription> s = cm.getSubscriptions();
            assertNotNull(s);

            for (com.redhat.rhn.domain.scc.SCCSubscription dbs : SCCCachingFactory.lookupSubscriptions()) {
                assertEquals("55REGCODE180", dbs.getRegcode());
                assertEquals("EMEA SLES x86/x86_64 Standard Support & Training", dbs.getName());
                assertEquals(1234, dbs.getSccId());
                assertEquals("ACTIVE", dbs.getStatus());
                assertContains(dbs.getProducts(), SUSEProductFactory.lookupByProductId(1322));
                assertContains(dbs.getProducts(), SUSEProductFactory.lookupByProductId(1324));
                assertEquals(702, dbs.getSystemLimit().longValue());
                assertEquals(Date.from(Instant.parse("2017-12-31T00:00:00.000Z")),
                        dbs.getExpiresAt());
            }
            s = cm.getSubscriptions();
            HibernateFactory.getSession().flush();
            for (SCCOrderItem item : SCCCachingFactory.lookupOrderItems()) {
                if (item.getSccId() == 9998L) {
                    assertEquals(10, item.getQuantity().longValue());
                    assertEquals(1234, item.getSubscriptionId().longValue());
                    assertEquals(Date.from(Instant.parse("2013-01-01T00:00:00.000Z")),
                            item.getEndDate());
                    assertEquals("662644474670", item.getSku());
                }
                else if (item.getSccId() == 9999L) {
                    assertEquals(100, item.getQuantity().longValue());
                    assertEquals(1234, item.getSubscriptionId().longValue());
                    assertEquals(Date.from(Instant.parse("2017-01-01T00:00:00.000Z")),
                            item.getEndDate());
                    assertEquals("874-005117", item.getSku());
                }
                else {
                    fail();
                }
            }
        }
        finally {
            Config.get().remove(ContentSyncManager.RESOURCE_PATH);
            SUSEProductTestUtils.deleteIfTempFile(subJson);
            SUSEProductTestUtils.deleteIfTempFile(orderJson);
            subtempFile.delete();
            ordertempFile.delete();
            fromdir.toFile().delete();
        }
    }

    /**
     * Test for {@link ContentSyncManager#updateChannels}.
     * @throws Exception if anything goes wrong
     */
    public void testUpdateChannels() throws Exception {
        File channelsXML = new File(TestUtils.findTestData(CHANNELS_XML).getPath());
        try {
            // Make sure that channel family "7261" exists
            User admin = UserTestUtils.createUserInOrgOne();
            ChannelFamilyTest.ensureChannelFamilyExists(admin, "7261");

            // Create a test channel and set a label that exists in the xml file
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
            cs.setType(ChannelFactory.lookupContentSourceType("yum"));
            cs.setOrg(null);
            cs = (ContentSource) TestUtils.saveAndReload(cs);
            c.getSources().add(cs);
            TestUtils.saveAndFlush(c);

            // Save SCC repo to the cache
            SCCRepository repo = new SCCRepository();
            repo.setUrl("https://updates.suse.com/repo/$RCE/SLES11-SP3-Pool/sle-11-x86_64");
            SCCCachingFactory.saveRepository(repo);

            // Update channel information from the xml file
            ContentSyncManager csm = new ContentSyncManager();
            ContentSyncManager.setChannelsXML(channelsXML);
            csm.updateChannelsInternal(null);

            // Verify channel attributes
            c = ChannelFactory.lookupByLabel(channelLabel);
            assertEquals("SUSE Linux Enterprise Server 11 SP3 x86_64", c.getDescription());
            assertEquals("SLES11-SP3-Pool for x86_64", c.getName());
            assertEquals("SUSE Linux Enterprise Server 11 SP3 x86_64", c.getSummary());
            assertEquals("slessp3", c.getUpdateTag());

            // Verify content sources (there is only one)
            Set<ContentSource> sources = c.getSources();
            for (ContentSource s : sources) {
                String url = "https://updates.suse.com/repo/$RCE/SLES11-SP3-Pool/"
                        + "sle-11-x86_64";
                assertEquals(url, s.getSourceUrl());
            }
        }
        finally {
            SUSEProductTestUtils.deleteIfTempFile(channelsXML);
        }
    }

    /**
     * Test for {@link ContentSyncManager#updateChannels}.
     * @throws Exception if anything goes wrong
     */
    public void testUpdateChannelsWithSimilarPath() throws Exception {
        File channelsXML = new File(TestUtils.findTestData(CHANNELS_XML).getPath());
        try {
            // Make sure that channel family "7261" exists
            User admin = UserTestUtils.createUserInOrgOne();
            ChannelFamilyTest.ensureChannelFamilyExists(admin, "7261");

            // Create a test channel and set a label that exists in the xml file
            String channelLabel = "sles12-sp1-debuginfo-pool-x86_64";
            Channel c = SUSEProductTestUtils.createTestVendorChannel();
            c.setLabel(channelLabel);
            c.setDescription("UPDATE ME!");
            c.setName("UPDATE ME!");
            c.setSummary("UPDATE ME!");
            c.setUpdateTag("UPDATE ME!");

            // Setup content source
            ContentSource cs = new ContentSource();
            cs.setLabel(c.getLabel());
            cs.setSourceUrl("https://updates.suse.com/SUSE/Products/SLE-SERVER/12-SP1/x86_64/product_debug?update-me");
            cs.setType(ChannelFactory.lookupContentSourceType("yum"));
            cs.setOrg(null);
            ChannelFactory.save(cs);
            cs = (ContentSource) TestUtils.saveAndReload(cs);
            c.getSources().add(cs);
            TestUtils.saveAndFlush(c);

            String channelLabel2 = "sles12-sp1-pool-x86_64";
            Channel c2 = SUSEProductTestUtils.createTestVendorChannel();
            c2.setLabel(channelLabel2);
            c2.setDescription("UPDATE ME 2!");
            c2.setName("UPDATE ME 2!");
            c2.setSummary("UPDATE ME 2!");
            c2.setUpdateTag("UPDATE ME 2!");

            // Setup content source
            ContentSource cs2 = new ContentSource();
            cs2.setLabel(c2.getLabel());
            cs2.setSourceUrl("https://updates.suse.com/SUSE/Products/SLE-SERVER/12-SP1/x86_64/product?update-me-too");
            cs2.setType(ChannelFactory.lookupContentSourceType("yum"));
            cs2.setOrg(null);
            ChannelFactory.save(cs2);
            cs2 = (ContentSource) TestUtils.saveAndReload(cs2);
            c2.getSources().add(cs2);
            TestUtils.saveAndFlush(c2);

            // Save SCC repo to the cache
            SCCRepository repo = new SCCRepository();
            repo.setUrl("https://updates.suse.com/SUSE/Products/SLE-SERVER/12-SP1/x86_64/product_debug?qgtMzfPFsLpauJuXu");
            SCCCachingFactory.saveRepository(repo);

            SCCRepository repo2 = new SCCRepository();
            repo2.setUrl("https://updates.suse.com/SUSE/Products/SLE-SERVER/12-SP1/x86_64/product?kANfkB7wMUO0u4rmgQ80UBe");
            SCCCachingFactory.saveRepository(repo2);

            // Update channel information from the xml file
            ContentSyncManager csm = new ContentSyncManager();
            ContentSyncManager.setChannelsXML(channelsXML);
            csm.updateChannelsInternal(null);

            // Verify channel attributes
            c = ChannelFactory.lookupByLabel(channelLabel);
            assertEquals("SLES12-SP1-Debuginfo-Pool for x86_64", c.getName());
            assertEquals("SUSE Linux Enterprise Server 12 SP1 x86_64", c.getSummary());

            // Verify content sources (there is only one)
            Set<ContentSource> sources = c.getSources();
            for (ContentSource s : sources) {
                String url = "https://updates.suse.com/SUSE/Products/SLE-SERVER/12-SP1/x86_64/product_debug?qgtMzfPFsLpauJuXu";
                assertEquals(url, s.getSourceUrl());
            }

            c2 = ChannelFactory.lookupByLabel(channelLabel2);
            assertEquals("SLES12-SP1-Pool for x86_64", c2.getName());
            assertEquals("SUSE Linux Enterprise Server 12 SP1 x86_64", c2.getSummary());

            // Verify content sources (there is only one)
            Set<ContentSource> sources2 = c2.getSources();
            for (ContentSource s2 : sources2) {
                String url = "https://updates.suse.com/SUSE/Products/SLE-SERVER/12-SP1/x86_64/product?kANfkB7wMUO0u4rmgQ80UBe";
                assertEquals(url, s2.getSourceUrl());
            }
        }
        finally {
            SUSEProductTestUtils.deleteIfTempFile(channelsXML);
        }
    }

    /**
     * Test for {@link ContentSyncManager#updateChannels}. Fix invalid assignment
     * @throws Exception if anything goes wrong
     */
    public void testUpdateChannelsFixInvalidAssignment() throws Exception {
        File channelsXML = new File(TestUtils.findTestData(CHANNELS_XML).getPath());
        try {
            // Make sure that channel family "7261" exists
            User admin = UserTestUtils.createUserInOrgOne();
            ChannelFamilyTest.ensureChannelFamilyExists(admin, "7261");

            // Create a test channel and set a label that exists in the xml file
            String channelLabel = "sles12-sp1-debuginfo-pool-x86_64";
            Channel c = SUSEProductTestUtils.createTestVendorChannel();
            c.setLabel(channelLabel);
            c.setDescription("UPDATE ME!");
            c.setName("UPDATE ME!");
            c.setSummary("UPDATE ME!");
            c.setUpdateTag("UPDATE ME!");

            // Setup content source
            ContentSource cs = new ContentSource();
            cs.setLabel(c.getLabel());
            cs.setSourceUrl("https://updates.suse.com/SUSE/Products/SLE-SERVER/12-SP1/x86_64/product_debug?update-me");
            cs.setType(ChannelFactory.lookupContentSourceType("yum"));
            cs.setOrg(null);
            ChannelFactory.save(cs);
            cs = (ContentSource) TestUtils.saveAndReload(cs);
            c.getSources().add(cs);
            TestUtils.saveAndFlush(c);

            String channelLabel2 = "sles12-sp1-pool-x86_64";
            Channel c2 = SUSEProductTestUtils.createTestVendorChannel();
            c2.setLabel(channelLabel2);
            c2.setDescription("UPDATE ME 2!");
            c2.setName("UPDATE ME 2!");
            c2.setSummary("UPDATE ME 2!");
            c2.setUpdateTag("UPDATE ME 2!");

            c2.getSources().add(cs);
            TestUtils.saveAndFlush(c2);

            // Save SCC repo to the cache
            SCCRepository repo = new SCCRepository();
            repo.setUrl("https://updates.suse.com/SUSE/Products/SLE-SERVER/12-SP1/x86_64/product_debug?qgtMzfPFsLpauJuXu");
            SCCCachingFactory.saveRepository(repo);

            SCCRepository repo2 = new SCCRepository();
            repo2.setUrl("https://updates.suse.com/SUSE/Products/SLE-SERVER/12-SP1/x86_64/product?kANfkB7wMUO0u4rmgQ80UBe");
            SCCCachingFactory.saveRepository(repo2);

            // Update channel information from the xml file
            ContentSyncManager csm = new ContentSyncManager();
            ContentSyncManager.setChannelsXML(channelsXML);
            csm.updateChannelsInternal(null);

            // Verify channel attributes
            c = ChannelFactory.lookupByLabel(channelLabel);
            assertEquals("SLES12-SP1-Debuginfo-Pool for x86_64", c.getName());
            assertEquals("SUSE Linux Enterprise Server 12 SP1 x86_64", c.getSummary());

            // Verify content sources (there is only one)
            Set<ContentSource> sources = c.getSources();
            for (ContentSource s : sources) {
                String url = "https://updates.suse.com/SUSE/Products/SLE-SERVER/12-SP1/x86_64/product_debug?qgtMzfPFsLpauJuXu";
                assertEquals(url, s.getSourceUrl());
            }

            c2 = ChannelFactory.lookupByLabel(channelLabel2);
            assertEquals("SLES12-SP1-Pool for x86_64", c2.getName());
            assertEquals("SUSE Linux Enterprise Server 12 SP1 x86_64", c2.getSummary());

            // Verify content sources (there is only one)
            Set<ContentSource> sources2 = c2.getSources();
            for (ContentSource s2 : sources2) {
                String url = "https://updates.suse.com/SUSE/Products/SLE-SERVER/12-SP1/x86_64/product?kANfkB7wMUO0u4rmgQ80UBe";
                assertEquals(url, s2.getSourceUrl());
            }
        }
        finally {
            SUSEProductTestUtils.deleteIfTempFile(channelsXML);
        }
    }

    /**
     * Test for {@link ContentSyncManager#updateSUSEProducts} inserting a new product.
     * @throws Exception if anything goes wrong
     */
    public void testUpdateSUSEProductsNew() throws Exception {
        File upgradePathsXML = new File(
                TestUtils.findTestData(UPGRADE_PATHS_XML).getPath());
        // Create test product attributes
        int productId = 12345;
        assertNull(SUSEProductFactory.lookupByProductId(productId));
        String name = TestUtils.randomString();
        String identifier = TestUtils.randomString();
        String version = TestUtils.randomString();
        String releaseType = TestUtils.randomString();
        String friendlyName = TestUtils.randomString();
        String productClass = TestUtils.randomString();

        // Setup a product as it comes from SCC
        SCCProduct p = new SCCProduct();
        p.setId(productId);
        p.setName(name);
        p.setIdentifier(identifier);
        p.setVersion(version);
        p.setReleaseType(releaseType);
        p.setFriendlyName(friendlyName);
        p.setProductClass(productClass);
        p.setArch("i686");
        List<SCCProduct> products = new ArrayList<SCCProduct>();
        products.add(p);

        // Call updateSUSEProducts()
        ContentSyncManager csm = new ContentSyncManager();
        csm.setUpgradePathsXML(upgradePathsXML);
        csm.updateSUSEProducts(products);

        // Verify that a new product has been created correctly
        SUSEProduct suseProduct = SUSEProductFactory.lookupByProductId(productId);
        assertEquals(identifier.toLowerCase(), suseProduct.getName());
        assertEquals(version.toLowerCase(), suseProduct.getVersion());
        assertEquals(releaseType.toLowerCase(), suseProduct.getRelease());
        assertEquals(friendlyName, suseProduct.getFriendlyName());
        assertEquals(PackageFactory.lookupPackageArchByLabel("i686"),
                suseProduct.getArch());
    }

    /**
     * Test for {@link ContentSyncManager#updateSUSEProducts} update a product.
     * @throws Exception if anything goes wrong
     */
    public void testUpdateSUSEProductsUpdate() throws Exception {
        File upgradePathsXML = new File(
                TestUtils.findTestData(UPGRADE_PATHS_XML).getPath());
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
        SUSEProductFactory.save(suseProduct);

        // Setup SCC product accordingly
        SCCProduct p = new SCCProduct();
        p.setId(productId);
        p.setIdentifier(name);
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
        csm.setUpgradePathsXML(upgradePathsXML);
        csm.updateSUSEProducts(products);

        // Verify that the product has been updated correctly
        suseProduct = SUSEProductFactory.lookupByProductId(productId);
        assertEquals(friendlyNameNew, suseProduct.getFriendlyName());
    }

    /**
     * Test for {@link ContentSyncManager#getAvailableChannels}.
     * @throws Exception if anything goes wrong
     */
    public void testGetAvailableChannels() throws Exception {
        // Create channel family with availability
        ChannelFamily channelFamily1 = ChannelFamilyFactoryTest.createTestChannelFamily();
        channelFamily1.setOrg(null);
        TestUtils.saveAndFlush(channelFamily1);

        // Create channel family with no availability
        ChannelFamily channelFamily2 = ChannelFamilyFactoryTest.createTestChannelFamily();
        channelFamily2.setOrg(null);
        TestUtils.saveAndFlush(channelFamily2);

        Credentials cred = CredentialsFactory.createSCCCredentials();
        cred.setUsername("user1");
        cred.setPassword("pw1");
        CredentialsFactory.storeCredentials(cred);

        SCCRepository r1 = new SCCRepository();
        r1.setUrl("http://scc.domain.top/c1?abcdefg");
        r1.setCredentials(cred);
        r1.setAutorefresh(false);
        SCCCachingFactory.saveRepository(r1);

        SCCRepository r2 = new SCCRepository();
        r2.setUrl("http://scc.domain.top/c2?123456");
        r2.setCredentials(cred);
        r2.setAutorefresh(false);
        SCCCachingFactory.saveRepository(r2);

        // Create c1 as a base channel and c2 as a child of it
        XMLChannel c1 = new XMLChannel();
        c1.setFamily(channelFamily1.getLabel());
        String baseChannelLabel = TestUtils.randomString();
        c1.setLabel(baseChannelLabel);
        c1.setParent("BASE");
        c1.setSourceUrl("http://scc.domain.top/c1");
        XMLChannel c2 = new XMLChannel();
        c2.setFamily(channelFamily1.getLabel());
        c2.setLabel(TestUtils.randomString());
        c2.setParent(baseChannelLabel);
        c2.setSourceUrl("http://scc.domain.top/c2");

        // Create c3 to test no availability
        XMLChannel c3 = new XMLChannel();
        c3.setFamily(channelFamily2.getLabel());
        c3.setLabel(TestUtils.randomString());
        c3.setSourceUrl("http://scc.domain.top/c3");

        // Create c4 with unknown channel family
        XMLChannel c4 = new XMLChannel();
        c4.setFamily(TestUtils.randomString());
        c4.setLabel(TestUtils.randomString());
        c4.setSourceUrl("http://scc.domain.top/c4");

        // Put all channels together to a list
        List<XMLChannel> allChannels = new ArrayList<XMLChannel>();
        allChannels.add(c1);
        allChannels.add(c2);
        allChannels.add(c3);
        allChannels.add(c4);

        // Available: c1 and c2. Not available: c3 and c4.
        ContentSyncManager csm = new ContentSyncManager();
        List<XMLChannel> availableChannels = csm.getAvailableChannels(allChannels);
        assertTrue(availableChannels.contains(c1));
        assertTrue(availableChannels.contains(c2));
        assertFalse(availableChannels.contains(c3));
        assertFalse(availableChannels.contains(c4));
    }

    /**
     * Test for {@link ContentSyncManager#updateSUSEProductChannels}.
     * @throws Exception if anything goes wrong
     */
    public void testUpdateSUSEProductChannels() throws Exception {
        // Setup a product in the database
        Channel channel = SUSEProductTestUtils.createTestVendorChannel();
        ChannelFamily family = channel.getChannelFamily();
        SUSEProduct product = SUSEProductTestUtils.createTestSUSEProduct(family);
        XMLProduct xmlProduct = new XMLProduct();
        xmlProduct.setId(product.getProductId());

        // Create a channel belonging to that product and assume it's available
        XMLChannel c1 = new XMLChannel();
        c1.setFamily(family.getLabel());
        c1.setLabel(channel.getLabel());
        List<XMLProduct> productList = new ArrayList<XMLProduct>();
        productList.add(xmlProduct);
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
        List<XMLChannel> availableChannels = new ArrayList<XMLChannel>();
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
     * Test for {@link ContentSyncManager#updateChannelFamilies} method, insert case.
     * @throws Exception if anything goes wrong
     */
    public void testUpdateChannelFamiliesInsert() throws Exception {
        // Get test data and insert
        List<XMLChannelFamily> channelFamilies = getChannelFamilies();
        ContentSyncManager csm = new ContentSyncManager();
        csm.updateChannelFamilies(channelFamilies);

        // Assert that families have been inserted correctly
        for (XMLChannelFamily cf : channelFamilies) {
            ChannelFamily family = ChannelFamilyFactory.lookupByLabel(
                    cf.getLabel(), null);
            assertNotNull(family);
            assertEquals(cf.getLabel(), family.getLabel());
            assertEquals(cf.getName(), family.getName());
            assertNotNull(family.getPublicChannelFamily());
        }
    }

    /**
     * Test for {@link ContentSyncManager#updateChannelFamilies} method, update case.
     * @throws Exception if anything goes wrong
     */
    public void testUpdateChannelFamiliesUpdate() throws Exception {
        // Get test data and insert
        List<XMLChannelFamily> channelFamilies = getChannelFamilies();
        ContentSyncManager csm = new ContentSyncManager();
        csm.updateChannelFamilies(channelFamilies);

        // Change all the values
        for (XMLChannelFamily cf : channelFamilies) {
            cf.setLabel(TestUtils.randomString());
            cf.setName(TestUtils.randomString());
            cf.setDefaultNodeCount(cf.getDefaultNodeCount() == 0 ? -1 : 0);
        }

        // Update again
        csm.updateChannelFamilies(channelFamilies);

        // Assert everything is as expected
        for (XMLChannelFamily cf : channelFamilies) {
            ChannelFamily family = ChannelFamilyFactory.lookupByLabel(
                    cf.getLabel(), null);
            assertNotNull(family);
            assertEquals(cf.getLabel(), family.getLabel());
            assertEquals(cf.getName(), family.getName());
            assertNotNull(family.getPublicChannelFamily());
        }
    }

    /**
     * Update the upgrade paths test.
     * @throws Exception if anything goes wrong
     */
    public void testUpdateUpgradePaths() throws Exception {
        File upgradePathsXML = new File(
                TestUtils.findTestData(UPGRADE_PATHS_XML).getPath());
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
            if (SUSEProductFactory.lookupByProductId(1002) == null) {
                p = SUSEProductTestUtils.createTestSUSEProduct(family);
                p.setProductId(1002);
                TestUtils.saveAndFlush(p);
            }
            if (SUSEProductFactory.lookupByProductId(1141) == null) {
                p = SUSEProductTestUtils.createTestSUSEProduct(family);
                p.setProductId(1141);
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

            List<SCCProduct> products = new ArrayList<SCCProduct>();
            int productId = 10012345;
            assertNull(SUSEProductFactory.lookupByProductId(productId));
            String name = TestUtils.randomString();
            String identifier = TestUtils.randomString();
            String version = TestUtils.randomString();
            String releaseType = TestUtils.randomString();
            String friendlyName = TestUtils.randomString();
            String productClass = TestUtils.randomString();

            // Setup a product as it comes from SCC
            SCCProduct prd = new SCCProduct();
            prd.setId(productId);
            prd.setName(name);
            prd.setIdentifier(identifier);
            prd.setVersion(version);
            prd.setReleaseType(releaseType);
            prd.setFriendlyName(friendlyName);
            prd.setProductClass(productClass);
            prd.setArch("i686");
            products.add(prd);

            productId = 10012346;
            assertNull(SUSEProductFactory.lookupByProductId(productId));
            name = TestUtils.randomString();
            identifier = TestUtils.randomString();
            version = TestUtils.randomString();
            releaseType = TestUtils.randomString();
            friendlyName = TestUtils.randomString();
            productClass = TestUtils.randomString();

            // Setup a 2nd product as it comes from SCC
            SCCProduct prd2 = new SCCProduct();
            prd2.setId(productId);
            prd2.setName(name);
            prd2.setIdentifier(identifier);
            prd2.setVersion(version);
            prd2.setReleaseType(releaseType);
            prd2.setFriendlyName(friendlyName);
            prd2.setProductClass(productClass);
            prd2.setArch("i686");
            List<Integer> predIn = new ArrayList<Integer>();
            predIn.add(10012345);
            prd2.setOnlinePredecessorIds(predIn);
            products.add(prd2);

            if (SUSEProductFactory.lookupByProductId(10012345) == null) {
                p = SUSEProductTestUtils.createTestSUSEProduct(family);
                p.setProductId(10012345);
                TestUtils.saveAndFlush(p);
            }
            if (SUSEProductFactory.lookupByProductId(10012346) == null) {
                p = SUSEProductTestUtils.createTestSUSEProduct(family);
                p.setProductId(10012346);
                TestUtils.saveAndFlush(p);
            }

            // Update the upgrade paths
            ContentSyncManager csm = new ContentSyncManager();
            csm.setUpgradePathsXML(upgradePathsXML);
            csm.updateUpgradePaths(products);

            // Check the results
            List<SUSEUpgradePath> upgradePaths =
                    SUSEProductFactory.findAllSUSEUpgradePaths();
            List<String> paths = new ArrayList<String>();
            for (SUSEUpgradePath path : upgradePaths) {
                String ident = String.format("%s-%s",
                        path.getFromProduct().getProductId(),
                        path.getToProduct().getProductId());
                paths.add(ident);
            }
            assertTrue(paths.contains("690-814"));
            assertTrue(paths.contains("1002-1141"));
            assertTrue(paths.contains("1193-1198"));
            assertTrue(paths.contains("10012345-10012346"));
        }
        finally {
            SUSEProductTestUtils.deleteIfTempFile(upgradePathsXML);
        }
    }

    /**
     * There is an upgrade path in the DB and SCC deletes the "from" product.
     * @throws Exception if anything goes wrong
     */
    public void testUpgradePathPredecessorDeleted() throws Exception {
        File upgradePathsEmptyXML = new File(
                TestUtils.findTestData(UPGRADE_PATHS_EMPTY_XML).getPath());
        try {
            List<SCCProduct> products = new ArrayList<SCCProduct>();

            // Setup a product as it comes from SCC
            int product1Id = 10012345;
            assertNull(SUSEProductFactory.lookupByProductId(product1Id));
            String name = TestUtils.randomString();
            String identifier = TestUtils.randomString();
            String version = TestUtils.randomString();
            String releaseType = TestUtils.randomString();
            String friendlyName = TestUtils.randomString();
            String productClass = TestUtils.randomString();

            SCCProduct product1 = new SCCProduct();
            product1.setId(product1Id);
            product1.setName(name);
            product1.setIdentifier(identifier);
            product1.setVersion(version);
            product1.setReleaseType(releaseType);
            product1.setFriendlyName(friendlyName);
            product1.setProductClass(productClass);
            product1.setArch("i686");
            products.add(product1);

            // Setup a 2nd product as it comes from SCC
            int product2Id = 10012346;
            assertNull(SUSEProductFactory.lookupByProductId(product2Id));
            name = TestUtils.randomString();
            identifier = TestUtils.randomString();
            version = TestUtils.randomString();
            releaseType = TestUtils.randomString();
            friendlyName = TestUtils.randomString();
            productClass = TestUtils.randomString();

            SCCProduct product2 = new SCCProduct();
            product2.setId(product2Id);
            product2.setName(name);
            product2.setIdentifier(identifier);
            product2.setVersion(version);
            product2.setReleaseType(releaseType);
            product2.setFriendlyName(friendlyName);
            product2.setProductClass(productClass);
            product2.setArch("i686");
            List<Integer> predIn = new ArrayList<Integer>();
            predIn.add(product1Id);
            product2.setOnlinePredecessorIds(predIn);
            products.add(product2);

            // Update SUSE products and upgrade paths
            ContentSyncManager csm = new ContentSyncManager();
            csm.setUpgradePathsXML(upgradePathsEmptyXML);
            csm.updateSUSEProducts(products);

            // There should be an upgrade path from product1 to product2
            assertEquals(1, SUSEProductFactory.findAllSUSEUpgradePaths().size());

            // Remove the first product
            products.remove(product1);
            product2.setOnlinePredecessorIds(new ArrayList<Integer>());
            csm.updateSUSEProducts(products);

            // There should be no upgrade paths
            assertEquals(true, SUSEProductFactory.findAllSUSEUpgradePaths().isEmpty());
        }
        finally {
            SUSEProductTestUtils.deleteIfTempFile(upgradePathsEmptyXML);
        }
    }

    /**
     * An upgrade path between two products is removed while the products still exist.
     * @throws Exception if anything goes wrong
     */
    public void testUpgradePathRemoved() throws Exception {
        File upgradePathsEmptyXML = new File(
                TestUtils.findTestData(UPGRADE_PATHS_EMPTY_XML).getPath());
        try {
            List<SCCProduct> products = new ArrayList<SCCProduct>();

            // Setup a product as it comes from SCC
            int product1Id = 10012345;
            assertNull(SUSEProductFactory.lookupByProductId(product1Id));
            String name = TestUtils.randomString();
            String identifier = TestUtils.randomString();
            String version = TestUtils.randomString();
            String releaseType = TestUtils.randomString();
            String friendlyName = TestUtils.randomString();
            String productClass = TestUtils.randomString();

            SCCProduct product1 = new SCCProduct();
            product1.setId(product1Id);
            product1.setName(name);
            product1.setIdentifier(identifier);
            product1.setVersion(version);
            product1.setReleaseType(releaseType);
            product1.setFriendlyName(friendlyName);
            product1.setProductClass(productClass);
            product1.setArch("i686");
            products.add(product1);

            // Setup a 2nd product as it comes from SCC
            int product2Id = 10012346;
            assertNull(SUSEProductFactory.lookupByProductId(product2Id));
            name = TestUtils.randomString();
            identifier = TestUtils.randomString();
            version = TestUtils.randomString();
            releaseType = TestUtils.randomString();
            friendlyName = TestUtils.randomString();
            productClass = TestUtils.randomString();

            SCCProduct product2 = new SCCProduct();
            product2.setId(product2Id);
            product2.setName(name);
            product2.setIdentifier(identifier);
            product2.setVersion(version);
            product2.setReleaseType(releaseType);
            product2.setFriendlyName(friendlyName);
            product2.setProductClass(productClass);
            product2.setArch("i686");
            List<Integer> predIn = new ArrayList<Integer>();
            predIn.add(product1Id);
            product2.setOnlinePredecessorIds(predIn);
            products.add(product2);

            // Update SUSE products and upgrade paths
            ContentSyncManager csm = new ContentSyncManager();
            csm.setUpgradePathsXML(upgradePathsEmptyXML);
            csm.updateSUSEProducts(products);

            // There should be an upgrade path from product1 to product2
            assertEquals(1, SUSEProductFactory.findAllSUSEUpgradePaths().size());

            // Remove the upgrade path via the predecessor Id
            product2.setOnlinePredecessorIds(new ArrayList<Integer>());
            csm.updateSUSEProducts(products);

            // There should be no upgrade paths
            assertEquals(true, SUSEProductFactory.findAllSUSEUpgradePaths().isEmpty());
        }
        finally {
            SUSEProductTestUtils.deleteIfTempFile(upgradePathsEmptyXML);
        }
    }

    /**
     * Test for {@link ContentSyncManager#listChannels}.
     * @throws Exception if anything goes wrong
     */
    public void testListChannels() throws Exception {
        File channelsXML = new File(TestUtils.findTestData("channels.xml").getPath());
        try {
            // Match against a manually created cache of SCC repositories
            SCCRepository repo = new SCCRepository();
            String sourceUrl =
                "https://updates.suse.com/repo/$RCE/SLES11-SP3-Pool/sle-11-x86_64";
            repo.setUrl(sourceUrl);
            SCCCachingFactory.saveRepository(repo);

            // Create a channel that is INSTALLED
            Channel channel = SUSEProductTestUtils.createTestVendorChannel();
            String label = "sles11-sp3-pool-x86_64";
            channel.setLabel(label);
            TestUtils.saveAndFlush(channel);

            // List channels and verify status
            ContentSyncManager csm = new ContentSyncManager();
            ContentSyncManager.setChannelsXML(channelsXML);
            List<XMLChannel> channels = csm.listChannels();
            for (XMLChannel c : channels) {
                if (StringUtils.isBlank(c.getSourceUrl())) {
                    assertEquals(MgrSyncStatus.AVAILABLE, c.getStatus());
                }
                else if (label.equals(c.getLabel())) {
                    assertEquals(MgrSyncStatus.INSTALLED, c.getStatus());
                }
                // URLs in channels.xml end with a slash, but not in SCC!
                else if ((sourceUrl + "/").equals(c.getSourceUrl())) {
                    // Copies of this repo (same URL!) are AVAILABLE
                    assertEquals(MgrSyncStatus.AVAILABLE, c.getStatus());
                }
                else {
                    assertEquals(MgrSyncStatus.UNAVAILABLE, c.getStatus());
                }
            }
        }
        finally {
            SUSEProductTestUtils.deleteIfTempFile(channelsXML);
        }
    }

    /**
     * Tests {@link ContentSyncManager#listProducts}, in particular the
     * filtering of unavailable products.
     * @throws Exception if anything goes wrong
     */
    public void testListProductsAvailability() throws Exception {
        // create one available product in the DB
        Channel availableDBChannel = SUSEProductTestUtils.createTestVendorChannel();
        ChannelFamily availableChannelFamily = availableDBChannel.getChannelFamily();
        availableChannelFamily.setOrg(null);
        final SUSEProduct availableDBProduct =
                SUSEProductTestUtils.createTestSUSEProduct(availableChannelFamily);

        // create one available product in channel.xml format
        final XMLChannel availableChannel = new XMLChannel();
        availableChannel.setFamily(availableChannelFamily.getLabel());
        availableChannel.setLabel(availableDBChannel.getLabel());
        availableChannel.setParent("BASE");
        availableChannel.setArch("x86_64");
        final XMLProduct availableProduct =
                new XMLProduct(availableDBProduct.getName(),
                        availableDBProduct.getProductId(), availableDBProduct.getVersion());
        availableChannel.setProducts(new LinkedList<XMLProduct>()
                { { add(availableProduct); } });

        // create one unavailable product in the DB
        Channel unavailableDBChannel = SUSEProductTestUtils.createTestVendorChannel();
        ChannelFamily unavailableChannelFamily = unavailableDBChannel.getChannelFamily();
        unavailableChannelFamily.setOrg(null);
        final SUSEProduct unavailableDBProduct =
                SUSEProductTestUtils.createTestSUSEProduct(unavailableChannelFamily);

        // create one unavailable product in channel.xml format
        final XMLChannel unavailableChannel = new XMLChannel();
        unavailableChannel.setFamily(unavailableChannelFamily.getLabel());
        unavailableChannel.setLabel(unavailableDBChannel.getLabel());
        unavailableChannel.setParent(TestUtils.randomString());
        unavailableChannel.setArch("x86_64");
        final XMLProduct unavailableProduct =
                new XMLProduct(unavailableDBProduct.getName(),
                        unavailableDBProduct.getProductId(),
                        unavailableDBProduct.getVersion());
        unavailableChannel.setProducts(new LinkedList<XMLProduct>()
                { { add(unavailableProduct); } });


        List<XMLChannel> allChannels = new LinkedList<XMLChannel>()
            { { add(availableChannel); add(unavailableChannel); } };

        ContentSyncManager csm = new ContentSyncManager();
        Collection<MgrSyncProductDto> products =
                csm.listProducts(csm.getAvailableChannels(allChannels));

        boolean found = false;
        for (MgrSyncProductDto product : products) {
            if (product.getFriendlyName().equals(availableDBProduct.getFriendlyName())) {
                found = true;
            }
            if (product.getFriendlyName().equals(unavailableDBProduct.getFriendlyName())) {
                fail("Unavailable product returned.");
            }
        }

        assertTrue(found);
    }

    /**
     * Tests {@link ContentSyncManager#listProducts}, in particular the
     * computing of product status.
     * @throws Exception if anything goes wrong
     */
    public void testListProductsStatus() throws Exception {
        // create one installed product in the DB
        Channel installedDBChannel = SUSEProductTestUtils.createTestVendorChannel();
        ChannelFamily installedChannelFamily = installedDBChannel.getChannelFamily();
        installedChannelFamily.setOrg(null);
        final SUSEProduct installedDBProduct =
                SUSEProductTestUtils.createTestSUSEProduct(installedChannelFamily);

        // create one installed product in channel.xml format
        final XMLChannel installedChannel = new XMLChannel();
        installedChannel.setFamily(installedChannelFamily.getLabel());
        installedChannel.setLabel(installedDBChannel.getLabel());
        installedChannel.setParent("BASE");
        installedChannel.setOptional(false);
        final XMLProduct installedProduct =
                new XMLProduct(installedDBProduct.getName(),
                        installedDBProduct.getProductId(), installedDBProduct.getVersion());
        installedChannel.setProducts(new LinkedList<XMLProduct>()
                { { add(installedProduct); } });

        // create one available product in the DB
        ChannelFamily availableChannelFamily =
                ChannelFamilyFactoryTest.createTestChannelFamily();
        availableChannelFamily.setOrg(null);
        final SUSEProduct availableDBProduct =
                SUSEProductTestUtils.createTestSUSEProduct(availableChannelFamily);

        // create one available product in channel.xml format
        final XMLChannel availableChannel = new XMLChannel();
        availableChannel.setFamily(availableChannelFamily.getLabel());
        availableChannel.setParent("BASE");
        availableChannel.setOptional(false);
        final XMLProduct availableProduct =
                new XMLProduct(availableDBProduct.getName(),
                        availableDBProduct.getProductId(), availableDBProduct.getVersion());
        availableChannel.setProducts(new LinkedList<XMLProduct>()
                { { add(availableProduct); } });

        List<XMLChannel> allChannels = new LinkedList<XMLChannel>()
            { { add(installedChannel); add(availableChannel); } };

        ContentSyncManager csm = new ContentSyncManager();
        Collection<MgrSyncProductDto> products = csm.listProducts(allChannels);

        for (MgrSyncProductDto product : products) {
            if (product.getId().equals(installedDBProduct.getProductId())) {
                assertEquals(MgrSyncStatus.INSTALLED, product.getStatus());
            }
            if (product.getId().equals(availableDBProduct.getProductId())) {
                assertEquals(MgrSyncStatus.AVAILABLE, product.getStatus());
            }
        }
    }

    /**
     * Test for {@link ContentSyncManager#addChannel}.
     * @throws Exception if anything goes wrong
     */
    public void testAddChannel() throws Exception {
        File channelsXML = new File(TestUtils.findTestData(CHANNELS_XML).getPath());
        ContentSyncManager csm = new ContentSyncManager();
        ContentSyncManager.setChannelsXML(channelsXML);
        try {
            // Manually create channel object as parsed from channels.xml
            XMLChannel xmlChannel = new XMLChannel();
            xmlChannel.setArch("x86_64");
            xmlChannel.setDescription("SUSE Linux Enterprise Server 11 SP3 x86_64");
            xmlChannel.setFamily("7261");
            xmlChannel.setLabel("sles11-sp3-pool-x86_64");
            xmlChannel.setName("SLES11-SP3-Pool for x86_64");
            xmlChannel.setSourceUrl("https://updates.suse.com/repo/$RCE/" +
                    "SLES11-SP3-Pool/sle-11-x86_64/");
            xmlChannel.setSummary("SUSE Linux Enterprise Server 11 SP3 x86_64");
            xmlChannel.setUpdateTag("slessp3");

            // Setup product
            XMLProduct product = new XMLProduct();
            product.setId(814L);
            product.setName("SUSE_SLES");
            product.setVersion("11.3");
            List<XMLProduct> products = new ArrayList<XMLProduct>();
            products.add(product);
            xmlChannel.setProducts(products);
            // Make sure that this product exists in the database
            SUSEProduct suseProduct = SUSEProductFactory.lookupByProductId(814);
            if (suseProduct == null) {
                suseProduct = new SUSEProduct();
                suseProduct.setName(TestUtils.randomString().toLowerCase());
                suseProduct.setProductId(814);
                SUSEProductFactory.save(suseProduct);
            }

            // Manually save SCC repository in cache to match against
            SCCRepository repo = new SCCRepository();
            repo.setUrl(xmlChannel.getSourceUrl());
            SCCCachingFactory.saveRepository(repo);

            // Make sure there is availability for channel family 7261
            User admin = UserTestUtils.createUserInOrgOne();
            ChannelFamily cf = ChannelFamilyTest.ensureChannelFamilyExists(
                    admin, "7261");
            ChannelFamilyTest.ensurePrivateChannelFamilyExists(admin, cf);
            HibernateFactory.getSession().flush();

            // Add the channel by label
            csm.addChannel(xmlChannel.getLabel(), null);

            // Check if channel has been added correctly
            Channel c = ChannelFactory.lookupByLabel(admin.getOrg(), xmlChannel.getLabel());
            assertNotNull(c);
            assertEquals(MgrSyncUtils.getChannelArch(xmlChannel), c.getChannelArch());
            assertEquals("/dev/null", c.getBaseDir());
            assertEquals(ChannelFamilyFactory.lookupByLabel(
                    xmlChannel.getFamily(), null), c.getChannelFamily());
            assertEquals("sha1", c.getChecksumTypeLabel());
            assertEquals(xmlChannel.getDescription(), c.getDescription());
            assertEquals(xmlChannel.getName(), c.getName());
            assertEquals(xmlChannel.getSummary(), c.getSummary());
            assertEquals(xmlChannel.getUpdateTag(), c.getUpdateTag());

            // Verify the content source
            assertEquals(1, c.getSources().size());
            for (ContentSource cs : c.getSources()) {
                assertEquals(xmlChannel.getLabel(), cs.getLabel());
                assertEquals(xmlChannel.getSourceUrl(), cs.getSourceUrl());
                assertEquals(ChannelFactory.lookupContentSourceType("yum"), cs.getType());
            }

            // Verify product to channel relationship
            SUSEProductChannel spc = new SUSEProductChannel();
            spc.setChannel(c);
            spc.setChannelLabel(c.getLabel());
            spc.setProduct(suseProduct);
            List<SUSEProductChannel> productChannels = SUSEProductFactory.
                    findAllSUSEProductChannels();
            assertTrue(productChannels.contains(spc));
        }
        finally {
            SUSEProductTestUtils.deleteIfTempFile(channelsXML);
        }
    }

    /**
     * Tests {@link ContentSyncManager#setupSourceURL} with a local filesystem link
     * using an URL pointing to an official SUSE server.
     * @throws Exception if anything goes wrong
     */
    public void testSetupSourceURLLocalFS() throws Exception {
        // Set offline mode
        Config.get().setString(ContentSyncManager.RESOURCE_PATH,
                System.getProperty("java.io.tmpdir"));

        // Make some data
        String repoUrlSCC =
                "https://updates.suse.com/SUSE/Products/SLE-SERVER/12/x86_64/product/";
        SCCRepository repo = new SCCRepository();
        repo.setUrl(repoUrlSCC);
        repo.setName("SLES12-Pool");
        assertEquals(String.format("file:%s/SUSE/Products/SLE-SERVER/12/x86_64/product",
                                   System.getProperty("java.io.tmpdir")),
                     new ContentSyncManager().setupSourceURL(repo, null));

        // Switch to online mode
        Config.get().remove(ContentSyncManager.RESOURCE_PATH);
    }

    /**
     * Tests {@link ContentSyncManager#setupSourceURL} with a local filesystem link
     * using an URL pointing to a 3rd party server.
     * @throws Exception if anything goes wrong
     */
    public void testSetupSourceURLLocalFSExtUrl() throws Exception {
        // Set offline mode
        Config.get().setString(ContentSyncManager.RESOURCE_PATH,
                System.getProperty("java.io.tmpdir"));

        // Make some data
        String repoUrlSCC =
                "https://www.somehost.com/path/to/resource?query=like&this=here";
        SCCRepository repo = new SCCRepository();
        repo.setUrl(repoUrlSCC);
        repo.setName("MyResource GA");
        assertEquals(String.format("file:%s/repo/RPMMD/%s",
                                   System.getProperty("java.io.tmpdir"),
                                   "MyResource"),
                     new ContentSyncManager().setupSourceURL(repo, null));

        // Switch to online mode
        Config.get().remove(ContentSyncManager.RESOURCE_PATH);
    }

    /**
     * Tests {@link ContentSyncManager#setupSourceURL}.
     * @throws Exception if something goes wrong
     */
    public void testSetupSourceURL() throws Exception {
        // Verify updates.suse.com uses token auth
        String repoUrlSCC =
                "https://updates.suse.com/repo/$RCE/SLES11-SP3-Pool/sle-11-x86_64/?asdfgh";
        ContentSyncManager csm = new ContentSyncManager();
        SCCRepository repo = new SCCRepository();
        repo.setUrl(repoUrlSCC);
        repo.setCredentials(getTestCredentials((2L)));
        assertEquals(repoUrlSCC, csm.setupSourceURL(repo, null));

        // Test basic auth with nu.novell.com
        String repoUrlNCC = "https://nu.novell.com/repo/$RCE/OES11-SP2-Pool/sle-11-x86_64/";
        repo = new SCCRepository();
        repo.setUrl("");
        repo.setCredentials(getTestCredentials((2L)));
        assertNull(csm.setupSourceURL(repo, null));
        repo.setUrl(repoUrlNCC);
        repo.setCredentials(getTestCredentials((0L)));
        assertEquals(repoUrlNCC + "?credentials=mirrcred_0",
                csm.setupSourceURL(repo, null));
        repo.setUrl(repoUrlNCC);
        repo.setCredentials(getTestCredentials((3L)));
        assertEquals(repoUrlNCC + "?credentials=mirrcred_3",
                csm.setupSourceURL(repo, null));

        // Fall back to official repo in case mirror is not reachable
        String mirrorUrl = "http://localhost/";
        repo.setUrl(repoUrlNCC);
        repo.setCredentials(getTestCredentials((0L)));
        assertEquals(repoUrlNCC + "?credentials=mirrcred_0",
                csm.setupSourceURL(repo, mirrorUrl));
        repo.setUrl(repoUrlSCC);
        repo.setCredentials(getTestCredentials((0L)));
        assertEquals(repoUrlSCC, csm.setupSourceURL(repo, mirrorUrl));
    }

    /**
     * Tests {@link ContentSyncManager#findMatchingRepo}.
     */
    public void testFindMatchingRepo() {
        final String base = "https://updates.suse.com/SUSE/Products/SLE-SERVER/12/x86_64";
        final SCCRepository good = new SCCRepository() { {
            setUrl(base + "/product?veryverylongtoken");
        } };
        final SCCRepository bad = new SCCRepository() { {
            setUrl(base + "/product_debug?veryverylongtoken");
        } };

        Collection<SCCRepository> repos = new LinkedList<SCCRepository>() { {
            add(good);
            add(bad);
        } };

        SCCRepository result = new ContentSyncManager().
                findMatchingRepo(repos, base + "/product///");
        assertEquals(good, result);

        final SCCRepository alternateGood = new SCCRepository() { {
            setUrl(base + "/product/");
        } };
        repos.remove(good);
        repos.add(alternateGood);

        result = new ContentSyncManager().
                findMatchingRepo(repos, base + "/product///");
        assertEquals(alternateGood, result);
}

    /**
     * Tests that the SUSEProductChannel class behaves correctly with Hibernate
     * (ensures there is no regression wrt bsc#932052).
     * @throws Exception if something goes wrong
     */
    public void testSUSEProductChannelUpdates() throws Exception {
        // Setup two products
        Channel channel = SUSEProductTestUtils.createTestVendorChannel();
        ChannelFamily family = channel.getChannelFamily();
        SUSEProduct product = SUSEProductTestUtils.createTestSUSEProduct(family);

        Channel channel2 = SUSEProductTestUtils.createTestVendorChannel();
        ChannelFamily family2 = channel2.getChannelFamily();
        SUSEProduct product2 = SUSEProductTestUtils.createTestSUSEProduct(family2);

        // Create a product channel
        SUSEProductChannel spc = new SUSEProductChannel();
        spc.setChannel(channel);
        spc.setChannelLabel(channel.getLabel());
        spc.setProduct(product);

        SUSEProductFactory.save(spc);
        HibernateFactory.getSession().flush();

        // change the product
        spc.setProduct(product2);
        SUSEProductFactory.save(spc);
        HibernateFactory.getSession().flush();

        // removes the changed product
        SUSEProductFactory.remove(spc);

        // flushes again, used to fail with exception in bsc#932052
        HibernateFactory.getSession().flush();
    }

    /**
     * Create credentials for testing given an ID.
     * @param id
     * @return credentials
     */
    private Credentials getTestCredentials(Long id) {
        Credentials creds = new Credentials();
        creds.setId(id);
        return creds;
    }

    /**
     * Clear all credentials from the database.
     */
    private void clearCredentials() {
        for (Credentials creds : CredentialsFactory.lookupSCCCredentials()) {
            CredentialsFactory.removeCredentials(creds);
        }
    }

    /**
     * Return a list of channel families containing random data as attributes.
     * @return list of channel families for testing
     */
    private List<XMLChannelFamily> getChannelFamilies() {
        List<XMLChannelFamily> channelFamilies =
                new ArrayList<XMLChannelFamily>();
        XMLChannelFamily family1 = new XMLChannelFamily();
        family1.setLabel(TestUtils.randomString());
        family1.setName(TestUtils.randomString());
        family1.setDefaultNodeCount(0);
        channelFamilies.add(family1);
        XMLChannelFamily family2 = new XMLChannelFamily();
        family2.setLabel(TestUtils.randomString());
        family2.setName(TestUtils.randomString());
        family2.setDefaultNodeCount(-1);
        channelFamilies.add(family2);
        return channelFamilies;
    }

    /**
     * Temporarily rename all installed vendor channels in order to avoid conflicts
     * whenever we use real channel labels in tests.
     */
    private void renameVendorChannels() {
        for (Channel c : ChannelFactory.listVendorChannels()) {
            c.setLabel(TestUtils.randomString());
            c.setName(TestUtils.randomString());
            TestUtils.saveAndFlush(c);
        }
        for (ContentSource cs : ChannelFactory.listVendorContentSources()) {
            cs.setLabel(TestUtils.randomString());
            cs.setSourceUrl(TestUtils.randomString());
            TestUtils.saveAndFlush(cs);
        }
    }

    public void testIsChannelOrLabelReserved() throws Exception {
        File channelsXML = new File(TestUtils.findTestData(CHANNELS_XML).getPath());
        ContentSyncManager.setChannelsXML(channelsXML);
        assertFalse(ContentSyncManager.isChannelNameReserved("suse"));
        assertFalse(ContentSyncManager.isChannelLabelReserved("label"));
        assertTrue(ContentSyncManager.isChannelLabelReserved("sles11-sp3-pool-x86_64"));
        assertTrue(ContentSyncManager.isChannelNameReserved("IBM-DLPAR-SDK"));
    }

    /**
     * Test for {@link ContentSyncManager#updateChannels}. Fix invalid assignment
     * @throws Exception if anything goes wrong
     */
    public void testUpdateChannelsCleanupNotAttachedRepos() throws Exception {
        File channelsXML = new File(TestUtils.findTestData(CHANNELS_XML).getPath());
        try {
            // Make sure that channel family "7261" exists
            User admin = UserTestUtils.createUserInOrgOne();
            ChannelFamilyTest.ensureChannelFamilyExists(admin, "7261");

            // Create a test channel and set a label that exists in the xml file
            String channelLabel = "sles12-sp1-debuginfo-pool-x86_64";
            Channel c = SUSEProductTestUtils.createTestVendorChannel();
            c.setLabel(channelLabel);
            c.setDescription("UPDATE ME!");
            c.setName("UPDATE ME!");
            c.setSummary("UPDATE ME!");
            c.setUpdateTag("UPDATE ME!");

            // Setup content source
            ContentSource cs = new ContentSource();
            cs.setLabel(c.getLabel());
            cs.setSourceUrl("https://updates.suse.com/SUSE/Products/SLE-SERVER/12-SP1/x86_64/product_debug?update-me");
            cs.setType(ChannelFactory.lookupContentSourceType("yum"));
            cs.setOrg(null);
            ChannelFactory.save(cs);
            cs = (ContentSource) TestUtils.saveAndReload(cs);
            c.getSources().add(cs);
            TestUtils.saveAndFlush(c);

            String channelLabel2 = "sles12-sp1-pool-x86_64";
            Channel c2 = SUSEProductTestUtils.createTestVendorChannel();
            c2.setLabel(channelLabel2);
            c2.setDescription("UPDATE ME 2!");
            c2.setName("UPDATE ME 2!");
            c2.setSummary("UPDATE ME 2!");
            c2.setUpdateTag("UPDATE ME 2!");

            c2.getSources().add(cs);
            TestUtils.saveAndFlush(c2);

            // Setup content source with is not accessible
            ContentSource csna = new ContentSource();
            csna.setLabel("sle-manager-tools12-updates-x86_64-sp2");
            csna.setSourceUrl("http://smt-scc.nue.suse.com/NOT/EXISTING/REPO/update?6W2Zmf33GRPn");
            csna.setType(ChannelFactory.lookupContentSourceType("yum"));
            csna.setOrg(null);
            ChannelFactory.save(csna);

            // Save SCC repo to the cache
            SCCRepository repo = new SCCRepository();
            repo.setUrl("https://updates.suse.com/SUSE/Products/SLE-SERVER/12-SP1/x86_64/product_debug?qgtMzfPFsLpauJuXu");
            SCCCachingFactory.saveRepository(repo);

            SCCRepository repo2 = new SCCRepository();
            repo2.setUrl("https://updates.suse.com/SUSE/Products/SLE-SERVER/12-SP1/x86_64/product?kANfkB7wMUO0u4rmgQ80UBe");
            SCCCachingFactory.saveRepository(repo2);

            // Update channel information from the xml file
            ContentSyncManager csm = new ContentSyncManager();
            ContentSyncManager.setChannelsXML(channelsXML);
            csm.updateChannelsInternal(null);

            // Verify channel attributes
            c = ChannelFactory.lookupByLabel(channelLabel);
            assertEquals("SLES12-SP1-Debuginfo-Pool for x86_64", c.getName());
            assertEquals("SUSE Linux Enterprise Server 12 SP1 x86_64", c.getSummary());

            // Verify content sources (there is only one)
            Set<ContentSource> sources = c.getSources();
            for (ContentSource s : sources) {
                String url = "https://updates.suse.com/SUSE/Products/SLE-SERVER/12-SP1/x86_64/product_debug?qgtMzfPFsLpauJuXu";
                assertEquals(url, s.getSourceUrl());
            }

            c2 = ChannelFactory.lookupByLabel(channelLabel2);
            assertEquals("SLES12-SP1-Pool for x86_64", c2.getName());
            assertEquals("SUSE Linux Enterprise Server 12 SP1 x86_64", c2.getSummary());

            // Verify content sources (there is only one)
            Set<ContentSource> sources2 = c2.getSources();
            for (ContentSource s2 : sources2) {
                String url = "https://updates.suse.com/SUSE/Products/SLE-SERVER/12-SP1/x86_64/product?kANfkB7wMUO0u4rmgQ80UBe";
                assertEquals(url, s2.getSourceUrl());
            }

            // Verify that we only have two content source and not the
            // not accessible anymore
            List<ContentSource> cses = ChannelFactory.listVendorContentSources();
            assertEquals(2, cses.size());
            for (ContentSource s3 : cses) {
                assertFalse("Found ContentSource which should not exist",
                        s3.getSourceUrl().equals("http://smt-scc.nue.suse.com/NOT/EXISTING/REPO/update?6W2Zmf33GRPn"));
            }
        }
        finally {
            SUSEProductTestUtils.deleteIfTempFile(channelsXML);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Clear data for all tests
        clearCredentials();
        SCCCachingFactory.clearRepositories();
        renameVendorChannels();
    }
}
