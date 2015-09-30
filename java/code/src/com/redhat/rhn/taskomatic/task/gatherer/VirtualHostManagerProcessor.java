package com.redhat.rhn.taskomatic.task.gatherer;

import com.redhat.rhn.domain.common.LoggingFactory;
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

import com.suse.manager.gatherer.JsonHost;

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
    private final Map<String, JsonHost> virtualHosts;
    private Set<Server> serversToDelete;
    private Logger log;

    public VirtualHostManagerProcessor(VirtualHostManager managerIn,
            Map<String, JsonHost> virtualHostsIn) {
        this.log = Logger.getLogger(VirtualHostManagerProcessor.class);
        this.virtualHostManager = managerIn;
        this.virtualHosts = virtualHostsIn;
        this.serversToDelete = new HashSet<>();
    }

    /**
     * Process given map of Virtual Hosts for Virtual Host Manager.
     * (Mimicks the logic of some handler methods from rhnVirtualization.py)
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
     * todo - corner case - user specifies esxi host and then they add vcenter with this
     *        host. result -> esxi host will be processed twice
     * todo (long term) - add a mechanism (possibly heuristics) which decides that a host is
     * already managed by poller. if positive -> skip it here
     *
     * @param hostLabel - name of the Server (corresponds to label of Virtual Host Manager)
     * @param jsonHost - object containing the information about the host and its VMs
     */
    private void processVirtualHost(String hostLabel, JsonHost jsonHost) {
        Server server = getOrCreateServer(hostLabel, jsonHost);
        if (! virtualHostManager.getServers().contains(server)) {
            virtualHostManager.addServer(server);
        }
        else {
            serversToDelete.remove(server);
        }

        updateHostVirtualInstance(server, jsonHost.getType());
        updateGuestsVirtualInstances(server, jsonHost.getVms());
    }

    private void updateHostVirtualInstance(Server server, String htype) {
        VirtualInstance serverVirtInstance = VirtualInstanceFactory.getInstance()
                .lookupHostVirtInstanceByHostId(server.getId());

        if (serverVirtInstance == null) { // __db_insert_system logic
            serverVirtInstance = new VirtualInstance();
            serverVirtInstance.setHostSystem(server);
            serverVirtInstance.setConfirmed(1L);

            serverVirtInstance.setState(
                    VirtualInstanceFactory.getInstance().getUnknownState());
            setVirtualInstanceType(htype, serverVirtInstance);

            VirtualInstanceFactory.getInstance().saveVirtualInstance(serverVirtInstance);
        } else if (serverVirtInstance.getConfirmed() != 1L) { // __db_update_system logic
            serverVirtInstance.setConfirmed(1L);
            setVirtualInstanceType(htype, serverVirtInstance);
            VirtualInstanceFactory.getInstance().saveVirtualInstance(serverVirtInstance);
        }
    }

    private void setVirtualInstanceType(String htype, VirtualInstance serverVirtInstance) {
        VirtualInstanceType type =
                VirtualInstanceFactory.getInstance().getVirtualInstanceType(htype);
        if (type == null) { // fallback
            type = VirtualInstanceFactory.getInstance().getParaVirtType();
            log.warn(String.format("Can't find virtual instance type for string '%s'. " +
                    "Defaulting to '%s'", htype, type));
        }
        serverVirtInstance.setType(type);
    }

    /**
     * Goes through all the vms(guests), creates/updates VirtualInstance entries
     * (Server - guests mapping)
     * todo fill virtualization type (and possibly state)
     * @param server to be processed
     * @param vms - guests to be mapped to this server
     */
    private void updateGuestsVirtualInstances(Server server, Map<String, String> vms) {
        vms.entrySet().stream().forEach(
                vmEntry -> {
                    String name = vmEntry.getKey();
                    String guid = vmEntry.getValue();
                    VirtualInstance virtualInstance = VirtualInstanceFactory.getInstance()
                            .lookupVirtualInstanceByUuid(guid);

                    if (virtualInstance == null) {
                        addGuestVirtualInstance(guid, name, server);
                    } else {
                        updateGuestVirtualInstance(virtualInstance, name, server);
                    }
                });
    }

    /**
     * Creates a new (guest) VirtualInstance for given VM GUID.
     * Sets given server as a host for this VirtualInstance.
     * Mimicks __db_insert_domain function
     *
     * @param vmGuid - guid of the new VirtualInstance
     * @param name
     * @param server - server to be set as host for the new VirtualInstance
     */
    private void addGuestVirtualInstance(String vmGuid, String name, Server server) {
        VirtualInstance newVm = new VirtualInstance();
        newVm.setState(VirtualInstanceFactory.getInstance().getStoppedState());
        newVm.setName(name);
        newVm.setHostSystem(server);
        newVm.setGuestSystem(null);
        newVm.setUuid(vmGuid);
        newVm.setConfirmed(1L);

        VirtualInstanceFactory.getInstance().saveVirtualInstance(newVm);

        server.addGuest(newVm);
        ServerFactory.save(server);
    }

    /**
     * Update mapping of given guest VirtualInstance to given (host) Server.
     * Mimicks __db_update_domain function
     *
     * @param virtualInstance
     * @param name
     * @param server
     */
    private void updateGuestVirtualInstance(VirtualInstance virtualInstance,
            String name, Server server) {
        Server oldSystem = virtualInstance.getHostSystem();
        if (oldSystem == null || oldSystem.getId() != server.getId()) {
            if (oldSystem != null) {
                oldSystem.removeGuest(virtualInstance);
            }
            virtualInstance.setState(VirtualInstanceFactory.getInstance().getStoppedState());
            virtualInstance.setName(name);
            virtualInstance.setHostSystem(server);
            virtualInstance.setConfirmed(1L);
            // after hostSystem.removeGuest, virtualInstance is not in hibernate session
            // let's add it
            VirtualInstanceFactory.getInstance().saveVirtualInstance(virtualInstance);

            server.addGuest(virtualInstance);
        }
    }

    private Server getOrCreateServer(String hostId, JsonHost jsonHost) {
        LoggingFactory.clearLogId();
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
