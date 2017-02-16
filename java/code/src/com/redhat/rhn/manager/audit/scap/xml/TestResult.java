package com.redhat.rhn.manager.audit.scap.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.Date;
import java.util.List;

/**
 * Created by matei on 2/14/17.
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public List<TestResultRuleResult> getPass() {
        return pass;
    }

    public void setPass(List<TestResultRuleResult> pass) {
        this.pass = pass;
    }

    public List<TestResultRuleResult> getFail() {
        return fail;
    }

    public void setFail(List<TestResultRuleResult> fail) {
        this.fail = fail;
    }

    public List<TestResultRuleResult> getError() {
        return error;
    }

    public void setError(List<TestResultRuleResult> error) {
        this.error = error;
    }

    public List<TestResultRuleResult> getUnknown() {
        return unknown;
    }

    public void setUnknown(List<TestResultRuleResult> unknown) {
        this.unknown = unknown;
    }

    public List<TestResultRuleResult> getNotapplicable() {
        return notapplicable;
    }

    public void setNotapplicable(List<TestResultRuleResult> notapplicable) {
        this.notapplicable = notapplicable;
    }

    public List<TestResultRuleResult> getNotchecked() {
        return notchecked;
    }

    public void setNotchecked(List<TestResultRuleResult> notchecked) {
        this.notchecked = notchecked;
    }

    public List<TestResultRuleResult> getNotselected() {
        return notselected;
    }

    public void setNotselected(List<TestResultRuleResult> notselected) {
        this.notselected = notselected;
    }

    public List<TestResultRuleResult> getInformational() {
        return informational;
    }

    public void setInformational(List<TestResultRuleResult> informational) {
        this.informational = informational;
    }

    public List<TestResultRuleResult> getFixed() {
        return fixed;
    }

    public void setFixed(List<TestResultRuleResult> fixed) {
        this.fixed = fixed;
    }
}
