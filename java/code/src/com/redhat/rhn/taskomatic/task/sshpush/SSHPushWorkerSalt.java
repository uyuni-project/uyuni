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
package com.redhat.rhn.taskomatic.task.sshpush;

import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.taskomatic.task.checkin.SystemSummary;
import com.redhat.rhn.taskomatic.task.threaded.QueueWorker;
import com.redhat.rhn.taskomatic.task.threaded.TaskQueue;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.services.SaltServerActionService;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.salt.MgrActionChains;
import com.suse.manager.webui.utils.salt.custom.SystemInfo;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.calls.modules.Test;

import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.Result;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.redhat.rhn.frontend.events.TransactionHelper.handlingTransaction;

/**
 * SSH push worker for checking in ssh-push systems and resuming action chains via Salt SSH.
 */
public class SSHPushWorkerSalt implements QueueWorker {

    private static final int SSH_TIMEOUT = 60;

    private enum ResumeOutcome {
        RESUMED,
        CONNECTION_REFUSED,
        ERROR,
        REBOOT_PENDING,
        NO_ACTION_CHAIN
    }

    private Logger log;
    private SystemSummary system;
    private TaskQueue parentQueue;

    private SaltService saltService;
    private SaltSSHService saltSSHService;
    private SaltServerActionService saltServerActionService;

    /**
     * Constructor.
     * @param logger Logger for this instance
     * @param systemIn the system to work with
     */
    public SSHPushWorkerSalt(Logger logger, SystemSummary systemIn) {
        log = logger;
        system = systemIn;
        saltService = SaltService.INSTANCE;
        saltSSHService = SaltService.INSTANCE.getSaltSSHService();
        saltServerActionService = SaltServerActionService.INSTANCE;
    }

    /**
     * Constructor.
     * @param logger Logger for this instance
     * @param systemIn the system to work with
     * @param saltServiceIn the salt service to work with
     * @param saltSSHServiceIn the {@link SaltSSHService} to work with
     * @param saltServerActionServiceIn the {@link SaltServerActionService} to work with
     */
    public SSHPushWorkerSalt(Logger logger, SystemSummary systemIn,
            SaltService saltServiceIn, SaltSSHService saltSSHServiceIn,
            SaltServerActionService saltServerActionServiceIn) {
        log = logger;
        system = systemIn;
        saltService = saltServiceIn;
        saltSSHService = saltSSHServiceIn;
        saltServerActionService = saltServerActionServiceIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParentQueue(TaskQueue queue) {
        parentQueue = queue;
    }

    /**
     * Get pending actions for the given minion server and execute those where the schedule
     * date and time has come.
     */
    @Override
    public void run() {
        parentQueue.workerStarting();

        try {
            Optional<MinionServer> minionOpt = handlingTransaction(
                    () -> MinionServerFactory.lookupById(system.getId()),
                    (err) -> log.error("Error looking up minion server id=" + system.getId()))
                    .flatMap(Function.identity());

            if (minionOpt.isEmpty()) {
                log.error("Minion not found id=" + system.getId());
                return;
            }

            MinionServer m = minionOpt.get();
            log.info("Executing ssh-push job for minion: " + m.getMinionId());
            if (system.isRebooting()) {
                ResumeOutcome res = resumeActionChainIfPending(m);
                if (!ResumeOutcome.CONNECTION_REFUSED.equals(res) &&
                        !ResumeOutcome.RESUMED.equals(res) &&
                        !ResumeOutcome.REBOOT_PENDING.equals(res)
                ) {
                    // no action chain was resumed and connection was successful so check in can be attempted
                    // wait until it completes to avoid assigning checkInFuture
                    // from this closure
                    performCheckin(m);
                }
            }
            else {
                performCheckin(m);
            }

            updateSystemInfo(m.getMinionId());
        }
        catch (Exception e) {
            log.debug("Error executing ssh-push job", e);
        }
        finally {
            parentQueue.workerDone();
        }

        log.debug("Nothing left to do for " + system.getMinionId() + ", exiting worker");
    }

    /**
     * Try to resume an action chain execution.
     * @param minion the ssh-push minion
     * @return true if there was an action chain execution pending and it was resumed successfully
     */
    private ResumeOutcome resumeActionChainIfPending(MinionServer minion) {
        log.debug("Checking if any chain execution needs to be resumed on minion: " + minion.getMinionId());
        Map<String, Result<Map<String, String>>> pendingResume;
        try {
            // first check if there's any pending action chain execution on the minion
            // fetch the extra_filerefs and next_action_id
            pendingResume = saltService.callSync(MgrActionChains.getPendingResume(),
                    new MinionList(minion.getMinionId()));
        }
        catch (SaltException e) {
            log.error("Could not retrieve pending action chain executions for minion", e);
            // retry later and skip performing the check-in
            throw new RuntimeException(e);
        }

        if (pendingResume.isEmpty()) {
            log.debug("Minion " + minion.getMinionId() + " is probably not up." +
                    " Checking later for pending action chain executions.");
            return ResumeOutcome.CONNECTION_REFUSED;
        }

        ResumeOutcome pendingResumeOutcome = pendingResume.get(minion.getMinionId()).fold(err -> {
                    String errorStr = err.fold(
                            Object::toString,
                            Object::toString,
                            jsonParsingError -> SaltUtils.decodeSaltErr(jsonParsingError),
                            Object::toString
                    );
                    if (errorStr.contains("Connection refused")) {
                        log.info("Connection refused to minion " + minion.getMinionId() +
                                ". Checking later for pending action chain executions.");
                        return ResumeOutcome.CONNECTION_REFUSED;
                    }
                    else {
                        log.error("mgractionchains.get_pending_resume failed: " + errorStr);
                        return ResumeOutcome.ERROR;
                    }
                },
                res -> ResumeOutcome.RESUMED);

        if (ResumeOutcome.CONNECTION_REFUSED.equals(pendingResumeOutcome)) {
            return pendingResumeOutcome;
        }
        else if (ResumeOutcome.ERROR.equals(pendingResumeOutcome)) {
            // something else went wrong, try to remove the pending action chain from the minion
            saltSSHService.cleanPendingActionChainAsync(minion);
            return ResumeOutcome.ERROR;
        }

        Optional<Map<String, String>> confValues = pendingResume.get(minion.getMinionId())
                .fold(err -> Optional.empty(), res -> Optional.of(res));

        if (!confValues.isPresent() || confValues.get().isEmpty()) {
            log.debug("No action chain execution pending on minion " + minion.getMinionId());
            return ResumeOutcome.NO_ACTION_CHAIN;
        }

        Map<String, String> values = confValues.get();
        String extraFileRefs = values.get("ssh_extra_filerefs");
        String nextActionIdStr = values.get("next_action_id");
        String nextChunk = values.get("next_chunk");
        String currentBoot = values.get("current_boot_time");
        String persistBoot = values.get("persist_boot_time");

        if (StringUtils.isBlank(currentBoot) || StringUtils.isBlank(persistBoot) ||
                StringUtils.isBlank(nextActionIdStr) || StringUtils.isBlank(nextChunk) ||
                StringUtils.isBlank(extraFileRefs)) {
            // can't continue if any of the required fields is missing
            log.error("Could not resume pending action chain execution, " +
                    "mgractionchains.get_pending_resume for ssh minion " + minion.getMinionId() +
                    "didn't return all required fields. Fields=" +
                    StringUtils.join(confValues));
            saltSSHService.cleanPendingActionChainAsync(minion);

            // fail the entire action chain if possible
            if (StringUtils.isNotBlank(nextActionIdStr)) {
                parseLong(nextActionIdStr).ifPresent(id ->
                        failActionChain(minion, Optional.of(id)));
                return ResumeOutcome.ERROR;
            }
        }

        Optional<LocalDateTime> currentBootTime = parseDateTime(minion, currentBoot);
        Optional<LocalDateTime> persistBootTime = parseDateTime(minion, persistBoot);
        if (!currentBootTime.isPresent() || !currentBootTime.isPresent()) {
            return ResumeOutcome.ERROR;
        }

        // status.uptime returns a few miliseconds of difference in the boot time between subsequent invocations
        // even when the boot time stays the same
        long timeDelta = Math.abs(
                currentBootTime.get().toLocalTime().toSecondOfDay() -
                        persistBootTime.get().toLocalTime().toSecondOfDay());
        if (timeDelta < 3) {
            // don't resume the action chain, the system hasn't rebooted yet
            log.info("Not resuming action chain execution. Minion " + minion.getMinionId() +
                    " hasn't rebooted yet");
            return ResumeOutcome.REBOOT_PENDING;
        }

        Optional<Long> nextActionId = parseLong(nextActionIdStr);
        if (nextActionId.isEmpty()) {
            log.error("Action chain can't be resumed. next_action_id is not a valid number");
            saltSSHService.cleanPendingActionChainAsync(minion);
            throw new RuntimeException("next_action_id is not a valid number");
        }

        Optional<Boolean> isNextActionFailed = handlingTransaction(() ->
                nextActionId
                    .map(ActionFactory::lookupById)
                    .flatMap(action -> action.getServerActions().stream()
                            .filter(sa -> sa.getServer().equals(minion)).findFirst())
                    .filter(sa -> ActionFactory.STATUS_FAILED.equals(sa.getStatus()))
                    .isPresent(),
                err ->
                        log.error("Error checking if next action is failed nextActionId=" +
                                nextActionIdStr +
                                ", minion=" + minion.getMinionId() + ": ", err)
        );
        if (isNextActionFailed.orElse(false)) {
            // Next action is failed due to some previous error in the action chain.
            // This means action chain is already failed, remove pending action chain execution
            saltSSHService.cleanPendingActionChainAsync(minion);
            return ResumeOutcome.NO_ACTION_CHAIN;
        }

        try {
            log.info("Resuming action chain execution on minion: " + minion.getMinionId());
            Map<String, Result<Map<String, State.ApplyResult>>> res = saltSSHService.callSyncSSH(
                    State.apply("actionchains.resumessh"),
                    new MinionList(minion.getMinionId()),
                    Optional.of(extraFileRefs),
                    Optional.of(SSH_TIMEOUT));

            Result<Map<String, State.ApplyResult>> chunkResult = res.get(minion.getMinionId());
            if (chunkResult != null) {
                if (chunkResult.result().isPresent()) {
                    Optional<Boolean> handledOk = handlingTransaction(() ->
                            saltServerActionService
                                .handleActionChainSSHResult(nextActionId, minion.getMinionId(),
                                        chunkResult.result().get())
                            , err ->
                                    log.error("Error handling action chain resume result for minion " +
                                            minion.getMinionId(), err)
                    );
                    return handledOk.orElse(false) ? ResumeOutcome.RESUMED : ResumeOutcome.ERROR;
                }
                else {
                    String errMsg = chunkResult.error().map(saltErr -> saltErr.fold(
                            Object::toString,
                            Object::toString,
                            e ->  "Error parsing JSON: " + e.getJson(),
                            Object::toString
                    )).orElse("Unknown error");
                    log.error("Error resuming action chain on minion " + minion.getMinionId() +
                            ": " + errMsg);
                    failActionChain(minion, nextActionId);
                }
            }
            else {
                log.error("No action chain result for minion " + minion.getMinionId());
                return ResumeOutcome.ERROR;
            }
        }
        catch (SaltException e) {
            log.error("Error resuming action on minion " + minion.getMinionId(), e);
            throw new RuntimeException(e);
        }

        return ResumeOutcome.RESUMED;
    }

    private void failActionChain(MinionServer minion, Optional<Long> nextActionId) {
        handlingTransaction(() ->
                        SaltServerActionService.failActionChain(minion.getMinionId(), nextActionId,
                                Optional.of("Could not resume pending action chain execution. " +
                                        "mgractionchains.get_pending_resume didn't return all " +
                                        "required fields"))
                ,
                (err) -> log.error("Error setting action chain to failed, nextActionId=" +
                        nextActionId + " minion=" + minion.getMinionId(), err)
        );
    }

    private Optional<LocalDateTime> parseDateTime(MinionServer minion, String dateTimeString) {
        try {
            return Optional.of(LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        catch (DateTimeParseException e) {
            log.error("Action chain can't be resumed. current_boot_time is not a valid date", e);
            saltSSHService.cleanPendingActionChainAsync(minion);
            return Optional.empty();
        }
    }

    private Optional<Long> parseLong(String idStr) {
        try {
            return Optional.of(Long.parseLong(idStr));
        }
        catch (NumberFormatException e) {
            log.error("Error parsing as long", e);
            return Optional.empty();
        }
    }

    private void performCheckin(MinionServer minion) {
        // Ping minion and perform check-in on success
        log.info("Performing a check-in for: " + minion.getMinionId());
        try {
            Map<String, Result<Boolean>> res = saltSSHService.callSyncSSH(Test.ping(),
                    new MinionList(minion.getMinionId()),
                    SSH_TIMEOUT);
            boolean ok = Optional.ofNullable(res.get(minion.getMinionId()))
                    .flatMap(r -> r.result())
                    .orElse(false);
            if (ok) {
                handlingTransaction(() ->
                        MinionServerFactory.lookupById(minion.getId())
                                .ifPresent(min -> min.updateServerInfo()),
                        err ->
                            log.info("Error checking in minion " + minion.getMinionId(), err)
                        );
            }
        }
        catch (SaltException e) {
            log.info("Salt error checking in m minion " + minion.getMinionId(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Apply util.systeminfo state on the specified ssh-minion in an asynchronous away
     * @param minionId minion id
     */
    private void updateSystemInfo(String minionId) {
        log.info("Updating system info for: " + minionId);
        LocalCall<SystemInfo> applySystemInfo =
                com.suse.manager.webui.utils.salt.State.apply(Arrays.asList(ApplyStatesEventMessage.SYSTEM_INFO),
                        Optional.empty(), Optional.of(true), Optional.empty(), SystemInfo.class);
        try {
            Map<String, Result<SystemInfo>> res = saltSSHService.callSyncSSH(applySystemInfo,
                    new MinionList(minionId),
                    SSH_TIMEOUT);
            Optional.ofNullable(res.get(minionId))
                    .flatMap(r -> r.result())
                    .ifPresent(r ->
                        handlingTransaction(() ->
                                        MinionServerFactory.findByMinionId(minionId)
                                                .ifPresent(min ->
                                                        SaltUtils.INSTANCE.updateSystemInfo(r, min)),
                                err ->
                                        log.info("Error updating the server info for minion " +
                                                minionId, err)
                        )
                    );
        }
        catch (SaltException e) {
            log.info("Salt error updating the server info for minion " + minionId, e);
            throw new RuntimeException(e);
        }
    }
}
