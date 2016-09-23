/**
 * Copyright (c) 2014 SUSE LLC
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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Class representation of a SUSE product upgrade.
 */
public class SUSEUpgradePath extends BaseDomainHelper implements Serializable {

    private SUSEProduct fromProduct;
    private SUSEProduct toProduct;

    /**
     * Default constructor.
     */
    public SUSEUpgradePath() {
    }

    /**
     * Constructor taking two {@link SUSEProduct}s.
     * @param from original product
     * @param to target product
     */
    public SUSEUpgradePath(SUSEProduct from, SUSEProduct to) {
        setFromProduct(from);
        setToProduct(to);
    }

    /**
     * @return the original product
     */
    public SUSEProduct getFromProduct() {
        return fromProduct;
    }

    /**
     * @param fromProductIn the original product to set
     */
    public void setFromProduct(SUSEProduct fromProductIn) {
        this.fromProduct = fromProductIn;
    }

    /**
     * @return the toProduct
     */
    public SUSEProduct getToProduct() {
        return toProduct;
    }

    /**
     * @param toProductIn the target product to set
     */
    public void setToProduct(SUSEProduct toProductIn) {
        this.toProduct = toProductIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getFromProduct())
                .append(getToProduct())
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof SUSEUpgradePath)) {
            return false;
        }
        SUSEUpgradePath otherSUSEUpgradePath = (SUSEUpgradePath) other;

        return new EqualsBuilder()
            .append(getFromProduct(), otherSUSEUpgradePath.getFromProduct())
            .append(getToProduct(), otherSUSEUpgradePath.getToProduct())
            .isEquals();
    }
}
