/**
 * Copyright (c) 2012 Novell
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

import java.io.Serializable;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.action.dup.DistUpgradeActionDetails;

/**
 * Class representation of a SUSE product upgrade.
 */
public class SUSEProductUpgrade extends BaseDomainHelper implements Serializable {

    private static final long serialVersionUID = 1865254811004902667L;

    private DistUpgradeActionDetails details;
    private SUSEProduct fromProduct;
    private SUSEProduct toProduct;

    /**
     * Constructor taking two {@link SUSEProduct}s.
     * @param from original product
     * @param to target product
     */
    public SUSEProductUpgrade(SUSEProduct from, SUSEProduct to) {
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
     * @return the details
     */
    public DistUpgradeActionDetails getDetails() {
        return details;
    }

    /**
     * @param detailsIn the action details to set
     */
    public void setDetails(DistUpgradeActionDetails detailsIn) {
        this.details = detailsIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((details == null) ? 0 : details.hashCode());
        result = prime * result +
                ((fromProduct == null) ? 0 : fromProduct.hashCode());
        result = prime * result +
                ((toProduct == null) ? 0 : toProduct.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SUSEProductUpgrade)) {
            return false;
        }
        SUSEProductUpgrade other = (SUSEProductUpgrade) obj;
        if (details == null) {
            if (other.details != null) {
                return false;
            }
        }
        else if (!details.equals(other.details)) {
            return false;
        }
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
