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

import com.redhat.rhn.domain.notification.NotificationMessageFactory;

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
import java.util.stream.Collectors;

/**
 * WebSocket EndPoint for showing notifications real-time in web UI.
 * NOTE: there's an EndPoint instance for each WebSocket session
 */
@ServerEndpoint(value = "/websocket/notifications", configurator = WebsocketSessionConfigurator.class)
public class Notification {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(Notification.class);

    private final static Object lock = new Object();
    private static Map<Session, User> wsSessions = new HashMap<>();

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
                sendMessage(session, String.valueOf(NotificationMessageFactory.unreadMessagesSize(user)));
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
        notifyAll(messageBody);
    }

    /**
     * Callback executed an error occurs.
     * @param session the WebSocket session
     * @param err the err that occurred
     */
    @OnError
    public void onError(Session session, Throwable err) {
        handbreakSession(session);
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
            }
        }
    }

    /**
     * A static method to notify all {@link Session}s attached to WebSocket from the outside
     * Must be synchronized. Sending messages concurrently from separate threads
     * will result in IllegalStateException.
     *
     * @param message the message to be sent
     */
    public static void notifyAll(String message) {
        // notify all open WebSocket sessions
        refreshOpenSessions();
        LOG.info(String.format("Notifying %s websocket sessions", wsSessions.size()));

        synchronized (lock) {
            wsSessions.forEach((session, user) -> {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    LOG.error(String.format("Error sending message to websocket [id:%s]", session.getId()), e);
                }
            });
        }
    }

    /**
     * Add a new WebSocket Session to the collection
     * @param session the session to add
     */
    private static void handshakeSession(User user, Session session) {
        synchronized (lock) {
            wsSessions.put(session, user);
        }
    }

    /**
     * Remove a WebSocket Session from the collection
     * @param session the session to remove
     */
    private static void handbreakSession(Session session) {
        synchronized (lock) {
            wsSessions.remove(session);
        }
    }

    /**
     * Keep only open WebSocket Session
     */
    private static void refreshOpenSessions() {
        synchronized (lock) {
            wsSessions = wsSessions.entrySet().stream().filter(ws -> ws.getKey().isOpen()).collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue
            ));
        }
    }

    static {
        Thread notificationThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    synchronized (lock) {
                        // if there are unread messages, notify it to all attached WebSocket sessions
                        wsSessions.forEach((session, user) -> {
                            int unreadCount = NotificationMessageFactory.unreadMessagesSize(user);
                            sendMessage(session, String.valueOf(unreadCount));
                        });
                    }
                    try {
                        // check every 10 second
                        sleep(10000);
                    }
                    catch (InterruptedException e) {
                    }
                }
            };
        };
        notificationThread.start();
    }
}
