/*
 * Copyright (c) 2021 SUSE LLC
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

import static com.redhat.rhn.domain.role.RoleFactory.ORG_ADMIN;
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
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.manager.EntityNotExistsException;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.maintenance.rescheduling.CancelRescheduleStrategy;
import com.suse.manager.maintenance.rescheduling.FailRescheduleStrategy;
import com.suse.manager.maintenance.rescheduling.RescheduleException;
import com.suse.manager.maintenance.rescheduling.RescheduleResult;
import com.suse.manager.maintenance.rescheduling.RescheduleStrategy;
import com.suse.manager.model.maintenance.CalendarAssignment;
import com.suse.manager.model.maintenance.CalendarFactory;
import com.suse.manager.model.maintenance.MaintenanceCalendar;
import com.suse.manager.model.maintenance.MaintenanceSchedule;
import com.suse.manager.model.maintenance.MaintenanceSchedule.ScheduleType;
import com.suse.manager.model.maintenance.ScheduleFactory;
import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.utils.HttpHelper;
import com.suse.utils.Opt;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
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
import java.util.stream.Collectors;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.CalendarComponent;

/**
 * MaintenanceManager
 */
public class MaintenanceManager {

    private static Logger log = LogManager.getLogger(MaintenanceManager.class);

    private ScheduleFactory scheduleFactory;
    private CalendarFactory calendarFactory;
    private IcalUtils icalUtils;

    /**
     * Constructor.
     */
    public MaintenanceManager() {
        scheduleFactory = new ScheduleFactory();
        calendarFactory = new CalendarFactory();
        icalUtils = new IcalUtils();
    }

    /**
     * List {@link MaintenanceSchedule}s assigned to given systems.
     *
     * @param systemIds the IDs of systems
     * @return the {@link MaintenanceSchedule}s assigned to given systems
     */
    public Set<MaintenanceSchedule> listSchedulesBySystems(Set<Long> systemIds) {
        return scheduleFactory.listBySystems(systemIds);
    }

    /**
     * List Maintenance Schedule Names belong to the given User
     * @param user the user
     * @return a list of Schedule names
     */
    public List<String> listScheduleNamesByUser(User user) {
        return scheduleFactory.listScheduleNamesByUser(user);
    }

    /**
     * List Maintenance Schedules belonging to the given User
     * @param user the user
     * @return a list of Maintenance Schedules
     */
    public List<MaintenanceSchedule> listSchedulesByUser(User user) {
        return scheduleFactory.listByUser(user);
    }

    /**
     * List Schedule names that use given calendar
     * @param user the user
     * @param calendar the calendar
     * @return a list of MaintenanceSchedules
     */
    public List<MaintenanceSchedule> listSchedulesByCalendar(User user, MaintenanceCalendar calendar) {
        return scheduleFactory.listByCalendar(user, calendar);
    }

    /**
     * Returns tuples representing calendar id, calendar label, and name of schedule assigned to the calendar.
     *
     * @see CalendarFactory.listCalendarToSchedulesAssignments
     *
     * @param user the user
     * @return the tuples representing the assignments of calendar to schedules
     */
    public List<CalendarAssignment> listCalendarToSchedulesAssignments(User user) {
        return calendarFactory.listCalendarToSchedulesAssignments(user);
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

        List<Long> systemIds = scheduleFactory.listSystemIdsWithSchedule(schedule);
        ensureSystemsAccessible(user, systemIds);

        return systemIds;
    }

    /**
     * Lookup a MaintenanceSchedule by user and name
     * @param user the user
     * @param name the schedule name
     * @return Optional Maintenance Schedule
     */
    public Optional<MaintenanceSchedule> lookupScheduleByUserAndName(User user, String name) {
        return scheduleFactory.lookupByUserAndName(user, name);
    }

    /**
     * Lookup MaintenanceSchedule by User and id
     * @param user the user
     * @param id the id of the schedule
     * @return Optional Maintenance Schedule
     */
    public Optional<MaintenanceSchedule> lookupScheduleByUserAndId(User user, Long id) {
        return scheduleFactory.lookupByUserAndId(user, id);
    }

    /**
     * Create a Maintenance Schedule
     * @param user the creator
     * @param name the schedule name
     * @param type the schedule type
     * @param calendar and optional Maintenance Calendar
     * @return the created Maintenance Schedule
     */
    public MaintenanceSchedule createSchedule(User user, String name, ScheduleType type,
            Optional<MaintenanceCalendar> calendar) {
        ensureOrgAdmin(user);
        ensureScheduleNotExists(user, name);
        MaintenanceSchedule ms = new MaintenanceSchedule();
        ms.setOrg(user.getOrg());
        ms.setName(name);
        ms.setScheduleType(type);
        calendar.ifPresent(ms::setCalendar);
        scheduleFactory.save(ms);
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
    public RescheduleResult updateSchedule(User user, String name, Map<String, String> details,
            List<RescheduleStrategy> rescheduleStrategy) {
        ensureOrgAdmin(user);
        MaintenanceSchedule schedule = lookupScheduleByUserAndName(user, name)
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
        scheduleFactory.save(schedule);
        return manageAffectedScheduledActions(user, schedule, rescheduleStrategy);
    }

    /**
     * Remove a MaintenanceSchedule
     * @param user the user
     * @param schedule the schedule
     */
    public void remove(User user, MaintenanceSchedule schedule) {
        ensureOrgAdmin(user);
        ensureScheduleAccessible(user, schedule);
        scheduleFactory.remove(schedule);
    }

    /**
     * List Maintenance Calendar Labels belonging to the given User
     * @param user the user
     * @return a list of Calendar labels
     */
    public List<String> listCalendarLabelsByUser(User user) {
        return calendarFactory.listCalendarLabelsByUser(user);
    }

    /**
     * List Maintenance Calendars belonging to the given User
     * @param user the user
     * @return a list of Maintenance Calendars
     */
    public List<MaintenanceCalendar> listCalendarsByUser(User user) {
        return calendarFactory.listByUser(user);
    }

    /**
     * Lookup Maintenance Calendar by User and Label
     * @param user the user
     * @param label the label of the calendar
     * @return Optional Maintenance Calendar
     */
    public Optional<MaintenanceCalendar> lookupCalendarByUserAndLabel(User user, String label) {
        return calendarFactory.lookupByUserAndLabel(user, label);
    }

    /**
     * Lookup Maintenance Calendar by User and id
     * @param user the user
     * @param id the id of the calendar
     * @return Optional Maintenance Calendar
     */
    public Optional<MaintenanceCalendar> lookupCalendarByUserAndId(User user, Long id) {
        return calendarFactory.lookupByUserAndId(user, id);
    }

    /**
     * Create a MaintenanceCalendar with ICal Data
     * @param user the creator
     * @param label the label for the calendar
     * @param ical the Calendar data in ICal format
     * @return the created Maintenance Calendar
     */
    public MaintenanceCalendar createCalendar(User user, String label, String ical) {
        ensureOrgAdmin(user);
        ensureCalendarNotExists(user, label);
        MaintenanceCalendar mc = new MaintenanceCalendar();
        mc.setOrg(user.getOrg());
        mc.setLabel(label);
        mc.setIcal(ical);
        calendarFactory.save(mc);
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
    public MaintenanceCalendar createCalendarWithUrl(User user, String label, String url) throws DownloadException {
        ensureOrgAdmin(user);
        ensureCalendarNotExists(user, label);
        MaintenanceCalendar mc = new MaintenanceCalendar();
        mc.setOrg(user.getOrg());
        mc.setLabel(label);
        mc.setUrl(url);
        mc.setIcal(fetchCalendarData(url));
        calendarFactory.save(mc);
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
            calendar.setIcal(fetchCalendarData(details.get("url")));
            calendar.setUrl(details.get("url"));
        }
        calendarFactory.save(calendar);
        List<RescheduleResult> result = new LinkedList<>();
        for (MaintenanceSchedule schedule: scheduleFactory.listByUserAndCalendar(user, calendar)) {
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
        calendarFactory.save(calendar);
        List<RescheduleResult> result = new LinkedList<>();
        for (MaintenanceSchedule schedule: scheduleFactory.listByUserAndCalendar(user, calendar)) {
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
        List<MaintenanceSchedule> schedules = scheduleFactory.listByUserAndCalendar(user, calendar);
        calendarFactory.remove(calendar);
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
     * Assign {@link MaintenanceSchedule} to given set of {@link Server}s.
     *
     * @param user the user
     * @param schedule the {@link MaintenanceSchedule}
     * @param systemIds the set of {@link Server} IDs
     * @param cancelAffectedActions whether the affected maintenance-only actions outside the window shall be cancelled
     * @throws PermissionException if the user does not have access to given servers
     * @throws IllegalArgumentException if systems have pending maintenance-only actions
     * @return the number of involved {@link Server}s
     */
    public int assignScheduleToSystems(User user, MaintenanceSchedule schedule, Set<Long> systemIds,
            boolean cancelAffectedActions) {
        return assignScheduleToSystems(user, schedule, systemIds,
                cancelAffectedActions ? Collections.singletonList(new CancelRescheduleStrategy()) :
                        Collections.emptyList());
    }

    /**
     * Assign {@link MaintenanceSchedule} to given set of {@link Server}s.
     *
     * @param user the user
     * @param schedule the {@link MaintenanceSchedule}
     * @param systemIds the set of {@link Server} IDs
     * @param strategies a chain of strategies to be used for rescheduling actions
     * @throws PermissionException if the user does not have access to given servers
     * @throws IllegalArgumentException if systems have pending maintenance-only actions
     * @return the number of involved {@link Server}s
     */
    public int assignScheduleToSystems(User user, MaintenanceSchedule schedule, Set<Long> systemIds,
            List<RescheduleStrategy> strategies) {
        ensureOrgAdmin(user);
        ensureSystemsAccessible(user, systemIds);
        ensureScheduleAccessible(user, schedule);

        RescheduleResult result = manageAffectedScheduledActionsForSystems(user, systemIds, schedule, strategies);

        if (!result.isSuccess()) {
            throw new IllegalArgumentException("Some systems have pending maintenance-only actions");
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
     * Given the systems, return upcoming maintenance windows.
     *
     * The windows are returned as a list of triples consisting of:
     * - window start date as a human-readable string
     * - window end date as a human-readable string
     * - start date as number of milliseconds since the epoch
     *
     * The formatting is done by {@link LocalizationService}.
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
    public Optional<List<MaintenanceWindowData>> calculateUpcomingMaintenanceWindows(Set<Long> systemIds)
            throws IllegalStateException {
        Set<MaintenanceSchedule> schedules = listSchedulesBySystems(systemIds);
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
        return icalUtils.calculateUpcomingMaintenanceWindows(schedule);
    }

    /**
     * Given a Maintenance Calendar return a list of maintenance windows based on the operation
     * to perform starting from the given date.
     *
     * @param user the current user
     * @param operation get previous, current or future maintenance windows based on the operation
     * @param id the id of the calendar or schedule
     * @param date the date to start looking for maintenance windows
     * @param startWithSunday whether to start the week on Sunday
     * @return the resulting list of maintenance windows
     */
    public List<MaintenanceWindowData> preprocessCalendarData(User user, String operation, Long id, Long date,
                                                              boolean startWithSunday) {
        Optional<MaintenanceCalendar> calendar = lookupCalendarByUserAndId(user, id);
        if (calendar.isEmpty()) {
            throw new EntityNotExistsException("Calendar with id: " + id + " does not exist!");
        }
        return getCalendarEvents(operation, calendar.get(), Optional.empty(), date, startWithSunday);
    }

    /**
     * Given a Maintenance Schedule return a list of maintenance windows based on the operation
     * to perform starting from the given date.
     *
     * @param user the current user
     * @param operation get previous, current or future maintenance windows based on the operation
     * @param id the id of the calendar or schedule
     * @param date the date to start looking for maintenance windows
     * @param startWithSunday whether to start the week on Sunday
     * @return the resulting list of maintenance windows
     */
    public List<MaintenanceWindowData> preprocessScheduleData(User user, String operation, Long id, Long date,
                                                              boolean startWithSunday) {
        Optional<MaintenanceSchedule> schedule = lookupScheduleByUserAndId(user, id);
        if (schedule.isEmpty()) {
            throw new EntityNotExistsException("Schedule with id: " + id + " does not exist!");
        }
        Optional<MaintenanceCalendar> calendar = schedule.get().getCalendarOpt();
        if (calendar.isEmpty()) {
            throw new EntityNotExistsException("Calendar with id: " + id + " does not exist!");
        }
        if (schedule.get().getScheduleType() == ScheduleType.MULTI) {
            return getCalendarEvents(operation, calendar.get(), ofNullable(schedule.get().getName()),
                    date, startWithSunday);
        }
        else {
            return getCalendarEvents(operation, calendar.get(), Optional.empty(), date, startWithSunday);
        }
    }

    /**
     * Given a maintenance calendar return a list of maintenance windows based on the operation, the date and
     * the start of the week. Only returns events for a specific event if the event name is provided.
     *
     * @param operation get previous, current or future maintenance windows based on the operation
     * @param calendar the maintenance calendar
     * @param eventName the optional event name
     * @param date the date
     * @param startWithSunday whether to start the week on Sunday
     * @return the resulting list of maintenance windows
     */
    public List<MaintenanceWindowData> getCalendarEvents(String operation, MaintenanceCalendar calendar,
                                                         Optional<String> eventName, Long date,
                                                         boolean startWithSunday) {
        if (operation.equals("skipBack")) {
            Optional<MaintenanceWindowData> lastWindow = icalUtils.getLastEvent(calendar, eventName, date);
            if (lastWindow.isEmpty()) {
                return new ArrayList<>();
            }
            date = lastWindow.get().getToMilliseconds();
        }
        else if (operation.equals("skipNext")) {
            Optional<MaintenanceWindowData> nextWindow = icalUtils.getNextEvent(calendar, eventName, date);
            if (nextWindow.isEmpty()) {
                return new ArrayList<>();
            }
            date = nextWindow.get().getFromMilliseconds();
        }

        Map<String, Long> activeRange = getActiveRange(date, startWithSunday);
        Long start = activeRange.get("start");
        Long end = activeRange.get("end");

        return icalUtils.getCalendarEvents(calendar, eventName, start, end);
    }

    /**
     * Given a date and the start of the week. Calculates the date range displayed by the calendar widget
     *
     * @param date the date
     * @param startWithSunday whether to start the week on Sunday
     * @return the calendars displayed date range
     */
    public Map<String, Long> getActiveRange(Long date, boolean startWithSunday) {
        ZonedDateTime t = ZonedDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneOffset.UTC);
        ZonedDateTime rangeStart = t.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
        if (startWithSunday) {
            rangeStart = rangeStart.getDayOfWeek().equals(DayOfWeek.SUNDAY) ? rangeStart :
                    rangeStart.minusDays(rangeStart.getDayOfWeek().getValue());
        }
        else {
            rangeStart = rangeStart.getDayOfWeek().equals(DayOfWeek.SUNDAY) ? rangeStart.minusDays(6) :
                    rangeStart.minusDays(rangeStart.getDayOfWeek().getValue() - 1);
        }

        ZonedDateTime rangeEnd = rangeStart.plusDays(42);

        // We add one more day to the beginning and end to prevent the potential loss of events due to
        // timezone shifts.
        return Map.of(
                "start", rangeStart.toInstant().minus(Period.ofDays(1)).toEpochMilli(),
                "end", rangeEnd.toInstant().plus(Period.ofDays(1)).toEpochMilli()
        );
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
    public void canActionBeScheduled(Set<Long> systemIds, Action action) {
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
            Set<MaintenanceSchedule> offendingSchedules = listSystemSchedulesNotMatchingDate(systemIds, scheduleDate);
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
    private Set<MaintenanceSchedule> listSystemSchedulesNotMatchingDate(Set<Long> systemIds, Date date) {
        return listSchedulesBySystems(systemIds).stream()
                .filter(schedule -> {
                    Collection<CalendarComponent> events = icalUtils.getCalendarEventsAtDate(
                            date,
                            schedule.getCalendarOpt().flatMap(c -> icalUtils.parseCalendar(c)),
                            getScheduleNameForMulti(schedule)
                    );
                    return events.isEmpty();
                })
                .collect(toSet());
    }

    protected String fetchCalendarData(String url) {
        try {
            HttpHelper http = new HttpHelper();
            HttpResponse response = http.sendGetRequest(url);
            StatusLine status = response.getStatusLine();
            int statusCode = status.getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                http.cleanup(response);
                log.error("Download failed, HTTP status code: {}", statusCode);
                throw new DownloadException(url, status.getReasonPhrase(), statusCode);
            }
            String ical = http.getBodyAsString(response, StandardCharsets.UTF_8);
            http.cleanup(response);
            return ical;
        }
        catch (IOException | ParseException e) {
            log.error("Download failed.", e);
            throw new DownloadException(url, e.getMessage(), 500);
        }
    }

    private RescheduleResult manageAffectedScheduledActionsForSystems(User user, Set<Long> serverIds,
            MaintenanceSchedule schedule, List<RescheduleStrategy> scheduleStrategy) {
        Set<Long> withMaintenanceActions = ServerFactory.filterSystemsWithPendingMaintOnlyActions(serverIds);
        if (withMaintenanceActions.isEmpty()) {
            return new RescheduleResult(schedule.getName(), true);
        }
        // If no rescheduleStrategy is given default to 'Fail'
        if (scheduleStrategy.isEmpty()) {
            scheduleStrategy = Collections.singletonList(new FailRescheduleStrategy());
        }
        List<Server> servers = ServerFactory.lookupByIdsAndOrg(withMaintenanceActions, user.getOrg());

        Optional<Calendar> calendarOpt = schedule.getCalendarOpt().flatMap(c -> icalUtils.parseCalendar(c));

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
                    c -> (sa -> !isActionInMaintenanceWindow(sa.getParentAction(), schedule, calendarOpt))))
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
            log.info("Rescheduling failed: {}", e.getMessage());
        }
        HibernateFactory.rollbackTransaction();
        HibernateFactory.closeSession();
        return new RescheduleResult(schedule.getName(), false);
    }

    protected RescheduleResult manageAffectedScheduledActions(User user, MaintenanceSchedule schedule,
            List<RescheduleStrategy> scheduleStrategy) {
        List<Long> systemIdsUsingSchedule = listSystemIdsWithSchedule(user, schedule);
        if (systemIdsUsingSchedule.isEmpty()) {
            return new RescheduleResult(schedule.getName(), true);
        }
        return manageAffectedScheduledActionsForSystems(user, new HashSet<>(systemIdsUsingSchedule), schedule,
                scheduleStrategy);
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
        Collection<CalendarComponent> events = icalUtils.getCalendarEventsAtDate(
                action.getEarliestAction(),
                calendarOpt,
                getScheduleNameForMulti(schedule)
        );

        if (!events.isEmpty()) {
            if (log.isDebugEnabled()) {
                events.stream().forEach(cc -> log.debug(
                        String.format("Action '%s' inside of maintenance window in '%s': '%s'",
                                action, schedule.getName(), cc)));
            }
            return true;
        }
        log.debug("Action '{}' outside of maintenance window '{}'", action, schedule.getName());
        return false;
    }

    private Collection<CalendarComponent> getCalendarForNow(MaintenanceSchedule ms) {
        return ms.getCalendarOpt()
                .map(cal -> icalUtils.getCalendarEventsAtDate(
                        new Date(), icalUtils.parseCalendar(cal),
                        getScheduleNameForMulti(ms)))
                .orElse(Collections.emptyList());
    }

    /**
     * Check if system is in maintenance mode
     *
     * @param server the server to check
     * @return true when the action is inside of a maintenance window, otherwise falsegg
     */
    public boolean isSystemInMaintenanceMode(Server server) {
        return server.getMaintenanceScheduleOpt()
                .map(schedule -> !getCalendarForNow(schedule).isEmpty())
                .orElse(true);
    }

    /**
     * Given a list of minions, sorts by maintenance mode status, logs skipped minions
     *
     * @param minions servers to check
     * @return List of minions in maintenance mode
     */
    public List<Long> systemIdsMaintenanceMode(List<MinionServer> minions) {
        Set<MaintenanceSchedule> schedulesInMaintMode = minions.stream()
                .flatMap(minion -> minion.getMaintenanceScheduleOpt().stream())
                .distinct()
                .filter(sched -> !getCalendarForNow(sched).isEmpty())
                .collect(Collectors.toSet());

        List<Long> minionsInMaintMode = minions.stream()
                .filter(minion -> minion.getMaintenanceScheduleOpt()
                .map(schedulesInMaintMode::contains) // keep minions that have maintenance mode
                .orElse(true)) // or that have no maintenance schedule whatsoever
                .map(Server::getId)
                .collect(toList());

         List<MinionServer> logList = minions.stream()
                 .filter(m -> !minionsInMaintMode.contains(m.getId()))
                 .collect(Collectors.toList());
         logSkippedMinions(logList);

         return minionsInMaintMode;
    }

    /**
     * Log the number of servers skipped and if debugging is enabled list the server ids
     *
     * @param servers the list of servers to log
     */
    private static void logSkippedMinions(List<MinionServer> servers) {
        log.warn("Skipping action for {} minions.", servers.size());
        if (log.isDebugEnabled()) {
            String serverNames = servers.stream()
                    .map(m -> m.getId().toString())
                    .collect(Collectors.joining(","));
            log.debug("Skipped minion ids: {}", serverNames);
        }
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
     * Convenience method: return schedule name if the schedule type is MULTI, return empty otherwise
     * @param schedule the schedule
     * @return optional of schedule name
     */
    private static Optional<String> getScheduleNameForMulti(MaintenanceSchedule schedule) {
        if (schedule.getScheduleType() == MaintenanceSchedule.ScheduleType.MULTI) {
            return of(schedule.getName());
        }
        return empty();
    }
}
