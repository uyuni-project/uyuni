/*
 * Copyright (c) 2019 SUSE LLC
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

package com.suse.manager.webui.controllers.admin.handlers;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withOrgAdmin;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.domain.user.User;

import com.suse.manager.webui.services.impl.MonitoringService;
import com.suse.manager.webui.utils.gson.ResultJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;

import spark.Request;
import spark.Response;

/**
 * Controller class providing backend code for the Admin -> Config pages.
 */
public class MonitoringApiController {

    // Logger
    private static final Logger LOG = LogManager.getLogger(MonitoringApiController.class);

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

    /**
     * Private constructor
     */
    private MonitoringApiController() { }

    /** Init routes for Config - Monitoring Api.*/
    public static void initRoutes() {
        get("/manager/api/admin/config/monitoring", withOrgAdmin(MonitoringApiController::status));
        post("/manager/api/admin/config/monitoring",
                withOrgAdmin(MonitoringApiController::changeMonitoringStatus));
    }

    /**
     * Get server monitoring status.
     * @param request http request
     * @param response http response
     * @param user current user
     * @return json response
     */
    public static String status(Request request, Response response, User user) {
        Optional<MonitoringService.MonitoringStatus> status = MonitoringService.getStatus();
        if (status.isPresent()) {
            return json(response, ResultJson.success(status.get()));
        }
        else {
            return json(response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    ResultJson.error("internal_error"));
        }
    }

    /**
     * Enable/disable server monitoring.
     * @param request http request
     * @param response http response
     * @param user current user
     * @return json response
     */
    public static String changeMonitoringStatus(Request request, Response response, User user) {
        Map<String, Boolean> jsonRequest;
        try {
            jsonRequest = GSON.fromJson(request.body(), new TypeToken<Map<String, Boolean>>() {
            }.getType());
        }
        catch (JsonParseException e) {
            LOG.error("Error parsing JSON body", e);
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("request_error"));
        }

        Boolean enable = jsonRequest.get("enable");
        if (enable != null) {
            Optional<MonitoringService.MonitoringStatus> exporters = enable ?
                    MonitoringService.enableMonitoring() :
                    MonitoringService.disableMonitoring();

            if (exporters.isPresent()) {
                return json(response, ResultJson.success(exporters.get()));
            }
            else {
                return json(response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ResultJson.error("internal_error"));
            }
        }
        else {
            LOG.error("Empty json request");
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("request_error"));
        }
    }

}
