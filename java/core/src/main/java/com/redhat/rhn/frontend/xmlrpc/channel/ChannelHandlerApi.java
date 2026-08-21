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
package com.redhat.rhn.frontend.xmlrpc.channel;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link ChannelHandler}.
 */
@Tag(name = "channel", description = "Provides method to get back a list of Software Channels.")
public interface ChannelHandlerApi {

    /**
     * Lists every visible software channel.
     *
     * @param loggedInUser the current user
     * @return the visible software channels
     */
    @ApiEndpointDoc(
        summary = "List all visible software channels.",
        method = HttpMethod.get,
        responseClass = SoftwareChannelListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    List<Map<String, Object>> listSoftwareChannels(User loggedInUser);

    /**
     * Lists the labels of the visible software channels with the given auto-sync setting.
     *
     * @param loggedInUser the current user
     * @param autoSync whether channels with auto-sync enabled are returned
     * @return the channel labels
     */
    @ApiEndpointDoc(
        summary = "List all visible software channels.",
        method = HttpMethod.get,
        responseClass = ChannelLabelListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "the list of channel labels")
    )
    List<String> listSoftwareChannelsByAutoSync(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "autoSync", description = "If should return channels with auto-sync " +
            "enabled (true) or disabled (false)", in = ParameterIn.QUERY, required = true) Boolean autoSync
    );

    /**
     * Lists the software channels the user's organization is entitled to.
     *
     * @param loggedInUser the current user
     * @return the channels
     */
    @ApiEndpointDoc(
        summary = "List all software channels that the user's organization is entitled to.",
        method = HttpMethod.get,
        responseClass = ChannelInfoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel info")
    )
    Object[] listAllChannels(User loggedInUser);

    /**
     * Lists the vendor software channels the user's organization is entitled to.
     *
     * @param loggedInUser the current user
     * @return the channels
     */
    @ApiEndpointDoc(
        summary = "Lists all the vendor software channels that the user's organization is entitled to.",
        method = HttpMethod.get,
        responseClass = ChannelInfoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel info")
    )
    Object[] listVendorChannels(User loggedInUser);

    /**
     * Lists the most popular software channels.
     *
     * @param loggedInUser the current user
     * @param popularityCount the minimum number of subscribed systems
     * @return the channels
     */
    @ApiEndpointDoc(
        summary = "List the most popular software channels. Channels that have at least the number " +
                "of systems subscribed as specified by the popularity count will be returned.",
        method = HttpMethod.get,
        responseClass = ChannelInfoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel info")
    )
    Object[] listPopularChannels(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "popularityCount", in = ParameterIn.QUERY, required = true) Integer popularityCount
    );

    /**
     * Lists the software channels that belong to the user's organization.
     *
     * @param loggedInUser the current user
     * @return the channels
     */
    @ApiEndpointDoc(
        summary = "List all software channels that belong to the user's organization.",
        method = HttpMethod.get,
        responseClass = ChannelInfoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel info")
    )
    Object[] listMyChannels(User loggedInUser);

    /**
     * Lists the software channels that may be shared by the user's organization.
     *
     * @param loggedInUser the current user
     * @return the channels
     */
    @ApiEndpointDoc(
        summary = "List all software channels that may be shared by the user's organization.",
        method = HttpMethod.get,
        responseClass = ChannelInfoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel info")
    )
    Object[] listSharedChannels(User loggedInUser);

    /**
     * Lists the retired software channels.
     *
     * @param loggedInUser the current user
     * @return the channels
     */
    @ApiEndpointDoc(
        summary = "List all retired software channels. These are channels that the user's organization " +
                "is entitled to, but are no longer supported because they have reached their " +
                "'end-of-life' date.",
        method = HttpMethod.get,
        responseClass = ChannelInfoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel info")
    )
    Object[] listRetiredChannels(User loggedInUser);

    /**
     * Lists the software channels the user is entitled to manage.
     *
     * @param loggedInUser the current user
     * @return the channels
     */
    @ApiEndpointDoc(
        summary = "List all software channels that the user is entitled to manage.",
        method = HttpMethod.get,
        responseClass = ChannelInfoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel info")
    )
    Object[] listManageableChannels(User loggedInUser);

    @Schema(name = "ChannelTreeNodeInfo", description = "channel info")
    @JsonPropertyOrder({"id", "label", "name", "providerName", "packages", "systems", "archName"})
    interface ChannelInfoDoc {

        /**
         * @return the channel identifier
         */
        @Schema(requiredMode = REQUIRED)
        Integer getId();

        /**
         * @return the channel label
         */
        @Schema(requiredMode = REQUIRED)
        String getLabel();

        /**
         * @return the channel name
         */
        @Schema(requiredMode = REQUIRED)
        String getName();

        /**
         * @return the name of the organization providing the channel
         */
        @Schema(name = "provider_name", requiredMode = REQUIRED)
        String getProviderName();

        /**
         * @return the number of packages in the channel
         */
        @Schema(requiredMode = REQUIRED)
        Integer getPackages();

        /**
         * @return the number of systems subscribed to the channel
         */
        @Schema(requiredMode = REQUIRED)
        Integer getSystems();

        /**
         * @return the channel architecture name
         */
        @Schema(name = "arch_name", requiredMode = REQUIRED)
        String getArchName();
    }

    @Schema(name = "SoftwareChannelInfo", description = "channel")
    @JsonPropertyOrder({"label", "name", "parentLabel", "endOfLife", "arch"})
    interface SoftwareChannelDoc {

        /**
         * @return the channel label
         */
        @Schema(requiredMode = REQUIRED)
        String getLabel();

        /**
         * @return the channel name
         */
        @Schema(requiredMode = REQUIRED)
        String getName();

        /**
         * @return the label of the parent channel, empty for a base channel
         */
        @Schema(name = "parent_label", requiredMode = REQUIRED)
        String getParentLabel();

        /**
         * @return the end of life date of the channel
         */
        @Schema(name = "end_of_life", requiredMode = REQUIRED)
        String getEndOfLife();

        /**
         * @return the channel architecture
         */
        @Schema(requiredMode = REQUIRED)
        String getArch();
    }

    @Schema(name = "ApiResponseChannelTreeNodeInfoList")
    interface ChannelInfoListResponse extends ApiResponseWrapper<List<ChannelInfoDoc>> { }

    @Schema(name = "ApiResponseSoftwareChannelInfoList")
    interface SoftwareChannelListResponse extends ApiResponseWrapper<List<SoftwareChannelDoc>> { }

    @Schema(name = "ApiResponseChannelLabelList")
    interface ChannelLabelListResponse extends ApiResponseWrapper<List<String>> { }
}
