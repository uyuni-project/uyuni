package com.redhat.rhn.domain.notification.types;

import com.redhat.rhn.domain.notification.NotificationMessage;

public interface NotificationData {
    NotificationMessage.NotificationMessageSeverity getSeverity();
    NotificationType getType();
}
