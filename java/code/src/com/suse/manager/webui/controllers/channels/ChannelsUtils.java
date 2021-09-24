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
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductChannel;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.channel.ChannelManager;

import com.suse.manager.webui.utils.gson.ChannelsJson;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

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

    /**
     * Returns a Stream of mandatory channels for a certain product, given its base channel in input
     *
     * @param baseChannel the product base channel
     * @return the Stream of mandatory channels
     */
    public static Stream<Channel> mandatoryChannelsByBaseChannel(Channel baseChannel) {
        if (!baseChannel.isBaseChannel()) {
            return Stream.empty();
        }

        // identify the product by the base channel name
        Optional<SUSEProduct> baseProduct = SUSEProductFactory.findProductByChannelLabel(baseChannel.getLabel());
        if (baseProduct.isEmpty()) {
            return Stream.empty();
        }

        return baseProduct.get().getSuseProductChannels().stream()
                .filter(pc -> pc.isMandatory())
                .map(SUSEProductChannel::getChannel)
                // filter out channels with different base than the given one
                .filter(c -> c.getParentChannel() == null ||
                        c.getParentChannel().getLabel().equals(baseChannel.getLabel()));
    }
}
