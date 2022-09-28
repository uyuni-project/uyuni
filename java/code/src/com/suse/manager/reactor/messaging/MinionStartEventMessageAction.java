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
import com.redhat.rhn.domain.server.MinionServerFactory;

import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.salt.netapi.datatypes.target.MinionList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Event message handler for {@link MinionStartEventMessage}.
 */
public class MinionStartEventMessageAction implements MessageAction {

    private static final Logger LOG = LogManager.getLogger(MinionStartEventMessageAction.class);

    // Reference to the SaltService instance
    private final SaltApi saltApi;

    /**
     * Constructor taking a {@link SystemQuery} instance.
     *
     * @param saltApiIn systemQuery instance for gathering data from a system.
     */
    public MinionStartEventMessageAction(SaltApi saltApiIn) {
        this.saltApi = saltApiIn;
    }

    @Override
    public void execute(EventMessage msg) {
        MinionStartEventMessage startMsg = (MinionStartEventMessage)msg;
        LOG.debug("Handle minion start event message for minion {}", startMsg.getMinionId());
        String minionId = startMsg.getMinionId();
        MinionServerFactory.findByMinionId(minionId)
                .ifPresent(minion -> {
            // Sync grains, modules and beacons, also update uptime and required grains on every minion restart
            MinionList minionTarget = new MinionList(minionId);
            saltApi.updateSystemInfo(minionTarget);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRunConcurrently() {
        return true;
    }
}
