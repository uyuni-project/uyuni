package com.redhat.rhn.taskomatic.task.gatherer;

import com.redhat.rhn.domain.server.CPU;
import com.redhat.rhn.domain.server.Network;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerInfo;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.server.VirtualInstanceType;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.suse.manager.gatherer.JSONHost;
import org.apache.commons.lang.RandomStringUtils;
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
        serversToDelete.addAll(virtualHostManager.getServers());
        virtualHosts.entrySet().forEach(
                virtualHost -> {
                    log.debug("Processing host: " + virtualHost.getKey());
                    processVirtualHost(virtualHost.getKey(), virtualHost.getValue());
                });
        serversToDelete.forEach(srv -> {
            log.debug("Removing link to virtual host: " + srv.getName());
            virtualHostManager.removeServer(srv);
        });
    }

    /**
     * Processes Virtual Host:
     * - if there is no Server entry for given hostLabel, create a new Server
     * - update mapping between this new Server and its VirtualInstance
     * - for each VM (guest) reported to be running on this host, update the mapping
     *
     * todo (long term) - add a mechanism (possibly heuristics) which decides that a host is
     * already managed by poller. if positive -> skip it here
     *
     * @param hostLabel name of the Server (corresponds to label of Virtual Host Manager)
     * @param jsonHost object containing the information about the host and its VMs
     */
    private void processVirtualHost(String hostLabel, JSONHost jsonHost) {
        Server server = getOrCreateServer(hostLabel, jsonHost);
        if (!virtualHostManager.getServers().contains(server)) {
            virtualHostManager.addServer(server);
        }
        else {
            serversToDelete.remove(server);
        }

        VirtualInstanceType virtType = extractVirtualInstanceType(jsonHost.getType());
        updateHostVirtualInstance(server, virtType);
        updateGuestsVirtualInstances(server, virtType, jsonHost.getVms());
    }

    private void updateHostVirtualInstance(Server server, VirtualInstanceType type) {
        VirtualInstance serverVirtInstance = VirtualInstanceFactory.getInstance()
                .lookupHostVirtInstanceByHostId(server.getId());

        if (serverVirtInstance == null) { // __db_insert_system logic
            serverVirtInstance = new VirtualInstance();
            serverVirtInstance.setHostSystem(server);
            serverVirtInstance.setConfirmed(1L);

            serverVirtInstance.setState(
                    VirtualInstanceFactory.getInstance().getUnknownState());
            serverVirtInstance.setType(type);

            VirtualInstanceFactory.getInstance().saveVirtualInstance(serverVirtInstance);
        }
        else if (serverVirtInstance.getConfirmed() != 1L) { // __db_update_system logic
            serverVirtInstance.setConfirmed(1L);
            serverVirtInstance.setType(type);
            VirtualInstanceFactory.getInstance().saveVirtualInstance(serverVirtInstance);
        }
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
     * Goes through all the vms(guests), creates/updates VirtualInstance entries
     * (Server - guests mapping)
     * @param server to be processed
     * @param type - virtualization type to be set to the guests
     * @param vms - guests to be mapped to this server
     */
    private void updateGuestsVirtualInstances(Server server, VirtualInstanceType type,
            Map<String, String> vms) {
        vms.entrySet().stream().forEach(
                vmEntry -> {
                    String name = vmEntry.getKey();
                    String guid = vmEntry.getValue();
                    VirtualInstance virtualInstance = VirtualInstanceFactory.getInstance()
                        .lookupVirtualInstanceByUuid(guid);

                    if (virtualInstance == null) {
                        addGuestVirtualInstance(guid, name, type, server, null);
                    }
                    else {
                        updateGuestVirtualInstance(virtualInstance, name, server);
                    }
                });
    }

    /**
     * Creates a new (guest) VirtualInstance for given VM GUID.
     * Sets given host as a host for this VirtualInstance.
     * Mimics __db_insert_domain function
     *
     * @param vmGuid - guid of the new VirtualInstance
     * @param name - name of the guest
     * @param type - virtualization type of the guest
     * @param host - host to be set as host system for the new VirtualInstance
     * @param guest - guest to be set as the guest system for the new VirtualInstance
     */
    private void addGuestVirtualInstance(String vmGuid, String name,
            VirtualInstanceType type, Server host, Server guest) {
        VirtualInstance virtualInstance = new VirtualInstance();
        virtualInstance.setUuid(vmGuid);
        virtualInstance.setConfirmed(1L);
        virtualInstance.setGuestSystem(guest);
        virtualInstance.setState(VirtualInstanceFactory.getInstance().getStoppedState());
        virtualInstance.setName(name);
        virtualInstance.setType(type);

        host.addGuest(virtualInstance); // will also set the hostSystem for virtualInstance
    }

    /**
     * Update mapping of given guest VirtualInstance to given (host) Server.
     * This method removes the old VirtualInstance and creates a new one.
     * Mimics __db_update_domain function
     *
     * @param virtualInstance
     * @param name
     * @param server
     */
    private void updateGuestVirtualInstance(VirtualInstance virtualInstance,
            String name, Server server) {
        Server oldHost = virtualInstance.getHostSystem();
        if (oldHost == null || oldHost.getId() != server.getId()) {
            VirtualInstanceFactory.getInstance().deleteVirtualInstanceOnly(virtualInstance);
            addGuestVirtualInstance(virtualInstance.getUuid(), name,
                    virtualInstance.getType(), server, virtualInstance.getGuestSystem());
        }
    }

    private Server getOrCreateServer(String hostId, JSONHost jsonHost) {
        Server server = ServerFactory.lookupForeignSystemByName(hostId);
        ServerInfo serverInfo = null;
        CPU cpu = null;
        if (server == null) {
            server = ServerFactory.createServer();
            // Create the server
            server.setName(hostId);
            // All new servers belong to org of the virtualHostManager
            server.setOrg(virtualHostManager.getOrg());
            server.setCreated(new Date());
            server.setDigitalServerId("foreign-" + RandomStringUtils.randomNumeric(32));
            server.setSecret(RandomStringUtils.randomAlphanumeric(64));

            serverInfo = new ServerInfo();
            serverInfo.setServer(server);
            server.setServerInfo(serverInfo);
            serverInfo.setCheckinCounter(0L);

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
        }
        else {
            serverInfo = server.getServerInfo();
            cpu = server.getCpu();
        }

        // mapping from JsonHost to the server entity

        server.setModified(new Date());
        server.setRam(jsonHost.getRamMb());
        server.setServerArch(ServerFactory.lookupServerArchByName(jsonHost.getCpuArch()));

        serverInfo.setCheckin(new Date());
        serverInfo.setCheckinCounter(serverInfo.getCheckinCounter() + 1);

        if (cpu == null) {
            cpu = new CPU();
        }
        cpu.setArch(ServerFactory.lookupCPUArchByName(jsonHost.getCpuArch()));
        cpu.setMHz(new Double(jsonHost.getCpuMhz()).toString());
        cpu.setNrCPU(jsonHost.getTotalCpuCores().longValue());
        cpu.setNrsocket(jsonHost.getTotalCpuSockets().longValue());
        cpu.setVendor(jsonHost.getCpuVendor());
        cpu.setModel(jsonHost.getCpuDescription());
        // todo hibernate riddle: why this crashes if it's in the block with the "new CPU()"
        cpu.setServer(server);
        server.setCpu(cpu);

        ServerFactory.save(server);

        Network n = new Network();
        n.setHostname(hostId);
        n.setServer(server);
        Set<Network> networks = new HashSet<>();
        networks.add(n);
        server.setNetworks(networks);

        if (server.getBaseEntitlement() == null) {
            server.setBaseEntitlement(EntitlementManager.FOREIGN);
        }
        return server;
    }
}
