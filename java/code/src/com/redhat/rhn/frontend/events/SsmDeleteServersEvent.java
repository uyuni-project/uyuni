/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.events;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.SystemManager;

import java.util.List;

/**
 * Event fired to carry the information necessary to perform subscription changes
 * for servers in the SSM.
 *
 * @see com.redhat.rhn.frontend.events.SsmChangeChannelSubscriptionsAction
 */
public class SsmDeleteServersEvent implements EventMessage {

    private Long userId;
    private List<Long> sids;
    private SystemManager.ServerCleanupType serverCleanupType;
    /**
     * Creates a new SSM server delete event to fire across the message bus.
     *
     * @param userIn    user making the changes; cannot be <code>null</code>
     * @param sidsIn server ids to delete; cannot be <code>null</code>
     * @param serverCleanupTypeIn how to clean up the system (if needed)
     */
    public SsmDeleteServersEvent(User userIn,
                                 List<Long> sidsIn,
                                 SystemManager.ServerCleanupType serverCleanupTypeIn
    ) {
        if (userIn == null) {
            throw new IllegalArgumentException("user cannot be null");
        }

        if (sidsIn == null) {
            throw new IllegalArgumentException("Server Id list cannot be null");
        }

        this.userId = userIn.getId();
        this.sids = sidsIn;
        this.serverCleanupType = serverCleanupTypeIn;
    }

    /**
     * @return will not be <code>null</code>
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * @return will not be <code>null</code>
     */
    public List<Long> getSids() {
        return sids;
    }

    /** {@inheritDoc} */
    public String toText() {
        return toString();
    }

    /**
     * @return how to clean up the system (if needed)
     */
    public SystemManager.ServerCleanupType getServerCleanupType() {
        return serverCleanupType;
    }

    /** {@inheritDoc} */
    public String toString() {
        return "SsmChannelSubscriptionsEvent[User Id: " + userId +
            ", Delete Count: " + sids.size() + "]";
    }
}
