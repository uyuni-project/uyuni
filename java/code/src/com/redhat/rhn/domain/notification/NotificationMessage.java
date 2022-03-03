/*
 * Copyright (c) 2017 SUSE LLC
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

package com.redhat.rhn.domain.notification;

import com.redhat.rhn.domain.notification.types.ChannelSyncFailed;
import com.redhat.rhn.domain.notification.types.ChannelSyncFinished;
import com.redhat.rhn.domain.notification.types.CreateBootstrapRepoFailed;
import com.redhat.rhn.domain.notification.types.NotificationData;
import com.redhat.rhn.domain.notification.types.NotificationType;
import com.redhat.rhn.domain.notification.types.OnboardingFailed;
import com.redhat.rhn.domain.notification.types.PaygAuthenticationUpdateFailed;
import com.redhat.rhn.domain.notification.types.StateApplyFailed;

import com.google.gson.Gson;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * A notification NotificationMessage Object.
 */
@Entity
@Table(name = "susenotificationmessage")
public class NotificationMessage implements Serializable {

    private Long id;
    private NotificationType type;
    private String data;
    private Date created;

    /**
     * Empty constructor
     */
    public NotificationMessage() {
    }

    /**
     * Default constructor for a NotificationMessage
     *
     * @param dataIn the json data of the message
     */
    public NotificationMessage(NotificationData dataIn) {
        setNotificationData(dataIn);
    }

    /**
     * @return Returns the id.
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "nmsg_seq")
    @SequenceGenerator(name = "nmsg_seq", sequenceName = "suse_notif_message_id_seq",
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
    @Column(name = "data")
    public String getData() {
        return data;
    }

    /**
     * Set the notification specific data.
     * @param notificationData notification data
     */
    @Transient
    public void setNotificationData(NotificationData notificationData) {
        setType(notificationData.getType());
        setData(new Gson().toJson(notificationData));
    }

    /**
     * Get the notification specific data.
     * @return the notification data
     */
    @Transient
    public NotificationData getNotificationData() {
        switch (getType()) {
            case OnboardingFailed: return new Gson().fromJson(getData(), OnboardingFailed.class);
            case ChannelSyncFailed: return new Gson().fromJson(getData(), ChannelSyncFailed.class);
            case ChannelSyncFinished:
                return new Gson().fromJson(getData(), ChannelSyncFinished.class);
            case CreateBootstrapRepoFailed:
                return new Gson().fromJson(getData(), CreateBootstrapRepoFailed.class);
            case StateApplyFailed:
                return new Gson().fromJson(getData(), StateApplyFailed.class);
            case PaygAuthenticationUpdateFailed:
                return new Gson().fromJson(getData(), PaygAuthenticationUpdateFailed.class);
            default: throw new RuntimeException("should not happen!");
        }
    }

    /**
     * @param dataIn The description to set.
     */
    public void setData(String dataIn) {
        this.data = dataIn;
    }

    /**
     * Get the type of this notification.
     * @return notification type
     */
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "type")
    public NotificationType getType() {
        return type;
    }

    /**
     * Sets this notifications type
     * @param typeIn notification type
     */
    public void setType(NotificationType typeIn) {
        this.type = typeIn;
    }

    /**
    * @return Returns the created date.
    */
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created")
    public Date getCreated() {
        return created;
    }

    /**
    * @param createdIn The created date to set.
    */
    public void setCreated(Date createdIn) {
        this.created = createdIn;
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
            .append(getId(), otherNotificationMessage.getId())
            .append(getData(), otherNotificationMessage.getData())
            .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getId())
            .append(getData())
            .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .append("data", getData())
            .toString();
    }


    /**
     * The enum type for a {@link NotificationMessage}
     */
    public enum NotificationMessageSeverity {
        info, warning, error
    }
}
