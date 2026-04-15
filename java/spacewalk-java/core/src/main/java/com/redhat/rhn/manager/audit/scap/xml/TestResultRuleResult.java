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

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * Bean used to unmarshall an intermediary SCAP report.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TestResultRuleResult {

    @XmlAttribute(required = true)
    private String id;

    @XmlElement(name = "ident")
    private List<TestResultRuleResultIdent> idents;

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
     * @return idents to get
     */
    public List<TestResultRuleResultIdent> getIdents() {
        return idents;
    }

    /**
     * @param identsIn to set
     */
    public void setIdents(List<TestResultRuleResultIdent> identsIn) {
        this.idents = identsIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof TestResultRuleResult that)) {
            return false;
        }

        return new EqualsBuilder()
            .append(id, that.id)
            .append(idents, that.idents)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(id)
            .append(idents)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", id)
            .append("idents", idents)
            .toString();
    }
}
