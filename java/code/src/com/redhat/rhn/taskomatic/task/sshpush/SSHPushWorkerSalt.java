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

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionStatus;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.frontend.dto.SystemPendingEventDto;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.taskomatic.task.threaded.QueueWorker;
import com.redhat.rhn.taskomatic.task.threaded.TaskQueue;

import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.services.SaltServerActionService;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.Test;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import org.apache.log4j.Logger;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.redhat.rhn.domain.action.ActionFactory.STATUS_COMPLETED;
import static com.redhat.rhn.domain.action.ActionFactory.STATUS_FAILED;
import static java.util.Optional.ofNullable;

/**
 * SSH push worker executing scheduled actions via Salt SSH.
 */
public class SSHPushWorkerSalt implements QueueWorker {

    private Logger log;
    private SSHPushSystem system;
    private TaskQueue parentQueue;

    private SaltService saltService;
    private SaltUtils saltUtils;

    /**
     * Constructor.
     * @param logger Logger for this instance
     * @param systemIn the system to work with
     */
    public SSHPushWorkerSalt(Logger logger, SSHPushSystem systemIn) {
        log = logger;
        system = systemIn;
        saltService = SaltService.INSTANCE;
        saltUtils = SaltUtils.INSTANCE;
    }

    /**
     * Constructor.
     * @param logger Logger for this instance
     * @param systemIn the system to work with
     * @param saltServiceIn the salt service to work with
     * @param saltUtilsIn the salt utils instance to work with
     */
    public SSHPushWorkerSalt(Logger logger, SSHPushSystem systemIn,
            SaltService saltServiceIn, SaltUtils saltUtilsIn) {
        log = logger;
        system = systemIn;
        saltService = saltServiceIn;
        saltUtils = saltUtilsIn;
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
        try {
            parentQueue.workerStarting();

            MinionServerFactory.lookupById(system.getId()).ifPresent(m -> {
                log.info("Executing actions for minion: " + m.getMinionId());

                boolean checkinNeeded = true;

                // TODO hande salt-ssh reboot


                // Perform a check-in if there is no pending actions
                if (checkinNeeded) {
                    performCheckin(m);
                }

                saltService.getUptimeForMinion(m).ifPresent(uptime ->
                        SaltUtils.INSTANCE.handleUptimeUpdate(m, uptime));

                log.debug("Nothing left to do for " + m.getMinionId() + ", exiting worker");
            });
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            HibernateFactory.rollbackTransaction();
        }
        finally {
            parentQueue.workerDone();
            HibernateFactory.closeSession();

            // Finished talking to this system
            SSHPushDriver.getCurrentSystems().remove(system);
        }
    }

    private void performCheckin(MinionServer minion) {
        // Ping minion and perform check-in on success
        log.info("Performing a check-in for: " + minion.getMinionId());
        Optional<Boolean> result = saltService
                .callSync(Test.ping(), minion.getMinionId());
        if (result.isPresent()) {
            minion.updateServerInfo();
        }
    }

    /**
     * Execute action on minion.
     *
     * @param action the action to be executed
     * @param minion minion on which the action will be executed
     */
    public boolean executeAction(Action action, MinionServer minion) {
        Optional<ServerAction> serverAction = action.getServerActions().stream()
                .filter(sa -> sa.getServer().equals(minion))
                .findFirst();
        if (serverAction.isPresent()) {
            ServerAction sa = serverAction.get();
            if (sa.getStatus().equals(STATUS_FAILED) ||
                    sa.getStatus().equals(STATUS_COMPLETED)) {
                log.info("Action '" + action.getName() + "' is completed or failed." +
                        " Skipping.");
                return false;
            }

            if (prerequisiteInStatus(sa, ActionFactory.STATUS_QUEUED)) {
                log.info("Prerequisite of action '" + action.getName() + "' is still" +
                        " queued. Skipping executing of the action.");
                return false;
            }

            if (prerequisiteInStatus(sa, ActionFactory.STATUS_FAILED)) {
                log.info("Failing action '" + action.getName() + "' as its prerequisite '" +
                        action.getPrerequisite().getName() + "' failed.");
                sa.setStatus(STATUS_FAILED);
                sa.setResultMsg("Prerequisite failed.");
                sa.setResultCode(-100L);
                sa.setCompletionTime(new Date());
                return false;
            }

            if (sa.getRemainingTries() < 1) {
                log.info("NOT executing and failing action '" + action.getName() + "' as" +
                        " the maximum number of re-trials has been reached.");
                sa.setStatus(STATUS_FAILED);
                sa.setResultMsg("Action has been picked up multiple times" +
                        " without a successful transaction;" +
                        " This action is now failed for this system.");
                sa.setCompletionTime(new Date());
                return false;
            }

            sa.setRemainingTries(sa.getRemainingTries() - 1);

            Map<LocalCall<?>, List<MinionServer>> calls = SaltServerActionService.INSTANCE
                    .callsForAction(action, Arrays.asList(minion));

            calls.keySet().forEach(call -> {
                Optional<JsonElement> result;
                // try-catch as we'd like to log the warning in case of exception
                try {
                    result = saltService
                            .callSync(new JsonElementCall(call), minion.getMinionId());
                } catch (RuntimeException e) {
                    log.warn("Exception for salt call for action: '" + action.getName() +
                            "'. Will be re-tried " + sa.getRemainingTries() + " times");
                    throw e;
                }

                if (!result.isPresent()) {
                    log.error("No result for salt call for action: '" + action.getName() +
                            "'. Will be re-tried " + sa.getRemainingTries() + " times");
                }

                result.ifPresent(r -> {
                    if (log.isTraceEnabled()) {
                        log.trace("Salt call result: " + r);
                    }
                    String function = (String) call.getPayload().get("fun");

                    // reboot needs special handling in case of ssh push
                    if (action.getActionType().equals(ActionFactory.TYPE_REBOOT)) {
                        sa.setStatus(ActionFactory.STATUS_PICKED_UP);
                    } else {
                        saltUtils.updateServerAction(sa, 0L, true, "n/a",
                                r, function);
                    }

                    // Perform a "check-in" after every executed action
                    minion.updateServerInfo();

                    // Perform a package profile update in the end if necessary
                    if (saltUtils.shouldRefreshPackageList(function, result)) {
                        log.info("Scheduling a package profile update");
                        Action pkgList;
                        try {
                            pkgList = ActionManager.schedulePackageRefresh(minion.getOrg(), minion);
                            executeAction(pkgList, minion);
                        }
                        catch (TaskomaticApiException e) {
                            log.error("Could not schedule package refresh for minion: " +
                                    minion.getMinionId());
                            log.error(e);
                        }
                    }
                });
            });
        }
        return false;
    }

    /**
     * Checks whether the parent action of given server action contains a server action
     * that is in given state and is associated with the server of given server action.
     * @param serverAction server action
     * @param state state
     * @return true if there exists a server action in given state associated with the same
     * server as serverAction and parent action of serverAction
     */
    private boolean prerequisiteInStatus(ServerAction serverAction, ActionStatus state) {
        Optional<Stream<ServerAction>> prerequisites =
                ofNullable(serverAction.getParentAction())
                        .map(Action::getPrerequisite)
                        .map(Action::getServerActions)
                        .map(a -> a.stream());

        return prerequisites
                .flatMap(serverActions ->
                        serverActions
                                .filter(s ->
                                        serverAction.getServer().equals(s.getServer()) &&
                                                state.equals(s.getStatus()))
                                .findAny())
                .isPresent();
    }

    /**
     * Manipulate a given {@link LocalCall} object to return a {@link JsonElement} instead
     * of the specified return type.
     */
    private class JsonElementCall extends LocalCall<JsonElement> {

        @SuppressWarnings("unchecked")
        JsonElementCall(LocalCall<?> call) {
            super((String) call.getPayload().get("fun"),
                    ofNullable((List<?>) call.getPayload().get("arg")),
                    ofNullable((Map<String, ?>) call.getPayload().get("kwarg")),
                    new TypeToken<JsonElement>() { });
        }
    }
}
