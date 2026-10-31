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

import com.suse.manager.reactor.messaging.JobReturnEventMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Event published when a Salt job returns.
 */
public class JobReturnedEvent implements MqttEvent {

    private final JobReturnEventMessage message;

    private JobReturnedEvent(JobReturnEventMessage messageIn) {
        this.message = messageIn;
    }

    /**
     * Build the event, unless the message carries no job return to report.
     * @param messageIn the reactor message describing the job return
     * @return the event, or null when there is nothing to publish
     */
    public static JobReturnedEvent from(JobReturnEventMessage messageIn) {
        return messageIn.getJobReturnEvent() == null ? null : new JobReturnedEvent(messageIn);
    }

    @Override
    public String getTopicSuffix() {
        return "jobs/returned";
    }

    @Override
    public Map<String, Object> getPayload() {
        var event = message.getJobReturnEvent();
        Map<String, Object> payload = new HashMap<>();
        payload.put("minionId", event.getMinionId());
        payload.put("jid", event.getJobId());
        if (event.getData() != null) {
            payload.put("fun", event.getData().getFun());
            payload.put("success", event.getData().isSuccess());
            payload.put("retcode", event.getData().getRetcode());
            payload.put("timestamp", event.getData().getTimestamp());
        }
        return payload;
    }
}
