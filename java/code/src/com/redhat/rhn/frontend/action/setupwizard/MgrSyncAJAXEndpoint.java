/**
 * Copyright (c) 2015 SUSE LLC
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
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.setup.ProductSyncException;
import com.redhat.rhn.manager.setup.ProductSyncManager;

import org.apache.log4j.Logger;
import org.directwebremoting.WebContextFactory;

import java.util.List;

/**
 * AJAX endpoint providing "mgr-sync refresh" and product handling functionalities.
 */
public class MgrSyncAJAXEndpoint {

    /** Logger instance. */
    private static Logger log = Logger.getLogger(MgrSyncAJAXEndpoint.class);

    /**
     * Exception thrown by the AJAX end-point. DWR-convertible so that it
     * can be handled on the client side.
     */
    public static class MgrSyncException extends Exception {
        /**
         * @param message the message
         */
        public MgrSyncException(String message) {
            super(message);
        }
    };

    /**
     * Synchronize channels as in "mgr-sync refresh".
     *
     * @throws MgrSyncException in case of an error
     */
    public void synchronizeChannels() throws MgrSyncException {
        ensureSatAdmin();
        try {
            ContentSyncManager csm = new ContentSyncManager();
            csm.updateChannels(null);
        }
        catch (Exception e) {
            log.fatal(e.getMessage(), e);
            throw new MgrSyncException(e.getLocalizedMessage());
        }
    }

    /**
     * Synchronize channel families as in "mgr-sync refresh".
     *
     * @throws MgrSyncException in case of an error
     */
    public void synchronizeChannelFamilies() throws MgrSyncException {
        ensureSatAdmin();
        try {
            ContentSyncManager csm = new ContentSyncManager();
            csm.updateChannelFamilies(csm.readChannelFamilies());
        }
        catch (Exception e) {
            log.fatal(e.getMessage(), e);
            throw new MgrSyncException(e.getLocalizedMessage());
        }
    }

    /**
     * Synchronize products as in "mgr-sync refresh".
     *
     * @throws MgrSyncException in case of an error
     */
    public void synchronizeProducts() throws MgrSyncException {
        ensureSatAdmin();
        try {
            ContentSyncManager csm = new ContentSyncManager();
            csm.updateSUSEProducts(csm.getProducts());
        }
        catch (Exception e) {
            log.fatal(e.getMessage(), e);
            throw new MgrSyncException(e.getLocalizedMessage());
        }
    }

    /**
     * Synchronize product channels as in "mgr-sync refresh".
     *
     * @throws MgrSyncException in case of an error
     */
    public void synchronizeProductChannels() throws MgrSyncException {
        ensureSatAdmin();
        try {
            ContentSyncManager csm = new ContentSyncManager();
            csm.updateSUSEProductChannels(csm.getAvailableChannels(csm.readChannels()));
        }
        catch (Exception e) {
            log.fatal(e.getMessage(), e);
            throw new MgrSyncException(e.getLocalizedMessage());
        }
    }

    /**
     * Synchronize subscriptions as in "mgr-sync refresh".
     *
     * @throws MgrSyncException in case of an error
     */
    public void synchronizeSubscriptions() throws MgrSyncException {
        ensureSatAdmin();
        ContentSyncManager csm = new ContentSyncManager();
        try {

            csm.updateSubscriptions(csm.getSubscriptions());
        }
        catch (Exception e) {
            log.fatal(e.getMessage(), e);
            throw new MgrSyncException(e.getLocalizedMessage());
        }
    }

    /**
     * Synchronize or add a given list of products.
     *
     * @param productIdents list of product idents
     * @throws ProductSyncException in case of errors
     */
    public void syncProducts(List<String> productIdents) throws ProductSyncException {
        ensureSatAdmin();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Add/Sync products: " + productIdents);
            }
            new ProductSyncManager().addProducts(productIdents);
        }
        catch (ProductSyncException e) {
            log.error(e);
            throw e;
        }
    }

    /**
     * Make sure that the current user is SAT_ADMIN.
     */
    private static void ensureSatAdmin() {
        User user = new RequestContext(WebContextFactory.get().getHttpServletRequest()).
                getCurrentUser();
        if (!user.hasRole(RoleFactory.SAT_ADMIN)) {
            throw new IllegalArgumentException("Must be SAT_ADMIN to synchronize products");
        }
    }
}
