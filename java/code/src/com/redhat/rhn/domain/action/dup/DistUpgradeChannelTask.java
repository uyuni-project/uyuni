/**
 * Copyright (c) 2012 Novell
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
package com.redhat.rhn.domain.action.dup;

import java.io.Serializable;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.channel.Channel;

/**
 * Class representation of a task to perform during a distribution upgrade.
 */
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
     * @param detailsIn the details to set
     */
    public void setDetails(DistUpgradeActionDetails detailsIn) {
        this.details = detailsIn;
    }
    /**
     * @return the channel
     */
    public Channel getChannel() {
        return channel;
    }
    /**
     * @param channelIn the channel to set
     */
    public void setChannel(Channel channelIn) {
        this.channel = channelIn;
    }
    /**
     * One of SUBSCRIBE/UNSUBSCRIBE.
     * @return the task
     */
    public char getTask() {
        return task;
    }
    /**
     * @param taskIn the task to set
     */
    public void setTask(char taskIn) {
        this.task = taskIn;
    }

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
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
