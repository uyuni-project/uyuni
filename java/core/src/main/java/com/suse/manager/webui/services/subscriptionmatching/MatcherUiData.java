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
package com.suse.manager.webui.services.subscriptionmatching;

import com.suse.matcher.json.MessageJson;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Backing data for the Subscription Matching UI.
 */
public class MatcherUiData {

    /** True if we have any data from subscription-matcher. */
    private boolean matcherDataAvailable;

    /** The latest start date of subscription-matcher's run. */
    private Date latestStart;

    /** The latest end date of subscription-matcher's run. */
    private Date latestEnd;

    /** The subscriptions. */
    private Map<String, Subscription> subscriptions = new HashMap<>();

    /** The messages. */
    private List<MessageJson> messages = new LinkedList<>();

    /** Products. */
    private Map<String, Product> products = new HashMap<>();

    /** Ids of unmatched products. */
    private Set<Long> unmatchedProductIds;

    /** Pinned matches. */
    private List<PinnedMatch> pinnedMatches = new LinkedList<>();

    /** The systems. */
    private Map<String, System> systems = new HashMap<>();

    /**
     * Standard constructor.
     *
     * @param matcherDataAvailableIn - true if the matcher data is available
     * @param latestStartIn the latest start date of subscription-matcher's run
     * @param latestEndIn the latest end date of subscription-matcher's run
     * @param messagesIn - list of messages
     * @param subscriptionsIn - list of subscriptions
     * @param productsIn - products
     * @param unmatchedProductIdsIn - list of ids of unmatched products
     * @param pinnedMatchesIn - list of pinned matches
     * @param systemsIn - list of systems
     */
    public MatcherUiData(boolean matcherDataAvailableIn, Date latestStartIn,
            Date latestEndIn, List<MessageJson> messagesIn,
            Map<String, Subscription> subscriptionsIn,
            Map<String, Product> productsIn,
            Set<Long> unmatchedProductIdsIn,
            List<PinnedMatch> pinnedMatchesIn,
            Map<String, System> systemsIn) {
        matcherDataAvailable = matcherDataAvailableIn;
        latestStart = latestStartIn;
        latestEnd = latestEndIn;
        messages = messagesIn;
        subscriptions = subscriptionsIn;
        products = productsIn;
        unmatchedProductIds = unmatchedProductIdsIn;
        pinnedMatches = pinnedMatchesIn;
        setSystems(systemsIn);
    }

    /**
     * Shortcut constructor for creating MatcherUiData with matcherDataAvailable = false
     *
     * @param latestStartIn the latest start date of subscription-matcher's run
     * @param latestEndIn the latest end date of subscription-matcher's run
     */
    public MatcherUiData(Date latestStartIn, Date latestEndIn) {
        this(false, latestStartIn, latestEndIn, new LinkedList<>(),
                new HashMap<>(), new HashMap<>(), new HashSet<>(), new LinkedList<>(),
                new HashMap<>());
    }

    /**
     * Gets the subscriptions.
     * @return the subscriptions
     */
    public Map<String, Subscription> getSubscriptions() {
        return subscriptions;
    }

    /**
     * Sets the subscriptions.
     *
     * @param subscriptionsIn - the subscriptions
     */
    public void setSubscriptions(Map<String, Subscription> subscriptionsIn) {
        subscriptions = subscriptionsIn;
    }

    /**
     * Gets the messages.
     *
     * @return the messages
     */
    public List<MessageJson> getMessages() {
        return messages;
    }

    /**
     * Sets the messages.
     *
     * @param messagesIn the messages
     */
    public void setMessages(List<MessageJson> messagesIn) {
        messages = messagesIn;
    }

    /**
     * True if the data from the matcher is available.
     *
     * @return true true if the matcher data is available
     */
    public boolean isMatcherDataAvailable() {
        return matcherDataAvailable;
    }

    /**
     * Sets the flag for the matcher data availability.
     *
     * @param matcherDataAvailableIn the flag
     */
    public void setMatcherDataAvailable(boolean matcherDataAvailableIn) {
        matcherDataAvailable = matcherDataAvailableIn;
    }

    /**
     * Gets the latest start.
     *
     * @return the latest start
     */
    public Date getLatestStart() {
        return latestStart;
    }

    /**
     * Sets the latest start.
     *
     * @param latestStartIn the new latest start
     */
    public void setLatestStart(Date latestStartIn) {
        latestStart = latestStartIn;
    }

    /**
     * Gets the latest end.
     *
     * @return the latest end
     */
    public Date getLatestEnd() {
        return latestEnd;
    }

    /**
     * Sets the latest end.
     *
     * @param latestEndIn the new latest end
     */
    public void setLatestEnd(Date latestEndIn) {
        latestEnd = latestEndIn;
    }

    /**
     * Gets the products.
     *
     * @return products
     */
    public Map<String, Product> getProducts() {
        return products;
    }

    /**
     * Sets the products.
     *
     * @param productsIn - the products
     */
    public void setProducts(Map<String, Product> productsIn) {
        products = productsIn;
    }

    /**
     * Gets the unmatchedProductIds.
     *
     * @return unmatchedProductIds
     */
    public Set<Long> getUnmatchedProductIds() {
        return unmatchedProductIds;
    }

    /**
     * Sets the unmatchedProductIds.
     *
     * @param unmatchedProductIdsIn - the unmatchedProductIds
     */
    public void setUnmatchedProductIds(Set<Long> unmatchedProductIdsIn) {
        unmatchedProductIds = unmatchedProductIdsIn;
    }

    /**
     * Gets the pinnedMatches.
     *
     * @return pinnedMatches
     */
    public List<PinnedMatch> getPinnedMatches() {
        return pinnedMatches;
    }

    /**
     * Sets the pinnedMatches.
     *
     * @param pinnedMatchesIn - the pinnedMatches
     */
    public void setPinnedMatches(List<PinnedMatch> pinnedMatchesIn) {
        pinnedMatches = pinnedMatchesIn;
    }

    /**
     * Gets the systems.
     *
     * @return the systems
     */
    public Map<String, System> getSystems() {
        return systems;
    }

    /**
     * Sets the systems.
     *
     * @param systemsIn the new systems
     */
    public void setSystems(Map<String, System> systemsIn) {
        systems = systemsIn;
    }
}
