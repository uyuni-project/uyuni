package com.redhat.rhn.domain.notification;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * A notification NotificationMessage Object.
 */
@Entity
@Table(name = "susenotificationmessage")
public class NotificationMessage {

    private Long id;
    private NotificationMessageType notificationSeverity;
    private String description;
    private boolean isRead = false;

    /**
     * Empty constructor
     */
    public NotificationMessage() {
    }

    /**
     * Default constructor for a NotificationMessage
     *
     * @param notificationSeverityIn the severity of the message
     * @param descriptionIn the description of the message
     * @param isReadIn if the message is already read or not
     */
    public NotificationMessage(NotificationMessageType notificationSeverityIn, String descriptionIn) {
        this.notificationSeverity= notificationSeverityIn;
        this.description = descriptionIn;
    }

    /**
     * @return Returns the id.
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "nmsg_seq")
    @SequenceGenerator(name = "nmsg_seq", sequenceName = "suse_notification_message_id_seq",
            allocationSize = 1)
    public Long getId() {
        return id;
    }

    /**
     * @param idIn The id to set.
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @return Returns the description.
     */
    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    /**
     * @param descriptionIn The description to set.
     */
    public void setDescription(String descriptionIn) {
        this.description = descriptionIn;
    }

    /**
     * @return Returns the read.
     */
    @Type(type = "yes_no")
    @Column(name = "is_read")
    public boolean getIsRead() {
        return isRead;
    }

    /**
     * @param isReadIn The read to set.
     */
    public void setIsRead(boolean isReadIn) {
        this.isRead = isReadIn;
    }

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "notificationSeverity")
    public NotificationMessageType getNotificationSeverity() {
        return notificationSeverity;
    }

    public void setNotificationSeverity(NotificationMessageType notificationSeverityIn) {
        this.notificationSeverity = notificationSeverityIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof NotificationMessage)) {
            return false;
        }
        NotificationMessage otherNotificationMessage = (NotificationMessage) other;
        return new EqualsBuilder()
            .append(getDescription(), otherNotificationMessage.getDescription())
            .append(getIsRead(), otherNotificationMessage.getIsRead())
            .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getDescription())
            .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .append("description", getDescription())
            .append("isRead", getIsRead())
            .toString();
    }

    public enum NotificationMessageType {
        info, warning, error
    }
}
