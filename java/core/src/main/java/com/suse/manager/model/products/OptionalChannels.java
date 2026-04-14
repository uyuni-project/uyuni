/*
 * Copyright (c) 2013--2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.model.products;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of Channels that are optional for a Product.
 */
public class OptionalChannels {

    /** The channel. */
    private List<Channel> channel;

    /**
     * Default constructor.
     */
    public OptionalChannels() {
        // required by Simple XML
    }

    /**
     * Instantiates a new optional channels object.
     * @param channelsIn the channels
     */
    public OptionalChannels(List<Channel> channelsIn) {
        channel = channelsIn;
    }

    /**
     * Gets the channels.
     * @return the channels
     */
    public List<Channel> getChannels() {
        if (channel == null) {
            channel = new ArrayList<>();
        }
        return this.channel;
    }
}
