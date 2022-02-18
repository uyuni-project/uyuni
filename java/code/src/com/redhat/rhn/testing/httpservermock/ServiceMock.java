/*
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

import simple.http.Request;
import simple.http.Response;
import simple.http.load.Service;
import simple.http.serve.FileContext;

//I wish I had... Java with closures
/**
 * A Simple framework container that captures a request and runs a Responder.
 */
public class ServiceMock extends Service {

    /**
     * The responder.
     */
    private final Responder responder;

    /**
     * A captured request, as received by ContainerServer.
     */
    private Request request;

    /**
     * Instantiate a new container mock.
     *
     * @param responderIn the responder
     */
    public ServiceMock(Responder responderIn) {
        super(new FileContext());
        this.responder = responderIn;
        this.request = null;
    }

    /**
     * Get the request.
     *
     * @return the request
     */
    public Request getRequest() {
        return request;
    }

    /**
     * @param requestIn Request
     * @param response Response
     * @throws Exception
     * @see simple.http.load.Service#process(simple.http.Request, simple.http.Response)
     */
    @Override
    protected void process(Request requestIn, Response response) throws Exception {
        this.request = requestIn;
        responder.respond(requestIn, response);
    }
}
