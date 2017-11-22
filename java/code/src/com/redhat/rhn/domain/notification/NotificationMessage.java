/**
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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * A notification NotificationMessage Object.
 */
@Entity
@Table(name = "susenotificationmessage")
public class NotificationMessage {

    private Long id;
    private NotificationMessageSeverity severity;
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
     * @param severityIn the severity of the message
     * @param descriptionIn the description of the message
     */
    public NotificationMessage(NotificationMessageSeverity severityIn, String descriptionIn) {
        this.severity = severityIn;
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

    /**
     * @return Returns the severity of the message to set.
     */
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "severity")
    public NotificationMessageSeverity getSeverity() {
        return severity;
    }

    /**
     * @param severityIn the severity of the message.
     */
    public void setSeverity(NotificationMessageSeverity severityIn) {
        this.severity = severityIn;
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


    /**
     * The enum type for a {@link Notification Message}
     */
    public enum NotificationMessageSeverity {
        info, warning, error
    }
}
