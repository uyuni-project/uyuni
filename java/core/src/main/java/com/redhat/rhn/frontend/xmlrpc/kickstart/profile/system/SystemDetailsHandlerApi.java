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
package com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system;

import com.redhat.rhn.domain.common.FileList;
import com.redhat.rhn.domain.kickstart.crypto.CryptoKey;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link SystemDetailsHandler}.
 */
@Tag(name = "kickstart.profile.system", description = "Provides methods to set various properties of a kickstart " +
        "profile.")
public interface SystemDetailsHandlerApi {

    /**
     * Checks the configuration management status of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the kickstart profile label
     * @return whether configuration management is enabled
     */
    @ApiEndpointDoc(
        summary = "Check the configuration management status for a kickstart profile.",
        requestClass = KickstartLabelRequest.class,
        responseClass = BooleanResponse.class,
        legacyDocResponse = @LegacyDocResponse(type = "boolean",
            name = "true if configuration management is enabled; otherwise, false")
    )
    boolean checkConfigManagement(User loggedInUser, String ksLabel);

    /**
     * Enables the configuration management flag of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the kickstart profile label
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Enables the configuration management flag in a kickstart profile",
        requestClass = KickstartLabelRequest.class,
        isIntegerResponse = true
    )
    int enableConfigManagement(User loggedInUser, String ksLabel);

    /**
     * Disables the configuration management flag of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the kickstart profile label
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Disables the configuration management flag in a kickstart profile",
        requestClass = KickstartLabelRequest.class,
        isIntegerResponse = true
    )
    int disableConfigManagement(User loggedInUser, String ksLabel);

    /**
     * Checks the remote commands status of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the kickstart profile label
     * @return whether remote commands support is enabled
     */
    @ApiEndpointDoc(
        summary = "Check the remote commands status flag for a kickstart profile.",
        requestClass = KickstartLabelRequest.class,
        responseClass = BooleanResponse.class,
        legacyDocResponse = @LegacyDocResponse(type = "boolean",
            name = "true if remote commands support is enabled; otherwise, false")
    )
    boolean checkRemoteCommands(User loggedInUser, String ksLabel);

    /**
     * Enables the remote command flag of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the kickstart profile label
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Enables the remote command flag in a kickstart profile",
        requestClass = KickstartLabelRequest.class,
        isIntegerResponse = true
    )
    int enableRemoteCommands(User loggedInUser, String ksLabel);

    /**
     * Disables the remote command flag of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the kickstart profile label
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Disables the remote command flag in a kickstart profile",
        requestClass = KickstartLabelRequest.class,
        isIntegerResponse = true
    )
    int disableRemoteCommands(User loggedInUser, String ksLabel);

    /**
     * Retrieves the SELinux enforcing mode of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the kickstart profile label
     * @return the SELinux enforcing mode
     */
    @ApiEndpointDoc(
        summary = "Retrieves the SELinux enforcing mode property of a kickstart profile.",
        method = HttpMethod.get,
        responseClass = SELinuxModeResponse.class,
        legacyDocResponse = @LegacyDocResponse(type = "string", name = "enforcing mode")
    )
    @Schema(allowableValues = {"enforcing", "permissive", "disabled"})
    String getSELinux(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "ksLabel", description = "the kickstart profile label",
            in = ParameterIn.QUERY, required = true) String ksLabel);

    /**
     * Sets the SELinux enforcing mode of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the kickstart profile label
     * @param enforcingMode the SELinux enforcing mode
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Sets the SELinux enforcing mode property of a kickstart profile so that a system created " +
            "using this profile will be have the appropriate SELinux enforcing mode.",
        requestClass = SetSELinuxRequest.class,
        isIntegerResponse = true
    )
    int setSELinux(User loggedInUser, String ksLabel, String enforcingMode);

    /**
     * Retrieves the locale of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the kickstart profile label
     * @return the locale of the profile
     */
    @ApiEndpointDoc(
        summary = "Retrieves the locale for a kickstart profile.",
        method = HttpMethod.get,
        responseClass = LocaleResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "locale info")
    )
    Map<String, Object> getLocale(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "ksLabel", description = "the kickstart profile label",
            in = ParameterIn.QUERY, required = true) String ksLabel);

    /**
     * Sets the locale of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the kickstart profile label
     * @param locale the locale
     * @param useUtc whether the hardware clock uses UTC
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Sets the locale for a kickstart profile.",
        requestClass = SetLocaleRequest.class,
        isIntegerResponse = true
    )
    int setLocale(User loggedInUser, String ksLabel, String locale, Boolean useUtc);

    /**
     * Sets the partitioning scheme of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the kickstart profile label
     * @param scheme the partitioning scheme
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set the partitioning scheme for a kickstart profile.",
        requestClass = SetPartitioningSchemeRequest.class,
        isIntegerResponse = true
    )
    int setPartitioningScheme(User loggedInUser, String ksLabel, List<String> scheme);

    /**
     * Gets the partitioning scheme of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the kickstart profile label
     * @return the partitioning scheme
     */
    @ApiEndpointDoc(
        summary = "Get the partitioning scheme for a kickstart profile.",
        method = HttpMethod.get,
        responseClass = StringListResponse.class,
        responseDescription = "a list of partitioning commands used to setup the partitions, logical volumes and " +
            "volume groups"
    )
    List<String> getPartitioningScheme(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "ksLabel", description = "the label of a kickstart profile",
            in = ParameterIn.QUERY, required = true) String ksLabel);

    /**
     * Lists the keys associated with a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the kickstart profile label
     * @return the keys of the profile
     */
    @ApiEndpointDoc(
        summary = "Returns the set of all keys associated with the given kickstart profile.",
        method = HttpMethod.get,
        responseClass = CryptoKeyListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "key")
    )
    Set<CryptoKey> listKeys(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "ksLabel", description = "the kickstart profile label",
            in = ParameterIn.QUERY, required = true) String ksLabel);

    /**
     * Adds keys to a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the kickstart profile label
     * @param descriptions the keys to add
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Adds the given list of keys to the specified kickstart profile.",
        requestClass = AddKeysRequest.class,
        isIntegerResponse = true
    )
    int addKeys(User loggedInUser, String ksLabel, List<String> descriptions);

    /**
     * Removes keys from a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the kickstart profile label
     * @param descriptions the keys to remove
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Removes the given list of keys from the specified kickstart profile.",
        requestClass = RemoveKeysRequest.class,
        isIntegerResponse = true
    )
    int removeKeys(User loggedInUser, String ksLabel, List<String> descriptions);

    /**
     * Lists the file preservations associated with a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the kickstart profile label
     * @return the file preservations of the profile
     */
    @ApiEndpointDoc(
        summary = "Returns the set of all file preservations associated with the given kickstart profile.",
        method = HttpMethod.get,
        responseClass = FileListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "file list")
    )
    Set<FileList> listFilePreservations(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "ksLabel", description = "the kickstart profile label",
            in = ParameterIn.QUERY, required = true) String ksLabel);

    /**
     * Adds file preservations to a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the kickstart profile label
     * @param filePreservations the file preservations to add
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Adds the given list of file preservations to the specified kickstart profile.",
        requestClass = FilePreservationsRequest.class,
        isIntegerResponse = true
    )
    int addFilePreservations(User loggedInUser, String ksLabel, List<String> filePreservations);

    /**
     * Removes file preservations from a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the kickstart profile label
     * @param filePreservations the file preservations to remove
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Removes the given list of file preservations from the specified kickstart profile.",
        requestClass = FilePreservationsRequest.class,
        isIntegerResponse = true
    )
    int removeFilePreservations(User loggedInUser, String ksLabel, List<String> filePreservations);

    /**
     * Sets the registration type of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the kickstart profile label
     * @param registrationType the registration type
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Sets the registration type of a given kickstart profile. Registration Type can be one of " +
            "reactivation/deletion/none These types determine the behaviour of the re registration when using " +
            "this profile.",
        requestClass = SetRegistrationTypeRequest.class,
        isIntegerResponse = true
    )
    int setRegistrationType(User loggedInUser, String ksLabel, String registrationType);

    /**
     * Returns the registration type of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the kickstart profile label
     * @return the registration type
     */
    @ApiEndpointDoc(
        summary = "returns the registration type of a given kickstart profile. Registration Type can be one of " +
            "reactivation/deletion/none These types determine the behaviour of the registration when using this " +
            "profile for reprovisioning.",
        requestClass = KickstartLabelRequest.class,
        responseClass = RegistrationTypeResponse.class,
        legacyDocResponse = @LegacyDocResponse(type = "string", name = "the registration type")
    )
    @Schema(allowableValues = {"reactivation", "deletion", "none"})
    String getRegistrationType(User loggedInUser, String ksLabel);

    @Schema(name = "ApiResponseBoolean")
    interface BooleanResponse extends ApiResponseWrapper<Boolean> { }

    @Schema(name = "ApiResponseSELinuxMode")
    interface SELinuxModeResponse extends ApiResponseWrapper<String> { }

    @Schema(name = "ApiResponseRegistrationType")
    interface RegistrationTypeResponse extends ApiResponseWrapper<String> { }

    @Schema(name = "ApiResponsePartitioningScheme")
    interface StringListResponse extends ApiResponseWrapper<List<String>> { }

    @Schema(name = "ApiResponseKickstartLocale")
    interface LocaleResponse extends ApiResponseWrapper<LocaleInfoDoc> { }

    @Schema(name = "ApiResponseCryptoKeyList")
    interface CryptoKeyListResponse extends ApiResponseWrapper<List<CryptoKeyDoc>> { }

    @Schema(name = "ApiResponseFileListList")
    interface FileListResponse extends ApiResponseWrapper<List<FileListDoc>> { }

    @Schema(name = "KickstartProfileLabelRequest")
    interface KickstartLabelRequest {

        /**
         * @return the kickstart profile label
         */
        @Schema(description = "the kickstart profile label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();
    }

    @Schema(name = "KickstartSetSELinuxRequest")
    @JsonPropertyOrder({"ksLabel", "enforcingMode"})
    interface SetSELinuxRequest {

        /**
         * @return the kickstart profile label
         */
        @Schema(description = "the kickstart profile label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return the SELinux enforcing mode
         */
        @Schema(description = "the SELinux enforcing mode",
                allowableValues = {"enforcing", "permissive", "disabled"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getEnforcingMode();
    }

    @Schema(name = "KickstartSetLocaleRequest")
    @JsonPropertyOrder({"ksLabel", "locale", "useUtc"})
    interface SetLocaleRequest {

        /**
         * @return the kickstart profile label
         */
        @Schema(description = "the kickstart profile label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return the locale
         */
        @Schema(description = "the locale", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLocale();

        /**
         * @return whether the hardware clock uses UTC
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"true", "false"},
                extensions = @Extension(name = "x-uyuni-doc-option-descriptions", properties = {
                    @ExtensionProperty(name = "true", value = "the hardware clock uses UTC"),
                    @ExtensionProperty(name = "false", value = "the hardware clock does not use UTC")
                }))
        Boolean getUseUtc();
    }

    @Schema(name = "KickstartSetPartitioningSchemeRequest")
    @JsonPropertyOrder({"ksLabel", "scheme"})
    interface SetPartitioningSchemeRequest {

        /**
         * @return the kickstart profile label
         */
        @Schema(description = "the label of the kickstart profile to update",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return the partitioning scheme
         */
        @Schema(description = "the partitioning scheme is a list of partitioning command strings used to setup " +
                    "the partitions, volume groups and logical volumes.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getScheme();
    }

    @Schema(name = "KickstartAddKeysRequest")
    @JsonPropertyOrder({"ksLabel", "descriptions"})
    interface AddKeysRequest {

        /**
         * @return the kickstart profile label
         */
        @Schema(description = "the kickstart profile label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return the keys to add
         */
        @Schema(description = "the list identifying the keys to add", requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getDescriptions();
    }

    @Schema(name = "KickstartRemoveKeysRequest")
    @JsonPropertyOrder({"ksLabel", "descriptions"})
    interface RemoveKeysRequest {

        /**
         * @return the kickstart profile label
         */
        @Schema(description = "the kickstart profile label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return the keys to remove
         */
        @Schema(description = "the list identifying the keys to remove",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getDescriptions();
    }

    @Schema(name = "KickstartFilePreservationsRequest")
    @JsonPropertyOrder({"ksLabel", "filePreservations"})
    interface FilePreservationsRequest {

        /**
         * @return the kickstart profile label
         */
        @Schema(description = "the kickstart profile label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return the file preservations
         */
        @Schema(description = "the list identifying the file preservations to add",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getFilePreservations();
    }

    @Schema(name = "KickstartSetRegistrationTypeRequest")
    @JsonPropertyOrder({"ksLabel", "registrationType"})
    interface SetRegistrationTypeRequest {

        /**
         * @return the kickstart profile label
         */
        @Schema(description = "the kickstart profile label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return the registration type
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"reactivation", "deletion", "none"},
                extensions = @Extension(name = "x-uyuni-doc-option-descriptions", properties = {
                    @ExtensionProperty(name = "reactivation",
                        value = "to try and generate a reactivation key and use that to register the system when " +
                            "reprovisioning a system."),
                    @ExtensionProperty(name = "deletion",
                        value = "to try and delete the existing system profile and reregister the system being " +
                            "reprovisioned as new"),
                    @ExtensionProperty(name = "none",
                        value = "to preserve the status quo and leave the current system as a duplicate on a " +
                            "reprovision.")
                }))
        String getRegistrationType();
    }

    @Schema(name = "KickstartLocaleInfo")
    @JsonPropertyOrder({"locale", "useUtc"})
    interface LocaleInfoDoc {

        /**
         * @return the locale
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLocale();

        /**
         * @return whether the hardware clock uses UTC
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"true", "false"},
                extensions = @Extension(name = "x-uyuni-doc-option-descriptions", properties = {
                    @ExtensionProperty(name = "true", value = "the hardware clock uses UTC"),
                    @ExtensionProperty(name = "false", value = "the hardware clock does not use UTC")
                }))
        Boolean getUseUtc();
    }

    @Schema(name = "KickstartCryptoKey")
    @JsonPropertyOrder({"description", "type", "content"})
    interface CryptoKeyDoc {

        /**
         * @return the description of the key
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();

        /**
         * @return the type of the key
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getType();

        /**
         * @return the content of the key
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getContent();
    }

    @Schema(name = "KickstartFileList")
    @JsonPropertyOrder({"name", "fileNames"})
    interface FileListDoc {

        /**
         * @return the name of the file list
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the file names of the file list
         */
        @Schema(name = "file_names", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "the list of file names")
        List<String> getFileNames();
    }
}
