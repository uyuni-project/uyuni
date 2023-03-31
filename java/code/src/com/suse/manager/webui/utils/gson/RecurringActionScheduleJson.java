/*
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

import com.redhat.rhn.domain.recurringactions.type.RecurringActionType;

/**
 * JSON representation of the Recurring Action Scheduling
 */
public class RecurringActionScheduleJson {

    private Long recurringActionId;

    /** Target ID */
    private Long targetId;

    /** Name of the schedule */
    private String scheduleName;

    /** Schedule is active */
    private boolean active;

    private RecurringActionDetailsDto details;

    /** The schedule target Type.
     * Either minion, group, selected minions or organization
     */
    private String targetType;

    /** The name of the target */
    private String targetName;

    /** Cron format string */
    private String cron;

    private RecurringActionType.ActionType actionType;
    private String actionTypeDescription;

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
     * @return the target type of the schedule
     */
    public String getTargetType() {
        return targetType;
    }

    /**
     * @return the Array containing Quartz information
     */
    public String getCron() {
        return cron;
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
     * Sets the targetType.
     *
     * @param targetTypeIn the targetType
     */
    public void setTargetType(String targetTypeIn) {
        targetType = targetTypeIn;
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
     *  Sets the name of the target
     *
     * @param nameIn name of the target
     */
    public void setTargetName(String nameIn) {
        this.targetName = nameIn;
    }

    /**
     * Sets the type of the action
     *
     * @param actionTypeIn the type of the action
     */
    public void setActionType(RecurringActionType.ActionType actionTypeIn) {
        this.actionType = actionTypeIn;
        this.actionTypeDescription = actionTypeIn.getDescription();
    }

    /**
     * Sets the type of the action based on the enumeration string
     *
     * @param actionTypeIn the enumeration string representing action's type
     */
     public void setActionType(String actionTypeIn) {
         setActionType(RecurringActionType.ActionType.valueOf(actionTypeIn));
     }

    /**
     * Gets the details of the recurring action
     * @return an instance of RecurringActionDetailsDto
     */
    public RecurringActionDetailsDto getDetails() {
        return details;
    }

    /**
     * Sets the details of the recurring action
     * @param detailsIn the details object
     */
    public void setDetails(RecurringActionDetailsDto detailsIn) {
        details = detailsIn;
    }
}
