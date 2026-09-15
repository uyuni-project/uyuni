/*
 * Copyright (c) 2020--2025 SUSE LLC
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

import com.redhat.rhn.common.localization.LocalizationService;

/**
 * Notification data for state apply failure.
 */
public class StateApplyFailed implements NotificationData {

    private String systemName;
    private Long systemId;
    private Long actionId;

    /**
     * Constructor
     * @param systemNameIn the name of the system
     * @param systemIdIn the id of the system
     * @param actionIdIn the id of the action
     */
    public StateApplyFailed(String systemNameIn, long systemIdIn, long actionIdIn) {
        this.systemName = systemNameIn;
        this.systemId = systemIdIn;
        this.actionId = actionIdIn;
    }

    /**
     * @return the system name
     */
    public String getSystemName() {
        return systemName;
    }

    /**
     * @return the system id
     */
    public Long getSystemId() {
        return systemId;
    }

    /**
     * @return the action id
     */
    public Long getActionId() {
        return actionId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSummary() {
        return LocalizationService.getInstance().getMessage("notification.stateapplyfailed",
                getSystemId().toString(), getActionId().toString(), getSystemName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDetails() {
        return "";
    }
}
