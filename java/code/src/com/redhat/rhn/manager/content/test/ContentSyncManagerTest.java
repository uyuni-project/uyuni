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
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.channel.test.ChannelFamilyFactoryTest;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;

import com.suse.mgrsync.MgrSyncChannelFamily;
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
        csm.setChannelsXML(getPathToFile("channels.xml"));
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

        File cf = new File(getPathToFile("channel_families.xml"));
        File c = new File(getPathToFile("channels.xml"));
        ContentSyncManager csm = new ContentSyncManager();
        csm.setChannelFamiliesXML(cf.getAbsolutePath());
        csm.setChannelsXML(c.getAbsolutePath());

        csm.updateSubscriptions(subscriptions);

        cf.delete();
        c.delete();
    }

    /**
     * Test for {@link ContentSyncManager#updateChannelFamilies() method.
     * @throws Exception
     */
    public void testUpdateChannelFamilies() throws Exception {
        File cf = new File(getPathToFile("channel_families.xml"));
        ContentSyncManager csm = new ContentSyncManager();
        csm.setChannelFamiliesXML(cf.getAbsolutePath());

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
            cf.delete();
        }
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
     * Finds a given testfile by name and returns the filesystem path.
     * @param filename name of the testfile
     * @return path to the testfile
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private String getPathToFile(String filename)
            throws ClassNotFoundException, IOException {
        return new File(TestUtils.findTestData(filename).getPath()).getAbsolutePath();
    }
}
