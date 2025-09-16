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

/**
 * PCI DEVICE DEFINES
 * These are taken from pci_ids.h in the linux kernel source and used to
 * properly identify the hardware
 */
public enum PciClassCodes {

    PCI_BASE_CLASS_STORAGE("1"),
    PCI_CLASS_STORAGE_SCSI("00"),
    PCI_CLASS_STORAGE_IDE("01"),
    PCI_CLASS_STORAGE_FLOPPY("02"),
    PCI_CLASS_STORAGE_IPI("03"),
    PCI_CLASS_STORAGE_RAID("04"),
    PCI_CLASS_STORAGE_OTHER("80"),

    PCI_BASE_CLASS_NETWORK("2"),
    PCI_CLASS_NETWORK_ETHERNET("00"),
    PCI_CLASS_NETWORK_TOKEN_RING("01"),
    PCI_CLASS_NETWORK_FDDI("02"),
    PCI_CLASS_NETWORK_ATM("03"),
    PCI_CLASS_NETWORK_OTHER("80"),

    PCI_BASE_CLASS_DISPLAY("3"),
    PCI_CLASS_DISPLAY_VGA("00"),
    PCI_CLASS_DISPLAY_XGA("01"),
    PCI_CLASS_DISPLAY_3D("02"),
    PCI_CLASS_DISPLAY_OTHER("80"),

    PCI_BASE_CLASS_MULTIMEDIA("4"),
    PCI_CLASS_MULTIMEDIA_VIDEO("00"),
    PCI_CLASS_MULTIMEDIA_AUDIO("01"),
    PCI_CLASS_MULTIMEDIA_PHONE("02"),
    PCI_CLASS_MULTIMEDIA_OTHER("80"),

    PCI_BASE_CLASS_BRIDGE("6"),
    PCI_CLASS_BRIDGE_HOST("00"),
    PCI_CLASS_BRIDGE_ISA("01"),
    PCI_CLASS_BRIDGE_EISA("02"),
    PCI_CLASS_BRIDGE_MC("03"),
    PCI_CLASS_BRIDGE_PCI("04"),
    PCI_CLASS_BRIDGE_PCMCIA("05"),
    PCI_CLASS_BRIDGE_NUBUS("06"),
    PCI_CLASS_BRIDGE_CARDBUS("07"),
    PCI_CLASS_BRIDGE_RACEWAY("08"),
    PCI_CLASS_BRIDGE_OTHER("80"),

    PCI_BASE_CLASS_COMMUNICATION("7"),
    PCI_CLASS_COMMUNICATION_SERIAL("00"),
    PCI_CLASS_COMMUNICATION_PARALLEL("01"),
    PCI_CLASS_COMMUNICATION_MULTISERIAL("02"),
    PCI_CLASS_COMMUNICATION_MODEM("03"),
    PCI_CLASS_COMMUNICATION_OTHER("80"),

    PCI_BASE_CLASS_INPUT("9"),
    PCI_CLASS_INPUT_KEYBOARD("00"),
    PCI_CLASS_INPUT_PEN("01"),
    PCI_CLASS_INPUT_MOUSE("02"),
    PCI_CLASS_INPUT_SCANNER("03"),
    PCI_CLASS_INPUT_GAMEPORT("04"),
    PCI_CLASS_INPUT_OTHER("80"),

    PCI_BASE_CLASS_SERIAL("C"),
    PCI_CLASS_SERIAL_FIREWIRE("00"),
    PCI_CLASS_SERIAL_ACCESS("01"),
    PCI_CLASS_SERIAL_SSA("02"),
    PCI_CLASS_SERIAL_USB("03"),
    PCI_CLASS_SERIAL_FIBER("04"),
    PCI_CLASS_SERIAL_SMBUS("05");

    private String code;

    /**
     * Private constructor.
     * @param codeIn the code
     */
    PciClassCodes(String codeIn) {
        this.code = codeIn;
    }

    /**
     * @return the code of the value.
     */
    public String getCode() {
        return code;
    }
}
