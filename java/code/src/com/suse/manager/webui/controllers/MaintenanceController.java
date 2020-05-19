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
import com.suse.manager.webui.utils.gson.MaintenanceScheduleJson;
import com.suse.manager.webui.utils.gson.ResultJson;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

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

    private static final Logger LOG = Logger.getLogger(MaintenanceController.class);
    private static final Gson GSON = new GsonBuilder().create();
    private static final MaintenanceManager mm = MaintenanceManager.instance();

    private MaintenanceController() { }

    /**
     * Invoked from Router. Initialize routes for Systems Views.
     *
     * @param jade the template engine
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        get("/manager/schedule/maintenance-schedules",
                withUserPreferences(withCsrfToken(withUser(MaintenanceController::maintenanceSchedules))),
                jade);
        get("/manager/api/maintenance", withUser(MaintenanceController::list));
        get("/manager/api/maintenance/:name", withUser(MaintenanceController::getScheduleDetails));
        get("/manager/api/maintenance/calendar", withUser(MaintenanceController::getCalendarNames));
        post("/manager/api/maintenance/save", withUser(MaintenanceController::save));
        Spark.delete("/manager/api/maintenance/:name/delete", withUser(MaintenanceController::delete));
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
        return new ModelAndView(new HashMap<>(), "templates/schedule/maintenance-schedules.jade");
    }

    public static String list(Request request, Response response, User user) {
        /* TODO: Implement function to retrieve all maintenance schedules the user has access to */
        List<MaintenanceSchedule> schedules = mm.listScheduleNamesByUser(user).stream().map(
                name -> mm.lookupMaintenanceScheduleByUserAndName(user, name).get()).collect(Collectors.toList());
        return json(response, schedulesToJson(schedules));
    }

    public static String getScheduleDetails(Request request, Response response, User user) {
        String scheduleName = request.params("name");
        MaintenanceSchedule schedule = mm.lookupMaintenanceScheduleByUserAndName(user, scheduleName).orElseThrow(
                () -> new EntityNotExistsException("")
        );
        return json(response, scheduleToJson(schedule));
    }

    public static String getCalendarNames(Request request, Response response, User user) {
        response.type("application/json");
        /* TODO: Return sorted query? */
        List<String> calendarNames = mm.listCalendarLabelsByUser(user);
        calendarNames.sort(String.CASE_INSENSITIVE_ORDER);
        return json(response, calendarNames);
    }

    public static String save(Request request, Response response, User user) {
        response.type("application/json");
        MaintenanceScheduleJson json = GSON.fromJson(request.body(), MaintenanceScheduleJson.class);

        if (json.isCalendarAdded()) {
            if (json.getCalendarName() == null) {
                /* TODO: Add localization */
                Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error("Calendar name required")));
            }
            else if (json.getCalendarType().equals("new") && json.getCalendarData() == null &&
                    json.getCalendarUrl() == null) {
                /* TODO: Add localization */
                Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error("Calendar data required")));
            }
        }

        createOrUpdateSchedule(user, json);
        return json(response, ResultJson.success());
    }

    private static void createOrUpdateSchedule(User user, MaintenanceScheduleJson json){
        try {
            Optional<MaintenanceCalendar> calendar = Optional.empty();
            if (json.isCalendarAdded()) {
                calendar = Optional.of(createOrGetCalendar(user, json));
            }

            if (json.getScheduleId() == null) {
                mm.createMaintenanceSchedule(user, json.getScheduleName(),
                        MaintenanceSchedule.ScheduleType.lookupByLabel(json.getScheduleType()), calendar);
            } else {
                /* TODO: Get strategy from webUI */
                List<String> rescheduleStrategy = List.of("Cancel");
                Map<String, String> details = new HashMap<>();
                details.put("type", json.getScheduleType());
                details.put("name", json.getScheduleName());
                details.put("calendar", json.getCalendarName());

                mm.updateMaintenanceSchedule(user, json.getScheduleName(), details,
                        mm.mapRescheduleStrategyStrings(rescheduleStrategy));
            }
        }
        catch (EntityNotExistsException | EntityExistsException e) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(e.getMessage())));
        }
    }

    private static MaintenanceCalendar createOrGetCalendar(User user, MaintenanceScheduleJson json) {
        /* Create new MaintenanceCalendar */
        if (json.getCalendarType().equals("new")) {
            if (json.getCalendarUrl() == null) {
                return mm.createMaintenanceCalendar(user, json.getCalendarName(), json.getCalendarData());
            }
            else {
                MaintenanceCalendar calendar = mm.createMaintenanceCalendarWithUrl(user, json.getCalendarName(), json.getCalendarUrl());
                /* TODO: Get strategy from webUI*/
                mm.refreshCalendar(user, calendar.getLabel(), mm.mapRescheduleStrategyStrings(List.of("Cancel")));
                return calendar;
            }
        }
        /* Lookup existing MaintenanceCalendar */
        else if (json.getCalendarType().equals("existing")) {
            return mm.lookupCalendarByUserAndLabel(user, json.getCalendarName()).orElseThrow(
                    () -> new EntityNotExistsException(json.getCalendarName()));
        }
        /* Invalid input provided */
        else {
            /* TODO: Localize */
            Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error("Error fetching Calendar")));
            return null;
        }
    }

    public static String delete(Request request, Response response, User user) {
        String name = request.params("name");
        Optional<MaintenanceSchedule> schedule = mm.lookupMaintenanceScheduleByUserAndName(user, name);
        if (schedule.isPresent()) {
            mm.remove(user, schedule.get());
        }
        else {
            Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }
        return  json(response, ResultJson.success());
    }

    private static List<MaintenanceScheduleJson> schedulesToJson(List<MaintenanceSchedule> schedules) {
        return schedules.stream().map(MaintenanceController::scheduleToJson).collect(Collectors.toList());
    }

    private static MaintenanceScheduleJson scheduleToJson(MaintenanceSchedule schedule) {
        MaintenanceScheduleJson json = new MaintenanceScheduleJson();

        json.setScheduleId(schedule.getId());
        json.setScheduleName(schedule.getName());
        json.setScheduleType(schedule.getScheduleType().toString());

        schedule.getCalendarOpt().ifPresent(maintenanceCalendar -> {
            json.setCalendarId(maintenanceCalendar.getId());
            json.setCalendarName(maintenanceCalendar.getLabel());
            json.setCalendarData(maintenanceCalendar.getIcal());
            maintenanceCalendar.getUrlOpt().ifPresent(json::setCalendarUrl);
        });

        return json;
    }
}