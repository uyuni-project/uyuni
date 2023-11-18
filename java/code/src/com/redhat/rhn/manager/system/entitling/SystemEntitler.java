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

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.common.validator.ValidatorResult;
import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.VirtualInstanceManager;

import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.pillar.MinionPillarManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * Class for adding entitlements to servers
 */
public class SystemEntitler {

    private static final Logger LOG = LogManager.getLogger(SystemEntitler.class);

    private SaltApi saltApi;
    private VirtManager virtManager;
    private MonitoringManager monitoringManager;
    private ServerGroupManager serverGroupManager;

    /**
     * @param saltApiIn instance for gathering data from a system.
     * @param virtManagerIn instance for managing virtual machines.
     * @param monitoringManagerIn instance for handling monitoring configuration.
     * @param serverGroupManagerIn
     */
    public SystemEntitler(SaltApi saltApiIn, VirtManager virtManagerIn,
            MonitoringManager monitoringManagerIn, ServerGroupManager serverGroupManagerIn) {
        this.saltApi = saltApiIn;
        this.virtManager = virtManagerIn;
        this.monitoringManager = monitoringManagerIn;
        this.serverGroupManager = serverGroupManagerIn;
    }

    /**
     * Checks whether or not a given server can be entitled with a specific entitlement
     * @param server The server in question
     * @param ent The entitlement to test
     * @return Returns true or false depending on whether or not the server can be
     * entitled to the passed in entitlement.
     */
    public boolean canEntitleServer(Server server, Entitlement ent) {
        return findServerGroupToEntitleServer(server, ent).isPresent();
    }

    /**
     * Entitles the given server to the given Entitlement.
     * @param server Server to be entitled.
     * @param ent Level of Entitlement.
     * @return ValidatorResult of errors and warnings.
     */
    public ValidatorResult addEntitlementToServer(Server server, Entitlement ent) {
        LOG.debug("Entitling: {}", ent.getLabel());
        ValidatorResult result = new ValidatorResult();

        if (server.hasEntitlement(ent)) {
            LOG.debug("server already entitled.");
            result.addError(new ValidatorError("system.entitle.alreadyentitled",
                    ent.getHumanReadableLabel()));
            return result;
        }

        boolean wasVirtEntitled = server.hasEntitlement(EntitlementManager.VIRTUALIZATION);
        if (EntitlementManager.VIRTUALIZATION.equals(ent)) {
            if (server.isVirtualGuest()) {
                result.addError(new ValidatorError("system.entitle.guestcantvirt"));
                return result;
            }
            // no special installed package required
        }
        else if (EntitlementManager.OSIMAGE_BUILD_HOST.equals(ent)) {
            saltApi.generateSSHKey(SaltSSHService.SSH_KEY_PATH, SaltSSHService.SUMA_SSH_PUB_KEY);
        }

        entitleServer(server, ent);

        server.asMinionServer().ifPresent(minion -> {
            serverGroupManager.updatePillarAfterGroupUpdateForServers(Arrays.asList(minion));

            if (wasVirtEntitled && !EntitlementManager.VIRTUALIZATION.equals(ent) ||
                    !wasVirtEntitled && EntitlementManager.VIRTUALIZATION.equals(ent)) {
                this.updateLibvirtEngine(minion);
                MinionPillarManager.INSTANCE.generatePillar(minion, false,
                    MinionPillarManager.PillarSubset.VIRTUALIZATION);
            }

            if (EntitlementManager.MONITORING.equals(ent)) {
                try {
                    monitoringManager.enableMonitoring(minion);
                }
                catch (ValidatorException e) {
                    LOG.error("Error enabling monitoring: {}", e.getMessage(), e);
                    result.addError(new ValidatorError("system.entitle.formula_error"));
                }
            }
        });

        LOG.debug("done.  returning null");
        return result;
    }

    private void entitleServer(Server server, Entitlement ent) {
        Optional<ServerGroup> serverGroup = findServerGroupToEntitleServer(server, ent);

        if (serverGroup.isPresent()) {
            ServerFactory.addServerHistoryWithEntitlementEvent(server, ent, "added system entitlement ");
            ServerFactory.addServerToGroup(server, serverGroup.get());
        }
        else {
            LOG.error("Cannot add entitlement: {} to system: {}", ent.getLabel(), server.getId());
        }
    }

    private Optional<ServerGroup> findServerGroupToEntitleServer(Server server, Entitlement ent) {
        Set<Entitlement> entitlements = server.getEntitlements();

        if (entitlements.isEmpty()) {
            return findServerGroupToEntitleAnUnentitledServer(server.getId(), ent);
        }
        return findServerGroupToEntitleAnEntitledServer(server, ent);
    }

    private Optional<ServerGroup> findServerGroupToEntitleAnUnentitledServer(Long serverId, Entitlement ent) {
        if (ent.isBase()) {
            Optional<ServerGroup> serverGroup = ServerGroupFactory
                    .findCompatibleServerGroupForBaseEntitlement(serverId, ent);
            if (!serverGroup.isPresent()) {
                LOG.warn("Could not find a compatible ServerGroup for base entitlement: {}, and server: {}",
                        ent.getLabel(), serverId);
            }
            return serverGroup;
        }
        return Optional.empty();
    }

    private Optional<ServerGroup> findServerGroupToEntitleAnEntitledServer(Server server, Entitlement ent) {
        if (ent.isBase()) {
            LOG.warn("Cannot set a base entitlement: {} as an addon entitlement for server: {}",
                    ent.getLabel(), server.getId());
            return Optional.empty();
        }

        Optional<Long> baseEntitlementId = server.getBaseEntitlementId();

        if (baseEntitlementId.isEmpty()) {
            LOG.warn("Cannot set a entitlement: {} as an addon entitlement for server: {}. The server has no base" +
                    " entitlement yet.", ent.getLabel(), server.getId());
            return Optional.empty();
        }

        Optional<ServerGroup> serverGroup = ServerGroupFactory
                .findCompatibleServerGroupForAddonEntitlement(server.getId(), ent, baseEntitlementId.get());
        if (!serverGroup.isPresent()) {
            LOG.warn("Cannot set a entitlement: {} as an addon entitlement for server: {}. The server base" +
                    " entitlement is not compatible.", ent.getLabel(), server.getId());
        }
        return serverGroup;
    }

    private void updateLibvirtEngine(MinionServer minion) {
        VirtualInstanceManager.updateHostVirtualInstance(minion,
                VirtualInstanceFactory.getInstance().getFullyVirtType());
        virtManager.updateLibvirtEngine(minion);
    }
}
