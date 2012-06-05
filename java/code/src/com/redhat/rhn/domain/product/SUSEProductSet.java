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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class representation of a set of {@link SUSEProduct}s installed on a server.
 */
public class SUSEProductSet {

    // The base product
    private SUSEProduct baseProduct;

    // List of add-on products
    private List<SUSEProduct> addonProducts = new ArrayList<SUSEProduct>();

    // A list of labels of missing channels
    private List<String> missingChannels = null;

    /**
     * Default constructor.
     */
    public SUSEProductSet() {
    }

    /**
     * Construct a {@link SUSEProductSet} using given IDs. Loads the respective
     * {@link SUSEProduct}s from the database.
     * @param baseProduct
     * @param addonProducts
     */
    public SUSEProductSet(Long baseProduct, List<Long> addonProducts) {
        setBaseProduct(SUSEProductFactory.getProductById(baseProduct));
        for (Long l : addonProducts) {
            addAddonProduct(SUSEProductFactory.getProductById(l));
        }
    }

    /**
     * Return the base product.
     * @return the baseProduct
     */
    public SUSEProduct getBaseProduct() {
        return baseProduct;
    }

    /**
     * Set the base product.
     * @param baseProduct the baseProduct to set
     */
    public void setBaseProduct(SUSEProduct baseProduct) {
        this.baseProduct = baseProduct;
    }

    /**
     * Return the list of add-on products.
     * @return the addonProducts
     */
    public List<SUSEProduct> getAddonProducts() {
        return addonProducts;
    }

    /**
     * Set the list of add-on products.
     * @param addonProducts the addonProducts to set
     */
    public void setAddonProducts(List<SUSEProduct> addonProducts) {
        this.addonProducts = addonProducts;
    }

    /**
     * Add a single add-on product.
     * @param addonProduct the addonProduct to add
     */
    public void addAddonProduct(SUSEProduct addonProduct) {
        if (addonProducts == null) {
            addonProducts = new ArrayList<SUSEProduct>();
        }
        addonProducts.add(addonProduct);
    }

    /**
     * Check if this set of products is empty.
     * @return true if there is no products.
     */
    public boolean isEmpty() {
        return baseProduct == null && addonProducts.isEmpty();
    }

    /**
     * Return IDs of all {@link SUSEProduct}s contained in this
     * {@link SUSEProductSet}, base as well as add-on products.
     * @return list of product IDs
     */
    public List<Long> getProductIDs() {
        List<Long> productIDs = new ArrayList<Long>();
        productIDs.add(baseProduct.getId());
        for (SUSEProduct p : addonProducts) {
            productIDs.add(p.getId());
        }
        return productIDs;
    }

    /**
     * Return the list of missing channels.
     */
    public List<String> getMissingChannels() {
        return missingChannels;
    }

    /**
     * Add a channel that is missing.
     * @param channelLabel
     */
    public void addMissingChannel(String channelLabel) {
        if (missingChannels == null) {
            missingChannels = new ArrayList<String>();
        }
        missingChannels.add(channelLabel);
        Collections.sort(missingChannels);
    }

    /**
     * Add a list of missing channels.
     * @param channelLabels
     */
    public void addMissingChannels(List<String> channelLabels) {
        if (missingChannels == null) {
            missingChannels = new ArrayList<String>();
        }
        missingChannels.addAll(channelLabels);
        Collections.sort(missingChannels);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[base: ");
        builder.append(baseProduct.getFriendlyName());
        if (!addonProducts.isEmpty()) {
            builder.append(", addon: ");
            int i = 0;
            for (SUSEProduct addon : addonProducts) {
                i++;
                builder.append(addon.getFriendlyName());
                if (i < addonProducts.size()) {
                    builder.append(", ");
                }
            }
        }
        builder.append("]");
        return builder.toString();
    }
}
