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
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * PackageArch
 */
@Entity
@Table(name = "rhnPackageKey")
public class PackageKey extends BaseDomainHelper implements Comparable<PackageKey> {

    @Id
    @GeneratedValue(generator = "pkey_seq")
    @GenericGenerator(
            name = "pkey_seq",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "rhn_pkey_id_seq"),
                    @Parameter(name = "increment_size", value = "1")
            })
    private Long id;

    @Column(name = "key_id")
    private String key;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private PackageProvider provider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "key_type_id")
    private PackageKeyType type;

    /**
     * @return Returns the type.
     */
    public PackageKeyType getType() {
        return type;
    }

    /**
     * @param typeIn The type to set.
     */
    public void setType(PackageKeyType typeIn) {
        this.type = typeIn;
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
    public String getKey() {
        return key;
    }

    /**
     * @param n The name to set.
     */
    public void setKey(String n) {
        this.key = n;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getKey()).toHashCode();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof PackageKey pk) {
            return new EqualsBuilder()
                    .append(this.key, pk.getKey())
                    .append(getId(), pk.getId()).isEquals();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(PackageKey o) {
        if (equals(o)) {
            return 0;
        }
        if (o == null) {
            return 1;
        }
        return getKey().compareTo(o.getKey());
    }

    /**
     * gets the provider associated with the gpg key
     * @return the provider
     */
    public PackageProvider getProvider() {
        return provider;
    }

    /**
     * sets the provider
     * @param providerIn the provider
     */
    public void setProvider(PackageProvider providerIn) {
        this.provider = providerIn;
    }
}
