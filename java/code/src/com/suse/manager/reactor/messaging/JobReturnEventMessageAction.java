/**
 * Copyright (c) 2016 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.reactor.messaging;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.suse.manager.reactor.hardware.CpuArchUtil;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.utils.SaltUtils.PackageChangeOutcome;
import com.suse.manager.webui.services.SaltActionChainGeneratorService;
import com.suse.manager.webui.utils.salt.custom.ScheduleMetadata;
import com.suse.manager.webui.utils.salt.custom.SystemInfo;
import com.suse.salt.netapi.event.JobReturnEvent;
import com.suse.salt.netapi.results.Ret;
import com.suse.salt.netapi.results.StateApplyResult;
import com.suse.utils.Json;
import com.suse.utils.Opt;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Handler class for {@link JobReturnEventMessage}.
 */
public class JobReturnEventMessageAction implements MessageAction {

    /**
     * Converts an event to json
     *
     * @param jobReturnEvent the return event
     */
    private static Optional<JsonElement> eventToJson(JobReturnEvent jobReturnEvent) {
        Optional<JsonElement> jsonResult = Optional.empty();
        try {
            jsonResult = Optional.ofNullable(
                jobReturnEvent.getData().getResult(JsonElement.class));
        }
        catch (JsonSyntaxException e) {
            LOG.error("JSON syntax error while decoding into a StateApplyResult:");
            LOG.error(jobReturnEvent.getData().getResult(JsonElement.class).toString());
        }
        return jsonResult;
    }

    /* Logger for this class */
    private static final Logger LOG = Logger.getLogger(JobReturnEventMessageAction.class);

    @Override
    public void execute(EventMessage msg) {
        JobReturnEventMessage jobReturnEventMessage = (JobReturnEventMessage) msg;
        JobReturnEvent jobReturnEvent = jobReturnEventMessage.getJobReturnEvent();

        // React according to the function the minion ran
        String function = jobReturnEvent.getData().getFun();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Job return event for minion: " +
                    jobReturnEvent.getMinionId() + "/" + jobReturnEvent.getJobId() +
                    " (" + function + ")");
        }

        // Prepare the job result as a json element
        Optional<JsonElement> jobResult = eventToJson(jobReturnEvent);

        // Check first if the received event was triggered by a single action execution
        Optional<Long> actionId = jobReturnEvent.getData().getMetadata(ScheduleMetadata.class).map(
                ScheduleMetadata::getSumaActionId);
        actionId.filter(id -> id > 0).ifPresent(id -> {
                jobResult.ifPresent(result ->
                    handleAction(id,
                            jobReturnEvent.getMinionId(),
                            jobReturnEvent.getData().getRetcode(),
                            jobReturnEvent.getData().isSuccess(),
                            jobReturnEvent.getJobId(),
                            jobResult.get(),
                            jobReturnEvent.getData().getFun()));
        });
        // Check if the event was triggered by an action chain execution
        Optional<Boolean> isActionChainResult = isActionChainResult(jobReturnEvent);
        boolean isActionChainInvolved = isActionChainResult.filter(isActionChain -> isActionChain).orElse(false);
        isActionChainResult.filter(isActionChain -> isActionChain).ifPresent(isActionChain -> {
            if (!jobResult.isPresent()) {
                return;
            }
            JsonElement jsonResult = jobResult.get();
            // The Salt reactor triggers a "suma-action-chain" job (mgractionchains.resume) at
            // 'minion/startup/event/'. This means the result might not be a JSON in case of
            // a Salt error when the 'mgractionchains' custom module is not yet deployed.
            if (jobReturnEvent.getData().getRetcode() == 1 && !jobReturnEvent.getData().isSuccess() &&
                    jobReturnEvent.getData().getResult().toString()
                            .contains("'mgractionchains.resume' is not available")) {
                return;
            }

            Map<String, StateApplyResult<Ret<JsonElement>>> actionChainResult;
            try {
                actionChainResult = Json.GSON.fromJson(jsonResult,
                        new TypeToken<Map<String, StateApplyResult<Ret<JsonElement>>>>() {
                        }.getType());
            }
            catch (JsonSyntaxException e) {
                LOG.error("Error mapping action chain result: " + jsonResult, e);
                throw e;
            }

            handleActionChainResult(jobReturnEvent.getMinionId(),
                    jobReturnEvent.getJobId(),
                    jobReturnEvent.getData().getRetcode(),
                    jobReturnEvent.getData().isSuccess(),
                    actionChainResult,
                    stateResult -> false);

            boolean packageRefreshNeeded = actionChainResult.entrySet().stream()
                    .map(entry -> SaltActionChainGeneratorService.parseActionChainStateId(entry.getKey())
                            .map(stateId -> handlePackageChanges(jobReturnEvent, entry.getValue().getName(),
                                    Optional.ofNullable(entry.getValue().getChanges().getRet())))
                            .orElse(false))
                    .collect(Collectors.toList())//This is needed to make sure we don't return early but execute
                    .stream()                    // handlePackageChange for all the results in actions chain result.
                    .anyMatch(s->Boolean.TRUE.equals(s));
            if (packageRefreshNeeded) {
                schedulePackageRefresh(jobReturnEvent.getMinionId());
            }
        });

        //For all jobs except when action chains are involved
        if (!isActionChainInvolved && handlePackageChanges(jobReturnEvent, function, jobResult)) {
            schedulePackageRefresh(jobReturnEvent.getMinionId());
        }

        // Check if event was triggered in response to state scheduled at minion start-up event
        if (isMinionStartup(jobReturnEvent)) {
            MinionServerFactory.findByMinionId(jobReturnEvent.getMinionId())
                    .ifPresent(minion -> jobResult
                            .ifPresent(result-> {
                                SystemInfo systemInfo = Json.GSON.fromJson(result, SystemInfo.class);
                                SaltUtils.INSTANCE.updateSystemInfo(systemInfo, minion);
                            }));
        }
      // For all jobs: update minion last checkin
        Optional<MinionServer> minion = MinionServerFactory.findByMinionId(
                jobReturnEvent.getMinionId());
        if (minion.isPresent()) {
            MinionServer m = minion.get();
            m.updateServerInfo();
            // for s390 update the host as well
            if (m.getCpu() != null &&
                CpuArchUtil.isS390(m.getCpu().getArch().getLabel())) {
                VirtualInstance virtInstance = m.getVirtualInstance();
                if (virtInstance != null && virtInstance.getHostSystem() != null) {
                    virtInstance.getHostSystem().updateServerInfo();
                }
            }
        }
    }

    /**
     * Handle action chain Salt result.
     *
     * @param minionId the minion id
     * @param jobId the job id
     * @param retCode the ret code
     * @param success whether result is successful or not
     * @param actionChainResult job result
     * @param skipFunction function to check if a result should be skipped from handling
     */
    public static void handleActionChainResult(
            String minionId, String jobId, int retCode, boolean success,
            Map<String, StateApplyResult<Ret<JsonElement>>> actionChainResult,
            Function<StateApplyResult<Ret<JsonElement>>, Boolean> skipFunction) {
        int chunk = 1;
        Long retActionChainId = null;
        boolean actionChainFailed = false;
        List<Long> failedActionIds = new ArrayList<>();
        for (Map.Entry<String, StateApplyResult<Ret<JsonElement>>> entry : actionChainResult.entrySet()) {
            String key = entry.getKey();
            StateApplyResult<Ret<JsonElement>> actionStateApply = entry.getValue();

            Optional<SaltActionChainGeneratorService.ActionChainStateId> stateId =
                    SaltActionChainGeneratorService.parseActionChainStateId(key);
            if (stateId.isPresent()) {
                retActionChainId = stateId.get().getActionChainId();
                chunk = stateId.get().getChunk();
                Long actionId = stateId.get().getActionId();
                if (skipFunction.apply(actionStateApply)) {
                    continue; // skip this state from handling
                }

                if (!actionStateApply.isResult()) {
                    actionChainFailed = true;
                    failedActionIds.add(actionId);
                    // don't stop handling the result entries if there's a failed action
                    // the result entries are not returned in order
                }
                handleAction(actionId,
                        minionId,
                        actionStateApply.isResult() ? 0 : -1,
                        actionStateApply.isResult(),
                        jobId,
                        actionStateApply.getChanges().getRet(),
                        actionStateApply.getName());
            }
            else if (!key.contains("schedule_next_chunk")) {
                LOG.warn("Could not find action id in action chain state key: " + key);
            }
        }

        if (retActionChainId != null) {
            if (actionChainFailed) {
                long firstFailedActionId = failedActionIds.stream().min(Long::compare).get();
                // Set rest of actions as FAILED due to failed prerequisite
                failDependentServerActions(firstFailedActionId, minionId, Optional.empty());
            }
            // Removing the generated SLS file
            SaltActionChainGeneratorService.INSTANCE.removeActionChainSLSFiles(
                    retActionChainId, minionId, chunk, actionChainFailed);
        }
    }

    /**
     * Set the given action to FAILED if not already in that state and also the dependent actions.
     * @param actionId the action id
     * @param minionId the minion id
     * @param message the result message to set in the server action
     */
    public static void failDependentServerActions(long actionId, String minionId, Optional<String> message) {
        Optional<MinionServer> minion = MinionServerFactory.findByMinionId(
               minionId);
        if (minion.isPresent()) {
            // set first action to failed if not already in that state
            Action action = ActionFactory.lookupById(actionId);
            Optional.ofNullable(action)
                    .ifPresent(firstAction ->
                            firstAction.getServerActions().stream()
                                    .filter(sa -> sa.getServerId().equals(minion.get().getId()))
                                    .filter(sa -> !ActionFactory.STATUS_FAILED.equals(sa.getStatus()))
                                    .filter(sa -> !ActionFactory.STATUS_COMPLETED.equals(sa.getStatus()))
                                    .findFirst()
                                    .ifPresent(sa -> sa.fail(message.orElse("Prerequisite failed"))));

            // walk dependent server actions recursively and set them to failed
            Stack<Long> actionIdsDependencies = new Stack<>();
            actionIdsDependencies.push(actionId);
            List<ServerAction> serverActions = ActionFactory
                    .listServerActionsForServer(minion.get(),
                            Arrays.asList(ActionFactory.STATUS_QUEUED, ActionFactory.STATUS_PICKED_UP,
                            ActionFactory.STATUS_FAILED), action.getCreated());

            while (!actionIdsDependencies.empty()) {
               Long acId = actionIdsDependencies.pop();
                List<ServerAction> serverActionsWithPrereq = serverActions.stream()
                   .filter(s -> s.getParentAction().getPrerequisite() != null)
                   .filter(s -> s.getParentAction().getPrerequisite().getId().equals(acId))
                   .collect(Collectors.toList());
               for (ServerAction sa : serverActionsWithPrereq) {
                   actionIdsDependencies.push(sa.getParentAction().getId());
                   sa.fail("Prerequisite failed");
               }
           }
       }
    }

    /**
     * This method does two things
     * 1) Update database with delta package info if enough information is available
     * 2) If there is not enough information then return true to indicate that full package refresh is needed
     * @param jobReturnEvent jobReturnEvent
     * @param function salt module name
     * @param jobResult result
     * @return return false If there is enough information to update database with new Package information(delta)
     *         return true If information is not enough and a full package refresh is needed
     */
    private boolean handlePackageChanges(JobReturnEvent jobReturnEvent, String function,
                                                Optional<JsonElement> jobResult) {

        return MinionServerFactory.findByMinionId(jobReturnEvent.getMinionId()).flatMap(minionServer ->
                jobResult.map(result -> {
                    boolean fullPackageRefreshNeeded = false;
                    try {
                        if (forcePackageListRefresh(jobReturnEvent) ||
                                SaltUtils.handlePackageChanges(function, result,
                                        minionServer) ==  PackageChangeOutcome.NEEDS_REFRESHING) {
                            fullPackageRefreshNeeded = true;
                        }
                    }
                     catch (JsonParseException e) {
                        LOG.warn("Could not determine if packages changed " +
                                "in call to " + function +
                                " because of a parse error");
                        LOG.warn(e);
                    }
                    return fullPackageRefreshNeeded;
                })
        ).orElse(false);
    }

    /**
     * Schedule package refresh on the minion
     * @param minionId ID of the minion for which package refresh should be scheduled
     */
    private void schedulePackageRefresh(String minionId) {
        MinionServerFactory.findByMinionId(minionId).ifPresent(minionServer -> {
            try {
                ActionManager.schedulePackageRefresh(minionServer.getOrg(), minionServer);
            }
            catch (TaskomaticApiException e) {
                LOG.error(e);
            }
        });
    }
    /**
     * Update the action properly based on the Job results from Salt.
     *
     * @param actionId the ID of the Action to handle
     * @param minionId the ID of the Minion who performed the action
     * @param retcode the retcode returned
     * @param success indicates if the job executed successfully
     * @param jobId the ID of the Salt job.
     * @param jsonResult the json results from the Salt job.
     * @param function the Salt function executed.
     */
    public static void handleAction(long actionId, String minionId, int retcode, boolean success,
                              String jobId, JsonElement jsonResult, String function) {
        // Lookup the corresponding action
        Optional<Action> action = Optional.ofNullable(ActionFactory.lookupById(actionId));
        if (action.isPresent()) {

            if (LOG.isDebugEnabled()) {
                LOG.debug("Matched salt job with action (id=" + actionId + ")");
            }

            Optional<MinionServer> minionServerOpt = MinionServerFactory.findByMinionId(minionId);
            minionServerOpt.ifPresent(minionServer -> {
                Optional<ServerAction> serverAction = action.get()
                        .getServerActions()
                        .stream()
                        .filter(sa -> sa.getServer().equals(minionServer)).findFirst();


                serverAction.ifPresent(sa -> {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Updating action for server: " + minionServer.getId());
                    }
                    try {
                        // Reboot has been scheduled so set reboot action to PICKED_UP.
                        // Wait until next "minion/start/event" to set it to COMPLETED.
                        if (action.get().getActionType().equals(ActionFactory.TYPE_REBOOT) &&
                                success && retcode == 0) {
                            sa.setStatus(ActionFactory.STATUS_PICKED_UP);
                            sa.setPickupTime(new Date());
                            return;
                        }
                        SaltUtils.INSTANCE.updateServerAction(sa,
                                retcode,
                                success,
                                jobId,
                                jsonResult,
                                function);
                        ActionFactory.save(sa);
                    }
                    catch (Exception e) {
                        LOG.error("Error processing Salt job return", e);
                        // DB exceptions cause the transaction to go into rollback-only
                        // state. We need to rollback this transaction first.
                        ActionFactory.rollbackTransaction();

                        sa.fail("An unexpected error has occurred. Please check the server logs.");

                        ActionFactory.save(sa);
                        // When we throw the exception again, the current transaction
                        // will be set to rollback-only, so we explicitly commit the
                        // transaction here
                        ActionFactory.commitTransaction();

                        // We don't actually want to catch any exceptions
                        throw e;
                    }
                });
            });
        }
        else {
            LOG.warn("Action referenced from Salt job was not found: " + actionId);
        }
    }

    private Optional<Boolean> isActionChainResult(JobReturnEvent event) {
        return event.getData().getMetadata(ScheduleMetadata.class).map(ScheduleMetadata::isActionChain);
    }

    /**
     * Find the action id corresponding to a given job return event in the job metadata.
     *
     * @param event the job return event
     * @return the corresponding action id or empty optional
     */
    private Optional<Long> getActionId(JobReturnEvent event) {
        return event.getData().getMetadata(ScheduleMetadata.class).map(
            ScheduleMetadata::getSumaActionId);
    }

    /**
     * Lookup the metadata to check if minion was restarted
     * @return
     */
    private boolean isMinionStartup(JobReturnEvent event) {
        return event.getData().getMetadata(ScheduleMetadata.class)
                .map(ScheduleMetadata::isMinionStartup)
                .orElse(false);
    }

    /**
     * Lookup the metadata to see if a package list refresh was requested.
     *
     * @param event the job return event
     * @return true if a package list refresh was requested, otherwise false
     */
    private boolean forcePackageListRefresh(JobReturnEvent event) {
        return event.getData().getMetadata(ScheduleMetadata.class)
                .map(ScheduleMetadata::isForcePackageListRefresh)
                .orElse(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRunConcurrently() {
        return true;
    }
}
