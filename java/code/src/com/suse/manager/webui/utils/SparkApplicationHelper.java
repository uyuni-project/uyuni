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
package com.suse.manager.webui.utils;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.security.CSRFTokenValidator;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.common.security.PermissionException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suse.manager.webui.Languages;

import de.neuland.jade4j.JadeConfiguration;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.Route;
import spark.Spark;
import spark.TemplateViewRoute;
import spark.template.jade.JadeTemplateEngine;

/**
 * Utility methods to integrate Spark with SUSE Manager's infrastructure.
 */
public class SparkApplicationHelper {

    private static final String TEMPLATE_ROOT = "com/suse/manager/webui/templates";
    private static final Gson GSON = new GsonBuilder().create();

    /**
     * Private constructor.
     */
    private SparkApplicationHelper() {
    }

    /**
     * Use in routes to automatically get the current user in your controller.
     * Example: <code>Spark.get("/url", withUser(Controller::method), jade);</code>
     * @param route the route
     * @return the template view route
     */
    public static TemplateViewRoute withUser(TemplateViewRouteWithUser route) {
        return (request, response) -> {
            User user = new RequestContext(request.raw()).getCurrentUser();
            return route.handle(request, response, user);
        };
    }

    /**
     * Use in routes to automatically get the current user in your controller.
     * Example: <code>Spark.get("/url", withUser(Controller::method));</code>
     * @param route the route
     * @return the route
     */
    public static Route withUser(RouteWithUser route) {
        return (request, response) -> {
            User user = new RequestContext(request.raw()).getCurrentUser();
            return route.handle(request, response, user);
        };
    }

    /**
     * Use in routes to automatically get the current user, which must be an Org
     * admin, in your controller.
     * Example: <code>Spark.get("/url", withOrgAdmin(Controller::method));</code>
     * @param route the route
     * @return the route
     */
    public static TemplateViewRoute withOrgAdmin(TemplateViewRouteWithUser route) {
        return (request, response) -> {
            User user = new RequestContext(request.raw()).getCurrentUser();
            if (user == null || !user.hasRole(RoleFactory.ORG_ADMIN)) {
                throw new PermissionException("no perms");
            }
            return route.handle(request, response, user);
        };
    }

    /**
     * Use in routes to automatically get the current user, which must be an Org
     * admin, in your controller.
     * Example: <code>Spark.get("/url", withOrgAdmin(Controller::method));</code>
     * @param route the route
     * @return the route
     */
    public static Route withOrgAdmin(RouteWithUser route) {
        return (request, response) -> {
            User user = new RequestContext(request.raw()).getCurrentUser();
            if (user == null || !user.hasRole(RoleFactory.ORG_ADMIN)) {
                throw new PermissionException("no perms");
            }
            return route.handle(request, response, user);
        };
    }

    /**
     * Returns a route that adds a CSRF token to model.
     *
     * The model associated with the input route must contain the data in the form of a Map
     * instance. The token will be inserted into this Map.
     * Otherwise exception will be thrown.
     *
     * @param route the route
     * @return the route that adds the CSRF token to the ModelAndView
     */
    public static TemplateViewRoute withCsrfToken(TemplateViewRoute route) {
        return (request, response) -> {
            ModelAndView modelAndView = route.handle(request, response);
            Object model = modelAndView.getModel();
            if (model instanceof Map) {
                ((Map) model).put("csrf_token",
                        CSRFTokenValidator.getToken(request.session().raw()));
            }
            else {
                throw new UnsupportedOperationException("CSRF token can be added only to" +
                        " a Map!");
            }
            return modelAndView;
        };
    }

    /**
     * Sets up this application and the Jade engine.
     * @return the jade template engine
     */
    public static JadeTemplateEngine setup() {
        // set up shared variables
        Map<String, Object> sharedVariables = new HashMap<>();

        // this filter is evaluated before every request
        Spark.before((request, response) -> {
            // add the current request to shared variables
            sharedVariables.put("request", request);
            // default for text/html or OpenSynphony will complain
            response.type("text/html");
            // init the flash scope
            FlashScopeHelper.handleFlashData(request, response);
        });

        // set up template engine
        JadeTemplateEngine jade = new JadeTemplateEngine(TEMPLATE_ROOT);

        // set up i10n engine and other default template variables
        sharedVariables.put("l", Languages.getInstance());
        sharedVariables.put("h", ViewHelper.getInstance());
        sharedVariables.put("isDevMode",
                Config.get().getBoolean("java.development_environment"));
        JadeConfiguration config = jade.configuration();
        config.setSharedVariables(sharedVariables);

        // capture json endpoint exceptions, let others pass (resulting in status code 500)
        Spark.exception(RuntimeException.class, (e, request, response) -> {
            if (request.headers("accept").contains("json")) {
                Map<String, Object> exc = new HashMap<>();
                exc.put("message", e.getMessage());
                response.type("application/json");
                response.body(GSON.toJson(exc));
                response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            }
            else {
                throw (RuntimeException) e;
            }
        });

        return jade;
    }
}
