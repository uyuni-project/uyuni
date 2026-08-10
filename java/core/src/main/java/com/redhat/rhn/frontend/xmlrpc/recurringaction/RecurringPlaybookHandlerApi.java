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

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * API contract for {@link RecurringPlaybookHandler}.
 */
@Tag(name = "recurring.playbook", description = "Provides methods to handle recurring ansible playbook execution " +
        "for minions, system groups and organizations.")
public interface RecurringPlaybookHandlerApi {

    /**
     * Create a new recurring playbook action.
     *
     * @param loggedInUser the current user
     * @param actionProps map containing action properties
     * @return the ID of the newly created recurring action
     */
    @ApiEndpointDoc(
        summary = "Create a new recurring Ansible playbook action.",
        requestClass = CreateRequest.class,
        responseClass = ActionIdResponse.class,
        responseDescription = "the ID of the newly created recurring action",
        legacyDocResponse = @LegacyDocResponse(type = "int", name = "id")
    )
    int create(User loggedInUser, Map<String, Object> actionProps);

    /**
     * Update a recurring Ansible playbook action.
     *
     * @param loggedInUser the current user
     * @param actionProps map containing properties to update
     * @return the ID of the updated recurring action
     */
    @ApiEndpointDoc(
        summary = "Update a recurring Ansbile playbook action.",
        requestClass = UpdateRequest.class,
        responseClass = ActionIdResponse.class,
        responseDescription = "the ID of the updated recurring action",
        legacyDocResponse = @LegacyDocResponse(type = "int", name = "id")
    )
    int update(User loggedInUser, Map<String, Object> actionProps);

    @Schema(name = "ApiResponseRecurringActionId")
    interface ActionIdResponse extends ApiResponseWrapper<Integer> { }

    @Schema(name = "CreateRecurringPlaybookRequest")
    interface CreateRequest {

        /**
         * @return the action properties
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        CreateActionPropsDoc getActionProps();
    }

    @Schema(name = "UpdateRecurringPlaybookRequest")
    interface UpdateRequest {

        /**
         * @return the action properties to update
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        UpdateActionPropsDoc getActionProps();
    }

    @Schema(name = "CreateRecurringPlaybookProps")
    @JsonPropertyOrder({"entityId", "name", "cronExpr", "extraVars", "flushCache", "inventoryPath",
        "playbookPath", "test"})
    interface CreateActionPropsDoc {

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
        @Schema(name = "cron_expr", description = "the execution frequency of the action as a cron expression",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getCronExpr();

        /**
         * @return extra variables to override existing vars or create new ones
         */
        @Schema(name = "extra_vars",
                description = "extra variables to override existing vars or create new ones (optional)",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getExtraVars();

        /**
         * @return whether the Ansible cache should be flushed
         */
        @Schema(name = "flush_cache", description = "whether the Ansible cache should be flushed (optional)",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Boolean getFlushCache();

        /**
         * @return the path to the configured Ansible inventory
         */
        @Schema(name = "inventory_path", description = "the path to the configured Ansible inventory",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getInventoryPath();

        /**
         * @return the path to the playbook to be executed
         */
        @Schema(name = "playbook_path", description = "the path to the playbook to be executed",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getPlaybookPath();

        /**
         * @return whether the action should be executed in test mode
         */
        @Schema(description = "whether the action should be executed in test mode (optional)",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Boolean getTest();
    }

    @Schema(name = "UpdateRecurringPlaybookProps")
    @JsonPropertyOrder({"id", "name", "cronExpr", "extraVars", "flushCache", "inventoryPath",
        "playbookPath", "test", "active"})
    interface UpdateActionPropsDoc {

        /**
         * @return the ID of the action to update
         */
        @Schema(description = "the ID of the action to update", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the name of the action
         */
        @Schema(description = "the name of the action (optional)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getName();

        /**
         * @return the execution frequency of the action
         */
        @Schema(name = "cron_expr", description = "the execution frequency of the action (optional)",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getCronExpr();

        /**
         * @return extra variables to override existing vars or create new ones
         */
        @Schema(name = "extra_vars",
                description = "extra variables to override existing vars or create new ones (optional)",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getExtraVars();

        /**
         * @return whether the Ansible cache should be flushed
         */
        @Schema(name = "flush_cache", description = "whether the Ansible cache should be flushed (optional)",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Boolean getFlushCache();

        /**
         * @return the path to the configured Ansible inventory
         */
        @Schema(name = "inventory_path", description = "the path to the configured Ansible inventory (optional",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getInventoryPath();

        /**
         * @return the path to the playbook to be executed
         */
        @Schema(name = "playbook_path", description = "the path to the playbook to be executed (optional)",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getPlaybookPath();

        /**
         * @return whether the action should be executed in test mode
         */
        @Schema(description = "whether the action should be executed in test mode (optional)",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Boolean getTest();

        /**
         * @return whether the action should be active
         */
        @Schema(description = "whether the action should be active (optional)",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Boolean getActive();
    }
}
