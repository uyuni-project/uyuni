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

package com.redhat.rhn.manager.ssm.channelchange;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.ssm.ChannelChangeAction;
import com.redhat.rhn.manager.ssm.ChannelSelection;
import com.redhat.rhn.manager.ssm.ChannelSelectionResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Represent a channel change where the base stays the same and only the children change
 */
public class OnlyChildChannelsChange extends AbstractChannelChange {

    private final Map<Long, ChannelChangeAction> childChannelActions;

    /**
     * Default constructor, used by {@link ChannelChangeFactory}
     * @param childChannelActionsIn the map of actions applied to the children
     */
    OnlyChildChannelsChange(Map<Long, ChannelChangeAction> childChannelActionsIn) {
       this.childChannelActions = childChannelActionsIn;
    }

    @Override
    public ChannelSelectionResult handleChange(User user, Server server, Map<String, List<Channel>> childrenByBaseMap) {
        Set<Channel> childChannels = super.getChildChannelsForChange(user, childChannelActions, server,
            server.getBaseChannel(), childrenByBaseMap);
        return new ChannelSelectionResult(server, new ChannelSelection(Optional.empty(), childChannels));
    }
}
