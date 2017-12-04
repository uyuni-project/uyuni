package com.redhat.rhn.domain.notification.types;

import com.redhat.rhn.domain.notification.NotificationMessage;

public class ChannelSyncFinished implements NotificationData {

    private Long channelId;

    public ChannelSyncFinished(Long channelId) {
        this.channelId = channelId;
    }

    public Long getChannelId() {
        return channelId;
    }

    @Override
    public NotificationMessage.NotificationMessageSeverity getSeverity() {
        return NotificationMessage.NotificationMessageSeverity.info;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.ChannelSyncFailed;
    }
}
