/*
 * Copyright (c) 2016--2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.reactor.hardware;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.server.CPU;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.server.VirtualInstanceState;
import com.redhat.rhn.domain.server.VirtualInstanceType;
import com.redhat.rhn.manager.entitlement.EntitlementManager;

import com.suse.manager.reactor.hardware.cpu.CpuInfoMapper;
import com.suse.manager.reactor.hardware.device.DeviceSynchronizer;
import com.suse.manager.reactor.hardware.dmi.DmiMapper;
import com.suse.manager.reactor.hardware.network.NetworkMapper;
import com.suse.manager.reactor.hardware.virtualization.VirtualizationMapper;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.services.SaltGrains;
import com.suse.manager.webui.utils.salt.custom.SumaUtil;
import com.suse.salt.netapi.calls.modules.Network;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Store minion hardware details in the SUSE Manager database.
 */
public class HardwareMapper {

    // Logger for this class
    private static final Logger LOG = LogManager.getLogger(HardwareMapper.class);

    private final MinionServer server;
    private final ValueMap grains;
    private final List<String> errors = new LinkedList<>();

    private final CpuInfoMapper cpuMapper;
    private final DmiMapper dmiMapper;
    private final NetworkMapper networkMapper;
    private final DeviceSynchronizer deviceSynchronizer;
    private final VirtualizationMapper virtualizationMapper;

    /**
     * Create a hardware mapper for a given server with grains.
     *
     * @param serverIn the minion server
     * @param grainsIn the grains
     */
    public HardwareMapper(MinionServer serverIn, ValueMap grainsIn) {
        this.server = serverIn;
        this.grains = grainsIn;
        this.cpuMapper = new CpuInfoMapper(serverIn, grains);
        this.dmiMapper = new DmiMapper(serverIn);
        this.networkMapper = new NetworkMapper(server, grains);
        this.deviceSynchronizer = new DeviceSynchronizer(server);
        this.virtualizationMapper = new VirtualizationMapper(server, grains);
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
     * Map CPU information from grains and cpuInfo.
     *
     * @param cpuInfo CPU info value map
     */
    public void mapCpuInfo(ValueMap cpuInfo) {
        cpuMapper.mapCpuInfo(cpuInfo).ifPresent(errors::add);
    }

    /**
     * Store DMI info as queried from Salt.
     *
     * @param smbiosRecordsBios smbios records of type "BIOS"
     * @param smbiosRecordsSystem smbios records of type "System"
     * @param smbiosRecordsBaseboard smbios records of type "Baseboard"
     * @param smbiosRecordsChassis smbios records of type "Chassis"
     */
    public void mapDmiInfo(
            Map<String, Object> smbiosRecordsBios,
            Map<String, Object> smbiosRecordsSystem,
            Map<String, Object> smbiosRecordsBaseboard,
            Map<String, Object> smbiosRecordsChassis
    ) {
        dmiMapper.mapDmiInfo(
                smbiosRecordsBios,
                smbiosRecordsSystem,
                smbiosRecordsBaseboard,
                smbiosRecordsChassis
        ).ifPresent(errors::add);
    }

    /**
     * Map devices as found in exported udevdb returned from Salt.
     *
     * @param udevdb exported contents of udevdb
     */
    public void mapDevices(List<Map<String, Object>> udevdb) {
        deviceSynchronizer.mapDevices(udevdb).ifPresent(errors::add);
    }

    /**
     * Map mainframe sysinfo to the database.
     *
     * @param readValuesOutput mainframe sysinfo as returned by mainframesysinfo.read_values
     */
    Map<String, String> getSysValuesMap(String readValuesOutput) {
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
        return sysvalues;
    }

    protected String getOsStringForS390Arch(Map<String, String> sysvalues) {
        return sysvalues.entrySet().stream()
                .filter(e -> e.getKey().toLowerCase().contains("control program"))
                .map(Map.Entry::getValue)
                .findFirst().orElse("z/VM");
    }

    //package-protected
    String computeOsStringForS390Arch(Map<String, String> sysvalues) {
        String osString = getOsStringForS390Arch(sysvalues);
        int index = osString.indexOf(" ");
        return index > 0 ? osString.substring(0, index) : osString;
    }

    //package-protected
    String computeOsVersionStringForS390Arch(Map<String, String> sysvalues) {
        String osString = getOsStringForS390Arch(sysvalues);
        int index = osString.indexOf(" ");
        return index > 0 ? osString.substring(index).replace(" ", "") : "N/A";
    }

    //package-protected
    String getS390ServerFamily(String type) {
        // z17, z16, z15: https://www.ibm.com/docs/en/zos/3.1.0?topic=system-identifying-server-requirements
        // other codes: https://www.ibm.com/support/pages/processor-version-codes-and-srm-constants

        return switch (type) {
            case "9175" -> "z17";
            case "3931", "3932" -> "z16";
            case "8561", "8562" -> "z15";
            case "3906", "3907" -> "z14";
            case "2964", "2965" -> "z13";
            case "2827", "2828" -> "z12";
            case "2817", "2818" -> "zEnterprise 114";
            case "2097", "2098" -> "z10";
            case "2094", "2096" -> "z9";
            default -> "";
        };
    }

    /**
     * Map mainframe sysinfo to the database.
     *
     * @param readValuesOutput mainframe sysinfo as returned by mainframesysinfo.read_values
     */
    public void mapSysinfo(String readValuesOutput) {
        String cpuarch = getCpuArch();
        Map<String, String> sysvalues = getSysValuesMap(readValuesOutput);

        // original code: hardware.py get_sysinfo()
        if (StringUtils.isNotBlank(sysvalues.get("Sequence Code")) &&
                StringUtils.isNotBlank(sysvalues.get("Type")) &&
                CpuArchUtil.isS390(cpuarch)) {
            // we're on a S390 mainframe and we have a
            // special case: we got info about a virtual host
            // where this system is running on

            String identifier = String.format("Z-%s", sysvalues.get("Sequence Code"));
            String os = computeOsStringForS390Arch(sysvalues);
            String osVersion = computeOsVersionStringForS390Arch(sysvalues);
            String type = sysvalues.get("Type");

            String name = String.format("IBM Mainframe %s %s %s",
                    getS390ServerFamily(type),
                    type,
                    sysvalues.get("Sequence Code"));
            long totalIfls = 0L;
            try {
                totalIfls = Long.parseLong(sysvalues.getOrDefault("CPUs Total", "0"));
            }
            catch (NumberFormatException e) {
                LOG.warn("Invalid 'CPUs Total' value: {}", e.getMessage());
            }

            // register the info about the S390 host in the db

            // original code: server_hardware.py class SystemInformation
            Server zhost = ServerFactory
                    .lookupForeignSystemByDigitalServerId(identifier);

            if (zhost == null) {
                // create a new z/OS host server entry
                zhost = ServerFactory.createServer();
                // OLDTODO extract this cpuarch + "-redhat-linux" in some common util
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
                                "CPU Arch: %s", os, osVersion, cpuarch));

                zhost.setDigitalServerId(identifier);
                zhost.setOrg(OrgFactory.getSatelliteOrg()); // OLDTODO clarify this
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
                zhost.setCpu(hostcpu); // OLDTODO test if this deletes any existing CPU
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
     * Map virtualization information to the database.
     *
     * @param smbiosRecordsSystem optional DMI information about the system
     */
    public void mapVirtualizationInfo(Optional<Map<String, Object>> smbiosRecordsSystem) {
        virtualizationMapper.mapVirtualizationInfo(smbiosRecordsSystem).ifPresent(errors::add);
    }

    /**
     * Store network information as returned by Salt.
     *
     * @param interfaces network interfaces
     * @param primaryIps primary IP addresses
     * @param netModules network modules
     * @param fqdns fqdns
     */
    public void mapNetworkInfo(
            Map<String, Network.Interface> interfaces,
            Optional<Map<SumaUtil.IPVersion, SumaUtil.IPRoute>> primaryIps,
            Map<String, Optional<String>> netModules,
            List<String> fqdns
    ) {
        networkMapper.mapNetworkInfo(interfaces, primaryIps, netModules, fqdns).ifPresent(errors::add);
    }

    /**
     * Return a (possibly empty) list of error messages.
     *
     * @return error messages
     */
    public List<String> getErrors() {
        return errors;
    }

}
