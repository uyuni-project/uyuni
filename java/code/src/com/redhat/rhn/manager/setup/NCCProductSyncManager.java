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
import org.simpleframework.xml.core.Persister;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author duncan
 */
public class NCCProductSyncManager extends ProductSyncManager {

   /** Product sync command command line. */
    public static final String[] PRODUCT_SYNC_COMMAND = {
        "/usr/bin/sudo",
        "/usr/sbin/mgr-ncc-sync"
    };

    /** Product sync command switch to obtain a list of products. */
    public static final String LIST_PRODUCT_SWITCH = "--list-products-xml";

    /** Product sync command switch add a product. */
    public static final String ADD_PRODUCT_SWITCH = "--add-product-by-ident";

    /** String returned by the sync command if there is any invalid mirror credential. */
    private static final String INVALID_MIRROR_CREDENTIAL_ERROR = "HTTP error code 401";

    /** String returned by the sync command if there is any invalid mirror credential. */
    private static final String COULD_NOT_CONNECT_ERROR = "connection failed.";

    /**
     * Product sync command switch to refresh product, channel and subscription
     * information without triggering any reposync.
     */
    public static final String REFRESH_SWITCH = "--refresh";

    /** The executor. */
    private Executor executor;

    /**
     * Default constructor.
     */
    public NCCProductSyncManager() {
        this(new SystemCommandExecutor());
    }

    /**
     * Executor constructor, use directly for tests.
     * @param executorIn the executor in
     */
    public NCCProductSyncManager(Executor executorIn) {
        executor = executorIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Product> getBaseProducts()
        throws ProductSyncManagerCommandException, ProductSyncManagerParseException {
        return parseBaseProducts(readProducts());
    }

     /**
     * Invoke external commands which list all the available SUSE products.
     * @return a String containing the XML description of the SUSE products
     * @throws ProductSyncManagerCommandException if external commands fail
     */
    public String readProducts() throws ProductSyncManagerCommandException {
        return runProductSyncCommand(LIST_PRODUCT_SWITCH);
    }

    /**
     * Run product sync command.
     * @param arguments the arguments
     * @return the string
     * @throws ProductSyncManagerCommandException the product sync manager exception
     */
    public String runProductSyncCommand(String... arguments)
        throws ProductSyncManagerCommandException {
        String[] commandLine =
                (String[]) ArrayUtils.addAll(PRODUCT_SYNC_COMMAND, arguments);
        int exitCode = executor.execute(commandLine);
        String output = executor.getLastCommandOutput();
        String errorMessage = executor.getLastCommandErrorMessage();
        if (exitCode != 0) {
            String message = "Error while running product sync command: " +
                    ArrayUtils.toString(commandLine);
            throw new ProductSyncManagerCommandException(message, exitCode, output,
                    errorMessage);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("This the output of product sync command:");
            logger.trace(output);
        }

        return output;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addProduct(String productIdent) throws ProductSyncManagerCommandException {
        runProductSyncCommand(ADD_PRODUCT_SWITCH, productIdent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshProducts()
        throws ProductSyncManagerCommandException, InvalidMirrorCredentialException,
        ConnectionException {
        try {
            runProductSyncCommand(REFRESH_SWITCH);
        }
        catch (ProductSyncManagerCommandException e) {
            if (e.getErrorCode() == 1) {
                if (e.getCommandErrorMessage().contains(INVALID_MIRROR_CREDENTIAL_ERROR)) {
                    throw new InvalidMirrorCredentialException();
                }
                if (e.getCommandErrorMessage().contains(COULD_NOT_CONNECT_ERROR)) {
                    throw new ConnectionException();
                }
            }
            throw e;
        }
    }

    /**
     * Parses an XML string into an ordered Set of products, does not handle
     * base/addon relationships.
     * @param xml the xml
     * @return the product set
     * @throws ProductSyncManagerParseException the product sync manager exception
     */
    private Set<Product> parsePlainProducts(String xml)
        throws ProductSyncManagerParseException {
        try {
            ProductList result =
                    new Persister().read(ProductList.class, IOUtils.toInputStream(xml));
            TreeSet<Product> products = new TreeSet<Product>(result.getProducts());
            return products;
        }
        catch (Exception e) {
            throw new ProductSyncManagerParseException(e);
        }
    }

    /**
     * Returns a list of base products from an XML string.
     * @param xml a String containing an XML description of SUSE products
     * @return list of parsed base products
     * @throws ProductSyncManagerParseException if the xml cannot be parsed
     */
    public List<Product> parseBaseProducts(String xml)
        throws ProductSyncManagerParseException {
        List<Product> result = new LinkedList<Product>();
        Set<Product> products = parsePlainProducts(xml);

        // associates ident codes to parsed product objects
        Map<String, Product> identProductMap = new HashMap<String, Product>();
        for (Product product : products) {
            identProductMap.put(product.getIdent(), product);
        }

        for (Product product : products) {
            if (product.isBase()) {
                result.add(product);
            }
            else {
                Product parent = identProductMap.get(product.getBaseProductIdent());
                product.setBaseProduct(parent);
                parent.getAddonProducts().add(product);
            }

            // If status is "P", get a more detailed status
            if (product.isProvided()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Product is provided: " + product.getName());
                }
                product.setSyncStatus(getProductSyncStatus(product));
            }
            else {
                product.setSyncStatus(Product.SyncStatus.NOT_MIRRORED);
            }
        }

        return result;
    }

}
