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

package com.suse.manager.webui.controllers;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.manager.EntityNotExistsException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suse.manager.maintenance.MaintenanceManager;
import com.suse.manager.model.maintenance.MaintenanceCalendar;
import com.suse.manager.model.maintenance.MaintenanceSchedule;
import com.suse.manager.webui.utils.gson.MaintenanceWindowJson;
import com.suse.manager.webui.utils.gson.ResultJson;
import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.jade.JadeTemplateEngine;

/**
 * Controller class providing the backend for API calls to work with maintenance windows.
 */
public class MaintenanceController {

    private static final Gson GSON = new GsonBuilder().create();
    private static final MaintenanceManager MM = MaintenanceManager.instance();

    private MaintenanceController() { }

    /**
     * Invoked from Router. Initialize routes for Systems Views.
     *
     * @param jade the template engine
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        get("/manager/schedule/maintenance/schedules",
                withUserPreferences(withCsrfToken(withUser(MaintenanceController::maintenanceSchedules))),
                jade);
        get("/manager/schedule/maintenance/calendars",
                withUserPreferences(withCsrfToken(withUser(MaintenanceController::maintenanceCalendars))),
                jade);
        get("/manager/api/maintenance/schedule/list", withUser(MaintenanceController::listSchedules));
        get("/manager/api/maintenance/calendar/list", withUser(MaintenanceController::listCalendars));
        get("/manager/api/maintenance/schedule/:name/details", withUser(MaintenanceController::getScheduleDetails));
        get("/manager/api/maintenance/calendar/:name/details", withUser(MaintenanceController::getCalendarDetails));
        get("/manager/api/maintenance/calendar", withUser(MaintenanceController::getCalendarNames));
        post("/manager/api/maintenance/schedule/save", withUser(MaintenanceController::saveSchedule));
        post("/manager/api/maintenance/calendar/save", withUser(MaintenanceController::saveCalendar));
        delete("/manager/api/maintenance/schedule/:name/delete", withUser(MaintenanceController::deleteSchedule));
        delete("/manager/api/maintenance/calendar/:name/delete", withUser(MaintenanceController::deleteCalendar));
    }

    /**
     * Handler for the Maintenance Schedules page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView maintenanceSchedules(Request request, Response response, User user) {
        Map<String, String> params = new HashMap<>();
        params.put("type", "schedule");
        return new ModelAndView(params, "templates/schedule/maintenance-windows.jade");
    }

    /**
     * Handler for the Maintenance Calendars page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView maintenanceCalendars(Request request, Response response, User user) {
        Map<String, String> params = new HashMap<>();
        params.put("type", "calendar");
        return new ModelAndView(params, "templates/schedule/maintenance-windows.jade");
    }

    /**
     * Processes a GET request to get a list of all Maintenance Schedules visible to given user
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static String listSchedules(Request request, Response response, User user) {
        /* TODO: Implement function to retrieve all maintenance schedules the user has access to */
        List<MaintenanceSchedule> schedules = MM.listScheduleNamesByUser(user).stream().map(
                name -> MM.lookupMaintenanceScheduleByUserAndName(user, name).get()).collect(Collectors.toList());
        return json(response, schedulesToJson(schedules));
    }

    /**
     * Processes a GET request to get a list of all Maintenance Calendars visible to given user
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static String listCalendars(Request request, Response response, User user) {
        /* TODO: Implement function to retrieve all maintenance calendars the user has access to */
        List<MaintenanceCalendar> calendars = MM.listCalendarLabelsByUser(user).stream().map(
                label -> MM.lookupCalendarByUserAndLabel(user, label).get()).collect(Collectors.toList());
        return json(response, calendarsToJson(user, calendars));
    }

    /**
     * Processes a GET request to get the details of a schedule identified by its name
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static String getScheduleDetails(Request request, Response response, User user) {
        String scheduleName = request.params("name");
        MaintenanceWindowJson json = new MaintenanceWindowJson();

        Optional<MaintenanceSchedule> schedule = MM.lookupMaintenanceScheduleByUserAndName(user, scheduleName);
        if (schedule.isEmpty()) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(
                    "Schedule " + scheduleName + " does not exist")));
        }
        json.setScheduleId(schedule.get().getId());
        json.setScheduleName(schedule.get().getName());
        json.setScheduleType(schedule.get().getScheduleType().toString());

        schedule.get().getCalendarOpt().ifPresent(maintenanceCalendar -> {
            json.setCalendarId(maintenanceCalendar.getId());
            json.setCalendarName(maintenanceCalendar.getLabel());
            json.setCalendarData(maintenanceCalendar.getIcal());
            maintenanceCalendar.getUrlOpt().ifPresent(json::setCalendarUrl);
        });
        return json(response, json);
    }

    /**
     * Processes a GET request to get the details of a calendar identified by its name
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static String getCalendarDetails(Request request, Response response, User user) {
        String calendarName = request.params("name");
        MaintenanceWindowJson json = new MaintenanceWindowJson();

        Optional<MaintenanceCalendar> calendar = MM.lookupCalendarByUserAndLabel(user, calendarName);
        if (calendar.isEmpty()) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(
                    "Calendar " + calendarName + " does not exist")));
        }
        json.setCalendarId(calendar.get().getId());
        json.setCalendarName(calendar.get().getLabel());
        json.setCalendarData(calendar.get().getIcal());
        calendar.get().getUrlOpt().ifPresent(json::setCalendarUrl);
        json.setScheduleNames(MM.listScheduleNamesByCalendar(user, calendar.get()));

        return json(response, json);
    }

    /**
     * Processes a GET request to get the names of all calendars visible to given user
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static String getCalendarNames(Request request, Response response, User user) {
        response.type("application/json");
        /* TODO: Return sorted query? */
        List<String> calendarNames = new ArrayList<>();
        calendarNames.add("<None>");
        calendarNames.addAll(MM.listCalendarLabelsByUser(user));

        calendarNames.sort(String.CASE_INSENSITIVE_ORDER);
        return json(response, calendarNames);
    }

    /**
     * Create or update a schedule
     *
     * @param request the request object
     * @param response the response obejct
     * @param user the authorized user
     * @return string containing the JSON response
     */
    public static String saveSchedule(Request request, Response response, User user) {
        response.type("application/json");
        MaintenanceWindowJson json = GSON.fromJson(request.body(), MaintenanceWindowJson.class);

        if (json.getCalendarName().isBlank()) {
            /* TODO: Add localization */
            Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error("Calendar name required")));
        }
        else if (json.getScheduleName().isBlank()) {
            /* TODO: Add localization */
            Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error("Schedule name required")));
        }
        createOrUpdateSchedule(user, json);
        return json(response, ResultJson.success());
    }

    /**
     * Create or update a calendar
     *
     * @param request the request object
     * @param response the response obejct
     * @param user the authorized user
     * @return string containing the JSON response
     */
    public static String saveCalendar(Request request, Response response, User user) {
        response.type("application/json");
        MaintenanceWindowJson json = GSON.fromJson(request.body(), MaintenanceWindowJson.class);

        if (json.getCalendarName().isBlank()) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error("Calendar name required")));
        }
        else if (json.getCalendarName().equals("<None>")) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(
                    "Invalid calendar name provided. Choose a different name"
            )));
        }
        else if (json.getCalendarUrl() == null && json.getCalendarData() == null) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error("Calenar data required")));
        }

        MM.lookupCalendarByUserAndLabel(user, json.getCalendarName()).ifPresentOrElse(
                /* update existing calendar */
                calendar -> {
                    /* TODO: Handle Data provided via url*/
                    if (json.getCalendarUrl() == null) {
                        Map<String, String> details = new HashMap<>();
                        details.put("label", calendar.getLabel());
                        details.put("ical", json.getCalendarData());
                        MM.updateCalendar(user, calendar.getLabel(), details,
                                MM.mapRescheduleStrategyStrings(List.of("Cancel"))
                        );
                    }
                },
                /* Create new calendar */
                () -> {
                    if (json.getCalendarUrl() == null) {
                        MM.createMaintenanceCalendar(user, json.getCalendarName(), json.getCalendarData());
                    }
                    else {
                        MM.createMaintenanceCalendarWithUrl(user, json.getCalendarName(), json.getCalendarUrl());
                    }
                }
        );
        return json(response, ResultJson.success());
    }

    private static void createOrUpdateSchedule(User user, MaintenanceWindowJson json) {
        try {
            if (json.getScheduleId() == null) {
                Optional<MaintenanceCalendar> calendar = Optional.empty();
                if (!json.getCalendarName().equals("<None>")) {
                    /* Lookup calendar */
                    /* TODO: Localize */
                    calendar = Optional.of(MM.lookupCalendarByUserAndLabel(user, json.getCalendarName())
                            .orElseThrow(()  -> new EntityNotExistsException(
                                    "Calendar " + json.getCalendarName() + " does not exist."
                            ))
                    );
                }
                /* Create new schedule */
                MM.createMaintenanceSchedule(
                        user,
                        json.getScheduleName(),
                        MaintenanceSchedule.ScheduleType.lookupByLabel(json.getScheduleType().toLowerCase()),
                        calendar
                );
            }
            else {
                /* Update existing schedule */
                /* TODO: Get strategy from webUI */
                List<String> rescheduleStrategy = List.of("Cancel");
                Map<String, String> details = new HashMap<>();
                details.put("type", json.getScheduleType().toLowerCase());
                details.put("name", json.getScheduleName());
                String label = json.getCalendarName();
                details.put("calendar", label.equals("<None>") ? "" : label);
                MM.updateMaintenanceSchedule(user, json.getScheduleName(), details,
                        MM.mapRescheduleStrategyStrings(rescheduleStrategy));
            }
        }
        catch (EntityNotExistsException | EntityExistsException e) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(e.getMessage())));
        }
    }

    /**
     * Deletes a given schedule
     *
      * @param request the request object
     * @param response the response object
     * @param user the authorzed user
     * @return the result JSON object
     */
    public static String deleteSchedule(Request request, Response response, User user) {
        String name = request.params("name");
        Optional<MaintenanceSchedule> schedule = MM.lookupMaintenanceScheduleByUserAndName(user, name);
        if (schedule.isPresent()) {
            MM.remove(user, schedule.get());
        }
        else {
            Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }
        return  json(response, ResultJson.success());
    }

    /**
     * Deletes a given calendar
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorzed user
     * @return the result JSON object
     */
    public static String deleteCalendar(Request request, Response response, User user) {
        String name = request.params("name");
        Optional<MaintenanceCalendar> calendar = MM.lookupCalendarByUserAndLabel(user, name);
        if (calendar.isPresent()) {
            /* TODO: Get strategy */
            MM.remove(user, calendar.get(), false);
        }
        else {
            Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }
        return  json(response, ResultJson.success());
    }

    private static List<MaintenanceWindowJson> schedulesToJson(List<MaintenanceSchedule> schedules) {
        return schedules.stream().map(MaintenanceController::scheduleToJson).collect(Collectors.toList());
    }

    private static List<MaintenanceWindowJson> calendarsToJson(User user, List<MaintenanceCalendar> calendars) {
        return calendars.stream().map(calendar -> calendarToJson(user, calendar)).collect(Collectors.toList());
    }

    private static MaintenanceWindowJson scheduleToJson(MaintenanceSchedule schedule) {
        MaintenanceWindowJson json = new MaintenanceWindowJson();

        json.setScheduleName(schedule.getName());
        schedule.getCalendarOpt().ifPresent(maintenanceCalendar -> {
            json.setCalendarName(maintenanceCalendar.getLabel());
        });

        return json;
    }

    private static MaintenanceWindowJson calendarToJson(User user, MaintenanceCalendar calendar) {
        MaintenanceWindowJson json = new MaintenanceWindowJson();

        json.setCalendarName(calendar.getLabel());
        json.setScheduleNames(MM.listScheduleNamesByCalendar(user, calendar));

        return json;
    }
}
