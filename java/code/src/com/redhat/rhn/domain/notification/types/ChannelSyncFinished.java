package com.redhat.rhn.domain.notification.types;

import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.notification.NotificationMessage;

public class ChannelSyncFinished implements NotificationData {

    private Long channelId;
    private String channelName;

    public ChannelSyncFinished(Long channelId) {
        this.channelId = channelId;
        this.channelName = ChannelFactory.lookupById(channelId).getName();
    }

    public Long getChannelId() {
        return channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    @Override
    public NotificationMessage.NotificationMessageSeverity getSeverity() {
        return NotificationMessage.NotificationMessageSeverity.info;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.ChannelSyncFinished;
    }
}
