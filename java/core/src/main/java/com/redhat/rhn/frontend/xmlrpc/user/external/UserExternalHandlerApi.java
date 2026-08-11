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
package com.redhat.rhn.frontend.xmlrpc.user.external;

import com.redhat.rhn.domain.org.usergroup.OrgUserExtGroup;
import com.redhat.rhn.domain.org.usergroup.UserExtGroup;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link UserExternalHandler}.
 */
@Tag(name = "user.external",
     description = "If you are using IPA integration to allow authentication of users from " +
                   "an external IPA server, these methods can be used to configure how those " +
                   "users are handled.")
public interface UserExternalHandlerApi {

    /**
     * Sets whether temporary roles should be kept between login sessions.
     *
     * @param loggedInUser the current user
     * @param keepRoles whether roles should be kept
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set whether we should keeps roles assigned to users because of their IPA groups even " +
                  "after they log in through a non-IPA method. Can only be called by a Uyuni Administrator.",
        requestClass = SetKeepTemporaryRolesRequest.class,
        isIntegerResponse = true
    )
    int setKeepTemporaryRoles(User loggedInUser, Boolean keepRoles);

    /**
     * Gets whether temporary roles are kept between login sessions.
     *
     * @param loggedInUser the current user
     * @return whether roles are kept
     */
    @ApiEndpointDoc(
        summary = "Get whether we should keeps roles assigned to users because of their IPA groups even " +
                  "after they log in through a non-IPA method. Can only be called by a Uyuni Administrator.",
        method = HttpMethod.get,
        responseClass = BooleanResponse.class,
        responseDescription = "True if we should keep roles after users log in through non-IPA method, " +
                              "false otherwise",
        legacyDocResponse = @LegacyDocResponse(name = "keep")
    )
    boolean getKeepTemporaryRoles(User loggedInUser);

    /**
     * Sets whether the IPA orgunit should determine the organization.
     *
     * @param loggedInUser the current user
     * @param useOrgUnit whether the orgunit should be used
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set whether we place users into the organization that corresponds to the \"orgunit\" set " +
                  "on the IPA server. The orgunit name must match exactly the Uyuni organization name. " +
                  "Can only be called by a Uyuni Administrator.",
        requestClass = SetUseOrgUnitRequest.class,
        isIntegerResponse = true
    )
    int setUseOrgUnit(User loggedInUser, Boolean useOrgUnit);

    /**
     * Gets whether the IPA orgunit determines the organization.
     *
     * @param loggedInUser the current user
     * @return whether the orgunit is used
     */
    @ApiEndpointDoc(
        summary = "Get whether we place users into the organization that corresponds to the \"orgunit\" set " +
                  "on the IPA server. The orgunit name must match exactly the Uyuni organization name. " +
                  "Can only be called by a Uyuni Administrator.",
        method = HttpMethod.get,
        responseClass = BooleanResponse.class,
        responseDescription = "True if we should use the IPA orgunit to determine which organization to " +
                              "create the user in, false otherwise",
        legacyDocResponse = @LegacyDocResponse(name = "use")
    )
    boolean getUseOrgUnit(User loggedInUser);

    /**
     * Sets the default organization for externally authenticated users.
     *
     * @param loggedInUser the current user
     * @param orgId the organization identifier
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set the default org that users should be added in if orgunit from IPA server isn't found " +
                  "or is disabled. Can only be called by a Uyuni Administrator.",
        requestClass = SetDefaultOrgRequest.class,
        isIntegerResponse = true
    )
    int setDefaultOrg(User loggedInUser, Integer orgId);

    /**
     * Gets the default organization for externally authenticated users.
     *
     * @param loggedInUser the current user
     * @return the default organization identifier
     */
    @ApiEndpointDoc(
        summary = "Get the default org that users should be added in if orgunit from IPA server isn't found " +
                  "or is disabled. Can only be called by a Uyuni Administrator.",
        method = HttpMethod.get,
        responseClass = OrgIdResponse.class,
        responseDescription = "ID of the default organization. 0 if there is no default",
        legacyDocResponse = @LegacyDocResponse(name = "id")
    )
    int getDefaultOrg(User loggedInUser);

    /**
     * Creates a mapping between an external group and a set of roles.
     *
     * @param loggedInUser the current user
     * @param name the name of the external group
     * @param roles the roles to assign
     * @return the newly created group
     */
    @ApiEndpointDoc(
        summary = "Externally authenticated users may be members of external groups. You can use these groups " +
                  "to assign additional roles to the users when they log in. " +
                  "Can only be called by a Uyuni Administrator.",
        requestClass = CreateExternalGroupToRoleMapRequest.class,
        responseClass = UserExtGroupResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "external group")
    )
    UserExtGroup createExternalGroupToRoleMap(User loggedInUser, String name, List<String> roles);

    /**
     * Gets the role mapping for an external group.
     *
     * @param loggedInUser the current user
     * @param name the name of the external group
     * @return the external group
     */
    @ApiEndpointDoc(
        summary = "Get a representation of the role mapping for an external group. " +
                  "Can only be called by a Uyuni Administrator.",
        method = HttpMethod.get,
        responseClass = UserExtGroupResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "external group")
    )
    UserExtGroup getExternalGroupToRoleMap(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "name", description = "Name of the external group.",
                   in = ParameterIn.QUERY, required = true) String name);

    /**
     * Replaces the roles of an external group.
     *
     * @param loggedInUser the current user
     * @param name the name of the external group
     * @param roles the roles to assign
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Update the roles for an external group. Replace previously set roles with the ones passed " +
                  "in here. Can only be called by a Uyuni Administrator.",
        requestClass = SetExternalGroupRolesRequest.class,
        isIntegerResponse = true
    )
    int setExternalGroupRoles(User loggedInUser, String name, List<String> roles);

    /**
     * Deletes the role mapping of an external group.
     *
     * @param loggedInUser the current user
     * @param name the name of the external group
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete the role map for an external group. Can only be called by a Uyuni Administrator.",
        requestClass = ExternalGroupNameRequest.class,
        isIntegerResponse = true
    )
    int deleteExternalGroupToRoleMap(User loggedInUser, String name);

    /**
     * Lists the role mappings of all known external groups.
     *
     * @param loggedInUser the current user
     * @return the external groups
     */
    @ApiEndpointDoc(
        summary = "List role mappings for all known external groups. " +
                  "Can only be called by a Uyuni Administrator.",
        method = HttpMethod.get,
        responseClass = UserExtGroupListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "external group")
    )
    List<UserExtGroup> listExternalGroupToRoleMaps(User loggedInUser);

    /**
     * Creates a mapping between an external group and a set of system groups.
     *
     * @param loggedInUser the current user
     * @param name the name of the external group
     * @param groupNames the server group names
     * @return the newly created group
     */
    @ApiEndpointDoc(
        summary = "Externally authenticated users may be members of external groups. You can use these groups " +
                  "to give access to server groups to the users when they log in. " +
                  "Can only be called by an org_admin.",
        requestClass = CreateExternalGroupToSystemGroupMapRequest.class,
        responseClass = OrgUserExtGroupResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "external group")
    )
    OrgUserExtGroup createExternalGroupToSystemGroupMap(User loggedInUser, String name, List<String> groupNames);

    /**
     * Gets the server group mapping for an external group.
     *
     * @param loggedInUser the current user
     * @param name the name of the external group
     * @return the external group
     */
    @ApiEndpointDoc(
        summary = "Get a representation of the server group mapping for an external group. " +
                  "Can only be called by an org_admin.",
        method = HttpMethod.get,
        responseClass = OrgUserExtGroupResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "external group")
    )
    OrgUserExtGroup getExternalGroupToSystemGroupMap(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "name", description = "Name of the external group.",
                   in = ParameterIn.QUERY, required = true) String name);

    /**
     * Replaces the server groups of an external group.
     *
     * @param loggedInUser the current user
     * @param name the name of the external group
     * @param groupNames the server group names
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Update the server groups for an external group. Replace previously set server groups with " +
                  "the ones passed in here. Can only be called by an org_admin.",
        requestClass = SetExternalGroupSystemGroupsRequest.class,
        isIntegerResponse = true
    )
    int setExternalGroupSystemGroups(User loggedInUser, String name, List<String> groupNames);

    /**
     * Deletes the server group mapping of an external group.
     *
     * @param loggedInUser the current user
     * @param name the name of the external group
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete the server group map for an external group. Can only be called by an org_admin.",
        requestClass = ExternalGroupNameRequest.class,
        isIntegerResponse = true
    )
    int deleteExternalGroupToSystemGroupMap(User loggedInUser, String name);

    /**
     * Lists the server group mappings of all known external groups.
     *
     * @param loggedInUser the current user
     * @return the external groups
     */
    @ApiEndpointDoc(
        summary = "List server group mappings for all known external groups. " +
                  "Can only be called by an org_admin.",
        method = HttpMethod.get,
        responseClass = OrgUserExtGroupListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "external group")
    )
    List<OrgUserExtGroup> listExternalGroupToSystemGroupMaps(User loggedInUser);

    @Schema(name = "ApiResponseBoolean")
    interface BooleanResponse extends ApiResponseWrapper<Boolean> { }

    @Schema(name = "ApiResponseDefaultOrgId")
    interface OrgIdResponse extends ApiResponseWrapper<Integer> { }

    @Schema(name = "ApiResponseUserExtGroup")
    interface UserExtGroupResponse extends ApiResponseWrapper<UserExtGroupDoc> { }

    @Schema(name = "ApiResponseUserExtGroupList")
    interface UserExtGroupListResponse extends ApiResponseWrapper<List<UserExtGroupDoc>> { }

    @Schema(name = "ApiResponseOrgUserExtGroup")
    interface OrgUserExtGroupResponse extends ApiResponseWrapper<OrgUserExtGroupDoc> { }

    @Schema(name = "ApiResponseOrgUserExtGroupList")
    interface OrgUserExtGroupListResponse extends ApiResponseWrapper<List<OrgUserExtGroupDoc>> { }

    @Schema(name = "SetKeepTemporaryRolesRequest")
    interface SetKeepTemporaryRolesRequest {

        /**
         * @return whether roles should be kept
         */
        @Schema(description = "True if we should keep roles after users log in through non-IPA method, " +
                              "false otherwise.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getKeepRoles();
    }

    @Schema(name = "SetUseOrgUnitRequest")
    interface SetUseOrgUnitRequest {

        /**
         * @return whether the IPA orgunit should be used
         */
        @Schema(description = "true if we should use the IPA orgunit to determine which organization to " +
                              "create the user in, false otherwise.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getUseOrgUnit();
    }

    @Schema(name = "SetDefaultOrgRequest")
    interface SetDefaultOrgRequest {

        /**
         * @return the organization identifier
         */
        @Schema(name = "orgId",
                description = "ID of the organization to set as the default org. 0 if there should not be " +
                              "a default organization.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getOrgId();
    }

    @Schema(name = "ExternalGroupNameRequest")
    interface ExternalGroupNameRequest {

        /**
         * @return the name of the external group
         */
        @Schema(description = "Name of the external group.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();
    }

    @Schema(name = "CreateExternalGroupToRoleMapRequest")
    @JsonPropertyOrder({"name", "roles"})
    interface CreateExternalGroupToRoleMapRequest {

        /**
         * @return the name of the external group
         */
        @Schema(description = "Name of the external group. Must be unique.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the roles to assign
         */
        @Schema(description = "role - Can be any of: satellite_admin, org_admin (implies all other roles " +
                              "except for satellite_admin), channel_admin, config_admin, system_group_admin, " +
                              "or activation_key_admin.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getRoles();
    }

    @Schema(name = "SetExternalGroupRolesRequest")
    @JsonPropertyOrder({"name", "roles"})
    interface SetExternalGroupRolesRequest {

        /**
         * @return the name of the external group
         */
        @Schema(description = "Name of the external group.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the roles to assign
         */
        @Schema(description = "role - Can be any of: satellite_admin, org_admin (implies all other roles " +
                              "except for satellite_admin), channel_admin, config_admin, system_group_admin, " +
                              "or activation_key_admin.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getRoles();
    }

    @Schema(name = "CreateExternalGroupToSystemGroupMapRequest")
    @JsonPropertyOrder({"name", "groupNames"})
    interface CreateExternalGroupToSystemGroupMapRequest {

        /**
         * @return the name of the external group
         */
        @Schema(description = "Name of the external group. Must be unique.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the server group names
         */
        @Schema(name = "groupNames", description = "the names of the server groups to grant access to.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getGroupNames();
    }

    @Schema(name = "SetExternalGroupSystemGroupsRequest")
    @JsonPropertyOrder({"name", "groupNames"})
    interface SetExternalGroupSystemGroupsRequest {

        /**
         * @return the name of the external group
         */
        @Schema(description = "Name of the external group.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the server group names
         */
        @Schema(name = "groupNames", description = "the names of the server groups to grant access to.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getGroupNames();
    }

    @Schema(name = "UserExtGroup")
    @JsonPropertyOrder({"name", "roles"})
    interface UserExtGroupDoc {

        /**
         * @return the name of the external group
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the roles assigned to the external group
         */
        @Schema(description = "role", requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getRoles();
    }

    @Schema(name = "OrgUserExtGroup")
    @JsonPropertyOrder({"name", "groups"})
    interface OrgUserExtGroupDoc {

        /**
         * @return the name of the external group
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the server groups assigned to the external group
         */
        @Schema(description = "roles", requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getGroups();
    }
}
