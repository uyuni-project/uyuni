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
 * Event message to trigger a package profile update for a server via the MessageQueue.
 */
public class UpdatePackageProfileEventMessage implements EventMessage {

    /* The id of the server to update the package profile */
    private Long serverId;

    /**
     * Construct a message to trigger a package profile update for a server given by id.
     *
     * @param serverIdIn the server id
     */
    public UpdatePackageProfileEventMessage(Long serverIdIn) {
        serverId = serverIdIn;
    }

    /**
     * Return the server id.
     *
     * @return server id
     */
    public Long getServerId() {
        return serverId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getUserId() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toText() {
        return toString();
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "UpdatePackageProfileEventMessage[serverId: " + serverId + "]";
    }
}
