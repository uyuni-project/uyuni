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

import com.redhat.rhn.common.messaging.JavaMailException;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.metrics.PrometheusExporter;
import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.reactor.messaging.ApplyStatesEventMessageAction;
import com.suse.manager.reactor.messaging.JobReturnEventMessage;
import com.suse.manager.reactor.messaging.JobReturnEventMessageAction;
import com.suse.manager.reactor.messaging.MinionStartEventDatabaseMessage;
import com.suse.manager.reactor.messaging.MinionStartEventMessage;
import com.suse.manager.reactor.messaging.MinionStartEventMessageAction;
import com.suse.manager.reactor.messaging.RefreshGeneratedSaltFilesEventMessage;
import com.suse.manager.reactor.messaging.RefreshGeneratedSaltFilesEventMessageAction;
import com.suse.manager.reactor.messaging.RegisterMinionEventMessage;
import com.suse.manager.reactor.messaging.RegisterMinionEventMessageAction;
import com.suse.manager.reactor.messaging.RunnableEventMessage;
import com.suse.manager.reactor.messaging.RunnableEventMessageAction;
import com.suse.manager.reactor.messaging.VirtpollerBeaconEventMessage;
import com.suse.manager.reactor.messaging.VirtpollerBeaconEventMessageAction;
import com.suse.manager.reactor.utils.MailHelper;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.salt.custom.VirtpollerData;
import com.suse.salt.netapi.datatypes.Event;
import com.suse.salt.netapi.event.BeaconEvent;
import com.suse.salt.netapi.event.EventListener;
import com.suse.salt.netapi.event.EventStream;
import com.suse.salt.netapi.event.JobReturnEvent;
import com.suse.salt.netapi.event.MinionStartEvent;
import com.suse.salt.netapi.exception.SaltException;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.websocket.CloseReason;

/**
 * Salt event reactor.
 */
public class SaltReactor implements EventListener {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(SaltReactor.class);

    // Reference to the SaltService instance
    private static final SaltService SALT_SERVICE = SaltService.INSTANCE;

    // The event stream object
    private EventStream eventStream;

    // Indicate that the reactor has been stopped
    private volatile boolean isStopped = false;

    // Executor service for handling incoming events
    private final ExecutorService executorService =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    // Reconnecting time (in seconds) to Salt event bus
    private static final int DELAY_TIME_SECONDS = 5;

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
        MessageQueue.registerAction(new ApplyStatesEventMessageAction(),
                ApplyStatesEventMessage.class);
        MessageQueue.registerAction(new JobReturnEventMessageAction(),
                JobReturnEventMessage.class);
        MessageQueue.registerAction(new RefreshGeneratedSaltFilesEventMessageAction(),
                RefreshGeneratedSaltFilesEventMessage.class);
        MessageQueue.registerAction(new RunnableEventMessageAction(),
                RunnableEventMessage.class);
        MessageQueue.registerAction(new VirtpollerBeaconEventMessageAction(),
                VirtpollerBeaconEventMessage.class);

        MessageQueue.publish(new RefreshGeneratedSaltFilesEventMessage());

        connectToEventStream();

        // register the executor service for exporting metrics
        PrometheusExporter.INSTANCE.registerThreadPool((ThreadPoolExecutor) this.executorService, "salt_reactor");

        SaltService.INSTANCE.setReactor(this);
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

        if (!isStopped) {
            LOG.warn("Reconnecting to the Salt event bus...");
            connectToEventStream();
        }
    }

    /**
     * Connect to Salt Event stream; if not connected, retry connections with
     * timeout.
     */
    public void connectToEventStream() {
        boolean connected = false;

        int retries = 0;

        while (!connected) {
            retries++;
            try {
                eventStream = SALT_SERVICE.getEventStream();
                eventStream.addEventListener(this);
                connected = true;
                if (retries > 1) {
                    LOG.warn("Successfully connected to the Salt event bus after " +
                            (retries - 1) + " retries.");
                }
                else {
                    LOG.info("Successfully connected to the Salt event bus");
                }
            }
            catch (SaltException e) {
                try {
                    LOG.error("Unable to connect: " + e + ", retrying in " +
                              DELAY_TIME_SECONDS + " seconds.");
                    Thread.sleep(1000 * DELAY_TIME_SECONDS);
                    if (retries == 1) {
                        MailHelper.sendAdminEmail("Cannot connect to salt event bus",
                                "salt-api daemon is not responding. Check the status of " +
                                        "salt-api daemon and (re)-start it if needed\n\n" +
                                        "This is the only notification you will receive.");
                    }
                }
                catch (JavaMailException javaMailException) {
                    LOG.error("Error sending email: " + javaMailException.getMessage());
                }
                catch (InterruptedException e1) {
                    LOG.error("Interrupted during sleep: " + e1);
                }
            }
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
                            MinionServerFactory.findByMinionId(beaconEvent.getMinionId())
                                    .ifPresent(minionServer -> {
                                        try {
                                            ActionManager.schedulePackageRefresh(
                                                    minionServer.getOrg(), minionServer);
                                        }
                                        catch (TaskomaticApiException e) {
                                            LOG.error(
                                                    "Could not schedule package refresh " +
                                                    "for minion: " +
                                                            minionServer.getMinionId());
                                            LOG.error(e);
                                        }
                                    });
                        }));
            }
            else if (beaconEvent.getBeacon().equals("virtpoller")) {
                MessageQueue.publish(new VirtpollerBeaconEventMessage(
                        beaconEvent.getMinionId(),
                        beaconEvent.getData(VirtpollerData.class)
                ));
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

    /**
     * @return the Salt event stream
     */
    public EventStream getEventStream() {
        return eventStream;
    }

}
