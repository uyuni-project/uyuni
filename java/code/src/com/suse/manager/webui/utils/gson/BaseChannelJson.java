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

import java.util.List;

/**
 * A specialized {@link ChannelJson} implementation to represent base channels
 */
public class BaseChannelJson extends ChannelJson {

    private final List<Long> recommendedChildrenId;

    /**
     * Builds an instance
     * @param baseIn the base channel
     * @param recommendedChildrenIdIn a list of recommended child channels
     */
    public BaseChannelJson(Channel baseIn, List<Long> recommendedChildrenIdIn) {
        super(baseIn, true);

        this.recommendedChildrenId = recommendedChildrenIdIn;
    }

    public List<Long> getRecommendedChildrenId() {
        return recommendedChildrenId;
    }
}
