/*
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
 * Event message to handle what needs to be done during minion start.
 */
public class MinionStartEventMessage implements EventMessage {

    private final String minionId;

    /**
     * @param minionIdIn the id of the minion that was started
     */
    public MinionStartEventMessage(String minionIdIn) {
        minionId = minionIdIn;
    }

    /**
     * @return the minion id
     */
    public String getMinionId() {
        return minionId;
    }

    @Override
    public Long getUserId() {
        return null;
    }

    @Override
    public String toText() {
        return toString();
    }

    @Override
    public String toString() {
        return "MinionStartEventMessage[minionId: " + minionId + "]";
    }
}
