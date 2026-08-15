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

import com.suse.manager.reactor.messaging.RegisterMinionEventMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Event published when a minion registers with the server.
 */
public class MinionRegisteredEvent implements MqttEvent {

    private final RegisterMinionEventMessage message;

    private MinionRegisteredEvent(RegisterMinionEventMessage messageIn) {
        this.message = messageIn;
    }

    /**
     * Build the event for the given message.
     * @param messageIn the reactor message describing the registration
     * @return the event
     */
    public static MinionRegisteredEvent from(RegisterMinionEventMessage messageIn) {
        return new MinionRegisteredEvent(messageIn);
    }

    @Override
    public String getTopicSuffix() {
        return "systems/registered";
    }

    @Override
    public Map<String, Object> getPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("minionId", message.getMinionId());
        // The management key is deliberately not published. It can be used to
        // register systems, so broadcasting it to every subscriber would hand
        // out a credential.
        message.getMinionStartupGrains().ifPresent(grains -> {
            grains.getMachineId().ifPresent(id -> payload.put("machineId", id));
            payload.put("saltbootInitrd", grains.getSaltbootInitrd());
        });
        return payload;
    }
}
