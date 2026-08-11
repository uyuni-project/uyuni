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
package com.redhat.rhn.frontend.xmlrpc.schedule;

import com.redhat.rhn.domain.user.User;

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
 * API contract for {@link ScheduleHandler}.
 */
@Tag(name = "schedule", description = "Methods to retrieve information about scheduled actions.")
public interface ScheduleHandlerApi {

    /**
     * Cancels all actions in the given list.
     *
     * @param loggedInUser the current user
     * @param actionIds the action identifiers
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Cancel all actions in given list. If an invalid action is provided, none of the actions " +
                  "given will be canceled.",
        requestClass = ActionIdsRequest.class,
        isIntegerResponse = true
    )
    int cancelActions(User loggedInUser, List<Integer> actionIds);

    /**
     * Fails a specific event on a specified system.
     *
     * @param loggedInUser the current user
     * @param sid the system identifier
     * @param actionId the action identifier
     * @param message the optional failure message
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Fail specific event on specified system",
        requestClass = FailSystemActionRequest.class,
        isIntegerResponse = true
    )
    int failSystemAction(User loggedInUser, Integer sid, Integer actionId, String message);

    /**
     * Lists all actions.
     *
     * @param loggedInUser the current user
     * @return the actions
     */
    @ApiEndpointDoc(
        summary = "Returns a list of all actions.  This includes completed, in progress, failed and " +
                  "archived actions.",
        method = HttpMethod.get,
        responseClass = ActionListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "action")
    )
    Object[] listAllActions(User loggedInUser);

    /**
     * Lists the actions that have completed successfully.
     *
     * @param loggedInUser the current user
     * @return the actions
     */
    @ApiEndpointDoc(
        summary = "Returns a list of actions that have completed successfully.",
        method = HttpMethod.get,
        responseClass = ActionListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "action")
    )
    Object[] listCompletedActions(User loggedInUser);

    /**
     * Lists the actions that are in progress.
     *
     * @param loggedInUser the current user
     * @return the actions
     */
    @ApiEndpointDoc(
        summary = "Returns a list of actions that are in progress.",
        method = HttpMethod.get,
        responseClass = ActionListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "action")
    )
    Object[] listInProgressActions(User loggedInUser);

    /**
     * Lists the actions that have failed.
     *
     * @param loggedInUser the current user
     * @return the actions
     */
    @ApiEndpointDoc(
        summary = "Returns a list of actions that have failed.",
        method = HttpMethod.get,
        responseClass = ActionListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "action")
    )
    Object[] listFailedActions(User loggedInUser);

    /**
     * Lists the actions that have been archived.
     *
     * @param loggedInUser the current user
     * @return the actions
     */
    @ApiEndpointDoc(
        summary = "Returns a list of actions that have been archived.",
        method = HttpMethod.get,
        responseClass = ActionListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "action")
    )
    Object[] listArchivedActions(User loggedInUser);

    /**
     * Lists all the actions that have been archived.
     *
     * @param loggedInUser the current user
     * @return the actions
     */
    @ApiEndpointDoc(
        summary = "Returns a list of actions that have been archived.",
        method = HttpMethod.get,
        responseClass = ActionListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "action")
    )
    Object[] listAllArchivedActions(User loggedInUser);

    /**
     * Lists all the actions that have been completed.
     *
     * @param loggedInUser the current user
     * @return the actions
     */
    @ApiEndpointDoc(
        summary = "Returns a list of actions that have been completed.",
        method = HttpMethod.get,
        responseClass = ActionListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "action")
    )
    Object[] listAllCompletedActions(User loggedInUser);

    /**
     * Lists the systems that have completed a specific action.
     *
     * @param loggedInUser the current user
     * @param actionId the action identifier
     * @return the systems
     */
    @ApiEndpointDoc(
        summary = "Returns a list of systems that have completed a specific action.",
        method = HttpMethod.get,
        responseClass = ActionSystemListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    Object[] listCompletedSystems(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "actionId", in = ParameterIn.QUERY, required = true) Integer actionId);

    /**
     * Lists the systems that have a specific action in progress.
     *
     * @param loggedInUser the current user
     * @param actionId the action identifier
     * @return the systems
     */
    @ApiEndpointDoc(
        summary = "Returns a list of systems that have a specific action in progress.",
        method = HttpMethod.get,
        responseClass = ActionSystemListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    Object[] listInProgressSystems(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "actionId", in = ParameterIn.QUERY, required = true) Integer actionId);

    /**
     * Lists the systems that have failed a specific action.
     *
     * @param loggedInUser the current user
     * @param actionId the action identifier
     * @return the systems
     */
    @ApiEndpointDoc(
        summary = "Returns a list of systems that have failed a specific action.",
        method = HttpMethod.get,
        responseClass = ActionSystemListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    Object[] listFailedSystems(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "actionId", in = ParameterIn.QUERY, required = true) Integer actionId);

    /**
     * Reschedules all actions in the given list.
     *
     * @param loggedInUser the current user
     * @param actionIds the action identifiers
     * @param onlyFailed whether only failed actions should be rescheduled
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Reschedule all actions in the given list.",
        requestClass = RescheduleActionsRequest.class,
        isIntegerResponse = true
    )
    int rescheduleActions(User loggedInUser, List<Integer> actionIds, boolean onlyFailed);

    /**
     * Archives all actions in the given list.
     *
     * @param loggedInUser the current user
     * @param actionIds the action identifiers
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Archive all actions in the given list.",
        requestClass = ActionIdsRequest.class,
        isIntegerResponse = true
    )
    int archiveActions(User loggedInUser, List<Integer> actionIds);

    /**
     * Deletes all archived actions in the given list.
     *
     * @param loggedInUser the current user
     * @param actionIds the action identifiers
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete all archived actions in the given list.",
        requestClass = ActionIdsRequest.class,
        isIntegerResponse = true
    )
    int deleteActions(User loggedInUser, List<Integer> actionIds);

    @Schema(name = "ApiResponseScheduleActionList")
    interface ActionListResponse extends ApiResponseWrapper<List<ScheduleActionDoc>> { }

    @Schema(name = "ApiResponseScheduleActionSystemList")
    interface ActionSystemListResponse extends ApiResponseWrapper<List<ScheduleSystemDoc>> { }

    @Schema(name = "ScheduleActionIdsRequest")
    interface ActionIdsRequest {

        /**
         * @return the action identifiers
         */
        @Schema(name = "actionIds", requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getActionIds();
    }

    @Schema(name = "RescheduleActionsRequest")
    @JsonPropertyOrder({"actionIds", "onlyFailed"})
    interface RescheduleActionsRequest {

        /**
         * @return the action identifiers
         */
        @Schema(name = "actionIds", requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getActionIds();

        /**
         * @return whether only failed actions should be rescheduled
         */
        @Schema(name = "onlyFailed", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getOnlyFailed();
    }

    @Schema(name = "FailSystemActionRequest")
    @JsonPropertyOrder({"sid", "actionId", "message"})
    interface FailSystemActionRequest {

        /**
         * @return the system identifier
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return the action identifier
         */
        @Schema(name = "actionId", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getActionId();

        /**
         * @return the failure message
         */
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getMessage();
    }

    @Schema(name = "ScheduleAction")
    @JsonPropertyOrder({"id", "name", "type", "scheduler", "earliest", "prerequisite",
        "completedSystems", "failedSystems", "inProgressSystems"})
    interface ScheduleActionDoc {

        /**
         * @return the action identifier
         */
        @Schema(description = "action ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the action name
         */
        @Schema(description = "action name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the action type
         */
        @Schema(description = "action type", requiredMode = Schema.RequiredMode.REQUIRED)
        String getType();

        /**
         * @return the user that scheduled the action
         */
        @Schema(description = "the user that scheduled the action (optional)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getScheduler();

        /**
         * @return the earliest date and time the action will be performed
         */
        @Schema(description = "the earliest date and time the action will be performed",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Date getEarliest();

        /**
         * @return the identifier of the prerequisite action
         */
        @Schema(description = "ID of the prerequisite action (optional)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getPrerequisite();

        /**
         * @return the number of systems that completed the action
         */
        @Schema(name = "completedSystems", description = "number of systems that completed the action",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getCompletedSystems();

        /**
         * @return the number of systems that failed the action
         */
        @Schema(name = "failedSystems", description = "number of systems that failed the action",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getFailedSystems();

        /**
         * @return the number of systems that are in progress
         */
        @Schema(name = "inProgressSystems", description = "number of systems that are in progress",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getInProgressSystems();
    }

    @Schema(name = "ScheduleActionSystem")
    @JsonPropertyOrder({"serverId", "serverName", "baseChannel", "timestamp", "message"})
    interface ScheduleSystemDoc {

        /**
         * @return the server identifier
         */
        @Schema(name = "server_id", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getServerId();

        /**
         * @return the server name
         */
        @Schema(name = "server_name", description = "server name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getServerName();

        /**
         * @return the base channel used by the server
         */
        @Schema(name = "base_channel", description = "base channel used by the server",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getBaseChannel();

        /**
         * @return the time the action was completed
         */
        @Schema(description = "the time the action was completed", requiredMode = Schema.RequiredMode.REQUIRED)
        Date getTimestamp();

        /**
         * @return the optional message containing details
         */
        @Schema(description = "optional message containing details of the action",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getMessage();
    }
}
