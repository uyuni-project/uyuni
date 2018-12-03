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
package com.suse.scc.model;

import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This is a SUSE repository as parsed from JSON coming in from SCC.
 */
public class SCCRepositoryJson {

    @SerializedName("id")
    private Long sccId;
    private String name;
    @SerializedName("distro_target")
    private String distroTarget;
    private String description;
    private String url;
    private boolean autorefresh;
    private boolean enabled;

    /**
     * @return the SCC id
     */
    public Long getSCCId() {
        return sccId;
    }

    /**
     * @param sccIdIn the SCC id to set
     */
    public void setSCCId(Long sccIdIn) {
        this.sccId = sccIdIn;
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
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabledIn the enabled to set
     */
    public void setEnabled(boolean enabledIn) {
        this.enabled = enabledIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SCCRepositoryJson)) {
            return false;
        }
        SCCRepositoryJson otherSCCRepository = (SCCRepositoryJson) other;
        return new EqualsBuilder()
            .append(getUrl(), otherSCCRepository.getUrl())
            .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getUrl())
            .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
        .append("sccId", getSCCId())
        .append("description", getDescription())
        .toString();
    }
}
