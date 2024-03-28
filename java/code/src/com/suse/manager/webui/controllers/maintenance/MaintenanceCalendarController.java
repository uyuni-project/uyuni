/*
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
import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.result;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.util.download.DownloadException;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.EntityNotExistsException;

import com.suse.manager.maintenance.IcalUtils;
import com.suse.manager.maintenance.MaintenanceManager;
import com.suse.manager.maintenance.rescheduling.RescheduleResult;
import com.suse.manager.maintenance.rescheduling.RescheduleStrategy;
import com.suse.manager.maintenance.rescheduling.RescheduleStrategyType;
import com.suse.manager.model.maintenance.CalendarAssignment;
import com.suse.manager.model.maintenance.MaintenanceCalendar;
import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.webui.utils.gson.MaintenanceCalendarJson;
import com.suse.manager.webui.utils.gson.ResultJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private static Logger log = LogManager.getLogger(MaintenanceCalendarController.class);

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
        get("/manager/api/maintenance/calendar/list", withUser(MaintenanceCalendarController::list));
        get("/manager/api/maintenance/calendar/:id/details", withUser(MaintenanceCalendarController::details));
        get("/manager/api/maintenance/calendar/names", asJson(withUser(MaintenanceCalendarController::getNames)));
        post("/manager/api/maintenance/calendar/save", asJson(withUser(MaintenanceCalendarController::save)));
        post("/manager/api/maintenance/calendar/refresh", asJson(withUser(MaintenanceCalendarController::refresh)));
        Spark.delete("/manager/api/maintenance/calendar/delete",
                asJson(withUser(MaintenanceCalendarController::delete)));
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
    public static String list(Request request, Response response, User user) {
        Map<Pair<Long, String>, List<CalendarAssignment>> assignmentsByCalendar = MM
                .listCalendarToSchedulesAssignments(user)
                .stream()
                .collect(Collectors.groupingBy(
                        assignment -> Pair.of(assignment.getCalendarId(), assignment.getCalendarName()),
                        Collectors.mapping(
                                tuple -> tuple,
                                Collectors.toList())));

        List<MaintenanceCalendarJson> calendarsWithSchedules = assignmentsByCalendar.entrySet().stream()
                .map(entry -> {
                    Long calId = entry.getKey().getKey();
                    String calName = entry.getKey().getValue();
                    List<Map<String, String>> schedules = entry.getValue().stream()
                            .filter(tuple -> tuple.getScheduleId() != null)
                            .map(tuple -> Map.of(
                                    "id", tuple.getScheduleId().toString(),
                                    "name", tuple.getScheduleName()))
                            .collect(Collectors.toList());
                    return new MaintenanceCalendarJson(calId, calName, schedules);
                })
                .collect(Collectors.toList());

        return json(response, calendarsWithSchedules, new TypeToken<>() { });
    }

    /**
     * Processes a GET request to get the details of a calendar identified by its id
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static String details(Request request, Response response, User user) {
        Long calendarId = Long.parseLong(request.params("id"));
        MaintenanceCalendarJson json = new MaintenanceCalendarJson();

        MaintenanceCalendar calendar = MM.lookupCalendarByUserAndId(user, calendarId).orElseThrow(
                () -> Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(LOCAL.getMessage(
                        "maintenance.calendar.id.not.exists", calendarId
                ))))
        );

        json.setId(calendar.getId());
        json.setName(calendar.getLabel());
        calendar.getUrlOpt().ifPresent(json::setUrl);
        json.setScheduleNames(MM.listSchedulesByCalendar(user, calendar).stream().map(
                schedule -> Map.of(
                        "id", schedule.getId().toString(),
                        "name", schedule.getName()
                )
        ).collect(Collectors.toList()));
        json.setEventNames(new IcalUtils().getEventNames(calendar));

        return json(response, json, new TypeToken<>() { });
    }

    /**
     * Processes a GET request to get the names of all calendars visible to given user
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static String getNames(Request request, Response response, User user) {
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
    public static String save(Request request, Response response, User user) {
        MaintenanceCalendarJson json = GSON.fromJson(request.body(), MaintenanceCalendarJson.class);

        if (json.getName().isBlank()) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(LOCAL.getMessage(
                    "maintenance.calendar.name.empty"
            ))));
        }
        else if (json.getUrl().isBlank() && json.getData() == null) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(LOCAL.getMessage(
                    "maintenance.calendar.data.empty"
            ))));
        }

        MM.lookupCalendarByUserAndLabel(user, json.getName()).ifPresentOrElse(
                /* update existing calendar */
                calendar -> {
                    if (json.getId() == null) {
                        Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(LOCAL.getMessage(
                                "maintenance.calendar.exists", json.getName()
                        ))));
                    }
                    Map<String, String> details = new HashMap<>();
                    details.put("label", calendar.getLabel());
                    if (!json.getUrl().isBlank()) {
                        details.put("url", json.getUrl());
                    }
                    else {
                        details.put("ical", json.getData());
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
                        log.info(e);
                        Spark.halt(HttpStatus.SC_INTERNAL_SERVER_ERROR, GSON.toJson(ResultJson.error(LOCAL.getMessage(
                                "maintenance.calendar.fetch.error", json.getUrl()
                        ))));
                    }
                },
                /* Create new calendar */
                () -> {
                    if (json.getData() == null) {
                        try {
                            MM.createCalendarWithUrl(user, json.getName(), json.getUrl());
                        }
                        catch (DownloadException e) {
                            log.info(e);
                            Spark.halt(HttpStatus.SC_INTERNAL_SERVER_ERROR, GSON.toJson(ResultJson.error(
                                    LOCAL.getMessage(
                                            "maintenance.calendar.fetch.error",
                                            json.getUrl()
                                    )
                            )));
                        }
                    }
                    else {
                        MM.createCalendar(user, json.getName(), json.getData());
                    }
                }
        );
        return result(response, ResultJson.success(), new TypeToken<>() { });
    }

    /**
     * Refresh calendar data from url
     *
     * @param request the request object
     * @param response the response obejct
     * @param user the authorized user
     * @return string containing the JSON response
     */
    public static String refresh(Request request, Response response, User user) {
        MaintenanceCalendarJson json = GSON.fromJson(request.body(), MaintenanceCalendarJson.class);

        try {
            RescheduleStrategy rescheduleStrategy = RescheduleStrategyType
                    .fromLabel(json.getRescheduleStrategy())
                    .createInstance();
            List<RescheduleResult> results = MM.refreshCalendar(
                    user,
                    json.getName(),
                    List.of(rescheduleStrategy)
            );
            handleRescheduleResult(results, rescheduleStrategy.getType());
        }
        catch (EntityNotExistsException e) {
            log.info(e);
            Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(LOCAL.getMessage(
                    "maintenance.calendar.not.exists", json.getName()
            ))));
        }
        catch (DownloadException e) {
            log.info(e);
            Spark.halt(HttpStatus.SC_INTERNAL_SERVER_ERROR, GSON.toJson(ResultJson.error(LOCAL.getMessage(
                    "maintenance.calendar.fetch.error", json.getUrl()
            ))));
        }

        return result(response, ResultJson.success(), new TypeToken<>() { });
    }

    /**
     * Deletes a given calendar
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorzed user
     * @return the result JSON object
     */
    public static String delete(Request request, Response response, User user) {
        MaintenanceCalendarJson json = GSON.fromJson(request.body(), MaintenanceCalendarJson.class);

        String name = json.getName();
        MM.lookupCalendarByUserAndLabel(user, name).ifPresentOrElse(
                calendar -> {
                    RescheduleStrategyType type = RescheduleStrategyType.fromLabel(json.getRescheduleStrategy());
                    handleRescheduleResult(MM.remove(user, calendar, type == CANCEL), type);
                },
                () -> Spark.halt(HttpStatus.SC_BAD_REQUEST)
        );

        return result(response, ResultJson.success(), new TypeToken<>() { });
    }
}
