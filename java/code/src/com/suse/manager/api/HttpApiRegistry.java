/*
 * Copyright (c) 2022 SUSE LLC
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
package com.suse.manager.api;

import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;

import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.HandlerFactory;

import com.suse.manager.webui.controllers.login.LoginController;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import spark.Route;

/**
 * Class that registers methods of the API handlers defined in a {@link HandlerFactory} to the HTTP API
 */
public class HttpApiRegistry {

    public static final String HTTP_API_ROOT = "/manager/api/";
    private static final Logger LOG = LogManager.getLogger(HttpApiRegistry.class);
    private final HandlerFactory handlerFactory;
    private final RouteFactory routeFactory;
    private final SparkRegistrationHelper registrationHelper;

    /**
     * Constructs a registry instance with the default {@link HandlerFactory}
     */
    public HttpApiRegistry() {
        this(HandlerFactory.getDefaultHandlerFactory(), new RouteFactory(), new SparkRegistrationHelper());
    }

    /**
     * Constructs a registry instance with the specified {@link HandlerFactory}
     * @param handlerFactoryIn the handler factory
     * @param routeFactoryIn the route factory that generates the {@link Route}s
     * @param registrationHelperIn the helper that registers {@link Route}s to {@link spark.Spark}
     */
    public HttpApiRegistry(HandlerFactory handlerFactoryIn, RouteFactory routeFactoryIn,
                           SparkRegistrationHelper registrationHelperIn) {
        this.handlerFactory = handlerFactoryIn;
        this.routeFactory = routeFactoryIn;
        this.registrationHelper = registrationHelperIn;
    }

    /**
     * Registers methods of the API handlers defined in the {@link HandlerFactory} as {@link Route}s to
     * {@link spark.Spark}
     */
    public void initRoutes() {
        final int[] methodCount = {0};
        new HttpApiLoggingInvocationProcessor().register();

        handlerFactory.getKeys().forEach(namespace -> {
            BaseHandler handler = handlerFactory.getHandler(namespace).get();
            LOG.debug(MessageFormat.format("Registering API namespace {0}", namespace));

            // Group overloads into a map of method names to overloaded method lists,
            // further separating between read-write and readonly methods
            // Keys: Method name X ReadOnly flag
            // Values: Lists of methods
            Map<Pair<String, Boolean>, List<Method>> methodsByName =
                    Arrays.stream(handler.getClass().getDeclaredMethods())
                            .filter(m -> Modifier.isPublic(m.getModifiers()))
                            .filter(HttpApiRegistry::isMethodAvailable)
                            .collect(Collectors.groupingBy(
                                    m -> new ImmutablePair<>(m.getName(), m.isAnnotationPresent(ReadOnly.class))));

            methodsByName.forEach((groupKey, methodList) -> {
                String path = HTTP_API_ROOT + namespace.replace('.', '/') + '/' + groupKey.getLeft();
                Route route = routeFactory.createRoute(methodList, handler);
                if (groupKey.getRight()) {
                    registrationHelper.addGetRoute(path, route);
                }
                else {
                    registrationHelper.addPostRoute(path, route);
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Registered method {0}.{1}{2}{3}",
                            namespace,
                            groupKey.getLeft(),
                            methodList.size() > 1 ?
                                    MessageFormat.format(" ({0} overloads)", methodList.size()) : "",
                            groupKey.getRight() ? " (Read-only)" : ""));
                }
                methodCount[0] += 1;
            });
        });

        registerAuthEndpoints();
        LOG.info("Registered {} methods in {} namespaces.", methodCount[0], handlerFactory.getKeys().size());
    }

    /**
     * Contains the endpoints that should be exposed without requiring any authentication.
     * @return a Set containing the URL to the public endpoints
     */
    public static Set<String> getUnautenticatedRoutes() {
        return Set.of(
            "/rhn/manager/api/api/getVersion",
            "/rhn/manager/api/api/systemVersion",
            "/rhn/manager/api/org/createFirst"
        );
    }

    /**
     * Register login/logout endpoints from {@link LoginController} to 'auth' namespace
     */
    private void registerAuthEndpoints() {
        registrationHelper.addPostRoute(HTTP_API_ROOT + "auth/login", LoginController::login);
        registrationHelper.addPostRoute(HTTP_API_ROOT + "auth/logout", withUser(LoginController::logout));
    }

    /**
     * Returns true if the method is available to be exposed in the HTTP interface
     * @param method the method
     * @return true if the method is available
     */
    private static boolean isMethodAvailable(Method method) {
        return !(method.isAnnotationPresent(ApiIgnore.class) &&
                Arrays.asList(method.getAnnotation(ApiIgnore.class).value()).contains(ApiType.HTTP));
    }
}
