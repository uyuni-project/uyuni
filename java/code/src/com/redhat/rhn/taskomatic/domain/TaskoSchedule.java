/*
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * a schedule represents a concrete bunch, that is scheduled with specified parameters,
 * in specified time period with some periodicity
 * TaskoSchedule
 */
public class TaskoSchedule {

    private Long id;
    private String jobLabel;
    private TaskoBunch bunch;
    private Integer orgId;
    private Date activeFrom;
    private Date activeTill;
    private String cronExpr;
    private byte[] data;
    private Date created;
    private Date modified;


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
            Map dataIn, Date activeFromIn, Date activeTillIn, String cronExprIn) {
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

    private byte[] serializeMap(Map dataMap) {
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
    public void setDataMap(Map dataMap) {
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
    public void setId(Long idIn) {
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
     * @return Returns the created.
     */
    public Date getCreated() {
        return created;
    }

    /**
     * @param createdIn The created to set.
     */
    public void setCreated(Date createdIn) {
        created = createdIn;
    }

    /**
     * @return Returns the modified.
     */
    public Date getModified() {
        return modified;
    }

    /**
     * @param modifiedIn The modified to set.
     */
    public void setModified(Date modifiedIn) {
        modified = modifiedIn;
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
