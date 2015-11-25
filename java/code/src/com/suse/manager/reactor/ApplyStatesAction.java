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
package com.suse.manager.reactor;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.suse.manager.webui.services.SaltService;
import com.suse.manager.webui.services.impl.SaltAPIService;
import com.suse.manager.webui.utils.salt.Grains;
import org.apache.log4j.Logger;

import java.util.stream.Collectors;

/**
 * Applies states to a server
 */
public class ApplyStatesAction implements MessageAction {

    private static final Logger LOG = Logger.getLogger(ApplyStatesAction.class);
    private final SaltService SALT_SERVICE;

    /**
     * Default constructor.
     */
    public ApplyStatesAction() {
        SALT_SERVICE = SaltAPIService.INSTANCE;
    }

    /**
     * Constructor taking a {@link SaltService} instance.
     *
     * @param saltService the salt service to use
     */
    public ApplyStatesAction(SaltService saltService) {
        SALT_SERVICE = saltService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(EventMessage event) {
        StateDirtyEvent stateDirtyEvent = ((StateDirtyEvent) event);
        Server server = ServerFactory.lookupById(stateDirtyEvent.getServerId());

        // Update state only for salt systems
        if (server.hasEntitlement(EntitlementManager.SALTSTACK)) {
            try {
                SALT_SERVICE.applyState(
                        new Grains("machine_id", server.getDigitalServerId()),
                        stateDirtyEvent.getStateNames());
            }
            catch (Exception e) {
                LOG.error(String.format(
                        "Applying states %s on serverId '%s' failed.",
                        stateDirtyEvent.getStateNames().stream()
                                .collect(Collectors.joining(", ")),
                        server.getId()), e);
            }
        }
    }
}
