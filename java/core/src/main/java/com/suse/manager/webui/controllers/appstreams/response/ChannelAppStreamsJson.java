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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.webui.controllers.appstreams.response;

import com.redhat.rhn.domain.channel.Channel;

/**
 * A JSON object representation of an AppStream channel
 */
public class ChannelAppStreamsJson {

    /**
     * Instantiate a JSON object
     * @param channelIn the channel
     */
    public ChannelAppStreamsJson(Channel channelIn) {
        this(channelIn.getId(), channelIn.getLabel(), channelIn.getName());
    }

    /**
     * Instantiate a JSON object
     * @param idIn    channel id
     * @param labelIn channel label
     * @param nameIn  channel name
     */
    public ChannelAppStreamsJson(Long idIn, String labelIn, String nameIn) {
        this.id = idIn;
        this.label = labelIn;
        this.name = nameIn;
    }

    private final Long id;
    private final String label;
    private final String name;

    public Long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getName() {
        return name;
    }
}
