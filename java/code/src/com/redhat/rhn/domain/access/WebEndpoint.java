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
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "endpoint", schema = "access")
@NamedNativeQueries({
        // TODO: Describe queries
        @NamedNativeQuery(name = "WebEndpoint_user_access_endpoint_scope",
                query = "SELECT e.* FROM access.endpoint e JOIN access.endpointNamespace en ON e.id = en.endpoint_id " +
                    "JOIN access.userNamespace un ON en.namespace_id = un.namespace_id WHERE un.user_id = :user_id " +
                    "AND e.endpoint = :endpoint AND e.http_method = :http_method AND e.scope = :scope LIMIT 1"
        ),
        @NamedNativeQuery(name = "WebEndpoint_user_access_endpoint",
                query = "SELECT e.* FROM access.endpoint e JOIN access.endpointNamespace en ON e.id = en.endpoint_id " +
                    "JOIN access.userNamespace un ON en.namespace_id = un.namespace_id WHERE un.user_id = :user_id " +
                    "AND e.endpoint = :endpoint AND e.http_method = :http_method LIMIT 1"
        ),
        @NamedNativeQuery(name = "WebEndpoint_user_access_class_method",
                query = "SELECT e.* FROM access.endpoint e JOIN access.endpointNamespace en ON e.id = en.endpoint_id " +
                    "JOIN access.userNamespace un ON en.namespace_id = un.namespace_id WHERE un.user_id = :user_id " +
                    "AND e.class_method = :class_method AND e.scope = :scope"
        ),
        @NamedNativeQuery(name = "WebEndpoint_get_unauthenticated_webui",
                query = "SELECT e.endpoint FROM access.endpoint e WHERE authorized = false"
        ),
        @NamedNativeQuery(name = "WebEndpoint_get_unauthenticated_api",
                query = "SELECT e.class_method FROM access.endpoint e WHERE scope = 'A' AND authorized = false"
        )
})
public class WebEndpoint extends BaseDomainHelper {
    private Long id;

    private String className;
    private String endpoint;
    private String httpMethod;
    private Scope scope;
    private Boolean authorized;

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

    public WebEndpoint() {
    }

    public WebEndpoint(String classNameIn, String endpointIn, String httpMethodIn, Scope scopeIn,
                       Boolean authorizedIn) {
        className = classNameIn;
        endpoint = endpointIn;
        httpMethod = httpMethodIn;
        scope = scopeIn;
        authorized = authorizedIn;
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "endpoint_seq")
    @SequenceGenerator(name = "endpoint_seq", sequenceName = "endpoint_id_seq", schema = "access",
            allocationSize = 1)
    public Long getId() {
        return id;
    }

    public void setId(Long idIn) {
        id = idIn;
    }

    @Column(name = "class_method")
    public String getClassName() {
        return className;
    }

    public void setClassName(String classNameIn) {
        this.className = classNameIn;
    }

    @Column
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpointIn) {
        this.endpoint = endpointIn;
    }

    @Column(name = "scope")
    @Enumerated(EnumType.STRING)
    public Scope getScope() {
        return scope;
    }

    @Column(name = "scope")
    public void setScope(Scope scopeIn) {
        this.scope = scopeIn;
    }

    @Column(name = "http_method")
    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethodIn) {
        httpMethod = httpMethodIn;
    }

    @Column(name = "authorized")
    public Boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(Boolean authorizedIn) {
        authorized = authorizedIn;
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
