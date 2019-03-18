/**
 * Copyright (c) 2017 SUSE LLC
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

package com.suse.manager.webui.utils.gson;

import com.redhat.rhn.domain.channel.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Channels JSON class
 */
public class ChannelsJson {

    private String activationKey;
    private ChannelJson base;
    private List<ChannelJson> children;

    /**
     * The Channel(single) JSON class
     */
    public static class ChannelJson {

        private Long id;
        private String label;
        private String archLabel;

        private String name;
        private boolean custom;
        private boolean subscribable;
        private boolean recommended;
        private boolean isCloned;
        private Long compatibleChannelPreviousSelection;

        /**
         * Instantiates a new Channel json.
         *
         * @param idIn   the id
         * @param labelIn the label
         * @param nameIn the name
         * @param customIn custom channel flag
         * @param subscribableIn subscribable flag
         * @param recommendedIn the channel is recommended by its parent channel
         * @param compatibleChannelPreviousSelectionIn the compatible channel id of the previous selection
         * @param isClonedIn if the channel is clonned
         * @param archLabelIn the architecture label
         */
        public ChannelJson(Long idIn, String labelIn, String nameIn, boolean customIn, boolean subscribableIn,
                           boolean isClonedIn, String archLabelIn, boolean recommendedIn,
                           Long compatibleChannelPreviousSelectionIn) {
            this.id = idIn;
            this.label = labelIn;
            this.name = nameIn;
            this.custom = customIn;
            this.subscribable = subscribableIn;
            this.recommended = recommendedIn;
            this.compatibleChannelPreviousSelection = compatibleChannelPreviousSelectionIn;
            this.isCloned = isClonedIn;
            this.archLabel = archLabelIn;
        }

        /**
         * Instantiates a new Channel json.
         *
         * @param idIn   the id
         * @param labelIn the label
         * @param nameIn the name
         * @param customIn custom channel flag
         * @param subscribableIn subscribable flag
         * @param isClonedIn if the channel is clonned
         * @param archLabelIn the architecture label
         */
        public ChannelJson(Long idIn, String labelIn, String nameIn, boolean customIn, boolean subscribableIn,
                           boolean isClonedIn, String archLabelIn) {
            this(idIn, labelIn, nameIn, customIn, subscribableIn, isClonedIn, archLabelIn, false, null);
        }

        /**
         * @return the id
         */
        public Long getId() {
            return id;
        }

        /**
         * @return the label
         */
        public String getLabel() {
            return label;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }


        /**
         * @return custom to get
         */
        public boolean isCustom() {
            return custom;
        }

        /**
         * @return subscribable to get
         */
        public boolean isSubscribable() {
            return subscribable;
        }

        /**
         * @return recommented to get
         */
        public boolean isRecommended() {
            return recommended;
        }

        /**
         * @return compatibleChannelPreviousSelection to get
         */
        public Long getCompatibleChannelPreviousSelection() {
            return compatibleChannelPreviousSelection;
        }

        public String getArchLabel() {
            return archLabel;
        }

    }

    /**
     * @return the activation key
     */
    public String getActivationKey() {
        return activationKey;
    }

    /**
     * @param activationKeyIn the activation key
     */
    public void setActivationKey(String activationKeyIn) {
        this.activationKey = activationKeyIn;
    }

    /**
     * @return the base channel
     */
    public ChannelJson getBase() {
        return base;
    }

    /**
     * @param baseIn the base channel
     */
    public void setBase(Channel baseIn) {
        this.base = new ChannelJson(
                baseIn.getId(), baseIn.getLabel(), baseIn.getName(), baseIn.isCustom(), true, baseIn.isCloned(),
                baseIn.getChannelArch().getLabel());
    }

    /**
     * @param baseIn the base channel
     */
    public void setBaseWithArchLabel(Channel baseIn) {
        this.base = new ChannelJson(
                baseIn.getId(), baseIn.getLabel(), baseIn.getName(), baseIn.isCustom(), true,
                baseIn.isCloned(), baseIn.getChannelArch().getLabel());
    }

    /**
     * @return the child channels
     */
    public List<ChannelJson> getChildren() {
        return children;
    }

    /**
     * @param childrenIn the child channels
     */
    public void setChildren(Stream<Channel> childrenIn) {
        this.children = childrenIn.map(
                (c) -> new ChannelJson(c.getId(), c.getLabel(), c.getName(), c.isCustom(), true,
                        c.isCloned(), c.getChannelArch().getLabel()))
                .collect(Collectors.toList());
    }

    /**
     * @param childrenIn the child channels
     * @param recommendedFlags the map of channels with recommended flag value
     */
    public void setChildrenWithRecommendedAndArch(Stream<Channel> childrenIn, Map<Long, Boolean> recommendedFlags) {
        this.children = childrenIn.map((c) -> {
            ChannelJson channelWithArch = new ChannelJson(
                    c.getId(),
                    c.getLabel(),
                    c.getName(),
                    c.isCustom(),
                    true,
                    c.isCloned(),
                    c.getChannelArch().getLabel(),
                    recommendedFlags.get(c.getId()),
                    null
            );
            return channelWithArch;
        }).collect(Collectors.toList());
    }

    /**
     * Create an object from a channel set
     *
     * @param channels the channels
     * @return the channels json
     */
    public static ChannelsJson fromChannelSet(Set<Channel> channels) {
        ChannelsJson channelsJson = new ChannelsJson();

        List<Channel> children = new ArrayList<>();

        for (Channel ch : channels) {
            if (ch.isBaseChannel()) {
                channelsJson.setBase(ch);
            }
            else {
                children.add(ch);
            }
        }
        channelsJson.setChildren(children.stream());

        return channelsJson;
    }
}
