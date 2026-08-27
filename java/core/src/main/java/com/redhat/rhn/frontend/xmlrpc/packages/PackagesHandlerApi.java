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
package com.redhat.rhn.frontend.xmlrpc.packages;

import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link PackagesHandler}.
 */
@Tag(name = "packages", description = "Methods to retrieve information about the Packages contained within this " +
        "server.")
public interface PackagesHandlerApi {

    /**
     * Retrieves the details of a package.
     *
     * @param loggedInUser the current user
     * @param pid the package ID
     * @return the details of the package
     */
    @ApiEndpointDoc(
        summary = "Retrieve details for the package with the ID.",
        method = HttpMethod.get,
        responseClass = PackageDetailsResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "package")
    )
    Map<String, Object> getDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "pid", in = ParameterIn.QUERY, required = true) Integer pid);

    /**
     * Lists the channels providing a package.
     *
     * @param loggedInUser the current user
     * @param pid the package ID
     * @return the channels providing the package
     */
    @ApiEndpointDoc(
        summary = "List the channels that provide the a package.",
        method = HttpMethod.get,
        responseClass = ProvidingChannelListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    Object[] listProvidingChannels(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "pid", in = ParameterIn.QUERY, required = true) Integer pid);

    /**
     * Lists the errata providing a package.
     *
     * @param loggedInUser the current user
     * @param pid the package ID
     * @return the errata providing the package
     */
    @ApiEndpointDoc(
        summary = "List the errata providing the a package.",
        method = HttpMethod.get,
        responseClass = ProvidingErrataListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "errata")
    )
    Object[] listProvidingErrata(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "pid", in = ParameterIn.QUERY, required = true) Integer pid);

    /**
     * Lists the files of a package.
     *
     * @param loggedInUser the current user
     * @param pid the package ID
     * @return the files of the package
     */
    @ApiEndpointDoc(
        summary = "List the files associated with a package.",
        method = HttpMethod.get,
        responseClass = PackageFileListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "file info")
    )
    Object[] listFiles(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "pid", in = ParameterIn.QUERY, required = true) Integer pid);

    /**
     * Lists the change log of a package.
     *
     * @param loggedInUser the current user
     * @param pid the package ID
     * @return the change log of the package
     */
    @ApiEndpointDoc(
        summary = "List the change log for a package.",
        method = HttpMethod.get,
        responseClass = StringResponse.class,
        legacyDocResponse = @LegacyDocResponse(plainText = true)
    )
    String listChangelog(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "pid", in = ParameterIn.QUERY, required = true) Integer pid);

    /**
     * Lists the dependencies of a package.
     *
     * @param loggedInUser the current user
     * @param pid the package ID
     * @return the dependencies of the package
     */
    @ApiEndpointDoc(
        summary = "List the dependencies for a package.",
        method = HttpMethod.get,
        responseClass = PackageDependencyListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "dependency")
    )
    Object[] listDependencies(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "pid", in = ParameterIn.QUERY, required = true) Integer pid);

    /**
     * Removes a package.
     *
     * @param loggedInUser the current user
     * @param pid the package ID
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Remove a package from #product().",
        requestClass = PackageIdRequest.class,
        isIntegerResponse = true
    )
    int removePackage(User loggedInUser, Integer pid);

    /**
     * Removes a source package.
     *
     * @param loggedInUser the current user
     * @param psid the package source ID
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Remove a source package.",
        requestClass = SourcePackageIdRequest.class,
        isIntegerResponse = true
    )
    int removeSourcePackage(User loggedInUser, Integer psid);

    /**
     * Lists all source packages of the organization.
     *
     * @param loggedInUser the current user
     * @return the source packages of the organization
     */
    @ApiEndpointDoc(
        summary = "List all source packages in user's organization.",
        method = HttpMethod.get,
        responseClass = SourcePackageListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "source_package")
    )
    Object[] listSourcePackages(@Parameter(hidden = true) User loggedInUser);

    /**
     * Looks up the packages matching the given NVREA.
     *
     * @param loggedInUser the current user
     * @param name the package name
     * @param version the package version
     * @param release the package release
     * @param epoch the package epoch
     * @param archLabel the architecture label
     * @return the matching packages
     */
    @ApiEndpointDoc(
        summary = "Lookup the details for packages with the given name, version, release, architecture label, " +
            "and (optionally) epoch.",
        method = HttpMethod.get,
        responseClass = PackageListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "package")
    )
    List<Package> findByNvrea(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "name", in = ParameterIn.QUERY, required = true) String name,
        @Parameter(name = "version", in = ParameterIn.QUERY, required = true) String version,
        @Parameter(name = "release", in = ParameterIn.QUERY, required = true) String release,
        @Parameter(name = "epoch", in = ParameterIn.QUERY, required = true,
            description = "If set to something other than empty string, strict matching will be used and the " +
                "epoch string must be correct. If set to an empty string, if the epoch is null or there is only " +
                "one NVRA combination, it will be returned.  (Empty string is recommended.)") String epoch,
        @Parameter(name = "archLabel", in = ParameterIn.QUERY, required = true) String archLabel);

    /**
     * Retrieves the download URL of a package.
     *
     * @param loggedInUser the current user
     * @param pid the package ID
     * @return the download URL
     */
    @ApiEndpointDoc(
        summary = "Retrieve the url that can be used to download a package. This will expire after a certain " +
            "time period.",
        method = HttpMethod.get,
        responseClass = StringResponse.class,
        responseDescription = "the download url",
        legacyDocResponse = @LegacyDocResponse(plainText = true)
    )
    String getPackageUrl(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "pid", in = ParameterIn.QUERY, required = true) Integer pid);

    /**
     * Retrieves the package file of a package.
     *
     * @param loggedInUser the current user
     * @param pid the package ID
     * @return the package file
     * @throws IOException when the package file cannot be read
     */
    @ApiEndpointDoc(
        summary = "Retrieve the package file associated with a package.",
        method = HttpMethod.get,
        responseClass = PackageFileResponse.class,
        responseDescription = "binary object - package file",
        legacyDocResponse = @LegacyDocResponse(type = "byte")
    )
    byte[] getPackage(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "pid", in = ParameterIn.QUERY, required = true) Integer pid) throws IOException;

    @Schema(name = "ApiResponseString")
    interface StringResponse extends ApiResponseWrapper<String> { }

    @Schema(name = "ApiResponsePackageDetails")
    interface PackageDetailsResponse extends ApiResponseWrapper<PackageDetailsDoc> { }

    @Schema(name = "ApiResponsePackageProvidingChannelList")
    interface ProvidingChannelListResponse extends ApiResponseWrapper<List<ProvidingChannelDoc>> { }

    @Schema(name = "ApiResponsePackageProvidingErrataList")
    interface ProvidingErrataListResponse extends ApiResponseWrapper<List<ProvidingErrataDoc>> { }

    @Schema(name = "ApiResponsePackageFileList")
    interface PackageFileListResponse extends ApiResponseWrapper<List<PackageFileDoc>> { }

    @Schema(name = "ApiResponsePackageDependencyList")
    interface PackageDependencyListResponse extends ApiResponseWrapper<List<PackageDependencyDoc>> { }

    @Schema(name = "ApiResponseSourcePackageList")
    interface SourcePackageListResponse extends ApiResponseWrapper<List<SourcePackageDoc>> { }

    @Schema(name = "ApiResponsePackageList")
    interface PackageListResponse extends ApiResponseWrapper<List<PackageDoc>> { }

    // The package file is written to the response as the array of numbers a Java byte array
    // serializes to, not as the base64 string a Byte element would document.
    @Schema(name = "ApiResponsePackageFile")
    interface PackageFileResponse extends ApiResponseWrapper<List<Integer>> { }

    @Schema(name = "PackageIdRequest")
    interface PackageIdRequest {

        /**
         * @return the package ID
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getPid();
    }

    @Schema(name = "SourcePackageIdRequest")
    interface SourcePackageIdRequest {

        /**
         * @return the package source ID
         */
        @Schema(description = "package source ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getPsid();
    }

    @Schema(name = "PackageDetails")
    @JsonPropertyOrder({"id", "name", "epoch", "version", "release", "archLabel", "providingChannels", "buildHost",
        "description", "checksum", "checksumType", "vendor", "summary", "cookie", "license", "file", "buildDate",
        "lastModifiedDate", "size", "path", "payloadSize"})
    interface PackageDetailsDoc {

        /**
         * @return the ID of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the name of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the epoch of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getEpoch();

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
         * @return the architecture label of the package
         */
        @Schema(name = "arch_label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getArchLabel();

        /**
         * @return the channels providing the package
         */
        @Schema(name = "providing_channels", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "Channel label providing this package.")
        List<String> getProvidingChannels();

        /**
         * @return the build host of the package
         */
        @Schema(name = "build_host", requiredMode = Schema.RequiredMode.REQUIRED)
        String getBuildHost();

        /**
         * @return the description of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();

        /**
         * @return the checksum of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getChecksum();

        /**
         * @return the checksum type of the package
         */
        @Schema(name = "checksum_type", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChecksumType();

        /**
         * @return the vendor of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getVendor();

        /**
         * @return the summary of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSummary();

        /**
         * @return the cookie of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getCookie();

        /**
         * @return the license of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLicense();

        /**
         * @return the file of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getFile();

        /**
         * @return the build date of the package
         */
        @Schema(name = "build_date", requiredMode = Schema.RequiredMode.REQUIRED)
        String getBuildDate();

        /**
         * @return the last modification date of the package
         */
        @Schema(name = "last_modified_date", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLastModifiedDate();

        /**
         * @return the size of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSize();

        /**
         * @return the path of the package
         */
        @Schema(description = "The path on the #product() server's file system that the package resides.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getPath();

        /**
         * @return the payload size of the package
         */
        @Schema(name = "payload_size", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPayloadSize();
    }

    @Schema(name = "PackageProvidingChannel")
    @JsonPropertyOrder({"label", "parentLabel", "name"})
    interface ProvidingChannelDoc {

        /**
         * @return the label of the channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the label of the parent channel
         */
        @Schema(name = "parent_label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getParentLabel();

        /**
         * @return the name of the channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();
    }

    @Schema(name = "PackageProvidingErrata")
    @JsonPropertyOrder({"advisory", "issueDate", "lastModifiedDate", "updateDate", "synopsis", "type"})
    interface ProvidingErrataDoc {

        /**
         * @return the advisory of the erratum
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdvisory();

        /**
         * @return the issue date of the erratum
         */
        @Schema(name = "issue_date", requiredMode = Schema.RequiredMode.REQUIRED)
        String getIssueDate();

        /**
         * @return the last modification date of the erratum
         */
        @Schema(name = "last_modified_date", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLastModifiedDate();

        /**
         * @return the update date of the erratum
         */
        @Schema(name = "update_date", requiredMode = Schema.RequiredMode.REQUIRED)
        String getUpdateDate();

        /**
         * @return the synopsis of the erratum
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSynopsis();

        /**
         * @return the type of the erratum
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getType();
    }

    @Schema(name = "PackageFileInfo")
    @JsonPropertyOrder({"path", "type", "lastModifiedDate", "checksum", "checksumType", "size", "linkto"})
    interface PackageFileDoc {

        /**
         * @return the path of the file
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getPath();

        /**
         * @return the type of the file
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getType();

        /**
         * @return the last modification date of the file
         */
        @Schema(name = "last_modified_date", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLastModifiedDate();

        /**
         * @return the checksum of the file
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getChecksum();

        /**
         * @return the checksum type of the file
         */
        @Schema(name = "checksum_type", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChecksumType();

        /**
         * @return the size of the file
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSize();

        /**
         * @return the link target of the file
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLinkto();
    }

    @Schema(name = "PackageDependency")
    @JsonPropertyOrder({"dependency", "dependencyType", "dependencyModifier"})
    interface PackageDependencyDoc {

        /**
         * @return the dependency
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDependency();

        /**
         * @return the type of the dependency
         */
        @Schema(name = "dependency_type", description = "One of the following:",
                allowableValues = {"requires", "conflicts", "obsoletes", "provides", "recommends", "suggests",
                    "supplements", "enhances", "predepends", "breaks"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getDependencyType();

        /**
         * @return the modifier of the dependency
         */
        @Schema(name = "dependency_modifier", requiredMode = Schema.RequiredMode.REQUIRED)
        String getDependencyModifier();
    }

    @Schema(name = "SourcePackageOverview")
    @JsonPropertyOrder({"id", "name"})
    interface SourcePackageDoc {

        /**
         * @return the ID of the source package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the name of the source package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();
    }

    @Schema(name = "PackageInfo")
    @JsonPropertyOrder({"name", "version", "release", "epoch", "id", "archLabel", "lastModified", "path",
        "partOfRetractedPatch", "provider"})
    interface PackageDoc {

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
         * @return the ID of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the architecture label of the package
         */
        @Schema(name = "arch_label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getArchLabel();

        /**
         * @return the last modification date of the package
         */
        @Schema(name = "last_modified", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getLastModified();

        /**
         * @return the path of the package
         */
        @Schema(description = "the path on that file system that the package resides",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getPath();

        /**
         * @return whether the package is part of a retracted patch
         */
        @Schema(name = "part_of_retracted_patch",
                description = "true if the package is a part of a retracted patch",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getPartOfRetractedPatch();

        /**
         * @return the provider of the package
         */
        @Schema(description = "the provider of the package, determined by the gpg key it was signed with.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getProvider();
    }
}
