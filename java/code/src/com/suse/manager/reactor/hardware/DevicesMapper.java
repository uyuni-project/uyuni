/**
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

import com.redhat.rhn.domain.server.Device;
import com.redhat.rhn.domain.server.MinionServer;

import com.redhat.rhn.domain.server.ServerFactory;
import com.suse.manager.reactor.utils.ValueMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maps the devices information. This is roughly a port of
 * the client side python code (hardware_gudev.py).
 */
public class DevicesMapper extends AbstractHardwareMapper<MinionServer> {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(DevicesMapper.class);

    private static final Pattern PRINTER_REGEX = Pattern.compile(".*/lp\\d+$");
    private static final String SYSFS_PATH = "P";
    private static final String ENTRIES = "E";

    /**
     * The constructor.
     * @param saltServiceInvoker a {@link SaltServiceInvoker} instance
     */
    public DevicesMapper(SaltServiceInvoker saltServiceInvoker) {
        super(saltServiceInvoker);
    }

    @Override
    public MinionServer doMap(MinionServer server, ValueMap grains) {
        String minionId = server.getMinionId();
        Optional<List<Map<String, Object>>> udevdb = saltInvoker.getUdevdb(minionId);

        // remove any existing devices in case we're refreshing the hw info
        for (Device device : server.getDevices()) {
            ServerFactory.delete(device);
        }
        server.getDevices().clear();

        if (!udevdb.isPresent() || udevdb.filter(List::isEmpty).isPresent()) {
            setError("Salt module 'udevdb.exportdb' returned an empty list");
            LOG.warn("Salt module 'udevdb.exportdb' returned an empty list " +
                    "for minion: " + minionId);
            return null;
        }

        udevdb.get().forEach(dbdev -> {
            String devpath = (String)dbdev.get(SYSFS_PATH); // sysfs path without /sys
            @SuppressWarnings("unchecked")
            ValueMap props = new ValueMap((Map<String, Object>)dbdev.get(ENTRIES));
            String subsys = props.getValueAsString("SUBSYSTEM");

            if ("pci".equals(subsys) || "usb".equals(subsys) ||
                    "block".equals(subsys) || "ccw".equals(subsys) ||
                    "scsi".equals(subsys)) {

                Device device = new Device();
                device.setBus(subsys);
                device.setDriver(props.getValueAsString("DRIVER"));
                device.setPcitype(classifyPciType(subsys));
                device.setDetached(0L);
                device.setDeviceClass(clasifyClass(minionId, dbdev));
                device.setDescription(getDeviceDesc(props));

                if (device.getDeviceClass() == null) {
                    device.setDeviceClass(Device.CLASS_OTHER);
                }
                if (StringUtils.isBlank(device.getDriver())) {
                    device.setDriver("unknown");
                }
                if (subsys.equals("block")) {
                    if (StringUtils.isNotBlank(props.getValueAsString("ID_BUS"))) {
                        device.setBus(props.getValueAsString("ID_BUS"));
                    }
                    // the sysname is the part after the last "/"
                    // see libudev/libudev-device.c, udev_device_set_syspath(...)
                    String name = StringUtils.substringAfterLast(devpath, "/");
                    device.setDevice(name);

                    if (props.getValueAsString("DEVTYPE").equals("partition")) {
                        // do not report partitions, just whole disks
                        return;
                    }
                    if (StringUtils.isNotBlank(props.getValueAsString("DM_NAME"))) {
                        // LVM device
                        return;
                    }
                    if (props.getValueAsString("MAJOR").equals("1")) {
                        // ram device
                        return;
                    }
                    if (props.getValueAsString("MAJOR").equals("7")) {
                        // character devices for virtual console terminals
                        return;
                    }
                    // This is interpreted as Physical. But what to do with it?
                    // result_item['prop1'] = ''
                    // This is interpreted as Logical. But what to do with it?
                    // result_item['prop2'] = ''
                }
                else if (subsys.equals("pci")) {
                    String pciClass = props.getValueAsString("PCI_ID");
                    if (StringUtils.isNotBlank(pciClass)) {
                        String[] ids = pciClass.split(":");
                        device.setProp1(ids.length > 0 ? ids[0] : null);
                        device.setProp2(ids.length > 1 ? ids[1] : null);
                    }
                    String pciSubsys = props.getValueAsString("PCI_SUBSYS_ID");
                    if (StringUtils.isNotBlank(pciSubsys)) {
                        String[] ids = pciSubsys.split(":");
                        device.setProp3(ids.length > 0 ? ids[0] : null);
                        device.setProp4(ids.length > 1 ? ids[1] : null);
                    }
                }
                else if (subsys.equals("usb")) {
                    String vendorId = props.getValueAsString("ID_VENDOR_ID");
                    if (StringUtils.isNotBlank(vendorId)) {
                        device.setProp1(vendorId);
                    }
                    String modelId = props.getValueAsString("ID_MODEL_ID");
                    if (StringUtils.isNotBlank(modelId)) {
                        device.setProp2(modelId);
                    }
                }
                else if (subsys.equals("scsi")) {
                    // skip scsi hosts and targets
                    if (!props.getValueAsString("DEVTYPE").equals("scsi_device")) {
                        return;
                    }
                    // check if this scsi device is already listed as a block device
                    if (udevdb.get().stream().anyMatch(dev ->
                        Objects.toString(dev.get(SYSFS_PATH), "").startsWith(devpath) &&
                            Optional.ofNullable(dev.get(ENTRIES))
                                .filter(Map.class::isInstance)
                                .map(Map.class::cast)
                                .filter(m -> "block".equals(m.get("SUBSYSTEM"))
                                ).isPresent())) {
                        return;
                    }

                }

                if (props.getValueAsString("ID_BUS").equals("scsi")) {
                    String idpath = props.getValueAsString("ID_PATH");
                    String dpath = props.getValueAsString("DEVPATH");
                    Matcher m;
                    if (StringUtils.isNotBlank(idpath)) {
                        m = Pattern.compile(".*scsi-(\\d+):(\\d+):(\\d+):(\\d+)")
                                .matcher(idpath);
                    }
                    else {
                        m = Pattern.compile(".*/(\\d+):(\\d+):(\\d+):(\\d+)/block/")
                                .matcher(dpath);
                    }
                    if (m.matches()) {
                        device.setProp1(m.group(1)); // DEV_HOST
                        device.setProp2(m.group(2)); // DEV_ID
                        device.setProp3(m.group(3)); // DEV_CHANNEL
                        device.setProp4(m.group(4)); // DEV_LUN
                    }
                }

                device.setServer(server);
                server.getDevices().add(device);

            }
        });

        return server;
    }

    private String getDeviceDesc(ValueMap attrs) {
        String subsys = attrs.getValueAsString("SUBSYSTEM");
        String result = null;

        String vendorFromDb = attrs.getValueAsString("ID_VENDOR_FROM_DATABASE");
        String modelFromDb = attrs.getValueAsString("ID_MODEL_FROM_DATABASE");

        if (subsys.equals("pci")) {
            String pciId = attrs.getValueAsString("PCI_ID");
            String[] ids = pciId.split(":");
            String vendorDesc = ids.length > 0 ? ids[0] : "";
            String deviceDesc = ids.length > 1 ? ids[1] : "";

            if (StringUtils.isNotBlank(vendorFromDb) ||
                    StringUtils.isNotBlank(modelFromDb)) {
                result = String.format("%s|%s", vendorFromDb, modelFromDb);
            }
            else {
                // TODO lookup in hwdata
                result = String.format("%s|%s", vendorDesc, deviceDesc);
            }

        }
        else if (subsys.equals("usb")) {
            String vendorId = attrs.getValueAsString("ID_VENDOR_ID");
            String product = attrs.getValueAsString("PRODUCT");
            if (StringUtils.isNotBlank(vendorId)) {
                String vendorDesc = vendorId;
                String deviceDesc = attrs.getValueAsString("ID_MODEL_ID");

                if (StringUtils.isNotBlank(vendorFromDb) ||
                        StringUtils.isNotBlank(modelFromDb)) {
                    result = String.format("%s|%s", vendorFromDb, modelFromDb);
                }
                else {
                    // TODO lookup in hwdata
                    result = String.format("%s|%s", vendorDesc, deviceDesc);
                }
            }
            else {
                String devtype = attrs.getValueAsString("DEVTYPE");
                if (devtype.equals("usb_interface")) {
                    String driver = attrs.getValueAsString("DRIVER");
                    if (driver.equals("usbhid")) {
                        result = "USB HID Interface";
                    }
                    else if (driver.equals("hub")) {
                        result = "USB Hub Interface";
                    }
                    else {
                        result = "USB Interface";
                    }
                }
                else if (devtype.equals("usb_device") && StringUtils.isNotBlank(product)) {
                    String[] p = product.split("/");
                    String vendorDesc = p.length > 0 ?
                            String.format("%04x", Integer.parseInt(p[0], 16)) : "";
                    String deviceDesc = p.length > 1 ?
                            String.format("%04x", Integer.parseInt(p[1], 16)) : "";

                    // TODO lookup in hwdata

                    result = String.format("%s|%s", vendorDesc,
                            deviceDesc);
                }
            }
        }
        else if (subsys.equals("block")) {
            result = attrs.getValueAsString("ID_MODEL");
        }

        return StringUtils.isNotBlank(result) ? result : null;
    }

    private String clasifyClass(String minionId, Map<String, Object> device) {
        String sysfsPath = (String)device.get(SYSFS_PATH);
        @SuppressWarnings("unchecked")
        ValueMap attrs = new ValueMap((Map<String, Object>)device.get(ENTRIES));

        String subsys = attrs.getValueAsString("SUBSYSTEM");
        String pciClass = attrs.getValueAsString("PCI_CLASS");

        String baseClass = parsePciBaseClass(pciClass);
        String subClass = parsePciSubClass(pciClass);

        // network devices
        if (PciClassCodes.PCI_BASE_CLASS_NETWORK.getCode().equals(baseClass)) {
            return Device.CLASS_OTHER;
        }

        // input devices
        // PCI
        if (PciClassCodes.PCI_BASE_CLASS_INPUT.getCode().equals(baseClass)) {
            if (PciClassCodes.PCI_CLASS_INPUT_KEYBOARD.getCode().equals(subClass)) {
                return Device.CLASS_KEYBOARD;
            }
            else if (PciClassCodes.PCI_CLASS_INPUT_MOUSE.getCode().equals(subClass)) {
                return Device.CLASS_MOUSE;
            }
        }

        // USB
        String idSerial = attrs.getValueAsString("ID_SERIAL");
        if (StringUtils.isNotBlank(idSerial)) {
            idSerial = idSerial.toLowerCase();
            if (idSerial.contains("keyboard")) {
                return Device.CLASS_KEYBOARD;
            }
            if (idSerial.contains("mouse")) {
                return Device.CLASS_MOUSE;
            }
        }

        // PCI devices
        if (StringUtils.isNotBlank(baseClass)) {
            if (baseClass.equals(PciClassCodes.PCI_BASE_CLASS_DISPLAY.getCode())) {
                return Device.CLASS_VIDEO;
            }
            else if (baseClass.equals(PciClassCodes.PCI_BASE_CLASS_SERIAL.getCode())) {
                if (PciClassCodes.PCI_CLASS_SERIAL_USB.getCode().equals(subClass)) {
                    return Device.CLASS_USB;
                }
                else if (PciClassCodes.PCI_CLASS_SERIAL_FIREWIRE.getCode()
                        .equals(subClass)) {
                    return Device.CLASS_FIREWIRE;
                }
            }
            else if (baseClass.equals(PciClassCodes.PCI_BASE_CLASS_STORAGE.getCode())) {
                if (PciClassCodes.PCI_CLASS_STORAGE_IDE.getCode().equals(subClass)) {
                    return Device.CLASS_IDE;
                }
                if (PciClassCodes.PCI_CLASS_STORAGE_SCSI.getCode().equals(subClass)) {
                    return Device.CLASS_SCSI;
                }
                if (PciClassCodes.PCI_CLASS_STORAGE_RAID.getCode().equals(subClass)) {
                    return Device.CLASS_RAID;
                }
                if (PciClassCodes.PCI_CLASS_STORAGE_FLOPPY.getCode().equals(subClass)) {
                    return Device.CLASS_FLOPPY;
                }
            }
            else if (baseClass.equals(PciClassCodes.PCI_BASE_CLASS_COMMUNICATION
                    .getCode()) && PciClassCodes.PCI_CLASS_COMMUNICATION_MODEM
                    .getCode().equals(subClass)) {
                return Device.CLASS_MODEM;
            }
            else if (baseClass.equals(PciClassCodes.PCI_BASE_CLASS_INPUT) &&
                    PciClassCodes.PCI_CLASS_INPUT_SCANNER.getCode().equals(subClass)) {
                return Device.CLASS_SCANNER;
            }
            else if (baseClass.equals(PciClassCodes.PCI_BASE_CLASS_MULTIMEDIA.getCode())) {
                if (PciClassCodes.PCI_CLASS_MULTIMEDIA_VIDEO.getCode().equals(subClass)) {
                    return Device.CLASS_CAPTURE;
                }
                if (PciClassCodes.PCI_CLASS_MULTIMEDIA_AUDIO.getCode().equals(subClass)) {
                    return Device.CLASS_AUDIO;
                }
            }
            else if (baseClass.equals(PciClassCodes.PCI_BASE_CLASS_BRIDGE.getCode()) &&
                    (PciClassCodes.PCI_CLASS_BRIDGE_PCMCIA.getCode().equals(subClass) ||
                            PciClassCodes.PCI_CLASS_BRIDGE_CARDBUS.getCode()
                                    .equals(subClass))) {
                return Device.CLASS_SOCKET;
            }
        }

        if (subsys.equals("block")) {
            if (StringUtils.isNotBlank(attrs.getValueAsString("ID_CDROM")) ||
                    (attrs.getValueAsString("ID_TYPE").equals("cd"))) {
                return Device.CLASS_CDROM;
            }
            else {
                return Device.CLASS_HD;
            }
        }
        else if (subsys.equals("sound")) {
            return Device.CLASS_AUDIO;
        }

        if (subsys.equals("scsi")) {
            if (attrs.getValueAsString("DEVTYPE").equals("scsi_device")) {
                int devType = getScsiDevType(minionId, sysfsPath);
                if (devType == 0 || devType == 14) {
                    return Device.CLASS_HD;
                }
                else if (devType == 1) {
                    return Device.CLASS_TAPE;
                }
                else if (devType == 5) {
                    return Device.CLASS_CDROM;
                }
                else {
                    return Device.CLASS_OTHER;
                }
            }
        }

        // printer
        if (PRINTER_REGEX.matcher(sysfsPath).matches()) {
            return Device.CLASS_PRINTER;
        }

        if (subsys.equals("scsi")) {
            return Device.CLASS_SCSI;
        }

        // Catchall for specific devices, only do this after all the others
        if (subsys.equals("pci") || subsys.equals("usb")) {
            return Device.CLASS_OTHER;
        }

        return null;
    }

    private int getScsiDevType(String minionId, String sysfsPath) {
        String path = "/sys" + sysfsPath + "/type";
        try {
            Optional<Integer> integer = saltInvoker
                    .getFileContent(minionId, path)
                    .map(StringUtils::trim)
                    .map(Integer::parseInt);
            if (!integer.isPresent()) {
                LOG.warn("Could not get content of file " + path);
            }
            return integer.orElse(-1);
        }
        catch (Exception e) {
            LOG.warn("Could not get content of file " + path, e);
            return -1;
        }
    }

    private String parsePciBaseClass(String pciClass) {
        if (StringUtils.isBlank(pciClass)) {
            return null;
        }
        return StringUtils.substring(pciClass, -6, -4);
    }

    private String parsePciSubClass(String pciClass) {
        if (StringUtils.isBlank(pciClass)) {
            return null;
        }
        return StringUtils.substring(pciClass, -4, -2);
    }


    private Long classifyPciType(String subsys) {
        if ("pci".equals(subsys)) {
            return 1L;
        }
        return -1L;
    }


}
