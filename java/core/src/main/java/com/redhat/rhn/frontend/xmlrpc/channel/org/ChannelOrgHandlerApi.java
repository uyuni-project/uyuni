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
package com.redhat.rhn.frontend.xmlrpc.channel.org;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * API contract for {@link ChannelOrgHandler}.
 */
@Tag(name = "channel.org", description = "Provides methods to retrieve and alter organization trust " +
        "settings for a channel.")
public interface ChannelOrgHandlerApi {

    /**
     * Lists the organizations associated with the given channel.
     *
     * @param loggedInUser the current user
     * @param label label of the channel
     * @return the organizations associated with the channel
     */
    @ApiEndpointDoc(
        summary = "List the organizations associated with the given channel that may be trusted.",
        requestClass = ChannelOrgLabelRequest.class,
        responseClass = OrgListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "org")
    )
    List list(User loggedInUser, String label);

    /**
     * Enables access to the channel for the given organization.
     *
     * @param loggedInUser the current user
     * @param label label of the channel
     * @param orgId ID of the org being granted access
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Enable access to the channel for the given organization.",
        requestClass = ChannelOrgEnableAccessRequest.class,
        isIntegerResponse = true
    )
    int enableAccess(User loggedInUser, String label, Integer orgId);

    /**
     * Disables access to the channel for the given organization.
     *
     * @param loggedInUser the current user
     * @param label label of the channel
     * @param orgId ID of the org being removed access
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Disable access to the channel for the given organization.",
        requestClass = ChannelOrgDisableAccessRequest.class,
        isIntegerResponse = true
    )
    int disableAccess(User loggedInUser, String label, Integer orgId);

    @Schema(name = "ChannelOrgLabelRequest")
    interface ChannelOrgLabelRequest {

        /**
         * @return label of the channel
         */
        @Schema(description = "label of the channel", requiredMode = REQUIRED)
        String getLabel();
    }

    @Schema(name = "ChannelOrgEnableAccessRequest")
    @JsonPropertyOrder({"label", "orgId"})
    interface ChannelOrgEnableAccessRequest {

        /**
         * @return label of the channel
         */
        @Schema(description = "label of the channel", requiredMode = REQUIRED)
        String getLabel();

        /**
         * @return ID of the org
         */
        @Schema(description = "ID of org being granted access", requiredMode = REQUIRED)
        Integer getOrgId();
    }

    @Schema(name = "ChannelOrgDisableAccessRequest")
    @JsonPropertyOrder({"label", "orgId"})
    interface ChannelOrgDisableAccessRequest {

        /**
         * @return label of the channel
         */
        @Schema(description = "label of the channel", requiredMode = REQUIRED)
        String getLabel();

        /**
         * @return ID of the org
         */
        @Schema(description = "ID of org being removed access", requiredMode = REQUIRED)
        Integer getOrgId();
    }

    @Schema(name = "ChannelOrg", description = "org")
    @JsonPropertyOrder({"orgId", "orgName", "accessEnabled"})
    interface ChannelOrgDoc {

        /**
         * @return the organization identifier
         */
        @Schema(name = "org_id", requiredMode = REQUIRED)
        Integer getOrgId();

        /**
         * @return the organization name
         */
        @Schema(name = "org_name", requiredMode = REQUIRED)
        String getOrgName();

        /**
         * @return whether access is enabled
         */
        @Schema(name = "access_enabled", requiredMode = REQUIRED)
        Boolean getAccessEnabled();
    }

    @Schema(name = "ApiResponseChannelOrgList")
    interface OrgListResponse extends ApiResponseWrapper<List<ChannelOrgDoc>> { }
}
