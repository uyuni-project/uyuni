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
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.manager.EntityNotExistsException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suse.manager.maintenance.IcalUtils;
import com.suse.manager.maintenance.MaintenanceManager;
import com.suse.manager.maintenance.rescheduling.RescheduleResult;
import com.suse.manager.maintenance.rescheduling.RescheduleStrategy;
import com.suse.manager.maintenance.rescheduling.RescheduleStrategyType;
import com.suse.manager.model.maintenance.MaintenanceCalendar;
import com.suse.manager.model.maintenance.MaintenanceSchedule;
import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.webui.utils.gson.MaintenanceWindowJson;
import com.suse.manager.webui.utils.gson.ResultJson;
import org.apache.http.HttpStatus;

import java.time.LocalDateTime;
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
 * Controller class providing the backend for API calls to work with maintenance schedules.
 */
public class MaintenanceScheduleController {

    private static IcalUtils icalUtils = new IcalUtils();
    private static final MaintenanceManager MM = new MaintenanceManager();
    private static final LocalizationService LOCAL = LocalizationService.getInstance();
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .serializeNulls()
            .create();

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
        get("/manager/api/maintenance/schedule/list",
                withUser(MaintenanceScheduleController::listSchedules));
        get("/manager/api/maintenance/schedule/:id/details",
                withUser(MaintenanceScheduleController::getScheduleDetails));
        post("/manager/api/maintenance/schedule/save",
                withUser(MaintenanceScheduleController::saveSchedule));
        delete("/manager/api/maintenance/schedule/delete",
                withUser(MaintenanceScheduleController::deleteSchedule));
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
     * Processes a GET request to get a list of all Maintenance Schedules visible to given user
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static String listSchedules(Request request, Response response, User user) {
        List<MaintenanceSchedule> schedules = MM.listSchedulesByUser(user);
        return json(response, schedulesToJson(schedules));
    }

    /**
     * Processes a GET request to get the details of a schedule identified by its id
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static String getScheduleDetails(Request request, Response response, User user) {
        Long scheduleId = Long.parseLong(request.params("id"));
        MaintenanceWindowJson json = new MaintenanceWindowJson();

        MaintenanceSchedule schedule = MM.lookupScheduleByUserAndId(user, scheduleId)
                .orElseThrow(() -> Spark.halt(
                        HttpStatus.SC_BAD_REQUEST,
                        GSON.toJson(ResultJson.error(LOCAL.getMessage(
                                "maintenance.schedule.id.not.exists", scheduleId
                        ))))
                );
        json.setScheduleId(schedule.getId());
        json.setScheduleName(schedule.getName());
        json.setScheduleType(schedule.getScheduleType().toString());

        icalUtils.calculateUpcomingMaintenanceWindows(schedule).ifPresent(windows -> json.setMaintenanceWindows(
                windows.stream().map(window -> Map.of(
                        "start", window.getFrom(),
                        "end", window.getTo()
                )).collect(Collectors.toList())
        ));

        schedule.getCalendarOpt().ifPresent(maintenanceCalendar -> {
            json.setCalendarName(maintenanceCalendar.getLabel());
            maintenanceCalendar.getUrlOpt().ifPresent(json::setCalendarUrl);
        });
        return json(response, json);
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

        if (json.getScheduleName().isBlank()) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(LOCAL.getMessage(
                    "maintenance.schedule.name.empty"
            ))));
        }
        try {
            if (json.getScheduleId() == null) {
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
                        json.getScheduleName(),
                        MaintenanceSchedule.ScheduleType.lookupByLabel(json.getScheduleType().toLowerCase()),
                        calendar
                );
            }
            else {
                /* Update existing schedule */
                RescheduleStrategy rescheduleStrategy = RescheduleStrategyType
                        .fromLabel(json.getRescheduleStrategy())
                        .createInstance();
                Map<String, String> details = new HashMap<>();
                details.put("type", json.getScheduleType().toLowerCase());
                details.put("calendar", json.getCalendarName().strip());
                RescheduleResult result = MM.updateSchedule(user, json.getScheduleName(), details,
                        List.of(rescheduleStrategy));
                handleRescheduleResult(List.of(result), rescheduleStrategy.getType());
            }
        }
        catch (EntityNotExistsException | EntityExistsException e) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(e.getMessage())));
        }
        return json(response, ResultJson.success());
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
        response.type("application/json");
        MaintenanceWindowJson json = GSON.fromJson(request.body(), MaintenanceWindowJson.class);

        String name = json.getScheduleName();
        MM.lookupScheduleByUserAndName(user, name).ifPresentOrElse(
                schedule -> MM.remove(user, schedule),
                () -> Spark.halt(HttpStatus.SC_BAD_REQUEST)
        );

        return json(response, ResultJson.success());
    }

    private static void handleRescheduleResult(List<RescheduleResult> results, RescheduleStrategyType strategy) {
        results.forEach(result -> {
            if (!result.isSuccess()) {
                String affectedSchedule = result.getScheduleName();
                String message = LOCAL.getMessage(strategy == CANCEL ?
                                "maintenance.action.reschedule.error.cancel" :
                                "maintenance.action.reschedule.error.fail",
                        affectedSchedule);
                Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(message)));
            }
        });
    }

    private static List<MaintenanceWindowJson> schedulesToJson(List<MaintenanceSchedule> schedules) {
        return schedules.stream().map(MaintenanceScheduleController::scheduleToJson).collect(Collectors.toList());
    }

    private static MaintenanceWindowJson scheduleToJson(MaintenanceSchedule schedule) {
        MaintenanceWindowJson json = new MaintenanceWindowJson();

        json.setScheduleId(schedule.getId());
        json.setScheduleName(schedule.getName());
        schedule.getCalendarOpt().ifPresent(maintenanceCalendar -> {
            json.setCalendarId(maintenanceCalendar.getId());
            json.setCalendarName(maintenanceCalendar.getLabel());
        });

        return json;
    }
}
