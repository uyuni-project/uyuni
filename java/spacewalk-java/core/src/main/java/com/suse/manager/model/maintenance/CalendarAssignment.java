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

package com.suse.manager.model.maintenance;

public class CalendarAssignment {

    private Long calendarId;
    private String calendarName;
    private Long scheduleId;
    private String scheduleName;

    /**
     * Constructor
     * @param calendarIdIn calendar id
     * @param calendarNameIn calendar name
     * @param scheduleIdIn schedule id
     * @param scheduleNameIn schedule name
     */
    public CalendarAssignment(Long calendarIdIn, String calendarNameIn, Long scheduleIdIn, String scheduleNameIn) {
        this.calendarId = calendarIdIn;
        this.calendarName = calendarNameIn;
        this.scheduleId = scheduleIdIn;
        this.scheduleName = scheduleNameIn;
    }

    /**
     * Gets the calendarId.
     *
     * @return calendarId
     */
    public Long getCalendarId() {
        return calendarId;
    }

    /**
     * Gets the calendarName.
     *
     * @return calendarName
     */
    public String getCalendarName() {
        return calendarName;
    }

    /**
     * Gets the scheduleId.
     *
     * @return scheduleId
     */
    public Long getScheduleId() {
        return scheduleId;
    }

    /**
     * Gets the scheduleName.
     *
     * @return scheduleName
     */
    public String getScheduleName() {
        return scheduleName;
    }
}
