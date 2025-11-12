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
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.ssm.ChannelChangeAction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base class for all the type of channel changes
 */
abstract class AbstractChannelChange implements ChannelChange {

    private static final Logger LOGGER = LogManager.getLogger(AbstractChannelChange.class);

    /**
     * Retrieves all the child channels allowed for the specified change
     * @param user the user performing the operation
     * @param childChannelActionsMap the actions map
     * @param server the server affected by the change
     * @param newBaseChannel the new base channel
     * @param childrenByBaseMap the map of the child channels allowed for the current base
     * @return the set of relevant child channels
     */
    protected Set<Channel> getChildChannelsForChange(User user, Map<Long, ChannelChangeAction> childChannelActionsMap,
                                                     Server server, Channel newBaseChannel,
                                                     Map<String, List<Channel>> childrenByBaseMap) {
        Set<Channel> result = new HashSet<>();

        childChannelActionsMap.forEach((childId, action) -> {
            if (action == ChannelChangeAction.SUBSCRIBE) {
                List<Channel> accessibleChildren = childrenByBaseMap.computeIfAbsent(
                    newBaseChannel.getLabel(),
                    k -> ChannelFactory.getAccessibleChildChannels(newBaseChannel, user)
                );

                // Find the accessible child with a matching id
                accessibleChildren.stream()
                    .filter(ac -> ac.getId().equals(childId))
                    .findFirst()
                    .ifPresentOrElse(
                        result::add,
                        () -> LOGGER.warn("Child channel id={} not found in accessible children of {} for user={}",
                            childId, newBaseChannel.getName(), user.getLogin())
                    );
            }
            else if (action == ChannelChangeAction.NO_CHANGE) {
                // Find the server child channel with a matching id
                server.getChildChannels().stream()
                    .filter(cc -> cc.getId().equals(childId))
                    .findFirst()
                    .ifPresent(result::add);
            }
        });

        return result;
    }
}
