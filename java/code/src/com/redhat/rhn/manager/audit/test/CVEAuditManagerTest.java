/**
 * Copyright (c) 2013 SUSE LLC
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
package com.redhat.rhn.manager.audit.test;

import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper.getAllRelevantChannels;
import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper.getRelevantChannels;
import static com.redhat.rhn.testing.ErrataTestUtils.createLaterTestPackage;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannel;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannelFamily;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannelProduct;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestClonedChannel;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestClonedErrata;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestCve;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestErrata;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestInstalledPackage;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestPackage;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestServer;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestUser;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestVendorBaseChannel;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestVendorChildChannel;
import static com.redhat.rhn.domain.product.test.SUSEProductTestUtils.createTestSUSEProduct;
import static com.redhat.rhn.domain.product.test.SUSEProductTestUtils.createTestSUSEProductChannel;
import static com.redhat.rhn.domain.product.test.SUSEProductTestUtils.createTestSUSEUpgradePath;
import static com.redhat.rhn.domain.product.test.SUSEProductTestUtils.installSUSEProductOnServer;
import static com.redhat.rhn.testing.ImageTestUtils.createImageInfo;
import static com.redhat.rhn.testing.ImageTestUtils.createImagePackage;

import java.util.*;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelProduct;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.errata.Cve;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageNameTest;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.SUSEProductDto;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.manager.audit.*;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

/**
 * Unit tests for {@link CVEAuditManager}.
 *
 * @version $Rev$
 */
public class CVEAuditManagerTest extends RhnBaseTestCase {

    /**
     * Test insertion and deletion for table suseCVEServerChannel:
     * {@link CVEAuditManager#deleteRelevantChannels()}
     * {@link CVEAuditManager#insertRelevantServerChannels(Map)}
     * @throws Exception if anything goes wrong
     */
    public void testInsertRelevantChannels() throws Exception {
        // Delete all relevant channel entries first
        CVEAuditManager.deleteRelevantChannels();
        assertTrue(getAllRelevantChannels().isEmpty());

        // Setup real servers and channels to satisfy FK constraints
        User user = createTestUser();
        Server server1 = createTestServer(user);
        Server server2 = createTestServer(user);
        Channel channel1 = createTestChannel(user);
        Channel channel2 = createTestChannel(user);
        Channel channel3 = createTestChannel(user);

        // Insert some records
        Map<Server, List<RankedChannel>> expected = new HashMap<>();
        RankedChannel pair1 = new RankedChannel(channel1.getId(), 0);
        RankedChannel pair2 = new RankedChannel(channel2.getId(), 1);
        RankedChannel pair3 = new RankedChannel(channel3.getId(), 2);
        expected.put(server1, Arrays.asList(pair1, pair2));
        expected.put(server2, Collections.singletonList(pair3));

        CVEAuditManager.insertRelevantServerChannels(expected);

        // Read and check if table content is as expected
        List<ServerChannelIdPair> pairsActual = getAllRelevantChannels();
        assertEquals(3, pairsActual.size());
        assertContains(pairsActual, new ServerChannelIdPair(server1.getId(), pair1.getChannelId(), pair1.getRank()));
        assertContains(pairsActual, new ServerChannelIdPair(server1.getId(), pair2.getChannelId(), pair2.getRank()));
        assertContains(pairsActual, new ServerChannelIdPair(server2.getId(), pair3.getChannelId(), pair3.getRank()));

        // Clean up
        CVEAuditManager.deleteRelevantChannels();
        assertTrue(getAllRelevantChannels().isEmpty());
    }

    /**
     * Verify that servers will be returned disregarding user permissions:
     * {@link CVEAuditManager#listAllServers()}
     * @throws Exception if anything goes wrong
     */
    public void testListAllServers() throws Exception {
        Set<SystemOverview> expected = new HashSet<SystemOverview>();
        User user1 = UserTestUtils.findNewUser("testuser1", "testorg1");
        Server server1 = createTestServer(user1);
        SystemOverview sysOverview1 = new SystemOverview();
        sysOverview1.setId(server1.getId());
        expected.add(sysOverview1);
        User user2 = UserTestUtils.findNewUser("testuser2", "testorg2");
        Server server2 = createTestServer(user2);
        SystemOverview sysOverview2 = new SystemOverview();
        sysOverview2.setId(server2.getId());
        expected.add(sysOverview2);
        User user3 = UserTestUtils.findNewUser("testuser3", "testorg3");
        Server server3 = createTestServer(user3);
        SystemOverview sysOverview3 = new SystemOverview();
        sysOverview3.setId(server3.getId());
        expected.add(sysOverview3);

        List<SystemOverview> systems = CVEAuditManager.listAllServers();
        assertTrue(systems.size() >= 3);

        // Check for all expected system IDs and remove them from the set
        for (SystemOverview s1 : systems) {
            Iterator<SystemOverview> expectedIterator = expected.iterator();
            while (expectedIterator.hasNext()) {
                SystemOverview s2 = expectedIterator.next();
                if (s1.getId().equals(s2.getId())) {
                    expectedIterator.remove();
                }
            }
            // Stop if all servers were found
            if (expected.isEmpty()) {
                break;
            }
        }
        assertTrue(expected.isEmpty());
    }

    /**
     * Test product channel relationships, tested methods are:
     * {@link CVEAuditManager#convertProductId(long)}
     * {@link CVEAuditManager#findSUSEProductChannels(long)}
     * {@link CVEAuditManager#findChannelProducts(List)}
     * @throws Exception if anything goes wrong
     */
    public void testFindSUSEProductChannels() throws Exception {
        // Create a SUSE product and channel products
        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct product = createTestSUSEProduct(channelFamily);
        ChannelProduct channelProduct1 = createTestChannelProduct();
        ChannelProduct channelProduct2 = createTestChannelProduct();
        // Create channels
        Channel baseChannel = createTestVendorBaseChannel(channelFamily, channelProduct1);
        Channel childChannel1 = createTestVendorChildChannel(baseChannel, channelProduct1);
        Channel childChannel2 = createTestVendorChildChannel(baseChannel, channelProduct2);
        Channel childChannel3 = createTestVendorChildChannel(baseChannel, channelProduct2);
        // Assign channels to SUSE product
        createTestSUSEProductChannel(baseChannel, product, true);
        createTestSUSEProductChannel(childChannel1, product, true);
        createTestSUSEProductChannel(childChannel2, product, true);
        createTestSUSEProductChannel(childChannel3, product, true);

        // Test the conversion between SUSE product and channel products
        List<Long> channelProductIDs = CVEAuditManager.convertProductId(product.getId());
        assertEquals(2, channelProductIDs.size());
        assertTrue(channelProductIDs.contains(channelProduct1.getId()));
        assertTrue(channelProductIDs.contains(channelProduct2.getId()));

        // Call findSUSEProductChannels() and verify the results
        List<Channel> channels =
                CVEAuditManager.findSUSEProductChannels(product.getId(),
                        baseChannel.getId());
        assertEquals(4, channels.size());
        assertTrue(channels.contains(baseChannel));
        assertTrue(channels.contains(childChannel1));
        assertTrue(channels.contains(childChannel2));
        assertTrue(channels.contains(childChannel3));

        // Query the other way around: findChannelProducts()
        List<Long> channelIDs = new ArrayList<Long>();
        for (Channel c : channels) {
            channelIDs.add(c.getId());
        }
        List<Long> productIDs = CVEAuditManager.findChannelProducts(channelIDs);
        assertEquals(2, productIDs.size());
        assertTrue(productIDs.contains(channelProduct1.getId()));
        assertTrue(productIDs.contains(channelProduct2.getId()));
    }

    /**
     * Verify that bnc#841240 is fixed:
     * Do not throw exceptions if a product channel is not synced
     * @throws Exception if anything goes wrong
     */
    public void testUnsyncedProductChannels() throws Exception {
        // Create a SUSE product and channel products
        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct product = createTestSUSEProduct(channelFamily);
        ChannelProduct channelProduct1 = createTestChannelProduct();
        // Create channels
        Channel baseChannel = createTestVendorBaseChannel(channelFamily, channelProduct1);
        Channel childChannel1 = createTestVendorChildChannel(baseChannel, channelProduct1);

        // Assign channels to SUSE product. Not adding the base channel on
        // purpose to simulate that it has not been synchronized
        createTestSUSEProductChannel(childChannel1, product, true);

        // should not throw any exception
        CVEAuditManager.findSUSEProductChannels(product.getId(), baseChannel.getId());
    }

    /**
     * Test upgrade paths going backwards:
     * {@link CVEAuditManager#findAllSourceProducts(Long)}
     * @throws Exception if anything goes wrong
     */
    public void testFindAllSourceProducts() throws Exception {
        // Create test products
        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct sles10sp2 = createTestSUSEProduct(channelFamily);
        SUSEProduct sles10sp3 = createTestSUSEProduct(channelFamily);
        SUSEProduct sles10sp4 = createTestSUSEProduct(channelFamily);
        SUSEProduct sles11sp1 = createTestSUSEProduct(channelFamily);
        SUSEProduct sles11sp2 = createTestSUSEProduct(channelFamily);
        createTestSUSEUpgradePath(sles10sp2, sles10sp3);
        createTestSUSEUpgradePath(sles10sp3, sles10sp4);
        createTestSUSEUpgradePath(sles11sp1, sles11sp2);

        // SLES 10 SP2-SP4
        List<SUSEProductDto> sourceProducts;
        sourceProducts = CVEAuditManager.findAllSourceProducts(sles10sp2.getId());
        assertEquals(0, sourceProducts.size());

        sourceProducts = CVEAuditManager.findAllSourceProducts(sles10sp3.getId());
        assertEquals(1, sourceProducts.size());
        sourceProducts.get(0).equals(sles10sp2);

        sourceProducts = CVEAuditManager.findAllSourceProducts(sles10sp4.getId());
        assertEquals(2, sourceProducts.size());
        sourceProducts.get(0).equals(sles10sp2);
        sourceProducts.get(1).equals(sles10sp3);

        // SLES 11 SP1-SP2
        sourceProducts = CVEAuditManager.findAllSourceProducts(sles11sp1.getId());
        assertEquals(0, sourceProducts.size());

        sourceProducts = CVEAuditManager.findAllSourceProducts(sles11sp2.getId());
        assertEquals(1, sourceProducts.size());
        sourceProducts.get(0).equals(sles11sp1);
    }

    /**
     * Test upgrade paths going forwards:
     * {@link CVEAuditManager#findAllTargetProducts(Long)}
     * @throws Exception if anything goes wrong
     */
    public void testFindAllTargetProducts() throws Exception {
        // Create test products
        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct sles10sp2 = createTestSUSEProduct(channelFamily);
        SUSEProduct sles10sp3 = createTestSUSEProduct(channelFamily);
        SUSEProduct sles10sp4 = createTestSUSEProduct(channelFamily);
        SUSEProduct sles11sp1 = createTestSUSEProduct(channelFamily);
        SUSEProduct sles11sp2 = createTestSUSEProduct(channelFamily);
        createTestSUSEUpgradePath(sles10sp2, sles10sp3);
        createTestSUSEUpgradePath(sles10sp3, sles10sp4);
        createTestSUSEUpgradePath(sles11sp1, sles11sp2);

        // SLES 10 SP2-SP4
        List<SUSEProductDto> targetProducts;
        targetProducts = CVEAuditManager.findAllTargetProducts(sles10sp2.getId());
        assertEquals(2, targetProducts.size());
        targetProducts.get(0).equals(sles10sp3);
        targetProducts.get(0).equals(sles10sp4);

        targetProducts = CVEAuditManager.findAllTargetProducts(sles10sp3.getId());
        assertEquals(1, targetProducts.size());
        targetProducts.get(0).equals(sles10sp4);

        targetProducts = CVEAuditManager.findAllTargetProducts(sles10sp4.getId());
        assertEquals(0, targetProducts.size());

        // SLES 11 SP1-SP2
        targetProducts = CVEAuditManager.findAllTargetProducts(sles11sp1.getId());
        assertEquals(1, targetProducts.size());
        targetProducts.get(0).equals(sles11sp2);

        targetProducts = CVEAuditManager.findAllTargetProducts(sles11sp2.getId());
        assertEquals(0, targetProducts.size());
    }

    /**
     * Test if the patch status is set correctly for given parameters:
     */
    public void testSetPatchStatus() {
        assertEquals(PatchStatus.PATCHED, CVEAuditManager.getPatchStatus(true, true, true));
        assertEquals(PatchStatus.PATCHED, CVEAuditManager.getPatchStatus(true, false, true));
        assertEquals(PatchStatus.AFFECTED_PATCH_APPLICABLE, CVEAuditManager.getPatchStatus(false, true, true));
        assertEquals(PatchStatus.AFFECTED_PATCH_INAPPLICABLE, CVEAuditManager.getPatchStatus(false, false, true));
        assertEquals(PatchStatus.NOT_AFFECTED, CVEAuditManager.getPatchStatus(false, false, false));
    }

    /**
     * Test the correct population of the table suseCVEServerChannel:
     * {@link CVEAuditManager#populateCVEChannels()}
     * @throws Exception if anything goes wrong
     */
    public void testPopulateCVEServerChannels() throws Exception {
        // Create a SUSE product and channel products
        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct product = createTestSUSEProduct(channelFamily);
        ChannelProduct channelProduct = createTestChannelProduct();
        // Create channels
        Channel baseChannel = createTestVendorBaseChannel(channelFamily, channelProduct);
        Channel childChannel1 = createTestVendorChildChannel(baseChannel, channelProduct);
        Channel childChannel2 = createTestVendorChildChannel(baseChannel, channelProduct);
        // Assign channels to SUSE product
        createTestSUSEProductChannel(baseChannel, product, true);
        createTestSUSEProductChannel(childChannel1, product, true);
        createTestSUSEProductChannel(childChannel2, product, true);

        // Setup a next SP product for verifying SP migrations
        SUSEProduct productNextSP = createTestSUSEProduct(channelFamily);
        createTestSUSEUpgradePath(product, productNextSP);
        // Create channels
        ChannelProduct channelProductNextSP = createTestChannelProduct();
        Channel baseChannelNextSP = createTestVendorBaseChannel(channelFamily, channelProductNextSP);
        Channel childChannelNextSP = createTestVendorChildChannel(baseChannelNextSP, channelProductNextSP);
        // Assign channels to SP product
        createTestSUSEProductChannel(baseChannelNextSP, productNextSP, true);
        createTestSUSEProductChannel(childChannelNextSP, productNextSP, true);

        // Setup a previous SP of the installed product
        SUSEProduct productPrevSP = createTestSUSEProduct(channelFamily);
        createTestSUSEUpgradePath(productPrevSP, product);
        // Create channels
        ChannelProduct channelProductPrevSP = createTestChannelProduct();
        Channel baseChannelPrevSP = createTestVendorBaseChannel(channelFamily, channelProductPrevSP);
        Channel childChannelPrevSP = createTestVendorChildChannel(baseChannelPrevSP, channelProductPrevSP);
        // Assign channels to SP product
        createTestSUSEProductChannel(baseChannelPrevSP, productPrevSP, true);
        createTestSUSEProductChannel(childChannelPrevSP, productPrevSP, true);

        // Create server and install product
        User user = createTestUser();
        List<Channel> subscribedChannels = new ArrayList<Channel>(Arrays.asList(baseChannel, childChannel1));
        Server server = createTestServer(user, subscribedChannels);
        installSUSEProductOnServer(product, server);

        // Populate the database
        CVEAuditManager.populateCVEChannels();

        // Get channels relevant for the above system
        List<ServerChannelIdPair> relevantChannels = getRelevantChannels(server.getId());
        assertEquals(7, relevantChannels.size());

        // Check assigned channels
        assertContains(relevantChannels, new ServerChannelIdPair(server.getId(), baseChannel.getId(), 0));
        assertContains(relevantChannels, new ServerChannelIdPair(server.getId(), childChannel1.getId(), 0));
        // Check unassigned product channels
        assertContains(relevantChannels, new ServerChannelIdPair(server.getId(), childChannel2.getId(), 1));
        // Check channels relevant for the next SP
        assertContains(relevantChannels, new ServerChannelIdPair(server.getId(), baseChannelNextSP.getId(), 2));
        assertContains(relevantChannels, new ServerChannelIdPair(server.getId(), childChannelNextSP.getId(), 2));
        // Check channels relevant for the previous SP
        assertContains(relevantChannels, new ServerChannelIdPair(server.getId(), baseChannelPrevSP.getId(), 100000));
        assertContains(relevantChannels, new ServerChannelIdPair(server.getId(), childChannelPrevSP.getId(), 100000));
    }

    /**
     * {@link CVEAuditManager#findProductChannels(List, Long)}.
     * @throws Exception if anything goes wrong
     */
    public void testFindProductChannels() throws Exception {
        // Create channels belonging to a channel product
        ChannelFamily channelFamily = createTestChannelFamily();
        ChannelProduct channelProduct = createTestChannelProduct();
        Channel baseChannel = createTestVendorBaseChannel(channelFamily, channelProduct);
        Channel childChannel1 = createTestVendorChildChannel(baseChannel, channelProduct);
        Channel childChannel2 = createTestVendorChildChannel(baseChannel, channelProduct);

        // All channels defined above should be returned
        List<Long> productIDs = new ArrayList<Long>(Arrays.asList(channelProduct.getId()));
        List<Channel> channels = CVEAuditManager.findProductChannels(productIDs, baseChannel.getId());
        assertEquals(3, channels.size());
        assertTrue(channels.contains(baseChannel));
        assertTrue(channels.contains(childChannel1));
        assertTrue(channels.contains(childChannel2));
    }

    /**
     * Runs listSystemsByPatchStatus with an unknown CVE identifier.
     * @throws Exception if anything goes wrong
     */
    public void testListSystemsByPatchStatusUnknown() throws Exception {
        String cveName = TestUtils.randomString().substring(0, 13);

        User user = createTestUser();

        boolean exceptionWasThrown = false;
        try {
            CVEAuditManager.listSystemsByPatchStatus(user, cveName,
                    EnumSet.allOf(PatchStatus.class));
        }
        catch (UnknownCVEIdentifierException e) {
            exceptionWasThrown = true;
        }

        assertTrue(exceptionWasThrown);
    }

    /**
     * Runs listSystemsByPatchStatus on a server with patch status NOT_AFFECTED
     * and tests result filtering.
     * @throws Exception if anything goes wrong
     */
    public void testListSystemsByPatchStatusNotAffected() throws Exception {
        // Create a CVE number
        String cveName = TestUtils.randomString().substring(0, 13);
        Cve cve = createTestCve(cveName);
        Set<Cve> cves = new HashSet<Cve>();
        cves.add(cve);

        // Create a server with a channel, one errata but no vulnerable
        // installed package
        User user = createTestUser();
        Errata errata = createTestErrata(user, cves);
        Channel channel = createTestChannel(user, errata);
        Set<Channel> channels = new HashSet<Channel>();
        channels.add(channel);
        createTestPackage(user, errata, channel, "noarch");
        Server server = createTestServer(user, channels);
        CVEAuditManager.populateCVEChannels();

        // No filtering
        EnumSet<PatchStatus> filter = EnumSet.allOf(PatchStatus.class);
        List<CVEAuditServer> results = CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
        assertSystemPatchStatus(server, PatchStatus.NOT_AFFECTED, results);

        // Everything is filtered except expected
        filter = EnumSet.of(PatchStatus.NOT_AFFECTED);
        results = CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
        assertSystemPatchStatus(server, PatchStatus.NOT_AFFECTED, results);

        // Only the expected result is filtered
        filter = EnumSet.complementOf(filter);
        results = CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
        assertSystemNotFound(server, results);
    }

    /**
     * Runs listSystemsByPatchStatus on a server with patch status PATCHED and
     * tests result filtering.
     * @throws Exception if anything goes wrong
     */
    public void testListSystemsByPatchStatusPatched() throws Exception {
        // Create a CVE number
        String cveName = TestUtils.randomString().substring(0, 13);
        Cve cve = createTestCve(cveName);
        Set<Cve> cves = new HashSet<Cve>();
        cves.add(cve);

        // Create a server with a channel, one errata and a patch package
        // already installed
        User user = createTestUser();
        Errata errata = createTestErrata(user, cves);
        Channel channel = createTestChannel(user, errata);
        Set<Channel> channels = new HashSet<Channel>();
        channels.add(channel);
        Package unpatched = createTestPackage(user, channel, "noarch");
        Package patched = createLaterTestPackage(user, errata, channel, unpatched);
        Server server = createTestServer(user, channels);
        createTestInstalledPackage(patched, server);
        CVEAuditManager.populateCVEChannels();

        // No filtering
        EnumSet<PatchStatus> filter = EnumSet.allOf(PatchStatus.class);
        List<CVEAuditServer> results =
                CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
        assertSystemPatchStatus(server, PatchStatus.PATCHED, results);

        // Everything is filtered except expected
        filter = EnumSet.of(PatchStatus.PATCHED);
        results = CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
        assertSystemPatchStatus(server, PatchStatus.PATCHED, results);

        // Only the expected result is filtered
        filter = EnumSet.complementOf(filter);
        results = CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
        assertSystemNotFound(server, results);
    }

    /**
     * Runs listSystemsByPatchStatus on a server with patch status
     * AFFECTED_PATCH_APPLICABLE and tests result filtering.
     * @throws Exception if anything goes wrong
     */
    public void testListSystemsByPatchStatusAffectedPatchApplicable() throws Exception {
        // Create a CVE number
        String cveName = TestUtils.randomString().substring(0, 13);
        Cve cve = createTestCve(cveName);
        Set<Cve> cves = new HashSet<Cve>();
        cves.add(cve);

        // Create a server with a channel, one errata and an upgradable package
        // already installed
        User user = createTestUser();
        Errata errata = createTestErrata(user, cves);
        Channel channel = createTestChannel(user, errata);
        Set<Channel> channels = new HashSet<Channel>();
        channels.add(channel);
        Package unpatched = createTestPackage(user, channel, "noarch");
        createLaterTestPackage(user, errata, channel, unpatched);
        Server server = createTestServer(user, channels);
        createTestInstalledPackage(unpatched, server);
        CVEAuditManager.populateCVEChannels();

        // No filtering
        EnumSet<PatchStatus> filter = EnumSet.allOf(PatchStatus.class);
        List<CVEAuditServer> results =
                CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
        assertSystemPatchStatus(server, PatchStatus.AFFECTED_PATCH_APPLICABLE, results);

        // Everything is filtered except expected
        filter = EnumSet.of(PatchStatus.AFFECTED_PATCH_APPLICABLE);
        results = CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
        assertSystemPatchStatus(server, PatchStatus.AFFECTED_PATCH_APPLICABLE, results);

        // Only the expected result is filtered
        filter = EnumSet.complementOf(filter);
        results = CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
        assertSystemNotFound(server, results);
    }

    /**
     * Runs listSystemsByPatchStatus on a server with patch status
     * AFFECTED_PATCH_INAPPLICABLE and tests result filtering.
     * @throws Exception if anything goes wrong
     */
    public void testListSystemsByPatchStatusAffectedPatchInapplicable() throws Exception {
        // Create a CVE number
        String cveName = TestUtils.randomString().substring(0, 13);
        Cve cve = createTestCve(cveName);
        Set<Cve> cves = new HashSet<Cve>();
        cves.add(cve);

        // Create a product
        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct product = createTestSUSEProduct(channelFamily);
        ChannelProduct channelProduct = createTestChannelProduct();
        // Create channels for the product
        Channel baseChannel = createTestVendorBaseChannel(channelFamily, channelProduct);
        Channel childChannel = createTestVendorChildChannel(baseChannel, channelProduct);
        // Assign channels to SUSE product
        createTestSUSEProductChannel(baseChannel, product, true);
        createTestSUSEProductChannel(childChannel, product, true);

        // Create an errata for a package, create patched and unpatched versions
        User user = createTestUser();
        Package unpatched = createTestPackage(user, baseChannel, "noarch");
        Errata errata = createTestErrata(user, cves);
        childChannel.addErrata(errata);
        TestUtils.saveAndFlush(childChannel);
        createLaterTestPackage(user, errata, childChannel, unpatched);

        // Create server with unpatched package
        Set<Channel> channels = new HashSet<Channel>();
        channels.add(baseChannel);
        Server server = createTestServer(user, channels);
        createTestInstalledPackage(unpatched, server);
        CVEAuditManager.populateCVEChannels();

        // No filtering
        EnumSet<PatchStatus> filter = EnumSet.allOf(PatchStatus.class);
        List<CVEAuditServer> results = CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
        assertSystemPatchStatus(server, PatchStatus.AFFECTED_PATCH_INAPPLICABLE, results);

        // Everything is filtered except expected
        filter = EnumSet.of(PatchStatus.AFFECTED_PATCH_INAPPLICABLE);
        results = CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
        assertSystemPatchStatus(server, PatchStatus.AFFECTED_PATCH_INAPPLICABLE, results);

        // Only the expected result is filtered
        filter = EnumSet.complementOf(filter);
        results = CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
        assertSystemNotFound(server, results);
    }

    /**
     * Runs listSystemsByPatchStatus on a server with patch status UNKNOWN,
     * checking that the same result is produced whether it has no relevant
     * channels or, at least, one.
     * @throws Exception if anything goes wrong
     */
    public void testListSystemsByPatchStatusNotAffectedWithNonRelevantPatches()
            throws Exception {
        // Create a CVE number
        String cveName = TestUtils.randomString().substring(0, 13);
        Cve cve = createTestCve(cveName);
        Set<Cve> cves = new HashSet<Cve>();
        cves.add(cve);

        // Create a server with a channel, one errata and an upgradable package
        // already installed
        User user = createTestUser();
        Channel channel = createTestChannel(user);
        Set<Channel> channels = new HashSet<Channel>();
        channels.add(channel);
        Package unpatched = createTestPackage(user, channel, "noarch");
        Server server = createTestServer(user, channels);
        createTestInstalledPackage(unpatched, server);

        // Create an errata with a patched package in an unrelated channel
        Errata errata = createTestErrata(user, cves);
        Channel unrelatedChannel = createTestChannel(user, errata);
        createLaterTestPackage(user, errata, unrelatedChannel, unpatched);

        // Test that server is not affected (eg. we do not have an applicable
        // patch for that vulnerability)
        EnumSet<PatchStatus> filter = EnumSet.allOf(PatchStatus.class);
        List<CVEAuditServer> results = CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
        assertSystemPatchStatus(server, PatchStatus.NOT_AFFECTED, results);
    }

    /**
     * Verify that channels containing patches are returned in the right order.
     * @throws Exception if anything goes wrong
     */
    public void testChannelOrderWithClonedChannels() throws Exception {
        // Create a CVE number
        String cveName = TestUtils.randomString().substring(0, 13);
        Cve cve = createTestCve(cveName);
        Set<Cve> cves = new HashSet<Cve>();
        cves.add(cve);

        // Create a channel with errata and packages
        User user = createTestUser();
        Errata errata = createTestErrata(user, cves);
        Channel channel = createTestChannel(user, errata);
        Package unpatched = createTestPackage(user, channel, "noarch");
        Package patched = createLaterTestPackage(user, errata, channel, unpatched);
        List<Package> packages = new ArrayList<Package>();
        packages.add(unpatched);
        packages.add(patched);

        // Create clones of channel and errata
        Errata errataClone = createTestClonedErrata(user, errata, cves, patched);
        Channel channelClone =
                createTestClonedChannel(user, errataClone, channel, packages);

        // Subscribe server to channel and install unpatched package
        Set<Channel> assignedChannels = new HashSet<Channel>();
        assignedChannels.add(channelClone);
        Server server = createTestServer(user, assignedChannels);
        createTestInstalledPackage(unpatched, server);

        // Find the relevant channels and ask for the above CVE
        CVEAuditManager.populateCVEChannels();
        List<CVEAuditServer> results =
                CVEAuditManager.listSystemsByPatchStatus(user, cveName, EnumSet.allOf(PatchStatus.class));

        // Verify the order of returned channels
        CVEAuditSystem result = findSystemRecord(server, results);
        assertEquals(PatchStatus.AFFECTED_PATCH_APPLICABLE, result.getPatchStatus());
        Iterator<ChannelIdNameLabelTriple> it = result.getChannels().iterator();
        assertEquals((Long) channelClone.getId(), (Long) it.next().getId());
    }

    /**
     * Verify that bnc#831047 is fixed:
     * Test the ON DELETE CASCADE of FK constraint to rhnServer.
     * @throws Exception if anything goes wrong
     */
    public void testFKConstraintServers() throws Exception {
        // Create just a server and a channel
        User user = createTestUser();
        Channel channel = createTestChannel(user);
        Set<Channel> channels = new HashSet<Channel>();
        channels.add(channel);
        Server server = createTestServer(user, channels);
        CVEAuditManager.populateCVEChannels();

        // Delete the server while it is still referenced
        ServerFactory.delete(server);
    }

    /**
     * Verify that bnc#831047 is fixed:
     * Test the ON DELETE CASCADE of FK constraint to rhnChannel.
     * @throws Exception if anything goes wrong
     */
    public void testFKConstraintChannels() throws Exception {
        // Create just a server and a channel
        User user = createTestUser();
        Channel channel = createTestChannel(user);
        Set<Channel> channels = new HashSet<Channel>();
        channels.add(channel);
        @SuppressWarnings("unused")
        Server server = createTestServer(user, channels);
        CVEAuditManager.populateCVEChannels();

        // Delete the channel while it is still referenced
        ChannelFactory.remove(channel);
    }

    /**
     * Verify that bnc#833783 is fixed:
     * Test that irrelevant packages do not alter a system's PATCHED status
     * @throws Exception if anything goes wrong
     */
    public void testPatchedSystemWithIrrelevantErrata() throws Exception {
        // Create a CVE number
        String cveName = TestUtils.randomString().substring(0, 13);
        Cve cve = createTestCve(cveName);
        Set<Cve> cves = new HashSet<Cve>();
        cves.add(cve);

        // Create a server with a channel, one errata and a patch package
        // already installed
        User user = createTestUser();
        Errata errata = createTestErrata(user, cves);
        Channel channel = createTestChannel(user, errata);
        Set<Channel> channels = new HashSet<Channel>();
        channels.add(channel);
        Package unpatched = createTestPackage(user, channel, "noarch");
        Package patched = createLaterTestPackage(user, errata, channel, unpatched);
        Server server = createTestServer(user, channels);
        createTestInstalledPackage(patched, server);
        CVEAuditManager.populateCVEChannels();

        // Create a further errata on a different package set
        Errata irrelevantErrata = createTestErrata(user, cves);
        createTestPackage(user, channel, "noarch");
        createLaterTestPackage(user, irrelevantErrata, channel, unpatched);

        // No filtering
        EnumSet<PatchStatus> filter = EnumSet.allOf(PatchStatus.class);
        List<CVEAuditServer> results = CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
        assertSystemPatchStatus(server, PatchStatus.PATCHED, results);
    }

    /**
     * Runs testPatchPartlyApplied on a server bnc#899266
     * @throws Exception if anything goes wrong
     */
    public void testPatchPartlyApplied() throws Exception {
        // Create a CVE number
        String cveName = TestUtils.randomString().substring(0, 13);
        Cve cve = createTestCve(cveName);
        Set<Cve> cves = new HashSet<Cve>();
        cves.add(cve);

        // Create a server with a channel, one errata, one upgradable package
        // and one package which is already installed
        User user = createTestUser();
        Errata errata = createTestErrata(user, cves);
        Channel channel = createTestChannel(user, errata);
        Set<Channel> channels = new HashSet<Channel>();
        channels.add(channel);
        // order is important first package is installed, the second one
        // can be upgraded.
        Package patched = createTestPackage(user, errata, channel, "noarch");
        Package unpatched = createTestPackage(user, channel, "noarch");
        createLaterTestPackage(user, errata, channel, unpatched);

        Server server = createTestServer(user, channels);
        createTestInstalledPackage(unpatched, server);
        createTestInstalledPackage(patched, server);
        CVEAuditManager.populateCVEChannels();

        // No filtering
        EnumSet<PatchStatus> filter = EnumSet.allOf(PatchStatus.class);
        List<CVEAuditServer> results = CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
        assertSystemPatchStatus(server, PatchStatus.AFFECTED_PATCH_APPLICABLE, results);
    }

    /**
     * Runs audit against a CVE which is covered by multiple erratas, and some of these erratas are missing in the
     * assigned channels (see bsc#1111963).
     * @throws Exception if anything goes wrong
     */
    public void testMultipleChannelsMultipleErratas() throws Exception {

        // Situation: The CVE has 2 different patches assigned to it. First of these patches are already installed
        // in the system. The later patch is only included in the original (e.g. vendor) channel and it is not
        // assigned.

        // Create a CVE number
        String cveName = TestUtils.randomString().substring(0, 13);
        Cve cve = createTestCve(cveName);
        Set<Cve> cves = new HashSet<>();
        cves.add(cve);

        // Create two erratas. One exists in both channels, second exists only in the original channel
        User user = createTestUser();
        Errata e1 = createTestErrata(user, cves);
        Errata e1clone = createTestErrata(user, cves);
        Errata e2 = createTestErrata(user, cves);

        // Create the original channel which includes all the erratas (up-to-date channel)
        Channel c1 = createTestChannel(user);
        c1.addErrata(e1);
        c1.addErrata(e2);

        // Create package p1 included in errata 1 and in both channels
        Package p1 = createTestPackage(user, e1, c1, "noarch");
        // Add the p1 package to the errata 1 clone
        e1clone.addPackage(p1);

        // Create package p2 as initially included in both channels
        Package p2 = createTestPackage(user, c1, "noarch");
        // Create a later version of p2 which is only included in e2
        Package p2later = createLaterTestPackage(user, e2, c1, p2);

        // Clone p1 and the older p2 package and the clone of errata e1 into a cloned channel
        // Errata 2 which patches p2 package to a newer version is the only thing missing in the cloned channel
        // This is the only difference between the two channels
        // The cloned channel will also have a higher channel rank (lower int value)
        Set<Package> packagesToClone = new HashSet<>();
        packagesToClone.add(p1);
        packagesToClone.add(p2);
        Channel c2 = createTestClonedChannel(user, e1clone, c1, packagesToClone);

        // Create the server and assign only the cloned channel into it
        // Install p1 and older p2 packages
        Server server = createTestServer(user, Collections.singleton(c2));
        createTestInstalledPackage(p1, server);
        createTestInstalledPackage(p2, server);

        CVEAuditManager.populateCVEChannels();

        // No filtering
        EnumSet<PatchStatus> filter = EnumSet.allOf(PatchStatus.class);
        List<CVEAuditServer> results = CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
        assertSystemPatchStatus(server, PatchStatus.AFFECTED_PATCH_INAPPLICABLE, results);

        // Assert correct number of results
        assertEquals(1, results.size());
        CVEAuditServer serverResult = results.get(0);

        // Assert correct (unnassigned) channel being reported
        assertEquals(1, serverResult.getChannels().size());
        assertEquals((long)c1.getId(), serverResult.getChannels().iterator().next().getId());

        // Assert correct missing errata being reported
        assertEquals(1, serverResult.getErratas().size());
        assertEquals((long)e2.getId(), serverResult.getErratas().iterator().next().getId());
    }

    /**
     * Runs testMultiVersionPackage on a server bsc#903723
     * @throws Exception if anything goes wrong
     */
    public void testMultiVersionPackage() throws Exception {
        // Create a CVE number
        String cveName = TestUtils.randomString().substring(0, 13);
        Cve cve = createTestCve(cveName);
        Set<Cve> cves = new HashSet<Cve>();
        cves.add(cve);

        // Create a server with a channel, one errata
        // and two packages (same name) which are installed
        User user = createTestUser();
        Errata errata = createTestErrata(user, cves);
        Channel channel = createTestChannel(user, errata);
        Set<Channel> channels = new HashSet<Channel>();
        channels.add(channel);

        Package p1 = createTestPackage(user, errata, channel, "noarch");
        Package p2 = createLaterTestPackage(user, errata, channel, p1);

        Server server = createTestServer(user, channels);
        createTestInstalledPackage(p1, server);
        createTestInstalledPackage(p2, server);
        CVEAuditManager.populateCVEChannels();

        // No filtering
        EnumSet<PatchStatus> filter = EnumSet.allOf(PatchStatus.class);
        List<CVEAuditServer> results = CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
        assertSystemPatchStatus(server, PatchStatus.PATCHED, results);
    }

    /**
     * Verify that a system is PATCHED as soon as at least one patch is installed, even
     * though there is another patch available in the next Service Pack (bsc#926146).
     *
     * @throws Exception if anything goes wrong
     */
    public void testLTSS() throws Exception {
        // Create a CVE number
        String cveName = TestUtils.randomString().substring(0, 13);
        Cve cve = createTestCve(cveName);
        Set<Cve> cves = new HashSet<Cve>();
        cves.add(cve);

        // Create SP2 and SP3 products + upgrade path
        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct productSP2 = createTestSUSEProduct(channelFamily);
        SUSEProduct productSP3 = createTestSUSEProduct(channelFamily);
        createTestSUSEUpgradePath(productSP2, productSP3);

        // Create channels for the products
        ChannelProduct channelProductSP2 = createTestChannelProduct();
        ChannelProduct channelProductSP3 = createTestChannelProduct();
        Channel baseChannelSP2 = createTestVendorBaseChannel(channelFamily, channelProductSP2);
        Channel ltssChannelSP2 = createTestVendorChildChannel(baseChannelSP2, channelProductSP2);
        Channel baseChannelSP3 = createTestVendorBaseChannel(channelFamily, channelProductSP3);
        Channel updateChannelSP3 = createTestVendorChildChannel(baseChannelSP3, channelProductSP3);

        // Assign channels to products (LTSS channel is *not* part of the product!)
        createTestSUSEProductChannel(baseChannelSP2, productSP2, true);
        createTestSUSEProductChannel(baseChannelSP3, productSP3, true);
        createTestSUSEProductChannel(updateChannelSP3, productSP3, true);

        // Create two errata: one in the LTSS channel and one in SP3 updates
        User user = createTestUser();
        Package unpatched = createTestPackage(user, baseChannelSP2, "noarch");

        Errata errataLTSS = createTestErrata(user, cves);
        ltssChannelSP2.addErrata(errataLTSS);
        TestUtils.saveAndFlush(ltssChannelSP2);
        Package patchedLTSS = createLaterTestPackage(user, errataLTSS, ltssChannelSP2, unpatched);

        Errata errataSP3 = createTestErrata(user, cves);
        updateChannelSP3.addErrata(errataSP3);
        TestUtils.saveAndFlush(updateChannelSP3);
        createLaterTestPackage(user, errataSP3, updateChannelSP3, patchedLTSS);

        // Setup SP2 channels
        Set<Channel> channelsSP2 = new HashSet<Channel>();
        channelsSP2.add(baseChannelSP2);
        channelsSP2.add(ltssChannelSP2);

        // Create server1: SP2 channels, only unpatched package installed (APPLICABLE)
        Server server1 = createTestServer(user, channelsSP2);
        createTestInstalledPackage(unpatched, server1);

        // Create server2: SP2 channels, patched package installed (PATCHED)
        Server server2 = createTestServer(user, channelsSP2);
        createTestInstalledPackage(patchedLTSS, server2);

        // Create server3: SP2 channels with LTSS patch installed + SP3 patch available,
        // should still return as PATCHED!
        Server server3 = createTestServer(user, channelsSP2);
        createTestInstalledPackage(patchedLTSS, server3);
        installSUSEProductOnServer(productSP2, server3);

        CVEAuditManager.populateCVEChannels();

        EnumSet<PatchStatus> filter = EnumSet.allOf(PatchStatus.class);
        List<CVEAuditServer> results =
                CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
        assertSystemPatchStatus(server1, PatchStatus.AFFECTED_PATCH_APPLICABLE, results);
        assertSystemPatchStatus(server2, PatchStatus.PATCHED, results);
        assertSystemPatchStatus(server3, PatchStatus.PATCHED, results);
    }

    /**
     * Test corner case where a system has a CVE covered by a package installed
     * from a further away channel rank, but the closest one is not installed
     *
     * @throws Exception if anything goes wrong
     */
    public void testCoveredByUnassigned() throws Exception {
        // Create a CVE number
        String cveName = TestUtils.randomString().substring(0, 13);
        Cve cve = createTestCve(cveName);
        Set<Cve> cves = new HashSet<Cve>();
        cves.add(cve);

        // Create SP2 and SP3 products + upgrade path
        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct productSP2 = createTestSUSEProduct(channelFamily);
        SUSEProduct productSP3 = createTestSUSEProduct(channelFamily);
        createTestSUSEUpgradePath(productSP2, productSP3);

        // Create channels for the products
        ChannelProduct channelProductSP2 = createTestChannelProduct();
        ChannelProduct channelProductSP3 = createTestChannelProduct();
        Channel baseChannelSP2 = createTestVendorBaseChannel(channelFamily, channelProductSP2);
        Channel ltssChannelSP2 = createTestVendorChildChannel(baseChannelSP2, channelProductSP2);
        Channel baseChannelSP3 = createTestVendorBaseChannel(channelFamily, channelProductSP3);
        Channel updateChannelSP3 = createTestVendorChildChannel(baseChannelSP3, channelProductSP3);

        // Assign channels to products (LTSS channel is *not* part of the product!)
        createTestSUSEProductChannel(baseChannelSP2, productSP2, true);
        createTestSUSEProductChannel(baseChannelSP3, productSP3, true);
        createTestSUSEProductChannel(updateChannelSP3, productSP3, true);

        // Create two errata: one in the LTSS channel and one in SP3 updates
        User user = createTestUser();
        Package unpatched = createTestPackage(user, baseChannelSP2, "noarch");

        Errata errataLTSS = createTestErrata(user, cves);
        ltssChannelSP2.addErrata(errataLTSS);
        TestUtils.saveAndFlush(ltssChannelSP2);

        Errata errataSP3 = createTestErrata(user, cves);
        updateChannelSP3.addErrata(errataSP3);
        TestUtils.saveAndFlush(updateChannelSP3);
        Package patchedSP3 = createLaterTestPackage(user, errataSP3, updateChannelSP3, unpatched);

        // as this is a very hypothetical scenario, the LTSS package has higher version than the
        // next SP package, otherwise it would show up as "installed"
        Package patchedLTSS = createLaterTestPackage(user, errataLTSS, ltssChannelSP2, patchedSP3);

        // Setup SP2 channels
        Set<Channel> channelsSP2 = new HashSet<Channel>();
        channelsSP2.add(baseChannelSP2);
        channelsSP2.add(ltssChannelSP2);

        // Create server: SP2 channels with LTSS patch available + SP3 patch installed
        // (not a normal situation, he may had copied it)
        // should still return as PATCHED!
        Server server = createTestServer(user, channelsSP2);
        createTestInstalledPackage(patchedSP3, server);
        installSUSEProductOnServer(productSP2, server);

        CVEAuditManager.populateCVEChannels();

        EnumSet<PatchStatus> filter = EnumSet.allOf(PatchStatus.class);
        List<CVEAuditServer> results =
                CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
        assertSystemPatchStatus(server, PatchStatus.PATCHED, results);
    }

    public void testAssignedChannelPreferredOverMigration() throws Exception {
        // Create a CVE number
        String cveName = TestUtils.randomString().substring(0, 13);
        Cve cve = createTestCve(cveName);
        Set<Cve> cves = new HashSet<>();
        cves.add(cve);

        // Create SP2 and SP3 products + upgrade path
        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct productSP2 = createTestSUSEProduct(channelFamily);
        SUSEProduct productSP3 = createTestSUSEProduct(channelFamily);
        createTestSUSEUpgradePath(productSP2, productSP3);

        // Create channels for the products
        ChannelProduct channelProductSP2 = createTestChannelProduct();
        ChannelProduct channelProductSP3 = createTestChannelProduct();
        Channel baseChannelSP2 = createTestVendorBaseChannel(channelFamily, channelProductSP2);
        Channel updateChannelSP2 = createTestVendorChildChannel(baseChannelSP2, channelProductSP2);
        Channel baseChannelSP3 = createTestVendorBaseChannel(channelFamily, channelProductSP3);
        Channel updateChannelSP3 = createTestVendorChildChannel(baseChannelSP3, channelProductSP3);

        // Assign channels to products
        createTestSUSEProductChannel(baseChannelSP2, productSP2, true);
        createTestSUSEProductChannel(updateChannelSP2, productSP2, true);
        createTestSUSEProductChannel(baseChannelSP3, productSP3, true);
        createTestSUSEProductChannel(updateChannelSP3, productSP3, true);

        // Create two errata: one in SP2 updates and one in SP3 updates
        User user = createTestUser();
        Errata errataSP2 = createTestErrata(user, cves);
        updateChannelSP2.addErrata(errataSP2);
        TestUtils.saveAndFlush(updateChannelSP2);
        Errata errataSP3 = createTestErrata(user, cves);
        updateChannelSP3.addErrata(errataSP3);
        TestUtils.saveAndFlush(updateChannelSP3);

        Package unpatched = createTestPackage(user, null, baseChannelSP2, "noarch");
        Package patchedSP2 = createLaterTestPackage(user, errataSP2, updateChannelSP2, unpatched);
        @SuppressWarnings("unused")
        Package patchedSP3 = createLaterTestPackage(user, errataSP3, updateChannelSP3, unpatched);

        // Create server: no patch is installed
        Set<Channel> channelsSP2 = new HashSet<>();
        channelsSP2.add(baseChannelSP2);
        channelsSP2.add(updateChannelSP2);
        Server server = createTestServer(user, channelsSP2);
        createTestInstalledPackage(unpatched, server);
        installSUSEProductOnServer(productSP2, server);

        CVEAuditManager.populateCVEChannels();
        EnumSet<PatchStatus> filter = EnumSet.allOf(PatchStatus.class);
        List<CVEAuditServer> results = CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
        assertSystemPatchStatus(server, PatchStatus.AFFECTED_PATCH_APPLICABLE, results);
        List<ChannelIdNameLabelTriple> channels = new ArrayList<>(results.get(0).getChannels());
        assertEquals(1, channels.size());
        assertEquals(updateChannelSP2.getId().longValue() ,channels.get(0).getId());
    }

    /**
     * Test that older products and service packs are considered *only* if there is no patch
     * available in a current or future product. Even if an "older" patch is fulfilled since
     * the installed package has a higher version number a system should still show up as
     * unpatched in case there is a patch available in the current product.
     *
     * See also here: http://bugzilla.suse.com/show_bug.cgi?id=954983
     *
     * @throws Exception if anything goes wrong
     */
    public void testIgnoreOldProductsWhenCurrentPatchAvailable() throws Exception {
        // Create a CVE number
        String cveName = TestUtils.randomString().substring(0, 13);
        Cve cve = createTestCve(cveName);
        Set<Cve> cves = new HashSet<Cve>();
        cves.add(cve);

        // Create SP2 and SP3 products + upgrade path
        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct productSP2 = createTestSUSEProduct(channelFamily);
        SUSEProduct productSP3 = createTestSUSEProduct(channelFamily);
        createTestSUSEUpgradePath(productSP2, productSP3);

        // Create channels for the products
        ChannelProduct channelProductSP2 = createTestChannelProduct();
        ChannelProduct channelProductSP3 = createTestChannelProduct();
        Channel baseChannelSP2 = createTestVendorBaseChannel(channelFamily, channelProductSP2);
        Channel ltssChannelSP2 = createTestVendorChildChannel(baseChannelSP2, channelProductSP2);
        Channel baseChannelSP3 = createTestVendorBaseChannel(channelFamily, channelProductSP3);
        Channel updateChannelSP3 = createTestVendorChildChannel(baseChannelSP3, channelProductSP3);

        // Assign channels to products
        createTestSUSEProductChannel(baseChannelSP2, productSP2, true);
        createTestSUSEProductChannel(ltssChannelSP2, productSP2, true);
        createTestSUSEProductChannel(baseChannelSP3, productSP3, true);
        createTestSUSEProductChannel(updateChannelSP3, productSP3, true);

        // Create two errata: one in the LTSS channel and one in SP3 updates
        User user = createTestUser();
        Errata errataLTSS = createTestErrata(user, cves);
        ltssChannelSP2.addErrata(errataLTSS);
        TestUtils.saveAndFlush(ltssChannelSP2);
        Errata errataSP3 = createTestErrata(user, cves);
        updateChannelSP3.addErrata(errataSP3);
        TestUtils.saveAndFlush(updateChannelSP3);

        // This is a realistic scenario: the LTSS package has a lower version than the
        // installed "upnatched" package, system showed up as patched in bsc#954983.
        Package patchedLTSS = createTestPackage(user, errataLTSS, ltssChannelSP2, "noarch");
        Package unpatched = createLaterTestPackage(user, null, baseChannelSP3, patchedLTSS);
        @SuppressWarnings("unused")
        Package patchedSP3 = createLaterTestPackage(
                user, errataSP3, updateChannelSP3, unpatched);

        // Create server: no patch is installed
        Set<Channel> channelsSP3 = new HashSet<Channel>();
        channelsSP3.add(baseChannelSP3);
        channelsSP3.add(updateChannelSP3);
        Server server = createTestServer(user, channelsSP3);
        createTestInstalledPackage(unpatched, server);
        installSUSEProductOnServer(productSP3, server);

        // Result is not PATCHED here, even though there is a package with a lower version
        // number from an older product's LTSS channel
        CVEAuditManager.populateCVEChannels();
        EnumSet<PatchStatus> filter = EnumSet.allOf(PatchStatus.class);
        List<CVEAuditServer> results = CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
        assertSystemPatchStatus(server, PatchStatus.AFFECTED_PATCH_APPLICABLE, results);
    }

    /**
     * Test the SDK scenario: one errata with two packages in different channels.
     *
     * @throws Exception if anything goes wrong
     */
    public void testSDK() throws Exception {
        // Create a CVE number
        String cveName = TestUtils.randomString().substring(0, 13);
        Cve cve = createTestCve(cveName);
        Set<Cve> cves = new HashSet<Cve>();
        cves.add(cve);

        // Create one errata that spreads over two different channels
        User user = createTestUser();
        Errata erratum = createTestErrata(user, cves);
        ChannelFamily channelFamily = createTestChannelFamily();
        ChannelProduct channelProductSP3 = createTestChannelProduct();
        Channel baseChannelSP3 = createTestVendorBaseChannel(channelFamily, channelProductSP3);
        Channel childChannelSDK = createTestVendorChildChannel(baseChannelSP3, channelProductSP3);
        baseChannelSP3.addErrata(erratum);
        childChannelSDK.addErrata(erratum);

        // Create two unpatched packages where both have patched packages in their
        // respective channels, all in the same erratum!
        Package unpatchedSDK = createTestPackage(user, childChannelSDK, "noarch");
        Package unpatched = createTestPackage(user, baseChannelSP3, "noarch");
        createLaterTestPackage(user, erratum, childChannelSDK, unpatchedSDK);
        Package patched = createLaterTestPackage(user, erratum, baseChannelSP3, unpatched);

        // server1: SDK channel not assigned, patched package not installed
        // -> AFFECTED_PATCH_APPLICABLE
        Set<Channel> serverChannels = new HashSet<Channel>();
        serverChannels.add(baseChannelSP3);
        Server server1 = createTestServer(user, serverChannels);
        createTestInstalledPackage(unpatched, server1);

        // server2: SDK channel not assigned, patched package installed
        // -> PATCHED
        Server server2 = createTestServer(user, serverChannels);
        createTestInstalledPackage(patched, server2);

        CVEAuditManager.populateCVEChannels();
        EnumSet<PatchStatus> filter = EnumSet.allOf(PatchStatus.class);
        List<CVEAuditServer> results = CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
        assertSystemPatchStatus(server1, PatchStatus.AFFECTED_PATCH_APPLICABLE, results);
        assertSystemPatchStatus(server2, PatchStatus.PATCHED, results);
    }

    /**
     * Test the Live Patching scenario:
     * - kernel and kernel-new in base channel
     * - kgraft-patch and kgraft-patch-new in live-patching child channel
     * - cloned channels
     * - kernel-new not available in the cloned channel
     * - kernel and kgraft-patch-new installed on the server
     * Test that this server is seen as "patched"
     *
     * @throws Exception if anything goes wrong
     */
    public void testLivePatchingAffectedPatched() throws Exception {
        // Create a CVE number
        String cveName = TestUtils.randomString().substring(0, 13);
        Cve cve = createTestCve(cveName);
        Set<Cve> cves = new HashSet<Cve>();
        cves.add(cve);

        // Create two errata that patches the same bug. 1 for kgraft and one for kernel
        User user = createTestUser();
        Errata kernelErratum = createTestErrata(user, cves);
        Errata kgraftErratum = createTestErrata(user, cves);
        ChannelFamily channelFamilySLES = createTestChannelFamily();
        ChannelProduct channelProductSLES12 = createTestChannelProduct();
        ChannelProduct channelProductLP12 = createTestChannelProduct();
        Channel baseChannelSLES12 = createTestVendorBaseChannel(channelFamilySLES, channelProductSLES12);
        Channel childChannelLP12 = createTestVendorChildChannel(baseChannelSLES12, channelProductLP12);
        baseChannelSLES12.addErrata(kernelErratum);
        childChannelLP12.addErrata(kgraftErratum);

        // Create two unpatched packages
        Package kernelDefault = createTestPackage(user, baseChannelSLES12, "i586");
        kernelDefault.setPackageName(PackageNameTest.createTestPackageName("kernel-default"));
        TestUtils.saveAndFlush(kernelDefault);

        Package kgraftDefault = createTestPackage(user, childChannelLP12, "i586");
        kgraftDefault.setPackageName(PackageNameTest.createTestPackageName("kgraft-patch-3_12_62-60_64_8-default"));
        TestUtils.saveAndFlush(kgraftDefault);

        createLaterTestPackage(user, kernelErratum, baseChannelSLES12, kernelDefault);
        Package kgraftDefaultNew = createLaterTestPackage(user, kgraftErratum, childChannelLP12, kgraftDefault);

        List<Package> packagesLP = new ArrayList<>();
        packagesLP.add(kgraftDefault);
        packagesLP.add(kgraftDefaultNew);

        // Create clones of channel and errata
        Channel baseChannelClone = ChannelFactoryTest.createTestClonedChannel(baseChannelSLES12, user);
        baseChannelClone.addPackage(kernelDefault);
        TestUtils.saveAndFlush(baseChannelClone);

        Errata errataKgraftClone = createTestClonedErrata(user, kgraftErratum, cves, kgraftDefaultNew);
        Channel channelClone =
                createTestClonedChannel(user, errataKgraftClone, childChannelLP12, packagesLP);

        // server: old kernel but new kgraft-patch installed from cloned channels
        // -> PATCHED
        Set<Channel> serverChannels = new HashSet<Channel>();
        serverChannels.add(baseChannelClone);
        serverChannels.add(channelClone);
        Server server = createTestServer(user, serverChannels);
        createTestInstalledPackage(kernelDefault, server);
        createTestInstalledPackage(kgraftDefaultNew, server);

        CVEAuditManager.populateCVEChannels();
        EnumSet<PatchStatus> filter = EnumSet.allOf(PatchStatus.class);
        List<CVEAuditServer> results = CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
        assertSystemPatchStatus(server, PatchStatus.PATCHED, results);
    }

    /**
     * Test the Live Patching scenario:
     * - kernel and kernel-new in base channel
     * - kgraft-patch and kgraft-patch-new in live-patching child channel
     * - cloned channels
     * - kernel-new not available in the cloned channel
     * - kernel and kgraft-patch installed on the server
     * Test that this server is seen as "applicable"
     *
     * @throws Exception if anything goes wrong
     */
    public void testLivePatchingAffectedPatchApplicable() throws Exception {
        // Create a CVE number
        String cveName = TestUtils.randomString().substring(0, 13);
        Cve cve = createTestCve(cveName);
        Set<Cve> cves = new HashSet<Cve>();
        cves.add(cve);

        // Create two errata that patches the same bug. 1 for kgraft and one for kernel
        User user = createTestUser();
        Errata kernelErratum = createTestErrata(user, cves);
        Errata kgraftErratum = createTestErrata(user, cves);
        ChannelFamily channelFamilySLES = createTestChannelFamily();
        ChannelProduct channelProductSLES12 = createTestChannelProduct();
        ChannelProduct channelProductLP12 = createTestChannelProduct();
        Channel baseChannelSLES12 = createTestVendorBaseChannel(channelFamilySLES, channelProductSLES12);
        Channel childChannelLP12 = createTestVendorChildChannel(baseChannelSLES12, channelProductLP12);
        baseChannelSLES12.addErrata(kernelErratum);
        childChannelLP12.addErrata(kgraftErratum);

        // Create two unpatched packages
        Package kernelDefault = createTestPackage(user, baseChannelSLES12, "i586");
        kernelDefault.setPackageName(PackageNameTest.createTestPackageName("kernel-default"));
        TestUtils.saveAndFlush(kernelDefault);

        Package kgraftDefault = createTestPackage(user, childChannelLP12, "i586");
        kgraftDefault.setPackageName(PackageNameTest.createTestPackageName("kgraft-patch-3_12_62-60_64_8-default"));
        TestUtils.saveAndFlush(kgraftDefault);

        createLaterTestPackage(user, kernelErratum, baseChannelSLES12, kernelDefault);
        Package kgraftDefaultNew = createLaterTestPackage(user, kgraftErratum, childChannelLP12, kgraftDefault);

        List<Package> packagesLP = new ArrayList<>();
        packagesLP.add(kgraftDefault);
        packagesLP.add(kgraftDefaultNew);

        // Create clones of channel and errata
        Channel baseChannelClone = ChannelFactoryTest.createTestClonedChannel(baseChannelSLES12, user);
        baseChannelClone.addPackage(kernelDefault);
        TestUtils.saveAndFlush(baseChannelClone);

        Errata errataKgraftClone = createTestClonedErrata(user, kgraftErratum, cves, kgraftDefaultNew);
        Channel channelClone = createTestClonedChannel(user, errataKgraftClone, childChannelLP12, packagesLP);

        // server: old kernel but new kgraft-patch installed from cloned channels
        // -> PATCHED
        Set<Channel> serverChannels = new HashSet<Channel>();
        serverChannels.add(baseChannelClone);
        serverChannels.add(channelClone);
        Server server = createTestServer(user, serverChannels);
        createTestInstalledPackage(kernelDefault, server);
        createTestInstalledPackage(kgraftDefault, server);

        CVEAuditManager.populateCVEChannels();
        EnumSet<PatchStatus> filter = EnumSet.allOf(PatchStatus.class);
        List<CVEAuditServer> results = CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
        assertSystemPatchStatus(server, PatchStatus.AFFECTED_PATCH_APPLICABLE, results);
    }

    /**
     * Test the Live Patching scenario:
     * - kernel and kernel-new in base channel
     * - kgraft-patch and kgraft-patch-new in live-patching child channel
     * - cloned channels
     * - kernel-new and kgraft-patch-new not available in the cloned channel
     * - kernel and kgraft-patch installed on the server
     * Test that this server is seen as "inapplicable"
     *
     * @throws Exception if anything goes wrong
     */
    public void testLivePatchingAffectedPatchInapplicable() throws Exception {
        // Create a CVE number
        String cveName = TestUtils.randomString().substring(0, 13);
        Cve cve = createTestCve(cveName);
        Set<Cve> cves = new HashSet<Cve>();
        cves.add(cve);

        // Create two errata that patches the same bug. 1 for kgraft and one for kernel
        User user = createTestUser();
        Errata kernelErratum = createTestErrata(user, cves);
        Errata kgraftErratum = createTestErrata(user, cves);
        ChannelFamily channelFamilySLES = createTestChannelFamily();
        ChannelProduct channelProductSLES12 = createTestChannelProduct();
        ChannelProduct channelProductLP12 = createTestChannelProduct();
        Channel baseChannelSLES12 = createTestVendorBaseChannel(channelFamilySLES, channelProductSLES12);
        Channel childChannelLP12 = createTestVendorChildChannel(baseChannelSLES12, channelProductLP12);
        baseChannelSLES12.addErrata(kernelErratum);
        childChannelLP12.addErrata(kgraftErratum);

        // Create two unpatched packages
        Package kernelDefault = createTestPackage(user, baseChannelSLES12, "i586");
        kernelDefault.setPackageName(PackageNameTest.createTestPackageName("kernel-default"));
        TestUtils.saveAndFlush(kernelDefault);

        Package kgraftDefault = createTestPackage(user, childChannelLP12, "i586");
        kgraftDefault.setPackageName(PackageNameTest.createTestPackageName("kgraft-patch-3_12_62-60_64_8-default"));
        TestUtils.saveAndFlush(kgraftDefault);

        createLaterTestPackage(user, kernelErratum, baseChannelSLES12, kernelDefault);
        Package kgraftDefaultNew = createLaterTestPackage(user, kgraftErratum, childChannelLP12, kgraftDefault);

        List<Package> packagesLP = new ArrayList<>();
        packagesLP.add(kgraftDefault);
        packagesLP.add(kgraftDefaultNew);

        // Create clones of channel and errata
        Channel baseChannelClone = ChannelFactoryTest.createTestClonedChannel(baseChannelSLES12, user);
        baseChannelClone.addPackage(kernelDefault);
        TestUtils.saveAndFlush(baseChannelClone);

        Channel channelClone = ChannelFactoryTest.createTestClonedChannel(childChannelLP12, user);
        channelClone.addPackage(kgraftDefault);
        TestUtils.saveAndFlush(channelClone);

        // server: old kernel but new kgraft-patch installed from cloned channels
        // -> PATCHED
        Set<Channel> serverChannels = new HashSet<Channel>();
        serverChannels.add(baseChannelClone);
        serverChannels.add(channelClone);
        Server server = createTestServer(user, serverChannels);
        createTestInstalledPackage(kernelDefault, server);
        createTestInstalledPackage(kgraftDefault, server);

        CVEAuditManager.populateCVEChannels();
        EnumSet<PatchStatus> filter = EnumSet.allOf(PatchStatus.class);
        List<CVEAuditServer> results = CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
        assertSystemPatchStatus(server, PatchStatus.AFFECTED_PATCH_INAPPLICABLE, results);
    }

    /**
     * Test the Live Patching scenario:
     * - kernel and kernel-new in base channel
     * - kgraft-patch and kgraft-patch-new in live-patching child channel
     * - cloned channels
     * - kernel-new and kgraft-patch-new available in the cloned channel
     * - kernel and kgraft-patch installed on the server
     * - CVE only patched in the real kernel.
     * Test that this server is seen as "applicable" and the only solution is to update the kernel
     *
     * @throws Exception if anything goes wrong
     */
    public void testLivePatchingAffectedOnlyKernelPatchApplicable() throws Exception {
        // Create a CVE number
        String cveName = TestUtils.randomString().substring(0, 13);
        Cve cve = createTestCve(cveName);
        Set<Cve> cvesKgraft = new HashSet<Cve>();
        cvesKgraft.add(cve);

        String cveNameKernelExclusive = TestUtils.randomString().substring(0, 13);
        Cve cveKernelExclusive = createTestCve(cveNameKernelExclusive);
        Set<Cve> cvesKernel = new HashSet<Cve>();
        cvesKernel.add(cve);
        cvesKernel.add(cveKernelExclusive);

        // Create two errata that patches the same bug. 1 for kgraft and one for kernel
        User user = createTestUser();
        Errata kernelErratum = createTestErrata(user, cvesKernel);
        Errata kgraftErratum = createTestErrata(user, cvesKgraft);
        ChannelFamily channelFamilySLES = createTestChannelFamily();
        ChannelProduct channelProductSLES12 = createTestChannelProduct();
        ChannelProduct channelProductLP12 = createTestChannelProduct();
        Channel baseChannelSLES12 = createTestVendorBaseChannel(channelFamilySLES, channelProductSLES12);
        Channel childChannelLP12 = createTestVendorChildChannel(baseChannelSLES12, channelProductLP12);
        baseChannelSLES12.addErrata(kernelErratum);
        childChannelLP12.addErrata(kgraftErratum);

        // Create two unpatched packages
        Package kernelDefault = createTestPackage(user, baseChannelSLES12, "i586");
        kernelDefault.setPackageName(PackageNameTest.createTestPackageName("kernel-default"));
        TestUtils.saveAndFlush(kernelDefault);

        Package kgraftDefault = createTestPackage(user, childChannelLP12, "i586");
        kgraftDefault.setPackageName(PackageNameTest.createTestPackageName("kgraft-patch-3_12_62-60_64_8-default"));
        TestUtils.saveAndFlush(kgraftDefault);

        Package kernelDefaultNew = createLaterTestPackage(user, kernelErratum, baseChannelSLES12, kernelDefault);
        Package kgraftDefaultNew = createLaterTestPackage(user, kgraftErratum, childChannelLP12, kgraftDefault);

        List<Package> packagesSLES = new ArrayList<>();
        packagesSLES.add(kernelDefault);
        packagesSLES.add(kernelDefaultNew);

        List<Package> packagesLP = new ArrayList<>();
        packagesLP.add(kgraftDefault);
        packagesLP.add(kgraftDefaultNew);

        // Create clones of channel and errata
        Errata errataKernelClone = createTestClonedErrata(user, kernelErratum, cvesKernel, kernelDefaultNew);
        Channel baseChannelClone = createTestClonedChannel(user, errataKernelClone, baseChannelSLES12, packagesSLES);

        Errata errataKgraftClone = createTestClonedErrata(user, kgraftErratum, cvesKgraft, kgraftDefaultNew);
        Channel channelClone = createTestClonedChannel(user, errataKgraftClone, childChannelLP12, packagesLP);

        // server: old kernel but new kgraft-patch installed from cloned channels
        // -> PATCHED
        Set<Channel> serverChannels = new HashSet<Channel>();
        serverChannels.add(baseChannelClone);
        serverChannels.add(channelClone);
        Server server = createTestServer(user, serverChannels);
        createTestInstalledPackage(kernelDefault, server);
        createTestInstalledPackage(kgraftDefault, server);

        CVEAuditManager.populateCVEChannels();
        EnumSet<PatchStatus> filter = EnumSet.allOf(PatchStatus.class);
        List<CVEAuditServer> results = CVEAuditManager.listSystemsByPatchStatus(user, cveNameKernelExclusive, filter);
        assertSystemPatchStatus(server, PatchStatus.AFFECTED_PATCH_APPLICABLE, results);

        Iterator<ChannelIdNameLabelTriple> it = results.get(0).getChannels().iterator();
        assertEquals((Long) baseChannelClone.getId(), (Long) it.next().getId());

        Iterator<ErrataIdAdvisoryPair> eit = results.get(0).getErratas().iterator();
        assertEquals((Long) errataKernelClone.getId(), (Long) eit.next().getId());
    }

    /**
     * Runs listImagesByPatchStatus on a server with patch status NOT_AFFECTED
     * and tests result filtering.
     * @throws Exception if anything goes wrong
     */
    public void testListImagesByPatchStatusNotAffected() throws Exception {
        // Create a CVE number
        String cveName = TestUtils.randomString().substring(0, 13);
        Cve cve = createTestCve(cveName);
        Set<Cve> cves = new HashSet<Cve>();
        cves.add(cve);

        // Create a server with a channel, one errata but no vulnerable
        // installed package
        User user = createTestUser();
        Errata errata = createTestErrata(user, cves);
        Channel channel = createTestChannel(user, errata);
        Set<Channel> channels = new HashSet<Channel>();
        channels.add(channel);
        createTestPackage(user, errata, channel, "noarch");
        ImageInfo image = createImageInfo(channels, user);
        CVEAuditManager.populateCVEChannels();

        // No filtering
        EnumSet<PatchStatus> filter = EnumSet.allOf(PatchStatus.class);
        List<CVEAuditImage> results =
                CVEAuditManager.listImagesByPatchStatus(user, cveName, filter);
        assertImagePatchStatus(image, PatchStatus.NOT_AFFECTED, results);

        // Everything is filtered except expected
        filter = EnumSet.of(PatchStatus.NOT_AFFECTED);
        results = CVEAuditManager.listImagesByPatchStatus(user, cveName, filter);
        assertImagePatchStatus(image, PatchStatus.NOT_AFFECTED, results);

        // Only the expected result is filtered
        filter = EnumSet.complementOf(filter);
        results = CVEAuditManager.listImagesByPatchStatus(user, cveName, filter);
        assertImageNotFound(image, results);
    }

    /**
     * Runs listImagesByPatchStatus on a server with patch status
     * AFFECTED_PATCH_APPLICABLE and tests result filtering.
     * @throws Exception if anything goes wrong
     */
    public void testListImagesByPatchStatusAffectedPatchApplicable() throws Exception {
        // Create a CVE number
        String cveName = TestUtils.randomString().substring(0, 13);
        Cve cve = createTestCve(cveName);
        Set<Cve> cves = new HashSet<Cve>();
        cves.add(cve);

        // Create a server with a channel, one errata and an upgradable package
        // already installed
        User user = createTestUser();
        Errata errata = createTestErrata(user, cves);
        Channel channel = createTestChannel(user, errata);
        Set<Channel> channels = new HashSet<Channel>();
        channels.add(channel);
        Package unpatched = createTestPackage(user, channel, "noarch");
        createLaterTestPackage(user, errata, channel, unpatched);

        ImageInfo image = createImageInfo(channels, user);
        createImagePackage(unpatched, image);

        CVEAuditManager.populateCVEChannels();

        // No filtering
        EnumSet<PatchStatus> filter = EnumSet.allOf(PatchStatus.class);
        List<CVEAuditImage> results =
                CVEAuditManager.listImagesByPatchStatus(user, cveName, filter);
        assertImagePatchStatus(image, PatchStatus.AFFECTED_PATCH_APPLICABLE, results);

        // Everything is filtered except expected
        filter = EnumSet.of(PatchStatus.AFFECTED_PATCH_APPLICABLE);
        results = CVEAuditManager.listImagesByPatchStatus(user, cveName, filter);
        assertImagePatchStatus(image, PatchStatus.AFFECTED_PATCH_APPLICABLE, results);

        // Only the expected result is filtered
        filter = EnumSet.complementOf(filter);
        results = CVEAuditManager.listImagesByPatchStatus(user, cveName, filter);
        assertImageNotFound(image, results);
    }

    /**
     * Runs listSystemsByPatchStatus on a server with patch status
     * AFFECTED_PATCH_INAPPLICABLE and tests result filtering.
     * @throws Exception if anything goes wrong
     */
    public void testListImagesByPatchStatusAffectedPatchInapplicable() throws Exception {
        // Create a CVE number
        String cveName = TestUtils.randomString().substring(0, 13);
        Cve cve = createTestCve(cveName);
        Set<Cve> cves = new HashSet<Cve>();
        cves.add(cve);

        // Create a product
        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct product = createTestSUSEProduct(channelFamily);
        ChannelProduct channelProduct = createTestChannelProduct();
        // Create channels for the product
        Channel baseChannel = createTestVendorBaseChannel(channelFamily, channelProduct);
        Channel childChannel = createTestVendorChildChannel(baseChannel, channelProduct);
        // Assign channels to SUSE product
        createTestSUSEProductChannel(baseChannel, product, true);
        createTestSUSEProductChannel(childChannel, product, true);

        // Create an errata for a package, create patched and unpatched versions
        User user = createTestUser();
        Package unpatched = createTestPackage(user, baseChannel, "noarch");
        Errata errata = createTestErrata(user, cves);
        childChannel.addErrata(errata);
        TestUtils.saveAndFlush(childChannel);
        createLaterTestPackage(user, errata, childChannel, unpatched);

        // Create server with unpatched package
        Set<Channel> channels = new HashSet<Channel>();
        channels.add(baseChannel);

        ImageInfo image = createImageInfo(channels, user);
        createImagePackage(unpatched, image);

        CVEAuditManager.populateCVEChannels();

        // No filtering
        EnumSet<PatchStatus> filter = EnumSet.allOf(PatchStatus.class);
        List<CVEAuditImage> results =
                CVEAuditManager.listImagesByPatchStatus(user, cveName, filter);
        assertImagePatchStatus(image, PatchStatus.AFFECTED_PATCH_INAPPLICABLE, results);

        // Everything is filtered except expected
        filter = EnumSet.of(PatchStatus.AFFECTED_PATCH_INAPPLICABLE);
        results = CVEAuditManager.listImagesByPatchStatus(user, cveName, filter);
        assertImagePatchStatus(image, PatchStatus.AFFECTED_PATCH_INAPPLICABLE, results);

        // Only the expected result is filtered
        filter = EnumSet.complementOf(filter);
        results = CVEAuditManager.listImagesByPatchStatus(user, cveName, filter);
        assertImageNotFound(image, results);
    }

    /**
     * Verify that we didn't mess up the result from the ordering
     * of the database during the evaluation of the result (bsc#1101033)
     *
     * @throws Exception if anything goes wrong
     */
    public void testResultContainsDistinctResults() throws Exception {
        // Create a CVE number
        String cveName = TestUtils.randomString().substring(0, 13);
        Cve cve = createTestCve(cveName);
        Set<Cve> cves = new HashSet<Cve>();
        cves.add(cve);

        // Create a channel with errata and packages
        User user = createTestUser();
        Errata errata = createTestErrata(user, cves);
        Channel channel = createTestChannel(user, errata);
        Package unpatched = createTestPackage(user, channel, "noarch");
        Package patched = createLaterTestPackage(user, errata, channel, unpatched);
        List<Package> packages = new ArrayList<Package>();
        packages.add(unpatched);
        packages.add(patched);

        // Create clones of channel and errata
        Errata errataClone = createTestClonedErrata(user, errata, cves, patched);
        Channel channelClone = createTestClonedChannel(user, errataClone, channel, packages);

        // Subscribe two servers to channel and install unpatched package
        Set<Channel> assignedChannels = new HashSet<Channel>();
        assignedChannels.add(channelClone);
        Server server1 = createTestServer(user, assignedChannels);
        createTestInstalledPackage(unpatched, server1);
        Server server2 = createTestServer(user, assignedChannels);
        createTestInstalledPackage(unpatched, server2);

        // Find the relevant channels and ask for the above CVE
        CVEAuditManager.populateCVEChannels();
        List<CVEAuditServer> results =
                CVEAuditManager.listSystemsByPatchStatus(user, cveName, EnumSet.allOf(PatchStatus.class));

        assertEquals(true, checkSystemRecordIsUnique(server1, results));
        assertEquals(true, checkSystemRecordIsUnique(server2, results));
    }

    /**
     * Find record for a given server in a list as returned by
     * listSystemsByPatchStatus().
     * @param server
     * @param results
     * @return result record for the given server
     */
    private CVEAuditSystem findSystemRecord(Server server, List<CVEAuditServer> results) {
        CVEAuditSystem ret = null;
        for (CVEAuditSystem result : results) {
            if (result.getId() == server.getId()) {
                ret = result;
                break;
            }
        }
        return ret;
    }

    /**
     * Check if the system is in the record more than one time (bsc#1101033)
     *
     * @param server
     * @param results
     * @return true if the system record is present one and only one time in the result
     */
    private boolean checkSystemRecordIsUnique(Server server, List<CVEAuditServer> results) {
        return results.stream().filter(r -> r.getId() == server.getId()).count() == 1;
    }

    /**
     * Looks for a server in a list of results from listSystemsByPatchStatus(),
     * asserts it has a certain patch status.
     * @param expectedServer the expected Server in the results
     * @param expectedPatchStatus the expected patch status
     * @param actualResults the actual results
     */
    private void assertSystemPatchStatus(Server expectedServer,
            PatchStatus expectedPatchStatus, List<CVEAuditServer> actualResults) {
        assertTrue(actualResults.size() >= 1);
        boolean found = false;
        for (CVEAuditServer result : actualResults) {
            if (result.getId() == expectedServer.getId()) {
                assertFalse(found);
                assertEquals(expectedServer.getName(), result.getName());
                assertEquals(expectedPatchStatus, result.getPatchStatus());
                found = true;
            }
        }
        assertTrue(found);
    }

    /**
     * Looks for a image in a list of results from listImagesByPatchStatus(),
     * asserts it has a certain patch status.
     * @param expectedImage the expected Image in the results
     * @param expectedPatchStatus the expected patch status
     * @param actualResults the actual results
     */
    private void assertImagePatchStatus(ImageInfo expectedImage,
            PatchStatus expectedPatchStatus, List<CVEAuditImage> actualResults) {
        assertTrue(actualResults.size() >= 1);
        boolean found = false;
        for (CVEAuditImage result : actualResults) {
            if (result.getId() == expectedImage.getId()) {
                assertFalse(found);
                assertEquals(expectedPatchStatus, result.getPatchStatus());
                found = true;
            }
        }
        assertTrue(found);
    }

    /**
     * Looks for a server in a list of results from listSystemsByPatchStatus(),
     * asserts it cannot be found.
     * @param expectedServer the expected Server in the results
     * @param expectedPatchStatus the expected patch status
     */
    private void assertSystemNotFound(Server expectedServer,
            List<CVEAuditServer> actualResults) {
        for (CVEAuditSystem result : actualResults) {
            assertTrue(result.getId() != expectedServer.getId());
        }
    }

    /**
     * Looks for a image in a list of results from listImagesByPatchStatus(),
     * asserts it cannot be found.
     * @param expectedImage the expected Image in the results
     * @param expectedPatchStatus the expected patch status
     */
    private void assertImageNotFound(ImageInfo expectedImage,
            List<CVEAuditImage> actualResults) {
        for (CVEAuditSystem result : actualResults) {
            assertTrue(result.getId() != expectedImage.getId());
        }
    }

    /**
     * This is needed because equals() on {@link ServerChannelIdPair} does not
     * consider the channel ranking, but only sid and cid.
     * @param relevantChannels
     * @param expected
     */
    private void assertContains(List<ServerChannelIdPair> relevantChannels,
            ServerChannelIdPair expected) {
        boolean contained = false;
        if (relevantChannels.contains(expected)) {
            int index = relevantChannels.indexOf(expected);
            ServerChannelIdPair actual = relevantChannels.get(index);
            contained = expected.getChannelRank() == actual.getChannelRank();
        }
        assertTrue(contained);
    }
}
