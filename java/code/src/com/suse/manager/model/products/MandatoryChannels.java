/**
 * Copyright (c) 2013 SUSE LLC
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

package com.suse.manager.model.products;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of Channels that are mandatory for a Product.
 */
@Root(name = "mandatory_channels")
public class MandatoryChannels {

    /** The channel. */
    @ElementList(inline = true, required = false)
    private List<Channel> channel;

    /**
     * Default constructor.
     */
    public MandatoryChannels() {
        // required by Simple XML
    }

    /**
     * Instantiates a new mandatory channels object.
     * @param channelsIn the channel
     */
    public MandatoryChannels(List<Channel> channelsIn) {
        channel = channelsIn;
    }

    /**
     * Gets the channels.
     * @return the channels
     */
    public List<Channel> getChannels() {
        if (channel == null) {
            channel = new ArrayList<Channel>();
        }
        return this.channel;
    }
}
