/*
 * Copyright (c) 2017--2025 SUSE LLC
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
    public String getSummary() {
        return LocalizationService.getInstance().getMessage("notification.onboardingfailed", getMinionId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDetails() {
        if (details != null) {
            return String.format("<pre>%s</pre>", details.replace("\\\\n", "\n"));
        }
        return "";
    }

    @Override
    public boolean isActionable() {
        return true;
    }
}
