/**
 * Copyright (c) 2012 SUSE LLC
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

import com.redhat.rhn.common.util.RpmVersionComparator;
import com.redhat.rhn.domain.server.InstalledProduct;

import com.google.gson.Gson;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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

    private Boolean isEveryChannelSynced = true;

    private String missingChannelsMessage = "";

    private String serializedProductIDs = "";

    // when calculating a target product set we ignore these products if the
    // baseproduct version is less than 15.
    // sle-manager-tools: The tools channel is treated as part of the base product
    private static final List<String> PRODUCTNAME_BLACKLIST =
            Arrays.asList("sle-manager-tools");
    /**
     * Default constructor.
     */
    public SUSEProductSet() {
    }

    /**
     * Construct a {@link SUSEProductSet} using given IDs. Loads the respective
     * {@link SUSEProduct}s from the database.
     * @param baseProductIn base product ID
     * @param addonProductsIn list of addon product IDs
     */
    public SUSEProductSet(Long baseProductIn, List<Long> addonProductsIn) {
        setBaseProduct(SUSEProductFactory.getProductById(baseProductIn));
        for (Long l : addonProductsIn) {
            addAddonProduct(SUSEProductFactory.getProductById(l));
        }
    }

    /**
     * Construct a {@link SUSEProductSet} using given SUSEProducts.
     * @param baseProductIn base product
     * @param addonProductsIn list of addon products
     */
    public SUSEProductSet(SUSEProduct baseProductIn, List<SUSEProduct> addonProductsIn) {
        setBaseProduct(baseProductIn);
        for (SUSEProduct l : addonProductsIn) {
            addAddonProduct(l);
        }
    }

    /**
     * Construct a {@link SUSEProductSet} using a set of InstalledProducts.
     * @param products list of installed products
     */
    public SUSEProductSet(Set<InstalledProduct> products) {
        for (InstalledProduct prd : products) {
            if (prd.isBaseproduct()) {
                setBaseProduct(prd.getSUSEProduct());
            }
            else {
                addAddonProduct(prd.getSUSEProduct());
            }
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
     * @param baseProductIn the baseProduct to set
     */
    public void setBaseProduct(SUSEProduct baseProductIn) {
        this.baseProduct = baseProductIn;
    }

    /**
     * Return the list of addon products.
     * @return list of addon products
     */
    public List<SUSEProduct> getAddonProducts() {
        return addonProducts;
    }

    /**
     * Set the list of addon products.
     * @param addonProductsIn the addon products to set
     */
    public void setAddonProducts(List<SUSEProduct> addonProductsIn) {
        this.addonProducts = addonProductsIn;
    }

    /**
     * Add a single add-on product.
     * @param addonProduct the addonProduct to add
     */
    public void addAddonProduct(SUSEProduct addonProduct) {
        if (addonProducts == null) {
            addonProducts = new ArrayList<SUSEProduct>();
        }
        if (addonProduct != null) {
            if (PRODUCTNAME_BLACKLIST.contains(addonProduct.getName()) &&
                    new RpmVersionComparator().compare(addonProduct.getVersion(), "15") < 0) {
                return;
            }
            addonProducts.add(addonProduct);
        }
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
     * @return list of missing channels
     */
    public List<String> getMissingChannels() {
        return missingChannels;
    }

    /**
     * Add a channel that is missing.
     * @param channelLabel label of missing channel
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
     * @param channelLabels list of missing channel labels
     */
    public void addMissingChannels(List<String> channelLabels) {
        if (missingChannels == null) {
            missingChannels = new ArrayList<String>();
        }
        missingChannels.addAll(channelLabels);
        Collections.sort(missingChannels);
    }

    /**
     * Return a flag to know if all channels are synced or not
     * @return true if all channels are synced
     */
    public Boolean getIsEveryChannelSynced() {
        return missingChannels == null || missingChannels.size() == 0;
    }

    /**
     * @param isEveryChannelSyncedIn The isEveryChannelSynced to set.
     */
    public void setIsEveryChannelSynced(Boolean isEveryChannelSyncedIn) {
        this.isEveryChannelSynced = isEveryChannelSyncedIn;
    }

    /**
     * Return a single String with channles that are not synced
     * @return the missingChannels single String
     */
    public String getMissingChannelsMessage() {
        String separator = System.getProperty("line.separator") + " - ";
        return separator + StringUtils.join(missingChannels, separator);
    }

    /**
     * @param missingChannelsMessageIn The missingChannelsMessage to set.
     */
    public void setMissingChannelsMessage(String missingChannelsMessageIn) {
        this.missingChannelsMessage = missingChannelsMessageIn;
    }

    /**
     * @return Returns the serializedProductIds.
     */
    public String getSerializedProductIDs() {
        return serializeProductIDs(getProductIDs());
    }

    /**
     * @param serializedProductIDsIn The serializedProductIDs to set.
     */
    public void setSerializedProductIDs(String serializedProductIDsIn) {
        this.serializedProductIDs = serializedProductIDsIn;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * Serialize product ids
     * @param ids the list of product ids
     * @return the serialized product ids
     */
    public static String serializeProductIDs(List<Long> ids) {
        return new Gson().toJson(ids);
    }
}
