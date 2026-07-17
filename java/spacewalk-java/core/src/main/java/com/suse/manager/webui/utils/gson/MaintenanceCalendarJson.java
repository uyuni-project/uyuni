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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JSON representation of the Maintenance Window scheduling
 */
public class MaintenanceCalendarJson {

    /**
     * Constructor
     */
    public MaintenanceCalendarJson() { }

    /**
     * Constructor
     * @param idIn calendar id
     * @param nameIn calenar name
     * @param scheduleNamesIn schedule names
     */
    public MaintenanceCalendarJson(Long idIn, String nameIn,
                                   List<Map<String, String>> scheduleNamesIn) {
        this.id = idIn;
        this.name = nameIn;
        this.scheduleNames = scheduleNamesIn;
    }

    /** calendar ID */
    private Long id;

    /** The name of the calendar */
    private String name;

    /** The url to the ical file */
    private String url;

    /** The calendars ical data */
    private String data;

    /** List of schedule names used by a calendar */
    private List<Map<String, String>> scheduleNames;

    /** List of event names used by the calendar */
    private Set<String> eventNames;

    /** The reschedule strategy */
    private String strategy;

    /**
     * Gets the id of the calendar
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id of the calendar
     *
     * @param idIn the id
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Gets the name of the calendar
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the calendar
     *
     * @param nameIn the name
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * Gets the ical data of the calendar
     *
     * @return the calendar data
     */
    public String getData() {
        return data;
    }

    /**
     * Sets the ical data of the calendar
     *
     * @param dataIn the calendar data
     */
    public void setData(String dataIn) {
        this.data = dataIn;
    }

    /**
     * Gets the url to the ical file
     *
     * @return the calendar Url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the url to the ical file
     *
     * @param urlIn the calendar Url
     */
    public void setUrl(String urlIn) {
        this.url = urlIn;
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
     * Gets a list of event names used by a calendar object
     *
     * @return the list of eventNames
     */
    public Set<String> getEventNames() {
        return eventNames;
    }

    /**
     * Sets a list of event names used by a calendar object
     *
     * @param eventNamesIn the list of eventNames
     */
    public void setEventNames(Set<String> eventNamesIn) {
        this.eventNames = eventNamesIn;
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
}
