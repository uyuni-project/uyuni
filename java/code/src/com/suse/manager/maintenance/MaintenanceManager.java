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
import static com.redhat.rhn.domain.role.RoleFactory.ORG_ADMIN;
import static java.util.Collections.emptySet;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.common.util.download.DownloadException;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionStatus;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.manager.EntityNotExistsException;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.model.maintenance.MaintenanceCalendar;
import com.suse.manager.model.maintenance.MaintenanceSchedule;
import com.suse.manager.model.maintenance.MaintenanceSchedule.ScheduleType;
import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.utils.HttpHelper;
import com.suse.utils.Opt;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.filter.Filter;
import net.fortuna.ical4j.filter.HasPropertyRule;
import net.fortuna.ical4j.filter.PeriodRule;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.property.Summary;

/**
 * MaintenanceManager
 */
public class MaintenanceManager {
    private static Logger log = Logger.getLogger(MaintenanceManager.class);

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
     * Given the systems, return upcoming maintenance windows.
     *
     * The windows are returned as a list of triples consisting of:
     * - window start date as a human-readable string
     * - window end date as a human-readable string
     * - start date as number of milliseconds since the epoch
     *
     * The formatting is done by {@link LocalizationService}.
     *
     * The upper limit of returned maintenance windows is currently hardcoded to 10.
     *
     * If given systems do not have any maint. <b>schedules</b> assigned, return an empty optional.
     * If given systems have different maint. schedules assigned, throw an exception.
     * Otherwise return list of maintenance windows (the list can be empty, if the schedule does not contain
     * any upcoming maintenance windows).
     *
     * @param systemIds the system ids
     * @return the optional upcoming maintenance windows
     * @throws IllegalStateException if two or more systems have different maint. schedules assigned
     */
    public Optional<List<Triple<String, String, Long>>> calculateUpcomingMaintenanceWindows(Set<Long> systemIds)
            throws IllegalStateException {
        Set<MaintenanceSchedule> schedules = MaintenanceManager.instance().listSchedulesOfSystems(systemIds);
        // if there are no schedules, there are no maintenance windows
        if (schedules.isEmpty()) {
            return empty();
        }

        // there are multiple schedules for systems, we throw an exception
        if (schedules.size() > 1) {
            String scheduleIds = schedules.stream().map(s -> s.getId().toString()).collect(joining(","));
            throw new IllegalStateException("Multiple schedules: " + scheduleIds);
        }

        MaintenanceSchedule schedule = schedules.iterator().next();
        return calculateMaintenanceWindows(schedule);
    }

    /**
     * Given MaintenanceSchedule calculate upcoming maintenance windows
     *
     * The windows are returned as a list of triples consisting of:
     * - window start date as a human-readable string
     * - window end date as a human-readable string
     * - start date as number of milliseconds since the epoch
     *
     * The formatting is done by {@link LocalizationService}.
     *
     * The upper limit of returned maintenance windows is currently hardcoded to 10.
     *
     * @param schedule the given MaintenanceSchedule
     * @return the optional upcoming maintenance windows
     */
    public Optional<List<Triple<String, String, Long>>> calculateMaintenanceWindows(MaintenanceSchedule schedule) {
        Optional<String> multiScheduleName = getScheduleNameForMulti(schedule);

        Stream<Pair<Instant, Instant>> periodStream = schedule.getCalendarOpt()
                .flatMap(c -> parseCalendar(c))
                .map(c -> calculateUpcomingPeriods(c, multiScheduleName, Instant.now(), 10))
                .orElseGet(Stream::empty);

        List<Triple<String, String, Long>> result = periodStream
                .map(p -> Triple.of(
                        LocalizationService.getInstance().formatDate(p.getLeft()),
                        LocalizationService.getInstance().formatDate(p.getRight()),
                        p.getLeft().toEpochMilli()))
                .collect(toList());
        return of(result);
    }

    /**
     * Convenience method: return schedule name if the schedule type is MULTI, return empty otherwise
     * @param schedule the schedule
     * @return optional of schedule name
     */
    private static Optional<String> getScheduleNameForMulti(MaintenanceSchedule schedule) {
        if (schedule.getScheduleType() == ScheduleType.MULTI) {
            return of(schedule.getName());
        }
        return empty();
    }

    /**
     * Calculate upcoming maintenance windows starting from given date based on calendar and optional filter name
     * (in case we're dealing with MULTI calendar and want to filter only events we're interested in).
     *
     * The algorithm only checks maintenance windows within roughly a year and a month since the startDate.
     *
     * @param calendar the {@link Calendar}
     * @param eventName for MULTI calendars: only deal with events with this name, filter out the rest
     * @param startDate the start date
     * @param limit upper limit of maintenance windows to return
     * @return the list of upcoming maintenance windows
     */
    public Stream<Pair<Instant, Instant>> calculateUpcomingPeriods(Calendar calendar, Optional<String> eventName,
            Instant startDate, int limit) {
        ComponentList<CalendarComponent> allEvents = calendar.getComponents(Component.VEVENT);

        Collection<CalendarComponent> filteredEvents = eventName.map(name -> {
            Predicate<CalendarComponent> summary = c -> c.getProperty("SUMMARY").equals(name);
            Predicate<CalendarComponent>[] ps = new Predicate[]{summary};
            Filter<CalendarComponent> filter = new Filter<>(ps, Filter.MATCH_ALL);
            return filter.filter(allEvents);
        }).orElse(allEvents);

        // we will look a year and month to the future
        Period period = new Period(new DateTime(startDate.toEpochMilli()), Duration.ofDays(365 + 31));

        List<PeriodList> periodLists = filteredEvents.stream()
                .map(c -> c.calculateRecurrenceSet(period))
                .filter(l -> !l.isEmpty())
                .collect(toList());

        Stream<Pair<Instant, Instant>> sortedLimited = periodLists.stream()
                .map(pl -> pl.stream())
                .reduce(Stream.empty(), Stream::concat)
                .sorted()
                .limit(limit)
                .map(p -> Pair.of(p.getStart().toInstant(), p.getRangeEnd().toInstant()));

        return sortedLimited;
    }

    /**
     * Check if an action can be scheduled at given date for given systems.
     *
     * If some systems have a {@link MaintenanceSchedule} and are outside of their maintenance windows,
     * throw the {@link NotInMaintenanceModeException} that bears the offending schedules.
     *
     * @param systemIds the system IDs to check
     * @param action the action
     * @throws NotInMaintenanceModeException when some systems are outside of maintenance window
     */
    public void checkMaintenanceWindows(Set<Long> systemIds, Action action) {
        Date scheduleDate = action.getEarliestAction();

        // we only take maintenance-mode-only actions and actions that don't have prerequisite
        // (first actions in action chains) into account
        if (action.getActionType().isMaintenancemodeOnly() && action.getPrerequisite() == null) {
            // Special Case: we want to allow channel changing but it calls a state.apply
            if (action.getActionType().equals(ActionFactory.TYPE_APPLY_STATES)) {
                ApplyStatesAction applyStatesAction = (ApplyStatesAction) action;
                if (applyStatesAction.getDetails() != null &&
                        applyStatesAction.getDetails().getMods().equals(
                            List.of(ApplyStatesEventMessage.CHANNELS))) {
                    return;
                }
            }
            Set<MaintenanceSchedule> offendingSchedules = listSystemSchedulesNotMachingDate(systemIds, scheduleDate);
            if (!offendingSchedules.isEmpty()) {
                throw new NotInMaintenanceModeException(offendingSchedules, scheduleDate);
            }
        }
    }

    /**
     * List {@link MaintenanceSchedule}s which are assigned to given systems and which do NOT match given date
     * (no maintenance windows in given date).
     *
     * @param systemIds the system IDs to check
     * @param date the schedule date of the action
     * @return set of {@link MaintenanceSchedule}s
     */
    private Set<MaintenanceSchedule> listSystemSchedulesNotMachingDate(Set<Long> systemIds, Date date) {
        return listSchedulesOfSystems(systemIds).stream()
                .filter(schedule -> {
                    Collection<CalendarComponent> events = getScheduleEventsAtDate(date, schedule, schedule
                            .getCalendarOpt().flatMap(c -> parseCalendar(c)));
                    return events.isEmpty();
                })
                .collect(toSet());
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


    /**
     * Save a MaintenanceSchedule
     * @param schedule the schedule
     */
    protected void save(MaintenanceSchedule schedule) {
        getSession().save(schedule);
    }

    /**
     * Remove a MaintenanceSchedule
     * @param user the user
     * @param schedule the schedule
     */
    public void remove(User user, MaintenanceSchedule schedule) {
        ensureOrgAdmin(user);
        ensureScheduleAccessible(user, schedule);
        getSession().remove(schedule);
    }

    /**
     * Save a MaintenanceCalendar
     * @param calendar the calendar
     */
    protected void save(MaintenanceCalendar calendar) {
        getSession().save(calendar);
    }

    /**
     * Remove a MaintenanceCalendar
     *
     * When a calendar is removed, depending schedules loose all Maintenance Windows.
     * This require to cancel all pending actions or let the removal fail.
     *
     * @param user the user
     * @param calendar the calendar
     * @param cancelScheduledActions cancel scheduled actions
     * @return List of results
     */
    public List<RescheduleResult> remove(User user, MaintenanceCalendar calendar, boolean cancelScheduledActions) {
        ensureOrgAdmin(user);
        ensureCalendarAccessible(user, calendar);
        List<RescheduleResult> result = new LinkedList<>();
        List<MaintenanceSchedule> schedules = listSchedulesByUserAndCalendar(user, calendar);
        getSession().remove(calendar);
        for (MaintenanceSchedule schedule: schedules) {
            schedule.setCalendar(null);
            List<RescheduleStrategy> strategy = new LinkedList<>();
            if (cancelScheduledActions) {
                strategy = Collections.singletonList(new CancelRescheduleStrategy());
            }
            RescheduleResult r = manageAffectedScheduledActions(user, schedule, strategy);
            if (!r.isSuccess()) {
                // in case of false, update failed and we had a DB rollback
                return Collections.singletonList(r);
            }
            result.add(r);
        }
        return result;
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
     * List Maintenance Schedules belonging to the given User
     * @param user the user
     * @return a list of Maintenance Schedules
     */
    @SuppressWarnings("unchecked")
    public List<MaintenanceSchedule> listMaintenanceSchedulesByUser(User user) {
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
    @SuppressWarnings("unchecked")
    public List<MaintenanceSchedule> listMaintenanceSchedulesByCalendar(User user, MaintenanceCalendar calendar) {
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
    @SuppressWarnings("unchecked")
    public Optional<MaintenanceSchedule> lookupMaintenanceScheduleByUserAndName(User user, String name) {
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
    @SuppressWarnings("unchecked")
    public Optional<MaintenanceSchedule> lookupMaintenanceScheduleByUserAndId(User user, Long id) {
        return getSession().createQuery("FROM MaintenanceSchedule WHERE org = :org AND id = :id")
                .setParameter("org", user.getOrg())
                .setParameter("id", id).uniqueResultOptional();
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
        ensureOrgAdmin(user);
        ensureScheduleNotExists(user, name);
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
     * @param details values which should be changed (type, calendar)
     * @param rescheduleStrategy which strategy should be executed when a rescheduling of actions is required
     * @return the updated MaintenanceSchedule
     */
    public RescheduleResult updateMaintenanceSchedule(User user, String name, Map<String, String> details,
            List<RescheduleStrategy> rescheduleStrategy) {
        ensureOrgAdmin(user);
        MaintenanceSchedule schedule = lookupMaintenanceScheduleByUserAndName(user, name)
                .orElseThrow(() -> new EntityNotExistsException(name));
        if (details.containsKey("type")) {
            schedule.setScheduleType(ScheduleType.lookupByLabel(details.get("type")));
        }
        if (details.containsKey("calendar")) {
            String label = details.get("calendar");
            MaintenanceCalendar calendar = null;
            if (!StringUtils.isBlank(label)) {
                calendar = lookupCalendarByUserAndLabel(user, label)
                        .orElseThrow(() -> new EntityNotExistsException(label));
            }
            schedule.setCalendar(calendar);
        }
        save(schedule);
        return manageAffectedScheduledActions(user, schedule, rescheduleStrategy);
    }

    /**
     * List Maintenance Calendar Labels belonging to the given User
     * @param user the user
     * @return a list of Calendar labels
     */
    @SuppressWarnings("unchecked")
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
    @SuppressWarnings("unchecked")
    public List<MaintenanceCalendar> listCalendarsByUser(User user) {
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
    @SuppressWarnings("unchecked")
    public Optional<MaintenanceCalendar> lookupCalendarByUserAndLabel(User user, String label) {
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
    @SuppressWarnings("unchecked")
    public Optional<MaintenanceCalendar> lookupCalendarByUserAndId(User user, Long id) {
        return getSession().createQuery("FROM MaintenanceCalendar WHERE org = :org AND id = :id")
                .setParameter("org", user.getOrg())
                .setParameter("id", id).uniqueResultOptional();
    }

    /**
     * Create a MaintenanceCalendar with ICal Data
     * @param user the creator
     * @param label the label for the calendar
     * @param ical the Calendar data in ICal format
     * @return the created Maintenance Calendar
     */
    public MaintenanceCalendar createMaintenanceCalendar(User user, String label, String ical) {
        ensureOrgAdmin(user);
        ensureCalendarNotExists(user, label);
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
     * @throws DownloadException when fetching data from url failed
     */
    public MaintenanceCalendar createMaintenanceCalendarWithUrl(User user, String label, String url)
            throws DownloadException {
        ensureOrgAdmin(user);
        ensureCalendarNotExists(user, label);
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
     * @param details the details which should be updated (ical, url)
     * @param rescheduleStrategy which strategy should be executed when a rescheduling of actions is required
     * @return true when the update was successfull, otherwise false
     * @throws DownloadException when fetching data from url failed
     */
    public List<RescheduleResult> updateCalendar(User user, String label, Map<String, String> details,
            List<RescheduleStrategy> rescheduleStrategy) throws DownloadException {
        ensureOrgAdmin(user);
        MaintenanceCalendar calendar = lookupCalendarByUserAndLabel(user, label)
                .orElseThrow(() -> new EntityNotExistsException(label));
        if (details.containsKey("ical")) {
            calendar.setIcal(details.get("ical"));
        }
        else if (details.containsKey("url")) {
            calendar.setUrl(details.get("url"));
            calendar.setIcal(fetchCalendarData(details.get("url")));
        }
        save(calendar);
        List<RescheduleResult> result = new LinkedList<>();
        for (MaintenanceSchedule schedule: listSchedulesByUserAndCalendar(user, calendar)) {
            RescheduleResult r = manageAffectedScheduledActions(user, schedule, rescheduleStrategy);
            if (!r.isSuccess()) {
                // in case of false, update failed and we had a DB rollback
                return Collections.singletonList(r);
            }
            result.add(r);
        }
        return result;
    }

    /**
     * Refresh the calendar data using the configured URL
     * @param user the user
     * @param label the calendar label
     * @param rescheduleStrategy which strategy should be executed when a rescheduling of actions is required
     * @return true when refresh was successful, otherwise false
     * @throws EntityNotExistsException when calendar or url does not exist
     * @throws DownloadException when fetching data from url failed
     */
    public List<RescheduleResult> refreshCalendar(User user, String label,
            List<RescheduleStrategy> rescheduleStrategy) throws EntityNotExistsException, DownloadException {
        ensureOrgAdmin(user);
        MaintenanceCalendar calendar = lookupCalendarByUserAndLabel(user, label)
                .orElseThrow(() -> new EntityNotExistsException(label));
        calendar.setIcal(fetchCalendarData(
                calendar.getUrlOpt().orElseThrow(() -> new EntityNotExistsException("url"))));
        save(calendar);
        List<RescheduleResult> result = new LinkedList<>();
        for (MaintenanceSchedule schedule: listSchedulesByUserAndCalendar(user, calendar)) {
            RescheduleResult r = manageAffectedScheduledActions(user, schedule, rescheduleStrategy);
            if (!r.isSuccess()) {
                // in case of false, update failed and we had a DB rollback
                return Collections.singletonList(r);
            }
            result.add(r);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<MaintenanceSchedule> listSchedulesByUserAndCalendar(User user, MaintenanceCalendar calendar) {
        return getSession()
                .createQuery("from MaintenanceSchedule WHERE org = :org and calendar = :calendar ORDER BY name ASC")
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

    protected RescheduleResult manageAffectedScheduledActions(User user, MaintenanceSchedule schedule,
            List<RescheduleStrategy> scheduleStrategy) {
        List<Long> systemIdsUsingSchedule = listSystemIdsWithSchedule(user, schedule);
        if (systemIdsUsingSchedule.isEmpty()) {
            return new RescheduleResult(schedule.getName(), true);
        }
        Set<Long> withMaintenanceActions = ServerFactory.filterSystemsWithPendingMaintOnlyActions(
                new HashSet<Long>(systemIdsUsingSchedule));
        if (withMaintenanceActions.isEmpty()) {
            return new RescheduleResult(schedule.getName(), true);
        }
        List<Server> servers = ServerFactory.lookupByIdsAndOrg(withMaintenanceActions, user.getOrg());

        Optional<Calendar> calendarOpt = schedule.getCalendarOpt().flatMap(c -> parseCalendar(c));

        List<ActionStatus> pending = new LinkedList<>();
        pending.add(ActionFactory.STATUS_PICKED_UP);
        pending.add(ActionFactory.STATUS_QUEUED);

        Map<Action, List<Server>> actionsForServerToReschedule = servers.stream()
            .flatMap(s -> ActionFactory.listServerActionsForServer(s, pending).stream())
            .filter(sa -> {
                Action a = sa.getParentAction();
                if (a.getPrerequisite() != null) {
                    // skip actions not first in a chain
                    return false;
                }
                if (a.getActionType().isMaintenancemodeOnly()) {
                    // test them, when they require maintenance mode
                    return true;
                }
                if (ActionFactory.lookupDependentActions(a)
                        .anyMatch(da -> da.getActionType().isMaintenancemodeOnly())) {
                    // check actions where a depended action in the chain requires
                    // maintenance mode
                    return true;
                }
                return false;
            })
            .filter(Opt.fold(calendarOpt,
                    () -> (sa -> true),
                    c -> (sa -> {
                        return !isActionInMaintenanceWindow(sa.getParentAction(), schedule, calendarOpt);
                    })))
            .collect(Collectors.groupingBy(ServerAction::getParentAction,
                    Collectors.mapping(ServerAction::getServer, toList())));

        try {
            for (RescheduleStrategy s : scheduleStrategy) {
                RescheduleResult result = s.reschedule(user, actionsForServerToReschedule, schedule);
                if (result.isSuccess()) {
                    return result;
                }
            }
            log.info("Rescheduling failed: no strategy succeeded");
        }
        catch (RescheduleException e) {
            log.info("Rescheduling failed: " + e.getMessage());
        }
        HibernateFactory.rollbackTransaction();
        HibernateFactory.closeSession();
        return new RescheduleResult(schedule.getName(), false);
    }

    private Optional<Calendar> parseCalendar(MaintenanceCalendar calendarIn) {
        return parseCalendar(new StringReader(calendarIn.getIcal()));
    }

    /**
     * Read calendar using given reader and parse it
     *
     * Public for testing.
     *
     * @param calendarReader the reader
     * @return the parsed calendar or empty, if there was a problem parsing the calendar
     */
    public Optional<Calendar> parseCalendar(Reader calendarReader) {
        CalendarBuilder builder = new CalendarBuilder();
        Calendar calendar = null;
        try {
            calendar = builder.build(calendarReader);
        }
        catch (IOException | ParserException e) {
            log.error("Unable to build the calendar from reader: " + calendarReader, e);
        }
        return ofNullable(calendar);
    }

    /**
     * Check if provided action is inside of a maintenance window
     *
     * @param action the action to check
     * @param schedule the schedule where the action belong to
     * @param calendarOpt the parsed calendar (for performance reasons)
     * @return true when the action is inside of a maintenance window, otherwise false
     */
    public boolean isActionInMaintenanceWindow(Action action, MaintenanceSchedule schedule,
            Optional<Calendar> calendarOpt) {
        Collection<CalendarComponent> events = getScheduleEventsAtDate(action.getEarliestAction(), schedule,
                calendarOpt);

        if (!events.isEmpty()) {
            if (log.isDebugEnabled()) {
                events.stream().forEach(cc -> log.debug(
                        String.format("Action '%s' inside of maintenance window in '%s': '%s'",
                                action, schedule.getName(), cc)));
            }
            return true;
        }
        log.debug(String.format("Action '%s' outside of maintenance window '%s'", action, schedule.getName()));
        return false;
    }

    private Collection<CalendarComponent> getScheduleEventsAtDate(
            Date date, MaintenanceSchedule schedule, Optional<Calendar> calendarOpt) {
        if (calendarOpt.isEmpty()) {
            return emptySet();
        }

        Period p = new Period(new DateTime(date), java.time.Duration.ofSeconds(1));
        ArrayList<Predicate<Component>> rules = new ArrayList<>();
        rules.add(new PeriodRule<>(p));

        if (schedule.getScheduleType().equals(ScheduleType.MULTI)) {
            Summary summary = new Summary(schedule.getName());
            HasPropertyRule<Component> propertyRule = new HasPropertyRule<>(summary);
            rules.add(propertyRule);
        }
        @SuppressWarnings("unchecked")
        Predicate<CalendarComponent>[] comArr = new Predicate[rules.size()];
        comArr = rules.toArray(comArr);

        Filter<CalendarComponent> filter = new Filter<>(comArr, Filter.MATCH_ALL);

        return filter.filter(calendarOpt.get().getComponents(Component.VEVENT));
    }

    /**
     * Assign {@link MaintenanceSchedule} to given set of {@link Server}s.
     *
     * @param user the user
     * @param schedule the {@link MaintenanceSchedule}
     * @param systemIds the set of {@link Server} IDs
     * @throws PermissionException if the user does not have access to given servers
     * @throws IllegalArgumentException if systems have pending maintenance-only actions
     * @return the number of involved {@link Server}s
     */
    public int assignScheduleToSystems(User user, MaintenanceSchedule schedule, Set<Long> systemIds) {
        ensureOrgAdmin(user);
        ensureSystemsAccessible(user, systemIds);
        ensureScheduleAccessible(user, schedule);

        Set<Long> withMaintenanceActions = ServerFactory.filterSystemsWithPendingMaintOnlyActions(systemIds);
        if (!withMaintenanceActions.isEmpty()) {
            throw new IllegalArgumentException("Systems have pending maintenance-only actions:" +
                    withMaintenanceActions);
        }

        return ServerFactory.setMaintenanceScheduleToSystems(schedule, systemIds);
    }

    /**
     * Retract {@link MaintenanceSchedule} from given set of {@link Server}s.
     *
     * @param user the user
     * @param systemIds the set of {@link Server} IDs
     * @throws PermissionException if the user does not have access to given servers
     * @return the number of involved {@link Server}s
     */
    public int retractScheduleFromSystems(User user, Set<Long> systemIds) {
        ensureOrgAdmin(user);
        ensureSystemsAccessible(user, systemIds);

        return ServerFactory.setMaintenanceScheduleToSystems(null, systemIds);
    }

    /**
     * List {@link Server} IDs with given schedule
     *
     * @param user the user
     * @param schedule the schedule
     * @return the {@link Server} IDS with given schedule
     */
    public List<Long> listSystemIdsWithSchedule(User user, MaintenanceSchedule schedule) {
        ensureOrgAdmin(user);
        ensureScheduleAccessible(user, schedule);

        @SuppressWarnings("unchecked")
        List<Long> systemIds = getSession().createQuery(
                "SELECT s.id from Server s " +
                        "WHERE s.maintenanceSchedule = :schedule")
                .setParameter("schedule", schedule)
                .list();

        ensureSystemsAccessible(user, systemIds);

        return systemIds;
    }

    /**
     * Ensures that given user has access to given systems
     *
     * @param user the user
     * @param systemIds the {@link Server} IDs
     * @throws PermissionException if the user does not have access
     */
    private void ensureSystemsAccessible(User user, Collection<Long> systemIds) {
        if (!SystemManager.areSystemsAvailableToUser(user.getId(), new ArrayList<>(systemIds))) {
            throw new PermissionException(String.format("User '%s' can't access systems.", user));
        }
    }

    /**
     * Ensures that given user has access to given {@link MaintenanceSchedule}
     *
     * @param user the user
     * @param schedule the {@link MaintenanceSchedule}
     * @throws PermissionException if the user does not have access
     */
    private void ensureScheduleAccessible(User user, MaintenanceSchedule schedule) {
        if (!user.getOrg().equals(schedule.getOrg())) {
            throw new PermissionException(String.format("User '%s' can't access schedule '%s'.", user, schedule));
        }
    }

    /**
     * Ensures that given user has access to given {@link MaintenanceCalendar}
     *
     * @param user the user
     * @param calendar the {@link MaintenanceCalendar}
     * @throws PermissionException if the user does not have access
     */
    private void ensureCalendarAccessible(User user, MaintenanceCalendar calendar) {
        if (!user.getOrg().equals(calendar.getOrg())) {
            throw new PermissionException(String.format("User '%s' can't access calendar '%s'.", user, calendar));
        }
    }

    /**
     * Ensures that given user has the Org admin role
     *
     * @param user the user
     * @throws PermissionException if the user does not have Org admin role
     */
    private static void ensureOrgAdmin(User user) {
        if (!user.hasRole(ORG_ADMIN)) {
            throw new PermissionException(ORG_ADMIN);
        }
    }

    /**
     * Ensures that the Maintenance Schedule does not exists yet
     *
     * @param user the user
     * @param name the schedule name
     * @throws EntityExistsException if a schedule with this name already exists
     */
    private void ensureScheduleNotExists(User user, String name) {
        if (listScheduleNamesByUser(user).contains(name)) {
            throw new EntityExistsException(String.format("Maintenance Schedule '%s' already exists", name));
        }
    }

    /**
     * Ensures that the Maintenance Calendar does not exists yet
     *
     * @param user the user
     * @param label the calendar label
     * @throws EntityExistsException if a calendar with this label already exists
     */
    private void ensureCalendarNotExists(User user, String label) {
        if (listCalendarLabelsByUser(user).contains(label)) {
            throw new EntityExistsException(String.format("Maintenance Calendar '%s' already exists", label));
        }
    }

    /**
     * Map reschedule strategy strings to objects
     * @param rescheduleStrategy list of strategy strings
     * @return list of strategy objects
     */
    public List<RescheduleStrategy> mapRescheduleStrategyStrings(List<String> rescheduleStrategy) {
        List<RescheduleStrategy> ret = new LinkedList<>();

        CancelRescheduleStrategy cancel = new CancelRescheduleStrategy();
        FailRescheduleStrategy fail = new FailRescheduleStrategy();
        for (String st : rescheduleStrategy) {
            if (st.equals(cancel.getType())) {
                ret.add(cancel);
            }
            else if (st.equals(fail.getType())) {
                ret.add(fail);
            }
            else {
                throw new EntityNotExistsException(String.format("Reschedule Strategy '%s' does not exist.", st));
            }
        }
        return ret;
    }
}
