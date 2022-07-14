/*
 * Copyright (c) 2017 SUSE LLC
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

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.notification.UserNotificationFactory;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * WebSocket EndPoint for showing notifications real-time in web UI.
 * NOTE: there's an EndPoint instance for each WebSocket session
 */
@ServerEndpoint(value = "/websocket/notifications", configurator = WebsocketSessionConfigurator.class)
public class Notification {

    private static final String WEB_USER_ID = "webUserID";

    public static final String USER_NOTIFICATIONS = "user-notifications";
    public static final String SSM_COUNT = "ssm-count";

    // Logger for this class
    private static final Logger LOG = LogManager.getLogger(Notification.class);

    private static final Object LOCK = new Object();
    private static final Gson GSON = new GsonBuilder().create();
    private static Map<Session, Set<String>> wsSessions = new HashMap<>();
    private static Set<Session> brokenSessions = new HashSet<>();
    private static final WebsocketHeartbeatService HEARTBEAT_SERVICE = GlobalInstanceHolder.WEBSOCKET_SESSION_MANAGER;

    /**
     * Callback executed when the WebSocket is opened.
     * @param session the WebSocket session
     * @param config the EndPoint config
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        if (session != null) {
            Optional.ofNullable(session.getUserProperties().get(WEB_USER_ID))
                    .map(webUserID -> (Long) webUserID)
                    .ifPresentOrElse(userId -> {
                        LOG.debug(String.format("Hooked a new websocket session [id:%s]", session.getId()));
                        handshakeSession(session);

                    },
                    ()-> LOG.debug("no authenticated user."));
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
        LOG.debug("Closing web socket session");
        handbreakSession(session);
    }

    /**
     * Callback executed when a message is received.
     * @param session the WebSocket session
     * @param messageBody the message as string
     */
    @OnMessage
    public void onMessage(Session session, String messageBody) {
        // Each session sends messages to tell us what action ID they need to monitor
        Set<String> watched = wsSessions.get(session);
        if (watched != null) {
            Optional<User> userOpt = Optional.ofNullable(session.getUserProperties().get(WEB_USER_ID))
                    .map(webUserID -> UserFactory.lookupById((Long) webUserID));
            userOpt.ifPresentOrElse(user -> {
                        try {
                            Set<String> request = GSON.fromJson(messageBody,
                                    new TypeToken<Set<String>>() { }.getType());
                            watched.addAll(request);

                            // Send the data
                            sendData(session, user, request);
                        }
                        catch (JsonSyntaxException e) {
                            LOG.error(String.format("Received invalid request: [message:%s]", messageBody));
                        }
                    },
                    () -> LOG.debug("no authenticated user.")
                );
        }
        else {
            LOG.debug(String.format("Session not registered or broken: [id:%s]", session.getId()));
        }
    }

    /**
     * Callback executed an error occurs.
     * @param session the WebSocket session
     * @param err the err that occurred
     */
    @OnError
    public void onError(Session session, Throwable err) {
        boolean didClientAbortedConnection = err instanceof EOFException ||
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
     * Must be synchronized. Sending messages concurrently from separate threads
     * will result in IllegalStateException.
     *
     * @param session the WebSocket session
     * @param message the message to be sent
     */
    public static void sendMessage(Session session, String message) {
        synchronized (LOCK) {
            try {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(message);
                }
                else {
                    LOG.debug(String.format("Could not send websocket message. Session [id:%s] is already closed.",
                            session.getId()));
                    handbreakSession(session);
                }
            }
            catch (IOException e) {
                LOG.debug(String.format("Could not send websocket message. Session [id:%s] is already closed.",
                        session.getId()));
                handbreakSession(session);
            }
        }
    }

    /**
     * A static method to notify all {@link Session}s attached to WebSocket from the outside
     * Must be synchronized. Sending messages concurrently from separate threads
     * will result in IllegalStateException.
     *
     * @param property which property to spread to all sessions
     */
    public static void spreadUpdate(String property) {
        // Check for closed sessions before notifying them
        clearBrokenSessions();

        synchronized (LOCK) {
            // if there are unread messages, notify it to all attached WebSocket sessions
            wsSessions.forEach((session, watched) -> {
                if (watched.contains(property)) {
                    Optional.ofNullable(session.getUserProperties().get(WEB_USER_ID))
                            .map(webUserID -> UserFactory.lookupById((Long) webUserID))
                            .ifPresent(user -> sendData(session, user, Set.of(property)));
                }
            });
        }
    }

    private static void sendData(Session session, User user, Set<String> properties) {
        Map<String, BiFunction<Session, User, Object>> preparers = Map.of(
                USER_NOTIFICATIONS, Notification::prepareUserNotifications,
                SSM_COUNT, Notification::prepareSsmCount
        );
        try {
            Map<String, Object> data = properties.stream()
                    .filter(preparers::containsKey)
                    .collect(Collectors.toMap(Function.identity(),
                            property -> preparers.get(property).apply(session, user)));
            if (!data.isEmpty()) {
                sendMessage(session, GSON.toJson(data));
            }
        }
        finally {
            HibernateFactory.closeSession();
        }
    }

    private static Object prepareUserNotifications(Session session, User user) {
        return UserNotificationFactory.unreadUserNotificationsSize(user);
    }

    private static Object prepareSsmCount(Session session, User user) {
        RhnSet systemSet = RhnSetDecl.SYSTEMS.lookup(user);
        return systemSet != null ? systemSet.size() : 0;
    }

    /**
     * A static method to clean up all invalid sessions
     */
    public static void clearBrokenSessions() {
        synchronized (LOCK) {
            // look for closed sessions in the valid set
            wsSessions.forEach((s, u) -> {
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
        HEARTBEAT_SERVICE.register(session);
        synchronized (LOCK) {
            wsSessions.put(session, new HashSet<>());
        }
    }

    /**
     * Add a WebSocket Session to the broken collection to clean them up
     * @param session the session to remove
     */
    private static void handbreakSession(Session session) {
        HEARTBEAT_SERVICE.unregister(session);
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
                spreadUpdate(USER_NOTIFICATIONS);
            }
            catch (Exception e) {
                LOG.error("Notification scheduledExecutorService exception", e);
                // clear WebSocket connections
                clearBrokenSessions();
            }
            finally {
                HibernateFactory.closeSession();
            }
        }, 30, 30, TimeUnit.SECONDS);
    }
}
