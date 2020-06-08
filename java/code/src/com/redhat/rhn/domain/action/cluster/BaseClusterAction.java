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

package com.redhat.rhn.domain.action.cluster;

import com.redhat.rhn.domain.action.Action;
import com.suse.manager.model.clusters.Cluster;

public class BaseClusterAction extends Action {

    private Cluster cluster;

    /**
     * @return cluster to get
     */
    public Cluster getCluster() {
        return cluster;
    }

    /**
     * @param clusterIn to set
     */
    public void setCluster(Cluster clusterIn) {
        this.cluster = clusterIn;
    }
}
