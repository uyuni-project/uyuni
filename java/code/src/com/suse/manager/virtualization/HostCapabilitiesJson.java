/**
 * Copyright (c) 2018 SUSE LLC
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
import java.util.Map;

/**
 * Class representing the salt output for host capabilities
 */
public class HostCapabilitiesJson {

    private List<Guest> guests;

    /**
     * @return Returns the guests.
     */
    public List<Guest> getGuests() {
        return guests;
    }

    /**
     * @param guestsIn The guests to set.
     */
    public void setGuests(List<Guest> guestsIn) {
        guests = guestsIn;
    }


    /**
     * Represents the items in the guests field.
     */
    public class Guest {

        private Arch arch;

        @SerializedName("os_type")
        private String osType;

        /**
         * @return Returns the arch.
         */
        public Arch getArch() {
            return arch;
        }

        /**
         * @param archIn The arch to set.
         */
        public void setArch(Arch archIn) {
            arch = archIn;
        }

        /**
         * @return Returns the osType.
         */
        public String getOsType() {
            return osType;
        }

        /**
         * @param osTypeIn The osType to set.
         */
        public void setOsType(String osTypeIn) {
            osType = osTypeIn;
        }

    }

    /**
     * Represents the arch field of a guest.
     */
    public class Arch {

        private String name;
        private Map<String, Object> domains;

        /**
         * @return Returns the name.
         */
        public String getName() {
            return name;
        }

        /**
         * @param nameIn The name to set.
         */
        public void setName(String nameIn) {
            name = nameIn;
        }

        /**
         * @return Returns the domains.
         */
        public Map<String, Object> getDomains() {
            return domains;
        }

        /**
         * @param domainsIn The domains to set.
         */
        public void setDomains(Map<String, Object> domainsIn) {
            domains = domainsIn;
        }
    }
}
