package com.redhat.rhn.domain.audit;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by matei on 2/15/17.
 */
public class XccdfRuleResult {

    private Long id;

    private XccdfTestResult testResult;

    private XccdfRuleResultType resultType;

    private Set<XccdfIdent> idents = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public XccdfTestResult getTestResult() {
        return testResult;
    }

    public void setTestResult(XccdfTestResult testResult) {
        this.testResult = testResult;
    }

    public XccdfRuleResultType getResultType() {
        return resultType;
    }

    public void setResultType(XccdfRuleResultType resultType) {
        this.resultType = resultType;
    }

    public Set<XccdfIdent> getIdents() {
        return idents;
    }

    public void setIdents(Set<XccdfIdent> idents) {
        this.idents = idents;
    }
}
