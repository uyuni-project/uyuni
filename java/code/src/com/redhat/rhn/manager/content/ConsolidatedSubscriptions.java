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
package com.redhat.rhn.manager.content;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper object containing two lists of {@link String} objects representing channel
 * subscriptions and system entitlements separately. The strings contained in those
 * lists are product classes that there is a subscription for.
 */
public class ConsolidatedSubscriptions {

    private Set<String> channelSubscriptions = new HashSet<String>();
    private Set<String> systemEntitlements = new HashSet<String>();

    /**
     * Return the list of channel subscriptions.
     * @return the subscriptions
     */
    public List<String> getChannelSubscriptions() {
        return Collections.unmodifiableList(new ArrayList<String>(channelSubscriptions));
    }

    /**
     * Add a channel subscription.
     * @param subscription the channel subscription to add
     */
    public void addChannelSubscription(String subscription) {
        channelSubscriptions.add(subscription);
    }

    /**
     * Return the list of system entitlements.
     * @return the entitlements
     */
    public List<String> getSystemEntitlements() {
        return Collections.unmodifiableList(new ArrayList<String>(systemEntitlements));
    }

    /**
     * Add a system entitlement.
     * @param systemEntitlements the entitlement to add
     */
    public void addSystemEntitlement(String entitlement) {
        systemEntitlements.add(entitlement);
    }
}
