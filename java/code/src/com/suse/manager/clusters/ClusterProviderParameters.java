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

package com.suse.manager.clusters;

import java.util.Map;
import java.util.Optional;

/**
 * Parameters for cluster provider queries.
 */
public class ClusterProviderParameters {
    private String clusterProvider;
    private Optional<Map<String, Object>> clusterParams;

    /**
     * @param clusterProviderIn cluster type
     * @param clusterParamsIn
     */
    public ClusterProviderParameters(String clusterProviderIn, Optional<Map<String, Object>> clusterParamsIn) {
        this.clusterProvider = clusterProviderIn;
        this.clusterParams = clusterParamsIn;
    }

    /**
     * @return clusterProvider to get
     */
    public String getClusterProvider() {
        return clusterProvider;
    }

    /**
     * @param clusterProviderIn to set
     */
    public void setClusterProvider(String clusterProviderIn) {
        this.clusterProvider = clusterProviderIn;
    }

    /**
     * @return clusterParams to get
     */
    public Optional<Map<String, Object>> getClusterParams() {
        return clusterParams;
    }

    /**
     * @param clusterParamsIn to set
     */
    public void setClusterParams(Optional<Map<String, Object>> clusterParamsIn) {
        this.clusterParams = clusterParamsIn;
    }
}
