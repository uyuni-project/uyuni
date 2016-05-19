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
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.suse.manager.webui.services.SaltStateGeneratorService;

/**
 * Generate repo files for managed systems whenever their channel assignments have changed.
 */
public class ChannelsChangedEventMessageAction implements MessageAction {

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(EventMessage event) {
        long serverId = ((ChannelsChangedEventMessage) event).getServerId();
        // Generate repo files only for salt minions
        MinionServerFactory.lookupById(serverId).ifPresent(minion -> {
            if (minion.hasEntitlement(EntitlementManager.SALT)) {
                SaltStateGeneratorService.INSTANCE.generatePillar(minion);
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
            }
        });
    }
}
