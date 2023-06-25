/*
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

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.entitlement.VirtualizationEntitlement;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.server.CPU;
import com.redhat.rhn.domain.server.CPUArch;
import com.redhat.rhn.domain.server.Device;
import com.redhat.rhn.domain.server.Dmi;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFQDN;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerNetAddress4;
import com.redhat.rhn.domain.server.ServerNetAddress6;
import com.redhat.rhn.domain.server.ServerNetworkFactory;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.server.VirtualInstanceState;
import com.redhat.rhn.domain.server.VirtualInstanceType;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.VirtualInstanceManager;

import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.services.SaltGrains;
import com.suse.manager.webui.utils.salt.custom.SumaUtil;
import com.suse.salt.netapi.calls.modules.Network;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Store minion hardware details in the SUSE Manager database.
 */
public class HardwareMapper {

    // Logger for this class
    private static final Logger LOG = LogManager.getLogger(HardwareMapper.class);

    private final MinionServer server;
    private final ValueMap grains;
    private List<String> errors = new LinkedList<>();

    private static final Pattern PRINTER_REGEX = Pattern.compile(".*/lp\\d+$");
    private static final String SYSFS_PATH = "P";
    private static final String ENTRIES = "E";
    private static final String EXTRA_ENTRIES = "X-Mgr";

    /**
     * Create a hardware mapper for a given server with grains.
     *
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
     * @return the value of the 'mem_total' grain
     */
    public long getTotalMemory() {
        return grains.getValueAsLong("mem_total").orElse(0L);
    }

    /**
     * @return the value of the 'swap_total' grain
     */
    public long getTotalSwapMemory() {
        return grains.getValueAsLong("swap_total").orElse(0L);
    }

    /**
     * Store CPU information given as a {@link ValueMap}.
     *
     * @param cpuinfo Salt returns /proc/cpuinfo data
     */
    public void mapCpuInfo(ValueMap cpuinfo) {
        final CPU cpu = Optional.ofNullable(server.getCpu()).orElseGet(CPU::new);

        // os.uname[4]
        String cpuarch = getCpuArch();

        if (StringUtils.isBlank(cpuarch)) {
            errors.add("CPU: Grain 'cpuarch' has no value");
            LOG.error("Grain 'cpuarch' has no value for minion: {}", server.getMinionId());
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
        else if (CpuArchUtil.isAarch64(cpuarch)) {
            cpu.setVendor(cpuarch);
            cpu.setModel(cpuarch);
            cpu.setBogomips(grains.getValueAsString("bogomips", 16));
            cpu.setVendor(grains.getValueAsString("cpu_vendor", 32));
            cpu.setStepping(grains.getValueAsString("cpu_stepping", 16));
            cpu.setModel(truncateModel(grains.getValueAsString("cpu_model")));
        }
        else {
            cpu.setVendor(cpuarch);
            cpu.setModel(cpuarch);
        }

        CPUArch arch = ServerFactory.lookupCPUArchByName(cpuarch);
        cpu.setArch(arch);

        cpu.setNrsocket(grains.getValueAsLong("cpu_sockets").orElse(1L));
        cpu.setNrCore(grains.getValueAsLong("cpu_cores").orElse(1L));
        cpu.setNrThread(grains.getValueAsLong("cpu_threads").orElse(1L));
        // Use our custom grain. Salt has a 'num_cpus' grain but it gives
        // the number of active CPUs not the total num of CPUs in the system.
        // On s390x this number of active and actual CPUs can be different.
        cpu.setNrCPU(grains.getValueAsLong("total_num_cpus").orElse(0L));

        if (arch != null) {
            cpu.setServer(server);
            server.setCpu(cpu);
        }
        else {
            // should not happen but cpu.arch is not nullable so if we don't have
            // the arch we cannot persist the cpu
            LOG.warn("Did not set server CPU. Could not find CPUArch in db for value '{}' for minion '{}",
                    cpuarch, server.getMinionId());
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

        ValueMap bios = new ValueMap(smbiosRecordsBios);
        ValueMap system = new ValueMap(smbiosRecordsSystem);
        ValueMap baseboard = new ValueMap(smbiosRecordsBaseboard);
        ValueMap chassis = new ValueMap(smbiosRecordsChassis);

        String biosVendor = bios.getOptionalAsString("vendor").orElse(null);
        String biosVersion = bios.getOptionalAsString("version").orElse(null);
        String biosReleseDate = bios.getOptionalAsString("release_date").orElse(null);

        String productName = system.getOptionalAsString("product_name").orElse(null);
        String systemVersion = system.getOptionalAsString("version").orElse(null);
        String systemSerial = system.getOptionalAsString("serial_number").orElse(null);

        String boardSerial = baseboard.getOptionalAsString("serial_number").orElse(null);

        String chassisSerial = chassis.getOptionalAsString("serial_number").orElse(null);
        String chassisTag = chassis.getOptionalAsString("asset_tag").orElse(null);

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
        if (biosVendor != null || biosVersion != null || biosReleseDate != null) {
            dmi.setBios(biosVendor, biosVersion, biosReleseDate);
        }
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

        if (udevdb == null || udevdb.isEmpty()) {
            errors.add("Devices: Salt module 'udevdb.exportdb' returned an empty list");
            LOG.error("Salt module 'udevdb.exportdb' returned an empty list for minion: {}", server.getMinionId());
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
                switch (subsys) {
                    case "block":
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
                        break;
                    case "pci":
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
                        break;
                    case "usb":
                        String vendorId = props.getValueAsString("ID_VENDOR_ID");
                        if (StringUtils.isNotBlank(vendorId)) {
                            device.setProp1(vendorId);
                        }
                        String modelId = props.getValueAsString("ID_MODEL_ID");
                        if (StringUtils.isNotBlank(modelId)) {
                            device.setProp2(modelId);
                        }
                        break;
                    case "scsi":
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
                        break;
                    default:
                        LOG.warn("ignore unknown subsystem {}", subsys);
                        break;
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
     * Map mainframe sysinfo to the database.
     *
     * @param readValuesOutput mainframe sysinfo as returned by mainframesysinfo.read_values
     */
    public void mapSysinfo(String readValuesOutput) {
        String cpuarch = getCpuArch();
        Map<String, String> sysvalues = new HashMap<>();
        for (String line : readValuesOutput.split("\\r?\\n")) {
            if (!line.contains(":")) {
                continue;
            }
            String[] split = StringUtils.split(line, ":", 2);
            if (split.length == 2) {
                sysvalues.put(StringUtils.trim(split[0]), StringUtils.trim(split[1]));
            }
        }

        // original code: hardware.py get_sysinfo()
        if (StringUtils.isNotBlank(sysvalues.get("Sequence Code")) &&
                StringUtils.isNotBlank(sysvalues.get("Type")) &&
                CpuArchUtil.isS390(cpuarch)) {
            // we're on a S390 mainframe and we have a
            // special case: we got info about a virtual host
            // where this system is running on

            String identifier = String.format("Z-%s", sysvalues.get("Sequence Code"));
            String os = "z/OS";
            String name = String.format("IBM Mainframe %s %s", sysvalues.get("Type"),
                    sysvalues.get("Sequence Code"));
            long totalIfls = 0L;
            try {
                totalIfls = Long.parseLong(sysvalues.getOrDefault("CPUs Total", "0"));
            }
            catch (NumberFormatException e) {
                LOG.warn("Invalid 'CPUs Total' value: {}", e.getMessage());
            }
            String type = sysvalues.get("Type");

            // register the info about the S390 host in the db

            // original code: server_hardware.py class SystemInformation
            Server zhost = ServerFactory
                    .lookupForeignSystemByDigitalServerId(identifier);

            if (zhost == null) {
                // create a new z/OS host server entry
                zhost = ServerFactory.createServer();
                // TODO extract this cpuarch + "-redhat-linux" in some common util
                zhost.setServerArch(ServerFactory
                        .lookupServerArchByLabel(cpuarch + "-redhat-linux"));
                zhost.setName(name);
                zhost.setOs(os);
                zhost.setRelease(type);
                zhost.setLastBoot(System.currentTimeMillis() / 1000);
                // see server_hardware.py SystemInformation.__init__()
                zhost.setDescription(
                        String.format("Initial Registration Parameters:\n" +
                                "OS: %s\n" +
                                "Release: %s\n" +
                                "CPU Arch: %s", os, sysvalues.get("Type"), cpuarch));

                zhost.setDigitalServerId(identifier);
                zhost.setOrg(OrgFactory.getSatelliteOrg()); // TODO clarify this
                zhost.setSecret(RandomStringUtils.random(64, 0, 0, true, true, null, new SecureRandom()));
                zhost.setAutoUpdate("N");
                zhost.setContactMethod(ServerFactory
                        .findContactMethodByLabel("default"));
                server.setLastBoot(System.currentTimeMillis() / 1000);

                ServerFactory.save(zhost);

                GlobalInstanceHolder.SYSTEM_ENTITLEMENT_MANAGER.setBaseEntitlement(zhost, EntitlementManager
                        .getByName(EntitlementManager.FOREIGN_ENTITLED));
                LOG.debug("New host created: {}", identifier);
            }

            // update checkin for new as well as already existing servers
            LOG.debug("Update server info for: {}", identifier);
            zhost.updateServerInfo();

            CPU hostcpu = zhost.getCpu();
            if (hostcpu == null || (hostcpu.getNrsocket() != null &&
                    hostcpu.getNrsocket().longValue() != totalIfls)) {
                LOG.debug("Update host cpu: {}", totalIfls);
                hostcpu = Optional.ofNullable(hostcpu).orElseGet(CPU::new);
                hostcpu.setNrCPU(totalIfls);
                hostcpu.setVersion(null);
                hostcpu.setMHz("0");
                hostcpu.setCache(null);
                hostcpu.setFamily(null);
                hostcpu.setBogomips(null);
                hostcpu.setNrsocket(totalIfls);
                hostcpu.setNrCore(totalIfls);
                hostcpu.setNrThread(1L);
                hostcpu.setArch(ServerFactory.lookupCPUArchByName(cpuarch));
                hostcpu.setFlags(null);
                hostcpu.setStepping(null);
                hostcpu.setModel(cpuarch);
                hostcpu.setVendor(type);
                zhost.setCpu(hostcpu); // TODO test if this deletes any existing CPU
                hostcpu.setServer(zhost);
            }

            VirtualInstanceFactory vinstFactory = VirtualInstanceFactory.getInstance();
            VirtualInstance vinst = vinstFactory
                    .lookupByGuestId(server.getId());
            if (vinst == null || vinst.getHostSystem() == null) {

                VirtualInstanceType fullVirtType = vinstFactory.getFullyVirtType();
                VirtualInstanceState unknownState = vinstFactory.getUnknownState();

                // first create the host
                VirtualInstance vinstHost = new VirtualInstance();
                vinstHost.setHostSystem(zhost);
                vinstHost.setGuestSystem(null);
                vinstHost.setConfirmed(1L);
                vinstHost.setUuid(null);
                vinstHost.setType(fullVirtType);
                vinstHost.setState(unknownState);
                vinstFactory.saveVirtualInstance(vinstHost);

                // create the guest
                VirtualInstance vinstGuest = new VirtualInstance();
                vinstGuest.setHostSystem(zhost);
                vinstGuest.setGuestSystem(server);
                vinstGuest.setConfirmed(1L);
                vinstGuest.setUuid(UUID.randomUUID().toString().replace("-", ""));
                vinstGuest.setType(fullVirtType);
                vinstGuest.setState(unknownState);
                vinstFactory.saveVirtualInstance(vinstGuest);
            }
            else if (!vinst.getHostSystem().getId().equals(zhost.getId())) {
                LOG.debug("Updating virtual instance {} with {}", vinst.getId(), zhost.getId());
                vinst.setHostSystem(zhost);
                vinstFactory.saveVirtualInstance(vinst);
            }
        }
    }

    /**
     * Map PAYG information for the server to the database.
     */
    public void mapPaygInfo() {
        server.setPayg(grains.getOptionalAsBoolean("is_payg_instance").orElse(false));
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
        if (!isForeign && virtualInstance.getTotalMemory() != null &&
                0 != virtualInstance.getTotalMemory()) {
            newMemory = virtualInstance.getTotalMemory();
        }
        return newMemory;
    }

    /**
     * Map virtualization information to the database.
     *
     * @param smbiosRecordsSystem optional DMI information about the system
     */
    public void mapVirtualizationInfo(Optional<Map<String, Object>> smbiosRecordsSystem) {
        String virtTypeLowerCase = StringUtils.lowerCase(
                grains.getValueAsString("virtual"));
        String virtSubtype = grains.getValueAsString("virtual_subtype");
        String instanceId = grains.getValueAsString("instance_id");
        String virtUuid = (StringUtils.isEmpty(instanceId)) ? grains.getValueAsString("uuid") : instanceId;
        int vCPUs = grains.getValueAsLong("total_num_cpus").orElse(0L).intValue();
        long memory = grains.getValueAsLong("mem_total").orElse(0L);

        if (virtTypeLowerCase == null) {
            errors.add("Virtualization: Grain 'virtual' has no value");
        }

        VirtualInstanceType type = null;

        if (VirtualizationEntitlement.isVirtualGuest(virtTypeLowerCase, virtSubtype)) {
            if (StringUtils.isNotBlank(virtUuid)) {

                virtUuid = StringUtils.remove(virtUuid, '-');

                String virtTypeLabel = virtTypeLowerCase;
                switch (virtTypeLowerCase) {
                    case "xen":
                        if ("Xen PV DomU".equals(virtSubtype)) {
                            virtTypeLabel = "para_virtualized";
                        }
                        else {
                            virtTypeLabel = "fully_virtualized";
                        }
                        break;
                    case "qemu":
                    case "kvm":
                        virtTypeLabel = "qemu";
                        break;
                    case "nitro":
                        virtTypeLabel = "aws_nitro";
                        break;
                    default:
                        LOG.info("Detected virtual instance type '{}' for minion '{}'",
                                virtTypeLabel, server.getMinionId());
                }
                if (virtSubtype.startsWith("Amazon EC2")) {
                    switch (virtTypeLowerCase) {
                        case "xen":
                            virtTypeLabel = "aws_xen";
                            break;
                        case "qemu":
                        case "kvm":
                        case "nitro":
                            virtTypeLabel = "aws_nitro";
                            break;
                        default:
                            virtTypeLabel = "aws";
                    }
                }
                type = VirtualInstanceFactory.getInstance()
                        .getVirtualInstanceType(virtTypeLabel);

                if (type == null) { // fallback
                    type = VirtualInstanceFactory.getInstance().getFullyVirtType();
                    LOG.warn(
                            "Can't find virtual instance type for string '{}'. " +
                            "Defaulting to '{}' for minion '{}'",
                            virtTypeLowerCase, type.getLabel(), server.getMinionId());
                }

            }
        }
        else if (smbiosRecordsSystem.isPresent()) {
            // there's no DMI on S390 and PPC64
            ValueMap dmiSystem = new ValueMap(smbiosRecordsSystem
                    .orElse(Collections.emptyMap()));
            String manufacturer = dmiSystem.getValueAsString("manufacturer");
            String productName = dmiSystem.getValueAsString("product_name");
            if ("HITACHI".equalsIgnoreCase(manufacturer) &&
                    productName.endsWith(" HVM LPAR")) {
                if (StringUtils.isEmpty(virtUuid)) {
                    virtUuid = "flex-guest";
                }
                type = VirtualInstanceFactory.getInstance()
                        .getVirtualInstanceType("virtage");
            }
        }

        if (type != null) {
            final VirtualInstanceType virtType = type;
            List<VirtualInstance> virtualInstances = VirtualInstanceFactory.getInstance()
                    .lookupVirtualInstanceByUuid(virtUuid);

            if (grains.getValueAsString("os_family").contentEquals("Suse") &&
                    grains.getValueAsString("osrelease").startsWith("11") &&
                        StringUtils.isEmpty(instanceId)) {
                virtUuid = fixAndReturnSle11Uuid(virtUuid);
                // Fix the "uuid" for already wrong created virtual instances
                for (VirtualInstance virtualInstance : virtualInstances) {
                    LOG.warn("Detected wrong 'uuid' for virtual instance. Coercing: [{}] -> [{}]",
                            virtualInstance.getUuid(), virtUuid);
                    VirtualInstanceFactory.getInstance()
                            .deleteVirtualInstanceOnly(virtualInstance);
                    VirtualInstanceManager.addGuestVirtualInstance(
                            virtUuid, virtualInstance.getName(), virtualInstance.getType(),
                            virtualInstance.getState(), virtualInstance.getHostSystem(),
                            virtualInstance.getGuestSystem());
                    }
                // Now collecting virtual instances with the correct uuid
                virtualInstances = VirtualInstanceFactory.getInstance()
                    .lookupVirtualInstanceByUuid(virtUuid);
            }

            if (virtualInstances.isEmpty()) {
                // For some reason, the uuid of the VM may have changed.
                // Check if we already have a VM with the same virtual_system_id.
                VirtualInstance virtualInstance = VirtualInstanceFactory.getInstance().lookupByGuestId(server.getId());
                if (virtualInstance == null) {
                    VirtualInstanceManager.addGuestVirtualInstance(
                            virtUuid, server.getName(), type,
                            VirtualInstanceFactory.getInstance().getRunningState(),
                            null, server, vCPUs, memory);
                }
                else {
                    virtualInstance.setUuid(virtUuid);
                    updateVirtualInstance(vCPUs, memory, virtType, virtualInstance);
                }
            }
            else {
                virtualInstances.forEach(
                        virtualInstance -> updateVirtualInstance(vCPUs, memory, virtType, virtualInstance));
            }
        }
    }

    /**
     * Update the virtual instance information
     * @param vCPUs virtual CPUs
     * @param memory memory
     * @param virtType virtual instance type
     * @param virtualInstance virtunalInstance to be updated
     */
    private void updateVirtualInstance(int vCPUs, long memory, VirtualInstanceType virtType,
                                       VirtualInstance virtualInstance) {
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
        List<VirtualInstance> wrongVirtualInstances = VirtualInstanceFactory
                .getInstance().lookupVirtualInstanceByUuid(virtUuid);
        wrongVirtualInstances.forEach(virtInstance ->
                VirtualInstanceFactory.getInstance()
                        .deleteVirtualInstanceOnly(virtInstance)
        );
        return virtUuidSwapped;
    }

    private void setFqdns(MinionServer serverIn, List<String> fqdns) {
        if (fqdns.isEmpty()) {
            LOG.warn("Salt module 'network.fqdns' returned en empty value for minion: {}", server.getMinionId());
        }
        else {
            Collection<ServerFQDN> serverFQDNs = serverIn.getFqdns();
            Collection<ServerFQDN> srvFqdnsObj = fqdns.stream().map(fqdn -> new ServerFQDN(serverIn, fqdn))
                    .collect(Collectors.toList());
            serverFQDNs.retainAll(srvFqdnsObj);
            serverFQDNs.addAll(srvFqdnsObj);
        }
    }

    /**
     * Store network information as returned by Salt.
     *
     * @param interfaces network interfaces
     * @param primaryIps primary IP addresses
     * @param netModules network modules
     * @param fqdns fqdns
     */
    public void mapNetworkInfo(Map<String, Network.Interface> interfaces, Optional<Map<SumaUtil.IPVersion,
            SumaUtil.IPRoute>> primaryIps, Map<String, Optional<String>> netModules, List<String> fqdns) {
        if (interfaces.isEmpty()) {
            errors.add("Network: Salt module 'network.interfaces' returned en empty value");
            LOG.error("Salt module 'network.interfaces' returned en empty value for minion: {}", server.getMinionId());
            return;
        }
        Optional<String> primaryIPv4 = primaryIps
                    .flatMap(x -> Optional.ofNullable(x.get(SumaUtil.IPVersion.IPV4)))
                    .map(SumaUtil.IPRoute::getSource)
                    .filter(addr -> !"127.0.0.1".equals(addr));
        Optional<String> primaryIPv6 = primaryIps
                    .flatMap(x -> Optional.ofNullable(x.get(SumaUtil.IPVersion.IPV6)))
                    .map(SumaUtil.IPRoute::getSource)
                    .filter(addr -> !"::1".equals(addr));

        server.setHostname(grains.getOptionalAsString("fqdn").orElse(null));
        setFqdns(server, fqdns);

        // remove interfaces not present in the Salt result
        server.getNetworkInterfaces().removeAll(
                server.getNetworkInterfaces().stream()
                        .filter(netIf -> !interfaces.containsKey(netIf.getName()))
                        .collect(Collectors.toSet()));

        // add/update interfaces from the Salt result
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
            HibernateFactory.getSession().flush();
            HibernateFactory.getSession().refresh(iface);

            List<ServerNetAddress4> dbipv4 = ServerNetworkFactory.findServerNetAddress4(iface.getInterfaceId());
            List<Network.INet> saltipv4 = Optional.ofNullable(saltIface.getInet()).orElse(new LinkedList<>());

            Set<ServerNetAddress4> dbfound = new HashSet<>();
            for (Network.INet inet : saltipv4) {
                boolean found = false;
                for (ServerNetAddress4 dbinet: dbipv4) {
                    if (inet.getAddress().orElse("").equals(dbinet.getAddress())) {
                        // update
                        dbinet.setNetmask(inet.getNetmask().orElse(null));
                        dbinet.setBroadcast(inet.getBroadcast().orElse(null));
                        found = true;
                        dbfound.add(dbinet);
                        break;
                    }
                }
                if (!found) {
                    // insert
                    ServerNetAddress4 ipv4 = new ServerNetAddress4();
                    ipv4.setInterfaceId(iface.getInterfaceId());
                    ipv4.setAddress(inet.getAddress().orElse(null));
                    ipv4.setNetmask(inet.getNetmask().orElse(null));
                    ipv4.setBroadcast(inet.getBroadcast().orElse(null));

                    ServerNetworkFactory.saveServerNetAddress4(ipv4);
                }
            }
            dbipv4.stream().filter(ipv4 -> !dbfound.contains(ipv4))
                    .forEach(ServerNetworkFactory::removeServerNetAddress4);

            List<ServerNetAddress6> dbipv6 = ServerNetworkFactory.findServerNetAddress6(iface.getInterfaceId());
            List<Network.INet6> saltipv6 = Optional.ofNullable(saltIface.getInet6()).orElse(new LinkedList<>());

            Set<ServerNetAddress6> dbfound6 = new HashSet<>();
            for (Network.INet6 inet : saltipv6) {
                boolean found = false;
                for (ServerNetAddress6 dbinet: dbipv6) {
                    if (inet.getAddress().equals(dbinet.getAddress())) {
                        // update
                        dbinet.setNetmask(inet.getPrefixlen());
                        dbinet.setScope(Optional.ofNullable(inet.getScope()).orElse("unknown"));
                        found = true;
                        dbfound6.add(dbinet);
                        break;
                    }
                }
                if (!found) {
                    // insert
                    ServerNetAddress6 ipv6 = new ServerNetAddress6();
                    ipv6.setInterfaceId(iface.getInterfaceId());
                    ipv6.setAddress(inet.getAddress());
                    ipv6.setNetmask(inet.getPrefixlen());
                    ipv6.setScope(Optional.ofNullable(inet.getScope()).orElse("unknown"));

                    ServerNetworkFactory.saveServerNetAddress6(ipv6);
                }
            }
            dbipv6.stream().filter(ipv6 -> !dbfound6.contains(ipv6))
                    .forEach(ServerNetworkFactory::removeServerNetAddress6);
        });

        // reset primary IP flag, we will re-compute it
        server.getNetworkInterfaces().forEach(n -> n.setPrimary(null));

        // find the interface having primary IPv4 addr
        Optional<NetworkInterface> primaryNetIf = primaryIPv4.flatMap(pipv4 ->
            server.getNetworkInterfaces().stream()
                .filter(netIf -> netIf.getIPv4Addresses().stream()
                        .anyMatch(addr -> Objects.equals(pipv4, addr.getAddress())))
                .findFirst());

        if (primaryNetIf.isEmpty()) {
            // no primary IPv4, fallback to IPv6
            primaryNetIf = primaryIPv6.flatMap(pipv6 ->
                server.getNetworkInterfaces().stream()
                    .filter(netIf -> netIf.getIPv6Addresses().stream()
                            .anyMatch(addr -> Objects.equals(pipv6, addr.getAddress())))
                    .findFirst());
        }

        // we found an interface with the same addr as the
        // primary IPv4/v6 addr, make it primary
        primaryNetIf.ifPresent(server::setPrimaryInterface);

        // set primary FQDN to hostname if no primary FQDN is specified
        if (StringUtils.isNotBlank(server.getHostname()) && server.getFqdns().stream()
               .noneMatch(ServerFQDN::isPrimary)) {

            server.setPrimaryFQDNWithName(server.getHostname());
        }
    }

    private Optional<NetworkInterface> firstNetIf(Server serverIn) {
        return serverIn.getNetworkInterfaces().stream()
                .filter(this::notLocalhost)
                // just sort alphabetically. eth0 should come first
                .sorted((if1, if2) -> ObjectUtils.compare(if1.getName(), if2.getName()))
                .findFirst();
    }

    private boolean notLocalhost(NetworkInterface netIf) {
        return !netIf.getIPv4Addresses().stream()
                .anyMatch(addr -> "127.0.0.1".equals(addr.getAddress())) &&
                !netIf.getIPv6Addresses().stream()
                .anyMatch(addr -> "::1".equals(addr.getAddress()));
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

        switch (subsys) {
            case "pci":
                String pciId = attrs.getValueAsString("PCI_ID");
                String[] ids = pciId.split(":");
                String pciVendorDesc = ids.length > 0 ? ids[0] : "";
                String pciDeviceDesc = ids.length > 1 ? ids[1] : "";

                if (StringUtils.isNotBlank(vendorFromDb) ||
                        StringUtils.isNotBlank(modelFromDb)) {
                    result = String.format("%s|%s", vendorFromDb, modelFromDb);
                }
                else {
                    // TODO lookup in hwdata
                    result = String.format("%s|%s", pciVendorDesc, pciDeviceDesc);
                }
                break;
            case "usb":
                String vendorId = attrs.getValueAsString("ID_VENDOR_ID");
                String product = attrs.getValueAsString("PRODUCT");
                if (StringUtils.isNotBlank(vendorId)) {
                    String usbDeviceDesc = attrs.getValueAsString("ID_MODEL_ID");

                    if (StringUtils.isNotBlank(vendorFromDb) ||
                            StringUtils.isNotBlank(modelFromDb)) {
                        result = String.format("%s|%s", vendorFromDb, modelFromDb);
                    }
                    else {
                        // TODO lookup in hwdata
                        result = String.format("%s|%s", vendorId, usbDeviceDesc);
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
                        String usbVendorDesc = p.length > 0 ?
                                String.format("%04x", Integer.parseInt(p[0], 16)) : "";
                        String usbDeviceDesc = p.length > 1 ?
                                String.format("%04x", Integer.parseInt(p[1], 16)) : "";

                        // TODO lookup in hwdata
                        result = String.format("%s|%s", usbVendorDesc, usbDeviceDesc);
                    }
                }
                break;
            case "block":
                result = attrs.getValueAsString("ID_MODEL");
                break;
            default:
                LOG.info("ignore unknown subsystem {}", subsys);
                break;
        }

        return StringUtils.isNotBlank(result) ? result : null;
    }

    private String classifyClass(String minionId, Map<String, Object> device) {
        String sysfsPath = (String)device.get(SYSFS_PATH);
        @SuppressWarnings("unchecked")
        ValueMap attrs = new ValueMap((Map<String, Object>) device.get(ENTRIES));
        ValueMap extraAttrs = new ValueMap();
        if (device.get(EXTRA_ENTRIES) != null) {
            extraAttrs = new ValueMap((Map<String, Object>) device.get(EXTRA_ENTRIES));
        }

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
            else if (baseClass.equals(PciClassCodes.PCI_BASE_CLASS_INPUT.getCode()) &&
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
                long devType = extraAttrs.getValueAsLong("SCSI_SYS_TYPE").orElse(-1L);
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
