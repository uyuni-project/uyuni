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

import com.redhat.rhn.domain.server.MinionServer;

import com.suse.manager.reactor.hardware.cpu.CpuInfoMapper;
import com.suse.manager.reactor.hardware.device.DeviceSynchronizer;
import com.suse.manager.reactor.hardware.dmi.DmiMapper;
import com.suse.manager.reactor.hardware.network.NetworkMapper;
import com.suse.manager.reactor.hardware.sysinfo.SysinfoMapper;
import com.suse.manager.reactor.hardware.virtualization.VirtualizationMapper;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.services.SaltGrains;
import com.suse.manager.webui.utils.salt.custom.SumaUtil;
import com.suse.salt.netapi.calls.modules.Network;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Store minion hardware details in the SUSE Manager database.
 */
public class HardwareMapper {

    private final ValueMap grains;
    private final List<String> errors = new LinkedList<>();

    private final CpuInfoMapper cpuMapper;
    private final DmiMapper dmiMapper;
    private final NetworkMapper networkMapper;
    private final DeviceSynchronizer deviceSynchronizer;
    private final SysinfoMapper sysinfoMapper;
    private final VirtualizationMapper virtualizationMapper;

    /**
     * Create a hardware mapper for a given server with grains.
     *
     * @param serverIn the minion server
     * @param grainsIn the grains
     */
    public HardwareMapper(MinionServer serverIn, ValueMap grainsIn) {
        this.grains = grainsIn;
        this.cpuMapper = new CpuInfoMapper(serverIn, grains);
        this.dmiMapper = new DmiMapper(serverIn);
        this.networkMapper = new NetworkMapper(serverIn, grains);
        this.deviceSynchronizer = new DeviceSynchronizer(serverIn);
        this.sysinfoMapper = new SysinfoMapper(serverIn, grains);
        this.virtualizationMapper = new VirtualizationMapper(serverIn, grains);
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
    public void mapSysinfo(String readValuesOutput) {
        sysinfoMapper.mapSysinfo(readValuesOutput).ifPresent(errors::add);
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
