/*
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
 * @apidoc.namespace subscriptionmatching.pinnedsubscription
 * @apidoc.doc Provides the namespace for operations on Pinned Subscriptions
 */
public class PinnedSubscriptionHandler extends BaseHandler {

    /**
     * Lists all Pinned Subscriptions
     *
     * @param loggedInUser - logged in user
     * @return list of all Pinned Subscriptions
     *
     * @apidoc.doc Lists all PinnedSubscriptions
     * @apidoc.param #session_key()
     * @apidoc.returntype
     *     #return_array_begin()
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
     * @param sid - id of system
     * @return new PinnedSubscription instance if successful, exception otherwise
     *
     * @apidoc.doc Creates a Pinned Subscription based on given subscription and system
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "subscriptionId" "Subscription ID")
     * @apidoc.param #param_desc("int", "sid" "System ID")
     * @apidoc.returntype $PinnedSubscriptionSerializer
     */
    public PinnedSubscription create(User loggedInUser, Integer subscriptionId,
            Integer sid) {
        ensureSatAdmin(loggedInUser);
        long systemIdLong = sid.longValue();
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
     * @param subscriptionId - id of Pinned Subscription to delete
     * @return 1 if successful, exception otherwise
     *
     * @apidoc.doc Deletes Pinned Subscription with given id
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "subscriptionId" "Pinned Subscription ID")
     * @apidoc.returntype #return_int_success()
     */
    public int delete(User loggedInUser, Integer subscriptionId) {
        ensureSatAdmin(loggedInUser);

        PinnedSubscription toDelete = PinnedSubscriptionFactory.getInstance()
                .lookupById(subscriptionId.longValue());

        if (toDelete == null) {
            throw new InvalidParameterException("Pinned Subscription with given id" +
                    " doesn't exist.");
        }

        PinnedSubscriptionFactory.getInstance().remove(toDelete);
        return BaseHandler.VALID;
    }
}
