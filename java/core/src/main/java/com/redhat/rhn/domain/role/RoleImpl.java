/*
 * Copyright (c) 2025 SUSE LLC
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

package com.redhat.rhn.domain.role;

import com.redhat.rhn.domain.BaseDomainHelper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * Class Role that reflects the DB representation of RHNUSERGROUP
 * DB table: RHNUSERGROUP
 */
@Entity
@Table(name = "RHNUSERGROUPTYPE")
@Immutable // Equivalent to mutable="false"
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class RoleImpl extends BaseDomainHelper implements Role {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rhn_usergroup_type_seq")
    @SequenceGenerator(name = "rhn_usergroup_type_seq", sequenceName = "rhn_usergroup_type_seq", allocationSize = 1)
    @Column(name = "id", nullable = false, updatable = false)
    protected Long id;

    @Column(name = "name", length = 64, nullable = false)
    private String name;

    @Column(name = "label", length = 64, nullable = false)
    private String label;

    /**
     * Protected constructor
     */
    protected RoleImpl() {
    }

    /** {@inheritDoc} */
    @Override
    public Long getId() {
        return this.id;
    }

    /**
     * Setter for id
     * @param idIn the id
     */
    protected void setId(Long idIn) {
        this.id = idIn;
    }

    /** {@inheritDoc} */
    @Override
    public String getLabel() {
        return this.label;
    }

    /** {@inheritDoc} */
    @Override
    public void setLabel(String labelIn) {
        this.label = labelIn;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return this.name;
    }

    /** {@inheritDoc} */
    @Override
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "RoleImpl.label: " + label;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof RoleImpl castOther)) {
            return false;
        }
        return new EqualsBuilder().append(getName(),
                castOther.getName()).append(getLabel(),
                castOther.getLabel()).isEquals();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getName()).
            append(getLabel()).toHashCode();
    }

}
