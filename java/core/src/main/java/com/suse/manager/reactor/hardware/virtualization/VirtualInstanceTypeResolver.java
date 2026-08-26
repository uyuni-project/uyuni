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

import static com.redhat.rhn.common.ExceptionMessage.NOT_INSTANTIABLE;
import static com.suse.manager.reactor.hardware.HardwareConstants.VIRTUAL_KVM;
import static com.suse.manager.reactor.hardware.HardwareConstants.VIRTUAL_NITRO;
import static com.suse.manager.reactor.hardware.HardwareConstants.VIRTUAL_QEMU;
import static com.suse.manager.reactor.hardware.HardwareConstants.VIRTUAL_SUBTYPE_AMAZON_EC2_PREFIX;
import static com.suse.manager.reactor.hardware.HardwareConstants.VIRTUAL_SUBTYPE_XEN_PV_DOMU;
import static com.suse.manager.reactor.hardware.HardwareConstants.VIRTUAL_XEN;
import static com.suse.manager.reactor.hardware.HardwareConstants.VIRT_TYPE_AWS;
import static com.suse.manager.reactor.hardware.HardwareConstants.VIRT_TYPE_AWS_NITRO;
import static com.suse.manager.reactor.hardware.HardwareConstants.VIRT_TYPE_AWS_XEN;
import static com.suse.manager.reactor.hardware.HardwareConstants.VIRT_TYPE_FULLY_VIRTUALIZED;
import static com.suse.manager.reactor.hardware.HardwareConstants.VIRT_TYPE_PARA_VIRTUALIZED;
import static com.suse.manager.reactor.hardware.HardwareConstants.VIRT_TYPE_QEMU;

import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.server.VirtualInstanceType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Resolves the {@link VirtualInstanceType} of a guest from the 'virtual' and 'virtual_subtype' grains.
 */
public final class VirtualInstanceTypeResolver {

    private static final Logger LOG = LogManager.getLogger(VirtualInstanceTypeResolver.class);

    /**
     * Look up the virtual instance type for a guest, falling back to the fully virtualized type when
     * the resolved label is unknown to the database.
     *
     * @param virtTypeLowerCase the lowercased 'virtual' grain
     * @param virtSubtype the 'virtual_subtype' grain
     * @param minionId the minion id, only used for logging
     * @return the virtual instance type, never null
     */
    static VirtualInstanceType resolve(String virtTypeLowerCase, String virtSubtype, String minionId) {
        String virtTypeLabel = resolveLabel(virtTypeLowerCase, virtSubtype, minionId);

        VirtualInstanceType type = VirtualInstanceFactory.getInstance().getVirtualInstanceType(virtTypeLabel);

        if (type == null) { // fallback
            type = VirtualInstanceFactory.getInstance().getFullyVirtType();
            LOG.warn("Can't find virtual instance type for string '{}'. Defaulting to '{}' for minion '{}'",
                    virtTypeLowerCase, type.getLabel(), minionId);
        }

        return type;
    }

    /**
     * Translate the 'virtual' and 'virtual_subtype' grains into a virtual instance type label.
     * Amazon EC2 subtypes take precedence over the plain hypervisor mapping.
     *
     * @param virtTypeLowerCase the lowercased 'virtual' grain
     * @param virtSubtype the 'virtual_subtype' grain
     * @param minionId the minion id, only used for logging
     * @return the virtual instance type label, which is not guaranteed to exist in the database
     */
    static String resolveLabel(String virtTypeLowerCase, String virtSubtype, String minionId) {
        String virtTypeLabel = switch (virtTypeLowerCase) {
            case VIRTUAL_XEN -> VIRTUAL_SUBTYPE_XEN_PV_DOMU.equals(virtSubtype) ?
                    VIRT_TYPE_PARA_VIRTUALIZED : VIRT_TYPE_FULLY_VIRTUALIZED;
            case VIRTUAL_QEMU, VIRTUAL_KVM -> VIRT_TYPE_QEMU;
            case VIRTUAL_NITRO -> VIRT_TYPE_AWS_NITRO;
            default -> {
                LOG.info("Detected virtual instance type '{}' for minion '{}'", virtTypeLowerCase, minionId);
                yield virtTypeLowerCase;
            }
        };

        if (virtSubtype.startsWith(VIRTUAL_SUBTYPE_AMAZON_EC2_PREFIX)) {
            virtTypeLabel = switch (virtTypeLowerCase) {
                case VIRTUAL_XEN -> VIRT_TYPE_AWS_XEN;
                case VIRTUAL_QEMU, VIRTUAL_KVM, VIRTUAL_NITRO -> VIRT_TYPE_AWS_NITRO;
                default -> VIRT_TYPE_AWS;
            };
        }

        return virtTypeLabel;
    }

    private VirtualInstanceTypeResolver() {
        throw new UnsupportedOperationException(NOT_INSTANTIABLE);
    }
}
