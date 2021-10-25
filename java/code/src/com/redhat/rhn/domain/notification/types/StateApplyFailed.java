/*
 * Copyright (c) 2020 SUSE LLC
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
    public NotificationMessage.NotificationMessageSeverity getSeverity() {
        return NotificationMessage.NotificationMessageSeverity.error;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotificationType getType() {
        return NotificationType.StateApplyFailed;
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
