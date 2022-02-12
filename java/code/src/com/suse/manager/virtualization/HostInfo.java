/*
 * Copyright (c) 2021 SUSE LLC
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
package com.suse.manager.virtualization;


import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Represents virtualization host informations
 */
public class HostInfo {
    private String hypervisor;

    @SerializedName("cluster_other_nodes")
    private List<String> clusterOtherNodes = List.of();

    /**
     * @return value of hypervisor
     */
    public String getHypervisor() {
        return hypervisor;
    }

    /**
     * @param hypervisorIn value of hypervisor
     */
    public void setHypervisor(String hypervisorIn) {
        hypervisor = hypervisorIn;
    }

    /**
     * @return value of clusterOtherNodes
     */
    public List<String> getClusterOtherNodes() {
        return clusterOtherNodes;
    }

    /**
     * @param clusterOtherNodesIn value of clusterOtherNodes
     */
    public void setClusterOtherNodes(List<String> clusterOtherNodesIn) {
        clusterOtherNodes = clusterOtherNodesIn;
    }
}
