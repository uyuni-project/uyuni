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

import static com.suse.manager.reactor.hardware.HardwareConstants.DEVTYPE_PARTITION;
import static com.suse.manager.reactor.hardware.HardwareConstants.DEVTYPE_SCSI_DEVICE;
import static com.suse.manager.reactor.hardware.HardwareConstants.DEVTYPE_USB_DEVICE;
import static com.suse.manager.reactor.hardware.HardwareConstants.DEVTYPE_USB_INTERFACE;
import static com.suse.manager.reactor.hardware.HardwareConstants.ID_TYPE_CD;
import static com.suse.manager.reactor.hardware.HardwareConstants.MAJOR_RAM_DEVICE;
import static com.suse.manager.reactor.hardware.HardwareConstants.MAJOR_VT_DEVICE;
import static com.suse.manager.reactor.hardware.HardwareConstants.SCSI_TYPE_CDROM;
import static com.suse.manager.reactor.hardware.HardwareConstants.SCSI_TYPE_DISK;
import static com.suse.manager.reactor.hardware.HardwareConstants.SCSI_TYPE_RBC;
import static com.suse.manager.reactor.hardware.HardwareConstants.SCSI_TYPE_TAPE;
import static com.suse.manager.reactor.hardware.HardwareConstants.UDEV_ENTRIES;
import static com.suse.manager.reactor.hardware.HardwareConstants.UDEV_EXTRA_ENTRIES;
import static com.suse.manager.reactor.hardware.HardwareConstants.UDEV_KEY_DEVPATH;
import static com.suse.manager.reactor.hardware.HardwareConstants.UDEV_KEY_DEVTYPE;
import static com.suse.manager.reactor.hardware.HardwareConstants.UDEV_KEY_DM_NAME;
import static com.suse.manager.reactor.hardware.HardwareConstants.UDEV_KEY_DRIVER;
import static com.suse.manager.reactor.hardware.HardwareConstants.UDEV_KEY_ID_BUS;
import static com.suse.manager.reactor.hardware.HardwareConstants.UDEV_KEY_ID_CDROM;
import static com.suse.manager.reactor.hardware.HardwareConstants.UDEV_KEY_ID_MODEL;
import static com.suse.manager.reactor.hardware.HardwareConstants.UDEV_KEY_ID_MODEL_FROM_DATABASE;
import static com.suse.manager.reactor.hardware.HardwareConstants.UDEV_KEY_ID_MODEL_ID;
import static com.suse.manager.reactor.hardware.HardwareConstants.UDEV_KEY_ID_PATH;
import static com.suse.manager.reactor.hardware.HardwareConstants.UDEV_KEY_ID_SERIAL;
import static com.suse.manager.reactor.hardware.HardwareConstants.UDEV_KEY_ID_TYPE;
import static com.suse.manager.reactor.hardware.HardwareConstants.UDEV_KEY_ID_VENDOR_FROM_DATABASE;
import static com.suse.manager.reactor.hardware.HardwareConstants.UDEV_KEY_ID_VENDOR_ID;
import static com.suse.manager.reactor.hardware.HardwareConstants.UDEV_KEY_MAJOR;
import static com.suse.manager.reactor.hardware.HardwareConstants.UDEV_KEY_PCI_CLASS;
import static com.suse.manager.reactor.hardware.HardwareConstants.UDEV_KEY_PCI_ID;
import static com.suse.manager.reactor.hardware.HardwareConstants.UDEV_KEY_PCI_SUBSYS_ID;
import static com.suse.manager.reactor.hardware.HardwareConstants.UDEV_KEY_PRODUCT;
import static com.suse.manager.reactor.hardware.HardwareConstants.UDEV_KEY_SUBSYSTEM;
import static com.suse.manager.reactor.hardware.HardwareConstants.UDEV_SYSFS_PATH;
import static com.suse.manager.reactor.hardware.PciClassCodes.PCI_CLASS_BRIDGE_CARDBUS;
import static com.suse.manager.reactor.hardware.PciClassCodes.PCI_CLASS_BRIDGE_PCMCIA;
import static com.suse.manager.reactor.hardware.PciClassCodes.PCI_CLASS_COMMUNICATION_MODEM;
import static com.suse.utils.Predicates.allProvided;

import com.redhat.rhn.domain.server.Device;

import com.suse.manager.reactor.utils.ValueMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maps hardware devices from udevdb to Device objects, classifying them and extracting relevant information
 * Handles PCI, USB, SCSI, block and CCW devices.
 */
public class DeviceMapper {
    private static final Logger LOG = LogManager.getLogger(DeviceMapper.class);

    public static final String BLOCK = "block";
    public static final String PCI = "pci";
    public static final String PCI_FORMAT = "%s|%s";
    public static final String SCSI = "scsi";
    public static final String USB = "usb";
    public static final String USB_DEVICE_FORMAT = "%04x";

    private static final Set<String> SCANNABLE_DEVICES_SUBSYSTEM = Set.of(PCI, USB, BLOCK, "ccw", SCSI);
    private static final Pattern PRINTER_REGEX = Pattern.compile(".*/lp\\d+$");

    private Device device;
    private String devSysFsPath;
    private String subsystem;
    private ValueMap props;

    /**
     * Maps hardware devices from udevdb.
     *
     * @param udevdb the udev database entries
     * @param dbdev the device entry to process
     * @return Optional error message if mapping failed
     */
    @SuppressWarnings("unchecked")
    public Device mapDevice(List<Map<String, Object>> udevdb, Map<String, Object> dbdev) {
        devSysFsPath = (String) dbdev.get(UDEV_SYSFS_PATH);
        props = new ValueMap((Map<String, Object>) dbdev.get(UDEV_ENTRIES));
        subsystem = props.getValueAsString(UDEV_KEY_SUBSYSTEM);

        if (!SCANNABLE_DEVICES_SUBSYSTEM.contains(subsystem)) {
            return null;
        }

        device = new Device();

        device.setBus(subsystem);
        device.setDriver(StringUtils.defaultIfBlank(props.getValueAsString(UDEV_KEY_DRIVER), "unknown"));
        device.setPcitype(classifyPciType());
        device.setDetached(0L);
        device.setDeviceClass(StringUtils.defaultIfBlank(classifyDeviceClass(dbdev), Device.CLASS_OTHER));
        device.setDescription(getDeviceDesc());

        switch (subsystem) {
            case BLOCK:
                if (!mapBlockDevice()) {
                    return null;
                }
                break;
            case PCI:
                mapPciDevice();
                break;
            case USB:
                mapUsbDevice();
                break;
            case SCSI:
                if (!mapScsiDevice(udevdb)) {
                    return null;
                }
                break;
            default:
                LOG.warn("Ignoring unknown subsystem {}", subsystem);
                break;
        }

        return device;
    }

    /**
     * Get device description based on available udev properties, using different strategies depending
     * on the device subsystem.
     * @return device description or null if it cannot be determined
     */
    protected String getDeviceDesc() {
        String result = null;

        String vendorFromDb = props.getValueAsString(UDEV_KEY_ID_VENDOR_FROM_DATABASE);
        String modelFromDb = props.getValueAsString(UDEV_KEY_ID_MODEL_FROM_DATABASE);

        switch (subsystem) {
            case PCI:
                String pciId = props.getValueAsString(UDEV_KEY_PCI_ID);
                String[] ids = pciId.split(":");
                String pciVendorDesc = ids.length > 0 ? ids[0] : StringUtils.EMPTY;
                String pciDeviceDesc = ids.length > 1 ? ids[1] : StringUtils.EMPTY;

                result = StringUtils.isAnyBlank(vendorFromDb, modelFromDb) ?
                    String.format(PCI_FORMAT, pciVendorDesc, pciDeviceDesc) :
                    String.format(PCI_FORMAT, vendorFromDb, modelFromDb);
                break;
            case USB:
                result = getDeviceDescUsb(vendorFromDb, modelFromDb);
                break;
            case BLOCK:
                result = props.getValueAsString(UDEV_KEY_ID_MODEL);
                break;
            default:
                LOG.info("ignore unknown subsystem {}", subsystem);
                break;
        }

        return StringUtils.trimToNull(result);
    }

    /**
     * Get USB device description, using vendor and model from database if available,
     * otherwise fallback to vendor and model id.
     * @param vendorFromDb vendor description from database
     * @param modelFromDb model description from database
     * @return device description or null if it cannot be determined
     */
    protected String getDeviceDescUsb(String vendorFromDb, String modelFromDb) {
        String vendorId = props.getValueAsString(UDEV_KEY_ID_VENDOR_ID);
        String product = props.getValueAsString(UDEV_KEY_PRODUCT);
        if (StringUtils.isNotBlank(vendorId)) {
            if (allProvided(vendorFromDb, modelFromDb)) {
                return String.format(PCI_FORMAT, vendorFromDb, modelFromDb);
            }

            // TODO lookup in hwdata
            String usbDeviceDesc = props.getValueAsString(UDEV_KEY_ID_MODEL_ID);
            return String.format(PCI_FORMAT, vendorId, usbDeviceDesc);
        }

        String devType = props.getValueAsString(UDEV_KEY_DEVTYPE);
        if (DEVTYPE_USB_INTERFACE.equals(devType)) {
            String driver = props.getValueAsString(UDEV_KEY_DRIVER);
            if (driver.equals("usbhid")) {
                return "USB HID Interface";
            }
            if (driver.equals("hub")) {
                return  "USB Hub Interface";
            }
            return  "USB Interface";
        }

        if (DEVTYPE_USB_DEVICE.equals(devType) && StringUtils.isNotBlank(product)) {
            String[] p = product.split("/");
            String usbVendorDesc = p.length > 0 ?
                    String.format(USB_DEVICE_FORMAT, Integer.parseInt(p[0], 16)) : StringUtils.EMPTY;
            String usbDeviceDesc = p.length > 1 ?
                    String.format(USB_DEVICE_FORMAT, Integer.parseInt(p[1], 16)) : StringUtils.EMPTY;

            // TODO lookup in hwdata
            return String.format(PCI_FORMAT, usbVendorDesc, usbDeviceDesc);
        }

        return null;
    }

    /**
     * Classify device class from udev data.
     * @param dbdev the udev device entry
     * @return device class or null if it cannot be determined
     */
    @SuppressWarnings({"java:S131", "unchecked"})
    protected String classifyDeviceClass(Map<String, Object> dbdev) {
        // PCI devices
        String pciClass = props.getValueAsString(UDEV_KEY_PCI_CLASS);
        String pciClassification = classifyPciDevices(pciClass);
        if (null != pciClassification) {
            return pciClassification;
        }

        // USB
        String idSerial = StringUtils.lowerCase(props.getValueAsString(UDEV_KEY_ID_SERIAL));
        if (StringUtils.isNotBlank(idSerial)) {
            if (idSerial.contains("keyboard")) {
                return Device.CLASS_KEYBOARD;
            }
            if (idSerial.contains("mouse")) {
                return Device.CLASS_MOUSE;
            }
        }

        switch (subsystem) {
            case BLOCK -> {
                boolean isCdrom = StringUtils.isNotBlank(props.getValueAsString(UDEV_KEY_ID_CDROM)) ||
                        ID_TYPE_CD.equals(props.getValueAsString(UDEV_KEY_ID_TYPE));

                return isCdrom ? Device.CLASS_CDROM : Device.CLASS_HD;
            }
            case "sound" -> {
                return Device.CLASS_AUDIO;
            }
            case SCSI -> {
                ValueMap extraAttrs = dbdev.get(UDEV_EXTRA_ENTRIES) != null ?
                    new ValueMap((Map<String, Object>) dbdev.get(UDEV_EXTRA_ENTRIES)) :
                    new ValueMap();
                return StringUtils.defaultIfBlank(classifyScsiDevices(extraAttrs), Device.CLASS_SCSI);
            }
            default -> { // checkstyle
            }
        }

        // Printer
        if (PRINTER_REGEX.matcher(devSysFsPath).matches()) {
            return Device.CLASS_PRINTER;
        }

        // Catchall for specific devices, only do this after all the others
        if (subsystem.equals(PCI) || subsystem.equals(USB)) {
            return Device.CLASS_OTHER;
        }

        return null;
    }

    /**
     * Map block device information.
     *
     * @return false if device should be skipped
     */
    protected boolean mapBlockDevice() {
        String idBusValue = props.getValueAsString(UDEV_KEY_ID_BUS);
        if (StringUtils.isNotBlank(idBusValue)) {
            device.setBus(idBusValue);
        }
        // The sysname is the part after the last "/"
        // see libudev/libudev-device.c, udev_device_set_syspath(...)
        String name = StringUtils.substringAfterLast(devSysFsPath, "/");
        device.setDevice(name);

        if (DEVTYPE_PARTITION.equals(props.getValueAsString(UDEV_KEY_DEVTYPE))) {
            // Do not report partitions, just whole disks
            return false;
        }
        if (StringUtils.isNotBlank(props.getValueAsString(UDEV_KEY_DM_NAME))) {
            // LVM device
            return false;
        }

        int major = Integer.parseInt(props.getValueAsString(UDEV_KEY_MAJOR));

        if (MAJOR_RAM_DEVICE == major) {
            // ram
            return false;
        }
        // skip if character devices for virtual console terminals
        return MAJOR_VT_DEVICE != major;
        // This is interpreted as Physical. But what to do with it?
        // result_item['prop1'] = ''
        // This is interpreted as Logical. But what to do with it?
        // result_item['prop2'] = ''
    }

    /**
     * Map PCI device information.
     */
    protected void mapPciDevice() {
        String pciClass = props.getValueAsString(UDEV_KEY_PCI_ID);
        if (StringUtils.isNotBlank(pciClass)) {
            String[] ids = pciClass.split(":");
            device.setProp1(ids.length > 0 ? ids[0] : null);
            device.setProp2(ids.length > 1 ? ids[1] : null);
        }
        String pciSubsys = props.getValueAsString(UDEV_KEY_PCI_SUBSYS_ID);
        if (StringUtils.isNotBlank(pciSubsys)) {
            String[] ids = pciSubsys.split(":");
            device.setProp3(ids.length > 0 ? ids[0] : null);
            device.setProp4(ids.length > 1 ? ids[1] : null);
        }
    }

    /**
     * Map USB device information.
     */
    protected void mapUsbDevice() {
        String vendorId = props.getValueAsString(UDEV_KEY_ID_VENDOR_ID);
        if (StringUtils.isNotBlank(vendorId)) {
            device.setProp1(vendorId);
        }
        String modelId = props.getValueAsString(UDEV_KEY_ID_MODEL_ID);
        if (StringUtils.isNotBlank(modelId)) {
            device.setProp2(modelId);
        }
    }

    /**
     * Map SCSI device information.
     *
     * @return false if device should be skipped
     */
    protected boolean mapScsiDevice(List<Map<String, Object>> udevdb) {
        // Skip SCSI hosts and targets
        if (!DEVTYPE_SCSI_DEVICE.equals(props.getValueAsString(UDEV_KEY_DEVTYPE))) {
            return false;
        }

        // Check if this SCSI device is already listed as a block device
        if (udevdb.stream().anyMatch(dev ->
                Objects.toString(dev.get(UDEV_SYSFS_PATH), StringUtils.EMPTY).startsWith(devSysFsPath) &&
                        Optional.ofNullable(dev.get(UDEV_ENTRIES))
                                .filter(Map.class::isInstance)
                                .map(Map.class::cast)
                                .filter(m -> BLOCK.equals(m.get(UDEV_KEY_SUBSYSTEM))
                                ).isPresent())) {
            return false;
        }

        // Handle SCSI device identifiers
        if (SCSI.equals(props.getValueAsString(UDEV_KEY_ID_BUS))) {
            String idpath = props.getValueAsString(UDEV_KEY_ID_PATH);
            String dpath = props.getValueAsString(UDEV_KEY_DEVPATH);
            Matcher m;
            if (StringUtils.isNotBlank(idpath)) {
                m = Pattern.compile(".*scsi-(\\d+):(\\d+):(\\d+):(\\d+)").matcher(idpath);
            }
            else {
                m = Pattern.compile(".*/(\\d+):(\\d+):(\\d+):(\\d+)/block/").matcher(dpath);
            }
            if (m.matches()) {
                device.setProp1(m.group(1)); // DEV_HOST
                device.setProp2(m.group(2)); // DEV_ID
                device.setProp3(m.group(3)); // DEV_CHANNEL
                device.setProp4(m.group(4)); // DEV_LUN
            }
        }

        return true;
    }

    /**
     * Classify SCSI devices
     * @param extraAttrs the extra attributes from udev for the SCSI device
     * @return device class or null if it cannot be determined
     */
    protected String classifyScsiDevices(ValueMap extraAttrs) {
        if (!DEVTYPE_SCSI_DEVICE.equals(props.getValueAsString(UDEV_KEY_DEVTYPE))) {
            return null;
        }
        long devType = extraAttrs.getValueAsLong("SCSI_SYS_TYPE").orElse(-1L);

        if (devType == SCSI_TYPE_DISK || devType == SCSI_TYPE_RBC) {
            return Device.CLASS_HD;
        }
         if (devType == SCSI_TYPE_TAPE) {
            return Device.CLASS_TAPE;
        }
         if (devType == SCSI_TYPE_CDROM) {
            return Device.CLASS_CDROM;
        }

        return Device.CLASS_OTHER;
    }

    /**
     * Parse PCI base class
     * @param pciClass the PCI_CLASS udev property
     * @return the PCI base class or null if it cannot be determined
     */
    protected String parsePciBaseClass(String pciClass) {
        if (StringUtils.isBlank(pciClass)) {
            return null;
        }
        return StringUtils.substring(pciClass, -6, -4);
    }

    /**
     * Parse PCI sub class
     * @param pciClass the PCI_CLASS udev property
     * @return the PCI sub class or null if it cannot be determined
     */
    protected String parsePciSubClass(String pciClass) {
        if (StringUtils.isBlank(pciClass)) {
            return null;
        }
        return StringUtils.substring(pciClass, -4, -2);
    }

    /**
     * Classify PCI devices based on their base and sub class codes.
     * @param pciClass the PCI_CLASS udev property
     * @return device class or null if it cannot be determined
     */
    protected String classifyPciDevices(String pciClass) {
        String baseClass = parsePciBaseClass(pciClass);
        String subClass = parsePciSubClass(pciClass);

        PciClassCodes pciBaseCode = PciClassCodes.fromBaseCode(baseClass);
        PciClassCodes pciSubCode = PciClassCodes.fromSubCode(pciBaseCode, subClass);

        if (null == pciBaseCode) {
            return null;
        }

        return switch (pciBaseCode) {
            case PCI_BASE_CLASS_STORAGE -> {
                if (pciSubCode == null) {
                    yield null;
                }
                yield switch (pciSubCode) {
                    case PCI_CLASS_STORAGE_SCSI -> Device.CLASS_SCSI;
                    case PCI_CLASS_STORAGE_IDE -> Device.CLASS_IDE;
                    case PCI_CLASS_STORAGE_FLOPPY -> Device.CLASS_FLOPPY;
                    case PCI_CLASS_STORAGE_RAID -> Device.CLASS_RAID;
                    default -> null;
                };
            }
            case PCI_BASE_CLASS_NETWORK -> Device.CLASS_OTHER;
            case PCI_BASE_CLASS_DISPLAY -> Device.CLASS_VIDEO;
            case PCI_BASE_CLASS_MULTIMEDIA -> {
                if (pciSubCode == null) {
                    yield null;
                }
                yield switch (pciSubCode) {
                    case PCI_CLASS_MULTIMEDIA_VIDEO -> Device.CLASS_CAPTURE;
                    case PCI_CLASS_MULTIMEDIA_AUDIO -> Device.CLASS_AUDIO;
                    default -> null;
                };
            }
            case PCI_BASE_CLASS_BRIDGE ->
                    (PCI_CLASS_BRIDGE_PCMCIA.equals(pciSubCode) || PCI_CLASS_BRIDGE_CARDBUS.equals(pciSubCode)) ?
                            Device.CLASS_SOCKET : null;
            case PCI_BASE_CLASS_COMMUNICATION ->
                    PCI_CLASS_COMMUNICATION_MODEM.equals(pciSubCode) ? Device.CLASS_MODEM : null;
            case PCI_BASE_CLASS_INPUT -> {
                if (pciSubCode == null) {
                    yield null;
                }
                yield switch (pciSubCode) {
                    case PCI_CLASS_INPUT_KEYBOARD -> Device.CLASS_KEYBOARD;
                    case PCI_CLASS_INPUT_MOUSE -> Device.CLASS_MOUSE;
                    case PCI_CLASS_INPUT_SCANNER -> Device.CLASS_SCANNER;
                    default -> null;
                };
            }
            case PCI_BASE_CLASS_SERIAL -> {
                if (pciSubCode == null) {
                    yield null;
                }
                yield switch (pciSubCode) {
                    case PCI_CLASS_SERIAL_FIREWIRE -> Device.CLASS_FIREWIRE;
                    case PCI_CLASS_SERIAL_USB -> Device.CLASS_USB;
                    default -> null;
                };
            }
            default -> null;
        };

    }

    protected Long classifyPciType() {
       return PCI.equals(subsystem) ? 1L : -1L;
    }

    public Device getDevice() {
        return device;
    }
}
