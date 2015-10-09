/**
 * Copyright (c) 2015 SUSE LLC
 * <p>
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 * <p>
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
 * SSE endpoint for running remote commands.
 */
@WebServlet(urlPatterns = {"/sse"}, asyncSupported = true)
public class SSEServlet extends HttpServlet {

    // Logger for this class
    private static Logger logger = Logger.getLogger(SSEServlet.class);
    private static Gson GSON = new GsonBuilder().create();

    private static Queue<AsyncContext> connections = new ConcurrentLinkedDeque<>();

    public static void sendEvent(Event event) {
        Iterator<AsyncContext> iterator = connections.iterator();
        iterator.forEachRemaining(connection -> {
            try {
                logger.debug("Sending event to connection: " + connection);
                PrintWriter out = connection.getResponse().getWriter();
                out.append("data: ");
                out.append(GSON.toJson(event));
                out.append("\n\n");
                out.flush();
            }
            catch (IllegalStateException | IOException e) {
                logger.error("Removing connection: " + e.getMessage());
                iterator.remove();
            }
        });
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        logger.debug("see started");

        // Set content type
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");

        // start async
        final AsyncContext asyncContext = request.startAsync();
        asyncContext.setTimeout(0);
        connections.add(asyncContext);
        asyncContext.addListener(new AsyncListener() {
            @Override
            public void onComplete(AsyncEvent event) throws IOException {
                logger.debug("sse complete");
                connections.remove(asyncContext);
            }

            @Override
            public void onTimeout(AsyncEvent event) throws IOException {
                logger.debug("sse timeout");
                connections.remove(asyncContext);
            }

            @Override
            public void onError(AsyncEvent event) throws IOException {
                logger.debug("sse error");
                connections.remove(asyncContext);
            }

            @Override
            public void onStartAsync(AsyncEvent event) throws IOException {
            }
        });
    }
}
