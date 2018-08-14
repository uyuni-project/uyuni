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

package com.suse.matcher.json;

import java.util.Set;

/**
 * JSON representation of a group of virtual guests which belong to the same
 * cloud, VMWare vCenter, etc.
 */
public class VirtualizationGroupJson {

    private Long id;
    private String name;
    private String type;
    private Set<Long> virtualGuestIds;

    /**
     * Standard constructor.
     * @param idIn an identifier, unique for a given type
     * @param nameIn a descriptive name
     * @param typeIn a type label
     * @param virtualGuestIdsIn set of ids of Virtual guests in this group
     */
    public VirtualizationGroupJson(Long idIn, String nameIn, String typeIn,
                                   Set<Long> virtualGuestIdsIn) {
        id = idIn;
        name = nameIn;
        type = typeIn;
        virtualGuestIds = virtualGuestIdsIn;
    }

    /**
     * Gets the id.
     *
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param idIn the new id
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param nameIn the new name
     */
    public void setName(String nameIn) {
        name = nameIn;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param typeIn the new type
     */
    public void setType(String typeIn) {
        type = typeIn;
    }

    /**
     * Gets the set of ids of Virtual guests in this group.
     *
     * @return set of ids of Virtual guests
     */
    public Set<Long> getVirtualGuestIds() {
        return virtualGuestIds;
    }

    /**
     * Sets the ids of Virtual guests in this group.
     *
     * @param virtualGuestIdsIn the new set of ids of Virtual guests
     */
    public void setVirtualGuestIds(Set<Long> virtualGuestIdsIn) {
        virtualGuestIds = virtualGuestIdsIn;
    }
}
