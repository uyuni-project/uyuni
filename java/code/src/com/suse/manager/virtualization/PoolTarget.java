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
package com.suse.manager.virtualization;

/**
 * Describes the target properties of a virtual storage pool
 */
public class PoolTarget {
    private String path;
    private String owner;
    private String group;
    private String mode;
    private String seclabel;

    /**
     * @return Returns the path.
     */
    public String getPath() {
        return path;
    }

    /**
     * @param pathIn The path to set.
     */
    public void setPath(String pathIn) {
        path = pathIn;
    }

    /**
     * @return Returns the owner.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @param ownerIn The owner to set.
     */
    public void setOwner(String ownerIn) {
        owner = ownerIn;
    }

    /**
     * @return Returns the group.
     */
    public String getGroup() {
        return group;
    }

    /**
     * @param groupIn The group to set.
     */
    public void setGroup(String groupIn) {
        group = groupIn;
    }

    /**
     * @return Returns the mode.
     */
    public String getMode() {
        return mode;
    }

    /**
     * @param modeIn The mode to set.
     */
    public void setMode(String modeIn) {
        mode = modeIn;
    }

    /**
     * @return Returns the seclabel.
     */
    public String getSeclabel() {
        return seclabel;
    }

    /**
     * @param seclabelIn The seclabel to set.
     */
    public void setSeclabel(String seclabelIn) {
        seclabel = seclabelIn;
    }
}
