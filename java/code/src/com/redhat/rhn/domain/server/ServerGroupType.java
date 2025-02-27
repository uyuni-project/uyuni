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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
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
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class ServerGroupType extends AbstractLabelNameHelper {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "servergroup_type_seq")
    @SequenceGenerator(name = "servergroup_type_seq", sequenceName = "rhn_servergroup_type_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id = 0L;

    @Column(name = "permanent", insertable = false, updatable = false)
    @Type(type = "yes_no")
    private boolean permanent = true;

    @Column(name = "is_base", insertable = false, updatable = false)
    @Type(type = "yes_no")
    private boolean isBase = true;

    @ManyToMany
    @JoinTable(
            name = "rhnServerGroupTypeFeature",
            joinColumns = @JoinColumn(name = "server_group_type_id"),
            inverseJoinColumns = @JoinColumn(name = "feature_id")
    )
    private Set<Feature> features = new HashSet<>();

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * @return Returns the isBase.
     */
    public boolean isBase() {
        return isBase;
    }
    /**
     * @param isBaseIn The isBase to set.
     */
    public void setIsBase(boolean isBaseIn) {
        this.isBase = isBaseIn;
    }

    /**
     * @return Returns the permanent.
     */
    public boolean isPermanent() {
        return permanent;
    }
    /**
     * @param permanentIn The permanent to set.
     */
    public void setPermanent(boolean permanentIn) {
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
