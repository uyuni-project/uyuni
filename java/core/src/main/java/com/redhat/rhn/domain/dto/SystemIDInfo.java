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
 * Class representing ID information of a system
 */
public class SystemIDInfo {

    private Long systemID;
    private String systemName;

    /**
     * Instantiates a new system ID info.
     *
     * @param systemIDIn the system ID
     * @param systemNameIn the system name
     */
    public SystemIDInfo(Long systemIDIn, String systemNameIn) {
        super();
        this.systemID = systemIDIn;
        this.systemName = systemNameIn;
    }

    /**
     * Gets the system name.
     *
     * @return the system name
     */
    public String getSystemName() {
        return systemName;
    }

    /**
     * Gets the system ID.
     *
     * @return the system ID
     */
    public Long getSystemID() {
        return systemID;
    }

    @Override
    public String toString() {
        return "SystemIDInfo [systemID=" + systemID + ", systemName=" + systemName + "]";
    }

}
