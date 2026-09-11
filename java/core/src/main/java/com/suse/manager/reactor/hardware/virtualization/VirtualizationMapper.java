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

import static com.suse.manager.reactor.hardware.HardwareConstants.DMI_KEY_MANUFACTURER;
import static com.suse.manager.reactor.hardware.HardwareConstants.DMI_KEY_PRODUCT_NAME;
import static com.suse.manager.reactor.hardware.HardwareConstants.GRAIN_MEM_TOTAL;
import static com.suse.manager.reactor.hardware.HardwareConstants.GRAIN_TOTAL_NUM_CPUS;

import com.redhat.rhn.domain.entitlement.VirtualizationEntitlement;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.server.VirtualInstanceType;

import com.suse.manager.reactor.utils.ValueMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Maps virtualization information from Salt grains and DMI data to the database.
 * Detects whether a minion is a virtual guest, which hypervisor it runs on and keeps its
 * virtual instance in sync.
 */
public class VirtualizationMapper {

    private static final Logger LOG = LogManager.getLogger(VirtualizationMapper.class);

    /** Reported back to the caller when the 'virtual' grain is missing. */
    public static final String GRAIN_VIRTUAL_HAS_NO_VALUE = "Virtualization: Grain 'virtual' has no value";

    // Grain keys
    private static final String GRAIN_INSTANCE_ID = "instance_id";
    private static final String GRAIN_OSRELEASE = "osrelease";
    private static final String GRAIN_OS_FAMILY = "os_family";
    private static final String GRAIN_UUID = "uuid";
    private static final String GRAIN_VIRTUAL = "virtual";
    private static final String GRAIN_VIRTUAL_SUBTYPE = "virtual_subtype";

    // Hitachi Virtage (HVM LPAR) detection through DMI
    private static final String DMI_MANUFACTURER_HITACHI = "HITACHI";
    private static final String DMI_PRODUCT_HVM_LPAR_SUFFIX = " HVM LPAR";
    private static final String FLEX_GUEST_UUID = "flex-guest";
    private static final String VIRT_TYPE_VIRTAGE = "virtage";

    // SLE 11 reports a wrong (big-endian) virtual UUID and needs to be coerced
    private static final String SLE11_OSRELEASE_PREFIX = "11";

    // Error messages
    private static final String VIRTUALIZATION_MAPPING_FAILED = "Virtualization mapping failed: ";

    private final MinionServer server;
    private final ValueMap grains;
    private final VirtualInstanceSynchronizer virtualInstanceSynchronizer;

    /**
     * Create a virtualization mapper.
     *
     * @param serverIn the minion server
     * @param grainsIn the grains
     */
    public VirtualizationMapper(MinionServer serverIn, ValueMap grainsIn) {
        this.server = serverIn;
        this.grains = grainsIn;
        this.virtualInstanceSynchronizer = new VirtualInstanceSynchronizer(serverIn);
    }

    /**
     * Map virtualization information to the database.
     *
     * @param smbiosRecordsSystem optional DMI information about the system
     * @return Optional error message if mapping failed
     */
    public Optional<String> mapVirtualizationInfo(Optional<Map<String, Object>> smbiosRecordsSystem) {
        try {
            String virtTypeLowerCase = StringUtils.lowerCase(grains.getValueAsString(GRAIN_VIRTUAL));
            String virtSubtype = grains.getValueAsString(GRAIN_VIRTUAL_SUBTYPE);
            String instanceId = grains.getValueAsString(GRAIN_INSTANCE_ID);
            String virtUuid = StringUtils.isEmpty(instanceId) ? grains.getValueAsString(GRAIN_UUID) : instanceId;
            int vCPUs = grains.getValueAsLong(GRAIN_TOTAL_NUM_CPUS).orElse(0L).intValue();
            long memory = grains.getValueAsLong(GRAIN_MEM_TOTAL).orElse(0L);

            // Report the missing grain, but keep going because DMI may still identify the system as a guest
            Optional<String> error = Optional.empty();
            if (StringUtils.isEmpty(virtTypeLowerCase)) {
                LOG.error("Grain 'virtual' has no value for minion: {}", server.getMinionId());
                error = Optional.of(GRAIN_VIRTUAL_HAS_NO_VALUE);
            }

            VirtualInstanceType type = null;

            if (VirtualizationEntitlement.isVirtualGuest(virtTypeLowerCase, virtSubtype)) {
                if (StringUtils.isNotBlank(virtUuid)) {
                    virtUuid = StringUtils.remove(virtUuid, '-');
                    type = VirtualInstanceTypeResolver.resolve(virtTypeLowerCase, virtSubtype, server.getMinionId());
                }
            }
            else if (smbiosRecordsSystem.isPresent()) {
                // there's no DMI on S390 and PPC64
                ValueMap dmiSystem = new ValueMap(smbiosRecordsSystem.orElse(Collections.emptyMap()));
                String manufacturer = dmiSystem.getValueAsString(DMI_KEY_MANUFACTURER);
                String productName = dmiSystem.getValueAsString(DMI_KEY_PRODUCT_NAME);
                if (DMI_MANUFACTURER_HITACHI.equalsIgnoreCase(manufacturer) &&
                        productName.endsWith(DMI_PRODUCT_HVM_LPAR_SUFFIX)) {
                    if (StringUtils.isEmpty(virtUuid)) {
                        virtUuid = FLEX_GUEST_UUID;
                    }
                    type = VirtualInstanceFactory.getInstance().getVirtualInstanceType(VIRT_TYPE_VIRTAGE);
                }
            }

            if (type != null) {
                virtualInstanceSynchronizer.synchronize(virtUuid, type, vCPUs, memory, needsSle11UuidFix(instanceId));
            }

            return error;
        }
        catch (Exception e) {
            LOG.error("Failed to map virtualization info for minion {} : {}", server.getMinionId(), e);
            return Optional.of(VIRTUALIZATION_MAPPING_FAILED + e.getMessage());
        }
    }

    /**
     * SLE 11 minions report the virtual UUID in the wrong byte order, unless it comes from the
     * 'instance_id' grain.
     *
     * @param instanceId the 'instance_id' grain
     * @return true if the reported UUID must be coerced to little endian
     */
    boolean needsSle11UuidFix(String instanceId) {
        return grains.getValueAsString(GRAIN_OS_FAMILY).contentEquals(ServerConstants.OS_FAMILY_SUSE) &&
                grains.getValueAsString(GRAIN_OSRELEASE).startsWith(SLE11_OSRELEASE_PREFIX) &&
                StringUtils.isEmpty(instanceId);
    }

}
