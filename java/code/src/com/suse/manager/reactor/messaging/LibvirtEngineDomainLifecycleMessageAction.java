/*
 * Copyright (c) 2018 SUSE LLC
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
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.server.VirtualInstanceState;
import com.redhat.rhn.manager.system.VirtualInstanceManager;

import com.suse.manager.virtualization.GuestDefinition;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.websocket.VirtNotifications;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Virt Engine Domain Lifecycle Event Action Handler
 */
public class LibvirtEngineDomainLifecycleMessageAction implements MessageAction {

    private static final Logger LOG = LogManager.getLogger(LibvirtEngineDomainLifecycleMessageAction.class);

    private final VirtManager virtManager;
    private final List<String> toRestart = new ArrayList<>();

    /**
     * @param virtManagerIn instance to manage virtualization
     */
    public LibvirtEngineDomainLifecycleMessageAction(VirtManager virtManagerIn) {
        this.virtManager = virtManagerIn;
    }

    @Override
    public void execute(EventMessage msg) {
        LibvirtEngineDomainLifecycleMessage message = (LibvirtEngineDomainLifecycleMessage)msg;

        if (message.getMinionId().isPresent()) {
            String minionId = message.getMinionId().get();
            String event = message.getEvent();

            MinionServerFactory.findByMinionId(minionId).ifPresent(minion -> {
                LOG.debug("Processing " + message.getEvent() + "/" + message.getDetail() + " on minion " + minionId);
                VirtualInstanceManager.updateHostVirtualInstance(minion,
                        VirtualInstanceFactory.getInstance().getFullyVirtType());

                final String guid = VirtualInstanceManager.fixUuidIfSwappedUuidExists(
                        message.getDomainUUID().replaceAll("-", ""));

                VirtNotifications.spreadGuestEvent(minion.getId(), guid, event, message.getDetail());

                List<VirtualInstance> vms = VirtualInstanceFactory.getInstance().lookupVirtualInstanceByUuid(guid);
                if (vms.isEmpty()) {
                    // We got a machine created from outside SUMA,
                    // ask Salt for details on it to create it
                    Optional<GuestDefinition> result = virtManager.getGuestDefinition(
                            minionId, message.getDomainUUID());

                    result.ifPresent(def -> {
                        VirtualInstanceState state = VirtualInstanceFactory.getInstance().getStoppedState();
                        if (Arrays.asList("started", "resumed").contains(event)) {
                            state = VirtualInstanceFactory.getInstance().getRunningState();
                        }

                        LOG.debug("Adding VM " + def.getName() + " with state to " + state.getLabel());
                        VirtualInstanceManager.addGuestVirtualInstance(def.getUuid().replaceAll("-", ""),
                                def.getName(), def.getVirtualInstanceType(), state, minion, null,
                                def.getVcpu().getMax(), def.getMaxMemory() / 1024);

                        // Check if the defined VM will require a manual restart
                        if (def.isRequiresRestart()) {
                            watchVirtualMachine(minionId, message.getDomainName());
                        }
                    });
                }
                else if (vms.size() == 1) {
                    // There shouldn't be two VMs with the same UUID across the world
                    VirtualInstance vm = vms.get(0);
                    List<String> stoppedEvents = Arrays.asList("defined", "stopped", "shutdown");
                    List<String> runningEvents = Arrays.asList("started", "resumed");
                    List<String> pausedEvents = Arrays.asList("suspended", "pmsuspended");
                    List<String> crashedEvents = Collections.singletonList("crashed");

                    Map<List<String>, VirtualInstanceState> statesMap = new HashMap<>();
                    statesMap.put(stoppedEvents, VirtualInstanceFactory.getInstance().getStoppedState());
                    statesMap.put(runningEvents, VirtualInstanceFactory.getInstance().getRunningState());
                    statesMap.put(pausedEvents, VirtualInstanceFactory.getInstance().getPausedState());
                    statesMap.put(crashedEvents, VirtualInstanceFactory.getInstance().getCrashedState());

                    VirtualInstanceState state = statesMap.entrySet().stream()
                            .filter(entry -> entry.getKey().contains(event))
                            .map(Map.Entry::getValue)
                            .findFirst().orElse(vms.get(0).getState());

                    LOG.debug("Changing VM " + vm + " state to " + state.getLabel());

                    // At the end of a migration we get a stopped/migrated event from the source host
                    // and a resumed/migrated event from the target host.
                    // Skip the stopped event since we will update the virtual host in the resumed one.
                    boolean migrated = message.getDetail().equals("migrated");
                    if (migrated && event.equals("stopped")) {
                        return;
                    }

                    // We need to check if the VM is still defined and delete it if needed
                    if (!migrated && Arrays.asList("undefined", "stopped", "shutdown", "crashed").contains(event) &&
                        virtManager.getGuestDefinition(minionId, message.getDomainUUID()).isEmpty()) {
                        // Ensure the state is updated since this could be shown for stopped cluster VMs
                        if (vm.isRegisteredGuest()) {
                            VirtualInstanceManager.updateGuestVirtualInstance(vm, vm.getName(), state, null,
                                    vm.getGuestSystem(), vm.getNumberOfCPUs(), vm.getTotalMemory());
                        }
                        VirtualInstanceManager.deleteGuestVirtualInstance(vm);
                        unwatchVirtualMachine(minionId, message.getDomainName());
                    }
                    else {
                        // Update the host when getting the event of the end of a live migration
                        // or if the server doesn't our record. This could happen if the hypervisor has been fenced
                        // and the VM moved to another hypervisor in the cluster.
                        boolean changedHost = event.equals("started") && !message.getDetail().equals("migrated") &&
                                (vm.getHostSystem() == null || !vm.getHostSystem().getId().equals(minion.getId()));
                        if (migrated && event.equals("resumed") || changedHost) {
                            VirtualInstanceManager.updateGuestVirtualInstance(vm, vm.getName(), state,
                                    minion, vm.getGuestSystem(), vm.getNumberOfCPUs(), vm.getTotalMemory());
                            return;
                        }
                        final Optional<GuestDefinition> updatedDef = message.getDetail().equals("updated") ?
                                virtManager.getGuestDefinition(minionId, message.getDomainUUID()) :
                                Optional.empty();

                        // Check if we need to restart the VM now
                        if (event.equals("stopped")) {
                            checkForRestart(minionId, message.getDomainName());
                        }

                        String name = updatedDef.isPresent() ? updatedDef.get().getName() : vm.getName();
                        Integer cpuCount = updatedDef.map(guestDefinitionIn -> guestDefinitionIn.getVcpu().getMax())
                                .orElseGet(vm::getNumberOfCPUs);
                        Long memory = updatedDef.map(guestDefinitionIn -> guestDefinitionIn.getMaxMemory() / 1024)
                                .orElseGet(vm::getTotalMemory);
                        VirtualInstanceManager.updateGuestVirtualInstanceProperties(
                                vm, name, "updated".equals(message.getDetail()) ? vm.getState() : state,
                                cpuCount, memory);
                    }
                }
                else {
                    LOG.error(String.format("More than one virtual machine with UUID %s: skipping data update", guid));
                }
            });
        }
        VirtNotifications.spreadRefresh("guest");
    }

    private void unwatchVirtualMachine(String minionId, String vmName) {
        synchronized (toRestart) {
            toRestart.remove(computeName(minionId, vmName));
        }
    }

    private void watchVirtualMachine(String minionId, String vmName) {
        synchronized (toRestart) {
            toRestart.add(computeName(minionId, vmName));
        }
    }

    private void checkForRestart(String minionId, String vmName) {
        String name = computeName(minionId, vmName);
        synchronized (toRestart) {
            if (toRestart.contains(name)) {
                virtManager.startGuest(minionId, vmName);
                toRestart.remove(name);
            }
        }
    }

    private String computeName(String minionId, String vmName) {
        return minionId + "-" + vmName;
    }
}
