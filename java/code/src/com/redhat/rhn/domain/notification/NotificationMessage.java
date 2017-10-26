package com.redhat.rhn.domain.notification;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Date;

/**
 * A notification NotificationMessage Object.
 */
public class NotificationMessage {

    private Long id;
    private String description;
    private boolean read;
    private Date created;
    private Date modified;

    /**
     * Empty constructor
     */
    public NotificationMessage() {
    }

    /**
     * Default constructor for a NotificationMessage
     *
     * @param descriptionIn the description of the message
     * @param readIn if the message is already read or not
     */
    public NotificationMessage(String descriptionIn, boolean readIn) {
        this.description = descriptionIn;
        this.read = readIn;
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
     * @return Returns the description.
     */
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
    public boolean isRead() {
        return read;
    }

    /**
     * @param readIn The read to set.
     */
    public void setRead(boolean readIn) {
        this.read = readIn;
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
        this.modified= modifiedIn;
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
            .append(getCreated(), otherNotificationMessage.getCreated())
            .append(isRead(), otherNotificationMessage.isRead())
            .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getDescription())
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
            .append("description", getDescription())
            .append("created", getCreated())
            .append("read", isRead())
            .toString();
    }
}
