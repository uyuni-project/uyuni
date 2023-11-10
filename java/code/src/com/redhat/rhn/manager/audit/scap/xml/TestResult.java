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

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * Bean used to unmarshall an intermediary SCAP report.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TestResult {

    @XmlAttribute(required = true)
    private String id;

    @XmlAttribute(name = "start-time", required = true)
    private Date startTime;

    @XmlAttribute(name = "end-time", required = true)
    private Date endTime;

    @XmlElementWrapper(name = "pass", required = true)
    @XmlElement(name = "rr")
    private List<TestResultRuleResult> pass;

    @XmlElementWrapper(name = "fail", required = true)
    @XmlElement(name = "rr")
    private List<TestResultRuleResult> fail;

    @XmlElementWrapper(name = "error", required = true)
    @XmlElement(name = "rr")
    private List<TestResultRuleResult> error;

    @XmlElementWrapper(name = "unknown", required = true)
    @XmlElement(name = "rr")
    private List<TestResultRuleResult> unknown;

    @XmlElementWrapper(name = "notapplicable", required = true)
    @XmlElement(name = "rr")
    private List<TestResultRuleResult> notapplicable;

    @XmlElementWrapper(name = "notchecked", required = true)
    @XmlElement(name = "rr")
    private List<TestResultRuleResult> notchecked;

    @XmlElementWrapper(name = "notselected", required = true)
    @XmlElement(name = "rr")
    private List<TestResultRuleResult> notselected;

    @XmlElementWrapper(name = "informational", required = true)
    @XmlElement(name = "rr")
    private List<TestResultRuleResult> informational;

    @XmlElementWrapper(name = "fixed", required = true)
    @XmlElement(name = "rr")
    private List<TestResultRuleResult> fixed;

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
     * @return startTime to get
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * @param startTimeIn to set
     */
    public void setStartTime(Date startTimeIn) {
        this.startTime = startTimeIn;
    }

    /**
     * @return endTime to get
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * @param endTimeIn to set
     */
    public void setEndTime(Date endTimeIn) {
        this.endTime = endTimeIn;
    }

    /**
     * @return pass to get
     */
    public List<TestResultRuleResult> getPass() {
        return pass;
    }

    /**
     * @param passIn to set
     */
    public void setPass(List<TestResultRuleResult> passIn) {
        this.pass = passIn;
    }

    /**
     * @return fail to get
     */
    public List<TestResultRuleResult> getFail() {
        return fail;
    }

    /**
     * @param failIn to set
     */
    public void setFail(List<TestResultRuleResult> failIn) {
        this.fail = failIn;
    }

    /**
     * @return error to get
     */
    public List<TestResultRuleResult> getError() {
        return error;
    }

    /**
     * @param errorIn to set
     */
    public void setError(List<TestResultRuleResult> errorIn) {
        this.error = errorIn;
    }

    /**
     * @return unknown to get
     */
    public List<TestResultRuleResult> getUnknown() {
        return unknown;
    }

    /**
     * @param unknownIn to set
     */
    public void setUnknown(List<TestResultRuleResult> unknownIn) {
        this.unknown = unknownIn;
    }

    /**
     * @return notapplicable to get
     */
    public List<TestResultRuleResult> getNotapplicable() {
        return notapplicable;
    }

    /**
     * @param notapplicableIn to set
     */
    public void setNotapplicable(List<TestResultRuleResult> notapplicableIn) {
        this.notapplicable = notapplicableIn;
    }

    /**
     * @return notchecked to get
     */
    public List<TestResultRuleResult> getNotchecked() {
        return notchecked;
    }

    /**
     * @param notcheckedIn to set
     */
    public void setNotchecked(List<TestResultRuleResult> notcheckedIn) {
        this.notchecked = notcheckedIn;
    }

    /**
     * @return notselected to get
     */
    public List<TestResultRuleResult> getNotselected() {
        return notselected;
    }

    /**
     * @param notselectedIn to set
     */
    public void setNotselected(List<TestResultRuleResult> notselectedIn) {
        this.notselected = notselectedIn;
    }

    /**
     * @return informational to get
     */
    public List<TestResultRuleResult> getInformational() {
        return informational;
    }

    /**
     * @param informationalIn to set
     */
    public void setInformational(List<TestResultRuleResult> informationalIn) {
        this.informational = informationalIn;
    }

    /**
     * @return fixed to get
     */
    public List<TestResultRuleResult> getFixed() {
        return fixed;
    }

    /**
     * @param fixedIn to set
     */
    public void setFixed(List<TestResultRuleResult> fixedIn) {
        this.fixed = fixedIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof TestResult)) {
            return false;
        }

        TestResult that = (TestResult) o;

        return new EqualsBuilder()
            .append(id, that.id)
            .append(startTime, that.startTime)
            .append(endTime, that.endTime)
            .append(pass, that.pass)
            .append(fail, that.fail)
            .append(error, that.error)
            .append(unknown, that.unknown)
            .append(notapplicable, that.notapplicable)
            .append(notchecked, that.notchecked)
            .append(notselected, that.notselected)
            .append(informational, that.informational)
            .append(fixed, that.fixed)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(id)
            .append(startTime)
            .append(endTime)
            .append(pass)
            .append(fail)
            .append(error)
            .append(unknown)
            .append(notapplicable)
            .append(notchecked)
            .append(notselected)
            .append(informational)
            .append(fixed)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", id)
            .append("startTime", startTime)
            .append("endTime", endTime)
            .append("pass", pass)
            .append("fail", fail)
            .append("error", error)
            .append("unknown", unknown)
            .append("notapplicable", notapplicable)
            .append("notchecked", notchecked)
            .append("notselected", notselected)
            .append("informational", informational)
            .append("fixed", fixed)
            .toString();
    }
}
