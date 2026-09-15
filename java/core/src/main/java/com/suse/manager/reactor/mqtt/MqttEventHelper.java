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
import org.hibernate.Transaction;
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
        publish(event, MqttPublisherService.getInstance());
    }

    /**
     * Publish an event through a specific publisher service.
     *
     * <p>Used by callers that already hold a service instance, so that publishing goes
     * through the same one they were configured with rather than the global singleton.</p>
     *
     * @param event the event to publish
     * @param service the publisher service to use, may be null
     */
    public static void publish(MqttEvent event, MqttPublisherService service) {
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
        publishAfterCommit(event, MqttPublisherService.getInstance());
    }

    /**
     * Publish an event through a specific publisher service once the transaction that
     * produced it has committed.
     *
     * @param event the event to publish
     * @param service the publisher service to use, may be null
     */
    public static void publishAfterCommit(MqttEvent event, MqttPublisherService service) {
        Transaction transaction;
        try {
            if (!HibernateFactory.inTransaction()) {
                publish(event, service);
                return;
            }
            transaction = HibernateFactory.getSession().getTransaction();
        }
        catch (RuntimeException e) {
            // No session on this thread, so there is no commit to wait for.
            LOG.debug("No Hibernate session available, publishing {} directly.", event.getTopicSuffix());
            publish(event, service);
            return;
        }

        transaction.runAfterCompletion(status -> {
            if (status == TransactionStatus.COMMITTED) {
                publish(event, service);
            }
            else {
                LOG.debug("Skipping MQTT event {}: transaction completed with status {}.",
                        event.getTopicSuffix(), status);
            }
        });
    }
}
