/**
 * Copyright (c) 2013 SUSE
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

import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper.
    createLaterTestPackage;
import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper.
    createTestChannel;
import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper.
    createTestChannelFamily;
import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper.
    createTestChannelProduct;
import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper.
    createTestClonedChannel;
import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper.
    createTestClonedErrata;
import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper.
    createTestCve;
import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper.
    createTestErrata;
import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper.
    createTestInstalledPackage;
import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper.
    createTestPackage;
import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper.
    createTestSUSEProduct;
import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper.
    createTestSUSEProductChannel;
import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper.
    createTestSUSEUpgradePath;
import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper.
    createTestServer;
import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper.
    createTestUser;
import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper.
    createTestVendorBaseChannel;
import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper.
    createTestVendorChildChannel;
import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper.
    getAllRelevantChannels;
import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper.
    getRelevantChannels;
import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper.
    installSUSEProductOnServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelProduct;
import com.redhat.rhn.domain.errata.Cve;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.SUSEProductDto;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.manager.audit.CVEAuditManager;
import com.redhat.rhn.manager.audit.CVEAuditSystem;
import com.redhat.rhn.manager.audit.ChannelIdNameLabelTriple;
import com.redhat.rhn.manager.audit.PatchStatus;
import com.redhat.rhn.manager.audit.ServerChannelIdPair;
import com.redhat.rhn.manager.audit.UnknownCVEIdentifierException;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

/**
 * Unit tests for {@link CVEAuditManager}.
 *
 * @version $Rev$
 */
public class CVEAuditManagerTest extends TestCase {

    /**
     * Test insertion and deletion for table suseCVEServerChannel:
     * {@link CVEAuditManager#deleteRelevantChannels()}
     * {@link CVEAuditManager#insertRelevantChannels(Set)}
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
        Set<ServerChannelIdPair> pairsExpected = new HashSet<ServerChannelIdPair>();
        ServerChannelIdPair pair1 =
                new ServerChannelIdPair(server1.getId(), channel1.getId(), 0);
        ServerChannelIdPair pair2 =
                new ServerChannelIdPair(server1.getId(), channel2.getId(), 1);
        ServerChannelIdPair pair3 =
                new ServerChannelIdPair(server2.getId(), channel3.getId(), 2);
        pairsExpected.add(pair1);
        pairsExpected.add(pair2);
        pairsExpected.add(pair3);
        CVEAuditManager.insertRelevantChannels(pairsExpected);

        // Read and check if table content is as expected
        List<ServerChannelIdPair> pairsActual = getAllRelevantChannels();
        assertEquals(3, pairsActual.size());
        assertContains(pairsActual, pair1);
        assertContains(pairsActual, pair2);
        assertContains(pairsActual, pair3);

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
        createTestSUSEProductChannel(baseChannel, product);
        createTestSUSEProductChannel(childChannel1, product);
        createTestSUSEProductChannel(childChannel2, product);
        createTestSUSEProductChannel(childChannel3, product);

        // Test the conversion between SUSE product and channel products
        List<Long> channelProductIDs = CVEAuditManager.convertProductId(product.getId());
        assertEquals(2, channelProductIDs.size());
        assertTrue(channelProductIDs.contains(channelProduct1.getId()));
        assertTrue(channelProductIDs.contains(channelProduct2.getId()));

        // Call findSUSEProductChannels() and verify the results
        List<Channel> channels = CVEAuditManager.findSUSEProductChannels(product.getId());
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
     * {@link CVEAuditManager#setPatchStatus(CVEAuditSystem, boolean,
     * boolean, boolean)}
     */
    public void testSetPatchStatus() {
        CVEAuditSystem system = new CVEAuditSystem(0L);

        CVEAuditManager.setPatchStatus(system, true, true, true);
        assertEquals(PatchStatus.PATCHED, system.getPatchStatus());

        CVEAuditManager.setPatchStatus(system, true, false, true);
        assertEquals(PatchStatus.PATCHED, system.getPatchStatus());

        CVEAuditManager.setPatchStatus(system, false, true, true);
        assertEquals(PatchStatus.AFFECTED_PATCH_APPLICABLE, system.getPatchStatus());

        CVEAuditManager.setPatchStatus(system, false, false, true);
        assertEquals(PatchStatus.AFFECTED_PATCH_INAPPLICABLE, system.getPatchStatus());

        CVEAuditManager.setPatchStatus(system, false, false, false);
        assertEquals(PatchStatus.NOT_AFFECTED, system.getPatchStatus());
    }

    /**
     * Test the correct population of the table suseCVEServerChannel:
     * {@link CVEAuditManager#populateCVEServerChannels()}
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
        createTestSUSEProductChannel(baseChannel, product);
        createTestSUSEProductChannel(childChannel1, product);
        createTestSUSEProductChannel(childChannel2, product);

        // Setup a next SP product for verifying SP migrations
        SUSEProduct productNextSP = createTestSUSEProduct(channelFamily);
        createTestSUSEUpgradePath(product, productNextSP);
        // Create channels
        ChannelProduct channelProductNextSP = createTestChannelProduct();
        Channel baseChannelNextSP =
                createTestVendorBaseChannel(channelFamily, channelProductNextSP);
        Channel childChannelNextSP =
                createTestVendorChildChannel(baseChannelNextSP, channelProductNextSP);
        // Assign channels to SP product
        createTestSUSEProductChannel(baseChannelNextSP, productNextSP);
        createTestSUSEProductChannel(childChannelNextSP, productNextSP);

        // Setup a previous SP of the installed product
        SUSEProduct productPrevSP = createTestSUSEProduct(channelFamily);
        createTestSUSEUpgradePath(productPrevSP, product);
        // Create channels
        ChannelProduct channelProductPrevSP = createTestChannelProduct();
        Channel baseChannelPrevSP =
                createTestVendorBaseChannel(channelFamily, channelProductPrevSP);
        Channel childChannelPrevSP =
                createTestVendorChildChannel(baseChannelPrevSP, channelProductPrevSP);
        // Assign channels to SP product
        createTestSUSEProductChannel(baseChannelPrevSP, productPrevSP);
        createTestSUSEProductChannel(childChannelPrevSP, productPrevSP);

        // Create server and install product
        User user = createTestUser();
        List<Channel> subscribedChannels =
                new ArrayList<Channel>(Arrays.asList(baseChannel, childChannel1));
        Server server = createTestServer(user, subscribedChannels);
        installSUSEProductOnServer(product, server);

        // Populate the database
        CVEAuditManager.populateCVEServerChannels();

        // Get channels relevant for the above system
        List<ServerChannelIdPair> relevantChannels = getRelevantChannels(server.getId());
        assertEquals(7, relevantChannels.size());

        // Check assigned channels
        assertContains(relevantChannels, new ServerChannelIdPair(server.getId(),
                baseChannel.getId(), 0));
        assertContains(relevantChannels, new ServerChannelIdPair(server.getId(),
                childChannel1.getId(), 0));
        // Check unassigned product channels
        assertContains(relevantChannels, new ServerChannelIdPair(server.getId(),
                childChannel2.getId(), 1));
        // Check channels relevant for the next SP
        assertContains(relevantChannels, new ServerChannelIdPair(server.getId(),
                baseChannelNextSP.getId(), 2));
        assertContains(relevantChannels, new ServerChannelIdPair(server.getId(),
                childChannelNextSP.getId(), 2));
        // Check channels relevant for the previous SP
        assertContains(relevantChannels, new ServerChannelIdPair(server.getId(),
                baseChannelPrevSP.getId(), 3));
        assertContains(relevantChannels, new ServerChannelIdPair(server.getId(),
                childChannelPrevSP.getId(), 3));
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
        List<Channel> channels =
                CVEAuditManager.findProductChannels(productIDs, baseChannel.getId());
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
        CVEAuditManager.populateCVEServerChannels();

        // No filtering
        EnumSet<PatchStatus> filter = EnumSet.allOf(PatchStatus.class);
        List<CVEAuditSystem> results =
                CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
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
        CVEAuditManager.populateCVEServerChannels();

        // No filtering
        EnumSet<PatchStatus> filter = EnumSet.allOf(PatchStatus.class);
        List<CVEAuditSystem> results =
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
        CVEAuditManager.populateCVEServerChannels();

        // No filtering
        EnumSet<PatchStatus> filter = EnumSet.allOf(PatchStatus.class);
        List<CVEAuditSystem> results =
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
        createTestSUSEProductChannel(baseChannel, product);
        createTestSUSEProductChannel(childChannel, product);

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
        CVEAuditManager.populateCVEServerChannels();

        // No filtering
        EnumSet<PatchStatus> filter = EnumSet.allOf(PatchStatus.class);
        List<CVEAuditSystem> results =
                CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
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
        List<CVEAuditSystem> results =
                CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
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
        CVEAuditManager.populateCVEServerChannels();
        List<CVEAuditSystem> results =
                CVEAuditManager.listSystemsByPatchStatus(user, cveName,
                        EnumSet.allOf(PatchStatus.class));

        // Verify the order of returned channels
        CVEAuditSystem result = findSystemRecord(server, results);
        assertEquals(PatchStatus.AFFECTED_PATCH_APPLICABLE, result.getPatchStatus());
        Iterator<ChannelIdNameLabelTriple> it = result.getChannels().iterator();
        assertEquals((Long) channelClone.getId(), (Long) it.next().getId());
        assertEquals((Long) channel.getId(), (Long) it.next().getId());
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
        CVEAuditManager.populateCVEServerChannels();

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
        CVEAuditManager.populateCVEServerChannels();

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
        CVEAuditManager.populateCVEServerChannels();

        // Create a further errata on a different package set
        Errata irrelevantErrata = createTestErrata(user, cves);
        createTestPackage(user, channel, "noarch");
        createLaterTestPackage(user, irrelevantErrata, channel, unpatched);

        // No filtering
        EnumSet<PatchStatus> filter = EnumSet.allOf(PatchStatus.class);
        List<CVEAuditSystem> results =
                CVEAuditManager.listSystemsByPatchStatus(user, cveName, filter);
        assertSystemPatchStatus(server, PatchStatus.PATCHED, results);
    }


    /**
     * Find record for a given server in a list as returned by
     * listSystemsByPatchStatus().
     * @param server
     * @param results
     * @return result record for the given server
     */
    private CVEAuditSystem findSystemRecord(Server server, List<CVEAuditSystem> results) {
        CVEAuditSystem ret = null;
        for (CVEAuditSystem result : results) {
            if (result.getSystemID() == server.getId()) {
                ret = result;
                break;
            }
        }
        return ret;
    }

    /**
     * Looks for a server in a list of results from listSystemsByPatchStatus(),
     * asserts it has a certain patch status.
     * @param expectedServer the expected Server in the results
     * @param expectedPatchStatus the expected patch status
     * @param actualResults the actual results
     */
    private void assertSystemPatchStatus(Server expectedServer,
            PatchStatus expectedPatchStatus, List<CVEAuditSystem> actualResults) {
        assertTrue(actualResults.size() >= 1);
        boolean found = false;
        for (CVEAuditSystem result : actualResults) {
            if (result.getSystemID() == expectedServer.getId()) {
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
            List<CVEAuditSystem> actualResults) {
        for (CVEAuditSystem result : actualResults) {
            assertTrue(result.getSystemID() != expectedServer.getId());
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
