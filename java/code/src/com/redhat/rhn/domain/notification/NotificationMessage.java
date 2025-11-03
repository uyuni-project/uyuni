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

package com.redhat.rhn.domain.notification;

import com.redhat.rhn.domain.notification.types.NotificationData;
import com.redhat.rhn.domain.notification.types.NotificationType;
import com.redhat.rhn.domain.notification.types.NotificationTypeAdapter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

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
    @GeneratedValue(generator = "nmsg_seq")
    @GenericGenerator(
            name = "nmsg_seq",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "suse_notif_message_id_seq"),
                    @Parameter(name = "increment_size", value = "1")
            })
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
        this.type = notificationData.getType();
        this.data = new NotificationTypeAdapter(type).toJson(notificationData);
    }

    /**
     * Get the notification specific data.
     * @return the notification data
     */
    @Transient
    public NotificationData getNotificationData() {
        return new NotificationTypeAdapter(type).fromJson(data);
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
    @Column(columnDefinition = "type")
    @Type(type = "com.redhat.rhn.domain.notification.types.NotificationTypeEnumType")
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
        if (!(other instanceof NotificationMessage otherNotificationMessage)) {
            return false;
        }
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
}
