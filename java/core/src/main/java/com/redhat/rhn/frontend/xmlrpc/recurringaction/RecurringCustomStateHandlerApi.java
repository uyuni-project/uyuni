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
package com.redhat.rhn.frontend.xmlrpc.recurringaction;

import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link RecurringCustomStateHandler}.
 */
@Tag(name = "recurring.custom", description = "Provides methods to handle recurring custom states for minions, " +
        "system groups and organizations.")
public interface RecurringCustomStateHandlerApi {

    /**
     * Create a new recurring custom state action.
     *
     * @param loggedInUser the current user
     * @param actionProps map containing action properties
     * @return the ID of the newly created recurring action
     */
    @ApiEndpointDoc(
        summary = "Create a new recurring custom state action.",
        requestClass = CreateRequest.class,
        responseClass = ActionIdResponse.class,
        responseDescription = "the ID of the newly created recurring action",
        legacyDocResponse = @LegacyDocResponse(type = "int", name = "id")
    )
    int create(User loggedInUser, Map<String, Object> actionProps);

    /**
     * Update a recurring custom state action.
     *
     * @param loggedInUser the current user
     * @param actionProps map containing properties to update
     * @return the ID of the updated recurring action
     */
    @ApiEndpointDoc(
        summary = "Update a recurring custom state action.",
        requestClass = UpdateRequest.class,
        responseClass = ActionIdResponse.class,
        responseDescription = "the ID of the updated recurring action",
        legacyDocResponse = @LegacyDocResponse(type = "int", name = "id")
    )
    int update(User loggedInUser, Map<String, Object> actionProps);

    /**
     * Lists all the custom states available to the user.
     *
     * @param loggedInUser the current user
     * @return the list of custom state names available to the user
     */
    @ApiEndpointDoc(
        summary = "List all the custom states available to the user.",
        method = HttpMethod.get,
        responseClass = CustomStateListResponse.class,
        responseDescription = "the list of custom channels available to the user"
    )
    List<String> listAvailable(@Parameter(hidden = true) User loggedInUser);

    @Schema(name = "ApiResponseRecurringActionId")
    interface ActionIdResponse extends ApiResponseWrapper<Integer> { }

    @Schema(name = "ApiResponseRecurringCustomStateList")
    interface CustomStateListResponse extends ApiResponseWrapper<List<String>> { }

    @Schema(name = "CreateRecurringCustomStateRequest")
    interface CreateRequest {

        /**
         * @return the action properties
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        CreateActionPropsDoc getActionProps();
    }

    @Schema(name = "UpdateRecurringCustomStateRequest")
    interface UpdateRequest {

        /**
         * @return the action properties to update
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        UpdateActionPropsDoc getActionProps();
    }

    @Schema(name = "CreateRecurringCustomStateProps")
    @JsonPropertyOrder({"entityType", "entityId", "name", "cronExpr", "states", "test"})
    interface CreateActionPropsDoc {

        /**
         * @return the type of the target entity
         */
        @Schema(name = "entity_type", description = "the type of the target entity. One of the following:",
                allowableValues = {"minion", "group", "org"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getEntityType();

        /**
         * @return the ID of the target entity
         */
        @Schema(name = "entity_id", description = "the ID of the target entity",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getEntityId();

        /**
         * @return the name of the recurring action
         */
        @Schema(description = "the name of the recurring action", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the execution frequency of the action as a cron expression
         */
        @Schema(name = "cron_expr",
                description = "the execution frequency of the action as a cron expression",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getCronExpr();

        /**
         * @return the ordered list of custom state names to be executed
         */
        @Schema(description = "the ordered list of custom state names to be executed",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getStates();

        /**
         * @return whether the action should be executed in test mode
         */
        @Schema(description = "whether the action should be executed in test mode (optional)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getTest();
    }

    @Schema(name = "UpdateRecurringCustomStateProps")
    @JsonPropertyOrder({"id", "name", "cronExpr", "states", "test", "active"})
    interface UpdateActionPropsDoc {

        /**
         * @return the ID of the action to update
         */
        @Schema(description = "the ID of the action to update", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the name of the action
         */
        @Schema(description = "the name of the action (optional)", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the execution frequency of the action
         */
        @Schema(name = "cron_expr", description = "the execution frequency of the action (optional)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getCronExpr();

        /**
         * @return the ordered list of custom state names to be executed
         */
        @Schema(description = "the ordered list of custom state names to be executed (optional)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getStates();

        /**
         * @return whether the action should be executed in test mode
         */
        @Schema(description = "whether the action should be executed in test mode (optional)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getTest();

        /**
         * @return whether the action should be active
         */
        @Schema(description = "whether the action should be active (optional)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getActive();
    }
}
