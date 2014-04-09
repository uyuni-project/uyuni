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
package com.redhat.rhn.frontend.action.setupwizard;

import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.setup.ProductSyncManager;
import com.redhat.rhn.manager.setup.ProductSyncManagerCommandException;

import org.apache.log4j.Logger;
import org.directwebremoting.WebContextFactory;

import java.util.List;

/**
 * Synchronizes products
 * @author Silvio Moioli <smoioli@suse.de>
 */
public class ProductSyncAction {

    /** Logger instance. */
    private static Logger log = Logger.getLogger(ProductSyncAction.class);

    /**
     * Synchronizes a single product.
     * @param productIdent the product ident
     * @throws ProductSyncManagerCommandException in case product
     * synchronization goes wrong
     */
    public void synchronizeSingle(String productIdent)
        throws ProductSyncManagerCommandException {
        checkUserRole();

        try {
            new ProductSyncManager().addProduct(productIdent);
        }
        catch (ProductSyncManagerCommandException e) {
            log.error(e);
            throw e;
        }
    }

    /**
     * Synchronizes a single product.
     * @param productIdents the product ident list
     * @throws ProductSyncManagerCommandException in case product
     * synchronization goes wrong
     */
    public void synchronizeMultiple(List<String> productIdents)
        throws ProductSyncManagerCommandException {
        checkUserRole();

        try {
            new ProductSyncManager().addProducts(productIdents);
        }
        catch (ProductSyncManagerCommandException e) {
            log.error(e);
            throw e;
        }
    }

    /**
     * Throws an exception if the currently logged user cannot synchronize
     * products.
     */
    private void checkUserRole() {
        User user = new RequestContext(
                WebContextFactory.get()
                .getHttpServletRequest())
                .getCurrentUser();

        if (!user.hasRole(RoleFactory.SAT_ADMIN)) {
            throw new IllegalArgumentException(
                    "Must be SAT_ADMIN to synchronize the products");
        }
    }
}
