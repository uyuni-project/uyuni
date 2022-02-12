/*
 * Copyright (c) 2017--2020 SUSE LLC
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

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.notification.NotificationMessage;

import java.util.Optional;

/**
 * Notification data for minion onboarding failure.
 */
public class OnboardingFailed implements NotificationData {

    private String minionId;
    private String details;

    /**
     * Constructor
     * @param minionIdIn minion id of the failed minion
     */
    public OnboardingFailed(String minionIdIn) {
        this.minionId = minionIdIn;
    }

    /**
     * Constructor
     * @param minionIdIn minion id of the failed minion
     * @param detailsIn the details
     */
    public OnboardingFailed(String minionIdIn, String detailsIn) {
        this.minionId = minionIdIn;
        this.details = Optional.ofNullable(detailsIn).orElse("");
    }

    /**
     * @return the minion id
     */
    public String getMinionId() {
        return minionId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotificationMessage.NotificationMessageSeverity getSeverity() {
        return NotificationMessage.NotificationMessageSeverity.error;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotificationType getType() {
        return NotificationType.OnboardingFailed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSummary() {
        return LocalizationService.getInstance().
                getMessage("notification.onboardingfailed", getMinionId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDetails() {
        return details;
    }
}
