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
package com.suse.manager.reactor.mqtt;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.reactor.messaging.BatchStartedEventMessage;
import com.suse.manager.reactor.messaging.ImageDeployedEventMessage;
import com.suse.manager.reactor.messaging.JobReturnEventMessage;
import com.suse.manager.reactor.messaging.RegisterMinionEventMessage;
import com.suse.manager.reactor.mqtt.event.BatchStartedEvent;
import com.suse.manager.reactor.mqtt.event.ImageDeployedEvent;
import com.suse.manager.reactor.mqtt.event.JobReturnedEvent;
import com.suse.manager.reactor.mqtt.event.MinionRegisteredEvent;
import com.suse.manager.reactor.mqtt.event.MqttEvent;
import com.suse.manager.reactor.mqtt.event.StatesAppliedEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Custom {@link MessageAction} for publishing Uyuni reactor events to an MQTT broker.
 *
 * <p>This action is registered alongside existing handlers in
 * {@link com.suse.manager.reactor.SaltReactor}. Each supported reactor message is
 * translated into an {@link MqttEvent}, which owns both its topic suffix and its
 * payload, so adding a new topic means adding a class rather than extending this one.</p>
 */
public class MqttEventAction implements MessageAction {

    private static final Logger LOG = LogManager.getLogger(MqttEventAction.class);

    private final MqttPublisherService mqttPublisherService;
    private final String topicPrefix;

    /**
     * Constructor taking the publisher service.
     * @param mqttPublisherServiceIn the publisher service instance
     */
    public MqttEventAction(MqttPublisherService mqttPublisherServiceIn) {
        this.mqttPublisherService = mqttPublisherServiceIn;
        this.topicPrefix = mqttPublisherServiceIn.getTopicPrefix();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(EventMessage msg) {
        if (msg == null) {
            return;
        }

        try {
            MqttEvent event = toMqttEvent(msg);
            if (event == null) {
                LOG.debug("Unhandled or empty event type in MqttEventAction: {}", msg.getClass().getName());
                return;
            }

            String topic = topicPrefix + "/" + event.getTopicSuffix();
            if (!mqttPublisherService.isEventEnabled(topic)) {
                LOG.debug("Event of type {} is disabled by configuration.", msg.getClass().getName());
                return;
            }

            LOG.debug("MqttEventAction.execute called for message of type: {}", msg.getClass().getName());
            // Deferred until the transaction commits, for the same reason as the
            // events raised from application code: the handlers registered
            // alongside this action may still be persisting the change, and a
            // rollback must not leave an event announcing it. Publishes
            // immediately when no transaction is active.
            MqttEventHelper.publishAfterCommit(event);
        }
        catch (Exception e) {
            LOG.error("Failed to process event in MqttEventAction: {}", e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRunConcurrently() {
        return true;
    }

    private MqttEvent toMqttEvent(EventMessage msg) {
        if (msg instanceof RegisterMinionEventMessage registerMsg) {
            return MinionRegisteredEvent.from(registerMsg);
        }
        else if (msg instanceof JobReturnEventMessage jobMsg) {
            return JobReturnedEvent.from(jobMsg);
        }
        else if (msg instanceof ApplyStatesEventMessage applyMsg) {
            return StatesAppliedEvent.from(applyMsg);
        }
        else if (msg instanceof ImageDeployedEventMessage imageMsg) {
            return ImageDeployedEvent.from(imageMsg);
        }
        else if (msg instanceof BatchStartedEventMessage batchMsg) {
            return BatchStartedEvent.from(batchMsg);
        }
        return null;
    }
}
