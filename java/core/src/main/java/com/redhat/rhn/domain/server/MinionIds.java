/*
 * Copyright (c) 2020 SUSE LLC
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

package com.redhat.rhn.domain.server;

/**
 * Class containing the ids of a minion.
 */
public class MinionIds {

    private Long serverId;
    private String minionId;

    /**
     * Constructor for MinionIds
     *
     * @param serverIdIn the server Id
     * @param minionIdIn the minion Id
     */
    public MinionIds(Long serverIdIn, String minionIdIn) {
        this.serverId = serverIdIn;
        this.minionId = minionIdIn;
    }

    /**
     * Getter for the server Id
     * @return the server Id
     */
    public Long getServerId() {
        return this.serverId;
    }

    /**
     * Getter for the minion Id
     * @return the minion Id
     */
    public String getMinionId() {
        return this.minionId;
    }

    @Override
    public String toString() {
        return "MinionIds [serverId=" + serverId + ", minionId=" + minionId + "]";
    }
}
