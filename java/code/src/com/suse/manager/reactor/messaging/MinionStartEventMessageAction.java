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
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.utils.MinionServerUtils;
import com.suse.salt.netapi.datatypes.target.MinionList;

import org.apache.log4j.Logger;

/**
 * Event message handler for {@link MinionStartEventMessage}.
 */
public class MinionStartEventMessageAction extends AbstractDatabaseAction {

    /* Logger for this class */
    private static final Logger LOG = Logger.getLogger(MinionStartEventMessageAction.class);

    // Reference to the SaltService instance
    private final SaltService SALT_SERVICE;

    /**
     * Default constructor.
     */
    public MinionStartEventMessageAction() {
        this(SaltService.INSTANCE);
    }

    /**
     * Constructor taking a {@link SaltService} instance.
     *
     * @param saltService the salt service to use
     */
    public MinionStartEventMessageAction(SaltService saltService) {
        SALT_SERVICE = saltService;
    }

    @Override
    protected void doExecute(EventMessage msg) {
        String minionId = ((MinionStartEventMessage) msg).getMinionId();
        MinionServerFactory.findByMinionId(minionId)
                .ifPresent(minion -> {
            // Update custom grains, modules and beacons on every minion restart
            MinionList minionTarget = new MinionList(minionId);
            SALT_SERVICE.syncGrains(minionTarget);
            SALT_SERVICE.syncBeacons(minionTarget);

            if (!MinionServerUtils.isSshPushMinion(minion)) {
                SALT_SERVICE.syncModules(minionTarget);
            }

            SALT_SERVICE.getUptimeForMinion(minion).ifPresent(uptime ->
                    SaltUtils.INSTANCE.handleUptimeUpdate(minion, uptime));
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
