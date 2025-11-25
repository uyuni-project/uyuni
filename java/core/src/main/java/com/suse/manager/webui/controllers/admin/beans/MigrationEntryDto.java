/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.manager.webui.controllers.admin.beans;

import com.redhat.rhn.domain.iss.IssSlave;

import com.suse.manager.model.hub.IssAccessToken;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.StringJoiner;

public class MigrationEntryDto {

    private long id;

    private boolean disabled;

    private String fqdn;

    private String accessToken;

    private String rootCA;

    private boolean selected;

    /**
     * Builds an instance from the given slave and the accessToken, if not null
     * @param rowId the unique row identifier
     * @param slave the ISS v1 slave
     * @param issAccessToken the ISS v3 access token, if available for the fqdn of the slave
     */
    public MigrationEntryDto(int rowId, IssSlave slave, IssAccessToken issAccessToken) {
        this.id = rowId;
        this.disabled = !"Y".equals(slave.getEnabled());
        this.fqdn = slave.getSlave();
        this.accessToken = issAccessToken != null ? issAccessToken.getToken() : null;
        this.rootCA = null;
        this.selected = !this.disabled;
    }

    public long getId() {
        return id;
    }

    public void setId(long idIn) {
        this.id = idIn;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setDisabled(boolean disabledIn) {
        this.disabled = disabledIn;
    }

    public String getFqdn() {
        return fqdn;
    }

    public void setFqdn(String fqdnIn) {
        this.fqdn = fqdnIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessTokenIn) {
        this.accessToken = accessTokenIn;
    }

    public String getRootCA() {
        return rootCA;
    }

    public void setRootCA(String rootCaIn) {
        this.rootCA = rootCaIn;
    }

    public void setSelected(boolean selectedIn) {
        this.selected = selectedIn;
    }

    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof MigrationEntryDto that)) {
            return false;
        }

        return new EqualsBuilder()
            .append(getId(), that.getId())
            .append(getFqdn(), that.getFqdn())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getId())
            .append(getFqdn())
            .toHashCode();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MigrationEntryDto.class.getSimpleName() + "[", "]")
            .add("id=" + id)
            .add("fqdn='" + fqdn + "'")
            .add("disabled=" + disabled)
            .add("selected=" + selected)
            .toString();
    }
}
