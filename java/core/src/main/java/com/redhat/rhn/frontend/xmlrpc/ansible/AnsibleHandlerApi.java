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
package com.redhat.rhn.frontend.xmlrpc.ansible;

import com.redhat.rhn.domain.server.ansible.AnsiblePath;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;
import com.suse.manager.webui.utils.salt.custom.AnsiblePlaybookSlsResult;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link AnsibleHandler}.
 */
@Tag(name = "ansible", description = "Provides methods to manage Ansible systems")
public interface AnsibleHandlerApi {

    /**
     * Schedules a playbook execution.
     *
     * @param loggedInUser the current user
     * @param playbookPath the path to the playbook file
     * @param inventoryPath the path to the inventory file
     * @param controlNodeId the system ID of the control node
     * @param earliestOccurrence the earliest occurrence of the execution command
     * @param actionChainLabel the label of the action chain to use
     * @param testMode whether the playbook shall be executed in test mode
     * @param ansibleArgs the additional arguments to pass to ansiblegate
     * @return the execute playbook action id
     */
    @ApiEndpointDoc(
        summary = "Schedule a playbook execution",
        requestClass = SchedulePlaybookRequest.class,
        responseClass = ActionIdResponse.class,
        responseDescription = "ID of the playbook execution action created",
        legacyDocResponse = @LegacyDocResponse(type = "int", name = "id")
    )
    Long schedulePlaybook(User loggedInUser, String playbookPath, String inventoryPath, Integer controlNodeId,
                          Date earliestOccurrence, String actionChainLabel, boolean testMode,
                          Map<String, Object> ansibleArgs);

    /**
     * Schedules a playbook execution with additional arguments and no test mode.
     *
     * @param loggedInUser the current user
     * @param playbookPath the path to the playbook file
     * @param inventoryPath the path to the inventory file
     * @param controlNodeId the system ID of the control node
     * @param earliestOccurrence the earliest occurrence of the execution command
     * @param actionChainLabel the label of the action chain to use
     * @param ansibleArgs the additional arguments to pass to ansiblegate
     * @return the execute playbook action id
     */
    @ApiEndpointDoc(
        summary = "Schedule a playbook execution",
        requestClass = SchedulePlaybookWithArgsRequest.class,
        responseClass = ActionIdResponse.class,
        responseDescription = "ID of the playbook execution action created",
        legacyDocResponse = @LegacyDocResponse(type = "int", name = "id")
    )
    Long schedulePlaybook(User loggedInUser, String playbookPath, String inventoryPath, Integer controlNodeId,
                          Date earliestOccurrence, String actionChainLabel, Map<String, Object> ansibleArgs);

    /**
     * Schedules a playbook execution in test mode or not, without additional arguments.
     *
     * @param loggedInUser the current user
     * @param playbookPath the path to the playbook file
     * @param inventoryPath the path to the inventory file
     * @param controlNodeId the system ID of the control node
     * @param earliestOccurrence the earliest occurrence of the execution command
     * @param actionChainLabel the label of the action chain to use
     * @param testMode whether the playbook shall be executed in test mode
     * @return the execute playbook action id
     */
    @ApiEndpointDoc(
        summary = "Schedule a playbook execution",
        requestClass = SchedulePlaybookWithTestModeRequest.class,
        responseClass = ActionIdResponse.class,
        responseDescription = "ID of the playbook execution action created",
        legacyDocResponse = @LegacyDocResponse(type = "int", name = "id")
    )
    Long schedulePlaybook(User loggedInUser, String playbookPath, String inventoryPath, Integer controlNodeId,
                          Date earliestOccurrence, String actionChainLabel, boolean testMode);

    /**
     * Schedules a playbook execution without test mode and without additional arguments.
     *
     * @param loggedInUser the current user
     * @param playbookPath the path to the playbook file
     * @param inventoryPath the path to the inventory file
     * @param controlNodeId the system ID of the control node
     * @param earliestOccurrence the earliest occurrence of the execution command
     * @param actionChainLabel the label of the action chain to use
     * @return the execute playbook action id
     */
    @ApiEndpointDoc(
        summary = "Schedule a playbook execution",
        requestClass = SchedulePlaybookBaseRequest.class,
        responseClass = ActionIdResponse.class,
        responseDescription = "ID of the playbook execution action created",
        legacyDocResponse = @LegacyDocResponse(type = "int", name = "id")
    )
    Long schedulePlaybook(User loggedInUser, String playbookPath, String inventoryPath, Integer controlNodeId,
                          Date earliestOccurrence, String actionChainLabel);

    /**
     * Lists the ansible paths of a control node.
     *
     * @param loggedInUser the current user
     * @param controlNodeId the id of the control node server
     * @return the ansible paths
     */
    @ApiEndpointDoc(
        summary = "List ansible paths for server (control node)",
        method = HttpMethod.get,
        responseClass = AnsiblePathListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "ansible path")
    )
    List<AnsiblePath> listAnsiblePaths(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "controlNodeId", description = "id of ansible control node server",
            in = ParameterIn.QUERY, required = true) Integer controlNodeId);

    /**
     * Looks up an ansible path by its id.
     *
     * @param loggedInUser the current user
     * @param pathId the path id
     * @return the matching ansible path
     */
    @ApiEndpointDoc(
        summary = "Lookup ansible path by path id",
        method = HttpMethod.get,
        responseClass = AnsiblePathResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "ansible path")
    )
    AnsiblePath lookupAnsiblePathById(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "pathId", description = "path id",
            in = ParameterIn.QUERY, required = true) Integer pathId);

    /**
     * Creates an ansible path.
     *
     * @param loggedInUser the current user
     * @param props the path properties
     * @return the created ansible path
     */
    @ApiEndpointDoc(
        summary = "Create ansible path",
        requestClass = CreateAnsiblePathRequest.class,
        responseClass = AnsiblePathResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "ansible path")
    )
    AnsiblePath createAnsiblePath(User loggedInUser, Map<String, Object> props);

    /**
     * Updates an ansible path.
     *
     * @param loggedInUser the current user
     * @param pathId the path id
     * @param props the path properties
     * @return the updated ansible path
     */
    @ApiEndpointDoc(
        summary = "Create ansible path",
        requestClass = UpdateAnsiblePathRequest.class,
        responseClass = AnsiblePathResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "ansible path")
    )
    AnsiblePath updateAnsiblePath(User loggedInUser, Integer pathId, Map<String, Object> props);

    /**
     * Removes an ansible path.
     *
     * @param loggedInUser the current user
     * @param pathId the path id
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Create ansible path",
        requestClass = PathIdRequest.class,
        isIntegerResponse = true
    )
    int removeAnsiblePath(User loggedInUser, Integer pathId);

    /**
     * Fetches the playbook contents from the control node.
     *
     * @param loggedInUser the current user
     * @param pathId the playbook path id
     * @param playbookRelPath the relative path of the playbook
     * @return the playbook contents
     */
    @ApiEndpointDoc(
        summary = "Fetch the playbook content from the control node using a synchronous salt call.",
        requestClass = FetchPlaybookContentsRequest.class,
        responseClass = PlaybookContentsResponse.class,
        responseDescription = "Text contents of the playbook",
        legacyDocResponse = @LegacyDocResponse(type = "string", name = "contents")
    )
    String fetchPlaybookContents(User loggedInUser, Integer pathId, String playbookRelPath);

    /**
     * Discovers the playbooks under the given playbook path.
     *
     * @param loggedInUser the current user
     * @param pathId the path id
     * @return the playbooks under the given path
     */
    @ApiEndpointDoc(
        summary = "Discover playbooks under given playbook path with given pathId",
        requestClass = PathIdRequest.class,
        responseClass = DiscoveredPlaybooksResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "playbooks")
    )
    Map<String, Map<String, AnsiblePlaybookSlsResult>> discoverPlaybooks(User loggedInUser, Integer pathId);

    /**
     * Introspects the inventory under the given inventory path.
     *
     * @param loggedInUser the current user
     * @param pathId the path id
     * @return the inventory contents under the given path
     */
    @ApiEndpointDoc(
        summary = "Introspect inventory under given inventory path with given pathId and return it in a " +
            "structured way",
        requestClass = PathIdRequest.class,
        responseClass = InventoryResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "Inventory in a nested structure")
    )
    Map<String, Map<String, Object>> introspectInventory(User loggedInUser, Integer pathId);

    @Schema(name = "ApiResponseAnsibleActionId")
    interface ActionIdResponse extends ApiResponseWrapper<Integer> { }

    @Schema(name = "ApiResponseAnsiblePath")
    interface AnsiblePathResponse extends ApiResponseWrapper<AnsiblePathDoc> { }

    @Schema(name = "ApiResponseAnsiblePathList")
    interface AnsiblePathListResponse extends ApiResponseWrapper<List<AnsiblePathDoc>> { }

    @Schema(name = "ApiResponseAnsiblePlaybookContents")
    interface PlaybookContentsResponse extends ApiResponseWrapper<String> { }

    @Schema(name = "ApiResponseAnsiblePlaybooks")
    interface DiscoveredPlaybooksResponse extends ApiResponseWrapper<DiscoveredPlaybooksDoc> { }

    @Schema(name = "ApiResponseAnsibleInventory")
    interface InventoryResponse extends ApiResponseWrapper<InventoryDoc> { }

    @Schema(name = "AnsibleSchedulePlaybookRequest")
    @JsonPropertyOrder({"playbookPath", "inventoryPath", "controlNodeId", "earliestOccurrence", "actionChainLabel",
        "testMode", "ansibleArgs"})
    interface SchedulePlaybookRequest extends SchedulePlaybookBaseRequest {

        /**
         * @return whether the playbook shall be executed in test mode
         */
        @Schema(description = "'true' if the playbook shall be executed in test mode",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getTestMode();

        /**
         * @return the additional arguments to pass to ansiblegate
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        AnsibleArgsDoc getAnsibleArgs();
    }

    @Schema(name = "AnsibleSchedulePlaybookWithTestModeRequest")
    @JsonPropertyOrder({"playbookPath", "inventoryPath", "controlNodeId", "earliestOccurrence", "actionChainLabel",
        "testMode"})
    interface SchedulePlaybookWithTestModeRequest extends SchedulePlaybookBaseRequest {

        /**
         * @return whether the playbook shall be executed in test mode
         */
        @Schema(description = "'true' if the playbook shall be executed in test mode",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getTestMode();
    }

    @Schema(name = "AnsibleSchedulePlaybookWithArgsRequest")
    @JsonPropertyOrder({"playbookPath", "inventoryPath", "controlNodeId", "earliestOccurrence",
        "actionChainLabel", "ansibleArgs"})
    interface SchedulePlaybookWithArgsRequest extends SchedulePlaybookBaseRequest {

        /**
         * @return the additional arguments to pass to ansiblegate
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        AnsibleArgsDoc getAnsibleArgs();
    }

    @Schema(name = "AnsibleSchedulePlaybookBaseRequest")
    @JsonPropertyOrder({"playbookPath", "inventoryPath", "controlNodeId", "earliestOccurrence", "actionChainLabel"})
    interface SchedulePlaybookBaseRequest {

        /**
         * @return the path to the playbook file
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getPlaybookPath();

        /**
         * @return the path to the inventory file
         */
        @Schema(description = "path to Ansible inventory or empty", requiredMode = Schema.RequiredMode.REQUIRED)
        String getInventoryPath();

        /**
         * @return the system ID of the control node
         */
        @Schema(description = "system ID of the control node", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getControlNodeId();

        /**
         * @return the earliest occurrence of the execution command
         */
        @Schema(description = "earliest the execution command can be sent to the control node. ignored when " +
            "actionChainLabel is used", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();

        /**
         * @return the label of the action chain to use
         */
        @Schema(description = "label of an action chain to use, or None", requiredMode = Schema.RequiredMode.REQUIRED)
        String getActionChainLabel();
    }

    @Schema(name = "AnsibleArgs")
    @JsonPropertyOrder({"extraVars", "flushCache"})
    interface AnsibleArgsDoc {

        /**
         * @return the extra variables
         */
        String getExtraVars();

        /**
         * @return whether to clear the fact cache
         */
        Boolean getFlushCache();
    }

    @Schema(name = "AnsiblePathIdRequest")
    interface PathIdRequest {

        /**
         * @return the path id
         */
        @Schema(description = "path id", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getPathId();
    }

    @Schema(name = "AnsibleFetchPlaybookContentsRequest")
    @JsonPropertyOrder({"pathId", "playbookRelPath"})
    interface FetchPlaybookContentsRequest {

        /**
         * @return the playbook path id
         */
        @Schema(description = "playbook path id", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getPathId();

        /**
         * @return the relative path of the playbook
         */
        @Schema(description = "relative path of playbook (inside path specified by\npathId)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getPlaybookRelPath();
    }

    @Schema(name = "AnsibleCreatePathRequest")
    interface CreateAnsiblePathRequest {

        /**
         * @return the path properties
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        CreatePathPropsDoc getProps();
    }

    @Schema(name = "AnsibleCreatePathProps")
    @JsonPropertyOrder({"type", "serverId", "path"})
    interface CreatePathPropsDoc {

        /**
         * @return the ansible path type
         */
        @Schema(description = "The ansible path type: 'inventory' or 'playbook'",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getType();

        /**
         * @return the id of the control node server
         */
        @Schema(name = "server_id", description = "ID of control node server",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getServerId();

        /**
         * @return the local path to the inventory or playbook
         */
        @Schema(description = "The local path to inventory/playbook", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPath();
    }

    @Schema(name = "AnsibleUpdatePathRequest")
    @JsonPropertyOrder({"pathId", "props"})
    interface UpdateAnsiblePathRequest {

        /**
         * @return the path id
         */
        @Schema(description = "path id", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getPathId();

        /**
         * @return the path properties
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        UpdatePathPropsDoc getProps();
    }

    @Schema(name = "AnsibleUpdatePathProps")
    interface UpdatePathPropsDoc {

        /**
         * @return the local path to the inventory or playbook
         */
        @Schema(description = "The local path to inventory/playbook", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPath();
    }

    @Schema(name = "AnsiblePathInfo", description = "ansible path")
    @LegacyDocResponse(name = "ansible path")
    @JsonPropertyOrder({"id", "type", "serverId", "path"})
    interface AnsiblePathDoc {

        /**
         * @return the path id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "path id")
        Integer getId();

        /**
         * @return the type label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "type label")
        String getType();

        /**
         * @return the id of the ansible control node system
         */
        @Schema(name = "server_id", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "id of the ansible control node system")
        Integer getServerId();

        /**
         * @return the local path to the inventory or playbook
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "local path to inventory or playbook")
        String getPath();
    }

    @Schema(name = "AnsibleDiscoveredPlaybooks", description = "playbooks")
    interface DiscoveredPlaybooksDoc {

        /**
         * @return the discovered playbooks keyed by playbook name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "playbook")
        AnsiblePathDoc getPlaybook();
    }

    @Schema(name = "AnsibleInventory", description = "Inventory in a nested structure")
    interface InventoryDoc {

        /**
         * @return the inventory item
         */
        @Schema(description = "Inventory item (can be nested)", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "object", name = "Inventory item")
        Object getInventoryItem();
    }
}
