/*
 * Copyright (c) 2025 SUSE LLC
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

import static com.suse.manager.webui.utils.SparkApplicationHelper.withOrgAdmin;
import static spark.Spark.get;
import static spark.Spark.patch;
import static spark.Spark.post;

import com.redhat.rhn.domain.user.User;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import spark.Request;
import spark.Response;

public class ISSApiController {

    // Logger
    private static final Logger LOG = LogManager.getLogger(ISSApiController.class);

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

    /**
     * Private constructor
     */
    private ISSApiController() { }

    /** Init routes for Config - Monitoring Api.*/
    public static void initRoutes() {
        get("/manager/api/admin/iss/hubs", withOrgAdmin(ISSApiController::pass));
        get("/manager/api/admin/iss/peripherals",
                withOrgAdmin(ISSApiController::pass));
        post("/manager/api/admin/iss/hubs", withOrgAdmin(ISSApiController::pass));
        post("/manager/api/admin/iss/peripherals",
                withOrgAdmin(ISSApiController::pass));
        patch("/manager/api/admin/iss/hubs/:id", withOrgAdmin(ISSApiController::pass));
        patch("/manager/api/admin/iss/peripherals/:id",
                withOrgAdmin(ISSApiController::pass));
    }

    /**
     * Get server monitoring status.
     * @param request http request
     * @param response http response
     * @param user current user
     * @return json response
     */
    public static String pass(Request request, Response response, User user) {
        return "";
    }

}
