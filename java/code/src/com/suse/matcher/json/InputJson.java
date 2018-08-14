/**
 * Copyright (c) 2016 SUSE LLC
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
package com.suse.matcher.json;

import java.util.Date;
import java.util.List;

/**
 * JSON representation of the matcher's input.
 */
public class InputJson {

    /** Date and time of the match (as it influences subscriptions). */
    private Date timestamp;

    /** The systems */
    private List<SystemJson> systems;

    /** Groups of virtual guests. */
    private List<VirtualizationGroupJson> virtualizationGroups;

    /** The products */
    private List<ProductJson> products;

    /** The subscriptions */
    private List<SubscriptionJson> subscriptions;

    /** The pinned matches */
    private List<MatchJson> pinnedMatches;

    /**
     * Standard constructor.
     *
     * @param timestampIn the date and time of the match
     * @param systemsIn the systems
     * @param virtualizationGroupsIn the groups
     * @param productsIn the products
     * @param subscriptionsIn the subscriptions
     * @param pinnedMatchesIn the pinned matches
     */
    public InputJson(Date timestampIn, List<SystemJson> systemsIn,
                     List<VirtualizationGroupJson> virtualizationGroupsIn,
                     List<ProductJson> productsIn, List<SubscriptionJson> subscriptionsIn,
                     List<MatchJson> pinnedMatchesIn) {
        timestamp = timestampIn;
        systems = systemsIn;
        virtualizationGroups = virtualizationGroupsIn;
        products = productsIn;
        subscriptions = subscriptionsIn;
        pinnedMatches = pinnedMatchesIn;
    }

    /**
     * Gets the systems.
     *
     * @return the systems
     */
    public List<SystemJson> getSystems() {
        return systems;
    }

    /**
     * Sets the systems.
     *
     * @param systemsIn the new systems
     */
    public void setSystems(List<SystemJson> systemsIn) {
        systems = systemsIn;
    }

    /**
     * Gets the groups of virtual guests.
     *
     * @return the groups of virtual guests
     */
    public List<VirtualizationGroupJson> getVirtualizationGroups() {
        return virtualizationGroups;
    }

    /**
     * Sets the groups of virtual guests.
     *
     * @param virtualizationGroupsIn the new groups of virtual guests
     */
    public void setVirtualizationGroups(
            List<VirtualizationGroupJson> virtualizationGroupsIn) {
        virtualizationGroups = virtualizationGroupsIn;
    }

    /**
     * Gets the products.
     *
     * @return the products
     */
    public List<ProductJson> getProducts() {
        return products;
    }

    /**
     * Sets the products.
     *
     * @param productsIn the new products
     */
    public void setProducts(List<ProductJson> productsIn) {
        products = productsIn;
    }

    /**
     * Gets the subscriptions.
     *
     * @return the subscriptions
     */
    public List<SubscriptionJson> getSubscriptions() {
        return subscriptions;
    }

    /**
     * Sets the subscriptions.
     *
     * @param subscriptionsIn the new subscriptions
     */
    public void setSubscriptions(List<SubscriptionJson> subscriptionsIn) {
        subscriptions = subscriptionsIn;
    }

    /**
     * Gets the pinned matches.
     *
     * @return the pinned matches
     */
    public List<MatchJson> getPinnedMatches() {
        return pinnedMatches;
    }

    /**
     * Sets the pinned matches.
     *
     * @param pinnedMatchesIn the new pinned matches
     */
    public void setPinnedMatches(List<MatchJson> pinnedMatchesIn) {
        pinnedMatches = pinnedMatchesIn;
    }

    /**
     * Gets the date and time of the match.
     *
     * @return the date and time of the match
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the date and time of the match.
     *
     * @param timestampIn the new date and time of the match
     */
    public void setTimestamp(Date timestampIn) {
        timestamp = timestampIn;
    }
}
