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

import static com.suse.manager.webui.utils.SparkApplicationHelper.GSON;
import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withRolesTemplate;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.get;
import static spark.Spark.halt;

import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.OpenApiConfig;
import com.suse.manager.api.docs.UyuniSwaggerReader;
import com.suse.manager.webui.utils.RouteWithUser;

import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

/**
 * Controller for the Swagger UI page and OpenAPI specification endpoints.
 */
public final class SwaggerController {

    private static final Map<String, Class<?>> HANDLERS = OpenApiConfig.getHandlerClasses();

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
        get("/manager/api/openapi/namespaces",
                asJson(withUser((RouteWithUser) (req, res, user) -> getNamespaces(req, res))));
        get("/manager/api/openapi/:namespace",
                asJson(withUser((RouteWithUser) (req, res, user) -> getSpec(req, res))));
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

    /**
     * Returns a sorted list of available API namespaces.
     *
     * @param req request object
     * @param res response object
     * @return JSON array of namespace names
     */
    public static String getNamespaces(Request req, Response res) {
        List<String> names = HANDLERS.keySet().stream().sorted().toList();
        return json(GSON, res, names, new TypeToken<>() { });
    }

    /**
     * Returns the OpenAPI specification for the requested namespace.
     *
     * @param req request object (must contain a {@code namespace} path parameter)
     * @param res response object
     * @return OpenAPI JSON spec, or 404 if the namespace is unknown
     */
    public static String getSpec(Request req, Response res) {
        String namespace = req.params("namespace");
        if (!HANDLERS.containsKey(namespace)) {
            halt(HttpStatus.SC_NOT_FOUND, String.format("Namespace not found: %s", namespace));
        }
        OpenAPI spec = new UyuniSwaggerReader().read(HANDLERS.get(namespace), namespace);
        return Json.pretty(spec);
    }
}
