/**
 * Copyright (c) 2020 SUSE LLC
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
import com.suse.salt.netapi.results.Ret;
import com.suse.salt.netapi.results.StateApplyResult;

import java.util.Map;

public class ClusterUpgradePlanSlsResult {

    @SerializedName("mgrcompat_|-mgr_cluster_upgrade_cluster_|-mgrclusters.upgrade_cluster_|-module_run")
    private StateApplyResult<Ret<Map<String, Object>>> upgradeResult;

    /**
     * @return upgradeResult to get
     */
    public StateApplyResult<Ret<Map<String, Object>>> getUpgradeResult() {
        return upgradeResult;
    }

    /**
     * @param upgradeResultIn to set
     */
    public void setUpgradeResult(StateApplyResult<Ret<Map<String, Object>>> upgradeResultIn) {
        this.upgradeResult = upgradeResultIn;
    }
}
