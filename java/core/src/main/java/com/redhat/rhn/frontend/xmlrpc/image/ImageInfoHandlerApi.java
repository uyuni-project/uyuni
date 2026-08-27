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
package com.redhat.rhn.frontend.xmlrpc.image;

import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageOverview;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ErrataOverview;

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
 * API contract for {@link ImageInfoHandler}.
 */
@Tag(name = "image", description = "Provides methods to access and modify images.")
public interface ImageInfoHandlerApi {

    /**
     * Lists the available images.
     *
     * @param loggedInUser the current user
     * @return the available images
     */
    @ApiEndpointDoc(
        summary = "List available images",
        method = HttpMethod.get,
        responseClass = ImageInfoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "image information")
    )
    List<ImageInfo> listImages(@Parameter(hidden = true) User loggedInUser);

    /**
     * Gets the details of an image.
     *
     * @param loggedInUser the current user
     * @param imageId the image ID
     * @return the details of the image
     */
    @ApiEndpointDoc(
        summary = "Get details of an image",
        method = HttpMethod.get,
        responseClass = ImageOverviewResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "image overview information")
    )
    ImageOverview getDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "imageId", in = ParameterIn.QUERY, required = true) Integer imageId);

    /**
     * Gets the pillar data of an image.
     *
     * @param loggedInUser the current user
     * @param imageId the image ID
     * @return the pillar data of the image
     */
    @ApiEndpointDoc(
        summary = "Get pillar data of an image. The \"size\" entries are converted to string.",
        method = HttpMethod.get,
        responseClass = ImagePillarResponse.class,
        legacyDocResponse = @LegacyDocResponse(type = "struct", name = "the pillar data")
    )
    Map<String, Object> getPillar(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "imageId", in = ParameterIn.QUERY, required = true) Integer imageId);

    /**
     * Sets the pillar data of an image.
     *
     * @param loggedInUser the current user
     * @param imageId the image ID
     * @param pillarData the pillar data
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set pillar data of an image. The \"size\" entries should be passed as string.",
        requestClass = SetPillarRequest.class,
        isIntegerResponse = true
    )
    int setPillar(User loggedInUser, Integer imageId, Map<String, Object> pillarData);

    /**
     * Imports an image and schedules an inspect afterwards.
     *
     * @param loggedInUser the current user
     * @param name the image name
     * @param version the version to import
     * @param buildHostId the system ID of the build host
     * @param storeLabel the label of the image store
     * @param activationKey the activation key
     * @param earliestOccurrence the earliest the inspect can run
     * @return the ID of the inspect action created
     */
    @ApiEndpointDoc(
        summary = "Import an image and schedule an inspect afterwards",
        requestClass = ImportImageRequest.class,
        responseClass = ImageActionIdResponse.class,
        legacyDocResponse = @LegacyDocResponse(type = "int", name = "the ID of the inspect action created")
    )
    Long importImage(User loggedInUser, String name, String version, Integer buildHostId, String storeLabel,
        String activationKey, Date earliestOccurrence);

    /**
     * Imports a container image and schedules an inspect afterwards.
     *
     * @param loggedInUser the current user
     * @param name the image name
     * @param version the version to import
     * @param buildHostId the system ID of the build host
     * @param storeLabel the label of the image store
     * @param activationKey the activation key
     * @param earliestOccurrence the earliest the inspect can run
     * @return the ID of the inspect action created
     */
    @ApiEndpointDoc(
        summary = "Import an image and schedule an inspect afterwards",
        requestClass = ImportContainerImageRequest.class,
        responseClass = ImageActionIdResponse.class,
        legacyDocResponse = @LegacyDocResponse(type = "int", name = "the ID of the inspect action created")
    )
    Long importContainerImage(User loggedInUser, String name, String version, Integer buildHostId,
        String storeLabel, String activationKey, Date earliestOccurrence);

    /**
     * Imports an OS image.
     *
     * @param loggedInUser the current user
     * @param name the image name
     * @param version the version to import
     * @param arch the image architecture
     * @return the ID of the image
     */
    @ApiEndpointDoc(
        summary = "Import an image and schedule an inspect afterwards",
        requestClass = ImportOSImageRequest.class,
        responseClass = ImageActionIdResponse.class,
        legacyDocResponse = @LegacyDocResponse(type = "int", name = "the ID of the image")
    )
    Long importOSImage(User loggedInUser, String name, String version, String arch);

    /**
     * Deletes an image file.
     *
     * @param loggedInUser the current user
     * @param imageId the image ID
     * @param file the file name
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete image file",
        requestClass = DeleteImageFileRequest.class,
        isIntegerResponse = true
    )
    int deleteImageFile(User loggedInUser, Integer imageId, String file);

    /**
     * Adds an image file.
     *
     * @param loggedInUser the current user
     * @param imageId the image ID
     * @param file the file name
     * @param type the image type
     * @param external whether the file is external
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Add an image file",
        requestClass = AddImageFileRequest.class,
        isIntegerResponse = true
    )
    Long addImageFile(User loggedInUser, Integer imageId, String file, String type, Boolean external);

    /**
     * Schedules an image build.
     *
     * @param loggedInUser the current user
     * @param profileLabel the label of the image profile
     * @param version the version to build
     * @param buildHostId the system ID of the build host
     * @param earliestOccurrence the earliest the build can run
     * @return the ID of the build action created
     */
    @ApiEndpointDoc(
        summary = "Schedule an image build",
        requestClass = ScheduleImageBuildRequest.class,
        responseClass = ImageActionIdResponse.class,
        legacyDocResponse = @LegacyDocResponse(type = "int", name = "the ID of the build action created")
    )
    Long scheduleImageBuild(User loggedInUser, String profileLabel, String version, Integer buildHostId,
        Date earliestOccurrence);

    /**
     * Lists the errata relevant for an image.
     *
     * @param loggedInUser the current user
     * @param imageId the image ID
     * @return the errata relevant for the image
     */
    @ApiEndpointDoc(
        summary = "Returns a list of all errata that are relevant for the image",
        method = HttpMethod.get,
        responseClass = ImageErrataListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "errata")
    )
    List<ErrataOverview> getRelevantErrata(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "imageId", in = ParameterIn.QUERY, required = true) Integer imageId);

    /**
     * Lists the packages installed on an image.
     *
     * @param loggedInUser the current user
     * @param imageId the image ID
     * @return the packages installed on the image
     */
    @ApiEndpointDoc(
        summary = "List the installed packages on the given image",
        method = HttpMethod.get,
        responseClass = ImagePackageListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "package")
    )
    List<Map<String, Object>> listPackages(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "imageId", in = ParameterIn.QUERY, required = true) Integer imageId);

    /**
     * Gets the custom data values of an image.
     *
     * @param loggedInUser the current user
     * @param imageId the image ID
     * @return the custom data values of the image
     */
    @ApiEndpointDoc(
        summary = "Get the custom data values defined for the image",
        method = HttpMethod.get,
        responseClass = ImageCustomValuesResponse.class,
        legacyDocResponse = @LegacyDocResponse(type = "struct", name = "the map of custom labels to custom values")
    )
    Map<String, String> getCustomValues(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "imageId", in = ParameterIn.QUERY, required = true) Integer imageId);

    /**
     * Deletes an image.
     *
     * @param loggedInUser the current user
     * @param imageId the image ID
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete an image",
        requestClass = ImageIdRequest.class,
        isIntegerResponse = true
    )
    int delete(User loggedInUser, Integer imageId);

    @Schema(name = "ApiResponseImageInfoList")
    interface ImageInfoListResponse extends ApiResponseWrapper<List<ImageInfoDoc>> { }

    @Schema(name = "ApiResponseImageOverview")
    interface ImageOverviewResponse extends ApiResponseWrapper<ImageOverviewDoc> { }

    @Schema(name = "ApiResponseImagePillar")
    interface ImagePillarResponse extends ApiResponseWrapper<Map<String, Object>> {

        /**
         * The pillar data is keyed by the pillar entries of the image, which carry scalars as
         * well as nested structures, so the payload is documented as a free form struct.
         *
         * @return the pillar data
         */
        @Override
        @Schema(description = "The payload result", requiredMode = Schema.RequiredMode.REQUIRED,
                additionalProperties = Schema.AdditionalPropertiesValue.TRUE)
        Map<String, Object> getResult();
    }

    @Schema(name = "ApiResponseImageActionId")
    interface ImageActionIdResponse extends ApiResponseWrapper<Long> { }

    @Schema(name = "ApiResponseImageErrataList")
    interface ImageErrataListResponse extends ApiResponseWrapper<List<ErrataOverviewDoc>> { }

    @Schema(name = "ApiResponseImagePackageList")
    interface ImagePackageListResponse extends ApiResponseWrapper<List<ImagePackageDoc>> { }

    @Schema(name = "ApiResponseImageCustomValues")
    interface ImageCustomValuesResponse extends ApiResponseWrapper<Map<String, String>> { }

    @Schema(name = "ImageSetPillarRequest")
    @JsonPropertyOrder({"imageId", "pillarData"})
    interface SetPillarRequest {

        /**
         * @return the image ID
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getImageId();

        /**
         * The pillar values are documented as free-form, so they are left unconstrained.
         *
         * @return the pillar data
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                additionalProperties = Schema.AdditionalPropertiesValue.TRUE)
        @LegacyDocResponse(type = "struct")
        Map<String, Object> getPillarData();
    }

    @Schema(name = "ImageImportRequest")
    @JsonPropertyOrder({"name", "version", "buildHostId", "storeLabel", "activationKey", "earliestOccurrence"})
    interface ImportImageRequest {

        /**
         * @return the image name
         */
        @Schema(description = "image name as specified in the store", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the version to import
         */
        @Schema(description = "version to import or empty", requiredMode = Schema.RequiredMode.REQUIRED)
        String getVersion();

        /**
         * @return the system ID of the build host
         */
        @Schema(description = "system ID of the build host", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getBuildHostId();

        /**
         * @return the label of the image store
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getStoreLabel();

        /**
         * @return the activation key
         */
        @Schema(description = "activation key to get the channel data from",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getActivationKey();

        /**
         * @return the earliest the inspect can run
         */
        @Schema(description = "earliest the following inspect can run",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();
    }

    @Schema(name = "ImageImportContainerRequest")
    @JsonPropertyOrder({"name", "version", "buildHostId", "storeLabel", "activationKey", "earliestOccurrence"})
    interface ImportContainerImageRequest {

        /**
         * @return the image name
         */
        @Schema(description = "image name as specified in the store", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the version to import
         */
        @Schema(description = "version to import or empty", requiredMode = Schema.RequiredMode.REQUIRED)
        String getVersion();

        /**
         * @return the system ID of the build host
         */
        @Schema(description = "system ID of the build host", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getBuildHostId();

        /**
         * @return the label of the image store
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getStoreLabel();

        /**
         * @return the activation key
         */
        @Schema(description = "activation key to get the channel data from",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getActivationKey();

        /**
         * @return the earliest the inspect can run
         */
        @Schema(description = "earliest the following inspect can run",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();
    }

    @Schema(name = "ImageImportOSRequest")
    @JsonPropertyOrder({"name", "version", "arch"})
    interface ImportOSImageRequest {

        /**
         * @return the image name
         */
        @Schema(description = "image name as specified in the store", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the version to import
         */
        @Schema(description = "version to import", requiredMode = Schema.RequiredMode.REQUIRED)
        String getVersion();

        /**
         * @return the image architecture
         */
        @Schema(description = "image architecture", requiredMode = Schema.RequiredMode.REQUIRED)
        String getArch();
    }

    @Schema(name = "ImageDeleteFileRequest")
    @JsonPropertyOrder({"imageId", "file"})
    interface DeleteImageFileRequest {

        /**
         * @return the image ID
         */
        @Schema(description = "ID of the image", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getImageId();

        /**
         * @return the file name
         */
        @Schema(description = "the file name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getFile();
    }

    @Schema(name = "ImageAddFileRequest")
    @JsonPropertyOrder({"imageId", "file", "type", "external"})
    interface AddImageFileRequest {

        /**
         * @return the image ID
         */
        @Schema(description = "ID of the image", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getImageId();

        /**
         * @return the file name
         */
        @Schema(description = "the file name, it must exist in the store",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getFile();

        /**
         * @return the image type
         */
        @Schema(description = "the image type", requiredMode = Schema.RequiredMode.REQUIRED)
        String getType();

        /**
         * @return whether the file is external
         */
        @Schema(description = "the file is external", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getExternal();
    }

    @Schema(name = "ImageScheduleBuildRequest")
    @JsonPropertyOrder({"profileLabel", "version", "buildHostId", "earliestOccurrence"})
    interface ScheduleImageBuildRequest {

        /**
         * @return the label of the image profile
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getProfileLabel();

        /**
         * @return the version to build
         */
        @Schema(description = "version to build or empty", requiredMode = Schema.RequiredMode.REQUIRED)
        String getVersion();

        /**
         * @return the system ID of the build host
         */
        @Schema(description = "system id of the build host", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getBuildHostId();

        /**
         * @return the earliest the build can run
         */
        @Schema(description = "earliest the build can run.", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();
    }

    @Schema(name = "ImageIdRequest")
    interface ImageIdRequest {

        /**
         * @return the image ID
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getImageId();
    }

    @Schema(name = "ImageInformation")
    @JsonPropertyOrder({"id", "name", "type", "version", "revision", "arch", "external", "storeLabel", "checksum",
        "obsolete"})
    interface ImageInfoDoc {

        /**
         * @return the ID of the image
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the name of the image
         */
        @Schema(description = "image name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the type of the image
         */
        @Schema(description = "image type", requiredMode = Schema.RequiredMode.REQUIRED)
        String getType();

        /**
         * @return the version of the image
         */
        @Schema(description = "image tag/version", requiredMode = Schema.RequiredMode.REQUIRED)
        String getVersion();

        /**
         * @return the build revision number of the image
         */
        @Schema(description = "image build revision number", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getRevision();

        /**
         * @return the architecture of the image
         */
        @Schema(description = "image architecture", requiredMode = Schema.RequiredMode.REQUIRED)
        String getArch();

        /**
         * @return whether the image is built externally
         */
        @Schema(description = "true if the image is built externally, false otherwise",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getExternal();

        /**
         * @return the label of the image store
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getStoreLabel();

        /**
         * @return the checksum of the image
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getChecksum();

        /**
         * @return whether the image is obsolete
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getObsolete();
    }

    @Schema(name = "ImageOverviewInformation")
    @JsonPropertyOrder({"id", "name", "type", "version", "revision", "arch", "external", "checksum", "profileLabel",
        "storeLabel", "buildStatus", "inspectStatus", "buildServerId", "securityErrata", "bugErrata",
        "enhancementErrata", "outdatedPackages", "installedPackages", "files", "obsolete"})
    interface ImageOverviewDoc {

        /**
         * @return the ID of the image
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the name of the image
         */
        @Schema(description = "image name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the type of the image
         */
        @Schema(description = "image type", requiredMode = Schema.RequiredMode.REQUIRED)
        String getType();

        /**
         * @return the version of the image
         */
        @Schema(description = "image tag/version", requiredMode = Schema.RequiredMode.REQUIRED)
        String getVersion();

        /**
         * @return the build revision number of the image
         */
        @Schema(description = "image build revision number", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getRevision();

        /**
         * @return the architecture of the image
         */
        @Schema(description = "image architecture", requiredMode = Schema.RequiredMode.REQUIRED)
        String getArch();

        /**
         * @return whether the image is built externally
         */
        @Schema(description = "true if the image is built externally, false otherwise",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getExternal();

        /**
         * @return the checksum of the image
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getChecksum();

        /**
         * @return the label of the image profile
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getProfileLabel();

        /**
         * @return the label of the image store
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getStoreLabel();

        /**
         * @return the build status of the image
         */
        @Schema(description = "One of:",
                allowableValues = {"queued", "picked up", "completed", "failed"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getBuildStatus();

        /**
         * @return the inspect status of the image
         */
        @Schema(description = "Available if the build is successful. One of:",
                allowableValues = {"queued", "picked up", "completed", "failed"},
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getInspectStatus();

        /**
         * @return the ID of the build server
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getBuildServerId();

        /**
         * @return the number of security errata
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSecurityErrata();

        /**
         * @return the number of bug errata
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getBugErrata();

        /**
         * @return the number of enhancement errata
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getEnhancementErrata();

        /**
         * @return the number of outdated packages
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getOutdatedPackages();

        /**
         * @return the number of installed packages
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getInstalledPackages();

        /**
         * @return the image files
         */
        @Schema(description = "image files", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "image information")
        List<ImageFileDoc> getFiles();

        /**
         * @return whether the image has been replaced in the store
         */
        @Schema(description = "true if the image has been replaced in the store",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getObsolete();
    }

    @Schema(name = "ErrataOverview")
    @JsonPropertyOrder({"id", "issueDate", "date", "updateDate", "advisorySynopsis", "advisoryType",
        "advisoryStatus", "advisoryName", "rebootSuggested", "restartSuggested"})
    interface ErrataOverviewDoc {

        /**
         * @return the ID of the erratum
         */
        @Schema(description = "errata ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the issue date of the erratum
         */
        @Schema(name = "issue_date", description = "the date erratum was updated (deprecated)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getIssueDate();

        /**
         * @return the creation date of the erratum
         */
        @Schema(description = "the date erratum was created (deprecated)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getDate();

        /**
         * @return the update date of the erratum
         */
        @Schema(name = "update_date", description = "the date erratum was updated (deprecated)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getUpdateDate();

        /**
         * @return the synopsis of the erratum
         */
        @Schema(name = "advisory_synopsis", description = "summary of the erratum",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdvisorySynopsis();

        /**
         * @return the type of the erratum
         */
        @Schema(name = "advisory_type", description = "type label such as 'Security', 'Bug Fix'",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdvisoryType();

        /**
         * @return the status of the erratum
         */
        @Schema(name = "advisory_status",
                description = "status label such as 'final', 'testing', 'stable', 'pending','retracted' or " +
                    "'unpushed'",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdvisoryStatus();

        /**
         * @return the name of the erratum
         */
        @Schema(name = "advisory_name", description = "name such as 'RHSA', etc.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdvisoryName();

        /**
         * @return whether a reboot is suggested
         */
        @Schema(name = "reboot_suggested",
                description = "A boolean flag signaling whether a system reboot is advisable following the " +
                    "application of the errata. Typical example is upon kernel update.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getRebootSuggested();

        /**
         * @return whether a package manager restart is suggested
         */
        @Schema(name = "restart_suggested",
                description = "A boolean flag signaling a weather reboot of the package manager is advisable " +
                    "following the application of the errata. This is commonly used to address update stack " +
                    "issues before proceeding with other updates.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getRestartSuggested();
    }

    @Schema(name = "ImageFile")
    @JsonPropertyOrder({"file", "type", "external", "url"})
    interface ImageFileDoc {

        /**
         * @return the name of the file
         */
        @Schema(description = "file name without path", requiredMode = Schema.RequiredMode.REQUIRED)
        String getFile();

        /**
         * @return the type of the file
         */
        @Schema(description = "file type", requiredMode = Schema.RequiredMode.REQUIRED)
        String getType();

        /**
         * @return whether the file is external
         */
        @Schema(description = "true if the file is external, false otherwise",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getExternal();

        /**
         * @return the URL of the file
         */
        @Schema(description = "file url", requiredMode = Schema.RequiredMode.REQUIRED)
        String getUrl();
    }

    @Schema(name = "ImagePackage")
    @JsonPropertyOrder({"name", "version", "release", "epoch", "arch"})
    interface ImagePackageDoc {

        /**
         * @return the name of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the version of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getVersion();

        /**
         * @return the release of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getRelease();

        /**
         * @return the epoch of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getEpoch();

        /**
         * @return the architecture of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getArch();
    }
}
