/**
 * Copyright (c) 2013 SUSE
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

package com.redhat.rhn.frontend.action.renderers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.redhat.rhn.manager.satellite.ReadProductsCommand;
import com.suse.manager.model.products.Product;
import com.suse.manager.model.products.ProductList;

/**
 * Asynchronously render the page content for product selection.
 */
public class ProductsRenderer extends BaseFragmentRenderer {

    private static Logger logger = Logger.getLogger(ProductsRenderer.class);

    // Attribute keys
    public static final String ATTRIB_PRODUCTS_LIST = "productsList";
    //public static final String ATTRIB_ERROR_MSG = "errorMsg";

    // The URL of the page to render
    private static final String PAGE_URL = "/WEB-INF/pages/admin/setup/suse-products-async.jsp";

    /**
     * {@inheritDoc}
     */
    @Override
    protected void render(User user, PageControl pc, HttpServletRequest request) {
        // Read the products
        ReadProductsCommand cmd = new ReadProductsCommand(user);
        ValidatorError[] errors = cmd.readProducts();

        // Parse XML into objects
        if (errors == null) {
            String output = cmd.getXMLOutput();
            logger.debug("Output --> " + output);
            InputStream stream = new ByteArrayInputStream(output.getBytes());
            List<Product> products = parse(stream);

            // Only show base products
            List<Product> productsFiltered = new ArrayList<Product>();
            for (Product p : products) {
                if (p.getParentProduct().isEmpty()) {
                    productsFiltered.add(p);
                }
            }

            // Sort the list
            Collections.sort(productsFiltered);

            // Set the "parentUrl" for the form (in rl:listset)
            request.setAttribute(ListTagHelper.PARENT_URL, "");
            request.setAttribute(ATTRIB_PRODUCTS_LIST, productsFiltered);
        }
    }

    /**
     * Parse {@link InputStream} into a List of {@link Product} objects.
     *
     * @param stream
     * @return list of products
     */
    private List<Product> parse(InputStream stream) {
        ProductList result = null;
        Serializer serializer = new Persister();
        try {
            result = serializer.read(ProductList.class, stream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.getProducts();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageUrl() {
        return PAGE_URL;
    }
}
