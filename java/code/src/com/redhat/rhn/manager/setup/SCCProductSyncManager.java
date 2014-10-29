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
package com.redhat.rhn.manager.setup;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.frontend.events.ScheduleRepoSyncEvent;
import com.redhat.rhn.manager.content.ContentSyncException;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.content.ListedProduct;

import com.suse.manager.model.products.Channel;
import com.suse.manager.model.products.MandatoryChannels;
import com.suse.manager.model.products.OptionalChannels;
import com.suse.manager.model.products.Product;
import com.suse.mgrsync.MgrSyncChannel;
import com.suse.mgrsync.MgrSyncStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Methods for listing and synchronizing products, the SCC version.
 */
public class SCCProductSyncManager extends ProductSyncManager {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Product> getBaseProducts() throws ProductSyncException {
        ContentSyncManager csm = new ContentSyncManager();
        try {
            // Convert the listed products to objects we can display
            Collection<ListedProduct> products = csm.listProducts(csm.listChannels());
            List<Product> result = convertProducts(products);

            // Determine their product sync status separately
            for (Product p : result) {
                if (p.isProvided()) {
                    p.setSyncStatus(getProductSyncStatus(p));
                }
                else {
                    p.setStatusNotMirrored();
                }
                for (Product addon : p.getAddonProducts()) {
                    if (addon.isProvided()) {
                        addon.setSyncStatus(getProductSyncStatus(addon));
                    }
                    else {
                        addon.setStatusNotMirrored();
                    }
                }
            }
            return result;
        }
        catch (ContentSyncException e) {
            throw new ProductSyncException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addProduct(String productIdent) throws ProductSyncException {
        Product product = findProductByIdent(productIdent);
        if (product != null) {
            try {
                // Add the channels first
                ContentSyncManager csm = new ContentSyncManager();
                for (Channel channel : product.getMandatoryChannels()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Add channel: " + channel.getLabel());
                    }
                    csm.addChannel(channel.getLabel(), null);
                }

                // Trigger sync of those channels
                for (Channel channel : product.getMandatoryChannels()) {
                    ScheduleRepoSyncEvent event =
                            new ScheduleRepoSyncEvent(channel.getLabel());
                    MessageQueue.publish(event);
                }
            }
            catch (ContentSyncException ex) {
                throw new ProductSyncException(ex.getMessage());
            }
        }
        else {
            String msg = String.format("Product %s cannot be found.", productIdent);
            throw new ProductSyncException(msg);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshProducts() throws ProductSyncException {
        ContentSyncManager csm = new ContentSyncManager();
        try {
            csm.updateChannels(Config.get().getString(ContentSyncManager.MIRROR_CFG_KEY));
            csm.updateChannelFamilies(csm.readChannelFamilies());
            csm.updateSUSEProducts(csm.getProducts());
            csm.updateSUSEProductChannels(csm.getAvailableChannels(csm.readChannels()));
            csm.updateSubscriptions(csm.getSubscriptions());
            csm.updateUpgradePaths();
        }
        catch (ContentSyncException e) {
            throw new ProductSyncException(e.getLocalizedMessage());
        }
    }

    /**
     * Convert a collection of {@link ListedProduct} to a collection of {@link Product}
     * for further display.
     *
     * @param products collection of {@link ListedProduct}
     * @return List of {@link Product}
     */
    private List<Product> convertProducts(Collection<ListedProduct> products) {
        List<Product> displayProducts = new ArrayList<Product>();
        for (ListedProduct p : products) {
            if (!p.getStatus().equals(MgrSyncStatus.UNAVAILABLE)) {
                Product displayProduct = convertProduct(p);
                displayProducts.add(displayProduct);
            }
        }
        return displayProducts;
    }

    /**
     * Convert a given {@link ListedProduct} to a {@link Product} for further display.
     *
     * @param productIn instance of {@link ListedProduct}
     * @return instance of {@link Product}
     */
    private Product convertProduct(final ListedProduct productIn) {
        // Sort product channels (mandatory/optional)
        List<Channel> mandatoryChannelsOut = new ArrayList<Channel>();
        List<Channel> optionalChannelsOut = new ArrayList<Channel>();
        for (MgrSyncChannel channelIn : productIn.getChannels()) {
            MgrSyncStatus statusIn = channelIn.getStatus();
            String statusOut = statusIn.equals(MgrSyncStatus.INSTALLED) ?
                    Channel.STATUS_PROVIDED : Channel.STATUS_NOT_PROVIDED;
            Channel channelOut = new Channel(channelIn.getLabel(), statusOut);
            if (channelIn.isOptional()) {
                optionalChannelsOut.add(channelOut);
            }
            else {
                mandatoryChannelsOut.add(channelOut);
            }
        }

        // Add base channel on top of everything else so it can be added first.
        Collections.sort(mandatoryChannelsOut, new Comparator<Channel>() {
            public int compare(Channel a, Channel b) {
                return a.getLabel().equals(productIn.getBaseChannel().getLabel()) ? -1 :
                        b.getLabel().equals(productIn.getBaseChannel().getLabel()) ? 1 : 0;
            }
        });

        // Setup the product that will be displayed
        Product displayProduct = new Product(productIn.getArch(), productIn.getIdent(),
                productIn.getFriendlyName(), "", new MandatoryChannels(mandatoryChannelsOut),
                new OptionalChannels(optionalChannelsOut));

        // Set extensions as addon products
        for (ListedProduct extension : productIn.getExtensions()) {
            Product ext = convertProduct(extension);
            ext.setBaseProduct(displayProduct);
            displayProduct.getAddonProducts().add(ext);
            ext.setBaseProductIdent(displayProduct.getIdent());
        }

        return displayProduct;
    }

    /**
     * Find a product for any given ident by looking through base and their addons.
     *
     * @param ident ident of a product
     * @return the {@link Product}
     * @throws ProductSyncException in case of an error
     */
    private Product findProductByIdent(String ident) throws ProductSyncException {
        for (Product p : getBaseProducts()) {
            if (p.getIdent().equals(ident)) {
                return p;
            }
            for (Product addon : p.getAddonProducts()) {
                if (addon.getIdent().equals(ident)) {
                    return addon;
                }
            }
        }
        return null;
    }
}
