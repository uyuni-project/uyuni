/**
 * Copyright (c) 2016 SUSE LLC
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
package com.redhat.rhn.domain.formula;

import java.util.Map;

/**
 * A class representing a formula with its metadata.
 */
public class Formula {

    private String name;
    private String description;
    private String group;

    /**
     * Default constructor
     * @param formulaName the name of the new formula
     */
    public Formula(String formulaName) {
        name = formulaName;
    }

    /**
     * Set the metadata from a map.
     * @param metadata a map with metadata values.
     */
    public void setMetadata(Map<String, Object> metadata) {
        description = (String) metadata.getOrDefault("description", "");
        group = (String) metadata.getOrDefault("group", "");
    }

    /**
     * @return the formula's name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the formula's description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the formula's group
     */
    public String getGroup() {
        return group;
    }
}
