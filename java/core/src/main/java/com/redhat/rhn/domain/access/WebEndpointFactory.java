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
import org.hibernate.type.StandardBasicTypes;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.persistence.Tuple;

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
        // Select an accessible endpoint by path, http method (GET, POST, etc.) and scope (Web UI or API)
        return getSession().createNativeQuery("""
                        SELECT * FROM access.userAccessTable e
                        WHERE user_id = :user_id
                        AND e.endpoint = :endpoint
                        AND e.http_method = :http_method
                        AND e.scope = :scope
                        LIMIT 1
                        """, WebEndpoint.class)
                .addSynchronizedEntityClass(WebEndpoint.class)
                .addSynchronizedEntityClass(Namespace.class)
                .setParameter("user_id", userId)
                .setParameter("endpoint", endpoint)
                .setParameter("http_method", httpMethod)
                .setParameter("scope", scope.name())
                .uniqueResultOptional();
    }

    /**
     * Look up user access table by user ID, path and HTTP method
     * @param userId the user ID
     * @param endpoint the path of the web endpoint
     * @param httpMethod the HTTP method of the endpoint
     * @return the web endpoint if found in the user access table
     */
    public static Optional<WebEndpoint> lookupByUserIdEndpoint(Long userId, String endpoint, String httpMethod) {
        // Select an accessible endpoint by path, http method (GET, POST, etc.)
        return getSession().createNativeQuery("""
                        SELECT * FROM access.userAccessTable e
                        WHERE user_id = :user_id
                        AND e.endpoint = :endpoint
                        AND e.http_method = :http_method
                        LIMIT 1
                        """, WebEndpoint.class)
                .addSynchronizedEntityClass(WebEndpoint.class)
                .addSynchronizedEntityClass(Namespace.class)
                .setParameter("user_id", userId)
                .setParameter("endpoint", endpoint)
                .setParameter("http_method", httpMethod)
                .uniqueResultOptional();
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
        // Select an accessible endpoint by its handler class and method
        return getSession().createNativeQuery("""
                        SELECT * FROM access.userAccessTable e
                        WHERE user_id = :user_id
                        AND e.scope = :scope
                        AND e.class_method = :class_method
                        LIMIT 1
                        """, WebEndpoint.class)
                .addSynchronizedEntityClass(WebEndpoint.class)
                .addSynchronizedEntityClass(Namespace.class)
                .setParameter("user_id", userId)
                .setParameter("class_method", classMethod)
                .setParameter("scope", scope.name())
                .uniqueResultOptional();
    }

    /**
     * Get all endpoints that don't require authorization
     * @return the set of endpoints that don't require authorization
     */
    public static Set<String> getUnauthorizedWebEndpoints() {
        // Get all endpoints that don't require authorization
        // TODO: Cache
        return getSession()
                .createQuery("SELECT w.endpoint FROM WebEndpoint w WHERE w.authRequired = false ", String.class)
                .getResultStream()
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Get API handler class and methods that don't require authorization
     * @return the set of unauthorized API methods as a string of qualified class name & method
     */
    public static Set<String> getUnauthorizedApiMethods() {
        // Get API handler class and methods that don't require authorization
        // TODO: Cache
        return getSession()
                .createQuery("SELECT w.className FROM WebEndpoint w WHERE w.scope =  :scope AND w.authRequired = false ", String.class)
                .setParameter("scope", WebEndpoint.Scope.A)
                .getResultStream()
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Get a {@code Stream} of all RBAC endpoints
     * @return the {@code Stream} of all endpoints
     */
    public static Stream<WebEndpoint> getAllEndpoints() {
        return getSession().createQuery("from WebEndpoint", WebEndpoint.class).stream();
    }
}
