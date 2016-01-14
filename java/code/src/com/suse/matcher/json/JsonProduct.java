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

/**
 * JSON representation of a product.
 */
public class JsonProduct {

    /** The id. */
    private Long id;

    /** The friendly name. */
    private String name;

    /**
     * Standard constructor.
     *
     * @param idIn the id
     * @param nameIn the name
     */
    public JsonProduct(Long idIn, String nameIn) {
        id = idIn;
        name = nameIn;
    }

    /**
     * Gets the id.
     *
     * @return the id
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
     * Gets the friendly name.
     *
     * @return the friendly name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the friendly name.
     *
     * @param nameIn the new friendly name
     */
    public void setName(String nameIn) {
        name = nameIn;
    }
}
