/**
 * Copyright (c) 2018 SUSE LLC
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
package com.suse.manager.webui.controllers.test;

import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.RhnMockHttpServletResponse;

import com.suse.manager.webui.utils.SparkTestUtils;

import org.jmock.lib.legacy.ClassImposteriser;

import java.io.UnsupportedEncodingException;
import java.util.Collections;

import spark.Request;
import spark.RequestResponseFactory;
import spark.Response;

/**
 * Base test case to be used for spark controllers. It provides convenience functions
 * to generate test requests.
 */
public class BaseControllerTestCase extends JMockBaseTestCaseWithUser {

    protected Response response;
    private final String baseUri = "http://localhost:8080/rhn";

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        setImposteriser(ClassImposteriser.INSTANCE);
        response = RequestResponseFactory.create(new RhnMockHttpServletResponse());
    }


    /**
     * Creates a request with csrf token.
     *
     * @param uri the uri
     * @param vars the vars
     * @return the request with csrf
     */
    protected Request getRequestWithCsrf(String uri, Object... vars) {
        Request request = SparkTestUtils.createMockRequest(baseUri + uri, vars);
        request.session(true).attribute("csrf_token", "bleh");
        return request;
    }

    /**
     * Creates a request with csrf token.
     *
     * @param uri the uri
     * @param vars the vars
     * @return the request with csrf
     */
    protected Request getPostRequestWithCsrfAndBody(String uri, String body,
                                                  Object... vars) throws UnsupportedEncodingException {
        Request request = SparkTestUtils.createMockRequestWithBody(baseUri + uri, Collections.emptyMap(), body, vars);
        request.session(true).attribute("csrf_token", "bleh");
        return request;
    }
}
