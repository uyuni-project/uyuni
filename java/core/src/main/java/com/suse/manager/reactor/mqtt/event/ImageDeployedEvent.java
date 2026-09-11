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

import com.suse.manager.reactor.messaging.ImageDeployedEventMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Event published when an image is deployed to a system.
 */
public class ImageDeployedEvent implements MqttEvent {

    private final ImageDeployedEventMessage message;

    private ImageDeployedEvent(ImageDeployedEventMessage messageIn) {
        this.message = messageIn;
    }

    /**
     * Build the event, unless the message carries no deployment to report.
     * @param messageIn the reactor message describing the deployment
     * @return the event, or null when there is nothing to publish
     */
    public static ImageDeployedEvent from(ImageDeployedEventMessage messageIn) {
        return messageIn.getImageDeployedEvent() == null ? null : new ImageDeployedEvent(messageIn);
    }

    @Override
    public String getTopicSuffix() {
        return "images/deployed";
    }

    @Override
    public Map<String, Object> getPayload() {
        var event = message.getImageDeployedEvent();
        Map<String, Object> payload = new HashMap<>();
        event.getMachineId().ifPresent(id -> payload.put("machineId", id));
        payload.put("grains", event.getGrains());
        return payload;
    }
}
