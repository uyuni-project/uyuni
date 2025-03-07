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

import com.redhat.rhn.domain.notification.NotificationMessage;

/**
 * Interface for all notification specific data.
 */
public interface NotificationData {
    /**
     * Gets the severity of the notification.
     *
     * @return severity of this notification
     */
    NotificationMessage.NotificationMessageSeverity getSeverity();

    /**
     * Gets the {@link NotificationType} associated with this notification. Default implementation uses
     * {@link NotificationType#byDataClass(Class)} to retrieve the type from this runtime class.
     *
     * @return type of this notification
     */
    default NotificationType getType() {
        return NotificationType.byDataClass(getClass());
    }

    /**
     * Gets the summary of the notification, localized in the user language.
     *
     * @return translated summary of this notification
     */
    String getSummary();

    /**
     * Gets the long description of the notification, localized in the user language.
     *
     * @return details of this notification
     */
    String getDetails();
}
