/*
 * Copyright (c) 2018 SUSE LLC
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

import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.salt.netapi.exception.SaltException;

import org.apache.log4j.Logger;


/**
 * Handler class for {@link SystemIdGenerateEventMessage}.
 */
public class SystemIdGenerateEventMessageAction implements MessageAction {

    /* Logger for this class */
    private static final Logger LOG = Logger.getLogger(SystemIdGenerateEventMessageAction.class);

    private static final String EVENT_TAG = "suse/systemid/generated";

    private final SystemQuery systemQuery;

    /**
     * @param systemQueryIn instance for getting information from a system.
     */
    public SystemIdGenerateEventMessageAction(SystemQuery systemQueryIn) {
        this.systemQuery = systemQueryIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(EventMessage msg) {
        String minionId = ((SystemIdGenerateEventMessage) msg).getMinionId();
        MinionServerFactory.findByMinionId(minionId).ifPresent(minion -> {
            try {
                systemQuery.notifySystemIdGenerated(minion);
            }
            catch (InstantiationException e) {
                LOG.warn(String.format("Unable to generate certificate: : %s", minionId));
            }
            catch (SaltException e) {
                LOG.warn(String.format("Unable to call event.fire for %s: %s", minionId, e.getMessage()));
            }
        });
    }
}
