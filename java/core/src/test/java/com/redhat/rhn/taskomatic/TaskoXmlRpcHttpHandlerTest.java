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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

import org.cobbler.XmlRpcException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import redstone.xmlrpc.XmlRpcServer;

class TaskoXmlRpcHttpHandlerTest {

    @Test
    @DisplayName("Sends 200 and the correct response")
    void canSend200AndCorrectResponse() {
        XmlRpcServer server = new StubXmlRpcServer("<ok/>");
        RecordingExchange exchange = new RecordingExchange();

        TaskoXmlRpcHttpHandler handler = new TaskoXmlRpcHttpHandler(server);
        handler.handle(exchange);

        assertEquals(200, exchange.getStatusCode());
        assertEquals("text/xml; charset=utf-8", exchange.getResponseHeaders().getFirst("Content-Type"));
        assertArrayEquals("<ok/>".getBytes(StandardCharsets.UTF_8), exchange.getWrittenBody());
        assertTrue(exchange.isClosed());
    }

    @Test
    @DisplayName("Sends 500 and empty body when the failure happens before committing the response")
    void canSend500BeforeResponseIsCommitted() {
        XmlRpcServer server = new StubXmlRpcServer(new XmlRpcException("boom"));
        RecordingExchange exchange = new RecordingExchange();

        TaskoXmlRpcHttpHandler handler = new TaskoXmlRpcHttpHandler(server);
        handler.handle(exchange);

        assertEquals(500, exchange.getStatusCode());
        assertTrue(exchange.isClosed());
    }

    @Test
    @DisplayName("Does not attempt to send 500 if the response has been already committed")
    void doesNotAttempt500IfResponseIsCommitted() {
        XmlRpcServer server = new StubXmlRpcServer("<ok/>");
        RecordingExchange exchange = new RecordingExchange(new FailingOutputStream());

        TaskoXmlRpcHttpHandler handler = new TaskoXmlRpcHttpHandler(server);
        handler.handle(exchange);

        assertEquals(200, exchange.getStatusCode());
        assertTrue(exchange.isClosed());
    }

    // An XmlRpcServer implementation that can either write the provided payload or throw an exception
    private static class StubXmlRpcServer extends XmlRpcServer {

        private final String payload;

        private final XmlRpcException exceptionToThrow;

        StubXmlRpcServer(String payloadIn) {
            payload = payloadIn;
            exceptionToThrow = null;
        }

        StubXmlRpcServer(XmlRpcException exceptionToThrowIn) {
            payload = "";
            exceptionToThrow = exceptionToThrowIn;
        }

        @Override
        public void execute(InputStream xmlInput, Writer output) throws XmlRpcException {
            if (exceptionToThrow != null) {
                throw exceptionToThrow;
            }

            try {
                output.write(payload);
                output.flush();
            }
            catch (IOException ex) {
                fail("Unexpected exception while writing the payload");
            }
        }
    }

    // An output stream that fails on writing data
    private static class FailingOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {
            throw new IOException("Cannot write response");
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            throw new IOException("Cannot write response");
        }
    }

    // An observable HttpExchange
    private static class RecordingExchange extends HttpExchange {
        private final Headers responseHeaders;
        private final InputStream requestBody;
        private final OutputStream responseBody;

        private int statusCode;

        private boolean closed;

        RecordingExchange() {
            this(new ByteArrayOutputStream());
        }


        RecordingExchange(OutputStream responseBodyIn) {
            responseHeaders = new Headers();
            requestBody = new ByteArrayInputStream(new byte[0]);
            responseBody = responseBodyIn;

            statusCode = -1;
            closed = false;
        }

        @Override
        public Headers getRequestHeaders() {
            return fail("getRequestHeaders() should not be called by the handler in tests.");
        }

        @Override
        public Headers getResponseHeaders() {
            return responseHeaders;
        }

        @Override
        public URI getRequestURI() {
            return fail("getRequestURI() should not be called by the handler in tests.");
        }

        @Override
        public String getRequestMethod() {
            return fail("getRequestMethod() should not be called by the handler in tests.");
        }

        @Override
        public HttpContext getHttpContext() {
            return fail("getHttpContext() should not be called by the handler in tests.");
        }

        @Override
        public void close() {
            closed = true;
            try {
                requestBody.close();
                responseBody.close();
            }
            catch (IOException ex) {
                // Close error are irrelevant to the handler logic and can be ignored
            }
        }

        @Override
        public InputStream getRequestBody() {
            return requestBody;
        }

        @Override
        public OutputStream getResponseBody() {
            return responseBody;
        }

        @Override
        public void sendResponseHeaders(int code, long responseLength) {
            statusCode = code;
        }

        @Override
        public InetSocketAddress getRemoteAddress() {
            return new InetSocketAddress("127.0.0.1", 12345);
        }

        @Override
        public int getResponseCode() {
            return statusCode;
        }

        @Override
        public InetSocketAddress getLocalAddress() {
            return fail("getLocalAddress() should not be called by the handler in tests.");
        }

        @Override
        public String getProtocol() {
            return fail("getProtocol() should not be called by the handler in tests.");
        }

        @Override
        public Object getAttribute(String name) {
            return fail("getAttribute() should not be called by the handler in tests.");
        }

        @Override
        public void setAttribute(String name, Object value) {
            fail("setAttribute() should not be called by the handler in tests.");
        }

        @Override
        public void setStreams(InputStream i, OutputStream o) {
            fail("setStreams() should not be called by the handler in tests.");
        }

        @Override
        public HttpPrincipal getPrincipal() {
            return fail("getPrincipal() should not be called by the handler in tests.");
        }

        int getStatusCode() {
            return statusCode;
        }

        boolean isClosed() {
            return closed;
        }

        byte[] getWrittenBody() {
            if (!(responseBody instanceof ByteArrayOutputStream out)) {
                return new byte[0];
            }
            return out.toByteArray();
        }
    }
 }
