/**
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
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Virt Engine Domain Lifecycle Event Action Handler
 */
public class LibvirtEngineDomainLifecycleMessageAction implements MessageAction {

    private static final Logger LOG = Logger.getLogger(LibvirtEngineDomainLifecycleMessageAction.class);

    private final VirtManager virtManager;

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

                List<VirtualInstance> vms = VirtualInstanceFactory.getInstance().lookupVirtualInstanceByUuid(guid);
                if (vms.isEmpty()) {
                    // We got a machine created from outside SUMA,
                    // ask Salt for details on it to create it
                    Optional<GuestDefinition> result = virtManager.getGuestDefinition(
                            minionId, message.getDomainName());

                    result.ifPresent(def -> {
                        VirtualInstanceState state = VirtualInstanceFactory.getInstance().getStoppedState();
                        if (Arrays.asList("started", "resumed").contains(event)) {
                            state = VirtualInstanceFactory.getInstance().getRunningState();
                        }

                        LOG.debug("Adding VM " + def.getName() + " with state to " + state.getLabel());
                        VirtualInstanceManager.addGuestVirtualInstance(def.getUuid().replaceAll("-", ""),
                                def.getName(), def.getVirtualInstanceType(), state, minion, null,
                                def.getVcpu().getMax(), def.getMaxMemory());
                    });
                }
                else {
                    List<String> stoppedEvents = Arrays.asList("defined", "stopped", "shutdown");
                    List<String> runningEvents = Arrays.asList("started", "resumed");
                    List<String> pausedEvents = Arrays.asList("suspended", "pmsuspended");
                    List<String> crashedEvents = Arrays.asList("crashed");

                    Map<List<String>, VirtualInstanceState> statesMap =
                            new HashMap<List<String>, VirtualInstanceState>();
                    statesMap.put(stoppedEvents, VirtualInstanceFactory.getInstance().getStoppedState());
                    statesMap.put(runningEvents, VirtualInstanceFactory.getInstance().getRunningState());
                    statesMap.put(pausedEvents, VirtualInstanceFactory.getInstance().getPausedState());
                    statesMap.put(crashedEvents, VirtualInstanceFactory.getInstance().getCrashedState());

                    VirtualInstanceState state = statesMap.entrySet().stream()
                            .filter(entry -> entry.getKey().contains(event))
                            .map(entry -> entry.getValue())
                            .findFirst().orElse(VirtualInstanceFactory.getInstance().getUnknownState());

                    LOG.debug("Changing VM " + vms.get(0) + " state to " + state.getLabel());

                    // We need to check if the VM is still defined and delete it if needed
                    if (Arrays.asList("undefined", "stopped", "shutdown", "crashed").contains(event) &&
                        !virtManager.getGuestDefinition(minionId, message.getDomainName()).isPresent()) {
                        vms.forEach(vm -> VirtualInstanceManager.deleteGuestVirtualInstance(vm));
                    }
                    else {
                        final Optional<GuestDefinition> updatedDef = message.getDetail().equals("updated") ?
                                virtManager.getGuestDefinition(minionId, message.getDomainName()) :
                                Optional.empty();

                        vms.forEach(vm -> {
                            String name = updatedDef.isPresent() ? updatedDef.get().getName() : vm.getName();
                            Integer cpuCount = updatedDef.isPresent() ?
                                    updatedDef.get().getVcpu().getMax() :
                                    vm.getNumberOfCPUs();
                            Long memory = updatedDef.isPresent() ?
                                    updatedDef.get().getMaxMemory() :
                                    vm.getTotalMemory();
                            VirtualInstanceManager.updateGuestVirtualInstanceProperties(
                                    vm, name, state, cpuCount, memory);
                        });
                    }
                }
            });
        }
    }
}
