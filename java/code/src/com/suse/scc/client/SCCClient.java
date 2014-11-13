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
