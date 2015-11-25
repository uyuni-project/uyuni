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
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.suse.manager.webui.utils.RepoFileUtils;
import org.apache.log4j.Logger;
import org.jose4j.lang.JoseException;

import java.io.IOException;

/**
 * Generate repo files for managed systems whenever their channel assignments have changed.
 */
public class GenerateRepoFileAction implements MessageAction {

    private static final Logger LOG = Logger.getLogger(GenerateRepoFileAction.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(EventMessage event) {
        long serverId = ((ChannelChangedEvent) event).getServerId();
        Server server = ServerFactory.lookupById(serverId);

        // Generate repo files only for salt minions
        if (server.hasEntitlement(EntitlementManager.SALTSTACK)) {
            try {
                RepoFileUtils.generateRepositoryFile(server);
                MessageQueue.publish(
                        new StateDirtyEvent(serverId, event.getUserId(), "channels"));
            }
            catch (IOException | JoseException e) {
                LOG.error(String.format(
                        "Generating repo file for server with serverId '%s' failed.",
                        serverId), e);
            }
        }
    }
}
