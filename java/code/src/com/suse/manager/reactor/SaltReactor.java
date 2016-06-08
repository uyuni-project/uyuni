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

import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.manager.action.ActionManager;
import com.suse.manager.reactor.messaging.ActionScheduledEventMessage;
import com.suse.manager.reactor.messaging.ActionScheduledEventMessageAction;
import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.reactor.messaging.ApplyStatesEventMessageAction;
import com.suse.manager.reactor.messaging.ChannelsChangedEventMessage;
import com.suse.manager.reactor.messaging.ChannelsChangedEventMessageAction;
import com.suse.manager.reactor.messaging.RunnableEventMessage;
import com.suse.manager.reactor.messaging.RunnableEventMessageAction;
import com.suse.manager.reactor.messaging.JobReturnEventMessage;
import com.suse.manager.reactor.messaging.JobReturnEventMessageAction;
import com.suse.manager.reactor.messaging.MinionStartEventDatabaseMessage;
import com.suse.manager.reactor.messaging.MinionStartEventMessage;
import com.suse.manager.reactor.messaging.MinionStartEventMessageAction;
import com.suse.manager.reactor.messaging.RefreshGeneratedSaltFilesEventMessage;
import com.suse.manager.reactor.messaging.RefreshGeneratedSaltFilesEventMessageAction;
import com.suse.manager.reactor.messaging.RefreshHardwareEventMessage;
import com.suse.manager.reactor.messaging.RefreshHardwareEventMessageAction;
import com.suse.manager.reactor.messaging.RegisterMinionEventMessage;
import com.suse.manager.reactor.messaging.RegisterMinionEventMessageAction;
import com.suse.manager.webui.services.SaltService;
import com.suse.manager.webui.services.impl.SaltAPIService;
import com.suse.salt.netapi.datatypes.Event;
import com.suse.salt.netapi.event.BeaconEvent;
import com.suse.salt.netapi.event.EventListener;
import com.suse.salt.netapi.event.EventStream;
import com.suse.salt.netapi.event.JobReturnEvent;
import com.suse.salt.netapi.event.MinionStartEvent;

import org.apache.log4j.Logger;

import java.io.IOException;
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
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * Start the salt reactor.
     */
    public void start() {
        // Configure message queue to handle minion registrations
        MessageQueue.registerAction(new RegisterMinionEventMessageAction(),
                RegisterMinionEventMessage.class);
        MessageQueue.registerAction(new MinionStartEventMessageAction(),
                MinionStartEventMessage.class);
        MessageQueue.registerAction(new MinionStartEventMessageAction(),
                MinionStartEventDatabaseMessage.class);
        MessageQueue.registerAction(new ChannelsChangedEventMessageAction(),
                ChannelsChangedEventMessage.class);
        MessageQueue.registerAction(new ApplyStatesEventMessageAction(),
                ApplyStatesEventMessage.class);
        MessageQueue.registerAction(new ActionScheduledEventMessageAction(),
                ActionScheduledEventMessage.class);
        MessageQueue.registerAction(new JobReturnEventMessageAction(),
                JobReturnEventMessage.class);
        MessageQueue.registerAction(new RefreshHardwareEventMessageAction(SALT_SERVICE),
                RefreshHardwareEventMessage.class);
        MessageQueue.registerAction(new RefreshGeneratedSaltFilesEventMessageAction(),
                RefreshGeneratedSaltFilesEventMessage.class);
        MessageQueue.registerAction(new RunnableEventMessageAction(),
                RunnableEventMessage.class);

        MessageQueue.publish(new RefreshGeneratedSaltFilesEventMessage());

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
            }
            catch (IOException e) {
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
                JobReturnEvent.parse(event).map(this::onJobReturnEvent).orElseGet(() ->
                BeaconEvent.parse(event).map(this::onBeaconEvent).orElse(() -> { })));
        executorService.submit(runnable);
    }

    /**
     * Trigger registration on minion start events.
     *
     * @param minionStartEvent minion start event
     * @return event handler runnable
     */
    private Runnable onMinionStartEvent(MinionStartEvent minionStartEvent) {
        return () -> {
            triggerMinionStart((String) minionStartEvent.getData().get("id"));
            triggerMinionRegistration((String) minionStartEvent.getData().get("id"));
        };
    }

    /**
     * Trigger handling of job return events.
     *
     * @param jobReturnEvent the job return event as we get it from salt
     * @return event handler runnable
     */
    private Runnable onJobReturnEvent(JobReturnEvent jobReturnEvent) {
        return () -> {
            MessageQueue.publish(new JobReturnEventMessage(jobReturnEvent));
        };
    }

    /**
     * Trigger handling of beacon events
     *
     * @param beaconEvent beacon event
     * @return event handler runnable
     */
    private Runnable onBeaconEvent(BeaconEvent beaconEvent) {
        return () -> {
            if (beaconEvent.getBeacon().equals("pkgset") &&
                    beaconEvent.getAdditional().equals("changed")) {
                MessageQueue.publish(
                    new RunnableEventMessage("ZypperEvent.PackageSetChanged", () -> {
                        MinionServerFactory
                            .findByMinionId(beaconEvent.getMinionId())
                            .ifPresent(minionServer ->
                                ActionManager.schedulePackageRefresh(minionServer.getOrg(),
                                        minionServer)
                            );
                }));
            }
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
        MessageQueue.publish(new RegisterMinionEventMessage(minionId));
    }

    /**
     * Stuff that needs to be done on minion start like cleaning up reboot actions.
     *
     * @param minionId the minion id of the minion starting
     */
    private void triggerMinionStart(String minionId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Trigger start for minion: " + minionId);
        }
        MessageQueue.publish(new MinionStartEventMessage(minionId));
    }
}
