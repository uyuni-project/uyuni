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

import com.redhat.rhn.common.localization.LocalizationService;

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
