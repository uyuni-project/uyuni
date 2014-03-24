/**
 * Copyright (c) 2011-2013 SUSE
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.redhat.rhn.testing.httpservermock;

import java.net.ServerSocket;
import java.util.concurrent.Callable;

import simple.http.Request;
import simple.http.connect.Connection;
import simple.http.connect.ConnectionFactory;

/**
 * Mocks a Web server allowing HTTP client classes to be tested.
 * 
 * @param <T>
 */
public class HttpServerMock {

    /**
     * Port the mock server will listen to.
     */
    public static int PORT = 12345;

    /**
     * Run an HTTP requester on this mock server, record the request and return it.
     * 
     * @param requester a method that will produce an HTTP request to this server (wrapped in a Callable object)
     * @return an object that encapsulates the HTTP request as received by this server
     * @throws Exception if network errors arise
     */
    public Request getRequest(Callable<?> requester) throws Exception {
        return runExchange(requester, new NotImplementedResponder()).request;
    }

    /**
     * Run an HTTP requester on this mock server, respond with responder, record the requester's result and return it.
     * 
     * @param requester a method that will produce an HTTP request to this server (wrapped in a Callable object)
     * @param responder an object that can respond to the HTTP request
     * @return the requester's result
     * @throws Exception if requester throws an exception or network errors arise
     */
    public <T> T getResult(Callable<T> requester, Responder responder) throws Exception {
        return runExchange(requester, responder).result;
    }

    /**
     * Run an HTTP request-response pair on this mock server.
     *
     * @param requester a method that will produce an HTTP request to this server (wrapped in a Callable object). Can
     *            return a convenience result that will be returned in ExchangeDetails
     * @param responder an object that can respond to the HTTP request
     * @param <T> type of the requester result
     * @param ignoreRequesterExceptions if true, ignore exceptions thrown by the requester object
     * @return an object that encapsulates the HTTP request as received by this server and the requester result
     * @throws Exception if requester throws an exception or network errors arise
     */
    private <T> ExchangeDetails<T> runExchange(Callable<T> requester,
            Responder responder) throws Exception {

        //LoaderEngine engine = new LoaderEngine();
        //engine.load("ServiceMock", "com.redhat.rhn.testing.httpservermock.ServiceMock", responder);
        //engine.link("*", "ServiceMock");
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
            } catch (Exception e) {
                throw e;
            }
        } finally {
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
        Request request = null;

        /** A generic result produced by an HTTP request. */
        T result = null;
    }
}