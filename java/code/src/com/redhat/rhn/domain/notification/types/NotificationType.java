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
import com.redhat.rhn.domain.Labeled;

import java.util.Arrays;

/**
 * Available notification types
 */
public enum NotificationType implements Labeled {
    // Keep the alphabetical order
    ChannelSyncFailed(ChannelSyncFailed.class, "channelsyncfailed"),
    ChannelSyncFinished(ChannelSyncFinished.class, "channelsyncfinished"),
    CreateBootstrapRepoFailed(CreateBootstrapRepoFailed.class, "createbootstraprepofailed"),
    EndOfLifePeriod(EndOfLifePeriod.class, "endoflifeperiod"),
    OnboardingFailed(OnboardingFailed.class, "onboardingfailed"),
    PaygAuthenticationUpdateFailed(PaygAuthenticationUpdateFailed.class, "paygauthenticationupdatefailed"),
    PaygNotCompliantWarning(PaygNotCompliantWarning.class, "paygnotcompliantwarning"),
    SCCOptOutWarning(SCCOptOutWarning.class, "sccoptoutwarning"),
    StateApplyFailed(StateApplyFailed.class, "stateapplyfailed"),
    SubscriptionWarning(SubscriptionWarning.class, "subscriptionwarning"),
    UpdateAvailable(UpdateAvailable.class, "updateavailable");

    private final String label;

    private final String description;

    private final Class<? extends NotificationData> dataClass;

    NotificationType(Class<? extends NotificationData> clazz, String descriptionId) {
        this.label = clazz.getSimpleName();
        this.description = LocalizationService.getInstance().getMessage(descriptionId);
        this.dataClass = clazz;
    }

    /**
     * Returns the label of this notification type. The label is represented by the simple name of the data class.
     * @return the label of this notification type.
     */
    @Override
    public String getLabel() {
        return label;
    }

    /**
     * Gets the localized description of this notification type.
     * @return the description in the current locale.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Retrieves the class describing the data associated to this notification type.
     * @return the class implement {@link NotificationData} relevant for this notification type.
     */
    public Class<? extends NotificationData> getDataClass() {
        return dataClass;
    }

    /**
     * Retrieves a notification type by its label.
     * @param label the label to search
     * @return the {@link NotificationType} matching the given label.
     * @throws IllegalArgumentException when no type matches the given label
     */
    public static NotificationType byLabel(String label) {
        return Arrays.stream(NotificationType.values())
            .filter(type -> type.getLabel().equals(label))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unable to find type for label %s".formatted(label)));
    }
}
