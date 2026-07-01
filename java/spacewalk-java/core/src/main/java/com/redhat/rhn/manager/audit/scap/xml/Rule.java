/*
 * Copyright (c) 2017--2026 SUSE LLC
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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * Rule bean for unmarshalling SCAP reports.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Rule {

    @XmlAttribute(name = "id")
    private String id;

    @XmlElement(name = "remediation")
    private String remediation;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param idIn the id to set
     */
    public void setId(String idIn) {
        this.id = idIn;
    }

    /**
     * @return the remediation
     */
    public String getRemediation() {
        return remediation;
    }

    /**
     * @param remediationIn the remediation to set
     */
    public void setRemediation(String remediationIn) {
        this.remediation = remediationIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Rule that)) {
            return false;
        }

        return new EqualsBuilder()
            .append(id, that.id)
            .append(remediation, that.remediation)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(id)
            .append(remediation)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", id)
            .append("remediation", remediation)
            .toString();
    }
}
