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
package com.suse.manager.webui.utils.salt.custom;

import com.suse.salt.netapi.calls.modules.Network;
import com.suse.salt.netapi.calls.modules.Smbios;
import com.suse.salt.netapi.results.Ret;
import com.suse.salt.netapi.results.StateApplyResult;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Object representation of the results of a call to state.apply hardware.profileupdate.
 */
public class HwProfileUpdateSlsResult {

    @SerializedName("module_|-grains_|-grains.items_|-run")
    private StateApplyResult<Ret<Map<String, Object>>> grains;

    @SerializedName("module_|-cpuinfo_|-status.cpuinfo_|-run")
    private StateApplyResult<Ret<Map<String, Object>>> cpuInfo;

    @SerializedName("module_|-udev_|-udev.exportdb_|-run")
    private StateApplyResult<Ret<List<Map<String, Object>>>> udevdb;

    @SerializedName("module_|-network-interfaces_|-network.interfaces_|-run")
    private StateApplyResult<Ret<Map<String, Network.Interface>>> networkInterfaces;

    @SerializedName("module_|-network-ips_|-sumautil.primary_ips_|-run")
    private StateApplyResult<Ret<Map<SumaUtil.IPVersion, SumaUtil.IPRoute>>> networkIPs;

    @SerializedName("module_|-network-modules_|-sumautil.get_net_modules_|-run")
    private StateApplyResult<Ret<Map<String, Optional<String>>>> networkModules;

    @SerializedName("module_|-smbios-records-bios_|-smbios.records_|-run")
    private Optional<StateApplyResult<Ret<List<Smbios.Record>>>> smbiosRecordsBios =
            Optional.empty();

    @SerializedName("module_|-smbios-records-system_|-smbios.records_|-run")
    private Optional<StateApplyResult<Ret<List<Smbios.Record>>>> smbiosRecordsSystem =
            Optional.empty();

    @SerializedName("module_|-smbios-records-baseboard_|-smbios.records_|-run")
    private Optional<StateApplyResult<Ret<List<Smbios.Record>>>> smbiosRecordsBaseboard =
            Optional.empty();

    @SerializedName("module_|-smbios-records-chassis_|-smbios.records_|-run")
    private Optional<StateApplyResult<Ret<List<Smbios.Record>>>> smbiosRecordsChassis =
            Optional.empty();

    @SerializedName("module_|-mainframe-sysinfo_|-mainframesysinfo.read_values_|-run")
    private Optional<StateApplyResult<Ret<String>>> mainframeSysinfo = Optional.empty();

    /**
     * @return the grains
     */
    public Map<String, Object> getGrains() {
        return grains.getChanges().getRet();
    }

    /**
     * @return information about CPUs
     */
    public Map<String, Object> getCpuInfo() {
        return cpuInfo.getChanges().getRet();
    }

    /**
     * @return exported contents of the udevdb
     */
    public List<Map<String, Object>> getUdevdb() {
        return udevdb.getChanges().getRet();
    }

    /**
     * @return network interfaces
     */
    public Map<String, Network.Interface> getNetworkInterfaces() {
        return networkInterfaces.getChanges().getRet();
    }

    /**
     * @return network IPs
     */
    public Map<SumaUtil.IPVersion, SumaUtil.IPRoute> getNetworkIPs() {
        return networkIPs.getChanges().getRet();
    }

    /**
     * @return network modules
     */
    public Map<String, Optional<String>> getNetworkModules() {
        return networkModules.getChanges().getRet();
    }

    /**
     * @return smbios records of type: BIOS
     */
    public Optional<Map<String, Object>> getSmbiosRecordsBios() {
        return getSmbiosRecords(smbiosRecordsBios);
    }

    /**
     * @return smbios records of type: System
     */
    public Optional<Map<String, Object>> getSmbiosRecordsSystem() {
        return getSmbiosRecords(smbiosRecordsSystem);
    }

    /**
     * @return smbios records of type: Baseboard
     */
    public Optional<Map<String, Object>> getSmbiosRecordsBaseboard() {
        return getSmbiosRecords(smbiosRecordsBaseboard);
    }

    /**
     * @return smbios records of type: Chassis
     */
    public Optional<Map<String, Object>> getSmbiosRecordsChassis() {
        return getSmbiosRecords(smbiosRecordsChassis);
    }

    /**
     * @return mainframe sysinfo or empty string if not present
     */
    public String getMainframeSysinfo() {
        if (mainframeSysinfo.isPresent()) {
            return mainframeSysinfo.get().getChanges().getRet();
        }
        return "";
    }

    private Optional<Map<String, Object>> getSmbiosRecords(
            Optional<StateApplyResult<Ret<List<Smbios.Record>>>> smbiosRecords) {
        return smbiosRecords
                .map(result -> result.getChanges())
                .map(changes -> changes.getRet())
                .map(records -> records.isEmpty() ?
                        Collections.emptyMap() : records.get(0).getData());
    }

}
