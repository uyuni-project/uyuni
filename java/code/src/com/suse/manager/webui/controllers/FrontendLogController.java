/**
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.rhn.domain.user.User;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import spark.Request;
import spark.Response;

/**
 * Spark controller class the FrontendLog handler.
 */
public class FrontendLogController {

    private static final Gson GSON = new GsonBuilder().create();

    private static Logger log = Logger.getLogger(FrontendLogController.class);

    private FrontendLogController() { }

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
        String message = "[" + user.getId() + "] - " + map.get("message").toString();

        switch (type) {
            case "info": log.info(message); break;
            case "debug": log.debug(message); break;
            case "warning": log.warn(message); break;
            case "error": log.error(message); break;
            default: log.info(message); break;
        }

        Map<String, Boolean> data = new HashMap<>();
        data.put("success", true);
        response.type("application/json");
        return GSON.toJson(data);
    }
}
