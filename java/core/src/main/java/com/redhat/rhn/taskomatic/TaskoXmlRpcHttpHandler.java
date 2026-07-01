/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.taskomatic;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import redstone.xmlrpc.XmlRpcServer;

/**
 * Bridges HTTP requests from {@link com.sun.net.httpserver.HttpServer} to the XML-RPC engine.
 */
public class TaskoXmlRpcHttpHandler implements HttpHandler {

    private static final Logger LOG = LogManager.getLogger(TaskoXmlRpcHttpHandler.class);

    private final XmlRpcServer server;

    /**
     * Create a new instance
     * @param serverIn the {@link XmlRpcServer} instance
     */
    public TaskoXmlRpcHttpHandler(XmlRpcServer serverIn) {
        this.server = serverIn;
    }

    /**
     * Executes one XML-RPC request and writes the XML response using UTF-8.
     *
     * @param exchange the HTTP exchange to process
     * @throws IOException when request or response I/O fails
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        ) {
            server.execute(in, writer);
            writer.flush();

            byte[] payload = outputStream.toByteArray();
            exchange.getResponseHeaders().set("Content-Type", "text/xml; charset=utf-8");
            exchange.sendResponseHeaders(200, payload.length);
            exchange.getResponseBody().write(payload);
        }
        catch (Exception ex) {
            LOG.error("Failed request by {}", exchange.getRemoteAddress(), ex);
            exchange.sendResponseHeaders(500, -1);
        }
        finally {
            exchange.close();
        }
    }
}
