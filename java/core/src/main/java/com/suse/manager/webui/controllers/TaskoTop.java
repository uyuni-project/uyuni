/*
 * Copyright (c) 2016 SUSE LLC
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
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withOrgAdmin;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.get;

import com.redhat.rhn.domain.user.User;

import com.suse.manager.webui.services.TaskoTopCollector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;
import java.util.HashMap;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

/**
 * Controller class providing backend code for "Runtime Execution"
 * (well known as TaskoTop) page.
 */
public class TaskoTop {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
            .serializeNulls()
            .create();

    private TaskoTop() { }

    /**
     * Invoked from Router. Initialize routes for Systems Views.
     *
     * @param jade the jade engine to use to render the pages.
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        get("/manager/admin/runtime-status",
                withUserPreferences(withCsrfToken(withOrgAdmin(TaskoTop::show))), jade);
        get("/manager/api/admin/runtime-status/data",
                asJson(withOrgAdmin(TaskoTop::data)));
    }

    /**
     * Displays the taskotop report page
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView show(Request request, Response response, User user) {
        return new ModelAndView(new HashMap<>(), "templates/taskotop/show.jade");
    }

    /**
     * Returns JSON data from taskotop
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON result of the API call
     */
    public static String data(Request request, Response response, User user) {
        Object data = new TaskoTopCollector().getData(user);
        return GSON.toJson(data);
    }
}
