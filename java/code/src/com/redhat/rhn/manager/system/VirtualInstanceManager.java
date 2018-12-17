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
package com.redhat.rhn.manager.system;

import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.server.VirtualInstanceState;
import com.redhat.rhn.domain.server.VirtualInstanceType;
import com.redhat.rhn.manager.BaseManager;

import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.utils.salt.custom.VmInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

/**
 * VirtualInstanceManager
 */
public class VirtualInstanceManager extends BaseManager {

    private static final String EVENT_TYPE_FULLREPORT = "fullreport";
    private static final String EVENT_TYPE_EXISTS = "exists";
    private static final String EVENT_TYPE_REMOVED = "removed";

    /**
     * Logger for this class
     */
    private static Logger log = Logger
            .getLogger(VirtualInstanceManager.class);

    private VirtualInstanceManager() {
    }

    /**
     * Update Virtual Instance of type host
     *
     * @param server the server
     * @param type the virtual instance type
     */
    public static void updateHostVirtualInstance(Server server, VirtualInstanceType type) {
        VirtualInstance serverVirtInstance = VirtualInstanceFactory.getInstance()
                .lookupHostVirtInstanceByHostId(server.getId());

        if (serverVirtInstance == null) {
            serverVirtInstance = new VirtualInstance();
            serverVirtInstance.setHostSystem(server);
            serverVirtInstance.setConfirmed(1L);

            serverVirtInstance.setState(
                    VirtualInstanceFactory.getInstance().getUnknownState());
            serverVirtInstance.setType(type);

            VirtualInstanceFactory.getInstance().saveVirtualInstance(serverVirtInstance);
        }
        else if (serverVirtInstance.getConfirmed() != 1L) {
            serverVirtInstance.setConfirmed(1L);
            VirtualInstanceFactory.getInstance().saveVirtualInstance(serverVirtInstance);
        }
    }

    /**
     * Goes through the plan, creates/updates/removes VirtualInstance entries
     * (Server - guests mapping)
     * @param server to be processed
     * @param plan - list of actions for guests mapped to this server
     */
    public static void updateGuestsVirtualInstances(Server server, List<VmInfo> plan) {
        VirtualInstanceFactory vinst = VirtualInstanceFactory.getInstance();
        List<String> uuidsToRemove = new LinkedList<>();
        for (VmInfo info : plan) {
            if (info.getEventType().equals(EVENT_TYPE_FULLREPORT)) {
                uuidsToRemove = server.getGuests().stream().map(g -> g.getUuid())
                        .collect(Collectors.toList());
                continue;
            }
            String name = info.getGuestProperties().getName();
            String uuid = info.getGuestProperties().getUuid().replace("-", "");
            int vCpus = info.getGuestProperties().getVcpus();
            long memory = info.getGuestProperties().getMemorySize();

            uuid = fixUuidIfSwappedUuidExists(uuid);
            uuidsToRemove.remove(uuid);
            VirtualInstanceType type = vinst.getVirtualInstanceType(
                    info.getGuestProperties().getVirtType());
            if (type == null) { // fallback
                type = vinst.getParaVirtType();
            }
            VirtualInstanceState state = vinst.getState(
                    info.getGuestProperties().getState())
                    .orElseGet(vinst::getUnknownState);

            List<VirtualInstance> virtualInstances =
                    vinst.lookupVirtualInstanceByUuid(uuid);

            if (virtualInstances.isEmpty() && info.getEventType()
                    .equals(EVENT_TYPE_EXISTS)) {
                addGuestVirtualInstance(uuid, name, type, state, server, null,
                        vCpus, memory);
            }
            else if (info.getEventType().equals(EVENT_TYPE_EXISTS)) {
                virtualInstances.stream().forEach(virtualInstance ->
                updateGuestVirtualInstance(virtualInstance, name, state, server,
                        virtualInstance.getGuestSystem(), vCpus, memory));
            }
            else if (info.getEventType().equals(EVENT_TYPE_REMOVED)) {
                virtualInstances.stream().forEach(virtualInstance ->
                vinst.deleteVirtualInstanceOnly(virtualInstance));
            }
        }

        for (String uuid : uuidsToRemove) {
            List<VirtualInstance> virtualInstances =
                    vinst.lookupVirtualInstanceByUuid(uuid);
            virtualInstances.stream().forEach(virtualInstance -> {
                deleteGuestVirtualInstance(virtualInstance);
            });
        }
    }

    /**
     * Goes through all the vms(guests), creates/updates VirtualInstance entries
     * (Server - guests mapping)
     * This function expect to always get a full list of guests running on the host
     *
     * @param server to be processed
     * @param type - virtualization type to be set to the guests
     * @param vms - guests to be mapped to this server
     * @param optionalVmData - guests optional data
     */
    public static void updateGuestsVirtualInstances(Server server, VirtualInstanceType type,
            Map<String, String> vms, Map<String, Map<String, String>> optionalVmData) {
        VirtualInstanceFactory vinst = VirtualInstanceFactory.getInstance();
        List<String> uuidsToRemove = server.getGuests().stream().map(g -> g.getUuid())
                .collect(Collectors.toList());
        vms.entrySet().stream().forEach(
                vmEntry -> {
                    String name = vmEntry.getKey();
                    String guid = vmEntry.getValue().replaceAll("-", "");

                    guid = fixUuidIfSwappedUuidExists(guid);
                    uuidsToRemove.remove(guid);
                    List<VirtualInstance> virtualInstances =
                            vinst.lookupVirtualInstanceByUuid(guid);

                    Map<String, String> vmData = optionalVmData.get(name);
                    VirtualInstanceState st = (vmData != null && vmData.get("vmState") != null) ?
                            vinst.getState(vmData.get("vmState"))
                                    .orElse(vinst.getUnknownState()) : vinst.getUnknownState();

                    if (virtualInstances.isEmpty()) {
                        addGuestVirtualInstance(guid, name, type, st, server, null);
                    }
                    else {
                        virtualInstances.stream().forEach(virtualInstance ->
                            updateGuestVirtualInstance(virtualInstance, name, st, server,
                                    virtualInstance.getGuestSystem()));
                    }
                });

        for (String uuid : uuidsToRemove) {
            List<VirtualInstance> virtualInstances =
                    vinst.lookupVirtualInstanceByUuid(uuid);
            virtualInstances.stream().forEach(virtualInstance -> {
                deleteGuestVirtualInstance(virtualInstance);
            });
        }
    }

    /**
     * Remove a virtual instance from the database
     *
     * @param virtualInstance guest to remove
     */
    public static void deleteGuestVirtualInstance(VirtualInstance virtualInstance) {
        if (virtualInstance.isRegisteredGuest()) {
            virtualInstance.getHostSystem().removeGuest(virtualInstance);
            virtualInstance.setHostSystem(null);
        }
        else {
            VirtualInstanceFactory.getInstance().deleteVirtualInstanceOnly(virtualInstance);
        }
    }

    /**
     * Creates a new (guest) VirtualInstance for given VM GUID.
     * Sets given host as a host for this VirtualInstance.
     *
     * @param vmGuid - guid of the new VirtualInstance
     * @param name - name of the guest
     * @param type - virtualization type of the guest
     * @param state - guest state
     * @param host - host to be set as host system for the new VirtualInstance
     * @param guest - guest to be set as the guest system for the new VirtualInstance
     */
    public static void addGuestVirtualInstance(String vmGuid, String name,
            VirtualInstanceType type, VirtualInstanceState state,
            Server host, Server guest) {
        addGuestVirtualInstance(vmGuid, name, type, state, host, guest, 0, 0);
    }

    /**
     * Creates a new (guest) VirtualInstance for given VM GUID.
     * Sets given host as a host for this VirtualInstance.
     *
     * @param vmGuid - guid of the new VirtualInstance
     * @param name - name of the guest
     * @param type - virtualization type of the guest
     * @param state - guest state
     * @param host - host to be set as host system for the new VirtualInstance
     * @param guest - guest to be set as the guest system for the new VirtualInstance
     * @param vCpus - number of CPUs
     * @param memory - total Memory
     */
    public static void addGuestVirtualInstance(String vmGuid, String name,
            VirtualInstanceType type, VirtualInstanceState state,
            Server host, Server guest, int vCpus, long memory) {

        List<VirtualInstance> virtualInstances = VirtualInstanceFactory
                .getInstance().lookupVirtualInstanceByUuid(vmGuid);

        if (virtualInstances.isEmpty()) {
            VirtualInstance virtualInstance = new VirtualInstance();
            virtualInstance.setUuid(vmGuid);
            virtualInstance.setConfirmed(1L);
            virtualInstance.setGuestSystem(guest);
            virtualInstance.setState(state);
            virtualInstance.setName(name);
            virtualInstance.setType(type);
            virtualInstance.setNumberOfCPUs(vCpus);
            virtualInstance.setTotalMemory(memory);

            if (host != null) {
                // will also set the hostSystem for virtualInstance when present
                host.addGuest(virtualInstance);
            }

            VirtualInstanceFactory.getInstance()
                    .saveVirtualInstance(virtualInstance);
        }
        else {
            log.warn("Preventing creation of a duplicated VirtualInstance " +
                     "for 'uuid': " + vmGuid);
        }
    }

    /**
     * Update mapping of given guest VirtualInstance to given (host) Server.
     * This method removes the old VirtualInstance and creates a new one.
     * Either host or guest must not be null.
     *
     * @param virtualInstance - the virtual instance
     * @param name - guest name
     * @param state - instance state
     * @param host - the host or null
     * @param guest - the guest or null
     */
    public static void updateGuestVirtualInstance(VirtualInstance virtualInstance,
            String name, VirtualInstanceState state, Server host, Server guest) {
        int vCpu = 0;
        long memory = 0;
        if (virtualInstance.getNumberOfCPUs() != null) {
            vCpu = virtualInstance.getNumberOfCPUs().intValue();
        }
        if (virtualInstance.getTotalMemory() != null) {
            memory = virtualInstance.getTotalMemory().longValue();
        }
        updateGuestVirtualInstance(virtualInstance, name, state, host, guest, vCpu,
                memory);
    }

    /**
     * Update mapping of given guest VirtualInstance to given (host) Server.
     * This method removes the old VirtualInstance and creates a new one.
     * Either host or guest must not be null.
     *
     * @param virtualInstance - the virtual instance
     * @param name - guest name
     * @param state - instance state
     * @param host - the host or null
     * @param guest - the guest or null
     * @param vCpus - number of CPUs
     * @param memory - total memory
     */
    public static void updateGuestVirtualInstance(VirtualInstance virtualInstance,
            String name, VirtualInstanceState state, Server host, Server guest,
            int vCpus, long memory) {
        Server oldHost = virtualInstance.getHostSystem();
        Server oldGuest = virtualInstance.getGuestSystem();
        if (oldHost == null || oldGuest == null ||
                !oldHost.equals(host) || !oldGuest.equals(guest) ||
                !name.equals(virtualInstance.getName()) ||
                !virtualInstance.getState().equals(state) ||
                !virtualInstance.getNumberOfCPUs().equals(vCpus) ||
                !virtualInstance.getTotalMemory().equals(memory)) {
            VirtualInstanceFactory.getInstance().deleteVirtualInstanceOnly(virtualInstance);
            addGuestVirtualInstance(virtualInstance.getUuid(), name,
                    virtualInstance.getType(), state, host, guest, vCpus, memory);
        }
    }

    /**
     * Update the properties of a VirtualInstance.
     *
     * @param virtualInstance - the virtual instance
     * @param name - guest name
     * @param state - instance state
     * @param vCpus - number of CPUs
     * @param memory - total memory
     */
    public static void updateGuestVirtualInstanceProperties(VirtualInstance virtualInstance,
            String name, VirtualInstanceState state, int vCpus, long memory) {
        virtualInstance.setName(name);
        virtualInstance.setState(state);
        virtualInstance.setNumberOfCPUs(vCpus);
        virtualInstance.setTotalMemory(memory);
        VirtualInstanceFactory.getInstance().saveVirtualInstance(virtualInstance);
    }

    /**
     * Return swapped uuid in case it already exists in a virtual instance.
     *
     * @param uuid - virtual instance uuid
     * @return Returns same uuid or swapped version if it exists as virtual instance
     */
    public static String fixUuidIfSwappedUuidExists(String uuid) {
        // The uuid value for the VM might not be read properly as little endian,
        // so we always try to match it with the possible swapped version in case
        // it already exists in the database.
        String virtUuidSwapped = SaltUtils.uuidToLittleEndian(uuid);
        if (!VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid(virtUuidSwapped).isEmpty()) {
            log.warn("Detected swapped UUID for a virtual instance: Coercing [" +
                    uuid + "] -> [" + virtUuidSwapped + "]");
            return virtUuidSwapped;
        }
        return uuid;
    }
}
