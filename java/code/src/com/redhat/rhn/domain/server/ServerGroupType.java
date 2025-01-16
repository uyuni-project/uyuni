/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.domain.server;

import com.redhat.rhn.domain.AbstractLabelNameHelper;
import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.manager.entitlement.EntitlementManager;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

/**
 * Class that represents the rhnServerGroupType table.
 */
@Entity
@Table(name = "rhnServerGroupType")
@Cacheable
@org.hibernate.annotations.Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_ONLY)
public class ServerGroupType extends AbstractLabelNameHelper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "label")
    private String label;

    @Column(name = "name")
    private String name;

    @Column(name = "permanent", insertable = false, updatable = false)
    private Character permanent;

    @Column(name = "is_base", insertable = false, updatable = false)
    private Character isBaseChar;

    @Column(name = "created", insertable = false, updatable = false)
    private java.sql.Timestamp created;

    @Column(name = "modified", insertable = false, updatable = false)
    private java.sql.Timestamp modified;

    @ManyToMany
    @JoinTable(
            name = "rhnServerGroupTypeFeature",
            joinColumns = @JoinColumn(name = "server_group_type_id"),
            inverseJoinColumns = @JoinColumn(name = "feature_id")
    )
    private Set<Feature> features = new HashSet<>(); // Initialize as an empty set

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long idIn) {
        this.id = idIn;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String labelIn) {
        this.label = labelIn;
    }

    public String getName() {
        return name;
    }

    public void setName(String nameIn) {
        this.name = nameIn;
    }

    public Character getPermanent() {
        return permanent;
    }

    public void setPermanent(Character permanentIn) {
        this.permanent = permanentIn;
    }

    public Character getIsBaseChar() {
        return isBaseChar;
    }

    public void setIsBaseChar(Character isBaseCharIn) {
        this.isBaseChar = isBaseCharIn;
    }

    public java.sql.Timestamp getCreated() {
        return created;
    }

    public void setCreated(java.sql.Timestamp createdIn) {
        this.created = createdIn;
    }

    public java.sql.Timestamp getModified() {
        return modified;
    }

    public void setModified(java.sql.Timestamp modifiedIn) {
        this.modified = modifiedIn;
    }

    public Set<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(Set<Feature> featuresIn) {
        this.features = featuresIn;
    }

    public boolean isBase() {
        return getIsBaseChar() != null && getIsBaseChar() == 'Y';
    }

    /**
     * Get the associated Entitlement
     * @return the Entitlement
     */
    public Entitlement getAssociatedEntitlement() {
        return EntitlementManager.getByName(this.getLabel());
    }

    @Override
    public String toString() {
        return "ServerGroupType{id=" + id + ", label='" + label + "', name='" + name + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ServerGroupType that = (ServerGroupType) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
