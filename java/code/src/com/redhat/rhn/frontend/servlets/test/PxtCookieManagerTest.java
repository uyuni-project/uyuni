/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.frontend.servlets.PxtCookieManager;
import com.redhat.rhn.manager.session.SessionManager;
import com.redhat.rhn.testing.MockObjectTestCase;

import org.jmock.Expectations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;


/**
 * PxtCookieManagerTest
 */
public class PxtCookieManagerTest extends MockObjectTestCase {

    private static final int TIMEOUT = 3600;

    private HttpServletRequest mockRequest;

    private PxtCookieManager manager;

    private String host;

    private String domain;

    private Long pxtSessionId;

    private HttpServletRequest getRequest() {
        return mockRequest;
    }

    @BeforeEach
    public void setUp() throws Exception {
        manager = new PxtCookieManager();

        pxtSessionId = 2658447890L;

        mockRequest = mock(HttpServletRequest.class);

        host = "somehost";
        domain = "redhat.com";

        context().checking(new Expectations() { {
            allowing(mockRequest).getServerName();
            will(returnValue(host + "." + domain));
            allowing(mockRequest).getHeader(with(any(String.class)));
            will(returnValue(null));
        } });
    }

    @Test
    public final void testGetPxtCookieWhenPxtCookieIsNotPresent() {
        final Cookie[] cookies = new Cookie[] {
                new Cookie("cookie-1", "one"),
                new Cookie("cookie-2", "two")
        };

        context().checking(new Expectations() { {
            allowing(mockRequest).getCookies();
            will(returnValue(cookies));
        } });

        assertNull(manager.getPxtCookie(getRequest()));
    }

    @Test
    public final void testGetPxtCookieWhenNoCookiesPresent() {
        context().checking(new Expectations() { {
            allowing(mockRequest).getCookies();
            will(returnValue(null));
        } });

        assertNull(manager.getPxtCookie(getRequest()));
    }

    @Test
    public final void testGetPxtCookieWhenPxtCookieIsPresentWithoutPxtPersonalities() {
        final Cookie[] cookies = new Cookie[] {
                new Cookie("cookie-1", "one"),
                new Cookie(PxtCookieManager.PXT_SESSION_COOKIE_NAME,
                        "we don't care about the value for this test"),
                new Cookie("cookie-2", "two")
        };

        context().checking(new Expectations() { {
            allowing(mockRequest).getCookies();
            will(returnValue(cookies));
        } });

        assertEquals(cookies[1], manager.getPxtCookie(getRequest()));
    }

    @Test
    public final void testCreatePxtCookieSetsNameWithoutPxtPersonalities() {
        Cookie pxtCookie = manager.createPxtCookie(pxtSessionId, getRequest(), TIMEOUT);

        assertEquals(PxtCookieManager.PXT_SESSION_COOKIE_NAME, pxtCookie.getName());
    }

    @Test
    public final void testCreatePxtCookieSetsValue() {
        String expected = pxtSessionId + "x" +
            SessionManager.generateSessionKey(pxtSessionId.toString());

        Cookie pxtCookie = manager.createPxtCookie(pxtSessionId, getRequest(), TIMEOUT);

        assertEquals(expected, pxtCookie.getValue());
    }

    @Test
    public final void testCreatePxtCookieSetsPath() {
        String expected = "/";

        Cookie pxtCookie = manager.createPxtCookie(pxtSessionId, getRequest(), TIMEOUT);

        assertEquals(expected, pxtCookie.getPath());
    }

    @Test
    public final void testCreatePxtCookieSetsMaxAge() {
        int expected = TIMEOUT;

        Cookie pxtCookie = manager.createPxtCookie(pxtSessionId, getRequest(), TIMEOUT);

        assertEquals(expected, pxtCookie.getMaxAge());
    }

    @Test
    public final void testCreatePxtCookieSetsSecure() {
        Cookie pxtCookie = manager.createPxtCookie(pxtSessionId, getRequest(), TIMEOUT);

        assertEquals(true, pxtCookie.getSecure());
    }

}
