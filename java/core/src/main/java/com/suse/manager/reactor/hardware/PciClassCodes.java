/*
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
package com.suse.manager.reactor.hardware;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * PCI DEVICE DEFINES
 * These are taken from pci_ids.h in the linux kernel source and used to
 * properly identify the hardware
 */
public enum PciClassCodes {

    PCI_BASE_CLASS_STORAGE("1", null),
    PCI_CLASS_STORAGE_SCSI("00", PCI_BASE_CLASS_STORAGE),
    PCI_CLASS_STORAGE_IDE("01", PCI_BASE_CLASS_STORAGE),
    PCI_CLASS_STORAGE_FLOPPY("02", PCI_BASE_CLASS_STORAGE),
    PCI_CLASS_STORAGE_IPI("03", PCI_BASE_CLASS_STORAGE),
    PCI_CLASS_STORAGE_RAID("04", PCI_BASE_CLASS_STORAGE),
    PCI_CLASS_STORAGE_OTHER("80", PCI_BASE_CLASS_STORAGE),

    PCI_BASE_CLASS_NETWORK("2", null),
    PCI_CLASS_NETWORK_ETHERNET("00", PCI_BASE_CLASS_NETWORK),
    PCI_CLASS_NETWORK_TOKEN_RING("01", PCI_BASE_CLASS_NETWORK),
    PCI_CLASS_NETWORK_FDDI("02", PCI_BASE_CLASS_NETWORK),
    PCI_CLASS_NETWORK_ATM("03", PCI_BASE_CLASS_NETWORK),
    PCI_CLASS_NETWORK_OTHER("80", PCI_BASE_CLASS_NETWORK),

    PCI_BASE_CLASS_DISPLAY("3", null),
    PCI_CLASS_DISPLAY_VGA("00", PCI_BASE_CLASS_DISPLAY),
    PCI_CLASS_DISPLAY_XGA("01", PCI_BASE_CLASS_DISPLAY),
    PCI_CLASS_DISPLAY_3D("02", PCI_BASE_CLASS_DISPLAY),
    PCI_CLASS_DISPLAY_OTHER("80", PCI_BASE_CLASS_DISPLAY),

    PCI_BASE_CLASS_MULTIMEDIA("4", null),
    PCI_CLASS_MULTIMEDIA_VIDEO("00", PCI_BASE_CLASS_MULTIMEDIA),
    PCI_CLASS_MULTIMEDIA_AUDIO("01", PCI_BASE_CLASS_MULTIMEDIA),
    PCI_CLASS_MULTIMEDIA_PHONE("02", PCI_BASE_CLASS_MULTIMEDIA),
    PCI_CLASS_MULTIMEDIA_OTHER("80", PCI_BASE_CLASS_MULTIMEDIA),

    PCI_BASE_CLASS_BRIDGE("6", null),
    PCI_CLASS_BRIDGE_HOST("00", PCI_BASE_CLASS_BRIDGE),
    PCI_CLASS_BRIDGE_ISA("01", PCI_BASE_CLASS_BRIDGE),
    PCI_CLASS_BRIDGE_EISA("02", PCI_BASE_CLASS_BRIDGE),
    PCI_CLASS_BRIDGE_MC("03", PCI_BASE_CLASS_BRIDGE),
    PCI_CLASS_BRIDGE_PCI("04", PCI_BASE_CLASS_BRIDGE),
    PCI_CLASS_BRIDGE_PCMCIA("05", PCI_BASE_CLASS_BRIDGE),
    PCI_CLASS_BRIDGE_NUBUS("06", PCI_BASE_CLASS_BRIDGE),
    PCI_CLASS_BRIDGE_CARDBUS("07", PCI_BASE_CLASS_BRIDGE),
    PCI_CLASS_BRIDGE_RACEWAY("08", PCI_BASE_CLASS_BRIDGE),
    PCI_CLASS_BRIDGE_OTHER("80", PCI_BASE_CLASS_BRIDGE),

    PCI_BASE_CLASS_COMMUNICATION("7", null),
    PCI_CLASS_COMMUNICATION_SERIAL("00", PCI_BASE_CLASS_COMMUNICATION),
    PCI_CLASS_COMMUNICATION_PARALLEL("01", PCI_BASE_CLASS_COMMUNICATION),
    PCI_CLASS_COMMUNICATION_MULTISERIAL("02", PCI_BASE_CLASS_COMMUNICATION),
    PCI_CLASS_COMMUNICATION_MODEM("03", PCI_BASE_CLASS_COMMUNICATION),
    PCI_CLASS_COMMUNICATION_OTHER("80", PCI_BASE_CLASS_COMMUNICATION),

    PCI_BASE_CLASS_INPUT("9", null),
    PCI_CLASS_INPUT_KEYBOARD("00", PCI_BASE_CLASS_INPUT),
    PCI_CLASS_INPUT_PEN("01", PCI_BASE_CLASS_INPUT),
    PCI_CLASS_INPUT_MOUSE("02", PCI_BASE_CLASS_INPUT),
    PCI_CLASS_INPUT_SCANNER("03", PCI_BASE_CLASS_INPUT),
    PCI_CLASS_INPUT_GAMEPORT("04", PCI_BASE_CLASS_INPUT),
    PCI_CLASS_INPUT_OTHER("80", PCI_BASE_CLASS_INPUT),

    PCI_BASE_CLASS_SERIAL("C", null),
    PCI_CLASS_SERIAL_FIREWIRE("00", PCI_BASE_CLASS_SERIAL),
    PCI_CLASS_SERIAL_ACCESS("01", PCI_BASE_CLASS_SERIAL),
    PCI_CLASS_SERIAL_SSA("02", PCI_BASE_CLASS_SERIAL),
    PCI_CLASS_SERIAL_USB("03", PCI_BASE_CLASS_SERIAL),
    PCI_CLASS_SERIAL_FIBER("04", PCI_BASE_CLASS_SERIAL),
    PCI_CLASS_SERIAL_SMBUS("05", PCI_BASE_CLASS_SERIAL);

    private final String code;
    private final PciClassCodes baseClass;

    private static final Map<String, PciClassCodes> BY_BASE_CLASS = new HashMap<>();
    private static final EnumMap<PciClassCodes, Map<String, PciClassCodes>> BY_SUB_CLASS =
        new EnumMap<>(PciClassCodes.class);

    static {
        for (PciClassCodes c : values()) {
            PciClassCodes baseClass = c.getBaseClass();
            if (null == baseClass) {
                BY_BASE_CLASS.put(c.code, c);
            }
            else {
                if (!BY_SUB_CLASS.containsKey(baseClass)) {
                    BY_SUB_CLASS.put(baseClass, new HashMap<>());
                }
                Map<String, PciClassCodes> basePciClassCodesMap = BY_SUB_CLASS.get(baseClass);
                basePciClassCodesMap.put(c.code, c);
            }
        }
    }

    /**
     * Private constructor.
     *
     * @param codeIn the code
     * @param baseClassIn the base class
     */
    PciClassCodes(String codeIn, PciClassCodes baseClassIn) {
        this.code = codeIn;
        this.baseClass = baseClassIn;
    }

    /**
     * @return the code of the value.
     */
    public String getCode() {
        return code;
    }

    /**
     * @return the base class of the value.
     */
    public PciClassCodes getBaseClass() {
        return baseClass;
    }

    /**
     * Get the enum value corresponding to the given base class code.
     * @param code the base class code
     * @return the corresponding enum value, or null if not found
     */
    public static PciClassCodes fromBaseCode(String code) {
        return BY_BASE_CLASS.get(code);
    }

    /**
     * Get the enum value corresponding to the given base class and sub class code.
     * @param base the base class enum value
     * @param subCode the sub class code
     * @return the corresponding enum value, or null if not found
     */
    public static PciClassCodes fromSubCode(PciClassCodes base, String subCode) {
        if (base == null || subCode == null) {
            return null;
        }

        Map<String, PciClassCodes> map = BY_SUB_CLASS.get(base);
        return map != null ? map.get(subCode) : null;
    }
}
