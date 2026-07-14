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
package com.redhat.rhn.frontend.xmlrpc.channel.access;

import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link ChannelAccessHandler}.
 */
@Tag(name = "channel.access", description = "Provides methods to retrieve and alter channel access restrictions.")
public interface ChannelAccessHandlerApi {

    /**
     * Enable user restrictions for the given channel. If enabled, only
     * selected users within the organization may subscribe to the channel.
     *
     * @param loggedInUser the current user
     * @param channelLabel the label of the channel to change
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Enable user restrictions for the given channel. If enabled, only " +
                  "selected users within the organization may subscribe to the channel.",
        requestClass = ChannelLabelRequest.class,
        isIntegerResponse = true
    )
    int enableUserRestrictions(User loggedInUser, String channelLabel);

    /**
     * Disable user restrictions for the given channel. If disabled,
     * all users within the organization may subscribe to the channel.
     *
     * @param loggedInUser the current user
     * @param channelLabel the label of the channel to change
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Disable user restrictions for the given channel. If disabled, " +
                  "all users within the organization may subscribe to the channel.",
        requestClass = ChannelLabelRequest.class,
        isIntegerResponse = true
    )
    int disableUserRestrictions(User loggedInUser, String channelLabel);

    /**
     * Set organization sharing access control.
     *
     * @param loggedInUser the current user
     * @param channelLabel the label of the channel to change
     * @param access the access value to set (one of 'public', 'private' or 'protected')
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set organization sharing access control.",
        requestClass = SetOrgSharingRequest.class,
        isIntegerResponse = true
    )
    int setOrgSharing(User loggedInUser, String channelLabel, String access);

    /**
     * Get organization sharing access control.
     *
     * @param loggedInUser the current user
     * @param channelLabel the label of the channel
     * @return the access value
     */
    @ApiEndpointDoc(
        summary = "Get organization sharing access control.",
        method = HttpMethod.get,
        responseClass = OrgSharingResponse.class,
        responseDescription = "The access value (one of the following: 'public', 'private', or 'protected')",
        legacyDocResponse = @LegacyDocResponse(
            type = "string",
            name = "access"
        )
    )
    String getOrgSharing(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(
            name = "channelLabel",
            description = "label of the channel",
            in = ParameterIn.QUERY,
            required = true
        ) String channelLabel
    );

    @Schema(name = "ChannelLabelRequest")
    interface ChannelLabelRequest {
        /**
         * @return label of the channel
         */
        @Schema(description = "label of the channel", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();
    }

    @Schema(name = "SetOrgSharingRequest")
    @JsonPropertyOrder({"channelLabel", "access"})
    interface SetOrgSharingRequest {
        /**
         * @return label of the channel
         */
        @Schema(description = "label of the channel", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();

        /**
         * @return access (one of the following: 'public', 'private' or 'protected')
         */
        @Schema(description = "access (one of the following: 'public', 'private' or 'protected')",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getAccess();
    }

    @Schema(name = "ApiResponseOrgSharing")
    interface OrgSharingResponse extends ApiResponseWrapper<String> { }
}
