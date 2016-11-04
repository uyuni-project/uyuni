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
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.task.threaded.QueueWorker;
import com.redhat.rhn.taskomatic.task.threaded.TaskQueue;

import com.suse.manager.reactor.messaging.JobReturnEventMessageAction;
import com.suse.manager.webui.services.SaltServerActionService;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.salt.netapi.calls.LocalCall;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import org.apache.log4j.Logger;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SSH push worker executing actions via salt-ssh.
 */
public class SSHPushWorkerSalt implements QueueWorker {

    private Logger log;
    private SSHPushSystem system;
    private TaskQueue parentQueue;

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
     * {@inheritDoc}
     */
    @Override
    public void run() {
        try {
            parentQueue.workerStarting();

            // Lookup the minion server
            MinionServerFactory.lookupById(system.getId()).ifPresent(m -> {
                log.info("Executing actions for minion: " + m.getMinionId());

                // Get the current list of pending actions and execute those in the right
                // order (TODO: consider prerequisites!)
                DataResult<SystemPendingEventDto> pendingEvents = SystemManager
                        .systemPendingEvents(m.getId(), null);
                log.debug("Number of pending actions: " + pendingEvents.size());

                for (SystemPendingEventDto event : pendingEvents) {
                    log.debug("Looking at pending action: " + event.getActionName());

                    // Compare dates to figure out if the time has come yet
                    ZonedDateTime now = ZonedDateTime.now();
                    ZonedDateTime scheduleDate = event.getScheduledFor().toInstant()
                            .atZone(ZoneId.systemDefault());

                    if (scheduleDate.isAfter(now)) {
                        // Nothing left to do at the moment, get out of here
                        break;
                    }
                    else {
                        log.info("Executing action: " + event.getActionName());
                        Action action = ActionFactory.lookupById(event.getId());
                        executeAction(action, m);
                    }
                }
                log.debug("Nothing left to do, exiting worker");
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

    private void executeAction(Action action, MinionServer minion) {
        Optional<ServerAction> serverAction = action.getServerActions().stream()
                .filter(sa -> sa.getServer().equals(minion))
                .findFirst();
        serverAction.ifPresent(sa -> {
            Map<LocalCall<?>, List<MinionServer>> calls =
                    SaltServerActionService.INSTANCE.callsForAction(
                            action, Arrays.asList(minion));

            calls.keySet().forEach(call -> {
                Optional<JsonElement> result = SaltService.INSTANCE.callSync(
                        new JsonElementCall(call), minion.getMinionId());
                log.trace("Result of call: " + result);
                result.ifPresent(r -> {
                    JobReturnEventMessageAction.updateServerAction(sa, 0L, true, "foo", r,
                            (String) call.getPayload().get("fun"));
                });
            });
        });
    }

    private class JsonElementCall extends LocalCall<JsonElement> {
        @SuppressWarnings("unchecked")
        public JsonElementCall(LocalCall<?> call) {
            super((String) call.getPayload().get("fun"),
                    Optional.ofNullable((List<?>) call.getPayload().get("arg")),
                    Optional.ofNullable((Map<String, ?>) call.getPayload().get("kwarg")),
                    new TypeToken<JsonElement>(){});
        }
    }
}
