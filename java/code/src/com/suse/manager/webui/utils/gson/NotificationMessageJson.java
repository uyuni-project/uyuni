/*
 * Copyright (c) 2017--2020 SUSE LLC
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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

/**
 * A notification NotificationMessageJson Object.
 */
public class NotificationMessageJson {

    private Long id;
    private NotificationMessage.NotificationMessageSeverity severity;
    private NotificationType type;
    private NotificationData data;
    private String summary;
    private String details;
    private boolean isRead;
    private Date created;
    private Date modified;

    /**
     * Empty constructor
     */
    public NotificationMessageJson() {
    }

    /**
     * Default constructor for a NotificationMessageJson
     *@param nm the {@link NotificationMessage} source object
     *@param isReadIn the read/unread flag
     */
    public NotificationMessageJson(NotificationMessage nm, boolean isReadIn) {
        this.id = nm.getId();
        this.data = nm.getNotificationData();
        this.severity = data.getSeverity();
        this.summary = data.getSummary();
        this.details = data.getDetails();
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

    /**
     * @return Returns the notification data.
     */
    public NotificationData getData() {
        return data;
    }

    /**
     * @param dataIn The notification data to set.
     */
    public void setData(NotificationData dataIn) {
        this.data = dataIn;
    }

    /**
     * @return the summary
     */
    public String getSummary() {
        return summary;
    }

    /**
     * @param summaryIn the summary to set
     */
    public void setSummary(String summaryIn) {
        this.summary = summaryIn;
    }

    /**
     * Gets the details.
     *
     * @return details
     */
    public String getDetails() {
        return details;
    }

    /**
     * Sets the details.
     *
     * @param detailsIn the details
     */
    public void setDetails(String detailsIn) {
        details = detailsIn;
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

    /**
     * @return Returns the severity.
     */
    public NotificationMessage.NotificationMessageSeverity getSeverity() {
        return severity;
    }

    /**
     * @param severityIn The severity to set.
     */
    public void setSeverity(NotificationMessage.NotificationMessageSeverity severityIn) {
        this.severity = severityIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof NotificationMessageJson)) {
            return false;
        }
        NotificationMessageJson otherNotificationMessage = (NotificationMessageJson) other;
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
