/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.reactor;

import com.redhat.rhn.common.messaging.EventMessage;

/**
 * Trigger actions whenever a server's channel assignments were changed.
 */
public class ChannelChangedEvent implements EventMessage {

    private final long serverId;
    private final long userId;

    /**
     * Constructor for creating a {@link ChannelChangedEvent} for a given server.
     *
     * @param serverId the server id
     * @param userId the user id
     */
    public ChannelChangedEvent(long serverId, long userId) {
        this.serverId = serverId;
        this.userId = userId;
    }

    /**
     * Return the server id.
     *
     * @return the server id
     */
    public Long getServerId() {
        return serverId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getUserId() {
        return this.userId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toText() {
        return toString();
    }

    @Override
    public String toString() {
        return "ChannelChangedEvent[serverId: " + serverId + "]";
    }
}
