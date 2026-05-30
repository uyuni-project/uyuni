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
import com.redhat.rhn.domain.channel.ChannelFactoryTest;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataTestBuilder;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.events.SyncFromVendorErrataEvent;
import com.redhat.rhn.frontend.xmlrpc.NoSuchChannelException;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.MessageQueueSpy;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.spec.channel.software.dto.SyncOperation;
import com.suse.spec.channel.software.dto.SyncRequest;
import com.suse.spec.channel.software.dto.SyncResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Tests for SyncFromVendorServiceImpl
 */
public class SyncFromVendorServiceImplTest extends BaseTestCaseWithUser {

    private SyncFromVendorServiceImpl service;
    private Channel targetChannel;
    private Org userOrg;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        service = new SyncFromVendorServiceImpl();
        targetChannel = ChannelFactoryTest.createTestChannel(user);
        userOrg = user.getOrg();
    }

    /**
     * Tests that sync with invalid target channel label throws NoSuchChannelException.
     */
    @Test
    public void testFailSyncWhenInvalidTargetChannel() {
        SyncRequest request = ChannelSoftwareTestUtils.createSyncRequest(SyncOperation.ERRATA_ONLY);

        NoSuchChannelException exception = assertThrows(NoSuchChannelException.class, () ->
                service.sync(user, "invalid-channel", request)
        );
        assertEquals("No such channel: invalid-channel", exception.getMessage());
    }

    /**
     * Tests that sync without permission on target channel throws PermissionCheckFailureException.
     */
    @Test
    public void testFailSyncWhenNoPermissionOnTarget() {
        // Create a user in same org but without channel admin permissions
        User otherUser = new UserTestUtils.UserBuilder().orgId(userOrg.getId()).build();
        SyncRequest request = ChannelSoftwareTestUtils.createSyncRequest(SyncOperation.ERRATA_ONLY);

        PermissionCheckFailureException exception = assertThrows(PermissionCheckFailureException.class, () ->
                service.sync(otherUser, targetChannel.getLabel(), request)
        );
        assertTrue(exception.getMessage().contains("User does not have permission to modify channel"));
    }

    // ERRATA_ONLY

    /**
     * Tests syncing erratas only from vendor to a non-cloned channel.
     * Verifies that erratas are cloned via cascading org lookup and packages set is empty.
     */
    @Test
    public void testSyncWhenErrataOnly() {
        // Create vendor errata
        Errata errata = new ErrataTestBuilder()
                .orgId(user.getOrg().getId())
                .buildAndSave();
        errata.setOrg(null);

        // Sync errata only
        SyncRequest request = ChannelSoftwareTestUtils.createSyncRequest(
                SyncOperation.ERRATA_ONLY, errata.getAdvisoryName()
        );
        SyncResponse response = service.sync(user, targetChannel.getLabel(), request);
        SyncResponse duplicatedResponse = service.sync(user, targetChannel.getLabel(), request);

        assertNotNull(response);
        // Returning errata is a cloned version
        assertFalse(response.erratas().contains(errata));
        Errata syncErrata = response.erratas().iterator().next();
        assertTrue(syncErrata.isCloned());
        assertTrue(syncErrata.getCves().containsAll(errata.getCves()));
        assertTrue(syncErrata.getPackages().containsAll(errata.getPackages()));
        assertTrue(syncErrata.getAdvisoryName().contains(errata.getAdvisoryName()));
        assertTrue(response.packages().isEmpty());

        // Assert duplicates of response will be the same
        assertEquals(response, duplicatedResponse);
    }

    /**
     * Tests syncing erratas only from vendor to a non-cloned channel asynchronously.
     * Repeats same setup as {@link SyncFromVendorServiceImplTest#testSyncWhenErrataOnly}.
     */
    @Test
    public void testSyncWhenErrataOnlyAsynchronously() {
        MessageQueueSpy mqSpy = new MessageQueueSpy();
        mqSpy.install();
        try {
            // Create vendor errata
            Errata errata = new ErrataTestBuilder()
                    .orgId(user.getOrg().getId())
                    .buildAndSave();
            errata.setOrg(null);

            // Sync errata only asynchronously
            SyncRequest request = ChannelSoftwareTestUtils.createSyncRequestAsync(
                    SyncOperation.ERRATA_ONLY, errata.getAdvisoryName()
            );
            SyncResponse response = service.sync(user, targetChannel.getLabel(), request);

            // Async response should be empty (work happens in background)
            assertNotNull(response);
            assertTrue(response.erratas().isEmpty());
            assertTrue(response.packages().isEmpty());

            // Verify event was published
            List<SyncFromVendorErrataEvent> events = mqSpy.getEvents(SyncFromVendorErrataEvent.class);
            assertEquals(1, events.size());
            assertEquals(user.getId(), events.get(0).getUserId());
            assertEquals(targetChannel.getLabel(), events.get(0).getTargetChannelLabel());
        }
        finally {
            mqSpy.uninstall();
        }
    }

    // PACKAGES_ONLY

    /**
     * Tests syncing packages using vendor match strategy.
     * Verifies that only packages matching existing name+arch in target are selected.
     */
    @Test
    public void testSyncWhenPackagesOnly() {
        // Create vendor errata with packages and unique advisory
        Errata errata = new ErrataTestBuilder().orgId(user.getOrg().getId()).buildAndSave();
        Package pkg1 = PackageTest.createTestPackage(userOrg);
        Package pkg2 = PackageTest.createTestPackage(userOrg);
        errata.addPackage(pkg1);
        errata.addPackage(pkg2);
        errata.setOrg(null);  // Vendor errata

        // Add one package to target
        targetChannel.addPackage(pkg1);

        // Sync
        SyncRequest request = ChannelSoftwareTestUtils.createSyncRequest(
                SyncOperation.PACKAGES_ONLY, errata.getAdvisoryName()
        );
        SyncResponse response = service.sync(user, targetChannel.getLabel(), request);
        SyncResponse duplicatedResponse = service.sync(user, targetChannel.getLabel(), request);

        // Assert first request only handles expected package
        assertNotNull(response);
        assertTrue(response.erratas().isEmpty());
        assertEquals(1, response.packages().size());
        assertTrue(response.packages().contains(pkg1));

        // Assert duplicates of response will be the same
        assertEquals(response, duplicatedResponse);
    }

    /**
     * Tests syncing packages using vendor match strategy asynchronously.
     * Repeats same setup as {@link SyncFromVendorServiceImplTest#testSyncWhenPackagesOnly}.
     */
    @Test
    public void testSyncWhenPackagesOnlyAsynchronously() {
        MessageQueueSpy mqSpy = new MessageQueueSpy();
        mqSpy.install();
        try {
            // Create vendor errata with packages
            Errata errata = new ErrataTestBuilder()
                    .orgId(user.getOrg().getId())
                    .buildAndSave();
            Package pkg1 = PackageTest.createTestPackage(userOrg);
            Package pkg2 = PackageTest.createTestPackage(userOrg);
            errata.addPackage(pkg1);
            errata.addPackage(pkg2);
            errata.setOrg(null);  // Vendor errata

            // Add one package to target
            targetChannel.addPackage(pkg1);

            // Sync asynchronously
            SyncRequest request = ChannelSoftwareTestUtils.createSyncRequestAsync(
                    SyncOperation.PACKAGES_ONLY, errata.getAdvisoryName()
            );
            SyncResponse response = service.sync(user, targetChannel.getLabel(), request);

            // Async response should be empty (work happens in background)
            assertNotNull(response);
            assertTrue(response.erratas().isEmpty());
            assertTrue(response.packages().isEmpty());

            // Verify event was published
            List<SyncFromVendorErrataEvent> events = mqSpy.getEvents(SyncFromVendorErrataEvent.class);
            assertEquals(1, events.size());
            assertEquals(SyncOperation.PACKAGES_ONLY, events.get(0).getSyncRequest().operation());
        }
        finally {
            mqSpy.uninstall();
        }
    }

    // ERRATA_AND_PACKAGES

    /**
     * Tests syncing both erratas and packages from vendor.
     * Verifies that both erratas and packages are cloned using vendor match strategy.
     */
    @Test
    public void testSyncWhenErrataAndPackages() {
        // Create a vendor errata with two packages
        Errata errata = new ErrataTestBuilder().orgId(user.getOrg().getId()).buildAndSave();
        Package pkg1 = PackageTest.createTestPackage(userOrg);
        Package pkg2 = PackageTest.createTestPackage(userOrg);
        errata.addPackage(pkg1);
        errata.addPackage(pkg2);
        errata.setOrg(null);  // Vendor errata

        // Setup target channel to have package matching pkg1's name+arch
        Package existingPkg1 = PackageTest.createTestPackage(userOrg);
        existingPkg1.setPackageName(pkg1.getPackageName());
        existingPkg1.setPackageArch(pkg1.getPackageArch());

        targetChannel.addPackage(existingPkg1);

        // Sync
        SyncRequest request = ChannelSoftwareTestUtils.createSyncRequest(SyncOperation.ERRATA_AND_PACKAGES);
        SyncResponse response = service.sync(user, targetChannel.getLabel(), request);
        SyncResponse duplicatedResponse = service.sync(user, targetChannel.getLabel(), request);

        assertNotNull(response);
        assertFalse(response.erratas().isEmpty());
        assertEquals(1, response.erratas().size());
        assertFalse(response.erratas().contains(errata));
        Errata syncErrata = response.erratas().iterator().next();
        assertTrue(syncErrata.isCloned());
        assertTrue(syncErrata.getCves().containsAll(errata.getCves()));
        assertTrue(syncErrata.getPackages().containsAll(errata.getPackages()));
        assertTrue(syncErrata.getAdvisoryName().contains(errata.getAdvisoryName()));

        // Verify only matching packages are included (vendor match strategy)
        assertFalse(response.packages().isEmpty());
        assertEquals(1, response.packages().size());
        assertTrue(response.packages().contains(pkg1));

        // Check the duplicated request response has same contents
        assertEquals(response, duplicatedResponse);
    }

    /**
     * Tests syncing both erratas and packages from vendor asynchronously.
     * Repeats same setup as {@link SyncFromVendorServiceImplTest#testSyncWhenErrataAndPackages}.
     */
    @Test
    public void testSyncWhenErrataAndPackagesAsynchronously() {
        MessageQueueSpy mqSpy = new MessageQueueSpy();
        mqSpy.install();
        try {
            // Create vendor errata
            Errata errata = new ErrataTestBuilder()
                    .orgId(user.getOrg().getId())
                    .buildAndSave();
            Package pkg1 = PackageTest.createTestPackage(userOrg);
            Package pkg2 = PackageTest.createTestPackage(userOrg);

            // Create a vendor errata with two packages
            errata.addPackage(pkg1);
            errata.addPackage(pkg2);
            errata.setOrg(null);  // Vendor errata

            // Setup target channel to have package matching pkg1's name+arch
            Package existingPkg1 = PackageTest.createTestPackage(userOrg);
            existingPkg1.setPackageName(pkg1.getPackageName());
            existingPkg1.setPackageArch(pkg1.getPackageArch());

            // Add one package to target
            targetChannel.addPackage(existingPkg1);

            // Sync asynchronously
            SyncRequest request = ChannelSoftwareTestUtils.createSyncRequestAsync(SyncOperation.ERRATA_AND_PACKAGES);
            SyncResponse response = service.sync(user, targetChannel.getLabel(), request);

            // Async response should be empty (work happens in background)
            assertNotNull(response);
            assertTrue(response.erratas().isEmpty());
            assertTrue(response.packages().isEmpty());

            // Verify event was published with correct parameters
            List<SyncFromVendorErrataEvent> events = mqSpy.getEvents(SyncFromVendorErrataEvent.class);
            assertEquals(1, events.size());
            assertEquals(SyncOperation.ERRATA_AND_PACKAGES, events.get(0).getSyncRequest().operation());
        }
        finally {
            mqSpy.uninstall();
        }
    }

    /**
     * Tests sync when no erratas are found in cascading org lookup.
     * Verifies that response contains empty sets.
     */
    @Test
    public void testSyncWhenErrataAndPackagesWhenNoErrataIsFound() {
        // Sync with non-existent advisory name
        SyncRequest request = ChannelSoftwareTestUtils.createSyncRequest(
                SyncOperation.ERRATA_AND_PACKAGES, "NON-EXISTENT-VENDOR"
        );
        SyncResponse response = service.sync(user, targetChannel.getLabel(), request);

        assertNotNull(response);
        assertTrue(response.erratas().isEmpty());
        assertTrue(response.packages().isEmpty());
    }

    /**
     * Tests sync when there are no packages to be handled.
     * Verifies that response contains empty sets.
     */
    @Test
    public void testSyncWhenErrataAndPackagesWhenNoPackagesIsFound() {
        // Create vendor errata
        Errata errata = new ErrataTestBuilder().orgId(user.getOrg().getId()).buildAndSave();
        errata.setOrg(null);

        // Sync
        SyncRequest request = ChannelSoftwareTestUtils.createSyncRequest(
                SyncOperation.ERRATA_AND_PACKAGES, errata.getAdvisoryName()
        );
        SyncResponse response = service.sync(user, targetChannel.getLabel(), request);

        assertNotNull(response);
        assertFalse(response.erratas().contains(errata));
        Errata syncErrata = response.erratas().iterator().next();
        assertTrue(syncErrata.isCloned());
        assertTrue(syncErrata.getCves().containsAll(errata.getCves()));
        assertTrue(syncErrata.getPackages().containsAll(errata.getPackages()));
        assertTrue(syncErrata.getAdvisoryName().contains(errata.getAdvisoryName()));
        assertTrue(response.packages().isEmpty());
    }


}
