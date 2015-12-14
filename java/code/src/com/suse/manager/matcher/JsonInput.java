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

package com.suse.manager.matcher;

import java.util.List;

/**
 * JSON representation of the matcher's input.
 */
public class JsonInput {

    /** The systems. */
    private List<JsonSystem> systems;

    /** The products. */
    private List<JsonProduct> products;

    /** The subscriptions. */
    private List<JsonSubscription> subscriptions;

    /** The pinned matches. */
    private List<JsonPinnedMatch> pinnedMatches;

    /**
     * Gets the systems.
     * @return the systems
     */
    public List<JsonSystem> getSystems() {
        return systems;
    }

    /**
     * Sets the systems.
     * @param systemsIn the new systems
     */
    public void setSystems(List<JsonSystem> systemsIn) {
        systems = systemsIn;
    }

    /**
     * Gets the products.
     * @return the products
     */
    public List<JsonProduct> getProducts() {
        return products;
    }

    /**
     * Sets the products.
     * @param productsIn the new products
     */
    public void setProducts(List<JsonProduct> productsIn) {
        products = productsIn;
    }

    /**
     * Gets the subscriptions.
     * @return the subscriptions
     */
    public List<JsonSubscription> getSubscriptions() {
        return subscriptions;
    }

    /**
     * Sets the subscriptions.
     * @param subscriptionsIn the new subscriptions
     */
    public void setSubscriptions(List<JsonSubscription> subscriptionsIn) {
        subscriptions = subscriptionsIn;
    }

    /**
     * Gets the pinned matches.
     * @return the pinned matches
     */
    public List<JsonPinnedMatch> getPinnedMatches() {
        return pinnedMatches;
    }

    /**
     * Sets the pinned matches.
     * @param pinnedMatchesIn the new pinned matches
     */
    public void setPinnedMatches(List<JsonPinnedMatch> pinnedMatchesIn) {
        pinnedMatches = pinnedMatchesIn;
    }
}
