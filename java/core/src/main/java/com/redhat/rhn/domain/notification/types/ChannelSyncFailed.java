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
    public String getSummary() {
        if (getChannelId() != null && getChannelName() != null) {
            return LocalizationService.getInstance().getMessage("notification.channelsyncfailed",
                    getChannelId().toString(), getChannelName());
        }
        return LocalizationService.getInstance().getMessage("notification.channelsyncforbidden");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDetails() {
        if (details != null) {
            return String.format("<pre>%s</pre>", details);
        }
        return "";
    }

    @Override
    public boolean isActionable() {
        return true;
    }
}
