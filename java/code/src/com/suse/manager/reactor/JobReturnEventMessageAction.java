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
package com.suse.manager.reactor;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.MinionServer;

import com.suse.manager.webui.services.impl.SaltAPIService;
import com.suse.manager.webui.utils.salt.JobReturnEvent;

import com.suse.saltstack.netapi.datatypes.target.MinionList;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

/**
 * Handler class for {@link JobReturnEventMessage}.
 */
public class JobReturnEventMessageAction implements MessageAction {

    /* Logger for this class */
    private static final Logger LOG = Logger.getLogger(JobReturnEventMessageAction.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(EventMessage msg) {
        JobReturnEventMessage jobReturnEventMessage = (JobReturnEventMessage) msg;
        JobReturnEvent jobReturnEvent = jobReturnEventMessage.getJobReturnEvent();

        // React according to the function the minion ran
        String function = (String) jobReturnEvent.getData().get("fun");

        if (LOG.isDebugEnabled()) {
            LOG.debug("Job return event for minion: " +
                    jobReturnEvent.getMinionId() + "/" + jobReturnEvent.getJobId() +
                    " (" + function + ")");
        }

        // Adjust action status if the job was scheduled by us
        getActionId(jobReturnEvent).ifPresent(id -> {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Matched salt job with action (id=" + id + ")");
            }

            Action action = ActionFactory.lookupById(id);
            Optional<MinionServer> minionServerOpt = MinionServerFactory
                    .findByMinionId(jobReturnEvent.getMinionId());
            minionServerOpt.ifPresent(minionServer -> {
                Optional<ServerAction> serverAction = action.getServerActions().stream()
                        .filter(sa -> sa.getServer().equals(minionServer)).findFirst();
                SaltAPIService.INSTANCE.deleteSchedule("scheduled-action-" + id,
                        new MinionList(jobReturnEvent.getMinionId()));
                serverAction.ifPresent(sa -> {
                    LOG.debug("Setting action status for server: " + minionServer.getId());

                    // TODO: Set all these correctly according to the job data
                    sa.setPickupTime(new Date());
                    sa.setStatus(ActionFactory.STATUS_COMPLETED);
                    sa.setResultMsg("FIXME");
                    sa.setResultCode(0L);
                    ActionFactory.save(action);
                });
            });
        });

        if (packagesChanged(jobReturnEvent)) {
            MinionServerFactory
                    .findByMinionId(jobReturnEvent.getMinionId())
                    .ifPresent(minionServer -> {
                MessageQueue.publish(
                        new UpdatePackageProfileEventMessage(minionServer.getId()));
            });
        }
    }

    /**
     * Find the action id corresponding to a given job return event in the job metadata.
     *
     * @param event the job return event
     * @return the corresponding action id or empty optional
     */
    @SuppressWarnings("unchecked")
    private Optional<Long> getActionId(JobReturnEvent event) {
        Map<String, Object> metadata = (Map<String, Object>) event.getData()
                .getOrDefault("metadata", Collections.EMPTY_MAP);
        Optional<Long> actionId = Optional.empty();
        Double actionIdDouble = (Double) metadata.get("suma-action-id");
        if (actionIdDouble != null) {
            actionId = Optional.ofNullable(actionIdDouble.longValue());
        }
        return actionId;
    }

    private boolean packagesChanged(JobReturnEvent event) {
        String function = (String) event.getData().get("fun");
        //TODO: add more events that change packages
        //TODO: this can be further optimized by inspecting the event content
        switch (function) {
            case "pkg.install": return true;
            case "pkg.remove": return true;
            case "state.apply": return true;
            default: return false;
        }
    }
}
