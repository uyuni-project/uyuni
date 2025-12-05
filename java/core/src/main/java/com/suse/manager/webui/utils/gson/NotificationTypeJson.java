/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.webui.utils.gson;

import com.redhat.rhn.domain.notification.types.NotificationType;

public record NotificationTypeJson(String label, String description) {

    /**
     * Create an instance from a notification type
     * @param notificationType the notification type
     */
    public NotificationTypeJson(NotificationType notificationType) {
        this(notificationType.getLabel(), notificationType.getDescription());
    }
}
