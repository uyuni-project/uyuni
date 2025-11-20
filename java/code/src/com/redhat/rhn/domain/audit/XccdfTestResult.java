/*
 * Copyright (c) 2012 Red Hat, Inc.
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


import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.scap.ScapActionDetails;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.org.OrgConfig;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.manager.audit.ScapManager;
import com.redhat.rhn.manager.audit.scap.RuleResultDiffer;
import com.redhat.rhn.manager.audit.scap.file.ScapFileManager;
import com.redhat.rhn.manager.audit.scap.file.ScapResultFile;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * XccdfTestResult - Class representation of the table rhnXccdfTestResult.
 */
@Entity
@Table(name = "rhnXccdfTestresult")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class XccdfTestResult implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xccdf_test_result_seq")
    @SequenceGenerator(name = "xccdf_test_result_seq", sequenceName = "rhn_xccdf_tresult_id_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_scap_id", nullable = false)
    private ScapActionDetails scapActionDetails;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benchmark_id", nullable = false)
    private XccdfBenchmark benchmark;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private XccdfProfile profile;

    @OneToMany(mappedBy = "testResult", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_ONLY)
    private Set<XccdfRuleResult> results = new HashSet<>();

    @Column(name = "identifier", length = 120)
    private String identifier;

    @Column(name = "start_time")
    private Date startTime;

    @Column(name = "end_time")
    private Date endTime;

    @Transient
    private Long comparableId = null;

    @Transient
    private String diffIcon = null;

    @Column(name = "errors", columnDefinition = "bytea")
    private byte[] errors = {};

    /**
     * Getter for id
     * @return Long to get
    */
    public Long getId() {
        return this.id;
    }

    /**
     * Setter for id
     * @param idIn to set
    */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Getter for server
     * @return Server to get
     */
    public Server getServer() {
        return this.server;
    }

    /**
     * Setter for server
     * @param serverIn to set
     */
    public void setServer(Server serverIn) {
        this.server = serverIn;
    }

    /**
     * Getter for scapActionDetails
     * @return ScapActionDetails to get
    */
    public ScapActionDetails getScapActionDetails() {
        return this.scapActionDetails;
    }

    /**
     * Setter for scapActionDetails
     * @param scapActionDetailsIn to set
    */
    public void setScapActionDetails(ScapActionDetails scapActionDetailsIn) {
        this.scapActionDetails = scapActionDetailsIn;
    }

    /**
     * Getter for benchmark
     * @return XccdfBenchmark to get
    */
    public XccdfBenchmark getBenchmark() {
        return this.benchmark;
    }

    /**
     * Setter for benchmark
     * @param benchmarkIn to set
    */
    public void setBenchmark(XccdfBenchmark benchmarkIn) {
        this.benchmark = benchmarkIn;
    }

    /**
     * Getter for profile
     * @return XccdfProfile to get
    */
    public XccdfProfile getProfile() {
        return this.profile;
    }

    /**
     * Setter for profile
     * @param profileIn to set
    */
    public void setProfile(XccdfProfile profileIn) {
        this.profile = profileIn;
    }

    /**
     * Getter for identifier
     * @return String to get
    */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * Setter for identifier
     * @param identifierIn to set
    */
    public void setIdentifier(String identifierIn) {
        this.identifier = identifierIn;
    }

    /**
     * Getter for startTime
     * @return Date to get
    */
    public Date getStartTime() {
        return this.startTime;
    }

    /**
     * Setter for startTime
     * @param startTimeIn to set
    */
    public void setStartTime(Date startTimeIn) {
        this.startTime = startTimeIn;
    }

    /**
     * Getter for endTime
     * @return Date to get
    */
    public Date getEndTime() {
        return this.endTime;
    }

    /**
     * Setter for endTime
     * @param endTimeIn to set
    */
    public void setEndTime(Date endTimeIn) {
        this.endTime = endTimeIn;
    }

    /**
     * Getter for errors
     * @return errors
     */
    public byte[] getErrors() {
        return this.errors;
    }

    /**
     * Setter for errors
     * @param errorsIn to set
    */
    public void setErrors(byte[] errorsIn) {
        this.errors = errorsIn;
    }

    /**
     * Getter for results
     * @return results to get
     */
    public Set<XccdfRuleResult> getResults() {
        return results;
    }

    /**
     * Setter for results
     * @param resultsIn to set
     */
    public void setResults(Set<XccdfRuleResult> resultsIn) {
        this.results = resultsIn;
    }

    /**
     * Get the String version of the Errors contents
     * @return String version of the Errors contents
     */
    public String getErrrosContents() {
        return HibernateFactory.getByteArrayContents(this.errors);
    }

    /**
     * Return the TestResult with metadata similar to the this one
     * @return id of testresult
     */
    public Long getComparableId() {
        if (comparableId == null) {
            comparableId = ScapManager.previousComparableTestResult(id);
        }
        return comparableId;
    }

    /**
     * Return name of the list icon, which best refers to the state of diff.
     * The diff between current TestResult and previous comparable TestResult.
     * @return the result
     */
    public String getDiffIcon() {
        if (diffIcon == null) {
            diffIcon = new RuleResultDiffer(getComparableId(), id).overallComparison();
        }
        return diffIcon;
    }

    /**
     * Return a list of detailed SCAP files assigned with this scan run.
     * @return the result
     */
    public List<ScapResultFile> getFiles() {
        return ScapFileManager.lookupFilesForTestResult(this);
    }

    /**
     * Return true if this XccdfTestResult can be deleted (based on the organization's
     * SCAP retention policy.
     * @return the result
     */
    public boolean getDeletable() {
        OrgConfig cfg = getServer().getOrg().getOrgConfig();
        Long retentionDays = cfg.getScapRetentionPeriodDays();
        if (retentionDays == null) {
            return false;
        }
        ServerAction serverAction = ActionFactory.getServerActionForServerAndAction(
                getServer(), getScapActionDetails().getParentAction());
        Date completionTime = serverAction.getCompletionTime();

        Calendar periodStart = Calendar.getInstance();
        periodStart.setTime(new Date());
        periodStart.add(Calendar.DATE, -1 * retentionDays.intValue());

        return completionTime.before(periodStart.getTime());
    }
}
