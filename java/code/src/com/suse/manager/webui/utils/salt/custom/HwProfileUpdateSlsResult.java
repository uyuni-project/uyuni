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
import com.suse.salt.netapi.results.Ret;
import com.suse.salt.netapi.results.StateApplyResult;

import com.google.gson.annotations.SerializedName;

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

    @SerializedName("module_|-network-interfaces_|-network.interfaces_|-run")
    private StateApplyResult<Ret<Map<String, Network.Interface>>> networkInterfaces;

    @SerializedName("module_|-network-ips_|-sumautil.primary_ips_|-run")
    private StateApplyResult<Ret<Map<SumaUtil.IPVersion, SumaUtil.IPRoute>>> networkIPs;

    @SerializedName("module_|-network-modules_|-sumautil.get_net_modules_|-run")
    private StateApplyResult<Ret<Map<String, Optional<String>>>> networkModules;

    /**
     * @return the grains
     */
    public StateApplyResult<Ret<Map<String, Object>>> getGrains() {
        return grains;
    }

    /**
     * @return information about CPUs
     */
    public StateApplyResult<Ret<Map<String, Object>>> getCpuInfo() {
        return cpuInfo;
    }

    /**
     * @return network interfaces
     */
    public StateApplyResult<Ret<Map<String, Network.Interface>>> getNetworkInterfaces() {
        return networkInterfaces;
    }

    /**
     * @return network IPs
     */
    public StateApplyResult<Ret<Map<SumaUtil.IPVersion, SumaUtil.IPRoute>>>
            getNetworkIPs() {
        return networkIPs;
    }

    /**
     * @return network modules
     */
    public StateApplyResult<Ret<Map<String, Optional<String>>>> getNetworkModules() {
        return networkModules;
    }
}
