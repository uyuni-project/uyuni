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
package com.suse.manager.xmlrpc.maintenance;

import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.EntityNotExistsFaultException;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.manager.EntityNotExistsException;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;

import com.suse.manager.maintenance.MaintenanceManager;
import com.suse.manager.model.maintenance.MaintenanceCalendar;
import com.suse.manager.model.maintenance.MaintenanceSchedule;
import com.suse.manager.model.maintenance.MaintenanceSchedule.ScheduleType;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Maintenance Schedule XMLRPC Handler
 *
 * @xmlrpc.namespace maintenance
 * @xmlrpc.doc Provides methods to access and modify Maintenance Schedules related entities
 */
public class MaintenanceHandler extends BaseHandler {

    private final MaintenanceManager mm = MaintenanceManager.instance();

    /**
     * List Schedule Names visible to user
     *
     * @param loggedInUser the user
     * @return list of schedule names
     *
     * @xmlrpc.doc List Schedule Names visible to user
     * @xmlrpc.param #session_key()
     * @xmlrpc.returntype #array_single("string", "maintenance schedule names")
     */
    public List<String> listScheduleNames(User loggedInUser) {
        return mm.listScheduleNamesByUser(loggedInUser);
    }

    /**
     * Lookup a specific Maintenance Schedule
     *
     * @param loggedInUser the user
     * @param name schedule name
     * @throws EntityNotExistsFaultException when Maintenance Schedule does not exist
     * @return the Maintenance Schedule
     *
     * @xmlrpc.doc Lookup a specific Maintenance Schedule
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "name", "Maintenance Schedule Name")
     * @xmlrpc.returntype
     * #array_begin()
     * $MaintenanceScheduleSerializer
     * #array_end()
     */
    public MaintenanceSchedule getScheduleDetails(User loggedInUser, String name) {
        return mm.lookupMaintenanceScheduleByUserAndName(loggedInUser, name)
                .orElseThrow(() -> new EntityNotExistsFaultException(name));
    }

    /**
     * Create a new Maintenance Schedule
     *
     * @param loggedInUser the user
     * @param name schedule name
     * @param type schedule type
     * @return the new Maintenance Schedule
     *
     * @xmlrpc.doc Create a new Maintenance Schedule
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "name", "Maintenance Schedule Name")
     * @xmlrpc.param #param_desc("string", "type", "Schedule type: single, multi")
     * @xmlrpc.returntype
     * #array_begin()
     * $MaintenanceScheduleSerializer
     * #array_end()
     */
    public MaintenanceSchedule createSchedule(User loggedInUser, String name, String type) {
        ensureOrgAdmin(loggedInUser);
        return mm.createMaintenanceSchedule(loggedInUser, name, ScheduleType.lookupByLabel(type),
                Optional.empty());
    }

    /**
     * Update a Maintenance Schedule
     *
     * @param loggedInUser the user
     * @param name schedule name
     * @param details values to update
     * @return the changed Maintenance Schedule
     *
     * @xmlrpc.doc Update a Maintenance Schedule
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "name", "Maintenance Schedule Name")
     * @xmlrpc.param
     *     #struct_begin("Maintenance Schedule Details")
     *         #prop_desc("string", "name", "new Schedule Name")
     *         #prop_desc("string", "type", "new Schedule Type")
     *           #options()
     *               #item("single")
     *               #item("multi")
     *           #options_end()
     *         #prop_desc("string", "calendar", "new calendar label")
     *     #struct_end()
     * @xmlrpc.returntype
     * #array_begin()
     * $MaintenanceScheduleSerializer
     * #array_end()
     */
    public MaintenanceSchedule updateSchedule(User loggedInUser, String name, Map<String, String> details) {
        ensureOrgAdmin(loggedInUser);

        // confirm that the user only provided valid keys in the map
        Set<String> validKeys = new HashSet<String>();
        validKeys.add("name");
        validKeys.add("type");
        validKeys.add("calendar");
        validateMap(validKeys, details);

        try {
            return mm.updateMaintenanceSchedule(loggedInUser, name, details);
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
    }

    /**
     * Delete a Maintenance Schedule
     *
     * @param loggedInUser the user
     * @param name schedule name
     * @throws EntityNotExistsFaultException when Maintenance Schedule does not exist
     * @return number of removed objects
     *
     * @xmlrpc.doc Remove a Maintenance Schedule
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "name", "Maintenance Schedule Name")
     * @xmlrpc.returntype #return_int_success()
     */
    public int deleteSchedule(User loggedInUser, String name) {
        ensureOrgAdmin(loggedInUser);
        Optional<MaintenanceSchedule> schedule = mm.lookupMaintenanceScheduleByUserAndName(loggedInUser, name);
        mm.remove(loggedInUser, schedule.orElseThrow(() -> new EntityNotExistsFaultException(name)));
        return 1;
    }


    /**
     * List Calendar Labels visible to user
     *
     * @param loggedInUser the user
     * @return list of calendar labels
     *
     * @xmlrpc.doc List Schedule Names visible to user
     * @xmlrpc.param #session_key()
     * @xmlrpc.returntype #array_single("string", "maintenance calendar labels")
     */
    public List<String> listCalendarLabels(User loggedInUser) {
        return mm.listCalendarLabelsByUser(loggedInUser);
    }

    /**
     * Lookup a specific Maintenance Calendar
     *
     * @param loggedInUser the user
     * @param label calendar label
     * @throws EntityNotExistsFaultException when Maintenance Calendar does not exist
     * @return the Maintenance Calendar
     *
     * @xmlrpc.doc Lookup a specific Maintenance Schedule
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "label", "Maintenance Calendar Label")
     * @xmlrpc.returntype
     * #array_begin()
     * $MaintenanceCalendarSerializer
     * #array_end()
     */
    public MaintenanceCalendar getCalendarDetails(User loggedInUser, String label) {
        return mm.lookupCalendarByUserAndLabel(loggedInUser, label)
                .orElseThrow(() -> new EntityNotExistsFaultException(label));
    }

    /**
     * Create a new Maintenance Calendar
     *
     * @param loggedInUser the user
     * @param label calendar label
     * @param ical calendar ical data
     * @return the new Maintenance Calendar
     *
     * @xmlrpc.doc Create a new Maintenance Calendar
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "label", "Maintenance Calendar Label")
     * @xmlrpc.param #param_desc("string", "ical", "ICal Calendar Data")
     * @xmlrpc.returntype
     * #array_begin()
     * $MaintenanceCalendarSerializer
     * #array_end()
     */
    public MaintenanceCalendar createCalendar(User loggedInUser, String label, String ical) {
        ensureOrgAdmin(loggedInUser);
        return mm.createMaintenanceCalendar(loggedInUser, label, ical);
    }

    /**
     * Create a new Maintenance Calendar
     *
     * @param loggedInUser the user
     * @param label calendar label
     * @param url calendar url
     * @return the new Maintenance Calendar
     *
     * @xmlrpc.doc Create a new Maintenance Calendar
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "label", "Maintenance Calendar Label")
     * @xmlrpc.param #param_desc("string", "url", "download URL for ICal Calendar Data")
     * @xmlrpc.returntype
     * #array_begin()
     * $MaintenanceCalendarSerializer
     * #array_end()
     */
    public MaintenanceCalendar createCalendarWithUrl(User loggedInUser, String label, String url) {
        ensureOrgAdmin(loggedInUser);
        return mm.createMaintenanceCalendarWithUrl(loggedInUser, label, url);
    }

    /**
     * Update a Maintenance Calendar
     *
     * @param loggedInUser the user
     * @param label calendar label
     * @param details values which should be updated
     * @return the changed Maintenance Calendar
     *
     * @xmlrpc.doc Update a Maintenance Calendar
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "label", "Maintenance Calendar Label")
     * @xmlrpc.param
     *     #struct_begin("Maintenance Calendar Details")
     *         #prop_desc("string", "label", "new Calendar Label")
     *         #prop_desc("string", "ical", "new ical Calendar data")
     *     #struct_end()
     * @xmlrpc.returntype
     * #array_begin()
     * $MaintenanceCalendarSerializer
     * #array_end()
     */
    public MaintenanceCalendar updateCalendar(User loggedInUser, String label, Map<String, String> details) {
        ensureOrgAdmin(loggedInUser);

        // confirm that the user only provided valid keys in the map
        Set<String> validKeys = new HashSet<String>();
        validKeys.add("label");
        validKeys.add("ical");
        validateMap(validKeys, details);

        try {
            return mm.updateCalendar(loggedInUser, label, details);
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
    }

    /**
     * Refresh the calendar data using the configured URL
     * @param loggedInUser user
     * @param label the calendar label
     * @return 1 on success
     *
     * @xmlrpc.doc Refresh Maintenance Calendar Data using the configured URL
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "label", "Maintenance Calendar Label")
     * @xmlrpc.returntype #return_int_success()
     */
    public int refreshCalendar(User loggedInUser, String label) {
        ensureOrgAdmin(loggedInUser);
        try {
            mm.refreshCalendar(loggedInUser, label);
            return 1;
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
    }

    /**
     * Delete a Maintenance Calendar
     *
     * @param loggedInUser the user
     * @param label calendar label
     * @throws EntityNotExistsFaultException when Maintenance Calendar does not exist
     * @return number of removed objects
     *
     * @xmlrpc.doc Remove a Maintenance Calendar
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "label", "Maintenance Calendar Label")
     * @xmlrpc.returntype #return_int_success()
     */
    public int deleteCalendar(User loggedInUser, String label) {
        ensureOrgAdmin(loggedInUser);
        Optional<MaintenanceCalendar> calendar = mm.lookupCalendarByUserAndLabel(loggedInUser, label);
        mm.remove(loggedInUser, calendar.orElseThrow(() -> new EntityNotExistsFaultException(label)));
        return 1;
    }

    /**
     * Assign schedule with given name to systems with given IDs
     *
     * @param loggedInUser the user
     * @param scheduleName the schedule name
     * @param systemIds the system IDs
     * @return the number of involved systems
     *
     * @xmlrpc.doc Assign schedule with given name to systems with given IDs.
     * Throws a PermissionCheckFailureException when some of the systems are not accessible by the user.
     * Throws a InvalidParameterException when some of the systems have pending actions that are not allowed in the
     * maintenance mode.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "scheduleName", "The schedule name")
     * @xmlrpc.param #array_single("int", "system IDs")
     * @xmlrpc.returntype #array_single("int", "number of involved systems")
     */
    public Integer assignScheduleToSystems(User loggedInUser, String scheduleName, List<Integer> systemIds) {
        ensureOrgAdmin(loggedInUser);
        MaintenanceSchedule schedule = mm
                .lookupMaintenanceScheduleByUserAndName(loggedInUser, scheduleName)
                .orElseThrow(() -> new EntityNotExistsFaultException(scheduleName));

        Set<Long> longIds = systemIds.stream().map(id -> id.longValue()).collect(Collectors.toSet());
        try {
            return mm.assignScheduleToSystems(loggedInUser, schedule, longIds);
        }
        catch (PermissionException e) {
            throw new PermissionCheckFailureException(e);
        }
        catch (IllegalArgumentException e) {
            throw new InvalidParameterException(e.getMessage());
        }
    }

    /**
     * Retract schedule with given name from systems with given IDs
     *
     * @param loggedInUser the user
     * @param systemIds the system IDs
     * @return the number of involved systems
     *
     * @xmlrpc.doc Retract schedule with given name from systems with given IDs
     * Throws a PermissionCheckFailureException when some of the systems are not accessible by the user.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #array_single("int", "system IDs")
     * @xmlrpc.returntype #array_single("int", "number of involved systems")
     */
    public Integer retractScheduleFromSystems(User loggedInUser, List<Integer> systemIds) {
        ensureOrgAdmin(loggedInUser);

        Set<Long> longIds = systemIds.stream().map(id -> id.longValue()).collect(Collectors.toSet());
        try {
            return mm.retractScheduleFromSystems(loggedInUser, longIds);
        }
        catch (PermissionException e) {
            throw new PermissionCheckFailureException(e);
        }
    }

    /**
     * List IDs of systems that have given schedule assigned
     *
     * @param loggedInUser the user
     * @param scheduleName the schedule name
     * @return the IDs of systems that have given schedule assigned
     *
     * @xmlrpc.doc List IDs of systems that have given schedule assigned
     * Throws a PermissionCheckFailureException when some of the systems are not accessible by the user.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "scheduleName", "The schedule name")
     * @xmlrpc.returntype #array_single("int", "system IDs")
    */
    public List<Long> listSystemsWithSchedule(User loggedInUser, String scheduleName) {
        ensureOrgAdmin(loggedInUser);
        MaintenanceSchedule schedule = mm
                .lookupMaintenanceScheduleByUserAndName(loggedInUser, scheduleName)
                .orElseThrow(() -> new EntityNotExistsFaultException(scheduleName));

        try {
            return mm.listSystemIdsWithSchedule(loggedInUser, schedule);
        }
        catch (PermissionException e) {
            throw new PermissionCheckFailureException(e);
        }
    }
}
