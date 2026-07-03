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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Asynchronous MQTT Publisher Service for broadcasting Uyuni events.
 *
 * Uses Eclipse Paho's {@link MqttAsyncClient} to publish structured JSON
 * event payloads to a Mosquitto MQTT broker without blocking the reactor
 * event pipeline.
 */
public class MqttPublisherService {

    private static final Logger LOG = LogManager.getLogger(MqttPublisherService.class);

    private static final String DEFAULT_BROKER_URL = "tcp://mosquitto:1883";
    private static final String CLIENT_ID_PREFIX = "uyuni-publisher-";
    private static final int CONNECTION_TIMEOUT_SECONDS = 15;
    private static final int KEEP_ALIVE_INTERVAL_SECONDS = 60;
    private static final int CONNECT_WAIT_MS = 10000;
    private static final int DISCONNECT_WAIT_MS = 5000;

    private final String brokerUrl;
    private final String clientId;
    private final Gson gson;
    private final ExecutorService executorService;

    private MqttAsyncClient client;
    private boolean isConnecting = false;

    /**
     * Default constructor using standard broker URL from system property
     * {@code uyuni.mqtt.broker.url} or {@code tcp://mosquitto:1883}.
     */
    public MqttPublisherService() {
        this(System.getProperty("uyuni.mqtt.broker.url", DEFAULT_BROKER_URL));
    }

    /**
     * Parameterized constructor.
     * @param brokerUrlIn the MQTT broker URL (e.g. tcp://hostname:1883)
     */
    public MqttPublisherService(String brokerUrlIn) {
        this.brokerUrl = brokerUrlIn;
        this.clientId = CLIENT_ID_PREFIX + UUID.randomUUID().toString().substring(0, 8);
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "mqtt-publisher-thread");
            t.setDaemon(true);
            return t;
        });

        LOG.warn("Initializing MqttPublisherService with broker: {} and client ID: {}",
                this.brokerUrl, this.clientId);
        connectAsync();
    }

    /**
     * Establish connection to the MQTT broker asynchronously.
     */
    private synchronized void connectAsync() {
        if (isConnecting || (client != null && client.isConnected())) {
            return;
        }

        isConnecting = true;
        executorService.submit(() -> {
            try {
                LOG.warn("Connecting to MQTT broker at {}...", brokerUrl);
                client = new MqttAsyncClient(brokerUrl, clientId,
                        new MemoryPersistence());

                MqttConnectOptions options = new MqttConnectOptions();
                options.setCleanSession(true);
                options.setAutomaticReconnect(true);
                options.setConnectionTimeout(CONNECTION_TIMEOUT_SECONDS);
                options.setKeepAliveInterval(KEEP_ALIVE_INTERVAL_SECONDS);

                IMqttToken token = client.connect(options);
                token.waitForCompletion(CONNECT_WAIT_MS);
                LOG.warn("Successfully connected to MQTT broker: {}", brokerUrl);
            }
            catch (MqttException e) {
                LOG.error("Failed to connect to MQTT broker: {}",
                        e.getMessage(), e);
            }
            finally {
                synchronized (MqttPublisherService.this) {
                    isConnecting = false;
                }
            }
        });
    }

    /**
     * Publish an event payload to a topic asynchronously.
     * @param topic target MQTT topic
     * @param payload the structured Java object to serialize and send
     */
    public void publish(final String topic, final Object payload) {
        executorService.submit(() -> {
            try {
                if (client == null || !client.isConnected()) {
                    LOG.warn("MQTT client not connected. Attempting reconnect "
                            + "and skipping publish to: {}", topic);
                    connectAsync();
                    return;
                }

                // Wrap payload with standard envelope metadata
                Map<String, Object> envelope = new HashMap<>();
                envelope.put("eventId", UUID.randomUUID().toString());
                envelope.put("timestamp", new java.util.Date());
                envelope.put("topic", topic);
                envelope.put("data", payload);

                String jsonPayload = gson.toJson(envelope);
                MqttMessage message = new MqttMessage(
                        jsonPayload.getBytes("UTF-8"));
                message.setQos(1); // At least once delivery

                client.publish(topic, message);
                LOG.warn("MQTT message published to {}: {}",
                        topic, jsonPayload);
            }
            catch (Exception e) {
                LOG.error("Error publishing MQTT message to topic {}: {}",
                        topic, e.getMessage(), e);
            }
        });
    }

    /**
     * Clean shutdown of the executor service and MQTT client.
     */
    public void shutdown() {
        LOG.warn("Shutting down MqttPublisherService...");
        executorService.shutdown();
        if (client != null) {
            try {
                if (client.isConnected()) {
                    IMqttToken token = client.disconnect();
                    token.waitForCompletion(DISCONNECT_WAIT_MS);
                }
                client.close();
            }
            catch (MqttException e) {
                LOG.error("Error during MQTT client shutdown: {}",
                        e.getMessage(), e);
            }
        }
    }
}
