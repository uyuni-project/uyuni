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
package com.redhat.rhn.domain.dto;

/**
 * Class representing ID information of a system group
 */
public class SystemGroupID {

    private Long groupID;
    private String groupName;

    /**
     * Instantiates a new system group ID.
     *
     * @param groupIDIn the group ID
     * @param groupNameIn the group name
     */
    public SystemGroupID(Long groupIDIn, String groupNameIn) {
        super();
        this.groupID = groupIDIn;
        this.groupName = groupNameIn;
    }

    /**
     * @return Returns the group ID.
     */
    public Long getGroupID() {
        return groupID;
    }

    /**
     * @return Returns the group name.
     */
    public String getGroupName() {
        return groupName;
    }

    @Override
    public String toString() {
        return "SystemGroupID [groupID=" + groupID + ", groupName=" + groupName + "]";
    }

}
