/**
 * Copyright (c) 2014 SUSE
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
package com.suse.scc.model;

import com.google.gson.annotations.SerializedName;

/**
 * This is a SUSE repository as parsed from JSON coming in from SCC.
 */
public class SCCRepository {

    private int id;
    private String name;
    @SerializedName("distro_target")
    private String distroTarget;
    private String description;
    private String url;
    private boolean autorefresh;
    // not in JSON
    private Long credentialsId;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param idIn the id to set
     */
    public void setId(int idIn) {
        this.id = idIn;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param nameIn the name to set
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @return the distroTarget
     */
    public String getDistroTarget() {
        return distroTarget;
    }

    /**
     * @param distroTargetIn the distroTarget to set
     */
    public void setDistroTarget(String distroTargetIn) {
        this.distroTarget = distroTargetIn;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param descriptionIn the description to set
     */
    public void setDescription(String descriptionIn) {
        this.description = descriptionIn;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param urlIn the url to set
     */
    public void setUrl(String urlIn) {
        this.url = urlIn;
    }

    /**
     * @return the autorefresh
     */
    public boolean isAutorefresh() {
        return autorefresh;
    }

    /**
     * @param autorefreshIn the autorefresh to set
     */
    public void setAutorefresh(boolean autorefreshIn) {
        this.autorefresh = autorefreshIn;
    }

    /**
     * Get the mirror credentials ID.
     * @return the credentialsId
     */
    public Long getCredentialsId() {
        return credentialsId;
    }

    /**
     * Set the mirror credentials ID this repo can be retrieved with.
     * @param credentialsIdIn the credentials ID to set
     */
    public void setCredentialsId(Long credentialsIdIn) {
        this.credentialsId = credentialsIdIn;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SCCRepository other = (SCCRepository) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }
}
