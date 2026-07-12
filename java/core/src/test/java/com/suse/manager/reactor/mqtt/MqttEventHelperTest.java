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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * Unit test for {@link MqttEventHelper}.
 */
public class MqttEventHelperTest {

    private TestMqttPublisherService testService;

    @BeforeEach
    public void setUp() {
        testService = new TestMqttPublisherService();
        MqttPublisherService.setInstance(testService);
    }

    @AfterEach
    public void tearDown() {
        MqttPublisherService.setInstance(null);
    }

    @Test
    public void testPublishUserCreated() {
        MqttEventHelper.publishUserCreated("johndoe", "admin", 123L);

        assertEquals(testService.getTopicPrefix() + "/users/created", testService.lastTopic);
        assertNotNull(testService.lastPayload);
        assertTrue(testService.lastPayload instanceof Map);

        Map<?, ?> data = (Map<?, ?>) testService.lastPayload;
        assertEquals("johndoe", data.get("username"));
        assertEquals("admin", data.get("creator"));
        assertEquals(123L, data.get("orgId"));
    }

    @Test
    public void testPublishOrgCreated() {
        MqttEventHelper.publishOrgCreated(456L, "My Company");

        assertEquals(testService.getTopicPrefix() + "/orgs/created", testService.lastTopic);
        assertNotNull(testService.lastPayload);
        assertTrue(testService.lastPayload instanceof Map);

        Map<?, ?> data = (Map<?, ?>) testService.lastPayload;
        assertEquals(456L, data.get("orgId"));
        assertEquals("My Company", data.get("orgName"));
    }

    @Test
    public void testPublishClmBuildStarted() {
        MqttEventHelper.publishClmBuildStarted("clm-proj-1", "admin");

        assertEquals(testService.getTopicPrefix() + "/clm/build_started", testService.lastTopic);
        assertNotNull(testService.lastPayload);
        assertTrue(testService.lastPayload instanceof Map);

        Map<?, ?> data = (Map<?, ?>) testService.lastPayload;
        assertEquals("clm-proj-1", data.get("projectLabel"));
        assertEquals("admin", data.get("username"));
    }

    @Test
    public void testPublishClmBuildCompleted() {
        MqttEventHelper.publishClmBuildCompleted("clm-proj-1", "v2", "admin");

        assertEquals(testService.getTopicPrefix() + "/clm/build_completed", testService.lastTopic);
        assertNotNull(testService.lastPayload);
        assertTrue(testService.lastPayload instanceof Map);

        Map<?, ?> data = (Map<?, ?>) testService.lastPayload;
        assertEquals("clm-proj-1", data.get("projectLabel"));
        assertEquals("v2", data.get("version"));
        assertEquals("admin", data.get("username"));
    }

    @Test
    public void testPublishWhenServiceNotInitialized() {
        MqttPublisherService.setInstance(null);

        // This should not throw exceptions, but simply fail-silent/skip
        MqttEventHelper.publishOrgCreated(1L, "Test Org");

        assertNull(testService.lastTopic);
        assertNull(testService.lastPayload);
    }

    @Test
    public void testEventFiltering() {
        System.setProperty("uyuni.mqtt.events.enabled", "orgs.created,users/created");
        try {
            TestMqttPublisherService filterService = new TestMqttPublisherService();
            MqttPublisherService.setInstance(filterService);

            // This should be published (orgs.created is in filter)
            MqttEventHelper.publishOrgCreated(123L, "Filtered Org");
            assertEquals(filterService.getTopicPrefix() + "/orgs/created", filterService.lastTopic);

            // Reset spy state
            filterService.lastTopic = null;
            filterService.lastPayload = null;

            // This should be published (users/created is in filter)
            MqttEventHelper.publishUserCreated("john", "admin", 1L);
            assertEquals(filterService.getTopicPrefix() + "/users/created", filterService.lastTopic);

            // Reset spy state
            filterService.lastTopic = null;
            filterService.lastPayload = null;

            // This should NOT be published (clm/build_started is NOT in filter)
            MqttEventHelper.publishClmBuildStarted("clm-1", "admin");
            assertNull(filterService.lastTopic);
        }
        finally {
            System.clearProperty("uyuni.mqtt.events.enabled");
            MqttPublisherService.setInstance(testService);
        }
    }

    @Test
    public void testBrokerCredentialsParsing() {
        System.setProperty("uyuni.mqtt.broker.username", "myuser");
        System.setProperty("uyuni.mqtt.broker.password", "mypass");
        try {
            TestMqttPublisherService credsService = new TestMqttPublisherService();
            assertEquals("myuser", credsService.getUsername());
            assertEquals("mypass", credsService.getPassword());
        }
        finally {
            System.clearProperty("uyuni.mqtt.broker.username");
            System.clearProperty("uyuni.mqtt.broker.password");
        }
    }

    private static class TestMqttPublisherService extends MqttPublisherService {
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
