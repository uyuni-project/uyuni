/*
 * Copyright (c) 2010--2015 Red Hat, Inc.
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

import com.redhat.rhn.taskomatic.TaskoFactory;

import java.util.Date;


/**
 * TaskoRun
 */
public class TaskoRun {

    public static final String STATUS_READY_TO_RUN = "READY";
    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_FINISHED = "FINISHED";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_SKIPPED = "SKIPPED";
    public static final String STATUS_INTERRUPTED = "INTERRUPTED";

    private Long id;
    private Integer orgId;
    private TaskoTemplate template;
    private Long scheduleId;
    private Date startTime;
    private Date endTime;
    private String status;
    private Date created;
    private Date modified;

    /**
     * default constructor required by hibernate
     */
    public TaskoRun() {
    }

    /**
     * constructor
     * run is always associated with organization, template and schedule
     * @param orgIdIn organization id
     * @param templateIn template id
     * @param scheduleIdIn schedule id
     */
    public TaskoRun(Integer orgIdIn, TaskoTemplate templateIn, Long scheduleIdIn) {
        setOrgId(orgIdIn);
        setTemplate(templateIn);
        setScheduleId(scheduleIdIn);
        saveStatus(STATUS_READY_TO_RUN);
    }

    /**
     * run start method
     * has to be called right before job execution
     */
    public void start() {
        setStartTime(new Date());
        saveStatus(STATUS_RUNNING);
    }

    /**
     * run finish method
     * has to be called right after job execution
     */
    public void finished() {
        setEndTime(new Date());
    }

    /**
     * if task execution will be skipped (used for queue tasks)
     */
    public void skipped() {
        Date now = new Date();
        setStartTime(now);
        setEndTime(now);
        saveStatus(STATUS_SKIPPED);
    }

    /**
     * if task execution fails
     */
    public void failed() {
        finished();
        saveStatus(TaskoRun.STATUS_FAILED);
    }

    /**
     * sets run status
     * @param statusIn status to set
     */
    public void saveStatus(String statusIn) {
        setStatus(statusIn);
        TaskoFactory.save(this);
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
        this.id = idIn;
    }

    /**
     * @return Returns the templateId.
     */
    public TaskoTemplate getTemplate() {
        return template;
    }

    /**
     * @param templateId The templateId to set.
     */
    public void setTemplate(TaskoTemplate templateId) {
        this.template = templateId;
    }


    /**
     * @return Returns the startTime.
     */
    public Date getStartTime() {
        return startTime;
    }


    /**
     * @param startTimeIn The startTime to set.
     */
    public void setStartTime(Date startTimeIn) {
        this.startTime = startTimeIn;
    }

    /**
     * @return Returns the endTime.
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * @param endTimeIn The endTime to set.
     */
    public void setEndTime(Date endTimeIn) {
        this.endTime = endTimeIn;
    }

    /**
     * @return Returns the status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param statusIn The status to set.
     */
    public void setStatus(String statusIn) {
        this.status = statusIn;
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
        this.created = createdIn;
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
        this.modified = modifiedIn;
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
        this.orgId = orgIdIn;
    }

    /**
     * @return Returns the jobLabel.
     */
    public Long getScheduleId() {
        return scheduleId;
    }

    /**
     * @param scheduleIdIn The jobLabel to set.
     */
    public void setScheduleId(Long scheduleIdIn) {
        this.scheduleId = scheduleIdIn;
    }
}
