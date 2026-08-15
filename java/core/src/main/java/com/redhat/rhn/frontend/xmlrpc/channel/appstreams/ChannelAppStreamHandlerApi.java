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
package com.redhat.rhn.frontend.xmlrpc.channel.appstreams;

import com.redhat.rhn.domain.channel.AppStream;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link ChannelAppStreamHandler}.
 */
@Tag(name = "channel.appstreams", description = "Provides methods to handle appstreams for channels.")
public interface ChannelAppStreamHandlerApi {

    /**
     * List the available module streams for a channel.
     *
     * @param loggedInUser the current user
     * @param channelLabel the channel label
     * @return the available module streams
     */
    @ApiEndpointDoc(
        summary = "List available module streams for a given channel.",
        method = HttpMethod.get,
        responseClass = AppStreamListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "AppStream")
    )
    List<AppStream> listModuleStreams(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "channelLabel", in = ParameterIn.QUERY, required = true) String channelLabel);

    /**
     * Check whether a channel is modular.
     *
     * @param loggedInUser the current user
     * @param channelLabel the channel label
     * @return true if the channel is modular
     */
    @ApiEndpointDoc(
        summary = "Check if channel is modular.",
        method = HttpMethod.get,
        responseClass = BooleanResponse.class,
        responseDescription = "true if the channel is modular",
        legacyDocResponse = @LegacyDocResponse(name = "result")
    )
    boolean isModular(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "channelLabel", in = ParameterIn.QUERY, required = true) String channelLabel);

    /**
     * List the modular channels of the user organization.
     *
     * @param loggedInUser the current user
     * @return the modular channels
     */
    @ApiEndpointDoc(
        summary = "List modular channels in users organization.",
        method = HttpMethod.get,
        responseClass = ChannelListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    List<Channel> listModular(@Parameter(hidden = true) User loggedInUser);

    @Schema(name = "ChannelAppStream")
    @JsonPropertyOrder({"stream", "module", "arch"})
    interface ChannelAppStreamDoc {

        /**
         * @return the stream name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getStream();

        /**
         * @return the module name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getModule();

        /**
         * @return the module architecture
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getArch();
    }

    @Schema(name = "ContentSource")
    @JsonPropertyOrder({"id", "label", "sourceUrl", "type"})
    interface ContentSourceDoc {

        /**
         * @return the content source id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the content source label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the content source url
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSourceUrl();

        /**
         * @return the content source type
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getType();
    }

    @Schema(name = "SoftwareChannel")
    @JsonPropertyOrder({"id", "name", "label", "archName", "archLabel", "summary", "description",
        "checksumLabel", "lastModified", "maintainerName", "maintainerEmail", "maintainerPhone",
        "supportPolicy", "gpgKeyUrl", "gpgKeyId", "gpgKeyFp", "yumrepoLastSync", "endOfLife",
        "parentChannelLabel", "cloneOriginal", "syncStatus", "contentSources"})
    interface ChannelDoc {

        /**
         * @return the channel id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the channel name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the channel label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the channel architecture name
         */
        @Schema(name = "arch_name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getArchName();

        /**
         * @return the channel architecture label
         */
        @Schema(name = "arch_label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getArchLabel();

        /**
         * @return the channel summary
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSummary();

        /**
         * @return the channel description
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();

        /**
         * @return the checksum label
         */
        @Schema(name = "checksum_label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChecksumLabel();

        /**
         * @return the last modification date
         */
        @Schema(name = "last_modified", requiredMode = Schema.RequiredMode.REQUIRED)
        Date getLastModified();

        /**
         * @return the maintainer name
         */
        @Schema(name = "maintainer_name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getMaintainerName();

        /**
         * @return the maintainer email
         */
        @Schema(name = "maintainer_email", requiredMode = Schema.RequiredMode.REQUIRED)
        String getMaintainerEmail();

        /**
         * @return the maintainer phone
         */
        @Schema(name = "maintainer_phone", requiredMode = Schema.RequiredMode.REQUIRED)
        String getMaintainerPhone();

        /**
         * @return the support policy
         */
        @Schema(name = "support_policy", requiredMode = Schema.RequiredMode.REQUIRED)
        String getSupportPolicy();

        /**
         * @return the GPG key url
         */
        @Schema(name = "gpg_key_url", requiredMode = Schema.RequiredMode.REQUIRED)
        String getGpgKeyUrl();

        /**
         * @return the GPG key id
         */
        @Schema(name = "gpg_key_id", requiredMode = Schema.RequiredMode.REQUIRED)
        String getGpgKeyId();

        /**
         * @return the GPG key fingerprint
         */
        @Schema(name = "gpg_key_fp", requiredMode = Schema.RequiredMode.REQUIRED)
        String getGpgKeyFp();

        /**
         * @return the last repository synchronisation date
         */
        @Schema(name = "yumrepo_last_sync", description = "(optional)")
        Date getYumrepoLastSync();

        /**
         * @return the end of life date
         */
        @Schema(name = "end_of_life", requiredMode = Schema.RequiredMode.REQUIRED)
        String getEndOfLife();

        /**
         * @return the parent channel label
         */
        @Schema(name = "parent_channel_label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getParentChannelLabel();

        /**
         * @return the label of the channel this one was cloned from
         */
        @Schema(name = "clone_original", requiredMode = Schema.RequiredMode.REQUIRED)
        String getCloneOriginal();

        /**
         * @return the synchronisation status
         */
        @Schema(name = "sync_status",
                description = "'C' for new created, 'S' for syncing and 'R' for ready",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSyncStatus();

        /**
         * @return the content sources of the channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "content source")
        List<ContentSourceDoc> getContentSources();
    }

    @Schema(name = "ApiResponseBoolean")
    interface BooleanResponse extends ApiResponseWrapper<Boolean> { }

    @Schema(name = "ApiResponseChannelAppStreamList")
    interface AppStreamListResponse extends ApiResponseWrapper<List<ChannelAppStreamDoc>> { }

    @Schema(name = "ApiResponseSoftwareChannelList")
    interface ChannelListResponse extends ApiResponseWrapper<List<ChannelDoc>> { }
}
