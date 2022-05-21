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

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.notification.NotificationMessage;

import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class EndOfLifePeriod implements NotificationData {

    private static final LocalizationService LOCALIZATION_SERVICE = LocalizationService.getInstance();

    private final Date endOfLifeDate;

    private final String majorVersion;

    /**
     * Default constructor.
     * @param endOfLifeDateIn the date after which the product is no longer supported
     */
    public EndOfLifePeriod(LocalDate endOfLifeDateIn) {
        this.endOfLifeDate = Date.from(endOfLifeDateIn.atStartOfDay(ZoneId.systemDefault()).toInstant());
        this.majorVersion = StringUtils.substringBeforeLast(ConfigDefaults.get().getProductVersion(), ".");
    }

    @Override
    public NotificationMessage.NotificationMessageSeverity getSeverity() {
        // Mark the notification as error 1 month before the actual expiration
        final LocalDate localEolDate = LocalDate.ofInstant(endOfLifeDate.toInstant(), ZoneId.systemDefault());
        if (ChronoUnit.MONTHS.between(localEolDate, LocalDate.now()) > -1) {
            return NotificationMessage.NotificationMessageSeverity.error;
        }

        return NotificationMessage.NotificationMessageSeverity.warning;
    }

    private boolean isExpired() {
        return endOfLifeDate.toInstant().isBefore(Instant.now());
    }

    @Override
    public NotificationType getType() {
        return NotificationType.EndOfLifePeriod;
    }

    @Override
    public String getSummary() {
        if (isExpired()) {
            return LOCALIZATION_SERVICE.getMessage("notification.endoflife.expired.summary", majorVersion);
        }

        return LOCALIZATION_SERVICE.getMessage("notification.endoflife.expiring.summary", majorVersion);
    }

    @Override
    public String getDetails() {
        final String deadline = LOCALIZATION_SERVICE.formatShortDate(endOfLifeDate);

        if (isExpired()) {
            return LOCALIZATION_SERVICE.getMessage("notification.endoflife.expired.detail", majorVersion, deadline);
        }

        return LOCALIZATION_SERVICE.getMessage("notification.endoflife.expiring.detail", majorVersion, deadline);
    }
}
