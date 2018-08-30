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
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.manager.system.VirtualInstanceManager;

/**
 * Virtpoller Beacon Event Action Handler
 */
public class VirtpollerBeaconEventMessageAction implements MessageAction {

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(EventMessage msg) {
        VirtpollerBeaconEventMessage vMsg = (VirtpollerBeaconEventMessage) msg;

        MinionServerFactory.findByMinionId(vMsg.getMinionId()).ifPresent(minion -> {
            VirtualInstanceManager.updateHostVirtualInstance(minion,
                    VirtualInstanceFactory.getInstance().getFullyVirtType());
            VirtualInstanceManager.updateGuestsVirtualInstances(minion,
                    vMsg.getVirtpollerData().getPlan());
        });
    }
}
