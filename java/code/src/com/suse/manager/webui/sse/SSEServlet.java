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

package com.suse.manager.webui.sse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suse.manager.webui.events.Event;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * SSE endpoint for forwarding events to connected browsers.
 */
@WebServlet(urlPatterns = {"/sse"}, asyncSupported = true)
public class SSEServlet extends HttpServlet {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(SSEServlet.class);

    // JSON serializer
    private static final Gson GSON = new GsonBuilder().create();

    // Pool of currently connected clients
    private static final Queue<AsyncContext> CONNECTIONS = new ConcurrentLinkedDeque<>();

    /**
     * Send an event to all currently connected clients.
     *
     * @param event the event to be forwarded to the clients
     */
    public static void sendEvent(Event event) {
        Iterator<AsyncContext> iterator = CONNECTIONS.iterator();
        iterator.forEachRemaining(connection -> {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Sending event on connection: " + connection);
                }
                PrintWriter out = connection.getResponse().getWriter();
                out.append("data: ");
                out.append(GSON.toJson(event));
                out.append("\n\n");
                out.flush();
            }
            catch (IllegalStateException | IOException e) {
                LOG.error("Removing connection: " + e.getMessage());
                iterator.remove();
            }
        });
    }

    /**
     * For every incoming request create an {@link AsyncContext} representing a
     * connection to a client.
     *
     * @param request the request object
     * @param response the response object
     * @throws ServletException if the request for the GET could not be handled
     * @throws IOException if an I/O error is detected when the request is handled
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LOG.debug("SSE event-stream requested");

        // Set content type
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");

        // Start async context
        final AsyncContext asyncContext = request.startAsync();
        asyncContext.setTimeout(0);
        CONNECTIONS.add(asyncContext);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Number of connections: " + CONNECTIONS.size());
        }

        asyncContext.addListener(new AsyncListener() {
            @Override
            public void onComplete(AsyncEvent event) throws IOException {
                LOG.debug("SSE complete");
                CONNECTIONS.remove(asyncContext);
            }

            @Override
            public void onTimeout(AsyncEvent event) throws IOException {
                LOG.debug("SSE timeout");
                CONNECTIONS.remove(asyncContext);
            }

            @Override
            public void onError(AsyncEvent event) throws IOException {
                LOG.debug("SSE error");
                CONNECTIONS.remove(asyncContext);
            }

            @Override
            public void onStartAsync(AsyncEvent event) throws IOException {
                LOG.debug("SSE start async");
            }
        });
    }
}
