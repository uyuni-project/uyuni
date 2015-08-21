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
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerInfo;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;

import org.apache.log4j.Logger;

import java.util.Date;

/**
 * Event handler to create system records for salt minions.
 */
public class RegisterMinionAction extends AbstractDatabaseAction {

    // Logger for this class
    private static final Logger log = Logger.getLogger(RegisterMinionAction.class);

    /**
     * {@inheritDoc}
     */
    protected void doExecute(EventMessage msg) {
        RegisterMinionEvent event = (RegisterMinionEvent) msg;
        try {
            User user = UserFactory.lookupById(event.getUserId());
            Server server = ServerFactory.createServer();
            server.setName(event.getMinionId());
            server.setOrg(user.getOrg());
            server.setOs("AwesomeOS");
            server.setRelease("Awesome Release");
            server.setRunningKernel("Awesome Kernel");
            server.setDigitalServerId(event.getMinionId());
            server.setSecret("pssst dont tell anyone");
            server.setAutoUpdate("N");
            server.setLastBoot(System.currentTimeMillis());
            server.setCreated(new Date());
            server.setModified(new Date());
            server.setContactMethod(ServerFactory.findContactMethodByLabel("default"));
            server.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
            ServerInfo serverInfo = new ServerInfo();
            serverInfo.setServer(server);
            server.setServerInfo(serverInfo);
            server.setRam(1024);
            ServerFactory.save(server);
        }
        catch (Throwable t) {
            log.error("Error registering minion for event: " + event, t);
        }
    }
}
