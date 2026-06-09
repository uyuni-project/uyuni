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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactoryTest;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataTestBuilder;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ErrataTestUtils;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Tests for ErrataResolver
 */
public class ErrataResolverTest extends BaseTestCaseWithUser {

    private Channel originalChannel;
    private Org userOrg;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        originalChannel = ChannelFactoryTest.createTestChannel(user);
        userOrg = user.getOrg();
    }

    /**
     * Tests that when an errata with the specified advisory name does not exist
     * in any org (original, user, or vendor), the resolver returns an empty set.
     */
    @Test
    void testResolveFromCascadingOrgReturnsEmptyWhenNotFound() {
        Set<Errata> result = ErrataResolver.resolveFromCascadingOrg(
                originalChannel, userOrg, List.of(TestUtils.randomString()), null, null
        );
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Tests that when no advisory name filter is provided,
     * the resolver returns all erratas from the user's org.
     */
    @Test
    void testResolveFromCascadingOrgWithoutAdvisoryNameFilter() {
        Errata errata1 = new ErrataTestBuilder().orgId(userOrg.getId()).buildAndSave();
        Errata errata2 = new ErrataTestBuilder().orgId(userOrg.getId()).buildAndSave();

        Set<Errata> result = ErrataResolver.resolveFromCascadingOrg(
                originalChannel, userOrg, null, null, null
        );

        assertNotNull(result);
        assertTrue(result.contains(errata1));
        assertTrue(result.contains(errata2));
        assertEquals(2, result.size());
    }

    /**
     * Tests that when an errata exists in the original channel's org
     */
    @Test
    void testResolveFromCascadingOrgFallsBackToUserOrg() {
        // Create errata in originalChannel's org
        Errata errata = new ErrataTestBuilder().orgId(userOrg.getId()).buildAndSave();
        String advisoryName = errata.getAdvisoryName();

        // Resolve
        Set<Errata> result = ErrataResolver.resolveFromCascadingOrg(
                originalChannel, null, List.of(advisoryName), null, null
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(errata));
        Errata resolved = result.iterator().next();
        assertEquals(originalChannel.getOrg().getId(), resolved.getOrg().getId());
    }

    /**
     * Tests that the resolver handles null original channel gracefully,
     * falling back to user org and vendor lookup only.
     */
    @Test
    void testResolveFromCascadingOrgWithNullOriginalChannel() {
        // Create errata in user org
        Errata errata = new ErrataTestBuilder().orgId(userOrg.getId()).buildAndSave();
        String advisoryName = errata.getAdvisoryName();

        // Resolve, dont provide original channel
        Set<Errata> result = ErrataResolver.resolveFromCascadingOrg(
                null, userOrg, List.of(advisoryName), null, null
        );

        assertNotNull(result);
        assertTrue(result.contains(errata));
        Errata resolved = result.iterator().next();
        assertEquals(originalChannel.getOrg().getId(), resolved.getOrg().getId());
    }

    /**
     * Tests that when an errata exists in the vendor source but not in the original or user orgs,
     * the resolver correctly returns the vendor version.
     */
    @Test
    void testResolveFromCascadingOrgFallsBackToVendor() {
        // Create errata without org
        Errata errata = new ErrataTestBuilder().orgId(userOrg.getId()).buildAndSave();
        errata.setOrg(null);
        TestUtils.saveAndFlush(errata);
        String advisoryName = errata.getAdvisoryName();

        // Resolve
        Set<Errata> result = ErrataResolver.resolveFromCascadingOrg(
                originalChannel, userOrg, List.of(advisoryName), null, null
        );

        assertNotNull(result);
        assertTrue(result.contains(errata));
        Errata resolved = result.iterator().next();
        assertNull(resolved.getOrg());
    }

    /**
     * Tests that the resolver handles errata using date filters.
     */
    @Test
    void testResolveFromCascadingOrgWithDateFilters() {
        LocalDate localDate = LocalDate.of(2022, 5, 29);
        ZonedDateTime startOfDayZoneDateTime = localDate.atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime endOfDayZoneDateTime = startOfDayZoneDateTime.plusDays(1).minusNanos(1);

        Instant startDate = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endDate =  endOfDayZoneDateTime.toInstant();

        // Erratas last modified at exact time boundaries should not be considered
        Errata atStartErrata = new ErrataTestBuilder().orgId(userOrg.getId()).buildAndSave();
        ErrataTestUtils.setErrataLastModified(atStartErrata, startDate);

        Errata atEndErrata = new ErrataTestBuilder().orgId(userOrg.getId()).buildAndSave();
        ErrataTestUtils.setErrataLastModified(atEndErrata, endDate);

        // A couple of erratas just a sec within the boundaries
        Errata afterStartErrata = new ErrataTestBuilder().orgId(userOrg.getId()).buildAndSave();
        ErrataTestUtils.setErrataLastModified(afterStartErrata, startOfDayZoneDateTime.plusSeconds(1).toInstant());

        Errata beforeEndErrata = new ErrataTestBuilder().orgId(userOrg.getId()).buildAndSave();
        ErrataTestUtils.setErrataLastModified(beforeEndErrata, endDate.minusSeconds(1));

        List<String> advisoryNames = List.of(
                atStartErrata.getAdvisoryName(),
                afterStartErrata.getAdvisoryName(),
                beforeEndErrata.getAdvisoryName(),
                atEndErrata.getAdvisoryName()
        );

        // resolve
        Set<Errata> result = ErrataResolver.resolveFromCascadingOrg(
                originalChannel, userOrg, advisoryNames, Date.from(startDate), Date.from(endDate)
        );

        assertNotNull(result);
        assertTrue(result.contains(beforeEndErrata));
        assertTrue(result.contains(afterStartErrata));
        assertEquals(2, result.size());
    }

}
