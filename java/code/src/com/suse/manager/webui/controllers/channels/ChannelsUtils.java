/**
 * Copyright (c) 2019 SUSE LLC
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
package com.suse.manager.webui.controllers.channels;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.channel.ChannelManager;

import com.suse.manager.webui.utils.gson.ChannelsJson;

import java.util.List;
import java.util.Map;

/**
 * Useful channels methods for web apis
 */
public class ChannelsUtils {

    private ChannelsUtils() {
    }

    /**
     * Generate a {@link ChannelsJson} object with all accessible child channels for the given base channel
     *
     * @param base the base channel
     * @param user the current user
     * @return the ChannelsJson object containing all base:children channels
     */
    public static ChannelsJson generateChannelJson(Channel base, User user) {
        List<Channel> children = ChannelFactory.getAccessibleChildChannels(base, user);
        Map<Long, Boolean> recommendedFlags = ChannelManager.computeChannelRecommendedFlags(base, children.stream());
        ChannelsJson jsonChannel = new ChannelsJson();
        jsonChannel.setBase(base);
        jsonChannel.setChildrenWithRecommendedAndArch(children.stream(), recommendedFlags);
        return jsonChannel;
    }

    /**
     * Get a list of base {@link Channel} visible to the current user
     *
     * @param user the current user
     * @return the list of base Channel the user can access
     */
    public static List<Channel> getPossibleBaseChannels(User user) {
        return ChannelFactory.listSubscribableBaseChannels(user);
    }

}
