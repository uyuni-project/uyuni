package com.redhat.rhn.domain.notification.types;

import com.redhat.rhn.domain.notification.NotificationMessage;

public class ChannelSyncFailed implements NotificationData {

    private Long channelId;

    public ChannelSyncFailed(Long channelId) {
        this.channelId = channelId;
    }

    public Long getChannelId() {
        return channelId;
    }

    @Override
    public NotificationMessage.NotificationMessageSeverity getSeverity() {
        return NotificationMessage.NotificationMessageSeverity.error;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.ChannelSyncFailed;
    }
}
