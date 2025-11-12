/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.common.messaging;

import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.frontend.events.AlignSoftwareTargetAction;
import com.redhat.rhn.frontend.events.AlignSoftwareTargetMsg;
import com.redhat.rhn.frontend.events.CloneErrataAction;
import com.redhat.rhn.frontend.events.CloneErrataEvent;
import com.redhat.rhn.frontend.events.NewCloneErrataAction;
import com.redhat.rhn.frontend.events.NewCloneErrataEvent;
import com.redhat.rhn.frontend.events.NewUserAction;
import com.redhat.rhn.frontend.events.NewUserEvent;
import com.redhat.rhn.frontend.events.RestartSatelliteAction;
import com.redhat.rhn.frontend.events.RestartSatelliteEvent;
import com.redhat.rhn.frontend.events.ScheduleRepoSyncAction;
import com.redhat.rhn.frontend.events.ScheduleRepoSyncEvent;
import com.redhat.rhn.frontend.events.SsmChangeBaseChannelSubscriptionsAction;
import com.redhat.rhn.frontend.events.SsmChangeBaseChannelSubscriptionsEvent;
import com.redhat.rhn.frontend.events.SsmChangeChannelSubscriptionsAction;
import com.redhat.rhn.frontend.events.SsmChangeChannelSubscriptionsEvent;
import com.redhat.rhn.frontend.events.SsmConfigFilesAction;
import com.redhat.rhn.frontend.events.SsmConfigFilesEvent;
import com.redhat.rhn.frontend.events.SsmDeleteServersAction;
import com.redhat.rhn.frontend.events.SsmDeleteServersEvent;
import com.redhat.rhn.frontend.events.SsmErrataAction;
import com.redhat.rhn.frontend.events.SsmErrataEvent;
import com.redhat.rhn.frontend.events.SsmInstallPackagesAction;
import com.redhat.rhn.frontend.events.SsmInstallPackagesEvent;
import com.redhat.rhn.frontend.events.SsmPowerManagementAction;
import com.redhat.rhn.frontend.events.SsmPowerManagementEvent;
import com.redhat.rhn.frontend.events.SsmRemovePackagesAction;
import com.redhat.rhn.frontend.events.SsmRemovePackagesEvent;
import com.redhat.rhn.frontend.events.SsmSystemRebootAction;
import com.redhat.rhn.frontend.events.SsmSystemRebootEvent;
import com.redhat.rhn.frontend.events.SsmUpgradePackagesAction;
import com.redhat.rhn.frontend.events.SsmUpgradePackagesEvent;
import com.redhat.rhn.frontend.events.SsmVerifyPackagesAction;
import com.redhat.rhn.frontend.events.SsmVerifyPackagesEvent;
import com.redhat.rhn.frontend.events.TraceBackAction;
import com.redhat.rhn.frontend.events.TraceBackEvent;
import com.redhat.rhn.frontend.events.UpdateErrataCacheAction;
import com.redhat.rhn.frontend.events.UpdateErrataCacheEvent;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.reactor.messaging.ChannelsChangedEventMessage;
import com.suse.manager.reactor.messaging.ChannelsChangedEventMessageAction;
import com.suse.manager.webui.services.iface.SaltApi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * A class that passes messages from the sender to an action class
 */
public class MessageQueue {
    private static final Logger LOGGER = LogManager.getLogger(MessageQueue.class);
    private static final Map<Class<? extends EventMessage>, List<MessageAction>> ACTIONS = new HashMap<>();
    private static final BlockingQueue<Runnable> MESSAGE_QUEUE = new LinkedBlockingQueue<>();
    private static MessageDispatcher dispatcher = null;
    private static int messageCount;

    /**
     * Util class so we don't have a usable constructor
     */
    private MessageQueue() {
    }

    /**
     * Publish a new message
     * Each message is wrapped in a ActionExecutor instance
     * @param msg EventMessage to publish to queue.
     */
    public static void publish(EventMessage msg) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("publish(EventMessage) - start: {}", msg.getClass().getName());
        }
        if (!isMessaging()) {
            startMessaging();
        }
        if (msg != null) {
            synchronized (ACTIONS) {
                List<MessageAction> handlers = ACTIONS.get(msg.getClass());
                if (handlers != null && !handlers.isEmpty()) {
                    LOGGER.debug("creating ActionExecutor");
                    ActionExecutor executor = new ActionExecutor(handlers, msg);
                    try {
                        MESSAGE_QUEUE.put(executor);
                        messageCount++;
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        LOGGER.error(e.getMessage(), e);
                    }
                }
                else {
                    LOGGER.debug("handlers is null, not processing!");
                }
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("publish(EventMessage) - end");
        }
    }

    /**
     * Returns {@link MessageAction}s corresponding to an {@link EventMessage}.
     * @param message message
     * @return actions
     */
    public static Stream<MessageAction> getActionsFor(EventMessage message) {
        return ACTIONS.get(message.getClass()).stream();
    }

    static ActionExecutor popEventMessage() throws InterruptedException {
        ActionExecutor retval = (ActionExecutor) MESSAGE_QUEUE.poll(500, TimeUnit.MILLISECONDS);
        if (retval != null) {
            synchronized (ACTIONS) {
                messageCount--;
            }
        }
        return retval;
    }

    /**
     * Start the messaging system
     */
    public static synchronized void startMessaging() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("startMessaging() - start");
        }
        if (isMessaging()) {
            return;
        }
        dispatcher = new MessageDispatcher();
        Thread dispatcherThread = new Thread(dispatcher);
        dispatcherThread.setName("RHN Message Dispatcher");
        dispatcherThread.setDaemon(false);
        dispatcherThread.start();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("startMessaging() - end");
        }
    }

    /**
     * Stop the messaging system
     */
    public static synchronized void stopMessaging() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("stopMessaging() - start");
        }
        dispatcher.stop();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("stopMessaging() - end");
        }
    }

    /**
     * Get the number of messages in the queue
     * @return int number of messages in queue.
     */
    public static int getMessageCount() {
        return messageCount;
    }

    /**
     * Register an action
     * @param act MessageAction
     * @param eventType type of event.
     */
    public static void registerAction(MessageAction act, Class<? extends EventMessage> eventType) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("registerAction(MessageAction, Class) - : {} class: {}", act, eventType.getName());
        }
        synchronized (ACTIONS) {
            List<MessageAction> handlers = ACTIONS.computeIfAbsent(eventType, k -> new ArrayList<>());
            handlers.add(act);
        }
    }

    /**
     * De-register an action
     * @param act MessageAction.
     * @param eventType Type of event.
     */
    public static void deRegisterAction(MessageAction act, Class<? extends EventMessage> eventType) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("deRegisterAction(MessageAction, Class) - start");
        }
        synchronized (ACTIONS) {
            List<MessageAction> handlers = ACTIONS.get(eventType);
            handlers.remove(act);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("deRegisterAction(MessageAction, Class) - end");
        }
    }

    /**
     * Get list of String Classnames of the registered Actions.  For Managment
     * of the MessageQueue and testability.
     * @return String[] array of registered events.
     */
    public static String[] getRegisteredEventNames() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("getRegisteredEventNames() - start");
        }
        String[] retval = null;
        synchronized (ACTIONS) {
            if (!ACTIONS.keySet().isEmpty()) {
                retval = new String[ACTIONS.keySet().size()];
                int index = 0;
                for (Class<? extends EventMessage> klazz : ACTIONS.keySet()) {
                    retval[index] = klazz.getName();
                    index++;
                }
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("getRegisteredEventNames() - end - null");
        }
        return retval;
    }

    /**
     * Check to see if the MessageQueue is running and available to
     * publish MessageEvents to
     * @return boolean true if MessageQueue is running.
     */
    public static boolean isMessaging() {
        return (dispatcher != null && !dispatcher.isStopped());
    }


    /**
     * Configures default messaging actions needed by RHN
     * This method should be called directly after <code>startMessaging</code>.
     *
     * @param saltApi Salt Api instance to use
     */
    public static void configureDefaultActions(SaltApi saltApi) {
        // Register the Actions for the Events
        // If we develop a large set of MessageEvents we may want to
        // refactor this block out into a class or method that
        // reads in some configuration from an XML file somewhere
        MessageQueue.registerAction(new TraceBackAction(), TraceBackEvent.class);
        MessageQueue.registerAction(new NewUserAction(), NewUserEvent.class);

        // this is to update the errata cache without blocking the login
        // for 40 seconds.
        MessageQueue.registerAction(new UpdateErrataCacheAction(),
                                    UpdateErrataCacheEvent.class);

        // Used for asynchronusly restarting the satellite
        MessageQueue.registerAction(new RestartSatelliteAction(),
                                    RestartSatelliteEvent.class);

        // Used to allow SSM channel changes to be run asynchronously
        MessageQueue.registerAction(new SsmChangeBaseChannelSubscriptionsAction(),
                SsmChangeBaseChannelSubscriptionsEvent.class);
        MessageQueue.registerAction(new SsmChangeChannelSubscriptionsAction(),
                                    SsmChangeChannelSubscriptionsEvent.class);

        SystemManager systemManager = new SystemManager(ServerFactory.SINGLETON, ServerGroupFactory.SINGLETON, saltApi);
        MessageQueue.registerAction(new SsmDeleteServersAction(systemManager),
                                    SsmDeleteServersEvent.class);

        // Used to allow SSM package installs to be run asynchronously
        MessageQueue.registerAction(new SsmInstallPackagesAction(),
                                    SsmInstallPackagesEvent.class);
        MessageQueue.registerAction(new SsmRemovePackagesAction(),
                                    SsmRemovePackagesEvent.class);
        MessageQueue.registerAction(new SsmVerifyPackagesAction(),
                                    SsmVerifyPackagesEvent.class);
        MessageQueue.registerAction(new SsmUpgradePackagesAction(),
                                    SsmUpgradePackagesEvent.class);

        // Used to allow SSM power management actions to be run asynchronously
        MessageQueue.registerAction(new SsmPowerManagementAction(),
            SsmPowerManagementEvent.class);

        //Clone Errata into a channel
        MessageQueue.registerAction(new CloneErrataAction(),
                                    CloneErrataEvent.class);
        MessageQueue.registerAction(new NewCloneErrataAction(),
                                    NewCloneErrataEvent.class);
        MessageQueue.registerAction(new SsmErrataAction(),
                                    SsmErrataEvent.class);

        // Copy SW source contents to an Environment target
        MessageQueue.registerAction(new AlignSoftwareTargetAction(),
                                    AlignSoftwareTargetMsg.class);

        // Asynchronously schedule immediate repo sync
        MessageQueue.registerAction(new ScheduleRepoSyncAction(),
                ScheduleRepoSyncEvent.class);

        // Misc
        MessageQueue.registerAction(new SsmSystemRebootAction(),
                                    SsmSystemRebootEvent.class);

        // Deploy configuration files
        MessageQueue.registerAction(new SsmConfigFilesAction(),
                                    SsmConfigFilesEvent.class);

        // Handle changes of channel assignments on minions
        MessageQueue.registerAction(new ChannelsChangedEventMessageAction(saltApi),
                ChannelsChangedEventMessage.class);
    }
}
