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
import com.redhat.rhn.manager.setup.ProductSyncManagerException;

import org.apache.log4j.Logger;
import org.directwebremoting.WebContextFactory;

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
     */
    public void synchronizeSingle(String productIdent) {
        User user = new RequestContext(WebContextFactory.get()
                .getHttpServletRequest())
                .getCurrentUser();

        if (!user.hasRole(RoleFactory.SAT_ADMIN)) {
            throw new IllegalArgumentException(
                    "Must be SAT_ADMIN to synchronize the products");
        }

        try {
            new ProductSyncManager().addProduct(productIdent);
        }
        catch (ProductSyncManagerException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }
}
