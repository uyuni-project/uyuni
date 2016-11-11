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
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.frontend.dto.SystemPendingEventDto;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.task.threaded.QueueWorker;
import com.redhat.rhn.taskomatic.task.threaded.TaskQueue;

import com.suse.manager.reactor.messaging.JobReturnEventMessageAction;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SSH push worker executing scheduled actions via Salt SSH.
 */
public class SSHPushWorkerSalt implements QueueWorker {

    private Logger log;
    private SSHPushSystem system;
    private TaskQueue parentQueue;

    private boolean packageListRefreshNeeded = false;

    /**
     * Constructor.
     * @param logger Logger for this instance
     * @param systemIn the system to work with
     */
    public SSHPushWorkerSalt(Logger logger, SSHPushSystem systemIn) {
        log = logger;
        system = systemIn;
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

                // Get pending actions and reverse to put them in ascending order
                // TODO: consider prerequisites
                DataResult<SystemPendingEventDto> pendingEvents = SystemManager
                        .systemPendingEvents(m.getId(), null);
                Collections.reverse(pendingEvents);
                log.debug("Number of pending actions: " + pendingEvents.size());
                int actionsExecuted = 0;

                for (SystemPendingEventDto event : pendingEvents) {
                    log.debug("Looking at pending action: " + event.getActionName());

                    ZonedDateTime now = ZonedDateTime.now();
                    ZonedDateTime scheduleDate = event.getScheduledFor().toInstant()
                            .atZone(ZoneId.systemDefault());

                    if (scheduleDate.isAfter(now)) {
                        // Nothing left to do at the moment, get out of here
                        break;
                    }
                    else {
                        log.info("Executing action (id=" + event.getId() + "): " +
                                event.getActionName());
                        Action action = ActionFactory.lookupById(event.getId());
                        try {
                            executeAction(action, m);
                        }
                        catch (Exception e) {
                            log.error("Error executing action: " + e.getMessage(), e);
                        }
                        actionsExecuted++;
                    }
                }

                // Perform a package profile update in the end if needed
                if (packageListRefreshNeeded) {
                    Action pkgList = ActionManager.schedulePackageRefresh(m.getOrg(), m);
                    executeAction(pkgList, m);
                }

                // Perform a check-in if there is no pending actions
                if (actionsExecuted == 0) {
                    performCheckin(m);
                }

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
        Optional<Boolean> result = SaltService.INSTANCE
                .callSync(Test.ping(), minion.getMinionId());
        if (result.isPresent()) {
            minion.updateServerInfo();
        }
    }

    private void executeAction(Action action, MinionServer minion) {
        Optional<ServerAction> serverAction = action.getServerActions().stream()
                .filter(sa -> sa.getServer().equals(minion))
                .findFirst();
        serverAction.ifPresent(sa -> {
            Map<LocalCall<?>, List<MinionServer>> calls = SaltServerActionService.INSTANCE
                    .callsForAction(action, Arrays.asList(minion));

            calls.keySet().forEach(call -> {
                Optional<JsonElement> result = SaltService.INSTANCE
                        .callSync(new JsonElementCall(call), minion.getMinionId());

                if (!result.isPresent()) {
                    log.error("No Salt call result for: " + action.getName());
                    // TODO: Implement retry mechanism based on number of failures?
                }

                result.ifPresent(r -> {
                    if (log.isTraceEnabled()) {
                        log.trace("Salt call result: " + r);
                    }
                    String function = (String) call.getPayload().get("fun");
                    JobReturnEventMessageAction.updateServerAction(sa, 0L, true, "n/a", r,
                            function);

                    // Perform a package profile update in the end if necessary
                    if (JobReturnEventMessageAction
                            .shouldRefreshPackageList(function, result)) {
                        log.info("Scheduling a package profile update");
                        this.packageListRefreshNeeded = true;
                    }

                    // Perform a "check-in" after every executed action
                    minion.updateServerInfo();
                });
            });
        });
    }

    /**
     * Manipulate a given {@link LocalCall} object to return a {@link JsonElement} instead
     * of the specified return type.
     */
    private class JsonElementCall extends LocalCall<JsonElement> {

        @SuppressWarnings("unchecked")
        JsonElementCall(LocalCall<?> call) {
            super((String) call.getPayload().get("fun"),
                    Optional.ofNullable((List<?>) call.getPayload().get("arg")),
                    Optional.ofNullable((Map<String, ?>) call.getPayload().get("kwarg")),
                    new TypeToken<JsonElement>() { });
        }
    }
}
