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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.domain.access;

import com.redhat.rhn.domain.BaseDomainHelper;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "endpoint", schema = "access")
public class WebEndpoint extends BaseDomainHelper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_method")
    private String className;

    @Column
    private String endpoint;

    @Column(name = "http_method")
    private String httpMethod;

    @ManyToMany
    @JoinTable(
            name = "endpointNamespace",
            schema = "access",
            joinColumns = @JoinColumn(name = "endpoint_id"),
            inverseJoinColumns = @JoinColumn(name = "namespace_id")
    )
    private Set<Namespace> namespaces = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private Scope scope;

    @Column(name = "auth_required")
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

    public Set<Namespace> getNamespaces() {
        return namespaces;
    }

    public boolean isAuthRequired() {
        return authRequired;
    }

    public void setAuthRequired(boolean authRequiredIn) {
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
