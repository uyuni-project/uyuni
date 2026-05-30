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
import com.redhat.rhn.domain.channel.InvalidChannelRoleException;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.errata.ErrataTestBuilder;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.events.SyncFromOriginalErrataEvent;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchChannelException;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.MessageQueueSpy;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.spec.channel.software.dto.SyncOperation;
import com.suse.spec.channel.software.dto.SyncRequest;
import com.suse.spec.channel.software.dto.SyncResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

/**
 * Tests for SyncFromOriginalServiceImpl
 */
public class SyncFromOriginalServiceImplTest extends BaseTestCaseWithUser {

    private SyncFromOriginalServiceImpl service;
    private Channel originalChannel;
    private Channel clonedChannel;
    private Org userOrg;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        service = new SyncFromOriginalServiceImpl();
        originalChannel = ChannelFactoryTest.createTestChannel(user);
        clonedChannel = ChannelFactoryTest.createTestClonedChannel(originalChannel, user);
        userOrg = user.getOrg();
    }

    /**
     * Tests that sync with invalid target channel label throws NoSuchChannelException.
     */
    @Test
    public void testFailSyncWhenInvalidSourceChannel() {
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
    public void testFailSyncWhenInvalidTargetChannel() {
        // Create a user in same org but without channel admin permissions
        User otherUser = new UserTestUtils.UserBuilder().orgId(userOrg.getId()).build();
        SyncRequest request = ChannelSoftwareTestUtils.createSyncRequest(SyncOperation.ERRATA_ONLY);

        PermissionCheckFailureException exception = assertThrows(PermissionCheckFailureException.class, () ->
            service.sync(otherUser, clonedChannel.getLabel(), request)
        );
        assertTrue(exception.getMessage().contains("User does not have permission to modify channel"));
    }

    /**
     * Tests that sync with non-cloned channel throws InvalidChannelException.
     * SyncFromOriginal requires the target channel to be cloned.
     */
    @Test
    public void testSyncWhenNonClonedChannel() {
        SyncRequest request = ChannelSoftwareTestUtils.createSyncRequest(SyncOperation.ERRATA_ONLY);

        InvalidChannelException exception = assertThrows(InvalidChannelException.class, () ->
            service.sync(user, originalChannel.getLabel(), request)
        );
        assertTrue(exception.getMessage().contains("Target channel must be a cloned channel."));
    }

    /**
     * Tests that sync with invalid original channel label throws InvalidChannelException.
     */
    @Test
    public void testFailSyncWhenInvalidOriginalChannel() throws InvalidChannelRoleException, TaskomaticApiException {
        // Remove original channel
        ChannelManager.deleteChannel(user, originalChannel.getLabel(), true);

        SyncRequest request = ChannelSoftwareTestUtils.createSyncRequest(SyncOperation.ERRATA_ONLY);

        InvalidChannelException exception = assertThrows(InvalidChannelException.class, () ->
                service.sync(user, clonedChannel.getLabel(), request)
        );
        assertTrue(exception.getMessage().contains("Cannot access original channel"));
    }

    // ERRATA_ONLY

    /**
     * Tests syncing erratas only from original to cloned channel.
     * Verifies it only handles erratas.
     */
    @Test
    public void testSyncWhenErrataOnly() {
        // Create errata in original channel
        Errata errata = new ErrataTestBuilder().orgId(user.getOrg().getId()).buildAndSave();
        Package pkg = PackageTest.createTestPackage(user.getOrg());
        errata.addPackage(pkg);
        ErrataFactory.addToChannel(errata, originalChannel, user, Set.of(pkg));

        // Sync
        SyncRequest request = ChannelSoftwareTestUtils.createSyncRequest(SyncOperation.ERRATA_ONLY);
        SyncResponse response = service.sync(user, clonedChannel.getLabel(), request);
        SyncResponse duplicatedResponse = service.sync(user, clonedChannel.getLabel(), request);

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

    // PACKAGES_ONLY

    /**
     * Tests syncing packages only from original channel hierarchy.
     * Verifies it only handles packages.
     */
    @Test
    public void testSyncWhenPackagesOnly() {
        // Create errata with packages in original channel
        Errata errata = new ErrataTestBuilder().orgId(user.getOrg().getId()).buildAndSave();
        Package pkg1 = PackageTest.createTestPackage(user.getOrg());
        Package pkg2 = PackageTest.createTestPackage(user.getOrg());
        errata.addPackage(pkg1);
        errata.addPackage(pkg2);
        ErrataFactory.addToChannel(errata, originalChannel, user, Set.of(pkg1, pkg2));

        // Sync
        SyncRequest request = ChannelSoftwareTestUtils.createSyncRequest(
                SyncOperation.PACKAGES_ONLY, errata.getAdvisoryName()
        );
        SyncResponse response = service.sync(user, clonedChannel.getLabel(), request);
        SyncResponse duplicatedResponse = service.sync(user, clonedChannel.getLabel(), request);

        // Assert first request only handles expected package
        assertNotNull(response);
        assertTrue(response.erratas().isEmpty());
        assertEquals(2, response.packages().size());
        assertTrue(response.packages().contains(pkg1));
        assertTrue(response.packages().contains(pkg2));

        // Assert duplicates of response will be the same
        assertEquals(response, duplicatedResponse);
    }

    // ERRATA_AND_PACKAGES

    /**
     * Tests syncing both erratas and packages from original channel hierarchy.
     * Verifies that:
     * - both erratas and packages handled
     * - repeating the sync will not clone again neither erratas nor packages
     */
    @Test
    public void testSyncWhenErrataAndPackages() {
        // Create errata with packages in original channel
        Errata errata = new ErrataTestBuilder().orgId(user.getOrg().getId()).buildAndSave();
        Package pkg1 = PackageTest.createTestPackage(user.getOrg());
        Package pkg2 = PackageTest.createTestPackage(user.getOrg());
        errata.addPackage(pkg1);
        errata.addPackage(pkg2);
        ErrataFactory.addToChannel(errata, originalChannel, user, Set.of(pkg1, pkg2));

        // Sync
        SyncRequest request = ChannelSoftwareTestUtils.createSyncRequest(SyncOperation.ERRATA_AND_PACKAGES);
        SyncResponse response = service.sync(user, clonedChannel.getLabel(), request);
        SyncResponse duplicatedResponse = service.sync(user, clonedChannel.getLabel(), request);

        assertNotNull(response);
        assertEquals(1, response.erratas().size());
        // Returning errata is a cloned version
        assertFalse(response.erratas().contains(errata));
        Errata syncErrata = response.erratas().iterator().next();
        assertTrue(syncErrata.isCloned());
        assertTrue(syncErrata.getCves().containsAll(errata.getCves()));
        assertTrue(syncErrata.getPackages().containsAll(errata.getPackages()));
        assertTrue(syncErrata.getAdvisoryName().contains(errata.getAdvisoryName()));

        assertEquals(2, response.packages().size());
        assertTrue(response.packages().contains(pkg1));
        assertTrue(response.packages().contains(pkg2));

        // Check the duplicated request response has same contents
        assertEquals(response, duplicatedResponse);
    }

    /**
     * Tests syncing asynchronously.
     * Repeats same setup as {@link SyncFromOriginalServiceImplTest#testSyncWhenErrataAndPackages}.
     */
    @Test
    public void testSyncWhenErrataAndPackagesAsynchronously() {
        MessageQueueSpy mqSpy = new MessageQueueSpy();
        mqSpy.install();
        try {
            // Create errata with packages in original channel
            Errata errata = new ErrataTestBuilder().orgId(user.getOrg().getId()).buildAndSave();
            Package pkg1 = PackageTest.createTestPackage(user.getOrg());
            Package pkg2 = PackageTest.createTestPackage(user.getOrg());
            errata.addPackage(pkg1);
            errata.addPackage(pkg2);
            ErrataFactory.addToChannel(errata, originalChannel, user, Set.of(pkg1, pkg2));

            // Sync asynchronously
            SyncRequest request = ChannelSoftwareTestUtils.createSyncRequestAsync(SyncOperation.ERRATA_AND_PACKAGES);
            SyncResponse response = service.sync(user, clonedChannel.getLabel(), request);
            SyncResponse duplicatedResponse = service.sync(user, clonedChannel.getLabel(), request);

            // Async response should be empty (work happens in background)
            assertNotNull(response);
            assertTrue(response.erratas().isEmpty());
            assertTrue(response.packages().isEmpty());

            // Assert duplicates of response will be the same in async
            assertEquals(response, duplicatedResponse);

            // Assert published events
            List<SyncFromOriginalErrataEvent> events = mqSpy.getEvents(SyncFromOriginalErrataEvent.class);
            assertEquals(2, events.size());
            assertEquals(1, events.stream().distinct().count());
            assertFalse(events.iterator().next().getSyncRequest().async());
        }
        finally {
            mqSpy.uninstall();
        }
    }

    /**
     * Tests syncing packages from original that happens to be also a cloned channel.
     * Verifies that the code correctly traverses the clone hierarchy to find the errata.
     */
    @Test
    public void testSyncWhenPackagesFromDoubleClonedChannel() {
        // Create errata with packages in the original channel
        Errata errata = new ErrataTestBuilder().orgId(user.getOrg().getId()).buildAndSave();
        Package pkg1 = PackageTest.createTestPackage(user.getOrg());
        Package pkg2 = PackageTest.createTestPackage(user.getOrg());
        errata.addPackage(pkg1);
        errata.addPackage(pkg2);
        ErrataFactory.addToChannel(errata, originalChannel, user, Set.of(pkg1, pkg2));

        // Create a double-cloned channel: original -> intermediate -> doubleClone
        Channel doubleClonedChannel = ChannelFactoryTest.createTestClonedChannel(clonedChannel, user);

        // Sync packages to the double-cloned channel
        SyncRequest request = ChannelSoftwareTestUtils.createSyncRequest(
                SyncOperation.ERRATA_AND_PACKAGES, errata.getAdvisoryName()
        );
        SyncResponse response = service.sync(user, doubleClonedChannel.getLabel(), request);

        // Verify both errata and packages were synced
        assertNotNull(response);
        assertFalse(response.erratas().isEmpty());
        assertEquals(1, response.erratas().size());

        // Verify packages were inherited through the clone hierarchy
        assertFalse(response.packages().isEmpty());
        assertEquals(2, response.packages().size());
        assertTrue(response.packages().contains(pkg1));
        assertTrue(response.packages().contains(pkg2));
    }

    /**
     * Tests syncing both erratas and packages from original channel when no errata is matched
     * Verifies it returns empty sets
     */
    @Test
    public void testSyncWhenErrataAndPackagesWhenNoErrataIsFound()  {
        SyncRequest request = ChannelSoftwareTestUtils.createSyncRequest(SyncOperation.ERRATA_AND_PACKAGES);
        SyncResponse response = service.sync(user, clonedChannel.getLabel(), request);

        assertNotNull(response);
        assertTrue(response.erratas().isEmpty());
        assertTrue(response.packages().isEmpty());
    }

    /**
     * Tests syncing both erratas and packages from original channel when there are no packages
     * to be handled.
     * Verifies it returns empty sets
     */
    @Test
    public void testSyncWhenErrataAndPackagesWhenNoPackagesIsFound() {
        // Create errata without packages in original channel
        Errata errata = new ErrataTestBuilder().orgId(user.getOrg().getId()).buildAndSave();
        originalChannel.addErrata(errata);

        SyncRequest request = ChannelSoftwareTestUtils.createSyncRequest(SyncOperation.ERRATA_AND_PACKAGES);
        SyncResponse response = service.sync(user, clonedChannel.getLabel(), request);

        assertNotNull(response);
        // Returning errata is a cloned version
        assertFalse(response.erratas().contains(errata));
        Errata syncErrata = response.erratas().iterator().next();
        assertTrue(syncErrata.isCloned());
        assertTrue(syncErrata.getCves().containsAll(errata.getCves()));
        assertTrue(syncErrata.getPackages().containsAll(errata.getPackages()));
        assertTrue(syncErrata.getAdvisoryName().contains(errata.getAdvisoryName()));

        assertTrue(response.packages().isEmpty());
    }
}
