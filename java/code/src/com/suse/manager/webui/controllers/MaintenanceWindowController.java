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
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.post;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.user.User;
import com.suse.manager.maintenance.MaintenanceManager;
import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.webui.utils.gson.ResultJson;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import spark.Request;
import spark.Response;


/**
 * Controller class providing backend code for the maintenance window pieces
 */
public class MaintenanceWindowController {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(MaintenanceWindowController.class);

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .serializeNulls()
            .create();

    private MaintenanceWindowController() { }

    /**
     * Invoked from Router. Initialize routes for Systems Views.
     */
    public static void initRoutes() {
        post("/manager/api/maintenance-windows", withUser(MaintenanceWindowController::getMaintenanceWindows));
    }

    /**
     * Get all the maintenance windows for the system ids selected
     *
     * @param req the request
     * @param res the response
     * @param user the current user
     * @return the json response
     */
    public static String getMaintenanceWindows(Request req, Response res, User user) {
        MaintenanceWindowsParams map = GSON.fromJson(req.body(), MaintenanceWindowsParams.class);

        Set<Long> systemIds = new HashSet<>(map.getSystemIds());

        String actionTypeLabel = map.getActionType();
        ActionType actionType = ActionFactory.lookupActionTypeByLabel(actionTypeLabel);

        Map<String, Object> data = new HashMap<>();

        if (actionType.isMaintenancemodeOnly()) {
            MaintenanceManager.instance()
                    .calculateUpcomingMaintenanceWindows(systemIds)
                    .ifPresent(windows -> data.put("maintenanceWindows", windows));
        }

        res.type("application/json");
        return json(res, ResultJson.success(data));
    }
}
