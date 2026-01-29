/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.domain.rhnpackage;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * PackageName
 */
@Entity
@Table(name = "rhnPackageName")
public class PackageName implements Comparable<PackageName>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RHN_PKG_NAME_SEQ")
    @SequenceGenerator(name = "RHN_PKG_NAME_SEQ", sequenceName = "RHN_PKG_NAME_SEQ", allocationSize = 1)
    private Long id;

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
    protected void setId(Long i) {
        this.id = i;
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
    public String toString() {
        return new ToStringBuilder(this).append("id", getId()).append("name", getName())
                .toString();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof PackageName otherPack) {
            return new EqualsBuilder().append(this.getName(), otherPack.getName()).append(
                    this.getId(), otherPack.getId()).isEquals();
        }
        return false;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.getName()).append(this.getId())
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(PackageName o) {
        if (equals(o)) {
            return 0;
        }
        return getName().compareTo(o.getName());
    }
}
