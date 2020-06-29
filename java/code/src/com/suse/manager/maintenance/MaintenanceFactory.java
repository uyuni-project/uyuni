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

package com.suse.manager.maintenance;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import com.suse.manager.model.maintenance.MaintenanceCalendar;
import com.suse.manager.model.maintenance.MaintenanceSchedule;

import org.apache.log4j.Logger;

/**
 * {@link HibernateFactory} for Maintenance Windows-related objects.
 */
public class MaintenanceFactory extends HibernateFactory {

    private static Logger log = Logger.getLogger(MaintenanceFactory.class);

    /**
     * Save a MaintenanceCalendar
     * @param calendar the calendar
     */
    public void save(MaintenanceCalendar calendar) {
        getSession().save(calendar);
    }

    /**
     * Save a MaintenanceSchedule
     * @param schedule the schedule
     */
    public void save(MaintenanceSchedule schedule) {
        saveObject(schedule);
    }

    /**
     * Remove a {@link MaintenanceSchedule}
     * @param schedule to remove
     */
    public void remove(MaintenanceSchedule schedule) {
        removeObject(schedule);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

}
