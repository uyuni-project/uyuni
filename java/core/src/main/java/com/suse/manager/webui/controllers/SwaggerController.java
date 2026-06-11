/*
 * Copyright (c) 2026 SUSE LLC
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

import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withRolesTemplate;
import static spark.Spark.get;

import com.redhat.rhn.domain.user.User;

import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

/**
 * Controller for the Swagger UI page.
 */
public final class SwaggerController {

    private SwaggerController() {
    }

    /**
     * Invoked from the router to initialize Swagger routes.
     *
     * @param jade jade template engine
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        get("/manager/swagger",
                withCsrfToken(withRolesTemplate(SwaggerController::createView)), jade);
    }

    /**
     * Handler for the Swagger page.
     *
     * @param req request object
     * @param res response object
     * @param user current user
     * @return model and view for the page
     */
    public static ModelAndView createView(Request req, Response res, User user) {
        Map<String, Object> data = new HashMap<>();
        return new ModelAndView(data, "templates/swagger/swagger.jade");
    }
}
