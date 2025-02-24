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

package com.redhat.rhn.domain.access;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.query.NativeQuery;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class WebEndpointFactory extends HibernateFactory {

    private static final Logger LOG = LogManager.getLogger(WebEndpointFactory.class);

    private WebEndpointFactory() {
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * Look up user access table by user ID, path, HTTP method and scope (Web UI or API)
     * @param userId the user ID
     * @param endpoint the path of the web endpoint
     * @param httpMethod the HTTP method of the endpoint
     * @param scope the scope of the endpoint (Web UI or API)
     * @return the web endpoint if found in the user access table
     */
    public static Optional<WebEndpoint> lookupByUserIdEndpointScope(Long userId, String endpoint, String httpMethod,
                                                                    WebEndpoint.Scope scope) {
        NativeQuery<WebEndpoint> query = getSession().getNamedNativeQuery("WebEndpoint_user_access_endpoint_scope");
        query.setParameter("user_id", userId);
        query.setParameter("endpoint", endpoint);
        query.setParameter("http_method", httpMethod);
        query.setParameter("scope", scope.name());
        return query.uniqueResultOptional();
    }

    /**
     * Look up user access table by user ID, path and HTTP method
     * @param userId the user ID
     * @param endpoint the path of the web endpoint
     * @param httpMethod the HTTP method of the endpoint
     * @return the web endpoint if found in the user access table
     */
    public static Optional<WebEndpoint> lookupByUserIdEndpoint(Long userId, String endpoint, String httpMethod) {
        NativeQuery<WebEndpoint> query = getSession().getNamedNativeQuery("WebEndpoint_user_access_endpoint");
        query.setParameter("user_id", userId);
        query.setParameter("endpoint", endpoint);
        query.setParameter("http_method", httpMethod);
        return query.uniqueResultOptional();
    }

    /**
     * Look up user access table by user ID, class method and scope
     * @param userId the user ID
     * @param classMethod the class and method name of the handler method
     * @param scope the scope of the endpoint (Web UI or API)
     * @return the web endpoint if found in the user access table
     */
    public static Optional<WebEndpoint> lookupByUserIdClassMethodScope(Long userId, String classMethod,
                                                                       WebEndpoint.Scope scope) {
        NativeQuery<WebEndpoint> query = getSession().getNamedNativeQuery("WebEndpoint_user_access_class_method");
        query.setParameter("user_id", userId);
        query.setParameter("class_method", classMethod);
        query.setParameter("scope", scope.name());
        return query.uniqueResultOptional();
    }

    /**
     * Get all unauthorized web UI endpoints
     * @return the set of unauthorized web endpoints
     */
    public static Set<String> getUnauthorizedWebEndpoints() {
        // TODO: Cache
        NativeQuery<String> query = getSession().getNamedNativeQuery("WebEndpoint_get_unauthenticated_webui");
        return query.getResultStream().collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Get all unauthorized API methods
     * @return the set of unauthorized API methods as a string of qualified class name & method
     */
    public static Set<String> getUnauthorizedApiMethods() {
        // TODO: Cache
        NativeQuery<String> query = getSession().getNamedNativeQuery("WebEndpoint_get_unauthenticated_api");
        return query.getResultStream().collect(Collectors.toUnmodifiableSet());
    }
}
