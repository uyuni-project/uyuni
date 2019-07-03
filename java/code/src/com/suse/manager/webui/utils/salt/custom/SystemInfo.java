/**
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

import com.google.gson.annotations.SerializedName;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.salt.netapi.results.Ret;
import com.suse.salt.netapi.results.StateApplyResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represent grains data of a minion
 */
public class SystemInfo {

    @SerializedName("module_|-status_uptime_|-status.uptime_|-run")
    private StateApplyResult<Ret<Map<String, Object>>> upTime;
    @SerializedName("module_|-grains_update_|-grains.item_|-run")
    private StateApplyResult<Ret<Map<String, Object>>> grains;

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
}
