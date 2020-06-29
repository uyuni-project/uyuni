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

package com.suse.manager.maintenance.factory;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.model.maintenance.MaintenanceCalendar;
import com.suse.manager.model.maintenance.MaintenanceSchedule;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * {@link HibernateFactory} for Maintenance Windows-related objects.
 */
public class ScheduleFactory extends HibernateFactory {

    private static Logger log = Logger.getLogger(ScheduleFactory.class);

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

    /**
     * List schedules by User and Calendar
     * @param user the User
     * @param calendar the Calendar
     * @return the list of Schedules
     */
    public List<MaintenanceSchedule> listSchedulesByUserAndCalendar(User user, MaintenanceCalendar calendar) {
        return getSession()
                .createQuery("FROM MaintenanceSchedule " +
                        "WHERE org = :org and calendar = :calendar " +
                        "ORDER BY name ASC")
                .setParameter("org", user.getOrg())
                .setParameter("calendar", calendar).getResultList();
    }

    /**
     * List Maintenance Schedule Names belong to the given User
     * @param user the user
     * @return a list of Schedule names
     */
    public List<String> listScheduleNamesByUser(User user) {
        return getSession()
                .createQuery("SELECT name FROM MaintenanceSchedule WHERE org = :org")
                .setParameter("org", user.getOrg())
                .list();
    }

    /**
     * List Maintenance Schedules belonging to the given User
     * @param user the user
     * @return a list of Maintenance Schedules
     */
    public List<MaintenanceSchedule> listSchedulesByUser(User user) {
        return getSession()
                .createQuery("FROM MaintenanceSchedule WHERE org = :org")
                .setParameter("org", user.getOrg())
                .list();
    }

    /**
     * List Schedule names that use given calendar
     * @param user the user
     * @param calendar the calendar
     * @return a list of MaintenanceSchedules
     */
    public List<MaintenanceSchedule> listSchedulesByCalendar(User user, MaintenanceCalendar calendar) {
        return getSession()
                .createQuery("FROM MaintenanceSchedule WHERE org = :org and calendar = :calendar")
                .setParameter("org", user.getOrg())
                .setParameter("calendar", calendar)
                .list();
    }

    /**
     * Lookup a MaintenanceSchedule by user and name
     * @param user the user
     * @param name the schedule name
     * @return Optional Maintenance Schedule
     */
    public Optional<MaintenanceSchedule> lookupScheduleByUserAndName(User user, String name) {
        return getSession().createNamedQuery("MaintenanceSchedule.lookupByUserAndName")
                .setParameter("orgId", user.getOrg().getId())
                .setParameter("name", name)
                .uniqueResultOptional();
    }

    /**
     * Lookup MaintenanceSchedule by User and id
     * @param user the user
     * @param id the id of the schedule
     * @return Optional Maintenance Schedule
     */
    public Optional<MaintenanceSchedule> lookupScheduleByUserAndId(User user, Long id) {
        return getSession().createQuery("FROM MaintenanceSchedule WHERE org = :org AND id = :id")
                .setParameter("org", user.getOrg())
                .setParameter("id", id)
                .uniqueResultOptional();
    }

    /**
     * Lists IDs of Systems with given Schedule
     *
     * @param schedule the Schedule
     * @return List of System IDs
     */
    public List<Long> listSystemIdsWithSchedule(MaintenanceSchedule schedule) {
        return getSession().createQuery(
                "SELECT s.id from Server s " +
                        "WHERE s.maintenanceSchedule = :schedule")
                .setParameter("schedule", schedule)
                .list();
    }

    /**
     * List {@link MaintenanceSchedule}s assigned to given systems.
     *
     * @param systemIds the IDs of systems
     * @return the {@link MaintenanceSchedule}s assigned to given systems
     */
    public Set<MaintenanceSchedule> listSchedulesOfSystems(Set<Long> systemIds) {
        if (systemIds.isEmpty()) {
            return emptySet();
        }

        return (Set<MaintenanceSchedule>) HibernateFactory.getSession()
                .createQuery(
                        "SELECT s.maintenanceSchedule " +
                                "FROM Server s " +
                                "WHERE s.maintenanceSchedule IS NOT NULL " +
                                "AND s.id IN (:systemIds)")
                .setParameter("systemIds", systemIds)
                .stream()
                .collect(toSet());
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
