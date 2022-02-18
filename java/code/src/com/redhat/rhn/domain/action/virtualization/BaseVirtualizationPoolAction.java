/*
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
package com.redhat.rhn.domain.action.virtualization;

import com.redhat.rhn.domain.action.Action;

/**
 * Represents the common pieces of virtual storage actions.
 */
public class BaseVirtualizationPoolAction extends Action {

    private String poolName;

    /**
     * @return Returns the name of the storage pool to apply the action on.
     */
    public String getPoolName() {
        return poolName;
    }

    /**
     * @param poolNameIn The name of the storage pool name to set.
     */
    public void setPoolName(String poolNameIn) {
        poolName = poolNameIn;
    }

    @Override
    public String getWebSocketActionId() {
        return String.format("pool-%s", getPoolName());
    }
}
