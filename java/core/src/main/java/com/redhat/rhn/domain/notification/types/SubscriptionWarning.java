/*
 * Copyright (c) 2022--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.redhat.rhn.domain.notification.types;

import com.redhat.rhn.common.localization.LocalizationService;

import com.suse.manager.matcher.MatcherJsonIO;
import com.suse.manager.webui.services.subscriptionmatching.SubscriptionMatchProcessor;
import com.suse.matcher.json.OutputJson;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class SubscriptionWarning implements NotificationData {

    private static final LocalizationService LOCALIZATION_SERVICE = LocalizationService.getInstance();

    /**
     * Check if there are subscription that are expired recently or that are expiring soon.
     * @return {@code true} if there are subscriptions expired recently or expiring soon.
     **/
    public boolean expiresSoon() {
        return expiresSoon(Instant.now(), new MatcherJsonIO(), new SubscriptionMatchProcessor());
    }

    /**
     * Check if there are subscription that are expired recently or that are expiring soon.
     * @param time the instant used to evaluate the expiration
     * @param matcherJsonIO the IO JSON logic for subscription-matcher
     * @param dataProcessor the data processor for subscription-matcher
     * @return {@code true} if there are subscriptions expired recently or expiring soon, considering the given time.
     **/
    boolean expiresSoon(Instant time, MatcherJsonIO matcherJsonIO, SubscriptionMatchProcessor dataProcessor) {
        Instant ninetyDaysFuture = time.plus(90, ChronoUnit.DAYS);
        Instant thirtyDaysPast = time.minus(30, ChronoUnit.DAYS);

        Optional<OutputJson> lastJsonOutput = matcherJsonIO.getLastMatcherOutput();
        return dataProcessor.listEligibleSubscriptions(lastJsonOutput).stream()
            .filter(s -> s.getEndDate() != null)
            .anyMatch(s -> {
                Instant end = s.getEndDate().toInstant();

                // Active and expiring soon (within 90 days)
                boolean isActiveAndExpiresSoon = end.isAfter(time) && end.isBefore(ninetyDaysFuture);

                // Expired recently (within 30 days)
                boolean isExpiredRecently = end.isBefore(time) && end.isAfter(thirtyDaysPast);

                return (isActiveAndExpiresSoon || isExpiredRecently);
            });
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
