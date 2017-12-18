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

package com.suse.manager.webui.utils.gson;

import com.redhat.rhn.domain.notification.NotificationMessage;

import com.redhat.rhn.domain.notification.types.NotificationData;
import com.redhat.rhn.domain.notification.types.NotificationType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Date;

/**
 * A notification JSONNotificationMessage Object.
 */
public class JSONNotificationMessage {

    private Long id;
    private NotificationMessage.NotificationMessageSeverity severity;
    private NotificationType type;
    private NotificationData data;
    private boolean isRead;
    private Date created;
    private Date modified;

    /**
     * Empty constructor
     */
    public JSONNotificationMessage() {
    }

    /**
     * Default constructor for a JSONNotificationMessage
     *@param nm the {@link NotificationMessage} source object
     *@param isReadIn the read/unread flag
     */
    public JSONNotificationMessage(NotificationMessage nm, boolean isReadIn) {
        this.id = nm.getId();
        this.severity = nm.getNotificationData().getSeverity();
        this.data = nm.getNotificationData();
        this.type = nm.getType();
        this.isRead = isReadIn;
        this.created = nm.getCreated();
    }

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param idIn The id to set.
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    public NotificationData getData() {
        return data;
    }

    public void setData(NotificationData data) {
        this.data = data;
    }

    /**
     * @return Returns the read.
     */
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
     * @return Returns the created date.
     */
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
     * @return Returns the modified date.
     */
    public Date getModified() {
        return modified;
    }

    /**
     * @param modifiedIn The modified date to set.
     */
    public void setModified(Date modifiedIn) {
        this.modified = modifiedIn;
    }

    public NotificationMessage.NotificationMessageSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(NotificationMessage.NotificationMessageSeverity severity) {
        this.severity = severity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof JSONNotificationMessage)) {
            return false;
        }
        JSONNotificationMessage otherNotificationMessage = (JSONNotificationMessage) other;
        return new EqualsBuilder()
            .append(getId(), otherNotificationMessage.getId())
            .append(getData(), otherNotificationMessage.getData())
            .append(getCreated(), otherNotificationMessage.getCreated())
            .append(getIsRead(), otherNotificationMessage.getIsRead())
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
            .append(getCreated())
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
            .append("created", getCreated())
            .append("isRead", getIsRead())
            .toString();
    }
}
