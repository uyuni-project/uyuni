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
import com.redhat.rhn.domain.notification.NotificationMessageFactory;

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
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Websocket endpoint for showing notifications real-time in web UI.
 * NOTE: there's an endpoint instance for each websocket session
 */
@ServerEndpoint(value = "/websocket/notifications")
public class Notification {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(Notification.class);

    private static List<Session> wsSessions = new LinkedList<>();

    /**
     * Callback executed when the websocket is opened.
     * @param session the websocket session
     * @param config the endpoint config
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        if (session == null) {
            try {
                LOG.debug("No web sessionId available. Closing the web socket.");
                session.close();
            }
            catch (IOException e) {
                LOG.debug("Error closing web socket session", e);
            }
        }
        LOG.debug("Hooked a new websocket session {id = " + session.getId() + "}");
        wsSessions.add(session);

        // update the notification counter to the unread messages
        sendMessage(session, NotificationMessageFactory.unreadMessagesSizeToString());
    }

    /**
     * Callback executed when the websocket is closed.
     * @param sessios the closed websocket {@link Session}
     */
    @OnClose
    public void onClose(Session session) {
        LOG.debug("Closing web socket session");
        wsSessions.remove(session);
    }

    /**
     * Callback executed when a message is received.
     * @param session the websocket session
     * @param messageBody the message as string
     */
    @OnMessage
    public void onMessage(Session session, String messageBody) {
        try {
            // notify all attached sessions
            for (Session s : wsSessions) {
                sendMessage(s, messageBody);
            }
        }
        catch (Exception e) {
            String message = "Error receiving a notification message";
            LOG.error(message, e);
            sendMessage(session, message);
        }
        finally {
            HibernateFactory.closeSession();
        }
    }

    /**
     * Must be synchronized. Sending messages concurrently from separate threads
     * will result in IllegalStateException.
     */
    public static void sendMessage(Session session, String message) {
        synchronized (session) {
            try {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(message);
                }
                else {
                    LOG.debug("Could not send websocket message. Session is closed.");
                    wsSessions.remove(session);
                }
            }
            catch (IOException e) {
                LOG.error("Error sending websocket message", e);
            }
        }
    }

    /**
     * A static method to notify all {@link Session}s attached to websocket from the outside
     * @param message
     */
    public static void notifyAll(String message) {
        // notify all open WebSocket sessions
        wsSessions = wsSessions.stream().filter(ws -> ws.isOpen()).collect(Collectors.toList());
        LOG.info("Notifying " + wsSessions.size() + " websocket sessions");

        for (Session ws : wsSessions) {
            try {
                ws.getBasicRemote().sendText(message);
            }
            catch (IOException e) {
                LOG.error("Error sending message to websocket { id : " + ws.getId() + "}", e);
            }
        }
    }

    /**
     * Callback executed an error occurs.
     * @param session the websocket session
     * @param err the err that occurred
     */
    @OnError
    public void onError(Session session, Throwable err) {
        wsSessions.remove(session);
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
}
