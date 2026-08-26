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
package com.suse.manager.reactor.hardware;

import static com.redhat.rhn.common.ExceptionMessage.NOT_INSTANTIABLE;

/**
 * Constants used in hardware mapping operations.
 */
public final class HardwareConstants {

    // CPU field length limits (from rhnCpu table schema)
    public static final int CPU_VENDOR_LENGTH = 32;
    public static final int CPU_MODEL_LENGTH = 32;
    public static final int CPU_VERSION_LENGTH = 32;
    public static final int CPU_FAMILY_LENGTH = 32;
    public static final int CPU_STEPPING_LENGTH = 16;
    public static final int CPU_FLAGS_LENGTH = 2048;
    public static final int CPU_BOGOMIPS_LENGTH = 16;
    public static final int CPU_CACHE_LENGTH = 16;
    public static final int CPU_MHZ_LENGTH = 16;

    // Block device major numbers
    public static final int MAJOR_RAM_DEVICE = 1;
    public static final int MAJOR_VT_DEVICE = 7;

    // SCSI device types (from SCSI specification)
    public static final int SCSI_TYPE_DISK = 0;
    public static final int SCSI_TYPE_TAPE = 1;
    public static final int SCSI_TYPE_CDROM = 5;
    public static final int SCSI_TYPE_RBC = 14;  // Reduced Block Commands

    // Udev database keys
    public static final String UDEV_SYSFS_PATH = "P"; // sysfs path without /sys
    public static final String UDEV_ENTRIES = "E";
    public static final String UDEV_EXTRA_ENTRIES = "X-Mgr";
    public static final String UDEV_KEY_DEVTYPE = "DEVTYPE";
    public static final String UDEV_KEY_DRIVER = "DRIVER";
    public static final String UDEV_KEY_ID_BUS = "ID_BUS";
    public static final String UDEV_KEY_ID_MODEL_ID = "ID_MODEL_ID";
    public static final String UDEV_KEY_ID_VENDOR_ID = "ID_VENDOR_ID";
    public static final String UDEV_KEY_PCI_ID = "PCI_ID";
    public static final String UDEV_KEY_SUBSYSTEM = "SUBSYSTEM";
    public static final String UDEV_KEY_PRODUCT = "PRODUCT";
    public static final String UDEV_KEY_ID_VENDOR_FROM_DATABASE = "ID_VENDOR_FROM_DATABASE";
    public static final String UDEV_KEY_ID_MODEL_FROM_DATABASE = "ID_MODEL_FROM_DATABASE";

    // Udev property keys - Device identification
    public static final String UDEV_KEY_ID_MODEL = "ID_MODEL";
    public static final String UDEV_KEY_ID_SERIAL = "ID_SERIAL";
    public static final String UDEV_KEY_ID_CDROM = "ID_CDROM";
    public static final String UDEV_KEY_ID_TYPE = "ID_TYPE";
    public static final String UDEV_KEY_ID_PATH = "ID_PATH";

    // Udev property keys - PCI specific
    public static final String UDEV_KEY_PCI_CLASS = "PCI_CLASS";
    public static final String UDEV_KEY_PCI_SUBSYS_ID = "PCI_SUBSYS_ID";

    // Udev property keys - Block device specific
    public static final String UDEV_KEY_DM_NAME = "DM_NAME";
    public static final String UDEV_KEY_MAJOR = "MAJOR";
    public static final String UDEV_KEY_DEVPATH = "DEVPATH";

    // Device type values (DEVTYPE property values)
    public static final String DEVTYPE_PARTITION = "partition";
    public static final String DEVTYPE_USB_INTERFACE = "usb_interface";
    public static final String DEVTYPE_USB_DEVICE = "usb_device";
    public static final String DEVTYPE_SCSI_DEVICE = "scsi_device";

    // ID_TYPE property values
    public static final String ID_TYPE_CD = "cd";

    // Grain keys for virtualization mapping
    public static final String GRAIN_VIRTUAL = "virtual";
    public static final String GRAIN_VIRTUAL_SUBTYPE = "virtual_subtype";
    public static final String GRAIN_INSTANCE_ID = "instance_id";
    public static final String GRAIN_UUID = "uuid";
    public static final String GRAIN_TOTAL_NUM_CPUS = "total_num_cpus";
    public static final String GRAIN_MEM_TOTAL = "mem_total";
    public static final String GRAIN_OS_FAMILY = "os_family";
    public static final String GRAIN_OSRELEASE = "osrelease";

    // Values of the 'virtual' grain
    public static final String VIRTUAL_XEN = "xen";
    public static final String VIRTUAL_QEMU = "qemu";
    public static final String VIRTUAL_KVM = "kvm";
    public static final String VIRTUAL_NITRO = "nitro";

    // Values of the 'virtual_subtype' grain
    public static final String VIRTUAL_SUBTYPE_XEN_PV_DOMU = "Xen PV DomU";
    public static final String VIRTUAL_SUBTYPE_AMAZON_EC2_PREFIX = "Amazon EC2";

    // Virtual instance type labels (from rhnVirtualInstanceType table)
    public static final String VIRT_TYPE_PARA_VIRTUALIZED = "para_virtualized";
    public static final String VIRT_TYPE_FULLY_VIRTUALIZED = "fully_virtualized";
    public static final String VIRT_TYPE_QEMU = "qemu";
    public static final String VIRT_TYPE_AWS = "aws";
    public static final String VIRT_TYPE_AWS_XEN = "aws_xen";
    public static final String VIRT_TYPE_AWS_NITRO = "aws_nitro";
    public static final String VIRT_TYPE_VIRTAGE = "virtage";

    // Hitachi Virtage (HVM LPAR) detection through DMI
    public static final String DMI_MANUFACTURER_HITACHI = "HITACHI";
    public static final String DMI_PRODUCT_HVM_LPAR_SUFFIX = " HVM LPAR";
    public static final String FLEX_GUEST_UUID = "flex-guest";

    // SLE 11 reports a wrong (big-endian) virtual UUID and needs to be coerced
    public static final String SLE11_OSRELEASE_PREFIX = "11";

    // Error messages
    public static final String GRAIN_VIRTUAL_HAS_NO_VALUE = "Virtualization: Grain 'virtual' has no value";
    public static final String HARDWARE_REFRESH_INCOMPLETE = "Hardware list could not be refreshed completely:\n";
    public static final String HARDWARE_REFRESH_ERROR = "Hardware list could not be refreshed";

    // Error codes
    public static final Long ERROR_RESULT_CODE = -1L;

    private HardwareConstants() {
        throw new UnsupportedOperationException(NOT_INSTANTIABLE);
    }
}
