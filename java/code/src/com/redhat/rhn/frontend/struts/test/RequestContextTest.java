/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.struts.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.testing.MockObjectTestCase;
import com.redhat.rhn.testing.RhnMockHttpServletRequest;
import com.redhat.rhn.testing.RhnMockHttpServletResponse;
import com.redhat.rhn.testing.RhnMockHttpSession;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.webui.controllers.login.LoginController;
import com.suse.manager.webui.utils.LoginHelper;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.HashMap;

import spark.ModelAndView;
import spark.RequestResponseFactory;
import spark.Response;
import spark.routematch.RouteMatch;

/**
 * RequestContextTest
 */
public class RequestContextTest extends MockObjectTestCase {

    /**
     */
    @Test
    public final void testGetLoggedInUser() {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "false");

        final String requestUrl = "http://localhost:8080/rhn/manager/login";
        final RouteMatch match = new RouteMatch(new Object(), requestUrl, requestUrl, "");
        final RhnMockHttpServletRequest mockRequest = new RhnMockHttpServletRequest();
        RhnMockHttpSession session = new RhnMockHttpSession();
        mockRequest.setSession(session);

        mockRequest.setRequestURL(requestUrl);
        mockRequest.setMethod("POST");
        mockRequest.setMethod("POST");
        mockRequest.setPathInfo(URI.create(requestUrl).getPath());
        mockRequest.addParameter("url_bounce", "/rhn/users/UserDetails.do?uid=1");

        Response response = RequestResponseFactory.create(new RhnMockHttpServletResponse());
        // logging in
        LoginHelper.successfulLogin(mockRequest, response.raw(), UserTestUtils.findNewUser("testUser", "testOrg" +
                this.getClass().getSimpleName()));
        ModelAndView result = LoginController.loginView(RequestResponseFactory.create(match, mockRequest), response);
        HashMap<String, String> model = (HashMap<String, String>) result.getModel();
        assertNotNull(session.getAttribute("webUserID"));
        assertEquals(model.get("url_bounce"), "/rhn/users/UserDetails.do?uid=1");
        RequestContext requestContext = new RequestContext(RequestResponseFactory.create(match, mockRequest).raw());
        assertNotNull(requestContext.getCurrentUser());
    }

    /**
     * Tests the pagination: First.
    */
    /*public void testProcessPaginationFirst() {
        MockHttpServletRequest request =
            new MockHttpServletRequest();
        request.setupAddParameter("First", "1");
        request.setupAddParameter("first_lower", "1");
        RequestContext requestContext = new RequestContext(request);
        String rc = requestContext.processPagination();
        assertEquals("1", rc);
    }*/

    /**
     * Tests the pagination: Prev.
     */
    /*public void testProcessPaginationPrev() {
        MockHttpServletRequest request =
            new MockHttpServletRequest();
        request.setupAddParameter("First", (String) null);
        request.setupAddParameter("Prev", "1");
        request.setupAddParameter("prev_lower", "10");
        RequestContext requestContext = new RequestContext(request);
        String rc = requestContext.processPagination();
        assertEquals("10", rc);
    }*/

    /**
     * Tests the pagination: Last.
     */
    /*public void testProcessPaginationLast() {
        MockHttpServletRequest request =
            new MockHttpServletRequest();
        request.setupAddParameter("First", (String)null);
        request.setupAddParameter("Prev", (String)null);
        request.setupAddParameter("Next", (String)null);
        request.setupAddParameter("Last", "1");
        request.setupAddParameter("last_lower", "30");
        RequestContext requestContext = new RequestContext(request);
        String rc = requestContext.processPagination();
        assertEquals("30", rc);
    }*/

    /**
     * Tests the pagination: Next.
     */
    /*public void testProcessPaginationNext() {
        MockHttpServletRequest request =
            new MockHttpServletRequest();
        request.setupAddParameter("First", (String)null);
        request.setupAddParameter("Prev", (String)null);
        request.setupAddParameter("Next", "1");
        request.setupAddParameter("next_lower", "20");
        RequestContext requestContext = new RequestContext(request);
        String rc = requestContext.processPagination();
        assertEquals("20", rc);
    }*/

    /**
     */
    @Test
    public void testbuildPageLink() {
        RhnMockHttpServletRequest request = new RhnMockHttpServletRequest();
        request.addParameter("someparam", "value");
        request.setQueryString("otherparam=foo&barparam=beer");
        request.addAttribute("requestedUri", "http://localhost/rhn/somePage.do");

        RequestContext requestContext = new RequestContext(request);

        String url = requestContext.buildPageLink("someparam", "value");
        assertEquals("http://localhost/rhn/somePage.do?" +
                "someparam=value&otherparam=foo&barparam=beer", url);
        request.setQueryString("otherparam=foo&barparam=beer&someparam=value");
        url = requestContext.buildPageLink("someparam", "zzzzz");

        assertEquals("http://localhost/rhn/somePage.do?" +
                "barparam=beer&otherparam=foo&someparam=zzzzz", url);
    }

}
