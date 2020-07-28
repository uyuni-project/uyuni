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

package com.suse.manager.webui.controllers.maintenance;

import static com.suse.manager.maintenance.rescheduling.RescheduleStrategyType.CANCEL;
import static com.suse.manager.webui.controllers.maintenance.MaintenanceController.handleRescheduleResult;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.util.download.DownloadException;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.EntityNotExistsException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suse.manager.maintenance.MaintenanceManager;
import com.suse.manager.maintenance.rescheduling.RescheduleResult;
import com.suse.manager.maintenance.rescheduling.RescheduleStrategy;
import com.suse.manager.maintenance.rescheduling.RescheduleStrategyType;
import com.suse.manager.model.maintenance.MaintenanceCalendar;
import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.webui.utils.gson.MaintenanceCalendarJson;
import com.suse.manager.webui.utils.gson.ResultJson;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Tuple;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.jade.JadeTemplateEngine;

/**
 * Controller class providing the backend for API calls to work with maintenance calendars.
 */
public class MaintenanceCalendarController {

    private static final MaintenanceManager MM = new MaintenanceManager();
    private static final LocalizationService LOCAL = LocalizationService.getInstance();
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .serializeNulls()
            .create();

    private MaintenanceCalendarController() { }

    /**
     * Initialize routes for MaintenanceCalendar Api.
     *
     * @param jade the template engine
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        get("/manager/schedule/maintenance/calendars",
                withUserPreferences(withCsrfToken(withUser(MaintenanceCalendarController::maintenanceCalendars))),
                jade);
        get("/manager/api/maintenance/calendar/list",
                withUser(MaintenanceCalendarController::listCalendars));
        get("/manager/api/maintenance/calendar/:id/details",
                withUser(MaintenanceCalendarController::getCalendarDetails));
        get("/manager/api/maintenance/calendar/names",
                withUser(MaintenanceCalendarController::getCalendarNames));
        post("/manager/api/maintenance/calendar/save",
                withUser(MaintenanceCalendarController::saveCalendar));
        post("/manager/api/maintenance/calendar/refresh",
                withUser(MaintenanceCalendarController::refreshCalendar));
        delete("/manager/api/maintenance/calendar/delete",
                withUser(MaintenanceCalendarController::deleteCalendar));
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
        Map<String, Object> params = new HashMap<>();
        params.put("type", "calendar");
        params.put("isAdmin", user.hasRole(RoleFactory.ORG_ADMIN));
        return new ModelAndView(params, "templates/schedule/maintenance-windows.jade");
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
        Map<Pair<Long, String>, List<Tuple>> assignmentsByCalendar = MM
                .listCalendarToSchedulesAssigments(user)
                .stream()
                .collect(Collectors.groupingBy(
                        tuple -> Pair.of(tuple.get(0, Long.class), tuple.get(1, String.class)),
                        Collectors.mapping(
                                tuple -> tuple,
                                Collectors.toList())));

        List<MaintenanceCalendarJson> calendarsWithSchedules = assignmentsByCalendar.entrySet().stream()
                .map(entry -> {
                    Long calId = entry.getKey().getKey();
                    String calName = entry.getKey().getValue();
                    List<Map<String, String>> schedules = entry.getValue().stream()
                            .filter(tuple -> tuple.get(2) != null) // schedule id != null
                            .map(tuple -> Map.of(
                                    "id", tuple.get(2, Long.class).toString(),
                                    "name", tuple.get(3, String.class)))
                            .collect(Collectors.toList());
                    return new MaintenanceCalendarJson(calId, calName, schedules);
                })
                .collect(Collectors.toList());

        return json(response, calendarsWithSchedules);
    }

    /**
     * Processes a GET request to get the details of a calendar identified by its id
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static String getCalendarDetails(Request request, Response response, User user) {
        Long calendarId = Long.parseLong(request.params("id"));
        MaintenanceCalendarJson json = new MaintenanceCalendarJson();

        MaintenanceCalendar calendar = MM.lookupCalendarByUserAndId(user, calendarId).orElseThrow(
                () -> Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(LOCAL.getMessage(
                        "maintenance.calendar.id.not.exists", calendarId
                ))))
        );

        json.setCalendarId(calendar.getId());
        json.setCalendarName(calendar.getLabel());
        json.setCalendarData(calendar.getIcal());
        calendar.getUrlOpt().ifPresent(json::setCalendarUrl);
        json.setScheduleNames(MM.listSchedulesByCalendar(user, calendar).stream().map(
                schedule -> Map.of(
                        "id", schedule.getId().toString(),
                        "name", schedule.getName()
                )
        ).collect(Collectors.toList()));

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
        return json(response, MM.listCalendarLabelsByUser(user));
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
        MaintenanceCalendarJson json = GSON.fromJson(request.body(), MaintenanceCalendarJson.class);

        if (json.getCalendarName().isBlank()) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(LOCAL.getMessage(
                    "maintenance.calendar.name.empty"
            ))));
        }
        else if (json.getCalendarUrl().isBlank() && json.getCalendarData() == null) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(LOCAL.getMessage(
                    "maintenance.calendar.data.empty"
            ))));
        }

        MM.lookupCalendarByUserAndLabel(user, json.getCalendarName()).ifPresentOrElse(
                /* update existing calendar */
                calendar -> {
                    if (json.getCalendarId() == null) {
                        Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(LOCAL.getMessage(
                                "maintenance.calendar.exists", json.getCalendarName()
                        ))));
                    }
                    Map<String, String> details = new HashMap<>();
                    details.put("label", calendar.getLabel());
                    if (!json.getCalendarUrl().isBlank()) {
                        details.put("url", json.getCalendarUrl());
                    }
                    else {
                        details.put("ical", json.getCalendarData());
                    }

                    try {
                        RescheduleStrategy rescheduleStrategy = RescheduleStrategyType
                                .fromLabel(json.getRescheduleStrategy())
                                .createInstance();
                        List<RescheduleResult> results = MM.updateCalendar(user, calendar.getLabel(), details,
                                List.of(rescheduleStrategy)
                        );
                        handleRescheduleResult(results, rescheduleStrategy.getType());
                    }
                    catch (DownloadException e) {
                        Spark.halt(HttpStatus.SC_INTERNAL_SERVER_ERROR, GSON.toJson(ResultJson.error(LOCAL.getMessage(
                                "maintenance.calendar.fetch.error", json.getCalendarUrl()
                        ))));
                    }
                },
                /* Create new calendar */
                () -> {
                    if (json.getCalendarData() == null) {
                        try {
                            MM.createCalendarWithUrl(user, json.getCalendarName(), json.getCalendarUrl());
                        }
                        catch (DownloadException e) {
                            Spark.halt(HttpStatus.SC_INTERNAL_SERVER_ERROR, GSON.toJson(ResultJson.error(
                                    LOCAL.getMessage(
                                            "maintenance.calendar.fetch.error",
                                            json.getCalendarUrl()
                                    )
                            )));
                        }
                    }
                    else {
                        MM.createCalendar(user, json.getCalendarName(), json.getCalendarData());
                    }
                }
        );
        return json(response, ResultJson.success());
    }

    /**
     * Refresh calendar data from url
     *
     * @param request the request object
     * @param response the response obejct
     * @param user the authorized user
     * @return string containing the JSON response
     */
    public static String refreshCalendar(Request request, Response response, User user) {
        response.type("application/json");
        MaintenanceCalendarJson json = GSON.fromJson(request.body(), MaintenanceCalendarJson.class);

        try {
            RescheduleStrategy rescheduleStrategy = RescheduleStrategyType
                    .fromLabel(json.getRescheduleStrategy())
                    .createInstance();
            List<RescheduleResult> results = MM.refreshCalendar(
                    user,
                    json.getCalendarName(),
                    List.of(rescheduleStrategy)
            );
            handleRescheduleResult(results, rescheduleStrategy.getType());
        }
        catch (EntityNotExistsException e) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(LOCAL.getMessage(
                    "maintenance.calendar.not.exists", json.getCalendarName()
            ))));
        }
        catch (DownloadException e) {
            Spark.halt(HttpStatus.SC_INTERNAL_SERVER_ERROR, GSON.toJson(ResultJson.error(LOCAL.getMessage(
                    "maintenance.calendar.fetch.error", json.getCalendarUrl()
            ))));
        }

        return json(response, ResultJson.success());
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
        response.type("application/json");
        MaintenanceCalendarJson json = GSON.fromJson(request.body(), MaintenanceCalendarJson.class);

        String name = json.getCalendarName();
        MM.lookupCalendarByUserAndLabel(user, name).ifPresentOrElse(
                calendar -> {
                    RescheduleStrategyType type = RescheduleStrategyType.fromLabel(json.getRescheduleStrategy());
                    handleRescheduleResult(MM.remove(user, calendar, type == CANCEL), type);
                },
                () -> Spark.halt(HttpStatus.SC_BAD_REQUEST)
        );

        return json(response, ResultJson.success());
    }
}
