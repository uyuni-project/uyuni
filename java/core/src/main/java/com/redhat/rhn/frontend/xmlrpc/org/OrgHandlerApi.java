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
package com.redhat.rhn.frontend.xmlrpc.org;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.OrgDto;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;
import com.suse.manager.api.docs.PublicApiEndpoint;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link OrgHandler}.
 */
@Tag(name = "org", description = "Contains methods to access common organization management " +
    "functions available from the web interface.")
public interface OrgHandlerApi {

    /**
     * Creates a new organization.
     *
     * @param loggedInUser the current user
     * @param orgName the organization name
     * @param adminLogin the new administrator login name
     * @param adminPassword the new administrator password
     * @param prefix the new administrator's prefix
     * @param firstName the new administrator's first name
     * @param lastName the new administrator's last name
     * @param email the new administrator's e-mail
     * @param usePamAuth whether PAM authentication is used for the new account
     * @return the newly created organization
     */
    @ApiEndpointDoc(
        summary = "Create a new organization and associated administrator account.",
        requestClass = CreateOrgRequest.class,
        responseClass = OrgResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "organization info")
    )
    OrgDto create(User loggedInUser, String orgName, String adminLogin, String adminPassword, String prefix,
                  String firstName, String lastName, String email, Boolean usePamAuth);

    /**
     * Creates the first organization and user after the initial setup.
     *
     * @param orgName the organization name
     * @param adminLogin the new administrator login name
     * @param adminPassword the new administrator password
     * @param firstName the new administrator's first name
     * @param lastName the new administrator's last name
     * @param email the new administrator's e-mail
     * @return the newly created organization
     */
    @PublicApiEndpoint
    @ApiEndpointDoc(
        summary = "Create first organization and user after initial setup without authentication",
        requestClass = CreateFirstOrgRequest.class,
        responseClass = OrgResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "organization info")
    )
    OrgDto createFirst(String orgName, String adminLogin, String adminPassword, String firstName, String lastName,
                       String email);

    /**
     * Deletes an organization.
     *
     * @param loggedInUser the current user
     * @param orgId the id of the organization to delete
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete an organization. The default organization (i.e. orgId=1) cannot be deleted.",
        requestClass = OrgIdRequest.class,
        isIntegerResponse = true
    )
    int delete(User loggedInUser, Integer orgId);

    /**
     * Reads the content lifecycle management patch synchronization config option.
     *
     * @param loggedInUser the current user
     * @param orgId the organization id
     * @return the config option value
     */
    @ApiEndpointDoc(
        summary = "Reads the content lifecycle management patch synchronization config option.",
        method = HttpMethod.get,
        responseClass = BooleanResponse.class,
        responseDescription = "Get the config option value",
        legacyDocResponse = @LegacyDocResponse(type = "boolean", name = "status")
    )
    Boolean getClmSyncPatchesConfig(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "orgId", in = ParameterIn.QUERY, required = true) Integer orgId);

    /**
     * Returns the detailed information about an organization given its id.
     *
     * @param loggedInUser the current user
     * @param orgId the organization id
     * @return the organization details
     */
    @ApiEndpointDoc(
        summary = "The detailed information about an organization given the organization ID or name.",
        method = HttpMethod.get,
        responseClass = OrgResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "organization info")
    )
    OrgDto getDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "orgId", in = ParameterIn.QUERY, required = true) Integer orgId);

    /**
     * Returns the detailed information about an organization given its name.
     *
     * @param loggedInUser the current user
     * @param name the organization name
     * @return the organization details
     */
    @ApiEndpointDoc(
        summary = "The detailed information about an organization given the organization ID or name.",
        method = HttpMethod.get,
        responseClass = OrgResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "organization info")
    )
    OrgDto getDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "name", in = ParameterIn.QUERY, required = true) String name);

    /**
     * Returns the status of the SCAP detailed result file upload settings.
     *
     * @param loggedInUser the current user
     * @param orgId the organization id
     * @return the SCAP file upload settings
     */
    @ApiEndpointDoc(
        summary = "Get the status of SCAP detailed result file upload settings for the given organization.",
        method = HttpMethod.get,
        responseClass = ScapUploadInfoResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "scap_upload_info")
    )
    Map<String, Object> getPolicyForScapFileUpload(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "orgId", in = ParameterIn.QUERY, required = true) Integer orgId);

    /**
     * Returns the status of the SCAP result deletion settings.
     *
     * @param loggedInUser the current user
     * @param orgId the organization id
     * @return the SCAP result deletion settings
     */
    @ApiEndpointDoc(
        summary = "Get the status of SCAP result deletion settings for the given organization.",
        method = HttpMethod.get,
        responseClass = ScapDeletionInfoResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "scap_deletion_info")
    )
    Map<String, Object> getPolicyForScapResultDeletion(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "orgId", in = ParameterIn.QUERY, required = true) Integer orgId);

    /**
     * Returns the status of the content staging settings.
     *
     * @param loggedInUser the current user
     * @param orgId the organization id
     * @return whether content staging is enabled
     */
    @ApiEndpointDoc(
        summary = "Get the status of content staging settings for the given organization. Returns true if " +
            "enabled, false otherwise.",
        method = HttpMethod.get,
        responseClass = BooleanResponse.class,
        responseDescription = "Get the status of content staging settings",
        legacyDocResponse = @LegacyDocResponse(type = "boolean", name = "status")
    )
    boolean isContentStagingEnabled(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "orgId", in = ParameterIn.QUERY, required = true) Integer orgId);

    /**
     * Returns whether errata e-mail notifications are enabled for the organization.
     *
     * @param loggedInUser the current user
     * @param orgId the organization id
     * @return whether errata e-mail notifications are enabled
     */
    @ApiEndpointDoc(
        summary = "Returns whether errata e-mail notifications are enabled for the organization",
        method = HttpMethod.get,
        responseClass = BooleanResponse.class,
        responseDescription = "Returns the status of the errata e-mail notification setting for the organization",
        legacyDocResponse = @LegacyDocResponse(type = "boolean", name = "status")
    )
    boolean isErrataEmailNotifsForOrg(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "orgId", in = ParameterIn.QUERY, required = true) Integer orgId);

    /**
     * Returns whether the organization administrator can manage the organization configuration.
     *
     * @param loggedInUser the current user
     * @param orgId the organization id
     * @return whether the organization administrator can manage the configuration
     */
    @ApiEndpointDoc(
        summary = "Returns whether Organization Administrator is able to manage his organization configuration. " +
            "This may have a high impact on general #product() performance.",
        method = HttpMethod.get,
        responseClass = BooleanResponse.class,
        responseDescription = "Returns the status org admin management setting",
        legacyDocResponse = @LegacyDocResponse(type = "boolean", name = "status")
    )
    boolean isOrgConfigManagedByOrgAdmin(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "orgId", in = ParameterIn.QUERY, required = true) Integer orgId);

    /**
     * Returns the list of organizations.
     *
     * @param loggedInUser the current user
     * @return the organizations
     */
    @ApiEndpointDoc(
        summary = "Returns the list of organizations.",
        method = HttpMethod.get,
        responseClass = OrgListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "organization info")
    )
    List<OrgDto> listOrgs(User loggedInUser);

    /**
     * Returns the list of users in a given organization.
     *
     * @param loggedInUser the current user
     * @param orgId the organization id
     * @return the users of the organization
     */
    @ApiEndpointDoc(
        summary = "Returns the list of users in a given organization.",
        method = HttpMethod.get,
        responseClass = OrgUserListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "user")
    )
    List listUsers(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "orgId", in = ParameterIn.QUERY, required = true) Integer orgId);

    /**
     * Sets the content lifecycle management patch synchronization config option.
     *
     * @param loggedInUser the current user
     * @param orgId the organization id
     * @param value the config option value
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Sets the content lifecycle management patch synchronization config option.",
        requestClass = SetClmSyncPatchesRequest.class,
        isIntegerResponse = true
    )
    Integer setClmSyncPatchesConfig(User loggedInUser, Integer orgId, Boolean value);

    /**
     * Sets the status of content staging for the given organization.
     *
     * @param loggedInUser the current user
     * @param orgId the organization id
     * @param enable whether to enable content staging
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set the status of content staging for the given organization.",
        requestClass = OrgEnableRequest.class,
        isIntegerResponse = true
    )
    Integer setContentStaging(User loggedInUser, Integer orgId, Boolean enable);

    /**
     * Enables or disables errata e-mail notifications for the organization.
     *
     * @param loggedInUser the current user
     * @param orgId the organization id
     * @param enable whether to enable the notifications
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Dis/enables errata e-mail notifications for the organization",
        requestClass = OrgEnableRequest.class,
        isIntegerResponse = true
    )
    Integer setErrataEmailNotifsForOrg(User loggedInUser, Integer orgId, Boolean enable);

    /**
     * Sets whether the organization administrator can manage the organization configuration.
     *
     * @param loggedInUser the current user
     * @param orgId the organization id
     * @param enable whether the organization administrator can manage the configuration
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Sets whether Organization Administrator can manage his organization configuration. This may " +
            "have a high impact on general #product() performance.",
        requestClass = OrgEnableRequest.class,
        isIntegerResponse = true
    )
    Integer setOrgConfigManagedByOrgAdmin(User loggedInUser, Integer orgId, Boolean enable);

    /**
     * Sets the status of the SCAP detailed result file upload settings.
     *
     * @param loggedInUser the current user
     * @param orgId the organization id
     * @param newSettings the new settings
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set the status of SCAP detailed result file upload settings for the given organization.",
        requestClass = SetScapFileUploadRequest.class,
        isIntegerResponse = true
    )
    int setPolicyForScapFileUpload(User loggedInUser, Integer orgId, Map<String, Object> newSettings);

    /**
     * Sets the status of the SCAP result deletion settings.
     *
     * @param loggedInUser the current user
     * @param orgId the organization id
     * @param newSettings the new settings
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set the status of SCAP result deletion settins for the given organization.",
        requestClass = SetScapDeletionRequest.class,
        isIntegerResponse = true
    )
    int setPolicyForScapResultDeletion(User loggedInUser, Integer orgId, Map<String, Object> newSettings);

    /**
     * Transfers systems from one organization to another.
     *
     * @param loggedInUser the current user
     * @param toOrgId the id of the destination organization
     * @param sids the ids of the systems to transfer
     * @return the ids of the transferred systems
     */
    @ApiEndpointDoc(
        summary = "Transfer systems from one organization to another. If executed by a #product() administrator, " +
            "the systems will be transferred from their current organization to the organization specified by " +
            "the toOrgId. If executed by an organization administrator, the systems must exist in the same " +
            "organization as that administrator and the systems will be transferred to the organization " +
            "specified by the toOrgId. In any scenario, the origination and destination organizations must be " +
            "defined in a trust.",
        requestClass = TransferSystemsRequest.class,
        responseClass = ServerIdListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "serverIdTransferred")
    )
    Object[] transferSystems(User loggedInUser, Integer toOrgId, List<Integer> sids);

    /**
     * Updates the name of an organization.
     *
     * @param loggedInUser the current user
     * @param orgId the organization id
     * @param name the new organization name
     * @return the updated organization
     */
    @ApiEndpointDoc(
        summary = "Updates the name of an organization",
        requestClass = UpdateNameRequest.class,
        responseClass = OrgResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "organization info")
    )
    OrgDto updateName(User loggedInUser, Integer orgId, String name);

    @Schema(name = "ApiResponseBoolean")
    interface BooleanResponse extends ApiResponseWrapper<Boolean> { }

    @Schema(name = "ApiResponseOrg")
    interface OrgResponse extends ApiResponseWrapper<OrgDoc> { }

    @Schema(name = "ApiResponseOrgList")
    interface OrgListResponse extends ApiResponseWrapper<List<OrgDoc>> { }

    @Schema(name = "ApiResponseOrgUserList")
    interface OrgUserListResponse extends ApiResponseWrapper<List<OrgUserDoc>> { }

    @Schema(name = "ApiResponseScapUploadInfo")
    interface ScapUploadInfoResponse extends ApiResponseWrapper<ScapUploadInfoDoc> { }

    @Schema(name = "ApiResponseScapDeletionInfo")
    interface ScapDeletionInfoResponse extends ApiResponseWrapper<ScapDeletionInfoDoc> { }

    @Schema(name = "ApiResponseServerIdList")
    interface ServerIdListResponse extends ApiResponseWrapper<List<Integer>> { }

    @Schema(name = "OrgCreateRequest")
    @JsonPropertyOrder({"orgName", "adminLogin", "adminPassword", "prefix", "firstName", "lastName", "email",
        "usePamAuth"})
    interface CreateOrgRequest {

        /**
         * @return the organization name
         */
        @Schema(description = "Organization name. Must meet same\ncriteria as in the web UI.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getOrgName();

        /**
         * @return the new administrator login name
         */
        @Schema(description = "New administrator login name.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdminLogin();

        /**
         * @return the new administrator password
         */
        @Schema(description = "New administrator password.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdminPassword();

        /**
         * @return the new administrator's prefix
         */
        @Schema(description = "New administrator's prefix. Must\nmatch one of the values available in the web UI. " +
            "(i.e. Dr., Mr., Mrs., Sr., etc.)", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPrefix();

        /**
         * @return the new administrator's first name
         */
        @Schema(description = "New administrator's first name.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getFirstName();

        /**
         * @return the new administrator's last name
         */
        @Schema(description = "New administrator's first name.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLastName();

        /**
         * @return the new administrator's e-mail
         */
        @Schema(description = "New administrator's e-mail.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getEmail();

        /**
         * @return whether PAM authentication is used
         */
        @Schema(description = "True if PAM authentication\nshould be used for the new administrator account.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getUsePamAuth();
    }

    @Schema(name = "OrgCreateFirstRequest")
    @JsonPropertyOrder({"orgName", "adminLogin", "adminPassword", "firstName", "lastName", "email"})
    interface CreateFirstOrgRequest {

        /**
         * @return the organization name
         */
        @Schema(description = "Organization name. Must meet same\ncriteria as in the web UI.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getOrgName();

        /**
         * @return the new administrator login name
         */
        @Schema(description = "New administrator login name.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdminLogin();

        /**
         * @return the new administrator password
         */
        @Schema(description = "New administrator password.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdminPassword();

        /**
         * @return the new administrator's first name
         */
        @Schema(description = "New administrator's first name.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getFirstName();

        /**
         * @return the new administrator's last name
         */
        @Schema(description = "New administrator's first name.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLastName();

        /**
         * @return the new administrator's e-mail
         */
        @Schema(description = "New administrator's e-mail.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getEmail();
    }

    @Schema(name = "OrgIdRequest")
    interface OrgIdRequest {

        /**
         * @return the organization id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getOrgId();
    }

    @Schema(name = "OrgEnableRequest")
    @JsonPropertyOrder({"orgId", "enable"})
    interface OrgEnableRequest {

        /**
         * @return the organization id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getOrgId();

        /**
         * @return whether to enable the setting
         */
        @Schema(description = "Use true/false to enable/disable", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getEnable();
    }

    @Schema(name = "OrgSetClmSyncPatchesRequest")
    @JsonPropertyOrder({"orgId", "value"})
    interface SetClmSyncPatchesRequest {

        /**
         * @return the organization id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getOrgId();

        /**
         * @return the config option value
         */
        @Schema(description = "The config option value", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getValue();
    }

    @Schema(name = "OrgUpdateNameRequest")
    @JsonPropertyOrder({"orgId", "name"})
    interface UpdateNameRequest {

        /**
         * @return the organization id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getOrgId();

        /**
         * @return the new organization name
         */
        @Schema(description = "Organization name. Must meet same\ncriteria as in the web UI.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();
    }

    @Schema(name = "OrgTransferSystemsRequest")
    @JsonPropertyOrder({"toOrgId", "sids"})
    interface TransferSystemsRequest {

        /**
         * @return the id of the destination organization
         */
        @Schema(description = "ID of the organization where the\nsystem(s) will be transferred to.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getToOrgId();

        /**
         * @return the ids of the systems to transfer
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getSids();
    }

    @Schema(name = "OrgSetScapFileUploadRequest")
    @JsonPropertyOrder({"orgId", "newSettings"})
    interface SetScapFileUploadRequest {

        /**
         * @return the organization id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getOrgId();

        /**
         * @return the new settings
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        ScapFileUploadSettingsDoc getNewSettings();
    }

    @Schema(name = "OrgScapFileUploadSettings")
    interface ScapFileUploadSettingsDoc {

        /**
         * @return whether the aggregation of detailed SCAP results is enabled
         */
        @Schema(description = "Aggregation of detailed SCAP results is enabled.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getEnabled();
    }

    @Schema(name = "OrgSetScapDeletionRequest")
    @JsonPropertyOrder({"orgId", "newSettings"})
    interface SetScapDeletionRequest {

        /**
         * @return the organization id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getOrgId();

        /**
         * @return the new settings
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        ScapDeletionSettingsDoc getNewSettings();
    }

    @Schema(name = "OrgScapDeletionSettings")
    @JsonPropertyOrder({"enabled", "retentionPeriod"})
    interface ScapDeletionSettingsDoc {

        /**
         * @return whether the deletion of SCAP results is enabled
         */
        @Schema(description = "Deletion of SCAP results is enabled", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getEnabled();

        /**
         * @return the retention period
         */
        @Schema(name = "retention_period",
                description = "Period (in days) after which a scan can be deleted (if enabled).",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getRetentionPeriod();
    }

    @Schema(name = "OrgScapUploadInfo", description = "scap_upload_info")
    @JsonPropertyOrder({"enabled", "sizeLimit"})
    interface ScapUploadInfoDoc {

        /**
         * @return whether the aggregation of detailed SCAP results is enabled
         */
        @Schema(description = "Aggregation of detailed SCAP results is enabled.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getEnabled();

        /**
         * @return the size limit for a single SCAP file upload
         */
        @Schema(name = "size_limit", description = "Limit (in Bytes) for a single SCAP file upload.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSizeLimit();
    }

    @Schema(name = "OrgScapDeletionInfo", description = "scap_deletion_info")
    @JsonPropertyOrder({"enabled", "retentionPeriod"})
    interface ScapDeletionInfoDoc {

        /**
         * @return whether the deletion of SCAP results is enabled
         */
        @Schema(description = "Deletion of SCAP results is enabled", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getEnabled();

        /**
         * @return the retention period
         */
        @Schema(name = "retention_period",
                description = "Period (in days) after which a scan can be deleted (if enabled).",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getRetentionPeriod();
    }

    @Schema(name = "OrgInfo", description = "organization info")
    @JsonPropertyOrder({"id", "name", "activeUsers", "systems", "trusts", "systemGroups", "activationKeys",
        "kickstartProfiles", "configurationChannels", "stagingContentEnabled"})
    interface OrgDoc {

        /**
         * @return the organization id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the organization name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the number of active users
         */
        @Schema(name = "active_users", description = "number of active users in the organization",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Integer getActiveUsers();

        /**
         * @return the number of systems
         */
        @Schema(description = "number of systems in the organization",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Integer getSystems();

        /**
         * @return the number of trusted organizations
         */
        @Schema(description = "number of trusted organizations",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Integer getTrusts();

        /**
         * @return the number of system groups
         */
        @Schema(name = "system_groups",
                description = "number of system groups in the organization (optional)")
        Integer getSystemGroups();

        /**
         * @return the number of activation keys
         */
        @Schema(name = "activation_keys",
                description = "number of activation keys in the organization (optional)")
        Integer getActivationKeys();

        /**
         * @return the number of kickstart profiles
         */
        @Schema(name = "kickstart_profiles",
                description = "number of kickstart profiles in the organization (optional)")
        Integer getKickstartProfiles();

        /**
         * @return the number of configuration channels
         */
        @Schema(name = "configuration_channels",
                description = "number of configuration channels in the organization (optional)")
        Integer getConfigurationChannels();

        /**
         * @return whether staging content is enabled
         */
        @Schema(name = "staging_content_enabled",
                description = "is staging content enabled in organization (optional)")
        Boolean getStagingContentEnabled();
    }

    @Schema(name = "OrgUser", description = "user")
    @JsonPropertyOrder({"login", "loginUc", "name", "email", "isOrgAdmin"})
    interface OrgUserDoc {

        /**
         * @return the login
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLogin();

        /**
         * @return the uppercase login
         */
        @Schema(name = "login_uc", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLoginUc();

        /**
         * @return the user name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the e-mail
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getEmail();

        /**
         * @return whether the user is an organization administrator
         */
        @Schema(name = "is_org_admin", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getIsOrgAdmin();
    }
}
