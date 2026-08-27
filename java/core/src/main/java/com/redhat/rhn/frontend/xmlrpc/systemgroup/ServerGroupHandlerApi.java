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
package com.redhat.rhn.frontend.xmlrpc.systemgroup;

import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.formula.Formula;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.frontend.xmlrpc.system.config.ServerConfigHandlerApi;
import com.redhat.rhn.frontend.xmlrpc.user.UserHandlerApi;

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
 * API contract for {@link ServerGroupHandler}.
 */
@Tag(name = "systemgroup", description = "Provides methods to access and modify system groups.")
public interface ServerGroupHandlerApi {

    /**
     * Lists the users who can administer a system group.
     *
     * @param loggedInUser the current user
     * @param systemGroupName the name of the system group
     * @return the administrators of the system group
     */
    @ApiEndpointDoc(
        summary = "Returns the list of users who can administer the given group. Caller must be " +
            "a system group admin or an organization administrator.",
        method = HttpMethod.get,
        responseClass = UserHandlerApi.UserListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "user")
    )
    List<User> listAdministrators(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "systemGroupName", in = ParameterIn.QUERY, required = true) String systemGroupName);

    /**
     * Adds or removes administrators of a system group.
     *
     * @param loggedInUser the current user
     * @param systemGroupName the name of the system group
     * @param loginNames the login names of the users
     * @param add whether the users are added
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Add or remove administrators to/from the given group. #product() and Organization " +
            "administrators are granted access to groups within their organization by default; " +
            "therefore, users with those roles should not be included in the array provided. " +
            "Caller must be an organization administrator.",
        requestClass = AddOrRemoveAdminsRequest.class,
        isIntegerResponse = true
    )
    int addOrRemoveAdmins(User loggedInUser, String systemGroupName, List<String> loginNames, boolean add);

    /**
     * Lists the systems of a system group.
     *
     * @param loggedInUser the current user
     * @param systemGroupName the name of the system group
     * @return the systems of the group
     */
    @ApiEndpointDoc(
        summary = "Return a list of systems associated with this system group. User must have " +
            "access to this system group.",
        method = HttpMethod.get,
        responseClass = ServerDetailsListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "server details")
    )
    List<Server> listSystems(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "systemGroupName", in = ParameterIn.QUERY, required = true) String systemGroupName);

    /**
     * Lists the systems of a system group with a reduced set of fields.
     *
     * @param loggedInUser the current user
     * @param systemGroupName the name of the system group
     * @return the systems of the group
     */
    @ApiEndpointDoc(
        summary = "Return a list of systems associated with this system group. User must have " +
            "access to this system group.",
        method = HttpMethod.get,
        responseClass = SystemOverviewListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    List<SystemOverview> listSystemsMinimal(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "systemGroupName", in = ParameterIn.QUERY, required = true) String systemGroupName);

    /**
     * Adds or removes systems of a system group.
     *
     * @param loggedInUser the current user
     * @param systemGroupName the name of the system group
     * @param serverIds the ids of the servers
     * @param add whether the servers are added
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Add/remove the given servers to a system group.",
        requestClass = AddOrRemoveSystemsRequest.class,
        isIntegerResponse = true
    )
    int addOrRemoveSystems(User loggedInUser, String systemGroupName, List<Integer> serverIds, Boolean add);

    /**
     * Creates a system group.
     *
     * @param loggedInUser the current user
     * @param name the name of the system group
     * @param description the description of the system group
     * @return the created system group
     */
    @ApiEndpointDoc(
        summary = "Create a new system group.",
        requestClass = CreateGroupRequest.class,
        responseClass = ServerGroupResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "server group")
    )
    ServerGroup create(User loggedInUser, String name, String description);

    /**
     * Deletes a system group.
     *
     * @param loggedInUser the current user
     * @param systemGroupName the name of the system group
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete a system group.",
        requestClass = SystemGroupNameRequest.class,
        isIntegerResponse = true
    )
    int delete(User loggedInUser, String systemGroupName);

    /**
     * Updates a system group.
     *
     * @param loggedInUser the current user
     * @param systemGroupName the name of the system group
     * @param description the description of the system group
     * @return the updated system group
     */
    @ApiEndpointDoc(
        summary = "Update an existing system group.",
        requestClass = UpdateGroupRequest.class,
        responseClass = ServerGroupResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "server group")
    )
    ServerGroup update(User loggedInUser, String systemGroupName, String description);

    /**
     * Lists the system groups that have no administrator.
     *
     * @param loggedInUser the current user
     * @return the system groups without an administrator
     */
    @ApiEndpointDoc(
        summary = "Returns a list of system groups that do not have an administrator. (who is not " +
            "an organization administrator, as they have implicit access to system groups) " +
            "Caller must be an organization administrator.",
        method = HttpMethod.get,
        responseClass = ServerGroupListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "server group")
    )
    List<ServerGroup> listGroupsWithNoAssociatedAdmins(@Parameter(hidden = true) User loggedInUser);

    /**
     * Lists the system groups accessible by the logged in user.
     *
     * @param loggedInUser the current user
     * @return the accessible system groups
     */
    @ApiEndpointDoc(
        summary = "Retrieve a list of system groups that are accessible by the logged in user.",
        method = HttpMethod.get,
        responseClass = ServerGroupListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "server group")
    )
    List<ManagedServerGroup> listAllGroups(@Parameter(hidden = true) User loggedInUser);

    /**
     * Returns the details of a system group by id.
     *
     * @param loggedInUser the current user
     * @param systemGroupId the id of the system group
     * @return the system group
     */
    @ApiEndpointDoc(
        summary = "Retrieve details of a ServerGroup based on it's id",
        method = HttpMethod.get,
        responseClass = ServerGroupResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "server group")
    )
    ServerGroup getDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "systemGroupId", in = ParameterIn.QUERY, required = true) Integer systemGroupId);

    /**
     * Returns the details of a system group by name.
     *
     * @param loggedInUser the current user
     * @param systemGroupName the name of the system group
     * @return the system group
     */
    @ApiEndpointDoc(
        summary = "Retrieve details of a ServerGroup based on it's name",
        method = HttpMethod.get,
        responseClass = ServerGroupResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "server group")
    )
    ServerGroup getDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "systemGroupName", in = ParameterIn.QUERY, required = true) String systemGroupName);

    /**
     * Lists the active systems of a system group.
     *
     * @param loggedInUser the current user
     * @param systemGroupName the name of the system group
     * @return the ids of the active systems
     */
    @ApiEndpointDoc(
        summary = "Lists active systems within a server group",
        method = HttpMethod.get,
        responseClass = ServerIdListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "server_id")
    )
    List<Long> listActiveSystemsInGroup(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "systemGroupName", in = ParameterIn.QUERY, required = true) String systemGroupName);

    /**
     * Lists the inactive systems of a system group using an inactivity time.
     *
     * @param loggedInUser the current user
     * @param systemGroupName the name of the system group
     * @param daysInactive the number of days a system must not check in
     * @return the ids of the inactive systems
     */
    @ApiEndpointDoc(
        summary = "Lists inactive systems within a server group using a specified inactivity time.",
        method = HttpMethod.get,
        responseClass = ServerIdListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "server_id")
    )
    List<Long> listInactiveSystemsInGroup(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "systemGroupName", in = ParameterIn.QUERY, required = true) String systemGroupName,
        @Parameter(name = "daysInactive", in = ParameterIn.QUERY, required = true,
                description = "Number of days a system must not check in to be considered inactive.")
        Integer daysInactive);

    /**
     * Lists the inactive systems of a system group using the default threshold.
     *
     * @param loggedInUser the current user
     * @param systemGroupName the name of the system group
     * @return the ids of the inactive systems
     */
    @ApiEndpointDoc(
        summary = "Lists inactive systems within a server group using the default 1 day threshold.",
        method = HttpMethod.get,
        responseClass = ServerIdListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "server_id")
    )
    List<Long> listInactiveSystemsInGroup(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "systemGroupName", in = ParameterIn.QUERY, required = true) String systemGroupName);

    /**
     * Schedules an action applying errata updates to the active systems of a group.
     *
     * @param loggedInUser the current user
     * @param systemGroupName the name of the system group
     * @param errataIds the errata ids
     * @return the ids of the scheduled actions
     */
    @ApiEndpointDoc(
        summary = "Schedules an action to apply errata updates to active systems from a group.",
        requestClass = ApplyErrataRequest.class,
        responseClass = ActionIdListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    List<Long> scheduleApplyErrataToActive(User loggedInUser, String systemGroupName, List<Integer> errataIds);

    /**
     * Schedules an action applying errata updates to the active systems of a group at a given time.
     *
     * @param loggedInUser the current user
     * @param systemGroupName the name of the system group
     * @param errataIds the errata ids
     * @param earliestOccurrence the earliest date
     * @return the ids of the scheduled actions
     */
    @ApiEndpointDoc(
        summary = "Schedules an action to apply errata updates to active systems from a group " +
            "at a given date/time.",
        requestClass = ApplyErrataAtDateRequest.class,
        responseClass = ActionIdListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    List<Long> scheduleApplyErrataToActive(User loggedInUser, String systemGroupName, List<Integer> errataIds,
            Date earliestOccurrence);

    /**
     * Schedules an action applying errata updates to the active systems of a group at a given time.
     *
     * @param loggedInUser the current user
     * @param systemGroupName the name of the system group
     * @param errataIds the errata ids
     * @param earliestOccurrence the earliest date
     * @param onlyRelevant whether only relevant errata are applied
     * @return the ids of the scheduled actions
     */
    @ApiEndpointDoc(
        summary = "Schedules an action to apply errata updates to active systems from a group " +
            "at a given date/time.",
        requestClass = ApplyErrataRelevantRequest.class,
        responseClass = ActionIdListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    List<Long> scheduleApplyErrataToActive(User loggedInUser, String systemGroupName, List<Integer> errataIds,
            Date earliestOccurrence, Boolean onlyRelevant);

    /**
     * Lists the configuration channels assigned to a system group.
     *
     * @param loggedInUser the current user
     * @param systemGroupName the name of the system group
     * @return the assigned configuration channels
     */
    @ApiEndpointDoc(
        summary = "List all Configuration Channels assigned to a system group",
        method = HttpMethod.get,
        responseClass = ServerConfigHandlerApi.ConfigChannelListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "configuration channel information")
    )
    List<ConfigChannel> listAssignedConfigChannels(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "systemGroupName", in = ParameterIn.QUERY, required = true) String systemGroupName);

    /**
     * Subscribes configuration channels to a system group.
     *
     * @param loggedInUser the current user
     * @param systemGroupName the name of the system group
     * @param configChannelLabels the labels of the configuration channels
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Subscribe given config channels to a system group",
        requestClass = ConfigChannelSubscriptionRequest.class,
        isIntegerResponse = true,
        responseDescription = "on success, exception on failure",
        legacyDocResponse = @LegacyDocResponse(type = "1", plainText = true)
    )
    int subscribeConfigChannel(User loggedInUser, String systemGroupName, List<String> configChannelLabels);

    /**
     * Unsubscribes configuration channels from a system group.
     *
     * @param loggedInUser the current user
     * @param systemGroupName the name of the system group
     * @param configChannelLabels the labels of the configuration channels
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Unsubscribe given config channels to a system group",
        requestClass = ConfigChannelSubscriptionRequest.class,
        isIntegerResponse = true,
        responseDescription = "on success, exception on failure",
        legacyDocResponse = @LegacyDocResponse(type = "1", plainText = true)
    )
    int unsubscribeConfigChannel(User loggedInUser, String systemGroupName, List<String> configChannelLabels);

    /**
     * Lists the formulas assigned to a system group.
     *
     * @param loggedInUser the current user
     * @param systemGroupName the name of the system group
     * @return the assigned formulas
     */
    @ApiEndpointDoc(
        summary = "List all Configuration Channels assigned to a system group",
        method = HttpMethod.get,
        responseClass = FormulaListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "formula")
    )
    List<Formula> listAssignedFormuals(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "systemGroupName", in = ParameterIn.QUERY, required = true) String systemGroupName);

    @Schema(name = "ApiResponseServerDetailsList")
    interface ServerDetailsListResponse extends ApiResponseWrapper<List<ServerDetailsDoc>> { }

    @Schema(name = "ApiResponseSystemOverviewList")
    interface SystemOverviewListResponse extends ApiResponseWrapper<List<SystemOverviewDoc>> { }

    @Schema(name = "ApiResponseServerGroup")
    interface ServerGroupResponse extends ApiResponseWrapper<ServerGroupDoc> { }

    @Schema(name = "ApiResponseServerGroupList")
    interface ServerGroupListResponse extends ApiResponseWrapper<List<ServerGroupDoc>> { }

    @Schema(name = "ApiResponseServerIdList")
    interface ServerIdListResponse extends ApiResponseWrapper<List<Integer>> { }

    @Schema(name = "ApiResponseActionIdList")
    interface ActionIdListResponse extends ApiResponseWrapper<List<Integer>> { }

    @Schema(name = "ApiResponseFormulaInfoList")
    interface FormulaListResponse extends ApiResponseWrapper<List<FormulaDoc>> { }

    @Schema(name = "SystemGroupNameRequest")
    interface SystemGroupNameRequest {

        /**
         * @return the name of the system group
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSystemGroupName();
    }

    @Schema(name = "SystemGroupAddOrRemoveAdminsRequest")
    @JsonPropertyOrder({"systemGroupName", "loginName", "add"})
    interface AddOrRemoveAdminsRequest {

        /**
         * @return the name of the system group
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSystemGroupName();

        /**
         * @return the login names of the users
         */
        @Schema(description = "User's loginName", requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getLoginName();

        /**
         * @return whether the users are added
         */
        @Schema(description = "1 to add administrators, 0 to remove.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "int")
        Boolean getAdd();
    }

    @Schema(name = "SystemGroupAddOrRemoveSystemsRequest")
    @JsonPropertyOrder({"systemGroupName", "serverIds", "add"})
    interface AddOrRemoveSystemsRequest {

        /**
         * @return the name of the system group
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSystemGroupName();

        /**
         * @return the ids of the servers
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getServerIds();

        /**
         * @return whether the servers are added
         */
        @Schema(description = "True to add to the group, False to remove.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getAdd();
    }

    @Schema(name = "SystemGroupCreateRequest")
    @JsonPropertyOrder({"name", "description"})
    interface CreateGroupRequest {

        /**
         * @return the name of the system group
         */
        @Schema(description = "Name of the system group.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the description of the system group
         */
        @Schema(description = "Description of the system group.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();
    }

    @Schema(name = "SystemGroupUpdateRequest")
    @JsonPropertyOrder({"systemGroupName", "description"})
    interface UpdateGroupRequest {

        /**
         * @return the name of the system group
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSystemGroupName();

        /**
         * @return the description of the system group
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();
    }

    @Schema(name = "SystemGroupApplyErrataRequest")
    @JsonPropertyOrder({"systemGroupName", "errataIds"})
    interface ApplyErrataRequest {

        /**
         * @return the name of the system group
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSystemGroupName();

        /**
         * @return the errata ids
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getErrataIds();
    }

    @Schema(name = "SystemGroupApplyErrataAtDateRequest")
    @JsonPropertyOrder({"systemGroupName", "errataIds", "earliestOccurrence"})
    interface ApplyErrataAtDateRequest extends ApplyErrataRequest {

        /**
         * @return the earliest date
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();
    }

    @Schema(name = "SystemGroupApplyErrataRelevantRequest")
    @JsonPropertyOrder({"systemGroupName", "errataIds", "earliestOccurrence", "onlyRelevant"})
    interface ApplyErrataRelevantRequest extends ApplyErrataAtDateRequest {

        /**
         * @return whether only relevant errata are applied
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getOnlyRelevant();
    }

    @Schema(name = "SystemGroupConfigChannelRequest")
    @JsonPropertyOrder({"systemGroupName", "configChannelLabels"})
    interface ConfigChannelSubscriptionRequest {

        /**
         * @return the name of the system group
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSystemGroupName();

        /**
         * @return the labels of the configuration channels
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getConfigChannelLabels();
    }

    @Schema(name = "ServerGroupInfo", description = "server group")
    @JsonPropertyOrder({"id", "name", "description", "orgId", "systemCount"})
    interface ServerGroupDoc {

        /**
         * @return the id of the system group
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

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
         * @return the id of the organization
         */
        @Schema(name = "org_id", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getOrgId();

        /**
         * @return the number of systems in the group
         */
        @Schema(name = "system_count", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSystemCount();
    }

    @Schema(name = "FormulaInfo", description = "formula")
    @JsonPropertyOrder({"name", "description", "formulaGroup"})
    interface FormulaDoc {

        /**
         * @return the name of the formula
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the description of the formula
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();

        /**
         * @return the group of the formula
         */
        @Schema(name = "formula_group", requiredMode = Schema.RequiredMode.REQUIRED)
        String getFormulaGroup();
    }

    @Schema(name = "SystemOverviewInfo", description = "system")
    @JsonPropertyOrder({"id", "name", "lastCheckin", "created", "lastBoot", "extraPkgCount", "outdatedPkgCount"})
    interface SystemOverviewDoc {

        /**
         * @return the system id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the system name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the last time the server successfully checked in
         */
        @Schema(name = "last_checkin", description = "last time server\n        successfully checked in",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getLastCheckin();

        /**
         * @return the server registration time
         */
        @Schema(description = "server registration time", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getCreated();

        /**
         * @return the last server boot time
         */
        @Schema(name = "last_boot", description = "last server boot time",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getLastBoot();

        /**
         * @return the number of packages not belonging to any assigned channel
         */
        @Schema(name = "extra_pkg_count", description = "number of packages not belonging\n" +
                "        to any assigned channel", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getExtraPkgCount();

        /**
         * @return the number of out-of-date packages
         */
        @Schema(name = "outdated_pkg_count", description = "number of out-of-date packages",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getOutdatedPkgCount();
    }

    @Schema(name = "ServerDetails", description = "server details")
    @JsonPropertyOrder({"id", "profileName", "machineId", "payg", "minionId", "baseEntitlement",
        "addonEntitlements", "autoUpdate", "release", "address1", "address2", "city", "state", "country",
        "building", "room", "rack", "description", "hostname", "lastBoot", "osaStatus", "lockStatus",
        "virtualization", "contactMethod"})
    interface ServerDetailsDoc {

        /**
         * @return the system id
         */
        @Schema(description = "system ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the profile name
         */
        @Schema(name = "profile_name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getProfileName();

        /**
         * @return the machine id
         */
        @Schema(name = "machine_id", requiredMode = Schema.RequiredMode.REQUIRED)
        String getMachineId();

        /**
         * @return whether the server instance is payg
         */
        @Schema(description = "Whether the server instance is payg or not",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getPayg();

        /**
         * @return the minion id
         */
        @Schema(name = "minion_id", requiredMode = Schema.RequiredMode.REQUIRED)
        String getMinionId();

        /**
         * @return the base entitlement label
         */
        @Schema(name = "base_entitlement", description = "system's base entitlement label",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getBaseEntitlement();

        /**
         * @return the addon entitlement labels
         */
        @Schema(name = "addon_entitlements",
                description = "system's addon entitlements labels,\n" +
                        "                  currently only 'virtualization_host'",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getAddonEntitlements();

        /**
         * @return whether auto errata updates are enabled
         */
        @Schema(name = "auto_update", description = "true if system has auto errata updates\n" +
                "                                     enabled", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getAutoUpdate();

        /**
         * @return the operating system release
         */
        @Schema(description = "the operating system release (i.e. 4AS,\n                 5Server)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getRelease();

        /**
         * @return the first address line
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getAddress1();

        /**
         * @return the second address line
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getAddress2();

        /**
         * @return the city
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getCity();

        /**
         * @return the state
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getState();

        /**
         * @return the country
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getCountry();

        /**
         * @return the building
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getBuilding();

        /**
         * @return the room
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getRoom();

        /**
         * @return the rack
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getRack();

        /**
         * @return the description
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();

        /**
         * @return the hostname
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getHostname();

        /**
         * @return the last boot time
         */
        @Schema(name = "last_boot", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getLastBoot();

        /**
         * @return the osa status
         */
        @Schema(name = "osa_status", description = "either 'unknown', 'offline', or 'online'",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getOsaStatus();

        /**
         * @return the lock status
         */
        @Schema(name = "lock_status", description = "True indicates that the system is locked.\n" +
                "     False indicates that the system is unlocked.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getLockStatus();

        /**
         * @return the virtualization type
         */
        @Schema(description = "virtualization type -\n     for virtual guests only (optional)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getVirtualization();

        /**
         * @return the contact method
         */
        @Schema(name = "contact_method", description = "one of the following:",
                allowableValues = {"default", "ssh-push", "ssh-push-tunnel"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getContactMethod();
    }
}
