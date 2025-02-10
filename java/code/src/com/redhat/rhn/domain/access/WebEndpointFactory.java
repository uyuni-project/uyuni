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

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class WebEndpointFactory extends HibernateFactory {

    private static final Logger log = LogManager.getLogger(WebEndpointFactory.class);

    private WebEndpointFactory() {
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    public static List<WebEndpoint> lookupByUserId(Long userId) {
        if (userId == null) {
            return new LinkedList<>();
        }
        NativeQuery<WebEndpoint> query = getSession().getNamedNativeQuery("WebEndpoint_userEndpoints");
        query.setParameter("user_id", userId);
        return query.getResultList();
    }

    public static Optional<WebEndpoint> lookupByUserIdEndpointScope(Long userId, String endpoint, String httpMethod, WebEndpoint.Scope scope) {
        NativeQuery<WebEndpoint> query = getSession().getNamedNativeQuery("WebEndpoint_user_access_endpoint_scope");
        query.setParameter("user_id", userId);
        query.setParameter("endpoint", endpoint);
        query.setParameter("http_method", httpMethod);
        query.setParameter("scope", scope.name());
        return query.uniqueResultOptional();
    }

    public static Optional<WebEndpoint> lookupByUserIdEndpoint(Long userId, String endpoint, String httpMethod) {
        NativeQuery<WebEndpoint> query = getSession().getNamedNativeQuery("WebEndpoint_user_access_endpoint");
        query.setParameter("user_id", userId);
        query.setParameter("endpoint", endpoint);
        query.setParameter("http_method", httpMethod);
        return query.uniqueResultOptional();
    }

    public static Optional<WebEndpoint> lookupByUserIdClassMethodScope(Long userId, String classMethod, WebEndpoint.Scope scope) {
        NativeQuery<WebEndpoint> query = getSession().getNamedNativeQuery("WebEndpoint_user_access_class_method");
        query.setParameter("user_id", userId);
        query.setParameter("class_method", classMethod);
        query.setParameter("scope", scope.name());
        return query.uniqueResultOptional();
    }

    public static Set<String> getUnauthorizedWebEndpoints() {
        // TODO: Cache
        NativeQuery<String> query = getSession().getNamedNativeQuery("WebEndpoint_get_unauthenticated_webui");
        return query.getResultStream().collect(Collectors.toUnmodifiableSet());
    }

    public static Set<String> getUnauthorizedApiMethods() {
        // TODO: Cache
        NativeQuery<String> query = getSession().getNamedNativeQuery("WebEndpoint_get_unauthenticated_api");
        return query.getResultStream().collect(Collectors.toUnmodifiableSet());
    }

    public static List<WebEndpoint> lookupAll() {
        return getSession().createQuery("FROM WebEndpoint").list();
    }

    public static Optional<WebEndpoint> lookupByEndpoint(String endpoint) {
        if (endpoint == null) {
            return Optional.empty();
        }
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<WebEndpoint> select = builder.createQuery(WebEndpoint.class);
        Root<WebEndpoint> root = select.from(WebEndpoint.class);
        select.where(builder.equal(root.get("endpoint"), endpoint));

        return getSession().createQuery(select).uniqueResultOptional();
    }

    public static List<WebEndpoint> lookupByScope(WebEndpoint.Scope scope) {
        if (scope == null) {
            return new LinkedList<>();
        }
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<WebEndpoint> select = builder.createQuery(WebEndpoint.class);
        Root<WebEndpoint> root = select.from(WebEndpoint.class);
        select.where(builder.equal(root.get("scope"), scope));

        return getSession().createQuery(select).getResultList();
    }
}
