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

package com.redhat.rhn.frontend.xmlrpc.subscriptionmatching;

import com.redhat.rhn.domain.server.PinnedSubscription;
import com.redhat.rhn.domain.server.PinnedSubscriptionFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;

import java.util.List;

/**
 * PinnedSubscription handler
 *
 * Important note: The ids of subscriptions in the endpoints correspond to ids
 * in the subscription data available either as a CSV (under [1]) or as a JSON (under [2]).
 *
 * [1]: rhn/manager/subscription-matching/subscription_report.csv
 * [2]: rhn/manager/api/subscription-matching/data
 *
 * @xmlrpc.namespace subscriptionmatching.pinnedsubscription
 * @xmlrpc.doc Provides the namespace for operations on Pinned Subscriptions
 */
public class PinnedSubscriptionHandler extends BaseHandler {

    /**
     * Lists all Pinned Subscriptions
     *
     * @param loggedInUser - logged in user
     * @return list of all Pinned Subscriptions
     *
     * @xmlrpc.doc Lists all PinnedSubscriptions
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.returntype
     *     #array()
     *         $PinnedSubscriptionSerializer
     *     #array_end()
     */
    public List<PinnedSubscription> list(User loggedInUser) {
        ensureSatAdmin(loggedInUser);
        return PinnedSubscriptionFactory.getInstance().listPinnedSubscriptions();
    }

    /**
     * Creates a Pinned Subscription based on given subscription and system
     *
     * @param loggedInUser - logged-in user
     * @param subscriptionId - id of subscription
     * @param systemId - id of system
     * @return new PinnedSubscription instance if successful, exception otherwise
     *
     * @xmlrpc.doc Creates a Pinned Subscription based on given subscription and system
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.param #param_desc("int", "subscriptionId" "Subscription Id")
     * @xmlrpc.param #param_desc("int", "systemId" "System Id")
     * @xmlrpc.returntype $PinnedSubscriptionSerializer
     */
    public PinnedSubscription create(User loggedInUser, Integer subscriptionId,
            Integer systemId) {
        ensureSatAdmin(loggedInUser);
        long systemIdLong = systemId.longValue();
        long subscriptionIdLong = subscriptionId.longValue();

        if (PinnedSubscriptionFactory.getInstance().lookupBySystemIdAndSubscriptionId(
                systemIdLong, subscriptionIdLong) != null) {
            throw new InvalidParameterException("Pinned Subscription with given" +
                    " parameters already exists.");
        }

        PinnedSubscription subscription = new PinnedSubscription();
        subscription.setSubscriptionId(subscriptionIdLong);
        subscription.setSystemId(systemIdLong);
        PinnedSubscriptionFactory.getInstance().save(subscription);

        return subscription;
    }

    /**
     * Deletes Pinned Subscription with given id
     *
     * @param loggedInUser - logged-in user
     * @param pinnedSubscriptionId - id of Pinned Subscription to delete
     * @return 1 if successful, exception otherwise
     *
     * @xmlrpc.doc Deletes Pinned Subscription with given id
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.param #param_desc("int", "pinnedSubscriptionId" "Pinned Subscription id")
     * @xmlrpc.returntype #return_int_success()
     */
    public int delete(User loggedInUser, Integer pinnedSubscriptionId) {
        ensureSatAdmin(loggedInUser);

        PinnedSubscription toDelete = PinnedSubscriptionFactory.getInstance()
                .lookupById(pinnedSubscriptionId.longValue());

        if (toDelete == null) {
            throw new InvalidParameterException("Pinned Subscription with given id" +
                    " doesn't exist.");
        }

        PinnedSubscriptionFactory.getInstance().remove(toDelete);
        return BaseHandler.VALID;
    }
}
