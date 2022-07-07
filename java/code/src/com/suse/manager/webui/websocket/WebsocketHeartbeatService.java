/*
 * Copyright (c) 2022 SUSE LLC
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.websocket.Session;

/**
 * Service that sends periodic pings to all the registered websocket connections, making sure the connections are kept
 * alive.
 *
 * <p>A 'ping' frame is a special websocket frame that has the opcode <code>0x9</code>. Clients must respond to this
 * ping with a 'pong' frame which has the opcode <code>0x10</code>. Most browsers do this transparently. Therefore, no
 * implementation on the client side is required.
 *
 * <p>The ping prevents the browsers from closing the connection due to inactivity. As the server, we're not explicitly
 * tracking the corresponding pongs, because the server configuration keeps the sessions open as long as the underlying
 * TCP connections are open.
 *
 * <p>If your websocket client endpoint closes the connections unexpectedly after being idle for some time, consider
 * registering your sessions with this service.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6455#section-5.5.2">RFC-6455 (sections 5.5.2, 5.5.3)</a>
 */
public class WebsocketHeartbeatService {
    private final Logger LOG = LogManager.getLogger(WebsocketHeartbeatService.class);
    private final Set<Session> wsSessions = ConcurrentHashMap.newKeySet();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

    /**
     * Constructs a new {@link WebsocketHeartbeatService} instance
     */
    public WebsocketHeartbeatService() {
        initScheduler();
    }

    /**
     * Registers a {@link Session} to the heartbeat service
     * @param session the websocket session
     */
    public void register(Session session) {
        wsSessions.add(session);
        LOG.debug("The websocket session (" + session.getId() + ") is registered for pings");
    }

    /**
     * Unregisters a {@link Session} from the heartbeat service
     *
     * The corresponding endpoint won't be pinged anymore.
     * @param session the websocket session
     */
    public void unregister(Session session) {
        wsSessions.remove(session);
        LOG.debug("The websocket session (" + session.getId() + ") is unregistered and won't be pinged anymore");
    }

    private void clearStaleSessions() {
        Set<Session> staleSessions = new HashSet<>();
        wsSessions.forEach(session -> {
            if (!session.isOpen()) {
                staleSessions.add(session);
            }
        });

        if (!staleSessions.isEmpty()) {
            LOG.debug(staleSessions.size() + " sessions are marked as 'stale'");
            staleSessions.forEach(this::unregister);
        }
    }

    /**
     * Pings all the registered {@link Session}s
     */
    private void sendPings() {
        AtomicLong openSessions = new AtomicLong(0);
        wsSessions.forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendPing(ByteBuffer.wrap("PING".getBytes(StandardCharsets.UTF_8)));
                    openSessions.incrementAndGet();
                }
                catch (IOException eIn) {
                    LOG.error("Failed to ping session (" + session.getId() + ")", eIn);
                }
            }
        });

        if (openSessions.get() > 0) {
            LOG.debug(openSessions.get() + " open sessions pinged");
        }
    }

    /**
     * Initializes the scheduling service for periodic pings
     */
    private void initScheduler() {
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            clearStaleSessions();
            sendPings();
        }, 30, 30, TimeUnit.SECONDS);
    }
}
