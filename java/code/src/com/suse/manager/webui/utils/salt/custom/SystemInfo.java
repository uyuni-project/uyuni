/*
 * Copyright (c) 2019 SUSE LLC
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

import com.suse.manager.reactor.utils.ValueMap;
import com.suse.salt.netapi.results.Ret;
import com.suse.salt.netapi.results.StateApplyResult;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represent grains data of a minion
 */
public class SystemInfo {

    @SerializedName("mgrcompat_|-status_uptime_|-status.uptime_|-module_run")
    private StateApplyResult<Ret<Map<String, Object>>> upTime;
    @SerializedName("mgrcompat_|-grains_update_|-grains.item_|-module_run")
    private StateApplyResult<Ret<Map<String, Object>>> grains;
    @SerializedName("mgrcompat_|-kernel_live_version_|-sumautil.get_kernel_live_version_|-module_run")
    private StateApplyResult<Ret<KernelLiveVersionInfo>> kernelLiveVersion;

    /**
     * Gets grains in key/value pair.
     *
     * @return the grains
     */
    private ValueMap getGrains() {
        return new ValueMap(grains != null ? grains.getChanges().getRet() : new HashMap<>());
    }

    /**
     * Gets the uptime of minion in seconds returned by status.uptime modules
     *
     * @return the grains
     */
    public Optional<Number> getUptimeSeconds() {
       return upTime != null ?  Optional.of((Number)upTime.getChanges().getRet().get("seconds")) : Optional.empty();
    }

    /**
     * Get the kernelrelease grain
     * @return kernelrelease grain
     */
    public Optional<String> getKerneRelese() {
       return getGrains().getOptionalAsString("kernelrelease");
    }

    /**
     * Get the kernel live patch version, if exists
     *
     * @return the kernel live patch version
     */
    public Optional<String> getKernelLiveVersion() {
        if (kernelLiveVersion == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(kernelLiveVersion.getChanges().getRet())
                .map(KernelLiveVersionInfo::getKernelLiveVersion);
    }

    /**
     * Get the master grain
     * @return master grain
     */
    public Optional<String> getMaster() {
       return getGrains().getOptionalAsString("master");
    }
}
