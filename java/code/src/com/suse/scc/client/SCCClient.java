/**
 * Copyright (c) 2014 SUSE LLC
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
package com.suse.scc.client;

import com.redhat.rhn.manager.content.ProductTreeEntry;
import com.suse.scc.model.*;

import java.util.List;

/**
 * Represents an endpoint for SCC.
 */
public interface SCCClient {

    /**
     * Gets and returns the list of repositories available to an organization.
     *
     * GET /connect/organizations/repositories
     *
     * @return list of repositories available to organization
     * @throws SCCClientException if anything goes wrong SCC side
     */
    List<SCCRepositoryJson> listRepositories() throws SCCClientException;

    /**
     * Gets and returns the list of all products.
     *
     * GET /connect/organizations/products/unscoped
     *
     * @return list of all available products
     * @throws SCCClientException if anything goes wrong SCC side
     */
    List<SCCProductJson> listProducts() throws SCCClientException;

    /**
     * Gets and returns the list of subscriptions available to an organization.
     *
     * GET /connect/organizations/subscriptions
     *
     * @return list of subscriptions available to organization
     * @throws SCCClientException if anything goes wrong SCC side
     */
    List<SCCSubscriptionJson> listSubscriptions() throws SCCClientException;

    /**
     * Gets and returns the list of orders available to an organization.
     *
     * GET /connect/organizations/orders
     *
     * @return list of orders available to organization
     * @throws SCCClientException if anything goes wrong SCC side
     */
    List<SCCOrderJson> listOrders() throws SCCClientException;


    /**
     * Gets and returns the list of product tree entries from scc
     * @return list of product tree entries.
     * @throws SCCClientException if anything goes wrong SCC side
     */
    List<ProductTreeEntry> productTree() throws SCCClientException;

    SCCSystemCredentialsJson createSystem(SCCRegisterSystemJson system) throws SCCClientException;
    void deleteSystem(long id) throws SCCClientException;
}
