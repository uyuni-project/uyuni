/*
 * Copyright (c) 2016--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.redhat.rhn.domain.product;

import com.redhat.rhn.domain.BaseDomainHelper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Class representation of SUSE product extensions.
 */
@Entity
@Table(name = "suseProductExtension")
@IdClass(ProductExtensionId.class)
public class SUSEProductExtension extends BaseDomainHelper implements Serializable {

    @Id
    @ManyToOne
    @JoinColumn(name = "base_pdid")
    private SUSEProduct baseProduct;

    @Id
    @ManyToOne
    @JoinColumn(name = "ext_pdid")
    private SUSEProduct extensionProduct;

    @Id
    @ManyToOne
    @JoinColumn(name = "root_pdid")
    private SUSEProduct rootProduct;

    @Column
    @Type(type = "yes_no")
    private boolean recommended = false;

    /**
     * Default constructor.
     */
    public SUSEProductExtension() {
    }

    /**
     * Constructor taking two {@link SUSEProduct}s.
     * @param base original product
     * @param extension target product
     * @param root root product
     * @param recommendedIn recommended
     */
    public SUSEProductExtension(SUSEProduct base, SUSEProduct extension, SUSEProduct root, boolean recommendedIn) {
        setBaseProduct(base);
        setExtensionProduct(extension);
        setRootProduct(root);
        setRecommended(recommendedIn);
    }

    /**
     * @return the base product
     */
    public SUSEProduct getBaseProduct() {
        return baseProduct;
    }

    /**
     * @param baseProductIn the base product to set
     */
    public void setBaseProduct(SUSEProduct baseProductIn) {
        this.baseProduct = baseProductIn;
    }

    /**
     * @return the extensionProduct
     */
    public SUSEProduct getExtensionProduct() {
        return extensionProduct;
    }

    /**
     * @param extensionProductIn the extension product to set
     */
    public void setExtensionProduct(SUSEProduct extensionProductIn) {
        this.extensionProduct = extensionProductIn;
    }

    /**
     * @return Returns the rootProduct.
     */
    public SUSEProduct getRootProduct() {
        return rootProduct;
    }

    /**
     * @param rootProductIn The rootProduct to set.
     */
    public void setRootProduct(SUSEProduct rootProductIn) {
        this.rootProduct = rootProductIn;
    }

    /**
     * @return Returns the recommended.
     */
    public boolean isRecommended() {
        return recommended;
    }

    /**
     * @param recommendedIn The recommended to set.
     */
    public void setRecommended(boolean recommendedIn) {
        this.recommended = recommendedIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getBaseProduct())
                .append(getExtensionProduct())
                .append(getRootProduct())
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SUSEProductExtension otherSUSEProductExtension)) {
            return false;
        }
        return new EqualsBuilder()
            .append(getBaseProduct(), otherSUSEProductExtension.getBaseProduct())
            .append(getExtensionProduct(), otherSUSEProductExtension.getExtensionProduct())
            .append(getRootProduct(), otherSUSEProductExtension.getRootProduct())
            .isEquals();
    }
}
