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

import java.util.Map;

/**
 * Base interface for all MQTT events.
 */
public interface MqttEvent {

    /**
     * Returns the topic suffix for this event.
     * @return the topic suffix (e.g. "users/created")
     */
    String getTopicSuffix();

    /**
     * Returns the key-value payload map for the event.
     * @return the payload map
     */
    Map<String, Object> getPayload();
}
