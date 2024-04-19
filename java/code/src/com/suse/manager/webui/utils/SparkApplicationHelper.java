/*
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

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.HibernateRuntimeException;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.security.CSRFTokenValidator;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.webui.Languages;
import com.suse.manager.webui.services.ThrottlingService;
import com.suse.manager.webui.services.TooManyCallsException;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.utils.ParameterizedTypeImpl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;

import java.lang.reflect.ParameterizedType;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import de.neuland.jade4j.JadeConfiguration;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import spark.TemplateViewRoute;
import spark.template.jade.JadeTemplateEngine;

/**
 * Utility methods to integrate Spark with SUSE Manager's infrastructure.
 */
public class SparkApplicationHelper {

    private static final String TEMPLATE_ROOT = "com/suse/manager/webui";
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .create();
    private static final ThrottlingService THROTTLER = GlobalInstanceHolder.THROTTLING_SERVICE;

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
     * Returns a route that adds system related variables to the model.
     * The request needs to have either a query parameter or a parameter named <code>sid</code> containing the
     * server ID.
     *
     * Adds the server object, the tabs for the system page and the inSSM variables for use in all System page.
     *
     * @param route the route
     *
     * @return the route that adds the user preferences to the ModelAndView
     */
    public static TemplateViewRoute withUserAndServer(TemplateViewRouteWithUserAndServer route) {
        return withUserAndServer(route, "sid");
    }

    /**
     * Returns a route that adds system related variables to the model.
     *
     * Adds the server object, the tabs for the system page and the inSSM variables for use in all System page.
     *
     * @param route the route
     * @param sidName the request parameter name or query parameter name for the server id
     *
     * @return the route that adds the user preferences to the ModelAndView
     */
    public static TemplateViewRoute withUserAndServer(TemplateViewRouteWithUserAndServer route, String sidName) {
        return (request, response) -> {
            User user = new RequestContext(request.raw()).getCurrentUser();
            try {
                long serverId = Long.parseLong(request.queryParamOrDefault(sidName, request.params(sidName)));
                Server server = SystemManager.lookupByIdAndUser(serverId, user);

                ModelAndView modelAndView = route.handle(request, response, user, server);
                Object model = modelAndView.getModel();

                ((Map) model).put("server", server);
                ((Map) model).put("inSSM", RhnSetDecl.SYSTEMS.get(user).contains(server.getId()));
                Map<String, String[]> params = Collections.singletonMap("sid", new String[]{server.getId().toString()});
                ((Map) model).put("tabs",
                        ViewHelper.getInstance().renderNavigationMenuWithParams(request,
                                "/WEB-INF/nav/system_detail.xml", params));
                return modelAndView;
            }
            catch (NumberFormatException e) {
                throw Spark.halt(HttpStatus.SC_NOT_FOUND, "Invalid server id: " + request.params(sidName));
            }
            catch (LookupException e) {
                throw Spark.halt(HttpStatus.SC_NOT_FOUND, "Server not found: " + request.params(sidName));
            }
        };
    }

    /**
     * Use in routes to automatically get the current user and the server in your controller.
     * The request needs to have either a query parameter or a parameter named <code>sid</code> containing the
     * server ID.
     *
     * @param route the route
     *
     * @return the route that adds the user preferences to the ModelAndView
     */
    public static Route withUserAndServer(RouteWithUserAndServer route) {
        return withUserAndServer(route, "sid");
    }

    /**
     * Use in routes to automatically get the current user and the server in your controller.
     *
     * @param route the route
     * @param sidName the name of the request parameter containing the server id
     *
     * @return the route that adds the user preferences to the ModelAndView
     */
    public static Route withUserAndServer(RouteWithUserAndServer route, String sidName) {
        return (request, response) -> {
            User user = new RequestContext(request.raw()).getCurrentUser();
            try {
                long serverId = Long.parseLong(request.params(sidName));
                Server server = SystemManager.lookupByIdAndUser(serverId, user);

                return route.handle(request, response, user, server);
            }
            catch (NumberFormatException e) {
                throw Spark.halt(HttpStatus.SC_NOT_FOUND, "Invalid server id: " + request.params(sidName));
            }
            catch (LookupException e) {
                throw Spark.halt(HttpStatus.SC_NOT_FOUND, "Server not found: " + request.params(sidName));
            }
        };
    }

    /**
     * Returns a route that adds the users docsLocale to model.
     * The model associated with the input route must contain the data in the form of a Map
     * instance. The locale will be inserted into this Map.
     * Otherwise exception will be thrown.
     *
     * @param route the route
     * @return the route that adds the docsLocale to the ModelAndView
     */
    public static TemplateViewRoute withDocsLocale(TemplateViewRoute route) {
        return (request, response) -> {
            ModelAndView modelAndView = route.handle(request, response);
            Object model = modelAndView.getModel();
            if (model instanceof Map) {
                User user = new RequestContext(request.raw()).getCurrentUser();
                String docsLocale = Objects.requireNonNullElse(
                        user.getPreferredDocsLocale(), ConfigDefaults.get().getDefaultDocsLocale());
                ((Map) model).put("docsLocale", docsLocale);
            }
            else {
                throw new UnsupportedOperationException("docsLocale can only be added to a Map!");
            }
            return modelAndView;
        };
    }

    /**
     * Use in routes to automatically get the current user in your controller and inject the roles into your template.
     * Example: <code>Spark.get("/url", withRolesTemplate(Controller::method));</code>
     * @param route the route
     * @return the route
     */
    public static TemplateViewRoute withRolesTemplate(TemplateViewRouteWithUser route) {
        return (request, response) -> {
            User user = new RequestContext(request.raw()).getCurrentUser();
            ModelAndView modelAndView = route.handle(request, response, user);
            List<String> roles = user.getRoles().stream()
                    .map(Role::getLabel)
                    .collect(Collectors.toList());
            Object model = modelAndView.getModel();
            if (model instanceof Map) {
                ((Map) model).put("roles", GSON.toJson(roles));
            }
            else {
                throw new UnsupportedOperationException("User roles can be added only to" +
                        " a Map!");
            }
            return modelAndView;
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
        return withRole(route, RoleFactory.ORG_ADMIN);
    }

    /**
     * Use in routes to automatically get the current user, which must be the product
     * admin, in your controller.
     * Example: <code>Spark.get("/url", withProductAdmin(Controller::method));</code>
     * @param route the route
     * @return the route
     */
    public static TemplateViewRoute withProductAdmin(TemplateViewRouteWithUser route) {
        return withRole(route, RoleFactory.SAT_ADMIN);
    }

    /**
     * Use in routes to automatically get the current user, which must be the product
     * admin, in your controller.
     * Example: <code>Spark.get("/url", withProductAdmin(Controller::method));</code>
     * @param route the route
     * @return the route
     */
    public static Route withProductAdmin(RouteWithUser route) {
        return withRole(route, RoleFactory.SAT_ADMIN);
    }

    /**
     * Use in routes to automatically get the current user, which must be an Org
     * admin, in your controller.
     * Example: <code>Spark.get("/url", withOrgAdmin(Controller::method));</code>
     * @param route the route
     * @return the route
     */
    public static Route withOrgAdmin(RouteWithUser route) {
        return withRole(route, RoleFactory.ORG_ADMIN);
    }

    /**
     * Use in routes to automatically get the current user, which must be an
     * Image Admin, in your controller.
     * Example: <code>Spark.get("/url", withOrgAdmin(Controller::method));</code>
     * @param route the route
     * @return the route
     */
    public static Route withImageAdmin(RouteWithUser route) {
        return withRole(route, RoleFactory.IMAGE_ADMIN);
    }

    /**
     * Use in routes to automatically get the current user, which must be an
     * Image Admin, in your controller.
     * Example: <code>Spark.get("/url", withOrgAdmin(Controller::method));</code>
     * @param route the route
     * @return the route
     */
    public static TemplateViewRoute withImageAdmin(TemplateViewRouteWithUser route) {
        return withRole(route, RoleFactory.IMAGE_ADMIN);
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
     * Returns a route that adds the user preferences variables to model.
     *
     * The model associated with the input route must contain the data in the form of a Map instance.
     * The preferences variables will be inserted into this Map. Otherwise exception will be thrown.
     *
     * @param route the route
     * @return the route that adds the user preferences to the ModelAndView
     */
    public static TemplateViewRoute withUserPreferences(TemplateViewRoute route) {
        return (request, response) -> {
            User user = new RequestContext(request.raw()).getCurrentUser();
            ModelAndView modelAndView = route.handle(request, response);
            Object model = modelAndView.getModel();
            if (model instanceof Map) {
                ((Map) model).put("pageSize", user.getPageSize());
            }
            else {
                throw new UnsupportedOperationException("User preferences can be added only to a Map!");
            }
            return modelAndView;
        };
    }

    /**
     * Use in routes to automatically get the current user, which must have the
     * role specified, in your controller. Example:
     * <code>Spark.get("/url", withRole(Controller::method, RoleFactory.SAT_ADMIN));</code>
     * @param route the route
     * @param role the required role to have access to the route
     * @return the route
     */
    private static TemplateViewRoute withRole(TemplateViewRouteWithUser route, Role role) {
        return (request, response) -> {
            User user = new RequestContext(request.raw()).getCurrentUser();
            if (user == null || !user.hasRole(role)) {
                throw new PermissionException("no perms");
            }
            return route.handle(request, response, user);
        };
    }

    /**
     * Use in routes to automatically get the current user, which must have the
     * role specified, in your controller. Example:
     * <code>Spark.get("/url", withRole(Controller::method, RoleFactory.SAT_ADMIN));</code>
     * @param route the route
     * @param role the required role to have access to the route
     * @return the route
     */
    private static Route withRole(RouteWithUser route, Role role) {
        return (request, response) -> {
            User user = new RequestContext(request.raw()).getCurrentUser();
            if (user == null || !user.hasRole(role)) {
                throw new PermissionException("no perms");
            }
            return route.handle(request, response, user);
        };
    }

    /**
     * Sets the content type to "application/json" for a {@link Route}
     * @param route the route
     * @return the route
     */
    public static Route asJson(Route route) {
        return (request, response) -> {
            response.type("application/json");
            return route.handle(request, response);
        };
    }

    /**
     * Apply rate-limiting to a specific authenticated endpoint
     *
     * The routes set up with throttling allow only a limited number of calls for each period. If the consumer exceeds
     * the limit, the call returns a 429 (Too many requests) response, with a 'Retry-After' header set to notify the
     * consumer to try the request again after a certain amount of time (RFC 6585).
     * @param route the route
     * @return the route
     */
    public static RouteWithUser throttling(RouteWithUser route) {
        return throttling(route, ThrottlingService.DEF_MAX_CALLS_PER_PERIOD,
                ThrottlingService.DEF_THROTTLE_PERIOD_SECS);
    }

    /**
     * Apply rate-limiting to a specific authenticated endpoint
     *
     * The routes set up with throttling allow only a limited number of calls for each period. If the consumer exceeds
     * the limit, the call returns a 429 (Too many requests) response, with a 'Retry-After' header set to notify the
     * consumer to try the request again after a certain amount of time (RFC 6585).
     * @param route the route
     * @param maxCalls maximum number of allowed calls per throttling period
     * @param period the throttling period in seconds
     * @return the route
     */
    public static RouteWithUser throttling(RouteWithUser route, long maxCalls, long period) {
        return (req, res, user) -> {
            try {
                THROTTLER.call(user.getId(), req.pathInfo(), maxCalls, period);
            }
            catch (TooManyCallsException e) {
                res.header("Retry-After", Long.toString(period));
                Spark.halt(429, "Too many requests");
            }
            return route.handle(req, res, user);
        };
    }

    /**
     * Returns true if the response content type is application/json
     * @param response the response
     * @return true if the content type is application/json
     */
    public static boolean isJson(Response response) {
        return response.type().contains("application/json");
    }

    /**
     * Returns true if the request is made to the API root
     * @param request the request
     * @return true if the request is made to the API root
     */
    public static boolean isApiRequest(Request request) {
        return request.pathInfo().startsWith("/manager/api/");
    }

    /**
     * Initially the transactions were committed only in {@link com.redhat.rhn.frontend.servlets.SessionFilter}
     *
     * But, it turns out that Spark closes response stream at {@link spark.http.matching.Body#serializeTo} and the
     * response is sent back before the SessionFilter finishes running, creating a race condition. It was even possible
     * to have an HTTP 200 response and a failure when committing the transaction.
     *
     * The {@link HibernateFactory#inTransaction} check was added to both sides to prevent commit from executing twice.
     */
    public static void setupHibernateSessionFilter() {
        Logger logger = LogManager.getLogger(SparkApplicationHelper.class);
        Spark.after((requestIn, responseIn) -> {
            boolean committed = false;
            try {
                if (HibernateFactory.inTransaction()) {
                    HibernateFactory.commitTransaction();
                }
                committed = true;
            }
            catch (HibernateException e) {
                logger.error(HibernateFactory.ROLLBACK_MSG, e);
                throw new HibernateRuntimeException(HibernateFactory.ROLLBACK_MSG, e);
            }
            catch (RuntimeException e) {
                logger.error(HibernateFactory.ROLLBACK_MSG, e);
                throw e;
            }
            finally {
                HibernateFactory.rollbackTransactionAndCloseSession(committed);
            }
        });
    }

    /**
     * Sets up this application and the Jade engine.
     * @return the jade template engine
     */
    public static JadeTemplateEngine setup() {
        // this filter is evaluated before every request
        Spark.before((request, response) -> {
            // default for text/html or OpenSynphony will complain
            response.type("text/html");
            // init the flash scope
            FlashScopeHelper.handleFlashData(request, response);
        });

        // capture json endpoint exceptions, let others pass (resulting in status code 500)
        Spark.exception(RuntimeException.class, (e, request, response) -> {
            if (request.headers("accept").contains("json") || isJson(response)) {
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


        // set up template engine
        JadeTemplateEngine jade = new JadeTemplateEngine(TEMPLATE_ROOT);

        // set up i10n engine and other default template variables
        Map<String, Object> sharedVariables = new HashMap<>();
        sharedVariables.put("l", Languages.getInstance());
        sharedVariables.put("h", ViewHelper.getInstance());
        sharedVariables.put("isDevMode",
                Config.get().getBoolean("java.development_environment"));
        sharedVariables.put("isUyuni", ConfigDefaults.get().isUyuni());
        sharedVariables.put("webVersion", ConfigDefaults.get().getProductVersion());
        sharedVariables.put("webBuildtimestamp", Config.get().getString("web.buildtimestamp"));
        JadeConfiguration config = jade.configuration();
        config.setSharedVariables(sharedVariables);
        return jade;
    }

    /**
     * Serialize the result and set the response content type to JSON.
     * @param response the http response
     * @param result the object to serialize to JSON
     * @return a JSON string
     * @deprecated use methods that provide type token instead
     */
    @Deprecated
    public static String json(Response response, Object result) {
        response.type("application/json");
        return GSON.toJson(result);
    }

    /**
     * Serialize the result and set the response content type to JSON.
     * @param response the http response
     * @param result the object to serialize to JSON
     * @return a JSON string
     * @deprecated use methods that provide type token instead
     */
    @Deprecated
    public static String json(Response response, Map<String, Object> result) {
        response.type("application/json");
        return GSON.toJson(result);
    }

    /**
     * Serialize the result and set the response content type to JSON
     * and the http status code to the given value.
     * @param response the http response
     * @param httpStatusCode the http status code of the response
     * @param result the object to serialize to JSON
     * @return a JSON string
     * @deprecated use methods that provide type token instead
     */
    @Deprecated
    public static String json(Response response, int httpStatusCode, Object result) {
        response.type("application/json");
        response.status(httpStatusCode);
        return GSON.toJson(result);
    }

    /**
     * Serialize the result and set the response content type to JSON.
     * @param gson {@link Gson} object to use for serialization
     * @param response the http response
     * @param result the object to serialize to JSON
     * @return a JSON string
     * @deprecated use methods that provide type token instead
     */
    @Deprecated
    public static String json(Gson gson, Response response, Object result) {
        response.type("application/json");
        return gson.toJson(result);
    }

    /**
     * Serialize null json
     * @param response the http response
     * @return a JSON string
     */
    public static String jsonNull(Response response) {
        response.type("application/json");
        return GSON.toJson(null);
    }

    /**
     * Serialize the result and set the response content type to JSON.
     * @param response the http response
     * @param result the json element to serialize
     * @return a JSON string
     */
    public static String json(Response response, JsonElement result) {
        response.type("application/json");
        return GSON.toJson(result);
    }

    /**
     * Serialize the result and set the response content type to JSON.
     * @param response the http response
     * @param result the boolean to serialize
     * @return a JSON string
     */
    public static String json(Response response, boolean result) {
        response.type("application/json");
        return GSON.toJson(result);
    }

    /**
     * Serialize the result and set the response content type to JSON.
     * @param response the http response
     * @param result the int to serialize
     * @return a JSON string
     */
    public static String json(Response response, int result) {
        response.type("application/json");
        return GSON.toJson(result);
    }

    /**
     * Serialize the result and set the response content type to JSON.
     * @param response the http response
     * @param result the string to serialize
     * @return a JSON string
     */
    public static String json(Response response, String result) {
        response.type("application/json");
        return GSON.toJson(result);
    }

    /**
     * Serialize the result and set the response content type to JSON.
     * @param response the http response
     * @param result the list of strings to serialize
     * @return a JSON string
     */
    public static String json(Response response, List<String> result) {
        response.type("application/json");
        return GSON.toJson(result);
    }

    /**
     * Serialize a message
     * @param response the http response
     * @param message the message to serialize
     * @return a JSON string
     */
    public static String message(Response response, String message) {
      return json(response, Collections.singletonMap("message", message), new TypeToken<>() { });
    }

    /**
     * Serialize the result and set the response content type to JSON.
     * @param response the http response
     * @param result the list of strings to serialize
     * @param type type token needed for gson
     * @param <T> type of the result
     * @return a JSON string
     */
    public static <T> String json(Response response, T result, TypeToken<T> type) {
        response.type("application/json");
        return GSON.toJson(result, type.getType());
    }

    /**
     * Serialize the result and set the response content type to JSON.
     * @param response the http response
     * @param result the object to serialize to JSON
     * @param httpStatusCode http status code
     * @param type type token needed for gson
     * @param <T> type of the result
     * @return a JSON string
     */
    public static <T> String json(Response response, int httpStatusCode, T result, TypeToken<T> type) {
        response.type("application/json");
        response.status(httpStatusCode);
        return GSON.toJson(result, type.getType());
    }

    /**
     * Serialize the result and set the response content type to JSON.
     * @param response the http response
     * @param result the object to serialize to JSON
     * @param type type token needed for gson
     * @param <T> type of the result
     * @return a JSON string
     */
    public static <T> String result(Response response, ResultJson<T> result, TypeToken<T> type) {
        response.type("application/json");
        ParameterizedType parameterizedType = new ParameterizedTypeImpl(null, ResultJson.class, type.getType());
        return GSON.toJson(result, parameterizedType);
    }

    /**
     * Serialize the result and set the response content type to JSON.
     * @param gson {@link Gson} object to use for serialization
     * @param response the http response
     * @param result the long to serialize to JSON
     * @return a JSON string
     */
    public static String json(Gson gson, Response response, long result) {
        response.type("application/json");
        return gson.toJson(result);
    }

    /**
     * Serialize the result and set the response content type to JSON.
     * @param gson {@link Gson} object to use for serialization
     * @param response the http response
     * @param result the object to serialize to JSON
     * @param type type token needed for gson
     * @param <T> type of the result
     * @return a JSON string
     */
    public static <T> String json(Gson gson, Response response, T result, TypeToken<T> type) {
        response.type("application/json");
        return gson.toJson(result, type.getType());
    }

    /**
     * Serialize the result and set the response content type to JSON
     * and the http status code to the given value.
     * @param response the http response
     * @param httpStatusCode the http status code of the response
     * @param result the object to serialize to JSON
     * @param type type token needed for gson
     * @param <T> type of the result
     * @return a JSON string
     */
    public static <T> String result(Response response, int httpStatusCode, ResultJson<T> result, TypeToken<T> type) {
        response.type("application/json");
        response.status(httpStatusCode);
        ParameterizedType parameterizedType = new ParameterizedTypeImpl(null, ResultJson.class, type.getType());
        return GSON.toJson(result, parameterizedType);
    }

    /**
     * Serialize the result and set the response content type to JSON
     * and the http status code to bad request.
     * @param response the http response
     * @param messages messages
     * @return a JSON string
     */
    public static String badRequest(Response response, String... messages) {
        response.type("application/json");
        response.status(HttpStatus.SC_BAD_REQUEST);
        return GSON.toJson(ResultJson.error(messages));
    }

    /**
     * Serialize the result and set the response content type to JSON
     * and the http status code to not found.
     * @param response the http response
     * @param messages messages
     * @return a JSON string
     */
    public static String notFound(Response response, String... messages) {
        response.type("application/json");
        response.status(HttpStatus.SC_NOT_FOUND);
        return GSON.toJson(ResultJson.error(messages));
    }

    /**
     * Serialize the result and set the response content type to JSON
     * and the http status code to internal server error.
     * @param response the http response
     * @param messages messages
     * @return a JSON string
     */
    public static String internalServerError(Response response, String... messages) {
        response.type("application/json");
        response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        return GSON.toJson(ResultJson.error(messages));
    }

    /**
     * Serialize the result and set the response content type to JSON
     * and the http status code to forbidden.
     * @param response the http response
     * @param messages messages
     * @return a JSON string
     */
    public static String forbidden(Response response, String... messages) {
        response.type("application/json");
        response.status(HttpStatus.SC_FORBIDDEN);
        return GSON.toJson(ResultJson.error(messages));
    }

    /**
     * Serialize the result and set the response content type to JSON
     * and the http status code to forbidden.
     * @param response the http response
     * @param result the result
     * @param httpStatusCode http status code
     * @return a JSON string
     * @deprecated use the corresponding error methods instead
     */
    @Deprecated
    public static String jsonError(Response response, int httpStatusCode, String result) {
        response.type("application/json");
        response.status(httpStatusCode);
        return GSON.toJson(result);
    }

    /**
     * Serialize the result and set the response content type to JSON
     * and the http status code to the given value.
     * @param gson {@link Gson} object to use for serialization
     * @param response the http response
     * @param httpStatusCode the http status code of the response
     * @param result the object to serialize to JSON
     * @return a JSON string
     * @deprecated use methods that provide type token instead
     */
    @Deprecated
    public static String json(Gson gson, Response response, int httpStatusCode, Object result) {
        response.type("application/json");
        response.status(httpStatusCode);
        return gson.toJson(result);
    }

    /**
     * Serialize the result and set the response content type to JSON
     * and the http status code to the given value.
     * @param gson {@link Gson} object to use for serialization
     * @param response the http response
     * @param httpStatusCode the http status code of the response
     * @param result the object to serialize to JSON
     * @param type type token needed for gson
     * @param <T> type of the result
     * @return a JSON string
     */
    public static <T> String json(Gson gson, Response response, int httpStatusCode, T result, TypeToken<T> type) {
        response.type("application/json");
        response.status(httpStatusCode);
        return gson.toJson(result, type.getType());
    }

    /**
     * Serialize the result and set the response content type to JSON
     * and the http status code to the given value.
     * @param gson {@link Gson} object to use for serialization
     * @param response the http response
     * @param httpStatusCode the http status code of the response
     * @param result the object to serialize to JSON
     * @param type type token needed for gson
     * @param <T> type of the result
     * @return a JSON string
     */
    public static <T> String result(Gson gson, Response response, int httpStatusCode, ResultJson<T> result,
                                    TypeToken<T> type) {
        response.type("application/json");
        response.status(httpStatusCode);
        ParameterizedType parameterizedType = new ParameterizedTypeImpl(null, ResultJson.class, type.getType());
        return gson.toJson(result, parameterizedType);
    }
}
