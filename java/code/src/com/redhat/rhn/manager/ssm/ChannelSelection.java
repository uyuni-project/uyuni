/**
 * Copyright (c) 2018 SUSE LLC
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

package com.redhat.rhn.manager.ssm;

import com.redhat.rhn.domain.channel.Channel;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Optional;
import java.util.Set;

/**
 * Represents a channel selection to be applied
 * @version $Rev$
 */
public class ChannelSelection {

    private Optional<Channel> newBaseChannel;
    private Set<Channel> childChannels;

    /**
     * Constructor for ChannelSelection
     * @param newBaseChannelIn the newBaseChannel
     * @param childChannelsIn the childChannels
     */
    public ChannelSelection(Optional<Channel> newBaseChannelIn, Set<Channel> childChannelsIn) {
        super();
        this.newBaseChannel = newBaseChannelIn;
        this.childChannels = childChannelsIn;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.newBaseChannel)
                .append(this.childChannels)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ChannelSelection other = (ChannelSelection) obj;

        return new EqualsBuilder()
                .append(this.newBaseChannel, other.newBaseChannel)
                .append(this.childChannels, other.childChannels)
                .isEquals();
    }

    /**
     * @return the newBaseChannel
     */
    public Optional<Channel> getNewBaseChannel() {
        return this.newBaseChannel;
    }

    /**
     * @return the childChannels
     */
    public Set<Channel> getChildChannels() {
        return this.childChannels;
    }

    @Override
    public String toString() {
        return "ChannelChange [newBaseChannel=" + newBaseChannel + ", childChannels=" +
                childChannels + "]";
    }

}
