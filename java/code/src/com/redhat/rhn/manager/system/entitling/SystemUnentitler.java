/*
 * Copyright (c) 2020 SUSE LLC
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

package com.redhat.rhn.manager.system.entitling;

import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.domain.server.EntitlementServerGroup;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.ServerGroupManager;

import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.pillar.MinionPillarManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * Class for removing entitlements from servers
 */
public class SystemUnentitler {

    private static final Logger LOG = LogManager.getLogger(SystemUnentitler.class);

    private final MonitoringManager monitoringManager;
    private final VirtManager virtManager;
    private final ServerGroupManager serverGroupManager;

    /**
     * @param virtManagerIn instance for managing virtual machines.
     * @param monitoringManagerIn instance for handling monitoring configuration.
     * @param serverGroupManagerIn
     */
    public SystemUnentitler(VirtManager virtManagerIn, MonitoringManager monitoringManagerIn,
                            ServerGroupManager serverGroupManagerIn) {
        this.virtManager = virtManagerIn;
        this.monitoringManager = monitoringManagerIn;
        this.serverGroupManager = serverGroupManagerIn;
    }

    /**
     * Removes all the entitlements related to a server.
     * @param server server to be unentitled.
     */
    public void removeAllServerEntitlements(Server server) {
        Set<Entitlement> entitlements = server.getEntitlements();
        entitlements.stream().forEach(e -> unentitleServer(server, e));
    }

    /**
     * Removes an entitlement from the given Server. If the given entitlement is the base entitlement,
     * removes all entitlements from the Server.
     * @param server the server
     * @param ent the entitlement
     */
    public void removeServerEntitlement(Server server, Entitlement ent) {
        if (!server.hasEntitlement(ent)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("server doesnt have entitlement: " + ent);
            }
            return;
        }

        if (ent.isBase()) {
            removeAllServerEntitlements(server);
        }
        else {
            unentitleServer(server, ent);
        }

        server.asMinionServer().ifPresent(s -> {
            serverGroupManager.updatePillarAfterGroupUpdateForServers(Arrays.asList(s));
            if (EntitlementManager.MONITORING.equals(ent)) {
                try {
                    monitoringManager.disableMonitoring(s);
                }
                catch (ValidatorException | IOException e) {
                    LOG.warn("Error disabling monitoring: " + e.getMessage());
                }
            }

            if (EntitlementManager.VIRTUALIZATION.equals(ent)) {
                virtManager.updateLibvirtEngine(s);
                MinionPillarManager.INSTANCE.generatePillar(s, false,
                    MinionPillarManager.PillarSubset.VIRTUALIZATION);
            }
        });
    }

    private void unentitleServer(Server server, Entitlement ent) {
        Optional<EntitlementServerGroup> entitlementServerGroup = server.findServerGroupByEntitlement(ent);

        if (entitlementServerGroup.isPresent()) {
            ServerFactory.addServerHistoryWithEntitlementEvent(server, ent, "removed system entitlement ");
            ServerFactory.removeServerFromGroup(server, entitlementServerGroup.get());
        }
        else {
            LOG.error("Cannot remove entitlement: " + ent.getLabel() + " from system: " + server.getId());
        }
    }

}
