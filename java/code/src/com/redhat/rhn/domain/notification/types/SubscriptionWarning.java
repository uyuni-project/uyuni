/*
 * Copyright (c) 2022 SUSE LLC
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
package com.redhat.rhn.domain.notification.types;

import static com.redhat.rhn.common.hibernate.HibernateFactory.getSession;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.notification.NotificationMessage;

import java.util.Optional;


public class SubscriptionWarning implements NotificationData {

    private static final LocalizationService LOCALIZATION_SERVICE = LocalizationService.getInstance();

    /**
     * returns true if subscriptions that are expired within 30 days or will expire in within 90 days.
     * @return boolean
     **/
    public boolean expiresSoon() {
        Optional<Boolean> result = getSession().createSQLQuery(
        "select exists (select name,  expires_at, status, subtype " +
                "from susesccsubscription where subtype != 'internal' " +
                " and ((status = 'ACTIVE' and expires_at < now() + interval '90 day') " +
                "or (status = 'EXPIRED' and expires_at > now() - interval '30 day')))").uniqueResultOptional();

        return result.orElse(false);
    }

    @Override
    public NotificationMessage.NotificationMessageSeverity getSeverity() {
        return NotificationMessage.NotificationMessageSeverity.warning;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.SubscriptionWarning;
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
