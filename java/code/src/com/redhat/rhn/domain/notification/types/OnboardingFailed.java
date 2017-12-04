package com.redhat.rhn.domain.notification.types;

import com.redhat.rhn.domain.notification.NotificationMessage;

public class OnboardingFailed implements NotificationData {

    private String minionId;

    public OnboardingFailed(String minionId) {
        this.minionId = minionId;
    }

    public String getMinionId() {
        return minionId;
    }

    @Override
    public NotificationMessage.NotificationMessageSeverity getSeverity() {
        return NotificationMessage.NotificationMessageSeverity.error;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.OnboardingFailed;
    }
}
