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

package com.suse.manager.webui.controllers.maintenance; import static org.junit.jupiter.api.Assertions.*;

import static com.suse.manager.maintenance.rescheduling.RescheduleStrategyType.CANCEL;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.post;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.user.User;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suse.manager.maintenance.MaintenanceManager;
import com.suse.manager.maintenance.rescheduling.RescheduleResult;
import com.suse.manager.maintenance.rescheduling.RescheduleStrategyType;
import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.webui.utils.gson.ResultJson;
import org.apache.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import spark.Request;
import spark.Response;
import spark.Spark;

/**
 * Controller class providing the backend for API calls to work with maintenance windows.
 */
public class MaintenanceController {

    private static final MaintenanceManager MM = new MaintenanceManager();
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
                withUser(MaintenanceController::getUpcomingMaintenanceWindows));
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

        res.type("application/json");
        return json(res, ResultJson.success(data));
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
}
