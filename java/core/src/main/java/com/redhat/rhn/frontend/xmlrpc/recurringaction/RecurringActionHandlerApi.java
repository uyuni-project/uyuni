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

import com.redhat.rhn.domain.recurringactions.RecurringAction;
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
 * API contract for {@link RecurringActionHandler}.
 */
@Tag(name = "recurring", description = "Provides methods to handle recurring actions for minions, " +
        "system groups and organizations.")
public interface RecurringActionHandlerApi {

    /**
     * Lists the recurring actions of the given entity.
     *
     * @param loggedInUser the current user
     * @param type the type of the target entity
     * @param id the ID of the target entity
     * @return the recurring actions of the entity
     */
    @ApiEndpointDoc(
        summary = "Return a list of recurring actions for a given entity.",
        method = HttpMethod.get,
        responseClass = RecurringActionListResponse.class,
        legacyDocResponse = @LegacyDocResponse(
            name = "recurring action information (some fields may be absent for some action types)")
    )
    List<? extends RecurringAction> listByEntity(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "type", description = "the type of the target entity. One of the following:",
            in = ParameterIn.QUERY, required = true,
            schema = @Schema(allowableValues = {"minion", "group", "org"})) String type,
        @Parameter(name = "id", description = "the ID of the target entity",
            in = ParameterIn.QUERY, required = true) Integer id);

    /**
     * Looks up the recurring action with the given ID.
     *
     * @param loggedInUser the current user
     * @param id the action ID
     * @return the recurring action
     */
    @ApiEndpointDoc(
        summary = "Find a recurring action with the given action ID.",
        method = HttpMethod.get,
        responseClass = RecurringActionResponse.class,
        legacyDocResponse = @LegacyDocResponse(
            name = "recurring action information (some fields may be absent for some action types)")
    )
    RecurringAction lookupById(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "id", description = "the action ID",
            in = ParameterIn.QUERY, required = true) Integer id);

    /**
     * Deletes the recurring action with the given ID.
     *
     * @param loggedInUser the current user
     * @param id the action ID
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete a recurring action with the given action ID.",
        requestClass = DeleteRequest.class,
        isIntegerResponse = true
    )
    int delete(User loggedInUser, Integer id);

    @Schema(name = "ApiResponseRecurringActionList")
    interface RecurringActionListResponse extends ApiResponseWrapper<List<RecurringActionDoc>> { }

    @Schema(name = "ApiResponseRecurringAction")
    interface RecurringActionResponse extends ApiResponseWrapper<RecurringActionDoc> { }

    @Schema(name = "RecurringActionDeleteRequest")
    interface DeleteRequest {

        /**
         * @return the action ID
         */
        @Schema(description = "the action ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();
    }

    @Schema(name = "RecurringActionInfo")
    @JsonPropertyOrder({"id", "name", "entityId", "entityType", "cronExpr", "created", "creator", "test", "states",
        "extraVars", "flushCache", "inventoryPath", "playbookPath", "active"})
    interface RecurringActionDoc {

        /**
         * @return the ID of the action
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the name of the action
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the ID of the target entity
         */
        @Schema(name = "entity_id", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getEntityId();

        /**
         * @return the type of the target entity
         */
        @Schema(name = "entity_type", requiredMode = Schema.RequiredMode.REQUIRED)
        String getEntityType();

        /**
         * @return the execution frequency of the action as a cron expression
         */
        @Schema(name = "cron_expr", requiredMode = Schema.RequiredMode.REQUIRED)
        String getCronExpr();

        /**
         * @return the creation date of the action
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getCreated();

        /**
         * @return the login of the user who created the action
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getCreator();

        /**
         * @return whether the action is executed in test mode
         */
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Boolean getTest();

        /**
         * @return the ordered list of states executed by a custom state action
         */
        @Schema(description = "the ordered list of states to be executed by a custom state action",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        List<String> getStates();

        /**
         * @return the contents of the Ansible extra variables
         */
        @Schema(name = "extra_vars", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getExtraVars();

        /**
         * @return whether the Ansible cache is flushed
         */
        @Schema(name = "flush_cache", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Boolean getFlushCache();

        /**
         * @return the path to the configured Ansible inventory
         */
        @Schema(name = "inventory_path", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getInventoryPath();

        /**
         * @return the path to the playbook to be executed
         */
        @Schema(name = "playbook_path", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getPlaybookPath();

        /**
         * @return whether the action is active
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getActive();
    }
}
