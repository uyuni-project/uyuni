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
 * Trigger getting the hardware information from a minion.
 */
public class GetHardwareInfoEventMessage implements EventMessage {

    private Long serverId;
    private boolean skipCpu;

    /**
     * Create a new event to trigger retrieving the hardware information.
     *
     * @param minionIdIn minion to register
     * @param machineIdIn the machine id
     * @param skipCpuIn don't retrieve CPU info from minion
     */
    public GetHardwareInfoEventMessage(Long serverIdIn, boolean skipCpuIn) {
        this.serverId = serverIdIn;
        this.skipCpu = skipCpuIn;
    }

    @Override
    public String toText() {
        return toString();
    }

    @Override
    public Long getUserId() {
        return null;
    }

    /**
     * @return The string representation of this object.
     */
    public String toString() {
        return "GetHardwareInfoEventMessage[serverId: " + serverId + "]";
    }

    /**
     * @return the serverId
     */
    public Long getServerId() {
        return serverId;
    }

    /**
     * @return don't get the CPU information from the minion.
     */
    public boolean isSkipCpu() {
        return skipCpu;
    }
}
