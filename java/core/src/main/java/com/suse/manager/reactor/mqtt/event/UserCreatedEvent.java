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

import java.util.HashMap;
import java.util.Map;

/**
 * Event for user registration.
 */
public class UserCreatedEvent implements MqttEvent {

    private final String username;
    private final Long userId;
    private final Long orgId;

    /**
     * Constructor.
     * @param usernameIn login of the created user
     * @param userIdIn ID of the created user
     * @param orgIdIn organization ID
     */
    public UserCreatedEvent(String usernameIn, Long userIdIn, Long orgIdIn) {
        this.username = usernameIn;
        this.userId = userIdIn;
        this.orgId = orgIdIn;
    }

    @Override
    public String getTopicSuffix() {
        return "users/created";
    }

    @Override
    public Map<String, Object> getPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("userId", userId);
        payload.put("orgId", orgId);
        return payload;
    }
}
