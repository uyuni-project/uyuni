/**
 * Copyright (c) 2020 SUSE LLC
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
package com.suse.manager.webui.utils.gson;

import java.util.Date;
import java.util.Map;

/**
 * JSON representation of the Recurring State Scheduling
 */
public class RecurringStateScheduleJson {

    private Long recurringActionId;

    /** Target ID */
    private Long targetId;

    /** Name of the schedule */
    private String scheduleName;

    /** Schedule is active */
    private boolean active;

    /** The schedule type */
    private String type;

    /** The schedule target Type.
     * Either minion, group, selected minions or organization
     */
    private String targetType;

    /** Array containing Quartz information */
    private Map<String, String> cronTimes;

    /** Cron format string */
    private String cron;

    /** Is test run */
    private boolean test;

    /** Schedule creation date **/
    private Date createdAt;

    /**
     * Gets the recurringActionId.
     *
     * @return recurringActionId
     */
    public Long getRecurringActionId() {
        return recurringActionId;
    }

    /**
     * Sets the recurringActionId.
     *
     * @param recurringActionIdIn the recurringActionId
     */
    public void setRecurringActionId(Long recurringActionIdIn) {
        recurringActionId = recurringActionIdIn;
    }

    /**
     * @return the minion ids
     */
    public Long getTargetId() {
        return targetId;
    }

    /**
     * @return the name of the schedule
     */
    public String getScheduleName() {
        return scheduleName;
    }

    /**
     * @return whether the schedule is active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @return the type of the schedule
     */
    public String getType() {
        return type;
    }

    /**
     * @return the target type of the schedule
     */
    public String getTargetType() {
        return targetType;
    }

    /**
     * @return the Array containing Quartz information
     */
    public Map<String, String> getCronTimes() {
        return cronTimes;
    }

    /**
     * @return the Array containing Quartz information
     */
    public String getCron() {
        return cron;
    }

    /**
     * @return the Array containing Quartz information
     */
    public boolean isTest() {
        return test;
    }

    /**
     * Sets the targetId.
     *
     * @param targetIdIn the targetId
     */
    public void setTargetId(Long targetIdIn) {
        targetId = targetIdIn;
    }

    /**
     * Sets the scheduleName.
     *
     * @param scheduleNameIn the scheduleName
     */
    public void setScheduleName(String scheduleNameIn) {
        scheduleName = scheduleNameIn;
    }

    /**
     * Sets the active.
     *
     * @param activeIn the active
     */
    public void setActive(boolean activeIn) {
        active = activeIn;
    }

    /**
     * Sets the type.
     *
     * @param typeIn the type
     */
    public void setType(String typeIn) {
        type = typeIn;
    }

    /**
     * Sets the targetType.
     *
     * @param targetTypeIn the targetType
     */
    public void setTargetType(String targetTypeIn) {
        targetType = targetTypeIn;
    }

    /**
     * Sets the cronTimes.
     *
     * @param cronTimesIn the cronTimes
     */
    public void setCronTimes(Map<String, String> cronTimesIn) {
        cronTimes = cronTimesIn;
    }

    /**
     * Sets the cron.
     *
     * @param cronIn the cron
     */
    public void setCron(String cronIn) {
        cron = cronIn;
    }

    /**
     * Sets the test.
     *
     * @param testIn the test
     */
    public void setTest(boolean testIn) {
        test = testIn;
    }

    /**
     * Gets the creation date
     *
     * @return createdAt
     */
    public Date getCreated() {
        return createdAt;
    }

    /**
     *  Sets the creation date
     *
     * @param createdAtIn schedule creation date
     */
    public void setCreated(Date createdAtIn) {
        this.createdAt = createdAtIn;
    }
}
