/*
 * Copyright (c) 2017--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.audit;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * XccdfRuleResult - Class representation of the table rhnXccdfRuleresult.
 */
@Entity
@Table(name = "rhnXccdfRuleresult")
@Immutable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class XccdfRuleResult implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rhn_xccdf_rresult_seq")
    @SequenceGenerator(name = "rhn_xccdf_rresult_seq", sequenceName = "rhn_xccdf_rresult_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "testresult_id")
    private XccdfTestResult testResult;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id")
    private XccdfRuleResultType resultType;

    @ManyToMany
    @JoinTable(
            name = "rhnXccdfRuleIdentMap",
            joinColumns = @JoinColumn(name = "rresult_id"),
            inverseJoinColumns = @JoinColumn(name = "ident_id"))
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
