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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility helper class for publishing Java-native events to the MQTT broker.
 */
public final class MqttEventHelper {

    private static final Logger LOG = LogManager.getLogger(MqttEventHelper.class);

    private MqttEventHelper() {
        // Prevent instantiation
    }

    /**
     * Build a fully-qualified MQTT topic using the FQDN-based prefix.
     * @param suffix the topic suffix (e.g. "users/created")
     * @return the full topic string (e.g. "uyuni/server.example.com/users/created")
     */
    private static String buildTopic(String suffix) {
        MqttPublisherService service = MqttPublisherService.getInstance();
        if (service != null) {
            return service.getTopicPrefix() + "/" + suffix;
        }
        return "uyuni/unknown/" + suffix;
    }

    /**
     * Publish an event to the MQTT broker if the service is active.
     * @param topic target MQTT topic
     * @param data payload data map
     */
    public static void publish(String topic, Map<String, Object> data) {
        MqttPublisherService service = MqttPublisherService.getInstance();
        if (service != null) {
            service.publish(topic, data);
        }
        else {
            LOG.debug("MqttPublisherService is not initialized. Event skipped on topic: {}", topic);
        }
    }

    /**
     * Publish user created event.
     * @param username the username of the created user
     * @param creator the username of the user who created them
     * @param orgId the organization ID
     */
    public static void publishUserCreated(String username, String creator, Long orgId) {
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("creator", creator);
        data.put("orgId", orgId);
        publish(buildTopic("users/created"), data);
    }

    /**
     * Publish organization created event.
     * @param orgId the organization ID
     * @param orgName the organization name
     */
    public static void publishOrgCreated(Long orgId, String orgName) {
        Map<String, Object> data = new HashMap<>();
        data.put("orgId", orgId);
        data.put("orgName", orgName);
        publish(buildTopic("orgs/created"), data);
    }

    /**
     * Publish CLM build started event.
     * @param projectLabel Content Lifecycle Project label
     * @param username the username who triggered the build
     */
    public static void publishClmBuildStarted(String projectLabel, String username) {
        Map<String, Object> data = new HashMap<>();
        data.put("projectLabel", projectLabel);
        data.put("username", username);
        publish(buildTopic("clm/build_started"), data);
    }

    /**
     * Publish CLM build completed event.
     * @param projectLabel Content Lifecycle Project label
     * @param version the built version string
     * @param username the username who triggered the build
     */
    public static void publishClmBuildCompleted(String projectLabel, String version, String username) {
        Map<String, Object> data = new HashMap<>();
        data.put("projectLabel", projectLabel);
        data.put("version", version);
        data.put("username", username);
        publish(buildTopic("clm/build_completed"), data);
    }
}
