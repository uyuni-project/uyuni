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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.reactor.messaging.RegisterMinionEventMessage;
import com.suse.manager.webui.utils.salt.custom.MinionStartupGrains;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

/**
 * Unit test for {@link MqttEventAction}.
 */
public class MqttEventActionTest {

    private TestMqttPublisherService mockPublisherService;
    private MqttEventAction mqttEventAction;

    @BeforeEach
    public void setUp() {
        mockPublisherService = new TestMqttPublisherService();
        mqttEventAction = new MqttEventAction(mockPublisherService);
    }

    /**
     * Test that a RegisterMinionEventMessage is correctly mapped
     * to the systems/registered topic with the expected payload.
     */
    @Test
    public void testExecuteRegisterMinion() {
        MinionStartupGrains grains =
                new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId("test-machine-id")
                .saltbootInitrd(true)
                .createMinionStartUpGrains();

        RegisterMinionEventMessage msg =
                new RegisterMinionEventMessage("test-minion",
                        Optional.of(grains));
        mqttEventAction.execute(msg);

        assertEquals(mockPublisherService.getTopicPrefix() + "/systems/registered",
                mockPublisherService.lastTopic);
        assertNotNull(mockPublisherService.lastPayload);
        assertTrue(mockPublisherService.lastPayload instanceof Map);
        Map<?, ?> data = (Map<?, ?>) mockPublisherService.lastPayload;
        assertEquals("test-minion", data.get("minionId"));
        assertEquals("test-machine-id", data.get("machineId"));
        assertEquals(true, data.get("saltbootInitrd"));
    }

    /**
     * Test that an ApplyStatesEventMessage is correctly mapped
     * to the states/applied topic with the expected payload.
     */
    @Test
    public void testExecuteApplyStates() {
        ApplyStatesEventMessage msg =
                new ApplyStatesEventMessage(1001L, true,
                        "state1", "state2");
        mqttEventAction.execute(msg);

        assertEquals(mockPublisherService.getTopicPrefix() + "/states/applied",
                mockPublisherService.lastTopic);
        assertNotNull(mockPublisherService.lastPayload);
        assertTrue(mockPublisherService.lastPayload instanceof Map);
        Map<?, ?> data = (Map<?, ?>) mockPublisherService.lastPayload;
        assertEquals(1001L, data.get("serverId"));
        assertEquals(true, data.get("forcePackageListRefresh"));
    }

    /**
     * Test spy sub-class to avoid real network connections.
     */
    private static class TestMqttPublisherService
            extends MqttPublisherService {

        private String lastTopic;
        private Object lastPayload;

        TestMqttPublisherService() {
            super("tcp://dummy-broker:1883");
        }

        @Override
        public void publish(String topic, Object payload) {
            this.lastTopic = topic;
            this.lastPayload = payload;
        }

        @Override
        public void shutdown() {
            // No-op for test
        }
    }
}
