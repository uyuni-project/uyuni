/**
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

package com.suse.manager.webui.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.redhat.rhn.domain.user.User;
import com.suse.manager.webui.services.impl.MonitoringService;
import com.suse.manager.webui.utils.gson.ResultJson;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;

/**
 * Controller class providing backend code for the Admin -> Config pages.
 */
public class AdminConfigController {

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

    /**
     * Private constructor
     */
    private AdminConfigController() { }

    // Logger
    private static final Logger LOG = Logger.getLogger(AdminConfigController.class);

    /**
     * Show monitoring tab.
     * @param request http request
     * @param response http response
     * @param user current user
     * @return the view to show
     */
    public static ModelAndView showMonitoring(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();
        return new ModelAndView(data, "templates/admin/config/monitoring.jade");
    }

    /**
     * Get server monitoring status.
     * @param request http request
     * @param response http response
     * @param user current user
     * @return json response
     */
    public static String status(Request request, Response response, User user) {
        Optional<Map<String, Boolean>> exporters = MonitoringService.getStatus();
        if (exporters.isPresent()) {
            Map<String, Object> jsonResult = new HashMap<>();
            jsonResult.put("exporters", exporters.get());
            return json(response, ResultJson.success(jsonResult));
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
            Optional<Map<String, Boolean>> exporters = enable ?
                    MonitoringService.enableMonitoring() :
                    MonitoringService.disableMonitoring();

            if (exporters.isPresent()) {
                Map<String, Object> jsonResult = new HashMap<>();
                jsonResult.put("exporters", exporters.get());

                return json(response, ResultJson.success(jsonResult));
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
