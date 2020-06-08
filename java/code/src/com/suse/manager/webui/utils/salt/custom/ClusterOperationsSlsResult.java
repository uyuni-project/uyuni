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

import com.suse.salt.netapi.results.Ret;
import com.suse.salt.netapi.results.StateApplyResult;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Object representation of the results of a call to state.apply
 * clusters.addnode or clusters.removenode
 */
public class ClusterOperationsSlsResult {
    @SerializedName("module_|-mgr_cluster_add_node_|-mgrclusters.add_node_|-run")
    private StateApplyResult<Ret<Map<String, Map<String, Object>>>> addNodeResult;

    @SerializedName("module_|-mgr_cluster_remove_nodes_|-mgrclusters.remove_node_|-run")
    private StateApplyResult<Ret<Map<String, Map<String, Object>>>> removeNodeResult;

    @SerializedName("module_|-mgr_cluster_list_nodes_|-mgrclusters.list_nodes_|-run")
    private StateApplyResult<Ret<Map<String, Map<String, Object>>>> listNodesResult;

    @SerializedName("module_|-mgr_cluster_list_nodes_|-mgrclusters.upgradecluster_|-run")
    private StateApplyResult<Ret<Map<String, Map<String, Object>>>> upgradeResult;

    /**
     * @return add node result
     */
    public StateApplyResult<Ret<Map<String, Map<String, Object>>>> getAddNodeResult() {
        return addNodeResult;
    }

    /**
     * @return remove node result
     */
    public StateApplyResult<Ret<Map<String, Map<String, Object>>>> getRemoveNodeResult() {
        return removeNodeResult;
    }

    /**
     * @return list nodes result
     */
    public StateApplyResult<Ret<Map<String, Map<String, Object>>>> listNodesResult() {
        return listNodesResult;
    }

    /**
     * @return upgrade result
     */
    public StateApplyResult<Ret<Map<String, Map<String, Object>>>> getUpgradeResult() {
        return upgradeResult;
    }
}
