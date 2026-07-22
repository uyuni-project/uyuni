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

import static com.redhat.rhn.common.ExceptionMessage.NOT_INSTANTIABLE;
import static com.suse.utils.Predicates.allProvided;
import static com.suse.utils.Predicates.isAbsent;

import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.UyuniErrorReport;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchChannelException;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.user.UserManager;

import com.suse.spec.channel.software.dto.SyncRequest;

import java.util.Date;

/**
 * Validation helper for channel software operations.
 * Centralizes validation of channels, permissions, and business rules that
 * can throw Exceptions.
 */
public class ChannelSoftwareValidationHelper {

    /**
     * Validates and looks up a channel by label for the given user.
     *
     * @param user The user performing the operation
     * @param channelLabel The channel label to look up
     * @return The validated channel
     * @throws NoSuchChannelException if the channel doesn't exist or user cannot access it
     */
    public static Channel validateAndLookupChannel(User user, String channelLabel) {
        Channel channel = ChannelFactory.lookupByLabelAndUser(channelLabel, user);
        if (channel == null) {
            throw new NoSuchChannelException(channelLabel);
        }
        return channel;
    }

    /**
     * Validates that the user has admin permission for the given channel.
     *
     * @param user The user to validate
     * @param channel The channel to check permission for
     * @throws PermissionCheckFailureException if the user lacks channel admin permission
     */
    public static void validateUserHasPermission(User user, Channel channel) {
        if (!UserManager.verifyChannelAdmin(user, channel)) {
            throw new PermissionCheckFailureException(
                    "User does not have permission to modify channel: " + channel.getLabel()
            );
        }
    }

    /**
     * Validates that a channel is a cloned.
     *
     * @param channel The channel to validate
     * @throws InvalidChannelException if the channel is not cloned
     */
    public static void validateChannelIsCloned(Channel channel) {
        if (!channel.isCloned()) {
            throw new InvalidChannelException(
                    "Target channel must be a cloned channel. " +
                    "Channel '" + channel.getLabel() + "' is not cloned."
            );
        }
    }

    /**
     * Validates that the original channel for a cloned channel is accessible.
     *
     * @param originalChannel The original channel to validate
     * @param targetChannelLabel The label of the target channel (for error message)
     * @throws InvalidChannelException if the original channel is not accessible
     */
    public static void validateOriginalChannelAccessible(Channel originalChannel, String targetChannelLabel) {
        if (originalChannel == null) {
            throw new InvalidChannelException("Cannot access original channel for: " + targetChannelLabel);
        }
    }

    /**
     * Validates request field-level constraints.
     * Aggregates all field errors into a UyuniErrorReport.
     *
     * @param targetChannelLabel Target channel label
     * @param sourceChannelLabel Source channel label (can be null if not required)
     * @param syncRequest SyncRequest containing criteria and operation details
     * @param requiresSourceChannel Whether source channel is required for this operation
     * @return UyuniErrorReport with all field validation errors
     */
    public static UyuniErrorReport validateRequestFields(
            String targetChannelLabel,
            String sourceChannelLabel,
            SyncRequest syncRequest,
            boolean requiresSourceChannel
    ) {
        UyuniErrorReport errorReport = new UyuniErrorReport();

        // Required field validation
        if (isAbsent(targetChannelLabel)) {
            errorReport.register("Target channel label is required");
        }

        if (requiresSourceChannel && isAbsent(sourceChannelLabel)) {
            errorReport.register("Source channel label is required");
        }

        // syncRequest internal validation, throwing exception not to register errors back to the user
        if (isAbsent(syncRequest)) {
            throw new RhnRuntimeException("SyncRequest not provided");
        }
        else {
            if (isAbsent(syncRequest.operation())) {
                throw new RhnRuntimeException("SyncOperation not provided");
            }
            if (isAbsent(syncRequest.criteria())) {
                throw new RhnRuntimeException("ErrataCriteria not provided");
            }

            Date startDate = syncRequest.criteria().startDate();
            Date endDate = syncRequest.criteria().endDate();
            // Date range validation
            if (allProvided(startDate, endDate) && endDate.before(startDate)) {
                errorReport.register("End date cannot be before start date");
            }
        }

        return errorReport;
    }

    /**
     * Validates that the channel does not have pending async jobs
     * @param channel the channel to check
     */
    public static void validateChannelHasNoPendingAsyncCloneJobs(Channel channel) {
        if (ErrataManager.channelHasPendingAsyncCloneJobs(channel)) {
            throw new IllegalStateException(
                    "Channel " + channel.getLabel() +
                    " has pending asynchronous errata clone jobs. " +
                    "You must wait until asynchronous errata clone jobs are done.");
        }
    }

    private ChannelSoftwareValidationHelper() {
        throw new UnsupportedOperationException(NOT_INSTANTIABLE);
    }

}
