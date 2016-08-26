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

import java.io.Serializable;

/**
 * Class representation of SUSE product extensions.
 */
public class SUSEProductExtension extends BaseDomainHelper implements Serializable {

    private SUSEProduct baseProduct;
    private SUSEProduct extensionProduct;

    /**
     * Default constructor.
     */
    public SUSEProductExtension() {
    }

    /**
     * Constructor taking two {@link SUSEProduct}s.
     * @param base original product
     * @param extension target product
     */
    public SUSEProductExtension(SUSEProduct base, SUSEProduct extension) {
        setBaseProduct(base);
        setExtensionProduct(extension);
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
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((baseProduct == null) ? 0 : baseProduct.hashCode());
        result = prime * result + ((extensionProduct == null) ?
                0 : extensionProduct.hashCode());
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        SUSEProductExtension other = (SUSEProductExtension) obj;
        if (baseProduct == null) {
            if (other.baseProduct != null) {
                return false;
            }
        }
        else if (!baseProduct.equals(other.baseProduct)) {
            return false;
        }
        if (extensionProduct == null) {
            if (other.extensionProduct != null) {
                return false;
            }
        }
        else if (!extensionProduct.equals(other.extensionProduct)) {
            return false;
        }
        return true;
    }
}
