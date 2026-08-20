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
package com.redhat.rhn.frontend.xmlrpc.sync.content;

import com.redhat.rhn.domain.product.MgrSyncChannelDto;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.content.MgrSyncProductDto;
import com.redhat.rhn.manager.setup.MirrorCredentialsDto;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Collection;
import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link ContentSyncHandler}.
 */
@Tag(name = "sync.content", description = "Provides the namespace for the content synchronization methods.")
public interface ContentSyncHandlerApi {

    /**
     * Lists all accessible products.
     *
     * @param loggedInUser the current user
     * @return the accessible products
     */
    @ApiEndpointDoc(
        summary = "List all accessible products.",
        method = HttpMethod.get,
        responseClass = ProductListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "product")
    )
    Collection<MgrSyncProductDto> listProducts(@Parameter(hidden = true) User loggedInUser);

    /**
     * Lists all accessible channels.
     *
     * @param loggedInUser the current user
     * @return the accessible channels
     */
    @ApiEndpointDoc(
        summary = "List all accessible channels.",
        method = HttpMethod.get,
        responseClass = ChannelListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    List<MgrSyncChannelDto> listChannels(@Parameter(hidden = true) User loggedInUser);

    /**
     * Lists the organization credentials.
     *
     * @param loggedInUser the current user
     * @return the organization credentials
     */
    @ApiEndpointDoc(
        summary = "List organization credentials (mirror credentials) available in\n#product().",
        method = HttpMethod.get,
        responseClass = CredentialsListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "credentials")
    )
    List<MirrorCredentialsDto> listCredentials(@Parameter(hidden = true) User loggedInUser);

    /**
     * Synchronizes the channel families.
     *
     * @param loggedInUser the current user
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Synchronize channel families between the Customer Center\nand the #product() database.",
        isIntegerResponse = true
    )
    Integer synchronizeChannelFamilies(User loggedInUser);

    /**
     * Synchronizes the SUSE products.
     *
     * @param loggedInUser the current user
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Synchronize SUSE products between the Customer Center\nand the #product() database.",
        isIntegerResponse = true
    )
    Integer synchronizeProducts(User loggedInUser);

    /**
     * Synchronizes the subscriptions.
     *
     * @param loggedInUser the current user
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Synchronize subscriptions between the Customer Center\nand the #product() database.",
        isIntegerResponse = true
    )
    Integer synchronizeSubscriptions(User loggedInUser);

    /**
     * Synchronizes the repositories.
     *
     * @param loggedInUser the current user
     * @param mirrorUrl the mirror url
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Synchronize repositories between the Customer Center\nand the #product() database.",
        requestClass = SynchronizeRepositoriesRequest.class,
        isIntegerResponse = true
    )
    Integer synchronizeRepositories(User loggedInUser, String mirrorUrl);

    /**
     * Adds a channel.
     *
     * @param loggedInUser the current user
     * @param channelLabel the label of the channel to add
     * @param mirrorUrl the mirror url
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Add a new channel to the #product() database",
        requestClass = AddChannelRequest.class,
        isIntegerResponse = true
    )
    Integer addChannel(User loggedInUser, String channelLabel, String mirrorUrl);

    /**
     * Adds a channel together with the channels it depends on.
     *
     * @param loggedInUser the current user
     * @param channelLabel the label of the channel to add
     * @param mirrorUrl the mirror url
     * @return the labels of the enabled channels
     */
    @ApiEndpointDoc(
        summary = "Add a new channel to the #product() database",
        requestClass = AddChannelRequest.class,
        responseClass = ChannelLabelListResponse.class,
        responseDescription = "enabled channel labels"
    )
    Object[] addChannels(User loggedInUser, String channelLabel, String mirrorUrl);

    /**
     * Adds organization credentials.
     *
     * @param loggedInUser the current user
     * @param username the credentials username
     * @param password the credentials password
     * @param primary whether these are the primary credentials
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Add organization credentials (mirror credentials) to #product().",
        requestClass = AddCredentialsRequest.class,
        isIntegerResponse = true
    )
    Integer addCredentials(User loggedInUser, String username, String password, boolean primary);

    /**
     * Deletes organization credentials.
     *
     * @param loggedInUser the current user
     * @param username the credentials username
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete organization credentials (mirror credentials) from #product().",
        requestClass = DeleteCredentialsRequest.class,
        isIntegerResponse = true
    )
    Integer deleteCredentials(User loggedInUser, String username);

    @Schema(name = "SyncRepositoriesRequest")
    interface SynchronizeRepositoriesRequest {

        /**
         * @return the mirror url
         */
        @Schema(description = "Optional mirror url or null", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getMirrorUrl();
    }

    @Schema(name = "SyncAddChannelRequest")
    @JsonPropertyOrder({"channelLabel", "mirrorUrl"})
    interface AddChannelRequest {

        /**
         * @return the label of the channel to add
         */
        @Schema(description = "Label of the channel to add", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();

        /**
         * @return the mirror url
         */
        @Schema(description = "Sync from mirror temporarily", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getMirrorUrl();
    }

    @Schema(name = "SyncAddCredentialsRequest")
    @JsonPropertyOrder({"username", "password", "primary"})
    interface AddCredentialsRequest {

        /**
         * @return the credentials username
         */
        @Schema(description = "Organization credentials (Mirror credentials) username",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getUsername();

        /**
         * @return the credentials password
         */
        @Schema(description = "Organization credentials (Mirror credentials) password",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getPassword();

        /**
         * @return whether these are the primary credentials
         */
        @Schema(description = "Make this the primary credentials", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getPrimary();
    }

    @Schema(name = "SyncDeleteCredentialsRequest")
    interface DeleteCredentialsRequest {

        /**
         * @return the credentials username
         */
        @Schema(description = "Username of credentials to delete", requiredMode = Schema.RequiredMode.REQUIRED)
        String getUsername();
    }

    @Schema(name = "MgrSyncChannel")
    @JsonPropertyOrder({"arch", "description", "family", "isSigned", "label", "name", "optional", "parent",
        "productName", "productVersion", "sourceUrl", "status", "summary", "updateTag", "installerUpdates"})
    interface ChannelDoc {

        /**
         * @return the architecture of the channel
         */
        @Schema(description = "architecture of the channel", requiredMode = Schema.RequiredMode.REQUIRED)
        String getArch();

        /**
         * @return the description of the channel
         */
        @Schema(description = "description of the channel", requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();

        /**
         * @return the channel family label
         */
        @Schema(description = "channel family label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getFamily();

        /**
         * @return whether the channel has signed metadata
         */
        @Schema(name = "is_signed", description = "channel has signed metadata",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getIsSigned();

        /**
         * @return the label of the channel
         */
        @Schema(description = "label of the channel", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the name of the channel
         */
        @Schema(description = "name of the channel", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return whether the channel is optional
         */
        @Schema(description = "channel is optional", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getOptional();

        /**
         * @return the label of the parent channel
         */
        @Schema(description = "the label of the parent channel", requiredMode = Schema.RequiredMode.REQUIRED)
        String getParent();

        /**
         * @return the product name
         */
        @Schema(name = "product_name", description = "product name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getProductName();

        /**
         * @return the product version
         */
        @Schema(name = "product_version", description = "product version",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getProductVersion();

        /**
         * @return the repository source URL
         */
        @Schema(name = "source_url", description = "repository source URL",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSourceUrl();

        /**
         * @return the status of the channel
         */
        @Schema(description = "'available', 'unavailable' or 'installed'",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getStatus();

        /**
         * @return the channel summary
         */
        @Schema(description = "channel summary", requiredMode = Schema.RequiredMode.REQUIRED)
        String getSummary();

        /**
         * @return the update tag
         */
        @Schema(name = "update_tag", description = "update tag", requiredMode = Schema.RequiredMode.REQUIRED)
        String getUpdateTag();

        /**
         * @return whether this is an installer update channel
         */
        @Schema(name = "installer_updates", description = "is an installer update channel",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getInstallerUpdates();
    }

    @Schema(name = "MgrSyncExtensionProduct")
    @JsonPropertyOrder({"friendlyName", "arch", "status", "channels"})
    interface ExtensionProductDoc {

        /**
         * @return the friendly name of the extension product
         */
        @Schema(name = "friendly_name", description = "friendly name of extension product",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getFriendlyName();

        /**
         * @return the architecture
         */
        @Schema(description = "architecture", requiredMode = Schema.RequiredMode.REQUIRED)
        String getArch();

        /**
         * @return the status of the extension product
         */
        @Schema(description = "'available', 'unavailable' or 'installed'",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getStatus();

        /**
         * @return the channels of the extension product
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "channel")
        List<ChannelDoc> getChannels();
    }

    @Schema(name = "MgrSyncProduct")
    @JsonPropertyOrder({"friendlyName", "arch", "status", "channels", "extensions", "recommended"})
    interface ProductDoc {

        /**
         * @return the friendly name of the product
         */
        @Schema(name = "friendly_name", description = "friendly name of the product",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getFriendlyName();

        /**
         * @return the architecture
         */
        @Schema(description = "architecture", requiredMode = Schema.RequiredMode.REQUIRED)
        String getArch();

        /**
         * @return the status of the product
         */
        @Schema(description = "'available', 'unavailable' or 'installed'",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getStatus();

        /**
         * @return the channels of the product
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "channel")
        List<ChannelDoc> getChannels();

        /**
         * @return the extensions of the product
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "extension product")
        List<ExtensionProductDoc> getExtensions();

        /**
         * @return whether the product is recommended
         */
        @Schema(description = "recommended", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getRecommended();
    }

    @Schema(name = "MirrorCredentials")
    @JsonPropertyOrder({"id", "user", "isPrimary"})
    interface CredentialsDoc {

        /**
         * @return the id of the credentials
         */
        @Schema(description = "ID of the credentials", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the username
         */
        @Schema(description = "username", requiredMode = Schema.RequiredMode.REQUIRED)
        String getUser();

        /**
         * @return whether these are the primary credentials
         */
        @Schema(name = "isPrimary", description = "primary", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getIsPrimary();
    }

    @Schema(name = "ApiResponseMgrSyncProductList")
    interface ProductListResponse extends ApiResponseWrapper<List<ProductDoc>> { }

    @Schema(name = "ApiResponseMgrSyncChannelList")
    interface ChannelListResponse extends ApiResponseWrapper<List<ChannelDoc>> { }

    @Schema(name = "ApiResponseMirrorCredentialsList")
    interface CredentialsListResponse extends ApiResponseWrapper<List<CredentialsDoc>> { }

    @Schema(name = "ApiResponseSyncChannelLabelList")
    interface ChannelLabelListResponse extends ApiResponseWrapper<List<String>> { }
}
