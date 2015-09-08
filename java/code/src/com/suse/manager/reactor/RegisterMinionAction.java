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
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerInfo;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;

import com.suse.manager.webui.models.MinionsModel;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Map;

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
        String minionId = event.getMinionId();

        // Match the minion via its machine_id
        String machineId = MinionsModel.getInstance().getMachineId(minionId);
        if (ServerFactory.findRegisteredMinion(machineId) != null) {
            if (log.isDebugEnabled()) {
                log.debug("Minion already registered, skipping registration: " +
                        minionId + " [" + machineId + "]");
            }
            return;
        }
        try {
            // Create the server
            Server server = ServerFactory.createServer();
            server.setName(minionId);
            server.setDigitalServerId(machineId);

            // All registered minions initially belong to the default organization
            server.setOrg(OrgFactory.getSatelliteOrg());

            // TODO: Set complete OS, hardware and network information here
            Map<String, Object> grains = MinionsModel.getInstance().grains(minionId);
            server.setOs((String) grains.get("osfullname"));
            server.setRelease((String) grains.get("osrelease"));
            server.setRunningKernel((String) grains.get("kernelrelease"));
            server.setSecret(RandomStringUtils.randomAlphanumeric(64));
            server.setAutoUpdate("N");
            server.setLastBoot(System.currentTimeMillis()/1000);
            server.setCreated(new Date());
            server.setModified(server.getCreated());
            server.setContactMethod(ServerFactory.findContactMethodByLabel("default"));
            server.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
            ServerInfo serverInfo = new ServerInfo();
            serverInfo.setServer(server);
            server.setServerInfo(serverInfo);
            server.setRam(((Double) grains.get("mem_total")).longValue());
            ServerFactory.save(server);
        }
        catch (Throwable t) {
            log.error("Error registering minion for event: " + event, t);
        }
    }
}
