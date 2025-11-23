/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.product;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

public class ProductExtensionId implements Serializable {

    protected SUSEProduct baseProduct;
    protected SUSEProduct extensionProduct;
    protected SUSEProduct rootProduct;

    /**
     * Constructor
     */
    public ProductExtensionId() { }

    /**
     * Constructor
     * @param baseProductIn the base product
     * @param extensionProductIn the extension product
     * @param rootProductIn the root product
     */
    public ProductExtensionId(SUSEProduct baseProductIn, SUSEProduct extensionProductIn, SUSEProduct rootProductIn) {
        baseProduct = baseProductIn;
        extensionProduct = extensionProductIn;
        rootProduct = rootProductIn;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(baseProduct)
                .append(extensionProduct)
                .append(rootProduct)
                .toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ProductExtensionId otherId)) {
            return false;
        }
        return new EqualsBuilder()
                .append(baseProduct, otherId.baseProduct)
                .append(extensionProduct, otherId.extensionProduct)
                .append(rootProduct, otherId.rootProduct)
                .isEquals();
    }
}

