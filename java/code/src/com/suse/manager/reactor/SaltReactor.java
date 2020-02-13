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

import static java.util.stream.Stream.empty;
import static java.util.stream.Stream.of;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.JavaMailException;
import com.redhat.rhn.common.messaging.MessageQueue;

import com.suse.manager.reactor.messaging.RegisterMinionEventMessage;
import com.suse.manager.reactor.messaging.RegisterMinionEventMessageAction;
import com.suse.manager.reactor.messaging.SystemIdGenerateEventMessage;
import com.suse.manager.reactor.messaging.SystemIdGenerateEventMessageAction;
import com.suse.manager.tasks.ActorManager;
import com.suse.manager.tasks.Command;
import com.suse.manager.tasks.actors.BatchStartedActor;
import com.suse.manager.tasks.actors.ImageDeployedActor;
import com.suse.manager.tasks.actors.JobReturnActor;
import com.suse.manager.tasks.actors.LibvirtEngineActor;
import com.suse.manager.tasks.actors.MinionStartEventActor;
import com.suse.manager.tasks.actors.PkgsetBeaconActor;
import com.suse.manager.tasks.actors.RefreshGeneratedSaltFilesActor;
import com.suse.manager.tasks.actors.VirtpollerBeaconActor;
import com.suse.manager.utils.MailHelper;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.salt.ImageDeployedEvent;
import com.suse.manager.webui.utils.salt.SystemIdGenerateEvent;
import com.suse.manager.webui.utils.salt.custom.VirtpollerData;
import com.suse.salt.netapi.datatypes.Event;
import com.suse.salt.netapi.event.BatchStartedEvent;
import com.suse.salt.netapi.event.BeaconEvent;
import com.suse.salt.netapi.event.EngineEvent;
import com.suse.salt.netapi.event.EventStream;
import com.suse.salt.netapi.event.JobReturnEvent;
import com.suse.salt.netapi.event.MinionStartEvent;
import com.suse.salt.netapi.exception.SaltException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * Salt event reactor.
 */
public class SaltReactor {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(SaltReactor.class);

    // Reference to the SaltService instance
    private static final SaltService SALT_SERVICE = SaltService.INSTANCE;
    public static final int THREAD_POOL_SIZE = ConfigDefaults.get().getSaltEventThreadPoolSize();

    // The event stream object
    private EventStream eventStream;

    // Indicate that the reactor has been stopped
    private volatile boolean isStopped = false;


    // Reconnecting time (in seconds) to Salt event bus
    private static final int DELAY_TIME_SECONDS = 5;

    /**
     * Start the salt reactor.
     */
    public void start() {
        // Configure message queue to handle minion registrations
        MessageQueue.registerAction(new RegisterMinionEventMessageAction(),
                RegisterMinionEventMessage.class);
        MessageQueue.registerAction(new SystemIdGenerateEventMessageAction(),
                SystemIdGenerateEventMessage.class);

        ActorManager.tell(new RefreshGeneratedSaltFilesActor.Message());

        connectToEventStream();

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
     * Attempts reconnection to the event stream should it be closed for unexpected reasons.
     */
    public void eventStreamClosed() {
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
                eventStream.addEventListener(new PGEventListener(this::eventStreamClosed, this::eventToMessages, this::eventToCommands));

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
                        MailHelper.withSmtp().sendAdminEmail("Cannot connect to salt event bus",
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

    private Stream<Command> eventToCommands(Event event) {
        // Setup handlers for different event types
        return JobReturnEvent.parse(event).map(this::eventToCommands).orElseGet(() ->
               BatchStartedEvent.parse(event).map(this::eventToCommands).orElseGet(() ->
               MinionStartEvent.parse(event).map(this::eventToCommands).orElseGet(() ->
               BeaconEvent.parse(event).map(this::eventToCommands).orElseGet(() ->
               EngineEvent.parse(event).map(this::eventToCommands).orElseGet(() ->
               ImageDeployedEvent.parse(event).map(this::eventToCommands).orElseGet(() ->
               empty()
        ))))));
    }

    private Stream<EventMessage> eventToMessages(Event event) {
        // Setup handlers for different event types
        return MinionStartEvent.parse(event).map(this::eventToMessages).orElseGet(() ->
               SystemIdGenerateEvent.parse(event).map(this::eventToMessages).orElseGet(() ->
               empty()
        ));
    }

    /**
     * Trigger handling of batch started events.
     *
     * @param batchStartedEvent the batch started event as we get it from salt
     * @return event handler runnable
     */
    private Stream<Command> eventToCommands(BatchStartedEvent batchStartedEvent) {
        return of(new BatchStartedActor.Message(batchStartedEvent));
    }

    /**
     * Trigger handling of systemid generate event.
     *
     * @param systemIdGenerateEvent the suse/systemid/generate event as we get it from salt
     * @return event handler runnable
     */
    private Stream<EventMessage> eventToMessages(SystemIdGenerateEvent systemIdGenerateEvent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Generate systemid file for minion: " + (String) systemIdGenerateEvent.getData().get("id"));
        }
        return of(new SystemIdGenerateEventMessage((String) systemIdGenerateEvent.getData().get("id")));
    }

    /**
     * Trigger ImageDeployed events.
     *
     * @param imageDeployedEvent image deployed event
     * @return event handler runnable
     */
    private Stream<Command> eventToCommands(ImageDeployedEvent imageDeployedEvent) {
        return of(new ImageDeployedActor.Message(imageDeployedEvent));
    }

    /**
     * Trigger registration on minion start events.
     *
     * @param minionStartEvent minion start event
     * @return event handler runnable
     */
    private Stream<EventMessage> eventToMessages(MinionStartEvent minionStartEvent) {
        String minionId = (String) minionStartEvent.getData().get("id");
        if (LOG.isDebugEnabled()) {
            LOG.debug("Trigger start and registration for minion: " + minionId);
        }
        return of(
            new RegisterMinionEventMessage(minionId)
        );
    }

    /**
     * Trigger registration on minion start events.
     *
     * @param minionStartEvent minion start event
     * @return event handler runnable
     */
    private Stream<Command> eventToCommands(MinionStartEvent minionStartEvent) {
        String minionId = (String) minionStartEvent.getData().get("id");
        if (LOG.isDebugEnabled()) {
            LOG.debug("Trigger start and registration for minion: " + minionId);
        }
        return of(
            new MinionStartEventActor.Message(minionId)
        );
    }

    /**
     * Trigger handling of engine events
     *
     * @param engineEvent engine event
     * @return event handler runnable
     */
    private Stream<Command> eventToCommands(EngineEvent engineEvent) {
        if ("libvirt_events".equals(engineEvent.getEngine())) {
            try {
                LibvirtEngineActor.Message message = LibvirtEngineActor.create(engineEvent);
                if (message != null) {
                    return of(message);
                }
                else {
                    LOG.debug("Unhandled libvirt engine event:" + engineEvent.getAdditional());
                }
            }
            catch (IllegalArgumentException e) {
                LOG.warn("Invalid libvirt engine event: " + engineEvent.getAdditional());
            }
        }
        return empty();
    }

    /**
     * Trigger handling of job return events.
     *
     * @param jobReturnEvent the job return event as we get it from salt
     * @return event handler runnable
     */
    private Stream<Command> eventToCommands(JobReturnEvent jobReturnEvent) {
        return of(new JobReturnActor.Message(jobReturnEvent));
    }

    /**
     * Trigger handling of beacon events
     *
     * @param beaconEvent beacon event
     * @return event handler runnable
     */
    private Stream<Command> eventToCommands(BeaconEvent beaconEvent) {
        if (beaconEvent.getBeacon().equals("pkgset") && beaconEvent.getAdditional().equals("changed")) {
            return of(new PkgsetBeaconActor.Message(beaconEvent));
        }
        if (beaconEvent.getBeacon().equals("virtpoller")) {
            return of(new VirtpollerBeaconActor.Message(
                beaconEvent.getMinionId(),
                beaconEvent.getData(VirtpollerData.class)
            ));
        }
        return empty();
    }

    /**
     * @return the Salt event stream
     */
    public EventStream getEventStream() {
        return eventStream;
    }
}
