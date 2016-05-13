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
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;
import org.apache.log4j.Logger;

/**
 * Handler class for {@link CheckinEventMessage}.
 */
public class CheckinEventMessageAction extends AbstractDatabaseAction {

    /* Logger for this class */
    private static final Logger LOG = Logger.getLogger(CheckinEventMessageAction.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void doExecute(EventMessage msg) {
        CheckinEventMessage checkinEventMessage = (CheckinEventMessage) msg;
        Server server = ServerFactory
                .lookupById(checkinEventMessage.getServerId());
        if (server != null) {
            server.updateServerInfo();
            ServerFactory.save(server);
        }
        else {
            LOG.info("Can't update checkin time of non-existing server: " +
                    checkinEventMessage.getServerId());
        }
    }

    @Override
    public boolean canRunConcurrently() {
        return true;
    }
}
