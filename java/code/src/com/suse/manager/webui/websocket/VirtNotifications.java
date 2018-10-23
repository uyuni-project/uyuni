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
package com.suse.manager.webui.websocket;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.virtualization.BaseVirtualizationAction;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.SystemManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suse.manager.webui.controllers.ECMAScriptDateAdapter;

import org.apache.log4j.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * Websocket endpoint for acting on virtual machines on Salt minions.
 * NOTE: there's an endpoint instance for each websocket session
 */
@ServerEndpoint(value = "/websocket/minion/virt-notifications", configurator = WebsocketSessionConfigurator.class)
public class VirtNotifications {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(VirtNotifications.class);
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
            .serializeNulls()
            .create();

    private static final Object LOCK = new Object();
    private static Map<Session, Set<Long>> wsSessions = new HashMap<>();
    private static Set<Session> brokenSessions = new HashSet<>();

    /**
     * Callback executed when the websocket is opened.
     * @param session the websocket session
     * @param config the endpoint config
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        if (session != null) {
            handshakeSession(session);
        }
        else {
            LOG.debug("No web sessionId available. Closing the web socket.");
        }
    }

    /**
     * Callback executed when the WebSocket is closed.
     * @param session the closed WebSocket {@link Session}
     */
    @OnClose
    public void onClose(Session session) {
        handbreakSession(session);
    }

    /**
     * Callback executed an error occurs.
     * @param session the websocket session
     * @param err the err that occurred
     */
    @OnError
    public void onError(Session session, Throwable err) {
        Boolean didClientAbortedConnection = err instanceof EOFException ||
                !session.isOpen() ||
                err.getMessage().startsWith("Unexpected error [32]");

        if (didClientAbortedConnection) {
            LOG.debug("The client aborted the connection.", err);
        }
        else {
            LOG.error("Websocket endpoint error", err);
        }
        handbreakSession(session);
    }

    /**
     * Callback executed when a message is received.
     * @param session the websocket session
     * @param messageBody the message as string
     */
    @OnMessage
    @SuppressWarnings("unchecked")
    public void onMessage(Session session, String messageBody) {
        // Each session sends messages to tell us what action ID they need to monitor
        Set<Long> serverIds = wsSessions.get(session);
        if (serverIds != null) {
            Long newId = null;
            try {
                newId = Long.parseLong(messageBody);
                serverIds.add(newId);

                // Send out a list of the latest pending actions to display
                User user = (User)session.getUserProperties().get("currentUser");
                Server server = SystemManager.lookupByIdAndUser(newId, user);
                List<ServerAction> serverActions = ActionFactory.listServerActionsForServer(server,
                        Arrays.asList(ActionFactory.STATUS_QUEUED, ActionFactory.STATUS_PICKEDUP));
                Map<String, List<ServerAction>> groupedActions = serverActions.stream()
                    .filter(sa -> sa.getParentAction() instanceof BaseVirtualizationAction)
                    .collect(Collectors.toMap(sa -> ((BaseVirtualizationAction)sa.getParentAction()).getUuid(),
                                              sa -> Arrays.asList(sa),
                                              (sa1, sa2) -> {
                                                  List<ServerAction> merged = new ArrayList<ServerAction>(sa1);
                                                  merged.addAll(sa2);
                                                  return merged;
                                              }));

                Map<String, Map<String, Object>> latestActions = groupedActions.keySet().stream()
                        .collect(Collectors.toMap(uuid -> uuid,
                                uuid -> groupedActions.get(uuid).stream()
                                    .sorted(Comparator.comparing(ServerAction::getCreated).reversed())
                                    .findFirst().map(sa -> {
                                        Map<String, Object> data = new HashMap<>();
                                        data.put("id", sa.getParentAction().getId());
                                        data.put("status", sa.getStatus().getName());
                                        return data;
                                    }).get()));

                 sendMessage(session, GSON.toJson(latestActions));
            }
            catch (NumberFormatException e) {
                LOG.error(String.format("Received invalid server Id: [message:%s]", messageBody));
            }
        }
        else {
            LOG.debug(String.format("Session not registered or broken: [id:%s]", session.getId()));
        }
    }

    /**
     * Must be synchronized. Sending messages concurrently from separate threads
     * will result in IllegalStateException.
     *
     * @param session the WebSocket session
     * @param message the message to be sent
     */
    public static void sendMessage(Session session, String message) {
        synchronized (session) {
            try {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(message);
                }
                else {
                    LOG.debug(String.format("Could not send websocket message. Session [id:%s] is closed.",
                            session.getId()));
                    handbreakSession(session);
                }
            }
            catch (IOException e) {
                LOG.error("Error sending websocket message", e);
                handbreakSession(session);
            }
        }
    }

    /**
     * A static method to notify all {@link Session}s attached to WebSocket from the outside
     * Must be synchronized. Sending messages concurrently from separate threads
     * will result in IllegalStateException.
     *
     * @param action the action that changed
     */
    public static void spreadActionUpdate(Action action) {
        synchronized (LOCK) {
            if (action instanceof BaseVirtualizationAction) {
                BaseVirtualizationAction virtAction = (BaseVirtualizationAction)action;
                // Notify sessions waiting for this action and stop watching for it
                wsSessions.forEach((session, servers) -> {
                    Optional<ServerAction> serverAction = action.getServerActions().stream()
                            .filter(sa -> servers.contains(sa.getServerId())).findFirst();

                    serverAction.ifPresent(sa -> {
                        Map<String, Object> actions = new HashMap<>();
                        actions.put("id", action.getId());
                        actions.put("status", sa.getStatus().getName());

                        Map<String, Object> msg = new HashMap<>();
                        msg.put(virtAction.getUuid(), actions);

                        sendMessage(session, GSON.toJson(msg));
                    });
                });
            }
        }
    }

    /**
     * A static method to clean up all invalid sessions
     */
    public static void clearBrokenSessions() {
        synchronized (LOCK) {
            // look for closed sessions in the valid set
            wsSessions.forEach((s, a) -> {
                if (!s.isOpen()) {
                    brokenSessions.add(s);
                }
            });

            // remove any invalid/broken session from the valid set
            // try to close it if it is still open
            brokenSessions.forEach(session -> {
                wsSessions.remove(session);
                if (session.isOpen()) {
                    try {
                        session.close();
                    }
                    catch (IOException e) {
                        LOG.error("Error trying to close the session manually", e);
                    }
                }
            });
            brokenSessions.clear();
        }
    }

    /**
     * Add a new WebSocket Session to the collection
     * @param session the session to add
     */
    private static void handshakeSession(Session session) {
        synchronized (LOCK) {
            wsSessions.put(session, new HashSet<>());
        }
    }

    /**
     * Add a WebSocket Session to the broken collection to clean them up
     * @param session the session to remove
     */
    private static void handbreakSession(Session session) {
        synchronized (LOCK) {
            brokenSessions.add(session);
        }
    }

    private static ScheduledExecutorService scheduledExecutorService;
    static {
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                clearBrokenSessions();
            }
            catch (Exception e) {
                LOG.error("VirtNotification scheduledExecutorService exception", e);
                // clear WebSocket connections
                clearBrokenSessions();
            }
            finally {
                HibernateFactory.closeSession();
            }
        }, 30, 30, TimeUnit.SECONDS);
    }
}
