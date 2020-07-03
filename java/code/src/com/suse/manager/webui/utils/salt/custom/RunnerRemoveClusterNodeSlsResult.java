/**
 * Copyright (c) 2020 SUSE LLC
 * <p>
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 * <p>
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.manager.webui.utils.salt.custom;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.suse.salt.netapi.results.Ret;
import com.suse.salt.netapi.results.StateApplyResult;

import java.util.Map;

public class RunnerRemoveClusterNodeSlsResult {

    @SerializedName("salt_|-mgr_remove_nodes_|-mgr_remove_nodes_|-state")
    private StateApplyResult<Ret<Map<String, Map<String, StateApplyResult<Ret<JsonElement>>>>>> removeNodes;

    /**
     * mgmt node -> module_|-mgr_cluster_remove_node_dev-min-caasp-worker-3.lan_|-mgrclusters.remove_node_|-run ->
     * @return removeNodes to get
     */
    public StateApplyResult<Ret<Map<String, Map<String, StateApplyResult<Ret<JsonElement>>>>>> getRemoveNodes() {
        return removeNodes;
    }
}
