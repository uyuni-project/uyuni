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

import static com.suse.manager.webui.controllers.maintenance.MaintenanceController.handleRescheduleResult;
import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.result;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withDocsLocale;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.EssentialServerDto;
import com.redhat.rhn.frontend.dto.SystemScheduleDto;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.manager.EntityNotExistsException;
import com.redhat.rhn.manager.ssm.SsmManager;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.maintenance.IcalUtils;
import com.suse.manager.maintenance.MaintenanceManager;
import com.suse.manager.maintenance.rescheduling.RescheduleResult;
import com.suse.manager.maintenance.rescheduling.RescheduleStrategy;
import com.suse.manager.maintenance.rescheduling.RescheduleStrategyType;
import com.suse.manager.model.maintenance.MaintenanceCalendar;
import com.suse.manager.model.maintenance.MaintenanceSchedule;
import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.webui.utils.PageControlHelper;
import com.suse.manager.webui.utils.ViewHelper;
import com.suse.manager.webui.utils.gson.MaintenanceScheduleJson;
import com.suse.manager.webui.utils.gson.PagedDataResultJson;
import com.suse.manager.webui.utils.gson.ResultJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
 * Controller class providing the backend for API calls to work with maintenance schedules.
 */
public class MaintenanceScheduleController {

    private static final MaintenanceManager MM = new MaintenanceManager();
    private static final LocalizationService LOCAL = LocalizationService.getInstance();
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .serializeNulls()
            .create();
    private static Logger log = LogManager.getLogger(MaintenanceScheduleController.class);

    private MaintenanceScheduleController() { }

    /**
     * Initialize routes for MaintenanceSchedule Api.
     *
     * @param jade the template engine
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        get("/manager/schedule/maintenance/schedules",
                withUserPreferences(withCsrfToken(withUser(MaintenanceScheduleController::maintenanceSchedules))),
                jade);
        get("/manager/systems/ssm/maintenance", withCsrfToken(withDocsLocale(withUser(
                MaintenanceScheduleController::ssmSchedules))), jade);
        get("/manager/api/maintenance/schedule/list", withUser(MaintenanceScheduleController::list));
        get("/manager/api/maintenance/schedule/:id/details", withUser(MaintenanceScheduleController::details));
        get("/manager/api/maintenance/schedule/:id/systems",
                asJson(withUser(MaintenanceScheduleController::assignedSystems)));
        get("/manager/api/maintenance/schedule/systems",
                asJson(withUser(MaintenanceScheduleController::systemSchedules)));
        post("/manager/api/maintenance/schedule/:id/setsystems",
                asJson(withUser(MaintenanceScheduleController::setAssignedSystems)));
        post("/manager/api/maintenance/schedule/:id/assign", asJson(withUser(MaintenanceScheduleController::assign)));
        post("/manager/api/maintenance/schedule/unassign", asJson(withUser(MaintenanceScheduleController::unassign)));
        post("/manager/api/maintenance/schedule/save", asJson(withUser(MaintenanceScheduleController::save)));
        Spark.delete("/manager/api/maintenance/schedule/delete",
                asJson(withUser(MaintenanceScheduleController::delete)));
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
        Map<String, Object> params = new HashMap<>();
        params.put("type", "schedule");
        params.put("isAdmin", user.hasRole(RoleFactory.ORG_ADMIN));
        return new ModelAndView(params, "templates/schedule/maintenance-windows.jade");
    }

    /**
     * Handler for the SSM schedule assignment page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView ssmSchedules(Request request, Response response, User user) {
        List<Long> systemIds = SsmManager.listServerIds(user);

        Map<String, Object> data = new HashMap<>();
        data.put("systems", GSON.toJson(systemIds));
        data.put("tabs",
                ViewHelper.getInstance().renderNavigationMenu(request, "/WEB-INF/nav/ssm.xml"));
        return new ModelAndView(data, "templates/ssm/schedules.jade");
    }

    /**
     * Processes a GET request to get a list of all Maintenance Schedules visible to given user
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static String list(Request request, Response response, User user) {
        List<MaintenanceSchedule> schedules = MM.listSchedulesByUser(user);
        return json(response, schedulesToJson(schedules), new TypeToken<>() { });
    }

    /**
     * Processes a GET request to get the details of a schedule identified by its id
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static String details(Request request, Response response, User user) {
        Long scheduleId = Long.parseLong(request.params("id"));
        MaintenanceScheduleJson json = new MaintenanceScheduleJson();

        MaintenanceSchedule schedule = MM.lookupScheduleByUserAndId(user, scheduleId)
                .orElseThrow(() -> Spark.halt(
                        HttpStatus.SC_BAD_REQUEST,
                        GSON.toJson(ResultJson.error(LOCAL.getMessage(
                                "maintenance.schedule.id.not.exists", scheduleId
                        ))))
                );
        json.setId(schedule.getId());
        json.setName(schedule.getName());
        json.setType(schedule.getScheduleType().toString());

        schedule.getCalendarOpt().ifPresent(maintenanceCalendar -> {
            if (schedule.getScheduleType().equals(MaintenanceSchedule.ScheduleType.SINGLE)) {
                json.setEventNames(new IcalUtils().getEventNames(maintenanceCalendar));
            }
            json.setCalendarName(maintenanceCalendar.getLabel());
        });
        return json(response, json, new TypeToken<>() { });
    }

    /**
     * Create or update a schedule
     *
     * @param request the request object
     * @param response the response obejct
     * @param user the authorized user
     * @return string containing the JSON response
     */
    public static String save(Request request, Response response, User user) {
        MaintenanceScheduleJson json = GSON.fromJson(request.body(), MaintenanceScheduleJson.class);

        if (json.getName().isBlank()) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(LOCAL.getMessage(
                    "maintenance.schedule.name.empty"
            ))));
        }
        try {
            if (json.getId() == null) {
                Optional<MaintenanceCalendar> calendar = Optional.empty();
                if (!json.getCalendarName().isBlank()) {
                    /* Lookup calendar */
                    calendar = Optional.of(MM.lookupCalendarByUserAndLabel(user, json.getCalendarName())
                            .orElseThrow(() -> new EntityNotExistsException(LOCAL.getMessage(
                                    "maintenance.calendar.not.exists", json.getCalendarName()
                            )))
                    );
                }
                /* Create new schedule */
                MM.createSchedule(
                        user,
                        json.getName(),
                        MaintenanceSchedule.ScheduleType.lookupByLabel(json.getType().toLowerCase()),
                        calendar
                );
            }
            else {
                /* Update existing schedule */
                RescheduleStrategy rescheduleStrategy = RescheduleStrategyType
                        .fromLabel(json.getRescheduleStrategy())
                        .createInstance();
                Map<String, String> details = new HashMap<>();
                details.put("type", json.getType().toLowerCase());
                details.put("calendar", json.getCalendarName().strip());
                RescheduleResult result = MM.updateSchedule(user, json.getName(), details,
                        List.of(rescheduleStrategy));
                handleRescheduleResult(List.of(result), rescheduleStrategy.getType());
            }
        }
        catch (EntityNotExistsException | EntityExistsException e) {
            log.info(e);
            Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(e.getMessage())));
        }
        return result(response, ResultJson.success(), new TypeToken<>() { });
    }

    /**
     * Deletes a given schedule
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorzed user
     * @return the result JSON object
     */
    public static String delete(Request request, Response response, User user) {
        MaintenanceScheduleJson json = GSON.fromJson(request.body(), MaintenanceScheduleJson.class);

        String name = json.getName();
        MM.lookupScheduleByUserAndName(user, name).ifPresentOrElse(
                schedule -> MM.remove(user, schedule),
                () -> Spark.halt(HttpStatus.SC_BAD_REQUEST)
        );

        return result(response, ResultJson.success(), new TypeToken<>() { });
    }

    /**
     * Returns a list of system IDs assigned to a specified maintenance schedule
     *
     * @param request the Spark request
     * @param response the Spark response
     * @param user the authorized user
     * @return the JSON list of system IDs
     */
    public static String assignedSystems(Request request, Response response, User user) {
        Long scheduleId = Long.parseLong(request.params("id"));
        List<Long> systemIds = new ArrayList<>();
        MM.lookupScheduleByUserAndId(user, scheduleId).ifPresentOrElse(
                schedule -> {
                    List<EssentialServerDto> systems = SystemManager.systemsInSchedule(user, schedule, null);
                    systemIds.addAll(systems.stream().map(EssentialServerDto::getId).collect(Collectors.toList()));
                },
                () -> Spark.halt(HttpStatus.SC_NOT_FOUND)
        );
        return json(response, systemIds, new TypeToken<>() { });
    }

    /**
     * Returns a paged list of systems visible to a user and their assigned schedules
     *
     * @param request the Spark request
     * @param response the Spark response
     * @param user the authorized user
     * @return the JSON response
     */
    public static String systemSchedules(Request request, Response response, User user) {
        PageControlHelper pageHelper = new PageControlHelper(request, "name");

        DataResult<SystemScheduleDto> systems = SystemManager.systemListWithSchedules(user, null);

        if ("id".equals(pageHelper.getFunction())) {
            // Return only IDs for "select all" function
            return json(response, systems.stream().map(SystemScheduleDto::getId).collect(Collectors.toList()),
                    new TypeToken<>() { });
        }

        systems = pageHelper.processPageControl(systems, new HashMap<>());
        return json(response, new PagedDataResultJson<>(systems, null), new TypeToken<>() { });
    }

    private class SystemAssignmentRequest {
        private List<Long> systemIds;
        private boolean cancelActions;
    }

    /**
     * Update a schedule's assigned system list, assigning/retracting individual systems as required
     *
     * The update is performed as follows:
     *  - Sets the schedule for the systems which are not assigned to the specified schedule
     *  - Clears the schedule for the systems that are already assigned to the specified schedule, but are not listed
     *    in the requested system list
     *  - The requested systems that are already assigned to the specified schedule remain untouched
     *
     * @param request the Spark request
     * @param response the Spark response
     * @param user the authorized user
     * @return the JSON response
     */
    public static String setAssignedSystems(Request request, Response response, User user) {
        SystemAssignmentRequest reqData = GSON.fromJson(request.body(), SystemAssignmentRequest.class);
        List<Long> requestedSysIds = reqData.systemIds;

        Long scheduleId = Long.parseLong(request.params("id"));
        MM.lookupScheduleByUserAndId(user, scheduleId).ifPresentOrElse(
                schedule -> {
                    // Get previously assigned system IDs
                    List<Long> systemsInSchedule = SystemManager.systemsInSchedule(user, schedule, null).stream()
                            .map(EssentialServerDto::getId).collect(Collectors.toList());
                    try {
                        // New systems to assign
                        MM.assignScheduleToSystems(user, schedule,
                                requestedSysIds.stream()
                                        .filter(s -> !systemsInSchedule.contains(s))
                                        .collect(Collectors.toSet()),
                                reqData.cancelActions);

                        // Systems to unassign
                        MM.retractScheduleFromSystems(user,
                                systemsInSchedule.stream()
                                        .filter(s -> !requestedSysIds.contains(s))
                                        .collect(Collectors.toSet()));
                    }
                    catch (IllegalArgumentException e) {
                        log.info(e);
                        Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(LOCAL.getMessage(
                                "maintenance.action.assign.error.fail"))));
                    }
                    catch (LookupException e) {
                        log.info(e);
                        Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(LOCAL.getMessage(
                                "maintenance.action.assign.error.systemnotfound"))));
                    }
                },
                () -> Spark.halt(HttpStatus.SC_NOT_FOUND)
        );
        return result(response, ResultJson.success(), new TypeToken<>() { });
    }

    /**
     * Assign a schedule to systems
     *
     * @param request the request object
     * @param response the response obejct
     * @param user the authorized user
     * @return string containing the JSON response
     */
    public static String assign(Request request, Response response, User user) {
        SystemAssignmentRequest reqData = GSON.fromJson(request.body(), SystemAssignmentRequest.class);
        List<Long> systemIds = reqData.systemIds;

        Long scheduleId = Long.parseLong(request.params("id"));
        MM.lookupScheduleByUserAndId(user, scheduleId).ifPresentOrElse(
                schedule -> {
                    try {
                        MM.assignScheduleToSystems(user, schedule, new HashSet<>(systemIds), reqData.cancelActions);
                    }
                    catch (IllegalArgumentException e) {
                        log.info(e);
                        Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(LOCAL.getMessage(
                                "maintenance.action.assign.error.fail"))));
                    }
                    catch (LookupException e) {
                        log.info(e);
                        Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(LOCAL.getMessage(
                                "maintenance.action.assign.error.systemnotfound"))));
                    }
                },
                () -> Spark.halt(HttpStatus.SC_NOT_FOUND)
        );
        return result(response, ResultJson.success(), new TypeToken<>() { });
    }

    /**
     * Unassign a schedule from systems
     *
     * @param request the request object
     * @param response the response obejct
     * @param user the authorized user
     * @return string containing the JSON response
     */
    public static String unassign(Request request, Response response, User user) {
        List<Long> systemIds = Arrays.asList(GSON.fromJson(request.body(), Long[].class));
        try {
            MM.retractScheduleFromSystems(user, new HashSet<>(systemIds));
        }
        catch (LookupException e) {
            log.info(e);
            Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(LOCAL.getMessage(
                    "maintenance.action.assign.error.systemnotfound"))));
        }
        return result(response, ResultJson.success(), new TypeToken<>() { });
    }

    private static List<MaintenanceScheduleJson> schedulesToJson(List<MaintenanceSchedule> schedules) {
        return schedules.stream().map(MaintenanceScheduleController::scheduleToJson).collect(Collectors.toList());
    }

    private static MaintenanceScheduleJson scheduleToJson(MaintenanceSchedule schedule) {
        MaintenanceScheduleJson json = new MaintenanceScheduleJson();

        json.setId(schedule.getId());
        json.setName(schedule.getName());
        schedule.getCalendarOpt().ifPresent(maintenanceCalendar -> {
            json.setCalendarId(maintenanceCalendar.getId());
            json.setCalendarName(maintenanceCalendar.getLabel());
        });

        return json;
    }
}
