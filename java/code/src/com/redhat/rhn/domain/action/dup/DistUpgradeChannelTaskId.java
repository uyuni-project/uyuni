/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.redhat.rhn.domain.action.dup;

import com.redhat.rhn.domain.channel.Channel;

import java.io.Serializable;
import java.util.Objects;

public class DistUpgradeChannelTaskId implements Serializable {
    private DistUpgradeActionDetails details;
    private Channel channel;

    /**
     * Constructor
     */
    public DistUpgradeChannelTaskId() { }

    /**
     * Constructor
     * @param detailsIn the details
     * @param channelIn the channel
     */
    public DistUpgradeChannelTaskId(DistUpgradeActionDetails detailsIn, Channel channelIn) {
        details = detailsIn;
        channel = channelIn;
    }

    public DistUpgradeActionDetails getDetails() {
        return details;
    }

    public void setDetails(DistUpgradeActionDetails detailsIn) {
        details = detailsIn;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channelIn) {
        channel = channelIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof DistUpgradeChannelTaskId that)) {
            return false;
        }
        return Objects.equals(details, that.details) &&
                Objects.equals(channel, that.channel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(details, channel);
    }
}
