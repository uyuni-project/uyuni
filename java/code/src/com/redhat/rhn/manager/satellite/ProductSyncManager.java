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
package com.redhat.rhn.manager.satellite;

import com.suse.manager.model.products.Product;
import com.suse.manager.model.products.ProductList;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.simpleframework.xml.core.Persister;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Simple command class for interacting (listing/adding) SUSE products.
 */
public class ProductSyncManager {

    private static Logger logger = Logger.getLogger(ProductSyncManager.class);

    private static final String[] LIST_PRODUCT_COMMAND = {
        "/usr/bin/sudo", "/usr/sbin/mgr-ncc-sync", "--list-products-xml"
    };

    /**
     * Gets the product hierarchy.
     * @return a Map which has base products as keys and a list containing
     *          their addon products as values
     * @throws ProductSyncManagerException if external commands or parsing fail
     */
    public Map<Product, List<Product>> getProductsHierarchy()
        throws ProductSyncManagerException {
        return this.parseProducts(readProducts());
    }

    /**
     * Invoke external commands which list all the available SUSE products.
     * @return a String containing the XML description of the SUSE products
     * @throws ProductSyncManagerException if external commands fail
     */
    public String readProducts() throws ProductSyncManagerException {
        Executor executor = new SystemCommandExecutor();

        int exitCode = executor.execute(LIST_PRODUCT_COMMAND);
        if (exitCode != 0) {
            String error = "Error while listing products with mgr-ncc-sync, " +
                "got exit code " + exitCode;
            logger.error(error);
            throw new ProductSyncManagerException(error);
        }
        String xml = executor.getLastCommandOutput();
        logger.debug("This the output of mgr-ncc-sync");
        logger.debug(xml);

        return xml;
    }

    /**
     * Parse {@link String} and populates the internal data structure containing
     * the products hierarchy
     * @param xml a String containing an XML description of SUSE products
     * @return a Map which has base products as keys and a list containing
     *          their addon products as values
     * @throws ProductSyncManagerException if external commands fail
     */
    public Map<Product, List<Product>> parseProducts(String xml)
        throws ProductSyncManagerException {
        Map<Product, List<Product>> productHierarchy =
                new TreeMap<Product, List<Product>>();
        try {
            ProductList result = new Persister().read(
                ProductList.class, IOUtils.toInputStream(xml));
            List<Product> products = result.getProducts();
            Map<String, Product> identProductMap = new HashMap<String, Product>();
            for (Product product : products) {
                identProductMap.put(product.getIdent(), product);
            }

            for (Product product : products) {
                if (product.getBaseProductIdent().isEmpty()) {
                    if (!productHierarchy.containsKey(product)) {
                        productHierarchy.put(product, new LinkedList<Product>());
                    }
                }
                else {
                    Product parent = identProductMap.get(product.getBaseProductIdent());
                    if (!productHierarchy.containsKey(parent)) {
                        productHierarchy.put(parent, new LinkedList<Product>());
                    }
                    productHierarchy.get(parent).add(product);
                }
            }

            return productHierarchy;
        }
        catch (Exception e) {
            throw new ProductSyncManagerException(e);
        }
    }
}
