/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.impl.channel.software.helper;

import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactoryTest;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.errata.ErrataTestBuilder;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tests for PackageCalculator
 */
public class PackageCalculatorTest extends BaseTestCaseWithUser {

    private Channel targetChannel;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        targetChannel = ChannelFactoryTest.createTestChannel(user);
    }

    // calculateFromCloneTree

    /**
     * Tests that calculateFromVendorMatch returns empty set when given empty errata set.
     */
    @Test
    void testCalculateFromVendorMatchEmptyErrataSet() {
        Set<Package> result = PackageCalculator.calculateFromVendorMatch(targetChannel, emptySet(), user);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Tests that calculateFromCloneTree handles empty errata set.
     */
    @Test
    public void testCalculateFromCloneTreeEmptyErrataSet() {
        Channel originalChannel = ChannelFactoryTest.createTestChannel(user);
        Channel clonedChannel = ChannelFactoryTest.createTestClonedChannel(originalChannel, user);

        Map<Errata, List<Package>> result =
                PackageCalculator.calculateFromCloneTree(clonedChannel, emptySet(), user, emptySet());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Tests that calculateFromCloneTree doesnt need to walk up the clone hierarchy
     * when the errata is already associated with the target channel, and can directly get packages from there.
     */
    @Test
    void testCalculateFromCloneTreeDirectlyGetsPackages() {
        // Create clone hierarchy
        Channel originalChannel = ChannelFactoryTest.createTestChannel(user);
        Channel clonedChannel = ChannelFactoryTest.createTestClonedChannel(originalChannel, user);

        // Create errata in original channel
        Errata errata = new ErrataTestBuilder().orgId(user.getOrg().getId()).buildAndSave();
        Package pkg = PackageTest.createTestPackage(user.getOrg());
        errata.addPackage(pkg);
        ErrataFactory.addToChannel(errata, originalChannel, user, Set.of(pkg));

        // Calculate
        Map<Errata, List<Package>> result =
        PackageCalculator.calculateFromCloneTree(clonedChannel, Set.of(errata), user, emptySet());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey(errata));
        assertEquals(1, result.get(errata).size());
        assertTrue(result.get(errata).contains(pkg));

    }

    /**
     * Tests that calculateFromCloneTree walks up the clone hierarchy
     * until it finds a channel that contains the errata.
     * In this test one of the erratas is only in the original channel,
     * and a second errata is in an intermediate clone.
     */
    @Test
    void testCalculateFromCloneTreeWalksUpCloneHierarchy() {
        // Create clone hierarchy: original -> clone1 -> clone2 -> clone3
        Channel originalChannel = ChannelFactoryTest.createTestChannel(user);
        Channel clone1 = ChannelFactoryTest.createTestClonedChannel(originalChannel, user);
        Channel clone2 = ChannelFactoryTest.createTestClonedChannel(clone1, user);
        Channel clone3 = ChannelFactoryTest.createTestClonedChannel(clone2, user);

        // Create first errata in original channel
        Errata errata1 = new ErrataTestBuilder().orgId(user.getOrg().getId()).buildAndSave();
        Package pkg1 = PackageTest.createTestPackage(user.getOrg());
        errata1.addPackage(pkg1);
        ErrataFactory.addToChannel(errata1, originalChannel, user, Set.of(pkg1));

        // Create a second errata in clone2 channel
        Errata errata2 = new ErrataTestBuilder().orgId(user.getOrg().getId()).buildAndSave();
        Package pkg2 = PackageTest.createTestPackage(user.getOrg());
        errata2.addPackage(pkg2);
        ErrataFactory.addToChannel(errata2, clone2, user, Set.of(pkg2));

        // Calculate
        Map<Errata, List<Package>> result =
                PackageCalculator.calculateFromCloneTree(clone3, Set.of(errata1, errata2), user, emptySet());

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(errata1));
        assertEquals(1, result.get(errata1).size());
        assertTrue(result.get(errata1).contains(pkg1));
        assertTrue(result.containsKey(errata2));
        assertEquals(1, result.get(errata2).size());
        assertTrue(result.get(errata2).contains(pkg2));

    }

}
