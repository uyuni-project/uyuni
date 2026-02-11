/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.webui.services;

import com.redhat.rhn.domain.access.Namespace;
import com.redhat.rhn.domain.access.NamespaceFactory;
import com.redhat.rhn.domain.access.WebEndpoint;
import com.redhat.rhn.domain.access.WebEndpointFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import spark.route.HttpMethod;
import spark.route.ServletRoutes;
import spark.routematch.RouteMatch;

/**
 * Utility class to verify that all Spark routes are properly mapped to corresponding RBAC endpoints.
 */
public class RbacRouteValidator {

    /**
     * Represents URI and HTTP method information for any route.
     */
    public static class RouteInfo {
        private final String uri;
        private final String httpMethod;

        /**
         * Constructs a {@code RouteInfo} object from a Spark {@link RouteMatch}.
         *
         * @param routeMatch The Spark route match containing the URI and HTTP method.
         */
        public RouteInfo(RouteMatch routeMatch) {
            this.uri = routeMatch.getMatchUri();
            this.httpMethod = routeMatch.getHttpMethod().toString().toUpperCase();
        }

        /**
         * Constructs a {@code RouteInfo} object from a {@link WebEndpoint} retrieved from the DB.
         *
         * @param endpoint The RBAC entry containing the URI (endpoint) and HTTP method.
         */
        public RouteInfo(WebEndpoint endpoint) {
            this.uri = endpoint.getEndpoint();
            this.httpMethod = endpoint.getHttpMethod().toUpperCase();
        }

        public String getUri() {
            return uri;
        }

        public String getHttpMethod() {
            return httpMethod;
        }

        @Override
        public boolean equals(Object oIn) {
            if (!(oIn instanceof RouteInfo routeInfo)) {
                return false;
            }
            return Objects.equals(uri, routeInfo.uri) && Objects.equals(httpMethod, routeInfo.httpMethod);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uri, httpMethod);
        }
    }

    private RbacRouteValidator() { }

    private static final Logger LOG = LogManager.getLogger(RbacRouteValidator.class);

    /**
     * Validates the defined Spark endpoints against the RBAC configuration in the database.
     * It checks for the following inconsistencies:
     * <ul>
     * <li>
     *     Routes defined in the Spark framework that do not have a corresponding {@link WebEndpoint} in the database.
     * </li>
     * <li>
     *     {@link WebEndpoint}s in the database that require authentication but are not associated with any
     *     {@link Namespace}.
     * </li>
     * <li>
     *     {@link Namespace}s in the database that are not accessible by any {@code AccessGroup} (excluding Sat Admin).
     * </li>
     * </ul>
     */
    public static void validateEndpoints() {
        LOG.debug("Validating RBAC data");

        // Collect endpoints defined in DB
        Map<RouteInfo, WebEndpoint> endpointMap = WebEndpointFactory.getAllEndpoints()
                .collect(Collectors.toMap(RouteInfo::new, e -> e));

        LOG.debug("Retrieved {} endpoints from DB", endpointMap.size());

        List<RouteInfo> missingRoutes = new ArrayList<>();
        List<WebEndpoint> noNamespaceEndpts = new ArrayList<>();

        // Collect endpoints registered to Spark
        ServletRoutes.get().findAll().stream()
                .filter(rm ->
                        // Filter out Spark meta-routes
                        !HttpMethod.before.equals(rm.getHttpMethod()) &&
                                !HttpMethod.after.equals(rm.getHttpMethod()) &&
                                !HttpMethod.afterafter.equals(rm.getHttpMethod()))
                .map(RouteInfo::new)
                .forEach(route -> {
                    // Validate each route against RBAC entries
                    WebEndpoint endpoint = endpointMap.get(route);

                    if (endpoint == null) {
                        // Route is not defined for RBAC
                        missingRoutes.add(route);
                    }
                    else if (endpoint.isAuthRequired() && endpoint.getNamespaces().isEmpty()) {
                        // Endpoint is not part of any namespace (inaccessible)
                        noNamespaceEndpts.add(endpoint);
                    }
                });

        if (missingRoutes.isEmpty() && noNamespaceEndpts.isEmpty()) {
            LOG.info("RBAC data validation successful.");
        }
        else {
            LOG.error("RBAC data validation failed.");

            // Collect namespaces that no group has access to (except for Sat Admin)
            List<Namespace> noAccessNamespaces = NamespaceFactory.list().stream()
                    .filter(ns -> ns.getAccessGroups().isEmpty())
                    .toList();

            logIssues(missingRoutes, noNamespaceEndpts, noAccessNamespaces);
        }
    }

    private static void logIssues(List<RouteInfo> missingRoutes, List<WebEndpoint> noNamespaceEndpts,
            List<Namespace> noAccessNamespaces) {
        boolean fatal = false;
        if (!missingRoutes.isEmpty()) {
            StringBuilder sb = new StringBuilder("Found " + missingRoutes.size() +
                    " endpoints missing RBAC mappings:");
            for (RouteInfo route : missingRoutes) {
                sb.append("\n\t").append(route.getUri())
                        .append(" [").append(route.getHttpMethod()).append("]");
            }
            LOG.error(sb::toString);
            fatal = true;
        }

        if (!noNamespaceEndpts.isEmpty()) {
            StringBuilder sb = new StringBuilder("Found " + noNamespaceEndpts.size() +
                    " endpoints missing namespace definitions:");
            for (WebEndpoint endpoint : noNamespaceEndpts) {
                sb.append("\n\t").append(endpoint.getEndpoint())
                        .append(" [").append(endpoint.getHttpMethod()).append("]");
            }
            LOG.error(sb::toString);
            fatal = true;
        }

        if (!noAccessNamespaces.isEmpty()) {
            StringBuilder sb = new StringBuilder("The following " + noAccessNamespaces.size() +
                    " namespaces can only be accessed by the satellite admin:");
            for (Namespace ns : noAccessNamespaces) {
                sb.append("\n\t").append(ns.getNamespace())
                        .append(" [").append(ns.getAccessMode().getLabel()).append("]");
            }
            LOG.warn(sb::toString);
        }

        if (fatal) {
            throw new RbacRouteValidationException();
        }
    }
}
