/**
 * Copyright (c) 2015 SUSE LLC
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
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;
import com.redhat.rhn.manager.errata.ErrataManager;

import com.suse.manager.webui.services.SaltStateGeneratorService;

/**
 * Handle changes of channel assignments on minions: trigger a refresh of the errata cache,
 * regenerate pillar data and propagate the changes to the minion via state application.
 */
public class ChannelsChangedEventMessageAction extends AbstractDatabaseAction {

    @Override
    protected void doExecute(EventMessage event) {
        long serverId = ((ChannelsChangedEventMessage) event).getServerId();

        // This message action acts only on salt minions
        MinionServerFactory.lookupById(serverId).ifPresent(minion -> {

            // Trigger update of the errata cache
            ErrataManager.insertErrataCacheTask(minion);

            // Regenerate the pillar data
            SaltStateGeneratorService.INSTANCE.generatePillar(minion);

            // Propagate changes to the minion via state.apply
            if (event.getUserId() != null) {
                MessageQueue.publish(new ApplyStatesEventMessage(serverId,
                        event.getUserId(), ApplyStatesEventMessage.CHANNELS)
                );
            }
            else {
                MessageQueue.publish(new ApplyStatesEventMessage(
                        serverId, ApplyStatesEventMessage.CHANNELS)
                );
            }
        });
    }
}
