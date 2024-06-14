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
package com.suse.manager.webui.utils.salt.custom;

import com.suse.salt.netapi.calls.modules.Network;
import com.suse.salt.netapi.calls.modules.Smbios;
import com.suse.salt.netapi.results.Ret;
import com.suse.salt.netapi.results.StateApplyResult;

import com.google.gson.annotations.SerializedName;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Object representation of the results of a call to state.apply hardware.profileupdate.
 */
public class HwProfileUpdateSlsResult {

    private static final Logger LOG = LogManager.getLogger(HwProfileUpdateSlsResult.class);

    @SerializedName("mgrcompat_|-grains_|-grains.items_|-module_run")
    private StateApplyResult<Ret<Map<String, Object>>> grains;

    @SerializedName("mgrcompat_|-cpuinfo_|-status.cpuinfo_|-module_run")
    private StateApplyResult<Ret<Map<String, Object>>> cpuInfo;

    @SerializedName(value = "mgrcompat_|-udev_|-udev.exportdb_|-module_run",
            alternate = {"mgrcompat_|-udevdb_|-udevdb.exportdb_|-module_run"})
    private StateApplyResult<Ret<List<Map<String, Object>>>> udevdb;

    @SerializedName("mgrcompat_|-network-interfaces_|-network.interfaces_|-module_run")
    private StateApplyResult<Ret<Map<String, Network.Interface>>> networkInterfaces;

    @SerializedName("mgrcompat_|-network-ips_|-sumautil.primary_ips_|-module_run")
    private StateApplyResult<Ret<Map<SumaUtil.IPVersion, SumaUtil.IPRoute>>> networkIPs;

    @SerializedName("mgrcompat_|-network-modules_|-sumautil.get_net_modules_|-module_run")
    private StateApplyResult<Ret<Map<String, Optional<String>>>> networkModules;

    @SerializedName("mgrcompat_|-instance-flavor_|-sumautil.instance_flavor_|-module_run")
    private StateApplyResult<Ret<Optional<String>>> instanceFlavor;

    @SerializedName("mgrcompat_|-dns_fqdns_|-mgrnet.dns_fqdns_|-module_run")
    private Optional<StateApplyResult<Optional<Ret<Map<String, List<String>>>>>> fqdnsFromMgrNetModule =
            Optional.empty();

    @SerializedName("mgrcompat_|-fqdns_|-network.fqdns_|-module_run")
    private Optional<StateApplyResult<Ret<Map<String, List<String>>>>> fqdnsFromNetworkModule =
            Optional.empty();

    @SerializedName("mgrcompat_|-smbios-records-bios_|-smbios.records_|-module_run")
    private Optional<StateApplyResult<Ret<List<Smbios.Record>>>> smbiosRecordsBios =
            Optional.empty();

    @SerializedName("mgrcompat_|-smbios-records-system_|-smbios.records_|-module_run")
    private Optional<StateApplyResult<Ret<List<Smbios.Record>>>> smbiosRecordsSystem =
            Optional.empty();

    @SerializedName("mgrcompat_|-smbios-records-baseboard_|-smbios.records_|-module_run")
    private Optional<StateApplyResult<Ret<List<Smbios.Record>>>> smbiosRecordsBaseboard =
            Optional.empty();

    @SerializedName("mgrcompat_|-smbios-records-chassis_|-smbios.records_|-module_run")
    private Optional<StateApplyResult<Ret<List<Smbios.Record>>>> smbiosRecordsChassis =
            Optional.empty();

    @SerializedName("mgrcompat_|-mainframe-sysinfo_|-mainframesysinfo.read_values_|-module_run")
    private Optional<StateApplyResult<Ret<String>>> mainframeSysinfo = Optional.empty();

    /**
     * @return the grains
     */
    public Map<String, Object> getGrains() {
        if (grains == null) {
            LOG.warn("grains value is null");
            return Collections.emptyMap();
        }
        return grains.getChanges().getRet();
    }

    /**
     * @return information about CPUs
     */
    public Map<String, Object> getCpuInfo() {
        if (cpuInfo == null) {
            LOG.warn("cpuInfo value is null");
            return Collections.emptyMap();
        }
        return cpuInfo.getChanges().getRet();
    }

    /**
     * @return exported contents of the udevdb
     */
    public List<Map<String, Object>> getUdevdb() {
        if (udevdb == null) {
            LOG.warn("udevdb value is null");
            return Collections.emptyList();
        }
        return udevdb.getChanges().getRet();
    }

    /**
     * @return network interfaces
     */
    public Map<String, Network.Interface> getNetworkInterfaces() {
        if (networkInterfaces == null) {
            LOG.warn("networkInterfaces value is null");
            return Collections.emptyMap();
        }
        return networkInterfaces.getChanges().getRet();
    }

    /**
     * @return network IPs
     */
    public Map<SumaUtil.IPVersion, SumaUtil.IPRoute> getNetworkIPs() {
        if (networkIPs == null) {
            LOG.warn("networkIPs value is null");
            return Collections.emptyMap();
        }
        return networkIPs.getChanges().getRet();
    }

    /**
     * @return network modules
     */
    public Map<String, Optional<String>> getNetworkModules() {
        if (networkModules == null) {
            LOG.warn("networkModules value is null");
            return Collections.emptyMap();
        }
        return networkModules.getChanges().getRet();
    }

    /**
     * @return the flavor of the instance
     */
    public Optional<String> getInstanceFlavor() {
        if (instanceFlavor == null) {
            return Optional.empty();
        }
        return instanceFlavor.getChanges().getRet();
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

    /**
     * Get the fqdns from network.fqdns module
     * and if module is not available then fall back to 'fqdns' grain.
     * @return fqdns of the system
     */
    public List<String> getFqdns() {
       return fqdnsFromNetworkModule.map(s->s.getChanges().getRet().get("fqdns"))
               .orElseGet(()-> (List<String>)this.getGrains().get("fqdns"));
     }

    /**
     * Get the fqdns from mgrnet.dns_fqdns module
     * @return fqdns from dns query
     */
    public List<String> getDnsFqdns() {
        return fqdnsFromMgrNetModule
                .flatMap(s -> s.getChanges())
                .map(c -> c.getRet())
                .map(s -> s.get("dns_fqdns"))
                .orElseGet(Collections::emptyList);
    }

    private Optional<Map<String, Object>> getSmbiosRecords(
            Optional<StateApplyResult<Ret<List<Smbios.Record>>>> smbiosRecords) {
        return smbiosRecords
                .map(StateApplyResult::getChanges)
                .map(Ret::getRet)
                .map(records -> records.isEmpty() ?
                        Collections.emptyMap() : records.get(0).getData());
    }


    /**
     * Get custom defined fqdns in grains that are useful for
     * fqdns that don't support reverse lookup from ip to fqdn.
     *
     * @return list of custom fqdns.
     */
    public List<String> getCustomFqdns() {
        return Optional.ofNullable(this.getGrains().get("susemanager")).flatMap(susemanager -> Optional.ofNullable(
                (List<String>)((Map<String, Object>)susemanager).get("custom_fqdns")
        )).orElseGet(Collections::emptyList);
    }

}
