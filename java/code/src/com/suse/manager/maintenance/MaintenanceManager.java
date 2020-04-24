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

import static com.redhat.rhn.common.hibernate.HibernateFactory.getSession;

import com.redhat.rhn.common.util.download.DownloadException;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.EntityNotExistsException;

import com.suse.manager.model.maintenance.MaintenanceSchedule;
import com.suse.manager.model.maintenance.MaintenanceSchedule.ScheduleType;
import com.suse.manager.utils.HttpHelper;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;

import com.suse.manager.model.maintenance.MaintenanceCalendar;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * MaintenanceManager
 */
public class MaintenanceManager {

    private static volatile MaintenanceManager instance = null;

    /**
     * Instantiate Maintenance Manager object
     *
     * @return MaintenanceManager object
     */
    public static MaintenanceManager instance() {
        if (instance == null) {
            synchronized (MaintenanceManager.class) {
                if (instance == null) {
                    instance = new MaintenanceManager();
                }
            }
        }
        return instance;
    }

    /**
     * Save a MaintenanceSchedule
     * @param schedule the schedule
     */
    public void save(MaintenanceSchedule schedule) {
        getSession().save(schedule);
    }

    /**
     * Remove a MaintenanceSchedule
     * @param schedule the schedule
     */
    public void remove(MaintenanceSchedule schedule) {
        getSession().remove(schedule);
    }

    /**
     * Save a MaintenanceCalendar
     * @param calendar the calendar
     */
    public void save(MaintenanceCalendar calendar) {
        getSession().save(calendar);
    }

    /**
     * Remove a MaintenanceCalendar
     * @param calendar the calendar
     */
    public void remove(MaintenanceCalendar calendar) {
        getSession().remove(calendar);
    }

    /**
     * List Maintenance Schedule Names belong to the given User
     * @param user the user
     * @return a list of Schedule names
     */
    @SuppressWarnings("unchecked")
    public List<String> listScheduleNamesByUser(User user) {
        return getSession()
            .createQuery("SELECT name FROM MaintenanceSchedule WHERE org = :org")
            .setParameter("org", user.getOrg())
            .list();
    }

    /**
     * Lookup a MaintenanceSchedule by user and name
     * @param user the user
     * @param name the schedule name
     * @return Optional Maintenance Schedule
     */
    @SuppressWarnings("unchecked")
    public Optional<MaintenanceSchedule> lookupMaintenanceScheduleByUserAndName(User user, String name) {
        return getSession().createNamedQuery("MaintenanceSchedule.lookupByUserAndName")
            .setParameter("orgId", user.getOrg().getId())
            .setParameter("name", name)
            .uniqueResultOptional();
    }

    /**
     * Create a Maintenance Schedule
     * @param user the creator
     * @param name the schedule name
     * @param type the schedule type
     * @param calendar and optional Maintenance Calendar
     * @return the created Maintenance Schedule
     */
    public MaintenanceSchedule createMaintenanceSchedule(User user, String name, ScheduleType type,
            Optional<MaintenanceCalendar> calendar) {
        MaintenanceSchedule ms = new MaintenanceSchedule();
        ms.setOrg(user.getOrg());
        ms.setName(name);
        ms.setScheduleType(type);
        calendar.ifPresent(ms::setCalendar);
        save(ms);
        return ms;
    }

    /**
     * Update a MaintenanceSchedule
     * @param user the user
     * @param name the schedule name
     * @param details values which should be changed (name, type, calendar)
     * @return the updated MaintenanceSchedule
     */
    public MaintenanceSchedule updateMaintenanceSchedule(User user, String name, Map<String, String> details) {
        MaintenanceSchedule schedule = lookupMaintenanceScheduleByUserAndName(user, name)
                .orElseThrow(() -> new EntityNotExistsException(name));
        if (details.containsKey("name")) {
            // TODO: should the identifier really be changeable?
            schedule.setName(details.get("name"));
        }
        if (details.containsKey("type")) {
            schedule.setScheduleType(ScheduleType.lookupByLabel(details.get("type")));
        }
        if (details.containsKey("calendar")) {
            MaintenanceCalendar calendar = lookupCalendarByUserAndLabel(user, details.get("calendar"))
                .orElseThrow(() -> new EntityNotExistsException(details.get("calendar")));

            schedule.setCalendar(calendar);
        }
        save(schedule);
        manageAffectedScheduledActions(user, schedule, Collections.EMPTY_LIST);
        return schedule;
    }

    /**
     * List Maintenance Calendar Labels belonging to the given User
     * @param user the user
     * @return a list of Calendar labels
     */
    public List<String> listCalendarLabelsByUser(User user) {
        return getSession()
            .createQuery("SELECT label FROM MaintenanceCalendar WHERE org = :org")
            .setParameter("org", user.getOrg())
            .list();
    }

    /**
     * Lookup Maintenance Calendar by User and Label
     * @param user the user
     * @param label the label of the calendar
     * @return Optional Maintenance Calendar
     */
    public Optional<MaintenanceCalendar> lookupCalendarByUserAndLabel(User user, String label) {
        return getSession().createNamedQuery("MaintenanceCalendar.lookupByUserAndName")
                .setParameter("orgId", user.getOrg().getId())
                .setParameter("label", label).uniqueResultOptional();
    }

    /**
     * Create a MaintenanceCalendar with ICal Data
     * @param user the creator
     * @param label the label for the calendar
     * @param ical the Calendar data in ICal format
     * @return the created Maintenance Calendar
     */
    public MaintenanceCalendar createMaintenanceCalendar(User user, String label, String ical) {
        MaintenanceCalendar mc = new MaintenanceCalendar();
        mc.setOrg(user.getOrg());
        mc.setLabel(label);
        mc.setIcal(ical);
        save(mc);
        return mc;
    }

    /**
     * Create a MaintenanceCalendar using an URL
     * @param user the creator
     * @param label the label for the calendar
     * @param url URL pointing to the Calendar Data
     * @return the created Maintenance Calendar
     */
    public MaintenanceCalendar createMaintenanceCalendarWithUrl(User user, String label, String url)
            throws DownloadException {
        MaintenanceCalendar mc = new MaintenanceCalendar();
        mc.setOrg(user.getOrg());
        mc.setLabel(label);
        mc.setUrl(url);
        mc.setIcal(fetchCalendarData(url));
        save(mc);
        return mc;
    }

    /**
     * Update a MaintenanceCalendar
     * @param user the user
     * @param label the calendar label
     * @param details the details which should be updated (label, ical, url)
     * @return the updated MaintenanceCalendar
     */
    public MaintenanceCalendar updateCalendar(User user, String label, Map<String, String> details) {
        MaintenanceCalendar calendar = lookupCalendarByUserAndLabel(user, label)
                .orElseThrow(() -> new EntityNotExistsException(label));
        if (details.containsKey("label")) {
            // TODO: should the identifier really be changeable?
            calendar.setLabel(details.get("label"));
        }
        if (details.containsKey("ical")) {
            calendar.setIcal(details.get("ical"));
        }
        else if (details.containsKey("url")) {
            calendar.setUrl(details.get("url"));
            calendar.setIcal(fetchCalendarData(details.get("url")));
        }
        save(calendar);
        listSchedulesByUserAndCalendar(user, calendar).forEach(schedule ->
            manageAffectedScheduledActions(user, schedule, Collections.EMPTY_LIST));
        return calendar;
    }

    /**
     * Refresh the calendar data using the configured URL
     * @param user the user
     * @param label the calendar label
     * @throws EntityNotExistsException when calendar or url does not exist
     */
    public void refreshCalendar(User user, String label) {
        MaintenanceCalendar calendar = lookupCalendarByUserAndLabel(user, label)
                .orElseThrow(() -> new EntityNotExistsException(label));
        calendar.setIcal(fetchCalendarData(
                calendar.getUrlOpt().orElseThrow(() -> new EntityNotExistsException("url"))));
        save(calendar);
        listSchedulesByUserAndCalendar(user, calendar).forEach(schedule ->
            manageAffectedScheduledActions(user, schedule, Collections.EMPTY_LIST));
    }

    @SuppressWarnings("unchecked")
    private List<MaintenanceSchedule> listSchedulesByUserAndCalendar(User user, MaintenanceCalendar calendar) {
        return getSession()
                .createQuery("from MaintenanceSchedule WHERE org = :org and calendar = :calendar")
                .setParameter("org", user.getOrg())
                .setParameter("calendar", calendar).getResultList();
    }

    protected String fetchCalendarData(String url) {
        try {
            HttpHelper http = new HttpHelper();
            HttpResponse response = http.sendGetRequest(url);
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() != HttpStatus.SC_OK) {
                throw new DownloadException(url, status.getReasonPhrase(), status.getStatusCode());
            }
            return http.getBodyAsString(response, "UTF-8");
        }
        catch (IOException e) {
            throw new DownloadException(url, e.getMessage(), 500);
        }
        catch (ParseException p) {
            throw new DownloadException(url, p.getMessage(), 500);
        }
    }

    protected void manageAffectedScheduledActions(User user, MaintenanceSchedule schedule,
            List<String> scheduleStrategy) {
        // TODO: implement it
    }
}
