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

import com.redhat.rhn.manager.content.ContentSyncException;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.content.ListedProduct;
import com.suse.manager.model.products.Channel;
import com.suse.manager.model.products.MandatoryChannels;
import com.suse.manager.model.products.OptionalChannels;
import com.suse.manager.model.products.Product;
import com.suse.manager.model.products.Product.SyncStatus;
import com.suse.mgrsync.MgrSyncChannel;
import com.suse.mgrsync.MgrSyncStatus;
import com.suse.scc.model.SCCRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author duncan
 */
public class SCCProductSyncManager extends ProductSyncManager {

    public List<Product> getBaseProducts() throws ProductSyncManagerCommandException, ProductSyncManagerParseException {
        ContentSyncManager csm = new ContentSyncManager();
        try {
        Collection<ListedProduct> products = csm.listProducts(
            csm.listChannels(csm.getRepositories()));
            return ncc2scc(products);
        }
        catch (ContentSyncException e) {
            throw new ProductSyncManagerParseException(e);
        }
    }

    private Product findProductByIdent(String ident)
            throws ProductSyncManagerCommandException,
                   ProductSyncManagerParseException {
        for (Product p : this.getBaseProducts()) {
            if (p.getIdent().equals(ident)
                && (p.getSyncStatus() != Product.SyncStatus.FINISHED)) {
                return p;
            }
        }

        return null;
    }

    public void addProduct(String productIdent) throws ProductSyncManagerCommandException {
        ContentSyncManager csm = new ContentSyncManager();
        Collection<SCCRepository> repos = csm.getRepositories();
        Product product = null;
        try {
            product = this.findProductByIdent(productIdent);
        } catch (ProductSyncManagerParseException ex) {
            throw new ProductSyncManagerCommandException(ex.getMessage(), -1,
                    ex.getMessage(), ex.getMessage());
        }

        if (product != null) {
            try {
                for (Channel mandatoryCh : product.getMandatoryChannels()) {
                    csm.addChannel(mandatoryCh.getLabel(), repos);
                    // Schedule reposync here
                }
            } catch (ContentSyncException ex) {
                throw new ProductSyncManagerCommandException(ex.getMessage(), -1,
                        ex.getMessage(), ex.getMessage());
            }
        }
        else {
            // Cry "Product cannot be installed"
        }
    }

    public void refreshProducts() throws ProductSyncManagerCommandException, InvalidMirrorCredentialException,
        ConnectionException {
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
                -1, e.getCause().getMessage(), e.getMessage());
        }
    }

    /**
     * Convert a collection of {@link ListedProduct} to a collection of {@link Product}
     * for further display.
     *
     * @param products
     * @return List of {@link Product}
     */
    private List<Product> ncc2scc(Collection<ListedProduct> products) {
        List<Product> sccProducts = new ArrayList<Product>();
        for (ListedProduct lp : products) {
            sccProducts.add(ncc2scc(lp));
        }
        return sccProducts;
    }

    /**
     * Convert a collection of {@link ListedProduct} to a collection of {@link Product}
     * for further display.
     *
     * @param products
     * @return List of {@link Product}
     */
    private Product ncc2scc(ListedProduct lp) {
        List<Channel> mandatoryChannels = new ArrayList<Channel>();
        List<Channel> optionalChannels = new ArrayList<Channel>();

        for (MgrSyncChannel mgrSyncChannel : lp.getChannels()) {
            MgrSyncStatus sccStatus = mgrSyncChannel.getStatus();
            String status = sccStatus.equals(MgrSyncStatus.INSTALLED)
                ? Channel.STATUS_PROVIDED : Channel.STATUS_NOT_PROVIDED;
            (mgrSyncChannel.isOptional()
                    ? optionalChannels
                    : mandatoryChannels).add(new Channel(mgrSyncChannel.getLabel(), status));
        }

        String identifier = "product-" + lp.getId();
        Product product = new Product(lp.getArch(), identifier,
                lp.getFriendlyName(), "",
                new MandatoryChannels(mandatoryChannels),
                new OptionalChannels(optionalChannels));
        if (product.isProvided()) {
            product.setSyncStatus(getProductSyncStatus(product));
        }
        else {
            product.setSyncStatus(Product.SyncStatus.NOT_MIRRORED);
        }

        // set extensions as addon products
        for (ListedProduct lpExt : lp.getExtensions()) {
            Product ext = ncc2scc(lpExt);
            ext.setBaseProduct(product);
            product.getAddonProducts().add(ext);
            ext.setBaseProductIdent(product.getIdent());
        }
        return product;
    }
}
