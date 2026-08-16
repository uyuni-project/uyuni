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
package com.redhat.rhn.frontend.xmlrpc.kickstart.filepreservation;

import com.redhat.rhn.domain.common.FileList;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.FilePreservationDto;

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
 * API contract for {@link FilePreservationListHandler}.
 */
@Tag(name = "kickstart.filepreservation", description = "Provides methods to retrieve and manipulate kickstart file " +
        "preservation lists.")
public interface FilePreservationListHandlerApi {

    /**
     * Create a new file preservation list.
     *
     * @param loggedInUser the current user
     * @param name the name of the file list to create
     * @param files the file names to include
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Create a new file preservation list.",
        requestClass = CreateRequest.class,
        isIntegerResponse = true
    )
    int create(User loggedInUser, String name, List<String> files);

    /**
     * Delete a file preservation list.
     *
     * @param loggedInUser the current user
     * @param name the name of the file list to delete
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete a file preservation list.",
        requestClass = DeleteRequest.class,
        isIntegerResponse = true
    )
    int delete(User loggedInUser, String name);

    /**
     * Return all the data associated with the given file preservation list.
     *
     * @param loggedInUser the current user
     * @param name the name of the file list to retrieve details for
     * @return the file list details
     */
    @ApiEndpointDoc(
        summary = "Returns all the data associated with the given file preservation list.",
        method = HttpMethod.get,
        responseClass = FileListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "file list")
    )
    FileList getDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "name", in = ParameterIn.QUERY, required = true,
                description = "name of the file list to retrieve details for") String name);

    /**
     * List all file preservation lists for the organization.
     *
     * @param loggedInUser the current user
     * @return the list of file preservation lists
     */
    @ApiEndpointDoc(
        summary = "List all file preservation lists for the organization\nassociated with the user logged " +
                  "into the given session",
        method = HttpMethod.get,
        responseClass = FilePreservationListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "file preservation")
    )
    List<FilePreservationDto> listAllFilePreservations(@Parameter(hidden = true) User loggedInUser);

    @Schema(name = "CreateFilePreservationRequest")
    @JsonPropertyOrder({"name", "files"})
    interface CreateRequest {

        /**
         * @return the name of the file list to create
         */
        @Schema(description = "name of the file list to create", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the file names to include
         */
        @Schema(description = "file names to include", requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getFiles();
    }

    @Schema(name = "DeleteFilePreservationRequest")
    interface DeleteRequest {

        /**
         * @return the name of the file list to delete
         */
        @Schema(description = "name of the file list to delete", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();
    }

    @Schema(name = "FilePreservationFileList")
    @JsonPropertyOrder({"name", "fileNames"})
    interface FileListDoc {

        /**
         * @return the file list name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the list of file names
         */
        @Schema(name = "file_names", description = "the list of file names",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getFileNames();
    }

    @Schema(name = "FilePreservation")
    @JsonPropertyOrder({"id", "name", "created", "lastModified"})
    interface FilePreservationDoc {

        /**
         * @return the file preservation list id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the file preservation list name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the creation date
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Date getCreated();

        /**
         * @return the last modification date
         */
        @Schema(name = "last_modified", requiredMode = Schema.RequiredMode.REQUIRED)
        Date getLastModified();
    }

    @Schema(name = "ApiResponseFilePreservationFileList")
    interface FileListResponse extends ApiResponseWrapper<FileListDoc> { }

    @Schema(name = "ApiResponseFilePreservationList")
    interface FilePreservationListResponse extends ApiResponseWrapper<List<FilePreservationDoc>> { }
}
