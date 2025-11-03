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
package com.redhat.rhn.domain.token;

import com.redhat.rhn.domain.Identifiable;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageName;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * TokenPackage
 */
@Entity
@Table(name = "rhnRegTokenPackages")
public class TokenPackage implements Identifiable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reg_tok_pkg_seq")
    @SequenceGenerator(name = "reg_tok_pkg_seq", sequenceName = "rhn_reg_tok_pkg_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "token_id")
    private Token token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "name_id")
    private PackageName packageName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arch_id")
    private PackageArch packageArch;

    /**
     * @return Returns the id.
     */
    @Override
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
     * @return Returns the token.
     */
    public Token getToken() {
        return token;
    }

    /**
     * @param t The token to set.
     */
    public void setToken(Token t) {
        this.token = t;
    }

    /**
     * @return Returns the package name.
     */
    public PackageName getPackageName() {
        return packageName;
    }

    /**
     * @param n The package name to set.
     */
    public void setPackageName(PackageName n) {
        this.packageName = n;
    }

    /**
     * @return Returns the package name.
     */
    public PackageArch getPackageArch() {
        return packageArch;
    }

    /**
     * @param a The package arch to set.
     */
    public void setPackageArch(PackageArch a) {
        this.packageArch = a;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("id", getId())
               .append("token", getToken())
               .append("packageName", getPackageName());

        if (this.getPackageArch() != null) {
            builder.append("packageArch", getPackageArch());
        }
        return builder.toString();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {

        if (!(other instanceof TokenPackage otherPack)) {
            return false;
        }

        EqualsBuilder builder = new EqualsBuilder();
        builder.append(getToken(), otherPack.getToken());
        builder.append(getPackageName(), otherPack.getPackageName());
        builder.append(getPackageArch(), otherPack.getPackageArch());

        return builder.isEquals();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {

        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getToken());
        builder.append(getPackageName());
        builder.append(getPackageArch());

        return builder.toHashCode();
    }
}
