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
package com.redhat.rhn.frontend.xmlrpc.user;

import com.redhat.rhn.domain.access.Namespace;
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
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link UserHandler}.
 */
@Tag(name = "user", description = "Provides methods to access and modify users.")
public interface UserHandlerApi {

    /**
     * Lists the users of the organization.
     *
     * @param loggedInUser the current user
     * @return the users of the organization
     */
    @ApiEndpointDoc(
        summary = "Returns a list of users in your organization.",
        method = HttpMethod.get,
        responseClass = UserListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "user")
    )
    List<User> listUsers(@Parameter(hidden = true) User loggedInUser);

    /**
     * Lists the roles of a user.
     *
     * @param loggedInUser the current user
     * @param login the login name of the user
     * @return the roles of the user
     */
    @ApiEndpointDoc(
        summary = "Returns a list of the user's roles.",
        method = HttpMethod.get,
        responseClass = StringListResponse.class,
        responseDescription = "the role label"
    )
    Set<String> listRoles(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "login", description = "user's login name",
            in = ParameterIn.QUERY, required = true) String login);

    /**
     * Lists the effective permissions of a user.
     *
     * @param loggedInUser the current user
     * @param login the login name of the user
     * @return the permissions of the user
     */
    @ApiEndpointDoc(
        summary = "Lists the effective RBAC permissions of a user.",
        method = HttpMethod.get,
        responseClass = NamespaceListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "namespace")
    )
    Set<Namespace> listPermissions(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "login", description = "user's login name",
            in = ParameterIn.QUERY, required = true) String login);

    /**
     * Lists the roles the user can assign to others.
     *
     * @param loggedInUser the current user
     * @return the assignable roles
     */
    @ApiEndpointDoc(
        summary = "Returns a list of user roles that this user can assign to others.",
        method = HttpMethod.get,
        responseClass = StringListResponse.class,
        responseDescription = "(role label)"
    )
    Set<String> listAssignableRoles(@Parameter(hidden = true) User loggedInUser);

    /**
     * Returns the details of a user.
     *
     * @param loggedInUser the current user
     * @param login the login name of the user
     * @return the details of the user
     */
    @ApiEndpointDoc(
        summary = "Returns the details about a given user.",
        method = HttpMethod.get,
        responseClass = UserDetailsResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "user details")
    )
    Map<String, Object> getDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "login", description = "User's login name.",
            in = ParameterIn.QUERY, required = true) String login);

    /**
     * Updates the details of a user.
     *
     * @param loggedInUser the current user
     * @param login the login name of the user
     * @param details the new details
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Updates the details of a user.",
        requestClass = SetDetailsRequest.class,
        isIntegerResponse = true
    )
    int setDetails(User loggedInUser, String login, Map<String, String> details);

    /**
     * Adds a role to a user.
     *
     * @param loggedInUser the current user
     * @param login the login name of the user
     * @param role the role label
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Adds a role to a user.",
        requestClass = AddRoleRequest.class,
        isIntegerResponse = true
    )
    int addRole(User loggedInUser, String login, String role);

    /**
     * Removes a role from a user.
     *
     * @param loggedInUser the current user
     * @param login the login name of the user
     * @param role the role label
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Remove a role from a user.",
        requestClass = RemoveRoleRequest.class,
        isIntegerResponse = true
    )
    int removeRole(User loggedInUser, String login, String role);

    /**
     * Creates a new user.
     *
     * @param loggedInUser the current user
     * @param login the login name of the new user
     * @param password the password of the new user
     * @param firstName the first name of the new user
     * @param lastName the last name of the new user
     * @param email the e-mail address of the new user
     * @param usePamAuth whether the user authenticates via PAM
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Create a new user.",
        requestClass = CreateUserRequest.class,
        isIntegerResponse = true
    )
    int create(User loggedInUser, String login, String password, String firstName, String lastName, String email,
        Integer usePamAuth);

    /**
     * Deletes a user.
     *
     * @param loggedInUser the current user
     * @param login the login name of the user
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete a user.",
        requestClass = DeleteUserRequest.class,
        isIntegerResponse = true
    )
    int delete(User loggedInUser, String login);

    /**
     * Disables a user.
     *
     * @param loggedInUser the current user
     * @param login the login name of the user
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Disable a user.",
        requestClass = DisableUserRequest.class,
        isIntegerResponse = true
    )
    int disable(User loggedInUser, String login);

    /**
     * Enables a user.
     *
     * @param loggedInUser the current user
     * @param login the login name of the user
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Enable a user.",
        requestClass = EnableUserRequest.class,
        isIntegerResponse = true
    )
    int enable(User loggedInUser, String login);

    /**
     * Toggles whether a user uses PAM authentication.
     *
     * @param loggedInUser the current user
     * @param login the login name of the user
     * @param val whether PAM authentication is used
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Toggles whether or not a user uses PAM authentication or basic #product() authentication.",
        requestClass = UsePamAuthenticationRequest.class,
        isIntegerResponse = true
    )
    int usePamAuthentication(User loggedInUser, String login, Integer val);

    /**
     * Adds a system group to the default system groups of a user.
     *
     * @param loggedInUser the current user
     * @param login the login name of the user
     * @param name the server group name
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Add system group to user's list of default system groups.",
        requestClass = DefaultSystemGroupRequest.class,
        isIntegerResponse = true
    )
    int addDefaultSystemGroup(User loggedInUser, String login, String name);

    /**
     * Adds system groups to the default system groups of a user.
     *
     * @param loggedInUser the current user
     * @param login the login name of the user
     * @param sgNames the server group names
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Add system groups to user's list of default system groups.",
        requestClass = DefaultSystemGroupsRequest.class,
        isIntegerResponse = true
    )
    int addDefaultSystemGroups(User loggedInUser, String login, List<String> sgNames);

    /**
     * Removes a system group from the default system groups of a user.
     *
     * @param loggedInUser the current user
     * @param login the login name of the user
     * @param sgName the server group name
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Remove a system group from user's list of default system groups.",
        requestClass = RemoveDefaultSystemGroupRequest.class,
        isIntegerResponse = true
    )
    int removeDefaultSystemGroup(User loggedInUser, String login, String sgName);

    /**
     * Removes system groups from the default system groups of a user.
     *
     * @param loggedInUser the current user
     * @param login the login name of the user
     * @param sgNames the server group names
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Remove system groups from a user's list of default system groups.",
        requestClass = DefaultSystemGroupsRequest.class,
        isIntegerResponse = true
    )
    int removeDefaultSystemGroups(User loggedInUser, String login, List<String> sgNames);

    /**
     * Returns the default system groups of a user.
     *
     * @param loggedInUser the current user
     * @param login the login name of the user
     * @return the default system groups of the user
     */
    @ApiEndpointDoc(
        summary = "Returns a user's list of default system groups.",
        method = HttpMethod.get,
        responseClass = SystemGroupListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system group")
    )
    Object[] listDefaultSystemGroups(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "login", description = "User's login name.",
            in = ParameterIn.QUERY, required = true) String login);

    /**
     * Returns the system groups a user can administer.
     *
     * @param loggedInUser the current user
     * @param login the login name of the user
     * @return the system groups the user can administer
     */
    @ApiEndpointDoc(
        summary = "Returns the system groups that a user can administer.",
        method = HttpMethod.get,
        responseClass = SystemGroupListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system group")
    )
    Object[] listAssignedSystemGroups(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "login", description = "User's login name.",
            in = ParameterIn.QUERY, required = true) String login);

    /**
     * Removes system groups from the assigned system groups of a user.
     *
     * @param loggedInUser the current user
     * @param login the login name of the user
     * @param sgNames the server group names
     * @param setDefault whether the groups are also removed from the default list
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Remove system groups from a user's list of assigned system groups.",
        requestClass = RemoveAssignedSystemGroupsRequest.class,
        isIntegerResponse = true
    )
    int removeAssignedSystemGroups(User loggedInUser, String login, List<String> sgNames, Boolean setDefault);

    /**
     * Removes a system group from the assigned system groups of a user.
     *
     * @param loggedInUser the current user
     * @param login the login name of the user
     * @param sgName the server group name
     * @param setDefault whether the group is also removed from the default list
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Remove system group from the user's list of assigned system groups.",
        requestClass = RemoveAssignedSystemGroupRequest.class,
        isIntegerResponse = true
    )
    int removeAssignedSystemGroup(User loggedInUser, String login, String sgName, Boolean setDefault);

    /**
     * Adds a system group to the assigned system groups of a user.
     *
     * @param loggedInUser the current user
     * @param login the login name of the user
     * @param sgName the server group name
     * @param setDefault whether the group is also added to the default list
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Add system group to user's list of assigned system groups.",
        requestClass = AddAssignedSystemGroupRequest.class,
        isIntegerResponse = true
    )
    int addAssignedSystemGroup(User loggedInUser, String login, String sgName, Boolean setDefault);

    /**
     * Adds system groups to the assigned system groups of a user.
     *
     * @param loggedInUser the current user
     * @param login the login name of the user
     * @param sgNames the server group names
     * @param setDefault whether the groups are also added to the default list
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Add system groups to user's list of assigned system groups.",
        requestClass = AddAssignedSystemGroupsRequest.class,
        isIntegerResponse = true
    )
    int addAssignedSystemGroups(User loggedInUser, String login, List<String> sgNames, Boolean setDefault);

    /**
     * Returns the current value of the createDefaultSystemGroup setting.
     *
     * @param loggedInUser the current user
     * @return the current value of the setting
     */
    @ApiEndpointDoc(
        summary = "Returns the current value of the CreateDefaultSystemGroup setting.",
        method = HttpMethod.get,
        isIntegerResponse = true
    )
    boolean getCreateDefaultSystemGroup(@Parameter(hidden = true) User loggedInUser);

    /**
     * Sets the value of the createDefaultSystemGroup setting.
     *
     * @param loggedInUser the current user
     * @param createDefaultSystemGroup the new value of the setting
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Sets the value of the createDefaultSystemGroup setting.",
        requestClass = SetCreateDefaultSystemGroupRequest.class,
        isIntegerResponse = true
    )
    int setCreateDefaultSystemGroup(User loggedInUser, Boolean createDefaultSystemGroup);

    /**
     * Sets whether the target user has read-only API access.
     *
     * @param loggedInUser the current user
     * @param login the login name of the user
     * @param readOnly whether the user has read-only access
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Sets whether the target user should have only read-only API access or standard full scale " +
            "access.",
        requestClass = SetReadOnlyRequest.class,
        isIntegerResponse = true
    )
    int setReadOnly(User loggedInUser, String login, Boolean readOnly);

    /**
     * Enables or disables errata mail notifications for a user.
     *
     * @param loggedInUser the current user
     * @param login the login name of the user
     * @param value whether errata notifications are enabled
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Enables/disables errata mail notifications for a specific user.",
        requestClass = SetErrataNotificationsRequest.class,
        isIntegerResponse = true
    )
    int setErrataNotifications(User loggedInUser, String login, Boolean value);

    @Schema(name = "ApiResponseUserList")
    interface UserListResponse extends ApiResponseWrapper<List<UserDoc>> { }

    @Schema(name = "ApiResponseUserRoleList")
    interface StringListResponse extends ApiResponseWrapper<List<String>> { }

    @Schema(name = "ApiResponseUserNamespaceList")
    interface NamespaceListResponse extends ApiResponseWrapper<List<UserNamespaceDoc>> { }

    @Schema(name = "ApiResponseUserDetails")
    interface UserDetailsResponse extends ApiResponseWrapper<UserDetailsDoc> { }

    @Schema(name = "ApiResponseSystemGroupList")
    interface SystemGroupListResponse extends ApiResponseWrapper<List<SystemGroupDoc>> { }

    @Schema(name = "UserLoginRequest")
    interface DeleteUserRequest {

        /**
         * @return the login name of the user
         */
        @Schema(description = "User login name to delete.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLogin();
    }

    @Schema(name = "UserDisableRequest")
    interface DisableUserRequest {

        /**
         * @return the login name of the user
         */
        @Schema(description = "User login name to disable.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLogin();
    }

    @Schema(name = "UserEnableRequest")
    interface EnableUserRequest {

        /**
         * @return the login name of the user
         */
        @Schema(description = "User login name to enable.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLogin();
    }

    @Schema(name = "UserSetDetailsRequest")
    @JsonPropertyOrder({"login", "details"})
    interface SetDetailsRequest {

        /**
         * @return the login name of the user
         */
        @Schema(description = "User's login name.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLogin();

        /**
         * @return the new details
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        UserSetDetailsDoc getDetails();
    }

    @Schema(name = "UserAddRoleRequest")
    @JsonPropertyOrder({"login", "role"})
    interface AddRoleRequest {

        /**
         * @return the login name of the user
         */
        @Schema(description = "user login name to update", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLogin();

        /**
         * @return the role label
         */
        @Schema(description = "the role label to add", requiredMode = Schema.RequiredMode.REQUIRED)
        String getRole();
    }

    @Schema(name = "UserRemoveRoleRequest")
    @JsonPropertyOrder({"login", "role"})
    interface RemoveRoleRequest {

        /**
         * @return the login name of the user
         */
        @Schema(description = "user login name to update", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLogin();

        /**
         * @return the role label
         */
        @Schema(description = "the role label to remove", requiredMode = Schema.RequiredMode.REQUIRED)
        String getRole();
    }

    @Schema(name = "UserCreateRequest")
    @JsonPropertyOrder({"login", "password", "firstName", "lastName", "email", "usePamAuth"})
    interface CreateUserRequest {

        /**
         * @return the login name of the new user
         */
        @Schema(description = "desired login name, will fail if already in use.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getLogin();

        /**
         * @return the password of the new user
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getPassword();

        /**
         * @return the first name of the new user
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getFirstName();

        /**
         * @return the last name of the new user
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLastName();

        /**
         * @return the e-mail address of the new user
         */
        @Schema(description = "User's e-mail address.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getEmail();

        /**
         * The longer handler overload adds the PAM flag, so it is documented as optional.
         *
         * @return whether the user authenticates via PAM
         */
        @Schema(description = "1 if you wish to use PAM authentication for this user, 0 otherwise.",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Integer getUsePamAuth();
    }

    @Schema(name = "UserUsePamAuthenticationRequest")
    @JsonPropertyOrder({"login", "val"})
    interface UsePamAuthenticationRequest {

        /**
         * @return the login name of the user
         */
        @Schema(description = "User's login name.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLogin();

        /**
         * @return whether PAM authentication is used
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"1 to enable PAM authentication", "0 to disable."})
        Integer getVal();
    }

    @Schema(name = "UserDefaultSystemGroupRequest")
    @JsonPropertyOrder({"login", "name"})
    interface DefaultSystemGroupRequest {

        /**
         * @return the login name of the user
         */
        @Schema(description = "User's login name.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLogin();

        /**
         * @return the server group name
         */
        @Schema(description = "server group name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();
    }

    @Schema(name = "UserDefaultSystemGroupsRequest")
    @JsonPropertyOrder({"login", "sgNames"})
    interface DefaultSystemGroupsRequest {

        /**
         * @return the login name of the user
         */
        @Schema(description = "User's login name.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLogin();

        /**
         * @return the server group names
         */
        @Schema(description = "server group names", requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getSgNames();
    }

    @Schema(name = "UserRemoveDefaultSystemGroupRequest")
    @JsonPropertyOrder({"login", "sgName"})
    interface RemoveDefaultSystemGroupRequest {

        /**
         * @return the login name of the user
         */
        @Schema(description = "User's login name.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLogin();

        /**
         * @return the server group name
         */
        @Schema(description = "server group name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getSgName();
    }

    @Schema(name = "UserRemoveAssignedSystemGroupsRequest")
    @JsonPropertyOrder({"login", "sgNames", "setDefault"})
    interface RemoveAssignedSystemGroupsRequest {

        /**
         * @return the login name of the user
         */
        @Schema(description = "User's login name.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLogin();

        /**
         * @return the server group names
         */
        @Schema(description = "server group names", requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getSgNames();

        /**
         * @return whether the groups are also removed from the default list
         */
        @Schema(description = "Should system groups also be removed from the user's list of default system groups.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getSetDefault();
    }

    @Schema(name = "UserRemoveAssignedSystemGroupRequest")
    @JsonPropertyOrder({"login", "sgName", "setDefault"})
    interface RemoveAssignedSystemGroupRequest {

        /**
         * @return the login name of the user
         */
        @Schema(description = "User's login name.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLogin();

        /**
         * @return the server group name
         */
        @Schema(description = "server group name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getSgName();

        /**
         * @return whether the group is also removed from the default list
         */
        @Schema(description = "Should system group also be removed from the user's list of default system groups.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getSetDefault();
    }

    @Schema(name = "UserAddAssignedSystemGroupRequest")
    @JsonPropertyOrder({"login", "sgName", "setDefault"})
    interface AddAssignedSystemGroupRequest {

        /**
         * @return the login name of the user
         */
        @Schema(description = "User's login name.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLogin();

        /**
         * @return the server group name
         */
        @Schema(description = "server group name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getSgName();

        /**
         * @return whether the group is also added to the default list
         */
        @Schema(description = "Should system group also be added to user's list of default system groups.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getSetDefault();
    }

    @Schema(name = "UserAddAssignedSystemGroupsRequest")
    @JsonPropertyOrder({"login", "sgNames", "setDefault"})
    interface AddAssignedSystemGroupsRequest {

        /**
         * @return the login name of the user
         */
        @Schema(description = "User's login name.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLogin();

        /**
         * @return the server group names
         */
        @Schema(description = "server group names", requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getSgNames();

        /**
         * @return whether the groups are also added to the default list
         */
        @Schema(description = "Should system groups also be added to user's list of default system groups.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getSetDefault();
    }

    @Schema(name = "UserSetCreateDefaultSystemGroupRequest")
    interface SetCreateDefaultSystemGroupRequest {

        /**
         * @return the new value of the setting
         */
        @Schema(description = "true if we should automatically create system groups, false otherwise.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getCreateDefaultSystemGroup();
    }

    @Schema(name = "UserSetReadOnlyRequest")
    @JsonPropertyOrder({"login", "readOnly"})
    interface SetReadOnlyRequest {

        /**
         * @return the login name of the user
         */
        @Schema(description = "User's login name.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLogin();

        /**
         * @return whether the user has read-only access
         */
        @Schema(description = "Sets whether the target user should have only read-only API access or standard " +
                    "full scale access.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getReadOnly();
    }

    @Schema(name = "UserSetErrataNotificationsRequest")
    @JsonPropertyOrder({"login", "value"})
    interface SetErrataNotificationsRequest {

        /**
         * @return the login name of the user
         */
        @Schema(description = "User's login name.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLogin();

        /**
         * @return whether errata notifications are enabled
         */
        @Schema(description = "True for enabling errata notifications, False for disabling",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getValue();
    }

    @Schema(name = "UserSetDetails")
    @JsonPropertyOrder({"firstNames", "firstName", "lastName", "email", "prefix", "password"})
    interface UserSetDetailsDoc {

        /**
         * @return the deprecated first names
         */
        @Schema(name = "first_names", description = "deprecated, use first_name",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getFirstNames();

        /**
         * @return the first name
         */
        @Schema(name = "first_name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getFirstName();

        /**
         * @return the last name
         */
        @Schema(name = "last_name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLastName();

        /**
         * @return the e-mail address
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getEmail();

        /**
         * @return the prefix
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getPrefix();

        /**
         * @return the password
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getPassword();
    }

    @Schema(name = "UserInfo")
    @JsonPropertyOrder({"id", "login", "loginUc", "enabled"})
    interface UserDoc {

        /**
         * @return the ID of the user
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Long getId();

        /**
         * @return the login name of the user
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLogin();

        /**
         * @return the upper case login name of the user
         */
        @Schema(name = "login_uc", description = "upper case version of the login",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getLoginUc();

        /**
         * @return whether the user is enabled
         */
        @Schema(description = "true if user is enabled, false if the user is disabled",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getEnabled();
    }

    @Schema(name = "UserNamespace", description = "namespace")
    @JsonPropertyOrder({"namespace", "accessMode", "description"})
    interface UserNamespaceDoc {

        /**
         * @return the namespace
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getNamespace();

        /**
         * @return the access mode
         */
        @Schema(name = "access_mode", requiredMode = Schema.RequiredMode.REQUIRED)
        String getAccessMode();

        /**
         * @return the description of the namespace
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();
    }

    @Schema(name = "UserDetails")
    @JsonPropertyOrder({"firstNames", "firstName", "lastName", "email", "orgId", "orgName", "prefix",
        "lastLoginDate", "createdDate", "enabled", "usePam", "readOnly", "errataNotification"})
    interface UserDetailsDoc {

        /**
         * @return the deprecated first names
         */
        @Schema(name = "first_names", description = "deprecated, use first_name",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getFirstNames();

        /**
         * @return the first name
         */
        @Schema(name = "first_name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getFirstName();

        /**
         * @return the last name
         */
        @Schema(name = "last_name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLastName();

        /**
         * @return the e-mail address
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getEmail();

        /**
         * @return the ID of the organization
         */
        @Schema(name = "org_id", requiredMode = Schema.RequiredMode.REQUIRED)
        Long getOrgId();

        /**
         * @return the name of the organization
         */
        @Schema(name = "org_name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getOrgName();

        /**
         * @return the prefix
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getPrefix();

        /**
         * @return the last login date
         */
        @Schema(name = "last_login_date", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLastLoginDate();

        /**
         * @return the creation date
         */
        @Schema(name = "created_date", requiredMode = Schema.RequiredMode.REQUIRED)
        String getCreatedDate();

        /**
         * @return whether the user is enabled
         */
        @Schema(description = "true if user is enabled, false if the user is disabled",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getEnabled();

        /**
         * @return whether the user uses PAM authentication
         */
        @Schema(name = "use_pam", description = "true if user is configured to use PAM authentication",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getUsePam();

        /**
         * @return whether the user is read-only
         */
        @Schema(name = "read_only", description = "true if user is readonly",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getReadOnly();

        /**
         * @return whether errata notifications are enabled
         */
        @Schema(name = "errata_notification",
                description = "true if errata e-mail notification is enabled for the user",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getErrataNotification();
    }

    @Schema(name = "UserSystemGroup")
    @JsonPropertyOrder({"id", "name", "description", "systemCount", "orgId"})
    interface SystemGroupDoc {

        /**
         * @return the ID of the system group
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Long getId();

        /**
         * @return the name of the system group
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the description of the system group
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();

        /**
         * @return the number of systems in the group
         */
        @Schema(name = "system_count", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSystemCount();

        /**
         * @return the ID of the organization
         */
        @Schema(name = "org_id", description = "Organization ID for this system group.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Long getOrgId();
    }
}
