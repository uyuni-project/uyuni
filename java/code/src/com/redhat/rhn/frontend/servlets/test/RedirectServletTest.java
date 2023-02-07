/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.frontend.servlets.test;

import com.redhat.rhn.frontend.security.RedirectServlet;
import com.redhat.rhn.testing.MockObjectTestCase;

import org.jmock.Expectations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * RedirectServletTest
 */
public class RedirectServletTest extends MockObjectTestCase {

    private class RedirectServletStub extends RedirectServlet {
        @Override
        public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

            super.doGet(request, response);
        }
    }

    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;

    private RedirectServletStub redirect;

    private String serverName;
    private String requestURI;
    private String redirectURI;

    @BeforeEach
    public void setUp() {
        redirect = new RedirectServletStub();

        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);

        requestURI = "/rhn/Redirect/foo/bar";
        redirectURI = "/foo/bar";
        serverName = "somehost.redhat.com";

        context().checking(new Expectations() { {
            allowing(mockRequest).getServerName();
            will(returnValue(serverName));
            allowing(mockRequest).getScheme();
            will(returnValue("https"));
            allowing(mockRequest).getRequestURI();
            will(returnValue(redirectURI));
            allowing(mockRequest).getRequestURL();
            will(returnValue(new StringBuffer("https://" + serverName + requestURI)));
        } });
    }

    private HttpServletRequest getRequest() {
        return mockRequest;
    }

    private HttpServletResponse getResponse() {
        return mockResponse;
    }

    @Test
    public final void testDoGet() throws Exception {
        context().checking(new Expectations() { {
           oneOf(mockResponse).sendRedirect("https://" + serverName + redirectURI);
           allowing(mockRequest).getQueryString();
           will(returnValue(null));
        } });

        redirect.doGet(getRequest(), getResponse());
    }

    @Test
    public final void testDoGetWithQueryString() throws Exception {
        final String queryString = encode("myparam") + "=" + encode("neo is the one!");

        context().checking(new Expectations() { {
                allowing(mockRequest).getQueryString();
                will(returnValue(queryString));
                oneOf(mockResponse).sendRedirect(
                        "https://" + serverName + redirectURI + "?" + queryString);
        } });

        redirect.doGet(getRequest(), getResponse());
    }

    private String encode(String string) {
        return URLEncoder.encode(string, StandardCharsets.UTF_8);
    }
}
