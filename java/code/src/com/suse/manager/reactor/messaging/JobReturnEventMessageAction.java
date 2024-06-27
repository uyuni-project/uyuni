/*
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
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.reactor.hardware.CpuArchUtil;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.utils.SaltUtils.PackageChangeOutcome;
import com.suse.manager.webui.services.SaltActionChainGeneratorService;
import com.suse.manager.webui.services.SaltServerActionService;
import com.suse.manager.webui.utils.salt.custom.ScheduleMetadata;
import com.suse.manager.webui.utils.salt.custom.SystemInfo;
import com.suse.salt.netapi.event.JobReturnEvent;
import com.suse.salt.netapi.results.Ret;
import com.suse.salt.netapi.results.StateApplyResult;
import com.suse.salt.netapi.utils.Xor;
import com.suse.utils.Json;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


/**
 * Handler class for {@link JobReturnEventMessage}.
 */
public class JobReturnEventMessageAction implements MessageAction {

    private final SaltServerActionService saltServerActionService;
    private final SaltUtils saltUtils;

    /**
     * @param saltServerActionServiceIn
     * @param saltUtilsIn
     */
    public JobReturnEventMessageAction(SaltServerActionService saltServerActionServiceIn, SaltUtils saltUtilsIn) {
        this.saltServerActionService = saltServerActionServiceIn;
        this.saltUtils = saltUtilsIn;
    }

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
            LOG.error("JSON syntax error while decoding into a StateApplyResult:", e);
            LOG.error(jobReturnEvent.getData().getResult(JsonElement.class).toString());
        }
        return jsonResult;
    }

    /* Logger for this class */
    private static final Logger LOG = LogManager.getLogger(JobReturnEventMessageAction.class);

    @Override
    public void execute(EventMessage msg) {
        JobReturnEventMessage jobReturnEventMessage = (JobReturnEventMessage) msg;
        JobReturnEvent jobReturnEvent = jobReturnEventMessage.getJobReturnEvent();

        // React according to the function the minion ran
        String function = jobReturnEvent.getData().getFun();

        List<Map<String, Object>> functionArgs = new LinkedList<>();
        if (jobReturnEvent.getData().getFunArgs() instanceof List) {
            List<Object> funArgs = (List<Object>) jobReturnEvent.getData().getFunArgs();
            if (!funArgs.isEmpty() && funArgs.get(0) instanceof Map) {
                functionArgs = funArgs.stream().filter(x -> x instanceof Map)
                        .map(x -> (Map<String, Object>) x).collect(Collectors.toList());
            }
        }
        boolean isFunctionTestMode = functionArgs.stream()
                .anyMatch(e -> e.containsKey("test") && Boolean.parseBoolean(e.get("test").toString()));

        if (Objects.isNull(function) && LOG.isDebugEnabled()) {
            LOG.debug("Function is null in JobReturnEvent -> \n{}", Json.GSON.toJson(jobReturnEvent));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Job return event for minion: {}/{} ({})", jobReturnEvent.getMinionId(),
                    jobReturnEvent.getJobId(), function);
        }

        // Prepare the job result as a json element
        Optional<JsonElement> jobResult = eventToJson(jobReturnEvent);

        // Check first if the received event was triggered by a single action execution
        Optional<Long> actionId = jobReturnEvent.getData().getMetadata(ScheduleMetadata.class).map(
                ScheduleMetadata::getSumaActionId);
        actionId.filter(id -> id > 0).ifPresent(id -> jobResult.ifPresent(result ->
            saltServerActionService.handleAction(id,
                    jobReturnEvent.getMinionId(),
                    jobReturnEvent.getData().getRetcode(),
                    jobReturnEvent.getData().isSuccess(),
                    jobReturnEvent.getJobId(),
                    jobResult.get(),
                    Optional.ofNullable(jobReturnEvent.getData().getFun()).map(Xor::right))));
        // Check if the event was triggered by an action chain execution
        Optional<Boolean> isActionChainResult = isActionChainResult(jobReturnEvent);
        boolean isActionChainInvolved = isActionChainResult.filter(isActionChain -> isActionChain).orElse(false);
        isActionChainResult.filter(isActionChain -> isActionChain).ifPresent(isActionChain -> {
            if (jobResult.isEmpty()) {
                return;
            }
            AtomicReference<Optional<User>> scheduler = new AtomicReference<>(Optional.empty());
            JsonElement jsonResult = jobResult.get();
            // The Salt reactor triggers a "suma-action-chain" job (mgractionchains.resume) at
            // 'minion/startup/event/'. This means the result might not be a JSON in case of
            // a Salt error when the 'mgractionchains' custom module is not yet deployed.
            if (Arrays.asList(1, 254).contains(jobReturnEvent.getData().getRetcode()) &&
                    !jobReturnEvent.getData().isSuccess() &&
                    jobReturnEvent.getData().getResult().toString().startsWith("'mgractionchains") &&
                    jobReturnEvent.getData().getResult().toString().endsWith("' is not available.")
            ) {

                jobReturnEvent.getData().getMetadata(ScheduleMetadata.class).ifPresent(metadata -> {
                    Optional<ActionChain> actionChain =
                            Optional.ofNullable(metadata.getActionChainId())
                                    .flatMap(ActionChainFactory::getActionChain);

                    actionChain.ifPresent(ac -> {
                            scheduler.set(Optional.ofNullable(ac.getUser()));
                            ac.getEntries().stream()
                                    .flatMap(ace -> ace.getAction().getServerActions().stream())
                                    .filter(sa -> sa.getServer().asMinionServer()
                                            .filter(m -> m.getMinionId().equals(jobReturnEvent.getMinionId()))
                                            .isPresent()
                                    )
                                    .filter(sa -> !sa.getStatus().isDone())
                                    .forEach(sa -> sa.fail(jobReturnEvent.getData().getResult().toString()));
                            if (ac.isDone()) {
                                ActionChainFactory.delete(ac);
                            }
                    });
                });

                return;
            }

            Map<String, StateApplyResult<Ret<JsonElement>>> actionChainResult;
            try {
                actionChainResult = Json.GSON.fromJson(jsonResult,
                        new TypeToken<Map<String, StateApplyResult<Ret<JsonElement>>>>() {
                        }.getType());
            }
            catch (JsonSyntaxException e) {
                LOG.error("Error mapping action chain result: {}", jsonResult, e);
                throw e;
            }

            saltServerActionService.handleActionChainResult(jobReturnEvent.getMinionId(),
                    jobReturnEvent.getJobId(),
                    actionChainResult,
                    stateResult -> false);

            if (isFunctionTestMode) {
                return;
            }

            Set<PackageChangeOutcome> packageRefreshNeeded = actionChainResult.entrySet().stream()
                    .map(entry -> SaltActionChainGeneratorService.parseActionChainStateId(entry.getKey())
                            .map(stateId -> handlePackageChanges(jobReturnEvent, entry.getValue().getName(),
                                    Optional.ofNullable(entry.getValue().getChanges().getRet())))
                            .orElse(PackageChangeOutcome.DONE)
                    ).collect(Collectors.toSet());

            if (packageRefreshNeeded.contains(PackageChangeOutcome.NEEDS_DELAYED_REFRESHING)) {
                schedulePackageRefresh(scheduler.get(), jobReturnEvent.getMinionId(),
                                       Date.from(Instant.now().plusSeconds(60)));
            }
            else if (packageRefreshNeeded.contains(PackageChangeOutcome.NEEDS_REFRESHING)) {
                schedulePackageRefresh(scheduler.get(), jobReturnEvent.getMinionId());
            }
        });

        //For all jobs except when action chains are involved or the action was in test mode
        if (!isActionChainInvolved && !isFunctionTestMode) {
            Date earliest = new Date();
            PackageChangeOutcome changeOutcome = handlePackageChanges(jobReturnEvent,
                            Optional.ofNullable(function).map(Xor::right), jobResult);
            if (changeOutcome != PackageChangeOutcome.DONE) {
                if (changeOutcome == PackageChangeOutcome.NEEDS_DELAYED_REFRESHING) {
                    earliest = Date.from(Instant.now().plusSeconds(60));
                }
                Optional<User> scheduler = Optional.empty();
                if (actionId.isPresent()) {
                    Optional<Action> action = Optional.ofNullable(ActionFactory.lookupById(actionId.get()));
                    if (action.isPresent()) {
                        scheduler = Optional.ofNullable(action.get().getSchedulerUser());
                        if (action.get().getActionType().equals(ActionFactory.TYPE_DIST_UPGRADE)) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.add(Calendar.SECOND, 30);
                            earliest = calendar.getTime();
                        }
                    }
                }
                schedulePackageRefresh(scheduler, jobReturnEvent.getMinionId(), earliest);
            }
        }

        // Check if event was triggered in response to state scheduled at minion start-up event
        if (isMinionStartup(jobReturnEvent)) {
            MinionServerFactory.findByMinionId(jobReturnEvent.getMinionId())
                    .ifPresent(minion -> jobResult
                            .ifPresent(result-> {
                                SystemInfo systemInfo = Json.GSON.fromJson(result, SystemInfo.class);
                                saltUtils.updateSystemInfo(systemInfo, minion);
                            }));

            /* Check in case any Action update it could complete any ActionChain that is still pending on that action.
             *
             * Scenario: when an ActionChain ends with a Reboot Action, before deleting the ActionChain we need to wait
             * until the target minion is rebooted and restarted. But even when the minion gets back with the
             * "mgractionchains.resume" it is not yet the time to delete the ActionChain. We still wait for
             * the "minion startup" tasks; indeed the Reboot Action is not yet marked as Completed until that task
             * is executed. At some point the Reboot Action is eventually set to Completed triggered by
             * the JobReturnEventMessageAction of the "minion startup" job, which is not part of the ActionChain.
             * The root of the problem is that the ActionChain has to wait until another Action is Completed,
             * but the return message of that Action does know nothing about the ActionChain who triggered it.
             *
             *
             * The following will check if, for all the existing ActionChain, there are any completely done.
             * If so, just remove it. (bsc#1188163)
             */

            MinionServerFactory.findByMinionId(jobReturnEvent.getMinionId())
                    .ifPresent(minion -> ActionChainFactory.getActionChainsByServer(minion).stream()
                    .filter(ActionChain::isDone)
                    .forEach(ActionChainFactory::delete));

        }
      // For all jobs: update minion last checkin
        Optional<MinionServer> minion = MinionServerFactory.findByMinionId(
                jobReturnEvent.getMinionId());
        if (minion.isPresent()) {
            MinionServer m = minion.get();
            if (jobResult.isEmpty()) {
                LOG.warn("Do not update server info since job={} running " +
                        "function={} in minion={} is empty",
                        jobReturnEvent.getJobId(),
                        function,
                        jobReturnEvent.getMinionId());
                return;
            }
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
     * This method does two things
     * 1) Update database with delta package info if enough information is available
     * 2) If there is not enough information then return true to indicate that full package refresh is needed
     * @param jobReturnEvent jobReturnEvent
     * @param function salt module name
     * @param jobResult result
     * @return return a PackageChangeOutcome
     *         DONE when there is enough information to update database with new Package information(delta)
     *         NEEDS_REFRESHING when information is not enough and a full package refresh is needed.
     *         If a refresh should happen with a delay, NEEDS_DELAYED_REFRESHING is returned
     */
    private PackageChangeOutcome handlePackageChanges(JobReturnEvent jobReturnEvent,
                    Optional<Xor<String[], String>> function, Optional<JsonElement> jobResult) {

        return MinionServerFactory.findByMinionId(jobReturnEvent.getMinionId()).flatMap(minionServer ->
                jobResult.map(result -> {
                    PackageChangeOutcome refreshType = PackageChangeOutcome.DONE;
                    try {
                        refreshType = saltUtils.handlePackageChanges(function, result, minionServer);
                    }
                     catch (JsonParseException e) {
                        LOG.warn("Could not determine if packages changed in call to {} because of a parse error",
                                 function, e);
                    }
                    if (refreshType == PackageChangeOutcome.DONE && forcePackageListRefresh(jobReturnEvent)) {
                        refreshType = PackageChangeOutcome.NEEDS_REFRESHING;
                    }
                    return refreshType;
                })
        ).orElse(PackageChangeOutcome.DONE);
    }

    /**
     * Schedule package refresh on the minion
     * @param minionId ID of the minion for which package refresh should be scheduled
     */
    private void schedulePackageRefresh(Optional<User> user, String minionId) {
        schedulePackageRefresh(user, minionId, new Date());
    }

    /**
     * Schedule package refresh on the minion
     * @param minionId ID of the minion for which package refresh should be scheduled
     * @param earliest The earliest time this action should be run.
     */
    private void schedulePackageRefresh(Optional<User> user, String minionId, Date earliest) {
        MinionServerFactory.findByMinionId(minionId).ifPresent(minionServer -> {
            try {
                ActionManager.schedulePackageRefresh(user, minionServer, earliest);
            }
            catch (TaskomaticApiException e) {
                LOG.error(e.getMessage(), e);
            }
        });
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
