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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Bean used to unmarshall an intermediary SCAP report.
 */
@XmlRootElement(name = "benchmark-resume")
@XmlAccessorType(XmlAccessType.FIELD)
public class BenchmarkResume {

    @XmlAttribute(required = true)
    private String id;

    @XmlAttribute(required = true)
    private String version;

    @XmlElement(name = "profile")
    private Profile profile;

    @XmlElement(name = "TestResult", required = true)
    private TestResult testResult;

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
     * @return profile to get
     */
    public Profile getProfile() {
        return profile;
    }

    /**
     * @param profileIn to set
     */
    public void setProfile(Profile profileIn) {
        this.profile = profileIn;
    }

    /**
     * @return testResult to get
     */
    public TestResult getTestResult() {
        return testResult;
    }

    /**
     * @param testResultIn to set
     */
    public void setTestResult(TestResult testResultIn) {
        this.testResult = testResultIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof BenchmarkResume)) {
            return false;
        }

        BenchmarkResume that = (BenchmarkResume) o;

        return new EqualsBuilder()
            .append(id, that.id)
            .append(version, that.version)
            .append(profile, that.profile)
            .append(testResult, that.testResult)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(id)
            .append(version)
            .append(profile)
            .append(testResult)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", id)
            .append("version", version)
            .append("profile", profile)
            .append("testResult", testResult)
            .toString();
    }
}

