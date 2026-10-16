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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link MqttPublisherService} configuration handling.
 *
 * <p>The service is exercised through a subclass that suppresses publishing, so
 * no broker connection is needed.</p>
 */
public class MqttPublisherServiceTest {

    private static final String BROKER = "tcp://dummy-broker:1883";
    private static final String TLS_BROKER = "ssl://dummy-broker:8883";

    @AfterEach
    public void tearDown() {
        System.clearProperty("uyuni.mqtt.events.enabled");
        System.clearProperty("uyuni.mqtt.broker.username");
        System.clearProperty("uyuni.mqtt.broker.password");
        System.clearProperty("uyuni.mqtt.qos");
        System.clearProperty("uyuni.mqtt.queue.limit");
        System.clearProperty("uyuni.mqtt.tls.ca.file");
        System.clearProperty("uyuni.mqtt.tls.keystore.file");
        System.clearProperty("uyuni.mqtt.tls.keystore.password");
        MqttPublisherService.setInstance(null);
    }

    /**
     * Every event is published when no filter is configured.
     */
    @Test
    public void testAllEventsEnabledWithoutFilter() {
        TestPublisher service = new TestPublisher();
        assertTrue(service.isEventEnabled(service.getTopicPrefix() + "/systems/registered"));
        assertTrue(service.isEventEnabled(service.getTopicPrefix() + "/clm/build_started"));
        assertTrue(service.isEventEnabled("anything/at/all"));
    }

    /**
     * A filter written with dots matches a topic written with slashes.
     */
    @Test
    public void testFilterAcceptsDotNotation() {
        System.setProperty("uyuni.mqtt.events.enabled", "systems.registered,jobs.returned");
        TestPublisher service = new TestPublisher();
        assertTrue(service.isEventEnabled(service.getTopicPrefix() + "/systems/registered"));
        assertTrue(service.isEventEnabled(service.getTopicPrefix() + "/jobs/returned"));
        assertFalse(service.isEventEnabled(service.getTopicPrefix() + "/states/applied"));
    }

    /**
     * The same filter may also be written with slashes.
     */
    @Test
    public void testFilterAcceptsSlashNotation() {
        System.setProperty("uyuni.mqtt.events.enabled", "users/created");
        TestPublisher service = new TestPublisher();
        assertTrue(service.isEventEnabled(service.getTopicPrefix() + "/users/created"));
        assertFalse(service.isEventEnabled(service.getTopicPrefix() + "/orgs/created"));
    }

    /**
     * The filter is matched without regard to case, and tolerates spacing.
     */
    @Test
    public void testFilterIgnoresCaseAndSpacing() {
        System.setProperty("uyuni.mqtt.events.enabled", " Systems.Registered ,  JOBS.RETURNED ");
        TestPublisher service = new TestPublisher();
        assertTrue(service.isEventEnabled(service.getTopicPrefix() + "/systems/registered"));
        assertTrue(service.isEventEnabled(service.getTopicPrefix() + "/jobs/returned"));
    }

    /**
     * A topic may be given with or without the server prefix.
     */
    @Test
    public void testFilterMatchesWithAndWithoutPrefix() {
        System.setProperty("uyuni.mqtt.events.enabled", "systems.registered");
        TestPublisher service = new TestPublisher();
        assertTrue(service.isEventEnabled(service.getTopicPrefix() + "/systems/registered"));
        assertTrue(service.isEventEnabled("systems/registered"));
    }

    /**
     * An empty filter is treated as no filter rather than as "publish nothing".
     */
    @Test
    public void testEmptyFilterEnablesEverything() {
        System.setProperty("uyuni.mqtt.events.enabled", "   ");
        TestPublisher service = new TestPublisher();
        assertTrue(service.isEventEnabled(service.getTopicPrefix() + "/states/applied"));
    }

    /**
     * The topic prefix always starts with the well known root segment.
     */
    @Test
    public void testTopicPrefixIsRootedAtUyuni() {
        TestPublisher service = new TestPublisher();
        assertTrue(service.getTopicPrefix().startsWith("uyuni/"),
                "expected prefix to start with uyuni/, was " + service.getTopicPrefix());
    }

    /**
     * Broker credentials are read from system properties.
     */
    @Test
    public void testCredentialsReadFromSystemProperties() {
        System.setProperty("uyuni.mqtt.broker.username", "uyuni-publisher");
        System.setProperty("uyuni.mqtt.broker.password", "secret");
        TestPublisher service = new TestPublisher();
        assertEquals("uyuni-publisher", service.getUsername());
        assertEquals("secret", service.getPassword());
    }

    /**
     * Absent credentials leave the service unauthenticated rather than failing.
     */
    @Test
    public void testCredentialsAbsentByDefault() {
        TestPublisher service = new TestPublisher();
        assertNull(service.getUsername());
        assertNull(service.getPassword());
    }

    /**
     * An unusable QoS or queue limit falls back to the default instead of
     * preventing the service from starting.
     */
    @Test
    public void testInvalidNumericSettingsDoNotPreventStartup() {
        System.setProperty("uyuni.mqtt.qos", "not-a-number");
        System.setProperty("uyuni.mqtt.queue.limit", "also-not-a-number");
        TestPublisher service = new TestPublisher();
        assertTrue(service.isEventEnabled(service.getTopicPrefix() + "/systems/registered"));
    }

    /**
     * A QoS outside the permitted range is replaced by the default.
     */
    @Test
    public void testOutOfRangeQosDoesNotPreventStartup() {
        System.setProperty("uyuni.mqtt.qos", "7");
        TestPublisher service = new TestPublisher();
        assertTrue(service.isEventEnabled(service.getTopicPrefix() + "/systems/registered"));
    }

    /**
     * Constructing the service registers it as the instance the helper uses.
     */
    @Test
    public void testConstructionRegistersGlobalInstance() {
        TestPublisher service = new TestPublisher();
        assertEquals(service, MqttPublisherService.getInstance());
    }

    /**
     * Publisher that never touches the network.
     */
    /**
     * A plain tcp:// URL connects without TLS.
     */
    @Test
    public void testPlainUrlDoesNotUseTls() {
        TestPublisher service = new TestPublisher();
        assertFalse(service.usesTls());
        assertFalse(service.isTlsMisconfigured());
    }

    /**
     * An ssl:// URL uses TLS, trusting the JVM default CAs when no CA file is set.
     */
    @Test
    public void testSslUrlUsesTls() {
        TestPublisher service = new TestPublisher(TLS_BROKER);
        assertTrue(service.usesTls());
        assertFalse(service.isTlsMisconfigured());
    }

    /**
     * A custom CA and a client certificate are picked up from the system properties.
     * @throws Exception if the test resources cannot be found
     */
    @Test
    public void testSslUrlWithCaAndClientCertificate() throws Exception {
        System.setProperty("uyuni.mqtt.tls.ca.file", MqttTlsSocketFactoryTest.resource("ca.pem"));
        System.setProperty("uyuni.mqtt.tls.keystore.file", MqttTlsSocketFactoryTest.resource("client.p12"));
        System.setProperty("uyuni.mqtt.tls.keystore.password", "test-password");
        TestPublisher service = new TestPublisher(TLS_BROKER);
        assertTrue(service.usesTls());
        assertFalse(service.isTlsMisconfigured());
    }

    /**
     * Broken TLS settings stop publishing instead of falling back to a plain connection.
     */
    @Test
    public void testInvalidTlsSettingsDisablePublishing() {
        System.setProperty("uyuni.mqtt.tls.ca.file", "/nonexistent/mqtt-ca.pem");
        TestPublisher service = new TestPublisher(TLS_BROKER);
        assertTrue(service.isTlsMisconfigured());
        assertFalse(service.usesTls());
    }

    /**
     * TLS files configured next to a tcp:// URL are ignored, not treated as an error.
     */
    @Test
    public void testTlsSettingsIgnoredForPlainUrl() {
        System.setProperty("uyuni.mqtt.tls.ca.file", "/nonexistent/mqtt-ca.pem");
        TestPublisher service = new TestPublisher();
        assertFalse(service.usesTls());
        assertFalse(service.isTlsMisconfigured());
    }

    /**
     * Connection errors report the innermost cause, where the TLS reason is.
     */
    @Test
    public void testRootCauseMessage() {
        Exception tls = new Exception("wrapper", new IllegalStateException("No subject alternative names matching"));
        assertEquals("No subject alternative names matching", MqttPublisherService.rootCauseMessage(tls));
        assertEquals("plain", MqttPublisherService.rootCauseMessage(new Exception("plain")));
    }

    private static class TestPublisher extends MqttPublisherService {

        TestPublisher() {
            this(BROKER);
        }

        TestPublisher(String brokerUrl) {
            super(brokerUrl);
        }

        @Override
        public void publish(String topic, Object payload) {
            // No-op for test
        }

        @Override
        public void shutdown() {
            // No-op for test
        }
    }
}
