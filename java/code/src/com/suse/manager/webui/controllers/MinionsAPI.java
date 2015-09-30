/**
 * Copyright (c) 2015 SUSE LLC
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
import com.suse.manager.webui.services.SaltService;
import com.suse.manager.webui.services.impl.SaltAPIService;
import com.suse.saltstack.netapi.datatypes.target.Glob;
import spark.Request;
import spark.Response;

import java.util.Map;
import java.util.Set;

/**
 * Controller class providing backend code for the minions page.
 */
public class MinionsAPI {

    private static final Gson GSON = new GsonBuilder().create();
    private static final SaltService SALT_SERVICE = SaltAPIService.INSTANCE;

    private MinionsAPI() { }

    /**
     * API endpoint to execute a command on salt minions by target glob
     * @param request the request object
     * @param response the response object
     * @return json result of the API call
     */
    public static String run(Request request, Response response) {
        String target = request.queryParams("target");
        String cmd = request.queryParams("cmd");
        Map<String, String> result = SALT_SERVICE.runRemoteCommand(new Glob(target), cmd);
        response.type("application/json");
        return GSON.toJson(result);
    }

    /**
     * API endpoint to get all minions matching a target glob
     * @param request the request object
     * @param response the response object
     * @return json result of the API call
     */
    public static String match(Request request, Response response) {
        String target = request.queryParams("target");
        Set<String> result = SALT_SERVICE.match(target).keySet();
        response.type("application/json");
        return GSON.toJson(result);
    }
}
