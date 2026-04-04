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

import com.redhat.rhn.domain.org.Org;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "accessGroup", schema = "access")
public class AccessGroup implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String label;
    private String description;
    @OneToOne
    @JoinColumn(name = "org_id")
    private Org org;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "accessGroupNamespace",
            schema = "access",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "namespace_id")
    )
    private Set<Namespace> namespaces;

    /**
     * Constructs a new access group
     */
    public AccessGroup() { }

    /**
     * Constructs a new access group with the specified values.
     *
     * @param labelIn the access group label
     * @param descriptionIn the description of the namespace
     * @param orgIn the organization that the group belongs to
     */
    public AccessGroup(String labelIn, String descriptionIn, Org orgIn) {
        label = labelIn;
        description = descriptionIn;
        org = orgIn;
        namespaces = new HashSet<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long idIn) {
        id = idIn;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String labelIn) {
        label = labelIn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String descriptionIn) {
        description = descriptionIn;
    }

    public Org getOrg() {
        return org;
    }

    public void setOrg(Org orgIn) {
        org = orgIn;
    }

    public Set<Namespace> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(Set<Namespace> namespacesIn) {
        namespaces = namespacesIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (this == oIn) {
            return true;
        }
        if (oIn == null || getClass() != oIn.getClass()) {
            return false;
        }
        AccessGroup that = (AccessGroup) oIn;
        return Objects.equals(label, that.label) &&
                Objects.equals(description, that.description) &&
                Objects.equals(org, that.org);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, description, org);
    }
}
