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

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.common.util.download.DownloadException;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionStatus;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.manager.EntityNotExistsException;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.model.maintenance.MaintenanceSchedule;
import com.suse.manager.model.maintenance.MaintenanceSchedule.ScheduleType;
import com.suse.manager.utils.HttpHelper;
import com.suse.utils.Opt;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.log4j.Logger;

import com.suse.manager.model.maintenance.MaintenanceCalendar;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.filter.Filter;
import net.fortuna.ical4j.filter.HasPropertyRule;
import net.fortuna.ical4j.filter.PeriodRule;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
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
     * @param user the user
     * @param calendar the calendar
     */
    public void remove(User user, MaintenanceCalendar calendar) {
        ensureOrgAdmin(user);
        ensureCalendarAccessible(user, calendar);
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
     * @param details values which should be changed (name, type, calendar)
     * @param rescheduleStrategy which strategy should be executed when a rescheduling of actions is required
     * @return the updated MaintenanceSchedule
     */
    public RescheduleResult updateMaintenanceSchedule(User user, String name, Map<String, String> details,
            List<RescheduleStrategy> rescheduleStrategy) {
        ensureOrgAdmin(user);
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
    @SuppressWarnings("unchecked")
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
     * @param details the details which should be updated (label, ical, url)
     * @param rescheduleStrategy which strategy should be executed when a rescheduling of actions is required
     * @return true when the update was successfull, otherwise false
     */
    public List<RescheduleResult> updateCalendar(User user, String label, Map<String, String> details,
            List<RescheduleStrategy> rescheduleStrategy) {
        ensureOrgAdmin(user);
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
        List<RescheduleResult> result = new LinkedList<>();
        for (MaintenanceSchedule schedule: listSchedulesByUserAndCalendar(user, calendar)) {
            RescheduleResult r = manageAffectedScheduledActions(user, schedule, rescheduleStrategy);
            if (!r.isSuccess()) {
                // in case of false, update failed and we had a DB rollback
                return new LinkedList<>();
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
     */
    public List<RescheduleResult> refreshCalendar(User user, String label,
            List<RescheduleStrategy> rescheduleStrategy) {
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
                return new LinkedList<>();
            }
            result.add(r);
        }
        return result;
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

        Optional<Calendar> calendarOpt = Opt.fold(schedule.getCalendarOpt(),
                () -> Optional.empty(),
                c -> {
                    StringReader sin = new StringReader(c.getIcal());
                    CalendarBuilder builder = new CalendarBuilder();
                    Calendar calendar = null;
                    try {
                        calendar = builder.build(sin);
                    }
                    catch (IOException | ParserException e) {
                        log.error("Unable to build the calendar: " + c.getLabel(), e);
                    }
                    return Optional.ofNullable(calendar);
                });

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
                    Collectors.mapping(ServerAction::getServer, Collectors.toList())));

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
        if (!calendarOpt.isPresent()) {
            return false;
        }

        Period p = new Period(new DateTime(action.getEarliestAction()), java.time.Duration.ofSeconds(1));
        ArrayList<Predicate<Component>> rules = new ArrayList<Predicate<Component>>();
        rules.add(new PeriodRule<Component>(p));

        if (schedule.getScheduleType().equals(ScheduleType.MULTI)) {
            Summary summary = new Summary(schedule.getName());
            HasPropertyRule<Component> propertyRule = new HasPropertyRule<Component>(summary);
            rules.add(propertyRule);
        }
        @SuppressWarnings("unchecked")
        Predicate<CalendarComponent>[] comArr = new Predicate[rules.size()];
        comArr = rules.toArray(comArr);

        Filter<CalendarComponent> filter = new Filter<CalendarComponent>(comArr, Filter.MATCH_ALL);

        Collection<CalendarComponent> events = filter.filter(calendarOpt.get().getComponents(Component.VEVENT));
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
     * @param name the calendar label
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
