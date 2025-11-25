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
import com.redhat.rhn.manager.ssm.ChannelSelectionResult;

import java.util.List;
import java.util.Map;

/**
 * Interface for handling a channel change
 */
@FunctionalInterface
public interface ChannelChange {

    /**
     * Handle the current change on the given server,
     * @param user the user performing the operation
     * @param server the server
     * @param childrenByBaseMap the child channels allowed for the current base
     * @return a {@link ChannelSelectionResult} describing the result of the operation
     */
    ChannelSelectionResult handleChange(User user, Server server, Map<String, List<Channel>> childrenByBaseMap);
}
