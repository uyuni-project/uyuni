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
package com.redhat.rhn.frontend.xmlrpc.kickstart;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.kickstart.KickstartIpRange;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.kickstart.KickstartDto;
import com.redhat.rhn.frontend.xmlrpc.channel.appstreams.ChannelAppStreamHandlerApi;

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
 * API contract for {@link KickstartHandler}.
 */
@Tag(name = "kickstart", description = "Provides methods to create kickstart files")
public interface KickstartHandlerApi {

    /**
     * Lists the kickstartable channels of the logged in user.
     *
     * @param loggedInUser the current user
     * @return the kickstartable channels
     */
    @ApiEndpointDoc(
        summary = "List kickstartable channels for the logged in user.",
        method = HttpMethod.get,
        responseClass = ChannelAppStreamHandlerApi.ChannelListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    List<Channel> listKickstartableChannels(@Parameter(hidden = true) User loggedInUser);

    /**
     * Lists the autoinstallable channels of the logged in user.
     *
     * @param loggedInUser the current user
     * @return the autoinstallable channels
     */
    @ApiEndpointDoc(
        summary = "List autoinstallable channels for the logged in user.",
        method = HttpMethod.get,
        responseClass = ChannelAppStreamHandlerApi.ChannelListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    List<Channel> listAutoinstallableChannels(@Parameter(hidden = true) User loggedInUser);

    /**
     * Imports a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param profileLabel the label of the new profile
     * @param virtualizationType the virtualization type
     * @param kickstartableTreeLabel the label of the kickstartable tree
     * @param kickstartFileContents the contents of the kickstart file
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Import a kickstart profile.",
        requestClass = ImportFileRequest.class,
        isIntegerResponse = true
    )
    int importFile(User loggedInUser, String profileLabel, String virtualizationType,
            String kickstartableTreeLabel, String kickstartFileContents);

    /**
     * Imports a kickstart profile with a kickstart host.
     *
     * @param loggedInUser the current user
     * @param profileLabel the label of the new profile
     * @param virtualizationType the virtualization type
     * @param kickstartableTreeLabel the label of the kickstartable tree
     * @param kickstartHost the kickstart hostname
     * @param kickstartFileContents the contents of the kickstart file
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Import a kickstart profile.",
        requestClass = ImportFileWithHostRequest.class,
        isIntegerResponse = true
    )
    int importFile(User loggedInUser, String profileLabel, String virtualizationType,
            String kickstartableTreeLabel, String kickstartHost, String kickstartFileContents);

    /**
     * Imports a kickstart profile with a kickstart host and an update type.
     *
     * @param loggedInUser the current user
     * @param profileLabel the label of the new profile
     * @param virtualizationType the virtualization type
     * @param kickstartableTreeLabel the label of the kickstartable tree
     * @param kickstartHost the kickstart hostname
     * @param kickstartFileContents the contents of the kickstart file
     * @param updateType how the profile updates itself
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Import a kickstart profile.",
        requestClass = ImportFileWithUpdateTypeRequest.class,
        isIntegerResponse = true
    )
    int importFile(User loggedInUser, String profileLabel, String virtualizationType,
            String kickstartableTreeLabel, String kickstartHost, String kickstartFileContents,
            String updateType);

    /**
     * Creates a kickstart profile with an update type.
     *
     * @param loggedInUser the current user
     * @param profileLabel the label of the new profile
     * @param virtualizationType the virtualization type
     * @param kickstartableTreeLabel the label of the kickstartable tree
     * @param kickstartHost the kickstart hostname
     * @param rootPassword the root password
     * @param updateType how the profile updates itself
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Create a kickstart profile.",
        requestClass = CreateProfileRequest.class,
        isIntegerResponse = true
    )
    int createProfile(User loggedInUser, String profileLabel, String virtualizationType,
            String kickstartableTreeLabel, String kickstartHost, String rootPassword, String updateType);

    /**
     * Creates a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param profileLabel the label of the new profile
     * @param virtualizationType the virtualization type
     * @param kickstartableTreeLabel the label of the kickstartable tree
     * @param kickstartHost the kickstart hostname
     * @param rootPassword the root password
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Create a kickstart profile.",
        requestClass = CreateProfileRequest.class,
        isIntegerResponse = true
    )
    int createProfile(User loggedInUser, String profileLabel, String virtualizationType,
            String kickstartableTreeLabel, String kickstartHost, String rootPassword);

    /**
     * Creates a kickstart profile with a custom download URL.
     *
     * @param loggedInUser the current user
     * @param profileLabel the label of the new profile
     * @param virtualizationType the virtualization type
     * @param kickstartableTreeLabel the label of the kickstartable tree
     * @param downloadUrl the download URL
     * @param rootPassword the root password
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Create a kickstart profile.",
        requestClass = CreateProfileWithCustomUrlRequest.class,
        isIntegerResponse = true
    )
    int createProfileWithCustomUrl(User loggedInUser, String profileLabel, String virtualizationType,
            String kickstartableTreeLabel, String downloadUrl, String rootPassword);

    /**
     * Creates a kickstart profile with a custom download URL and an update type.
     *
     * @param loggedInUser the current user
     * @param profileLabel the label of the new profile
     * @param virtualizationType the virtualization type
     * @param kickstartableTreeLabel the label of the kickstartable tree
     * @param downloadUrl the download URL
     * @param rootPassword the root password
     * @param updateType how the profile updates itself
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Create a kickstart profile.",
        requestClass = CreateProfileWithCustomUrlRequest.class,
        isIntegerResponse = true
    )
    int createProfileWithCustomUrl(User loggedInUser, String profileLabel, String virtualizationType,
            String kickstartableTreeLabel, String downloadUrl, String rootPassword, String updateType);

    /**
     * Lists the kickstart profiles visible to the user's organization.
     *
     * @param loggedInUser the current user
     * @return the kickstart profiles
     */
    @ApiEndpointDoc(
        summary = "Provides a list of kickstart profiles visible to the user's organization.",
        method = HttpMethod.get,
        responseClass = KickstartListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "kickstart")
    )
    List<KickstartDto> listKickstarts(@Parameter(hidden = true) User loggedInUser);

    /**
     * Lists all IP ranges and their associated kickstarts.
     *
     * @param loggedInUser the current user
     * @return the IP ranges
     */
    @ApiEndpointDoc(
        summary = "List all Ip Ranges and their associated kickstarts available " +
            "in the user's org.",
        method = HttpMethod.get,
        responseClass = KickstartIpRangeListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "kickstart IP range")
    )
    List<KickstartIpRange> listAllIpRanges(@Parameter(hidden = true) User loggedInUser);

    /**
     * Finds the kickstart associated with an IP address.
     *
     * @param loggedInUser the current user
     * @param ipAddress the ip address to search for
     * @return the label of the kickstart
     */
    @ApiEndpointDoc(
        summary = "Find an associated kickstart for a given ip address.",
        method = HttpMethod.get,
        responseClass = StringResponse.class,
        responseDescription = "label of the kickstart. Empty string if not found",
        legacyDocResponse = @LegacyDocResponse(name = "label")
    )
    String findKickstartForIp(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "ipAddress", in = ParameterIn.QUERY, required = true,
                description = "The ip address to search for (i.e. 192.168.0.1)") String ipAddress);

    /**
     * Deletes a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the profile to remove
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete a kickstart profile",
        requestClass = KsLabelRequest.class,
        isIntegerResponse = true
    )
    int deleteProfile(User loggedInUser, String ksLabel);

    /**
     * Enables or disables a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param profileLabel the label of the profile
     * @param disabled whether the profile is disabled
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Enable/Disable a Kickstart Profile",
        requestClass = DisableProfileRequest.class,
        isIntegerResponse = true
    )
    int disableProfile(User loggedInUser, String profileLabel, Boolean disabled);

    /**
     * Tells whether a kickstart profile is disabled.
     *
     * @param loggedInUser the current user
     * @param profileLabel the label of the profile
     * @return whether the profile is disabled
     */
    @ApiEndpointDoc(
        summary = "Returns whether a kickstart profile is disabled",
        method = HttpMethod.get,
        responseClass = BooleanResponse.class,
        responseDescription = "true if profile is disabled",
        legacyDocResponse = @LegacyDocResponse(name = "disabled")
    )
    boolean isProfileDisabled(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "profileLabel", in = ParameterIn.QUERY, required = true,
                description = "kickstart profile label") String profileLabel);

    /**
     * Renames a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param originalLabel the label of the profile to rename
     * @param newLabel the new label
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Rename a kickstart profile in #product().",
        requestClass = RenameProfileRequest.class,
        isIntegerResponse = true
    )
    int renameProfile(User loggedInUser, String originalLabel, String newLabel);

    /**
     * Clones a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabelToClone the label of the profile to clone
     * @param newKsLabel the label of the cloned profile
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Clone a Kickstart Profile",
        requestClass = CloneProfileRequest.class,
        isIntegerResponse = true
    )
    int cloneProfile(User loggedInUser, String ksLabelToClone, String newKsLabel);

    /**
     * Imports a raw kickstart file.
     *
     * @param loggedInUser the current user
     * @param profileLabel the label of the new profile
     * @param virtualizationType the virtualization type
     * @param kickstartableTreeLabel the label of the kickstartable tree
     * @param kickstartFileContents the contents of the kickstart file
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Import a raw kickstart file into #product().",
        requestClass = ImportFileRequest.class,
        isIntegerResponse = true
    )
    int importRawFile(User loggedInUser, String profileLabel, String virtualizationType,
            String kickstartableTreeLabel, String kickstartFileContents);

    /**
     * Imports a raw kickstart file with an update type.
     *
     * @param loggedInUser the current user
     * @param profileLabel the label of the new profile
     * @param virtualizationType the virtualization type
     * @param kickstartableTreeLabel the label of the kickstartable tree
     * @param kickstartFileContents the contents of the kickstart file
     * @param updateType how the profile updates itself
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Import a raw kickstart file into #product().",
        requestClass = ImportRawFileWithUpdateTypeRequest.class,
        isIntegerResponse = true
    )
    int importRawFile(User loggedInUser, String profileLabel, String virtualizationType,
            String kickstartableTreeLabel, String kickstartFileContents, String updateType);

    @Schema(name = "ApiResponseKickstartList")
    interface KickstartListResponse extends ApiResponseWrapper<List<KickstartDoc>> { }

    @Schema(name = "ApiResponseKickstartIpRangeList")
    interface KickstartIpRangeListResponse extends ApiResponseWrapper<List<KickstartIpRangeDoc>> { }

    @Schema(name = "ApiResponseString")
    interface StringResponse extends ApiResponseWrapper<String> { }

    @Schema(name = "ApiResponseBoolean")
    interface BooleanResponse extends ApiResponseWrapper<Boolean> { }

    @Schema(name = "KickstartImportFileRequest")
    @JsonPropertyOrder({"profileLabel", "virtualizationType", "kickstartableTreeLabel", "kickstartFileContents"})
    interface ImportFileRequest {

        /**
         * @return the label of the new profile
         */
        @Schema(description = "Label for the new kickstart profile.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getProfileLabel();

        /**
         * @return the virtualization type
         */
        @Schema(description = "none, para_host, qemu, xenfv or xenpv.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getVirtualizationType();

        /**
         * @return the label of the kickstartable tree
         */
        @Schema(description = "Label of a kickstartable tree to associate the new profile with.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getKickstartableTreeLabel();

        /**
         * @return the contents of the kickstart file
         */
        @Schema(description = "Contents of the kickstart file to import.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getKickstartFileContents();
    }

    @Schema(name = "KickstartImportFileWithHostRequest")
    @JsonPropertyOrder({"profileLabel", "virtualizationType", "kickstartableTreeLabel", "kickstartHost",
        "kickstartFileContents"})
    interface ImportFileWithHostRequest {

        /**
         * @return the label of the new profile
         */
        @Schema(description = "Label for the new kickstart profile.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getProfileLabel();

        /**
         * @return the virtualization type
         */
        @Schema(description = "none, para_host, qemu, xenfv or xenpv.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getVirtualizationType();

        /**
         * @return the label of the kickstartable tree
         */
        @Schema(description = "Label of a kickstartable tree to associate the new profile with.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getKickstartableTreeLabel();

        /**
         * @return the kickstart hostname
         */
        @Schema(description = "Kickstart hostname (of a #product() server or proxy) used to construct " +
                "the default download URL for the new kickstart profile. Using this option signifies " +
                "that this default URL will be used instead of any url/nfs/cdrom/harddrive commands " +
                "in the kickstart file itself.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getKickstartHost();

        /**
         * @return the contents of the kickstart file
         */
        @Schema(description = "Contents of the kickstart file to import.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getKickstartFileContents();
    }

    @Schema(name = "KickstartImportFileWithUpdateTypeRequest")
    @JsonPropertyOrder({"profileLabel", "virtualizationType", "kickstartableTreeLabel", "kickstartHost",
        "kickstartFileContents", "updateType"})
    interface ImportFileWithUpdateTypeRequest extends ImportFileWithHostRequest {

        /**
         * @return how the profile updates itself
         */
        @Schema(description = "Should the profile update itself to use the newest tree available? " +
                "Possible values are: none (default) or all (includes custom Kickstart Trees).",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getUpdateType();
    }

    @Schema(name = "KickstartCreateProfileRequest")
    @JsonPropertyOrder({"profileLabel", "virtualizationType", "kickstartableTreeLabel", "kickstartHost",
        "rootPassword", "updateType"})
    interface CreateProfileRequest {

        /**
         * @return the label of the new profile
         */
        @Schema(description = "Label for the new kickstart profile.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getProfileLabel();

        /**
         * @return the virtualization type
         */
        @Schema(description = "none, para_host, qemu, xenfv or xenpv.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getVirtualizationType();

        /**
         * @return the label of the kickstartable tree
         */
        @Schema(description = "Label of a kickstartable tree to associate the new profile with.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getKickstartableTreeLabel();

        /**
         * @return the kickstart hostname
         */
        @Schema(description = "Kickstart hostname (of a #product() server or proxy) used to construct " +
                "the default download URL for the new kickstart profile.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getKickstartHost();

        /**
         * @return the root password
         */
        @Schema(description = "Root password.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getRootPassword();

        /**
         * @return how the profile updates itself
         */
        @Schema(description = "Should the profile update itself to use the newest tree available? " +
                "Possible values are: none (default) or all (includes custom Kickstart Trees).")
        String getUpdateType();
    }

    @Schema(name = "KickstartCreateProfileWithCustomUrlRequest")
    @JsonPropertyOrder({"profileLabel", "virtualizationType", "kickstartableTreeLabel", "downloadUrl",
        "rootPassword", "updateType"})
    interface CreateProfileWithCustomUrlRequest {

        /**
         * @return the label of the new profile
         */
        @Schema(description = "Label for the new kickstart profile.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getProfileLabel();

        /**
         * @return the virtualization type
         */
        @Schema(description = "none, para_host, qemu, xenfv or xenpv.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getVirtualizationType();

        /**
         * @return the label of the kickstartable tree
         */
        @Schema(description = "Label of a kickstartable tree to associate the new profile with.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getKickstartableTreeLabel();

        /**
         * @return the download URL
         */
        @Schema(description = "Download URL, or 'default' to use the kickstart tree's default URL.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "boolean")
        String getDownloadUrl();

        /**
         * @return the root password
         */
        @Schema(description = "Root password.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getRootPassword();

        /**
         * @return how the profile updates itself
         */
        @Schema(description = "Should the profile update itself to use the newest tree available? " +
                "Possible values are: none (default) or all (includes custom Kickstart Trees).")
        String getUpdateType();
    }

    @Schema(name = "KickstartDeleteProfileRequest")
    interface KsLabelRequest {

        /**
         * @return the label of the profile to remove
         */
        @Schema(description = "The label of the kickstart profile you want to remove",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();
    }

    @Schema(name = "KickstartDisableProfileRequest")
    @JsonPropertyOrder({"profileLabel", "disabled"})
    interface DisableProfileRequest {

        /**
         * @return the label of the profile
         */
        @Schema(description = "Label for the kickstart tree you want to en/disable",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getProfileLabel();

        /**
         * @return whether the profile is disabled
         */
        @Schema(description = "true to disable the profile", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "string")
        Boolean getDisabled();
    }

    @Schema(name = "KickstartRenameProfileRequest")
    @JsonPropertyOrder({"originalLabel", "newLabel"})
    interface RenameProfileRequest {

        /**
         * @return the label of the profile to rename
         */
        @Schema(description = "Label for the kickstart profile you want to rename",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getOriginalLabel();

        /**
         * @return the new label
         */
        @Schema(description = "new label to change to", requiredMode = Schema.RequiredMode.REQUIRED)
        String getNewLabel();
    }

    @Schema(name = "KickstartCloneProfileRequest")
    @JsonPropertyOrder({"ksLabelToClone", "newKsLabel"})
    interface CloneProfileRequest {

        /**
         * @return the label of the profile to clone
         */
        @Schema(description = "Label of the kickstart profile to clone",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabelToClone();

        /**
         * @return the label of the cloned profile
         */
        @Schema(description = "label of the cloned profile", requiredMode = Schema.RequiredMode.REQUIRED)
        String getNewKsLabel();
    }

    @Schema(name = "KickstartImportRawFileWithUpdateTypeRequest")
    @JsonPropertyOrder({"profileLabel", "virtualizationType", "kickstartableTreeLabel", "kickstartFileContents",
        "updateType"})
    interface ImportRawFileWithUpdateTypeRequest extends ImportFileRequest {

        /**
         * @return how the profile updates itself
         */
        @Schema(description = "Should the profile update itself to use the newest tree available? " +
                "Possible values are: none (default) or all (includes custom Kickstart Trees).",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getUpdateType();
    }

    @Schema(name = "KickstartInfo", description = "kickstart")
    @JsonPropertyOrder({"label", "treeLabel", "name", "advancedMode", "orgDefault", "active", "updateType"})
    interface KickstartDoc {

        /**
         * @return the label of the kickstart
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the label of the kickstartable tree
         */
        @Schema(name = "tree_label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getTreeLabel();

        /**
         * @return the name of the kickstart
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return whether the kickstart is in advanced mode
         */
        @Schema(name = "advanced_mode", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getAdvancedMode();

        /**
         * @return whether the kickstart is the organization default
         */
        @Schema(name = "org_default", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getOrgDefault();

        /**
         * @return whether the kickstart is active
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getActive();

        /**
         * @return how the profile updates itself
         */
        @Schema(name = "update_type", requiredMode = Schema.RequiredMode.REQUIRED)
        String getUpdateType();
    }

    @Schema(name = "KickstartIpRangeInfo", description = "kickstart IP range")
    @JsonPropertyOrder({"ksLabel", "max", "min"})
    interface KickstartIpRangeDoc {

        /**
         * @return the kickstart label associated with the IP range
         */
        @Schema(description = "the kickstart label associated with the IP range",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return the max IP of the range
         */
        @Schema(description = "the max IP of the range", requiredMode = Schema.RequiredMode.REQUIRED)
        String getMax();

        /**
         * @return the min IP of the range
         */
        @Schema(description = "the min IP of the range", requiredMode = Schema.RequiredMode.REQUIRED)
        String getMin();
    }
}
