/*
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
package com.suse.manager.reactor.messaging;

import com.redhat.rhn.common.messaging.EventMessage;

import com.suse.manager.webui.utils.salt.custom.MinionStartupGrains;

import java.util.Optional;
/**
 * Event for triggering creation of system records for salt minions.
 */
public class RegisterMinionEventMessage implements EventMessage {

    private String minionId;
    private Optional<MinionStartupGrains> minionStartupGrains;

    /**
     * Create a new event to trigger system registration.
     *
     * @param minionIdIn minion to register
     * @param minionStartupGrainsIn grains to be passed
     */
    public RegisterMinionEventMessage(String minionIdIn, Optional<MinionStartupGrains> minionStartupGrainsIn) {
        if (minionIdIn == null) {
            throw new IllegalArgumentException("minionId cannot be null");
        }
        this.minionId = minionIdIn;
        this.minionStartupGrains = minionStartupGrainsIn;
    }

    /**
     * Return null here since we don't necessarily have a user, it could be that we are just
     * synchronizing unregistered minions into the database.
     *
     * @return null since we don't have a user
     */
    public Long getUserId() {
        return null;
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
     * Return the Optional of MinionStartupGrains object
     *
     * @return minionStartupGrains
     */
    public Optional<MinionStartupGrains> getMinionStartupGrains() {
        return minionStartupGrains;
    }

    /**
     * {@inheritDoc}
     */
    public String toText() {
        return toString();
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "RegisterMinionEvent[minionId: " + minionId + "]";
    }
}
