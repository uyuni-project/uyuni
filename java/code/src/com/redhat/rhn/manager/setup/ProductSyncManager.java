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

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.satellite.ReadProductsCommand;
import com.suse.manager.model.products.Product;
import com.suse.manager.model.products.ProductList;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

/**
 * API to operate on SUSE products and subscriptions
 */
public class ProductSyncManager {

    private static final Logger logger = Logger.getLogger(ProductSyncManager.class);
    private final User user;

    public ProductSyncManager(User user) {
        this.user = user;
    }

    /**
     * @return all available products
     */
    public List<Product> getProducts() {
        // Read the products
        ReadProductsCommand cmd = new ReadProductsCommand(this.user);
        ValidatorError[] errors = cmd.execute();

        return cmd.getProducts();
    }

    /**
     * @return all available base products (ie. no parent product) 
     */
    public List<Product> getBaseProducts() {
        // Only show base products
        List<Product> productsFiltered = new ArrayList<Product>();
        for (Product p : getProducts()) {
            if (p.getParentProduct().isEmpty()) {
                productsFiltered.add(p);
            }
        }
        // Sort the list
        Collections.sort(productsFiltered);
        return productsFiltered;
    }
}
