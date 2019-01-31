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
import com.redhat.rhn.domain.server.ServerFactory;

import com.suse.manager.webui.utils.salt.custom.ScheduleMetadata;
import com.suse.salt.netapi.event.BatchStartedEvent.Data;
import com.suse.utils.Opt;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Handler class for {@link BatchStartedEventMessage}.
 */
public class BatchStartedEventMessageAction implements MessageAction {

    private static final Logger LOG = Logger.getLogger(BatchStartedEventMessageAction.class);

    @Override
    public void execute(EventMessage msg) {
        Data eventData = ((BatchStartedEventMessage) msg).getBatchStartedEvent().getData();
        List<String> downMinions = eventData.getDownMinions();

        if (!downMinions.isEmpty()) {
            Optional<Long> actionId = eventData.getMetadata(ScheduleMetadata.class).map(
                    ScheduleMetadata::getSumaActionId);
            actionId.filter(id -> id > 0).ifPresent(id -> handleBatchStartedAction(id, downMinions));
        }
    }

    /**
     * Update the action properly based on the event results from Salt.
     *
     * @param actionId the ID of the Action to handle
     * @param minionId the ID of the Minion who performed the action
     */
    private static void handleBatchStartedAction(long actionId, List<String> minionIds) {
        Optional<Action> action = Optional.ofNullable(ActionFactory.lookupById(actionId));
        if (action.isPresent()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Matched salt job with action (id=" + actionId + ")");
            }
            Map<String, Long> minionServerIds = ServerFactory.findServerIdsByMinionIds(minionIds);

            Set<ServerAction> serverActions = action.get().getServerActions();

            minionServerIds.entrySet().stream().forEach(entry -> {
                Optional<ServerAction> serverAction = serverActions.stream()
                        .filter(sa -> sa.getServerId().equals(entry.getValue()))
                        .findFirst();

                serverAction.ifPresent(sa -> handleServerAction(sa, entry.getKey()));
            });
        }
        else {
            LOG.warn("Action referenced from Salt job was not found: " + actionId);
        }
    }

    /**
     * Update a given server action properly for a given minion.
     *
     * @param serverAction the server action
     * @param minionServer the minion who performed the server action
     */
    private static void handleServerAction(ServerAction sa, String minionId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Marking server action as failed for server: " + minionId);
        }
        sa.fail("Minion is down");
        ActionFactory.save(sa);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRunConcurrently() {
        return true;
    }
}
