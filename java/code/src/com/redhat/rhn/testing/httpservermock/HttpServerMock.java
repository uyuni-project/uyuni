/**
 * Copyright (c) 2014 SUSE LLC
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
package com.redhat.rhn.testing.httpservermock;

import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.concurrent.Callable;

import simple.http.Request;
import simple.http.connect.Connection;
import simple.http.connect.ConnectionFactory;

/**
 * Mocks a Web server allowing HTTP client classes to be tested.
 */
public class HttpServerMock {

    /**
     * Port this mock server will listen to.
     */
    private final int port;

    /**
     * Instantiates a new http server mock.
     */
    public HttpServerMock() {
        /*
         * Try hard not to conflict with other testsuites, taking into account
         * that current version of Simple HTTP requires some seconds to kill a
         * server instance and there is no straightforward way to "kill" one.
         */
        port = (int) (
                10000 +
                (System.currentTimeMillis() % 100) * 100 +
                new Random().nextInt(100));
    }

    /**
     * Gets an URI object to connect to this server.
     *
     * @return the uri
     */
    public URI getURI() {
        URI result = null;
        try {
            result = new URI("http://localhost:" + port);
        }
        catch (URISyntaxException e) {
            // never happens
        }
        return result;
    }

    /**
     * Run an HTTP requester on this mock server, record the request and return
     * it.
     *
     * @param requester a method that will produce an HTTP request to this
     * server (wrapped in a Callable object)
     * @return an object that encapsulates the HTTP request as received by this
     * server
     * @throws Exception if network errors arise
     */
    public Request getRequest(Callable<?> requester) throws Exception {
        return runExchange(requester, new NotImplementedResponder()).request;
    }

    /**
     * Run an HTTP requester on this mock server, respond with responder, record
     * the requester's result and return it.
     * @param <T> type of the requester result
     *
     * @param requester a method that will produce an HTTP request to this
     * server (wrapped in a Callable object)
     * @param responder an object that can respond to the HTTP request
     * @return the requester's result
     * @throws Exception if requester throws an exception or network errors
     * arise
     */
    public <T> T getResult(Callable<T> requester, Responder responder) throws Exception {
        return runExchange(requester, responder).result;
    }

    /**
     * Run an HTTP request-response pair on this mock server.
     * @param <T> type of the requester result
     *
     * @param requester a method that will produce an HTTP request to this
     * server (wrapped in a Callable object). Can return a convenience result
     * that will be returned in ExchangeDetails
     * @param responder an object that can respond to the HTTP request
     * @param ignoreRequesterExceptions if true, ignore exceptions thrown by the
     * requester object
     * @return an object that encapsulates the HTTP request as received by this
     * server and the requester result
     * @throws Exception if requester throws an exception or network errors
     * arise
     */
    private <T> ExchangeDetails<T> runExchange(Callable<T> requester, Responder responder)
        throws Exception {
        ServiceMock service = new ServiceMock(responder);
        EngineMock engine = new EngineMock(service);

        Connection connection = null;
        ServerSocket socket = null;
        ExchangeDetails<T> exchangeDetails = new ExchangeDetails<T>();
        try {
            connection = ConnectionFactory.getConnection(engine);
            socket = new ServerSocket(port);
            connection.connect(socket);

            try {
                exchangeDetails.result = requester.call();
            }
            catch (Exception e) {
                throw e;
            }
        }
        finally {
            if (socket != null) {
                socket.close();
            }
        }

        exchangeDetails.request = service.getRequest();
        return exchangeDetails;
    }

    /**
     * Details of a request-response pair.
     * @param <T> result type
     */
    class ExchangeDetails<T> {

        /** An HTTP request, as received by the HTTP server. */
        private Request request = null;

        /** A generic result produced by an HTTP request. */
        private T result = null;
    }
}
