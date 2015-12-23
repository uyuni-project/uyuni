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
 * TODO: Write a comment.
 */
public class UpdatePackageProfileEventMessage implements EventMessage {

    private Long serverId;
    private String minionId;

    /**
     * TODO: We want to change this to expect only the serverId as soon as we store the
     * minion ID in the database.
     */
    public UpdatePackageProfileEventMessage(Long serverIdIn, String minionIdIn) {
        serverId = serverIdIn;
        minionId = minionIdIn;
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
     * Return the minion id.
     *
     * @return minion id
     */
    public String getMinionId() {
        return minionId;
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
        return "UpdatePackageProfileEvent[minionId: " + minionId + "]";
    }
}
