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

import static com.redhat.rhn.domain.notification.types.NotificationSeverity.ERROR;
import static com.redhat.rhn.domain.notification.types.NotificationSeverity.INFO;
import static com.redhat.rhn.domain.notification.types.NotificationSeverity.WARNING;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.Labeled;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

/**
 * Available notification types
 */
public enum NotificationType implements Labeled {
    // Keep the alphabetical order
    CHANNEL_SYNC_FAILED(ChannelSyncFailed.class, ERROR, "channelsyncfailed"),
    CHANNEL_SYNC_FINISHED(ChannelSyncFinished.class, INFO, "channelsyncfinished"),
    CREATE_BOOTSTRAP_REPO_FAILED(CreateBootstrapRepoFailed.class, ERROR, "createbootstraprepofailed"),
    END_OF_LIFE_PERIOD(EndOfLifePeriod.class, WARNING, "endoflifeperiod"),
    HUB_REGISTRATION_CHANGED(HubRegistrationChanged.class, INFO, "hubregistrationchanged"),
    ONBOARDING_FAILED(OnboardingFailed.class, ERROR, "onboardingfailed"),
    PAYG_AUTHENTICATION_UPDATE_FAILED(PaygAuthenticationUpdateFailed.class, ERROR, "paygauthenticationupdatefailed"),
    PAYG_NOT_COMPLIANT_WARNING(PaygNotCompliantWarning.class, WARNING, "paygnotcompliantwarning"),
    PXE_EVENT_FAILED(PXEEventFailed.class, ERROR, "pxeeventfailed"),
    SCC_OPT_OUT_WARNING(SCCOptOutWarning.class, WARNING, "sccoptoutwarning"),
    REPORT_DATABASE_UPDATED_FAILED(ReportDatabaseUpdateFailed.class, ERROR, "reportdbupdatefailed"),
    STATE_APPLY_FAILED(StateApplyFailed.class, ERROR, "stateapplyfailed"),
    SUBSCRIPTION_WARNING(SubscriptionWarning.class, WARNING, "subscriptionwarning"),
    UPDATE_AVAILABLE(UpdateAvailable.class, WARNING, "updateavailable");

    private final String label;

    private final String description;

    private final NotificationSeverity defaultSeverity;

    private final Class<? extends NotificationData> dataClass;

    NotificationType(Class<? extends NotificationData> clazz, NotificationSeverity severity,  String descriptionId) {
        this.label = clazz.getSimpleName();
        this.description = LocalizationService.getInstance().getMessage(descriptionId);
        this.defaultSeverity = severity;
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
     * Retrieves the default severity of this notification type. The actual severity associated with a notification
     * may differ, thus use {@link NotificationData#getSeverity()} to retrieve the exact value for a specific
     * notification data instance.
     * @return the default {@link NotificationSeverity} associated with this type.
     */
    public NotificationSeverity getDefaultSeverity() {
        return defaultSeverity;
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
        return findByField(NotificationType::getLabel, label);
    }

    /**
     * Retrieves a notification type by its {@link NotificationData} implementation class.
     * @param dataClass the class representing the notification data
     * @return the {@link NotificationType} matching the given {@link NotificationData} class.
     * @throws IllegalArgumentException when no type matches the given data class
     */
    public static NotificationType byDataClass(Class<? extends NotificationData> dataClass) {
        return findByField(NotificationType::getDataClass, dataClass);
    }

    private static <T> NotificationType findByField(Function<NotificationType, T> getter, T value) {
        return Arrays.stream(NotificationType.values())
            .filter(type -> Objects.equals(value, getter.apply(type)))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unable to find type for %s".formatted(value)));
    }
}
