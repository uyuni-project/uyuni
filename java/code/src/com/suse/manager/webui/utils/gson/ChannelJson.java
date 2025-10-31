/*
 * Copyright (c) 2025 SUSE LLC
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
import com.redhat.rhn.frontend.dto.EssentialChannelDto;

/**
 * The Channel(single) JSON class
 */
public class ChannelJson {

    private final Long id;
    private final String label;
    private final String archLabel;
    private final String name;
    private final String standardizedName;
    private final boolean custom;
    private final boolean subscribable;
    private final boolean recommended;
    private final boolean isCloned;
    private final Long compatibleChannelPreviousSelection;

    /**
     * Instantiates a new instance from a {@link Channel} object
     *
     * @param channelIn the channel object
     * @param subscribableIn subscribable flag
     */
    public ChannelJson(Channel channelIn, boolean subscribableIn) {
        this(channelIn.getId(), channelIn.getLabel(), channelIn.getChannelArch().getLabel(), channelIn.getName(),
            channelIn.isCustom(), subscribableIn, false, channelIn.isCloned(), null);
    }

    /**
     * Instantiates a new instance from a {@link Channel} object
     *
     * @param channelIn the channel object
     * @param subscribableIn subscribable flag
     * @param recommendedIn the channel is recommended by its parent channel
     * @param compatibleChannelPreviousSelectionIn the compatible channel id of the previous selection
     */
    public ChannelJson(Channel channelIn, boolean subscribableIn, boolean recommendedIn,
                       Long compatibleChannelPreviousSelectionIn) {
        this(channelIn.getId(), channelIn.getLabel(), channelIn.getChannelArch().getLabel(), channelIn.getName(),
            channelIn.isCustom(), subscribableIn, recommendedIn, channelIn.isCloned(),
            compatibleChannelPreviousSelectionIn);
    }

    /**
     * Instantiates a new instance from a {@link EssentialChannelDto} object
     *
     * @param channelIn the channel object
     * @param subscribableIn subscribable flag
     */
    public ChannelJson(EssentialChannelDto channelIn, boolean subscribableIn) {
        this(channelIn.getId(), channelIn.getLabel(), channelIn.getArchLabel(), channelIn.getName(),
            channelIn.isCustom(), subscribableIn, false, channelIn.isCloned(), null);
    }

    /**
     * Instantiates a new Channel json.
     * @param idIn the id
     * @param labelIn the label
     * @param archLabelIn the architecture label
     * @param nameIn the name
     * @param customIn custom channel flag
     * @param subscribableIn subscribable flag
     * @param recommendedIn the channel is recommended by its parent channel
     * @param isClonedIn if the channel is cloned
     * @param compatibleChannelPreviousSelectionIn the compatible channel id of the previous selection
     */
    private ChannelJson(Long idIn, String labelIn, String archLabelIn, String nameIn, boolean customIn,
                        boolean subscribableIn, boolean recommendedIn, boolean isClonedIn,
                        Long compatibleChannelPreviousSelectionIn) {
        this.id = idIn;
        this.label = labelIn;
        this.name = nameIn;
        this.standardizedName = nameIn != null ? nameIn.toLowerCase() : null;
        this.custom = customIn;
        this.subscribable = subscribableIn;
        this.recommended = recommendedIn;
        this.compatibleChannelPreviousSelection = compatibleChannelPreviousSelectionIn;
        this.isCloned = isClonedIn;
        this.archLabel = archLabelIn;
    }

    public Long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getArchLabel() {
        return archLabel;
    }

    public String getName() {
        return name;
    }

    public String getStandardizedName() {
        return standardizedName;
    }

    public boolean isCustom() {
        return custom;
    }

    public boolean isSubscribable() {
        return subscribable;
    }

    public boolean isRecommended() {
        return recommended;
    }

    public boolean isCloned() {
        return isCloned;
    }

    public Long getCompatibleChannelPreviousSelection() {
        return compatibleChannelPreviousSelection;
    }
}
