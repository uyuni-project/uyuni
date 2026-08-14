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
package com.redhat.rhn.frontend.xmlrpc.org.trusts;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.OrgTrustOverview;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link OrgTrustHandler}.
 */
@Tag(name = "org.trusts", description = "Provides methods to retrieve and alter organization trusts.")
public interface OrgTrustHandlerApi {

    /**
     * Lists the organizations that may be trusted.
     *
     * @param loggedInUser the current user
     * @return the organizations that may be trusted
     */
    @ApiEndpointDoc(
        summary = "List all organizations trusted by the user's organization.",
        method = HttpMethod.get,
        responseClass = TrustedOrgListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "trusted organizations")
    )
    Object[] listOrgs(User loggedInUser);

    /**
     * Lists the channels provided by the given organization.
     *
     * @param loggedInUser the current user
     * @param orgId the trusted organization identifier
     * @return the channels provided
     */
    @ApiEndpointDoc(
        summary = "Lists all software channels that the organization given may consume.",
        method = HttpMethod.get,
        responseClass = ChannelInfoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel info")
    )
    Object[] listChannelsProvided(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "orgId", description = "Id of the trusted organization",
            in = ParameterIn.QUERY, required = true) Integer orgId
    );

    /**
     * Lists the channels consumed by the given organization.
     *
     * @param loggedInUser the current user
     * @param orgId the trusted organization identifier
     * @return the channels consumed
     */
    @ApiEndpointDoc(
        summary = "Lists all software channels that the organization given is consuming.",
        method = HttpMethod.get,
        responseClass = ChannelInfoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel info")
    )
    Object[] listChannelsConsumed(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "orgId", description = "Id of the trusted organization",
            in = ParameterIn.QUERY, required = true) Integer orgId
    );

    /**
     * Returns the trust details of the given organization.
     *
     * @param loggedInUser the current user
     * @param orgId the trusted organization identifier
     * @return the trust details
     */
    @ApiEndpointDoc(
        summary = "The trust details about an organization given the organization's ID.",
        method = HttpMethod.get,
        responseClass = OrgTrustDetailsResponse.class
    )
    Map<String, Object> getDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "orgId", description = "Id of the trusted organization",
            in = ParameterIn.QUERY, required = true) Integer orgId
    );

    /**
     * Lists the trusts of the given organization.
     *
     * @param loggedInUser the current user
     * @param orgId the organization identifier
     * @return the trusts
     */
    @ApiEndpointDoc(
        summary = "Returns the list of trusted organizations.",
        method = HttpMethod.get,
        responseClass = OrgTrustListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "trusted organizations")
    )
    List<OrgTrustOverview> listTrusts(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "orgId", description = "Id of the organization",
            in = ParameterIn.QUERY, required = true) Integer orgId
    );

    /**
     * Adds a trust between two organizations.
     *
     * @param loggedInUser the current user
     * @param orgId the organization identifier
     * @param trustOrgId the organization identifier to trust
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Add an organization to the list of trusted organizations.",
        requestClass = OrgTrustRequest.class,
        isIntegerResponse = true
    )
    int addTrust(User loggedInUser, Integer orgId, Integer trustOrgId);

    /**
     * Removes a trust between two organizations.
     *
     * @param loggedInUser the current user
     * @param orgId the organization identifier
     * @param trustOrgId the organization identifier to remove the trust from
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Remove an organization from the list of trusted organizations.",
        requestClass = OrgTrustRequest.class,
        isIntegerResponse = true
    )
    int removeTrust(User loggedInUser, Integer orgId, Integer trustOrgId);

    /**
     * Lists the systems affected by a trust change.
     *
     * @param loggedInUser the current user
     * @param orgId the organization identifier
     * @param trustOrgId the trusted organization identifier
     * @return the affected systems
     */
    @ApiEndpointDoc(
        summary = "Get a list of systems within the trusted organization that would be affected " +
                "if the trust relationship was removed.",
        method = HttpMethod.get,
        responseClass = AffectedSystemListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "affected systems")
    )
    List<Map<String, Object>> listSystemsAffected(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "orgId", description = "Id of the organization",
            in = ParameterIn.QUERY, required = true) Integer orgId,
        @Parameter(name = "trustOrgId", description = "Id of the trusted organization",
            in = ParameterIn.QUERY, required = true) Integer trustOrgId
    );

    @Schema(name = "OrgTrustRequest")
    @JsonPropertyOrder({"orgId", "trustOrgId"})
    interface OrgTrustRequest {

        /**
         * @return the organization identifier
         */
        @Schema(requiredMode = REQUIRED)
        Integer getOrgId();

        /**
         * @return the trusted organization identifier
         */
        @Schema(requiredMode = REQUIRED)
        Integer getTrustOrgId();
    }

    @Schema(name = "TrustedOrg", description = "trusted organizations")
    @JsonPropertyOrder({"orgId", "orgName", "sharedChannels"})
    interface TrustedOrgDoc {

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
         * @return the number of shared channels
         */
        @Schema(name = "shared_channels", requiredMode = REQUIRED)
        Integer getSharedChannels();
    }

    @Schema(name = "TrustChannelInfo", description = "channel info")
    @JsonPropertyOrder({"channelId", "channelName", "packages", "systems"})
    interface ChannelInfoDoc {

        /**
         * @return the channel identifier
         */
        @Schema(name = "channel_id", requiredMode = REQUIRED)
        Integer getChannelId();

        /**
         * @return the channel name
         */
        @Schema(name = "channel_name", requiredMode = REQUIRED)
        String getChannelName();

        /**
         * @return the number of packages
         */
        @Schema(requiredMode = REQUIRED)
        Integer getPackages();

        /**
         * @return the number of systems
         */
        @Schema(requiredMode = REQUIRED)
        Integer getSystems();
    }

    @Schema(name = "OrgTrustDetails", description = "org trust details")
    @JsonPropertyOrder({"created", "trustedSince", "channelsProvided", "channelsConsumed",
        "systemsMigratedTo", "systemsMigratedFrom", "systemsTransferredTo", "systemsTransferredFrom"})
    interface OrgTrustDetailsDoc {

        /**
         * @return the date the organization was created
         */
        @Schema(requiredMode = REQUIRED)
        Date getCreated();

        /**
         * @return the date the organization has been trusted since
         */
        @Schema(name = "trusted_since", requiredMode = REQUIRED)
        Date getTrustedSince();

        /**
         * @return the number of channels provided
         */
        @Schema(name = "channels_provided", requiredMode = REQUIRED)
        Integer getChannelsProvided();

        /**
         * @return the number of channels consumed
         */
        @Schema(name = "channels_consumed", requiredMode = REQUIRED)
        Integer getChannelsConsumed();

        /**
         * @return the number of systems migrated to the organization
         */
        @Schema(name = "systems_migrated_to", requiredMode = REQUIRED)
        Integer getSystemsMigratedTo();

        /**
         * @return the number of systems migrated from the organization
         */
        @Schema(name = "systems_migrated_from", requiredMode = REQUIRED)
        Integer getSystemsMigratedFrom();

        /**
         * @return the number of systems transferred to the organization
         */
        @Schema(name = "systems_transferred_to", requiredMode = REQUIRED)
        Integer getSystemsTransferredTo();

        /**
         * @return the number of systems transferred from the organization
         */
        @Schema(name = "systems_transferred_from", requiredMode = REQUIRED)
        Integer getSystemsTransferredFrom();
    }

    @Schema(name = "OrgTrustOverviewDoc", description = "trusted organizations")
    @JsonPropertyOrder({"orgId", "orgName", "trustEnabled"})
    interface OrgTrustOverviewDoc {

        /**
         * @return the organization identifier
         */
        @Schema(requiredMode = REQUIRED)
        Integer getOrgId();

        /**
         * @return the organization name
         */
        @Schema(requiredMode = REQUIRED)
        String getOrgName();

        /**
         * @return whether the trust is enabled
         */
        @Schema(requiredMode = REQUIRED)
        Boolean getTrustEnabled();
    }

    @Schema(name = "AffectedSystem", description = "affected systems")
    @JsonPropertyOrder({"systemId", "systemName"})
    interface AffectedSystemDoc {

        /**
         * @return the system identifier
         */
        @Schema(requiredMode = REQUIRED)
        Integer getSystemId();

        /**
         * @return the system name
         */
        @Schema(requiredMode = REQUIRED)
        String getSystemName();
    }

    @Schema(name = "ApiResponseTrustedOrgList")
    interface TrustedOrgListResponse extends ApiResponseWrapper<List<TrustedOrgDoc>> { }

    @Schema(name = "ApiResponseTrustChannelInfoList")
    interface ChannelInfoListResponse extends ApiResponseWrapper<List<ChannelInfoDoc>> { }

    @Schema(name = "ApiResponseOrgTrustDetails")
    interface OrgTrustDetailsResponse extends ApiResponseWrapper<OrgTrustDetailsDoc> { }

    @Schema(name = "ApiResponseOrgTrustOverviewList")
    interface OrgTrustListResponse extends ApiResponseWrapper<List<OrgTrustOverviewDoc>> { }

    @Schema(name = "ApiResponseAffectedSystemList")
    interface AffectedSystemListResponse extends ApiResponseWrapper<List<AffectedSystemDoc>> { }
}
