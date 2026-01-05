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

package com.suse.manager.webui.controllers.maintenance;

import static com.suse.manager.maintenance.rescheduling.RescheduleStrategyType.CANCEL;
import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.result;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.user.RhnTimeZone;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.EntityNotExistsException;

import com.suse.manager.maintenance.MaintenanceManager;
import com.suse.manager.maintenance.MaintenanceWindowData;
import com.suse.manager.maintenance.rescheduling.RescheduleResult;
import com.suse.manager.maintenance.rescheduling.RescheduleStrategyType;
import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.webui.utils.gson.MaintenanceWindowDataJson;
import com.suse.manager.webui.utils.gson.ResultJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import spark.Request;
import spark.Response;
import spark.Spark;

/**
 * Controller class providing the backend for API calls to work with maintenance windows.
 */
public class MaintenanceController {

    private static final MaintenanceManager MM = new MaintenanceManager();
    private static Logger log = LogManager.getLogger(MaintenanceController.class);
    private static final LocalizationService LOCAL = LocalizationService.getInstance();
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .serializeNulls()
            .create();

    private MaintenanceController() { }

    /**
     * Invoked from Router. Initialize routes for MaintenanceWindow Api.
     */
    public static void initRoutes() {
        // upcoming maintenance windows for systems
        post("/manager/api/maintenance/upcoming-windows",
                asJson(withUser(MaintenanceController::getUpcomingMaintenanceWindows)));
        get("/manager/api/maintenance/events/:operation/:type/:startOfWeek/:date/:id",
                asJson(withUser(MaintenanceController::getEvents)));
    }

    /**
     * Get all the maintenance windows for the system ids selected
     *
     * @param req the request
     * @param res the response
     * @param user the current user
     * @return the json response
     */
    public static String getUpcomingMaintenanceWindows(Request req, Response res, User user) {
        MaintenanceWindowsParams map = GSON.fromJson(req.body(), MaintenanceWindowsParams.class);

        Set<Long> systemIds = new HashSet<>(map.getSystemIds());

        String actionTypeLabel = map.getActionType();
        ActionType actionType = ActionFactory.lookupActionTypeByLabel(actionTypeLabel);

        Map<String, Object> data = new HashMap<>();

        if (actionType.isMaintenancemodeOnly()) {
            try {
                MM.calculateUpcomingMaintenanceWindows(systemIds)
                        .ifPresent(windows -> data.put("maintenanceWindows", windows));
            }
            catch (IllegalStateException e) {
                data.put("maintenanceWindowsMultiSchedules", true);
            }
        }

        return result(res, ResultJson.success(data), new TypeToken<>() { });
    }

    /**
     * Get the maintenance windows based on the provided request params
     *
     * @param request the request
     * @param response the response
     * @param user the current user
     * @return the json response
     */
    public static String getEvents(Request request, Response response, User user) {
        String type = request.params("type");
        Long date = Long.parseLong(request.params("date"));
        Long id = Long.parseLong(request.params("id"));
        String operation = request.params("operation");
        boolean startWithSunday = "0".equals(request.params("startOfWeek"));

        List<MaintenanceWindowData> events = new ArrayList<>();
        try {
            if (type.equals("calendar")) {
                events = MM.preprocessCalendarData(user, operation, id, date, startWithSunday);
            }
            else if (type.equals("schedule")) {
                events = MM.preprocessScheduleData(user, operation, id, date, startWithSunday);
            }
            else {
                throw new EntityNotExistsException(
                        "Type: " + type + " does not exist! Choose either 'schedule' or 'calendar'"
                );
            }
        }
        catch (EntityNotExistsException e) {
            log.error(e.getMessage());
            Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(e.getMessage())));
        }
        return json(response, eventsToJson(user, events), new TypeToken<>() { });
    }

    static void handleRescheduleResult(List<RescheduleResult> results, RescheduleStrategyType strategy) {
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

    /**
     * Given a date returns the date in the user configured timezone
     *
     * @param user the current user
     * @param date the date
     * @return the date string in the user configured timezone
     */
    private static String applyTimezoneShift(User user, Long date) {
        RhnTimeZone timezone = user.getTimeZone();
        ZoneId zoneId;
        if (timezone == null) {
            // Fallback to server timezone if no user timezone is configured
            zoneId = TimeZone.getDefault().toZoneId();
        }
        else {
            zoneId = ZoneId.of(timezone.getOlsonName());
        }
       return ZonedDateTime.ofInstant(Instant.ofEpochMilli(date), zoneId)
               .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm z"));
    }

    private static List<MaintenanceWindowDataJson> eventsToJson(User user, List<MaintenanceWindowData> events) {
        return events.stream().map(event -> eventToJson(user, event)).collect(Collectors.toList());
    }

    private static MaintenanceWindowDataJson eventToJson(User user, MaintenanceWindowData event) {
        MaintenanceWindowDataJson json = new MaintenanceWindowDataJson();
        json.setTitle(event.getName());
        json.setStart(applyTimezoneShift(user, event.getFromMilliseconds()));
        json.setEnd(applyTimezoneShift(user, event.getToMilliseconds()));

        return json;
    }
}
