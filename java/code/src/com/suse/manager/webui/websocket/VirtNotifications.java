/*
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
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.webui.controllers.ECMAScriptDateAdapter;
import com.suse.manager.webui.websocket.json.VirtNotificationMessage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

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
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .create();

    private static final Object LOCK = new Object();
    private static Map<Session, Set<VirtNotificationMessage>> wsSessions = new HashMap<>();
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
        Set<VirtNotificationMessage> serverIds = wsSessions.get(session);
        if (serverIds != null) {
            Optional<User> userOpt = Optional.ofNullable(session.getUserProperties().get("webUserID"))
                    .map(webUserID -> UserFactory.lookupById((Long) webUserID));
            if (userOpt.isPresent()) {
                try {
                    VirtNotificationMessage request = GSON.fromJson(messageBody, VirtNotificationMessage.class);
                    serverIds.add(request);

                    // Send out a list of the latest pending actions to display
                    if (request.getGuestUuid().isEmpty() && request.getSid().isPresent()) {
                        Server server = SystemManager.lookupByIdAndUser(request.getSid().get(), userOpt.get());
                        List<ServerAction> serverActions = ActionFactory.listServerActionsForServer(server,
                                Arrays.asList(ActionFactory.STATUS_QUEUED, ActionFactory.STATUS_PICKED_UP));

                        Map<String, List<ServerAction>> groupedActions = serverActions.stream()
                                .filter(sa -> ActionFactory.isVirtualizationActionType(
                                        sa.getParentAction().getActionType()))
                                .collect(Collectors.toMap(sa -> sa.getParentAction().getWebSocketActionId(),
                                        Arrays::asList,
                                        (sa1, sa2) -> {
                                            List<ServerAction> merged = new ArrayList<>(sa1);
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
                                                    data.put("type", sa.getParentAction().getActionType().getLabel());
                                                    data.put("name", sa.getParentAction().getName());
                                                    return data;
                                                }).get()));

                        sendMessage(session, GSON.toJson(latestActions));

                    }
                }
                catch (JsonSyntaxException e) {
                    LOG.error(String.format("Received invalid request: [message:%s]", messageBody));
                }
            }
            else {
                LOG.debug("no authenticated user.");
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
            // Notify sessions waiting for this action
            wsSessions.forEach((session, requests) -> {
                List<Long> servers = requests.stream()
                        .filter(req -> req.getGuestUuid().isEmpty() && req.getSid().isPresent())
                        .map(req -> req.getSid().get())
                        .collect(Collectors.toList());
                Optional<ServerAction> serverAction = action.getServerActions().stream()
                        .filter(sa -> servers.contains(sa.getServerId()))
                        .findFirst();

                serverAction.ifPresent(sa -> {
                    Map<String, Object> actions = new HashMap<>();
                    actions.put("id", action.getId());
                    actions.put("status", sa.getStatus().getName());
                    actions.put("type", sa.getParentAction().getActionType().getLabel());
                    actions.put("name", sa.getParentAction().getName());

                    Map<String, Object> msg = new HashMap<>();
                    msg.put(action.getWebSocketActionId(), actions);

                    sendMessage(session, GSON.toJson(msg));
                });
            });
        }
    }

    /**
     * A static method to notify all {@link Session}s attached to WebSocket that a refresh is needed.
     * Must be synchronized. Sending messages concurrently from separate threads
     * will result in IllegalStateException.
     *
     * @param kind the kind of object list that needs refresh. One of "guest", "pool"
     */
    public static void spreadRefresh(String kind) {
        synchronized (LOCK) {
            // Notify sessions waiting for this action
            wsSessions.forEach((session, requests) -> {
                if (requests.stream().anyMatch(req -> req.getGuestUuid().isEmpty())) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("refresh", kind);
                    sendMessage(session, GSON.toJson(data));
                }
            });
        }
    }

    /**
     * A static method to notify all {@link Session}s attached to WebSocket from the outside
     * Must be synchronized. Sending messages concurrently from separate threads
     * will result in IllegalStateException.
     *
     * @param sid the server ID of the virtual host where the VM is located
     * @param uuid the UUID of the VM
     * @param event the libvirt event
     * @param detail the libvirt event detail
     */
    public static void spreadGuestEvent(Long sid, String uuid, String event, String detail) {
        synchronized (LOCK) {
            // Notify sessions waiting for this action
            wsSessions.forEach((session, requests) -> {
                if (requests.stream()
                        .anyMatch(req -> req.getSid().isEmpty() && uuid.equals(req.getGuestUuid().orElse(null)))) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("hostId", sid);
                    data.put("event", event);
                    data.put("detail", detail);
                    sendMessage(session, GSON.toJson(data));
                }
            });
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
