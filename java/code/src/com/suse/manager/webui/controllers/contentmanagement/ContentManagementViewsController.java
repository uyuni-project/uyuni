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
package com.suse.manager.webui.controllers.contentmanagement;

import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static spark.Spark.get;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

/**
 * Spark controller class for content management pages and API endpoints.
 */
public class ContentManagementViewsController {

    private static Logger log = Logger.getLogger(ContentManagementViewsController.class);

    private ContentManagementViewsController() {
    }

    /**
     * Init all the routes used by ContentManagementViewsController
     * @param jade the used jade template engine
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        get("/manager/contentmanagement/createproject",
                withCsrfToken(ContentManagementViewsController::createProjectView), jade);
        get("/manager/contentmanagement/listprojects",
                withCsrfToken(ContentManagementViewsController::listProjectsView), jade);
        get("/manager/contentmanagement/project",
                withCsrfToken(ContentManagementViewsController::projectView), jade);
    }

    /**
     * Returns a view to create a new management projects
     *
     * @param req  the request object
     * @param res  the response object
     * @return the model and view
     */
    public static ModelAndView createProjectView(Request req, Response res) {
        Map<String, Object> data = new HashMap<>();
        return new ModelAndView(data, "controllers/contentmanagement/templates/create-project.jade");
    }

    /**
     * Returns a view to list management projects
     *
     * @param req  the request object
     * @param res  the response object
     * @return the model and view
     */
    public static ModelAndView listProjectsView(Request req, Response res) {
        Map<String, Object> data = new HashMap<>();
        return new ModelAndView(data, "controllers/contentmanagement/templates/list-projects.jade");
    }

    /**
     * Returns a view to show a management project detail.
     *
     * @param req  the request object
     * @param res  the response object
     * @return the model and view
     */
    public static ModelAndView projectView(Request req, Response res) {
        Map<String, Object> data = new HashMap<>();
        return new ModelAndView(data, "controllers/contentmanagement/templates/project.jade");
    }

}
