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

import java.util.List;
import java.util.Map;

/**
 * JSON representation of the Maintenance Window scheduling
 */
public class MaintenanceWindowJson {

    /** schedule ID */
    private Long scheduleId;

    /** Name of the schedule */
    private String scheduleName;

    /** Type of the schedule */
    private String scheduleType;

    /** calendar ID */
    private Long calendarId;

    /** The name of the calendar */
    private String calendarName;

    /** The url to the ical file */
    private String calendarUrl;

    /** The calendars ical data */
    private String calendarData;

    /** List of schedule names used by a calendar */
    private List<Map<String, String>> scheduleNames;

    /** The reschedule strategy */
    private String strategy;

    /** The upcoming maintenance windows */
    private List<Map<String, String>> maintenanceWindows;

    /**
     * Gets the id of the schedule
     *
     * @return the scheduleId
     */
    public Long getScheduleId() {
        return scheduleId;
    }

    /**
     * Sets the id of the schedule
     *
     * @param scheduleIdIn the scheduleId
     */
    public void setScheduleId(Long scheduleIdIn) {
        this.scheduleId = scheduleIdIn;
    }

    /**
     * Gets the name of the schedule
     *
     * @return the scheduleName
     */
    public String getScheduleName() {
        return scheduleName;
    }

    /**
     * Sets the name of the schedule
     *
     * @param scheduleNameIn the scheduleName
     */
    public void setScheduleName(String scheduleNameIn) {
        this.scheduleName = scheduleNameIn;
    }

    /**
     * Gets the type of the schedule
     *
     * @return the scheduleType
     */
    public String getScheduleType() {
        return scheduleType;
    }

    /**
     * Sets the type of the schedule
     *
     * @param scheduleTypeIn the scheduleType
     */
    public void setScheduleType(String scheduleTypeIn) {
        this.scheduleType = scheduleTypeIn;
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
     * Gets the ical data of the calendar
     *
     * @return the calendarData
     */
    public String getCalendarData() {
        return calendarData;
    }

    /**
     * Sets the ical data of the calendar
     *
     * @param calendarDataIn the calendarData
     */
    public void setCalendarData(String calendarDataIn) {
        this.calendarData = calendarDataIn;
    }

    /**
     * Gets the url to the ical file
     *
     * @return the calendarUrl
     */
    public String getCalendarUrl() {
        return calendarUrl;
    }

    /**
     * Sets the url to the ical file
     *
     * @param calendarUrlIn the calendarUrl
     */
    public void setCalendarUrl(String calendarUrlIn) {
        this.calendarUrl = calendarUrlIn;
    }

    /**
     * Gets a list of schedule names using a calendar object
     *
     * @return the list of scheduleNames
     */
    public List<Map<String, String>> getScheduleNames() {
        return scheduleNames;
    }

    /**
     * Sets a list of schedule names using a calendar object
     *
     * @param scheduleNamesIn the list of scheduleNames
     */
    public void setScheduleNames(List<Map<String, String>> scheduleNamesIn) {
        this.scheduleNames = scheduleNamesIn;
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
     * Gets the upcoming maintenance windows
     *
     * @return the upcoming maintenanceWindows
     */
    public List<Map<String, String>> getMaintenanceWindows() {
        return maintenanceWindows;
    }

    /**
     * Sets the upcoming maintenance Windows
     *
     * @param maintenanceWindowsIn the upcoming maintenance windows
     */
    public void setMaintenanceWindows(List<Map<String, String>> maintenanceWindowsIn) {
        this.maintenanceWindows = maintenanceWindowsIn;
    }
}
