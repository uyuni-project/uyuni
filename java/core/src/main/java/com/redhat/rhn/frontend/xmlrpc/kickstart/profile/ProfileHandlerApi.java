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
package com.redhat.rhn.frontend.xmlrpc.kickstart.profile;

import com.redhat.rhn.domain.kickstart.KickstartScript;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.kickstart.KickstartOptionValue;
import com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandlerApi;
import com.redhat.rhn.frontend.xmlrpc.kickstart.profile.keys.KeysHandlerApi;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link ProfileHandler}.
 */
@Tag(name = "kickstart.profile",
    description = "Provides methods to access and modify many aspects of a kickstart profile.")
public interface ProfileHandlerApi {

    /**
     * Returns the kickstart tree of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @return the label of the kickstart tree
     */
    @ApiEndpointDoc(
        summary = "Get the kickstart tree for a kickstart profile.",
        method = HttpMethod.get,
        responseClass = StringResponse.class,
        responseDescription = "Label of the kickstart tree.",
        legacyDocResponse = @LegacyDocResponse(name = "kstreeLabel")
    )
    String getKickstartTree(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "ksLabel", in = ParameterIn.QUERY, required = true,
                description = "Label of kickstart profile to be changed.") String ksLabel);

    /**
     * Returns the update type of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @return the update type
     */
    @ApiEndpointDoc(
        summary = "Get the update type for a kickstart profile.",
        method = HttpMethod.get,
        responseClass = StringResponse.class,
        responseDescription = "Update type for this Kickstart Profile.",
        legacyDocResponse = @LegacyDocResponse(name = "update_type")
    )
    String getUpdateType(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "ksLabel", in = ParameterIn.QUERY, required = true,
                description = "Label of kickstart profile.") String ksLabel);

    /**
     * Returns the ks.cfg preservation option of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @return the value of the option
     */
    @ApiEndpointDoc(
        summary = "Get ks.cfg preservation option for a kickstart profile.",
        method = HttpMethod.get,
        responseClass = BooleanResponse.class,
        responseDescription = "The value of the option. True means that ks.cfg will be copied to " +
            "/root, false means that it will not",
        legacyDocResponse = @LegacyDocResponse(name = "preserve")
    )
    Boolean getCfgPreservation(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "ksLabel", in = ParameterIn.QUERY, required = true,
                description = "Label of kickstart profile to be changed.") String ksLabel);

    /**
     * Sets the ks.cfg preservation option of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @param preserve whether ks.cfg is copied to /root
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set ks.cfg preservation option for a kickstart profile.",
        requestClass = SetCfgPreservationRequest.class,
        isIntegerResponse = true
    )
    int setCfgPreservation(User loggedInUser, String ksLabel, Boolean preserve);

    /**
     * Sets the logging options of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @param pre whether to log the pre section
     * @param post whether to log the post section
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set logging options for a kickstart profile.",
        requestClass = SetLoggingRequest.class,
        isIntegerResponse = true
    )
    int setLogging(User loggedInUser, String ksLabel, Boolean pre, Boolean post);

    /**
     * Sets the kickstart tree of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @param kstreeLabel the label of the new kickstart tree
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set the kickstart tree for a kickstart profile.",
        requestClass = SetKickstartTreeRequest.class,
        isIntegerResponse = true
    )
    int setKickstartTree(User loggedInUser, String ksLabel, String kstreeLabel);

    /**
     * Sets the update type of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @param updateType the new update type
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set the update typefor a kickstart profile.",
        requestClass = SetUpdateTypeRequest.class,
        isIntegerResponse = true
    )
    int setUpdateType(User loggedInUser, String ksLabel, String updateType);

    /**
     * Returns the child channels of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @return the labels of the child channels
     */
    @ApiEndpointDoc(
        summary = "Get the child channels for a kickstart profile.",
        method = HttpMethod.get,
        responseClass = StringListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channelLabel")
    )
    List<String> getChildChannels(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "ksLabel", in = ParameterIn.QUERY, required = true,
                description = "Label of kickstart profile.") String ksLabel);

    /**
     * Sets the child channels of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @param channelLabels the labels of the child channels
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set the child channels for a kickstart profile.",
        requestClass = SetChildChannelsRequest.class,
        isIntegerResponse = true
    )
    int setChildChannels(User loggedInUser, String ksLabel, List<String> channelLabels);

    /**
     * Lists the pre and post scripts of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @return the list of scripts
     */
    @ApiEndpointDoc(
        summary = "List the pre and post scripts for a kickstart profile in the order they will " +
            "run during the kickstart.",
        method = HttpMethod.get,
        responseClass = ScriptListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "kickstart script")
    )
    List<KickstartScript> listScripts(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "ksLabel", in = ParameterIn.QUERY, required = true,
                description = "The label of the kickstart") String ksLabel);

    /**
     * Changes the order in which the kickstart scripts run.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @param preScripts the IDs of the ordered pre scripts
     * @param postScriptsBeforeRegistration the IDs of the ordered post scripts run before
     *        registration
     * @param postScriptsAfterRegistration the IDs of the ordered post scripts run after
     *        registration
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Change the order that kickstart scripts will run for this kickstart profile. " +
            "Scripts will run in the order they appear in the array. There are three arrays, one " +
            "for all pre scripts, one for the post scripts that run before registration and " +
            "server actions happen, and one for post scripts that run after registration. All " +
            "scripts must be included in one of these lists, as appropriate.",
        requestClass = OrderScriptsRequest.class,
        isIntegerResponse = true
    )
    int orderScripts(User loggedInUser, String ksLabel, List<Integer> preScripts,
            List<Integer> postScriptsBeforeRegistration, List<Integer> postScriptsAfterRegistration);

    /**
     * Adds a pre or post script to a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @param name the name of the script
     * @param contents the contents of the script
     * @param interpreter the interpreter to use
     * @param type the type of the script
     * @param chroot whether to run the script in the chrooted install location
     * @return the id of the added script
     */
    @ApiEndpointDoc(
        summary = "Add a pre/post script to a kickstart profile.",
        requestClass = AddScriptRequest.class,
        isIntegerResponse = true,
        responseDescription = "the id of the added script",
        legacyDocResponse = @LegacyDocResponse(name = "id")
    )
    int addScript(User loggedInUser, String ksLabel, String name, String contents,
            String interpreter, String type, Boolean chroot);

    /**
     * Adds a pre or post script to a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @param name the name of the script
     * @param contents the contents of the script
     * @param interpreter the interpreter to use
     * @param type the type of the script
     * @param chroot whether to run the script in the chrooted install location
     * @param template whether templating using cobbler is enabled
     * @return the id of the added script
     */
    @ApiEndpointDoc(
        summary = "Add a pre/post script to a kickstart profile.",
        requestClass = AddScriptWithTemplateRequest.class,
        isIntegerResponse = true,
        responseDescription = "the id of the added script",
        legacyDocResponse = @LegacyDocResponse(name = "id")
    )
    int addScript(User loggedInUser, String ksLabel, String name, String contents,
            String interpreter, String type, Boolean chroot, Boolean template);

    /**
     * Adds a pre or post script to a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @param name the name of the script
     * @param contents the contents of the script
     * @param interpreter the interpreter to use
     * @param type the type of the script
     * @param chroot whether to run the script in the chrooted install location
     * @param template whether templating using cobbler is enabled
     * @param erroronfail whether to throw an error if the script fails
     * @return the id of the added script
     */
    @ApiEndpointDoc(
        summary = "Add a pre/post script to a kickstart profile.",
        requestClass = AddScriptWithErrorOnFailRequest.class,
        isIntegerResponse = true,
        responseDescription = "the id of the added script",
        legacyDocResponse = @LegacyDocResponse(name = "id")
    )
    int addScript(User loggedInUser, String ksLabel, String name, String contents,
            String interpreter, String type, Boolean chroot, Boolean template, Boolean erroronfail);

    /**
     * Removes a script from a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @param scriptId the id of the script to remove
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Remove a script from a kickstart profile.",
        requestClass = RemoveScriptRequest.class,
        isIntegerResponse = true
    )
    int removeScript(User loggedInUser, String ksLabel, Integer scriptId);

    /**
     * Downloads the full contents of a kickstart file.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @param host the host to use when referring to the server
     * @return the contents of the kickstart file
     */
    @ApiEndpointDoc(
        summary = "Download the full contents of a kickstart file.",
        requestClass = DownloadKickstartRequest.class,
        responseClass = StringResponse.class,
        responseDescription = "The contents of the kickstart file. Note: if an activation key is " +
            "not associated with the kickstart file, registration will not occur in the generated " +
            "%post section. If one is associated, it will be used for registration",
        legacyDocResponse = @LegacyDocResponse(name = "ks")
    )
    String downloadKickstart(User loggedInUser, String ksLabel, String host);

    /**
     * Downloads the Cobbler-rendered kickstart file.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @return the contents of the kickstart file
     */
    @ApiEndpointDoc(
        summary = "Downloads the Cobbler-rendered Kickstart file.",
        requestClass = KsLabelRequest.class,
        responseClass = StringResponse.class,
        responseDescription = "The contents of the kickstart file",
        legacyDocResponse = @LegacyDocResponse(name = "ks")
    )
    String downloadRenderedKickstart(User loggedInUser, String ksLabel);

    /**
     * Returns the advanced options of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @return the advanced options
     */
    @ApiEndpointDoc(
        summary = "Get advanced options for a kickstart profile.",
        method = HttpMethod.get,
        responseClass = AdvancedOptionListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "option")
    )
    Object[] getAdvancedOptions(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "ksLabel", in = ParameterIn.QUERY, required = true,
                description = "Label of kickstart profile to be changed.") String ksLabel);

    /**
     * Sets the advanced options of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @param options the advanced options
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set advanced options for a kickstart profile. 'md5_crypt_rootpw' is not " +
            "supported anymore. If 'sha256_crypt_rootpw' is set to 'True', 'root_pw' is taken as " +
            "plaintext and will sha256 encrypted on server side, otherwise a hash encoded " +
            "password (according to the auth option) is expected",
        requestClass = SetAdvancedOptionsRequest.class,
        isIntegerResponse = true
    )
    int setAdvancedOptions(User loggedInUser, String ksLabel, List<Map<String, String>> options);

    /**
     * Returns the custom options of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @return the custom options
     */
    @ApiEndpointDoc(
        summary = "Get custom options for a kickstart profile.",
        method = HttpMethod.get,
        responseClass = CustomOptionListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "option")
    )
    Object[] getCustomOptions(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "ksLabel", in = ParameterIn.QUERY, required = true) String ksLabel);

    /**
     * Sets the custom options of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @param options the custom options
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set custom options for a kickstart profile.",
        requestClass = SetCustomOptionsRequest.class,
        isIntegerResponse = true
    )
    int setCustomOptions(User loggedInUser, String ksLabel, List<String> options);

    /**
     * Lists all ip ranges of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @return the list of ip ranges
     */
    @ApiEndpointDoc(
        summary = "List all ip ranges for a kickstart profile.",
        method = HttpMethod.get,
        responseClass = KickstartHandlerApi.KickstartIpRangeListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "kickstart IP range")
    )
    Set listIpRanges(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "ksLabel", in = ParameterIn.QUERY, required = true,
                description = "The label of the kickstart") String ksLabel);

    /**
     * Adds an ip range to a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @param min the minimum of the range
     * @param max the maximum of the range
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Add an ip range to a kickstart profile.",
        requestClass = AddIpRangeRequest.class,
        isIntegerResponse = true
    )
    int addIpRange(User loggedInUser, String ksLabel, String min, String max);

    /**
     * Removes an ip range from a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @param ipAddress an ip address falling within the range to remove
     * @return 1 on successful removal, 0 if the range was not found
     */
    @ApiEndpointDoc(
        summary = "Remove an ip range from a kickstart profile.",
        requestClass = RemoveIpRangeRequest.class,
        isIntegerResponse = true,
        responseDescription = "1 on successful removal, 0 if range wasn't found for the specified " +
            "kickstart, exception otherwise",
        legacyDocResponse = @LegacyDocResponse(name = "status")
    )
    int removeIpRange(User loggedInUser, String ksLabel, String ipAddress);

    /**
     * Compares the activation keys of two kickstart profiles.
     *
     * @param loggedInUser the current user
     * @param kickstartLabel1 the label of the first kickstart profile
     * @param kickstartLabel2 the label of the second kickstart profile
     * @return the comparison info
     */
    @ApiEndpointDoc(
        summary = "Returns a list for each kickstart profile; each list will contain activation " +
            "keys not present on the other profile.",
        requestClass = CompareRequest.class,
        responseClass = ActivationKeyComparisonResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "Comparison Info")
    )
    Map<String, List<ActivationKey>> compareActivationKeys(User loggedInUser, String kickstartLabel1,
            String kickstartLabel2);

    /**
     * Compares the packages of two kickstart profiles.
     *
     * @param loggedInUser the current user
     * @param kickstartLabel1 the label of the first kickstart profile
     * @param kickstartLabel2 the label of the second kickstart profile
     * @return the comparison info
     */
    @ApiEndpointDoc(
        summary = "Returns a list for each kickstart profile; each list will contain package " +
            "names not present on the other profile.",
        requestClass = CompareRequest.class,
        responseClass = PackageComparisonResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "Comparison Info")
    )
    Map<String, Set<String>> comparePackages(User loggedInUser, String kickstartLabel1,
            String kickstartLabel2);

    /**
     * Compares the advanced options of two kickstart profiles.
     *
     * @param loggedInUser the current user
     * @param kickstartLabel1 the label of the first kickstart profile
     * @param kickstartLabel2 the label of the second kickstart profile
     * @return the comparison info
     */
    @ApiEndpointDoc(
        summary = "Returns a list for each kickstart profile; each list will contain the " +
            "properties that differ between the profiles and their values for that specific " +
            "profile .",
        requestClass = CompareRequest.class,
        responseClass = AdvancedOptionComparisonResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "Comparison Info")
    )
    Map<String, List<KickstartOptionValue>> compareAdvancedOptions(User loggedInUser,
            String kickstartLabel1, String kickstartLabel2);

    /**
     * Returns the variables of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @return the kickstart variables
     */
    @ApiEndpointDoc(
        summary = "Returns a list of variables associated with the specified kickstart profile",
        method = HttpMethod.get,
        responseClass = VariableResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "kickstart variable")
    )
    Map<String, Object> getVariables(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "ksLabel", in = ParameterIn.QUERY, required = true) String ksLabel);

    /**
     * Associates a list of kickstart variables with a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @param variables the kickstart variables
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Associates list of kickstart variables with the specified kickstart profile",
        requestClass = SetVariablesRequest.class,
        isIntegerResponse = true
    )
    int setVariables(User loggedInUser, String ksLabel, Map<String, Object> variables);

    /**
     * Lists the available OS repositories for a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @return the available repository labels
     */
    @ApiEndpointDoc(
        summary = "Lists available OS repositories to associate with the provided kickstart " +
            "profile.",
        method = HttpMethod.get,
        responseClass = StringListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "repositoryLabel")
    )
    String[] getAvailableRepositories(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "ksLabel", in = ParameterIn.QUERY, required = true) String ksLabel);

    /**
     * Lists the OS repositories associated with a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @return the associated repository labels
     */
    @ApiEndpointDoc(
        summary = "Lists all OS repositories associated with provided kickstart profile.",
        method = HttpMethod.get,
        responseClass = StringListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "repositoryLabel")
    )
    String[] getRepositories(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "ksLabel", in = ParameterIn.QUERY, required = true) String ksLabel);

    /**
     * Associates OS repositories to a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @param repoLabels the repository labels
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Associates OS repository to a kickstart profile.",
        requestClass = SetRepositoriesRequest.class,
        isIntegerResponse = true
    )
    int setRepositories(User loggedInUser, String ksLabel, List<String> repoLabels);

    /**
     * Returns the virtualization type of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @return the label of the virtualization type
     */
    @ApiEndpointDoc(
        summary = "For given kickstart profile label returns label of virtualization type it's " +
            "using",
        method = HttpMethod.get,
        responseClass = StringResponse.class,
        responseDescription = "Label of virtualization type.",
        legacyDocResponse = @LegacyDocResponse(name = "virtLabel")
    )
    String getVirtualizationType(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "ksLabel", in = ParameterIn.QUERY, required = true) String ksLabel);

    /**
     * Sets the virtualization type of a kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the label of the kickstart profile
     * @param typeLabel the label of the virtualization type
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "For given kickstart profile label sets its virtualization type.",
        requestClass = SetVirtualizationTypeRequest.class,
        isIntegerResponse = true
    )
    int setVirtualizationType(User loggedInUser, String ksLabel, String typeLabel);

    @Schema(name = "ApiResponseString")
    interface StringResponse extends ApiResponseWrapper<String> { }

    @Schema(name = "ApiResponseBoolean")
    interface BooleanResponse extends ApiResponseWrapper<Boolean> { }

    @Schema(name = "ApiResponseStringList")
    interface StringListResponse extends ApiResponseWrapper<List<String>> { }

    @Schema(name = "ApiResponseKickstartScriptList")
    interface ScriptListResponse extends ApiResponseWrapper<List<KickstartScriptDoc>> { }

    @Schema(name = "ApiResponseKickstartAdvancedOptionList")
    interface AdvancedOptionListResponse extends ApiResponseWrapper<List<AdvancedOptionDoc>> { }

    @Schema(name = "ApiResponseKickstartCustomOptionList")
    interface CustomOptionListResponse extends ApiResponseWrapper<List<CustomOptionDoc>> { }

    @Schema(name = "ApiResponseKickstartVariable")
    interface VariableResponse extends ApiResponseWrapper<VariableDoc> { }

    @Schema(name = "ApiResponseActivationKeyComparison")
    interface ActivationKeyComparisonResponse extends ApiResponseWrapper<ActivationKeyComparisonDoc> { }

    @Schema(name = "ApiResponsePackageComparison")
    interface PackageComparisonResponse extends ApiResponseWrapper<PackageComparisonDoc> { }

    @Schema(name = "ApiResponseAdvancedOptionComparison")
    interface AdvancedOptionComparisonResponse
        extends ApiResponseWrapper<AdvancedOptionComparisonDoc> { }

    @Schema(name = "KickstartProfileKsLabelRequest")
    interface KsLabelRequest {

        /**
         * @return the label of the kickstart profile
         */
        @Schema(description = "The label of the kickstart to download.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();
    }

    @Schema(name = "KickstartProfileSetCfgPreservationRequest")
    @JsonPropertyOrder({"ksLabel", "preserve"})
    interface SetCfgPreservationRequest {

        /**
         * @return the label of the kickstart profile
         */
        @Schema(description = "Label of kickstart profile to be changed.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return whether ks.cfg is copied to /root
         */
        @Schema(description = "whether or not ks.cfg and all %include fragments will be copied to " +
                "/root.", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getPreserve();
    }

    @Schema(name = "KickstartProfileSetLoggingRequest")
    @JsonPropertyOrder({"ksLabel", "pre", "post"})
    interface SetLoggingRequest {

        /**
         * @return the label of the kickstart profile
         */
        @Schema(description = "Label of kickstart profile to be changed.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return whether to log the pre section
         */
        @Schema(description = "whether or not to log the pre section of a kickstart to " +
                "/root/ks-pre.log", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getPre();

        /**
         * @return whether to log the post section
         */
        @Schema(description = "whether or not to log the post section of a kickstart to " +
                "/root/ks-post.log", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getPost();
    }

    @Schema(name = "KickstartProfileSetKickstartTreeRequest")
    @JsonPropertyOrder({"ksLabel", "kstreeLabel"})
    interface SetKickstartTreeRequest {

        /**
         * @return the label of the kickstart profile
         */
        @Schema(description = "Label of kickstart profile to be changed.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return the label of the new kickstart tree
         */
        @Schema(description = "Label of new kickstart tree.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getKstreeLabel();
    }

    @Schema(name = "KickstartProfileSetUpdateTypeRequest")
    @JsonPropertyOrder({"ksLabel", "updateType"})
    interface SetUpdateTypeRequest {

        /**
         * @return the label of the kickstart profile
         */
        @Schema(description = "Label of kickstart profile to be changed.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return the new update type
         */
        @Schema(description = "The new update type to set. Possible values are 'all' and 'none'.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getUpdateType();
    }

    @Schema(name = "KickstartProfileSetChildChannelsRequest")
    @JsonPropertyOrder({"ksLabel", "channelLabels"})
    interface SetChildChannelsRequest {

        /**
         * @return the label of the kickstart profile
         */
        @Schema(description = "Label of kickstart profile to be changed.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return the labels of the child channels
         */
        @Schema(description = "List of labels of child channels",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getChannelLabels();
    }

    @Schema(name = "KickstartProfileOrderScriptsRequest")
    @JsonPropertyOrder({"ksLabel", "preScripts", "postScriptsBeforeRegistration",
        "postScriptsAfterRegistration"})
    interface OrderScriptsRequest {

        /**
         * @return the label of the kickstart profile
         */
        @Schema(description = "The label of the kickstart", requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return the IDs of the ordered pre scripts
         */
        @Schema(description = "IDs of the ordered pre scripts",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getPreScripts();

        /**
         * @return the IDs of the ordered post scripts run before registration
         */
        @Schema(description = "IDs of the ordered post scripts that will run before registration",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getPostScriptsBeforeRegistration();

        /**
         * @return the IDs of the ordered post scripts run after registration
         */
        @Schema(description = "IDs of the ordered post scripts that will run after registration",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getPostScriptsAfterRegistration();
    }

    @Schema(name = "KickstartProfileAddScriptRequest")
    @JsonPropertyOrder({"ksLabel", "name", "contents", "interpreter", "type", "chroot"})
    interface AddScriptRequest {

        /**
         * @return the label of the kickstart profile
         */
        @Schema(description = "The kickstart label to add the script to.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return the name of the script
         */
        @Schema(description = "The kickstart script name.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the contents of the script
         */
        @Schema(description = "The full script to add.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getContents();

        /**
         * @return the interpreter to use
         */
        @Schema(description = "The path to the interpreter to use (i.e. /bin/bash). An empty " +
                "string will use the kickstart default interpreter.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getInterpreter();

        /**
         * @return the type of the script
         */
        @Schema(description = "The type of script (either 'pre' or 'post').",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getType();

        /**
         * @return whether to run the script in the chrooted install location
         */
        @Schema(description = "Whether to run the script in the chrooted install location " +
                "(recommended) or not.", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getChroot();
    }

    @Schema(name = "KickstartProfileAddScriptWithTemplateRequest")
    @JsonPropertyOrder({"ksLabel", "name", "contents", "interpreter", "type", "chroot", "template"})
    interface AddScriptWithTemplateRequest {

        /**
         * @return the label of the kickstart profile
         */
        @Schema(description = "The kickstart label to add the script to.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return the name of the script
         */
        @Schema(description = "The kickstart script name.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the contents of the script
         */
        @Schema(description = "The full script to add.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getContents();

        /**
         * @return the interpreter to use
         */
        @Schema(description = "The path to the interpreter to use (i.e. /bin/bash). An empty " +
                "string will use the kickstart default interpreter.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getInterpreter();

        /**
         * @return the type of the script
         */
        @Schema(description = "The type of script (either 'pre' or 'post').",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getType();

        /**
         * @return whether to run the script in the chrooted install location
         */
        @Schema(description = "Whether to run the script in the chrooted install location " +
                "(recommended) or not.", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getChroot();

        /**
         * @return whether templating using cobbler is enabled
         */
        @Schema(description = "Enable templating using cobbler.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getTemplate();
    }

    @Schema(name = "KickstartProfileAddScriptWithErrorOnFailRequest")
    @JsonPropertyOrder({"ksLabel", "name", "contents", "interpreter", "type", "chroot", "template",
        "erroronfail"})
    interface AddScriptWithErrorOnFailRequest {

        /**
         * @return the label of the kickstart profile
         */
        @Schema(description = "The kickstart label to add the script to.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return the name of the script
         */
        @Schema(description = "The kickstart script name.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the contents of the script
         */
        @Schema(description = "The full script to add.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getContents();

        /**
         * @return the interpreter to use
         */
        @Schema(description = "The path to the interpreter to use (i.e. /bin/bash). An empty " +
                "string will use the kickstart default interpreter.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getInterpreter();

        /**
         * @return the type of the script
         */
        @Schema(description = "The type of script (either 'pre' or 'post').",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getType();

        /**
         * @return whether to run the script in the chrooted install location
         */
        @Schema(description = "Whether to run the script in the chrooted install location " +
                "(recommended) or not.", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getChroot();

        /**
         * @return whether templating using cobbler is enabled
         */
        @Schema(description = "Enable templating using cobbler.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getTemplate();

        /**
         * @return whether to throw an error if the script fails
         */
        @Schema(description = "Whether to throw an error if the script fails or not",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getErroronfail();
    }

    @Schema(name = "KickstartProfileRemoveScriptRequest")
    @JsonPropertyOrder({"ksLabel", "scriptId"})
    interface RemoveScriptRequest {

        /**
         * @return the label of the kickstart profile
         */
        @Schema(description = "The kickstart from which to remove the script from.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return the id of the script to remove
         */
        @Schema(description = "The id of the script to remove.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getScriptId();
    }

    @Schema(name = "KickstartProfileDownloadKickstartRequest")
    @JsonPropertyOrder({"ksLabel", "host"})
    interface DownloadKickstartRequest {

        /**
         * @return the label of the kickstart profile
         */
        @Schema(description = "The label of the kickstart to download.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return the host to use when referring to the server
         */
        @Schema(description = "The host to use when referring to the #product() server. Usually " +
                "this should be the FQDN, but could be the ip address or shortname as well.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getHost();
    }

    @Schema(name = "KickstartProfileSetAdvancedOptionsRequest")
    @JsonPropertyOrder({"ksLabel", "options"})
    interface SetAdvancedOptionsRequest {

        /**
         * @return the label of the kickstart profile
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return the advanced options
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "advanced options")
        List<AdvancedOptionRequestDoc> getOptions();
    }

    @Schema(name = "KickstartProfileAdvancedOption", description = "advanced options")
    @JsonPropertyOrder({"name", "arguments"})
    interface AdvancedOptionRequestDoc {

        /**
         * @return the name of the advanced option
         */
        @Schema(description = "Name of the advanced option. Valid Option names: autostep, " +
                "interactive, install, upgrade, text, network, cdrom, harddrive, nfs, url, lang, " +
                "langsupport keyboard, mouse, device, deviceprobe, zerombr, clearpart, " +
                "bootloader, timezone, auth, rootpw, selinux, reboot, firewall, xconfig, skipx, " +
                "key, ignoredisk, autopart, cmdline, firstboot, graphical, iscsi, iscsiname, " +
                "logging, monitor, multipath, poweroff, halt, services, shutdown, user, vnc, " +
                "zfcp, driverdisk, sha256_crypt_rootpw",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the arguments of the option
         */
        @Schema(description = "Arguments of the option", requiredMode = Schema.RequiredMode.REQUIRED)
        String getArguments();
    }

    @Schema(name = "KickstartProfileSetCustomOptionsRequest")
    @JsonPropertyOrder({"ksLabel", "options"})
    interface SetCustomOptionsRequest {

        /**
         * @return the label of the kickstart profile
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return the custom options
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getOptions();
    }

    @Schema(name = "KickstartProfileAddIpRangeRequest")
    @JsonPropertyOrder({"ksLabel", "min", "max"})
    interface AddIpRangeRequest {

        /**
         * @return the label of the kickstart profile
         */
        @Schema(description = "The label of the kickstart", requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return the minimum of the range
         */
        @Schema(description = "The ip address making up the minimum of the range (i.e. " +
                "192.168.0.1)", requiredMode = Schema.RequiredMode.REQUIRED)
        String getMin();

        /**
         * @return the maximum of the range
         */
        @Schema(description = "The ip address making up the maximum of the range (i.e. " +
                "192.168.0.254)", requiredMode = Schema.RequiredMode.REQUIRED)
        String getMax();
    }

    @Schema(name = "KickstartProfileRemoveIpRangeRequest")
    @JsonPropertyOrder({"ksLabel", "ipAddress"})
    interface RemoveIpRangeRequest {

        /**
         * @return the label of the kickstart profile
         */
        @Schema(description = "The kickstart label of the ip range you want to remove",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return an ip address falling within the range to remove
         */
        @Schema(description = "An Ip Address that falls within the range that you are wanting to " +
                "remove. The min or max of the range will work.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getIpAddress();
    }

    @Schema(name = "KickstartProfileCompareRequest")
    @JsonPropertyOrder({"kickstartLabel1", "kickstartLabel2"})
    interface CompareRequest {

        /**
         * @return the label of the first kickstart profile
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getKickstartLabel1();

        /**
         * @return the label of the second kickstart profile
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getKickstartLabel2();
    }

    @Schema(name = "KickstartProfileSetVariablesRequest")
    @JsonPropertyOrder({"ksLabel", "variables"})
    interface SetVariablesRequest {

        /**
         * @return the label of the kickstart profile
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return the kickstart variables
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        VariableDoc getVariables();
    }

    @Schema(name = "KickstartProfileSetRepositoriesRequest")
    @JsonPropertyOrder({"ksLabel", "repoLabels"})
    interface SetRepositoriesRequest {

        /**
         * @return the label of the kickstart profile
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return the repository labels
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getRepoLabels();
    }

    @Schema(name = "KickstartProfileSetVirtualizationTypeRequest")
    @JsonPropertyOrder({"ksLabel", "typeLabel"})
    interface SetVirtualizationTypeRequest {

        /**
         * @return the label of the kickstart profile
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return the label of the virtualization type
         */
        @Schema(description = "One of the following: 'none', 'qemu', 'para_host', 'xenpv', 'xenfv'",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getTypeLabel();
    }

    @Schema(name = "KickstartScriptInfo", description = "kickstart script")
    @JsonPropertyOrder({"id", "name", "contents", "scriptType", "interpreter", "chroot",
        "erroronfail", "template", "beforeRegistration"})
    interface KickstartScriptDoc {

        /**
         * @return the id of the script
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the name of the script
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the contents of the script
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getContents();

        /**
         * @return the type of the script
         */
        @Schema(name = "script_type", description = "the type of script ('pre' or 'post')",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getScriptType();

        /**
         * @return the interpreter to use
         */
        @Schema(description = "the scripting language interpreter to use for this script.  An " +
                "empty string indicates the default kickstart shell.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getInterpreter();

        /**
         * @return whether the script runs in the chroot environment
         */
        @Schema(description = "true if the script will be executed within the chroot environment",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getChroot();

        /**
         * @return whether the script throws an error if it fails
         */
        @Schema(description = "true if the script will throw an error if it fails",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getErroronfail();

        /**
         * @return whether templating using cobbler is enabled
         */
        @Schema(description = "true if templating using cobbler is enabled",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getTemplate();

        /**
         * @return whether the script runs before registration
         */
        @Schema(description = "true if script will run before the server registers and performs " +
                "server actions", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getBeforeRegistration();
    }

    @Schema(name = "KickstartAdvancedOptionInfo", description = "option")
    @JsonPropertyOrder({"name", "arguments"})
    interface AdvancedOptionDoc {

        /**
         * @return the name of the option
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the arguments of the option
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getArguments();
    }

    @Schema(name = "KickstartCustomOptionInfo", description = "option")
    @JsonPropertyOrder({"id", "arguments"})
    interface CustomOptionDoc {

        /**
         * @return the id of the option
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the arguments of the option
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getArguments();
    }

    @Schema(name = "KickstartOptionValueInfo", description = "value")
    @JsonPropertyOrder({"name", "value", "enabled"})
    interface KickstartOptionValueDoc {

        /**
         * @return the name of the option
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the value of the option
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getValue();

        /**
         * @return whether the option is enabled
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getEnabled();
    }

    @Schema(name = "KickstartVariableInfo", description = "kickstart variable")
    @JsonPropertyOrder({"key", "value"})
    interface VariableDoc {

        /**
         * @return the key of the variable
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getKey();

        /**
         * @return the value of the variable
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "string or int")
        String getValue();
    }

    @Schema(name = "KickstartActivationKeyComparison", description = "Comparison Info")
    @JsonPropertyOrder({"kickstartLabel1", "kickstartLabel2"})
    interface ActivationKeyComparisonDoc {

        /**
         * @return the activation keys only present on the first profile
         */
        @Schema(description = "Actual label of the first kickstart profile is the key into the " +
                "struct", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "array", name = "activation key")
        List<KeysHandlerApi.ActivationKeyDoc> getKickstartLabel1();

        /**
         * @return the activation keys only present on the second profile
         */
        @Schema(description = "Actual label of the second kickstart profile is the key into the " +
                "struct", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "array", name = "activation key")
        List<KeysHandlerApi.ActivationKeyDoc> getKickstartLabel2();
    }

    @Schema(name = "KickstartPackageComparison", description = "Comparison Info")
    @JsonPropertyOrder({"kickstartLabel1", "kickstartLabel2"})
    interface PackageComparisonDoc {

        /**
         * @return the package names only present on the first profile
         */
        @Schema(description = "Actual label of the first kickstart profile is the key into the " +
                "struct", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "array", name = "package name")
        List<String> getKickstartLabel1();

        /**
         * @return the package names only present on the second profile
         */
        @Schema(description = "Actual label of the second kickstart profile is the key into the " +
                "struct", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "array", name = "package name")
        List<String> getKickstartLabel2();
    }

    @Schema(name = "KickstartAdvancedOptionComparison", description = "Comparison Info")
    @JsonPropertyOrder({"kickstartLabel1", "kickstartLabel2"})
    interface AdvancedOptionComparisonDoc {

        /**
         * @return the differing options of the first profile
         */
        @Schema(description = "Actual label of the first kickstart profile is the key into the " +
                "struct", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "array", name = "value")
        List<KickstartOptionValueDoc> getKickstartLabel1();

        /**
         * @return the differing options of the second profile
         */
        @Schema(description = "Actual label of the second kickstart profile is the key into the " +
                "struct", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "array", name = "value")
        List<KickstartOptionValueDoc> getKickstartLabel2();
    }
}
