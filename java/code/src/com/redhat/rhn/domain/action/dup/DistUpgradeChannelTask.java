package com.redhat.rhn.domain.action.dup;

import java.io.Serializable;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.channel.Channel;

public class DistUpgradeChannelTask extends BaseDomainHelper implements Serializable {

    private static final long serialVersionUID = -5332431075711058873L;

    public static final char SUBSCRIBE = 'S';
    public static final char UNSUBSCRIBE = 'U';

    private DistUpgradeActionDetails details;
    private Channel channel;
    private char task;

    /**
     * @return the details
     */
    public DistUpgradeActionDetails getDetails() {
        return details;
    }
    /**
     * @param details the details to set
     */
    public void setDetails(DistUpgradeActionDetails details) {
        this.details = details;
    }
    /**
     * @return the channel
     */
    public Channel getChannel() {
        return channel;
    }
    /**
     * @param channel the channel to set
     */
    public void setChannel(Channel channel) {
        this.channel = channel;
    }
    /**
     * One of SUBSCRIBE/UNSUBSCRIBE.
     * @return the task
     */
    public char getTask() {
        return task;
    }
    /**
     * @param task the task to set
     */
    public void setTask(char task) {
        this.task = task;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((channel == null) ? 0 : channel.hashCode());
        result = prime * result + ((details == null) ? 0 : details.hashCode());
        result = prime * result + task;
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DistUpgradeChannelTask)) {
            return false;
        }
        DistUpgradeChannelTask other = (DistUpgradeChannelTask) obj;
        if (channel == null) {
            if (other.channel != null) {
                return false;
            }
        }
        else if (!channel.equals(other.channel)) {
            return false;
        }
        if (details == null) {
            if (other.details != null) {
                return false;
            }
        }
        else if (!details.equals(other.details)) {
            return false;
        }
        if (task != other.task) {
            return false;
        }
        return true;
    }
}
