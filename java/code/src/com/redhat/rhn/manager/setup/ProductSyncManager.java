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

import com.redhat.rhn.manager.satellite.Executor;
import com.redhat.rhn.manager.satellite.SystemCommandExecutor;

import com.suse.manager.model.products.Product;
import com.suse.manager.model.products.ProductList;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.simpleframework.xml.core.Persister;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Simple command class for interacting (listing/adding) SUSE products.
 */
public class ProductSyncManager {

    /** Product sync command command line. */
    public static final String[] PRODUCT_SYNC_COMMAND = {
        "/usr/bin/sudo",
        "/usr/sbin/mgr-ncc-sync"
    };

    /** Product sync command switch to obtain a list of products. */
    public static final String LIST_PRODUCT_SWITCH = "--list-products-xml";

    /** Product sync command switch add a product. */
    public static final String ADD_PRODUCT_SWITCH = "--add-product-by-ident";

    /**
     * Product sync command switch to refresh product, channel and subscription
     * information without triggering any reposync.
     */
    public static final String REFRESH_SWITCH = "--refresh";

    /** The logger. */
    private static Logger logger = Logger.getLogger(ProductSyncManager.class);

    /** The executor. */
    private Executor executor;

    /**
     * Default constructor.
     */
    public ProductSyncManager() {
        this(new SystemCommandExecutor());
    }

    /**
     * Executor constructor, use directly for tests.
     * @param executorIn the executor in
     */
    public ProductSyncManager(Executor executorIn) {
        executor = executorIn;
    }

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
        return runProductSyncCommand(LIST_PRODUCT_SWITCH);
    }

    /**
     * Run product sync command.
     * @param arguments the arguments
     * @return the string
     * @throws ProductSyncManagerException the product sync manager exception
     */
    public String runProductSyncCommand(String... arguments)
        throws ProductSyncManagerException {
        String[] commandLine =
                (String[]) ArrayUtils.addAll(PRODUCT_SYNC_COMMAND, arguments);
        int exitCode = executor.execute(commandLine);
        if (exitCode != 0) {
            String error = "Error while running product sync command " +
                "got exit code " + exitCode;
            logger.error(error);
            throw new ProductSyncManagerException(error);
        }
        String output = executor.getLastCommandOutput();
        logger.debug("This the output of product sync command");
        logger.debug(output);
        return output;
    }

    /**
     * Parse {@link String} and populates the internal data structure containing
     * the products hierarchy.
     * @param xml a String containing an XML description of SUSE products
     * @return a Map which has base products as keys and a list containing their
     * addon products as values
     * @throws ProductSyncManagerException if the xml cannot be parsed
     */
    public Map<Product, List<Product>> parseProducts(String xml)
        throws ProductSyncManagerException {
        Map<Product, List<Product>> productHierarchy =
                new TreeMap<Product, List<Product>>();
        Set<Product> products = getProductSet(xml);
        Map<String, Product> identProductMap = new HashMap<String, Product>();
        for (Product product : products) {
            identProductMap.put(product.getIdent(), product);
        }

        for (Product product : products) {
            if (product.isBaseProduct()) {
                productHierarchy.put(product, new LinkedList<Product>());
            }
            else {
                Product parent = identProductMap.get(product.getBaseProductIdent());
                productHierarchy.get(parent).add(product);
            }
        }

        return productHierarchy;
    }

    /**
     * Gets the product set.
     *
     * @param xml the xml
     * @return the product set
     * @throws ProductSyncManagerException the product sync manager exception
     */
    private Set<Product> getProductSet(String xml) throws ProductSyncManagerException {
        try {
            ProductList result =
                    new Persister().read(ProductList.class, IOUtils.toInputStream(xml));
            TreeSet<Product> products = new TreeSet<Product>(result.getProducts());
            return products;
        }
        catch (Exception e) {
            throw new ProductSyncManagerException(e);
        }
    }

    /**
     * Adds the product.
     * @param productIdent the product ident
     * @throws ProductSyncManagerException if the product addition failed
     */
    public void addProduct(final String productIdent) throws ProductSyncManagerException {
        runProductSyncCommand(ADD_PRODUCT_SWITCH, productIdent);
    }

    /**
     * Refresh product, channel and subscription information without triggering
     * any reposysnc.
     * @throws ProductSyncManagerException if the refresh failed
     */
    public void refreshProducts() throws ProductSyncManagerException {
        runProductSyncCommand(REFRESH_SWITCH);
    }
}
