/*
 * Copyright (c) 2017--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.manager.webui.utils.gson;

import com.redhat.rhn.domain.notification.NotificationMessage;

import java.util.Date;

/**
 * A notification NotificationMessageJson Object.
 *
 * @param id the id
 * @param severity the severity
 * @param type the type
 * @param summary the summary
 * @param details the details
 * @param read the read flag
 * @param actionable the actionable glag
 * @param created the creation date
 */
public record NotificationMessageJson(
    Long id,
    String severity,
    String type,
    String summary,
    String details,
    boolean read,
    boolean actionable,
    Date created
) {

    /**
     * Default constructor for a NotificationMessageJson
     *@param nm the {@link NotificationMessage} source object
     *@param readIn the read/unread flag
     */
    public NotificationMessageJson(NotificationMessage nm, boolean readIn) {
        this(
            nm.getId(),
            nm.getNotificationData().getSeverity().getLabel(),
            nm.getType().getLabel(),
            nm.getNotificationData().getSummary(),
            nm.getNotificationData().getDetails(),
            readIn,
            nm.getNotificationData().isActionable(),
            nm.getCreated()
        );
    }
}
