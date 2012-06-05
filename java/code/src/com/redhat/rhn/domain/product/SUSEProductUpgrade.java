package com.redhat.rhn.domain.product;

import java.io.Serializable;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.action.dup.DistUpgradeActionDetails;

public class SUSEProductUpgrade extends BaseDomainHelper implements Serializable {

    private static final long serialVersionUID = 1865254811004902667L;

    private DistUpgradeActionDetails details;
    private SUSEProduct fromProduct;
    private SUSEProduct toProduct;

    /**
     * Constructor taking two {@link SUSEProduct}s.
     */
    public SUSEProductUpgrade(SUSEProduct from, SUSEProduct to) {
        setFromProduct(from);
        setToProduct(to);
    }

    /**
     * @return the fromProduct
     */
    public SUSEProduct getFromProduct() {
        return fromProduct;
    }
    /**
     * @param fromProduct the fromProduct to set
     */
    public void setFromProduct(SUSEProduct fromProduct) {
        this.fromProduct = fromProduct;
    }
    /**
     * @return the toProduct
     */
    public SUSEProduct getToProduct() {
        return toProduct;
    }
    /**
     * @param toProduct the toProduct to set
     */
    public void setToProduct(SUSEProduct toProduct) {
        this.toProduct = toProduct;
    }
    /**
     * @return the details
     */
    public DistUpgradeActionDetails getDetails() {
        return details;
    }
    /**
     * @param details the details to set
     */
    public void setDetails(DistUpgradeActionDetails details) {
        this.details = details;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((details == null) ? 0 : details.hashCode());
        result = prime * result
                + ((fromProduct == null) ? 0 : fromProduct.hashCode());
        result = prime * result
                + ((toProduct == null) ? 0 : toProduct.hashCode());
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
