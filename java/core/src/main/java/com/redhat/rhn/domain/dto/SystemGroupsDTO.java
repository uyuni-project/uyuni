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

import java.util.List;

/**
 * DTO class that contains information regarding to the groups a system is member of.
 */
public class SystemGroupsDTO {

    private Long systemID;
    private List<SystemGroupID> systemGroups;

    /**
     * Instantiates a new system groups DTO.
     *
     * @param systemIDIn the system ID
     * @param systemGroupsIn the system groups
     */
    public SystemGroupsDTO(Long systemIDIn, List<SystemGroupID> systemGroupsIn) {
        super();
        this.systemID = systemIDIn;
        this.systemGroups = systemGroupsIn;
    }

    /**
     * Gets the system ID.
     *
     * @return the system ID
     */
    public Long getSystemID() {
        return systemID;
    }

    /**
     * Gets the system groups.
     *
     * @return the system groups
     */
    public List<SystemGroupID> getSystemGroups() {
        return systemGroups;
    }

    @Override
    public String toString() {
        return "SystemGroupsInfo [systemID=" + systemID + ", systemGroups=" + systemGroups + "]";
    }

}
