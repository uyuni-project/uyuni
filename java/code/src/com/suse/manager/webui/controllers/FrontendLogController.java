/*
 * Copyright (c) 2018 SUSE LLC
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

import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.throttling;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.post;

import com.redhat.rhn.domain.user.User;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

import spark.Request;
import spark.Response;

/**
 * Spark controller class the FrontendLog handler.
 */
public class FrontendLogController {

    private static final Gson GSON = new GsonBuilder().create();

    private static Logger log = LogManager.getLogger(FrontendLogController.class);

    /**
     * Initialize the {@link spark.Route}s served by this controller
     */
    public void initRoutes() {
        post("/manager/frontend-log", asJson(withUser(throttling(FrontendLogController::log))));
    }

    /**
     * Returns JSON data about the success of the log action
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON result of the API call
     */
    public static String log(Request request, Response response, User user) {
        Map<String, Object> map = GSON.fromJson(request.body(), Map.class);
        String type = map.get("level").toString();

        // Normalize the unicode message to canonical form to ensure no invalid characters are present
        String message = Normalizer.normalize(map.get("message").toString(), Normalizer.Form.NFC);
        message = "[" + user.getId() + " - " + request.userAgent() + "] - " + message;


        switch (type) {
            case "info": log.info(message); break;
            case "debug": log.debug(message); break;
            case "warning": log.warn(message); break;
            case "error": log.error(message); break;
            default: log.info(message); break;
        }

        Map<String, Boolean> data = new HashMap<>();
        data.put("success", true);
        return GSON.toJson(data);
    }
}
