/*
 * Copyright (c) 2025 SUSE LLC
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

package com.redhat.rhn.domain.notification.types;

import com.google.gson.Gson;

public class NotificationTypeAdapter {

    private static final Gson GSON = new Gson();

    private final Class<? extends NotificationData> dataClass;

    /**
     * Creates an adapter of the specified {@link NotificationData}
     * @param type the type of {@link NotificationData}
     */
    public NotificationTypeAdapter(NotificationType type) {
        this.dataClass = type.getDataClass();
    }

    /**
     * Converts the given json to the correct NotificationData.
     * @param json the json representing the data
     * @return the notification data
     */
    public NotificationData fromJson(String json) {
        return GSON.fromJson(json, dataClass);
    }

    /**
     * Converts the given notification data to a valid json string
     * @param data the notification data
     * @return the json representing the data
     */
    public String toJson(NotificationData data) {
        return GSON.toJson(data, dataClass);
    }
}
