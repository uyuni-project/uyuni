/*
 * Copyright (c) 2017--2020 SUSE LLC
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
 * Notification data for channel sync finish.
 */
public class ChannelSyncFinished implements NotificationData {

    private Long channelId;
    private String channelName;

    /**
     * Constructor
     * @param channelIdIn id of the channel that finished
     * @param channelNameIn name of the channel that finished
     */
    public ChannelSyncFinished(Long channelIdIn, String channelNameIn) {
        this.channelId = channelIdIn;
        this.channelName = channelNameIn;
    }

    /**
     * @return the channel id
     */
    public Long getChannelId() {
        return channelId;
    }

    /**
     * @return the channel name
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotificationMessage.NotificationMessageSeverity getSeverity() {
        return NotificationMessage.NotificationMessageSeverity.info;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotificationType getType() {
        return NotificationType.ChannelSyncFinished;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSummary() {
        return LocalizationService.getInstance().getMessage("notification.channelsyncfinished",
                getChannelId().toString(), getChannelName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDetails() {
        return "";
    }
}
