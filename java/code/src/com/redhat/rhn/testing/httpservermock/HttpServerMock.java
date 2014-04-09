/**
 * Copyright (c) 2014 SUSE
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
import java.util.concurrent.Callable;

import simple.http.Request;
import simple.http.connect.Connection;
import simple.http.connect.ConnectionFactory;

/**
 * Mocks a Web server allowing HTTP client classes to be tested.
 */
public class HttpServerMock {

    /**
     * Port the mock server will listen to.
     */
    public static final int PORT = 12345;

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
            socket = new ServerSocket(PORT);
            connection.connect(socket);

            try {
                exchangeDetails.result = requester.call();
            }
            catch (Exception e) {
                throw e;
            }
        }
        finally {
            if (connection != null) {
                socket.close();
            }
        }

        exchangeDetails.request = service.getRequest();
        return exchangeDetails;
    }

    /**
     * Details of a request-response pair.
     */
    class ExchangeDetails<T> {

        /** An HTTP request, as received by the HTTP server. */
        private Request request = null;

        /** A generic result produced by an HTTP request. */
        private T result = null;
    }
}
