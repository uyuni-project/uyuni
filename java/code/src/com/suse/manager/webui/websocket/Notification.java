/**
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

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.notification.UserNotificationFactory;

import com.redhat.rhn.domain.user.User;
import org.apache.log4j.Logger;

import javax.websocket.OnOpen;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnError;
import javax.websocket.Session;
import javax.websocket.EndpointConfig;
import javax.websocket.server.ServerEndpoint;
import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket EndPoint for showing notifications real-time in web UI.
 * NOTE: there's an EndPoint instance for each WebSocket session
 */
@ServerEndpoint(value = "/websocket/notifications", configurator = WebsocketSessionConfigurator.class)
public class Notification {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(Notification.class);

    private static final Object LOCK = new Object();
    private static Map<Session, User> wsSessions = new HashMap<>();
    private static Set<Session> brokenSessions = new HashSet<>();

    /**
     * Callback executed when the WebSocket is opened.
     * @param session the WebSocket session
     * @param config the EndPoint config
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        User user = (User)session.getUserProperties().get("currentUser");
        if (session != null) {
            if (user != null) {
                LOG.debug(String.format("Hooked a new websocket session [id:%s]", session.getId()));
                handshakeSession(user, session);

                // update the notification counter to the unread messages
                try {
                    sendMessage(session, String.valueOf(UserNotificationFactory.unreadUserNotificationsSize(user)));
                }
                finally {
                    HibernateFactory.closeSession();
                }
            }
            else {
                LOG.debug("no authenticated user.");
            }
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
        LOG.debug(String.format("Received [message:%s] from session [id:%s].", messageBody, session.getId()));
    }

    /**
     * Callback executed an error occurs.
     * @param session the WebSocket session
     * @param err the err that occurred
     */
    @OnError
    public void onError(Session session, Throwable err) {
        if (err instanceof EOFException) {
            LOG.debug("The client aborted the connection.", err);
        }
        else if (err.getMessage().startsWith("Unexpected error [32]")) {
            // [32] "Broken pipe" is caught when the client side breaks the connection.
            LOG.debug("The client broke the connection.", err);
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
        synchronized (session) {
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
                if (session.isOpen()) {
                    LOG.warn(String.format("Error sending websocket message," +
                                           " despite the Session [id:%s] still being open",
                                            session.getId()),
                            e);
                }
                else {
                    LOG.debug(String.format("Could not send websocket message. Session [id:%s] is already closed.",
                                            session.getId()),
                            e);
                    handbreakSession(session);
                }
            }
        }
    }

    /**
     * A static method to notify all {@link Session}s attached to WebSocket from the outside
     * Must be synchronized. Sending messages concurrently from separate threads
     * will result in IllegalStateException.
     */
    public static void spreadUpdate() {
        synchronized (LOCK) {
            // if there are unread messages, notify it to all attached WebSocket sessions
            wsSessions.forEach((session, user) -> {
                sendMessage(session, String.valueOf(UserNotificationFactory.unreadUserNotificationsSize(user)));
            });
        }
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
    private static void handshakeSession(User user, Session session) {
        synchronized (LOCK) {
            wsSessions.put(session, user);
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
        ScheduledFuture scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                clearBrokenSessions();
                spreadUpdate();
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
