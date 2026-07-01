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

import com.redhat.rhn.common.localization.LocalizationService;

import com.suse.manager.matcher.MatcherJsonIO;
import com.suse.matcher.json.OutputJson;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

public class SubscriptionWarning implements NotificationData {

    private static final LocalizationService LOCALIZATION_SERVICE = LocalizationService.getInstance();

    /**
     * returns true if subscriptions that are expired within 30 days or will expire in within 90 days.
     * @return boolean
     **/
    public boolean expiresSoon() {
        Optional<OutputJson> output = new MatcherJsonIO().getLastMatcherOutput();
        if (output.isPresent()) {
            Instant now = Instant.now();
            Instant ninetyDaysFuture = now.plus(90, ChronoUnit.DAYS);
            Instant thirtyDaysPast = now.minus(30, ChronoUnit.DAYS);

            return output.get().getSubscriptions().stream()
                    .anyMatch(s -> {
                        Date endDate = s.getEndDate();
                        if (endDate == null) {
                            return false;
                        }
                        Instant end = endDate.toInstant();

                        // Active and expiring soon (within 90 days)
                        boolean isActiveAndExpiresSoon = end.isAfter(now) && end.isBefore(ninetyDaysFuture);

                        // Expired recently (within 30 days)
                        boolean isExpiredRecently = end.isBefore(now) && end.isAfter(thirtyDaysPast);

                        return (isActiveAndExpiresSoon || isExpiredRecently);
                    });
        }
        return false;
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
