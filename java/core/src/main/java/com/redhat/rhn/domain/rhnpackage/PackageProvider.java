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

import com.redhat.rhn.domain.BaseDomainHelper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * PackageArch
 */
@Entity
@Table(name = "rhnPackageProvider")
public class PackageProvider extends BaseDomainHelper implements Comparable<PackageProvider> {

    @Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "package_provider_seq")
	@SequenceGenerator(name = "package_provider_seq", sequenceName = "rhn_package_provider_id_seq", allocationSize = 1)
    private Long id;

    @Column
    private String name;

    @OneToMany(mappedBy = "provider", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    private Set<PackageKey> keys = new HashSet<>();

    /**
     * @return Returns the keys.
     */
    public Set<PackageKey> getKeys() {
        return keys;
    }

    /**
     * @param keysIn The keys to set.
     */
    public void setKeys(Set<PackageKey> keysIn) {
        this.keys = keysIn;
    }

    /**
     * Add a package key to this provider
     * @param key the key to add
     */
    public void addKey(PackageKey key) {
        this.getKeys().add(key);
    }

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
     *
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getName()).toHashCode();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object archIn) {
        if (archIn instanceof PackageProvider prov) {
            return new EqualsBuilder()
                    .append(this.name, prov.getName())
                    .append(getId(), prov.getId()).isEquals();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(PackageProvider o) {
        if (equals(o)) {
            return 0;
        }
        if (o == null) {
            return 1;
        }
        return getName().compareTo(o.getName());
    }
}
