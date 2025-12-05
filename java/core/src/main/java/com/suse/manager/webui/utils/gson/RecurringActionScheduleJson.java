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

import com.redhat.rhn.domain.recurringactions.GroupRecurringAction;
import com.redhat.rhn.domain.recurringactions.MinionRecurringAction;
import com.redhat.rhn.domain.recurringactions.OrgRecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringAction;
import com.redhat.rhn.domain.recurringactions.type.RecurringActionType;
import com.redhat.rhn.frontend.dto.BaseTupleDto;

import java.util.List;
import java.util.Objects;

import javax.persistence.Tuple;

/**
 * JSON representation of the Recurring Action Scheduling
 */
public class RecurringActionScheduleJson extends BaseTupleDto {

    /**
     * Default constructor
     */
    public RecurringActionScheduleJson() { }

    /**
     * Create an instance based on an entity and its type
     * @param recurringActionIn the entity instance to be turned into JSON object
     */
    public RecurringActionScheduleJson(RecurringAction recurringActionIn) {
        setRecurringActionId(recurringActionIn.getId());
        setScheduleName(recurringActionIn.getName());
        setCron(recurringActionIn.getCronExpr());
        setActive(recurringActionIn.isActive());
        setTargetType(recurringActionIn.getTargetType().toString());
        setTargetId(recurringActionIn.getEntityId());
        setTargetName(getEntityName(recurringActionIn));
        setActionType(recurringActionIn.getActionType());
    }

    /**
     * Constructor used to populate using DTO projection from the data of query that list recurring actions
     *
     * @param tuple JPA tuple
     */
    public RecurringActionScheduleJson(Tuple tuple) {
        setRecurringActionId(
            getTupleValue(tuple, "recurring_action_id", Number.class).map(Number::longValue).orElse(null)
        );
        setTargetId(getTupleValue(tuple, "target_id", Number.class).map(Number::longValue).orElse(null));
        setTargetName(getTupleValue(tuple, "target_name", String.class).orElse("-"));
        setScheduleName(getTupleValue(tuple, "schedule_name", String.class).orElse("-"));
        setActive(getTupleValue(tuple, "active", Character.class).orElse('-').equals('Y'));
        setTargetType(getTupleValue(tuple, "target_type", String.class).orElse("-"));
        setCron(getTupleValue(tuple, "cron", String.class).orElse("-"));
        setActionType(getTupleValue(tuple, "action_type", String.class).orElse("-"));
    }

    private Long recurringActionId;

    /** Target ID */
    private Long targetId;

    /** Ids of the entities members */
    private List<Long> memberIds;

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

    /** Action type */
    private RecurringActionType.ActionType actionType;

    /** Action type description */
    private String actionTypeDescription;

    /** User has permission to access the target */
    private boolean targetAccessible = true;

    /**
     * @return recurring action id
     */
    @Override
    public Long getId() {
        return getRecurringActionId();
    }

    /**
     * Gets the List of member ids
     *
     * @return the member ids
     */
    public List<Long> getMemberIds() {
        return memberIds;
    }

    /**
     * Gets the recurringActionId.
     *
     * @return recurringActionId
     */
    public Long getRecurringActionId() {
        return recurringActionId;
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
     * @return the type of recurring action
     */
    public RecurringActionType.ActionType getActionType() {
        return actionType;
    }

    /**
     * Gets the action type description
     *
     * @return the action type description
     */
    public String getActionTypeDescription() {
        return actionTypeDescription;
    }

    /**
     * Gets the details of the recurring action
     * @return an instance of RecurringActionDetailsDto
     */
    public RecurringActionDetailsDto getDetails() {
        return details;
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
     * Sets the details of the recurring action
     * @param detailsIn the details object
     */
    public void setDetails(RecurringActionDetailsDto detailsIn) {
        details = detailsIn;
    }


    /**
     * @return whether the user has permission to access the target of a recurring action
     */
    public boolean isTargetAccessible() {
        return targetAccessible;
    }

    /**
     * Sets if the user has permission to access the target of the recurring action
     * @param targetAccessibleIn the boolean indicating if the target is accessible
    */
    public void setTargetAccessible(boolean targetAccessibleIn) {
        targetAccessible = targetAccessibleIn;
    }

    private static String getEntityName(RecurringAction action) {
        switch (action.getTargetType()) {
            case MINION:
                return ((MinionRecurringAction) action).getMinion().getName();
            case GROUP:
                return ((GroupRecurringAction) action).getGroup().getName();
            case ORG:
                return ((OrgRecurringAction) action).getOrg().getName();
            default:
                throw new IllegalStateException("Unsupported type " + action.getTargetType());
        }
    }

    @Override
    public boolean equals(Object oIn) {
        if (this == oIn) {
            return true;
        }
        if (oIn == null || getClass() != oIn.getClass()) {
            return false;
        }
        RecurringActionScheduleJson that = (RecurringActionScheduleJson) oIn;
        return Objects.equals(recurringActionId, that.recurringActionId) && Objects.equals(targetId, that.targetId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recurringActionId, targetId);
    }
}
