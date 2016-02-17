/**
 * Copyright (c) 2016 SUSE LLC
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
package com.suse.manager.reactor.messaging;

import com.redhat.rhn.common.messaging.EventMessage;

/**
 * Event message to handle what needs to be done during minion start
 */
public class MinionStartEventMessage implements EventMessage {

    private String minionId;

    public MinionStartEventMessage(String minionId) {
        this.minionId = minionId;
    }

    /**
     * @return the minion id
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
    @Override
    public String toString() {
        return "MinionStartEventMessage[minionId: " + minionId + "]";
    }
}
