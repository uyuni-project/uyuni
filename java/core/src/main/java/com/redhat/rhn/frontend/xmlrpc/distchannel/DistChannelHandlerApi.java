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
package com.redhat.rhn.frontend.xmlrpc.distchannel;

import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link DistChannelHandler}.
 */
@Tag(name = "distchannel", description = "Provides methods to access and modify distribution channel information")
public interface DistChannelHandlerApi {

    /**
     * Lists the default distribution channel maps.
     *
     * @param loggedInUser the current user
     * @return list of dist channel maps
     */
    @ApiEndpointDoc(
        summary = "Lists the default distribution channel maps",
        method = HttpMethod.get,
        responseClass = DistChannelMapListResponse.class
    )
    Object[] listDefaultMaps(@Parameter(hidden = true) User loggedInUser);

    /**
     * Lists distribution channel maps valid for an organization, product admin rights needed.
     * When no organization is given, the maps valid for the user's own organization are listed.
     *
     * @param loggedInUser the current user
     * @param orgId the organization ID, or null for the user's own organization
     * @return list of dist channel maps
     */
    @ApiEndpointDoc(
        summary = "Lists distribution channel maps valid for an organization, Uyuni admin rights needed. " +
                  "When orgId is omitted, the maps valid for the user's own organization are listed.",
        method = HttpMethod.get,
        responseClass = DistChannelMapListResponse.class
    )
    Object[] listMapsForOrg(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(
            name = "orgId",
            in = ParameterIn.QUERY,
            required = false
        ) Integer orgId
    );

    /**
     * Sets, overrides (/removes if channelLabel empty) a distribution channel map
     * within an organization.
     *
     * @param loggedInUser the current user
     * @param os the operating system
     * @param release the release
     * @param archName the channel architecture label
     * @param channelLabel the channel label
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Sets, overrides (/removes if channelLabel empty) a distribution channel map " +
                  "within an organization",
        requestClass = SetMapForOrgRequest.class,
        isIntegerResponse = true
    )
    int setMapForOrg(User loggedInUser, String os, String release, String archName, String channelLabel);

    @Schema(name = "SetMapForOrgRequest")
    @JsonPropertyOrder({"os", "release", "archName", "channelLabel"})
    interface SetMapForOrgRequest {

        /**
         * @return the operating system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getOs();

        /**
         * @return the release
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getRelease();

        /**
         * @return the channel architecture label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getArchName();

        /**
         * @return the channel label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();
    }

    @Schema(name = "DistributionChannelMap")
    @JsonPropertyOrder({"os", "release", "archName", "channelLabel", "orgSpecific"})
    interface DistChannelMapDoc {

        /**
         * @return the operating system
         */
        @Schema(description = "operating system", requiredMode = Schema.RequiredMode.REQUIRED)
        String getOs();

        /**
         * @return the OS release
         */
        @Schema(description = "OS release", requiredMode = Schema.RequiredMode.REQUIRED)
        String getRelease();

        /**
         * @return the channel architecture
         */
        @Schema(name = "arch_name", description = "channel architecture",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getArchName();

        /**
         * @return the channel label
         */
        @Schema(name = "channel_label", description = "channel label",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();

        /**
         * @return 'Y' if organization specific, 'N' if default
         */
        @Schema(name = "org_specific", description = "'Y' organization specific, 'N' default",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getOrgSpecific();
    }

    @Schema(name = "ApiResponseDistChannelMapList")
    interface DistChannelMapListResponse extends ApiResponseWrapper<List<DistChannelMapDoc>> { }
}
