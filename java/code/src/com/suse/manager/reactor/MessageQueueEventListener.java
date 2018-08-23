/**
 * Copyright (c) 2018 SUSE LLC
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

package com.suse.manager.reactor;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageQueue;

import com.suse.salt.netapi.datatypes.Event;
import com.suse.salt.netapi.event.EventListener;
import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Dispatches Salt events on the MessageQueue.
 */
public class MessageQueueEventListener implements EventListener {

    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(MessageQueueEventListener.class);

    /**
     * Executor service for handling incoming events
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
    );

    /**
     * Function to call when the event stream gets closed
     */
    private final Runnable eventStreamClosed;

    /**
     * Function that maps Events to MessageQueue's EventMessages
     */
    private final Function<Event, Stream<EventMessage>> eventToMessages;

    /**
     * Standard constructor.
     *
     * @param eventStreamClosedIn function to call when the event stream gets closed
     * @param eventToMessagesIn function that maps Events to MessageQueue's EventMessages
     */
    public MessageQueueEventListener(Runnable eventStreamClosedIn,
                                     Function<Event, Stream<EventMessage>> eventToMessagesIn) {
        this.eventStreamClosed = eventStreamClosedIn;
        this.eventToMessages = eventToMessagesIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notify(Event event) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Event: " + event.getTag() + " -> " + event.getData());
        }

        executorService.submit(() -> {
            eventToMessages.apply(event).forEach(MessageQueue::publish);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void eventStreamClosed(int code, String phrase) {
        LOG.warn("Event stream closed: " + phrase + " [" + code + "]");
        eventStreamClosed.run();
    }
}
