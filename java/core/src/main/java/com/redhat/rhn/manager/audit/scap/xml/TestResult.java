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

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.Date;
import java.util.List;

/**
 * Bean used to unmarshall an intermediary SCAP report.
 */
@Root(strict = false)
public class TestResult {

    @Attribute
    private String id;

    @Attribute(name = "start-time")
    private Date startTime;

    @Attribute(name = "end-time")
    private Date endTime;

    @ElementList(name = "pass")
    private List<TestResultRuleResult> pass;

    @ElementList(name = "fail")
    private List<TestResultRuleResult> fail;

    @ElementList(name = "error")
    private List<TestResultRuleResult> error;

    @ElementList(name = "unknown")
    private List<TestResultRuleResult> unknown;

    @ElementList(name = "notapplicable")
    private List<TestResultRuleResult> notapplicable;

    @ElementList(name = "notchecked")
    private List<TestResultRuleResult> notchecked;

    @ElementList(name = "notselected")
    private List<TestResultRuleResult> notselected;

    @ElementList(name = "informational")
    private List<TestResultRuleResult> informational;

    @ElementList(name = "fixed")
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
}
