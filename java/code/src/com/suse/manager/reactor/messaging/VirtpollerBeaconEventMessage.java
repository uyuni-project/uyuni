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

import com.suse.manager.webui.utils.salt.custom.VirtpollerData;

/**
 * Virtpoller Beacon Event message to handle
 */
public class VirtpollerBeaconEventMessage implements EventMessage {

    private final VirtpollerData data;

    private final String minionId;

    /**
     * Constructor
     *
     * @param minionIdIn the minionId
     * @param dataIn the virtpoller data from salt
     */
    public VirtpollerBeaconEventMessage(String minionIdIn, VirtpollerData dataIn) {
        minionId = minionIdIn;
        data = dataIn;
    }

    /**
     * @return the minionId
     */
    public String getMinionId() {
        return minionId;
    }

    /**
     * @return the virtpoller data from salt
     */
    public VirtpollerData getVirtpollerData() {
        return data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "VirtpollerBeaconEventMessage[minionId: " + minionId + "]";
    }
}
