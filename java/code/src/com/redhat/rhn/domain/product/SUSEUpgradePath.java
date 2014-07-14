/**
 * Copyright (c) 2014 SUSE
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

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fromProduct == null) ? 0 : fromProduct.hashCode());
        result = prime * result + ((toProduct == null) ? 0 : toProduct.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SUSEUpgradePath other = (SUSEUpgradePath) obj;
        if (fromProduct == null) {
            if (other.fromProduct != null) {
                return false;
            }
        }
        else if (!fromProduct.equals(other.fromProduct)) {
            return false;
        }
        if (toProduct == null) {
            if (other.toProduct != null) {
                return false;
            }
        }
        else if (!toProduct.equals(other.toProduct)) {
            return false;
        }
        return true;
    }
}
