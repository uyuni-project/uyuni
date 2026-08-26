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
package com.redhat.rhn.frontend.xmlrpc.chain;

import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

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
 * API contract for {@link ActionChainHandler}.
 */
@Tag(name = "actionchain", description = "Provides the namespace for the Action Chain methods.")
public interface ActionChainHandlerApi {

    /**
     * Lists the currently available action chains.
     *
     * @param loggedInUser the current user
     * @return the available action chains
     */
    @ApiEndpointDoc(
        summary = "List currently available action chains.",
        method = HttpMethod.get,
        responseClass = ActionChainListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "chain")
    )
    List<Map<String, Object>> listChains(@Parameter(hidden = true) User loggedInUser);

    /**
     * Lists all actions in an action chain.
     *
     * @param loggedInUser the current user
     * @param chainLabel the label of the chain
     * @return the entries of the action chain
     */
    @ApiEndpointDoc(
        summary = "List all actions in the particular Action Chain.",
        method = HttpMethod.get,
        responseClass = ActionChainEntryListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "entry")
    )
    List<Map<String, Object>> listChainActions(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "chainLabel", in = ParameterIn.QUERY, required = true,
                description = "Label of the chain") String chainLabel);

    /**
     * Removes an action from an action chain.
     *
     * @param loggedInUser the current user
     * @param chainLabel the label of the chain
     * @param actionId the action id
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Remove an action from an Action Chain.",
        requestClass = RemoveActionRequest.class,
        isIntegerResponse = true
    )
    Integer removeAction(User loggedInUser, String chainLabel, Integer actionId);

    /**
     * Deletes an action chain by label.
     *
     * @param loggedInUser the current user
     * @param chainLabel the label of the chain
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete action chain by label.",
        requestClass = ChainLabelRequest.class,
        isIntegerResponse = true
    )
    Integer deleteChain(User loggedInUser, String chainLabel);

    /**
     * Creates an action chain.
     *
     * @param loggedInUser the current user
     * @param chainLabel the label of the chain
     * @return the id of the created action chain
     */
    @ApiEndpointDoc(
        summary = "Create an Action Chain.",
        requestClass = ChainLabelRequest.class,
        isIntegerResponse = true,
        responseDescription = "The ID of the created action chain",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Integer createChain(User loggedInUser, String chainLabel);

    /**
     * Adds a system reboot to an action chain.
     *
     * @param loggedInUser the current user
     * @param sid the system id
     * @param chainLabel the label of the chain
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Add system reboot to an Action Chain.",
        requestClass = SystemAndChainRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Integer addSystemReboot(User loggedInUser, Integer sid, String chainLabel);

    /**
     * Adds an errata update for one system to an action chain.
     *
     * @param loggedInUser the current user
     * @param sid the system id
     * @param errataIds the errata ids
     * @param chainLabel the label of the chain
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Adds Errata update to an Action Chain.",
        requestClass = ErrataUpdateBySystemRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Integer addErrataUpdate(User loggedInUser, Integer sid, List<Integer> errataIds, String chainLabel);

    /**
     * Adds an errata update for several systems to an action chain.
     *
     * @param loggedInUser the current user
     * @param sids the system ids
     * @param errataIds the errata ids
     * @param chainLabel the label of the chain
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Adds Errata update to an Action Chain.",
        requestClass = ErrataUpdateBySystemsRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Integer addErrataUpdate(User loggedInUser, List<Integer> sids, List<Integer> errataIds, String chainLabel);

    /**
     * Adds an errata update for several systems to an action chain.
     *
     * @param loggedInUser the current user
     * @param sids the system ids
     * @param errataIds the errata ids
     * @param chainLabel the label of the chain
     * @param onlyRelevant whether irrelevant errata are skipped instead of raising an error
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Adds Errata update to an Action Chain.",
        requestClass = ErrataUpdateRelevantRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action ID of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Integer addErrataUpdate(User loggedInUser, List<Integer> sids, List<Integer> errataIds, String chainLabel,
            Boolean onlyRelevant);

    /**
     * Adds an action removing installed packages to an action chain.
     *
     * @param loggedInUser the current user
     * @param sid the system id
     * @param packageIds the package ids
     * @param chainLabel the label of the chain
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Adds an action to remove installed packages on the system to an Action Chain.",
        requestClass = PackageActionRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action or exception",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Integer addPackageRemoval(User loggedInUser, Integer sid, List<Integer> packageIds, String chainLabel);

    /**
     * Adds a package installation action to an action chain.
     *
     * @param loggedInUser the current user
     * @param sid the system id
     * @param packageIds the package ids
     * @param chainLabel the label of the chain
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Adds package installation action to an Action Chain.",
        requestClass = PackageInstallRequest.class,
        isIntegerResponse = true
    )
    Integer addPackageInstall(User loggedInUser, Integer sid, List<Integer> packageIds, String chainLabel);

    /**
     * Adds an action verifying installed packages to an action chain.
     *
     * @param loggedInUser the current user
     * @param sid the system id
     * @param packageIds the package ids
     * @param chainLabel the label of the chain
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Adds an action to verify installed packages on the system to an Action Chain.",
        requestClass = PackageActionRequest.class,
        isIntegerResponse = true
    )
    Integer addPackageVerify(User loggedInUser, Integer sid, List<Integer> packageIds, String chainLabel);

    /**
     * Adds an action upgrading installed packages to an action chain.
     *
     * @param loggedInUser the current user
     * @param sid the system id
     * @param packageIds the package ids
     * @param chainLabel the label of the chain
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Adds an action to upgrade installed packages on the system to an Action Chain.",
        requestClass = PackageActionRequest.class,
        isIntegerResponse = true,
        responseDescription = "The id of the action or throw an exception",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    int addPackageUpgrade(User loggedInUser, Integer sid, List<Integer> packageIds, String chainLabel);

    /**
     * Adds a labelled action running a script to an action chain.
     *
     * @param loggedInUser the current user
     * @param sid the system id
     * @param chainLabel the label of the chain
     * @param scriptLabel the label of the script
     * @param uid the user id on the system
     * @param gid the group id on the system
     * @param timeout the timeout
     * @param scriptBody the base64 encoded script body
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Add an action with label to run a script to an Action Chain.",
        requestClass = LabelledScriptRunRequest.class,
        isIntegerResponse = true,
        responseDescription = "The id of the action or throw an exception",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Integer addScriptRun(User loggedInUser, Integer sid, String chainLabel, String scriptLabel, String uid,
            String gid, Integer timeout, String scriptBody);

    /**
     * Adds an action running a script to an action chain.
     *
     * @param loggedInUser the current user
     * @param sid the system id
     * @param chainLabel the label of the chain
     * @param uid the user id on the system
     * @param gid the group id on the system
     * @param timeout the timeout
     * @param scriptBody the base64 encoded script body
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Add an action to run a script to an Action Chain.",
        requestClass = ScriptRunRequest.class,
        isIntegerResponse = true,
        responseDescription = "The id of the action or throw an exception",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Integer addScriptRun(User loggedInUser, Integer sid, String chainLabel, String uid, String gid,
            Integer timeout, String scriptBody);

    /**
     * Adds an action applying the highstate to an action chain.
     *
     * @param loggedInUser the current user
     * @param sid the system id
     * @param chainLabel the label of the chain
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Adds an action to apply highstate on the system to an Action Chain.",
        requestClass = SystemAndChainRequest.class,
        isIntegerResponse = true,
        responseDescription = "The id of the action or throw an exception",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Integer addApplyHighstate(User loggedInUser, Integer sid, String chainLabel);

    /**
     * Schedules an action chain so that its actions occur.
     *
     * @param loggedInUser the current user
     * @param chainLabel the label of the chain
     * @param date the earliest date
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Schedule the Action Chain so that its actions will actually occur.",
        requestClass = ScheduleChainRequest.class,
        isIntegerResponse = true
    )
    Integer scheduleChain(User loggedInUser, String chainLabel, Date date);

    /**
     * Adds an action deploying a configuration file to an action chain.
     *
     * @param loggedInUser the current user
     * @param chainLabel the label of the chain
     * @param sid the system id
     * @param revisionSpecifiers the revisions to deploy
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Adds an action to deploy a configuration file to an Action Chain.",
        requestClass = ConfigurationDeploymentRequest.class,
        isIntegerResponse = true
    )
    Integer addConfigurationDeployment(User loggedInUser, String chainLabel, Integer sid,
            List<Map<String, Object>> revisionSpecifiers);

    /**
     * Renames an action chain.
     *
     * @param loggedInUser the current user
     * @param previousLabel the previous chain label
     * @param newLabel the new chain label
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Rename an Action Chain.",
        requestClass = RenameChainRequest.class,
        isIntegerResponse = true
    )
    Integer renameChain(User loggedInUser, String previousLabel, String newLabel);

    @Schema(name = "ApiResponseActionChainList")
    interface ActionChainListResponse extends ApiResponseWrapper<List<ActionChainDoc>> { }

    @Schema(name = "ApiResponseActionChainEntryList")
    interface ActionChainEntryListResponse extends ApiResponseWrapper<List<ActionChainEntryDoc>> { }

    @Schema(name = "ActionChainLabelRequest")
    interface ChainLabelRequest {

        /**
         * @return the label of the chain
         */
        @Schema(description = "Label of the chain", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChainLabel();
    }

    @Schema(name = "ActionChainRemoveActionRequest")
    @JsonPropertyOrder({"chainLabel", "actionId"})
    interface RemoveActionRequest {

        /**
         * @return the label of the chain
         */
        @Schema(description = "Label of the chain", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChainLabel();

        /**
         * @return the action id
         */
        @Schema(description = "Action ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getActionId();
    }

    @Schema(name = "ActionChainSystemRequest")
    @JsonPropertyOrder({"sid", "chainLabel"})
    interface SystemAndChainRequest {

        /**
         * @return the system id
         */
        @Schema(description = "System ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return the label of the chain
         */
        @Schema(description = "Label of the chain", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChainLabel();
    }

    @Schema(name = "ActionChainErrataUpdateBySystemRequest")
    @JsonPropertyOrder({"sid", "errataIds", "chainLabel"})
    interface ErrataUpdateBySystemRequest {

        /**
         * @return the system id
         */
        @Schema(description = "System ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return the errata ids
         */
        @Schema(description = "Errata ID", requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getErrataIds();

        /**
         * @return the label of the chain
         */
        @Schema(description = "Label of the chain", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChainLabel();
    }

    @Schema(name = "ActionChainErrataUpdateBySystemsRequest")
    @JsonPropertyOrder({"sids", "errataIds", "chainLabel"})
    interface ErrataUpdateBySystemsRequest {

        /**
         * @return the system ids
         */
        @Schema(description = "System IDs", requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getSids();

        /**
         * @return the errata ids
         */
        @Schema(description = "Errata ID", requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getErrataIds();

        /**
         * @return the label of the chain
         */
        @Schema(description = "Label of the chain", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChainLabel();
    }

    @Schema(name = "ActionChainErrataUpdateRelevantRequest")
    @JsonPropertyOrder({"sids", "errataIds", "chainLabel", "onlyRelevant"})
    interface ErrataUpdateRelevantRequest extends ErrataUpdateBySystemsRequest {

        /**
         * @return whether irrelevant errata are skipped
         */
        @Schema(description = "If true, InvalidErrataException is thrown if an errata " +
                "is not applicable to a system.", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getOnlyRelevant();
    }

    @Schema(name = "ActionChainPackageActionRequest")
    @JsonPropertyOrder({"sid", "packageIds", "chainLabel"})
    interface PackageActionRequest {

        /**
         * @return the system id
         */
        @Schema(description = "System ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return the package ids
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getPackageIds();

        /**
         * @return the label of the chain
         */
        @Schema(description = "Label of the chain", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChainLabel();
    }

    @Schema(name = "ActionChainPackageInstallRequest")
    @JsonPropertyOrder({"sid", "packageIds", "chainLabel"})
    interface PackageInstallRequest {

        /**
         * @return the system id
         */
        @Schema(description = "System ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return the package ids
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getPackageIds();

        /**
         * @return the label of the chain
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getChainLabel();
    }

    @Schema(name = "ActionChainLabelledScriptRunRequest")
    @JsonPropertyOrder({"sid", "chainLabel", "scriptLabel", "uid", "gid", "timeout", "scriptBody"})
    interface LabelledScriptRunRequest {

        /**
         * @return the system id
         */
        @Schema(description = "System ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return the label of the chain
         */
        @Schema(description = "Label of the chain", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChainLabel();

        /**
         * @return the label of the script
         */
        @Schema(description = "Label of the script", requiredMode = Schema.RequiredMode.REQUIRED)
        String getScriptLabel();

        /**
         * @return the user id on the system
         */
        @Schema(description = "User ID on the particular system", requiredMode = Schema.RequiredMode.REQUIRED)
        String getUid();

        /**
         * @return the group id on the system
         */
        @Schema(description = "Group ID on the particular system", requiredMode = Schema.RequiredMode.REQUIRED)
        String getGid();

        /**
         * @return the timeout
         */
        @Schema(description = "Timeout", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getTimeout();

        /**
         * @return the base64 encoded script body
         */
        @Schema(description = "Base64 encoded script body", requiredMode = Schema.RequiredMode.REQUIRED)
        String getScriptBody();
    }

    @Schema(name = "ActionChainScriptRunRequest")
    @JsonPropertyOrder({"sid", "chainLabel", "uid", "gid", "timeout", "scriptBody"})
    interface ScriptRunRequest {

        /**
         * @return the system id
         */
        @Schema(description = "System ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return the label of the chain
         */
        @Schema(description = "Label of the chain", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChainLabel();

        /**
         * @return the user id on the system
         */
        @Schema(description = "User ID on the particular system", requiredMode = Schema.RequiredMode.REQUIRED)
        String getUid();

        /**
         * @return the group id on the system
         */
        @Schema(description = "Group ID on the particular system", requiredMode = Schema.RequiredMode.REQUIRED)
        String getGid();

        /**
         * @return the timeout
         */
        @Schema(description = "Timeout", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getTimeout();

        /**
         * @return the base64 encoded script body
         */
        @Schema(description = "Base64 encoded script body", requiredMode = Schema.RequiredMode.REQUIRED)
        String getScriptBody();
    }

    @Schema(name = "ActionChainScheduleRequest")
    @JsonPropertyOrder({"chainLabel", "date"})
    interface ScheduleChainRequest {

        /**
         * @return the label of the chain
         */
        @Schema(description = "Label of the chain", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChainLabel();

        /**
         * @return the earliest date
         */
        @Schema(description = "Earliest date", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getDate();
    }

    @Schema(name = "ActionChainConfigurationDeploymentRequest")
    @JsonPropertyOrder({"chainLabel", "sid", "revisionSpecifiers"})
    interface ConfigurationDeploymentRequest {

        /**
         * @return the label of the chain
         */
        @Schema(description = "Label of the chain", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChainLabel();

        /**
         * @return the system id
         */
        @Schema(description = "System ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return the revisions to deploy
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "config revision specifier")
        List<ConfigRevisionSpecifierDoc> getRevisionSpecifiers();
    }

    @Schema(name = "ActionChainRenameRequest")
    @JsonPropertyOrder({"previousLabel", "newLabel"})
    interface RenameChainRequest {

        /**
         * @return the previous chain label
         */
        @Schema(description = "Previous chain label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPreviousLabel();

        /**
         * @return the new chain label
         */
        @Schema(description = "New chain label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getNewLabel();
    }

    @Schema(name = "ActionChainConfigRevisionSpecifier", description = "config revision specifier")
    @JsonPropertyOrder({"channelLabel", "filePath", "revision"})
    interface ConfigRevisionSpecifierDoc {

        /**
         * @return the channel label
         */
        @Schema(description = "Channel label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();

        /**
         * @return the path of the configuration file
         */
        @Schema(description = "Path of the configuration file", requiredMode = Schema.RequiredMode.REQUIRED)
        String getFilePath();

        /**
         * @return the revision number
         */
        @Schema(description = "Revision number", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getRevision();
    }

    @Schema(name = "ActionChainInfo", description = "chain")
    @JsonPropertyOrder({"label", "entrycount"})
    interface ActionChainDoc {

        /**
         * @return the label of the action chain
         */
        @Schema(description = "Label of an Action Chain", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the number of entries in the action chain
         */
        @Schema(description = "Number of entries in the Action Chain",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getEntrycount();
    }

    @Schema(name = "ActionChainEntryInfo", description = "entry")
    @JsonPropertyOrder({"id", "label", "created", "earliest", "type", "modified", "cuid"})
    interface ActionChainEntryDoc {

        /**
         * @return the action id
         */
        @Schema(description = "Action ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the label of the action
         */
        @Schema(description = "Label of an Action", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the created date/time
         */
        @Schema(description = "Created date/time", requiredMode = Schema.RequiredMode.REQUIRED)
        String getCreated();

        /**
         * @return the earliest scheduled date/time
         */
        @Schema(description = "Earliest scheduled date/time", requiredMode = Schema.RequiredMode.REQUIRED)
        String getEarliest();

        /**
         * @return the type of the action
         */
        @Schema(description = "Type of the action", requiredMode = Schema.RequiredMode.REQUIRED)
        String getType();

        /**
         * @return the modified date/time
         */
        @Schema(description = "Modified date/time", requiredMode = Schema.RequiredMode.REQUIRED)
        String getModified();

        /**
         * @return the creator uid
         */
        @Schema(description = "Creator UID", requiredMode = Schema.RequiredMode.REQUIRED)
        String getCuid();
    }
}
