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

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.reactor.messaging.AbstractLibvirtEngineMessage;
import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.reactor.messaging.ApplyStatesEventMessageAction;
import com.suse.manager.reactor.messaging.BatchStartedEventMessage;
import com.suse.manager.reactor.messaging.BatchStartedEventMessageAction;
import com.suse.manager.reactor.messaging.ImageDeployedEventMessage;
import com.suse.manager.reactor.messaging.ImageDeployedEventMessageAction;
import com.suse.manager.reactor.messaging.JobReturnEventMessage;
import com.suse.manager.reactor.messaging.JobReturnEventMessageAction;
import com.suse.manager.reactor.messaging.LibvirtEngineDomainLifecycleMessage;
import com.suse.manager.reactor.messaging.LibvirtEngineDomainLifecycleMessageAction;
import com.suse.manager.reactor.messaging.MinionStartEventDatabaseMessage;
import com.suse.manager.reactor.messaging.MinionStartEventMessage;
import com.suse.manager.reactor.messaging.MinionStartEventMessageAction;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.utils.salt.MinionStartupGrains;
import com.suse.manager.reactor.messaging.RefreshGeneratedSaltFilesEventMessage;
import com.suse.manager.reactor.messaging.RefreshGeneratedSaltFilesEventMessageAction;
import com.suse.manager.reactor.messaging.RegisterMinionEventMessage;
import com.suse.manager.reactor.messaging.RegisterMinionEventMessageAction;
import com.suse.manager.reactor.messaging.RunnableEventMessage;
import com.suse.manager.reactor.messaging.RunnableEventMessageAction;
import com.suse.manager.reactor.messaging.SystemIdGenerateEventMessage;
import com.suse.manager.reactor.messaging.SystemIdGenerateEventMessageAction;
import com.suse.manager.reactor.messaging.VirtpollerBeaconEventMessage;
import com.suse.manager.reactor.messaging.VirtpollerBeaconEventMessageAction;
import com.suse.manager.virtualization.VirtManager;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.utils.salt.ImageDeployedEvent;
import com.suse.manager.webui.utils.salt.MinionStartEvent;
import com.suse.manager.webui.utils.salt.SystemIdGenerateEvent;
import com.suse.manager.webui.utils.salt.custom.VirtpollerData;
import com.suse.salt.netapi.datatypes.Event;
import com.suse.salt.netapi.event.BatchStartedEvent;
import com.suse.salt.netapi.event.BeaconEvent;
import com.suse.salt.netapi.event.EngineEvent;
import com.suse.salt.netapi.event.EventStream;
import com.suse.salt.netapi.event.JobReturnEvent;
import org.apache.log4j.Logger;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Salt event reactor.
 */
public class SaltReactor {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(SaltReactor.class);

    // Reference to the SaltService instance
    private final SaltApi saltApi;
    private final SystemQuery systemQuery;

    // The event stream object
    private EventStream eventStream;

    private PGEventListener listener;

    // Indicate that the reactor has been stopped
    private volatile boolean isStopped = false;

    /**
     * Processing salt events
     * @param saltApiIn instance to talk to salt
     * @param systemQueryIn instance to get system information.
     */
    public SaltReactor(SaltApi saltApiIn, SystemQuery systemQueryIn) {
        this.saltApi = saltApiIn;
        this.systemQuery = systemQueryIn;
    }

    /**
     * Start the salt reactor.
     */
    public void start() {
        VirtManager virtManager = new VirtManager(systemQuery);

        // Configure message queue to handle minion registrations
        MessageQueue.registerAction(new RegisterMinionEventMessageAction(systemQuery),
                RegisterMinionEventMessage.class);
        MessageQueue.registerAction(new MinionStartEventMessageAction(systemQuery),
                MinionStartEventMessage.class);
        MessageQueue.registerAction(new MinionStartEventMessageAction(systemQuery),
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
        MessageQueue.registerAction(new SystemIdGenerateEventMessageAction(systemQuery),
                SystemIdGenerateEventMessage.class);
        MessageQueue.registerAction(new ImageDeployedEventMessageAction(systemQuery),
                ImageDeployedEventMessage.class);
        MessageQueue.registerAction(new LibvirtEngineDomainLifecycleMessageAction(virtManager),
                LibvirtEngineDomainLifecycleMessage.class);
        MessageQueue.registerAction(new BatchStartedEventMessageAction(),
                BatchStartedEventMessage.class);

        MessageQueue.publish(new RefreshGeneratedSaltFilesEventMessage());

        connectToEventStream();
    }

    /**
     * Stop the salt reactor.
     */
    public void stop() {
        isStopped = true;
        if (eventStream != null) {
            eventStream.removeEventListener(listener);
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
        listener = new PGEventListener(this::eventStreamClosed, this::eventToMessages);
        eventStream = saltApi.getEventStream();
        eventStream.addEventListener(listener);
    }

    private Stream<EventMessage> eventToMessages(Event event) {
        // Setup handlers for different event types
        return MinionStartEvent.parse(event).map(this::eventToMessages).orElseGet(() ->
               JobReturnEvent.parse(event).map(this::eventToMessages).orElseGet(() ->
               BatchStartedEvent.parse(event).map(this::eventToMessages).orElseGet(() ->
               SystemIdGenerateEvent.parse(event).map(this::eventToMessages).orElseGet(() ->
               ImageDeployedEvent.parse(event).map(this::eventToMessages).orElseGet(() ->
               EngineEvent.parse(event).map(this::eventToMessages).orElseGet(() ->
               BeaconEvent.parse(event).map(this::eventToMessages).orElse(
               empty()
        )))))));
    }

    /**
     * Trigger handling of batch started events.
     *
     * @param batchStartedEvent the batch started event as we get it from salt
     * @return event handler runnable
     */
    private Stream<EventMessage> eventToMessages(BatchStartedEvent batchStartedEvent) {
        return of(new BatchStartedEventMessage(batchStartedEvent));
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
    private Stream<EventMessage> eventToMessages(ImageDeployedEvent imageDeployedEvent) {
        return of(new ImageDeployedEventMessage(imageDeployedEvent));
    }

    /**
     * Trigger registration on minion start events.
     *
     * @param minionStartEvent minion start event
     * @return event handler runnable
     */
    private Stream<EventMessage> eventToMessages(MinionStartEvent minionStartEvent) {
        String minionId = (String) minionStartEvent.getData().get("id");
        Optional<MinionStartupGrains> startupGrains = minionStartEvent.getStartUpGrains(MinionStartupGrains.class);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Trigger start and registration for minion: " + minionId);
        }
        return of(
            new MinionStartEventMessage(minionId),
            new RegisterMinionEventMessage(minionId, startupGrains)
        );
    }

    /**
     * Trigger handling of engine events
     *
     * @param engineEvent engine event
     * @return event handler runnable
     */
    private Stream<EventMessage> eventToMessages(EngineEvent engineEvent) {
        if ("libvirt_events".equals(engineEvent.getEngine())) {
            try {
                AbstractLibvirtEngineMessage message = AbstractLibvirtEngineMessage.create(engineEvent);
                if (message != null) {
                    return of(message);
                }
                else {
                    LOG.debug("Unhandled libvirt engine event:" +
                              engineEvent.getAdditional());
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
    private Stream<EventMessage> eventToMessages(JobReturnEvent jobReturnEvent) {
        return of(new JobReturnEventMessage(jobReturnEvent));
    }

    /**
     * Trigger handling of beacon events
     *
     * @param beaconEvent beacon event
     * @return event handler runnable
     */
    private Stream<EventMessage> eventToMessages(BeaconEvent beaconEvent) {
        if (beaconEvent.getBeacon().equals("pkgset") && beaconEvent.getAdditional().equals("changed")) {
            return of(
                    new RunnableEventMessage("ZypperEvent.PackageSetChanged", () -> {
                        MinionServerFactory.findByMinionId(beaconEvent.getMinionId()).ifPresent(minionServer -> {
                            try {
                                ActionManager.schedulePackageRefresh(minionServer.getOrg(), minionServer);
                            }
                            catch (TaskomaticApiException e) {
                                LOG.error("Could not schedule package refresh for minion: " +
                                        minionServer.getMinionId());
                                LOG.error(e);
                            }
                        });
                    })
            );
        }
        if (beaconEvent.getBeacon().equals("virtpoller")) {
            return of(new VirtpollerBeaconEventMessage(
                beaconEvent.getMinionId(),
                beaconEvent.getData(VirtpollerData.class)
            ));
        }
        return empty();
    }

}
