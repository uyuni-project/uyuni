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

import java.util.Optional;

/**
 * Notification data for channel sync failure.
 */
public class ChannelSyncFailed implements NotificationData {

    private Long channelId;
    private String channelName;
    private String details;

    /**
     * Constructor
     * @param channelIdIn id of the channel that failed
     * @param channelNameIn name of the channel that failed
     * @param detailsIn details if available
     */
    public ChannelSyncFailed(Long channelIdIn, String channelNameIn, String detailsIn) {
        this.channelId = channelIdIn;
        this.channelName = channelNameIn;
        this.details = Optional.ofNullable(detailsIn).orElse("");
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
        return NotificationMessage.NotificationMessageSeverity.error;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotificationType getType() {
        return NotificationType.ChannelSyncFailed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSummary() {
        return LocalizationService.getInstance().getMessage("notification.channelsyncfailed",
                getChannelId().toString(), getChannelName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDetails() {
        return details;
    }
}
