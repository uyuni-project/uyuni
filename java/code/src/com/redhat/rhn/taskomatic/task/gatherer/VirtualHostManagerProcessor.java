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

package com.redhat.rhn.taskomatic.task.gatherer;

import com.redhat.rhn.domain.server.CPU;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.server.VirtualInstanceType;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerNodeInfo;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.VirtualInstanceManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.gatherer.JSONHost;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Logic for processing Virtual Host Managers based on the gatherer output.
 * Handles mapping of virtual systems.
 *
 * Based on rhnVirtualization.py logic.
 */
public class VirtualHostManagerProcessor {

    private final VirtualHostManager virtualHostManager;
    private final Map<String, JSONHost> virtualHosts;
    private Set<Server> serversToDelete;
    private Set<VirtualHostManagerNodeInfo> nodesToDelete;
    private Logger log;

    /**
     * Instantiates a new virtual host manager processor, will update a virtual
     * host manager with information coming from a JSONHost.
     *
     * @param managerIn the virtual host manager
     * @param virtualHostsIn the virtual hosts information from JSON
     */
    public VirtualHostManagerProcessor(VirtualHostManager managerIn,
            Map<String, JSONHost> virtualHostsIn) {
        this.log = Logger.getLogger(VirtualHostManagerProcessor.class);
        this.virtualHostManager = managerIn;
        this.virtualHosts = virtualHostsIn;
        this.serversToDelete = new HashSet<>();
        this.nodesToDelete = new HashSet<>();
    }

    /**
     * Process given map of Virtual Hosts for Virtual Host Manager.
     * (Mimics the logic of some handler methods from rhnVirtualization.py)
     *
     * Goes through the map of Virtual Hosts, for each of calls method for updating
     * mapping.
     */
    public void processMapping() {
        log.debug("Processing Virtual Host Manager: " + virtualHostManager);
        if (virtualHosts == null) {
            log.error("Virtual Host Manager " + virtualHostManager.getLabel() +
                      ": Please check the virtual-host-gatherer logfile.");
            return;
        }
        serversToDelete.addAll(virtualHostManager.getServers());
        nodesToDelete.addAll(virtualHostManager.getNodes());
        virtualHosts.entrySet().forEach(
                virtualHost -> {
                    log.debug("Processing host: " + virtualHost.getKey());
                    processVirtualHost(virtualHost.getKey(), virtualHost.getValue());
                });
        serversToDelete.forEach(srv -> {
            log.debug("Removing link to virtual host: " + srv.getName());
            virtualHostManager.removeServer(srv);
        });
        nodesToDelete.forEach(node -> {
            log.debug("Removing virtual host node: " + node.getName());
            virtualHostManager.removeNode(node);
        });
    }

    /**
     * Processes Virtual Host:
     * - if there is no Server entry for given hostLabel, create a new Server
     * - if no server was created (e.f. for Kubernetes) then create a nodeInfo
     * - for Server, update mapping between this new Server and its VirtualInstance
     * - for each VM (guest) reported to be running on this host, update the mapping
     *
     * @param hostLabel name of the Server (corresponds to label of Virtual Host Manager)
     * @param jsonHost object containing the information about the host and its VMs
     */
    private void processVirtualHost(String hostLabel, JSONHost jsonHost) {
        Server server = updateAndGetServer(hostLabel, jsonHost,
                VirtualHostManagerFactory.KUBERNETES);
        if (server == null) {
            VirtualHostManagerNodeInfo nodeInfo = updateAndGetNodeInfo(hostLabel, jsonHost);
            if (!virtualHostManager.getNodes().contains(nodeInfo)) {
                virtualHostManager.getNodes().add(nodeInfo);
            }
            else {
                nodesToDelete.remove(nodeInfo);
            }
            // for Kubernetes we don't create a foreign entitled server
            // if one doesn't already exist
            return;
        }
        if (!virtualHostManager.getServers().contains(server)) {
            virtualHostManager.addServer(server);
        }
        else {
            serversToDelete.remove(server);
        }

        VirtualInstanceType virtType = extractVirtualInstanceType(jsonHost.getType());
        VirtualInstanceManager.updateHostVirtualInstance(server, virtType);
        VirtualInstanceManager.updateGuestsVirtualInstances(server, virtType,
                jsonHost.getVms(), jsonHost.getOptionalVmData());
    }

    private VirtualHostManagerNodeInfo updateAndGetNodeInfo(String hostLabel,
                                                            JSONHost jsonHost) {
        return VirtualHostManagerFactory.getInstance()
                .lookupNodeInfoByIdentifier(jsonHost.getHostIdentifier())
                .map(i -> updateNodeInfo(i, hostLabel, jsonHost))
                .orElse(createNewNodeInfo(hostLabel, jsonHost));
    }

    private VirtualHostManagerNodeInfo updateNodeInfo(VirtualHostManagerNodeInfo info,
            String hostLabel, JSONHost jsonHost) {
        info.setName(hostLabel);
        info.setNodeArch(ServerFactory.lookupServerArchByName(jsonHost.getCpuArch()));
        info.setCpuSockets(jsonHost.getTotalCpuSockets());
        info.setCpuCores(jsonHost.getTotalCpuCores());
        info.setRam(jsonHost.getRamMb());
        info.setOs(jsonHost.getOs());
        info.setOsVersion(jsonHost.getOsVersion());
        return info;
    }

    private VirtualHostManagerNodeInfo createNewNodeInfo(String hostLabel,
                                                         JSONHost jsonHost) {
        VirtualHostManagerNodeInfo info = new VirtualHostManagerNodeInfo();
        info.setIdentifier(jsonHost.getHostIdentifier());
        return updateNodeInfo(info, hostLabel, jsonHost);
    }

    /**
     * Extracts virtual instance type from string. Falls back to para virtualization if the
     * requested string doesn't match to any existing virtualization type.

     * @param candidate - source string
     * @return - VirtualInstanceType corresponding to source string
     */
    private VirtualInstanceType extractVirtualInstanceType(String candidate) {
        VirtualInstanceType type =
                VirtualInstanceFactory.getInstance().getVirtualInstanceType(candidate);
        if (type == null) { // fallback
            type = VirtualInstanceFactory.getInstance().getParaVirtType();
            log.warn(String.format("Can't find virtual instance type for string '%s'. " +
                    "Defaulting to '%s'", candidate, type));
        }
        return type;
    }

    /**
     * Updates server with given hostId according to data in jsonHost.
     * If such server doesn't exist, create it beforehand unless the type
     * of the server matches skipCreateForType.
     *
     * @param hostId - id of server to update
     * @param jsonHost - data for updating
     * @param skipCreateForType - don't create a new server for the given host type
     * @return the updated server
     */
    private Server updateAndGetServer(String hostId,
                                      JSONHost jsonHost,
                                      String skipCreateForType) {
        Server server = ServerFactory.lookupForeignSystemByDigitalServerId(
                buildServerFullDigitalId(jsonHost.getHostIdentifier()));
        if (server == null) {
            if (skipCreateForType.equalsIgnoreCase(jsonHost.getType())) {
                return null;
            }
            server = createNewServer(hostId, jsonHost);
        }
        else {
            updateServerMiscFields(server, jsonHost);
        }

        updateServerCpu(server, jsonHost);
        server.updateServerInfo();
        updateServerNetwork(server, hostId);

        if (server.getBaseEntitlement() == null) {
            try {
                server.setBaseEntitlement(EntitlementManager.FOREIGN);
            }
            catch (TaskomaticApiException e) {
                // never happens for foreign
            }
        }
        return server;
    }

    /**
     * Builds full digital id from virtual host manager id and the host identifier (string
     * consisting of those two separated by '-'. For instance: 1000000001-my_host_id).
     *
     * @param hostIdentifier host identifier (from gatherer)
     * @return full digital server id
     */
    private String buildServerFullDigitalId(String hostIdentifier) {
        return virtualHostManager.getId() + "-" + hostIdentifier;
    }

    private Server createNewServer(String hostId, JSONHost jsonHost) {
        Server server = ServerFactory.createServer();
        // Create the server
        server.setName(hostId);
        // All new servers belong to org of the virtualHostManager
        server.setOrg(virtualHostManager.getOrg());
        server.setCreated(new Date());
        server.setDigitalServerId(buildServerFullDigitalId(jsonHost.getHostIdentifier()));
        server.setSecret(RandomStringUtils.randomAlphanumeric(64));

        String serverDescription = "Initial Registration Parameters:\n";
        serverDescription += "OS: " + jsonHost.getOs() + "\n";
        serverDescription += "Release: " + jsonHost.getOsVersion() + "\n";
        serverDescription += "CPU Arch: " + jsonHost.getCpuArch() + "\n";
        server.setDescription(serverDescription);
        server.setAutoUpdate("N");
        server.setContactMethod(ServerFactory.findContactMethodByLabel("default"));

        // these are used in the Server.equals() method any make hibernate think
        // the server objects are different.
        // this result into inserting duplicate entries into
        // suseServerVirtualHostManager table
        server.setLastBoot(System.currentTimeMillis() / 1000);
        server.setOs(jsonHost.getOs());
        server.setRelease(jsonHost.getOsVersion());

        updateServerMiscFields(server, jsonHost);

        ServerFactory.save(server);
        return server;
    }

    private void updateServerMiscFields(Server server, JSONHost jsonHost) {
        // fields that need to be updated on both create (before ServerFactory.save)
        // and update server
        server.setModified(new Date());
        server.setRam(jsonHost.getRamMb());
        server.setServerArch(ServerFactory.lookupServerArchByName(jsonHost.getCpuArch()));
    }

    private void updateServerCpu(Server server, JSONHost jsonHost) {
        CPU cpu = server.getCpu();
        if (cpu == null) {
            cpu = new CPU();
        }

        cpu.setArch(ServerFactory.lookupCPUArchByName(jsonHost.getCpuArch()));
        cpu.setMHz(new Long(Math.round(jsonHost.getCpuMhz())).toString());
        cpu.setNrCPU(jsonHost.getTotalCpuCores().longValue());
        cpu.setNrsocket(jsonHost.getTotalCpuSockets().longValue());
        cpu.setVendor(jsonHost.getCpuVendor());
        cpu.setModel(jsonHost.getCpuDescription());

        cpu.setServer(server);
        server.setCpu(cpu);
    }

    private void updateServerNetwork(Server server, String hostId) {
        server.setHostname(hostId);
    }
}
