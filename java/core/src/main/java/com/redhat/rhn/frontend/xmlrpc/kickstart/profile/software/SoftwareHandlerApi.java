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
package com.redhat.rhn.frontend.xmlrpc.kickstart.profile.software;

import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link SoftwareHandler}.
 */
@Tag(name = "kickstart.profile.software", description = "Provides methods to access and modify the software list " +
        "associated with a kickstart profile.")
public interface SoftwareHandlerApi {

    /**
     * Get a list of a kickstart profile's software packages.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @return the list of the kickstart profile's software packages
     */
    @ApiEndpointDoc(
        summary = "Get a list of a kickstart profile's software packages.",
        method = HttpMethod.get,
        responseClass = SoftwareListResponse.class,
        responseDescription = "the list of the kickstart profile's software packages"
    )
    List<String> getSoftwareList(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "ksLabel", in = ParameterIn.QUERY, required = true,
            description = "the label of the kickstart profile") String ksLabel);

    /**
     * Set the list of software packages for a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @param packageList the list of package names to be set on the profile
     * @param ignoreMissing whether missing packages should be ignored
     * @param noBase whether the @Base package group should not be installed
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set the list of software packages for a kickstart profile.",
        requestClass = SetSoftwareListRequest.class,
        isIntegerResponse = true
    )
    int setSoftwareList(User loggedInUser, String ksLabel, List<String> packageList, Boolean ignoreMissing,
                        Boolean noBase);

    /**
     * Append the list of software packages to a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @param packageList the list of package names to be added to the profile
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Append the list of software packages to a kickstart profile. Duplicate packages will be ignored.",
        requestClass = AppendToSoftwareListRequest.class,
        isIntegerResponse = true
    )
    int appendToSoftwareList(User loggedInUser, String ksLabel, List<String> packageList);

    /**
     * Set the software details of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @param params the software parameters
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Sets kickstart profile software details.",
        requestClass = SetSoftwareDetailsRequest.class,
        isIntegerResponse = true
    )
    int setSoftwareDetails(User loggedInUser, String ksLabel, Map<String, Object> params);

    /**
     * Get the software details of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @return the software details of the kickstart profile
     */
    @ApiEndpointDoc(
        summary = "Gets kickstart profile software details.",
        method = HttpMethod.get,
        responseClass = SoftwareDetailsResponse.class
    )
    Map<String, Boolean> getSoftwareDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "ksLabel", in = ParameterIn.QUERY, required = true,
            description = "the label of the kickstart profile") String ksLabel);

    @Schema(name = "SetKickstartSoftwareListRequest")
    @JsonPropertyOrder({"ksLabel", "packageList", "ignoreMissing", "noBase"})
    interface SetSoftwareListRequest {

        /**
         * @return the label of the kickstart profile
         */
        @Schema(description = "the label of the kickstart profile", requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return the list of package names to be set on the profile
         */
        @Schema(description = "the list of package names to be set on the profile",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getPackageList();

        /**
         * @return whether missing packages should be ignored
         */
        @Schema(description = "ignore missing packages if true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Boolean getIgnoreMissing();

        /**
         * @return whether the @Base package group should not be installed
         */
        @Schema(description = "don't install @Base package group if true",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Boolean getNoBase();
    }

    @Schema(name = "AppendToKickstartSoftwareListRequest")
    @JsonPropertyOrder({"ksLabel", "packageList"})
    interface AppendToSoftwareListRequest {

        /**
         * @return the label of the kickstart profile
         */
        @Schema(description = "the label of the kickstart profile", requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return the list of package names to be added to the profile
         */
        @Schema(description = "the list of package names to be added to the profile",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getPackageList();
    }

    @Schema(name = "SetKickstartSoftwareDetailsRequest")
    @JsonPropertyOrder({"ksLabel", "params"})
    interface SetSoftwareDetailsRequest {

        /**
         * @return the label of the kickstart profile
         */
        @Schema(description = "the label of the kickstart profile", requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return the software parameters
         */
        @Schema(description = "kickstart packages info", requiredMode = Schema.RequiredMode.REQUIRED)
        KickstartPackagesInfoDoc getParams();
    }

    @Schema(name = "KickstartPackagesInfo")
    @JsonPropertyOrder({"noBase", "ignoreMissing"})
    interface KickstartPackagesInfoDoc {

        /**
         * @return whether the @Base package group should be installed
         */
        @Schema(description = "install @Base package group", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getNoBase();

        /**
         * @return whether missing packages should be ignored
         */
        @Schema(description = "ignore missing packages", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getIgnoreMissing();
    }

    @Schema(name = "ApiResponseKickstartSoftwareList")
    interface SoftwareListResponse extends ApiResponseWrapper<List<String>> { }

    @Schema(name = "ApiResponseKickstartPackagesInfo")
    interface SoftwareDetailsResponse extends ApiResponseWrapper<KickstartPackagesInfoDoc> { }
}
