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
package com.redhat.rhn.frontend.xmlrpc.kickstart.tree;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.kickstart.KickstartableTreeDetail;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link KickstartTreeHandler}.
 */
@Tag(name = "kickstart.tree", description = "Provides methods to access and modify the kickstart trees.")
public interface KickstartTreeHandlerApi {

    /**
     * Returns the details of the kickstartable tree with the given label.
     *
     * @param loggedInUser the current user
     * @param treeLabel the label of the kickstartable tree
     * @return the kickstartable tree details
     */
    @ApiEndpointDoc(
        summary = "The detailed information about a kickstartable tree given the tree name.",
        method = HttpMethod.get,
        responseClass = KickstartTreeDetailResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "kickstartable tree")
    )
    KickstartableTreeDetail getDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "treeLabel", description = "Label of kickstartable tree to search.",
                in = ParameterIn.QUERY, required = true) String treeLabel);

    /**
     * Lists the available kickstartable trees of a channel.
     *
     * @param loggedInUser the current user
     * @param channelLabel the label of the channel
     * @return the kickstartable trees
     */
    @ApiEndpointDoc(
        summary = "List the available kickstartable trees for the given channel.",
        requestClass = ChannelLabelRequest.class,
        responseClass = KickstartTreeListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "kickstartable tree")
    )
    @SuppressWarnings("rawtypes")
    List list(User loggedInUser, String channelLabel);

    /**
     * Lists the available kickstartable install types.
     *
     * @param loggedInUser the current user
     * @return the kickstart install types
     */
    @ApiEndpointDoc(
        summary = "List the available kickstartable install types (rhel2,3,4,5 and\nfedora9+).",
        method = HttpMethod.get,
        responseClass = KickstartInstallTypeListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "kickstart install type")
    )
    @SuppressWarnings("rawtypes")
    List listInstallTypes(@Parameter(hidden = true) User loggedInUser);

    /**
     * Creates a kickstart tree.
     *
     * @param loggedInUser the current user
     * @param treeLabel the label of the new kickstart tree
     * @param basePath the path to the base of the kickstart tree
     * @param channelLabel the label of the channel to associate
     * @param installType the kickstart install type label
     * @param kernelOptions the options passed to the kernel when booting
     * @param postKernelOptions the options passed to the kernel after installation
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Create a Kickstart Tree (Distribution) in #product().",
        requestClass = CreateTreeRequest.class,
        isIntegerResponse = true
    )
    int create(User loggedInUser, String treeLabel, String basePath, String channelLabel, String installType,
               String kernelOptions, String postKernelOptions);

    /**
     * Updates a kickstart tree.
     *
     * @param loggedInUser the current user
     * @param treeLabel the label of the kickstart tree
     * @param basePath the path to the base of the kickstart tree
     * @param channelLabel the label of the channel to associate
     * @param installType the kickstart install type label
     * @param kernelOptions the options passed to the kernel when booting
     * @param postKernelOptions the options passed to the kernel after installation
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Edit a Kickstart Tree (Distribution) in #product().",
        requestClass = UpdateTreeRequest.class,
        isIntegerResponse = true
    )
    int update(User loggedInUser, String treeLabel, String basePath, String channelLabel, String installType,
               String kernelOptions, String postKernelOptions);

    /**
     * Renames a kickstart tree.
     *
     * @param loggedInUser the current user
     * @param originalLabel the current label of the kickstart tree
     * @param newLabel the new label of the kickstart tree
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Rename a Kickstart Tree (Distribution) in #product().",
        requestClass = RenameTreeRequest.class,
        isIntegerResponse = true
    )
    int rename(User loggedInUser, String originalLabel, String newLabel);

    /**
     * Deletes a kickstart tree.
     *
     * @param loggedInUser the current user
     * @param treeLabel the label of the kickstart tree
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete a Kickstart Tree (Distribution) from #product().",
        requestClass = TreeLabelRequest.class,
        isIntegerResponse = true
    )
    int delete(User loggedInUser, String treeLabel);

    /**
     * Deletes a kickstart tree and every profile associated with it.
     *
     * @param loggedInUser the current user
     * @param treeLabel the label of the kickstart tree
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete a kickstarttree and any profiles associated with\nit.",
        requestClass = TreeLabelRequest.class,
        isIntegerResponse = true
    )
    int deleteTreeAndProfiles(User loggedInUser, String treeLabel);

    @Schema(name = "KickstartChannelLabelRequest")
    interface ChannelLabelRequest {

        /**
         * @return the label of the channel
         */
        @Schema(description = "Label of channel to search.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();
    }

    @Schema(name = "KickstartTreeLabelRequest")
    interface TreeLabelRequest {

        /**
         * @return the label of the kickstart tree
         */
        @Schema(description = "Label for the kickstart tree to delete.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getTreeLabel();
    }

    @Schema(name = "KickstartRenameTreeRequest")
    @JsonPropertyOrder({"originalLabel", "newLabel"})
    interface RenameTreeRequest {

        /**
         * @return the current label of the kickstart tree
         */
        @Schema(description = "Label for the kickstart tree to rename.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getOriginalLabel();

        /**
         * @return the new label of the kickstart tree
         */
        @Schema(description = "The kickstart tree's new label.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getNewLabel();
    }

    @Schema(name = "KickstartCreateTreeRequest")
    @JsonPropertyOrder({"treeLabel", "basePath", "channelLabel", "installType", "kernelOptions",
        "postKernelOptions"})
    interface CreateTreeRequest {

        /**
         * @return the label of the new kickstart tree
         */
        @Schema(description = "The new kickstart tree label.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getTreeLabel();

        /**
         * @return the path to the base of the kickstart tree
         */
        @Schema(description = "Path to the base or root of the kickstart tree.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getBasePath();

        /**
         * @return the label of the channel to associate
         */
        @Schema(description = "Label of channel to associate with the kickstart tree. ",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();

        /**
         * @return the kickstart install type label
         */
        @Schema(description = "Label for KickstartInstallType (rhel_6, rhel_7, rhel_8, rhel_9, fedora_9).",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getInstallType();

        /**
         * @return the options passed to the kernel when booting
         */
        @Schema(description = "Options to be passed to the kernel when booting for the installation. ")
        String getKernelOptions();

        /**
         * @return the options passed to the kernel after installation
         */
        @Schema(description = "Options to be passed to the kernel when booting for the installation. ")
        String getPostKernelOptions();
    }

    @Schema(name = "KickstartUpdateTreeRequest")
    @JsonPropertyOrder({"treeLabel", "basePath", "channelLabel", "installType", "kernelOptions",
        "postKernelOptions"})
    interface UpdateTreeRequest {

        /**
         * @return the label of the kickstart tree
         */
        @Schema(description = "Label for the kickstart tree.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getTreeLabel();

        /**
         * @return the path to the base of the kickstart tree
         */
        @Schema(description = "Path to the base or root of the kickstart tree.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getBasePath();

        /**
         * @return the label of the channel to associate
         */
        @Schema(description = "Label of channel to associate with kickstart tree.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();

        /**
         * @return the kickstart install type label
         */
        @Schema(description = "Label for KickstartInstallType (rhel_6, rhel_7, rhel_8, rhel_9, fedora_9).",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getInstallType();

        /**
         * @return the options passed to the kernel when booting
         */
        @Schema(description = "Options to be passed to the kernel when booting for the installation. ")
        String getKernelOptions();

        /**
         * @return the options passed to the kernel after installation
         */
        @Schema(description = "Options to be passed to the kernel when booting for the installation. ")
        String getPostKernelOptions();
    }

    @Schema(name = "KickstartInstallType")
    @JsonPropertyOrder({"id", "label", "name"})
    interface KickstartInstallTypeDoc {

        /**
         * @return the install type id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the install type label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the install type name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();
    }

    @Schema(name = "KickstartTree")
    @JsonPropertyOrder({"id", "label", "basePath", "channelId"})
    interface KickstartTreeDoc {

        /**
         * @return the tree id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the tree label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the base path of the tree
         */
        @Schema(name = "base_path", requiredMode = Schema.RequiredMode.REQUIRED)
        String getBasePath();

        /**
         * @return the id of the associated channel
         */
        @Schema(name = "channel_id", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getChannelId();
    }

    @Schema(name = "KickstartTreeDetail")
    @JsonPropertyOrder({"id", "label", "absPath", "channelId", "kernelOptions", "postKernelOptions",
        "installType"})
    interface KickstartTreeDetailDoc {

        /**
         * @return the tree id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the tree label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the absolute path of the tree
         */
        @Schema(name = "abs_path", requiredMode = Schema.RequiredMode.REQUIRED)
        String getAbsPath();

        /**
         * @return the id of the associated channel
         */
        @Schema(name = "channel_id", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getChannelId();

        /**
         * @return the options passed to the kernel when booting
         */
        @Schema(name = "kernel_options", requiredMode = Schema.RequiredMode.REQUIRED)
        String getKernelOptions();

        /**
         * @return the options passed to the kernel after installation
         */
        @Schema(name = "post_kernel_options", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPostKernelOptions();

        /**
         * @return the install type of the tree
         */
        @Schema(name = "install_type", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "kickstart install type")
        KickstartInstallTypeDoc getInstallType();
    }

    @Schema(name = "ApiResponseKickstartTreeDetail")
    interface KickstartTreeDetailResponse extends ApiResponseWrapper<KickstartTreeDetailDoc> { }

    @Schema(name = "ApiResponseKickstartTreeList")
    interface KickstartTreeListResponse extends ApiResponseWrapper<List<KickstartTreeDoc>> { }

    @Schema(name = "ApiResponseKickstartInstallTypeList")
    interface KickstartInstallTypeListResponse extends ApiResponseWrapper<List<KickstartInstallTypeDoc>> { }
}
