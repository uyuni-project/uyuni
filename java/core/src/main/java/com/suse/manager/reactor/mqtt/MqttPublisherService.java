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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    private static final String DEFAULT_TOPIC_PREFIX = "uyuni/localhost";
    private static final String CLIENT_ID_PREFIX = "uyuni-publisher-";
    private static final int CONNECTION_TIMEOUT_SECONDS = 15;
    private static final int KEEP_ALIVE_INTERVAL_SECONDS = 60;
    private static final int CONNECT_WAIT_MS = 10000;
    private static final int DISCONNECT_WAIT_MS = 5000;

    private static volatile MqttPublisherService instance;

    private final String brokerUrl;
    private final String clientId;
    private final String topicPrefix;
    private final String username;
    private final String password;
    private final Set<String> enabledEvents;
    private final Gson gson;
    private final ExecutorService executorService;
    private final int qos;

    private MqttAsyncClient client;
    private boolean isConnecting = false;

    /**
     * Returns the global instance of the publisher service.
     * @return the active MqttPublisherService instance, or null if not initialized
     */
    public static MqttPublisherService getInstance() {
        return instance;
    }

    /**
     * Sets the global instance of the publisher service.
     * @param instanceIn the instance to set
     */
    public static synchronized void setInstance(MqttPublisherService instanceIn) {
        instance = instanceIn;
    }

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
        this.topicPrefix = resolveFqdn();
        this.username = System.getProperty("uyuni.mqtt.broker.username", System.getenv("UYUNI_MQTT_BROKER_USERNAME"));
        this.password = System.getProperty("uyuni.mqtt.broker.password", System.getenv("UYUNI_MQTT_BROKER_PASSWORD"));

        String enabledEventsProp = System.getProperty("uyuni.mqtt.events.enabled");
        if (enabledEventsProp != null && !enabledEventsProp.trim().isEmpty()) {
            this.enabledEvents = new HashSet<>();
            for (String event : enabledEventsProp.split(",")) {
                this.enabledEvents.add(event.trim().toLowerCase());
            }
        }
        else {
            this.enabledEvents = null;
        }

        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();

        int qosVal = 1;
        try {
            qosVal = Integer.parseInt(System.getProperty("uyuni.mqtt.qos", "1"));
            if (qosVal < 0 || qosVal > 2) {
                LOG.warn("Invalid QoS value configured: {}. Defaulting to 1.", qosVal);
                qosVal = 1;
            }
        }
        catch (NumberFormatException e) {
            LOG.warn("Invalid QoS format. Defaulting to 1.");
        }
        this.qos = qosVal;

        int queueLimit = 10000;
        try {
            queueLimit = Integer.parseInt(System.getProperty("uyuni.mqtt.queue.limit", "10000"));
        }
        catch (NumberFormatException e) {
            LOG.warn("Invalid queue limit format. Defaulting to 10000.");
        }

        final int finalQueueLimit = queueLimit;
        this.executorService = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(finalQueueLimit),
                r -> {
                    Thread t = new Thread(r, "mqtt-publisher-thread");
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.DiscardOldestPolicy() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                        LOG.warn("MQTT publication queue is full (limit: {}). " +
                                "Dropping oldest publication task.", finalQueueLimit);
                        super.rejectedExecution(r, e);
                    }
                }
        );

        LOG.warn("Initializing MqttPublisherService with broker: {}, " +
                "client ID: {}, topic prefix: {}",
                this.brokerUrl, this.clientId, this.topicPrefix);
        connectAsync();
        setInstance(this);
    }

    /**
     * Resolve the server FQDN for use in topic naming.
     * Falls back to {@code DEFAULT_TOPIC_PREFIX} on failure.
     * @return the topic prefix in the form "uyuni/fqdn"
     */
    private static String resolveFqdn() {
        try {
            String fqdn = InetAddress.getLocalHost().getCanonicalHostName();
            LOG.info("Resolved server FQDN for MQTT topics: {}", fqdn);
            return "uyuni/" + fqdn;
        }
        catch (UnknownHostException e) {
            LOG.warn("Unable to resolve server FQDN, using default " +
                    "topic prefix: {}", DEFAULT_TOPIC_PREFIX, e);
            return DEFAULT_TOPIC_PREFIX;
        }
    }

    /**
     * Returns the FQDN-based topic prefix (e.g. "uyuni/uyuni.example.com").
     * @return the topic prefix string
     */
    public String getTopicPrefix() {
        return topicPrefix;
    }

    /**
     * Returns the MQTT broker username.
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the MQTT broker password.
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Checks if a given topic is enabled for publishing.
     * @param topic the target topic string
     * @return true if the event is enabled (or no config filter is set), false otherwise
     */
    public boolean isEventEnabled(String topic) {
        if (enabledEvents == null) {
            return true;
        }
        String suffix = topic;
        if (topic.startsWith(topicPrefix)) {
            suffix = topic.substring(topicPrefix.length());
        }
        if (suffix.startsWith("/")) {
            suffix = suffix.substring(1);
        }
        String dotFormat = suffix.replace('/', '.').toLowerCase();
        String slashFormat = suffix.toLowerCase();
        return enabledEvents.contains(dotFormat) || enabledEvents.contains(slashFormat);
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

                if (username != null && !username.trim().isEmpty()) {
                    options.setUserName(username);
                    if (password != null) {
                        options.setPassword(password.toCharArray());
                    }
                }

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
        if (!isEventEnabled(topic)) {
            LOG.debug("MQTT event publication skipped because it is not enabled: {}", topic);
            return;
        }
        executorService.submit(() -> {
            try {
                if (client == null || !client.isConnected()) {
                    LOG.warn("MQTT client not connected. Attempting reconnect " +
                            "and skipping publish to: {}", topic);
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
                message.setQos(this.qos);

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
