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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.rhnpackage;

import com.redhat.rhn.domain.BaseDomainHelper;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * PackageCapability
 */
@Entity
@Table(name = "rhnPackageCapability")
public class PackageCapability extends BaseDomainHelper {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RHN_PKG_CAPABILITY_ID_SEQ")
    @SequenceGenerator(name = "RHN_PKG_CAPABILITY_ID_SEQ", sequenceName = "RHN_PKG_CAPABILITY_ID_SEQ",
            allocationSize = 1)
    private Long id;

    @Column
    private String name;

    @Column
    private String version;

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
     * @return Returns the version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param v The version to set.
     */
    public void setVersion(String v) {
        this.version = v;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(this.getName());
        hash.append(this.getVersion());
        return hash.toHashCode();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PackageCapability cap)) {
            return false;
        }
        return getVersion().equals(cap.getVersion()) && getName().equals(cap.getName());
    }
}
