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
package com.suse.manager.reactor.messaging;

import com.redhat.rhn.common.messaging.EventMessage;

/**
 * Trigger getting the hardware information from a minion.
 */
public class GetHardwareInfoEventMessage implements EventMessage {

    private Long serverId;

    /**
     * Create a new event to trigger retrieving the hardware information.
     *
     * @param serverIdIn minion to register
     */
    public GetHardwareInfoEventMessage(Long serverIdIn) {
        this.serverId = serverIdIn;
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

}
