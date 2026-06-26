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
import com.suse.manager.webui.utils.salt.custom.MinionStartupGrains;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom {@link MessageAction} for publishing Uyuni reactor events to an MQTT broker.
 *
 * <p>This action is registered alongside existing handlers in
 * {@link com.suse.manager.reactor.SaltReactor} and intercepts five key event types,
 * mapping each to a structured MQTT topic and JSON payload.</p>
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

        LOG.warn("MqttEventAction.execute called for message of type: {}", msg.getClass().getName());

        try {
            if (msg instanceof RegisterMinionEventMessage registerMsg) {
                handleRegisterMinion(registerMsg);
            }
            else if (msg instanceof JobReturnEventMessage jobMsg) {
                handleJobReturn(jobMsg);
            }
            else if (msg instanceof ApplyStatesEventMessage applyMsg) {
                handleApplyStates(applyMsg);
            }
            else if (msg instanceof ImageDeployedEventMessage imageMsg) {
                handleImageDeployed(imageMsg);
            }
            else if (msg instanceof BatchStartedEventMessage batchMsg) {
                handleBatchStarted(batchMsg);
            }
            else {
                LOG.debug("Unhandled event type in MqttEventAction: {}",
                        msg.getClass().getName());
            }
        }
        catch (Exception e) {
            LOG.error("Failed to process event in MqttEventAction: {}",
                    e.getMessage(), e);
        }
    }

    private void handleRegisterMinion(RegisterMinionEventMessage registerMsg) {
        Map<String, Object> data = new HashMap<>();
        data.put("minionId", registerMsg.getMinionId());
        registerMsg.getMinionStartupGrains().ifPresent(grains -> {
            grains.getMachineId().ifPresent(id -> data.put("machineId", id));
            data.put("saltbootInitrd", grains.getSaltbootInitrd());
            grains.getSuseManagerGrain()
                    .flatMap(MinionStartupGrains.SuseManagerGrain::getManagementKey)
                    .ifPresent(key -> data.put("managementKey", key));
        });
        mqttPublisherService.publish(topicPrefix + "/systems/registered", data);
    }

    private void handleJobReturn(JobReturnEventMessage jobMsg) {
        var event = jobMsg.getJobReturnEvent();
        if (event != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("minionId", event.getMinionId());
            data.put("jid", event.getJobId());
            if (event.getData() != null) {
                data.put("fun", event.getData().getFun());
                data.put("success", event.getData().isSuccess());
                data.put("retcode", event.getData().getRetcode());
                data.put("timestamp", event.getData().getTimestamp());
            }
            mqttPublisherService.publish(topicPrefix + "/jobs/returned", data);
        }
    }

    private void handleApplyStates(ApplyStatesEventMessage applyMsg) {
        Map<String, Object> data = new HashMap<>();
        data.put("serverId", applyMsg.getServerId());
        data.put("userId", applyMsg.getUserId());
        data.put("stateNames", applyMsg.getStateNames());
        data.put("forcePackageListRefresh",
                applyMsg.isForcePackageListRefresh());
        data.put("directCall", applyMsg.isDirectCall());
        mqttPublisherService.publish(topicPrefix + "/states/applied", data);
    }

    private void handleImageDeployed(ImageDeployedEventMessage imageMsg) {
        var event = imageMsg.getImageDeployedEvent();
        if (event != null) {
            Map<String, Object> data = new HashMap<>();
            event.getMachineId().ifPresent(
                    id -> data.put("machineId", id));
            data.put("grains", event.getGrains());
            mqttPublisherService.publish(topicPrefix + "/images/deployed", data);
        }
    }

    private void handleBatchStarted(BatchStartedEventMessage batchMsg) {
        var event = batchMsg.getBatchStartedEvent();
        if (event != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("jid", event.getJobId());
            if (event.getData() != null) {
                data.put("availableMinions",
                        event.getData().getAvailableMinions());
                data.put("downMinions",
                        event.getData().getDownMinions());
                data.put("timestamp",
                        event.getData().getTimestamp());
            }
            mqttPublisherService.publish(
                    topicPrefix + "/batches/started", data);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRunConcurrently() {
        return true;
    }
}
