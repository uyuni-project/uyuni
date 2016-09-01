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
package com.suse.manager.reactor.hardware;

import com.redhat.rhn.domain.server.CPU;
import com.redhat.rhn.domain.server.CPUArch;
import com.redhat.rhn.domain.server.Device;
import com.redhat.rhn.domain.server.Dmi;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerNetAddress4;
import com.redhat.rhn.domain.server.ServerNetAddress6;
import com.redhat.rhn.domain.server.ServerNetworkFactory;

import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.services.SaltGrains;
import com.suse.manager.webui.utils.salt.custom.SumaUtil;
import com.suse.salt.netapi.calls.modules.Network;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Store minion hardware details in the SUSE Manager database.
 */
public class HardwareMapper {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(HardwareMapper.class);

    private final MinionServer server;
    private final ValueMap grains;
    private boolean hasPrimaryInterfaceSet = false;
    private List<String> errors = new LinkedList<>();

    private static final Pattern PRINTER_REGEX = Pattern.compile(".*/lp\\d+$");
    private static final String SYSFS_PATH = "P";
    private static final String ENTRIES = "E";

    /**
     * @param serverIn the minion server
     * @param grainsIn the grains
     */
    public HardwareMapper(MinionServer serverIn, ValueMap grainsIn) {
        this.server = serverIn;
        this.grains = grainsIn;
    }

    /**
     * @return the value of the `cpuarch` grain
     */
    public String getCpuArch() {
        return grains.getValueAsString(SaltGrains.CPUARCH.getValue()).toLowerCase();
    }

    /**
     * Store CPU information given as a {@link ValueMap}.
     *
     * @param cpuinfo Salt returns /proc/cpuinfo data
     */
    public void mapCpuInfo(ValueMap cpuinfo) {
        final CPU cpu = Optional.ofNullable(server.getCpu()).orElseGet(CPU::new);

        // os.uname[4]
        String cpuarch = grains.getValueAsString(SaltGrains.CPUARCH.getValue())
                .toLowerCase();

        if (StringUtils.isBlank(cpuarch)) {
            errors.add("CPU: Grain 'cpuarch' has no value");
            LOG.error("Grain 'cpuarch' has no value for minion: " + server.getMinionId());
            return;
        }

        // See hardware.py read_cpuinfo()
        if (CpuArchUtil.isX86(cpuarch)) {
            if (cpuarch.equals("x86_64")) {
                cpuarch = "x86_64";
            }
            else {
                cpuarch = "i386";
            }

            // /proc/cpuinfo -> model name
            cpu.setModel(truncateModel(grains.getValueAsString("cpu_model")));
            // some machines don't report cpu MHz
            cpu.setMHz(truncateMhz(cpuinfo.get("cpu MHz").flatMap(ValueMap::toString)
                    .orElse("-1")));
            cpu.setVendor(truncateVendor(cpuinfo, "vendor_id"));
            cpu.setStepping(truncateStepping(cpuinfo, "stepping"));
            cpu.setFamily(truncateFamily(cpuinfo, "cpu family"));
            cpu.setCache(truncateCache(cpuinfo, "cache size"));
            cpu.setBogomips(truncateBogomips(cpuinfo, "bogomips"));
            cpu.setFlags(truncateFlags(cpuinfo.getValueAsCollection("flags")
                    .map(c -> c.stream()
                        .map(e -> Objects.toString(e, ""))
                        .collect(Collectors.joining(" ")))
                    .orElse(null)));
            cpu.setVersion(truncateVersion(cpuinfo, "model"));

        }
        else if (CpuArchUtil.isPPC64(cpuarch)) {
            cpu.setModel(truncateModel(cpuinfo.getValueAsString("cpu")));
            cpu.setVersion(truncateVersion(cpuinfo, "revision"));
            cpu.setBogomips(truncateBogomips(cpuinfo, "bogompis"));
            cpu.setVendor(truncateVendor(cpuinfo, "machine"));
            cpu.setMHz(truncateMhz(StringUtils.substring(cpuinfo.get("clock")
                    .flatMap(ValueMap::toString)
                    .orElse("-1MHz"), 0, -3))); // remove MHz sufix
        }
        else if (CpuArchUtil.isS390(cpuarch)) {
            cpu.setVendor(truncateVendor(cpuinfo, "vendor_id"));
            cpu.setModel(truncateModel(cpuarch));
            cpu.setBogomips(truncateBogomips(cpuinfo, "bogomips per cpu"));
            cpu.setFlags(truncateFlags(cpuinfo.get("features")
                    .flatMap(ValueMap::toString).orElse(null)));
            cpu.setMHz("0");
        }
        else {
            cpu.setVendor(cpuarch);
            cpu.setModel(cpuarch);
        }

        CPUArch arch = ServerFactory.lookupCPUArchByName(cpuarch);
        cpu.setArch(arch);

        cpu.setNrsocket(grains.getValueAsLong("cpusockets").orElse(null));
        // Use our custom grain. Salt has a 'num_cpus' grain but it gives
        // the number of active CPUs not the total num of CPUs in the system.
        // On s390x this number of active and actual CPUs can be different.
        cpu.setNrCPU(grains.getValueAsLong("total_num_cpus").orElse(0L));

        if (arch != null) {
            // should not happen but arch is not nullable so if we don't have
            // the arch we cannot insert the cpu data
            cpu.setServer(server);
            server.setCpu(cpu);
        }
    }

    /**
     * Store DMI info as queried from Salt.
     *
     * @param smbiosRecordsBios smbios records of type "BIOS"
     * @param smbiosRecordsSystem smbios records of type "System"
     * @param smbiosRecordsBaseboard smbios records of type "Baseboard"
     * @param smbiosRecordsChassis smbios records of type "Chassis"
     */
    public void mapDmiInfo(Map<String, Object> smbiosRecordsBios,
            Map<String, Object> smbiosRecordsSystem,
            Map<String, Object> smbiosRecordsBaseboard,
            Map<String, Object> smbiosRecordsChassis) {
        String biosVendor = null, biosVersion = null, biosReleseDate = null,
                productName = null, systemVersion = null, systemSerial = null,
                chassisSerial = null, chassisTag = null, boardSerial = null;

        ValueMap bios = new ValueMap(smbiosRecordsBios);
        ValueMap system = new ValueMap(smbiosRecordsSystem);
        ValueMap baseboard = new ValueMap(smbiosRecordsChassis);
        ValueMap chassis = new ValueMap(smbiosRecordsChassis);
        try {
            biosVendor = bios.getOptionalAsString("vendor").orElse(null);
            biosVersion = bios.getOptionalAsString("version").orElse(null);
            biosReleseDate = bios.getOptionalAsString("release_date").orElse(null);

            productName = system.getOptionalAsString("product_name").orElse(null);
            systemVersion = system.getOptionalAsString("version").orElse(null);
            systemSerial = system.getOptionalAsString("serial_number").orElse(null);

            boardSerial = baseboard.getOptionalAsString("serial_number").orElse(null);

            chassisSerial = chassis.getOptionalAsString("serial_number").orElse(null);
            chassisTag = chassis.getOptionalAsString("asset_tag").orElse(null);
        }
        catch (com.google.gson.JsonSyntaxException e) {
            LOG.warn("Could not retrieve DMI info from minion '" + server.getMinionId() +
                    "': " + e.getMessage());
            // In order to behave like the "old style" registration
            // go on and persist an empty Dmi bean.
            errors.add("DMI: Could not retrieve DMI records: " + e.getMessage());
        }

        Dmi dmi = server.getDmi();
        if (dmi == null) {
            dmi = new Dmi();
        }
        StringBuilder dmiSystem = new StringBuilder();
        if (StringUtils.isNotBlank(productName)) {
            dmiSystem.append(productName);
        }
        if (StringUtils.isNotBlank(systemVersion)) {
            if (dmiSystem.length() > 0) {
                dmiSystem.append(" ");
            }
            dmiSystem.append(systemVersion);
        }
        dmi.setSystem(dmiSystem.length() > 0 ? dmiSystem.toString().trim() : null);
        dmi.setProduct(productName);
        dmi.setBios(biosVendor, biosVersion, biosReleseDate);
        dmi.setVendor(biosVendor);

        dmi.setAsset(String.format("(chassis: %s) (chassis: %s) (board: %s) (system: %s)",
                Objects.toString(chassisSerial, ""), Objects.toString(chassisTag, ""),
                Objects.toString(boardSerial, ""), Objects.toString(systemSerial, "")));

        dmi.setServer(server);
        server.setDmi(dmi);
    }

    /**
     * Map devices as found in exported udevdb returned from Salt.
     *
     * @param udevdb exported contents of udevdb
     */
    public void mapDevices(List<Map<String, Object>> udevdb) {
        // remove any existing devices in case we're refreshing the hw info
        for (Device device : server.getDevices()) {
            ServerFactory.delete(device);
        }
        server.getDevices().clear();

        if (udevdb.isEmpty()) {
            errors.add("Devices: Salt module 'udevdb.exportdb' returned an empty list");
            LOG.error("Salt module 'udevdb.exportdb' returned an empty list " +
                    "for minion: " + server.getMinionId());
            return;
        }

        udevdb.forEach(dbdev -> {
            String devpath = (String)dbdev.get(SYSFS_PATH); // sysfs path without /sys
            @SuppressWarnings("unchecked")
            ValueMap props = new ValueMap((Map<String, Object>) dbdev.get(ENTRIES));
            String subsys = props.getValueAsString("SUBSYSTEM");

            if ("pci".equals(subsys) || "usb".equals(subsys) ||
                    "block".equals(subsys) || "ccw".equals(subsys) ||
                    "scsi".equals(subsys)) {

                Device device = new Device();
                device.setBus(subsys);
                device.setDriver(props.getValueAsString("DRIVER"));
                device.setPcitype(classifyPciType(subsys));
                device.setDetached(0L);
                device.setDeviceClass(classifyClass(server.getMinionId(), dbdev));
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
                    if (udevdb.stream().anyMatch(dev ->
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
    }

    /**
     * Store network information as returned by Salt.
     *
     * @param interfaces network interfaces
     * @param primaryIps primary IP addresses
     * @param netModules network modules
     */
    public void mapNetworkInfo(Map<String, Network.Interface> interfaces,
            Optional<Map<SumaUtil.IPVersion, SumaUtil.IPRoute>> primaryIps,
            Map<String, Optional<String>> netModules) {
        if (interfaces.isEmpty()) {
            errors.add("Network: Salt module 'network.interfaces' returned en empty value");
            LOG.error("Salt module 'network.interfaces' returned en empty value " +
                    "for minion: " + server.getMinionId());
            return;
        }

        Optional<String> primaryIPv4 = primaryIps
                    .flatMap(x -> Optional.ofNullable(x.get(SumaUtil.IPVersion.IPV4)))
                    .map(SumaUtil.IPRoute::getSource);
        Optional<String> primaryIPv6 = primaryIps
                    .flatMap(x -> Optional.ofNullable(x.get(SumaUtil.IPVersion.IPV6)))
                    .map(SumaUtil.IPRoute::getSource);

        com.redhat.rhn.domain.server.Network network =
                new com.redhat.rhn.domain.server.Network();
        network.setHostname(grains.getOptionalAsString("fqdn").orElse(null));
        primaryIPv4.ifPresent(network::setIpaddr);
        primaryIPv6.ifPresent(network::setIp6addr);

        server.getNetworks().clear();
        server.addNetwork(network);

        interfaces.forEach((name, saltIface) -> {
            NetworkInterface ifaceEntity = server.getNetworkInterface(name);
            if (ifaceEntity == null) {
                // we got a new interface
                ifaceEntity = new NetworkInterface();
            }
            // else update the existing interface
            final NetworkInterface iface = ifaceEntity;

            iface.setHwaddr(saltIface.getHWAddr());
            iface.setModule(netModules.get(name).orElse(null));
            iface.setServer(server);
            iface.setName(name);

            server.addNetworkInterface(iface);

            // we have to do this because we need the id of the interface afterwards
            ServerFactory.saveNetworkInterface(iface);
            // flush & refresh iface because generated="insert"
            // on interfaceId does not seem to work
            ServerFactory.getSession().flush();
            ServerFactory.getSession().refresh(iface);

            Optional<Network.INet> inet = Optional.ofNullable(saltIface.getInet())
                    .flatMap(addr -> addr.stream().findFirst());

            inet.ifPresent(addr4 -> {
                // set IPv4 network info
                ServerNetAddress4 ipv4 = ServerNetworkFactory
                        .findServerNetAddress4(iface.getInterfaceId());
                if (ipv4 == null) {
                    ipv4 = new ServerNetAddress4();
                }
                ipv4.setInterfaceId(iface.getInterfaceId());
                ipv4.setAddress(addr4.getAddress().orElse(null));
                ipv4.setNetmask(addr4.getNetmask().orElse(null));
                ipv4.setBroadcast(addr4.getBroadcast().orElse(null));

                ServerNetworkFactory.saveServerNetAddress4(ipv4);

                if (StringUtils.equals(ipv4.getAddress(), primaryIPv4.orElse(null))) {
                    hasPrimaryInterfaceSet = true;
                    iface.setPrimary("Y");
                }
            });

            Optional<Network.INet6> inet6 = Optional.ofNullable(saltIface.getInet6())
                    .flatMap(addr -> addr.stream().findFirst());
            inet6.ifPresent(addr6 -> {
                // set IPv6 network info
                ServerNetAddress6 ipv6 = ServerNetworkFactory
                        .findServerNetAddress6(iface.getInterfaceId());
                if (ipv6 == null) {
                    ipv6 = new ServerNetAddress6();
                }
                ipv6.setInterfaceId(iface.getInterfaceId());
                ipv6.setAddress(addr6.getAddress());
                ipv6.setNetmask(addr6.getPrefixlen());
                // scope is part of the entity's composite-id
                // so if it's null we'll get a list with null on namedQuery.list()
                // therefore we need a default value
                ipv6.setScope(Optional.ofNullable(addr6.getScope()).orElse("unknown"));

                ServerNetworkFactory.saveServerNetAddress6(ipv6);
            });
        });

        if (!hasPrimaryInterfaceSet) {
            primaryIPv6.ifPresent(ipv6Primary -> {
                server.getNetworkInterfaces().stream()
                    .filter(netIf -> netIf.getIPv6Addresses().stream()
                        .anyMatch(address -> ipv6Primary.equals(address.getAddress()))
                    )
                    .findFirst()
                    .ifPresent(primaryNetIf -> primaryNetIf.setPrimary("Y"));
            });
        }
    }

    /**
     * Return a (possibly empty) list of error messages.
     *
     * @return error messages
     */
    public List<String> getErrors() {
        return errors;
    }

    private String truncateVendor(ValueMap cpuinfo, String key) {
        return cpuinfo.getValueAsString(key, 32);
    }

    private String truncateVersion(ValueMap cpuinfo, String key) {
        return cpuinfo.getValueAsString(key, 32);
    }

    private String truncateBogomips(ValueMap cpuinfo, String key) {
        return cpuinfo.getValueAsString(key, 16);
    }

    private String truncateCache(ValueMap cpuinfo, String key) {
        return cpuinfo.getValueAsString(key, 16);
    }

    private String truncateFamily(ValueMap cpuinfo, String key) {
        return cpuinfo.getValueAsString(key, 32);
    }

    private String truncateMhz(String value) {
        return StringUtils.substring(value, 0, 16);
    }

    private String truncateStepping(ValueMap cpuinfo, String key) {
        return cpuinfo.getValueAsString(key, 16);
    }

    private String truncateModel(String value) {
        return StringUtils.substring(value, 0, 32);
    }

    private String truncateFlags(String value) {
        return StringUtils.substring(value, 0, 2048);
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
                    result = String.format("%s|%s", vendorDesc, deviceDesc);
                }
            }
        }
        else if (subsys.equals("block")) {
            result = attrs.getValueAsString("ID_MODEL");
        }

        return StringUtils.isNotBlank(result) ? result : null;
    }

    private String classifyClass(String minionId, Map<String, Object> device) {
        String sysfsPath = (String)device.get(SYSFS_PATH);
        @SuppressWarnings("unchecked")
        ValueMap attrs = new ValueMap((Map<String, Object>) device.get(ENTRIES));

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

    // FIXME: Can we find this scsi dev type without calling Salt again?
    private int getScsiDevType(String minionId, String sysfsPath) {
        String path = "/sys" + sysfsPath + "/type";
        LOG.warn("FIXME: Need to get contents of file: " + path);
        return -1;
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
