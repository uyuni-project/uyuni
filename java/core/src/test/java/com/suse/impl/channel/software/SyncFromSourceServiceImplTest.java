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
package com.suse.impl.channel.software;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFactoryTest;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.errata.ErrataTestBuilder;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.events.SyncFromSourceErrataEvent;
import com.redhat.rhn.frontend.xmlrpc.NoSuchChannelException;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.MessageQueueSpy;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.spec.channel.software.dto.ErrataCriteria;
import com.suse.spec.channel.software.dto.SyncOperation;
import com.suse.spec.channel.software.dto.SyncRequest;
import com.suse.spec.channel.software.dto.SyncResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Tests for SyncFromSourceServiceImpl
 */
public class SyncFromSourceServiceImplTest extends BaseTestCaseWithUser {

    private SyncFromSourceServiceImpl service;
    private Channel sourceChannel;
    private Channel targetChannel;
    private Org userOrg;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        service = new SyncFromSourceServiceImpl();
        sourceChannel = ChannelFactoryTest.createTestChannel(user);
        targetChannel = ChannelFactoryTest.createTestChannel(user);
        userOrg = user.getOrg();
    }

    /**
     * Tests that sync with invalid source channel label throws NoSuchChannelException.
     */
    @Test
    void testFailSyncWhenInvalidSourceChannel() {
        SyncRequest request = ChannelSoftwareTestUtils.createSyncRequest(SyncOperation.ERRATA_ONLY);

        NoSuchChannelException exception = assertThrows(NoSuchChannelException.class, () ->
                service.sync(user, "invalid-channel", targetChannel.getLabel(), request)
        );
        assertEquals("No such channel: invalid-channel", exception.getMessage());
    }

    /**
     * Tests that sync with invalid target channel label throws NoSuchChannelException.
     */
    @Test
    void testFailSyncWhenInvalidTargetChannel() {
        SyncRequest request = ChannelSoftwareTestUtils.createSyncRequest(SyncOperation.ERRATA_ONLY);

        NoSuchChannelException exception = assertThrows(NoSuchChannelException.class, () ->
                service.sync(user, sourceChannel.getLabel(), "invalid-channel", request)
        );
        assertEquals("No such channel: invalid-channel", exception.getMessage());
    }

    /**
     * Tests that sync without permission on target channel throws PermissionCheckFailureException.
     */
    @Test
    void testFailSyncWhenNoPermissionOnTargetChannel() {
        // Create a user in same org but without channel admin permissions
        User otherUser = new UserTestUtils.UserBuilder().orgId(userOrg.getId()).build();
        SyncRequest request = ChannelSoftwareTestUtils.createSyncRequest(SyncOperation.ERRATA_ONLY);

        PermissionCheckFailureException exception = assertThrows(PermissionCheckFailureException.class, () ->
                service.sync(otherUser, sourceChannel.getLabel(), targetChannel.getLabel(), request)
        );
        assertTrue(exception.getMessage().contains("User does not have permission to modify channel"));
    }

    // ERRATA_ONLY

    /**
     * Tests syncing erratas only from source to target channel.
     * When no filters are provides, merges ALL erratas in the source channel
     */
    @Test
    void testSyncWhenErrataOnly() {
        // Create errata with a package in source channel
        Errata errata = new ErrataTestBuilder().orgId(user.getOrg().getId()).buildAndSave();
        sourceChannel.addErrata(errata);
        Package pkg = PackageTest.createTestPackage(userOrg);
        errata.addPackage(pkg);

        // Assert setup
        assertFalse(targetChannel.getErratas().contains(errata)); // Target channel does not contain the errata
        assertFalse(errata.getPackages().isEmpty()); // Errata contains packages
        assertTrue(targetChannel.getPackages().isEmpty()); // Target channel does not contain any package

        // Sync
        SyncRequest request = ChannelSoftwareTestUtils.createSyncRequest(SyncOperation.ERRATA_ONLY);
        SyncResponse response = service.sync(user, sourceChannel.getLabel(), targetChannel.getLabel(), request);
        SyncResponse duplicatedResponse =
                service.sync(user, sourceChannel.getLabel(), targetChannel.getLabel(), request);

        // Assert first request only handles expected errata
        assertNotNull(response);
        assertEquals(1, response.erratas().size());
        assertTrue(response.erratas().contains(errata));
        assertTrue(response.packages().isEmpty());

        // Assert duplicated one does not consider that errata anymore
        assertTrue(duplicatedResponse.erratas().isEmpty());
        assertTrue(duplicatedResponse.packages().isEmpty());

        // Assert channel changes from db
        Channel reloadedTargetChannel = ChannelFactory.lookupById(targetChannel.getId());
        assertTrue(reloadedTargetChannel.getErratas().contains(errata)); // Channel NOW does contain the errata
        assertTrue(reloadedTargetChannel.getPackages().isEmpty()); // Channel STILL does not contain any package
    }

    // PACKAGES_ONLY

    /**
     * Tests syncing packages only from source to target channel.
     * When no filters are provided, merges ALL packages in the source channel.
    */
    @Test
    void testSyncWhenPackagesOnly() {
        TestSetupPackagesOnly testSetup = getTestSetupPackagesOnly();

        // Assert setup
        // Errata 2 contains pkg2
        assertTrue(testSetup.e2().getPackages().contains(testSetup.p2()));
        // Target channel does not contain the errata 2
        assertFalse(targetChannel.getErratas().contains(testSetup.e2()));
        // Target channel contains packages 2 and 3
        assertFalse(targetChannel.getPackages().contains(testSetup.p2()));
        assertFalse(targetChannel.getPackages().contains(testSetup.p3()));

        // Sync
        SyncRequest request = ChannelSoftwareTestUtils.createSyncRequest(SyncOperation.PACKAGES_ONLY);
        SyncResponse response = service.sync(user, sourceChannel.getLabel(), targetChannel.getLabel(), request);
        SyncResponse duplicatedResponse =
                service.sync(user, sourceChannel.getLabel(), targetChannel.getLabel(), request);

        // Assert first request only handles expected package
        assertNotNull(response);
        assertTrue(response.erratas().isEmpty());
        assertEquals(2, response.packages().size());
        assertTrue(response.packages().contains(testSetup.p2()));
        assertTrue(response.packages().contains(testSetup.p3()));

        // Assert duplicated one does not consider that package anymore
        assertTrue(duplicatedResponse.erratas().isEmpty());
        assertTrue(duplicatedResponse.packages().isEmpty());

        // Assert channel changes from db
        Channel reloadedTargetChannel = ChannelFactory.lookupById(targetChannel.getId());
        // Target channel STILL does not contain the errata 2
        assertFalse(reloadedTargetChannel.getErratas().contains(testSetup.e2()));
        // Target channel NOW does contain the packages 2 and 3
        assertTrue(reloadedTargetChannel.getPackages().contains(testSetup.p2()));
        assertTrue(reloadedTargetChannel.getPackages().contains(testSetup.p3()));

    }

    /**
     * Tests syncing packages only from source to target channel.
     * Uses same setup as {@link SyncFromSourceServiceImplTest#testSyncWhenPackagesOnly} to verify that when
     * filters are provided, only packages associated to the filtered erratas are merged.
     */
    @Test
    void testSyncWhenPackagesOnlyWithFilters() {
        TestSetupPackagesOnly testSetup = getTestSetupPackagesOnly();

        // Assert setup
        // Errata 2 contains pkg2
        assertTrue(testSetup.e2().getPackages().contains(testSetup.p2()));
        // Target channel does not contain the errata 2
        assertFalse(targetChannel.getErratas().contains(testSetup.e2()));
        // Target channel contains packages 2 and 3
        assertFalse(targetChannel.getPackages().contains(testSetup.p2()));
        assertFalse(targetChannel.getPackages().contains(testSetup.p3()));

        // Sync
        SyncRequest request = new SyncRequest(
                new ErrataCriteria(
                        List.of(
                                testSetup.e1().getAdvisoryName(),
                                testSetup.e2().getAdvisoryName()
                        ),
                        Date.from(Instant.now().minus(1, ChronoUnit.DAYS)),
                        Date.from(Instant.now().plus(1, ChronoUnit.DAYS))
                ),
                SyncOperation.PACKAGES_ONLY, false, false, false);
        SyncResponse response = service.sync(user, sourceChannel.getLabel(), targetChannel.getLabel(), request);

        assertNotNull(response);
        assertTrue(response.erratas().isEmpty());
        assertEquals(1, response.packages().size());
        assertTrue(response.packages().contains(testSetup.p2()));

        // Assert channel changes from db
        Channel reloadedTargetChannel = ChannelFactory.lookupById(targetChannel.getId());
        // Target channel STILL does not contain the errata 2
        assertFalse(reloadedTargetChannel.getErratas().contains(testSetup.e2()));
        // Target channel NOW does contain the packages 2
        assertTrue(reloadedTargetChannel.getPackages().contains(testSetup.p2()));
        // Target channel STILL does not contain the packages 3 (as it was not associated to filtered erratas)
        assertFalse(reloadedTargetChannel.getPackages().contains(testSetup.p3()));
    }

    /**
     * Tests syncing packages only from source to target channel.
     * Same test as {@link SyncFromSourceServiceImplTest#testSyncWhenPackagesOnlyWithFilters}
     * but in a scenario where filters do not match any errata.
     */
    @Test
    void testSyncWhenPackagesOnlyWithFiltersReturnsNoErratas() {
        getTestSetupPackagesOnly();

        // Sync
        SyncRequest request = new SyncRequest(
                new ErrataCriteria(
                        null, Date.from(Instant.now().plus(1, ChronoUnit.DAYS)), null
                ),
                SyncOperation.PACKAGES_ONLY, false, false, false);
        SyncResponse response = service.sync(user, sourceChannel.getLabel(), targetChannel.getLabel(), request);

        assertNotNull(response);
        assertTrue(response.erratas().isEmpty());
        assertTrue(response.packages().isEmpty());
    }

    /**
     * Tests sync when source and target have same packages.
     * Verifies that package difference calculation returns empty set.
     */
    @Test
    void testSyncPackageOnlyWhenIdenticalPackagesReturnsEmpty() {
        // Add same packages to both channels
        Package pkg = PackageTest.createTestPackage(userOrg);
        sourceChannel.addPackage(pkg);
        targetChannel.addPackage(pkg);

        // Sync
        SyncRequest request = ChannelSoftwareTestUtils.createSyncRequest(SyncOperation.PACKAGES_ONLY);
        SyncResponse response = service.sync(user, sourceChannel.getLabel(), targetChannel.getLabel(), request);

        assertNotNull(response);
        assertTrue(response.erratas().isEmpty());
        assertTrue(response.packages().isEmpty());
    }

    // ERRATA_AND_PACKAGES

    /**
     * Tests syncing both erratas and packages from source to target channel without errata filters
     * Verifies that all erratas and packages in source are merged, regardless if they are related to each other
     */
    @Test
    void testSyncWhenErrataAndPackages() {
        // Create errata with packages in source channel
        Errata errata = new ErrataTestBuilder().orgId(user.getOrg().getId()).buildAndSave();
        Package pkg1 = PackageTest.createTestPackage(userOrg);
        errata.addPackage(pkg1);
        ErrataFactory.addToChannel(errata, sourceChannel, user, Set.of(pkg1));

        // Add a package to source channel not associated to an errata
        Package pkg2 = PackageTest.createTestPackage(userOrg);
        sourceChannel.addPackage(pkg2);

        // Assert setup
        assertTrue(targetChannel.getErratas().isEmpty()); // Target channel does not contain erratas
        assertTrue(targetChannel.getPackages().isEmpty()); // Target channel does not contain package

        // Sync
        SyncRequest request = ChannelSoftwareTestUtils.createSyncRequest(SyncOperation.ERRATA_AND_PACKAGES);
        SyncResponse response = service.sync(user, sourceChannel.getLabel(), targetChannel.getLabel(), request);
        SyncResponse duplicateResponse =
                service.sync(user, sourceChannel.getLabel(), targetChannel.getLabel(), request);

        // Assert first request will handle expected errata and packages
        assertNotNull(response);
        assertEquals(1, response.erratas().size());
        assertTrue(response.erratas().contains(errata));
        assertEquals(2, response.packages().size());
        assertTrue(response.packages().contains(pkg1));
        assertTrue(response.packages().contains(pkg2));

        // Assert duplicated one does not consider any of already handles erratas and packages
        assertNotNull(duplicateResponse);
        assertTrue(duplicateResponse.erratas().isEmpty());
        assertTrue(duplicateResponse.packages().isEmpty());

        // Assert channel changes from db
        Channel reloadedTargetChannel = ChannelFactory.lookupById(targetChannel.getId());
        assertTrue(reloadedTargetChannel.getErratas().contains(errata)); // Target channel NOW contains errata
        // Target channel NOW contains both packages
        assertTrue(reloadedTargetChannel.getPackages().contains(pkg1));
        assertTrue(reloadedTargetChannel.getPackages().contains(pkg1));
    }

    /**
     * Tests syncing both erratas and packages from source to target channel with errata filters
     * Verifies that all filtered erratas are merged and ONLY packages associated to the erratas are merged.
     */
    @Test
    void testSyncWhenErrataAndPackagesWithFilters() {
        // Create errata with packages in source channel
        Errata errata = new ErrataTestBuilder().orgId(user.getOrg().getId()).buildAndSave();
        Package pkg1 = PackageTest.createTestPackage(userOrg);
        errata.addPackage(pkg1);
        ErrataFactory.addToChannel(errata, sourceChannel, user, Set.of(pkg1));

        // Add a package to source channel not associated to an errata
        Package pkg2 = PackageTest.createTestPackage(userOrg);
        sourceChannel.addPackage(pkg2);

        // Assert setup
        assertTrue(targetChannel.getErratas().isEmpty()); // Target channel does not contain erratas
        assertTrue(targetChannel.getPackages().isEmpty()); // Target channel does not contain package
        assertTrue(pkg2.getErrata().isEmpty()); // Package 2 is not associated to any errata

        // Sync
        SyncRequest request = new SyncRequest(
                new ErrataCriteria(
                        List.of(errata.getAdvisoryName()),
                        Date.from(Instant.now().minus(1, ChronoUnit.DAYS)),
                        Date.from(Instant.now().plus(1, ChronoUnit.DAYS))
                ),
                SyncOperation.ERRATA_AND_PACKAGES, false, true, true);
        SyncResponse response = service.sync(user, sourceChannel.getLabel(), targetChannel.getLabel(), request);
        SyncResponse duplicateResponse =
                service.sync(user, sourceChannel.getLabel(), targetChannel.getLabel(), request);

        // Assert first request will handle expected errata and packages
        assertNotNull(response);
        assertEquals(1, response.erratas().size());
        assertTrue(response.erratas().contains(errata));
        assertEquals(1, response.packages().size());
        assertTrue(response.packages().contains(pkg1));

        // Assert duplicated one does not consider any of already handles erratas and packages
        assertNotNull(duplicateResponse);
        assertTrue(duplicateResponse.erratas().isEmpty());
        assertTrue(duplicateResponse.packages().isEmpty());

        // Assert channel changes from db
        Channel reloadedTargetChannel = ChannelFactory.lookupById(targetChannel.getId());
        assertTrue(reloadedTargetChannel.getErratas().contains(errata)); // Target channel NOW contains errata
        // Target channel NOW contains pkg1
        assertTrue(reloadedTargetChannel.getPackages().contains(pkg1));
        // Target channel STILL does not contain pkg2
        assertFalse(reloadedTargetChannel.getPackages().contains(pkg2));
    }

    /**
     * Tests sync when source channel has no erratas.
     * Verifies that response contains empty sets.
     */
    @Test
    void testSyncErrataAndPackagesWhenSourceChannelHasNoErrata() {
        SyncRequest request = ChannelSoftwareTestUtils.createSyncRequest(SyncOperation.ERRATA_AND_PACKAGES);
        SyncResponse response = service.sync(user, sourceChannel.getLabel(), targetChannel.getLabel(), request);

        assertNotNull(response);
        assertTrue(response.erratas().isEmpty());
        assertTrue(response.packages().isEmpty());
    }

    /**
     * Reuses setup in {@link SyncFromSourceServiceImplTest#testSyncWhenErrataAndPackages} to verify async
     * response is always empty and that events are scheduled in sync mode.
     */
    @Test
    void testSyncWhenErrataAndPackagesAsynchronously() {
        MessageQueueSpy mqSpy = new MessageQueueSpy();
        mqSpy.install();

        // Create errata with packages in source channel
        Errata errata = new ErrataTestBuilder().orgId(user.getOrg().getId()).buildAndSave();
        Package pkg1 = PackageTest.createTestPackage(userOrg);
        errata.addPackage(pkg1);
        ErrataFactory.addToChannel(errata, sourceChannel, user, Set.of(pkg1));

        // Add a package to source channel not associated to an errata
        Package pkg2 = PackageTest.createTestPackage(userOrg);
        sourceChannel.addPackage(pkg2);

        try {
            // Sync
            SyncRequest request = ChannelSoftwareTestUtils.createSyncRequestAsync(SyncOperation.ERRATA_AND_PACKAGES);
            SyncResponse response = service.sync(user, sourceChannel.getLabel(), targetChannel.getLabel(), request);
            SyncResponse duplicatedResponse =
                    service.sync(user, sourceChannel.getLabel(), targetChannel.getLabel(), request);

            // Assert first request will only return the queued erratas but not any package details
            assertNotNull(response);
            assertTrue(response.erratas().isEmpty());
            assertTrue(response.packages().isEmpty());

            // Assert duplicates of response will be the same in async
            assertEquals(response, duplicatedResponse);

            // Assert published events
            List<SyncFromSourceErrataEvent> events = mqSpy.getEvents(SyncFromSourceErrataEvent.class);
            assertEquals(2, events.size());
            assertEquals(1, events.stream().distinct().count());
            assertFalse(events.iterator().next().getSyncRequest().async());
        }
        finally {
            mqSpy.uninstall();
        }
    }

    /**
     * Creates a setup with 2 erratas EX and 3 packages PY as:
     * E1 -> P1
     * E2 -> P2
     * P3 (isolated in source channel)
     *
     * Target channel will have E1, P1.
     * Source channel will have E1, P1, E2, P2 and P3.
     */
    private TestSetupPackagesOnly getTestSetupPackagesOnly() {
        // Create 2 erratas and 3 packages
        Errata e1 = new ErrataTestBuilder().orgId(user.getOrg().getId()).buildAndSave();
        Errata e2 = new ErrataTestBuilder().orgId(user.getOrg().getId()).buildAndSave();
        Package p1 = PackageTest.createTestPackage(userOrg);
        Package p2 = PackageTest.createTestPackage(userOrg);
        Package p3 = PackageTest.createTestPackage(userOrg);

        // Add packages to the erratas
        e1.addPackage(p1);
        e2.addPackage(p2);

        // Target channel has E1 and P1
        targetChannel.addErrata(e1);
        targetChannel.addPackage(p1);

        // Source channel has E1, E2, P1, P2 and P3
        sourceChannel.addErrata(e1);
        sourceChannel.addErrata(e2);
        sourceChannel.addPackage(p1);
        sourceChannel.addPackage(p2);
        sourceChannel.addPackage(p3);

        return new TestSetupPackagesOnly(e1, e2, p1, p2, p3);
    }

    private record TestSetupPackagesOnly(Errata e1, Errata e2, Package p1, Package p2, Package p3) {
    }
}
