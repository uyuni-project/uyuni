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
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.ssm.ChannelChangeAction;
import com.redhat.rhn.manager.ssm.ChannelSelection;
import com.redhat.rhn.manager.ssm.ChannelSelectionResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Represent a channel change where the base changes from the given current base to a new base
 */
public class ExplicitBaseChange extends AbstractChannelChange {

    private static final Logger LOGGER = LogManager.getLogger(ExplicitBaseChange.class);

    private final Channel currentBase;

    private final long newBaseId;

    private final Map<Long, ChannelChangeAction> childChannelActions;

    /**
     * Default constructor, used by {@link ChannelChangeFactory}
     * @param currentBaseIn the current base
     * @param newBaseIdIn the new base id
     * @param childChannelActionsIn the map of actions applied to the children
     */
    ExplicitBaseChange(Channel currentBaseIn, long newBaseIdIn, Map<Long, ChannelChangeAction> childChannelActionsIn) {
        this.currentBase = currentBaseIn;
        this.newBaseId = newBaseIdIn;
        this.childChannelActions = childChannelActionsIn;
    }

    @Override
    public ChannelSelectionResult handleChange(User user, Server server, Map<String, List<Channel>> childrenByBaseMap) {
        if (!isNewBaseIsCompatible(user, server)) {
            String baseId = Optional.ofNullable(server.getBaseChannel()).map(b -> b.getId() + "").orElse("none");
            Long serverId = server.getId();

            LOGGER.error("New base id={} not compatible with base id={} for serverId={}", newBaseId, baseId, serverId);

            return new ChannelSelectionResult(server, "incompatible_base");
        }

        Channel newBaseChannel = ChannelFactory.lookupById(newBaseId);
        Set<Channel> childChannels = super.getChildChannelsForChange(user, childChannelActions, server, newBaseChannel,
            childrenByBaseMap);

        return new ChannelSelectionResult(server, new ChannelSelection(Optional.of(newBaseChannel), childChannels));
    }

    private boolean isNewBaseIsCompatible(User user, Server srv) {
        // If it's the same id it's compatible
        if (currentBase != null && currentBase.getId().equals(newBaseId)) {
            return true;
        }

        // If the system doesn't currently have a base
        if (currentBase == null) {
            // Check if the possible base channels for the system contains the new id
            return ChannelManager.listBaseChannelsForSystem(user, srv).stream()
                .anyMatch(abc -> abc.getId().equals(newBaseId));
        }

        // Otherwise verify if the new base is among the compatible channels list
        return ChannelManager.listCompatibleBaseChannelsForChannel(user, currentBase).stream()
            .anyMatch(cbc -> cbc.getId().equals(newBaseId));

    }

}
