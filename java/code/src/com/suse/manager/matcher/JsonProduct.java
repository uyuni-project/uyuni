/**
 * Copyright (c) 2015 SUSE LLC
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

package com.suse.manager.matcher;

/**
 * JSON representation of a product
 */
public class JsonProduct {

    /** The id. */
    private Long id;

    /** A friendly name. */
    private String name;

    /**
     * Constructor
     * @param idIn the id
     * @param nameIn the friendly name
     */
    public JsonProduct(Long idIn, String nameIn) {
        id = idIn;
        name = nameIn;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }


    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param idIn the id to set
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }


    /**
     * @param nameIn the name to set
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }
}

