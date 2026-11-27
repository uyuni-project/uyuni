/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.redhat.rhn.manager.errata.syncwithpackages;

import static com.redhat.rhn.manager.errata.syncwithpackages.SyncErrataWithPackagesTestUtils.createCloneOf;
import static com.redhat.rhn.manager.errata.syncwithpackages.SyncErrataWithPackagesTestUtils.createErrata;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactoryTest;
import com.redhat.rhn.domain.errata.ClonedErrata;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class SyncErrataWithPackagesErrataHelperTest extends BaseTestCaseWithUser {

    // a map that accepts the null source channel of errata found without one
    private static Map<Errata, Channel> mapOf(Errata errata, Channel source) {
        Map<Errata, Channel> map = new HashMap<>();
        map.put(errata, source);
        return map;
    }

    /**
     * Advisory names are looked up in the source channel; matching errata are mapped to the source channel and unknown
     * names are left out.
     */
    @Test
    void testFindInSourceByNames() {
        Channel source = channel();
        Errata wanted = createErrata(user.getOrg(), source);
        createErrata(user.getOrg(), source);

        Map<Errata, Channel> found = SyncErrataWithPackagesErrataHelper.findInSource(source,
                List.of(wanted.getAdvisoryName(), "NOT-THERE"));

        assertEquals(Map.of(wanted, source), found);
    }

    /**
     * An empty list of advisory names returns every erratum of the source channel, and none from other channels.
     */
    @Test
    void testFindInSourceWithoutNamesReturnsAllErrataOfTheChannel() {
        Channel source = channel();
        Errata first = createErrata(user.getOrg(), source);
        Errata second = createErrata(user.getOrg(), source);
        createErrata(user.getOrg(), channel());

        Map<Errata, Channel> found = SyncErrataWithPackagesErrataHelper.findInSource(source, List.of());

        assertEquals(Map.of(first, source, second, source), found);
    }

    /**
     * Only the errata of the source channel last modified within the date range are returned.
     */
    @Test
    void testFindInSourceByDates() {
        Channel source = channel();
        Errata inRange = createErrata(user.getOrg(), source);
        Errata outOfRange = createErrata(user.getOrg(), source);
        HibernateFactory.getSession().flush();
        Date start = new Date(System.currentTimeMillis() - 3_600_000L);
        Date end = new Date(System.currentTimeMillis() + 3_600_000L);
        HibernateFactory.getSession()
                .createNativeMutationQuery("UPDATE rhnErrata SET last_modified = :date WHERE id = :id")
                .setParameter("date", new Date(start.getTime() - 3_600_000L))
                .setParameter("id", outOfRange.getId())
                .executeUpdate();

        Map<Errata, Channel> found = SyncErrataWithPackagesErrataHelper.findInSource(source, start, end);

        assertEquals(Map.of(inRange, source), found);
    }

    /**
     * When several clone ancestors hold the erratum, the nearest one is its source channel.
     */
    @Test
    void testFindByCriteriaUsesTheNearestAncestorHoldingTheAdvisory() {
        Channel original = channel();
        Channel middle = ChannelFactoryTest.createTestClonedChannel(original, user);
        Channel target = ChannelFactoryTest.createTestClonedChannel(middle, user);
        Errata errata = createErrata(null, original);
        middle.addErrata(errata);

        Map<Errata, Channel> found = SyncErrataWithPackagesErrataHelper.findByCriteria(user, target,
                List.of(errata.getAdvisoryName()));

        assertEquals(Map.of(errata, middle), found);
    }

    /**
     * When the nearest ancestor does not hold the erratum, the walk goes up to further ancestors.
     */
    @Test
    void testFindByCriteriaWalksUpToFurtherAncestors() {
        Channel original = channel();
        Channel middle = ChannelFactoryTest.createTestClonedChannel(original, user);
        Channel target = ChannelFactoryTest.createTestClonedChannel(middle, user);
        Errata errata = createErrata(null, original);

        Map<Errata, Channel> found = SyncErrataWithPackagesErrataHelper.findByCriteria(user, target,
                List.of(errata.getAdvisoryName()));

        assertEquals(Map.of(errata, original), found);
    }

    /**
     * The target channel itself is never the source channel, even when it holds the erratum; the erratum is then found
     * through the org lookup without source.
     */
    @Test
    void testFindByCriteriaNeverUsesTheTargetAsSource() {
        Channel original = channel();
        Channel target = ChannelFactoryTest.createTestClonedChannel(original, user);
        Errata errata = createErrata(user.getOrg(), target);

        Map<Errata, Channel> found = SyncErrataWithPackagesErrataHelper.findByCriteria(user, target,
                List.of(errata.getAdvisoryName()));

        assertEquals(mapOf(errata, null), found);
    }

    /**
     * Clone ancestors the user cannot access are skipped, so the erratum is found through the fallback lookups without
     * source.
     */
    @Test
    void testFindByCriteriaSkipsInaccessibleAncestors() {
        Org otherOrg = UserTestUtils.createOrg(this);
        Channel inaccessible = ChannelFactoryTest.createTestChannel(otherOrg);
        Channel target = ChannelFactoryTest.createTestClonedChannel(inaccessible, user);
        Errata vendor = createErrata(null, inaccessible);

        Map<Errata, Channel> found = SyncErrataWithPackagesErrataHelper.findByCriteria(user, target,
                List.of(vendor.getAdvisoryName()));

        // the ancestor is skipped, so the vendor erratum is found without a source channel
        assertEquals(mapOf(vendor, null), found);
    }

    /**
     * Without a holding ancestor, an advisory name is looked up among the target channel org errata, then the user org
     * errata, then vendor errata, and the first match wins; unknown names are left out.
     */
    @Test
    void testFindByCriteriaFallsBackToTargetOrgThenUserOrgThenVendor() {
        Org targetOrg = UserTestUtils.createOrg(this);
        Channel target = ChannelFactoryTest.createTestChannel(targetOrg);
        String all = "ALL-" + System.nanoTime();
        String userAndVendor = "USER-VENDOR-" + System.nanoTime();
        String vendorOnly = "VENDOR-" + System.nanoTime();
        Errata inTargetOrg = createErrata(all, null, targetOrg, null);
        createErrata(all, null, user.getOrg(), null);
        createErrata(all, null, null, null);
        Errata inUserOrg = createErrata(userAndVendor, null, user.getOrg(), null);
        createErrata(userAndVendor, null, null, null);
        Errata vendor = createErrata(vendorOnly, null, null, null);

        Map<Errata, Channel> found = SyncErrataWithPackagesErrataHelper.findByCriteria(user, target,
                List.of(all, userAndVendor, vendorOnly, "NOT-THERE"));

        Map<Errata, Channel> expected = mapOf(inTargetOrg, null);
        expected.put(inUserOrg, null);
        expected.put(vendor, null);
        assertEquals(expected, found);
    }

    /**
     * The erratum is its own representative when the target channel holds it.
     */
    @Test
    void testFindRepresentativeInTargetReturnsTheErratumItself() {
        Channel target = channel();
        Errata errata = createErrata(user.getOrg(), target);

        assertEquals(Optional.of(errata),
                SyncErrataWithPackagesErrataHelper.findRepresentativeErrataInTarget(target, errata));
    }

    /**
     * A clone of the erratum held by the target channel is its representative.
     */
    @Test
    void testFindRepresentativeInTargetReturnsAClone() {
        Channel target = channel();
        Errata vendor = createErrata(null, null);
        Errata clone = createCloneOf(user.getOrg(), vendor, target);

        assertEquals(Optional.of(clone),
                SyncErrataWithPackagesErrataHelper.findRepresentativeErrataInTarget(target, vendor));
    }

    /**
     * A sibling of the erratum (another clone of the same original) held by the target channel is its representative.
     */
    @Test
    void testFindRepresentativeInTargetReturnsASibling() {
        Channel target = channel();
        Errata vendor = createErrata(null, null);
        Errata clone = createCloneOf(user.getOrg(), vendor, null);
        Errata sibling = createCloneOf(user.getOrg(), vendor, target);

        assertEquals(Optional.of(sibling),
                SyncErrataWithPackagesErrataHelper.findRepresentativeErrataInTarget(target, clone));
    }

    /**
     * No representative is found when the target channel holds neither the erratum, nor a clone, nor a sibling of it.
     */
    @Test
    void testFindRepresentativeInTargetReturnsNothingWhenNotHeld() {
        Errata vendor = createErrata(null, channel());
        createCloneOf(user.getOrg(), vendor, null);

        assertEquals(Optional.empty(),
                SyncErrataWithPackagesErrataHelper.findRepresentativeErrataInTarget(channel(), vendor));
    }

    /**
     * An erratum owned by the org is its own representative.
     */
    @Test
    void testRepresentativeOwnedByReturnsTheOwnErratum() {
        Errata own = createErrata(user.getOrg(), null);

        assertEquals(own, SyncErrataWithPackagesErrataHelper.getRepresentativeErrataOwnedBy(user.getOrg(), own));
    }

    /**
     * When the org already has several clones of the erratum, the oldest one is the representative.
     */
    @Test
    void testRepresentativeOwnedByReusesTheOldestClone() {
        Errata vendor = createErrata(null, null);
        Errata oldest = createCloneOf(user.getOrg(), vendor, null);
        createCloneOf(user.getOrg(), vendor, null);

        assertEquals(oldest, SyncErrataWithPackagesErrataHelper.getRepresentativeErrataOwnedBy(user.getOrg(), vendor));
    }

    /**
     * When the org has no clone of the erratum, a new clone owned by the org is created, with the erratum as original
     * and the same packages.
     */
    @Test
    void testRepresentativeOwnedByClonesWhenNoCloneExists() {
        Errata vendor = createErrata(null, null, pkg());

        Errata representative =
                SyncErrataWithPackagesErrataHelper.getRepresentativeErrataOwnedBy(user.getOrg(), vendor);

        assertNotEquals(vendor, representative);
        assertTrue(representative.isCloned());
        assertEquals(vendor, ((ClonedErrata) representative).getOriginal());
        assertEquals(user.getOrg(), representative.getOrg());
        assertEquals(vendor.getPackages(), representative.getPackages());
    }


    private Package pkg() {
        return PackageTest.createTestPackage(user.getOrg());
    }

    private Channel channel() {
        return ChannelFactoryTest.createTestChannel(user);
    }
}
