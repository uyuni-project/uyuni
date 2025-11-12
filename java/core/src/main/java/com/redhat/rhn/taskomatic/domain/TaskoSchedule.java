/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2010--2012 Red Hat, Inc.
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
package com.redhat.rhn.taskomatic.domain;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.BaseDomainHelper;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * a schedule represents a concrete bunch, that is scheduled with specified parameters,
 * in specified time period with some periodicity
 * TaskoSchedule
 */
@Entity
@Table(name = "rhnTaskoSchedule")
public class TaskoSchedule extends BaseDomainHelper {

    @Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tasko_schedule_seq")
	@SequenceGenerator(name = "tasko_schedule_seq", sequenceName = "RHN_TASKO_SCHEDULE_ID_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "job_label")
    private String jobLabel;

    @ManyToOne
    @JoinColumn(name = "bunch_id")
    private TaskoBunch bunch;
    @Column(name = "org_id")
    private Integer orgId;
    @Column(name = "active_from")
    private Date activeFrom;
    @Column(name = "active_till")
    private Date activeTill;
    @Column(name = "cron_expr")
    private String cronExpr;
    @Column
    private byte[] data;

    /**
     * default constructor required by hibernate
     */
    public TaskoSchedule() {
    }

    /**
     * constructor
     * schedule is always associated with organization, bunch, job name, job parameter,
     * time period when active and cron expression, how often is shall get scheduled
     * @param orgIdIn organization id
     * @param bunchIn bunch id
     * @param jobLabelIn job name
     * @param dataIn job parameter
     * @param activeFromIn scheduled from
     * @param activeTillIn scheduled till
     * @param cronExprIn cron expression
     */
    public TaskoSchedule(Integer orgIdIn, TaskoBunch bunchIn, String jobLabelIn,
            Map<String, Object> dataIn, Date activeFromIn, Date activeTillIn, String cronExprIn) {
        setOrgId(orgIdIn);
        setBunch(bunchIn);
        setJobLabel(jobLabelIn);
        data = serializeMap(dataIn);
        setCronExpr(cronExprIn);
        setActiveFrom(Objects.requireNonNullElseGet(activeFromIn, Date::new));
        if ((cronExprIn == null) || (cronExprIn.isEmpty())) {
            // set activeFrom for single runs
            setActiveTill(getActiveFrom());
        }
        if (activeTillIn != null) {
            setActiveTill(activeTillIn);
        }
    }

    /**
     * sanity check for predefined schedules
     * (defined directly in the DB)
     */
    public void sanityCheckForPredefinedSchedules() {
        if (getActiveTill() == null) {
            if ((cronExpr == null) || (cronExpr.isEmpty())) {
                // set activeTill for single runs
                setActiveTill(new Date());
                HibernateFactory.commitTransaction();
            }
        }
    }

    /**
     * unschedule this particular schedule
     */
    public void unschedule() {
        setActiveTill(new Date());
    }

    private byte[] serializeMap(Map<String, Object> dataMap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (null != dataMap) {
            ObjectOutputStream out;
            try {
                out = new ObjectOutputStream(baos);
                out.writeObject(dataMap);
                out.flush();
            }
            catch (IOException e) {
                return null;
            }
        }
        return baos.toByteArray();
    }

    private Map<String, Object> getDataMapFromBlob(byte[] blob) {
        Object obj = null;

        try {
            if (blob != null) {
                InputStream binaryInput = new ByteArrayInputStream(blob);
                ObjectInputStream in = new ObjectInputStream(binaryInput);
                obj = in.readObject();
                in.close();
            }
        }
        catch (Exception e) {
            // Do nothing
        }
        return (Map<String, Object>) obj;
    }

    /**
     * set job parameters
     * @param dataMap job parameters
     */
    public void setDataMap(Map<String, Object> dataMap) {
        data = serializeMap(dataMap);
    }

    /**
     * get job parameters
     * @return job paramters
     */
    public Map<String, Object> getDataMap() {
        return getDataMapFromBlob(getData());
    }

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param idIn The id to set.
     */
    protected void setId(Long idIn) {
        id = idIn;
    }

    /**
     * @return Returns the jobLabel.
     */
    public String getJobLabel() {
        return jobLabel;
    }

    /**
     * @param jobLabelIn The jobLabel to set.
     */
    public void setJobLabel(String jobLabelIn) {
        jobLabel = jobLabelIn;
    }

    /**
     * @return Returns the bunch.
     */
    public TaskoBunch getBunch() {
        return bunch;
    }

    /**
     * @param bunchIn The bunch to set.
     */
    public void setBunch(TaskoBunch bunchIn) {
        bunch = bunchIn;
    }

    /**
     * @return Returns the orgId.
     */
    public Integer getOrgId() {
        return orgId;
    }

    /**
     * @param orgIdIn The orgId to set.
     */
    public void setOrgId(Integer orgIdIn) {
        orgId = orgIdIn;
    }

    /**
     * @return Returns the activeFrom.
     */
    public Date getActiveFrom() {
        return activeFrom;
    }

    /**
     * @param activeFromIn The activeFrom to set.
     */
    public void setActiveFrom(Date activeFromIn) {
        activeFrom = activeFromIn;
    }

    /**
     * @return Returns the activeTill.
     */
    public Date getActiveTill() {
        return activeTill;
    }

    /**
     * @param activeTillIn The activeTill to set.
     */
    public void setActiveTill(Date activeTillIn) {
        activeTill = activeTillIn;
    }

    /**
     * @return Returns the data.
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @param datain The params to set.
     */
    public void setData(byte[] datain) {
        this.data = datain;
    }

    /**
     * @return Returns the cronExpr.
     */
    public String getCronExpr() {
        return cronExpr;
    }

    /**
     * @param cronExprIn The cronExpr to set.
     */
    public void setCronExpr(String cronExprIn) {
        cronExpr = cronExprIn;
    }

    /**
     * checks whether cron expression is defined
     * @return true, if it's a cron schedule
     */
    public boolean isCronSchedule() {
        return (cronExpr != null) && !cronExpr.isEmpty();
    }
}
