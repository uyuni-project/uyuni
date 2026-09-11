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
 * Event for organization creation.
 */
public class OrgCreatedEvent implements MqttEvent {

    private final Long orgId;
    private final String orgName;

    /**
     * Constructor.
     * @param orgIdIn organization ID
     * @param orgNameIn organization name
     */
    public OrgCreatedEvent(Long orgIdIn, String orgNameIn) {
        this.orgId = orgIdIn;
        this.orgName = orgNameIn;
    }

    @Override
    public String getTopicSuffix() {
        return "orgs/created";
    }

    @Override
    public Map<String, Object> getPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("orgId", orgId);
        payload.put("orgName", orgName);
        return payload;
    }
}
