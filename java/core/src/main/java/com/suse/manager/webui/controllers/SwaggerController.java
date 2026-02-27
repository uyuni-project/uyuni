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

public class SwaggerController {

    /**
     * @param jade JadeTemplateEngine
     * Invoked from Router. Init routes for Swagger Views.
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        get("/manager/swagger",
                withCsrfToken(withRolesTemplate(SwaggerController::createView)), jade);
    }

    /**
     * Handler for the swagger page.
     *
     * @param req the request object
     * @param res the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView createView(Request req, Response res, User user) {
        Map<String, Object> data = new HashMap<>();
        return new ModelAndView(data, "templates/swagger/swagger.jade");
    }
}
