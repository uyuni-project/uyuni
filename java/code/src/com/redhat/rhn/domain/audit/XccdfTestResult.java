/**
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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.scap.ScapActionDetails;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.org.OrgConfig;
import com.redhat.rhn.manager.audit.ScapManager;
import com.redhat.rhn.manager.audit.scap.RuleResultDiffer;
import com.redhat.rhn.manager.audit.scap.file.ScapFileManager;
import com.redhat.rhn.manager.audit.scap.file.ScapResultFile;

/**
 * XccdfTestResult - Class representation of the table rhnXccdfTestResult.
 * @version $Rev$
 */
public class XccdfTestResult {

    private Long id;
    private Server server;
    private ScapActionDetails scapActionDetails;
    private XccdfBenchmark benchmark;
    private XccdfProfile profile;
    private String identifier;
    private Date startTime;
    private Date endTime;
    private byte[] errors;

    private Long comparableId = null;
    private String diffIcon = null;

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
    public Boolean getDeletable() {
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
