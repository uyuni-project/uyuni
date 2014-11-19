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

import com.redhat.rhn.domain.scc.SCCRepository;

import com.suse.scc.model.SCCProduct;
import com.suse.scc.model.SCCSubscription;

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
    List<SCCRepository> listRepositories() throws SCCClientException;

    /**
     * Gets and returns the list of all products.
     *
     * GET /connect/organizations/products/unscoped
     *
     * @return list of all available products
     * @throws SCCClientException if anything goes wrong SCC side
     */
    List<SCCProduct> listProducts() throws SCCClientException;

    /**
     * Gets and returns the list of subscriptions available to an organization.
     *
     * GET /connect/organizations/subscriptions
     *
     * @return list of subscriptions available to organization
     * @throws SCCClientException if anything goes wrong SCC side
     */
    List<SCCSubscription> listSubscriptions() throws SCCClientException;

}
