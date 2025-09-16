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

import java.io.Serializable;
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
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "namespace", schema = "access")
public class Namespace implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String namespace;
    @ManyToMany(mappedBy = "namespaces")
    private Set<WebEndpoint> endpoints = new HashSet<>();
    @Enumerated(EnumType.STRING)
    @Column(name = "access_mode")
    private AccessMode accessMode;
    @ManyToMany(mappedBy = "namespaces")
    private Set<AccessGroup> accessGroups = new HashSet<>();
    private String description;

    public enum AccessMode {
        R("View"),
        W("Modify");

        private final String label;

        AccessMode(String labelIn) {
            this.label = labelIn;
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * Default constructor for Namespace.
     * Initializes an empty instance of Namespace.
     */
    public Namespace() {
    }

    /**
     * Constructs a new Namespace with the specified values.
     *
     * @param namespaceIn the namespace identifier
     * @param accessModeIn the access mode (View or Modify)
     * @param descriptionIn the description of the namespace
     */
    public Namespace(String namespaceIn, AccessMode accessModeIn, String descriptionIn) {
        namespace = namespaceIn;
        accessMode = accessModeIn;
        description = descriptionIn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long idIn) {
        id = idIn;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespaceIn) {
        namespace = namespaceIn;
    }

    public Set<WebEndpoint> getEndpoints() {
        return endpoints;
    }

    public AccessMode getAccessMode() {
        return accessMode;
    }

    public void setAccessMode(AccessMode accessModeIn) {
        accessMode = accessModeIn;
    }

    public Set<AccessGroup> getAccessGroups() {
        return accessGroups;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String descriptionIn) {
        description = descriptionIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (this == oIn) {
            return true;
        }
        if (oIn == null || getClass() != oIn.getClass()) {
            return false;
        }
        Namespace that = (Namespace) oIn;
        return Objects.equals(namespace, that.namespace) &&
                Objects.equals(accessMode, that.accessMode) &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, accessMode, description);
    }
}
