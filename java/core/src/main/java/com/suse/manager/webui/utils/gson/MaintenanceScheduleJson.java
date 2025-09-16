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

import java.util.Set;

/**
 * JSON representation of the Maintenance Window scheduling
 */
public class MaintenanceScheduleJson {

    /** schedule ID */
    private Long id;

    /** Name of the schedule */
    private String name;

    /** Type of the schedule */
    private String type;

    /** calendar ID */
    private Long calendarId;

    /** The name of the calendar */
    private String calendarName;

    /** The reschedule strategy */
    private String strategy;

    /** List of event names used by the schedule */
    private Set<String> eventNames;

    /**
     * Gets the id of the schedule
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id of the schedule
     *
     * @param idIn the id
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Gets the name of the schedule
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the schedule
     *
     * @param nameIn the name
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * Gets the type of the schedule
     *
     * @return the scheduleType
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of the schedule
     *
     * @param typeIn the scheduleType
     */
    public void setType(String typeIn) {
        this.type = typeIn;
    }

    /**
     * Gets the id of the calendar
     *
     * @return the calendarId
     */
    public Long getCalendarId() {
        return calendarId;
    }

    /**
     * Sets the id of the calendar
     *
     * @param calendarIdIn the calendarId
     */
    public void setCalendarId(Long calendarIdIn) {
        this.calendarId = calendarIdIn;
    }

    /**
     * Gets the name of the calendar
     *
     * @return the calendarName
     */
    public String getCalendarName() {
        return calendarName;
    }

    /**
     * Sets the name of the calendar
     *
     * @param calendarNameIn the calendarName
     */
    public void setCalendarName(String calendarNameIn) {
        this.calendarName = calendarNameIn;
    }

    /**
     * Gets the reschedule strategy
     *
     * @return the reschedule strategy
     */
    public String getRescheduleStrategy() {
        return strategy;
    }

    /**
     * Sets the reschedule strategy
     *
     * @param strategyIn the reschedule strategy
     */
    public void setRescheduleStrategy(String strategyIn) {
        this.strategy = strategyIn;
    }

    /**
     * Gets a list of event names used by a schedule object
     *
     * @return the list of eventNames
     */
    public Set<String> getEventNames() {
        return eventNames;
    }

    /**
     * Sets a list of event names used by a schedule object
     *
     * @param eventNamesIn the list of eventNames
     */
    public void setEventNames(Set<String> eventNamesIn) {
        this.eventNames = eventNamesIn;
    }
}
