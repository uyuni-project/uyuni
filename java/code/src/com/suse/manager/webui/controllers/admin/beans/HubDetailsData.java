/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.webui.controllers.admin.beans;

import com.suse.manager.model.hub.IssHub;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;

public class HubDetailsData {

    private long id;

    private String fqdn;

    private String rootCA;

    private String gpgKey;

    private String sccUsername;

    private Date created;

    private Date modified;

    /**
     * Create an instance from the hub entity.
     * @param hub the hub
     */
    public HubDetailsData(IssHub hub) {
        this.id = hub.getId();
        this.fqdn = hub.getFqdn();
        this.rootCA = hub.getRootCa();
        this.gpgKey = hub.getGpgKey();
        this.sccUsername = hub.getMirrorCredentials().getUsername();
        this.created = hub.getCreated();
        this.modified = hub.getModified();
    }

    public long getId() {
        return id;
    }

    public void setId(long idIn) {
        this.id = idIn;
    }

    public String getFqdn() {
        return fqdn;
    }

    public void setFqdn(String fqdnIn) {
        this.fqdn = fqdnIn;
    }

    public String getRootCA() {
        return rootCA;
    }

    public void setRootCA(String rootCAIn) {
        this.rootCA = rootCAIn;
    }

    public String getGpgKey() {
        return gpgKey;
    }

    public void setGpgKey(String gpgKeyIn) {
        this.gpgKey = gpgKeyIn;
    }

    public String getSccUsername() {
        return sccUsername;
    }

    public void setSccUsername(String sccUsernameIn) {
        this.sccUsername = sccUsernameIn;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date createdIn) {
        this.created = createdIn;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modifiedIn) {
        this.modified = modifiedIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof HubDetailsData that)) {
            return false;
        }

        return new EqualsBuilder()
            .append(getFqdn(), that.getFqdn())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getFqdn())
            .toHashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HubDetailsData{");
        sb.append("id=").append(id);
        sb.append(", fqdn='").append(fqdn).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
