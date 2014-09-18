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

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.manager.content.ContentSyncException;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.content.ListedProduct;

import com.suse.manager.model.products.Channel;
import com.suse.manager.model.products.MandatoryChannels;
import com.suse.manager.model.products.OptionalChannels;
import com.suse.manager.model.products.Product;
import com.suse.mgrsync.MgrSyncChannel;
import com.suse.mgrsync.MgrSyncStatus;
import com.suse.scc.model.SCCRepository;

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
    public List<Product> getBaseProducts() throws ProductSyncManagerCommandException,
            ProductSyncManagerParseException {
        ContentSyncManager csm = new ContentSyncManager();
        try {
            Collection<ListedProduct> products = csm.listProducts(
                    csm.listChannels(csm.getRepositories()));
            return convertProducts(products);
        }
        catch (ContentSyncException e) {
            throw new ProductSyncManagerParseException(e);
        }
    }

    /**
     * Find a product for any given ident by looking through base and their addons.
     * @param ident ident of a product
     * @return the {@link Product}
     * @throws ProductSyncManagerCommandException
     * @throws ProductSyncManagerParseException
     */
    private Product findProductByIdent(String ident)
            throws ProductSyncManagerCommandException, ProductSyncManagerParseException {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void addProduct(String productIdent) throws ProductSyncManagerCommandException {
        ContentSyncManager csm = new ContentSyncManager();
        Product product = null;
        try {
            product = this.findProductByIdent(productIdent);
        } catch (ProductSyncManagerParseException ex) {
            throw new ProductSyncManagerCommandException(ex.getMessage(), -1,
                    ex.getMessage(), ex.getMessage());
        }

        if (product != null) {
            try {
                // Add the channels first
                Collection<SCCRepository> repos = csm.getRepositories();
                for (Channel channel : product.getMandatoryChannels()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Add channel: " + channel.getLabel());
                    }
                    csm.addChannel(channel.getLabel(), repos);
                }

                // Commit the transaction to make sure that the channels are there
                HibernateFactory.commitTransaction();

                // Trigger sync of those channels
                for (Channel channel : product.getMandatoryChannels()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Trigger sync: " + channel.getLabel());
                    }
                    scheduleSingleSatRepoSync(channel);
                }
            }
            catch (ContentSyncException ex) {
                throw new ProductSyncManagerCommandException(ex.getMessage(), -1,
                        ex.getMessage(), ex.getMessage());
            }
        }
        else {
            // XXX: Refactor this to use a more generic exception class here
            String msg = String.format("Product %s cannot be found.", productIdent);
            throw new ProductSyncManagerCommandException(msg, -1, msg, msg);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshProducts() throws ProductSyncManagerCommandException,
            InvalidMirrorCredentialException, ConnectionException {
        ContentSyncManager csm = new ContentSyncManager();
        try {
            csm.updateChannels(csm.getRepositories());
            csm.updateChannelFamilies(csm.readChannelFamilies());
            csm.updateSUSEProducts(csm.getProducts());
            csm.updateSUSEProductChannels(csm.getAvailableChannels(csm.readChannels()));
            csm.updateSubscriptions(csm.getSubscriptions());
            csm.updateUpgradePaths();
        }
        catch (ContentSyncException e) {
            throw new ProductSyncManagerCommandException(e.getLocalizedMessage(),
                -1, e.getMessage(), e.getMessage());
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
     * @param product instance of {@link ListedProduct}
     * @param ident a counter enumerating all products
     * @return instance of {@link Product}
     */
    private Product convertProduct(final ListedProduct product) {
        List<Channel> mandatoryChannels = new ArrayList<Channel>();
        List<Channel> optionalChannels = new ArrayList<Channel>();

        for (MgrSyncChannel mgrSyncChannel : product.getChannels()) {
            MgrSyncStatus sccStatus = mgrSyncChannel.getStatus();
            (mgrSyncChannel.isOptional() ? optionalChannels : mandatoryChannels)
                    .add(new Channel(mgrSyncChannel.getLabel(),
                            sccStatus.equals(MgrSyncStatus.INSTALLED)
                                ? Channel.STATUS_PROVIDED
                                : Channel.STATUS_NOT_PROVIDED));
        }

        // Add base channel on top of everything else so it can be added first.
        Collections.sort(mandatoryChannels, new Comparator<Channel>() {
            public int compare(Channel a, Channel b) {
                return a.getLabel().equals(product.getBaseChannel().getLabel()) ? -1 :
                       b.getLabel().equals(product.getBaseChannel().getLabel()) ? 1 : 0;
            };
        });

        Product displayProduct = new Product(product.getArch(), product.getIdent(),
                product.getFriendlyName(), "",
                new MandatoryChannels(mandatoryChannels),
                new OptionalChannels(optionalChannels));
        displayProduct.setSyncStatus(displayProduct.isProvided()
                              ? this.getProductSyncStatus(displayProduct)
                              : Product.SyncStatus.NOT_MIRRORED);

        // Set extensions as addon products, increase ident with every addon
        for (ListedProduct extension : product.getExtensions()) {
            Product ext = convertProduct(extension);
            ext.setBaseProduct(displayProduct);
            displayProduct.getAddonProducts().add(ext);
            ext.setBaseProductIdent(displayProduct.getIdent());
        }

        return displayProduct;
    }
}
