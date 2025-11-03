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

import com.redhat.rhn.domain.user.User;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Type;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * A notification UserNotification Object.
 */
@Entity
@Table(name = "suseusernotification", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "message_id"})})
public class UserNotification {

    private Long id;
    private Long userId;
    private Long messageId;
    private boolean read = false;
    private NotificationMessage message;

    /**
     * Empty constructor
     */
    public UserNotification() {
    }

    /**
     * Default constructor for a UserNotification
     *
     * @param userIn the user
     * @param messageIn the message
     */
    public UserNotification(User userIn, NotificationMessage messageIn) {
        this.userId = userIn.getId();
        this.message = messageIn;
    }

    /**
     * @return Returns the id.
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "unsg_seq")
    @SequenceGenerator(name = "unsg_seq", sequenceName = "suse_user_notif_id_seq", allocationSize = 1)
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
     * @return Returns the user id.
     */
    @Column(name = "user_id")
    public Long getUserId() {
        return userId;
    }

    /**
     * @param userIdIn The user id to set.
     */
    public void setUserId(Long userIdIn) {
        this.userId = userIdIn;
    }

    /**
     * @return Returns the message id.
     */
    @Column(name = "message_id", insertable = false, updatable = false)
    public Long getMessageId() {
        return messageId;
    }

    /**
     * @param messageIdIn The message id to set.
     */
    public void setMessageId(Long messageIdIn) {
        this.messageId = messageIdIn;
    }

    /**
     * @return Returns the read.
     */
    @Type(type = "yes_no")
    @Column(name = "read")
    public boolean getRead() {
        return read;
    }

    /**
     * @param readIn The read to set.
     */
    public void setRead(boolean readIn) {
        this.read = readIn;
    }

    /**
     * Get the notification message
     *
     * @return the referenced message
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", referencedColumnName = "id")
    public NotificationMessage getMessage() {
        return message;
    }

    /**
     * @param messageIn the referenced message
     */
    public void setMessage(NotificationMessage messageIn) {
        this.message = messageIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof UserNotification otherUserNotification)) {
            return false;
        }
        return new EqualsBuilder()
                .append(getUserId(), otherUserNotification.getUserId())
                .append(getMessageId(), otherUserNotification.getMessageId())
                .append(getRead(), otherUserNotification.getRead())
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getUserId())
                .append(getMessageId())
                .append(getRead())
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("userId", getUserId())
                .append("messageId", getMessageId())
                .append("read", getRead())
                .toString();
    }
}
