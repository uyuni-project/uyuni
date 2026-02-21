/*
 * Copyright (c) 2022--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.notification.types;

import static com.redhat.rhn.common.hibernate.HibernateFactory.getSession;

import com.redhat.rhn.common.localization.LocalizationService;

import java.util.Optional;


public class SubscriptionWarning implements NotificationData {

    private static final LocalizationService LOCALIZATION_SERVICE = LocalizationService.getInstance();

    /**
     * returns true if subscriptions that are expired within 30 days or will expire in within 90 days.
     * @return boolean
     **/
    public boolean expiresSoon() {
        // The following query aims to find if there are subscriptions that will expire in 90 days
        // or have expired within 30 days.
        // Initially, the query was looking directly in the suseSCCSubscription table, but that approach was
        // sometimes warning the user about subscriptions that were then not present in the subscription matcher page.
        // To fill this discrepancy, now the query checks subscriptions "joined on" the orders, as they appear
        // in the input for the subscription matcher (see MatcherJsonIO.getJsonSubscriptions).
        // In this way, the user is warned only with subscriptions actually appearing in the subscription matcher page,
        // while they don't get pointlessly warned about unmatched subscriptions or test/promotional subscriptions
        // (hence not present in the subscription matcher page).
        // Also, although the starts_at/expires_at subscription dates and the start_date/end_date order dates should be
        // consistent, we take the suseSCCOrderItem.end_date as reference to check the subscription validity in time

        Optional<Boolean> result = getSession().createNativeQuery(
                """
                        SELECT EXISTS
                         (
                            SELECT ord.sku, ord.quantity, subs.scc_id, subs.name, subs.status, subs.subtype
                            FROM suseSCCOrderItem AS ord
                            JOIN suseSCCSubscription AS subs
                            ON ord.subscription_id = subs.scc_id
                            WHERE
                            subs.subtype != 'internal' AND subs.subtype != 'test' AND
                            ((subs.status = 'ACTIVE' AND ord.end_date < now() + interval '90 DAY')
                                  OR (subs.status = 'EXPIRED' AND ord.end_date > now() - interval '30 day'))
                        )
                    """).uniqueResultOptional();

        return result.orElse(false);
    }

    @Override
    public String getSummary() {
         return LOCALIZATION_SERVICE.getMessage("notification.subscriptionwarning.summary");
    }

    @Override
    public String getDetails() {
        return LOCALIZATION_SERVICE.getMessage("notification.subscriptionwarning.detail");
    }
}
