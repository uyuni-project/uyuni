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
 * Event for CLM build started.
 */
public class ClmBuildStartedEvent implements MqttEvent {

    private final String projectLabel;
    private final String username;

    /**
     * Constructor.
     * @param projectLabelIn Content Lifecycle Project label
     * @param usernameIn username of the user who triggered the build
     */
    public ClmBuildStartedEvent(String projectLabelIn, String usernameIn) {
        this.projectLabel = projectLabelIn;
        this.username = usernameIn;
    }

    @Override
    public String getTopicSuffix() {
        return "clm/build_started";
    }

    @Override
    public Map<String, Object> getPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("projectLabel", projectLabel);
        payload.put("username", username);
        return payload;
    }
}
