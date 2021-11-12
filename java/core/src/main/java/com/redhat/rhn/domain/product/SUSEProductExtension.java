/**
 * Copyright (c) 2016 SUSE LLC
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
package com.redhat.rhn.domain.product;

import com.redhat.rhn.domain.BaseDomainHelper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Class representation of SUSE product extensions.
 */
public class SUSEProductExtension extends BaseDomainHelper implements Serializable {

    private SUSEProduct baseProduct;
    private SUSEProduct extensionProduct;
    private SUSEProduct rootProduct;
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
        if (!(other instanceof SUSEProductExtension)) {
            return false;
        }
        SUSEProductExtension otherSUSEProductExtension = (SUSEProductExtension) other;

        return new EqualsBuilder()
            .append(getBaseProduct(), otherSUSEProductExtension.getBaseProduct())
            .append(getExtensionProduct(), otherSUSEProductExtension.getExtensionProduct())
            .append(getRootProduct(), otherSUSEProductExtension.getRootProduct())
            .isEquals();
    }
}
