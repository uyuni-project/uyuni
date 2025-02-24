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

import com.redhat.rhn.domain.BaseDomainHelper;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Table;

@Entity
@Table(name = "endpoint", schema = "access")
@NamedNativeQuery(name = "WebEndpoint_user_access_endpoint_scope",
        // Select an accessible endpoint by path, http method (GET, POST, etc.) and scope (Web UI or API)
        query = "SELECT * FROM access.userAccessTable e WHERE user_id = :user_id AND e.endpoint = :endpoint " +
                "AND e.http_method = :http_method AND e.scope = :scope LIMIT 1"
)
@NamedNativeQuery(name = "WebEndpoint_user_access_endpoint",
        // Select an accessible endpoint by path, http method (GET, POST, etc.)
        query = "SELECT * FROM access.userAccessTable e WHERE user_id = :user_id AND e.endpoint = :endpoint " +
                "AND e.http_method = :http_method LIMIT 1"
)
@NamedNativeQuery(name = "WebEndpoint_user_access_class_method",
        // Select an accessible endpoint by its handler class and method
        query = "SELECT * FROM access.userAccessTable e WHERE user_id = :user_id AND e.scope = :scope " +
                "AND e.class_method = :class_method LIMIT 1"
)
@NamedNativeQuery(name = "WebEndpoint_get_unauthenticated_webui",
        // Get unauthenticated Web UI endpoints
        query = "SELECT endpoint FROM access.endpoint WHERE auth_required = false"
)
@NamedNativeQuery(name = "WebEndpoint_get_unauthenticated_api",
        // Get unauthenticated API endpoints
        query = "SELECT class_method FROM access.endpoint WHERE scope = 'A' AND auth_required = false"
)
public class WebEndpoint extends BaseDomainHelper {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "class_method")
    private String className;
    private String endpoint;
    private String httpMethod;
    @Enumerated(EnumType.STRING)
    private Scope scope;
    private Boolean authRequired;

    public enum Scope {
        A("API"),
        W("Web application");

        private final String label;

        Scope(String labelIn) {
            this.label = labelIn;
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * Default constructor for WebEndpoint.
     * Initializes an empty instance of WebEndpoint.
     */
    public WebEndpoint() {
    }

    /**
     * Constructs a new WebEndpoint with the specified values.
     *
     * @param classNameIn the name of the class associated with the endpoint
     * @param endpointIn the endpoint URL or path
     * @param httpMethodIn the HTTP method (e.g., GET, POST)
     * @param scopeIn the scope that defines where the endpoint is used (API or Web application)
     * @param authRequiredIn whether the endpoint requires authorization
     */
    public WebEndpoint(String classNameIn, String endpointIn, String httpMethodIn, Scope scopeIn,
                       Boolean authRequiredIn) {
        className = classNameIn;
        endpoint = endpointIn;
        httpMethod = httpMethodIn;
        scope = scopeIn;
        authRequired = authRequiredIn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long idIn) {
        id = idIn;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String classNameIn) {
        this.className = classNameIn;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpointIn) {
        this.endpoint = endpointIn;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scopeIn) {
        this.scope = scopeIn;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethodIn) {
        httpMethod = httpMethodIn;
    }

    public Boolean isAuthRequired() {
        return authRequired;
    }

    public void setAuthRequired(Boolean authRequiredIn) {
        authRequired = authRequiredIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (this == oIn) {
            return true;
        }
        if (oIn == null || getClass() != oIn.getClass()) {
            return false;
        }
        WebEndpoint that = (WebEndpoint) oIn;
        return Objects.equals(className, that.className) &&
                Objects.equals(endpoint, that.endpoint) &&
                Objects.equals(httpMethod, that.httpMethod) &&
                scope == that.scope;
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, endpoint, scope, httpMethod);
    }
}
