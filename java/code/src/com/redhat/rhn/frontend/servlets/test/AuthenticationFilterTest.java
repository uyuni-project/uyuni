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
package com.redhat.rhn.frontend.servlets.test;

import com.redhat.rhn.frontend.security.AuthenticationService;
import com.redhat.rhn.frontend.servlets.AuthenticationFilter;
import com.redhat.rhn.testing.MockObjectTestCase;

import org.jmock.Expectations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Vector;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * AuthFilterTest
 */
public class AuthenticationFilterTest extends MockObjectTestCase {

    private class AuthenticationFilterStub extends AuthenticationFilter {
        @Override
        public void setAuthenticationService(AuthenticationService service) {
            super.setAuthenticationService(service);
        }
    }

    private AuthenticationFilterStub filter;

    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private FilterChain mockFilterChain;
    private RequestDispatcher mockDispatcher;

    private AuthenticationService mockAuthService;

    @BeforeEach
    public void setUp() {
        filter = new AuthenticationFilterStub();

        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockFilterChain = mock(FilterChain.class);
        mockAuthService = mock(AuthenticationService.class);
        mockDispatcher = mock(RequestDispatcher.class);

        setDefaultStubs();
    }

    private void setDefaultStubs() {
        context().checking(new Expectations() { {
            allowing(mockRequest).getRequestURI();
            will(returnValue("/rhn/YourRhn.do"));
            allowing(mockRequest).getRequestDispatcher(with(any(String.class)));
            will(returnValue(mockDispatcher));
            allowing(mockRequest).getHeaders(with(any(String.class)));
            returnValue(new Vector<String>().elements());
            allowing(mockRequest).getRemoteAddr();
            returnValue("aaa.bbb.ccc.ddd");
            allowing(mockRequest).getMethod();
            will(returnValue("GET"));
            allowing(mockRequest).getContentType();
            will(returnValue(null));
            allowing(mockRequest).getAttribute("session");
            will(returnValue(null));
            allowing(mockRequest).setAttribute(
                    with(any(String.class)),
                    with(any(Object.class)));
            allowing(mockRequest).getCookies();
            will(returnValue(null));
            allowing(mockRequest).getServletPath();
            will(returnValue("/YourRhn.do"));
            allowing(mockRequest).getHeader(with(any(String.class)));
            will(returnValue(null));
        } });

        filter.setAuthenticationService(mockAuthService);
    }

    private HttpServletRequest getRequest() {
        return mockRequest;
    }

    private HttpServletResponse getResponse() {
        return mockResponse;
    }

    private FilterChain getFilterChain() {
        return mockFilterChain;
    }

    @Test
    public final void testDoFilterWhenAuthenticationSucceeds() throws Exception {
        context().checking(new Expectations() { {
            allowing(mockRequest).setAttribute(with("session"),
                    with(aNull(String.class)));
            atLeast(1).of(mockAuthService).validate(with(any(HttpServletRequest.class)),
                    with(any(HttpServletResponse.class)));
            will(returnValue(true));
            oneOf(mockFilterChain).doFilter(with(any(HttpServletRequest.class)),
                    with(any(HttpServletResponse.class)));
            oneOf(mockAuthService).refresh(with(any(HttpServletRequest.class)),
                    with(any(HttpServletResponse.class)));
        } });

        filter.doFilter(getRequest(), getResponse(), getFilterChain());
    }

    @Test
    public final void testDoFilterWhenAuthenticationFails() throws Exception {
        context().checking(new Expectations() { {
            atLeast(1).of(mockAuthService).validate(with(any(HttpServletRequest.class)),
                    with(any(HttpServletResponse.class)));
            will(returnValue(false));
            oneOf(mockAuthService).redirectToLogin(with(any(HttpServletRequest.class)),
                    with(any(HttpServletResponse.class)));
        } });

        filter.doFilter(getRequest(), getResponse(), getFilterChain());
    }
}
