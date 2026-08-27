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
package com.redhat.rhn.frontend.xmlrpc.system.config;

import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ConfigFileNameDto;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link ServerConfigHandler}.
 */
@Tag(name = "system.config", description = "Provides methods to access and modify many aspects of configuration " +
        "channels and server association. basically system.config name space")
public interface ServerConfigHandlerApi {

    /**
     * Lists the files in a given server.
     *
     * @param loggedInUser the current user
     * @param sid the server ID
     * @param listLocal whether the local override channel is listed
     * @return the files of the server
     */
    @ApiEndpointDoc(
        summary = "Return the list of files in a given channel.",
        method = HttpMethod.get,
        responseClass = ConfigFileNameListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "configuration file information")
    )
    List<ConfigFileNameDto> listFiles(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid,
        @Parameter(name = "listLocal", in = ParameterIn.QUERY, required = true,
            schema = @Schema(allowableValues = {"true", "false"},
                extensions = @Extension(name = "x-uyuni-doc-option-descriptions", properties = {
                    @ExtensionProperty(name = "true",
                        value = "to return configuration files in the system's local override configuration channel"),
                    @ExtensionProperty(name = "false",
                        value = "to return configuration files in the system's sandbox configuration channel")
                }))) Boolean listLocal);

    /**
     * Creates or updates a path on a server.
     *
     * @param loggedInUser the current user
     * @param sid the server ID
     * @param path the configuration file or directory path
     * @param isDir whether the path is a directory
     * @param data the properties of the path
     * @param commitToLocal whether the path is committed to the local override channel
     * @return the created or updated configuration revision
     */
    @ApiEndpointDoc(
        summary = "Create a new file (text or binary) or directory with the given path, or update an existing " +
            "path on a server.",
        requestClass = CreatePathRequest.class,
        responseClass = ConfigRevisionResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "configuration revision information")
    )
    ConfigRevision createOrUpdatePath(User loggedInUser, Integer sid, String path, Boolean isDir,
        Map<String, Object> data, Boolean commitToLocal);

    /**
     * Creates or updates a symbolic link on a server.
     *
     * @param loggedInUser the current user
     * @param sid the server ID
     * @param path the configuration file or directory path
     * @param data the properties of the symbolic link
     * @param commitToLocal whether the path is committed to the local override channel
     * @return the created or updated configuration revision
     */
    @ApiEndpointDoc(
        summary = "Create a new symbolic link with the given path, or update an existing path.",
        requestClass = CreateSymlinkRequest.class,
        responseClass = ConfigRevisionResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "configuration revision information")
    )
    ConfigRevision createOrUpdateSymlink(User loggedInUser, Integer sid, String path, Map<String, Object> data,
        Boolean commitToLocal);

    /**
     * Looks up the latest revisions of the given paths.
     *
     * @param loggedInUser the current user
     * @param sid the server ID
     * @param paths the paths to look up
     * @param searchLocal whether the local override channel is searched
     * @return the latest revisions of the requested paths
     */
    @ApiEndpointDoc(
        summary = "Given a list of paths and a server, returns details about the latest revisions of the paths.",
        method = HttpMethod.get,
        responseClass = ConfigRevisionListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "configuration revision information")
    )
    List<ConfigRevision> lookupFileInfo(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid,
        @Parameter(name = "paths", description = "paths to lookup on.",
            in = ParameterIn.QUERY, required = true) List<String> paths,
        @Parameter(name = "searchLocal", in = ParameterIn.QUERY, required = true,
            schema = @Schema(allowableValues = {"1", "0"},
                extensions = @Extension(name = "x-uyuni-doc-option-descriptions", properties = {
                    @ExtensionProperty(name = "1",
                        value = "to search configuration file paths in the system's local override configuration " +
                            "or systems subscribed central channels"),
                    @ExtensionProperty(name = "0",
                        value = "to search configuration file paths in the system's sandbox configuration channel")
                }))) Boolean searchLocal);

    /**
     * Removes file paths from a server.
     *
     * @param loggedInUser the current user
     * @param sid the server ID
     * @param paths the paths to remove
     * @param deleteFromLocal whether the paths are removed from the local override channel
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Removes file paths from a local or sandbox channel of a server.",
        requestClass = DeleteFilesRequest.class,
        isIntegerResponse = true
    )
    int deleteFiles(User loggedInUser, Integer sid, List<String> paths, Boolean deleteFromLocal);

    /**
     * Schedules a deploy action for all the configuration files of the given systems.
     *
     * @param loggedInUser the current user
     * @param sids the IDs of the systems
     * @param date the earliest date for the deploy action
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Schedules a deploy action for all the configuration files on the given list of systems.",
        requestClass = DeployAllRequest.class,
        isIntegerResponse = true
    )
    int deployAll(User loggedInUser, List<Number> sids, Date date);

    /**
     * Lists the global configuration channels of a system.
     *
     * @param loggedInUser the current user
     * @param sid the server ID
     * @return the global configuration channels of the system
     */
    @ApiEndpointDoc(
        summary = "List all global('Normal', 'State') configuration channels associated to a system in the order " +
            "of their ranking.",
        method = HttpMethod.get,
        responseClass = ConfigChannelListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "configuration channel information")
    )
    List<ConfigChannel> listChannels(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Appends configuration channels to the subscribed channels of the given systems.
     *
     * @param loggedInUser the current user
     * @param sids the IDs of the systems
     * @param configChannelLabels the labels of the configuration channels
     * @param addToTop whether the channels are prepended
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Given a list of servers and configuration channels, this method appends the configuration " +
            "channels to either the top or the bottom (whichever you specify) of a system's subscribed " +
            "configuration channels list. The ordering of the configuration channels provided in the add list " +
            "is maintained while adding. If one of the configuration channels in the 'add' list has been " +
            "previously subscribed by a server, the subscribed channel will be re-ranked to the appropriate place.",
        requestClass = AddChannelsRequest.class,
        isIntegerResponse = true
    )
    int addChannels(User loggedInUser, List<Number> sids, List<String> configChannelLabels, Boolean addToTop);

    /**
     * Replaces the configuration channels of the given systems.
     *
     * @param loggedInUser the current user
     * @param sids the IDs of the systems
     * @param configChannelLabels the labels of the configuration channels
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Replace the existing set of config channels on the given servers. Channels are ranked " +
            "according to their order in the configChannelLabels array.",
        requestClass = SetChannelsRequest.class,
        isIntegerResponse = true
    )
    int setChannels(User loggedInUser, List<Number> sids, List<String> configChannelLabels);

    /**
     * Removes configuration channels from the given systems.
     *
     * @param loggedInUser the current user
     * @param sids the IDs of the systems
     * @param configChannelLabels the labels of the configuration channels
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Remove config channels from the given servers.",
        requestClass = RemoveChannelsRequest.class,
        isIntegerResponse = true
    )
    int removeChannels(User loggedInUser, List<Number> sids, List<String> configChannelLabels);

    /**
     * Schedules highstate application for the given systems.
     *
     * @param user the current user
     * @param sids the IDs of the systems
     * @param earliest the earliest occurrence
     * @param test whether the states are run in test-only mode
     * @return the ID of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Schedule highstate application for a given system.",
        requestClass = ScheduleApplyRequest.class,
        responseClass = ActionIdResponse.class,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(type = "int", name = "actionId")
    )
    Long scheduleApplyConfigChannel(User user, List<Integer> sids, Date earliest, Boolean test);

    @Schema(name = "ApiResponseConfigFileNameList")
    interface ConfigFileNameListResponse extends ApiResponseWrapper<List<ConfigFileNameDoc>> { }

    @Schema(name = "ApiResponseConfigRevision")
    interface ConfigRevisionResponse extends ApiResponseWrapper<ConfigRevisionDoc> { }

    @Schema(name = "ApiResponseConfigRevisionList")
    interface ConfigRevisionListResponse extends ApiResponseWrapper<List<ConfigRevisionDoc>> { }

    @Schema(name = "ApiResponseConfigChannelList")
    interface ConfigChannelListResponse extends ApiResponseWrapper<List<ConfigChannelDoc>> { }

    @Schema(name = "ApiResponseConfigActionId")
    interface ActionIdResponse extends ApiResponseWrapper<Integer> { }

    @Schema(name = "ServerConfigCreatePathRequest")
    @JsonPropertyOrder({"sid", "path", "isDir", "data", "commitToLocal"})
    interface CreatePathRequest {

        /**
         * @return the server ID
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return the configuration file or directory path
         */
        @Schema(description = "the configuration file/directory path", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPath();

        /**
         * @return whether the path is a directory
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"True", "False"},
                extensions = @Extension(name = "x-uyuni-doc-option-descriptions", properties = {
                    @ExtensionProperty(name = "True", value = "if the path is a directory"),
                    @ExtensionProperty(name = "False", value = "if the path is a file")
                }))
        Boolean getIsDir();

        /**
         * @return the properties of the path
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        PathDataDoc getData();

        /**
         * @return whether the path is committed to the local override channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"1", "0"},
                extensions = @Extension(name = "x-uyuni-doc-option-descriptions", properties = {
                    @ExtensionProperty(name = "1",
                        value = "to commit configuration files to the system's local override configuration channel"),
                    @ExtensionProperty(name = "0",
                        value = "to commit configuration files to the system's sandbox configuration channel")
                }))
        Boolean getCommitToLocal();
    }

    @Schema(name = "ServerConfigCreateSymlinkRequest")
    @JsonPropertyOrder({"sid", "path", "data", "commitToLocal"})
    interface CreateSymlinkRequest {

        /**
         * @return the server ID
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return the configuration file or directory path
         */
        @Schema(description = "the configuration file/directory path", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPath();

        /**
         * @return the properties of the symbolic link
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        SymlinkDataDoc getData();

        /**
         * @return whether the path is committed to the local override channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"1", "0"},
                extensions = @Extension(name = "x-uyuni-doc-option-descriptions", properties = {
                    @ExtensionProperty(name = "1",
                        value = "to commit configuration files to the system's local override configuration channel"),
                    @ExtensionProperty(name = "0",
                        value = "to commit configuration files to the system's sandbox configuration channel")
                }))
        Boolean getCommitToLocal();
    }

    @Schema(name = "ServerConfigDeleteFilesRequest")
    @JsonPropertyOrder({"sid", "paths", "deleteFromLocal"})
    interface DeleteFilesRequest {

        /**
         * @return the server ID
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return the paths to remove
         */
        @Schema(description = "paths to remove.", requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getPaths();

        /**
         * @return whether the paths are removed from the local override channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"True", "False"},
                extensions = @Extension(name = "x-uyuni-doc-option-descriptions", properties = {
                    @ExtensionProperty(name = "True",
                        value = "to delete configuration file paths from the system's local override " +
                            "configuration channel"),
                    @ExtensionProperty(name = "False",
                        value = "to delete configuration file paths from the system's sandbox configuration channel")
                }))
        Boolean getDeleteFromLocal();
    }

    @Schema(name = "ServerConfigDeployAllRequest")
    @JsonPropertyOrder({"sids", "date"})
    interface DeployAllRequest {

        /**
         * @return the IDs of the systems
         */
        @Schema(description = "IDs of the systems to schedule configuration files deployment",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getSids();

        /**
         * @return the earliest date for the deploy action
         */
        @Schema(description = "Earliest date for the deploy action.", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getDate();
    }

    @Schema(name = "ServerConfigAddChannelsRequest")
    @JsonPropertyOrder({"sids", "configChannelLabels", "addToTop"})
    interface AddChannelsRequest {

        /**
         * @return the IDs of the systems
         */
        @Schema(description = "IDs of the systems to add the channels to.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getSids();

        /**
         * @return the labels of the configuration channels
         */
        @Schema(description = "List of configuration channel labels in the ranked order.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getConfigChannelLabels();

        /**
         * @return whether the channels are prepended
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"true", "false"},
                extensions = @Extension(name = "x-uyuni-doc-option-descriptions", properties = {
                    @ExtensionProperty(name = "true",
                        value = "to prepend the given channels list to the top of the configuration channels " +
                            "list of a server"),
                    @ExtensionProperty(name = "false",
                        value = "to append the given  channels list to the bottom of the configuration channels " +
                            "list of a server")
                }))
        Boolean getAddToTop();
    }

    @Schema(name = "ServerConfigSetChannelsRequest")
    @JsonPropertyOrder({"sids", "configChannelLabels"})
    interface SetChannelsRequest {

        /**
         * @return the IDs of the systems
         */
        @Schema(description = "IDs of the systems to set the channels on.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getSids();

        /**
         * @return the labels of the configuration channels
         */
        @Schema(description = "List of configuration channel labels in the ranked order.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getConfigChannelLabels();
    }

    @Schema(name = "ServerConfigRemoveChannelsRequest")
    @JsonPropertyOrder({"sids", "configChannelLabels"})
    interface RemoveChannelsRequest {

        /**
         * @return the IDs of the systems
         */
        @Schema(description = "the IDs of the systems from which you would like to remove configuration channels..",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getSids();

        /**
         * @return the labels of the configuration channels
         */
        @Schema(description = "List of configuration channel labels to remove.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getConfigChannelLabels();
    }

    @Schema(name = "ServerConfigScheduleApplyRequest")
    @JsonPropertyOrder({"sids", "earliestOccurrence", "test"})
    interface ScheduleApplyRequest {

        /**
         * @return the IDs of the systems
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getSids();

        /**
         * @return the earliest occurrence
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();

        /**
         * @return whether the states are run in test-only mode
         */
        @Schema(description = "Run states in test-only mode", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getTest();
    }

    @Schema(name = "ServerConfigPathData")
    @JsonPropertyOrder({"contents", "contentsEnc64", "owner", "group", "permissions", "macroStartDelimiter",
        "macroEndDelimiter", "selinuxCtx", "revision", "binary"})
    interface PathDataDoc {

        /**
         * @return the contents of the file
         */
        @Schema(description = "Contents of the file (text or base64 encoded if binary) ((only for non-directories)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getContents();

        /**
         * @return whether the content is base64 encoded
         */
        @Schema(name = "contents_enc64",
                description = "Identifies base64 encoded content (default: disabled, only for non-directories).",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getContentsEnc64();

        /**
         * @return the owner of the file or directory
         */
        @Schema(description = "Owner of the file/directory.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getOwner();

        /**
         * @return the group of the file or directory
         */
        @Schema(description = "Group name of the file/directory.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getGroup();

        /**
         * @return the permissions of the file or directory
         */
        @Schema(description = "Octal file/directory permissions (eg: 644)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getPermissions();

        /**
         * @return the macro start delimiter
         */
        @Schema(name = "macro-start-delimiter",
                description = "Config file macro end delimiter. Use null or empty string to accept the default. " +
                    "(only for non-directories)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getMacroStartDelimiter();

        /**
         * @return the macro end delimiter
         */
        @Schema(name = "macro-end-delimiter",
                description = "Config file macro end delimiter. Use null or empty string to accept the default. " +
                    "(only for non-directories)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getMacroEndDelimiter();

        /**
         * @return the SELinux context
         */
        @Schema(name = "selinux_ctx", description = "SeLinux context (optional)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSelinuxCtx();

        /**
         * @return the revision number
         */
        @Schema(description = "next revision number, auto increment for null",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getRevision();

        /**
         * @return whether the content is binary
         */
        @Schema(description = "mark the binary content, if True, base64 encoded content is expected " +
                    "(only for non-directories)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getBinary();
    }

    @Schema(name = "ServerConfigSymlinkData")
    @JsonPropertyOrder({"targetPath", "selinuxCtx", "revision"})
    interface SymlinkDataDoc {

        /**
         * @return the target path of the symbolic link
         */
        @Schema(name = "target_path", description = "The target path for the symbolic link",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getTargetPath();

        /**
         * @return the SELinux context
         */
        @Schema(name = "selinux_ctx", description = "SELinux Security context (optional)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSelinuxCtx();

        /**
         * @return the revision number
         */
        @Schema(description = "next revision number, auto increment for null",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getRevision();
    }

    @Schema(name = "ConfigFileNameInfo")
    @JsonPropertyOrder({"type", "path", "channelLabel", "lastModified", "channelType"})
    interface ConfigFileNameDoc {

        /**
         * @return the type of the path
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"file", "directory", "symlink"})
        String getType();

        /**
         * @return the path of the file
         */
        @Schema(description = "File Path", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPath();

        /**
         * @return the label of the central configuration channel
         */
        @Schema(name = "channel_label",
                description = "the label of the  central configuration channel that has this file. Note this " +
                    "entry only shows up if the file has not been overridden by a central channel.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();

        /**
         * @return the type of the configuration channel
         */
        @Schema(name = "channel_type", requiredMode = Schema.RequiredMode.REQUIRED)
        ConfigChannelTypeDoc getChannelType();

        /**
         * @return the last modification date
         */
        @Schema(name = "last_modified", description = "Last Modified Date",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getLastModified();
    }

    @Schema(name = "ConfigRevisionInfo")
    @JsonPropertyOrder({"type", "path", "targetPath", "channel", "contents", "contentsEnc64", "revision", "creation",
        "modified", "owner", "group", "permissions", "permissionsMode", "selinuxCtx", "binary", "sha256",
        "macroStartDelimiter", "macroEndDelimiter"})
    interface ConfigRevisionDoc {

        /**
         * @return the type of the path
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"file", "directory", "symlink"})
        String getType();

        /**
         * @return the path of the file
         */
        @Schema(description = "File Path", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPath();

        /**
         * @return the target path of the symbolic link
         */
        @Schema(name = "target_path",
                description = "Symbolic link Target File Path. Present for Symbolic links only.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getTargetPath();

        /**
         * @return the name of the channel
         */
        @Schema(description = "Channel Name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannel();

        /**
         * @return the contents of the file
         */
        @Schema(description = "File contents (base64 encoded according to the contents_enc64 attribute)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getContents();

        /**
         * @return whether the content is base64 encoded
         */
        @Schema(name = "contents_enc64", description = " Identifies base64 encoded content",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getContentsEnc64();

        /**
         * @return the revision number
         */
        @Schema(description = "File Revision", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getRevision();

        /**
         * @return the creation date
         */
        @Schema(description = "Creation Date", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getCreation();

        /**
         * @return the last modification date
         */
        @Schema(description = "Last Modified Date", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getModified();

        /**
         * @return the owner of the file
         */
        @Schema(description = "File Owner. Present for files or directories only.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getOwner();

        /**
         * @return the group of the file
         */
        @Schema(description = "File Group. Present for files or directories only.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getGroup();

        /**
         * @return the permissions of the file
         */
        @Schema(description = "File Permissions (Deprecated). Present for files or directories only.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getPermissions();

        /**
         * @return the permissions mode of the file
         */
        @Schema(name = "permissions_mode",
                description = "File Permissions. Present for files or directories only.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getPermissionsMode();

        /**
         * @return the SELinux context
         */
        @Schema(name = "selinux_ctx", description = "SELinux Context (optional).",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSelinuxCtx();

        /**
         * @return whether the file is binary
         */
        @Schema(description = "true/false , Present for files only.", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getBinary();

        /**
         * @return the sha256 signature of the file
         */
        @Schema(description = "File's sha256 signature. Present for files only.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSha256();

        /**
         * @return the macro start delimiter
         */
        @Schema(name = "macro-start-delimiter",
                description = "Macro start delimiter for a config file. Present for text files only.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getMacroStartDelimiter();

        /**
         * @return the macro end delimiter
         */
        @Schema(name = "macro-end-delimiter",
                description = "Macro end delimiter for a config file. Present for text files only.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getMacroEndDelimiter();
    }

    @Schema(name = "ConfigChannelInfo")
    @JsonPropertyOrder({"id", "orgId", "label", "name", "description", "configChannelType"})
    interface ConfigChannelDoc {

        /**
         * @return the ID of the channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the ID of the organization
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getOrgId();

        /**
         * @return the label of the channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the name of the channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the description of the channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();

        /**
         * @return the type of the channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        ConfigChannelTypeDoc getConfigChannelType();
    }

    @Schema(name = "ConfigChannelTypeInfo")
    @LegacyDocResponse(name = "configuration channel type information")
    @JsonPropertyOrder({"id", "label", "name", "priority"})
    interface ConfigChannelTypeDoc {

        /**
         * @return the ID of the channel type
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the label of the channel type
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the name of the channel type
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the priority of the channel type
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getPriority();
    }
}
