/*
 * Copyright (c) 2017 SUSE LLC
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

package com.redhat.rhn.manager.visualization.json;

/**
 * POJO representing virtual host manager to be displayed in visualization.
 */
public class VirtualHostManager {

    private String id;
    private String parentId;
    private String name;
    private String type = "vhm";

    /**
     * Standard constructor
     * @param idIn id as long
     * @param nameIn name
     */
    public VirtualHostManager(Long idIn, String nameIn) {
        if (idIn != null) {
            this.id = idIn.toString();
        }
        this.name = nameIn;
    }

    /**
     * Standard constructor
     * @param idIn id
     * @param parentIdIn id of parent
     * @param nameIn name
     */
    public VirtualHostManager(String idIn, String parentIdIn, String nameIn) {
        this.id = idIn;
        this.parentId = parentIdIn;
        this.name = nameIn;
    }

    /**
     * Gets the id.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param idIn - the id
     * @return this
     */
    public VirtualHostManager setId(String idIn) {
        id = idIn;
        return this;
    }

    /**
     * Gets the parentId.
     *
     * @return parentId
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * Sets the parentId.
     *
     * @param parentIdIn - the parentId
     * @return this
     */
    public VirtualHostManager setParentId(String parentIdIn) {
        parentId = parentIdIn;
        return this;
    }

    /**
     * Gets the name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param nameIn - the name
     * @return this
     */
    public VirtualHostManager setName(String nameIn) {
        name = nameIn;
        return this;
    }

    /**
     * Gets the type.
     *
     * @return type
     */
    public String getType() {
        return type;
    }
}
