/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.common.messaging;

/**
 * Wrapper around MessageQueue that allows publisher implementation to be swapped.
 * Main use case for using MessageQueueHolder.publish() instead of MessageQueue.publish()
 * is to allow tests to swap in a mock publisher without needing to mock static methods.
 */
public final class MessageQueueHolder {

    private static MessageQueuePublisher publisher = MessageQueue::publish;

    /**
     * Util class, no public constructor
     */
    private MessageQueueHolder() {
    }

    /**
     * Publish a message to the queue.
     * @param msg EventMessage to publish
     */
    public static void publish(EventMessage msg) {
        publisher.publish(msg);
    }

    /**
     * Test-only: swap the publisher implementation.
     * @param pub the publisher to use
     */
    public static void setPublisher(MessageQueuePublisher pub) {
        publisher = pub;
    }

    /**
     * Test-only: reset to default MessageQueue publisher.
     */
    public static void reset() {
        publisher = MessageQueue::publish;
    }

    /**
     * Functional interface for publishing messages.
     */
    @FunctionalInterface
    public interface MessageQueuePublisher {
        /**
         * Publish a message.
         * @param msg the message to publish
         */
        void publish(EventMessage msg);
    }
}
