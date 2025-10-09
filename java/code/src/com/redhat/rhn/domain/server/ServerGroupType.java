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

import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_ONLY;

import com.redhat.rhn.domain.AbstractLabelNameHelper;
import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.manager.entitlement.EntitlementManager;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Immutable;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Class that represents the rhnServerGroupType table.
 *
 */
@Entity
@Table(name = "rhnServerGroupType")
@Immutable
@Cache(usage = READ_ONLY)
public class ServerGroupType /*extends AbstractLabelNameHelper*/ {
    @Column(updatable = false, insertable = false)
    private char permanent;
    @Column(name = "is_base", updatable = false, insertable = false)
    private char isBaseChar;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rhnServerGroupTypeFeature",
            joinColumns = @JoinColumn(name = "server_group_type_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "feature_id"))
    private Set<Feature> features = new HashSet<>();


//------------------------------------
    @Id
    @Column
    private Long id;
    @Column
    private String label;
    @Column
    private String name;


    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param i The id to set.
     */
    public void setId(Long i) {
        this.id = i;
    }

    /**
     * @return Returns the label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param l The label to set.
     */
    public void setLabel(String l) {
        this.label = l;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param n The name to set.
     */
    public void setName(String n) {
        this.name = n;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.getId())
                .append(this.getName())
                .append(this.getLabel())
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object oth) {
        if (!(oth instanceof AbstractLabelNameHelper other)) {
            return false;
        }
        return new EqualsBuilder().append(this.getId(), other.getId())
                .append(this.getName(), other.getName())
                .append(this.getLabel(), other.getLabel())
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass().getName() + " : id: " + getId();
    }
//------------------------------------



    /**
     * @return Returns the isBase.
     */
    public char getIsBaseChar() {
        return isBaseChar;
    }
    /**
     * @param isBaseCharIn The isBase to set.
     */
    public void setIsBaseChar(char isBaseCharIn) {
        this.isBaseChar = isBaseCharIn;
    }

    /**
     * @return true if this server group type is a base type, false otherwise
     */
    public boolean isBase() {
        return getIsBaseChar() == 'Y';
    }
    /**
     * @return Returns the permanent.
     */
    public char getPermanent() {
        return permanent;
    }
    /**
     * @param permanentIn The permanent to set.
     */
    public void setPermanent(char permanentIn) {
        this.permanent = permanentIn;
    }

    /**
     * @return Returns the features.
     */
    public Set<Feature> getFeatures() {
        return features;
    }

    /**
     * @param featuresIn The features to set.
     */
    public void setFeatures(Set<Feature> featuresIn) {
        features = featuresIn;
    }

    /**
     * Get the associated Entitlement
     * @return the Entitlement
     */
    public Entitlement getAssociatedEntitlement() {
        return EntitlementManager.getByName(this.getLabel());
    }
}
