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

package com.suse.manager.model.maintenance;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.user.User;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * {@link HibernateFactory} for {@link MaintenanceCalendar}
 */
public class CalendarFactory extends HibernateFactory {

    private static Logger log = Logger.getLogger(CalendarFactory.class);

    /**
     * Save a MaintenanceCalendar
     * @param calendar the calendar
     */
    public void save(MaintenanceCalendar calendar) {
        getSession().save(calendar);
    }

    /**
     * Remove a {@link MaintenanceCalendar}
     * @param calendar to remove
     */
    public void remove(MaintenanceCalendar calendar) {
        removeObject(calendar);
    }

    /**
     * List Maintenance Calendar Labels belonging to the given User
     * @param user the user
     * @return a list of Calendar labels
     */
    public List<String> listCalendarLabelsByUser(User user) {
        return getSession()
                .createQuery("SELECT label FROM MaintenanceCalendar WHERE org = :org ORDER BY label ASC")
                .setParameter("org", user.getOrg())
                .list();
    }

    /**
     * List Maintenance Calendars belonging to the given User
     * @param user the user
     * @return a list of Maintenance Calendars
     */
    public List<MaintenanceCalendar> listByUser(User user) {
        return getSession()
                .createQuery("FROM MaintenanceCalendar WHERE org = :org")
                .setParameter("org", user.getOrg())
                .list();
    }

    /**
     * Lookup Maintenance Calendar by User and Label
     * @param user the user
     * @param label the label of the calendar
     * @return Optional Maintenance Calendar
     */
    public Optional<MaintenanceCalendar> lookupByUserAndLabel(User user, String label) {
        return getSession().createNamedQuery("MaintenanceCalendar.lookupByUserAndName")
                .setParameter("orgId", user.getOrg().getId())
                .setParameter("label", label).uniqueResultOptional();
    }

    /**
     * Lookup Maintenance Calendar by User and id
     * @param user the user
     * @param id the id of the calendar
     * @return Optional Maintenance Calendar
     */
    public Optional<MaintenanceCalendar> lookupByUserAndId(User user, Long id) {
        return getSession().createQuery("FROM MaintenanceCalendar WHERE org = :org AND id = :id")
                .setParameter("org", user.getOrg())
                .setParameter("id", id).uniqueResultOptional();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
