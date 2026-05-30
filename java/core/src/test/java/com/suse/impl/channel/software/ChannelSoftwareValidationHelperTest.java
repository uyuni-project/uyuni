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

import static com.redhat.rhn.domain.role.RoleFactory.ORG_ADMIN;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.UyuniErrorReport;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactoryTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchChannelException;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.manager.errata.AsyncErrataCloneCounter;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.spec.channel.software.dto.ErrataCriteria;
import com.suse.spec.channel.software.dto.SyncOperation;
import com.suse.spec.channel.software.dto.SyncRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;

/**
 * Tests for ChannelSoftwareValidationHelper.
 */
class ChannelSoftwareValidationHelperTest extends BaseTestCaseWithUser {

    private User admin;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        admin = UserTestUtils.createUser();
        admin.addPermanentRole(RoleFactory.CHANNEL_ADMIN);
        UserTestUtils.addUserRole(admin, RoleFactory.CHANNEL_ADMIN);
    }

    //validateAndLookupChannel
    @Test
    void testValidateAndLookupChannelValidChannel() {
        Channel channel = ChannelFactoryTest.createTestChannel(admin);

        Channel result = ChannelSoftwareValidationHelper.validateAndLookupChannel(admin, channel.getLabel());

        assertNotNull(result);
        assertEquals(channel.getId(), result.getId());
    }

    @Test
    void testValidateAndLookupChannelInvalidLabel() {
        assertThrows(NoSuchChannelException.class, () ->
            ChannelSoftwareValidationHelper.validateAndLookupChannel(admin, "non-existent-channel")
        );
    }

    // validateUserHasPermission
    @Test
    void testValidateUserHasPermissionWithPermission() {
        Channel channel = ChannelFactoryTest.createTestChannel(admin);
        admin.addPermanentRole(ORG_ADMIN);

        // Should not throw
        ChannelSoftwareValidationHelper.validateUserHasPermission(admin, channel);
    }

    @Test
    void testValidateUserHasPermissionNoPermission() {
        Channel channel = ChannelFactoryTest.createTestChannel(admin);

        assertThrows(PermissionCheckFailureException.class, () ->
            ChannelSoftwareValidationHelper.validateUserHasPermission(user, channel)
        );
    }

    // validateChannelIsCloned
    @Test
    void testValidateChannelIsClonedClonedChannel() {
        Channel original = ChannelFactoryTest.createTestChannel(admin);
        Channel cloned = ChannelFactoryTest.createTestClonedChannel(original, admin);

        // Should not throw
        ChannelSoftwareValidationHelper.validateChannelIsCloned(cloned);
    }

    @Test
    void testValidateChannelIsCloned() {
        Channel channel = ChannelFactoryTest.createTestChannel(admin);

        InvalidChannelException ex = assertThrows(InvalidChannelException.class, () ->
            ChannelSoftwareValidationHelper.validateChannelIsCloned(channel)
        );
        assertTrue(ex.getMessage().contains("is not cloned"));
    }

    // validateOriginalChannelAccessible
    @Test
    void testValidateOriginalChannelAccessibleValidOriginal() {
        Channel original = ChannelFactoryTest.createTestChannel(admin);

        // Should not throw
        ChannelSoftwareValidationHelper.validateOriginalChannelAccessible(original, null);
    }

    @Test
    void testValidateOriginalChannelAccessibleNullOriginal() {
        InvalidChannelException ex = assertThrows(InvalidChannelException.class, () ->
            ChannelSoftwareValidationHelper.validateOriginalChannelAccessible(null, "target-label")
        );
        assertEquals("Cannot access original channel for: target-label", ex.getMessage());
    }

    // validateRequestFields


    @Test
    void testValidateRequestFieldsWithoutSyncRequest() {
        RhnRuntimeException exception = assertThrows(RhnRuntimeException.class, () ->
                ChannelSoftwareValidationHelper.validateRequestFields(
                        TestUtils.randomString(),
                        null,
                        null,
                        false
                )
        );

        assertEquals("SyncRequest not provided", exception.getMessage());
    }

    @Test
    void testValidateRequestFieldsWithoutSyncOperation() {
        RhnRuntimeException exception = assertThrows(RhnRuntimeException.class, () ->
                ChannelSoftwareValidationHelper.validateRequestFields(
                        TestUtils.randomString(),
                        null,
                        new SyncRequest(
                                new ErrataCriteria(null, null, null),
                                null,
                                false,
                                false,
                                false
                        ),
                        false
                )
        );

        assertEquals("SyncOperation not provided", exception.getMessage());
    }

    @Test
    void testValidateRequestFieldsWithoutErrataCriteria() {
        RhnRuntimeException exception = assertThrows(RhnRuntimeException.class, () ->
                ChannelSoftwareValidationHelper.validateRequestFields(
                        TestUtils.randomString(),
                        null,
                        new SyncRequest(
                                null,
                                SyncOperation.ERRATA_AND_PACKAGES,
                                false,
                                false,
                                false
                        ),
                        false
                )
        );

        assertEquals("ErrataCriteria not provided", exception.getMessage());
    }


    @Test
    void testValidateRequestFieldsWithMinimalData() {
        SyncRequest syncRequest = new SyncRequest(
                new ErrataCriteria(null, null, null),
                SyncOperation.ERRATA_AND_PACKAGES,
                false,
                false,
                false
        );

        UyuniErrorReport report = ChannelSoftwareValidationHelper.validateRequestFields(
                TestUtils.randomString(),
                null,
                syncRequest,
                false
        );

        assertFalse(report.hasErrors());
    }

    @Test
    void testValidateRequestFieldsWillFullData() {
        Instant now = Instant.now();
        SyncRequest syncRequest = new SyncRequest(
                new ErrataCriteria(
                        emptyList(),
                        Date.from(now.minusSeconds(1)),
                        Date.from(now)),
                SyncOperation.ERRATA_AND_PACKAGES,
                false,
                false,
                false
        );
        UyuniErrorReport report = ChannelSoftwareValidationHelper.validateRequestFields(
                TestUtils.randomString(),
                TestUtils.randomString(),
                syncRequest,
                true
        );

        assertFalse(report.hasErrors());
    }

    @Test
    void testValidateRequestFieldsWhenAllUserValidationsFail() {
        Instant now = Instant.now();
        SyncRequest syncRequest = new SyncRequest(
                new ErrataCriteria(
                        emptyList(),
                        Date.from(now),
                        Date.from(now.minusSeconds(1))
                ),
                SyncOperation.ERRATA_AND_PACKAGES,
                false,
                false,
                false
        );
        UyuniErrorReport report = ChannelSoftwareValidationHelper.validateRequestFields(
                null,
                null,
                syncRequest,
                true
        );

        assertTrue(report.hasErrors());
        String[] errorMessages = report.getErrorMessages();
        assertEquals(3,  errorMessages.length);
        assertTrue(Arrays.stream(errorMessages).anyMatch(s -> s.contains("Target channel label is required")));
        assertTrue(Arrays.stream(errorMessages).anyMatch(s -> s.contains("Source channel label is required")));
        assertTrue(Arrays.stream(errorMessages).anyMatch(s -> s.contains("End date cannot be before start date")));
    }

    // validateChannelHasNoPendingAsyncCloneJobs
    @Test
    void testValidateChannelHasNoPendingAsyncCloneJobsWhenNoPendingJobs() {
        Channel channel = ChannelFactoryTest.createTestChannel(admin);
        ChannelSoftwareValidationHelper.validateChannelHasNoPendingAsyncCloneJobs(channel);
    }

    @Test
    void testValidateChannelHasNoPendingAsyncCloneJobsWhenHasPendingJobs() {
        Channel channel = ChannelFactoryTest.createTestChannel(admin);

        // Simulate pending async job
        AsyncErrataCloneCounter.getInstance().addAsyncErrataCloneJob(channel.getId());

        try {
            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                ChannelSoftwareValidationHelper.validateChannelHasNoPendingAsyncCloneJobs(channel)
            );
            assertTrue(ex.getMessage().contains("has pending asynchronous errata clone jobs"));
            assertTrue(ex.getMessage().contains(channel.getLabel()));
        }
        finally {
            AsyncErrataCloneCounter.getInstance().removeAsyncErrataCloneJob(channel.getId());
        }
    }

    @Test
    void testValidateChannelHasNoPendingAsyncCloneJobsAfterJobCompletion() {
        Channel channel = ChannelFactoryTest.createTestChannel(admin);

        // Simulate pending async job
        AsyncErrataCloneCounter.getInstance().addAsyncErrataCloneJob(channel.getId());

        // "Complete" the job
        AsyncErrataCloneCounter.getInstance().removeAsyncErrataCloneJob(channel.getId());

        ChannelSoftwareValidationHelper.validateChannelHasNoPendingAsyncCloneJobs(channel);
    }


}
