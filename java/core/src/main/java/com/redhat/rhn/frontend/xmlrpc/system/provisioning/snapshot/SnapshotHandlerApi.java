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
package com.redhat.rhn.frontend.xmlrpc.system.provisioning.snapshot;

import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.rhnpackage.PackageNevra;
import com.redhat.rhn.domain.server.ServerSnapshot;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link SnapshotHandler}.
 */
@Tag(name = "system.provisioning.snapshot",
    description = "Provides methods to access and delete system snapshots.")
public interface SnapshotHandlerApi {

    /**
     * Lists the snapshots of a given system.
     *
     * @param loggedInUser the current user
     * @param sid the system id
     * @param startDate the start date
     * @param endDate the end date
     * @return the snapshots of the system
     */
    @ApiEndpointDoc(
        summary = "List snapshots for a given system.",
        method = HttpMethod.get,
        responseClass = ServerSnapshotListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "server snapshot")
    )
    List<ServerSnapshot> listSnapshots(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid,
        @Parameter(name = "startDate", in = ParameterIn.QUERY, required = true) Date startDate,
        @Parameter(name = "endDate", in = ParameterIn.QUERY, required = true) Date endDate);

    /**
     * Lists the snapshots of a given system.
     *
     * @param loggedInUser the current user
     * @param sid the system id
     * @param dateDetails the dates narrowing the snapshots to list
     * @return the snapshots of the system
     */
    @ApiEndpointDoc(
        summary = "List snapshots for a given system.",
        method = HttpMethod.get,
        responseClass = ServerSnapshotListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "server snapshot")
    )
    List<ServerSnapshot> listSnapshots(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid,
        @Parameter(name = "dateDetails", in = ParameterIn.QUERY, required = true,
            schema = @Schema(implementation = DateDetailsDoc.class)) Map<String, Date> dateDetails);

    /**
     * Lists the packages associated with a snapshot.
     *
     * @param loggedInUser the current user
     * @param snapId the snapshot id
     * @return the packages of the snapshot
     */
    @ApiEndpointDoc(
        summary = "List the packages associated with a snapshot.",
        method = HttpMethod.get,
        responseClass = PackageNevraListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "package nevra")
    )
    Set<PackageNevra> listSnapshotPackages(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "snapId", in = ParameterIn.QUERY, required = true) Integer snapId);

    /**
     * Lists the config files associated with a snapshot.
     *
     * @param loggedInUser the current user
     * @param snapId the snapshot id
     * @return the config files of the snapshot
     */
    @ApiEndpointDoc(
        summary = "List the config files associated with a snapshot.",
        method = HttpMethod.get,
        responseClass = ConfigRevisionListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "configuration revision information")
    )
    Set<ConfigRevision> listSnapshotConfigFiles(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "snapId", in = ParameterIn.QUERY, required = true) Integer snapId);

    /**
     * Deletes all snapshots across multiple systems based on the given date criteria.
     *
     * @param loggedInUser the current user
     * @param startDate the start date
     * @param endDate the end date
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Deletes all snapshots across multiple systems based on the given date criteria.",
        requestClass = DeleteSnapshotsByDateRequest.class,
        isIntegerResponse = true
    )
    int deleteSnapshots(User loggedInUser, Date startDate, Date endDate);

    /**
     * Deletes all snapshots of a system based on the given date criteria.
     *
     * @param loggedInUser the current user
     * @param sid the system id
     * @param startDate the start date
     * @param endDate the end date
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Deletes all snapshots for a given system based on the date criteria.",
        requestClass = DeleteSnapshotsBySystemAndDateRequest.class,
        isIntegerResponse = true
    )
    int deleteSnapshots(User loggedInUser, Integer sid, Date startDate, Date endDate);

    /**
     * Deletes all snapshots across multiple systems based on the given date criteria.
     *
     * @param loggedInUser the current user
     * @param dateDetails the dates narrowing the snapshots to delete
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Deletes all snapshots across multiple systems based on the given date criteria.",
        requestClass = DeleteSnapshotsByDateDetailsRequest.class,
        isIntegerResponse = true
    )
    int deleteSnapshots(User loggedInUser, Map dateDetails);

    /**
     * Deletes all snapshots of a system based on the given date criteria.
     *
     * @param loggedInUser the current user
     * @param sid the system id
     * @param dateDetails the dates narrowing the snapshots to delete
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Deletes all snapshots for a given system based on the date criteria.",
        requestClass = DeleteSnapshotsBySystemAndDateDetailsRequest.class,
        isIntegerResponse = true
    )
    int deleteSnapshots(User loggedInUser, Integer sid, Map<String, Date> dateDetails);

    /**
     * Deletes a snapshot with the given snapshot id.
     *
     * @param loggedInUser the current user
     * @param snapId the snapshot id
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Deletes a snapshot with the given snapshot id",
        requestClass = DeleteSnapshotRequest.class,
        isIntegerResponse = true
    )
    int deleteSnapshot(User loggedInUser, Integer snapId);

    /**
     * Adds a tag to a snapshot.
     *
     * @param loggedInUser the current user
     * @param snapId the snapshot id
     * @param tagName the name of the tag
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Adds tag to snapshot",
        requestClass = AddTagRequest.class,
        isIntegerResponse = true
    )
    int addTagToSnapshot(User loggedInUser, Integer snapId, String tagName);

    /**
     * Rolls a server back to a snapshot.
     *
     * @param loggedInUser the current user
     * @param sid the system id
     * @param snapId the snapshot id
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Rollbacks server to snapshot",
        requestClass = RollbackToSnapshotRequest.class,
        isIntegerResponse = true
    )
    int rollbackToSnapshot(User loggedInUser, Integer sid, Integer snapId);

    /**
     * Rolls a server back to the snapshot carrying the given tag.
     *
     * @param loggedInUser the current user
     * @param sid the system id
     * @param tagName the name of the snapshot tag
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Rollbacks server to snapshot",
        requestClass = RollbackToTagRequest.class,
        isIntegerResponse = true
    )
    int rollbackToTag(User loggedInUser, Integer sid, String tagName);

    /**
     * Rolls every server carrying the given snapshot tag back to that snapshot.
     *
     * @param loggedInUser the current user
     * @param tagName the name of the snapshot tag
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Rollbacks server to snapshot",
        requestClass = RollbackToTagByNameRequest.class,
        isIntegerResponse = true
    )
    int rollbackToTag(User loggedInUser, String tagName);

    @Schema(name = "ApiResponseServerSnapshotList")
    interface ServerSnapshotListResponse extends ApiResponseWrapper<List<ServerSnapshotDoc>> { }

    @Schema(name = "ApiResponsePackageNevraList")
    interface PackageNevraListResponse extends ApiResponseWrapper<List<PackageNevraDoc>> { }

    @Schema(name = "ApiResponseConfigRevisionList")
    interface ConfigRevisionListResponse extends ApiResponseWrapper<List<ConfigRevisionDoc>> { }

    @Schema(name = "SnapshotDeleteRequest")
    interface DeleteSnapshotRequest {

        /**
         * @return the snapshot id
         */
        @Schema(description = "ID of snapshot to delete", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSnapId();
    }

    @Schema(name = "SnapshotDeleteByDateRequest")
    @JsonPropertyOrder({"startDate", "endDate"})
    interface DeleteSnapshotsByDateRequest {

        /**
         * @return the start date
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getStartDate();

        /**
         * @return the end date
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEndDate();
    }

    @Schema(name = "SnapshotDeleteBySystemAndDateRequest")
    @JsonPropertyOrder({"sid", "startDate", "endDate"})
    interface DeleteSnapshotsBySystemAndDateRequest {

        /**
         * @return the system id
         */
        @Schema(description = "ID of system to delete snapshots for",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return the start date
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getStartDate();

        /**
         * @return the end date
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEndDate();
    }

    @Schema(name = "SnapshotDeleteByDateDetailsRequest")
    interface DeleteSnapshotsByDateDetailsRequest {

        /**
         * @return the dates narrowing the snapshots to delete
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        DateDetailsDoc getDateDetails();
    }

    @Schema(name = "SnapshotDeleteBySystemAndDateDetailsRequest")
    @JsonPropertyOrder({"sid", "dateDetails"})
    interface DeleteSnapshotsBySystemAndDateDetailsRequest {

        /**
         * @return the system id
         */
        @Schema(description = "ID of system to delete snapshots for",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return the dates narrowing the snapshots to delete
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        DateDetailsDoc getDateDetails();
    }

    @Schema(name = "SnapshotDateDetails")
    @JsonPropertyOrder({"startDate", "endDate"})
    interface DateDetailsDoc {

        /**
         * @return the start date
         */
        @Schema(description = "Optional, unless endDate\nis provided.")
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getStartDate();

        /**
         * @return the end date
         */
        @Schema(description = "Optional.")
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEndDate();
    }

    @Schema(name = "SnapshotRollbackToTagByNameRequest")
    interface RollbackToTagByNameRequest {

        /**
         * @return the name of the snapshot tag
         */
        @Schema(description = "Name of the snapshot tag", requiredMode = Schema.RequiredMode.REQUIRED)
        String getTagName();
    }

    @Schema(name = "SnapshotAddTagRequest")
    @JsonPropertyOrder({"snapId", "tagName"})
    interface AddTagRequest {

        /**
         * @return the snapshot id
         */
        @Schema(description = "ID of the snapshot", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSnapId();

        /**
         * @return the name of the snapshot tag
         */
        @Schema(description = "Name of the snapshot tag", requiredMode = Schema.RequiredMode.REQUIRED)
        String getTagName();
    }

    @Schema(name = "SnapshotRollbackRequest")
    @JsonPropertyOrder({"sid", "snapId"})
    interface RollbackToSnapshotRequest {

        /**
         * @return the system id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return the snapshot id
         */
        @Schema(description = "ID of the snapshot", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSnapId();
    }

    @Schema(name = "SnapshotRollbackToTagRequest")
    @JsonPropertyOrder({"sid", "tagName"})
    interface RollbackToTagRequest {

        /**
         * @return the system id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return the name of the snapshot tag
         */
        @Schema(description = "Name of the snapshot tag", requiredMode = Schema.RequiredMode.REQUIRED)
        String getTagName();
    }

    @Schema(name = "ServerSnapshotInfo", description = "server snapshot")
    @JsonPropertyOrder({"id", "reason", "created", "channels", "groups", "entitlements", "configChannels", "tags",
        "invalidReason"})
    interface ServerSnapshotDoc {

        /**
         * @return the snapshot id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the reason for the snapshot's existence
         */
        @Schema(description = "the reason for the snapshot's existence",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getReason();

        /**
         * @return the creation date
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getCreated();

        /**
         * @return the labels of the associated channels
         */
        @Schema(description = "labels of channels associated with the\nsnapshot",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getChannels();

        /**
         * @return the names of the associated server groups
         */
        @Schema(description = "names of server groups associated with\nthe snapshot",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getGroups();

        /**
         * @return the names of the associated system entitlements
         */
        @Schema(description = "names of system entitlements associated\nwith the snapshot",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getEntitlements();

        /**
         * @return the labels of the associated config channels
         */
        @Schema(name = "config_channels",
                description = "labels of config channels the snapshot\nis associated with",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getConfigChannels();

        /**
         * @return the tag names associated with the snapshot
         */
        @Schema(description = "tag names associated with this snapshot",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getTags();

        /**
         * @return the reason the snapshot is invalid
         */
        @Schema(name = "Invalid_reason",
                description = "if the snapshot is invalid, this is the\nreason (optional)")
        String getInvalidReason();
    }

    @Schema(name = "PackageNevraInfo", description = "package nevra")
    @JsonPropertyOrder({"name", "epoch", "version", "release", "arch"})
    interface PackageNevraDoc {

        /**
         * @return the package name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the package epoch
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getEpoch();

        /**
         * @return the package version
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getVersion();

        /**
         * @return the package release
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getRelease();

        /**
         * @return the package architecture
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getArch();
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
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
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
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getContents();

        /**
         * @return whether the content is base64 encoded
         */
        @Schema(name = "contents_enc64", description = " Identifies base64 encoded content",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
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
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getOwner();

        /**
         * @return the group of the file
         */
        @Schema(description = "File Group. Present for files or directories only.",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getGroup();

        /**
         * @return the permissions of the file
         */
        @Schema(description = "File Permissions (Deprecated). Present for files or directories only.",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Integer getPermissions();

        /**
         * @return the permissions mode of the file
         */
        @Schema(name = "permissions_mode",
                description = "File Permissions. Present for files or directories only.",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getPermissionsMode();

        /**
         * @return the SELinux context
         */
        @Schema(name = "selinux_ctx", description = "SELinux Context (optional).",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getSelinuxCtx();

        /**
         * @return whether the file is binary
         */
        @Schema(description = "true/false , Present for files only.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Boolean getBinary();

        /**
         * @return the sha256 signature of the file
         */
        @Schema(description = "File's sha256 signature. Present for files only.",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getSha256();

        /**
         * @return the macro start delimiter
         */
        @Schema(name = "macro-start-delimiter",
                description = "Macro start delimiter for a config file. Present for text files only.",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getMacroStartDelimiter();

        /**
         * @return the macro end delimiter
         */
        @Schema(name = "macro-end-delimiter",
                description = "Macro end delimiter for a config file. Present for text files only.",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getMacroEndDelimiter();
    }
}
