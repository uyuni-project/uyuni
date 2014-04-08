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

import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.renderers.BaseFragmentRenderer;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.manager.setup.ProductSyncManager;
import com.redhat.rhn.manager.setup.ProductSyncManagerException;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;


/**
 * Asynchronously render the page content for product selection.
 */
public class ProductsRenderer extends BaseFragmentRenderer {

    private static Logger logger = Logger.getLogger(ProductsRenderer.class);

    /** Attribute keys */
    public static final String ATTRIB_BASE_PRODUCTS_MAP = "baseProducts";

    /** The URL of the page to render */
    private static final String PAGE_URL =
            "/WEB-INF/pages/common/fragments/setup/suse-products-async.jspf";

    /**
     * {@inheritDoc}
     */
    @Override
    protected void render(User user, PageControl pc, HttpServletRequest request) {
        if (!user.hasRole(RoleFactory.SAT_ADMIN)) {
            throw new IllegalArgumentException("Must be SAT_ADMIN" +
                    "to read the products");
        }

        try {
            ProductSyncManager productSyncManager = new ProductSyncManager();
            productSyncManager.refreshProducts();
            request.setAttribute(ATTRIB_BASE_PRODUCTS_MAP,
                    productSyncManager.getBaseProducts());
        }
        catch (ProductSyncManagerException e) {
            logger.error("Exception while rendering products: " + e.getMessage());
            throw new RuntimeException(e);
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
