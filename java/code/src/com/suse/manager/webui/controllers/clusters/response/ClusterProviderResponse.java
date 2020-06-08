/**
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

package com.suse.manager.webui.controllers.clusters.response;

public class ClusterProviderResponse {

    private long id;
    private String label;
    private String name;
    private String description;

    /**
     * @return id to get
     */
    public long getId() {
        return id;
    }

    /**
     * @param idIn to set
     */
    public void setId(long idIn) {
        this.id = idIn;
    }

    /**
     * @return label to get
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param labelIn to set
     */
    public void setLabel(String labelIn) {
        this.label = labelIn;
    }

    /**
     * @return name to get
     */
    public String getName() {
        return name;
    }

    /**
     * @param nameIn to set
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @return description to get
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param descriptionIn to set
     */
    public void setDescription(String descriptionIn) {
        this.description = descriptionIn;
    }
}
