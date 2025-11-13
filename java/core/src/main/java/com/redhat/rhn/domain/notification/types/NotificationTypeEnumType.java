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

package com.redhat.rhn.domain.notification.types;

import com.redhat.rhn.domain.CustomEnumType;

public class NotificationTypeEnumType extends CustomEnumType<NotificationType, String> {

    /**
     * Default constructor
     */
    public NotificationTypeEnumType() {
        super(NotificationType.class, String.class, NotificationType::getLabel, NotificationType::byLabel);
    }
}
