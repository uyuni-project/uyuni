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

import com.redhat.rhn.common.hibernate.HibernateFactory;

import com.suse.manager.reactor.mqtt.event.MqttEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.resource.transaction.spi.TransactionStatus;

/**
 * Utility helper class for publishing Java-native events to the MQTT broker.
 */
public final class MqttEventHelper {

    private static final Logger LOG = LogManager.getLogger(MqttEventHelper.class);

    private MqttEventHelper() {
        // Prevent instantiation
    }

    /**
     * Publish an event to the MQTT broker if the service is active and the event type is enabled.
     * @param event the event to publish
     */
    public static void publish(MqttEvent event) {
        MqttPublisherService service = MqttPublisherService.getInstance();
        if (service != null) {
            String topic = service.getTopicPrefix() + "/" + event.getTopicSuffix();
            if (service.isEventEnabled(topic)) {
                service.publish(topic, event.getPayload());
            }
            else {
                LOG.debug("MQTT event publication skipped because it is not enabled: {}", topic);
            }
        }
        else {
            LOG.debug("MqttPublisherService is not initialized. Event skipped.");
        }
    }

    /**
     * Publish an event once the transaction that produced it has committed.
     *
     * <p>Publishing is asynchronous and cannot be undone, so an event emitted while the
     * transaction is still open would announce a change that a later rollback discards.
     * When no transaction is pending the event is published immediately.</p>
     *
     * @param event the event to publish
     */
    public static void publishAfterCommit(MqttEvent event) {
        if (!HibernateFactory.inTransaction()) {
            publish(event);
            return;
        }

        HibernateFactory.getSession().getTransaction().runAfterCompletion(status -> {
            if (status == TransactionStatus.COMMITTED) {
                publish(event);
            }
            else {
                LOG.debug("Skipping MQTT event {}: transaction completed with status {}.",
                        event.getTopicSuffix(), status);
            }
        });
    }
}
