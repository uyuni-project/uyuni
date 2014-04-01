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
package com.redhat.rhn.frontend.action.renderers.setupwizard;

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.renderers.BaseFragmentRenderer;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.redhat.rhn.manager.satellite.ReadProductsCommand;

import com.suse.manager.model.products.Product;
import com.suse.manager.model.products.ProductList;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * Asynchronously render the page content for product selection.
 */
public class ProductsRenderer extends BaseFragmentRenderer {

    private static Logger logger = Logger.getLogger(ProductsRenderer.class);

    /** Attribute keys */
    public static final String ATTRIB_PRODUCTS_LIST = "productsList";

    /** The URL of the page to render */
    private static final String PAGE_URL =
            "/WEB-INF/pages/admin/setup/suse-products-async.jsp";

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
            List<Product> products = cmd.getProducts();

            // Sort the list
            Collections.sort(products);

            // Set the "parentUrl" for the form (in rl:listset)
            request.setAttribute(ListTagHelper.PARENT_URL, "");
            request.setAttribute(ATTRIB_PRODUCTS_LIST, products);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageUrl() {
        return PAGE_URL;
    }
}
