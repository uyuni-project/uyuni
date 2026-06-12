/*
 * Copyright (c) 2026 SUSE LLC
 * Copyright (c) 2009--2013 Red Hat, Inc.
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
package com.redhat.rhn.domain.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.errata.ErrataFactoryTest;
import com.redhat.rhn.domain.errata.ErrataTestBuilder;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.PackageTest;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.EntityExistsException;

/**
 * ChannelTest
 */
@SuppressWarnings("deprecation")
public class ChannelTest extends BaseTestCaseWithUser {

    private static Logger log = LogManager.getLogger(ChannelTest.class);


    @Test
    public void testRemovePackage() throws Exception {
        Channel c = ChannelFactoryTest.createTestChannel(user);
        Package p = PackageTest.createTestPackage(user.getOrg());
        ChannelTestUtility.testAddPackage(c, p);
        ChannelFactory.save(c);
        c.removePackage(p);
        assertEquals(0, c.getPackageCount());
        assertTrue(c.getPackages().isEmpty());

    }

    @Test
    public void testChannel() throws Exception {
        Channel c = ChannelFactoryTest.createTestChannel(user);
        //add an errata
        Errata e = ErrataFactoryTest.createTestErrata(user.getId());
        c.addErrata(e);
        assertEquals(c.getErratas().size(), 1);
        ChannelFactory.save(c);

        log.debug("Looking up id [{}]", c.getId());
        Channel c2 = ChannelFactory.lookupById(c.getId());
        log.debug("Finished lookup");
        assertEquals(c2.getErratas().size(), 1);

        assertEquals(c.getLabel(), c2.getLabel());
        assertNotNull(c.getChannelArch());

        Channel c3 = ChannelFactoryTest.createTestChannel(user);

        c.setParentChannel(c3);
        assertEquals(c.getParentChannel().getId(), c3.getId());

        //Test isGloballySubscribable
        assertTrue(c.isGloballySubscribable(c.getOrg()));
        c.setGloballySubscribable(false, c.getOrg());
        assertFalse(c.isGloballySubscribable(c.getOrg()));
        c.setGloballySubscribable(true, c.getOrg());
        assertTrue(c.isGloballySubscribable(c.getOrg()));


    }

    @Test
    public void testChannelGpgCheck() throws Exception {
        Channel c = ChannelFactoryTest.createTestChannel(user, true);
        ChannelFactory.save(c);
        assertTrue(c.isGPGCheck());
        TestUtils.flushAndEvict(c);
        Channel c1 = ChannelFactory.lookupById(c.getId());
        assertTrue(c1.isGPGCheck());

        Channel c2 = ChannelFactoryTest.createTestChannel(user, false);
        ChannelFactory.save(c2);
        assertFalse(c2.isGPGCheck());
        TestUtils.flushAndEvict(c2);
        Channel c3 = ChannelFactory.lookupById(c2.getId());
        assertFalse(c3.isGPGCheck());
    }

    @Test
    public void testEquals() throws Exception {
        Channel c1 = ChannelFactoryTest.createTestChannel(user);
        Channel c2 = ChannelFactoryTest.createTestChannel(user);
        assertNotEquals(c1, c2);
        Channel c3 = ChannelFactory.lookupById(c1.getId());
        Set<Channel> testSet = new HashSet<>();
        testSet.add(c1);
        testSet.add(c2);
        testSet.add(c3);
        assertEquals(2, testSet.size());
    }

    @Test
    public void testDistChannelMap() throws Exception {
        Channel c = ChannelFactoryTest.createTestChannel(user);
        ChannelTestUtils.addDistMapToChannel(c);
        c = (Channel) TestUtils.reload(c);
        assertNotNull(c.getDistChannelMaps());
        assertFalse(c.getDistChannelMaps().isEmpty());
    }

    @Test
    public void testIsProxy() throws Exception {
        Channel c = ChannelFactoryTest.createTestChannel(user);
        ChannelFamily cfam = ChannelFamilyFactoryTest.createTestChannelFamily(user, false);
        cfam.setLabel(ChannelFamilyFactory.PROXY_CHANNEL_FAMILY_LABEL);

        c.setChannelFamily(cfam);

        c = TestUtils.saveAndFlush(c);

        Channel c2 = ChannelFactory.lookupById(c.getId());
        assertTrue(c2.isProxy());
    }

    @Test
    public void testIsSub() throws Exception {
        Channel c = ChannelFactoryTest.createTestChannel(user);
        Server s = ServerTestUtils.createTestSystem(user);
        assertTrue(c.isSubscribable(c.getOrg(), s));
    }

    @Test
    public void testDeleteChannel() throws Exception {
        Channel c = ChannelFactoryTest.createTestChannel(user);
        Long id = c.getId();
        assertNotNull(c);
        ChannelFactory.save(c);
        assertNotNull(ChannelFactory.lookupById(id));
        ChannelFactory.remove(c);
        TestUtils.flushAndEvict(c);
        assertNull(ChannelFactory.lookupById(id));
    }

    @Test
    public void testIsBaseChannel() {
        Channel c = new Channel();
        Channel p = new Channel();
        c.setParentChannel(p);
        assertFalse(c.isBaseChannel());
        c.setParentChannel(null);
        assertTrue(c.isBaseChannel());
    }

    @Test
    void testAddAndEditAndRemoveErrata() {
        // Create a channel and an errata
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        Errata errata = ErrataFactoryTest.createTestErrata(user.getId());

        // By default, neither channel has erratas nor erratas have any channel
        assertTrue(channel.getErratas().isEmpty());
        assertTrue(errata.getChannels().isEmpty());

        // Add the errata to the channel
        channel.addErrata(errata);
        ChannelFactory.save(channel);

        Long channelId = channel.getId();
        Long errataId = errata.getId();

        // Check persistence
        TestUtils.flushAndClearSession();

        // Reload from database
        Channel reloadedChannel = ChannelFactory.lookupById(channelId);
        Errata reloadedErrata = ErrataFactory.lookupById(errataId);

        // Assert channel has errata and vice versa
        assertEquals(1, reloadedChannel.getErratas().size());
        assertTrue(reloadedChannel.getErratas().contains(reloadedErrata));
        assertEquals(1, reloadedErrata.getChannels().size());
        assertTrue(reloadedErrata.getChannels().contains(reloadedChannel));


        // Remove the errata
        reloadedChannel.removeErrata(errata);
        TestUtils.flushAndClearSession();

        // Reload from database and verify they are no longer related
        Channel finalChannel = ChannelFactory.lookupById(channelId);
        Errata finalErrata = ErrataFactory.lookupById(errataId);
        assertTrue(finalChannel.getErratas().isEmpty());
        assertTrue(finalErrata.getChannels().isEmpty());
    }

    @Test
    void testUpdateErrataAndPackages() {
        // Create a channel and an errata
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        String originalAdvisory = "original";
        String updatedAdvisory = "updated";
        String updatedDescription = TestUtils.randomString();

        Errata errata = new ErrataTestBuilder().orgId(user.getOrg().getId()).advisory(originalAdvisory).buildAndSave();
        Package pkg = PackageTest.createTestPackage(user.getOrg());

        // Add the errata to the channel
        channel.addErrata(errata);
        channel.addPackage(pkg);
        ChannelFactory.save(channel);

        Long channelId = channel.getId();
        Long errataId = errata.getId();
        Long pkgId = pkg.getId();

        // Check persistence
        TestUtils.flushAndClearSession();

        // Reload from database
        Channel reloadedChannel = ChannelFactory.lookupById(channelId);
        Errata reloadedErrata = ErrataFactory.lookupById(errataId);
        Package reloadedPackage = PackageFactory.lookupByIdAndOrg(pkgId, user.getOrg());

        // Update channel and package and save the channel
        reloadedErrata.setAdvisoryName(updatedAdvisory);
        reloadedPackage.setDescription(updatedDescription);
        ChannelFactory.save(reloadedChannel);
        TestUtils.flushAndClearSession();

        Errata updatedErrata = ErrataFactory.lookupById(errataId);
        Package updatedPackage = PackageFactory.lookupByIdAndOrg(pkgId, user.getOrg());
        assertEquals(updatedAdvisory, updatedErrata.getAdvisoryName());
        assertEquals(updatedDescription, updatedPackage.getDescription());
    }

    @Test
    void testAddAndRemovePackage() throws Exception {
        // Create a channel and a package
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        Package pkg = PackageTest.createTestPackage(user.getOrg());

        // By default, neither channel has packages nor packages have any channel
        assertTrue(channel.getPackages().isEmpty());
        assertTrue(pkg.getChannels().isEmpty());

        // Add the package to the channel
        channel.addPackage(pkg);

        // Assert channel has package
        assertEquals(1, channel.getPackages().size());
        assertEquals(pkg, channel.getPackages().iterator().next());

        // Assert the package also contains the channel
        assertEquals(1, pkg.getChannels().size());
        assertEquals(channel, pkg.getChannels().iterator().next());

        // Remove the package
        channel.removePackage(pkg);

        assertTrue(channel.getPackages().isEmpty());
        assertTrue(pkg.getChannels().isEmpty());
    }

    @Test
    public void testAddPackage() throws Exception {
        Channel c = ChannelFactoryTest.createTestChannel(user);
        Package p = PackageTest.createTestPackage(user.getOrg());
        assertNotNull(c);
        assertEquals("channel-x86_64", c.getChannelArch().getLabel());
        assertNotNull(p);
        assertEquals("noarch", p.getPackageArch().getLabel());

        try {
            ChannelTestUtility.testAddPackage(c, p);
        }
        catch (Exception e) {
            fail("noarch should be acceptible in an x86_64 channel");
        }


        try {
            PackageArch pa = PackageFactory.lookupPackageArchByLabel("aarch64");
            assertNotNull(pa);
            p.setPackageArch(pa);
            ChannelTestUtility.testAddPackage(c, p);
            fail("aarch64 is not acceptible in an x86_64 channel");
        }
        catch (Exception e) {
            // expected.
        }

    }

    @Test
    public void testContentSource() throws Exception {
        Channel c = ChannelFactoryTest.createTestChannel(user);
        ContentSource cs = new ContentSource();
        cs.setLabel("repo_label-" + c.getLabel());
        cs.setSourceUrl("fake url");
        List<ContentSourceType> cst = ChannelFactory.listContentSourceTypes();
        cs.setType(cst.get(0));
        cs.setOrg(user.getOrg());
        cs = TestUtils.saveAndReload(cs);
        c.getSources().add(cs);
        c = TestUtils.saveAndReload(c);
        assertNotEmpty(c.getSources());
    }

    @Test
    public void testIsTypeRpm() throws Exception {
        Channel c = ChannelFactoryTest.createTestChannel(user);
        ChannelArch arch = ChannelFactory.lookupArchByLabel("channel-ia64");
        c.setChannelArch(arch);

        assertTrue(c.isTypeRpm());
    }

    @Test
    public void testIsTypeDeb() throws Exception {
        Channel c = ChannelFactoryTest.createTestChannel(user);
        ChannelArch arch = ChannelFactory.lookupArchByLabel("channel-ia64-deb");
        c.setChannelArch(arch);

        assertTrue(c.isTypeDeb());
    }

    @Test
    public void testIsModular() throws Exception {
        Channel c = ChannelFactoryTest.createTestChannel(user);
        assertNull(c.getModules());
        assertFalse(c.isModular());

        c.setModules(new Modules());
        assertNotNull(c.getModules());
        assertTrue(c.isModular());
    }

    // ERRATA ONLY SPECIFIC

    @Test
    void testAddErratas() {
        int numErratas = 3;
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        List<Errata> errataList = new ArrayList<>();
        for (int i = 0; i < numErratas; i++) {
            errataList.add(ErrataFactoryTest.createTestErrata(user.getId()));
        }

        // Initially empty
        assertTrue(channel.getErratas().isEmpty());

        // Add multiple erratas at once
        channel.addErratas(errataList);

        // Verify all erratas were added to channel
        assertEquals(errataList.size(), channel.getErratas().size());
        errataList.forEach(errata -> assertTrue(channel.getErratas().contains(errata)));

        // Verify bidirectional (ie, all erratas have the channel)
        errataList.forEach(errata -> {
            assertEquals(1, errata.getChannels().size());
            assertTrue(errata.getChannels().contains(channel));
        });

        // Check persistence
        ChannelFactory.save(channel);

        // Gather ids for later lookups
        Long channelId = channel.getId();
        List<Long> errataIdList = errataList.stream().map(Errata::getId).toList();

        // Flush and clear session to detach entities
        TestUtils.flushAndClearSession();

        // Reload from database
        Channel reloadedChannel = ChannelFactory.lookupById(channelId);
        assertNotNull(reloadedChannel);

        // Verify relationship persisted
        assertEquals(errataList.size(), reloadedChannel.getErratas().size());
        for (Long errataId : errataIdList) {
            Errata errata = ErrataFactory.lookupById(errataId);
            assertEquals(1, errata.getChannels().size());
            assertTrue(errata.getChannels().contains(reloadedChannel));
        }
    }

    @Test
    void testRemoveErratas() {
        // Create channel with erratas
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        Errata errata1 = ErrataFactoryTest.createTestErrata(user.getId());
        Errata errata2 = ErrataFactoryTest.createTestErrata(user.getId());

        // Add erratas
        channel.addErrata(errata1);
        channel.addErrata(errata2);
        ChannelFactory.save(channel);

        // Gather ids for later lookups
        Long channelId = channel.getId();
        Long errata1Id = errata1.getId();
        Long errata2Id = errata2.getId();

        // Flush and clear session to detach entities
        TestUtils.flushAndClearSession();

        // Reload and remove one errata
        Channel reloadedChannel = ChannelFactory.lookupById(channelId);
        Errata reloadedErrata1 = ErrataFactory.lookupById(errata1Id);

        reloadedChannel.removeErrata(reloadedErrata1);
        assertEquals(1, reloadedChannel.getErratas().size());
        ChannelFactory.save(reloadedChannel);

        TestUtils.flushAndClearSession();

        // Reload and verify only errata2 remains
        Channel finalChannel = ChannelFactory.lookupById(channelId);
        assertEquals(1, finalChannel.getErratas().size());

        Errata finalErrata1 = ErrataFactory.lookupById(errata1Id);
        Errata finalErrata2 = ErrataFactory.lookupById(errata2Id);

        assertFalse(finalErrata1.getChannels().contains(finalChannel));
        assertTrue(finalErrata2.getChannels().contains(finalChannel));
    }

    @Test
    void testClearErratas() {
        // Create channel with erratas
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        Errata errata1 = ErrataFactoryTest.createTestErrata(user.getId());
        Errata errata2 = ErrataFactoryTest.createTestErrata(user.getId());

        // Add erratas
        channel.addErrata(errata1);
        channel.addErrata(errata2);
        ChannelFactory.save(channel);

        // Gather ids for later lookups
        Long channelId = channel.getId();
        Long errata1Id = errata1.getId();
        Long errata2Id = errata2.getId();

        // Flush and clear session to detach entities
        TestUtils.flushAndClearSession();

        // Reload and clear all erratas
        Channel reloadedChannel = ChannelFactory.lookupById(channelId);
        reloadedChannel.clearErratas();
        assertTrue(reloadedChannel.getErratas().isEmpty());
        ChannelFactory.save(reloadedChannel);

        TestUtils.flushAndClearSession();

        // Reload and verify channel has no erratas
        Channel finalChannel = ChannelFactory.lookupById(channelId);
        assertTrue(finalChannel.getErratas().isEmpty());

        // Verify erratas no longer have the channel
        Errata finalErrata1 = ErrataFactory.lookupById(errata1Id);
        Errata finalErrata2 = ErrataFactory.lookupById(errata2Id);

        assertFalse(finalErrata1.getChannels().contains(finalChannel));
        assertFalse(finalErrata2.getChannels().contains(finalChannel));
    }

    @Test
    void testAddDuplicateErrata() {
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        Errata errata = ErrataFactoryTest.createTestErrata(user.getId());

        // Add errata twice
        channel.addErrata(errata);
        channel.addErrata(errata);

        // Should still only have one
        assertEquals(1, channel.getErratas().size());
        assertEquals(1, errata.getChannels().size());
    }

    @Test
    void testRemoveNonAssociatedErrata() {
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        Errata errata = ErrataFactoryTest.createTestErrata(user.getId());
        ChannelFactory.save(channel);

        // Flush and clear session to detach entities
        TestUtils.flushAndClearSession();

        // Reload and clear all erratas
        Channel reloadedChannel = ChannelFactory.lookupById(channel.getId());

        // Add errata twice
        channel.removeErrata(errata);

        // Should still only have one
        assertTrue(reloadedChannel.getErratas().isEmpty());
        ChannelFactory.save(reloadedChannel);
        TestUtils.flushAndClearSession();

        // Reload
        Channel finalChannel = ChannelFactory.lookupById(channel.getId());
        assertTrue(finalChannel.getErratas().isEmpty());
    }

    // PACKAGES ONLY SPECIFIC

    @Test
    void testAddPackages() {
        int numPackages = 3;
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        List<Package> packageList = new ArrayList<>();
        for (int i = 0; i < numPackages; i++) {
            packageList.add(PackageTest.createTestPackage(user.getOrg()));
        }

        // Initially empty
        assertTrue(channel.getPackages().isEmpty());

        // Add multiple packages at once
        channel.addPackages(packageList);

        // Verify all packages were added to channel
        assertEquals(packageList.size(), channel.getPackages().size());
        packageList.forEach(pkg -> assertTrue(channel.getPackages().contains(pkg)));

        // Verify bidirectional (ie, all packages have the channel)
        packageList.forEach(pkg -> {
            assertEquals(1, pkg.getChannels().size());
            assertTrue(pkg.getChannels().contains(channel));
        });

        // Check persistence
        ChannelFactory.save(channel);

        // Gather ids for later lookups
        Long channelId = channel.getId();
        List<Long> packageIdList = packageList.stream().map(Package::getId).toList();

        // Flush and clear session to detach entities
        TestUtils.flushAndClearSession();

        // Reload from database
        Channel reloadedChannel = ChannelFactory.lookupById(channelId);
        assertNotNull(reloadedChannel);

        // Verify relationship persisted
        assertEquals(packageList.size(), reloadedChannel.getPackages().size());
        for (Long pkgId : packageIdList) {
            Package pkg = PackageFactory.lookupByIdAndOrg(pkgId, user.getOrg());
            assertEquals(1, pkg.getChannels().size());
            assertTrue(pkg.getChannels().contains(reloadedChannel));
        }
    }

    @Test
    void testRemovePackages() {
        // Create channel with packages
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        Package package1 = PackageTest.createTestPackage(user.getOrg());
        Package package2 = PackageTest.createTestPackage(user.getOrg());

        // Add packages
        channel.addPackage(package1);
        channel.addPackage(package2);
        ChannelFactory.save(channel);

        // Gather ids for later lookups
        Long channelId = channel.getId();
        Long package1Id = package1.getId();
        Long package2Id = package2.getId();

        // Flush and clear session to detach entities
        TestUtils.flushAndClearSession();

        // Reload and remove one package
        Channel reloadedChannel = ChannelFactory.lookupById(channelId);
        Package reloadedPackage1 = PackageFactory.lookupByIdAndOrg(package1Id, user.getOrg());

        reloadedChannel.removePackage(reloadedPackage1);
        assertEquals(1, reloadedChannel.getPackages().size());
        ChannelFactory.save(reloadedChannel);

        TestUtils.flushAndClearSession();

        // Reload and verify only package2 remains
        Channel finalChannel = ChannelFactory.lookupById(channelId);
        assertEquals(1, finalChannel.getPackages().size());

        Package finalPackage1 = PackageFactory.lookupByIdAndOrg(package1Id, user.getOrg());
        Package finalPackage2 = PackageFactory.lookupByIdAndOrg(package2Id, user.getOrg());

        assertFalse(finalPackage1.getChannels().contains(finalChannel));
        assertTrue(finalPackage2.getChannels().contains(finalChannel));
    }

    @Test
    void testAddDuplicatePackage() {
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        Package pkg = PackageTest.createTestPackage(user.getOrg());

        // Add package twice
        channel.addPackage(pkg);
        channel.addPackage(pkg);

        // Should still only have one (Set behavior)
        assertEquals(1, channel.getPackages().size());
        assertEquals(1, pkg.getChannels().size());
    }

    @Test
    void testRemoveNonAssociatedPackage() {
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        Package pkg = PackageTest.createTestPackage(user.getOrg());
        ChannelFactory.save(channel);

        // Flush and clear session to detach entities
        TestUtils.flushAndClearSession();

        // Reload and clear all packages
        Channel reloadedChannel = ChannelFactory.lookupById(channel.getId());

        // Add package twice
        channel.removePackage(pkg);

        // Should still only have one
        assertTrue(reloadedChannel.getPackages().isEmpty());
        ChannelFactory.save(reloadedChannel);
        TestUtils.flushAndClearSession();

        // Reload
        Channel finalChannel = ChannelFactory.lookupById(channel.getId());
        assertTrue(finalChannel.getPackages().isEmpty());
    }


    // ERRATA + PACKAGES SPECIFIC

    /**
     * Test complex overlapping relationships: channel with erratas and packages,
     * where pkg2 is shared between both erratas.
     * Verify all relationships are persisted correctly in the database and can be reloaded.
     */
    @Test
    void testChannelWithRelatedErratasAndPackages() {
        // Create channel with both erratas and packages
        Channel channel = ChannelFactoryTest.createTestChannel(user);

        // Create packages
        Package pkg1 = PackageTest.createTestPackage(user.getOrg());
        Package pkg2 = PackageTest.createTestPackage(user.getOrg());
        Package pkg3 = PackageTest.createTestPackage(user.getOrg());

        // Create erratas
        Errata errata1 = new ErrataTestBuilder().orgId(user.getOrg().getId()).buildAndSave();
        Errata errata2 = new ErrataTestBuilder().orgId(user.getOrg().getId()).buildAndSave();

        // Build the relationship graph
        // Add packages to erratas
        errata1.addPackage(pkg1);
        errata1.addPackage(pkg2);
        errata2.addPackage(pkg2);
        errata2.addPackage(pkg3);

        // Add both erratas and packages to channel
        channel.addErrata(errata1);
        channel.addErrata(errata2);
        channel.addPackage(pkg1);
        channel.addPackage(pkg2);
        channel.addPackage(pkg3);

        // Save
        ChannelFactory.save(channel);
        ErrataFactory.save(errata1);
        ErrataFactory.save(errata2);

        Long channelId = channel.getId();
        Long errata1Id = errata1.getId();
        Long errata2Id = errata2.getId();
        Long pkg1Id = pkg1.getId();
        Long pkg2Id = pkg2.getId();
        Long pkg3Id = pkg3.getId();

        // Clear session
        TestUtils.flushAndClearSession();

        // Reload everything
        Channel reloadedChannel = ChannelFactory.lookupById(channelId);
        Errata reloadedErrata1 = ErrataFactory.lookupById(errata1Id);
        Errata reloadedErrata2 = ErrataFactory.lookupById(errata2Id);
        Package reloadedPkg1 = PackageFactory.lookupByIdAndOrg(pkg1Id, user.getOrg());
        Package reloadedPkg2 = PackageFactory.lookupByIdAndOrg(pkg2Id, user.getOrg());
        Package reloadedPkg3 = PackageFactory.lookupByIdAndOrg(pkg3Id, user.getOrg());

        // Verify channel-errata relationships persisted
        assertEquals(2, reloadedChannel.getErratas().size());
        assertTrue(reloadedErrata1.getChannels().contains(reloadedChannel));
        assertTrue(reloadedErrata2.getChannels().contains(reloadedChannel));

        // Verify channel-package relationships persisted
        assertEquals(3, reloadedChannel.getPackages().size());
        assertTrue(reloadedPkg1.getChannels().contains(reloadedChannel));
        assertTrue(reloadedPkg2.getChannels().contains(reloadedChannel));
        assertTrue(reloadedPkg3.getChannels().contains(reloadedChannel));

        // Verify errata-package relationships persisted
        assertEquals(2, reloadedErrata1.getPackages().size());
        assertTrue(reloadedErrata1.getPackages().contains(reloadedPkg1));
        assertTrue(reloadedErrata1.getPackages().contains(reloadedPkg2));

        assertEquals(2, reloadedErrata2.getPackages().size());
        assertTrue(reloadedErrata2.getPackages().contains(reloadedPkg2));
        assertTrue(reloadedErrata2.getPackages().contains(reloadedPkg3));

        // Verify package-errata relationships persisted
        assertEquals(1, reloadedPkg1.getErrata().size());
        assertTrue(reloadedPkg1.getErrata().contains(reloadedErrata1));

        assertEquals(2, reloadedPkg2.getErrata().size()); // Part of both erratas
        assertTrue(reloadedPkg2.getErrata().contains(reloadedErrata1));
        assertTrue(reloadedPkg2.getErrata().contains(reloadedErrata2));

        assertEquals(1, reloadedPkg3.getErrata().size());
        assertTrue(reloadedPkg3.getErrata().contains(reloadedErrata2));
    }

    /**
     * Test that removeErrata() only removes the channel-errata relationship,
     * leaving channel's packages, errata's packages, and all other relationships
     * intact. Verified against database persistence.
     *
     * @throws Exception if test fails
     */
    @Test
    void testRemoveErrataAffectsOnlyErrataChannelRelationship() throws Exception {
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        Package pkg1 = PackageTest.createTestPackage(user.getOrg());
        Package pkg2 = PackageTest.createTestPackage(user.getOrg());
        Errata errata = new ErrataTestBuilder().orgId(user.getOrg().getId()).buildAndSave();

        // Establish all relationships
        errata.addPackage(pkg1);
        errata.addPackage(pkg2);
        channel.addErrata(errata);
        channel.addPackage(pkg1);
        channel.addPackage(pkg2);

        // Save and reload to verify initial state from DB
        ChannelFactory.save(channel);
        ErrataFactory.save(errata);

        Long channelId = channel.getId();
        Long errataId = errata.getId();
        Long pkg1Id = pkg1.getId();
        Long pkg2Id = pkg2.getId();

        TestUtils.flushAndClearSession();

        // Reload and verify initial persisted state
        Channel reloadedChannel = ChannelFactory.lookupById(channelId);
        Errata reloadedErrata = ErrataFactory.lookupById(errataId);

        assertEquals(1, reloadedChannel.getErratas().size());
        assertEquals(2, reloadedChannel.getPackages().size());
        assertEquals(2, reloadedErrata.getPackages().size());

        // Remove errata from channel
        reloadedChannel.removeErrata(reloadedErrata);
        ChannelFactory.save(reloadedChannel);

        TestUtils.flushAndClearSession();

        // Reload and verify persisted state after removal
        Channel finalChannel = ChannelFactory.lookupById(channelId);
        Errata finalErrata = ErrataFactory.lookupById(errataId);
        Package finalPkg1 = PackageFactory.lookupByIdAndOrg(pkg1Id, user.getOrg());
        Package finalPkg2 = PackageFactory.lookupByIdAndOrg(pkg2Id, user.getOrg());

        // Verify channel-errata relationship removed
        assertTrue(finalChannel.getErratas().isEmpty());
        assertFalse(finalErrata.getChannels().contains(finalChannel));

        // Verify channel still has packages
        assertEquals(2, finalChannel.getPackages().size());
        assertTrue(finalChannel.getPackages().contains(finalPkg1));
        assertTrue(finalChannel.getPackages().contains(finalPkg2));

        // Verify errata still has packages
        assertEquals(2, finalErrata.getPackages().size());
        assertTrue(finalErrata.getPackages().contains(finalPkg1));
        assertTrue(finalErrata.getPackages().contains(finalPkg2));

        // Verify packages still have channel
        assertTrue(finalPkg1.getChannels().contains(finalChannel));
        assertTrue(finalPkg2.getChannels().contains(finalChannel));

        // Verify packages still have errata
        assertTrue(finalPkg1.getErrata().contains(finalErrata));
        assertTrue(finalPkg2.getErrata().contains(finalErrata));
    }

    /**
     * Test that removePackage() only removes the package-channel relationship,
     * leaving channel's erratas, errata's packages, and all other relationships
     * intact. Verified against database persistence.
     *
     * @throws Exception if test fails
     */
    @Test
    void testRemovePackageAffectsOnlyPackageChannelRelationship() throws Exception {
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        Package pkg1 = PackageTest.createTestPackage(user.getOrg());
        Package pkg2 = PackageTest.createTestPackage(user.getOrg());
        Errata errata = new ErrataTestBuilder().orgId(user.getOrg().getId()).buildAndSave();

        // Establish all relationships
        errata.addPackage(pkg1);
        errata.addPackage(pkg2);
        channel.addErrata(errata);
        channel.addPackage(pkg1);
        channel.addPackage(pkg2);

        // Save and reload to verify initial state from DB
        ChannelFactory.save(channel);
        ErrataFactory.save(errata);

        Long channelId = channel.getId();
        Long errataId = errata.getId();
        Long pkg1Id = pkg1.getId();
        Long pkg2Id = pkg2.getId();

        TestUtils.flushAndClearSession();

        // Reload and verify initial persisted state
        Channel reloadedChannel = ChannelFactory.lookupById(channelId);
        Errata reloadedErrata = ErrataFactory.lookupById(errataId);
        Package reloadedPkg1 = PackageFactory.lookupByIdAndOrg(pkg1Id, user.getOrg());

        assertEquals(1, reloadedChannel.getErratas().size());
        assertEquals(2, reloadedChannel.getPackages().size());
        assertEquals(2, reloadedErrata.getPackages().size());

        // Remove package from channel
        reloadedChannel.removePackage(reloadedPkg1);
        ChannelFactory.save(reloadedChannel);

        TestUtils.flushAndClearSession();

        // Reload and verify persisted state after removal
        Channel finalChannel = ChannelFactory.lookupById(channelId);
        Errata finalErrata = ErrataFactory.lookupById(errataId);
        Package finalPkg1 = PackageFactory.lookupByIdAndOrg(pkg1Id, user.getOrg());
        Package finalPkg2 = PackageFactory.lookupByIdAndOrg(pkg2Id, user.getOrg());

        // Verify package-channel relationship removed
        assertEquals(1, finalChannel.getPackages().size());
        assertFalse(finalChannel.getPackages().contains(finalPkg1));
        assertFalse(finalPkg1.getChannels().contains(finalChannel));

        // Verify channel still has errata
        assertEquals(1, finalChannel.getErratas().size());
        assertTrue(finalChannel.getErratas().contains(finalErrata));

        // Verify errata still has the removed package
        assertEquals(2, finalErrata.getPackages().size());
        assertTrue(finalErrata.getPackages().contains(finalPkg1));
        assertTrue(finalErrata.getPackages().contains(finalPkg2));

        // Verify errata still has channel
        assertTrue(finalErrata.getChannels().contains(finalChannel));

        // Verify removed package still has errata
        assertTrue(finalPkg1.getErrata().contains(finalErrata));
    }

    /**
     * Test that clearErratas() only removes channel-errata relationships,
     * leaving channel's packages and all errata-package relationships intact.
     * Verified against database persistence.
     */
    @Test
    void testClearErratasDoesNotAffectPackages() {
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        Package pkg1 = PackageTest.createTestPackage(user.getOrg());
        Package pkg2 = PackageTest.createTestPackage(user.getOrg());
        Errata errata1 = new ErrataTestBuilder().orgId(user.getOrg().getId()).buildAndSave();
        Errata errata2 = new ErrataTestBuilder().orgId(user.getOrg().getId()).buildAndSave();

        // Establish relationships
        errata1.addPackage(pkg1);
        errata2.addPackage(pkg2);
        channel.addErrata(errata1);
        channel.addErrata(errata2);
        channel.addPackage(pkg1);
        channel.addPackage(pkg2);

        // Save and reload to verify initial state from DB
        ChannelFactory.save(channel);
        ErrataFactory.save(errata1);
        ErrataFactory.save(errata2);

        Long channelId = channel.getId();
        Long errata1Id = errata1.getId();
        Long errata2Id = errata2.getId();
        Long pkg1Id = pkg1.getId();
        Long pkg2Id = pkg2.getId();

        TestUtils.flushAndClearSession();

        // Reload and verify initial persisted state
        Channel reloadedChannel = ChannelFactory.lookupById(channelId);
        assertEquals(2, reloadedChannel.getErratas().size());
        assertEquals(2, reloadedChannel.getPackages().size());

        // Clear all erratas
        reloadedChannel.clearErratas();
        ChannelFactory.save(reloadedChannel);

        TestUtils.flushAndClearSession();

        // Reload and verify persisted state after clearing
        Channel finalChannel = ChannelFactory.lookupById(channelId);
        Errata finalErrata1 = ErrataFactory.lookupById(errata1Id);
        Errata finalErrata2 = ErrataFactory.lookupById(errata2Id);
        Package finalPkg1 = PackageFactory.lookupByIdAndOrg(pkg1Id, user.getOrg());
        Package finalPkg2 = PackageFactory.lookupByIdAndOrg(pkg2Id, user.getOrg());

        // Verify erratas cleared
        assertTrue(finalChannel.getErratas().isEmpty());

        // Verify packages still exist
        assertEquals(2, finalChannel.getPackages().size());
        assertTrue(finalChannel.getPackages().contains(finalPkg1));
        assertTrue(finalChannel.getPackages().contains(finalPkg2));

        // Verify packages still have their erratas
        assertTrue(finalPkg1.getErrata().contains(finalErrata1));
        assertTrue(finalPkg2.getErrata().contains(finalErrata2));

        // Verify erratas still have their packages
        assertEquals(1, finalErrata1.getPackages().size());
        assertTrue(finalErrata1.getPackages().contains(finalPkg1));
        assertEquals(1, finalErrata2.getPackages().size());
        assertTrue(finalErrata2.getPackages().contains(finalPkg2));
    }
    /**
     * When Channel has CascadeType.PERSIST and you add a detached Errata to it,
     * Hibernate will try to persist the detached entity, which throws an exception.
     * persist() is only valid for new/transient entities.
     * Correct options:
     * 1. Merge the detached entity first: errata = session.merge(errata)
     * 2. Reload the entity fresh: errata = ErrataFactory.lookupById(id)
     * 3. Use flushAndClearSession() before creating new relationships
     */
    @Test
    void testPassingDetachedEntityInCascadePersistThrowsException() {
        // Create and persist an errata
        Errata errata = new ErrataTestBuilder().orgId(user.getOrg().getId()).buildAndSave();
        Long errataId = errata.getId();

        // Detach the errata
        TestUtils.flushAndEvict(errata);

        // At this moment, errata is detached, ie, it has an id but is not managed
        // Anti-pattern: Create a channel and add the detached errata to it
        Channel newChannel = ChannelFactoryTest.createTestChannel(user);
        newChannel.addErrata(errata);

        // When saving the channel, CascadeType.PERSIST will try to persist the errata
        // But errata is DETACHED, it already has an ID so Hibernate throws exception
        assertThrows(EntityExistsException.class, () -> {
            ChannelFactory.save(newChannel);
            HibernateFactory.getSession().flush();
        });

        // Quick rollback
        newChannel.removeErrata(errata);

        // A correct approach would be:
        Errata reloadedErrata = ErrataFactory.lookupById(errataId);
        newChannel.addErrata(reloadedErrata);

        // Create a NEW errata without saving it -> fits cascade persist
        Errata newErrata = new ErrataTestBuilder().orgId(user.getOrg().getId()).build();
        assertNull(newErrata.getId());
        newChannel.addErrata(newErrata);

        // Create a NEW errata, saving it, managed by hibernate -> fits cascade merge
        Errata managedErrata = new ErrataTestBuilder().orgId(user.getOrg().getId()).buildAndSave();
        newChannel.addErrata(managedErrata);

        // Save and clear session
        ChannelFactory.save(newChannel);
        TestUtils.flushAndClearSession();

        // Reload and verify all erratas have the channel and vice versa
        Channel reloadedChannel = ChannelFactory.lookupById(newChannel.getId());
        assertTrue(reloadedChannel.getErratas().contains(reloadedErrata));
        assertTrue(reloadedChannel.getErratas().contains(newErrata));
        assertTrue(reloadedChannel.getErratas().contains(managedErrata));

        assertTrue(reloadedErrata.getChannels().contains(reloadedChannel));
        assertTrue(newErrata.getChannels().contains(reloadedChannel));
        assertTrue(managedErrata.getChannels().contains(reloadedChannel));
    }
}
