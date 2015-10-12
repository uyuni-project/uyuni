/**
 * Copyright (c) 2015 SUSE LLC
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

import com.redhat.rhn.common.messaging.MessageQueue;

import com.suse.manager.webui.events.ManagedFileChangedEvent;
import com.suse.manager.webui.services.SaltService;
import com.suse.manager.webui.services.impl.SaltAPIService;
import com.suse.manager.webui.sse.SSEServlet;
import com.suse.saltstack.netapi.datatypes.Event;
import com.suse.saltstack.netapi.event.BeaconEvent;
import com.suse.saltstack.netapi.event.EventListener;
import com.suse.saltstack.netapi.event.EventStream;
import com.suse.saltstack.netapi.event.MinionStartEvent;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.websocket.CloseReason;

/**
 * Salt event reactor.
 */
public class SaltReactor implements EventListener {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(SaltReactor.class);

    // Reference to the SaltService instance
    private static final SaltService SALT_SERVICE = SaltAPIService.INSTANCE;

    // The event stream object
    private EventStream eventStream;

    // Indicate that the reactor has been stopped
    private volatile boolean isStopped = false;

    // Executor service for handling incoming events
    private ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * Start the salt reactor.
     */
    public void start() {
        // Configure message queue to handle minion registrations
        MessageQueue.registerAction(new RegisterMinionAction(),
                RegisterMinionEvent.class);

        // Sync minions to systems in the database
        LOG.debug("Syncing minions to the database");
        SALT_SERVICE.getKeys().getMinions().forEach(this::triggerMinionRegistration);

        // Initialize the event stream
        eventStream = SALT_SERVICE.getEventStream();
        eventStream.addEventListener(this);
    }

    /**
     * Stop the salt reactor.
     */
    public void stop() {
        isStopped = true;
        if (eventStream != null) {
            try {
                eventStream.close();
            } catch (IOException e) {
                LOG.error("Error stopping the salt reactor", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void eventStreamClosed(CloseReason closeReason) {
        LOG.warn("Event stream closed: " + closeReason.getReasonPhrase() +
                " [" + closeReason.getCloseCode() + "]");

        // Try to reconnect
        if (!isStopped) {
            LOG.warn("Reconnecting to event stream...");
            eventStream = SALT_SERVICE.getEventStream();
            eventStream.addEventListener(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notify(Event event) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Event: " + event.getTag() + " -> " + event.getData());
        }

        // Setup handlers for different event types
        Runnable runnable =
                MinionStartEvent.parse(event).map(this::onMinionStartEvent).orElseGet(() ->
                BeaconEvent.parse(event).map(this::onBeaconEvent).orElse(() -> {}));
        executorService.submit(runnable);
    }

    /**
     * Event handler for beacon events.
     *
     * @param beaconEvent beacon event
     * @return event handler runnable
     */
    private Runnable onBeaconEvent(BeaconEvent beaconEvent) {
        return () -> {
            // Detect changes of managed files using the "managedwatch" beacon
            if (beaconEvent.getBeacon().equals("managedwatch")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>)
                        beaconEvent.getData().get("data");
                String path = (String) data.get("path");
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Managed file has changed: " + path);
                }

                // Send event via SSE to connected clients
                SSEServlet.sendEvent(new ManagedFileChangedEvent(
                        beaconEvent.getMinionId(), path, (String) data.get("diff")));
            }
        };
    }

    /**
     * Trigger registration on minion start events.
     *
     * @param minionStartEvent minion start event
     * @return event handler runnable
     */
    private Runnable onMinionStartEvent(MinionStartEvent minionStartEvent) {
        return () -> {
            triggerMinionRegistration((String) minionStartEvent.getData().get("id"));
        };
    }

    /**
     * Trigger the registration of a minion in case it is not registered yet.
     *
     * @param minionId the minion id of the minion to be registered
     */
    private void triggerMinionRegistration(String minionId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Trigger registration for minion: " + minionId);
        }
        MessageQueue.publish(new RegisterMinionEvent(minionId));
    }
}
