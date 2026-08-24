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
package com.redhat.rhn.frontend.xmlrpc.configchannel;

import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.config.EncodedConfigRevision;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ConfigChannelDto;
import com.redhat.rhn.frontend.dto.ConfigFileDto;
import com.redhat.rhn.frontend.dto.ConfigSystemDto;
import com.redhat.rhn.frontend.xmlrpc.system.config.ServerConfigHandlerApi;
import com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandlerApi;

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
 * API contract for {@link ConfigChannelHandler}.
 */
@Tag(name = "configchannel",
    description = "Provides methods to access and modify many aspects of configuration channels.")
public interface ConfigChannelHandlerApi {

    /**
     * Creates a new global config channel.
     *
     * @param loggedInUser the current user
     * @param label the label of the config channel
     * @param name the name of the config channel
     * @param description the description of the config channel
     * @return the newly created config channel
     */
    @ApiEndpointDoc(
        summary = "Create a new global config channel. Caller must be at least a config admin or " +
            "an organization admin.",
        requestClass = CreateRequest.class,
        responseClass = ConfigChannelResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "configuration channel information")
    )
    ConfigChannel create(User loggedInUser, String label, String name, String description);

    /**
     * Creates a new global config channel of the given type.
     *
     * @param loggedInUser the current user
     * @param label the label of the config channel
     * @param name the name of the config channel
     * @param description the description of the config channel
     * @param type the type of the config channel
     * @return the newly created config channel
     */
    @ApiEndpointDoc(
        summary = "Create a new global config channel. Caller must be at least a config admin or " +
            "an organization admin.",
        requestClass = CreateWithTypeRequest.class,
        responseClass = ConfigChannelResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "configuration channel information")
    )
    ConfigChannel create(User loggedInUser, String label, String name, String description, String type);

    /**
     * Creates a new global config channel of the given type with an init.sls file.
     *
     * @param loggedInUser the current user
     * @param label the label of the config channel
     * @param name the name of the config channel
     * @param description the description of the config channel
     * @param type the type of the config channel
     * @param pathInfo the path info
     * @return the newly created config channel
     */
    @ApiEndpointDoc(
        summary = "Create a new global config channel. Caller must be at least a config admin or " +
            "an organization admin.",
        requestClass = CreateWithPathInfoRequest.class,
        responseClass = ConfigChannelResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "configuration channel information")
    )
    ConfigChannel create(User loggedInUser, String label, String name, String description, String type,
            Map<String, Object> pathInfo);

    /**
     * Deletes the specified revisions of a given configuration file.
     *
     * @param loggedInUser the current user
     * @param label the label of the config channel to lookup on
     * @param filePath the configuration file path
     * @param revisions the list of revisions to delete
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete specified revisions of a given configuration file",
        requestClass = DeleteFileRevisionsRequest.class,
        isIntegerResponse = true
    )
    int deleteFileRevisions(User loggedInUser, String label, String filePath, List<Integer> revisions);

    /**
     * Returns the list of revisions for the specified config file.
     *
     * @param loggedInUser the current user
     * @param label the label of the config channel to lookup on
     * @param filePath the config file path to examine
     * @return the list of revisions
     */
    @ApiEndpointDoc(
        summary = "Get list of revisions for specified config file",
        method = HttpMethod.get,
        responseClass = ServerConfigHandlerApi.ConfigRevisionListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "configuration revision information")
    )
    List getFileRevisions(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "label", in = ParameterIn.QUERY, required = true,
                description = "label of config channel to lookup on") String label,
        @Parameter(name = "filePath", in = ParameterIn.QUERY, required = true,
                description = "config file path to examine") String filePath);

    /**
     * Returns the given revision of the specified config file.
     *
     * @param loggedInUser the current user
     * @param label the label of the config channel to lookup on
     * @param filePath the config file path to examine
     * @param revision the config file revision to examine
     * @return the config revision
     */
    @ApiEndpointDoc(
        summary = "Get revision of the specified config file",
        method = HttpMethod.get,
        responseClass = ServerConfigHandlerApi.ConfigRevisionResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "configuration revision information")
    )
    ConfigRevision getFileRevision(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "label", in = ParameterIn.QUERY, required = true,
                description = "label of config channel to lookup on") String label,
        @Parameter(name = "filePath", in = ParameterIn.QUERY, required = true,
                description = "config file path to examine") String filePath,
        @Parameter(name = "revision", in = ParameterIn.QUERY, required = true,
                description = "config file revision to examine") Integer revision);

    /**
     * Synchronizes all files on the disk to the current state of the database.
     *
     * @param loggedInUser the current user
     * @param labels the configuration channel labels to synchronize files from
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Synchronize all files on the disk to the current state of the database.",
        requestClass = SyncSaltFilesOnDiskRequest.class,
        isIntegerResponse = true
    )
    int syncSaltFilesOnDisk(User loggedInUser, List<String> labels);

    /**
     * Returns the given revision of the specified config file, base64 encoded.
     *
     * @param loggedInUser the current user
     * @param label the label of the config channel to lookup on
     * @param filePath the config file path to examine
     * @param revision the config file revision to examine
     * @return the config revision
     */
    @ApiEndpointDoc(
        summary = "Get revision of the specified configuration file and transmit the contents as " +
            "base64 encoded.",
        method = HttpMethod.get,
        responseClass = ServerConfigHandlerApi.ConfigRevisionResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "configuration revision information")
    )
    EncodedConfigRevision getEncodedFileRevision(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "label", in = ParameterIn.QUERY, required = true,
                description = "label of config channel to lookup on") String label,
        @Parameter(name = "filePath", in = ParameterIn.QUERY, required = true,
                description = "config file path to examine") String filePath,
        @Parameter(name = "revision", in = ParameterIn.QUERY, required = true,
                description = "config file revision to examine") Integer revision);

    /**
     * Looks up the details of a config channel by label.
     *
     * @param loggedInUser the current user
     * @param label the label of the config channel
     * @return the config channel
     */
    @ApiEndpointDoc(
        summary = "Lookup config channel details.",
        method = HttpMethod.get,
        responseClass = ConfigChannelResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "configuration channel information")
    )
    ConfigChannel getDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "label", in = ParameterIn.QUERY, required = true) String label);

    /**
     * Looks up the details of a config channel by ID.
     *
     * @param loggedInUser the current user
     * @param id the ID of the config channel
     * @return the config channel
     */
    @ApiEndpointDoc(
        summary = "Lookup config channel details.",
        method = HttpMethod.get,
        responseClass = ConfigChannelResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "configuration channel information")
    )
    ConfigChannel getDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "id", in = ParameterIn.QUERY, required = true,
                description = "the channel ID") Integer id);

    /**
     * Updates a global config channel.
     *
     * @param loggedInUser the current user
     * @param label the label of the config channel
     * @param name the name of the config channel
     * @param description the description of the config channel
     * @return the updated config channel
     */
    @ApiEndpointDoc(
        summary = "Update a global config channel. Caller must be at least a config admin or an " +
            "organization admin, or have access to a system containing this config channel.",
        requestClass = UpdateRequest.class,
        responseClass = ConfigChannelResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "configuration channel information")
    )
    ConfigChannel update(User loggedInUser, String label, String name, String description);

    /**
     * Lists details on a list of channels given their channel labels.
     *
     * @param loggedInUser the current user
     * @param labels the channel labels
     * @return the list of config channels
     */
    @ApiEndpointDoc(
        summary = "Lists details on a list of channels given their channel labels.",
        method = HttpMethod.get,
        responseClass = ServerConfigHandlerApi.ConfigChannelListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "configuration channel information")
    )
    List<ConfigChannel> lookupChannelInfo(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "labels", in = ParameterIn.QUERY, required = true,
                description = "the channel labels") List<String> labels);

    /**
     * Lists all the global config channels accessible to the logged-in user.
     *
     * @param loggedInUser the current user
     * @return the list of config channels
     */
    @ApiEndpointDoc(
        summary = "List all the global config channels accessible to the logged-in user.",
        method = HttpMethod.get,
        responseClass = ConfigChannelDtoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "configuration channel information")
    )
    List<ConfigChannelDto> listGlobals(@Parameter(hidden = true) User loggedInUser);

    /**
     * Updates the init.sls file for the given state channel.
     *
     * @param loggedInUser the current user
     * @param label the channel label
     * @param pathInfo the path info
     * @return the new config revision
     */
    @ApiEndpointDoc(
        summary = "Update the init.sls file for the given state channel. User can only update " +
            "contents, nothing else.",
        requestClass = UpdateInitSlsRequest.class,
        responseClass = ServerConfigHandlerApi.ConfigRevisionResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "configuration revision information")
    )
    ConfigRevision updateInitSls(User loggedInUser, String label, Map<String, Object> pathInfo);

    /**
     * Creates a new file or directory with the given path, or updates an existing path.
     *
     * @param loggedInUser the current user
     * @param label the channel label
     * @param path the path of the file or directory
     * @param isDir whether the path is a directory
     * @param pathInfo the path info
     * @return the new config revision
     */
    @ApiEndpointDoc(
        summary = "Create a new file or directory with the given path, or update an existing path.",
        requestClass = CreateOrUpdatePathRequest.class,
        responseClass = ServerConfigHandlerApi.ConfigRevisionResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "configuration revision information")
    )
    ConfigRevision createOrUpdatePath(User loggedInUser, String label, String path, boolean isDir,
            Map<String, Object> pathInfo);

    /**
     * Creates a new symbolic link with the given path, or updates an existing path.
     *
     * @param loggedInUser the current user
     * @param label the channel label
     * @param path the path of the symbolic link
     * @param pathInfo the path info
     * @return the new config revision
     */
    @ApiEndpointDoc(
        summary = "Create a new symbolic link with the given path, or update an existing path in " +
            "config channel of 'normal' type.",
        requestClass = CreateOrUpdateSymlinkRequest.class,
        responseClass = ServerConfigHandlerApi.ConfigRevisionResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "configuration revision information")
    )
    ConfigRevision createOrUpdateSymlink(User loggedInUser, String label, String path,
            Map<String, Object> pathInfo);

    /**
     * Returns details about the latest revisions of the given paths.
     *
     * @param loggedInUser the current user
     * @param label the label of the config channel to lookup on
     * @param paths the list of paths to examine
     * @return the list of config revisions
     */
    @ApiEndpointDoc(
        summary = "Given a list of paths and a channel, returns details about the latest " +
            "revisions of the paths.",
        method = HttpMethod.get,
        responseClass = ServerConfigHandlerApi.ConfigRevisionListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "configuration revision information")
    )
    List<ConfigRevision> lookupFileInfo(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "label", in = ParameterIn.QUERY, required = true,
                description = "label of config channel to lookup on") String label,
        @Parameter(name = "paths", in = ParameterIn.QUERY, required = true,
                description = "list of paths to examine") List<String> paths);

    /**
     * Returns details about the given revision of the given path.
     *
     * @param loggedInUser the current user
     * @param label the label of the config channel to lookup on
     * @param path the path of the file or directory
     * @param revision the revision number
     * @return the config revision
     */
    @ApiEndpointDoc(
        summary = "Given a path, revision number, and a channel, returns details about the latest " +
            "revisions of the paths.",
        method = HttpMethod.get,
        responseClass = ServerConfigHandlerApi.ConfigRevisionResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "configuration revision information")
    )
    ConfigRevision lookupFileInfo(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "label", in = ParameterIn.QUERY, required = true,
                description = "label of config channel to lookup on") String label,
        @Parameter(name = "path", in = ParameterIn.QUERY, required = true,
                description = "path of file/directory") String path,
        @Parameter(name = "revision", in = ParameterIn.QUERY, required = true,
                description = "the revision number") Integer revision);

    /**
     * Returns the list of files in a channel.
     *
     * @param loggedInUser the current user
     * @param label the label of the config channel to list files on
     * @return the list of files
     */
    @ApiEndpointDoc(
        summary = "Return a list of files in a channel.",
        method = HttpMethod.get,
        responseClass = ConfigFileDtoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "configuration file information")
    )
    List<ConfigFileDto> listFiles(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "label", in = ParameterIn.QUERY, required = true,
                description = "label of config channel to list files on") String label);

    /**
     * Deletes a list of global config channels.
     *
     * @param loggedInUser the current user
     * @param labels the configuration channel labels to delete
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete a list of global config channels. Caller must be a config admin.",
        requestClass = DeleteChannelsRequest.class,
        isIntegerResponse = true
    )
    int deleteChannels(User loggedInUser, List<String> labels);

    /**
     * Removes file paths from a global channel.
     *
     * @param loggedInUser the current user
     * @param label the channel to remove the files from
     * @param paths the file paths to remove
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Remove file paths from a global channel.",
        requestClass = DeleteFilesRequest.class,
        isIntegerResponse = true
    )
    int deleteFiles(User loggedInUser, String label, List<String> paths);

    /**
     * Schedules a comparison of the latest revision of a file against the deployed version.
     *
     * @param loggedInUser the current user
     * @param label the label of the config channel
     * @param path the file path
     * @param sids the list of system IDs that the comparison will be performed on
     * @return the action ID of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Schedule a comparison of the latest revision of a file against the version " +
            "deployed on a list of systems.",
        requestClass = ScheduleFileComparisonsRequest.class,
        responseClass = ServerConfigHandlerApi.ActionIdResponse.class,
        responseDescription = "the action ID of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(type = "int", name = "actionId")
    )
    Integer scheduleFileComparisons(User loggedInUser, String label, String path, List<Integer> sids);

    /**
     * Checks for the existence of the given config channel.
     *
     * @param loggedInUser the current user
     * @param label the channel to check for
     * @return 1 if the channel exists, 0 otherwise
     */
    @ApiEndpointDoc(
        summary = "Check for the existence of the config channel provided.",
        requestClass = ChannelExistsRequest.class,
        isIntegerResponse = true,
        responseDescription = "1 if exists, 0 otherwise",
        legacyDocResponse = @LegacyDocResponse(name = "existence")
    )
    int channelExists(User loggedInUser, String label);

    /**
     * Schedules an immediate configuration deployment for all subscribed systems.
     *
     * @param loggedInUser the current user
     * @param label the configuration channel's label
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Schedule an immediate configuration deployment for all systems subscribed to a " +
            "particular configuration channel.",
        requestClass = DeployAllSystemsRequest.class,
        isIntegerResponse = true
    )
    int deployAllSystems(User loggedInUser, String label);

    /**
     * Schedules a configuration deployment for all subscribed systems at the given date.
     *
     * @param loggedInUser the current user
     * @param label the configuration channel's label
     * @param date the date to schedule the action
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Schedule a configuration deployment for all systems subscribed to a particular " +
            "configuration channel.",
        requestClass = DeployAllSystemsAtDateRequest.class,
        isIntegerResponse = true
    )
    int deployAllSystems(User loggedInUser, String label, Date date);

    /**
     * Schedules a configuration deployment of a certain file for all subscribed systems.
     *
     * @param loggedInUser the current user
     * @param label the configuration channel's label
     * @param filePath the configuration file path
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Schedule a configuration deployment of a certain file for all systems " +
            "subscribed to a particular configuration channel.",
        requestClass = DeployAllSystemsFileRequest.class,
        isIntegerResponse = true
    )
    int deployAllSystems(User loggedInUser, String label, String filePath);

    /**
     * Schedules a configuration deployment of a certain file for all subscribed systems at a date.
     *
     * @param loggedInUser the current user
     * @param label the configuration channel's label
     * @param filePath the configuration file path
     * @param date the date to schedule the action
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Schedule a configuration deployment of a certain file for all systems " +
            "subscribed to a particular configuration channel.",
        requestClass = DeployAllSystemsFileAtDateRequest.class,
        isIntegerResponse = true
    )
    int deployAllSystems(User loggedInUser, String label, String filePath, Date date);

    /**
     * Returns the list of systems subscribed to a configuration channel.
     *
     * @param loggedInUser the current user
     * @param label the label of the config channel
     * @return the list of subscribed systems
     */
    @ApiEndpointDoc(
        summary = "Return a list of systems subscribed to a configuration channel",
        method = HttpMethod.get,
        responseClass = ConfigSystemDtoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    List<ConfigSystemDto> listSubscribedSystems(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "label", in = ParameterIn.QUERY, required = true,
                description = "label of the config channel to list subscribed systems") String label);

    /**
     * Returns the list of groups a given configuration channel is assigned to.
     *
     * @param loggedInUser the current user
     * @param label the label of the config channel
     * @return the list of assigned system groups
     */
    @ApiEndpointDoc(
        summary = "Return a list of Groups where a given configuration channel is assigned to",
        method = HttpMethod.get,
        responseClass = ServerGroupHandlerApi.ServerGroupListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "server group")
    )
    List<ManagedServerGroup> listAssignedSystemGroups(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "label", in = ParameterIn.QUERY, required = true,
                description = "label of the config channel to list assigned groups") String label);

    @Schema(name = "ApiResponseConfigChannel")
    interface ConfigChannelResponse extends ApiResponseWrapper<ServerConfigHandlerApi.ConfigChannelDoc> { }

    @Schema(name = "ApiResponseConfigChannelDtoList")
    interface ConfigChannelDtoListResponse extends ApiResponseWrapper<List<ConfigChannelDtoDoc>> { }

    @Schema(name = "ApiResponseConfigFileDtoList")
    interface ConfigFileDtoListResponse extends ApiResponseWrapper<List<ConfigFileDtoDoc>> { }

    @Schema(name = "ApiResponseConfigSystemDtoList")
    interface ConfigSystemDtoListResponse extends ApiResponseWrapper<List<ConfigSystemDtoDoc>> { }

    @Schema(name = "ConfigChannelCreateRequest")
    @JsonPropertyOrder({"label", "name", "description"})
    interface CreateRequest {

        /**
         * @return the label of the config channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the name of the config channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the description of the config channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();
    }

    @Schema(name = "ConfigChannelCreateWithTypeRequest")
    @JsonPropertyOrder({"label", "name", "description", "type"})
    interface CreateWithTypeRequest {

        /**
         * @return the label of the config channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the name of the config channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the description of the config channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();

        /**
         * @return the type of the config channel
         */
        @Schema(description = "the channel type either 'normal' or 'state'",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getType();
    }

    @Schema(name = "ConfigChannelCreateWithPathInfoRequest")
    @JsonPropertyOrder({"label", "name", "description", "type", "pathInfo"})
    interface CreateWithPathInfoRequest {

        /**
         * @return the label of the config channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the name of the config channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the description of the config channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();

        /**
         * @return the type of the config channel
         */
        @Schema(description = "the channel type either 'normal' or 'state'",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getType();

        /**
         * @return the path info
         */
        @Schema(description = "the path info", requiredMode = Schema.RequiredMode.REQUIRED)
        CreatePathInfoDoc getPathInfo();
    }

    @Schema(name = "ConfigChannelCreatePathInfo")
    @JsonPropertyOrder({"contents", "contentsEnc64"})
    interface CreatePathInfoDoc {

        /**
         * @return the contents of the init.sls file
         */
        @Schema(description = "contents of the init.sls file", requiredMode = Schema.RequiredMode.REQUIRED)
        String getContents();

        /**
         * @return whether the content is base64 encoded
         */
        @Schema(name = "contents_enc64",
                description = "identifies base64 encoded content(default: disabled)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getContentsEnc64();
    }

    @Schema(name = "ConfigChannelDeleteFileRevisionsRequest")
    @JsonPropertyOrder({"label", "filePath", "revisions"})
    interface DeleteFileRevisionsRequest {

        /**
         * @return the label of the config channel to lookup on
         */
        @Schema(description = "label of config channel to lookup on",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the configuration file path
         */
        @Schema(description = "configuration file path", requiredMode = Schema.RequiredMode.REQUIRED)
        String getFilePath();

        /**
         * @return the list of revisions to delete
         */
        @Schema(description = "list of revisions to delete", requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getRevisions();
    }

    @Schema(name = "ConfigChannelSyncSaltFilesRequest")
    interface SyncSaltFilesOnDiskRequest {

        /**
         * @return the configuration channel labels to synchronize files from
         */
        @Schema(description = "configuration channel labels to synchronize files from",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getLabels();
    }

    @Schema(name = "ConfigChannelUpdateRequest")
    @JsonPropertyOrder({"label", "name", "description"})
    interface UpdateRequest {

        /**
         * @return the label of the config channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the name of the config channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the description of the config channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();
    }

    @Schema(name = "ConfigChannelUpdateInitSlsRequest")
    @JsonPropertyOrder({"label", "pathInfo"})
    interface UpdateInitSlsRequest {

        /**
         * @return the channel label
         */
        @Schema(description = "the channel label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the path info
         */
        @Schema(description = "the path info", requiredMode = Schema.RequiredMode.REQUIRED)
        InitSlsPathInfoDoc getPathInfo();
    }

    @Schema(name = "ConfigChannelInitSlsPathInfo")
    @JsonPropertyOrder({"contents", "contentsEnc64", "revision"})
    interface InitSlsPathInfoDoc {

        /**
         * @return the contents of the init.sls file
         */
        @Schema(description = "contents of the init.sls file", requiredMode = Schema.RequiredMode.REQUIRED)
        String getContents();

        /**
         * @return whether the content is base64 encoded
         */
        @Schema(name = "contents_enc64",
                description = "identifies base64 encoded content(default: disabled)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getContentsEnc64();

        /**
         * @return the next revision number
         */
        @Schema(description = "next revision number, auto increment for null",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getRevision();
    }

    @Schema(name = "ConfigChannelCreateOrUpdatePathRequest")
    @JsonPropertyOrder({"label", "path", "isDir", "pathInfo"})
    interface CreateOrUpdatePathRequest {

        /**
         * @return the channel label
         */
        @Schema(description = "the channel label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the path of the file or directory
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getPath();

        /**
         * @return whether the path is a directory
         */
        @Schema(description = "true if the path is a directory, False if it is a file",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getIsDir();

        /**
         * @return the path info
         */
        @Schema(description = "the path info", requiredMode = Schema.RequiredMode.REQUIRED)
        PathInfoDoc getPathInfo();
    }

    @Schema(name = "ConfigChannelPathInfo")
    @JsonPropertyOrder({"contents", "contentsEnc64", "owner", "group", "permissions", "selinuxCtx",
        "macroStartDelimiter", "macroEndDelimiter", "revision", "binary"})
    interface PathInfoDoc {

        /**
         * @return the contents of the file
         */
        @Schema(description = "contents of the file (text or base64 encoded if binary or want to " +
                "preserve control characters like LF, CR etc.)(only for non-directories)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getContents();

        /**
         * @return whether the content is base64 encoded
         */
        @Schema(name = "contents_enc64",
                description = "identifies base64 encoded content (default: disabled, only for " +
                        "non-directories)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getContentsEnc64();

        /**
         * @return the owner of the file or directory
         */
        @Schema(description = "owner of the file/directory", requiredMode = Schema.RequiredMode.REQUIRED)
        String getOwner();

        /**
         * @return the group name of the file or directory
         */
        @Schema(description = "group name of the file/directory",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getGroup();

        /**
         * @return the permissions of the file or directory
         */
        @Schema(description = "octal file/directory permissions (eg: 644)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getPermissions();

        /**
         * @return the SELinux security context
         */
        @Schema(name = "selinux_ctx", description = "SELinux Security context (optional)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSelinuxCtx();

        /**
         * @return the macro start delimiter
         */
        @Schema(name = "macro-start-delimiter",
                description = "config file macro start delimiter. Use null or empty string to " +
                        "accept the default. (only for non-directories)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getMacroStartDelimiter();

        /**
         * @return the macro end delimiter
         */
        @Schema(name = "macro-end-delimiter",
                description = "config file macro end delimiter. Use null or empty string to " +
                        "accept the default. (only for non-directories)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getMacroEndDelimiter();

        /**
         * @return the next revision number
         */
        @Schema(description = "next revision number, auto increment for null",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getRevision();

        /**
         * @return whether the content is binary
         */
        @Schema(description = "mark the binary content, if True, base64 encoded content is " +
                "expected (only for non-directories)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getBinary();
    }

    @Schema(name = "ConfigChannelCreateOrUpdateSymlinkRequest")
    @JsonPropertyOrder({"label", "path", "pathInfo"})
    interface CreateOrUpdateSymlinkRequest {

        /**
         * @return the channel label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the path of the symbolic link
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getPath();

        /**
         * @return the path info
         */
        @Schema(description = "the path info", requiredMode = Schema.RequiredMode.REQUIRED)
        SymlinkPathInfoDoc getPathInfo();
    }

    @Schema(name = "ConfigChannelSymlinkPathInfo")
    @JsonPropertyOrder({"targetPath", "selinuxCtx", "revision"})
    interface SymlinkPathInfoDoc {

        /**
         * @return the target path for the symbolic link
         */
        @Schema(name = "target_path", description = "the target path for the symbolic link",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getTargetPath();

        /**
         * @return the SELinux security context
         */
        @Schema(name = "selinux_ctx", description = "SELinux Security context (optional)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSelinuxCtx();

        /**
         * @return the next revision number
         */
        @Schema(description = "next revision number, skip this field for automatic revision " +
                "number assignment",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getRevision();
    }

    @Schema(name = "ConfigChannelDeleteChannelsRequest")
    interface DeleteChannelsRequest {

        /**
         * @return the configuration channel labels to delete
         */
        @Schema(description = "configuration channel labels to delete",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getLabels();
    }

    @Schema(name = "ConfigChannelDeleteFilesRequest")
    @JsonPropertyOrder({"label", "paths"})
    interface DeleteFilesRequest {

        /**
         * @return the channel to remove the files from
         */
        @Schema(description = "channel to remove the files from",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the file paths to remove
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getPaths();
    }

    @Schema(name = "ConfigChannelScheduleFileComparisonsRequest")
    @JsonPropertyOrder({"label", "path", "sids"})
    interface ScheduleFileComparisonsRequest {

        /**
         * @return the label of the config channel
         */
        @Schema(description = "label of config channel", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the file path
         */
        @Schema(description = "file path", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPath();

        /**
         * @return the list of system IDs
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getSids();
    }

    @Schema(name = "ConfigChannelChannelExistsRequest")
    interface ChannelExistsRequest {

        /**
         * @return the channel to check for
         */
        @Schema(description = "channel to check for", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();
    }

    @Schema(name = "ConfigChannelDeployAllSystemsRequest")
    interface DeployAllSystemsRequest {

        /**
         * @return the configuration channel's label
         */
        @Schema(description = "the configuration channel's label",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();
    }

    @Schema(name = "ConfigChannelDeployAllSystemsAtDateRequest")
    @JsonPropertyOrder({"label", "date"})
    interface DeployAllSystemsAtDateRequest {

        /**
         * @return the configuration channel's label
         */
        @Schema(description = "the configuration channel's label",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the date to schedule the action
         */
        @Schema(description = "the date to schedule the action",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getDate();
    }

    @Schema(name = "ConfigChannelDeployAllSystemsFileRequest")
    @JsonPropertyOrder({"label", "filePath"})
    interface DeployAllSystemsFileRequest {

        /**
         * @return the configuration channel's label
         */
        @Schema(description = "the configuration channel's label",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the configuration file path
         */
        @Schema(description = "the configuration file path",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getFilePath();
    }

    @Schema(name = "ConfigChannelDeployAllSystemsFileAtDateRequest")
    @JsonPropertyOrder({"label", "filePath", "date"})
    interface DeployAllSystemsFileAtDateRequest {

        /**
         * @return the configuration channel's label
         */
        @Schema(description = "the configuration channel's label",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the configuration file path
         */
        @Schema(description = "the configuration file path",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getFilePath();

        /**
         * @return the date to schedule the action
         */
        @Schema(description = "the date to schedule the action",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getDate();
    }

    @Schema(name = "ConfigChannelDtoInfo", description = "configuration channel information")
    @JsonPropertyOrder({"id", "orgId", "label", "name", "description", "type", "configChannelType"})
    interface ConfigChannelDtoDoc {

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
        String getType();

        /**
         * @return the type information of the channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        ServerConfigHandlerApi.ConfigChannelTypeDoc getConfigChannelType();
    }

    @Schema(name = "ConfigFileDtoInfo", description = "configuration file information")
    @JsonPropertyOrder({"type", "path", "lastModified"})
    interface ConfigFileDtoDoc {

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
         * @return the last modification date
         */
        @Schema(name = "last_modified", description = "Last Modified Date",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getLastModified();
    }

    @Schema(name = "ConfigSystemDtoInfo", description = "system")
    @JsonPropertyOrder({"id", "name"})
    interface ConfigSystemDtoDoc {

        /**
         * @return the ID of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the name of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();
    }
}
