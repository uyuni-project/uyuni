/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.reactor.hardware.virtualization;

import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.server.VirtualInstanceType;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.VirtualInstanceManager;

import com.suse.manager.utils.SaltUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * Synchronizes the virtual instance of a minion registered as a guest with the database.
 */
public class VirtualInstanceSynchronizer {

    private static final Logger LOG = LogManager.getLogger(VirtualInstanceSynchronizer.class);

    private final MinionServer server;

    /**
     * Create a virtual instance synchronizer.
     *
     * @param serverIn the minion server
     */
    public VirtualInstanceSynchronizer(MinionServer serverIn) {
        this.server = serverIn;
    }

    /**
     * Create or update the virtual instance of the guest identified by the given UUID.
     *
     * @param virtUuid the UUID of the guest as reported by the minion
     * @param virtType the virtual instance type of the guest
     * @param vCPUs the number of virtual CPUs seen by the guest
     * @param memory the amount of memory seen by the guest
     * @param applySle11UuidFix whether the UUID must be coerced to little endian (broken on SLE 11)
     */
    public void synchronize(
            String virtUuid, VirtualInstanceType virtType, int vCPUs, long memory, boolean applySle11UuidFix
    ) {
        String uuid = virtUuid;
        List<VirtualInstance> virtualInstances = VirtualInstanceFactory.getInstance().lookupVirtualInstanceByUuid(uuid);

        if (applySle11UuidFix) {
            uuid = fixAndReturnSle11Uuid(uuid);
            // Fix the "uuid" for already wrong created virtual instances
            for (VirtualInstance virtualInstance : virtualInstances) {
                LOG.warn("Detected wrong 'uuid' for virtual instance. Coercing: [{}] -> [{}]",
                        virtualInstance.getUuid(), uuid);
                VirtualInstanceFactory.getInstance().deleteVirtualInstanceOnly(virtualInstance);
                VirtualInstanceManager.addGuestVirtualInstance(
                        uuid, virtualInstance.getName(), virtualInstance.getType(),
                        virtualInstance.getState(), virtualInstance.getHostSystem(),
                        virtualInstance.getGuestSystem());
            }
            // Now collecting virtual instances with the correct uuid
            virtualInstances = VirtualInstanceFactory.getInstance().lookupVirtualInstanceByUuid(uuid);
        }

        if (virtualInstances.isEmpty()) {
            // For some reason, the uuid of the VM may have changed.
            // Check if we already have a VM with the same virtual_system_id.
            VirtualInstance virtualInstance = VirtualInstanceFactory.getInstance().lookupByGuestId(server.getId());
            if (virtualInstance == null) {
                VirtualInstanceManager.addGuestVirtualInstance(
                        uuid, server.getName(), virtType,
                        VirtualInstanceFactory.getInstance().getRunningState(),
                        null, server, vCPUs, memory);
            }
            else {
                virtualInstance.setUuid(uuid);
                updateVirtualInstance(vCPUs, memory, virtType, virtualInstance);
            }
        }
        else {
            virtualInstances.forEach(
                    virtualInstance -> updateVirtualInstance(vCPUs, memory, virtType, virtualInstance));
        }
    }

    /**
     * Update the virtual instance information
     *
     * @param vCPUs virtual CPUs
     * @param memory memory
     * @param virtType virtual instance type
     * @param virtualInstance virtualInstance to be updated
     */
    private void updateVirtualInstance(
            int vCPUs, long memory, VirtualInstanceType virtType, VirtualInstance virtualInstance
    ) {
        long newMemory = getUpdatedGuestMemory(memory, virtualInstance);
        String name = virtualInstance.getName();
        if (StringUtils.isBlank(name)) {
            // use minion name only when the hypervisor name is unknown
            name = server.getName();
        }
        if (virtType != virtualInstance.getType()) {
            LOG.info("Changing the type from -> {} to -> {}", virtualInstance.getType().getLabel(),
                    virtType.getLabel());
            // Set rereg manual as DB trigger fire on update only, but we delete/insert.
            Optional.ofNullable(virtualInstance.getGuestSystem()).ifPresent(
                    gSrv -> SCCCachingFactory.setReregRequired(gSrv, true));
            virtualInstance.setType(virtType);
        }
        // Don't update memory with kernel-seen one
        VirtualInstanceManager.updateGuestVirtualInstance(virtualInstance, name,
                VirtualInstanceFactory.getInstance().getRunningState(),
                virtualInstance.getHostSystem(), server, vCPUs, newMemory);
    }

    /**
     * Get the memory amount to set. Most of the times we don't want to update it since a better value comes from
     * the virtual host, but for foreign hosts and systems with no memory set, take the value seen from the guest OS:
     * it's better than no value at all.
     */
    private long getUpdatedGuestMemory(long memory, VirtualInstance virtualInstance) {
        boolean isForeign = virtualInstance.getHostSystem() != null &&
                virtualInstance.getHostSystem().hasEntitlement(EntitlementManager.FOREIGN);
        long newMemory = memory;
        // Only foreign system (s390 and VHM) and systems with no memory set should have updated memory
        if (!isForeign && virtualInstance.getTotalMemory() != null && 0 != virtualInstance.getTotalMemory()) {
            newMemory = virtualInstance.getTotalMemory();
        }
        return newMemory;
    }

    /**
     * Determine the correct virtual guest UUID on SLE11 systems:
     * - Returns "swapped" (little-endianized) UUID and clean up a
     * dangling virtual instance with incorrect UUID if such exists.
     *
     * @param virtUuid - the virtual UUID as reported from grains
     * @return the correct UUID of a virtual guest
     */
    private String fixAndReturnSle11Uuid(String virtUuid) {
        // Fix the wrong "uuid" reported by the minion
        // and remove buggy VirtualInstances with such wrong "uuid" from the DB.
        String virtUuidSwapped = SaltUtils.uuidToLittleEndian(virtUuid);
        LOG.warn("Virtual machine doesn't report correct virtual UUID: {}. Coercing to : {}.", virtUuid,
                virtUuidSwapped);
        List<VirtualInstance> wrongVirtualInstances =
                VirtualInstanceFactory.getInstance().lookupVirtualInstanceByUuid(virtUuid);
        wrongVirtualInstances.forEach(virtInstance ->
                VirtualInstanceFactory.getInstance().deleteVirtualInstanceOnly(virtInstance)
        );
        return virtUuidSwapped;
    }

}
