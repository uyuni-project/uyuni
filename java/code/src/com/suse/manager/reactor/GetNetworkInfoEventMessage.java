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
import com.suse.manager.reactor.utils.ValueMap;

/**
 * Triggers getting the network information from a minion.
 */
public class GetNetworkInfoEventMessage implements EventMessage {

    private String machineId;
    private String minionId;
    private ValueMap grains;

    public GetNetworkInfoEventMessage(String machineIdIn, String minionIdIn, ValueMap grainsIn) {
        machineId = machineIdIn;
        minionId = minionIdIn;
        grains = grainsIn;
    }

    @Override
    public String toText() {
        return null;
    }

    @Override
    public Long getUserId() {
        return null;
    }

    public String getMachineId() {
        return machineId;
    }

    public String getMinionId() {
        return minionId;
    }

    public ValueMap getGrains() {
        return grains;
    }
}
