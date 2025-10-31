/*
 * Copyright (c) 2017--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.webui.utils.gson;

import com.redhat.rhn.domain.channel.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Channels JSON class
 */
public class ChannelsJson {

    private final BaseChannelJson base;
    private final List<ChildChannelJson> children;
    private final String activationKey;

    /**
     * Builds an instance
     * @param baseIn the base channel
     * @param childrenIn the list of children
     */
    public ChannelsJson(Channel baseIn, List<Channel> childrenIn) {
        this(baseIn, childrenIn, null, null);
    }

    /**
     * Builds an instance
     * @param baseIn the base channel
     * @param childrenIn the list of children
     * @param activationKeyIn the activation key
     */
    public ChannelsJson(Channel baseIn, List<Channel> childrenIn, String activationKeyIn) {
        this(baseIn, childrenIn, activationKeyIn, null);
    }

    /**
     * Builds an instance
     * @param baseIn the base channel
     * @param childrenIn the list of children
     * @param recommendedFlagsIn a map detailing which channels are recommended
     */
    public ChannelsJson(Channel baseIn, List<Channel> childrenIn, Map<Long, Boolean> recommendedFlagsIn) {
        this(baseIn, childrenIn, null, recommendedFlagsIn);
    }

    private ChannelsJson(Channel baseIn, List<Channel> childrenIn, String activationKeyIn,
                         Map<Long, Boolean> recommendedFlagsIn) {
        List<Long> recommendedChildrenId;

        this.activationKey = activationKeyIn;
        if (childrenIn != null) {
            Long parentIdIn = baseIn != null ? baseIn.getId() : null;

            recommendedChildrenId = new ArrayList<>();
            this.children = childrenIn.stream()
                .map(child -> {
                    boolean recommended = recommendedFlagsIn != null && recommendedFlagsIn.get(child.getId());
                    if (recommended) {
                        recommendedChildrenId.add(child.getId());
                    }

                    return new ChildChannelJson(child, parentIdIn, recommended);
                })
                .toList();
        }
        else {
            recommendedChildrenId = List.of();
            this.children = List.of();
        }

        if (baseIn != null) {
            this.base = new BaseChannelJson(baseIn, recommendedChildrenId);
        }
        else {
            this.base = null;
        }
    }

    public BaseChannelJson getBase() {
        return base;
    }

    public List<ChildChannelJson> getChildren() {
        return children;
    }

    public String getActivationKey() {
        return activationKey;
    }

    /**
     * Create an object from a channel set
     *
     * @param channels the channels
     * @return the channels json
     */
    public static ChannelsJson fromChannelSet(Set<Channel> channels) {
        return fromChannelSet(channels, null);
    }

    /**
     * Create an object from a channel set and an activation key
     *
     * @param channels the channels
     * @param activationKey the activation key
     * @return the channels json
     */
    public static ChannelsJson fromChannelSet(Set<Channel> channels, String activationKey) {
        Channel base = null;
        List<Channel> children = new ArrayList<>();

        for (Channel ch : channels) {
            if (ch.isBaseChannel()) {
                base = ch;
            }
            else {
                children.add(ch);
            }
        }

        return new ChannelsJson(base, children, activationKey);
    }
}
