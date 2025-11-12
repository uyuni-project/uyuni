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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Represent a channel change where the base changes from the default base to an explicit new base
 */
public class DefaultBaseChange extends AbstractChannelChange {
    private static final Logger LOGGER = LogManager.getLogger(DefaultBaseChange.class);

    private final Set<PossibleChange> baseChangeSet;

    DefaultBaseChange() {
        this.baseChangeSet = new HashSet<>();
    }

    /**
     * Add a possible change
     * @param newBaseId the new base id
     * @param childChannelActions the map of actions applied to the children
     */
    public void addChange(long newBaseId, Map<Long, ChannelChangeAction> childChannelActions) {
        this.baseChangeSet.add(new PossibleChange(newBaseId, childChannelActions));
    }

    @Override
    public ChannelSelectionResult handleChange(User user, Server server, Map<String, List<Channel>> childrenByBaseMap) {

        // Figure out the base channel
        Channel guessedChannel = ChannelManager.guessServerBaseChannel(user, server.getId()).orElse(null);
        if (guessedChannel == null) {
            LOGGER.error("Could not guess base channel for serverId={} user={}", server.getId(), user.getLogin());
            return new ChannelSelectionResult(server, "no_base_channel_guess");
        }

        // Extract the change to apply from the available
        PossibleChange actualChange = selectChangeByDefaultBase(guessedChannel);
        if (actualChange == null) {
            LOGGER.warn("No base channel change found for serverId={}", server.getId());
            return new ChannelSelectionResult(server, "no_base_change_found");
        }

        Channel newBaseChannel = ChannelFactory.lookupById(actualChange.newBaseId());
        Map<Long, ChannelChangeAction> childChannelActionsMap = actualChange.childChannelActions();

        Set<Channel> childChannels = super.getChildChannelsForChange(user, childChannelActionsMap, server,
            newBaseChannel, childrenByBaseMap);

        return new ChannelSelectionResult(server, new ChannelSelection(Optional.of(newBaseChannel), childChannels));
    }

    private PossibleChange selectChangeByDefaultBase(Channel guessedChannel) {
        return baseChangeSet.stream()
            .filter(change -> change.newBaseId() == guessedChannel.getId())
            .findFirst()
            .orElse(null);
    }

    private record PossibleChange(long newBaseId, Map<Long, ChannelChangeAction> childChannelActions) { }
}
