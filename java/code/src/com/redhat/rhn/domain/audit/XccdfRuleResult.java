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
package com.redhat.rhn.domain.audit;

import java.util.HashSet;
import java.util.Set;

/**
 * XccdfRuleResult - Class representation of the table rhnXccdfRuleresult.
 */
public class XccdfRuleResult {

    private Long id;

    private XccdfTestResult testResult;

    private XccdfRuleResultType resultType;

    private Set<XccdfIdent> idents = new HashSet<>();

    /**
     * @return id to get
     */
    public Long getId() {
        return id;
    }

    /**
     * @param idIn to set
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @return testResults to get
     */
    public XccdfTestResult getTestResult() {
        return testResult;
    }

    /**
     * @param testResultIn to set
     */
    public void setTestResult(XccdfTestResult testResultIn) {
        this.testResult = testResultIn;
    }

    /**
     * @return resultType to get
     */
    public XccdfRuleResultType getResultType() {
        return resultType;
    }

    /**
     * @param resultTypeIn to set
     */
    public void setResultType(XccdfRuleResultType resultTypeIn) {
        this.resultType = resultTypeIn;
    }

    /**
     * @return idents to get
     */
    public Set<XccdfIdent> getIdents() {
        return idents;
    }

    /**
     * @param identsIn idents to set
     */
    public void setIdents(Set<XccdfIdent> identsIn) {
        this.idents = identsIn;
    }
}
