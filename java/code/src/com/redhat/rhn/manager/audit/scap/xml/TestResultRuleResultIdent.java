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
package com.redhat.rhn.manager.audit.scap.xml;

import com.suse.utils.xml.EmptyStringAdapter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Bean used to unmarshall an intermediary SCAP report.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TestResultRuleResultIdent {

    @XmlAttribute(required = true)
    private String system;

    @XmlValue
    @XmlJavaTypeAdapter(EmptyStringAdapter.class)
    private String text;

    /**
     * @return system to get
     */
    public String getSystem() {
        return system;
    }

    /**
     * @param systemIn to set
     */
    public void setSystem(String systemIn) {
        this.system = systemIn;
    }

    /**
     * @return text to get
     */
    public String getText() {
        return text;
    }

    /**
     * @param textIn to set
     */
    public void setText(String textIn) {
        this.text = textIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof TestResultRuleResultIdent)) {
            return false;
        }

        TestResultRuleResultIdent that = (TestResultRuleResultIdent) o;

        return new EqualsBuilder()
            .append(system, that.system)
            .append(text, that.text)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(system)
            .append(text)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("system", system)
            .append("text", text)
            .toString();
    }
}

