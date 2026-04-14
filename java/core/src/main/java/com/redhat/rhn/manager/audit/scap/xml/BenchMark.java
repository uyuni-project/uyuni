/*
 * Copyright (c) 2025--2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.manager.audit.scap.xml;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * BenchMark bean for unmarshalling SCAP benchmark XML.
 */
@XmlRootElement(name = "benchmark")
@XmlAccessorType(XmlAccessType.FIELD)
public class BenchMark {

    @XmlAttribute(required = true)
    private String id;

    @XmlAttribute(required = true)
    private String version;

    @XmlElement(name = "profile")
    private List<Profile> profiles;

    /**
     * @return id to get
     */
    public String getId() {
        return id;
    }

    /**
     * @param idIn to set
     */
    public void setId(String idIn) {
        this.id = idIn;
    }

    /**
     * @return version to get
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param versionIn to set
     */
    public void setVersion(String versionIn) {
        this.version = versionIn;
    }

    /**
     * @return profiles to get
     */
    public List<Profile> getProfiles() {
        return profiles;
    }

    /**
     * @param profilesIn to set
     */
    public void setProfiles(List<Profile> profilesIn) {
        this.profiles = profilesIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof BenchMark that)) {
            return false;
        }

        return new EqualsBuilder()
            .append(id, that.id)
            .append(version, that.version)
            .append(profiles, that.profiles)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(id)
            .append(version)
            .append(profiles)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", id)
            .append("version", version)
            .append("profiles", profiles)
            .toString();
    }
}
