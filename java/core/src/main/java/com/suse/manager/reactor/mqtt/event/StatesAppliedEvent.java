/*
 * Copyright (c) 2026 SUSE LLC
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
package com.suse.manager.reactor.mqtt.event;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Event published when Salt states are applied to a system.
 */
public class StatesAppliedEvent implements MqttEvent {

    private final ApplyStatesEventMessage message;

    private StatesAppliedEvent(ApplyStatesEventMessage messageIn) {
        this.message = messageIn;
    }

    /**
     * Build the event for the given message.
     * @param messageIn the reactor message describing the state application
     * @return the event
     */
    public static StatesAppliedEvent from(ApplyStatesEventMessage messageIn) {
        return new StatesAppliedEvent(messageIn);
    }

    @Override
    public String getTopicSuffix() {
        return "states/applied";
    }

    @Override
    public Map<String, Object> getPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("serverId", message.getServerId());
        payload.put("userId", message.getUserId());
        payload.put("stateNames", message.getStateNames());
        payload.put("forcePackageListRefresh", message.isForcePackageListRefresh());
        payload.put("directCall", message.isDirectCall());
        return payload;
    }
}
