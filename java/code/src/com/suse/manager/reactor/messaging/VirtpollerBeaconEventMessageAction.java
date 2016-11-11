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
package com.suse.manager.reactor.messaging;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.server.VirtualInstanceState;
import com.redhat.rhn.domain.server.VirtualInstanceType;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;

import com.suse.manager.webui.utils.salt.custom.VmInfo;

import java.util.List;


/**
 * Virtpoller Beacon Event Action Handler
 */
public class VirtpollerBeaconEventMessageAction extends AbstractDatabaseAction {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doExecute(EventMessage msg) {
        VirtpollerBeaconEventMessage vMsg = (VirtpollerBeaconEventMessage) msg;

        MinionServerFactory.findByMinionId(vMsg.getMinionId()).ifPresent(minion -> {
            updateHostVirtualInstance(minion,
                    VirtualInstanceFactory.getInstance().getFullyVirtType());
            updateGuestsVirtualInstances(minion, vMsg.getVirtpollerData().getPlan());
        });
    }

    private void updateHostVirtualInstance(Server server, VirtualInstanceType type) {
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
     * Goes through all the vms(guests), creates/updates VirtualInstance entries
     * (Server - guests mapping)
     * @param server to be processed
     * @param type - virtualization type to be set to the guests
     * @param vms - guests to be mapped to this server
     */
    private void updateGuestsVirtualInstances(Server server, List<VmInfo> vms) {
        VirtualInstanceFactory vinst = VirtualInstanceFactory.getInstance();
        vms.forEach(vm -> {
            String name = vm.getGuestProperties().getName();
            String uuid = vm.getGuestProperties().getUuid().replace("-", "");
            VirtualInstanceType type = vinst.getVirtualInstanceType(
                    vm.getGuestProperties().getVirtType());
            if (type == null) { // fallback
                type = vinst.getParaVirtType();
            }
            VirtualInstanceState state = vinst.getState(vm.getGuestProperties().getState())
                    .orElseGet(vinst::getUnknownState);

            List<VirtualInstance> virtualInstances =
                    vinst.lookupVirtualInstanceByUuid(uuid);

            if (virtualInstances.isEmpty()) {
                addGuestVirtualInstance(uuid, name, type, state, server, null);
            }
            else {
                virtualInstances.stream().forEach(virtualInstance ->
                updateGuestVirtualInstance(virtualInstance, name, state, server));
            }
        });
    }

    /**
     * Creates a new (guest) VirtualInstance for given VM GUID.
     * Sets given host as a host for this VirtualInstance.
     *
     * @param vmGuid - guid of the new VirtualInstance
     * @param name - name of the guest
     * @param type - virtualization type of the guest
     * @param host - host to be set as host system for the new VirtualInstance
     * @param guest - guest to be set as the guest system for the new VirtualInstance
     */
    private void addGuestVirtualInstance(String vmGuid, String name,
            VirtualInstanceType type, VirtualInstanceState state,
            Server host, Server guest) {
        VirtualInstance virtualInstance = new VirtualInstance();
        virtualInstance.setUuid(vmGuid);
        virtualInstance.setConfirmed(1L);
        virtualInstance.setGuestSystem(guest);
        virtualInstance.setState(state);
        virtualInstance.setName(name);
        virtualInstance.setType(type);

        host.addGuest(virtualInstance); // will also set the hostSystem for virtualInstance
    }

    /**
     * Update mapping of given guest VirtualInstance to given (host) Server.
     * This method removes the old VirtualInstance and creates a new one.
     *
     * @param virtualInstance
     * @param name
     * @param server
     */
    private void updateGuestVirtualInstance(VirtualInstance virtualInstance,
            String name, VirtualInstanceState state, Server server) {
        Server oldHost = virtualInstance.getHostSystem();
        if (oldHost == null ||
                !oldHost.getId().equals(server.getId()) ||
                !name.equals(virtualInstance.getName()) ||
                !virtualInstance.getState().equals(state)) {
            VirtualInstanceFactory.getInstance().deleteVirtualInstanceOnly(virtualInstance);
            addGuestVirtualInstance(virtualInstance.getUuid(), name,
                    virtualInstance.getType(), state, server,
                    virtualInstance.getGuestSystem());
        }
    }
}
